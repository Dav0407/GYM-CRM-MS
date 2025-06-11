package com.epam.gym_crm;

import com.epam.gym_crm.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class GymcrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymcrmApplication.class, args);
	}

	@Bean
	CommandLineRunner addPlainPasswords(UserService userService) {
		return args -> {
			userService.addPlainPassword("john.doe", "password123");
			userService.addPlainPassword("jane.smith", "password123");
			userService.addPlainPassword("mike.johnson", "password123");
			userService.addPlainPassword("emma.brown", "password123");
			userService.addPlainPassword("david.williams", "password123");
			userService.addPlainPassword("sarah.miller", "password123");
		};
	}

}

