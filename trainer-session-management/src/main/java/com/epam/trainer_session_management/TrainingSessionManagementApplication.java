package com.epam.trainer_session_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TrainingSessionManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingSessionManagementApplication.class, args);
    }

}
