package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.storage.FileMetaDataRepository;
import com.kobal.FileStorageApp.storage.FileStorageService;
import com.kobal.FileStorageApp.user.AppUser;
import com.kobal.FileStorageApp.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.Principal;
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


    public FileServiceImpl(FileMetaDataRepository fileMetaDataRepository, FileStorageService fileStorageService, UserRepository userRepository) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
    }

    @Override
    public FileMetaDataDTO uploadFile(Principal principal, FilePath uploadFilePath, MultipartFile file) {
       AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
               .orElseThrow(() -> new RuntimeException("user not found"));
        FileMetaData parentFolder = fileMetaDataRepository
                .findByUserIdAndAbsolutePath(user.getId(), uploadFilePath.toString())
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

        try {
            fileStorageService.upload(dto, file.getInputStream());
            fileMetaDataRepository.save(fileMetaData);
        } catch (Exception e) {
            System.out.println(e);
            throw new UserFileException("Failed to upload the file");
        }

        return dto;
    }

    @Override
    public InputStream download(Principal principal, FilePath path) {
        Long userId = Long.valueOf(principal.getName());
        FileMetaData fileMetaData = fileMetaDataRepository.findByUserIdAndAbsolutePath(userId, path.toString())
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
    public void createDirectory(Principal principal, FilePath directoryToCreate) {
        AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
                .orElseThrow(() -> new RuntimeException("user not found"));

        Optional<FileMetaData> directoryOptional = fileMetaDataRepository
                .findByUserIdAndAbsolutePath(user.getId(), directoryToCreate.toString());


        if (directoryOptional.isPresent()) {
            throw new UserFileException("A file already exists with name '%s'".formatted(directoryOptional.get().getName()));
        }

        FilePath parent = directoryToCreate.getParent();
        FileMetaData directory = fileMetaDataRepository
                .findByUserIdAndAbsolutePath(user.getId(), parent.toString())
                .orElseThrow(() -> new UserFileNotFoundException("The folder in which you want to create a new folder does not exist."));

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
        fileMetaDataRepository.save(file);
        fileStorageService.createDirectory(dtoFromMetaData(file, user.getId()));
    }

    @Override
    public List<FileMetaDataDTO> getFilesInDirectory(Principal principal, FilePath path) {
        Long userId = Long.valueOf(principal.getName());
        String directoryPath = path.toString();

        Optional<FileMetaData> directory = fileMetaDataRepository.findByUserIdAndAbsolutePath(userId, directoryPath);
        if (directory.isEmpty()) {
            // TODO: log
            throw new UserFileNotFoundException("Could not find file with path '%s'".formatted(path));
        }



        return fileMetaDataRepository
                .getSubFoldersByUserIdAndPath(userId, directory.get().getAbsolutePath())
                .stream()
                .map(metaData -> dtoFromMetaData(metaData, userId))
                .toList();
    }


    @Override
    public List<FileMetaDataDTO> deleteFilesInDirectory(Principal principal, FilePath directory, List<String> fileNames) {
        AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
                .orElseThrow(() -> new RuntimeException("user not found"));
        List<FileMetaData> filesToDelete = fileMetaDataRepository.findByUserIdAndPathAndNames(user.getId(),directory.toString(), fileNames);
        List<FileMetaData> successfullyDeletedFiles = filesToDelete.stream()
                .filter(metaData -> fileStorageService.delete(dtoFromMetaData(metaData, user.getId())))
                .toList();
        fileMetaDataRepository.deleteAllInBatch(successfullyDeletedFiles);

        return successfullyDeletedFiles.stream()
                .map(metaData -> dtoFromMetaData(metaData, user.getId()))
                .toList();
    }

    @Override
    public List<FileMetaDataDTO> copyFilesToDirectory(Principal principal, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {
        AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
                .orElseThrow(() -> new UsernameNotFoundException("Could not find user."));

        List<String> pathsToFiles = fileNames.stream()
                .map(fromDirectory::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToCopy = fileMetaDataRepository
                .getFilesByUserIdAndPathStartingWith(user.getId(), pathsToFiles);

        FileMetaData targetDirectory = fileMetaDataRepository
                .getFileMetaDataByUserIdAndPath(Long.valueOf(user.getName()), toDirectory.toString())
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
    public Optional<FileMetaDataDTO> getFileMetaDataByPath(Principal principal, FilePath filePath) {
        Long userId = Long.valueOf(principal.getName());
        Optional<FileMetaData> dataOptional = fileMetaDataRepository.findByUserIdAndAbsolutePath(userId, filePath.toString());
        return dataOptional
                .map(metaData -> dtoFromMetaData(metaData, userId));
    }

    @Override
    public List<FileMetaDataDTO> moveFilesToDirectory(Principal principal, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {
        Long userId = Long.valueOf(principal.getName());
        List<String> pathsToFiles = fileNames.stream()
                .map(fromDirectory::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToMove = fileMetaDataRepository
                .getFilesByUserIdAndPathStartingWith(userId, pathsToFiles);

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
}
