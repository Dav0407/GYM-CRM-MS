package com.epam.gym_crm.service.impl;

import com.epam.gym_crm.entity.User;
import com.epam.gym_crm.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    // Using ConcurrentHashMap for thread safety
    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    //decryption phrase for secret_key = Hello, hello my name is Igris004
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //Access token generation
    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "access");

        if (userDetails instanceof User) {
            claims.put("role", ((User) userDetails).getRole().toString());
        } else {
            claims.put("role", "ROLE_USER");
        }

        return buildToken(claims, userDetails, accessTokenExpiration);
    }

    //Refresh token would have slightly longer expiration than access token
    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        return buildToken(claims, userDetails, refreshTokenExpiration);
    }

    // by doing so, we are ensuring that user is not trying to access with refresh token
    @Override
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && isTokenNotExpired(token) && !isRefreshToken(token);
    }

    @Override
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && isTokenNotExpired(token) && isRefreshToken(token);
    }

    @Override
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("token_type").toString();
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    //Add token to blacklist
    @Override
    public void blackListToken(String token) {
        Date expirationDate = extractExpiration(token);
        blacklistedTokens.put(token, expirationDate);
    }

    // Clean up expired tokens from the blacklist periodically
    @Scheduled(fixedRate = 3600000) // Runs every hour
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }

    public boolean isTokenNotExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    //Check if token is blacklisted
    private boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}