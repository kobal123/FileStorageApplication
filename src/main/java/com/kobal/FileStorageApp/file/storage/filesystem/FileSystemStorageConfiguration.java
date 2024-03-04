package com.kobal.FileStorageApp.file.storage.filesystem;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Objects;

@Configuration
@ConfigurationProperties("store.filesystem")
public class FileSystemStorageConfiguration {

    private Path rootFolder;

    public FileSystemStorageConfiguration() {
    }

    public FileSystemStorageConfiguration(Path root) {
        this.rootFolder = root;
    }

    public FileSystemStorageConfiguration(String root) {
        this.rootFolder = Path.of(root);
    }

    public Path getRoot() {
        return rootFolder;
    }

    public void setRoot(Path root) {
        this.rootFolder = root;
    }
}
