package com.kobal.FileStorageApp.file.storage.google;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.*;
import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.file.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.file.Paths;


@Service
@ConditionalOnProperty(
        value="storage.current",
        havingValue = "google-cloud",
        matchIfMissing = false)
public class GoogleCloudFileStore implements FileStorageService {

    private final String bucketName;
    private final Storage storage;

    public GoogleCloudFileStore(@Value("${google.projectId}") String projectId,
                                @Value("${google.bucket}") String bucketName,
                                @Value("${google.credentialPath}") String credential) throws IOException {
        this.bucketName = bucketName;
        this.storage = StorageOptions

                .newBuilder()
                .setProjectId(projectId)
                .setCredentials(GoogleCredentials.fromStream(
                        new FileInputStream(Paths.get(credential).toFile())
                ))
                .build()
                .getService();
    }

    @Override
    public boolean upload(FileMetaDataDTO metaDataDTO, InputStream inputStream) {
        String fileName = getFileName(metaDataDTO);
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo
                .newBuilder(blobId)
                .setContentType("text/plain")
                .build();
        try {
            storage.createFrom(blobInfo, inputStream);
            return true;
        } catch (IOException exception) {
            // log
            return false;
        }
    }

    private String getFileName(FileMetaDataDTO metaDataDTO) {
        return metaDataDTO.getUserId() + "/" + metaDataDTO.getUuid();
    }

    @Override
    public InputStream download(FileMetaDataDTO metaDataDTO) {
        String fileName = getFileName(metaDataDTO);
        Blob blob = storage.get(BlobId.of(bucketName, fileName));
        if (blob == null) {
            return InputStream.nullInputStream();
        }
        ReadChannel readChannel = blob.reader();
        return Channels.newInputStream(readChannel);
    }

    @Override
    public boolean delete(FileMetaDataDTO metaDataDTO) {
        String fileName = getFileName(metaDataDTO);
        Blob blob = storage.get(BlobId.of(bucketName, fileName));
        if (blob == null)
            return true;
        return storage.delete(blob.getBlobId());
    }

    @Override
    public boolean rename(FileMetaDataDTO metaDataDTO, String name) {
        // empty on purpose
        return true;
    }

    @Override
    public boolean move(FileMetaDataDTO source, FileMetaDataDTO target) {
        // empty on purpose
        return true;
    }

    @Override
    public boolean copy(FileMetaDataDTO source, FileMetaDataDTO target) {
        // check if blob exists
        String sourceFileName = getFileName(source);
        String targetFileName = getFileName(target);
        Blob sourceBlob = storage.get(BlobId.of(bucketName, sourceFileName));
        if (sourceBlob == null) {
            throw new RuntimeException();
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, targetFileName).build();
        storage.copy(Storage.CopyRequest.of(sourceBlob.getBlobId(), blobInfo));

        return false;
    }

    @Override
    public boolean createDirectory(FileMetaDataDTO fileMetaDataDTO) {
        // empty on purpose
        return true;
    }
}
