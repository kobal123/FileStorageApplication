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
    private Long usedBytes;
    private final Long storageLimitInBytes = 1000 * 1000 * 1000L; // 1 GB;

    public void setUser(AppUser user) {
        this.user = user;
    }

    public void setUsedBytes(Long usedBytes) {
        this.usedBytes = usedBytes;
    }


    public UserStorageInfo() {
    }

    public UserStorageInfo(Long id,
                           AppUser user,
                           Long usedBytes) {
        this.id = id;
        this.user = user;
        this.usedBytes = usedBytes;
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
        return usedBytes;
    }

    public Long storageLimitInBytes() {
        return storageLimitInBytes;
    }

    public Long availableSpaceInBytes() {
        return storageLimitInBytes - usedBytes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserStorageInfo) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.user, that.user) &&
                Objects.equals(this.usedBytes, that.usedBytes) &&
                Objects.equals(this.storageLimitInBytes, that.storageLimitInBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, usedBytes, storageLimitInBytes);
    }

    @Override
    public String toString() {
        return "UserStorageInfo[" +
                "id=" + id + ", " +
                "user=" + user + ", " +
                "sizeInBytes=" + usedBytes + ", " +
                "storageLimitInBytes=" + storageLimitInBytes + ']';
    }

}
