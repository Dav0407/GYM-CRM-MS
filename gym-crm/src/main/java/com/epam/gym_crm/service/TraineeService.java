package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import org.springframework.security.access.AccessDeniedException;

public interface TraineeService extends UserCreationService, TokenGenerationService<Trainee, TraineeResponseDTO> {
    TraineeResponseDTO createTraineeProfile(CreateTraineeProfileRequestDTO request);
    TraineeResponseDTO getTraineeById(Long id);
    TraineeProfileResponseDTO getTraineeByUsername(String username);
    Trainee getTraineeEntityByUsername(String username);
    TraineeProfileResponseDTO updateTraineeProfile(UpdateTraineeProfileRequestDTO request);
    void updateStatus(String username);
    TraineeProfileResponseDTO deleteTraineeProfileByUsername(String username);
    void checkOwnership(String requestedUsername) throws AccessDeniedException;
}
