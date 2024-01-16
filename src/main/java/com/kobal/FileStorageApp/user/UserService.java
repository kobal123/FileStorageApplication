package com.kobal.FileStorageApp.user;


import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.user.model.AppUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FileMetaDataRepository fileMetaDataRepository;


    public UserService(UserRepository userRepository, FileMetaDataRepository fileMetaDataRepository) {
        this.userRepository = userRepository;
        this.fileMetaDataRepository = fileMetaDataRepository;
    }

    public AppUser registerOrLoadUserFromOidcUser(OidcUser oidcUser) {
        // check if user already exist with this email

        Optional<AppUser> optionalAppUser = userRepository.getUserByEmail(oidcUser.getEmail());

        if (optionalAppUser.isPresent()) {
            return optionalAppUser.get();
        }

        AppUser user = new AppUser();
        user.setEmail(oidcUser.getEmail());
        user.setName(oidcUser.getName());
        user.setPassword(null);
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

        return user;
    }
}
