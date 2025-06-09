package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingTypeResponseDTO;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.TrainerService;
import com.epam.gym_crm.service.TrainingService;
import com.epam.gym_crm.service.TrainingTypeService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();
    }

    @Test
    void addTraining_ShouldReturnCreated() throws Exception {
        AddTrainingRequestDTO request = AddTrainingRequestDTO.builder()
                .traineeUsername("trainee.user")
                .trainerUsername("trainer.user")
                .trainingName("Morning Workout")
                .trainingDate(dateFormat.parse("2023-12-31"))
                .trainingDuration(60)
                .build();

        TrainingResponseDTO response = TrainingResponseDTO.builder()
                .trainingName("Morning Workout")
                .trainingDate(request.getTrainingDate())
                .trainingDuration(60)
                .build();

        when(trainingService.addTraining(any(AddTrainingRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainings")
                        .header("Username", "trainer.user")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trainingName").value("Morning Workout"))
                .andExpect(jsonPath("$.trainingDuration").value(60));
    }

    @Test
    void getTraineeTrainings_ShouldReturnList() throws Exception {

        doNothing().when(traineeService).checkOwnership(anyString());

        GetTraineeTrainingsRequestDTO request = GetTraineeTrainingsRequestDTO.builder()
                .traineeUsername("trainee.user")
                .from(dateFormat.parse("2023-01-01"))
                .to(dateFormat.parse("2023-12-31"))
                .build();

        List<TraineeTrainingResponseDTO> trainings = Arrays.asList(
                TraineeTrainingResponseDTO.builder()
                        .trainingName("Morning Session")
                        .trainingDate(dateFormat.parse("2023-06-15"))
                        .trainingDuration(45)
                        .trainerName("John Trainer")
                        .trainingType("Cardio")
                        .build(),
                TraineeTrainingResponseDTO.builder()
                        .trainingName("Evening Session")
                        .trainingDate(dateFormat.parse("2023-06-20"))
                        .trainingDuration(60)
                        .trainerName("Jane Trainer")
                        .trainingType("Strength")
                        .build()
        );

        when(trainingService.getTraineeTrainings(any(GetTraineeTrainingsRequestDTO.class))).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainings/trainees")
                        .header("Username", "trainee.user")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Session"))
                .andExpect(jsonPath("$[1].trainingName").value("Evening Session"));
    }

    @Test
    void getTrainerTrainings_ShouldReturnList() throws Exception {

        doNothing().when(trainerService).checkOwnership(anyString());

        GetTrainerTrainingsRequestDTO request = GetTrainerTrainingsRequestDTO.builder()
                .trainerUsername("trainer.user")
                .from(dateFormat.parse("2023-01-01"))
                .to(dateFormat.parse("2023-12-31"))
                .build();

        List<TrainerTrainingResponseDTO> trainings = Arrays.asList(
                TrainerTrainingResponseDTO.builder()
                        .trainingName("Morning Session")
                        .trainingDate(dateFormat.parse("2023-06-15"))
                        .trainingDuration(45)
                        .traineeName("John Trainee")
                        .build(),
                TrainerTrainingResponseDTO.builder()
                        .trainingName("Evening Session")
                        .trainingDate(dateFormat.parse("2023-06-20"))
                        .trainingDuration(60)
                        .traineeName("Jane Trainee")
                        .build()
        );

        when(trainingService.getTrainerTrainings(any(GetTrainerTrainingsRequestDTO.class))).thenReturn(trainings);

        mockMvc.perform(post("/api/v1/trainings/trainers")
                        .header("Username", "trainer.user")
                        .header("Password", "validPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].traineeName").value("John Trainee"))
                .andExpect(jsonPath("$[1].traineeName").value("Jane Trainee"));

        verify(trainingService).getTrainerTrainings(any(GetTrainerTrainingsRequestDTO.class));
    }

    @Test
    void getTrainingTypes_ShouldReturnList() throws Exception {
        List<TrainingTypeResponseDTO> trainingTypes = Arrays.asList(
                TrainingTypeResponseDTO.builder()
                        .id(1L)
                        .trainingTypeName("Cardio")
                        .build(),
                TrainingTypeResponseDTO.builder()
                        .id(2L)
                        .trainingTypeName("Strength")
                        .build()
        );

        when(trainingTypeService.getAllTrainingTypes()).thenReturn(trainingTypes);

        mockMvc.perform(get("/api/v1/trainings/types")
                        .header("Username", "admin.user")
                        .header("Password", "validPass"))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$[0].trainingTypeName").value("Cardio"))
                .andExpect(jsonPath("$[1].trainingTypeName").value("Strength"));
    }
}
