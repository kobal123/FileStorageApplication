package com.kobal.FileStorageApp.file.service;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FileService {

    Optional<FileMetaDataDTO> uploadFile(Long userId, FilePath path, MultipartFile file);

    InputStream download(Long userId, FilePath path);
    boolean createDirectory(Long userId, FilePath directoryPath);
    List<FileMetaDataDTO> getFilesInDirectory(Long userId, FilePath path);

    List<FileMetaDataDTO> deleteFilesInDirectory(Long userId, FilePath directory, List<String> files);

    List<FileMetaDataDTO> moveFilesToDirectory(Long userId, FilePath from, FilePath to, List<String> fileNames);

    void rename(Long userId, FilePath pathToFile, String newName);

    List<FileMetaDataDTO> copyFilesToDirectory(Long userId, FilePath from, FilePath to, List<String> fileNames);

    Optional<FileMetaDataDTO> getFileMetaDataByPath(Long userId, FilePath filePath);

    List<FileMetaDataDTO> searchFiles(Long userId, String filename, boolean directoryOnly);
}
