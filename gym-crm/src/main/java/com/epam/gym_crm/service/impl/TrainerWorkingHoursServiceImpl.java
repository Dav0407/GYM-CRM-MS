package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.client.TrainerWorkingHoursClient;
import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import com.epam.gym_crm.dto.response.TrainerWorkloadResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkingHoursServiceImpl {

    private final TrainerWorkingHoursClient client;

    @CircuitBreaker(name = "trainerSessionManagement", fallbackMethod = "getTrainerHoursFallback")
    public TrainerWorkloadResponse getTrainerHours(String trainerUsername, String year, String month) {
        log.info("Calling getTrainerHours for {}", trainerUsername);
        return client.getTrainerHours(trainerUsername, year, month);
    }

    @CircuitBreaker(name = "trainerSessionManagement", fallbackMethod = "computeTrainerHoursFallback")
    public TrainerWorkloadResponse computeTrainerHours(TrainerWorkloadRequest request) {
        log.info("Calling computeTrainerHours for {}", request.getTrainerUsername());
        return client.computeTrainerHours(request);
    }

    public TrainerWorkloadResponse getTrainerHoursFallback(String trainerUsername, String year, String month, Throwable t) {
        log.error("Circuit breaker fallback triggered for getTrainerHours - Username: {}, Year: {}, Month: {}, Error: {}",
                trainerUsername, year, month, t.getMessage());
        return new TrainerWorkloadResponse("Default Username", year, month, 0.0F);
    }

    public TrainerWorkloadResponse computeTrainerHoursFallback(TrainerWorkloadRequest request, Throwable t) {
        log.error("Fallback for computeTrainerHours: {}", t.toString());
        return new TrainerWorkloadResponse(request.getTrainerUsername(), "N/A", "N/A", 0.0F);
    }
}
