package com.kobal.FileStorageApp.user.userdetails;

import com.kobal.FileStorageApp.user.AppUser;
import com.kobal.FileStorageApp.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user with name '%s'".formatted(username));
        return userRepository.getUserByName(username)
                .orElseThrow(() -> {
                    logger.warn("Failed to load user with name '%s'".formatted(username));
                    return new UsernameNotFoundException(String.format("Could not find user with name '%s'", username));
                });
    }
}
