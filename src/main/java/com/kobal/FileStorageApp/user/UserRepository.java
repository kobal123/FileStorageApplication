package com.kobal.FileStorageApp.user;

import com.kobal.FileStorageApp.user.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> getUserByName(String name);
    Optional<AppUser> getUserByEmail(String email);

}
