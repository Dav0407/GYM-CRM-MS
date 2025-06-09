package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {
    @Test
    void testUserBuilderAndGettersSetters() {
        // Create user with builder
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        // Test getters
        assertEquals(1L, user.getId());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertTrue(user.getIsActive());

        // Test setters
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setPassword("newpassword");
        user.setIsActive(false);

        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertEquals("newpassword", user.getPassword());
        assertFalse(user.getIsActive());

        // Test UserDetails implementation
        assertTrue(user.getAuthorities().isEmpty());
    }

    @Test
    void testUserRelationships() {
        User user = new User();
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        user.setTrainee(trainee);
        user.setTrainer(trainer);

        assertEquals(trainee, user.getTrainee());
        assertEquals(trainer, user.getTrainer());
    }
}