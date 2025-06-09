package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.service.TraineeService;
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

import java.text.SimpleDateFormat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TraineeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();
    }

    @Test
    void registerTrainee_ShouldReturnCreated() throws Exception {
        CreateTraineeProfileRequestDTO request = new CreateTraineeProfileRequestDTO();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
        request.setAddress("123 Main St");

        TraineeResponseDTO response = new TraineeResponseDTO();
        response.setUsername("john.doe");
        response.setPassword("password123");

        when(traineeService.createTraineeProfile(any(CreateTraineeProfileRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainees/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("password123"));
    }

    @Test
    void getTraineeProfile_WithValidCredentials_ShouldReturnProfile() throws Exception {
        String username = "john.doe";
        TraineeProfileResponseDTO response = new TraineeProfileResponseDTO();
        response.setFirstName("John");
        response.setLastName("Doe");

        User user = new User();
        user.setUsername(username);
        user.setPassword("validPass");

        when(traineeService.getTraineeByUsername(username)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/{username}", username)
                        .header("Username", username)
                        .header("Password", "validPass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void updateTraineeProfile_WithValidRequest_ShouldReturnUpdatedProfile() throws Exception {
        // Create complete valid request with all required fields
        UpdateTraineeProfileRequestDTO request = UpdateTraineeProfileRequestDTO.builder()
                .username("john.doe")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .dateOfBirth(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"))
                .address("123 Updated St")
                .isActive(true)  // Must not be null
                .build();

        // Mock response
        TraineeProfileResponseDTO response = TraineeProfileResponseDTO.builder()
                .username("john.doe")
                .firstName("John Updated")
                .lastName("Doe Updated")
                .birthDate(request.getDateOfBirth())
                .address("123 Updated St")
                .isActive(true)
                .build();

        // Mock user validation
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("validPass");

        when(traineeService.updateTraineeProfile(any(UpdateTraineeProfileRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees")
                        .header("Username", "john.doe")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // Keep this for debugging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.firstName").value("John Updated"))
                .andExpect(jsonPath("$.lastName").value("Doe Updated"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void deleteTraineeProfile_WithValidCredentials_ShouldReturnSuccess() throws Exception {
        String username = "john.doe";
        TraineeProfileResponseDTO response = new TraineeProfileResponseDTO();
        response.setFirstName("John");
        response.setLastName("Doe");

        User user = new User();
        user.setUsername(username);
        user.setPassword("validPass");

        when(traineeService.deleteTraineeProfileByUsername(username)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/trainees/{username}", username)
                        .header("Username", username)
                        .header("Password", "validPass"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void switchTraineeStatus_WithValidCredentials_ShouldReturnUpdatedProfile() throws Exception {
        String username = "john.doe";

        // Create complete response with isActive status
        TraineeProfileResponseDTO response = TraineeProfileResponseDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .username(username)
                .isActive(false)  // Note: Using isActive to match DTO
                .birthDate(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"))
                .address("123 Main St")
                .build();

        User user = new User();
        user.setUsername(username);
        user.setPassword("validPass");

        // Mock the service calls
        doNothing().when(traineeService).updateStatus(username);
        when(traineeService.getTraineeByUsername(username)).thenReturn(response);

        // Perform and verify
        mockMvc.perform(patch("/api/v1/trainees/{trainee-username}/status", username)
                        .header("Username", username)
                        .header("Password", "validPass"))
                .andDo(print())  // Keep for debugging
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").exists())  // Check field exists
                .andExpect(jsonPath("$.isActive").isBoolean())  // Verify type
                .andExpect(jsonPath("$.isActive").value(false));  // Verify value
    }
}