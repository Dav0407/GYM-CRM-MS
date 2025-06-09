package com.epam.gym_crm.repository;

import com.epam.gym_crm.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUser_Id(Long userId);
    Optional<Trainee> findByUser_Username(String userUsername);
}