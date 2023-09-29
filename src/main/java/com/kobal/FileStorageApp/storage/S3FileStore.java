package com.kobal.FileStorageApp.storage;

import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.fileservice.FilePath;

import java.io.InputStream;
import java.util.List;

public class S3FileStore implements FileStorageService {

    @Override
    public boolean upload(FileMetaDataDTO metaDataDTO, InputStream inputStream) {
        return false;
    }

    @Override
    public InputStream download(FileMetaDataDTO metaDataDTO) {
        return null;
    }

    @Override
    public boolean delete(FileMetaDataDTO metaDataDTO) {
        return false;
    }

    @Override
    public boolean rename(FileMetaDataDTO metaDataDTO, String name) {
        return false;
    }

    @Override
    public boolean move(FileMetaDataDTO source, FileMetaDataDTO target) {
        return false;
    }

    @Override
    public boolean copy(FileMetaDataDTO source, FileMetaDataDTO target) {
        return false;
    }

    @Override
    public boolean createDirectory(FileMetaDataDTO fileMetaDataDTO) {
        return false;
    }
}
