package com.kobal.FileStorageApp.configuration;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ConfigurationGuard implements InitializingBean {

    @Value("${storage.current:#{null}}")
    private String storageType;
    private final Logger logger = LoggerFactory.getLogger(ConfigurationGuard.class);
    private static final String[] POSSIBLE_VALUES = {"filesystem", "google-cloud", "test"};

    @PostConstruct
    public void afterPropertiesSet() {
        if (this.storageType == null) {
            logger.error("storage.current environment variable must be configured. Possible values: {}", Arrays.toString(POSSIBLE_VALUES));
            throw new IllegalArgumentException("storage.current environment variable must be configured. Possible values: %s"
                    .formatted(Arrays.toString(POSSIBLE_VALUES)));
        } else if (!List.of(POSSIBLE_VALUES).contains(storageType)) {
            logger.error("Bad value for storage.current environment variable. Possible values: {}", Arrays.toString(POSSIBLE_VALUES));
            throw new IllegalArgumentException("Bad value for storage.current environment variable. Possible values: %s"
                    .formatted(Arrays.toString(POSSIBLE_VALUES)));
        }
    }
}
