package com.kobal.FileStorageApp.user.storage;

import com.kobal.FileStorageApp.user.model.AppUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public record UserStorageInfo(@Id
                              Long id,
                              @OneToOne
                              AppUser user,
                              Long sizeInBytes,
                              Long storageLimitInBytes) { }
