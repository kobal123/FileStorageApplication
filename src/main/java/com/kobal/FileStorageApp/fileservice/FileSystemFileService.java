package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public void uploadFile(String username, Path uploadFilePath, InputStream fileInputStream) {
        Path filePath = BASE_PATH.resolve(username).resolve(uploadFilePath);

        validateDirectory(filePath.getParent());
        File file = filePath.toFile();
        boolean wasCreated;
        try {
            wasCreated = file.createNewFile();
        } catch (IOException ignored) {
            throw new UserFileException("Failed to upload file.");
        }

        if (!wasCreated)
            throw new UserFileException("There was an error creating the file");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_READ_SIZE];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            file.delete();
            throw new UserFileException("There was an error creating the file");
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
    public List<String> deleteFilesInDirectory(String username, Path directory, List<String> fileNames) {
        Path directoryPath = BASE_PATH.resolve(username).resolve(directory);
        validateDirectory(directoryPath);
        List<Path> filesToDelete = fileNames.stream()
                .map(directoryPath::resolve)
                .toList();


        List<String> failedDeletion = new ArrayList<>();

        for (Path filePath : filesToDelete) {
            File file = filePath.toFile();

            if (file.isDirectory()) {
                boolean success = FileSystemUtils.deleteRecursively(file);
                if (!success)
                    failedDeletion.add(file.getName());
            } else {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    failedDeletion.add(file.getName());
                }
            }
        }
        return failedDeletion;


//        try {
//                List<Path> directoriesToDelete = new ArrayList<>();
//                Files.walkFileTree(directoryPath, new SimpleFileVisitor<>() {
//                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                        if (filesToDelete.contains(file)) {
//                            Files.delete(file);
//                            successfullyDeleted.add(file.getFileName().toString());
//                        }
//
//                        return FileVisitResult.CONTINUE;
//                    }
//
//                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
//
//                        // if a directory is deleted we have to recursively delete everything in it.
//                        // Files.delete does not work.
//                        if (filesToDelete.contains(dir))
//                            directoriesToDelete.add(dir);
//                        return FileVisitResult.CONTINUE;
//                    }
//                });
//                for (Path path : directoriesToDelete) {
//                    FileSystemUtils.deleteRecursively(path);
//                }
//
//        } catch (IOException e) {
//            throw new UserFileException("There was an error deleting the files");
//        }

    }

    @Override
    public void deleteDirectoryByUsername(String username, Path directory) {

    }

    private void validateDirectory(Path directoryPath) {
        File directory = directoryPath.toFile();
        if (!directory.exists()) {
            throw new UserFileNotFoundException("Directory not found");
        }
        if (!directory.isDirectory()) {
            throw new UserFileBadRequestException("File is not a directory");

        }
    }
}
