package com.kobal.FileStorageApp.configuration.dev;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.UserRepository;
import com.kobal.FileStorageApp.user.role.Role;
import com.kobal.FileStorageApp.user.role.RoleName;
import com.kobal.FileStorageApp.user.role.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

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
            for (int k = 0; k < 1; k++) {

                AppUser user = new AppUser();
                user.setName("user" + k);
                user.setPassword(passwordEncoder.encode("password"));
                user.setEmail("email%s@company.com".formatted(k));
                Role role = k == 0 ? new Role(RoleName.ROLE_ADMIN) :new Role(RoleName.ROLE_USER);
                roleRepository.save(role);
                user.addRole(role);
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
            }
        }
}
