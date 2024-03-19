package com.kobal.FileStorageApp.file.service;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;

import java.util.List;

public record BatchOperationResult (
    List<FileMetaDataDTO> success,
    List<FileMetaDataDTO> failed
){}
