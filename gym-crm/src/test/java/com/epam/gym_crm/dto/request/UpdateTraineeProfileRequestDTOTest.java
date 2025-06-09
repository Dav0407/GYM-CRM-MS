package com.epam.gym_crm.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateTraineeProfileRequestDTOTest {

    @Test
    void testNoArgsConstructor() {
        UpdateTraineeProfileRequestDTO dto = new UpdateTraineeProfileRequestDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Date dob = new Date();

        UpdateTraineeProfileRequestDTO dto = new UpdateTraineeProfileRequestDTO(
                "John",
                "Doe",
                "john_doe",
                dob,
                "123 Main St",
                true
        );

        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getUsername()).isEqualTo("john_doe");
        assertThat(dto.getDateOfBirth()).isEqualTo(dob);
        assertThat(dto.getAddress()).isEqualTo("123 Main St");
        assertThat(dto.getIsActive()).isEqualTo(true);
    }

    @Test
    void testBuilder() {
        Date dob = new Date();

        UpdateTraineeProfileRequestDTO dto = UpdateTraineeProfileRequestDTO.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("alice_smith")
                .dateOfBirth(dob)
                .address("456 Elm St")
                .isActive(true)
                .build();

        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
        assertThat(dto.getUsername()).isEqualTo("alice_smith");
        assertThat(dto.getDateOfBirth()).isEqualTo(dob);
        assertThat(dto.getAddress()).isEqualTo("456 Elm St");
        assertThat(dto.getIsActive()).isEqualTo(true);
    }

    @Test
    void testToString() {
        Date dob = new Date();

        UpdateTraineeProfileRequestDTO dto = UpdateTraineeProfileRequestDTO.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .username("charlie_brown")
                .dateOfBirth(dob)
                .address("789 Pine St")
                .isActive(true)
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("firstName=Charlie", "lastName=Brown", "username=charlie_brown");
    }
}
