package com.kobal.FileStorageApp.file.storage.filesystem;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.file.storage.FileStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

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
        File file = getFullPathFromMetaData(metaData).toFile();

        boolean wasCreated;
        try {
            wasCreated = file.createNewFile();
        } catch (IOException exception) {
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
        Path filepath = getFullPathFromMetaData(metaData);
        File file = filepath.toFile();
        if (file.isDirectory()) {
            throw new UserFileException("Cannot directly download directory");
        }
        try {
            return Files.newInputStream(filepath);
        } catch (IOException e) {
            return InputStream.nullInputStream();
        }
    }

    @Override
    public boolean delete(FileMetaDataDTO metaData) {
        Path file = getFullPathFromMetaData(metaData);
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
        Path filepath = getFullPathFromMetaData(metaData);
        Path destination = getPathFromMetaData(metaData).resolve(name);

        if (destination.toFile().exists())
            return false;
        try {
            Files.move(filepath, destination);
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean move(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path moveFromPath = getFullPathFromMetaData(source);
        Path moveToPath = getFullPathFromMetaData(target);
        // if file is currently being downloaded moving could result
        // in an exception thrown.

        try {
            Files.move(moveFromPath, moveToPath.resolve(source.getName()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean copy(FileMetaDataDTO source, FileMetaDataDTO target) {
        Path copyFromPath = getFullPathFromMetaData(source);
        Path copyToPath = getFullPathFromMetaData(target);
        try {
            Files.copy(copyFromPath, copyToPath.resolve(source.getName()));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean createDirectory(FileMetaDataDTO fileMetaDataDTO) {
        Path newDirectoryPath = getFullPathFromMetaData(fileMetaDataDTO);

        try {
            Files.createDirectory(newDirectoryPath);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     *  Returns a path to the file including the file name. The file is not guaranteed to exist.
     *
     * @param metaData Specifies the file
     * @return Path to the file including the file name
     */
    public Path getFullPathFromMetaData(FileMetaDataDTO metaData) {
        return Paths.get(BASE_PATH.toString(),
                String.valueOf(metaData.getUserId()),
                metaData.getAbsolutePath());
    }

    /**
     *  Returns a path to the file not including the file name. The file is not guaranteed to exist.
     *
     * @param metaData Specifies the file
     * @return Path to the file not including the file name.
     */
    public Path getPathFromMetaData(FileMetaDataDTO metaData) {
        return Paths.get(BASE_PATH.toString(),
                String.valueOf(metaData.getUserId()),
                metaData.getPath());
    }
}
