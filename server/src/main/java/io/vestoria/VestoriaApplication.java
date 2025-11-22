package io.vestoria;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.vestoria.entity.UserEntity;
import io.vestoria.repository.UserRepository;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class VestoriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VestoriaApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		return (args) -> {
			if (userRepository.findByUsername("admin").isEmpty()) {
				userRepository.save(UserEntity.builder()
						.username("admin")
						.password(passwordEncoder.encode("admin123"))
						.email("admin@vestoria.io")
						.balance(BigDecimal.valueOf(1000000))
						.level(100)
						.xp(0L)
						.isAdmin(true)
						.build());
			}
		};
	}
}
