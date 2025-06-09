package com.epam.trainer_session_management.controller;

import com.epam.trainer_session_management.dto.TrainerWorkloadRequest;
import com.epam.trainer_session_management.dto.TrainerWorkloadResponse;
import com.epam.trainer_session_management.enums.ActionType;
import com.epam.trainer_session_management.service.TrainerWorkingHoursService;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainerHoursControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TrainerWorkingHoursService trainerWorkingHoursService;

    @InjectMocks
    private TrainerHoursController trainerHoursController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerHoursController).build();
    }

    @Test
    void saveTrainerHours_WithValidRequest_ShouldReturnSuccess() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(createDate(3, 15))
                .trainingDuration(120)
                .actionType(ActionType.ADD)
                .build();

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .year("2024")
                .month("MARCH")
                .workingHours(2.0f)
                .build();

        when(trainerWorkingHoursService.calculateAndSave(any(TrainerWorkloadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/manage-working-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("MARCH"))
                .andExpect(jsonPath("$.workingHours").value(2.0));
    }

    @Test
    void saveTrainerHours_WithDeleteAction_ShouldReturnUpdatedHours() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("jane.smith")
                .trainerFirstName("Jane")
                .trainerLastName("Smith")
                .isActive(true)
                .trainingDate(createDate(4, 10))
                .trainingDuration(60)
                .actionType(ActionType.DELETE)
                .build();

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("jane.smith")
                .year("2024")
                .month("APRIL")
                .workingHours(1.5f)
                .build();

        when(trainerWorkingHoursService.calculateAndSave(any(TrainerWorkloadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/manage-working-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("jane.smith"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("APRIL"))
                .andExpect(jsonPath("$.workingHours").value(1.5));
    }

    @Test
    void saveTrainerHours_WithInactiveTrainer_ShouldHandleCorrectly() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("bob.wilson")
                .trainerFirstName("Bob")
                .trainerLastName("Wilson")
                .isActive(false)
                .trainingDate(createDate(5, 20))
                .trainingDuration(90)
                .actionType(ActionType.ADD)
                .build();

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("bob.wilson")
                .year("2024")
                .month("MAY")
                .workingHours(0.5f)
                .build();

        when(trainerWorkingHoursService.calculateAndSave(any(TrainerWorkloadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/manage-working-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("bob.wilson"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("MAY"))
                .andExpect(jsonPath("$.workingHours").value(0.5));
    }

    @Test
    void saveTrainerHours_WithWrongContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/v1/manage-working-hours")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getTrainerHours_WithValidParameters_ShouldReturnHours() throws Exception {
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .year("2024")
                .month("MARCH")
                .workingHours(2.0f)
                .build();

        when(trainerWorkingHoursService.getTrainerWorkingHours("john.doe", "2024", "MARCH"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", "john.doe")
                        .param("year", "2024")
                        .param("month", "MARCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("MARCH"))
                .andExpect(jsonPath("$.workingHours").value(2.0));
    }

    @Test
    void getTrainerHours_WithLowercaseMonth_ShouldReturnHours() throws Exception {
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("alice.brown")
                .year("2024")
                .month("april")
                .workingHours(3.5f)
                .build();

        when(trainerWorkingHoursService.getTrainerWorkingHours("alice.brown", "2024", "april"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", "alice.brown")
                        .param("year", "2024")
                        .param("month", "april"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("alice.brown"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("april"))
                .andExpect(jsonPath("$.workingHours").value(3.5));
    }

    @Test
    void getTrainerHours_WithMissingUsername_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("year", "2024")
                        .param("month", "MARCH"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainerHours_WithMissingYear_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", "john.doe")
                        .param("month", "MARCH"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainerHours_WithMissingMonth_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", "john.doe")
                        .param("year", "2024"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTrainerHours_WithZeroHours_ShouldReturnValidResponse() throws Exception {
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("charlie.davis")
                .year("2024")
                .month("JUNE")
                .workingHours(0.0f)
                .build();

        when(trainerWorkingHoursService.getTrainerWorkingHours("charlie.davis", "2024", "JUNE"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", "charlie.davis")
                        .param("year", "2024")
                        .param("month", "JUNE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("charlie.davis"))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("JUNE"))
                .andExpect(jsonPath("$.workingHours").value(0.0));
    }

    @Test
    void getTrainerHours_WithSpecialCharactersInUsername_ShouldReturnValidResponse() throws Exception {
        String usernameWithDots = "maria.garcia-lopez";
        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername(usernameWithDots)
                .year("2024")
                .month("JULY")
                .workingHours(4.0f)
                .build();

        when(trainerWorkingHoursService.getTrainerWorkingHours(usernameWithDots, "2024", "JULY"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/manage-working-hours")
                        .param("trainerUsername", usernameWithDots)
                        .param("year", "2024")
                        .param("month", "JULY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value(usernameWithDots))
                .andExpect(jsonPath("$.year").value("2024"))
                .andExpect(jsonPath("$.month").value("JULY"))
                .andExpect(jsonPath("$.workingHours").value(4.0));
    }

    @Test
    void saveTrainerHours_WithMinutesToHoursConversion_ShouldCalculateCorrectly() throws Exception {
        TrainerWorkloadRequest request = TrainerWorkloadRequest.builder()
                .trainerUsername("isabel.wilson")
                .trainerFirstName("Isabel")
                .trainerLastName("Wilson")
                .isActive(true)
                .trainingDate(createDate(9, 20))
                .trainingDuration(90) // 90 minutes = 1.5 hours
                .actionType(ActionType.ADD)
                .build();

        TrainerWorkloadResponse response = TrainerWorkloadResponse.builder()
                .trainerUsername("isabel.wilson")
                .year("2024")
                .month("SEPTEMBER")
                .workingHours(1.5f)
                .build();

        when(trainerWorkingHoursService.calculateAndSave(any(TrainerWorkloadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/manage-working-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("isabel.wilson"))
                .andExpect(jsonPath("$.workingHours").value(1.5));
    }

    // Helper method
    private Date createDate(int month, int day) {
        LocalDate localDate = LocalDate.of(2024, month, day);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}