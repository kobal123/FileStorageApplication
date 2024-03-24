package com.kobal.FileStorageApp.file.service;

import com.google.cloud.storage.StorageException;
import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public FileMetaDataDTO uploadFile(Long userId, FilePath uploadFilePath, MultipartFile file) {
        FileMetaData parentFolder = fileMetaDataRepository
                .findByUserIdAndPathAndName(
                        userId,
                        uploadFilePath.getPath(),
                        uploadFilePath.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("Folder does not exist"));

        long requiredSpace = file.getSize();
        UserStorageInfo storageInfo = userStorageInfoRepository.getByUserId(userId);
        if (requiredSpace > storageInfo.availableSpaceInBytes()) {
            throw new UserStorageSpaceExecption("Not enough space available.");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setModified(LocalDateTime.now(ZoneId.of("UTC")));
        fileMetaData.setParent(parentFolder);
        fileMetaData.setUser(user);
        fileMetaData.setFileUUID(UUID.randomUUID());
        fileMetaData.setIsDirectory(false);
        fileMetaData.setSize(file.getSize());
        fileMetaData.setName(file.getOriginalFilename());
        fileMetaData.setPath(parentFolder.getAbsolutePath());
        FileMetaDataDTO dto = FileMetaDataDTO.fromFileMetaData(userId, fileMetaData);

        fileMetaDataRepository.save(fileMetaData);

        try {
            fileStorageService.upload(dto, file.getInputStream());
        } catch (Exception e) {
            logger.error("Failed to upload file %s".formatted(file.getOriginalFilename()));
            throw new UserFileException("Failed to upload the file");
        }

        return dto;
    }

    @Override
    public InputStream download(Long userId, FilePath path) {
        FileMetaData fileMetaData = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, path.getPath(), path.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find file"));

        FileMetaDataDTO dto = FileMetaDataDTO.fromFileMetaData(userId, fileMetaData);
        return fileStorageService.download(dto);
    }


    @Transactional(rollbackFor = StorageException.class)
    @Override
    public boolean createDirectory(Long userId, FilePath directoryToCreate) {
        Optional<FileMetaData> directoryOptional = fileMetaDataRepository.findByUserIdAndPathAndName(
                userId,
                directoryToCreate.getPath(),
                directoryToCreate.getFileName()
        );


        if (directoryOptional.isPresent()) {
            throw new UserFileException("A file already exists with name '%s'"
                    .formatted(directoryOptional.get().getName()));
        }

        FilePath parent = directoryToCreate.getParent();
        FileMetaData directory = fileMetaDataRepository
                .findByUserIdAndPathAndName(
                        userId,
                        parent.getPath(),
                        parent.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("The folder in which you want to create a new folder does not exist. Path:%s ".formatted(parent)));

        if (!directory.isDirectory()) {
            throw new UserFileException("Could not create directory. The path provided was a regular file, not a directory");
        }

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));
        FileMetaData file = new FileMetaData();
        file.setName(directoryToCreate.getFileName());
        file.setParent(directory);
        file.setUser(user);
        file.setFileUUID(UUID.randomUUID());
        file.setIsDirectory(true);
        file.setPath(directory.getAbsolutePath());
        file.setModified(LocalDateTime.now(ZoneId.of("UTC")));
        file.setSize(0L);
        try {
            fileMetaDataRepository.save(file);
            fileStorageService.createDirectory(FileMetaDataDTO.fromFileMetaData(userId, file));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public List<FileMetaDataDTO> getFilesInDirectory(Long userId, FilePath path) {

        Optional<FileMetaData> directoryOptional = fileMetaDataRepository.findByUserIdAndPathAndName(
                userId,
                path.getPath(),
                path.getFileName()
        );

        if (directoryOptional.isEmpty()) {
            throw new UserFileNotFoundException("Could not find file with path '%s'".formatted(path));
        }

        FileMetaData directory = directoryOptional.get();
        if (!directory.isDirectory()) {
            throw new UserFileException("The path '%s' is not a directory.".formatted(path));
        }
        return fileMetaDataRepository
                .getSubDirectoriesByParentAndUserId(directory, userId)
                .stream()
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData))
                .toList();
    }


    @Override
    public BatchOperationResult deleteFilesInDirectory(Long userId,
                                                       List<FilePath> filePaths) {
        if (filePaths.isEmpty()) {
            throw new UserFileBadRequestException("Must specify at least one file to copy");
        }
        Set<String> paths = filePaths.stream().map(FilePath::getPath).collect(Collectors.toSet());
        if (paths.size() > 1) {
            throw new UserFileBadRequestException("Cannot delete files from 2 or more folders at the same time.");
        }

        String path = filePaths.get(0).getPath();

        List<FileMetaData> filesToDelete = fileMetaDataRepository.findByUserIdAndPathAndNames(
                userId,
                path,
                filePaths.stream().map(FilePath::getFileName).toList());

        List<FileMetaData> successfullyDeletedFiles = filesToDelete.stream()
                .filter(metaData -> fileStorageService.delete(FileMetaDataDTO.fromFileMetaData(userId, metaData)))
                .toList();
        fileMetaDataRepository.deleteAllInBatch(successfullyDeletedFiles);

        List<FileMetaDataDTO> success = successfullyDeletedFiles.stream()
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData))
                .toList();
        List<FileMetaDataDTO> failed = filesToDelete.stream()
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData))
                .filter(fileMetaDataDTO -> !success.contains(fileMetaDataDTO))
                .toList();

        return new BatchOperationResult(success, failed);
    }

    @Override
    public BatchOperationResult copyFilesToDirectory(Long userId,
                                                     List<FilePath> filePaths,
                                                     FilePath targetDirectoryPath) {

        if (filePaths.isEmpty()) {
            throw new UserFileBadRequestException("Must specify at least one file to copy");
        }
        Set<String> paths = filePaths.stream().map(FilePath::getPath).collect(Collectors.toSet());
        if (paths.size() > 1) {
            throw new UserFileBadRequestException("Cannot copy files from 2 or more folders at the same time");
        }

        String path = filePaths.get(0).getPath();

        FileMetaData targetDirectory = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, targetDirectoryPath.getPath(), targetDirectoryPath.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        List<FileMetaData> filesToCopy = fileMetaDataRepository
                .findByUserIdAndPathAndNames(userId, path, filePaths.stream().map(FilePath::getFileName).toList());

        Long requiredSpace = filesToCopy.stream().map(FileMetaData::getSize).reduce(0L, Long::sum);
        UserStorageInfo storageInfo = userStorageInfoRepository.getByUserId(userId);
        if (requiredSpace > storageInfo.availableSpaceInBytes()) {
            throw new UserStorageSpaceExecption("Not enough space available.");
        }

        FileMetaDataDTO targetDirectoryDTO = FileMetaDataDTO.fromFileMetaData(userId, targetDirectory);
        List<FileMetaData> successfullyCopiedFiles = filesToCopy.stream()
                .filter(metaData -> fileStorageService.copy(FileMetaDataDTO.fromFileMetaData(userId, metaData), targetDirectoryDTO))
                .toList();

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user."));

        List<FileMetaData> filesToCreate = successfullyCopiedFiles.stream()
                .map(metaData -> {
                    FileMetaData file = new FileMetaData();
                    file.setName(metaData.getName());
                    file.setParent(targetDirectory);
                    file.setUser(user);
                    file.setSize(metaData.getSize());
                    file.setFileUUID(UUID.randomUUID());
                    file.setIsDirectory(metaData.isDirectory());
                    file.setPath(targetDirectory.getAbsolutePath());
                    file.setModified(LocalDateTime.now(ZoneId.of("UTC")));
                    file.setIsStarred(false);
                    return file;
                }).toList();
        fileMetaDataRepository.saveAll(filesToCreate);

        List<FileMetaDataDTO> success = successfullyCopiedFiles.stream()
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData))
                .toList();

        List<FileMetaDataDTO> failed = filesToCopy.stream()
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData))
                .filter(metaData -> !success.contains(metaData))
                .toList();


        return new BatchOperationResult(success, failed);
    }

    @Override
    public Optional<FileMetaDataDTO> getFileMetaDataByPath(Long userId, FilePath filePath) {
        Optional<FileMetaData> dataOptional = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, filePath.getPath(), filePath.getFileName());
        return dataOptional
                .map(metaData -> FileMetaDataDTO.fromFileMetaData(userId, metaData));
    }

    @Override
    public List<FileMetaDataDTO> searchFiles(Long userId, String filename, boolean directoryOnly) {
        Pageable pageable = Pageable.ofSize(10);
        return fileMetaDataRepository.findByUserIdAndNameContaining(userId, filename, pageable, directoryOnly)
                .stream()
                .map(fileMetaData -> FileMetaDataDTO.fromFileMetaData(userId, fileMetaData))
                .toList();
    }

    @Override
    public BatchOperationResult moveFilesToDirectory(Long userId,
                                                     List<FilePath> filePaths, FilePath targetDirectoryPath) {

        if (filePaths.isEmpty()) {
            throw new UserFileBadRequestException("Must specify at least one file to copy");
        }
        Set<String> paths = filePaths.stream().map(FilePath::getPath).collect(Collectors.toSet());
        if (paths.size() > 1) {
            throw new UserFileBadRequestException("Cannot move files from 2 or more folders at the same time.");
        }

        String path = filePaths.get(0).getPath();
        FileMetaData targetDirectory = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, targetDirectoryPath.getPath(), targetDirectoryPath.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        List<FileMetaData> filesToMove = fileMetaDataRepository.findByUserIdAndPathAndNames(
                userId,
                path,
                filePaths.stream().map(FilePath::getFileName).toList());


        Long requiredSpace = filesToMove.stream().map(FileMetaData::getSize).reduce(0L, Long::sum);
        UserStorageInfo storageInfo = userStorageInfoRepository.getByUserId(userId);
        if (requiredSpace > storageInfo.availableSpaceInBytes()) {
            throw new UserStorageSpaceExecption("Not enough space available.");
        }


        FileMetaDataDTO targetDirectoryDTO = FileMetaDataDTO.fromFileMetaData(userId, targetDirectory);
        List<FileMetaData> successfullyMovedFiles = filesToMove.stream()
                .filter(metaData -> fileStorageService.move(FileMetaDataDTO.fromFileMetaData(userId, metaData), targetDirectoryDTO))
                .toList();

        List<Long> movedFilesIds = successfullyMovedFiles.stream().map(FileMetaData::getId).toList();
        fileMetaDataRepository.updateFilePathsByIdAndUserId(userId, movedFilesIds, targetDirectoryDTO.getAbsolutePath());



        List<FileMetaDataDTO> successfullyMoved = successfullyMovedFiles.stream()
                .map(metadata -> FileMetaDataDTO.fromFileMetaData(userId, metadata))
                .toList();

        List<FileMetaDataDTO> failedToMove = filesToMove.stream()
                .filter(metaData -> !successfullyMovedFiles.contains(metaData))
                .map(metadata -> FileMetaDataDTO.fromFileMetaData(userId, metadata))
                .toList();
        return new BatchOperationResult(successfullyMoved, failedToMove);
    }

    @Transactional
    @Override
    public FileMetaDataDTO rename(Long userId, FilePath pathToFile, String newName) {
        FileMetaData file = fileMetaDataRepository
                .findByUserIdAndPathAndName(userId, pathToFile.getPath(), pathToFile.getFileName())
                .orElseThrow(() -> new UserFileNotFoundException("File does not exist"));

        String newPath = new FilePath()
                .addPartRaw(pathToFile.getPath())
                .addPartRaw(newName).toString();

        fileMetaDataRepository.updateFilePath(userId, file.getAbsolutePath(), newPath);
        file.setName(newName);
        fileMetaDataRepository.save(file);
        fileStorageService.rename(FileMetaDataDTO.fromFileMetaData(userId, file), newName);

        return FileMetaDataDTO.fromFileMetaData(userId, file);
    }
}
