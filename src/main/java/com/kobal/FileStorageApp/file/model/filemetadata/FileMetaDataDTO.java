package com.kobal.FileStorageApp.file.model.filemetadata;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileMetaDataDTO {

    private final long userId;
    private final String name;
    private final String path;
    private final long size;
    private final LocalDateTime modified;
    private final boolean isDirectory;
    private final UUID uuid;
    private final String absolutePath;

    public FileMetaDataDTO(Long userId, String name, String path, long size, LocalDateTime modified, boolean isDirectory, UUID uuid) {
        this.userId = userId;
        this.name = name;
        this.path = path;
        this.size = size;
        this.modified = modified;
        this.isDirectory = isDirectory;
        this.uuid = uuid;
        this.absolutePath = path + "/" + name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public Long getSize() {
        return size;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Long getUserId() {
        return userId;
    }
    public boolean isDirectory() {
        return isDirectory;
    }

}
