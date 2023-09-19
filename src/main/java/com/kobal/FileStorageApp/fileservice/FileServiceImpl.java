package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.storage.FileMetaDataRepository;
import com.kobal.FileStorageApp.storage.FileStorageService;
import com.kobal.FileStorageApp.storage.FileSystemStorageConfiguration;
import com.kobal.FileStorageApp.user.AppUser;
import com.kobal.FileStorageApp.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
    private  final Path BASE_PATH;


    public FileServiceImpl(FileMetaDataRepository fileMetaDataRepository, FileStorageService fileStorageService, UserRepository userRepository, FileSystemStorageConfiguration fileSystemStorageConfiguration) {
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        BASE_PATH = fileSystemStorageConfiguration.getRoot();
    }

    @Override
    public FileMetaDataDTO uploadFile(Principal principal, FilePath uploadFilePath, MultipartFile file) {
       AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
               .orElseThrow(() -> new RuntimeException("user not found"));

        FilePath filePath = new FilePath("root")
                .addPartRaw(String.valueOf(user.getId()))
                .addPartRaw(uploadFilePath.toString());


        FileMetaData parentFolder = fileMetaDataRepository
                .findByUserIdAndAbsolutePath(user.getId(), filePath.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Folder does not exist"));


        FileMetaData fileMetaData = new FileMetaData();
        fileMetaData.setModified(LocalDateTime.now(ZoneId.of("UTC")));
        fileMetaData.setParent(parentFolder);
        fileMetaData.setUser(user);
        fileMetaData.setFileUUID(UUID.randomUUID());
        fileMetaData.setIsDirectory(false);
        fileMetaData.setSize(file.getSize());
        fileMetaData.setName(file.getOriginalFilename());
        String absolute = parentFolder.getAbsolutePath();
        fileMetaData.setPath(absolute);// path is the path up to the folder containing the file

        try {
            FileMetaDataDTO dto = dtoFromMetaData(fileMetaData);
            fileStorageService.upload(dto, file.getInputStream());
            fileMetaDataRepository.save(fileMetaData);
        } catch (IOException e) {
            throw new UserFileException("Failed to a file");
        }

        return dtoFromMetaData(fileMetaData);
    }

    @Override
    public InputStream download(Principal principal, FilePath path) {
        Long userId = Long.valueOf(principal.getName());
        FileMetaData fileMetaData = fileMetaDataRepository.findByUserIdAndAbsolutePath(userId, path.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find file"));

        FileMetaDataDTO dto = dtoFromMetaData(fileMetaData);
        return fileStorageService.download(dto);
    }

    private FileMetaDataDTO dtoFromMetaData(FileMetaData metaData) {
        return new FileMetaDataDTO(metaData.getName(),
                metaData.getPath(),
                metaData.getSize(),
                metaData.getModified(),
                metaData.isDirectory(),
                metaData.getFileUUID());
    }

    @Override
    public void createDirectory(Principal principal, FilePath directoryToCreate) {
        Path userRootDirectory = BASE_PATH.resolve(principal.getName());
        Path directoryPath = userRootDirectory.resolve(directoryToCreate.toString());
        File directory = directoryPath.toFile();

        if (directory.exists())
            throw new UserFileException("A file or directory already exists with this name");

        boolean wasCreated = directory.mkdir();

        if (!wasCreated) {
            throw new UserFileException("Failed to create directory");
        }
    }

    @Override
    public List<FileMetaDataDTO> getFilesInDirectory(Principal principal, FilePath pathToDirectory) {
        Long userId = Long.valueOf(principal.getName());
        FilePath filePath = new FilePath("root")
                .addPartRaw(principal.getName())
                .addPartRaw(pathToDirectory.toString());

        return fileMetaDataRepository
                .getSubFoldersByUserIdAndPath(userId, filePath.toString())
                .stream()
                .map(this::dtoFromMetaData)
                .toList();
    }


    @Override
    public List<FileMetaDataDTO> deleteFilesInDirectory(Principal principal, FilePath directory, List<String> fileNames) {
        List<String> pathsToFiles = fileNames.stream()
                .map(name -> FilePath.from(directory)
                        .addPartEncoded(name)
                )
                .map(FilePath::toString)
                .toList();

        AppUser user = userRepository.findById(Long.valueOf(principal.getName()))
                .orElseThrow(() -> new RuntimeException("user not found"));

        List<FileMetaData> filesToDelete = fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), pathsToFiles);
        List<FileMetaData> successfullyDeletedFiles = filesToDelete.stream()
                .filter(metaData -> fileStorageService.delete(dtoFromMetaData(metaData)))
                .toList();
        fileMetaDataRepository.deleteAllInBatch(successfullyDeletedFiles);

        return successfullyDeletedFiles.stream().map(this::dtoFromMetaData).toList();
    }

    @Override
    public List<FileMetaDataDTO> copyFilesToDirectory(Principal principal, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {
        AppUser user = checkUserExistsOrElseThrow(principal.getName(), "User could not be found");
        FilePath sourceDirectoryPath = addRootPrefixToPath(fromDirectory, user.getId());
        FilePath targetDirectoryPath = addRootPrefixToPath(toDirectory, user.getId());
        List<String> pathsToFiles = fileNames.stream()
                .map(sourceDirectoryPath::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToCopy = fileMetaDataRepository
                .getFilesByUserIdAndPathStartingWith(user.getId(), pathsToFiles);

        FileMetaData targetDirectory = fileMetaDataRepository
                .getFileMetaDataByUserIdAndPath(Long.valueOf(user.getName()), targetDirectoryPath.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        FileMetaDataDTO targetDirectoryDTO = dtoFromMetaData(targetDirectory);
        List<FileMetaData> successfullyCopiedFiles = filesToCopy.stream()
                .filter(metaData -> fileStorageService.copy(dtoFromMetaData(metaData), targetDirectoryDTO))
                .toList();

        List<FileMetaData> filesToCreate = successfullyCopiedFiles.stream().map(metaData -> {
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

        return successfullyCopiedFiles.stream().map(this::dtoFromMetaData).toList();
    }

    @Override
    public Optional<FileMetaDataDTO> getFileMetaDataByPath(Principal principal, FilePath filePath) {
        Long userId = Long.valueOf(principal.getName());
        Optional<FileMetaData> dataOptional = fileMetaDataRepository.getFileMetaDataByUserIdAndName(userId, filePath.toString());
        return dataOptional.map(this::dtoFromMetaData);
    }

    @Override
    public List<FileMetaDataDTO> moveFilesToDirectory(Principal principal, FilePath fromDirectory, FilePath toDirectory, List<String> fileNames) {
        AppUser user = checkUserExistsOrElseThrow(principal.getName(), "User could not be found");
        FilePath sourceDirectoryPath = addRootPrefixToPath(fromDirectory, user.getId());
        FilePath targetDirectoryPath = addRootPrefixToPath(toDirectory, user.getId());
        List<String> pathsToFiles = fileNames.stream()
                .map(sourceDirectoryPath::addPartRawCopy)
                .map(FilePath::toString)
                .toList();

        List<FileMetaData> filesToMove = fileMetaDataRepository
                .getFilesByUserIdAndPathStartingWith(user.getId(), pathsToFiles);

        FileMetaData targetDirectory = fileMetaDataRepository
                .getFileMetaDataByUserIdAndPath(Long.valueOf(user.getName()), targetDirectoryPath.toString())
                .orElseThrow(() -> new UserFileNotFoundException("Could not find directory"));

        FileMetaDataDTO targetDirectoryDTO = dtoFromMetaData(targetDirectory);
        List<FileMetaData> successfullyMovedFiles = filesToMove.stream()
                        .filter(metaData -> fileStorageService.move(dtoFromMetaData(metaData), targetDirectoryDTO))
                        .toList();

        List<Long> movedFilesIds = successfullyMovedFiles.stream().map(FileMetaData::getId).toList();
        fileMetaDataRepository.updateFilePathsByIdAndUserId(user.getId(), movedFilesIds, targetDirectoryDTO.getAbsolutePath());

        return successfullyMovedFiles.stream().map(this::dtoFromMetaData).toList();
    }

    private AppUser checkUserExistsOrElseThrow(String id, String message) {
        return userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException(message));
    }

    private FilePath addRootPrefixToPath(FilePath path, Long userId) {
        return new FilePath("root")
                .addPartRaw(String.valueOf(userId))
                .addPartRaw(path);
    }

}
