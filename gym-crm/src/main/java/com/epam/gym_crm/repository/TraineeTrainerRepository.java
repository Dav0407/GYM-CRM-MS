package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraineeTrainerRepository extends JpaRepository<TraineeTrainer, Long> {
    Optional<TraineeTrainer> findByTraineeAndTrainer(Trainee trainee, Trainer trainer);
    List<TraineeTrainer> findAllByTrainee_User_Username(String username);
}