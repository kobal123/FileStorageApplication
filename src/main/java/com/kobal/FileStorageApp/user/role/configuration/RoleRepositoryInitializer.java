package com.kobal.FileStorageApp.user.role.configuration;

import com.kobal.FileStorageApp.user.role.Role;
import com.kobal.FileStorageApp.user.role.RoleName;
import com.kobal.FileStorageApp.user.role.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleRepositoryInitializer {
    private final RoleRepository roleRepository;
    private final Logger logger = LoggerFactory.getLogger(RoleRepositoryInitializer.class);
    public RoleRepositoryInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void createRoles() {
        for (var role : RoleName.values()) {
            Optional<Role> roleOptional = roleRepository.getRoleByName(role);
            if (roleOptional.isEmpty()) {
                logger.trace("Creating role %s".formatted(role.toString()));
                roleRepository.save(new Role(role));
            }
        }
    }
}
