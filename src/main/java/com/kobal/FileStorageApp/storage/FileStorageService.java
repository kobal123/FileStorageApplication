package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaDataDTO;
import java.io.InputStream;
import java.util.List;

public interface FileStorageService {
    boolean upload(FileMetaDataDTO metaDataDTO, InputStream inputStream);
    InputStream download(FileMetaDataDTO metaDataDTO);

    boolean delete(FileMetaDataDTO metaDataDTO);

    boolean rename(FileMetaDataDTO metaDataDTO, String name);

    boolean move(FileMetaDataDTO source, FileMetaDataDTO target);

    boolean copy(FileMetaDataDTO source, FileMetaDataDTO target);
}
