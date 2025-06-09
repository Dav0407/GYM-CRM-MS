package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUser_Id(Long userId);
    Optional<Trainer> findByUser_Username(String username);

    @Query("SELECT t FROM Trainer t WHERE t NOT IN " +
            "(SELECT tt.trainer FROM TraineeTrainer tt WHERE tt.trainee.user.username = :traineeUsername) " +
            "AND t.user.isActive = true")
    List<Trainer> findUnassignedTrainersByTraineeUsername(@Param("traineeUsername") String traineeUsername);

}