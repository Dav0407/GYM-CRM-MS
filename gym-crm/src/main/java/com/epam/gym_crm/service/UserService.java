package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface UserService {
    String generateUsername(String firstName, String lastName);

    String generateRandomPassword();

    String encryptPassword(String password);

    User saveUser(User user);

    UserResponseDTO changePassword(ChangePasswordRequestDTO request);

    void updateStatus(String username);

    void deleteUser(String username);

    User getUserByUsername(String username);

    AuthenticationResponseDTO login(LogInRequestDTO request);

    void refreshToken(HttpServletRequest request, HttpServletResponse response)  throws IOException;

    String getPlainPassword(String username);

    void addPlainPassword(String username, String password);
}
