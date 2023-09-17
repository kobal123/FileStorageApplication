package com.kobal.FileStorageApp.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties("file.storage")
public class FileSystemStorageConfiguration {
    private Path root;

    public FileSystemStorageConfiguration() {
    }

    public FileSystemStorageConfiguration(Path root) {
        this.root = root;
    }

    public FileSystemStorageConfiguration(String root) {
        this.root = Path.of(root);
    }

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }
}
