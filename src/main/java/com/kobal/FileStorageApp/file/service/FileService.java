package com.kobal.FileStorageApp.file.service;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface FileService {



    Optional<FileMetaDataDTO> uploadFile(Principal principal, FilePath path, MultipartFile file);

    InputStream download(Principal principal, FilePath path);
    boolean createDirectory(Principal principal, FilePath directoryPath);
    List<FileMetaDataDTO> getFilesInDirectory(Principal principal, FilePath path);

    List<FileMetaDataDTO> deleteFilesInDirectory(Principal principal, FilePath directory, List<String> files);

    List<FileMetaDataDTO> moveFilesToDirectory(Principal principal, FilePath from, FilePath to, List<String> fileNames);

    List<FileMetaDataDTO> copyFilesToDirectory(Principal principal, FilePath from, FilePath to, List<String> fileNames);

    Optional<FileMetaDataDTO> getFileMetaDataByPath(Principal principal, FilePath filePath);

    List<FileMetaDataDTO> findFilesWithName(Principal principal, String filename);
}
