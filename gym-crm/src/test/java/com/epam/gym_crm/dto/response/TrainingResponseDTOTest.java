package com.epam.gym_crm.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TrainingResponseDTOTest {

    @Test
    void testNoArgsConstructor() {
        TrainingResponseDTO dto = new TrainingResponseDTO();
        assertThat(dto).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Date trainingDate = new Date();
        TrainingResponseDTO dto = new TrainingResponseDTO(
                1L,
                new TraineeResponseDTO(),
                new TrainerResponseDTO(),
                "Java Basics",
                "Technical",
                trainingDate,
                60
        );

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTrainingName()).isEqualTo("Java Basics");
        assertThat(dto.getTrainingType()).isEqualTo("Technical");
        assertThat(dto.getTrainingDate()).isEqualTo(trainingDate);
        assertThat(dto.getTrainingDuration()).isEqualTo(60);
    }

    @Test
    void testBuilder() {
        Date trainingDate = new Date();
        TrainingResponseDTO dto = TrainingResponseDTO.builder()
                .id(2L)
                .trainingName("Spring Boot")
                .trainingType("Technical")
                .trainingDate(trainingDate)
                .trainingDuration(90)
                .build();

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getTrainingName()).isEqualTo("Spring Boot");
        assertThat(dto.getTrainingType()).isEqualTo("Technical");
        assertThat(dto.getTrainingDate()).isEqualTo(trainingDate);
        assertThat(dto.getTrainingDuration()).isEqualTo(90);
    }

    @Test
    void testToString() {
        TrainingResponseDTO dto = TrainingResponseDTO.builder()
                .id(3L)
                .trainingName("React Native")
                .build();

        String dtoString = dto.toString();
        assertThat(dtoString).contains("id=3", "trainingName=React Native");
    }
}
