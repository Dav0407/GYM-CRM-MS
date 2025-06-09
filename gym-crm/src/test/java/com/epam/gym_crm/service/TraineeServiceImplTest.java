package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.mapper.TraineeMapper;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.service.impl.TraineeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private CreateTraineeProfileRequestDTO createRequest;
    private UpdateTraineeProfileRequestDTO updateRequest;
    private User user;
    private Trainee trainee;
    private TraineeResponseDTO traineeResponseDTO;
    private TraineeProfileResponseDTO traineeProfileResponseDTO;
    private Date dateOfBirth;

    @BeforeEach
    void setUp() {
        dateOfBirth = new Date(); // Use current date or a specific date for testing

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
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("mocked-access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("mocked-refresh-token");


        TraineeResponseDTO response = traineeService.createTraineeProfile(createRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("123 Main St", response.getAddress());

        verify(userService, times(1)).generateUsername("John", "Doe");
        verify(userService, times(1)).generateRandomPassword();
        verify(userService, times(1)).saveUser(any(User.class));
        verify(traineeRepository, times(1)).save(any(Trainee.class));
        verify(traineeMapper, times(1)).toTraineeResponseDTO(trainee);
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

        assertThrows(RuntimeException.class, () -> traineeService.getTraineeById(1L));

        verify(traineeRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTraineeByUsername() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(traineeProfileResponseDTO);

        TraineeProfileResponseDTO response = traineeService.getTraineeByUsername("john.doe");

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(dateOfBirth, response.getBirthDate());
        assertEquals("123 Main St", response.getAddress());

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
        verify(traineeMapper, times(1)).toTraineeProfileResponseDTO(trainee);
    }

    @Test
    void testGetTraineeByUsername_NotFound() {
        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.getTraineeByUsername("john.doe"));

        verify(userService, times(1)).getUserByUsername("john.doe");
        verify(traineeRepository, times(1)).findByUser_Id(1L);
    }

    @Test
    void testUpdateTraineeProfile() {
        when(traineeRepository.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainee));

        // Prepare an updated trainee response DTO
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

        verify(traineeRepository, times(1)).findByUser_Username("jane.smith");
        verify(traineeMapper, times(1)).toTraineeProfileResponseDTO(trainee);
    }

    @Test
    void testUpdateTraineeProfile_NotFound() {
        when(traineeRepository.findByUser_Username("jane.smith")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> traineeService.updateTraineeProfile(updateRequest));

        verify(traineeRepository, times(1)).findByUser_Username("jane.smith");
    }

    @Test
    void testDeleteTraineeProfileByUsername() {

        when(userService.getUserByUsername("john.doe")).thenReturn(user);
        when(traineeRepository.findByUser_Id(1L)).thenReturn(Optional.of(trainee));
        when(traineeMapper.toTraineeProfileResponseDTO(trainee)).thenReturn(traineeProfileResponseDTO);

        doNothing().when(userService).deleteUser("john.doe");

        traineeService.deleteTraineeProfileByUsername("john.doe");

        verify(userService, times(1)).deleteUser("john.doe");
    }

}