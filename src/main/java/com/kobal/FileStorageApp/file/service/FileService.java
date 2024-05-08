package com.kobal.FileStorageApp.file.service;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface FileService {

    FileMetaDataDTO uploadFile(Long userId, FilePath path, MultipartFile file);

    InputStream download(Long userId, FilePath path);
    boolean createDirectory(Long userId, FilePath directoryPath);
    List<FileMetaDataDTO> getFilesInDirectory(Long userId, FilePath path);

    BatchOperationResult deleteFilesInDirectory(Long userId, List<FilePath> files);

    BatchOperationResult moveFilesToDirectory(Long userId, List<FilePath> files, FilePath target);

    FileMetaDataDTO rename(Long userId, FilePath pathToFile, String newName);

    BatchOperationResult copyFilesToDirectory(Long userId, List<FilePath> files, FilePath targetDirectoryPath);

    Optional<FileMetaDataDTO> getFileMetaDataByPath(Long userId, FilePath filePath);

    List<FileMetaDataDTO> searchFiles(Long userId, String filename, boolean directoryOnly);
}
