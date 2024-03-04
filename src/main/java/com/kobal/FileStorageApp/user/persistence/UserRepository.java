package com.kobal.FileStorageApp.user.persistence;

import com.kobal.FileStorageApp.user.OAuthIssuer;
import com.kobal.FileStorageApp.user.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> getUserByIssuerAndSubject(OAuthIssuer issuer, String sub);

}
