package com.kobal.FileStorageApp.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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

    AppUser user = new AppUser(1L, "user", OAuthIssuer.GOOGLE, "sub");
    Principal principal = () -> String.valueOf(user.getId());

    private final FilePath notExistingDirectoryPath = new FilePath()
            .addPartEncoded("path")
            .addPartEncoded("to")
            .addPartDecoded("notExitingDir");

    private final FilePath existingDirectoryPath = new FilePath()
            .addPartEncoded("path")
            .addPartEncoded("to")
            .addPartDecoded("existingDirectory");

    private final FileMetaData directoryMetaData = createMetaData(existingDirectoryPath, user, true);


    @Test
    void uploadFileShouldThrowFileNotFoundExceptionWhenDirectoryDoesNotExist() throws Exception {

        // given
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        // then
        assertThrows(UserFileNotFoundException.class, () -> fileService.uploadFile(user.getId(), notExistingDirectoryPath, file));
    }

    @Test
    void uploadFileShouldPassWhenDirectoryExists() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(directoryMetaData));

        // when
        Optional<FileMetaDataDTO> fileMetaDataDTO = fileService.uploadFile(user.getId(), existingDirectoryPath, file);

        // then
        assertTrue(fileMetaDataDTO.isPresent());
        assertEquals(fileMetaDataDTO.get().getPath(), directoryMetaData.getAbsolutePath());
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
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndPathAndName(
                        user.getId(),
                        existingDirectoryPath.getPath(),
                        existingDirectoryPath.getFileName()))
                .thenReturn(Optional.of(directoryMetaData));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.createDirectory(user.getId(), existingDirectoryPath));
    }

    @Test
    void createDirectoryShouldThrowUserFileExceptionIfPathIsNotDirectory() {
        // given
        FilePath pathToExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        FileMetaData file = createMetaData(pathToExistingFile, user, false);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
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
    void deleteFilesInDirectoryShouldThrowFileNotFoundExceptionIfPathDoesNotExist() {
    }

    @Test
    void copyFilesToDirectory() {
    }

    @Test
    void getFileMetaDataByPath() {
    }

    @Test
    void moveFilesToDirectory() {
    }


    private FileMetaData createMetaData(FilePath path, AppUser user, boolean isDirectory) {
        return new FileMetaData(1L, path.getFileName(), 1000L, LocalDateTime.now(), isDirectory, path.getParent().toString());
    }
}