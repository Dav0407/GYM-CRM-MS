package com.epam.gym_crm.dto.request;

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
public class UpdateTraineeProfileRequestDTO {

    @NotBlank(message = "First name can not be null or empty")
    private String firstName;

    @NotBlank(message = "Last name can not be null or empty")
    private String lastName;

    @NotBlank(message = "Username can not be null or empty")
    private String username;

    private Date dateOfBirth;
    private String address;

    @NotNull(message = "IsActive can not be null or empty")
    private Boolean isActive;
}
