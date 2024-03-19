package com.kobal.FileStorageApp.file.model.filemetadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kobal.FileStorageApp.file.service.FilePath;
import java.time.LocalDateTime;
import java.util.UUID;

public class FileMetaDataDTO {

    @JsonIgnore
    private final long userId;
    private final UUID uuid;
    private final String name;
    private final String path;
    private final long size;
    private final LocalDateTime modified;
    private final boolean isDirectory;
    private final String absolutePath;
    private final boolean isStarred;

    public FileMetaDataDTO(Long userId, String name, String path, long size, LocalDateTime modified, boolean isDirectory, UUID uuid, boolean isStarred) {
        this.userId = userId;
        this.name = name;
        this.path = path;
        this.size = size;
        this.modified = modified;
        this.isDirectory = isDirectory;
        this.uuid = uuid;
        this.isStarred = isStarred;
        this.absolutePath = new FilePath().addPartRaw(path).addPartRaw(name).toString();
    }

    public static FileMetaDataDTO fromFileMetaData(Long userId, FileMetaData metaData) {
        return new FileMetaDataDTO(
                userId,
                metaData.getName(),
                metaData.getPath(),
                metaData.getSize(),
                metaData.getModified(),
                metaData.isDirectory(),
                metaData.getFileUUID(),
                metaData.isStarred()
        );
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileMetaDataDTO dto = (FileMetaDataDTO) o;

        if (userId != dto.userId) return false;
        if (size != dto.size) return false;
        if (isDirectory != dto.isDirectory) return false;
        if (isStarred != dto.isStarred) return false;
        if (!uuid.equals(dto.uuid)) return false;
        if (!name.equals(dto.name)) return false;
        if (!path.equals(dto.path)) return false;
        if (!modified.equals(dto.modified)) return false;
        return absolutePath.equals(dto.absolutePath);
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + path.hashCode();
        result = 31 * result + (int) (size ^ (size >>> 32));
        result = 31 * result + modified.hashCode();
        result = 31 * result + (isDirectory ? 1 : 0);
        result = 31 * result + absolutePath.hashCode();
        result = 31 * result + (isStarred ? 1 : 0);
        return result;
    }
}
