package io.vestoria.controller;

import io.vestoria.dto.request.CreateBuildingRequestDto;
import io.vestoria.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/build")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateBuildingRequestDto request) {
        return buildingService.createBuilding(request);
    }

}
