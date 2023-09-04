package com.kobal.FileStorageApp;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileMetaDataDTO {

    private final String name;
    private final Long size;
    private final LocalDateTime modified;
    private final Boolean isDirectory;
    private final UUID uuid;

    public FileMetaDataDTO(String name, Long size, LocalDateTime modified, boolean isDirectory, UUID uuid) {
        this.name = name;
        this.size = size;
        this.modified = modified;

        this.isDirectory = isDirectory;
        this.uuid = uuid;
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

    public boolean isDirectory() {
        return isDirectory;
    }

}
