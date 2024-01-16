package com.kobal.FileStorageApp.file.storage.filesystem;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemFileStoreTest {

    @TempDir
    Path directory;

    private FileSystemFileStore fileSystemFileStore;
    private final Long userId = 1L;
    private Path userRootDirectory;

    @BeforeEach
    void setup() throws IOException {
        FileSystemStorageConfiguration fileSystemStorageConfiguration = new FileSystemStorageConfiguration(directory);
        fileSystemFileStore = new FileSystemFileStore(fileSystemStorageConfiguration);

        this.userRootDirectory = Files.createDirectory(directory.resolve(String.valueOf(userId)));

    }
    @Test
    void upload() throws IOException {
        //given
        boolean success = true;
        FileMetaDataDTO fileMetaData = new FileMetaDataDTO(1L,
                "file.txt",
                "",
                1000L,
                LocalDateTime.now(),
                false,
                UUID.randomUUID());
        //when
        boolean actual = fileSystemFileStore.upload(fileMetaData, FileInputStream.nullInputStream());

        // then
        assertEquals(success, actual);
        assertTrue(Files.exists(userRootDirectory.resolve(fileMetaData.getName())));
    }

    @Test
    void download() throws IOException {
        //given
        FileMetaDataDTO fileMetaData = new FileMetaDataDTO(1L,
                "file.txt",
                "",
                1000L,
                LocalDateTime.now(),
                false,
                UUID.randomUUID());
        byte[] fileContent = "File content".getBytes();
        Path file = createFileInUserDirectoryWithBytes("file.txt", fileContent);

        //when
        try (InputStream actual = fileSystemFileStore.download(fileMetaData)) {
            // then
            assertArrayEquals(actual.readAllBytes(), fileContent);
        }
    }

    @Test
    void deleteShouldPassWhenDeletingSingleFile() {
        //given
        boolean expected = true;
        String fileName = "file_to_delete";
        FileMetaDataDTO fileMetaData = new FileMetaDataDTO(1L,
                fileName,
                "",
                1000L,
                LocalDateTime.now(),
                false,
                UUID.randomUUID());
        Path fileToDelete = createFileInUserDirectoryWithBytes(fileName, new byte[]{});

        //when
        boolean actual = fileSystemFileStore.delete(fileMetaData);
        // then
        assertEquals(expected, actual);
        assertTrue(Files.notExists(userRootDirectory.resolve(fileMetaData.getName())));
    }

    @Test
    void deleteShouldPassWhenDeletingDirectory() throws IOException {
        //given
        boolean expected = true;
        String fileName = "directoryToDelete";
        FileMetaDataDTO fileMetaData = new FileMetaDataDTO(1L,
                fileName,
                "",
                1000L,
                LocalDateTime.now(),
                true,
                UUID.randomUUID());
        Path directoryToDelete = Files.createDirectory(userRootDirectory.resolve(fileName));
        List<Path> files = List.of(
                directoryToDelete.resolve("file1"),
                directoryToDelete.resolve("file2"),
                directoryToDelete.resolve("file3")
        );

        for (Path path : files) {
            Files.createFile(path);
        }

        //when
        boolean actual = fileSystemFileStore.delete(fileMetaData);

        // then
        assertEquals(expected, actual);
        assertTrue(Files.notExists(userRootDirectory.resolve(fileMetaData.getName())));
        for (Path path : files) {
            assertTrue(Files.notExists(path));
        }
    }

    @Test
    void renameShouldSucceed() {
        // given
        FileMetaDataDTO dto = createDTOWithRandomData(userId, 0l, false);
        Path filepath = createFileInUserDirectoryWithBytes(dto.getName(), new byte[]{});
        String newName = "changedFileName";

        // when
        boolean result = fileSystemFileStore.rename(dto, newName);
        File renamedFile = filepath.getParent().resolve(newName).toFile();

        // then
        assertTrue(result);
        assertTrue(renamedFile.exists());
        assertFalse(filepath.toFile().exists());
    }

    @Test
    void renameShouldFailIfFileDoesNotExist() {
        // given
        FileMetaDataDTO dto = createDTOWithRandomData(userId, 0l, false);
        String newName = "changedFileName";

        // when
        boolean result = fileSystemFileStore.rename(dto, newName);

        // then
        assertFalse(result);
    }

    @Test
    void renameShouldFailIfFileAlreadyExistsWithName() {
        // given
        String newName = "changedFileName";
        FileMetaDataDTO dto = createDTOWithRandomData(userId, 0l, false);
        Path toRename = createFileInUserDirectoryWithBytes(dto.getName(), new byte[]{});
        Path destination = createFileInUserDirectoryWithBytes(newName, new byte[]{});

        // when
        boolean succeeded = fileSystemFileStore.rename(dto, newName);
        // then
        assertFalse(succeeded);
    }


    @Test
    void moveShouldSucceedIfDestinationExists() {
        // given
        FileMetaDataDTO source = createDTOWithRandomData(userId, 0l, true);
        createFileInUserDirectoryWithBytes(source.getName(), new byte[]{});

        File destination = createDirectoryInUserDirectory(randomString()).toFile();
        FileMetaDataDTO destinationDTO = new FileMetaDataDTO(userId,
                destination.getName(),
                "",
                0l,
                LocalDateTime.now(),
                true,
                UUID.randomUUID());


        // when
        boolean result = fileSystemFileStore.move(source, destinationDTO);

        // then
        assertTrue(result);
    }

    @Test
    void copyShouldReturnTrueIfFileAndDestinationExists() {
        // given
        FileMetaDataDTO source = createDTOWithRandomData(userId, 0l, true);
        createFileInUserDirectoryWithBytes(source.getName(), new byte[]{});

        File destinationDirectory = createDirectoryInUserDirectory(randomString()).toFile();
        FileMetaDataDTO destinationDTO = new FileMetaDataDTO(userId,
                destinationDirectory.getName(),
                "",
                0l,
                LocalDateTime.now(),
                true,
                UUID.randomUUID());


        // when
        boolean result = fileSystemFileStore.copy(source, destinationDTO);

        // then
        assertTrue(result);
        File sourceFile = fileSystemFileStore.getFullPathFromMetaData(source).toFile();
        assertTrue(sourceFile.exists());
        File copy = fileSystemFileStore.getFullPathFromMetaData(destinationDTO).resolve(sourceFile.getName()).toFile();
        assertTrue(copy.exists());
    }

    @Test
    void createDirectoryShouldReturnFalseIfPathDoesNotExist() {
        // given
        String notExistingDirectory = randomString();
        FileMetaDataDTO dto = createDTOWithRandomDataAtPath(userId, 0l, true, notExistingDirectory);

        // when
        boolean result = fileSystemFileStore.createDirectory(dto);

        // then
        assertFalse(result);
    }

    @Test
    void createDirectoryShouldReturnTrueIfPathExists() {
        // given
        FileMetaDataDTO dto = createDTOWithRandomData(userId, 0l, true);
        // when
        boolean result = fileSystemFileStore.createDirectory(dto);

        // then
        assertTrue(result);
    }

    private Path createFileInUserDirectoryWithBytes(String fileName, byte[] data) {
        Path file = null;
        try {
            file = Files.createFile(userRootDirectory.resolve(fileName));
            Files.write(file, data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private Path createDirectoryInUserDirectory(String directoryName) {
        Path file;
        try {
            file = Files.createDirectory(userRootDirectory.resolve(directoryName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    private FileMetaDataDTO createDTOWithRandomData(Long userId, Long size, boolean isdirectory) {
        return new FileMetaDataDTO(
                userId,
                randomString(),
                "",
                size,
                LocalDateTime.now(),
                isdirectory,
                UUID.randomUUID());
    }

    private FileMetaDataDTO createDTOWithRandomDataAtPath(Long userId, Long size, boolean isdirectory, String path) {
        return new FileMetaDataDTO(
                userId,
                randomString(),
                path,
                size,
                LocalDateTime.now(),
                isdirectory,
                UUID.randomUUID());
    }

    private String randomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}