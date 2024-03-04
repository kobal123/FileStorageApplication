package com.kobal.FileStorageApp.user.storage;

import com.kobal.FileStorageApp.user.model.AppUser;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public final class UserStorageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private AppUser user;
    private Long sizeInBytes;
    private final Long storageLimitInBytes = 1000 * 1000 * 1000L; // 1 GB;

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }


    public UserStorageInfo() {
    }

    public UserStorageInfo(Long id,
                           AppUser user,
                           Long sizeInBytes) {
        this.id = id;
        this.user = user;
        this.sizeInBytes = sizeInBytes;
    }

    @Id
    public Long id() {
        return id;
    }

    @OneToOne
    public AppUser user() {
        return user;
    }

    public Long sizeInBytes() {
        return sizeInBytes;
    }

    public Long storageLimitInBytes() {
        return storageLimitInBytes;
    }

    public Long availableSpaceInBytes() {
        return storageLimitInBytes - sizeInBytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserStorageInfo) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.user, that.user) &&
                Objects.equals(this.sizeInBytes, that.sizeInBytes) &&
                Objects.equals(this.storageLimitInBytes, that.storageLimitInBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, sizeInBytes, storageLimitInBytes);
    }

    @Override
    public String toString() {
        return "UserStorageInfo[" +
                "id=" + id + ", " +
                "user=" + user + ", " +
                "sizeInBytes=" + sizeInBytes + ", " +
                "storageLimitInBytes=" + storageLimitInBytes + ']';
    }

}
