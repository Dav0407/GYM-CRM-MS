package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.UpdateTrainerListRequestDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import com.epam.gym_crm.mapper.TrainerMapper;
import com.epam.gym_crm.repository.TraineeTrainerRepository;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.TraineeTrainerService;
import com.epam.gym_crm.service.TrainerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class TraineeTrainerServiceImpl implements TraineeTrainerService {

    private final TraineeTrainerRepository traineeTrainerRepository;
    private final TrainerMapper trainerMapper;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Override
    @Transactional
    public TraineeTrainer createTraineeTrainer(String traineeUsername, String trainerUsername) {
        log.info("Attempting to create a trainee-trainer relationship: Trainee = {}, Trainer = {}", traineeUsername, trainerUsername);

        // Retrieve trainee and trainer
        Trainee trainee = traineeService.getTraineeEntityByUsername(traineeUsername);
        Trainer trainer = trainerService.getTrainerEntityByUsername(trainerUsername);

        // Check if relationship already exists
        Optional<TraineeTrainer> existingRelation = traineeTrainerRepository.findByTraineeAndTrainer(trainee, trainer);
        if (existingRelation.isPresent()) {
            log.warn("Trainee-Trainer relationship already exists between {} and {}", traineeUsername, trainerUsername);
            return existingRelation.get();
        }

        // Create and save new relationship
        TraineeTrainer traineeTrainer = TraineeTrainer.builder()
                .trainee(trainee)
                .trainer(trainer)
                .build();

        TraineeTrainer savedTraineeTrainer = traineeTrainerRepository.save(traineeTrainer);
        log.info("Successfully created Trainee-Trainer relationship with ID: {}", savedTraineeTrainer.getId());

        return savedTraineeTrainer;
    }

    @Override
    public List<TraineeTrainer> findByTraineeUsername(String traineeUsername) {
        log.info("Fetching trainee-trainer relationships for trainee: {}", traineeUsername);
        return traineeTrainerRepository.findAllByTrainee_User_Username(traineeUsername);
    }

    @Override
    @Transactional
    public List<TrainerSecureResponseDTO> updateTraineeTrainers(UpdateTrainerListRequestDTO request) {

        log.info("Updating trainers list for trainee: {}", request.getTraineeUsername());

        // Fetch the trainee
        Trainee trainee = traineeService.getTraineeEntityByUsername(request.getTraineeUsername());
        if (trainee == null) {
            throw new IllegalArgumentException("Trainee not found: " + request.getTraineeUsername());
        }

        // Remove existing trainer relationships
        List<TraineeTrainer> existingRelations = traineeTrainerRepository.findAllByTrainee_User_Username(request.getTraineeUsername());
        traineeTrainerRepository.deleteAll(existingRelations);
        log.info("Removed {} existing trainer relations for trainee {}", existingRelations.size(), request.getTraineeUsername());
        // Add new trainer relationships
        List<TraineeTrainer> newRelations = request.getTrainerUsernames().stream()
                .map(trainerUsername -> {
                    Trainer trainer = trainerService.getTrainerEntityByUsername(trainerUsername);
                    if (trainer == null) {
                        log.warn("Trainer not found: {}", trainerUsername);
                        return null;
                    }
                    return TraineeTrainer.builder()
                            .trainer(trainer)
                            .trainee(trainee)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        traineeTrainerRepository.saveAll(newRelations);
        log.info("Added {} new trainers for trainee {}", newRelations.size(), request.getTraineeUsername());

        return newRelations.stream()
                .filter(traineeTrainer -> traineeTrainer.getTrainee().equals(trainee))
                .map(TraineeTrainer::getTrainer)
                .map(trainerMapper::toTrainerSecureResponseDTO)
                .toList();
    }

}
