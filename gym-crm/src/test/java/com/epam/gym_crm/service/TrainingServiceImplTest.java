package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerWorkloadResponse;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.Training;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TrainingMapper;
import com.epam.gym_crm.repository.TrainingRepository;
import com.epam.gym_crm.service.impl.TrainerWorkingHoursServiceImpl;
import com.epam.gym_crm.service.impl.TrainingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TraineeTrainerService traineeTrainerService;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    @Mock
    private TrainerWorkingHoursServiceImpl trainerWorkingHoursService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;
    private TraineeTrainingResponseDTO traineeTrainingResponseDTO;
    private TrainerTrainingResponseDTO trainerTrainingResponseDTO;
    private TrainingResponseDTO trainingResponseDTO;
    private TrainerProfileResponseDTO trainerProfileResponseDTO;
    private TraineeProfileResponseDTO traineeProfileResponseDTO;

    @BeforeEach
    void setUp() {
        // Set up common test data
        User traineeUser = new User();
        traineeUser.setId(1L);
        traineeUser.setUsername("trainee1");
        traineeUser.setFirstName("TraineeFirst");
        traineeUser.setLastName("TraineeLast");

        User trainerUser = new User();
        trainerUser.setId(2L);
        trainerUser.setUsername("trainer1");
        trainerUser.setFirstName("TrainerFirst");
        trainerUser.setLastName("TrainerLast");

        trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Strength");

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUser(traineeUser);
        trainee.setDateOfBirth(new Date());
        trainee.setAddress("123 Trainee St");

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(trainerUser);
        trainer.setSpecialization(trainingType);

        training = new Training();
        training.setId(1L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        training.setTrainingName("Test Training");
        training.setTrainingDate(new Date());
        training.setTrainingDuration(60);

        traineeTrainingResponseDTO = new TraineeTrainingResponseDTO();
        traineeTrainingResponseDTO.setTrainingName("Test Training");
        traineeTrainingResponseDTO.setTrainingDate(new Date());
        traineeTrainingResponseDTO.setTrainingType("Strength");
        traineeTrainingResponseDTO.setTrainingDuration(60);
        traineeTrainingResponseDTO.setTrainerName("TrainerFirst TrainerLast");

        trainerTrainingResponseDTO = new TrainerTrainingResponseDTO();
        trainerTrainingResponseDTO.setTrainingName("Test Training");
        trainerTrainingResponseDTO.setTrainingDate(new Date());
        trainerTrainingResponseDTO.setTrainingType("Strength");
        trainerTrainingResponseDTO.setTrainingDuration(60);
        trainerTrainingResponseDTO.setTraineeName("TraineeFirst TraineeLast");

        TrainerResponseDTO trainerResponseDTO = new TrainerResponseDTO();
        trainerResponseDTO.setId(1L);
        trainerResponseDTO.setFirstName("TraineeFirst");
        trainerResponseDTO.setLastName("TraineeLast");

        TraineeResponseDTO traineeResponseDTO = new TraineeResponseDTO();
        traineeResponseDTO.setId(1L);
        traineeResponseDTO.setFirstName("TraineeFirst");
        traineeResponseDTO.setLastName("TraineeLast");

        trainingResponseDTO = new TrainingResponseDTO();
        trainingResponseDTO.setTrainingName("Test Training");
        trainingResponseDTO.setTrainingDate(new Date());
        trainingResponseDTO.setTrainingType("Strength");
        trainingResponseDTO.setTrainingDuration(60);
        trainingResponseDTO.setTrainer(trainerResponseDTO);
        trainingResponseDTO.setTrainee(traineeResponseDTO);

        // Create TrainerProfileResponseDTO
        trainerProfileResponseDTO = new TrainerProfileResponseDTO();
        trainerProfileResponseDTO.setId(1L);
        trainerProfileResponseDTO.setFirstName("TrainerFirst");
        trainerProfileResponseDTO.setLastName("TrainerLast");
        trainerProfileResponseDTO.setUsername("trainer1");
        trainerProfileResponseDTO.setPassword("password");
        trainerProfileResponseDTO.setIsActive(true);
        trainerProfileResponseDTO.setSpecialization("Strength");

        // Create TraineeProfileResponseDTO
        traineeProfileResponseDTO = new TraineeProfileResponseDTO();
        traineeProfileResponseDTO.setId(1L);
        traineeProfileResponseDTO.setFirstName("TraineeFirst");
        traineeProfileResponseDTO.setLastName("TraineeLast");
        traineeProfileResponseDTO.setUsername("trainee1");
        traineeProfileResponseDTO.setPassword("password");
        traineeProfileResponseDTO.setIsActive(true);
        traineeProfileResponseDTO.setBirthDate(new Date());
        traineeProfileResponseDTO.setAddress("123 Trainee St");
        traineeProfileResponseDTO.setTrainers(new ArrayList<>());
    }

    @Test
    void getTraineeTrainings_Success() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");
        request.setFrom(new Date(System.currentTimeMillis() - 86400000)); // yesterday
        request.setTo(new Date()); // today
        request.setTrainingType("Strength");

        List<Training> trainings = List.of(training);

        // Fix: Use a concrete object instead of any()
        when(trainerService.getTrainerByUsername("trainer1")).thenReturn(trainerProfileResponseDTO);
        when(trainingRepository.findAllTraineeTrainings(
                eq("trainee1"), eq("trainer1"), any(Date.class), any(Date.class), eq("Strength")))
                .thenReturn(trainings);
        when(trainingMapper.toTraineeTrainingResponseDTO(any(Training.class)))
                .thenReturn(traineeTrainingResponseDTO);

        // Act
        List<TraineeTrainingResponseDTO> result = trainingService.getTraineeTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Training", result.get(0).getTrainingName());
        verify(trainingRepository).findAllTraineeTrainings(
                eq("trainee1"), eq("trainer1"), any(Date.class), any(Date.class), eq("Strength"));
    }

    @Test
    void getTraineeTrainings_InvalidDateRange_ThrowsException() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");
        request.setFrom(new Date()); // today
        request.setTo(new Date(System.currentTimeMillis() - 86400000)); // yesterday

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTraineeTrainings(request));

        assertEquals("Invalid date range: 'from' date cannot be after 'to' date.", exception.getMessage());
        verify(trainingRepository, never()).findAllTraineeTrainings(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void getTraineeTrainings_TrainerNotFound_ThrowsException() {
        // Arrange
        GetTraineeTrainingsRequestDTO request = new GetTraineeTrainingsRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("nonexistent");

        when(trainerService.getTrainerByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTraineeTrainings(request));

        assertTrue(exception.getMessage().contains("Trainer not found with username:"));
        verify(trainingRepository, never()).findAllTraineeTrainings(anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    void getTrainerTrainings_Success() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer1");
        request.setTraineeUsername("trainee1");
        request.setFrom(new Date(System.currentTimeMillis() - 86400000)); // yesterday
        request.setTo(new Date()); // today

        List<Training> trainings = List.of(training);

        // Fix: Use concrete objects instead of any()
        when(trainerService.getTrainerByUsername("trainer1")).thenReturn(trainerProfileResponseDTO);
        when(traineeService.getTraineeByUsername("trainee1")).thenReturn(traineeProfileResponseDTO);
        when(trainingRepository.findAllTrainerTrainings(
                eq("trainer1"), eq("trainee1"), any(Date.class), any(Date.class)))
                .thenReturn(trainings);
        when(trainingMapper.toTrainerTrainingResponseDTO(any(Training.class)))
                .thenReturn(trainerTrainingResponseDTO);

        // Act
        List<TrainerTrainingResponseDTO> result = trainingService.getTrainerTrainings(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Training", result.get(0).getTrainingName());
        verify(trainingRepository).findAllTrainerTrainings(
                eq("trainer1"), eq("trainee1"), any(Date.class), any(Date.class));
    }

    @Test
    void getTrainerTrainings_InvalidDateRange_ThrowsException() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer1");
        request.setFrom(new Date()); // today
        request.setTo(new Date(System.currentTimeMillis() - 86400000)); // yesterday

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTrainerTrainings(request));

        assertEquals("Invalid date range: 'from' date cannot be after 'to' date.", exception.getMessage());
        verify(trainingRepository, never()).findAllTrainerTrainings(anyString(), anyString(), any(), any());
    }

    @Test
    void getTrainerTrainings_TrainerNotFound_ThrowsException() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("nonexistent");

        when(trainerService.getTrainerByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTrainerTrainings(request));

        assertTrue(exception.getMessage().contains("Trainer not found with username:"));
        verify(trainingRepository, never()).findAllTrainerTrainings(anyString(), anyString(), any(), any());
    }

    @Test
    void getTrainerTrainings_TraineeNotFound_ThrowsException() {
        // Arrange
        GetTrainerTrainingsRequestDTO request = new GetTrainerTrainingsRequestDTO();
        request.setTrainerUsername("trainer1");
        request.setTraineeUsername("nonexistent");

        // Fix: Use concrete object instead of any()
        when(trainerService.getTrainerByUsername("trainer1")).thenReturn(trainerProfileResponseDTO);
        when(traineeService.getTraineeByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.getTrainerTrainings(request));

        assertTrue(exception.getMessage().contains("Trainee not found with username:"));
        verify(trainingRepository, never()).findAllTrainerTrainings(anyString(), anyString(), any(), any());
    }

    @Test
    void addTraining_Success() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");
        request.setTrainingDate(new Date());
        request.setTrainingDuration(60);
        request.setTrainingName("Test Training");

        // Mock entities
        Trainee trainee = new Trainee();
        User traineeUser = new User();
        traineeUser.setUsername("trainee1");
        trainee.setUser(traineeUser);

        Trainer trainer = new Trainer();
        User trainerUser = new User();
        trainerUser.setUsername("trainer1");
        trainerUser.setIsActive(true);
        trainer.setUser(trainerUser);

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Yoga");
        trainer.setSpecialization(specialization);

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Yoga");

        Training training = new Training();
        training.setTrainingName("Test Training");
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        TrainingResponseDTO trainingResponseDTO = new TrainingResponseDTO();
        trainingResponseDTO.setTrainingName("Test Training");

        // Mock service calls
        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer);
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.of(trainingType));
        when(trainingRepository.save(any(Training.class))).thenReturn(training);
        when(trainingMapper.toTrainingResponseDTO(training)).thenReturn(trainingResponseDTO);
        when(trainerWorkingHoursService.computeTrainerHours(any(TrainerWorkloadRequest.class)))
                .thenReturn(new TrainerWorkloadResponse());

        // Act
        TrainingResponseDTO result = trainingService.addTraining(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Training", result.getTrainingName());
        verify(trainingRepository).save(any(Training.class));
        verify(traineeTrainerService).createTraineeTrainer("trainee1", "trainer1");
        verify(trainerWorkingHoursService).computeTrainerHours(any(TrainerWorkloadRequest.class));
    }

    @Test
    void addTraining_TraineeNotFound_ThrowsException() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("nonexistent");
        request.setTrainerUsername("trainer1");

        when(traineeService.getTraineeEntityByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(request));

        assertTrue(exception.getMessage().contains("No trainee found with username:"));
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void addTraining_TrainerNotFound_ThrowsException() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("nonexistent");

        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("nonexistent")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(request));

        assertTrue(exception.getMessage().contains("No trainer found with username:"));
        verify(trainingRepository, never()).save(any(Training.class));
    }

    @Test
    void addTraining_TrainingTypeNotFound_ThrowsException() {
        // Arrange
        AddTrainingRequestDTO request = new AddTrainingRequestDTO();
        request.setTraineeUsername("trainee1");
        request.setTrainerUsername("trainer1");

        when(traineeService.getTraineeEntityByUsername("trainee1")).thenReturn(trainee);
        when(trainerService.getTrainerEntityByUsername("trainer1")).thenReturn(trainer);
        when(trainingTypeService.findByValue(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(request));

        assertTrue(exception.getMessage().contains("Invalid training type:"));
        verify(trainingRepository, never()).save(any(Training.class));
    }
}