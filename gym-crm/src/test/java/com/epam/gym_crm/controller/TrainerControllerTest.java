package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.service.TraineeTrainerService;
import com.epam.gym_crm.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainerControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeTrainerService traineeTrainerService;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
    }

    @Test
    void registerTrainer_ShouldReturnCreated() throws Exception {
        CreateTrainerProfileRequestDTO request = new CreateTrainerProfileRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setTrainingType("Fitness");

        TrainerResponseDTO response = TrainerResponseDTO.builder()
                .username("john.doe")
                .password("password123")
                .build();

        when(trainerService.createTrainerProfile(any(CreateTrainerProfileRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("password123"));
    }

    @Test
    void getTrainerProfile_WithValidCredentials_ShouldReturnProfile() throws Exception {
        String username = "john.doe";
        TrainerProfileResponseDTO response = TrainerProfileResponseDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .specialization("Fitness")
                .isActive(true)
                .build();

        User user = new User();
        user.setUsername(username);
        user.setPassword("validPass");

        when(trainerService.getTrainerByUsername(username)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/{username}", username)
                        .header("Username", username)
                        .header("Password", "validPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateTrainerProfile_WithValidRequest_ShouldReturnUpdatedProfile() throws Exception {
        UpdateTrainerProfileRequestDTO request = UpdateTrainerProfileRequestDTO.builder()
                .username("john.doe")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .trainingTypeName("Yoga")
                .isActive(true)
                .build();

        TrainerProfileResponseDTO response = TrainerProfileResponseDTO.builder()
                .firstName("John Updated")
                .lastName("Doe Updated")
                .specialization("Yoga")
                .isActive(true)
                .build();

        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("validPass");

        when(trainerService.updateTrainerProfile(any(UpdateTrainerProfileRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainers")
                        .header("Username", "john.doe")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getNotAssignedTrainers_ShouldReturnList() throws Exception {
        String traineeUsername = "trainee.user";
        List<TrainerSecureResponseDTO> trainers = Arrays.asList(
                TrainerSecureResponseDTO.builder()
                        .username("trainer1")
                        .firstName("Trainer")
                        .lastName("One")
                        .specialization("Fitness")
                        .build(),
                TrainerSecureResponseDTO.builder()
                        .username("trainer2")
                        .firstName("Trainer")
                        .lastName("Two")
                        .specialization("Yoga")
                        .build()
        );

        User user = new User();
        user.setUsername(traineeUsername);
        user.setPassword("validPass");

        when(trainerService.getNotAssignedTrainersByTraineeUsername(traineeUsername)).thenReturn(trainers);

        mockMvc.perform(get("/api/v1/trainers/not-assigned/{username}", traineeUsername)
                        .header("Username", traineeUsername)
                        .header("Password", "validPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("trainer1"))
                .andExpect(jsonPath("$[1].username").value("trainer2"));
    }

    @Test
    void updateTraineesTrainersList_ShouldReturnUpdatedList() throws Exception {
        // Given
        UpdateTrainerListRequestDTO request = UpdateTrainerListRequestDTO.builder()
                .traineeUsername("trainee.user")
                .trainerUsernames(Arrays.asList("trainer1", "trainer2"))
                .build();

        // Create complete response objects with all required fields
        List<TrainerSecureResponseDTO> updatedTrainers = Arrays.asList(
                TrainerSecureResponseDTO.builder()
                        .id(1L)
                        .username("trainer1")
                        .firstName("Trainer")
                        .lastName("One")
                        .specialization("Fitness")
                        .build(),
                TrainerSecureResponseDTO.builder()
                        .id(2L)
                        .username("trainer2")
                        .firstName("Trainer")
                        .lastName("Two")
                        .specialization("Yoga")
                        .build()
        );

        User user = new User();
        user.setUsername("trainee.user");
        user.setPassword("validPass");

        // Use doReturn().when() for more reliable stubbing
        doReturn(updatedTrainers).when(traineeTrainerService).updateTraineeTrainers(any(UpdateTrainerListRequestDTO.class));

        // When/Then
        mockMvc.perform(put("/api/v1/trainers/assign")
                        .header("Username", "trainee.user")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // Add this to print the response for debugging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("trainer1"))
                .andExpect(jsonPath("$[0].firstName").value("Trainer"))
                .andExpect(jsonPath("$[0].lastName").value("One"))
                .andExpect(jsonPath("$[1].username").value("trainer2"))
                .andExpect(jsonPath("$[1].firstName").value("Trainer"))
                .andExpect(jsonPath("$[1].lastName").value("Two"));

        verify(traineeTrainerService).updateTraineeTrainers(any(UpdateTrainerListRequestDTO.class));
    }

    @Test
    void switchTrainerStatus_ShouldReturnUpdatedProfile() throws Exception {
        String username = "john.doe";
        TrainerProfileResponseDTO response = TrainerProfileResponseDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .isActive(false)
                .build();

        User user = new User();
        user.setUsername(username);
        user.setPassword("validPass");

        doNothing().when(trainerService).updateStatus(username);
        when(trainerService.getTrainerByUsername(username)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/trainers/{trainer-username}/status", username)
                        .header("Username", username)
                        .header("Password", "validPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }
}