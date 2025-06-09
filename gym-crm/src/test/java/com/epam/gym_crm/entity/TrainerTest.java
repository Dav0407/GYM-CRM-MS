package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrainerTest {
    @Test
    void testTrainerBuilderAndGettersSetters() {
        // Create required objects
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Trainer");

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Yoga");

        // Create trainer using builder
        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(specialization)
                .traineeTrainers(new ArrayList<>())
                .build();

        // Test getters
        assertEquals(1L, trainer.getId());
        assertEquals(user, trainer.getUser());
        assertEquals(specialization, trainer.getSpecialization());
        assertNotNull(trainer.getTraineeTrainers());

        // Test setters
        User newUser = new User();
        newUser.setFirstName("Jane");
        newUser.setLastName("Coach");

        TrainingType newSpecialization = new TrainingType();
        newSpecialization.setTrainingTypeName("Pilates");

        trainer.setId(2L);
        trainer.setUser(newUser);
        trainer.setSpecialization(newSpecialization);

        assertEquals(2L, trainer.getId());
        assertEquals(newUser, trainer.getUser());
        assertEquals(newSpecialization, trainer.getSpecialization());
    }
}