package com.kobal.FileStorageApp;

import com.kobal.FileStorageApp.fileservice.FilePath;
import com.kobal.FileStorageApp.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(uniqueConstraints={
        @UniqueConstraint(columnNames = {"path", "name", "user_id"}),
        @UniqueConstraint(columnNames = {"fileUUID", "user_id"}),

})
public class FileMetaData{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String name;

    private Long size;

    @Column(nullable = false)
    private LocalDateTime modified;

    @Column(nullable = false)
    private Boolean isDirectory;

    @Column(nullable = false)
    private String path;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER)
    private FileMetaData parent;

    public UUID getFileUUID() {
        return fileUUID;
    }

    public void setFileUUID(UUID fileUUID) {
        this.fileUUID = fileUUID;
    }

    @Column(nullable = false)
    private UUID fileUUID;

    public FileMetaData() {}

    public FileMetaData(Long id, String name, Long size, LocalDateTime modified, Boolean isDirectory, String path) {

        this.id = id;
        this.name = name;
        this.size = size;
        this.modified = modified;
        this.isDirectory = isDirectory;
        this.path = path;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public FileMetaData getParent() {
        return parent;
    }

    public void setParent(FileMetaData parent) {
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public Boolean isDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(Boolean directory) {
        isDirectory = directory;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getAbsolutePath()  {
        return new FilePath()
                .addPartRaw(path)
                .addPartEncoded(name)
                .toString();
    }

    @Override
    public String toString() {
        return "FileMetaData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", modified=" + modified +
                ", isDirectory=" + isDirectory +
                ", path='" + path + '\'' +
                ", parent=" + parent +
                ", fileUUID=" + fileUUID +
                '}';
    }
}
