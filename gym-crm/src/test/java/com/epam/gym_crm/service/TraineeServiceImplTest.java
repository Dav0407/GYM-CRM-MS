package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.TraineeMapper;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.service.impl.TraineeServiceImpl;
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

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private CreateTraineeProfileRequestDTO createRequest;
    private UpdateTraineeProfileRequestDTO updateRequest;
    private User user;
    private User authenticatedUser;
    private Trainee trainee;
    private TraineeResponseDTO traineeResponseDTO;
    private TraineeProfileResponseDTO traineeProfileResponseDTO;
    private Date dateOfBirth;

    @BeforeEach
    void setUp() {
        dateOfBirth = new Date();

        createRequest = new CreateTraineeProfileRequestDTO();
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setDateOfBirth(dateOfBirth);
        createRequest.setAddress("123 Main St");

        updateRequest = new UpdateTraineeProfileRequestDTO();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");
        updateRequest.setUsername("jane.smith");
        updateRequest.setDateOfBirth(dateOfBirth);
        updateRequest.setAddress("456 Elm St");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername("john.doe");
        user.setPassword("password");
        user.setIsActive(true);

        authenticatedUser = new User();
        authenticatedUser.setId(2L);
        authenticatedUser.setUsername("authenticated.user");

        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress("123 Main St");
        trainee.setUser(user);

        traineeResponseDTO = new TraineeResponseDTO();
        traineeResponseDTO.setId(1L);
        traineeResponseDTO.setFirstName("John");
        traineeResponseDTO.setLastName("Doe");
        traineeResponseDTO.setUsername("john.doe");
        traineeResponseDTO.setPassword("password");
        traineeResponseDTO.setIsActive(true);
        traineeResponseDTO.setBirthDate(dateOfBirth);
        traineeResponseDTO.setAddress("123 Main St");

        traineeProfileResponseDTO = new TraineeProfileResponseDTO();
        traineeProfileResponseDTO.setId(1L);
        traineeProfileResponseDTO.setFirstName("John");
        traineeProfileResponseDTO.setLastName("Doe");
        traineeProfileResponseDTO.setUsername("john.doe");
        traineeProfileResponseDTO.setPassword("password");
        traineeProfileResponseDTO.setIsActive(true);
        traineeProfileResponseDTO.setBirthDate(dateOfBirth);
        traineeProfileResponseDTO.setAddress("123 Main St");
    }

    @Test
    void testCreateTraineeProfile() {
        when(userService.generateUsername(anyString(), anyString())).thenReturn("john.doe");
        when(userService.generateRandomPassword()).thenReturn("password");
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);
        when(traineeMapper.toTraineeResponseDTO(trainee)).thenReturn(traineeResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mocked-refresh-token");

        TraineeResponseDTO response = traineeService.createTraineeProfile(createRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("123 Main St", response.getAddress());
        assertEquals("plainPassword", response.getPassword());
        assertEquals("mocked-access-token", response.getAccessToken());
        assertEquals("mocked-refresh-token", response.getRefreshToken());

        verify(userService, times(1)).generateUsername("John", "Doe");
        verify(userService, times(1)).generateRandomPassword();
        verify(userService, times(1)).saveUser(any(User.class));
        verify(traineeRepository, times(1)).save(any(Trainee.class));
        verify(traineeMapper, times(1)).toTraineeResponseDTO(trainee);
        verify(userService, times(1)).getPlainPassword("john.doe");
        verify(jwtService, times(1)).generateAccessToken(any(User.class));
        verify(jwtService, times(1)).generateRefreshToken(any(User.class));
    }

    @Test
    void testGetTraineeById() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toTraineeResponseDTO(trainee)).thenReturn(traineeResponseDTO);

        TraineeResponseDTO response = traineeService.getTraineeById(1L);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("password", response.getPassword());
        assertTrue(response.getIsActive());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("123 Main St", response.getAddress());

        verify(traineeRepository, times(1)).findById(1L);
        verify(traineeMapper, times(1)).toTraineeResponseDTO(trainee);
    }

    @Test
    void testGetTraineeById_NotFound() {
        when(traineeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> traineeService.getTraineeById(1L));

        assertEquals("Trainee not found with ID: 1", exception.getMessage());
        verify(traineeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTraineeByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(traineeProfileResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");

        TraineeProfileResponseDTO response = traineeService.getTraineeByUsername("john.doe");

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("123 Main St", response.getAddress());
        assertEquals("plainPassword", response.getPassword());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
        verify(traineeMapper, times(1)).toTraineeProfileResponseDTO(trainee);
        verify(userService, times(1)).getPlainPassword("john.doe");
    }

    @Test
    void testGetTraineeByUsername_NotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> traineeService.getTraineeByUsername("john.doe"));

        assertEquals("Trainee not found with username: john.doe", exception.getMessage());
        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetTraineeEntityByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getTraineeEntityByUsername("john.doe");

        assertNotNull(result);
        assertEquals(trainee.getId(), result.getId());
        assertEquals(trainee.getAddress(), result.getAddress());
        assertEquals(trainee.getDateOfBirth(), result.getDateOfBirth());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testGetTraineeEntityByUsername_NotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> traineeService.getTraineeEntityByUsername("john.doe"));

        assertEquals("Trainee not found with username: john.doe", exception.getMessage());
        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testUpdateTraineeProfile() {
        when(traineeRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainee));
        when(userService.getPlainPassword("jane.smith")).thenReturn("plainPassword");

        TraineeProfileResponseDTO updatedResponseDTO = new TraineeProfileResponseDTO();
        updatedResponseDTO.setId(1L);
        updatedResponseDTO.setFirstName("Jane");
        updatedResponseDTO.setLastName("Smith");
        updatedResponseDTO.setUsername("jane.smith");
        updatedResponseDTO.setBirthDate(dateOfBirth);
        updatedResponseDTO.setAddress("456 Elm St");
        updatedResponseDTO.setIsActive(true);

        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(updatedResponseDTO);

        TraineeProfileResponseDTO response = traineeService.updateTraineeProfile(updateRequest);

        assertNotNull(response);
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("456 Elm St", response.getAddress());
        assertEquals("plainPassword", response.getPassword());

        // Verify that the trainee entity was updated
        assertEquals("Jane", trainee.getUser().getFirstName());
        assertEquals("Smith", trainee.getUser().getLastName());
        assertEquals(dateOfBirth, trainee.getDateOfBirth());
        assertEquals("456 Elm St", trainee.getAddress());

        verify(traineeRepository, times(1)).findByUser_Username("jane.smith");
        verify(traineeMapper, times(1)).toTraineeProfileResponseDTO(trainee);
        verify(userService, times(1)).getPlainPassword("jane.smith");
    }

    @Test
    void testUpdateTraineeProfile_NotFound() {
        when(traineeRepository.findByUser_Username("jane.smith")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> traineeService.updateTraineeProfile(updateRequest));

        assertEquals("Trainee not found with username: jane.smith", exception.getMessage());
        verify(traineeRepository, times(1)).findByUser_Username("jane.smith");
    }

    @Test
    void testUpdateStatus() {
        doNothing().when(userService).updateStatus("john.doe");

        traineeService.updateStatus("john.doe");

        verify(userService, times(1)).updateStatus("john.doe");
    }

    @Test
    void testDeleteTraineeProfileByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(traineeProfileResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");
        doNothing().when(userService).deleteUser("john.doe");

        TraineeProfileResponseDTO response = traineeService.deleteTraineeProfileByUsername("john.doe");

        assertNotNull(response);
        assertEquals("plainPassword", response.getPassword());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
        verify(traineeMapper, times(1)).toTraineeProfileResponseDTO(trainee);
        verify(userService, times(2)).getPlainPassword("john.doe"); // Called twice in the method
        verify(userService, times(1)).deleteUser("john.doe");
    }

    @Test
    void testGetUserService() {
        UserService result = traineeService.getUserService();
        assertEquals(userService, result);
    }

    @Test
    void testGetRole() {
        User.Role result = traineeService.getRole();
        assertEquals(User.Role.TRAINEE, result);
    }

    @Test
    void testGetJwtService() {
        JwtService result = traineeService.getJwtService();
        assertEquals(jwtService, result);
    }

    @Test
    void testGetUserFromEntity() {
        User result = traineeService.getUserFromEntity(trainee);
        assertEquals(user, result);
    }

    @Test
    void testSetAccessToken() {
        TraineeResponseDTO response = new TraineeResponseDTO();
        String accessToken = "test-access-token";

        traineeService.setAccessToken(response, accessToken);

        assertEquals(accessToken, response.getAccessToken());
    }

    @Test
    void testSetRefreshToken() {
        TraineeResponseDTO response = new TraineeResponseDTO();
        String refreshToken = "test-refresh-token";

        traineeService.setRefreshToken(response, refreshToken);

        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void testCheckOwnership_Success() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);

            // This should not throw an exception
            assertDoesNotThrow(() -> traineeService.checkOwnership("john.doe"));
        }
    }

    @Test
    void testCheckOwnership_AccessDenied() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(authenticatedUser);

            AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                    () -> traineeService.checkOwnership("john.doe"));

            assertEquals("You are not authorized to access this resource", exception.getMessage());
        }
    }

    @Test
    void testCreateTraineeProfile_WithTrimmedData() {
        // Test with data that needs trimming
        createRequest.setAddress("  123 Main St  ");

        when(userService.generateUsername(anyString(), anyString())).thenReturn("john.doe");
        when(userService.generateRandomPassword()).thenReturn("password");
        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);
        when(traineeMapper.toTraineeResponseDTO(trainee)).thenReturn(traineeResponseDTO);
        when(userService.getPlainPassword("john.doe")).thenReturn("plainPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mocked-refresh-token");

        TraineeResponseDTO response = traineeService.createTraineeProfile(createRequest);

        assertNotNull(response);

        // Verify that save was called and the address was trimmed
        verify(traineeRepository, times(1)).save(argThat(trainee ->
                "123 Main St".equals(trainee.getAddress())
        ));
    }

    @Test
    void testUpdateTraineeProfile_WithTrimmedData() {
        // Test with data that needs trimming
        updateRequest.setFirstName("  Jane  ");
        updateRequest.setLastName("  Smith  ");
        updateRequest.setAddress("  456 Elm St  ");

        when(traineeRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainee));
        when(userService.getPlainPassword("jane.smith")).thenReturn("plainPassword");

        TraineeProfileResponseDTO updatedResponseDTO = new TraineeProfileResponseDTO();
        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(updatedResponseDTO);

        traineeService.updateTraineeProfile(updateRequest);

        // Verify that the fields were trimmed
        assertEquals("Jane", trainee.getUser().getFirstName());
        assertEquals("Smith", trainee.getUser().getLastName());
        assertEquals("456 Elm St", trainee.getAddress());
    }
}