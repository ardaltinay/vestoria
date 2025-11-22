package io.vestoria.controller;

import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.request.CreateBuildingRequestDto;
import io.vestoria.dto.request.SetProductionRequestDto;
import io.vestoria.dto.response.BuildingProductionTypeDto;
import io.vestoria.entity.UserEntity;
import io.vestoria.enums.BuildingSubType;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final BuildingConverter buildingConverter;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateBuildingRequestDto request, Principal principal) {
        return ResponseEntity
                .ok(buildingService.createBuilding(principal.getName(), request.getType(), request.getTier(),
                        request.getSubType()));
    }

    @PostMapping("/upgrade/{buildingId}")
    public ResponseEntity<?> upgrade(@PathVariable UUID buildingId, Principal principal) {
        return ResponseEntity.ok(buildingService.upgradeBuilding(buildingId, principal.getName()));
    }

    @PostMapping("/production/{buildingId}")
    public ResponseEntity<?> setProduction(@PathVariable UUID buildingId,
            @RequestBody SetProductionRequestDto request, Principal principal) {
        UserEntity user = buildingService.getUserRepository().findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return ResponseEntity.ok(buildingService.setProduction(buildingId, user.getId(), request.getProductionType()));
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(buildingService.getUserBuildings(principal.getName()));
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

}
