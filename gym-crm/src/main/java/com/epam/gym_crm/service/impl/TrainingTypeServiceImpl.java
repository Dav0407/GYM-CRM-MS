package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.mapper.TrainingTypeMapper;
import com.epam.gym_crm.repository.TrainingTypeRepository;
import com.epam.gym_crm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainingTypeMapper trainingTypeMapper;

    @Override
    public Optional<TrainingType> findByValue(String value) {
        log.info("Finding TrainingType by value: {}", value);
        Optional<TrainingType> trainingType = trainingTypeRepository.findByTrainingTypeNameIgnoreCase(value);

        trainingType.ifPresentOrElse(
                type -> log.info("TrainingType found: {}", type),
                () -> log.warn("TrainingType not found for value: {}", value)
        );

        return trainingType;
    }

    @Override
    public List<TrainingTypeResponseDTO> getAllTrainingTypes() {
        log.info("Fetching all TrainingTypes");
        return trainingTypeRepository.findAll().stream()
                .map(trainingTypeMapper::toTrainingTypeResponseDTO)
                .toList();
    }
}
