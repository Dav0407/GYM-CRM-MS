package com.epam.trainer_session_management.controller;

import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.service.TrainerWorkingHoursService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/manage-working-hours")
@RequiredArgsConstructor
public class TrainerHoursController {

    private final TrainerWorkingHoursService trainerWorkingHoursService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerWorkloadResponse> saveTrainerHours(@RequestBody TrainerWorkloadRequest request) {
        return ResponseEntity.ok(trainerWorkingHoursService.calculateAndSave(request));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerWorkloadResponse> getTrainerHours(@RequestParam String trainerUsername, @RequestParam String year, @RequestParam String month) {
        return ResponseEntity.ok(trainerWorkingHoursService.getTrainerWorkingHours(trainerUsername, year, month));
    }
}
