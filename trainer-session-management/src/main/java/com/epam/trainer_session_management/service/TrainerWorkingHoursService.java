package com.epam.trainer_session_management.service;

import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;

public interface TrainerWorkingHoursService {

    TrainerWorkloadResponse calculateAndSave(TrainerWorkloadRequest request);

    TrainerWorkloadResponse getTrainerWorkingHours(String trainerUsername, String year, String month);
}
