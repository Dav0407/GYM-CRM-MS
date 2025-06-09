package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrainingTest {
    @Test
    void testTrainingBuilderAndGettersSetters() {
        // Create required objects
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("CrossFit");

        Date trainingDate = new Date();

        // Create training using builder
        Training training = Training.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning Workout")
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDuration(60)
                .build();

        // Test getters
        assertEquals(1L, training.getId());
        assertEquals(trainee, training.getTrainee());
        assertEquals(trainer, training.getTrainer());
        assertEquals("Morning Workout", training.getTrainingName());
        assertEquals(trainingType, training.getTrainingType());
        assertEquals(trainingDate, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());

        // Test setters
        Trainee newTrainee = new Trainee();
        Trainer newTrainer = new Trainer();
        TrainingType newTrainingType = new TrainingType();
        newTrainingType.setTrainingTypeName("HIIT");
        Date newDate = new Date(trainingDate.getTime() + 86400000); // Next day

        training.setId(2L);
        training.setTrainee(newTrainee);
        training.setTrainer(newTrainer);
        training.setTrainingName("Evening Session");
        training.setTrainingType(newTrainingType);
        training.setTrainingDate(newDate);
        training.setTrainingDuration(45);

        assertEquals(2L, training.getId());
        assertEquals(newTrainee, training.getTrainee());
        assertEquals(newTrainer, training.getTrainer());
        assertEquals("Evening Session", training.getTrainingName());
        assertEquals(newTrainingType, training.getTrainingType());
        assertEquals(newDate, training.getTrainingDate());
        assertEquals(45, training.getTrainingDuration());
    }
}