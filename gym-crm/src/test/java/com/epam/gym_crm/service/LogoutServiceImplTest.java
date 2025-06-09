package com.epam.gym_crm.service;

import com.epam.gym_crm.service.impl.LogoutServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {

    private LogoutServiceImpl logoutService;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutServiceImpl(jwtService);
    }

    @Test
    void testLogout_WithValidBearerToken_ShouldBlacklistToken() {
        // Setup
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);

        // Execute
        logoutService.logout(request, response, authentication);

        // Verify
        verify(jwtService, times(1)).blackListToken(token);
    }

    @Test
    void testLogout_WithNullAuthHeader_ShouldNotBlacklistToken() {
        // Setup
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Execute
        logoutService.logout(request, response, authentication);

        // Verify
        verify(jwtService, never()).blackListToken(anyString());
    }

    @Test
    void testLogout_WithNonBearerToken_ShouldNotBlacklistToken() {
        // Setup
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

        // Execute
        logoutService.logout(request, response, authentication);

        // Verify
        verify(jwtService, never()).blackListToken(anyString());
    }

    @Test
    void testLogout_WithEmptyBearerToken_ShouldBlacklistEmptyString() {
        // Setup
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");

        // Execute
        logoutService.logout(request, response, authentication);

        // Verify
        verify(jwtService, times(1)).blackListToken("");
    }

    @Test
    void testLogout_WithWhitespacesInToken_ShouldPreserveWhitespaces() {
        // Setup
        String tokenWithSpaces = "token with spaces";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + tokenWithSpaces);

        // Execute
        logoutService.logout(request, response, authentication);

        // Verify
        verify(jwtService, times(1)).blackListToken(tokenWithSpaces);
    }

    @Test
    void testLogout_WithNullAuthentication_ShouldStillProcessToken() {
        // Setup
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);

        // Execute
        logoutService.logout(request, response, null);

        // Verify token is still blacklisted even with null authentication
        verify(jwtService, times(1)).blackListToken(token);
    }
}