package com.kobal.FileStorageApp.fileservice;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface FileService {

    Optional<File> getFile(String username, Path path);
    void uploadFile(String username, Path path, InputStream file);

    void createDirectory(String username, Path directoryPath);
    List<File> getFilesinDirectory(String username, Path path);
    void deleteFileByName(String username, String filename);

    List<String> deleteFilesInDirectory(String username, Path directory, List<String> files);

    void deleteDirectoryByUsername(String username, Path directory);

    List<String> moveFilesToDirectory(String username, Path from, Path to, List<String> fileNames);

    List<String> copyFilesToDirectory(String username, Path from, Path to, List<String> fileNames);
}
