package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.mapper.TrainingTypeMapper;
import com.epam.gym_crm.repository.TrainingTypeRepository;
import com.epam.gym_crm.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    @Test
    void findByValue_WhenTrainingTypeExists_ReturnsTrainingType() {
        String value = "Cardio";
        TrainingType expectedTrainingType = new TrainingType();
        expectedTrainingType.setTrainingTypeName(value);

        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(value)).thenReturn(Optional.of(expectedTrainingType));

        Optional<TrainingType> result = trainingTypeService.findByValue(value);

        assertTrue(result.isPresent());
        assertEquals(value, result.get().getTrainingTypeName());
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(value);
    }

    @Test
    void findByValue_WhenTrainingTypeDoesNotExist_ReturnsEmptyOptional() {
        String value = "InvalidType";

        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase(value)).thenReturn(Optional.empty());

        Optional<TrainingType> result = trainingTypeService.findByValue(value);

        assertFalse(result.isPresent());
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase(value);
    }

    @Test
    void getAllTrainingTypes_WhenTypesExist_ReturnsListOfDTOs() {

        TrainingType cardio = new TrainingType(1L, "Cardio");
        TrainingType strength = new TrainingType(2L, "Strength");
        List<TrainingType> trainingTypes = Arrays.asList(cardio, strength);

        TrainingTypeResponseDTO cardioDTO = new TrainingTypeResponseDTO(1L, "Cardio");
        TrainingTypeResponseDTO strengthDTO = new TrainingTypeResponseDTO(2L, "Strength");
        List<TrainingTypeResponseDTO> expectedDTOs = Arrays.asList(cardioDTO, strengthDTO);

        when(trainingTypeRepository.findAll()).thenReturn(trainingTypes);
        when(trainingTypeMapper.toTrainingTypeResponseDTO(cardio)).thenReturn(cardioDTO);
        when(trainingTypeMapper.toTrainingTypeResponseDTO(strength)).thenReturn(strengthDTO);

        List<TrainingTypeResponseDTO> result = trainingTypeService.getAllTrainingTypes();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedDTOs, result);

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeMapper).toTrainingTypeResponseDTO(cardio);
        verify(trainingTypeMapper).toTrainingTypeResponseDTO(strength);
    }

    @Test
    void getAllTrainingTypes_WhenNoTypesExist_ReturnsEmptyList() {

        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        List<TrainingTypeResponseDTO> result = trainingTypeService.getAllTrainingTypes();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trainingTypeRepository).findAll();
    }
}