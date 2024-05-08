package com.kobal.FileStorageApp.user.service;


import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.user.OAuthIssuer;
import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.persistence.UserRepository;
import com.kobal.FileStorageApp.user.storage.UserStorageInfo;
import com.kobal.FileStorageApp.user.storage.UserStorageInfoRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final UserStorageInfoRepository userStorageInfoRepository;

    public UserService(UserRepository userRepository, FileMetaDataRepository fileMetaDataRepository, UserStorageInfoRepository userStorageInfoRepository) {
        this.userRepository = userRepository;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.userStorageInfoRepository = userStorageInfoRepository;
    }

    @Cacheable
    public Long getUserIdFromJwt(Jwt jwt) {
        OAuthIssuer issuer = OAuthIssuer.fromIssuer(jwt.getIssuer().toString());
        String subject = jwt.getSubject();
        AppUser user = userRepository.getUserByIssuerAndSubject(issuer, subject)
                .orElseThrow(() -> new RuntimeException("User could not be found"));

        return user.getId();
    }

    @Cacheable
    private Optional<AppUser> getUserFromJwt(Jwt jwt) {
        OAuthIssuer issuer = OAuthIssuer.fromIssuer(jwt.getIssuer().toString());
        String subject = jwt.getSubject();

        return userRepository.getUserByIssuerAndSubject(issuer, subject);
    }

    public AppUser register(Jwt jwt) {
        OAuthIssuer issuer = OAuthIssuer.fromIssuer(jwt.getIssuer().toString());
        String sub = jwt.getSubject();
        String email = jwt.getClaim("email");
        Optional<AppUser> optionalAppUser = getUserFromJwt(jwt);

        if (optionalAppUser.isPresent()) {
            return optionalAppUser.get();
        }

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setSubject(sub);
        user.setIssuer(issuer);
        userRepository.save(user);

        FileMetaData rootDir = new FileMetaData();
        rootDir.setName("/");
        rootDir.setPath("");
        rootDir.setUser(user);
        rootDir.setIsDirectory(true);
        rootDir.setModified(LocalDateTime.now());
        rootDir.setFileUUID(UUID.randomUUID());
        rootDir.setParent(null);
        rootDir.setSize(0L);
        fileMetaDataRepository.save(rootDir);

        UserStorageInfo storageInfo = new UserStorageInfo();
        storageInfo.setUser(user);
        storageInfo.setUsedBytes(0L);

        userStorageInfoRepository.save(storageInfo);

        return user;
    }
}
