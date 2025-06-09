package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTypeTest {
    @Test
    void testTrainingTypeBuilderAndGettersSetters() {
        // Create training type using builder
        TrainingType trainingType = TrainingType.builder()
                .id(1L)
                .trainingTypeName("Cardio")
                .build();

        // Test getters
        assertEquals(1L, trainingType.getId());
        assertEquals("Cardio", trainingType.getTrainingTypeName());

        // Test setters
        trainingType.setId(2L);
        trainingType.setTrainingTypeName("Strength Training");

        assertEquals(2L, trainingType.getId());
        assertEquals("Strength Training", trainingType.getTrainingTypeName());
    }

    @Test
    void testToString() {
        TrainingType trainingType = new TrainingType(1L, "Swimming");
        String toString = trainingType.toString();

        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Swimming"));
    }
}