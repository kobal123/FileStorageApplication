package com.kobal.FileStorageApp.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus (HttpStatus.INTERNAL_SERVER_ERROR)
public class UserFileException extends  RuntimeException{
    public UserFileException(String s) {
        super(s);
    }
}
