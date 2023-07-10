package com.kobal.FileStorageApp.fileservice;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FileService {

    Optional<File> getFile(String username, Path path);
    void uploadFile(String username, Path path, InputStream file);

    void createDirectory(String username, Path directoryPath);
    Stream<String> loadFileNamesFromDirectory(Path directoryPath);
    void deleteFileByName(String username, String filename);

    void deleteFilesInDirectory(String username, Path directory, List<Path> files);

    void deleteDirectoryByUsername(String username, Path directory);
}
