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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

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

    // ==================== saveUser Tests ====================

    @Test
    void saveUser_Successful() {
        // Given
        User userToSave = new User();
        userToSave.setUsername("testuser");

        when(userRepository.save(userToSave)).thenReturn(userToSave);

        // When
        User result = userService.saveUser(userToSave);

        // Then
        assertNotNull(result);
        assertEquals(userToSave, result);
        verify(userRepository).save(userToSave);
    }

    // ==================== changePassword Tests ====================

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
        assertEquals("newEncodedPassword", user.getPassword());
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

    // ==================== generateUsername Tests ====================

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
    void generateUsername_MultipleConflicts() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("john.doe1")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("john.doe2")).thenReturn(Optional.empty());

        String username = userService.generateUsername("John", "Doe");

        assertEquals("john.doe2", username);
        verify(userRepository).findByUsername("john.doe");
        verify(userRepository).findByUsername("john.doe1");
        verify(userRepository).findByUsername("john.doe2");
    }

    @Test
    void generateUsername_WithWhitespace() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        String username = userService.generateUsername("  John  ", "  Doe  ");

        assertEquals("john.doe", username);
        verify(userRepository).findByUsername("john.doe");
    }

    // ==================== generateRandomPassword Tests ====================

    @Test
    void generateRandomPassword_ReturnsValidPassword() {
        String password = userService.generateRandomPassword();

        assertNotNull(password);
        assertEquals(10, password.length());
        assertTrue(password.matches("[A-Za-z0-9]+"));
    }

    @Test
    void generateRandomPassword_GeneratesDifferentPasswords() {
        String password1 = userService.generateRandomPassword();
        String password2 = userService.generateRandomPassword();

        assertNotEquals(password1, password2);
    }

    // ==================== encryptPassword Tests ====================

    @Test
    void encryptPassword_ReturnsEncodedPassword() {
        String plainPassword = "plainPassword";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);

        String result = userService.encryptPassword(plainPassword);

        assertEquals(encodedPassword, result);
        verify(passwordEncoder).encode(plainPassword);
    }

    // ==================== updateStatus Tests ====================

    @Test
    void updateStatus_Successful() {
        when(userRepository.toggleStatus("john.doe")).thenReturn(1);

        userService.updateStatus("john.doe");

        verify(userRepository).toggleStatus("john.doe");
    }

    @Test
    void updateStatus_DatabaseException_ThrowsServiceException() {
        when(userRepository.toggleStatus("john.doe")).thenThrow(new RuntimeException("Database error"));

        assertThrows(ServiceException.class, () -> userService.updateStatus("john.doe"));
        verify(userRepository).toggleStatus("john.doe");
    }

    // ==================== deleteUser Tests ====================

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

    // ==================== getUserByUsername Tests ====================

    @Test
    void getUserByUsername_UserExists_ReturnsUser() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("john.doe");

        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByUsername("john.doe");
    }

    @Test
    void getUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("john.doe"));
        verify(userRepository).findByUsername("john.doe");
    }

    // ==================== login Tests ====================

    @Test
    void login_Successful() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        when(bruteForceProtectorService.isBlocked("john.doe")).thenReturn(false);
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

        verify(bruteForceProtectorService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
        verify(userMapper).toUserResponseDTO(user);
        verify(bruteForceProtectorService).loginSucceeded("john.doe");
    }

    @Test
    void login_UserBlocked_ThrowsLockedException() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("password")
                .build();

        when(bruteForceProtectorService.isBlocked("john.doe")).thenReturn(true);

        assertThrows(LockedException.class, () -> userService.login(request));
        verify(bruteForceProtectorService).isBlocked("john.doe");
        verifyNoInteractions(authenticationManager, jwtService, userMapper);
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("wrongPassword")
                .build();

        when(bruteForceProtectorService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException());

        assertThrows(UserNotFoundException.class, () -> userService.login(request));
        verify(bruteForceProtectorService).isBlocked("john.doe");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(bruteForceProtectorService).loginFailed("john.doe");
        verifyNoInteractions(jwtService, userMapper);
    }

    @Test
    void login_UsernameConvertedToLowerCase() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("JOHN.DOE")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        when(bruteForceProtectorService.isBlocked("JOHN.DOE")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        userService.login(request);

        verify(authenticationManager).authenticate(argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken &&
                        "john.doe".equals(token.getName())
        ));
    }

    // ==================== refreshToken Tests ====================

    @Test
    void refreshToken_Successful() throws IOException {
        String refreshToken = "validRefreshToken";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("john.doe");
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);
        when(response.getOutputStream()).thenReturn(outputStream);

        userService.refreshToken(request, response);

        verify(jwtService).blackListToken(refreshToken);
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
        verify(response).getOutputStream();
    }

    @Test
    void refreshToken_NoAuthHeader_DoesNothing() throws IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        userService.refreshToken(request, response);

        verifyNoInteractions(jwtService, userRepository, response);
    }

    @Test
    void refreshToken_InvalidAuthHeader_DoesNothing() throws IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Invalid header");

        userService.refreshToken(request, response);

        verifyNoInteractions(jwtService, userRepository, response);
    }


    @Test
    void refreshToken_UserNotFound_DoesNothing() {
        String refreshToken = "validRefreshToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("john.doe");
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.refreshToken(request, response));
        verify(userRepository).findByUsername("john.doe");
        verifyNoMoreInteractions(jwtService);
    }

    @Test
    void refreshToken_InvalidRefreshToken_DoesNothing() throws IOException {
        String refreshToken = "invalidRefreshToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn("john.doe");
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid(refreshToken, user)).thenReturn(false);

        userService.refreshToken(request, response);

        verify(jwtService).isRefreshTokenValid(refreshToken, user);
        verify(jwtService, never()).blackListToken(anyString());
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void refreshToken_NullUsername_DoesNothing() throws IOException {
        String refreshToken = "validRefreshToken";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + refreshToken);
        when(jwtService.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        userService.refreshToken(request, response);

        verify(jwtService).extractUsername(refreshToken);
        verifyNoInteractions(userRepository);
        verifyNoMoreInteractions(jwtService);
    }

    // ==================== Plain Password Management Tests ====================

    @Test
    void addPlainPassword_StoresPassword() {
        String username = "john.doe";
        String password = "plainPassword";

        userService.addPlainPassword(username, password);
        String retrievedPassword = userService.getPlainPassword(username);

        assertEquals(password, retrievedPassword);
    }

    @Test
    void getPlainPassword_NonExistentUser_ReturnsNull() {
        String retrievedPassword = userService.getPlainPassword("nonexistent");

        assertNull(retrievedPassword);
    }

    @Test
    void login_SetsPlainPasswordInResponse() {
        LogInRequestDTO request = LogInRequestDTO.builder()
                .username("john.doe")
                .password("password")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        userService.addPlainPassword("john.doe", "plainPassword");

        when(bruteForceProtectorService.isBlocked("john.doe")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        AuthenticationResponseDTO result = userService.login(request);

        assertEquals("plainPassword", result.getUser().getPassword());
    }
}