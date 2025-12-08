package io.vestoria.controller;

import io.vestoria.dto.response.ItemDefinition;
import io.vestoria.service.GameDataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game-data")
@RequiredArgsConstructor
public class GameDataController {

    private final GameDataService gameDataService;

    @GetMapping("/items")
    public ResponseEntity<List<ItemDefinition>> getItems() {
        return ResponseEntity.ok(gameDataService.getAllItems());
    }
}
