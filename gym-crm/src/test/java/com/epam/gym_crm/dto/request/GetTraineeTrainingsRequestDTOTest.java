package com.epam.gym_crm.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class GetTraineeTrainingsRequestDTOTest {

    @Test
    void testNoArgsConstructor() {
        GetTraineeTrainingsRequestDTO dto = new GetTraineeTrainingsRequestDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Date fromDate = new Date();
        Date toDate = new Date();

        GetTraineeTrainingsRequestDTO dto = new GetTraineeTrainingsRequestDTO(
                "john_doe",
                fromDate,
                toDate,
                "trainer_01",
                "Cardio"
        );

        assertThat(dto.getTraineeUsername()).isEqualTo("john_doe");
        assertThat(dto.getTrainerUsername()).isEqualTo("trainer_01");
        assertThat(dto.getFrom()).isEqualTo(fromDate);
        assertThat(dto.getTo()).isEqualTo(toDate);
        assertThat(dto.getTrainingType()).isEqualTo("Cardio");
    }

    @Test
    void testBuilder() {
        Date fromDate = new Date();
        Date toDate = new Date();

        GetTraineeTrainingsRequestDTO dto = GetTraineeTrainingsRequestDTO.builder()
                .traineeUsername("alice_smith")
                .trainerUsername("trainer_02")
                .from(fromDate)
                .to(toDate)
                .trainingType("Strength")
                .build();

        assertThat(dto.getTraineeUsername()).isEqualTo("alice_smith");
        assertThat(dto.getTrainerUsername()).isEqualTo("trainer_02");
        assertThat(dto.getFrom()).isEqualTo(fromDate);
        assertThat(dto.getTo()).isEqualTo(toDate);
        assertThat(dto.getTrainingType()).isEqualTo("Strength");
    }

    @Test
    void testToString() {
        Date fromDate = new Date();
        Date toDate = new Date();

        GetTraineeTrainingsRequestDTO dto = GetTraineeTrainingsRequestDTO.builder()
                .traineeUsername("test_user")
                .trainerUsername("trainer_x")
                .from(fromDate)
                .to(toDate)
                .trainingType("HIIT")
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("traineeUsername=test_user", "trainerUsername=trainer_x", "trainingType=HIIT");
    }
}
