package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeTrainerTest {
    @Test
    void testTraineeTrainerBuilderAndGettersSetters() {
        // Create required objects
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        // Create trainee-trainer using builder
        TraineeTrainer traineeTrainer = TraineeTrainer.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .build();

        // Test getters
        assertEquals(1L, traineeTrainer.getId());
        assertEquals(trainee, traineeTrainer.getTrainee());
        assertEquals(trainer, traineeTrainer.getTrainer());

        // Test setters
        Trainee newTrainee = new Trainee();
        Trainer newTrainer = new Trainer();

        traineeTrainer.setId(2L);
        traineeTrainer.setTrainee(newTrainee);
        traineeTrainer.setTrainer(newTrainer);

        assertEquals(2L, traineeTrainer.getId());
        assertEquals(newTrainee, traineeTrainer.getTrainee());
        assertEquals(newTrainer, traineeTrainer.getTrainer());
    }

    @Test
    void testToString() {
        TraineeTrainer traineeTrainer = new TraineeTrainer(1L, new Trainee(), new Trainer());
        String toString = traineeTrainer.toString();

        assertTrue(toString.contains("1"));
    }
}