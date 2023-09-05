package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.fileservice.FilePath;

import java.io.InputStream;

public interface FileStorageService {
    boolean upload(FileMetaDataDTO metaDataDTO, InputStream inputStream);
    InputStream download(FileMetaDataDTO metaDataDTO);

    boolean delete(FileMetaDataDTO metaDataDTO);

    boolean rename(FileMetaDataDTO metaDataDTO, String name);
}
