package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.dto.request.CreateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.request.UpdateTraineeProfileRequestDTO;
import com.epam.gym_crm.dto.response.TraineeProfileResponseDTO;
import com.epam.gym_crm.dto.response.TraineeResponseDTO;
import com.epam.gym_crm.entity.Trainee;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.TraineeMapper;
import com.epam.gym_crm.repository.TraineeRepository;
import com.epam.gym_crm.service.JwtService;
import com.epam.gym_crm.service.TraineeService;
import com.epam.gym_crm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeServiceImpl implements TraineeService {

    private final JwtService jwtService;
    private final TraineeRepository traineeRepository;
    private final TraineeMapper traineeMapper;
    private final UserService userService;

    @Transactional
    @Override
    public TraineeResponseDTO createTraineeProfile(CreateTraineeProfileRequestDTO request) {
        log.info("Creating new trainee profile for: {} {}", request.getFirstName(), request.getLastName());

        User user = createUser(request.getFirstName(), request.getLastName());

        Trainee trainee = Trainee.builder()
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress().trim())
                .user(user)
                .build();

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Trainee profile created successfully: {}", savedTrainee);


        TraineeResponseDTO response = getTraineeResponseDTO(savedTrainee);

        //Here It is important to give a plain password instead of hashed from db
        response.setPassword(userService.getPlainPassword(response.getUsername()));

        return addTokensToDTO(savedTrainee, response);
    }

    @Override
    public TraineeResponseDTO getTraineeById(Long id) {

        log.info("Fetching trainee by ID: {}", id);

        Trainee trainee = traineeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + id));

        return getTraineeResponseDTO(trainee);
    }

    @Override
    public TraineeProfileResponseDTO getTraineeByUsername(String username) {

        User userByUsername = userService.getUserByUsername(username);

        Trainee trainee = traineeRepository.findByUser_Id(userByUsername.getId())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + userByUsername.getUsername()));

        TraineeProfileResponseDTO traineeProfileResponseDTO = traineeMapper.toTraineeProfileResponseDTO(trainee);

        traineeProfileResponseDTO.setPassword(userService.getPlainPassword(username));

        return traineeProfileResponseDTO;
    }

    @Override
    public Trainee getTraineeEntityByUsername(String username) {
        User userByUsername = userService.getUserByUsername(username);

        return traineeRepository.findByUser_Id(userByUsername.getId())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + userByUsername.getUsername()));
    }

    @Transactional
    @Override
    public TraineeProfileResponseDTO updateTraineeProfile(UpdateTraineeProfileRequestDTO request) {
        Trainee trainee = traineeRepository.findByUser_Username(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Trainee not found with username: " + request.getUsername()));

        trainee.getUser().setFirstName(request.getFirstName().trim());
        trainee.getUser().setLastName(request.getLastName().trim());
        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress().trim());

        TraineeProfileResponseDTO traineeProfileResponseDTO = traineeMapper.toTraineeProfileResponseDTO(trainee);
        traineeProfileResponseDTO.setPassword(userService.getPlainPassword(request.getUsername()));

        return traineeProfileResponseDTO;
    }

    @Override
    public void updateStatus(String username) {
        userService.updateStatus(username);
    }

    @Transactional
    @Override
    public TraineeProfileResponseDTO deleteTraineeProfileByUsername(String username) {
        TraineeProfileResponseDTO traineeByUsername = getTraineeByUsername(username);

        traineeByUsername.setPassword(userService.getPlainPassword(username));

        userService.deleteUser(username);
        return traineeByUsername;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public User.Role getRole() {
        return User.Role.TRAINEE;
    }

    private TraineeResponseDTO getTraineeResponseDTO(Trainee trainee) {
        return traineeMapper.toTraineeResponseDTO(trainee);
    }

    @Override
    public JwtService getJwtService() {
        return jwtService;
    }

    @Override
    public User getUserFromEntity(Trainee entity) {
        return entity.getUser();
    }

    @Override
    public void setAccessToken(TraineeResponseDTO response, String accessToken) {
        response.setAccessToken(accessToken);
    }

    @Override
    public void setRefreshToken(TraineeResponseDTO response, String refreshToken) {
        response.setRefreshToken(refreshToken);
    }

    public void checkOwnership(String requestedUsername) throws AccessDeniedException {
        User connectedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authenticatedUsername = connectedUser.getUsername();
        if (!authenticatedUsername.equals(requestedUsername)) {
            throw new AccessDeniedException("You are not authorized to access this resource");
        }
    }
}
