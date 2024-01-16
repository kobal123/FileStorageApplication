package com.kobal.FileStorageApp.exceptions;

public class UserFileAlreadyExistsException extends RuntimeException {
    public UserFileAlreadyExistsException(String msg) {
        super(msg);
    }
    UserFileAlreadyExistsException() {}
}
