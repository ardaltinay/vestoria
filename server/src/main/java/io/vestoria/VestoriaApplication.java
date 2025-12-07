package io.vestoria;

import io.vestoria.entity.UserEntity;
import io.vestoria.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class VestoriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(VestoriaApplication.class, args);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Istanbul"));
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return (args) -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                UserEntity admin = UserEntity.builder().username("admin").password(passwordEncoder.encode("admin123"))
                        .email("admin@vestoria.io").balance(BigDecimal.valueOf(999_999_999_999_999L)).level(100).xp(0L)
                        .isAdmin(true).build();
                userRepository.save(admin);
            }
        };
    }
}
