package com.kobal.FileStorageApp.fileservice;

import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.FileMetaDataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface FileService {



    FileMetaDataDTO uploadFile(Principal principal, FilePath path, MultipartFile file);

    InputStream download(Principal principal, FilePath path);
    void createDirectory(Principal principal, FilePath directoryPath);
    List<FileMetaDataDTO> getFilesInDirectory(Principal principal, FilePath path);

    List<String> deleteFilesInDirectory(Principal principal, FilePath directory, List<String> files);

    List<String> moveFilesToDirectory(Principal principal, FilePath from, FilePath to, List<String> fileNames);

    List<String> copyFilesToDirectory(Principal principal, FilePath from, FilePath to, List<String> fileNames);

    Optional<FileMetaDataDTO> getFileMetaDataByUserNameAndFilePath(String name, FilePath filePath);
}
