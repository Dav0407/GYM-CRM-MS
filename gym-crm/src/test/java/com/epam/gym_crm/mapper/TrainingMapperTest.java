package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.Training;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingMapperTest {

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainingMapperImpl mapper; // Note: Using the implementation class

    private Training training;
    private Date testDate;

    @BeforeEach
    void setUp() throws Exception {
        testDate = new SimpleDateFormat("yyyy-MM-dd").parse("2023-06-15");

        // Setup minimal Training entity
        training = new Training();
        training.setId(1L);
        training.setTrainingName("Morning Yoga Session");
        training.setTrainingDate(testDate);
        training.setTrainingDuration(60);

        // Setup related entities (just enough for testing)
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga");
        training.setTrainingType(trainingType);

        Trainee trainee = new Trainee();
        training.setTrainee(trainee);

        Trainer trainer = new Trainer();
        training.setTrainer(trainer);
    }

    @Test
    void toTrainingResponseDTO_ShouldMapCorrectly() {
        // Setup mock responses
        TraineeResponseDTO mockTraineeResponse = new TraineeResponseDTO();
        mockTraineeResponse.setUsername("alice.smith");

        TrainerResponseDTO mockTrainerResponse = new TrainerResponseDTO();
        mockTrainerResponse.setUsername("john.doe");

        when(traineeMapper.toTraineeResponseDTO(any(Trainee.class)))
                .thenReturn(mockTraineeResponse);
        when(trainerMapper.toTrainerResponseDTO(any(Trainer.class)))
                .thenReturn(mockTrainerResponse);

        // When
        TrainingResponseDTO dto = mapper.toTrainingResponseDTO(training);

        // Then
        assertNotNull(dto);
        assertEquals("Morning Yoga Session", dto.getTrainingName());
        assertEquals(testDate, dto.getTrainingDate());
        assertEquals(60, dto.getTrainingDuration());
        assertEquals("Yoga", dto.getTrainingType());

        // Verify nested mappers were called
        verify(traineeMapper).toTraineeResponseDTO(training.getTrainee());
        verify(trainerMapper).toTrainerResponseDTO(training.getTrainer());

        // Verify nested DTOs
        assertEquals("alice.smith", dto.getTrainee().getUsername());
        assertEquals("john.doe", dto.getTrainer().getUsername());
    }

    @Test
    void toTraineeTrainingResponseDTO_ShouldMapCorrectly() {
        // Setup trainer user
        User trainerUser = new User();
        trainerUser.setUsername("john.doe");
        training.getTrainer().setUser(trainerUser);

        // When
        TraineeTrainingResponseDTO dto = mapper.toTraineeTrainingResponseDTO(training);

        // Then
        assertNotNull(dto);
        assertEquals("Morning Yoga Session", dto.getTrainingName());
        assertEquals(testDate, dto.getTrainingDate());
        assertEquals(60, dto.getTrainingDuration());
        assertEquals("Yoga", dto.getTrainingType());
        assertEquals("john.doe", dto.getTrainerName());

        // Verify no unnecessary mapper calls
        verifyNoInteractions(traineeMapper, trainerMapper);
    }

    @Test
    void toTrainerTrainingResponseDTO_ShouldMapCorrectly() {
        // Setup trainee user
        User traineeUser = new User();
        traineeUser.setUsername("alice.smith");
        training.getTrainee().setUser(traineeUser);

        // When
        TrainerTrainingResponseDTO dto = mapper.toTrainerTrainingResponseDTO(training);

        // Then
        assertNotNull(dto);
        assertEquals("Morning Yoga Session", dto.getTrainingName());
        assertEquals(testDate, dto.getTrainingDate());
        assertEquals(60, dto.getTrainingDuration());
        assertEquals("Yoga", dto.getTrainingType());
        assertEquals("alice.smith", dto.getTraineeName());

        // Verify no unnecessary mapper calls
        verifyNoInteractions(traineeMapper, trainerMapper);
    }

    @Test
    void toTrainingResponseDTO_WithNullFields_ShouldHandleNulls() {
        // Given
        training.setTrainee(null);
        training.setTrainer(null);
        training.setTrainingType(null);

        // When
        TrainingResponseDTO dto = mapper.toTrainingResponseDTO(training);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTrainee());
        assertNull(dto.getTrainer());
        assertNull(dto.getTrainingType());
    }
}