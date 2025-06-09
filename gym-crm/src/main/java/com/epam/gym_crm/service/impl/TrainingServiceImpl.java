package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.AddTrainingRequestDTO;
import com.epam.gym_crm.dto.request.GetTraineeTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.GetTrainerTrainingsRequestDTO;
import com.epam.gym_crm.dto.request.TrainerWorkloadRequest;
import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerWorkloadResponse;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.entity.Training;
import com.epam.gym_crm.entity.TrainingType;
import com.epam.gym_crm.exception.ResourceNotFoundException;
import com.epam.gym_crm.mapper.TrainingMapper;
import com.epam.gym_crm.repository.TrainingRepository;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.TraineeTrainerService;
import com.epam.gym_crm.service.TrainerService;
import com.epam.gym_crm.service.TrainingService;
import com.epam.gym_crm.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainerWorkingHoursServiceImpl trainerWorkingHoursService;

    private final TrainingRepository trainingRepository;

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    private final TrainingTypeService trainingTypeService;
    private final TraineeTrainerService traineeTrainerService;
    private final TrainingMapper trainingMapper;

    @Override
    public List<TraineeTrainingResponseDTO> getTraineeTrainings(GetTraineeTrainingsRequestDTO request) {
        log.info("Fetching trainings for trainee: {}", request.getTraineeUsername());

        String traineeUsername = request.getTraineeUsername().trim();

        // Validate date range
        if (request.getFrom() != null && request.getTo() != null && request.getFrom().after(request.getTo())) {
            log.error("Invalid date range: 'from' date is after 'to' date. ");
            throw new IllegalArgumentException("Invalid date range: 'from' date cannot be after 'to' date.");
        }

        String trainerUsername = request.getTrainerUsername().trim();

        if (trainerService.getTrainerByUsername(trainerUsername) == null) {
            throw new IllegalArgumentException("Trainer not found with username: " + trainerUsername);
        }

        List<TraineeTrainingResponseDTO> trainings = trainingRepository.findAllTraineeTrainings(
                        traineeUsername, trainerUsername, request.getFrom(), request.getTo(), request.getTrainingType()
                ).stream()
                .map(trainingMapper::toTraineeTrainingResponseDTO)
                .toList();

        log.info("Found {} trainings for trainee: {}", trainings.size(), traineeUsername);

        return trainings;
    }


    @Override
    public List<TrainerTrainingResponseDTO> getTrainerTrainings(GetTrainerTrainingsRequestDTO request) {
        log.info("Fetching trainings for trainer: {}", request.getTrainerUsername());

        String trainerUsername = request.getTrainerUsername().trim();

        // Validate date range
        if (request.getFrom() != null && request.getTo() != null && request.getFrom().after(request.getTo())) {
            log.error("Invalid date range: 'from' date is after 'to' date.");
            throw new IllegalArgumentException("Invalid date range: 'from' date cannot be after 'to' date.");
        }

        // Check if trainer exists
        if (trainerService.getTrainerByUsername(trainerUsername) == null) {
            throw new IllegalArgumentException("Trainer not found with username: " + trainerUsername);
        }

        String traineeUsername = request.getTraineeUsername();

        if (traineeUsername != null && traineeService.getTraineeByUsername(traineeUsername) == null) {
            throw new IllegalArgumentException("Trainee not found with username: " + traineeUsername);
        }

        // Fetch trainings
        List<TrainerTrainingResponseDTO> trainings = trainingRepository.findAllTrainerTrainings(
                        trainerUsername, traineeUsername, request.getFrom(), request.getTo())
                .stream()
                .map(trainingMapper::toTrainerTrainingResponseDTO)
                .toList();

        log.info("Found {} trainings for trainer: {}", trainings.size(), trainerUsername);

        return trainings;
    }

    @Override
    public TrainingResponseDTO addTraining(AddTrainingRequestDTO request) {
        log.info("Adding new training...");

        var trainee = traineeService.getTraineeEntityByUsername(request.getTraineeUsername().trim());
        if (trainee == null) {
            throw new IllegalArgumentException("No trainee found with username: " + request.getTraineeUsername());
        }

        var trainer = trainerService.getTrainerEntityByUsername(request.getTrainerUsername().trim());
        if (trainer == null) {
            throw new IllegalArgumentException("No trainer found with username: " + request.getTrainerUsername());
        }

        TrainingType trainingType = trainingTypeService.findByValue(trainer.getSpecialization().getTrainingTypeName())
                .orElseThrow(() -> new IllegalArgumentException("Invalid training type: " + trainer.getSpecialization().getTrainingTypeName()));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(request.getTrainingDuration());
        training.setTrainingName(request.getTrainingName());

        TrainerWorkloadRequest trainerWorkloadRequest = trainer.getUser().getIsActive() ? addWorkingHours(training) : deleteWorkingHours(training);

        TrainerWorkloadResponse trainerWorkloadResponse = trainerWorkingHoursService.computeTrainerHours(trainerWorkloadRequest);
        log.info("Saved trainer workload: {}", trainerWorkloadResponse);

        Training savedTraining = trainingRepository.save(training);
        log.info("Training saved successfully with ID: {}", savedTraining.getId());

        traineeTrainerService.createTraineeTrainer(trainee.getUser().getUsername(), trainer.getUser().getUsername());

        return trainingMapper.toTrainingResponseDTO(savedTraining);
    }

    @Override
    public void deleteTraining(Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new ResourceNotFoundException("Training not found with id: " + trainingId));

        trainerWorkingHoursService.computeTrainerHours(deleteWorkingHours(training));

        trainingRepository.deleteById(trainingId);
    }

    private static TrainerWorkloadRequest addWorkingHours(Training training) {
        return TrainerWorkloadRequest.builder()
                .trainerUsername(training.getTrainer().getUser().getUsername())
                .trainerFirstName(training.getTrainer().getUser().getFirstName())
                .trainerLastName(training.getTrainer().getUser().getLastName())
                .isActive(training.getTrainer().getUser().getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(TrainerWorkloadRequest.ActionType.ADD)
                .build();
    }

    private static TrainerWorkloadRequest deleteWorkingHours(Training training) {
        return TrainerWorkloadRequest.builder()
                .trainerUsername(training.getTrainer().getUser().getUsername())
                .trainerFirstName(training.getTrainer().getUser().getFirstName())
                .trainerLastName(training.getTrainer().getUser().getLastName())
                .isActive(training.getTrainer().getUser().getIsActive())
                .trainingDate(training.getTrainingDate())
                .trainingDuration(training.getTrainingDuration())
                .actionType(TrainerWorkloadRequest.ActionType.DELETE)
                .build();
    }
}
