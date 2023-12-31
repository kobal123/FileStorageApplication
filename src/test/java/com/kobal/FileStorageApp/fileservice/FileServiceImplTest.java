package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.storage.FileMetaDataRepository;
import com.kobal.FileStorageApp.storage.FileStorageService;
import com.kobal.FileStorageApp.user.AppUser;
import com.kobal.FileStorageApp.user.UserRepository;
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

    AppUser user = new AppUser(1L, "user","user@email.com", "password");
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
        assertThrows(UserFileNotFoundException.class, () -> fileService.uploadFile(principal, notExistingDirectoryPath, file));
    }

    @Test
    void uploadFileShouldPassWhenDirectoryExists() throws Exception {
        // given
        MultipartFile file = new MockMultipartFile("file.txt", "file content".getBytes());
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), existingDirectoryPath.toString()))
                .thenReturn(Optional.of(directoryMetaData));

        // when
        FileMetaDataDTO fileMetaDataDTO = fileService.uploadFile(principal, existingDirectoryPath, file);

        // then
        assertEquals(fileMetaDataDTO.getPath(), directoryMetaData.getAbsolutePath());
    }

    @Test
    void downloadShouldThrowFileNotFoundExceptionWhenDirectoryDoesNotExist() {
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), notExistingDirectoryPath.toString()))
                .thenReturn(Optional.empty());
        assertThrows(UserFileNotFoundException.class, () -> fileService.download(principal, notExistingDirectoryPath));
    }

    @Test
    void createDirectoryShouldThrowUserFileExceptionIfDirectoryAlreadyExists() {
        // given
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), existingDirectoryPath.toString()))
                .thenReturn(Optional.of(directoryMetaData));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.createDirectory(principal, existingDirectoryPath));
    }

    @Test
    void createDirectoryShouldThrowUserFileExceptionIfPathIsNotDirectory() {
        // given
        FilePath pathToExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        FileMetaData file = createMetaData(pathToExistingFile, user, false);
        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), pathToExistingFile.toString()))
                .thenReturn(Optional.of(file));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.createDirectory(principal, pathToExistingFile));
    }

    @Test
    void getFilesInDirectoryShouldThrowUserFileExceptionIfPathIsNotDirectory() {
        // given
        FilePath pathToExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        FileMetaData file = createMetaData(pathToExistingFile, user, false);
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), pathToExistingFile.toString()))
                .thenReturn(Optional.of(file));

        // when
        // then
        assertThrows(UserFileException.class, () -> fileService.getFilesInDirectory(principal, pathToExistingFile));
    }

    @Test
    void getFilesInDirectoryShouldThrowUsrFileNotFoundExceptionIfPathDoesNotExist() {
        // given
        FilePath pathToNotExistingFile = new FilePath().addPartEncoded("path").addPartEncoded("to_file");
        Mockito.when(fileMetaDataRepository.findByUserIdAndAbsolutePath(user.getId(), pathToNotExistingFile.toString()))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThrows(UserFileNotFoundException.class, () -> fileService.getFilesInDirectory(principal, pathToNotExistingFile));
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