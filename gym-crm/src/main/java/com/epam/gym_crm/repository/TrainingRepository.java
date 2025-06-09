package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("SELECT t FROM Training t " +
            "WHERE t.trainee.user.username = :traineeUsername " +
            "AND t.trainer.user.username = COALESCE(:trainerUsername, t.trainer.user.username) " +
            "AND t.trainingDate >= COALESCE(:from, t.trainingDate) " +
            "AND t.trainingDate <= COALESCE(:to, t.trainingDate) " +
            "AND t.trainingType.trainingTypeName = COALESCE(:trainingTypeName, t.trainingType.trainingTypeName)")
    List<Training> findAllTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("trainerUsername") String trainerUsername,
            @Param("from") Date from,
            @Param("to") Date to,
            @Param("trainingTypeName") String trainingTypeName);

    @Query("SELECT t FROM Training t " +
            "WHERE t.trainer.user.username = :trainerUsername " +
            "AND t.trainee.user.username = COALESCE(:traineeUsername, t.trainee.user.username) " +
            "AND t.trainingDate >= COALESCE(:from, t.trainingDate) " +
            "AND t.trainingDate <= COALESCE(:to, t.trainingDate)")
    List<Training> findAllTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("traineeUsername") String traineeUsername,
            @Param("from") Date from,
            @Param("to") Date to);
}