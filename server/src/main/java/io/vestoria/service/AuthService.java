package io.vestoria.service;

import io.vestoria.converter.AuthConverter;
import io.vestoria.dto.request.LoginRequestDto;
import io.vestoria.dto.request.RegisterRequestDto;
import io.vestoria.dto.response.AuthResponseDto;
import io.vestoria.dto.response.AuthResult;
import io.vestoria.entity.UserEntity;
import io.vestoria.exception.BusinessRuleException;
import io.vestoria.exception.ResourceNotFoundException;
import io.vestoria.repository.UserRepository;
import io.vestoria.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthConverter authConverter;

    public AuthResult login(LoginRequestDto request) {
        UserEntity userEntity = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!StringUtils.hasLength(userEntity.getPassword())
                || !passwordEncoder.matches(request.getPassword(), userEntity.getPassword())) {
            throw new BusinessRuleException("Şifre Yanlış!");
        }

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        if (Boolean.TRUE.equals(userEntity.getIsAdmin())) {
            roles.add("ROLE_ADMIN");
        }

        String token = jwtTokenProvider.generateToken(userEntity.getUsername(), roles);
        AuthResponseDto userDto = authConverter.toAuthDto(userEntity);
        return AuthResult.builder().token(token).user(userDto).build();
    }

    public AuthResult register(RegisterRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername().toUpperCase())) {
            throw new BusinessRuleException("Bu Kullanıcı Adı Kullanılıyor!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Bu Email Adresi Kullanılıyor!");
        }

        UserEntity entity = UserEntity.builder().username(request.getUsername().toUpperCase())
                .password(passwordEncoder.encode(request.getPassword())).email(request.getEmail()).level(1)
                .balance(BigDecimal.valueOf(30000)).xp(1L).isAdmin(false).build();

        @SuppressWarnings("null")
        UserEntity saved = userRepository.save(entity);

        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        String token = jwtTokenProvider.generateToken(saved.getUsername(), roles);
        AuthResponseDto userDto = authConverter.toAuthDto(saved);
        return AuthResult.builder().token(token).user(userDto).build();
    }
}
