package com.epam.trainer_session_management.repository;

import com.epam.trainer_session_management.document.TrainerWorkingHours;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainerWorkingHoursRepository extends MongoRepository<TrainerWorkingHours, String> {
}