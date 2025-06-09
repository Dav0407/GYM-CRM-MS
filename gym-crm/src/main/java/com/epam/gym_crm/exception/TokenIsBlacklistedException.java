package com.epam.gym_crm.exception;

public class TokenIsBlacklistedException extends RuntimeException {
    public TokenIsBlacklistedException(String message) {
        super(message);
    }
}
