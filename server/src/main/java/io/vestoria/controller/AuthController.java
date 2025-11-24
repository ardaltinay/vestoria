package io.vestoria.controller;

import io.vestoria.dto.request.LoginRequestDto;
import io.vestoria.dto.request.RegisterRequestDto;
import io.vestoria.dto.response.AuthResult;
import io.vestoria.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        AuthResult auth = authService.login(request);
        Cookie cookie = new Cookie("accessToken", auth.token());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Prod'da true
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 gün
        response.addCookie(cookie);
        return ResponseEntity.ok(auth.user());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResult auth = authService.register(request);
        return ResponseEntity.ok(auth.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().body("Çıkış işlemi başarılı!");
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
}
