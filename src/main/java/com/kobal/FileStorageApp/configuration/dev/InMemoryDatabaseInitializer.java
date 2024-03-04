package com.kobal.FileStorageApp.configuration.dev;

import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.user.persistence.UserRepository;
import com.kobal.FileStorageApp.user.role.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("localdev")
public class InMemoryDatabaseInitializer {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final FileMetaDataRepository fileMetaDataRepository;
        private final RoleRepository roleRepository;

    public InMemoryDatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, FileMetaDataRepository fileMetaDataRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.roleRepository = roleRepository;
    }


    @PostConstruct
        public void init() {

        }
}
