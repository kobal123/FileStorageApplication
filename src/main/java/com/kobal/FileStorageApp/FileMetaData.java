package com.kobal.FileStorageApp;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class FileMetaData {

    @Id
    @GeneratedValue
    private Long id;

    private String path;
    private String name;
    private Long size;
    private Long modified;
    private Long created;
}
