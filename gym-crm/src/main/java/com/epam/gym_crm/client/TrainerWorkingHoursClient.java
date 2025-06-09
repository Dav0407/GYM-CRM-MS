package com.epam.gym_crm.client;

import com.epam.gym_crm.config.FeignClientConfig;
import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import com.epam.gym_crm.dto.response.TrainerWorkloadResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "trainerSessionManagement", url = "${application.config.students-url}", configuration = FeignClientConfig.class)
public interface TrainerWorkingHoursClient {

    @PostMapping
    TrainerWorkloadResponse computeTrainerHours(@RequestBody TrainerWorkloadRequest request);

    @GetMapping
    TrainerWorkloadResponse getTrainerHours(@RequestParam String trainerUsername, @RequestParam String year, @RequestParam String month);

}
