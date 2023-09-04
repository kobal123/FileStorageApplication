package com.kobal.FileStorageApp.storage;



import com.kobal.FileStorageApp.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {

    List<FileMetaData> getChildrenByUserNameAndParentPath(String username, String path);

    List<FileMetaData> getChildrenByUserNameAndPath(String username, String path);

    Optional<FileMetaData> getFileMetaDataByUserNameAndPath(String username, String path);

    Optional<FileMetaData> getFileMetaDataByUserNameAndName(String username, String name);

    @Query("SELECT f FROM FileMetaData f WHERE CONCAT(f.path, '/', f.name) = :absolutePath AND f.user.name = :userName")
    Optional<FileMetaData> findByAbsolutePath(@Param("userName") String username,
                                              @Param("absolutePath") String absolutePath);

    @Query("DELETE FROM FileMetaData f WHERE CONCAT(f.path, '/', f.name) IN :absolutePaths AND f.user.name = :userName")
    List<FileMetaData> deleteByAbsolutePath(@Param("userName") String username,
                                            @Param("absolutePaths")List<String> absolutePaths);
//    FileMetaData getFileMetaDataFromDirectory(String file, Path directory);

//    boolean deleteFileMetaDataFromDirectory(String file, Path directory);

//    boolean deleteDirectory(Path directory);

//    void createDirectory(Path directoryPath);
}