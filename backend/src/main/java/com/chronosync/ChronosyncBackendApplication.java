package com.chronosync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // ✅ IMPORTANT
public class ChronosyncBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronosyncBackendApplication.class, args);
	}
}

