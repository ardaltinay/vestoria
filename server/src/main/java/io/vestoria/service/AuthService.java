package io.vestoria.service;

import io.vestoria.converter.AuthConverter;
import io.vestoria.dto.request.LoginRequestDto;
import io.vestoria.dto.request.RegisterRequestDto;
import io.vestoria.dto.response.AuthResult;
import io.vestoria.dto.response.AuthResponseDto;
import io.vestoria.entity.UserEntity;
import io.vestoria.repository.UserRepository;
import io.vestoria.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthConverter authConverter;

    public AuthResult login(LoginRequestDto request) {
        UserEntity userEntity = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (!StringUtils.hasLength(userEntity.getPassword())
                || !passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException("Şifre Yanlış!");
        }

        String token = jwtTokenProvider.generateToken(userEntity.getUsername());
        AuthResponseDto userDto = authConverter.toAuthDto(userEntity);
        return AuthResult.builder().token(token).user(userDto).build();
    }

    public AuthResult register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Bu Kullanıcı Adı Kullanılıyor!");
        }

        if (userRepository.existByEmail(request.getEmail())) {
            throw new RuntimeException("Bu Email Adresi Kullanııyor!");
        }

        UserEntity entity = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .level(1)
                .balance(BigDecimal.valueOf(30000))
                .xp(1L)
                .build();

        UserEntity saved = userRepository.save(entity);
        String token = jwtTokenProvider.generateToken(saved.getUsername());
        AuthResponseDto userDto = authConverter.toAuthDto(saved);
        return AuthResult.builder().token(token).user(userDto).build();
    }
}
