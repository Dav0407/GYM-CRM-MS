package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TrainerMapper;
import com.epam.gym_crm.repository.TraineeTrainerRepository;
import com.epam.gym_crm.service.impl.TraineeTrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeTrainerServiceImplTest {

    @Mock
    private TraineeTrainerRepository traineeTrainerRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TraineeTrainerServiceImpl traineeTrainerService;

    private Trainee trainee;
    private Trainer trainer1;
    private Trainer trainer2;
    private TraineeTrainer traineeTrainer;
    private TrainerSecureResponseDTO trainerSecureResponseDTO;

    @BeforeEach
    void setUp() {
        // Set up common test data
        User traineeUser = new User();
        traineeUser.setId(1L);
        traineeUser.setUsername("trainee1");
        traineeUser.setFirstName("TraineeFirst");
        traineeUser.setLastName("TraineeLast");
        traineeUser.setIsActive(true);

        User trainerUser1 = new User();
        trainerUser1.setId(2L);
        trainerUser1.setUsername("trainer1");
        trainerUser1.setFirstName("TrainerFirst1");
        trainerUser1.setLastName("TrainerLast1");
        trainerUser1.setIsActive(true);

        User trainerUser2 = new User();
        trainerUser2.setId(3L);
        trainerUser2.setUsername("trainer2");
        trainerUser2.setFirstName("TrainerFirst2");
        trainerUser2.setLastName("TrainerLast2");
        trainerUser2.setIsActive(true);

        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Strength");

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(traineeUser);
        trainee.setDateOfBirth(new Date());
        trainee.setAddress("123 Trainee St");

        trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setUser(trainerUser1);
        trainer1.setSpecialization(trainingType);

        trainer2 = new Trainer();
        trainer2.setId(2L);
        trainer2.setUser(trainerUser2);
        trainer2.setSpecialization(trainingType);

        traineeTrainer = new TraineeTrainer();
        traineeTrainer.setId(1L);
        traineeTrainer.setTrainee(trainee);
        traineeTrainer.setTrainer(trainer1);

        trainerSecureResponseDTO = new TrainerSecureResponseDTO();
        trainerSecureResponseDTO.setId(1L);
        trainerSecureResponseDTO.setFirstName("TrainerFirst1");
        trainerSecureResponseDTO.setLastName("TrainerLast1");
        trainerSecureResponseDTO.setSpecialization("Strength");
    }

    @Test
    void createTraineeTrainer_Success() {
        // Arrange
        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer1);
        when(traineeTrainerRepository.findByTraineeAndTrainer(trainee, trainer1)).thenReturn(Optional.empty());
        when(traineeTrainerRepository.save(any(TraineeTrainer.class))).thenReturn(traineeTrainer);

        // Act
        TraineeTrainer result = traineeTrainerService.createTraineeTrainer("trainee1", "trainer1");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer1, result.getTrainer());
        verify(traineeTrainerRepository).save(any(TraineeTrainer.class));
    }

    @Test
    void createTraineeTrainer_RelationshipAlreadyExists() {
        // Arrange
        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer1);
        when(traineeTrainerRepository.findByTraineeAndTrainer(trainee, trainer1)).thenReturn(Optional.of(traineeTrainer));

        // Act
        TraineeTrainer result = traineeTrainerService.createTraineeTrainer("trainee1", "trainer1");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(traineeTrainerRepository, times(0)).save(any(TraineeTrainer.class));
    }

    @Test
    void findByTraineeUsername_Success() {
        // Arrange
        List<TraineeTrainer> traineeTrainers = new ArrayList<>();
        traineeTrainers.add(traineeTrainer);

        when(traineeTrainerRepository.findAllByTrainee_User_Username("trainee1")).thenReturn(traineeTrainers);

        // Act
        List<TraineeTrainer> result = traineeTrainerService.findByTraineeUsername("trainee1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(trainee, result.get(0).getTrainee());
        assertEquals(trainer1, result.get(0).getTrainer());
    }

    @Test
    void updateTraineeTrainers_Success() {
        // Arrange
        UpdateTrainerListRequestDTO request = new UpdateTrainerListRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsernames(Arrays.asList("trainer1", "trainer2"));

        List<TraineeTrainer> existingRelations = new ArrayList<>();
        existingRelations.add(traineeTrainer);

        TraineeTrainer newTraineeTrainer1 = new TraineeTrainer();
        newTraineeTrainer1.setId(2L);
        newTraineeTrainer1.setTrainee(trainee);
        newTraineeTrainer1.setTrainer(trainer1);

        TraineeTrainer newTraineeTrainer2 = new TraineeTrainer();
        newTraineeTrainer2.setId(3L);
        newTraineeTrainer2.setTrainee(trainee);
        newTraineeTrainer2.setTrainer(trainer2);

        TrainerSecureResponseDTO trainerSecureResponseDTO2 = new TrainerSecureResponseDTO();
        trainerSecureResponseDTO2.setId(2L);
        trainerSecureResponseDTO2.setFirstName("TrainerFirst2");
        trainerSecureResponseDTO2.setLastName("TrainerLast2");
        trainerSecureResponseDTO2.setSpecialization("Strength");

        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(traineeTrainerRepository.findAllByTrainee_User_Username("trainee1")).thenReturn(existingRelations);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer1);
        when(trainerService.getTrainerEntityByUsername("trainer2")).thenReturn(trainer2);
        when(trainerMapper.toTrainerSecureResponseDTO(trainer1)).thenReturn(trainerSecureResponseDTO);
        when(trainerMapper.toTrainerSecureResponseDTO(trainer2)).thenReturn(trainerSecureResponseDTO2);

        // Act
        List<TrainerSecureResponseDTO> result = traineeTrainerService.updateTraineeTrainers(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(traineeTrainerRepository).deleteAll(existingRelations);
        verify(traineeTrainerRepository).saveAll(any());
    }

    @Test
    void updateTraineeTrainers_TraineeNotFound() {
        // Arrange
        UpdateTrainerListRequestDTO request = new UpdateTrainerListRequestDTO();
        request.setTraineeUsername("nonexistent");
        request.setTrainerUsernames(Arrays.asList("trainer1", "trainer2"));

        when(traineeService.getTraineeEntityByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeTrainerService.updateTraineeTrainers(request));

        assertTrue(exception.getMessage().contains("Trainee not found"));
        verify(traineeTrainerRepository, times(0)).deleteAll(any());
        verify(traineeTrainerRepository, times(0)).saveAll(any());
    }

    @Test
    void updateTraineeTrainers_TrainerNotFound() {
        // Arrange
        UpdateTrainerListRequestDTO request = new UpdateTrainerListRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsernames(Arrays.asList("trainer1", "nonexistent"));

        List<TraineeTrainer> existingRelations = new ArrayList<>();
        existingRelations.add(traineeTrainer);

        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(traineeTrainerRepository.findAllByTrainee_User_Username("trainee1")).thenReturn(existingRelations);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer1);
        when(trainerService.getTrainerEntityByUsername("nonexistent")).thenReturn(null);
        when(trainerMapper.toTrainerSecureResponseDTO(trainer1)).thenReturn(trainerSecureResponseDTO);

        // Act
        List<TrainerSecureResponseDTO> result = traineeTrainerService.updateTraineeTrainers(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only one valid trainer
        verify(traineeTrainerRepository).deleteAll(existingRelations);
        verify(traineeTrainerRepository).saveAll(any());
    }
}