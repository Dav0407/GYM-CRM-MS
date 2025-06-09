package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.service.TraineeTrainerService;
import com.epam.gym_crm.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainers")
@Tag(name = "Trainer API", description = "API endpoints for managing trainer profiles and assignments in the Gym CRM system")
public class TrainerController {

    private final TrainerService trainerService;
    private final TraineeTrainerService traineeTrainerService;

    @Operation(summary = "Register a new trainer", description = "Creates a new trainer profile with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer profile created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerResponseDTO> registerTrainer(@Valid @RequestBody
                                                              @Parameter(description = "Trainer profile creation details", required = true)
                                                              CreateTrainerProfileRequestDTO request) {
        TrainerResponseDTO response = trainerService.createTrainerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainer profile", description = "Retrieves the profile of a trainer by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Trainer profile found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> getTrainerProfile(@PathVariable("username") @NotBlank(message = "Username is required")
                                                                       @Parameter(description = "Username of the trainer to retrieve", required = true, example = "jane.smith")
                                                                       String username) {
        trainerService.checkOwnership(username);
        TrainerProfileResponseDTO response = trainerService.getTrainerByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Update trainer profile", description = "Updates an existing trainer profile with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Trainer profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> updateTrainerProfile(@Valid @RequestBody @Parameter(description = "Trainer profile update details", required = true)
                                                                          UpdateTrainerProfileRequestDTO request) {
        trainerService.checkOwnership(request.getUsername());
        TrainerProfileResponseDTO response = trainerService.updateTrainerProfile(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get unassigned trainers", description = "Retrieves a list of trainers not assigned to a specific trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "List of unassigned trainers found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerSecureResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/not-assigned/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerSecureResponseDTO>> getNotAssignedTrainers(@PathVariable("username") @NotBlank(message = "Username is required")
                                                                                 @Parameter(description = "Username of the trainee to check unassigned trainers for", required = true, example = "john.doe")
                                                                                 String username) {
        trainerService.checkOwnership(username);
        List<TrainerSecureResponseDTO> response = trainerService.getNotAssignedTrainersByTraineeUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Update trainee's trainer list", description = "Updates the list of trainers assigned to a trainee.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Trainer list updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerSecureResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(value = "/assign", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TrainerSecureResponseDTO>> updateTraineesTrainersList(@Valid @RequestBody @Parameter(description = "Details for updating the trainer list for a trainee", required = true)
                                                                                     UpdateTrainerListRequestDTO request) {
        trainerService.checkOwnership(request.getTraineeUsername());
        List<TrainerSecureResponseDTO> response = traineeTrainerService.updateTraineeTrainers(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Switch trainer status", description = "Toggles the active status of a trainer profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Trainer status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainerProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainer not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping(value = "/{trainer-username}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TrainerProfileResponseDTO> switchTrainerStatus(@PathVariable("trainer-username") @NotBlank(message = "Username is required")
                                                                         @Parameter(description = "Username of the trainer whose status will be toggled", required = true, example = "jane.smith")
                                                                         String trainerUsername) {
        trainerService.checkOwnership(trainerUsername);
        trainerService.updateStatus(trainerUsername);
        TrainerProfileResponseDTO response = trainerService.getTrainerByUsername(trainerUsername);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
