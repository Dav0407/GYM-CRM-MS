package com.epam.gym_crm.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TraineeTest {

    @Test
    void testTraineeBuilder() {
        User user = User.builder()
                .id(1L)
                .username("test@example.com")
                .build();

        Training training1 = new Training();
        Training training2 = new Training();

        TraineeTrainer traineeTrainer1 = new TraineeTrainer();
        TraineeTrainer traineeTrainer2 = new TraineeTrainer();

        Date dob = new Date();

        Trainee trainee = Trainee.builder()
                .id(1L)
                .dateOfBirth(dob)
                .address("Tashkent")
                .user(user)
                .trainings(Arrays.asList(training1, training2))
                .traineeTrainers(Arrays.asList(traineeTrainer1, traineeTrainer2))
                .build();

        assertThat(trainee.getId()).isEqualTo(1L);
        assertThat(trainee.getDateOfBirth()).isEqualTo(dob);
        assertThat(trainee.getAddress()).isEqualTo("Tashkent");
        assertThat(trainee.getUser()).isEqualTo(user);
        assertThat(trainee.getTrainings()).hasSize(2);
        assertThat(trainee.getTraineeTrainers()).hasSize(2);
    }

    @Test
    void testSettersAndGetters() {
        Trainee trainee = new Trainee();
        Date dob = new Date();

        trainee.setId(2L);
        trainee.setDateOfBirth(dob);
        trainee.setAddress("Samarkand");

        User user = new User();
        trainee.setUser(user);

        Training training = new Training();
        trainee.setTrainings(List.of(training));

        TraineeTrainer traineeTrainer = new TraineeTrainer();
        trainee.setTraineeTrainers(List.of(traineeTrainer));

        assertThat(trainee.getId()).isEqualTo(2L);
        assertThat(trainee.getDateOfBirth()).isEqualTo(dob);
        assertThat(trainee.getAddress()).isEqualTo("Samarkand");
        assertThat(trainee.getUser()).isEqualTo(user);
        assertThat(trainee.getTrainings()).containsExactly(training);
        assertThat(trainee.getTraineeTrainers()).containsExactly(traineeTrainer);
    }
}
