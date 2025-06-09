package com.epam.gym_crm.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTraineeProfileRequestDTOTest {

    @Test
    void testNoArgsConstructor() {
        CreateTraineeProfileRequestDTO dto = new CreateTraineeProfileRequestDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Date dob = new Date();
        CreateTraineeProfileRequestDTO dto = new CreateTraineeProfileRequestDTO(
                "John",
                "Doe",
                dob,
                "123 Main St, City"
        );

        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getDateOfBirth()).isEqualTo(dob);
        assertThat(dto.getAddress()).isEqualTo("123 Main St, City");
    }

    @Test
    void testBuilder() {
        Date dob = new Date();
        CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth(dob)
                .address("456 Elm St, Town")
                .build();

        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
        assertThat(dto.getDateOfBirth()).isEqualTo(dob);
        assertThat(dto.getAddress()).isEqualTo("456 Elm St, Town");
    }

    @Test
    void testToString() {
        CreateTraineeProfileRequestDTO dto = CreateTraineeProfileRequestDTO.builder()
                .firstName("Test")
                .lastName("User")
                .address("789 Oak St, Village")
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("firstName=Test", "lastName=User", "address=789 Oak St, Village");
    }
}
