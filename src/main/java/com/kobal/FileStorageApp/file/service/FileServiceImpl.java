package com.kobal.FileStorageApp.file.service;

import com.kobal.FileStorageApp.exceptions.UserStorageSpaceExecption;
import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.file.storage.FileStorageService;
import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.persistence.UserRepository;
import com.kobal.FileStorageApp.user.storage.UserStorageInfo;
import com.kobal.FileStorageApp.user.storage.UserStorageInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    private final FileMetaDataRepository fileMetaDataRepository;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final UserStorageInfoRepository userStorageInfoRepository;
    private final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public FileServiceImpl(FileMetaDataRepository fileMetaDataRepository, FileStorageService fileStorageService, UserRepository userRepository, UserStorageInfoRepository userStorageInfoRepository) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.userStorageInfoRepository = userStorageInfoRepository;
    }

    @Override
    public Optional<FileMetaDataDTO> uploadFile(Long userId, FilePath uploadFilePath, MultipartFile file) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        FileMetaData parentFolder = fileMetaDataRepository
                .findByUserIdAndPathAndName(user.getId(), uploadFilePath.getPath(), uploadFilePath.getFileName() )
                .orElseThrow(() -> new UserFileNotFoundException("Folder does not exist"));


        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setModified(LocalDateTime.now(ZoneId.of("UTC")));
        fileMetaData.setParent(parentFolder);
        fileMetaData.setUser(user);
        fileMetaData.setFileUUID(UUID.randomUUID());
        fileMetaData.setIsDirectory(false);
        fileMetaData.setSize(file.getSize());
        fileMetaData.setName(file.getOriginalFilename());
        fileMetaData.setPath(parentFolder.getAbsolutePath());// path is the path up to the folder containing the file
        FileMetaDataDTO dto = dtoFromMetaData(fileMetaData, user.getId());

        boolean uploadSuccessful = false;
        try {
            uploadSuccessful = fileStorageService.upload(dto, file.getInputStream());
        } catch (Exception e) {
            throw new UserFileException("Failed to upload the file");
        }
        if (uploadSuccessful) {
            boolean failed = saveFileMetaData(fileMetaData, dto);
            if (failed) {
                return Optional.empty();
            }
        }


        return Optional.of(dto);
    }

    private boolean saveFileMetaData(FileMetaData fileMetaData, FileMetaDataDTO dto) {
        try {
            fileMetaDataRepository.save(fileMetaData);

        } catch (Exception e) {
            logger.error("Failed to save file metadata to database. Attempting to delete file from file store.");
            try {
                fileStorageService.delete(dto);
            } catch (Exception exception) {
                logger.error("Failed delete file from file store. {}".formatted(fileMetaData));
                return false;
            }
        }
        return true;
    }

    @Override
    public InputStream download(Long userId, FilePath path) {
        FileMetaData fileMetaData = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, path.getPath(), path.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find file"));

        FileMetaDataDTO dto = dtoFromMetaData(fileMetaData, userId);
        return fileStorageService.download(dto);
    }

    private FileMetaDataDTO dtoFromMetaData(FileMetaData metaData, Long userId) {
        return new FileMetaDataDTO(
                userId,
                metaData.getName(),
                metaData.getPath(),
                metaData.getSize(),
                metaData.getModified(),
                metaData.isDirectory(),
                metaData.getFileUUID()
        );
    }

    @Override
    public boolean createDirectory(Long userId, FilePath directoryToCreate) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        Optional<FileMetaData> directoryOptional = fileMetaDataRepository
                .findByUserIdAndPathAndName(user.getId(),
                        directoryToCreate.getPath(),
                        directoryToCreate.getFileName());


        if (directoryOptional.isPresent()) {
            throw new UserFileException("A file already exists with name '%s'"
                    .formatted(directoryOptional.get().getName()));
        }

        FilePath parent = directoryToCreate.getParent();
        FileMetaData directory = fileMetaDataRepository
                .findByUserIdAndPathAndName(user.getId(),
                        parent.getPath(),
                        parent.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("The folder in which you want to create a new folder does not exist. Path:%s ".formatted(parent)));

        if (!directory.isDirectory()) {
            throw new UserFileException("Could not create directory. The path provided was a regular file, not a directory");
        }

        FileMetaData file = new FileMetaData();
        file.setName(directoryToCreate.getFileName());
        file.setParent(directory);
        file.setUser(user);
        file.setFileUUID(UUID.randomUUID());
        file.setIsDirectory(true);
        file.setPath(directory.getAbsolutePath());
        file.setModified(LocalDateTime.now(ZoneId.of("UTC")));
        file.setSize(0L);
        // this is not atomic. Somehow solve it?
        try {
            fileMetaDataRepository.save(file);
            fileStorageService.createDirectory(dtoFromMetaData(file, user.getId()));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public List<FileMetaDataDTO> getFilesInDirectory(Long userId, FilePath path) {
        String directoryPath = path.toString();

        Optional<FileMetaData> directoryOptional = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, path.getPath(), path.getFileName());
        if (directoryOptional.isEmpty()) {
            // TODO: log
            throw new UserFileNotFoundException("Could not find file with path '%s'".formatted(path));
        }

        FileMetaData directory = directoryOptional.get();
        if (!directory.isDirectory()) {
            throw new UserFileException("The path '%s' is not a directory.".formatted(path));
        }
        return fileMetaDataRepository
                .getSubDirectoriesByParentAndUserId(directory, userId)
                .stream()
                .map(metaData -> dtoFromMetaData(metaData, userId))
                .toList();
    }


    @Override
    public List<FileMetaDataDTO> deleteFilesInDirectory(Long userId, FilePath directory, List<String> fileNames) {
        List<FileMetaData> filesToDelete = fileMetaDataRepository.findByUserIdAndPathAndNames(userId, directory.toString(), fileNames);
        List<FileMetaData> successfullyDeletedFiles = filesToDelete.stream()
                .filter(metaData -> fileStorageService.delete(dtoFromMetaData(metaData, userId)))
                .toList();
        fileMetaDataRepository.deleteAllInBatch(successfullyDeletedFiles);

        return successfullyDeletedFiles.stream()
                .map(metaData -> dtoFromMetaData(metaData, userId))
                .toList();
    }

    @Override
    public List<FileMetaDataDTO> copyFilesToDirectory(Long userId, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user."));

        List<String> pathsToFiles = fileNames.stream()
                .map(fromDirectory::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToCopy = fileMetaDataRepository
                .getFilesByUserIdAndPaths(user.getId(), pathsToFiles);

        FileMetaData targetDirectory = fileMetaDataRepository
                .getFileMetaDataByUserIdAndPath(userId, toDirectory.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        FileMetaDataDTO targetDirectoryDTO = dtoFromMetaData(targetDirectory, user.getId());
        List<FileMetaData> successfullyCopiedFiles = filesToCopy.stream()
                .filter(metaData -> fileStorageService.copy(dtoFromMetaData(metaData, user.getId()), targetDirectoryDTO))
                .toList();

        List<FileMetaData> filesToCreate = successfullyCopiedFiles.stream()
                .map(metaData -> {
                    FileMetaData file = new FileMetaData();
                    file.setName(metaData.getName());
                    file.setParent(targetDirectory);
                    file.setUser(user);
                    file.setFileUUID(UUID.randomUUID());
                    file.setIsDirectory(metaData.isDirectory());
                    file.setPath(targetDirectory.getAbsolutePath());
                    file.setModified(LocalDateTime.now(ZoneId.of("UTC")));
                    return file;
                }).toList();
        fileMetaDataRepository.saveAll(filesToCreate);

        return successfullyCopiedFiles.stream()
                .map(metaData -> dtoFromMetaData(metaData, user.getId()))
                .toList();
    }

    @Override
    public Optional<FileMetaDataDTO> getFileMetaDataByPath(Long userId, FilePath filePath) {
//        Long userId = Long.valueOf(userId.getName());
        Optional<FileMetaData> dataOptional = fileMetaDataRepository.findByUserIdAndPathAndName(userId, filePath.getPath(), filePath.getFileName());
        return dataOptional
                .map(metaData -> dtoFromMetaData(metaData, userId));
    }

    @Override
    public List<FileMetaDataDTO> searchFiles(Long userId, String filename, boolean directoryOnly) {
        Pageable pageable = Pageable.ofSize(10);
        return fileMetaDataRepository.findByUserIdAndNameContaining(userId, filename, pageable, directoryOnly)
                .stream()
                .map(fileMetaData -> dtoFromMetaData(fileMetaData, userId))
                .toList();
    }

    @Override
    public List<FileMetaDataDTO> moveFilesToDirectory(Long userId, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {

        List<String> pathsToFiles = fileNames.stream()
                .map(fromDirectory::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToMove = fileMetaDataRepository
                .getFilesByUserIdAndPaths(userId, pathsToFiles);

        Long requiredSpace = filesToMove.stream().map(FileMetaData::getSize).reduce(0L, Long::sum);
        UserStorageInfo storageInfo = userStorageInfoRepository.getByUserId(userId);
        if (requiredSpace > storageInfo.availableSpaceInBytes()) {
            throw new UserStorageSpaceExecption("Not enough space available.");
        }

        FileMetaData targetDirectory = fileMetaDataRepository
                .getFileMetaDataByUserIdAndPath(userId, toDirectory.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        FileMetaDataDTO targetDirectoryDTO = dtoFromMetaData(targetDirectory, userId);
        List<FileMetaData> successfullyMovedFiles = filesToMove.stream()
                .filter(metaData -> fileStorageService.move(dtoFromMetaData(metaData, userId), targetDirectoryDTO))
                .toList();

        List<Long> movedFilesIds = successfullyMovedFiles.stream().map(FileMetaData::getId).toList();
        fileMetaDataRepository.updateFilePathsByIdAndUserId(userId, movedFilesIds, targetDirectoryDTO.getAbsolutePath());

        return successfullyMovedFiles.stream()
                .map(metaData -> dtoFromMetaData(metaData, userId))
                .toList();
    }

    @Transactional
    @Override
    public void rename(Long userId, FilePath pathToFile, String newName) {
//        String oldAbsoluteFilePath = pathToFile.toString();
        FileMetaData file = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, pathToFile.getPath(), pathToFile.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("File does not exist"));
        String newPath = new FilePath()
                        .addPartRaw(pathToFile.getPath())
                        .addPartRaw(newName).toString();
        fileMetaDataRepository.updateFilePath(userId, file.getAbsolutePath(), newPath);
        file.setName(newName);
        fileMetaDataRepository.save(file);
    }
}
