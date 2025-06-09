package com.epam.gym_crm.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    boolean isRefreshTokenValid(String token, UserDetails userDetails);
    boolean isRefreshToken(String token);
    boolean isAccessTokenValid(String token, UserDetails userDetails);
    void blackListToken(String token);
}
