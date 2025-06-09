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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainings")
@Tag(name = "Training API", description = "API endpoints for managing trainings and training types in the Gym CRM system")
public class TrainingController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    @Operation(summary = "Add a new training", description = "Creates a new training session with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Training created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainingResponseDTO> addTraining(@Valid @RequestBody
                                                           @Parameter(description = "Details for creating a new training session", required = true)
                                                           AddTrainingRequestDTO request) {
        TrainingResponseDTO response = trainingService.addTraining(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee trainings", description = "Retrieves a list of trainings for a specific trainee based on the provided criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Trainee trainings found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeTrainingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/trainees", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TraineeTrainingResponseDTO>> getTraineeTrainings(@Valid @RequestBody
                                                                                @Parameter(description = "Criteria for retrieving trainee trainings (e.g., username, date range)", required = true)
                                                                                GetTraineeTrainingsRequestDTO request) {
        traineeService.checkOwnership(request.getTraineeUsername());
        List<TraineeTrainingResponseDTO> response = trainingService.getTraineeTrainings(request);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @Operation(summary = "Get trainer trainings", description = "Retrieves a list of trainings for a specific trainer based on the provided criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Trainer trainings found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerTrainingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/trainers", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerTrainingResponseDTO>> getTrainerTrainings(@Valid @RequestBody
                                                                                @Parameter(description = "Criteria for retrieving trainer trainings (e.g., username, date range)", required = true)
                                                                                GetTrainerTrainingsRequestDTO request) {
        trainerService.checkOwnership(request.getTrainerUsername());
        List<TrainerTrainingResponseDTO> response = trainingService.getTrainerTrainings(request);
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @Operation(summary = "Get all training types", description = "Retrieves a list of all available training types in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Training types found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingTypeResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainingTypeResponseDTO>> getTrainingTypes() {
        List<TrainingTypeResponseDTO> response = trainingTypeService.getAllTrainingTypes();
        return ResponseEntity.status(HttpStatus.FOUND).body(response);
    }

    @Operation(summary = "Delete a training", description = "Deletes an existing training session by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid training ID"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Training not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(value = "/{trainingId}")
    public ResponseEntity<Void> deleteTraining(
            @Parameter(description = "ID of the training session to delete", required = true)
            @PathVariable Long trainingId) {
        trainingService.deleteTraining(trainingId);
        return ResponseEntity.ok().build();
    }

}