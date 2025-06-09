package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, Long> {

    Optional<TrainingType> findByTrainingTypeNameIgnoreCase(String trainingTypeName);
}