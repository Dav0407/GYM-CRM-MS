package com.epam.gym_crm.service;

import com.epam.gym_crm.dto.request.ChangePasswordRequestDTO;
import com.epam.gym_crm.dto.request.LogInRequestDTO;
import com.epam.gym_crm.dto.response.AuthenticationResponseDTO;
import com.epam.gym_crm.dto.response.UserResponseDTO;
import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.exception.InvalidPasswordException;
import com.epam.gym_crm.exception.UserNotFoundException;
import com.epam.gym_crm.mapper.UserMapper;
import com.epam.gym_crm.repository.UserRepository;
import com.epam.gym_crm.service.impl.BruteForceProtectorService;
import com.epam.gym_crm.service.impl.UserServiceImpl;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private BruteForceProtectorService bruteForceProtectorService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john.doe");
        user.setPassword("encodedPassword");

        userResponseDTO = UserResponseDTO.builder()
                .username("john.doe")
                .build();
    }

    @Test
    void changePassword_Successful() {
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .username("john.doe")
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        UserResponseDTO result = userService.changePassword(request);

        assertNotNull(result);
        assertEquals(userResponseDTO, result);
        verify(userRepository).findByUsername("john.doe");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userMapper).toUserResponseDTO(user);
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .username("john.doe")
                .oldPassword("oldPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changePassword(request));
        verify(userRepository).findByUsername("john.doe");
        verifyNoInteractions(passwordEncoder, userMapper);
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsException() {
        ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
                .username("john.doe")
                .oldPassword("wrongPassword")
                .newPassword("newPassword")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.changePassword(request));
        verify(userRepository).findByUsername("john.doe");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(userMapper);
    }

    @Test
    void generateUsername_UniqueUsername() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        String username = userService.generateUsername("John", "Doe");

        assertEquals("john.doe", username);
        verify(userRepository).findByUsername("john.doe");
    }

    @Test
    void generateUsername_UsernameConflict() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("john.doe1")).thenReturn(Optional.empty());

        String username = userService.generateUsername("John", "Doe");

        assertEquals("john.doe1", username);
        verify(userRepository).findByUsername("john.doe");
        verify(userRepository).findByUsername("john.doe1");
    }

    @Test
    void generateRandomPassword() {
        String password = userService.generateRandomPassword();

        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]+"));
    }

    @Test
    void updateStatus_Successful() {
        when(userRepository.toggleStatus("john.doe")).thenReturn(1);

        userService.updateStatus("john.doe");

        verify(userRepository).toggleStatus("john.doe");
    }

    @Test
    void updateStatus_UserNotFound_ThrowsException() {
        when(userRepository.toggleStatus("john.doe")).thenReturn(0);

        assertThrows(ServiceException.class, () -> userService.updateStatus("john.doe"));
        verify(userRepository).toggleStatus("john.doe");
    }

    @Test
    void deleteUser_Successful() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.deleteUser("john.doe");

        verify(userRepository).findByUsername("john.doe");
        verify(userRepository).deleteByUsername("john.doe");
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deleteUser("john.doe"));
        verify(userRepository).findByUsername("john.doe");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void login_Successful() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user); // This is the key part

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        AuthenticationResponseDTO result = userService.login(request);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals(userResponseDTO, result.getUser());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
        verify(userMapper).toUserResponseDTO(user);
    }


    @Test
    void login_InvalidCredentials_ThrowsException() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException());

        assertThrows(UserNotFoundException.class, () -> userService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userRepository, jwtService, userMapper);
    }
}