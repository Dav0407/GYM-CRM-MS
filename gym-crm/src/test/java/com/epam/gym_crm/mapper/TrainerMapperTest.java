package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeSecureResponseDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerMapperTest {

    private final TrainerMapper mapper = Mappers.getMapper(TrainerMapper.class);
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        // Setup User
        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setIsActive(true);

        // Setup TrainingType
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Fitness");

        // Setup Trainee
        User traineeUser = new User();
        traineeUser.setFirstName("Alice");
        traineeUser.setLastName("Smith");
        traineeUser.setUsername("alice.smith");

        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(traineeUser);

        // Setup Trainer
        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

        // Setup TraineeTrainer relationship
        TraineeTrainer traineeTrainer = new TraineeTrainer();
        traineeTrainer.setTrainee(trainee);
        traineeTrainer.setTrainer(trainer);

        trainer.setTraineeTrainers(List.of(traineeTrainer));
    }

    @Test
    void toTrainerResponseDTO_ShouldMapCorrectly() {
        // When
        TrainerResponseDTO dto = mapper.toTrainerResponseDTO(trainer);

        // Then
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("password123", dto.getPassword());
        assertTrue(dto.getIsActive());
        assertEquals("Fitness", dto.getSpecialization());
    }

    @Test
    void toTrainerProfileResponseDTO_ShouldMapCorrectly() {
        // When
        TrainerProfileResponseDTO dto = mapper.toTrainerProfileResponseDTO(trainer);

        // Then
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("password123", dto.getPassword());
        assertTrue(dto.getIsActive());
        assertEquals("Fitness", dto.getSpecialization());

        // Verify trainees mapping
        assertNotNull(dto.getTrainees());
        assertEquals(1, dto.getTrainees().size());

        TraineeSecureResponseDTO traineeDto = dto.getTrainees().get(0);
        assertEquals("alice.smith", traineeDto.getUsername());
        assertEquals("Alice", traineeDto.getFirstName());
        assertEquals("Smith", traineeDto.getLastName());
    }

    @Test
    void toTrainerProfileResponseDTO_WithNullTrainees_ShouldHandleNull() {
        // Given
        trainer.setTraineeTrainers(null);

        // When
        TrainerProfileResponseDTO dto = mapper.toTrainerProfileResponseDTO(trainer);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTrainees());
    }

    @Test
    void toTrainerProfileResponseDTO_WithEmptyTrainees_ShouldReturnEmptyList() {
        // Given
        trainer.setTraineeTrainers(List.of());

        // When
        TrainerProfileResponseDTO dto = mapper.toTrainerProfileResponseDTO(trainer);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.getTrainees());
        assertTrue(dto.getTrainees().isEmpty());
    }

    @Test
    void toTrainerSecureResponseDTO_ShouldMapCorrectly() {
        // When
        TrainerSecureResponseDTO dto = mapper.toTrainerSecureResponseDTO(trainer);

        // Then
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("Fitness", dto.getSpecialization());
        // Note: password and isActive should not be in secure DTO
    }

    @Test
    void mapTrainees_ShouldMapCorrectly() {
        // Given
        List<TraineeTrainer> traineeTrainers = trainer.getTraineeTrainers();

        // When
        List<TraineeSecureResponseDTO> result = mapper.mapTrainees(traineeTrainers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TraineeSecureResponseDTO dto = result.get(0);
        assertEquals("alice.smith", dto.getUsername());
        assertEquals("Alice", dto.getFirstName());
        assertEquals("Smith", dto.getLastName());
    }

    @Test
    void mapTrainees_WithNullInput_ShouldReturnNull() {
        // When
        List<TraineeSecureResponseDTO> result = mapper.mapTrainees(null);

        // Then
        assertNull(result);
    }
}