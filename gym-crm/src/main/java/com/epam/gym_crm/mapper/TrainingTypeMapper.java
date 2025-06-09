package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.entity.TrainingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {
    TrainingTypeResponseDTO toTrainingTypeResponseDTO(TrainingType trainingType);
}
