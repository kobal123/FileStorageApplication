package com.kobal.FileStorageApp.file.storage;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import java.io.InputStream;

public interface FileStorageService {
    boolean upload(FileMetaDataDTO metaDataDTO, InputStream inputStream);
    InputStream download(FileMetaDataDTO metaDataDTO);

    boolean delete(FileMetaDataDTO metaDataDTO);

    boolean rename(FileMetaDataDTO metaDataDTO, String name);

    boolean move(FileMetaDataDTO source, FileMetaDataDTO target);

    boolean copy(FileMetaDataDTO source, FileMetaDataDTO target);

    boolean createDirectory(FileMetaDataDTO fileMetaDataDTO);
}
