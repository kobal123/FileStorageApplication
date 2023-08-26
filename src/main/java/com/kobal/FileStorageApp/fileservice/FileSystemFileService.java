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
import java.security.Principal;
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
    public Optional<File> getFile(Principal principal, Path path){
        Path actualPath = BASE_PATH.resolve(principal.getName()).resolve(path);
        File file = new File(actualPath.toString());
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    @Override
    public void uploadFile(Principal principal, Path uploadFilePath, InputStream fileInputStream) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path filePath = userRootDirectory.resolve(uploadFilePath);

        validateDirectory(filePath.getParent(), userRootDirectory);
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
    public void createDirectory(Principal principal, Path directoryToCreate) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path directoryPath = userRootDirectory.resolve(directoryToCreate);
        validateDirectory(directoryPath.getParent(), userRootDirectory);


        File directory = directoryPath.toFile();

        if (directory.exists())
            throw new UserFileException("A file or directory already exists with this name");

        boolean wasCreated = directory.mkdir();

        if (!wasCreated) {
            throw new UserFileException("Failed to create directory");
        }
    }

    @Override
    public List<File> getFilesInDirectory(Principal principal, Path pathToDirectory) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path absoluteDirectoryPath = userRootDirectory.resolve(pathToDirectory);
        validateDirectory(absoluteDirectoryPath, userRootDirectory);
        File directory = absoluteDirectoryPath.toFile();

        if (!directory.exists())
            throw new UserFileNotFoundException("Directory does not exist");

        File[] files = directory.listFiles();
        if (files == null)
            throw new UserFileException("File was not a directory");

        return List.of(files);
    }


    @Override
    public List<String> deleteFilesInDirectory(Principal principal, Path directory, List<String> fileNames) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path directoryPath = userRootDirectory.resolve(directory);
        validateDirectory(directoryPath, userRootDirectory);
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
    }

    @Override
    public List<String> copyFilesToDirectory(Principal principal, Path fromDirectory, Path toDirectory, List<String> fileNames) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path copyFrom = userRootDirectory.resolve(fromDirectory);
        Path copyTo = userRootDirectory.resolve(toDirectory);
        validateDirectory(copyFrom, userRootDirectory);
        validateDirectory(copyTo, userRootDirectory);

        List<FileMoveCopy> sourceAndDestination = fileNames.stream()
                .map(s -> new FileMoveCopy(
                        copyFrom.resolve(s),
                        copyTo.resolve(s)
                ))
                .toList();

        List<String> failedCopy = new ArrayList<>();

        for (FileMoveCopy filePath : sourceAndDestination) {
            File file = filePath.from.toFile();

            if (file.isDirectory()) {
                try {
                    FileSystemUtils.copyRecursively(filePath.from, filePath.to);
                } catch (IOException e) {
                    failedCopy.add(file.getName());
                }
            } else {
                try {
                    Files.copy(filePath.from, filePath.to);
                } catch (IOException e) {
                    failedCopy.add(file.getName());
                }
            }
        }
        return failedCopy;
    }
    @Override
    public List<String> moveFilesToDirectory(Principal principal, Path fromDirectory, Path toDirectory, List<String> fileNames) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path moveFrom = userRootDirectory.resolve(fromDirectory);
        Path moveTo = userRootDirectory.resolve(toDirectory);
        validateDirectory(moveFrom, userRootDirectory);
        validateDirectory(moveTo, userRootDirectory);

        List<FileMoveCopy> sourceAndDestination = fileNames.stream()
                .map(s -> new FileMoveCopy(
                        moveFrom.resolve(s),
                        moveTo.resolve(s)
                )).toList();

        List<String> failedCopy = new ArrayList<>();
        for (FileMoveCopy filePath : sourceAndDestination) {
            try {
                Files.move(filePath.from, filePath.to);
            } catch (IOException e) {
                failedCopy.add(filePath.from.getFileName().toString());
            }
        }
        return failedCopy;
    }

    private void validateDirectory(Path directoryPathToCheck, Path userRootDirectory) {
        if (directoryPathToCheck == null)
            throw new UserFileException("Cannot access this directory");

        directoryPathToCheck = directoryPathToCheck.normalize().toAbsolutePath();
        if (!directoryPathToCheck.startsWith(userRootDirectory))
            throw new UserFileException("Cannot access this directory");

        File directory = directoryPathToCheck.toFile();
        if (!directory.exists()) {
            throw new UserFileNotFoundException("Directory not found");
        }
        if (!directory.isDirectory()) {
            throw new UserFileBadRequestException("File is not a directory");
        }
    }


    record FileMoveCopy(Path from, Path to) { }
}
