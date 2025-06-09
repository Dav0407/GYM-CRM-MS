package com.epam.gym_crm.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTrainerProfileRequestDTOTest {

    @Test
    void testNoArgsConstructor() {
        UpdateTrainerProfileRequestDTO dto = new UpdateTrainerProfileRequestDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        UpdateTrainerProfileRequestDTO dto = new UpdateTrainerProfileRequestDTO(
                "John",
                "Doe",
                "john_doe",
                "Strength Training",
                true
        );

        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getUsername()).isEqualTo("john_doe");
        assertThat(dto.getTrainingTypeName()).isEqualTo("Strength Training");
    }

    @Test
    void testBuilder() {
        UpdateTrainerProfileRequestDTO dto = UpdateTrainerProfileRequestDTO.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice_smith")
                .trainingTypeName("Cardio")
                .build();

        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
        assertThat(dto.getUsername()).isEqualTo("alice_smith");
        assertThat(dto.getTrainingTypeName()).isEqualTo("Cardio");
    }

    @Test
    void testToString() {
        UpdateTrainerProfileRequestDTO dto = UpdateTrainerProfileRequestDTO.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .username("charlie_brown")
                .trainingTypeName("Yoga")
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("firstName=Charlie", "lastName=Brown", "username=charlie_brown", "trainingTypeName=Yoga");
    }
}
