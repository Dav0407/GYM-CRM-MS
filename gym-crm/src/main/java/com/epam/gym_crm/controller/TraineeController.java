package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.service.TraineeService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainees")
@Tag(name = "Trainee API", description = "API endpoints for managing trainee profiles in the Gym CRM system")
public class TraineeController {

    private final TraineeService traineeService;

    @Operation(summary = "Register a new trainee", description = "Creates a new trainee profile with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee profile created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TraineeResponseDTO> registerTrainee(@Valid @RequestBody
                                                              @Parameter(description = "Trainee profile creation details", required = true)
                                                              CreateTraineeProfileRequestDTO request) {
        TraineeResponseDTO response = traineeService.createTraineeProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee profile", description = "Retrieves the profile of a trainee by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Trainee profile found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TraineeProfileResponseDTO> getTraineeProfile(@PathVariable("username") @NotBlank(message = "Username is required")
                                                                       @Parameter(description = "Username of the trainee to retrieve", required = true, example = "john.doe")
                                                                       String username) {
        traineeService.checkOwnership(username);
        TraineeProfileResponseDTO response = traineeService.getTraineeByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Update trainee profile", description = "Updates an existing trainee profile with the provided details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Trainee profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TraineeProfileResponseDTO> updateTraineeProfile(@Valid @RequestBody @Parameter(description = "Trainee profile update details", required = true)
                                                                          UpdateTraineeProfileRequestDTO request) {
        traineeService.checkOwnership(request.getUsername());
        TraineeProfileResponseDTO response = traineeService.updateTraineeProfile(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Deletes a trainee profile by their username.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainee profile deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @DeleteMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TraineeProfileResponseDTO> deleteTraineeProfile(@PathVariable("username") @NotBlank(message = "Username is required")
                                                                          @Parameter(description = "Username of the trainee to delete", required = true, example = "john.doe")
                                                                          String username) {
        traineeService.checkOwnership(username);
        TraineeProfileResponseDTO response = traineeService.deleteTraineeProfileByUsername(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @Operation(summary = "Switch trainee status", description = "Toggles the active status of a trainee profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Trainee status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TraineeProfileResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "404", description = "Trainee not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PatchMapping(value = "/{trainee-username}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TraineeProfileResponseDTO> switchTraineeStatus(@PathVariable("trainee-username") @NotBlank(message = "Username is required")
                                                                         @Parameter(description = "Username of the trainee whose status will be toggled", required = true, example = "john.doe")
                                                                         String traineeUsername) {
        traineeService.checkOwnership(traineeUsername);
        traineeService.updateStatus(traineeUsername);
        TraineeProfileResponseDTO response = traineeService.getTraineeByUsername(traineeUsername);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}


