package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.filesecurity.SecurityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class FileSystemFileService implements FileService {

    private final SecurityService securityService;

    @Value("BASE_FILESYSTEM_PATH")
    private  String BASE_FILESYSTEM_PATH;

    public FileSystemFileService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Optional<File> getFile(String username, Path path){
        File file = new File(path.toUri());



        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    @Override
    public void uploadFile(String username, Path path, InputStream file) {
        boolean canAccess = securityService.authorize(username, path);
    }

    @Override
    public void createDirectory(String username, Path directoryPath) {
        File directory = new File(directoryPath.toUri());
        boolean wasCreated = directory.mkdir();

        if (!wasCreated) {
            throw new RuntimeException("Directory already exists");
        }
    }

    @Override
    public Stream<String> loadFileNamesFromDirectory(Path directoryPath) {
        File file = new File(directoryPath.toUri());

        if (!file.exists()) {
            throw new UserFileNotFoundException();
        }

        if (!file.isDirectory()) {
            throw new RuntimeException("file was not a directory");
        }

        return Stream.of(file.list());
    }



    @Override
    public void deleteFileByName(String username, String filename) {

    }

    @Override
    public void deleteFilesInDirectory(String username, Path directoryPath, List<Path> files) {
        boolean canAccess = securityService.authorize(username, directoryPath);

        File directory = new File(directoryPath.toUri());



    }

    @Override
    public void deleteDirectoryByUsername(String username, Path directory) {

    }
}
