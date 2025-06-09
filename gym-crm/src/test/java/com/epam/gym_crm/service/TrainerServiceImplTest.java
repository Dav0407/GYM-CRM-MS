package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TrainerMapper;
import com.epam.gym_crm.repository.TrainerRepository;
import com.epam.gym_crm.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private UserService userService;

    @Mock
    private TrainerMapper trainerMapper; // Add mock for TrainerMapper

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private CreateTrainerProfileRequestDTO createRequest;
    private UpdateTrainerProfileRequestDTO updateRequest;
    private User user;
    private Trainer trainer;
    private TrainingType trainingType;
    private TrainerResponseDTO trainerResponseDTO;
    private TrainerProfileResponseDTO trainerProfileResponseDTO;
    private TrainerSecureResponseDTO trainerSecureResponseDTO;

    @BeforeEach
    void setUp() {
        createRequest = new CreateTrainerProfileRequestDTO();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setTrainingType("Fitness");

        updateRequest = new UpdateTrainerProfileRequestDTO();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setUsername("jane.smith");
        updateRequest.setTrainingTypeName("Yoga");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setIsActive(true);

        trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Fitness");

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

        // Create a TrainerResponseDTO for mapping
        trainerResponseDTO = new TrainerResponseDTO();
        trainerResponseDTO.setId(1L);
        trainerResponseDTO.setFirstName("John");
        trainerResponseDTO.setLastName("Doe");
        trainerResponseDTO.setUsername("john.doe");
        trainerResponseDTO.setSpecialization("Fitness");

        trainerProfileResponseDTO = new TrainerProfileResponseDTO();
        trainerProfileResponseDTO.setId(1L);
        trainerProfileResponseDTO.setFirstName("Jane");
        trainerProfileResponseDTO.setLastName("Smith");
        trainerProfileResponseDTO.setUsername("jane.smith");
        trainerProfileResponseDTO.setSpecialization("Yoga");

        trainerSecureResponseDTO = new TrainerSecureResponseDTO();
        trainerSecureResponseDTO.setId(1L);
        trainerSecureResponseDTO.setFirstName("John");
        trainerSecureResponseDTO.setLastName("Doe");
        trainerSecureResponseDTO.setUsername("john.doe");
        trainerSecureResponseDTO.setSpecialization("Fitness");
    }

    @Test
    void testCreateTrainerProfile() {
        // Mock dependencies
        when(userService.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(userService.generateRandomPassword()).thenReturn("randomPassword123");
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(trainingTypeService.findByValue("Fitness")).thenReturn(Optional.of(trainingType));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mocked-refresh-token");


        Trainer savedTrainer = new Trainer();
        savedTrainer.setUser(user);
        savedTrainer.setSpecialization(trainingType);

        when(trainerRepository.save(any(Trainer.class))).thenReturn(savedTrainer);

        lenient().when(trainerMapper.toTrainerResponseDTO(any(Trainer.class))).thenReturn(trainerResponseDTO);

        TrainerResponseDTO createdTrainer = trainerService.createTrainerProfile(createRequest);

        // Assertions
        assertNotNull(createdTrainer);
        assertEquals("john.doe", createdTrainer.getUsername());
        assertEquals("Fitness", createdTrainer.getSpecialization());
        verify(userService, times(1)).generateUsername("John", "Doe");
        verify(userService, times(1)).generateRandomPassword();
        verify(userService, times(1)).saveUser(any(User.class));
        verify(trainingTypeService, times(1)).findByValue("Fitness");
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void testCreateTrainerProfileWithInvalidRequest() {
        createRequest.setFirstName("");
        createRequest.setLastName("");

        assertThrows(IllegalArgumentException.class, () -> trainerService.createTrainerProfile(createRequest));

        verify(userService, never()).generateUsername(anyString(), anyString());
        verify(userService, never()).generateRandomPassword();
        verify(userService, never()).saveUser(any(User.class));
        verify(trainingTypeService, never()).findByValue(anyString());
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void testGetTrainerById() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));

        // Mock the mapper
        when(trainerMapper.toTrainerResponseDTO(trainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO foundTrainer = trainerService.getTrainerById(1L);

        assertNotNull(foundTrainer);
        assertEquals(1L, foundTrainer.getId());
        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1)).toTrainerResponseDTO(trainer);
    }

    @Test
    void testGetTrainerByIdNotFound() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.getTrainerById(1L));

        verify(trainerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTrainerByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainer));

        // Mock the mapper
        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);

        TrainerProfileResponseDTO foundTrainer = trainerService.getTrainerByUsername("john.doe");

        assertNotNull(foundTrainer);
        assertEquals(1L, foundTrainer.getId());
        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
        verify(trainerMapper, times(1)).toTrainerProfileResponseDTO(trainer);
    }

    @Test
    void testGetTrainerByUsernameNotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.getTrainerByUsername("john.doe"));

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testUpdateTrainerProfile() {
        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.of(trainingType));

        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);

        TrainerProfileResponseDTO updatedTrainer = trainerService.updateTrainerProfile(updateRequest);

        assertNotNull(updatedTrainer);
        assertEquals("Jane", updatedTrainer.getFirstName());
        assertEquals("Yoga", updatedTrainer.getSpecialization());
        verify(trainerRepository, times(1)).findByUser_Username("jane.smith");
        verify(trainingTypeService, times(1)).findByValue("Yoga");
        verify(trainerMapper, times(1)).toTrainerProfileResponseDTO(trainer);
    }

    @Test
    void testUpdateTrainerProfileNotFound() {
        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> trainerService.updateTrainerProfile( updateRequest));

        verify(trainerRepository, times(1)).findByUser_Username("jane.smith");
        verify(trainingTypeService, never()).findByValue(anyString());
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void testUpdateStatus() {
        doNothing().when(userService).updateStatus("john.doe");

        trainerService.updateStatus("john.doe");

        verify(userService, times(1)).updateStatus("john.doe");
    }

    @Test
    void testGetNotAssignedTrainersByTraineeUsername() {
        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1")).thenReturn(List.of(trainer));

        // Mock the mapper for the list
        when(trainerMapper.toTrainerSecureResponseDTO(trainer)).thenReturn(trainerSecureResponseDTO);

        List<TrainerSecureResponseDTO> unassignedTrainers = trainerService.getNotAssignedTrainersByTraineeUsername("trainee1");

        assertNotNull(unassignedTrainers);
        assertEquals(1, unassignedTrainers.size());
        verify(trainerRepository, times(1)).findUnassignedTrainersByTraineeUsername("trainee1");
        verify(trainerMapper, times(1)).toTrainerSecureResponseDTO(trainer);
    }
}