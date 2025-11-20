package io.vestoria.controller;

import io.vestoria.dto.response.DashboardStatsDto;
import io.vestoria.entity.UserEntity;
import io.vestoria.repository.UserRepository;
import io.vestoria.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserRepository userRepository;

  @GetMapping("/dashboard-stats")
  public ResponseEntity<DashboardStatsDto> getDashboardStats(Principal principal) {
    UserEntity user = userRepository.findByUsername(principal.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    return ResponseEntity.ok(userService.getDashboardStats(user));
  }
}
