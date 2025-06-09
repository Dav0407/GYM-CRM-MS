package com.epam.gym_crm.dto.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTrainerProfileRequestDTOTest {

    @Test
    void testNoArgsConstructor() {
        CreateTrainerProfileRequestDTO dto = new CreateTrainerProfileRequestDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        CreateTrainerProfileRequestDTO dto = new CreateTrainerProfileRequestDTO(
                "John",
                "Doe",
                "Yoga"
        );

        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getTrainingType()).isEqualTo("Yoga");
    }

    @Test
    void testBuilder() {
        CreateTrainerProfileRequestDTO dto = CreateTrainerProfileRequestDTO.builder()
                .firstName("Alice")
                .lastName("Smith")
                .trainingType("Weightlifting")
                .build();

        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
        assertThat(dto.getTrainingType()).isEqualTo("Weightlifting");
    }

    @Test
    void testToString() {
        CreateTrainerProfileRequestDTO dto = CreateTrainerProfileRequestDTO.builder()
                .firstName("Test")
                .lastName("User")
                .trainingType("Cardio")
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("firstName=Test", "lastName=User", "trainingType=Cardio");
    }
}
