package com.kobal.FileStorageApp.fileservice;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface FileService {



    Optional<File> getFile(Principal principal, Path path);
    void uploadFile(Principal principal, Path path, InputStream file);

    void createDirectory(Principal principal, Path directoryPath);
    List<File> getFilesInDirectory(Principal principal, Path path);

    List<String> deleteFilesInDirectory(Principal principal, Path directory, List<String> files);

    List<String> moveFilesToDirectory(Principal principal, Path from, Path to, List<String> fileNames);

    List<String> copyFilesToDirectory(Principal principal, Path from, Path to, List<String> fileNames);
}
