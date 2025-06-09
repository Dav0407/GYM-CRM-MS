package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.entity.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    Optional<TrainingType> findByValue(String value);
    List<TrainingTypeResponseDTO> getAllTrainingTypes();
}
