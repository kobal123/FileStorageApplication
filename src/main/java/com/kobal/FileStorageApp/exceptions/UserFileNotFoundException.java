package com.kobal.FileStorageApp.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No file was found with this name.")
public class UserFileNotFoundException extends RuntimeException{

    public UserFileNotFoundException(String message) {
        super(message);
    }
}
