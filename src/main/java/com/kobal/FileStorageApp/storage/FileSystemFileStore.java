package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileSystemFileStore implements FileStorageService {
    private final Path BASE_PATH;

    public FileSystemFileStore(FileSystemStorageConfiguration fileSystemStorageConfiguration) {
        this.BASE_PATH = fileSystemStorageConfiguration.getRoot();
    }


    @Override
    public boolean upload(FileMetaDataDTO path, InputStream inputStream) {
        File file = BASE_PATH.resolve(path.toString()).toFile();

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
    public InputStream download(FileMetaDataDTO path) {
        File file = BASE_PATH.resolve(path.toString()).toFile();
        try {
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(FileMetaDataDTO metaData) {
        File file = BASE_PATH.resolve(metaData.getPath()).toFile();
        boolean isDirectory = metaData.isDirectory();
        if (file.isDirectory() != isDirectory) // should never happen??
            return false;

        if (isDirectory) {
            return FileSystemUtils.deleteRecursively(file);
        } else {
            try {
                if (!file.exists())
                    return false;
                Files.delete(file.toPath());
            } catch (IOException exception) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean rename(FileMetaDataDTO metaData, String name) {
        File file = BASE_PATH.resolve(metaData.getAbsolutePath()).toFile();
        File destination = BASE_PATH
                .resolve(metaData.getPath())
                .resolve(name)
                .toFile();
        return file.renameTo(destination);
    }

    @Override
    public boolean move(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path moveFromPath = BASE_PATH.resolve(source.getAbsolutePath());
        Path moveToPath = BASE_PATH.resolve(target.getAbsolutePath());
        try {
            Files.move(moveFromPath, moveToPath);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean copy(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path copyFromPath = BASE_PATH.resolve(source.getAbsolutePath());
        Path copyToPath = BASE_PATH.resolve(target.getAbsolutePath());
        try {
            Files.copy(copyFromPath, copyToPath);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
