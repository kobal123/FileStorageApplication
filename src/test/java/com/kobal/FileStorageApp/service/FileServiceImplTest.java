package com.kobal.FileStorageApp.service;

import com.kobal.FileStorageApp.exceptions.UserStorageSpaceExecption;
import com.kobal.FileStorageApp.file.service.BatchOperationResult;
import com.kobal.FileStorageApp.file.service.FilePath;
import com.kobal.FileStorageApp.file.service.FileServiceImpl;
import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.file.persistence.FileMetaDataRepository;
import com.kobal.FileStorageApp.file.storage.FileStorageService;
import com.kobal.FileStorageApp.user.OAuthIssuer;
import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.persistence.UserRepository;
import com.kobal.FileStorageApp.user.storage.UserStorageInfo;
import com.kobal.FileStorageApp.user.storage.UserStorageInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {


    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private FileMetaDataRepository fileMetaDataRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserStorageInfoRepository userStorageInfoRepository;
    private Long createdMetadataCount = 0L;

    AppUser user = new AppUser(1L, "user00@gmail.com", OAuthIssuer.GOOGLE, "sub0");
    UserStorageInfo userStorageInfo = new UserStorageInfo(0L, user, 0L);
    UserStorageInfo userStorageInfoNoSpaceAvailable = new UserStorageInfo(0L, user, 1000*1000*1000L);


    private final FilePath notExistingDirectoryPath = new FilePath()
            .addPartEncoded("path")
            .addPartEncoded("to")
            .addPartDecoded("notExitingDir");

    private final FilePath existingDirectoryPath = new FilePath()
            .addPartEncoded("path")
            .addPartEncoded("to")
            .addPartDecoded("existingDirectory");

    private final FileMetaData existingDirectoryMetaData = createMetaData(existingDirectoryPath, user, true);
    private final FileMetaDataDTO existingDirectoryMetaDataDTO = FileMetaDataDTO
            .fromFileMetaData(user.getId(), existingDirectoryMetaData);

    @Test
    void uploadFileShouldThrowFileNotFoundExceptionWhenDirectoryDoesNotExist() {

        // given
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        // when
        // then
        assertThrows(UserFileNotFoundException.class, () -> fileService.uploadFile(user.getId(), notExistingDirectoryPath, file));
    }

    @Test
    void uploadFileShouldPassWhenDirectoryExists() {
        // given
        String expected = existingDirectoryMetaData.getAbsolutePath();
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfo);

        // when
        FileMetaDataDTO actual = fileService.uploadFile(user.getId(), existingDirectoryPath, file);

        // then
        assertEquals(expected, actual.getPath());
    }


    @Test
    void uploadFileFailIfNotEnoughSpaceAvailable() {
        // given
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfoNoSpaceAvailable);

        // when
        // then
        assertThrows(UserStorageSpaceExecption.class, () ->
                fileService.uploadFile(user.getId(), existingDirectoryPath, file));
    }

    @Test
    void downloadShouldThrowFileNotFoundExceptionWhenDirectoryDoesNotExist() {
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(user.getId(),
                        notExistingDirectoryPath.getPath(),
                        notExistingDirectoryPath.getFileName()))
                .thenReturn(Optional.empty());
        assertThrows(UserFileNotFoundException.class, () -> fileService.download(user.getId(), notExistingDirectoryPath));
    }

    @Test
    void createDirectoryShouldThrowUserFileExceptionIfDirectoryAlreadyExists() {
        // given
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.createDirectory(user.getId(), existingDirectoryPath));
    }

    @Test
    void createDirectoryShouldThrowUserFileExceptionIfPathIsNotDirectory() {
        // given
        FilePath pathToExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        FileMetaData file = createMetaData(pathToExistingFile, user, false);
//        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        pathToExistingFile.getPath(),
                        pathToExistingFile.getFileName()))
                .thenReturn(Optional.of(file));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.createDirectory(user.getId(), pathToExistingFile));
    }

    @Test
    void getFilesInDirectoryShouldThrowUserFileExceptionIfPathIsNotDirectory() {
        // given
        FilePath pathToExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        FileMetaData file = createMetaData(pathToExistingFile, user, false);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        pathToExistingFile.getPath(),
                        pathToExistingFile.getFileName()))
                .thenReturn(Optional.of(file));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.getFilesInDirectory(user.getId(), new FilePath(file.getAbsolutePath())));
    }

    @Test
    void getFilesInDirectoryShouldThrowUsrFileNotFoundExceptionIfPathDoesNotExist() {
        // given
        FilePath pathToNotExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        pathToNotExistingFile.getPath(),
                        pathToNotExistingFile.getFileName()))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThrows(UserFileNotFoundException.class, () -> fileService.getFilesInDirectory(user.getId(), pathToNotExistingFile));
    }

    @Test
    void deleteFilesInDirectoryShouldPass() {

        // given
        BatchOperationResult expected = new BatchOperationResult(List.of(existingDirectoryMetaDataDTO), List.of());
        Boolean deleteSuccess = true;
        List<FilePath> filePaths = List.of(existingDirectoryPath);
        List<String> fileNames = List.of(existingDirectoryPath.getFileName());
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndNames(
                user.getId(),
                existingDirectoryPath.getPath(),
                fileNames)).thenReturn(List.of(existingDirectoryMetaData));

        Mockito.when(fileStorageService.delete(existingDirectoryMetaDataDTO))
                .thenReturn(deleteSuccess);
       BatchOperationResult actual = fileService.deleteFilesInDirectory(user.getId(), filePaths);
        assertEquals(expected, actual);
    }

    @Test
    void copyFilesToDirectoryShouldPass() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        FileMetaData testFileMetadata = createMetaData(testFilepath, user, false);
        FileMetaDataDTO testFileMetadataDTO = FileMetaDataDTO.fromFileMetaData(user.getId(), testFileMetadata);
        BatchOperationResult expected = new BatchOperationResult(List.of(testFileMetadataDTO), List.of());

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfo);
        Mockito.when(fileStorageService.copy(testFileMetadataDTO, existingDirectoryMetaDataDTO))
                .thenReturn(true);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndNames(
                        user.getId(),
                        testFilepath.getPath(),
                        List.of(testFilepath.getFileName())))
                .thenReturn(List.of(testFileMetadata));

        // when
        BatchOperationResult actual = fileService.copyFilesToDirectory(user.getId(), filePaths, existingDirectoryPath);

        // then
        assertEquals(expected, actual);
    }

@Test
    void copyFilesToDirectoryShouldFailIfNotEnoughSpaceAvailable() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        FileMetaData testFileMetadata = createMetaData(testFilepath, user, false);
        FileMetaDataDTO testFileMetadataDTO = FileMetaDataDTO.fromFileMetaData(user.getId(), testFileMetadata);

        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfoNoSpaceAvailable);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndNames(
                        user.getId(),
                        testFilepath.getPath(),
                        List.of(testFilepath.getFileName())))
                .thenReturn(List.of(testFileMetadata));
        // when
        // then
        assertThrows(UserStorageSpaceExecption.class,
                () -> fileService.copyFilesToDirectory(user.getId(), filePaths, existingDirectoryPath));
    }


    @Test
    void copyFilesToDirectoryShouldFailIfTargetDoesNotExist() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        notExistingDirectoryPath.getPath(),
                        notExistingDirectoryPath.getFileName()))
                .thenReturn(Optional.empty());
        // when
        // then
        assertThrows(UserFileNotFoundException.class, () ->
                fileService.copyFilesToDirectory(user.getId(), filePaths, notExistingDirectoryPath));
    }


    @Test
    void moveFilesToDirectoryShouldPassIfTargetDirectoryExists() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        FileMetaData testFileMetadata = createMetaData(testFilepath, user, false);
        FileMetaDataDTO testFileMetadataDTO = FileMetaDataDTO.fromFileMetaData(user.getId(), testFileMetadata);
        BatchOperationResult expected = new BatchOperationResult(List.of(testFileMetadataDTO), List.of());

        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfo);
        Mockito.when(fileStorageService.move(testFileMetadataDTO, existingDirectoryMetaDataDTO))
                .thenReturn(true);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndNames(
                        user.getId(),
                        testFilepath.getPath(),
                        List.of(testFilepath.getFileName())))
                .thenReturn(List.of(testFileMetadata));

        // when
        BatchOperationResult actual = fileService.moveFilesToDirectory(user.getId(), filePaths, existingDirectoryPath);

        // then
        assertEquals(expected, actual);
    }


    @Test
    void moveFilesToDirectoryShouldFailIfTargetDoesNotExist() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        notExistingDirectoryPath.getPath(),
                        notExistingDirectoryPath.getFileName()))
                .thenReturn(Optional.empty());
        // when
        // then
        assertThrows(UserFileNotFoundException.class, () ->
                fileService.moveFilesToDirectory(user.getId(), filePaths, notExistingDirectoryPath));
    }

    @Test
    void moveFilesToDirectoryShouldFailIfNotEnoughSpaceAvailable() {
        // given
        FilePath testFilepath = FilePath.raw("a/b/c/file.txt");
        List<FilePath> filePaths = List.of(testFilepath);
        FileMetaData testFileMetadata = createMetaData(testFilepath, user, false);
        Mockito.when(userStorageInfoRepository.getByUserId(user.getId())).thenReturn(userStorageInfoNoSpaceAvailable);
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(existingDirectoryMetaData));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndNames(
                        user.getId(),
                        testFilepath.getPath(),
                        List.of(testFilepath.getFileName())))
                .thenReturn(List.of(testFileMetadata));
        // when
        // then
        assertThrows(UserStorageSpaceExecption.class,
                () -> fileService.moveFilesToDirectory(user.getId(), filePaths, existingDirectoryPath));
    }

    private FileMetaData createMetaData(FilePath path, AppUser user, boolean isDirectory) {
        FileMetaData fileMetaData = new FileMetaData(createdMetadataCount++, path.getFileName(), 1000L, LocalDateTime.now(), isDirectory, path.getPath());
        fileMetaData.setFileUUID(UUID.randomUUID());
        fileMetaData.setUser(user);
        return fileMetaData;
    }
}