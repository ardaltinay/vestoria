package io.vestoria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@org.springframework.cache.annotation.EnableCaching
public class VestoriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(VestoriaApplication.class, args);
	}

}
