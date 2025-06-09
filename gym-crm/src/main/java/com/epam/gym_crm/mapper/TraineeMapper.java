package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.TraineeTrainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    @Named("toTraineeResponseDTO")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.password", target = "password")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "dateOfBirth", target = "birthDate")
    TraineeResponseDTO toTraineeResponseDTO(Trainee trainee);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.password", target = "password")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "dateOfBirth", target = "birthDate")
    @Mapping(source = "traineeTrainers", target = "trainers", qualifiedByName = "mapTrainers")
    TraineeProfileResponseDTO toTraineeProfileResponseDTO(Trainee trainee);

    @Named("mapTrainers")
    default List<TrainerSecureResponseDTO> mapTrainers(List<TraineeTrainer> traineeTrainers) {
        if (traineeTrainers == null) {
            return null;
        }
        return traineeTrainers.stream()
                .map(tt -> TrainerSecureResponseDTO.builder()
                        .id(tt.getTrainer().getId())
                        .firstName(tt.getTrainer().getUser().getFirstName())
                        .lastName(tt.getTrainer().getUser().getLastName())
                        .username(tt.getTrainer().getUser().getUsername())
                        .specialization(tt.getTrainer().getSpecialization().getTrainingTypeName())
                        .build())
                .toList();
    }
}