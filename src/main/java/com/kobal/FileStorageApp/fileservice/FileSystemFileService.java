package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FileSystemFileService implements FileService {

    private final int BUFFER_READ_SIZE = 8 * 1024;
    private  final Path BASE_PATH;


    public FileSystemFileService( @Value("${BASE_FILESYSTEM_PATH}") String basePath) {
        BASE_PATH = Path.of(basePath);
    }

    @Override
    public Optional<File> getFile(String username, Path path){
        Path actualPath = BASE_PATH.resolve(username).resolve(path);
        File file = new File(actualPath.toString());
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    @Override
    public void uploadFile(String username, Path path, InputStream fileInputStream) {
        Path filePath = BASE_PATH.resolve(username).resolve(path);

        validateDirectory(filePath.getParent());
        File file = filePath.toFile();
        boolean wasCreated;
        try {
            wasCreated = file.createNewFile();
        } catch (IOException ignored) {
            throw new UserFileException("Failed to upload file.");
        }

        if (!wasCreated)
            throw new RuntimeException("There was an error creating the file");

        try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile())) {
            byte[] buffer = new byte[BUFFER_READ_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("There was an error creating the file");
        }
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
    public List<File> getFilesinDirectory(String username, Path path) {
        Path filePath = BASE_PATH.resolve(username).resolve(path);
        validateDirectory(filePath.getParent());
        File directory = filePath.toFile();
        File[] files = directory.listFiles();

        if (files == null)
            throw new UserFileNotFoundException("file was not a directory");


        return List.of(files);
    }



    @Override
    public void deleteFileByName(String username, String filename) {

    }

    @Override
    public void deleteFilesInDirectory(String username, Path directoryPath, List<Path> files) {
        File directory = new File(directoryPath.toUri());
    }

    @Override
    public void deleteDirectoryByUsername(String username, Path directory) {

    }


    private void validateDirectory(Path directoryPath) {
        File directory = directoryPath.toFile();
        if (!directory.exists()) {
            throw new UserFileNotFoundException("File not found");
        }

        if (!directory.isDirectory()) {
            throw new UserFileNotFoundException("file was not a directory");
        }
    }
}
