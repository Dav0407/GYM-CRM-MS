package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;

import java.util.List;

public interface TrainingService {
    List<TraineeTrainingResponseDTO> getTraineeTrainings(GetTraineeTrainingsRequestDTO request);
    List<TrainerTrainingResponseDTO> getTrainerTrainings(GetTrainerTrainingsRequestDTO request);
    TrainingResponseDTO addTraining(AddTrainingRequestDTO request);

    void deleteTraining(Long trainingId);
}
