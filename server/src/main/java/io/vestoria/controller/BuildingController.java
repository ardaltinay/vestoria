package io.vestoria.controller;

import io.vestoria.converter.BuildingConverter;
import io.vestoria.dto.request.CreateBuildingRequestDto;
import io.vestoria.dto.request.SetProductionRequestDto;
import io.vestoria.dto.response.AuthResponseDto;
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

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateBuildingRequestDto request, Principal principal) {
        return ResponseEntity
                .ok(buildingConverter.toResponseDto(
                        buildingService.createBuilding(principal.getName(), request.getType(), request.getTier(),
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
        // Return updated user balance
        UserEntity user = buildingService.getUserRepository().findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return ResponseEntity.ok(AuthResponseDto.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(user.getBalance())
                .level(user.getLevel())
                .xp(user.getXp())
                .isAdmin(user.getIsAdmin())
                .build());
    }

    @PostMapping("/production/{buildingId}")
    public ResponseEntity<?> setProduction(@PathVariable UUID buildingId,
            @RequestBody SetProductionRequestDto request, Principal principal) {
        UserEntity user = buildingService.getUserRepository().findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        return ResponseEntity.ok(buildingConverter
                .toResponseDto(buildingService.setProduction(buildingId, user.getId(), request.getProductionType())));
    }

    @PostMapping("/{buildingId}/start-sales")
    public ResponseEntity<?> startSales(@PathVariable UUID buildingId, Principal principal) {
        buildingService.startSales(buildingId, principal.getName());
        // Fetch updated user to return
        UserEntity user = buildingService.getUserRepository().findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));
        // We need a converter to return AuthResponseDto.
        // BuildingController doesn't have AuthConverter. We can inject it or just
        // return the user entity (not recommended) or a specific DTO.
        // Let's inject AuthConverter.
        return ResponseEntity.ok(AuthResponseDto.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(user.getBalance())
                .level(user.getLevel())
                .xp(user.getXp())
                .isAdmin(user.getIsAdmin())
                .build());
    }

    @PostMapping("/{buildingId}/start-production")
    public ResponseEntity<?> startProduction(@PathVariable UUID buildingId, Principal principal) {
        buildingService.startProduction(buildingId, principal.getName());
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

}
