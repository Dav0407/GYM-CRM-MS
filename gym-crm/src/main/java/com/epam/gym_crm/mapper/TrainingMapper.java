package com.epam.gym_crm.mapper;

import com.epam.gym_crm.dto.response.TraineeTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainerTrainingResponseDTO;
import com.epam.gym_crm.dto.response.TrainingResponseDTO;
import com.epam.gym_crm.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TraineeMapper.class, TrainerMapper.class})
public interface TrainingMapper {
    @Mapping(source = "trainee", target = "trainee", qualifiedByName = "toTraineeResponseDTO")
    @Mapping(source = "trainer", target = "trainer", qualifiedByName = "toTrainerResponseDTO")
    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    TrainingResponseDTO toTrainingResponseDTO(Training training);

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainer.user.username", target = "trainerName")
    TraineeTrainingResponseDTO toTraineeTrainingResponseDTO(Training training);

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingType")
    @Mapping(source = "trainee.user.username", target = "traineeName")
    TrainerTrainingResponseDTO toTrainerTrainingResponseDTO(Training training);
}