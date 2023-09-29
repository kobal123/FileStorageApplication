package com.kobal.FileStorageApp.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStorageInfoRepository extends JpaRepository<UserStorageInfo, Long> {
}
