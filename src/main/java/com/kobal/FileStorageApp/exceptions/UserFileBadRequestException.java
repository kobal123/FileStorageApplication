package com.kobal.FileStorageApp.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UserFileBadRequestException extends RuntimeException {
    public UserFileBadRequestException(String s) {
        super(s);
    }
}
