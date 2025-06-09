package com.epam.gym_crm.service;

import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    private static final String TEST_USERNAME = "testuser";
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

        // Only mock what's absolutely necessary
        lenient().when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
    }

    @Test
    void generateAccessToken_withUserDetails_shouldGenerateValidToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
        assertFalse(jwtService.isRefreshToken(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("ROLE_USER", claims.get("role"));
        assertEquals("access", claims.get("token_type"));
    }

    @Test
    void generateAccessToken_withUser_shouldIncludeRole() {
        User user = new User();
        user.setUsername(TEST_USERNAME);
        user.setRole(User.Role.TRAINEE);

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("TRAINEE", claims.get("role"));
        assertEquals("access", claims.get("token_type"));
    }

    @Test
    void generateRefreshToken_shouldGenerateValidRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
        assertTrue(jwtService.isRefreshToken(token));

        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("refresh", claims.get("token_type"));
    }

    @Test
    void isAccessTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(userDetails);
        assertTrue(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void isAccessTokenValid_shouldReturnFalseForRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        assertFalse(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void isRefreshTokenValid_shouldReturnTrueForValidRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        assertTrue(jwtService.isRefreshTokenValid(token, userDetails));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalseForAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);
        assertFalse(jwtService.isRefreshTokenValid(token, userDetails));
    }

    @Test
    void blackListToken_shouldInvalidateToken() {
        String token = jwtService.generateAccessToken(userDetails);
        jwtService.blackListToken(token);

        assertFalse(jwtService.isAccessTokenValid(token, userDetails));

        // Verify token is in blacklist
        Map<String, Date> blacklist = (Map<String, Date>)
                ReflectionTestUtils.getField(jwtService, "blacklistedTokens");
        assertTrue(blacklist.containsKey(token));
    }

    @Test
    void extractClaim_shouldReturnRequestedClaim() {
        String token = jwtService.generateAccessToken(userDetails);
        Function<Claims, String> subjectExtractor = Claims::getSubject;

        assertEquals(TEST_USERNAME, jwtService.extractClaim(token, subjectExtractor));
    }
}