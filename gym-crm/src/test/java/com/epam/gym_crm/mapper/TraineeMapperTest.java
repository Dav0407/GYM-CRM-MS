package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeMapperTest {

    private final TraineeMapper mapper = Mappers.getMapper(TraineeMapper.class);
    private Trainee trainee;
    private Date testDate;

    @BeforeEach
    void setUp() throws Exception {
        testDate = new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01");

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setIsActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Fitness");

        Trainer trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setUser(user);
        trainer1.setSpecialization(trainingType);

        TraineeTrainer traineeTrainer1 = new TraineeTrainer();
        traineeTrainer1.setTrainer(trainer1);

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(user);
        trainee.setDateOfBirth(testDate);
        trainee.setAddress("123 Main St");
        trainee.setTraineeTrainers(List.of(traineeTrainer1));
    }

    @Test
    void toTraineeResponseDTO_ShouldMapCorrectly() {
        // When
        TraineeResponseDTO dto = mapper.toTraineeResponseDTO(trainee);

        // Then
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("password123", dto.getPassword());
        assertTrue(dto.getIsActive());
        assertEquals(testDate, dto.getBirthDate());
    }

    @Test
    void toTraineeProfileResponseDTO_ShouldMapCorrectly() {
        // When
        TraineeProfileResponseDTO dto = mapper.toTraineeProfileResponseDTO(trainee);

        // Then
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("password123", dto.getPassword());
        assertTrue(dto.getIsActive());
        assertEquals(testDate, dto.getBirthDate());
        assertEquals("123 Main St", dto.getAddress());

        // Verify trainers mapping
        assertNotNull(dto.getTrainers());
        assertEquals(1, dto.getTrainers().size());

        TrainerSecureResponseDTO trainerDto = dto.getTrainers().get(0);
        assertEquals(1L, trainerDto.getId());
        assertEquals("John", trainerDto.getFirstName());
        assertEquals("Doe", trainerDto.getLastName());
        assertEquals("john.doe", trainerDto.getUsername());
        assertEquals("Fitness", trainerDto.getSpecialization());
    }

    @Test
    void toTraineeProfileResponseDTO_WithNullTrainers_ShouldHandleNull() {
        // Given
        trainee.setTraineeTrainers(null);

        // When
        TraineeProfileResponseDTO dto = mapper.toTraineeProfileResponseDTO(trainee);

        // Then
        assertNotNull(dto);
        assertNull(dto.getTrainers());
    }

    @Test
    void toTraineeProfileResponseDTO_WithEmptyTrainers_ShouldReturnEmptyList() {
        // Given
        trainee.setTraineeTrainers(List.of());

        // When
        TraineeProfileResponseDTO dto = mapper.toTraineeProfileResponseDTO(trainee);

        // Then
        assertNotNull(dto);
        assertNotNull(dto.getTrainers());
        assertTrue(dto.getTrainers().isEmpty());
    }

    @Test
    void mapTrainers_ShouldMapCorrectly() {
        // Given
        List<TraineeTrainer> traineeTrainers = trainee.getTraineeTrainers();

        // When
        List<TrainerSecureResponseDTO> result = mapper.mapTrainers(traineeTrainers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        TrainerSecureResponseDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("john.doe", dto.getUsername());
        assertEquals("Fitness", dto.getSpecialization());
    }

    @Test
    void mapTrainers_WithNullInput_ShouldReturnNull() {
        // When
        List<TrainerSecureResponseDTO> result = mapper.mapTrainers(null);

        // Then
        assertNull(result);
    }
}