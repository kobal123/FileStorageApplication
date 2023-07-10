package com.kobal.FileStorageApp.filesecurity;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Objects;

@Service
@Aspect
public class FileSystemSecurityService implements SecurityService{

    //@Before("execution(* com.example.YourClass.*(..)) && args(String, param2)")
    @Before("execution(* com.kobal.FileStorageApp.fileservice.FileSystemFileService.*(..)) && args(username, filepath, ..)")
    @Override
    public boolean authorize(String username, java.nio.file.Path filepath) {
        //TODO: if multiple users can access the same directories, this won't work.
        System.out.println("ASPECT CALLED");
        return Objects.equals(username, filepath.getName(0).toString());
    }
}
