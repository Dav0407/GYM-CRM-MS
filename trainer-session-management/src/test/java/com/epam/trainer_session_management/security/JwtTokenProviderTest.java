package com.epam.trainer_session_management.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Secret = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", base64Secret);
    }

    private String generateToken(String username, String role, long validityMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + validityMillis);

        return Jwts.builder().subject(username)
                .claim("role", role).issuedAt(now).expiration(exp)
                .signWith(secretKey)
                .compact();
    }

    @Test
    void testExtractUsername() {
        String token = generateToken("trainer@example.com", "TRAINER", 10000);
        String username = jwtTokenProvider.extractUsername(token);
        assertEquals("trainer@example.com", username);
    }

    @Test
    void testExtractRole() {
        String token = generateToken("trainer@example.com", "TRAINER", 10000);
        String role = jwtTokenProvider.extractRole(token);
        assertEquals("TRAINER", role);
    }

    @Test
    void testIsValidTrainerToken_validToken() {
        String token = generateToken("trainer@example.com", "TRAINER", 10000);
        assertTrue(jwtTokenProvider.isValidTrainerToken(token));
    }

    @Test
    void testIsValidTrainerToken_invalidRole() {
        String token = generateToken("user@example.com", "CLIENT", 10000);
        assertFalse(jwtTokenProvider.isValidTrainerToken(token));
    }

    @Test
    void testIsValidTrainerToken_expiredToken() {
        String token = generateToken("trainer@example.com", "TRAINER", -10000); // Already expired
        assertFalse(jwtTokenProvider.isValidTrainerToken(token));
    }

    @Test
    void testIsValidTrainerToken_malformedToken() {
        String malformedToken = "not.a.valid.token";
        assertFalse(jwtTokenProvider.isValidTrainerToken(malformedToken));
    }
}
