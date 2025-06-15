package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.ResourceNotFoundException;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.TrainerMapper;
import com.epam.gym_crm.repository.TrainerRepository;
import com.epam.gym_crm.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
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
    private TrainerMapper trainerMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private CreateTrainerProfileRequestDTO createRequest;
    private UpdateTrainerProfileRequestDTO updateRequest;
    private User user;
    private User authenticatedUser;
    private Trainer trainer;
    private TrainingType trainingType;
    private TrainingType newTrainingType;
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
        updateRequest.setIsActive(true);

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password123");
        user.setIsActive(true);

        authenticatedUser = new User();
        authenticatedUser.setId(2L);
        authenticatedUser.setUsername("authenticated.user");

        trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Fitness");

        newTrainingType = new TrainingType();
        newTrainingType.setId(2L);
        newTrainingType.setTrainingTypeName("Yoga");

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUser(user);
        trainer.setSpecialization(trainingType);

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
        when(userService.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(userService.generateRandomPassword()).thenReturn("randomPassword123");
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(trainingTypeService.findByValue("Fitness")).thenReturn(Optional.of(trainingType));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toTrainerResponseDTO(any(Trainer.class))).thenReturn(trainerResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mocked-refresh-token");

        TrainerResponseDTO result = trainerService.createTrainerProfile(createRequest);

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("Fitness", result.getSpecialization());
        assertEquals("plainPassword", result.getPassword());
        assertEquals("mocked-access-token", result.getAccessToken());
        assertEquals("mocked-refresh-token", result.getRefreshToken());

        verify(userService, times(1)).generateUsername("John", "Doe");
        verify(userService, times(1)).generateRandomPassword();
        verify(userService, times(1)).saveUser(any(User.class));
        verify(trainingTypeService, times(1)).findByValue("Fitness");
        verify(trainerRepository, times(1)).save(any(Trainer.class));
        verify(trainerMapper, times(1)).toTrainerResponseDTO(any(Trainer.class));
        verify(userService, times(1)).getPlainPassword("john.doe");
        verify(jwtService, times(1)).generateAccessToken(any(User.class));
        verify(jwtService, times(1)).generateRefreshToken(any(User.class));
    }

    @Test
    void testCreateTrainerProfile_TrainingTypeNotFound() {
        when(userService.generateUsername("John", "Doe")).thenReturn("john.doe");
        when(userService.generateRandomPassword()).thenReturn("randomPassword123");
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(trainingTypeService.findByValue("Fitness")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> trainerService.createTrainerProfile(createRequest));

        assertEquals("Training type not found: Fitness", exception.getMessage());

        verify(userService, times(1)).generateUsername("John", "Doe");
        verify(userService, times(1)).generateRandomPassword();
        verify(userService, times(1)).saveUser(any(User.class));
        verify(trainingTypeService, times(1)).findByValue("Fitness");
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void testGetTrainerById() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toTrainerResponseDTO(trainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO result = trainerService.getTrainerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(trainerRepository, times(1)).findById(1L);
        verify(trainerMapper, times(1)).toTrainerResponseDTO(trainer);
    }

    @Test
    void testGetTrainerById_NotFound() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> trainerService.getTrainerById(1L));

        assertEquals("Trainer not found with ID: 1", exception.getMessage());
        verify(trainerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTrainerByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");

        TrainerProfileResponseDTO result = trainerService.getTrainerByUsername("john.doe");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("plainPassword", result.getPassword());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
        verify(trainerMapper, times(1)).toTrainerProfileResponseDTO(trainer);
        verify(userService, times(1)).getPlainPassword("john.doe");
    }

    @Test
    void testGetTrainerByUsername_NotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> trainerService.getTrainerByUsername("john.doe"));

        assertEquals("Trainer not found with username: john.doe", exception.getMessage());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetTrainerEntityByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getTrainerEntityByUsername("john.doe");

        assertNotNull(result);
        assertEquals(trainer.getId(), result.getId());
        assertEquals(trainer.getUser().getUsername(), result.getUser().getUsername());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetTrainerEntityByUsername_NotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(trainerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> trainerService.getTrainerEntityByUsername("john.doe"));

        assertEquals("Trainer not found with username: john.doe", exception.getMessage());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(trainerRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testUpdateTrainerProfile() {
        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.of(newTrainingType));
        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);
        when(userService.getPlainPassword("jane.smith")).thenReturn("plainPassword");

        TrainerProfileResponseDTO result = trainerService.updateTrainerProfile(updateRequest);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Yoga", result.getSpecialization());
        assertEquals("plainPassword", result.getPassword());

        // Verify that the trainer entity was updated
        assertEquals("Jane", trainer.getUser().getFirstName());
        assertEquals("Smith", trainer.getUser().getLastName());
        assertEquals(newTrainingType, trainer.getSpecialization());

        verify(trainerRepository, times(1)).findByUser_Username("jane.smith");
        verify(trainingTypeService, times(1)).findByValue("Yoga");
        verify(trainerMapper, times(1)).toTrainerProfileResponseDTO(trainer);
        verify(userService, times(1)).getPlainPassword("jane.smith");
        verify(userService, never()).updateStatus(anyString()); // Status didn't change
    }

    @Test
    void testUpdateTrainerProfile_WithStatusChange() {
        updateRequest.setIsActive(false); // Different from trainer's current status (true)

        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.of(newTrainingType));
        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);
        when(userService.getPlainPassword("jane.smith")).thenReturn("plainPassword");
        doNothing().when(userService).updateStatus("john.doe");

        TrainerProfileResponseDTO result = trainerService.updateTrainerProfile(updateRequest);

        assertNotNull(result);
        verify(userService, times(1)).updateStatus("john.doe"); // Status change triggered
    }

    @Test
    void testUpdateTrainerProfile_NotFound() {
        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> trainerService.updateTrainerProfile(updateRequest));

        assertEquals("Trainer not found with username: jane.smith", exception.getMessage());

        verify(trainerRepository, times(1)).findByUser_Username("jane.smith");
        verify(trainingTypeService, never()).findByValue(anyString());
    }

    @Test
    void testUpdateTrainerProfile_TrainingTypeNotFound() {
        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> trainerService.updateTrainerProfile(updateRequest));

        assertEquals("Training type not found: Yoga", exception.getMessage());

        verify(trainerRepository, times(1)).findByUser_Username("jane.smith");
        verify(trainingTypeService, times(1)).findByValue("Yoga");
    }

    @Test
    void testUpdateTrainerProfile_WithTrimmedData() {
        updateRequest.setFirstName("  Jane  ");
        updateRequest.setLastName("  Smith  ");

        when(trainerRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeService.findByValue("Yoga")).thenReturn(Optional.of(newTrainingType));
        when(trainerMapper.toTrainerProfileResponseDTO(trainer)).thenReturn(trainerProfileResponseDTO);
        when(userService.getPlainPassword("jane.smith")).thenReturn("plainPassword");

        trainerService.updateTrainerProfile(updateRequest);

        // Verify that the fields were trimmed
        assertEquals("Jane", trainer.getUser().getFirstName());
        assertEquals("Smith", trainer.getUser().getLastName());
    }

    @Test
    void testUpdateStatus() {
        doNothing().when(userService).updateStatus("john.doe");

        trainerService.updateStatus("john.doe");

        verify(userService, times(1)).updateStatus("john.doe");
    }

    @Test
    void testGetNotAssignedTrainersByTraineeUsername() {
        List<Trainer> trainers = List.of(trainer);
        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1")).thenReturn(trainers);
        when(trainerMapper.toTrainerSecureResponseDTO(trainer)).thenReturn(trainerSecureResponseDTO);

        List<TrainerSecureResponseDTO> result = trainerService.getNotAssignedTrainersByTraineeUsername("trainee1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(trainerSecureResponseDTO, result.get(0));

        verify(trainerRepository, times(1)).findUnassignedTrainersByTraineeUsername("trainee1");
        verify(trainerMapper, times(1)).toTrainerSecureResponseDTO(trainer);
    }

    @Test
    void testGetNotAssignedTrainersByTraineeUsername_EmptyList() {
        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1")).thenReturn(Collections.emptyList());

        List<TrainerSecureResponseDTO> result = trainerService.getNotAssignedTrainersByTraineeUsername("trainee1");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(trainerRepository, times(1)).findUnassignedTrainersByTraineeUsername("trainee1");
        verify(trainerMapper, never()).toTrainerSecureResponseDTO(any());
    }

    @Test
    void testGetNotAssignedTrainersByTraineeUsername_ThrowsException() {
        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1"))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> trainerService.getNotAssignedTrainersByTraineeUsername("trainee1"));

        assertEquals("Failed to retrieve unassigned trainers", exception.getMessage());
        assertEquals("Database error", exception.getCause().getMessage());

        verify(trainerRepository, times(1)).findUnassignedTrainersByTraineeUsername("trainee1");
    }

    @Test
    void testGetTrainerResponseDTO() {
        when(trainerMapper.toTrainerResponseDTO(trainer)).thenReturn(trainerResponseDTO);

        TrainerResponseDTO result = trainerService.getTrainerResponseDTO(trainer);

        assertEquals(trainerResponseDTO, result);
        verify(trainerMapper, times(1)).toTrainerResponseDTO(trainer);
    }

    @Test
    void testGetUserService() {
        UserService result = trainerService.getUserService();
        assertEquals(userService, result);
    }

    @Test
    void testGetRole() {
        User.Role result = trainerService.getRole();
        assertEquals(User.Role.TRAINER, result);
    }

    @Test
    void testGetJwtService() {
        JwtService result = trainerService.getJwtService();
        assertEquals(jwtService, result);
    }

    @Test
    void testGetUserFromEntity() {
        User result = trainerService.getUserFromEntity(trainer);
        assertEquals(user, result);
    }

    @Test
    void testSetAccessToken() {
        TrainerResponseDTO response = new TrainerResponseDTO();
        String accessToken = "test-access-token";

        trainerService.setAccessToken(response, accessToken);

        assertEquals(accessToken, response.getAccessToken());
    }

    @Test
    void testSetRefreshToken() {
        TrainerResponseDTO response = new TrainerResponseDTO();
        String refreshToken = "test-refresh-token";

        trainerService.setRefreshToken(response, refreshToken);

        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void testCheckOwnership_Success() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);

            assertDoesNotThrow(() -> trainerService.checkOwnership("john.doe"));
        }
    }

    @Test
    void testCheckOwnership_AccessDenied() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authenticatedUser);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> trainerService.checkOwnership("john.doe"));

            assertEquals("You are not authorized to access this resource", exception.getMessage());
        }
    }
}