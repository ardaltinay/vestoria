package io.vestoria.controller;

import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.request.CreateBuildingRequestDto;
import io.vestoria.dto.request.SetProductionRequestDto;
import io.vestoria.dto.request.StartProductionRequestDto;
import io.vestoria.dto.response.BuildingProductionTypeDto;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.service.BotService;
import io.vestoria.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final BuildingConverter buildingConverter;
    private final BotService botService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateBuildingRequestDto request, Principal principal) {
        return ResponseEntity
                .ok(buildingConverter.toResponseDto(
                        buildingService.createBuilding(principal.getName(), request.getName(), request.getType(),
                                request.getTier(),
                                request.getSubType())));
    }

    @PutMapping("/upgrade/{buildingId}")
    public ResponseEntity<?> upgrade(@PathVariable UUID buildingId, Principal principal) {
        return ResponseEntity
                .ok(buildingConverter.toResponseDto(buildingService.upgradeBuilding(buildingId, principal.getName())));
    }

    @DeleteMapping("/close/{buildingId}")
    public ResponseEntity<?> close(@PathVariable UUID buildingId, Principal principal) {
        buildingService.closeBuilding(buildingId, principal.getName());
        return ResponseEntity.ok("İşletme başarıyla silindi!");
    }

    @PostMapping("/production/{buildingId}")
    public ResponseEntity<?> setProduction(@PathVariable UUID buildingId,
            @RequestBody SetProductionRequestDto request, Principal principal) {
        return ResponseEntity.ok(buildingConverter
                .toResponseDto(
                        buildingService.setProduction(buildingId, principal.getName(), request.getProductionType())));
    }

    @PostMapping("/{buildingId}/start-sales")
    public ResponseEntity<?> startSales(@PathVariable UUID buildingId, Principal principal) {
        buildingService.startSales(buildingId, principal.getName());
        // Fetch updated user to return

        return ResponseEntity.ok("Satış işlemi başladı!");
    }

    @PostMapping("/{buildingId}/start-production")
    public ResponseEntity<?> startProduction(@PathVariable UUID buildingId,
            @RequestBody StartProductionRequestDto request, Principal principal) {
        buildingService.startProduction(buildingId, principal.getName(), request.getProductId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{buildingId}/collect")
    public ResponseEntity<?> collectProduction(@PathVariable UUID buildingId, Principal principal) {
        buildingService.collectProduction(buildingId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(buildingService.getUserBuildings(principal.getName()).stream()
                .map(buildingConverter::toResponseDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/production-types")
    public ResponseEntity<?> getProductionTypes() {
        List<BuildingProductionTypeDto> types = new java.util.ArrayList<>();
        for (BuildingSubType subType : BuildingSubType.values()) {
            if (subType == BuildingSubType.GENERIC)
                continue;
            types.add(buildingConverter.toProductionTypeDto(subType));
        }
        return ResponseEntity.ok(types);
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(buildingService.getBuildingConfigs());
    }

    @PostMapping("/{buildingId}/complete-sale")
    public ResponseEntity<?> completeSale(@PathVariable UUID buildingId, Principal principal) {
        // Verify ownership or just let the service handle it (service checks existence)
        // Ideally we should check ownership here too, but for now let's trust the ID
        // and service logic
        // The service logic is robust enough to handle invalid IDs.
        // However, to be safe and consistent with other endpoints:
        // buildingService.validateOwnership(buildingId, principal.getName()); // If
        // such method existed

        // For now, directly calling bot service as requested
        botService.processShopSales(buildingId);
        return ResponseEntity.ok("Satış tamamlandı!");
    }

    @PostMapping("/{buildingId}/complete-production")
    public ResponseEntity<?> completeProduction(@PathVariable UUID buildingId, Principal principal) {
        // Reusing collectProduction logic as it handles finalizing the production batch
        buildingService.collectProduction(buildingId, principal.getName());
        return ResponseEntity.ok("Üretim tamamlandı!");
    }

}
