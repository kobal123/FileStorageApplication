package com.kobal.FileStorageApp.configuration.dev;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.file.storage.FileStorageService;
import com.kobal.FileStorageApp.user.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Profile("localdev")
@Component
public class FileStorageCleanUp {

    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(FileStorageCleanUp.class);

    public FileStorageCleanUp(FileMetaDataRepository fileMetaDataRepository, FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @PreDestroy
    private void cleanUp() {
        var users = userRepository.findAll();
        logger.debug("Starting local development storage cleanup");

        for (var user : users ) {

            var files = fileMetaDataRepository.getAllFilesByUserId(user.getId());
            for (var file : files) {
                logger.debug("Deleting file {}", file);

                boolean failed = fileStorageService.delete(new FileMetaDataDTO(user.getId(),
                        file.getName(),
                        file.getPath(),
                        file.getSize(),
                        file.getModified(),
                        file.isDirectory(),
                        file.getFileUUID()));
                if (failed) {
                    logger.warn("Could not delete file {}", file);
                }
            }
        }
        logger.debug("Local development storage cleanup ended");
    }
}
