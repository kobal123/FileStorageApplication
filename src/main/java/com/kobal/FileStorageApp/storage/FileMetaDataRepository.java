package com.kobal.FileStorageApp.storage;



import com.kobal.FileStorageApp.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {

    List<FileMetaData> getChildrenByUserIdAndParentPath(Long userId, String path);

    @Query("SELECT f FROM FileMetaData f WHERE f.path = :file_path AND f.user.id = :userId")
    List<FileMetaData> getSubFoldersByUserIdAndPath(@Param("userId") Long userId,
                                                    @Param("file_path") String path);

    Optional<FileMetaData> getFileMetaDataByUserIdAndPath(Long userId, String path);

    Optional<FileMetaData> getFileMetaDataByUserIdAndName(Long userId, String name);

    @Query("SELECT f FROM FileMetaData f WHERE CONCAT(f.path, '/', f.name) = :absolutePath AND f.user.id = :userId")
    Optional<FileMetaData> findByAbsolutePath(@Param("userId") Long userId,
                                              @Param("absolutePath") String absolutePath);

    @Query("DELETE FROM FileMetaData f WHERE CONCAT(f.path, '/', f.name) IN :absolutePaths AND f.user.id = :userId")
    List<FileMetaData> deleteByAbsolutePath(@Param("userId") Long userId,
                                            @Param("absolutePaths")List<String> absolutePaths);

    @Query("select f from FileMetaData f where CONCAT(f.path, '/', f.name) IN :absolutePaths AND f.user.id = :userID")
    List<FileMetaData> getFilesByUserIdAndPathStartingWith(@Param("userID") Long id,
                                                           @Param("absolutePaths")List<String> absolutePaths);

}