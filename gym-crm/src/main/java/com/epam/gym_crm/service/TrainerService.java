package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.CreateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTrainerProfileRequestDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainer;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface TrainerService extends UserCreationService, TokenGenerationService<Trainer, TrainerResponseDTO> {
    TrainerResponseDTO createTrainerProfile(CreateTrainerProfileRequestDTO request);
    TrainerResponseDTO getTrainerById(Long id);
    TrainerProfileResponseDTO getTrainerByUsername(String username);
    Trainer getTrainerEntityByUsername(String username);
    TrainerProfileResponseDTO updateTrainerProfile(UpdateTrainerProfileRequestDTO request);
    void updateStatus(String username);
    List<TrainerSecureResponseDTO> getNotAssignedTrainersByTraineeUsername(String traineeUsername);
    TrainerResponseDTO getTrainerResponseDTO(Trainer trainer);
    void checkOwnership(String requestedUsername) throws AccessDeniedException;
}
