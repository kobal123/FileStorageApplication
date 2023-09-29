package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(
        value="storage.current",
        havingValue = "filesystem",
        matchIfMissing = true)
public class FileSystemFileStore implements FileStorageService {
    private final Path BASE_PATH;

    public FileSystemFileStore(FileSystemStorageConfiguration fileSystemStorageConfiguration) {
        this.BASE_PATH = fileSystemStorageConfiguration.getRoot();
    }


    @Override
    public boolean upload(FileMetaDataDTO metaData, InputStream inputStream) {
        File file = getPathFromMetaData(metaData).toFile();

        boolean wasCreated;
        try {
            wasCreated = file.createNewFile();
        } catch (IOException ignored) {
            throw new UserFileException("Failed to upload file.");
        }

        if (!wasCreated)
            throw new UserFileException("There was an error creating the file");

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            int BUFFER_READ_SIZE = 8 * 1024;
            byte[] buffer = new byte[BUFFER_READ_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            file.delete();
            throw new UserFileException("There was an error creating the file");
        }
        return true;
    }

    @Override
    public InputStream download(FileMetaDataDTO metaData) {
        File file = getPathFromMetaData(metaData).toFile();
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(FileMetaDataDTO metaData) {
        Path file = getPathFromMetaData(metaData);
        boolean isDirectory = metaData.isDirectory();
        if (file.toFile().isDirectory() != isDirectory) // should never happen??
            return false;



        if (isDirectory) {

            try {
                return FileSystemUtils.deleteRecursively(file);
            } catch (IOException e) {
                return false;
            }
        } else {
            try {
                if (Files.notExists(file))
                    return true;
                Files.delete(file);
            } catch (IOException exception) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean rename(FileMetaDataDTO metaData, String name) {
        File file = getPathFromMetaData(metaData).toFile();
        File destination = BASE_PATH
                .resolve(metaData.getPath())
                .resolve(name)
                .toFile();
        return file.renameTo(destination);
    }

    @Override
    public boolean move(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path moveFromPath = getPathFromMetaData(source);
        Path moveToPath = getPathFromMetaData(target);
        try {
            Files.move(moveFromPath, moveToPath);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean copy(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path copyFromPath = getPathFromMetaData(source);
        Path copyToPath = getPathFromMetaData(target);
        try {
            Files.copy(copyFromPath, copyToPath);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private Path getPathFromMetaData(FileMetaDataDTO metaData) {
        return Paths.get(BASE_PATH.toString(),
                String.valueOf(metaData.getUserId()),
                metaData.getAbsolutePath());
    }
}
