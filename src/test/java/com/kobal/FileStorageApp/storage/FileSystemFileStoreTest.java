package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.FileMetaDataDTO;
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
import java.util.Arrays;
import java.util.List;
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
            assertTrue(Arrays.equals(actual.readAllBytes(), fileContent));
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
    void rename() {
        //TODO: implementation
    }

    @Test
    void move() {
        //TODO: implementation
    }

    @Test
    void copy() {
        //TODO: implementation
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
}