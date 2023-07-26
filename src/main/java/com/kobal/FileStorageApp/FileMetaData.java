package com.kobal.FileStorageApp;


import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class FileMetaData {

    private final String name;
    private final Long size;
    private final LocalDateTime modified;

    public FileMetaData(String name, Long size, Long modified) {
        this.name = name;
        this.size = size;
        this.modified = LocalDateTime.ofInstant(Instant.ofEpochMilli(modified),
                        TimeZone.getDefault().toZoneId());
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

}
