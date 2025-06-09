package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.entity.TrainingType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingTypeMapperTest {

    private final TrainingTypeMapper mapper = Mappers.getMapper(TrainingTypeMapper.class);

    @Test
    void toTrainingTypeResponseDTO_ShouldMapCorrectly() {
        // Given
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Yoga");

        // When
        TrainingTypeResponseDTO dto = mapper.toTrainingTypeResponseDTO(trainingType);

        // Then
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Yoga", dto.getTrainingTypeName());
    }

    @Test
    void toTrainingTypeResponseDTO_WithNullInput_ShouldReturnNull() {
        // When
        TrainingTypeResponseDTO dto = mapper.toTrainingTypeResponseDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    void toTrainingTypeResponseDTO_WithPartialData_ShouldMapCorrectly() {
        // Given
        TrainingType trainingType = new TrainingType();
        trainingType.setId(2L);
        // trainingTypeName intentionally left null

        // When
        TrainingTypeResponseDTO dto = mapper.toTrainingTypeResponseDTO(trainingType);

        // Then
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertNull(dto.getTrainingTypeName());
    }
}