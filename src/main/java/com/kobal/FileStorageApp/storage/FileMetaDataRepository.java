package com.kobal.FileStorageApp.storage;



import com.kobal.FileStorageApp.FileMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {


    @Query("SELECT f FROM FileMetaData f WHERE f.path = :file_path AND f.user.id = :userId")
    List<FileMetaData> getSubFoldersByUserIdAndPath(@Param("userId") Long userId,
                                                    @Param("file_path") String path);

    Optional<FileMetaData> getFileMetaDataByUserIdAndPath(Long userId, String path);


    @Query("""
            SELECT f FROM FileMetaData f
             WHERE CONCAT_WS('/',f.path, f.name) = CONCAT('/', :absolutePath)
            AND f.user.id = :userId
            """)
    Optional<FileMetaData> findByUserIdAndAbsolutePath(@Param("userId") Long userId,
                                                       @Param("absolutePath") String absolutePath);

    @Query("""
        SELECT f FROM FileMetaData f
        WHERE f.path = :file_path and f.name IN :file_names AND f.user.id = :userId
    """)
    List<FileMetaData> findByUserIdAndPathAndNames(@Param("userId") Long userId,
                                                       @Param("file_path")String filePath,
                                                       @Param("file_names") List<String> fileNames);


    @Modifying
    @Query("UPDATE FileMetaData f SET f.path = :newFilePath WHERE f.user.id = :userId AND f.id IN :fileIds")
    void updateFilePathsByIdAndUserId(@Param("userId") Long userId,
                                      @Param("fileIds") List<Long> fileIds,
                                      @Param("newFilePath") String path);

    @Query("SELECT f FROM FileMetaData f WHERE CONCAT_WS('/', f.path, f.name) IN :absolutePaths AND f.user.id = :userID")
    List<FileMetaData> getFilesByUserIdAndPathStartingWith(@Param("userID") Long id,
                                                           @Param("absolutePaths")List<String> absolutePaths);

}