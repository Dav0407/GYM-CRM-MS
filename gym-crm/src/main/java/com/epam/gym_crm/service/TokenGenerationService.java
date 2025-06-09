package com.epam.gym_crm.service;

import com.epam.gym_crm.entity.User;


public interface TokenGenerationService<E, R> {

    JwtService getJwtService();

    default R addTokensToDTO(E entity, R response) {
        User user = getUserFromEntity(entity);
        JwtService jwtService = getJwtService();

        String accessToken = jwtService.generateAccessToken(user);

        String refreshToken = jwtService.generateRefreshToken(user);

        setAccessToken(response, accessToken);
        setRefreshToken(response, refreshToken);

        return response;
    }

    User getUserFromEntity(E entity);
    void setAccessToken(R response, String accessToken);
    void setRefreshToken(R response, String refreshToken);
}