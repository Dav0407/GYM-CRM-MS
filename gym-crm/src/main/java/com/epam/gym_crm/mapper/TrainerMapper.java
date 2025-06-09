package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeSecureResponseDTO;
import com.epam.gym_crm.dto.response.TrainerProfileResponseDTO;
import com.epam.gym_crm.dto.response.TrainerResponseDTO;
import com.epam.gym_crm.dto.response.TrainerSecureResponseDTO;
import com.epam.gym_crm.entity.TraineeTrainer;
import com.epam.gym_crm.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Named("toTrainerResponseDTO")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.password", target = "password")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerResponseDTO toTrainerResponseDTO(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.password", target = "password")
    @Mapping(source = "user.isActive", target = "isActive")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    @Mapping(source = "traineeTrainers", target = "trainees", qualifiedByName = "mapTrainees")
    TrainerProfileResponseDTO toTrainerProfileResponseDTO(Trainer trainer);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "specialization.trainingTypeName", target = "specialization")
    TrainerSecureResponseDTO toTrainerSecureResponseDTO(Trainer trainer);

    @Named("mapTrainees")
    default List<TraineeSecureResponseDTO> mapTrainees(List<TraineeTrainer> traineeTrainers) {
        if (traineeTrainers == null) {
            return null;
        }
        return traineeTrainers.stream()
                .map(tt -> TraineeSecureResponseDTO.builder()
                        .username(tt.getTrainee().getUser().getUsername())
                        .firstName(tt.getTrainee().getUser().getFirstName())
                        .lastName(tt.getTrainee().getUser().getLastName())
                        .build())
                .toList();
    }
}
