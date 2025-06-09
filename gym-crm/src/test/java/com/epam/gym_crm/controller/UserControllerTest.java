package com.epam.gym_crm.controller;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.handler.GlobalExceptionHandler;
import com.epam.gym_crm.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        LogInRequestDTO request = new LogInRequestDTO();
        request.setUsername("testUser");
        request.setPassword("testPass");

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername("testUser");
        userResponseDTO.setIsActive(true);

        AuthenticationResponseDTO response = new AuthenticationResponseDTO();
        response.setUser(userResponseDTO);

        when(userService.login(any(LogInRequestDTO.class))).thenReturn(response);


        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testLogin_ValidationFailed_MissingUsername() throws Exception {
        // Given
        LogInRequestDTO request = new LogInRequestDTO();
        request.setPassword("testPass"); // username is missing

        // When & Then
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // Add this to see detailed error
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.errorMessage").value("One or more fields are invalid."));

        verify(userService, never()).login(any());
    }

    @Test
    void testChangePassword_Success() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass");

        UserResponseDTO response = new UserResponseDTO();
        response.setUsername("testUser");
        response.setIsActive(true);

        when(userService.changePassword(any(ChangePasswordRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testUser")
                        .header("Password", "oldPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService, times(1)).changePassword(any(ChangePasswordRequestDTO.class));
    }

    @Test
    void testChangePassword_ValidationFailed_MissingNewPassword() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("oldPass");
        // newPassword is missing

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .header("Username", "testUser")
                        .header("Password", "oldPass")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any());
    }

    @Test
    void testChangePassword_InvalidOldPassword() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("testUser");
        request.setOldPassword("wrongOldPass");
        request.setNewPassword("newPass");

        when(userService.changePassword(any(ChangePasswordRequestDTO.class)))
                .thenThrow(new InvalidPasswordException("Old password is incorrect."));

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()) // or isForbidden(), based on your exception handler
                .andExpect(jsonPath("$.businessErrorCode").exists())
                .andExpect(jsonPath("$.errorMessage").value("Old password is incorrect."));

        verify(userService).changePassword(any(ChangePasswordRequestDTO.class));
    }

    @Test
    void testChangePassword_UserNotFound() throws Exception {
        // Given
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setUsername("nonExistingUser");
        request.setOldPassword("somePass");
        request.setNewPassword("newPass");

        when(userService.changePassword(any(ChangePasswordRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found."));

        // When & Then
        mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.businessErrorCode").exists())
                .andExpect(jsonPath("$.errorMessage").value("User not found."));

        verify(userService).changePassword(any(ChangePasswordRequestDTO.class));
    }

}