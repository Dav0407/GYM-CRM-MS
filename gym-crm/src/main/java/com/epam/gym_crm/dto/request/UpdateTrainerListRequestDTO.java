package com.epam.gym_crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTrainerListRequestDTO {

    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotNull(message = "Trainer list is required")
    private List<String> trainerUsernames;
}
