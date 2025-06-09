package com.epam.gym_crm.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Getter
public enum BusinessErrorCodes {

    USER_UNAUTHORIZED(401, UNAUTHORIZED, "User unauthorized"),
    INTERNAL_ERROR(500, INTERNAL_SERVER_ERROR, "Something went wrong in the server side, it is not you."),
    USER_NOT_FOUND(404, NOT_FOUND, "User with these credentials does not exist"),
    RESOURCE_NOT_FOUND(404, NOT_FOUND, "Resource with these credentials does not exist"),
    VALIDATION_FAILED(400, BAD_REQUEST, "Validation failed"),
    ACCESS_FORBIDDEN(403, FORBIDDEN, "Access forbidden");

    private final int code;

    private final HttpStatus httpStatus;

    private final String description;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }

}
