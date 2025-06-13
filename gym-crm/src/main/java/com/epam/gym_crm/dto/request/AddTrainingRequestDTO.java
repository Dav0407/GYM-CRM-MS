package com.epam.gym_crm.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddTrainingRequestDTO {

    @NotBlank(message = "Trainee username can not be null or empty")
    private String traineeUsername;

    @NotBlank(message = "Trainer username can not be null or empty")
    private String trainerUsername;

    @NotBlank(message = "Training name can not be null or empty")
    private String trainingName;

    @NotNull(message = "Training date can not be null or empty")
    @Future(message = "trainingDate can not be in the past")
    private Date trainingDate;

    @Min(value = 30, message = "Training duration must be minimum 30 minutes")
    @Max(value = 240, message = "Training duration must be maximum 4 hours")
    @NotNull(message = "Training duration can not be null or empty")
    private Integer trainingDurationInMinutes;
}
