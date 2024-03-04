package com.kobal.FileStorageApp.exceptions;

public class UserStorageSpaceExecption extends RuntimeException {
    public UserStorageSpaceExecption(String message) {
        super(message);
    }

    public UserStorageSpaceExecption() {}
}
