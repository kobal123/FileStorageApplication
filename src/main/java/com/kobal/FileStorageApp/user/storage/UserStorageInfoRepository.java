package com.kobal.FileStorageApp.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStorageInfoRepository extends JpaRepository<UserStorageInfo, Long> {

    Optional<UserStorageInfo> getByUserId(Long userId);

}
