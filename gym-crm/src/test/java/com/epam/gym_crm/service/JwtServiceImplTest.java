package com.epam.gym_crm.service;

import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetails otherUserDetails;

    private static final String TEST_USERNAME = "testuser";
    private static final String OTHER_USERNAME = "otheruser";
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_TOKEN_EXPIRATION = 3600000;
    private static final long REFRESH_TOKEN_EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);

        lenient().when(userDetails.getUsername()).thenReturn(TEST_USERNAME);
        lenient().when(otherUserDetails.getUsername()).thenReturn(OTHER_USERNAME);
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
    void isAccessTokenValid_shouldReturnFalseForBlacklistedToken() {
        String token = jwtService.generateAccessToken(userDetails);
        jwtService.blackListToken(token);

        assertFalse(jwtService.isAccessTokenValid(token, userDetails));
    }

    @Test
    void isAccessTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);

        assertFalse(jwtService.isAccessTokenValid(token, otherUserDetails));
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
    void isRefreshTokenValid_shouldReturnFalseForBlacklistedToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        jwtService.blackListToken(token);

        assertFalse(jwtService.isRefreshTokenValid(token, userDetails));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateRefreshToken(userDetails);

        assertFalse(jwtService.isRefreshTokenValid(token, otherUserDetails));
    }

    @Test
    void isRefreshToken_shouldReturnTrueForRefreshToken() {
        String token = jwtService.generateRefreshToken(userDetails);
        assertTrue(jwtService.isRefreshToken(token));
    }

    @Test
    void isRefreshToken_shouldReturnFalseForAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void isRefreshToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.isRefreshToken("invalid.token.here"));
    }

    @Test
    void blackListToken_shouldInvalidateToken() {
        String token = jwtService.generateAccessToken(userDetails);
        jwtService.blackListToken(token);

        assertFalse(jwtService.isAccessTokenValid(token, userDetails));

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

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateAccessToken(userDetails);

        assertEquals(TEST_USERNAME, jwtService.extractUsername(token));
    }

    @Test
    void extractAllClaims_shouldReturnAllTokenClaims() {
        String token = jwtService.generateAccessToken(userDetails);

        Claims claims = jwtService.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void isTokenNotExpired_shouldReturnTrueForValidToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertTrue(jwtService.isTokenNotExpired(token));
    }

    @Test
    void isTokenNotExpired_shouldReturnFalseForExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10L);

        String token = jwtService.generateAccessToken(userDetails);
        Thread.sleep(15);

        assertFalse(jwtService.isTokenNotExpired(token));

        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", ACCESS_TOKEN_EXPIRATION);
    }

    @Test
    void cleanupExpiredTokens_shouldRemoveExpiredTokensFromBlacklist() throws InterruptedException {
        String validToken = jwtService.generateAccessToken(userDetails);
        jwtService.blackListToken(validToken);

        Map<String, Date> blacklist = (Map<String, Date>)
                ReflectionTestUtils.getField(jwtService, "blacklistedTokens");

        Date pastDate = new Date(System.currentTimeMillis() - 10000);
        blacklist.put("expired-token", pastDate);

        assertEquals(2, blacklist.size());

        jwtService.cleanupExpiredTokens();

        assertEquals(1, blacklist.size());
        assertFalse(blacklist.containsKey("expired-token"));
        assertTrue(blacklist.containsKey(validToken));
    }

    @Test
    void extractAllClaims_shouldThrowExceptionForInvalidToken() {
        assertThrows(JwtException.class, () -> {
            jwtService.extractAllClaims("invalid.token.here");
        });
    }

    @Test
    void generateAccessToken_withUserRole_shouldIncludeCorrectRole() {
        User trainer = new User();
        trainer.setUsername(TEST_USERNAME);
        trainer.setRole(User.Role.TRAINER);

        String token = jwtService.generateAccessToken(trainer);
        Claims claims = jwtService.extractAllClaims(token);

        assertEquals("TRAINER", claims.get("role"));
    }

    @Test
    void blackListToken_shouldStoreCorrectExpirationDate() {
        String token = jwtService.generateAccessToken(userDetails);
        Date originalExpiration = jwtService.extractClaim(token, Claims::getExpiration);

        jwtService.blackListToken(token);

        Map<String, Date> blacklist = (Map<String, Date>)
                ReflectionTestUtils.getField(jwtService, "blacklistedTokens");

        assertEquals(originalExpiration, blacklist.get(token));
    }

    @Test
    void cleanupExpiredTokens_shouldHandleEmptyBlacklist() {
        Map<String, Date> blacklist = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(jwtService, "blacklistedTokens", blacklist);

        assertDoesNotThrow(() -> jwtService.cleanupExpiredTokens());

        assertEquals(0, blacklist.size());
    }
}