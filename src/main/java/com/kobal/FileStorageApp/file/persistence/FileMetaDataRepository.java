package com.kobal.FileStorageApp.file.persistence;


import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {

    @Query("SELECT f FROM FileMetaData f WHERE f.user.id = :userId AND f.name = :fileName " +
            "AND f.isDirectory = :isDirectory")
    List<FileMetaData> findByUserIdAndNameContaining(@Param("userId") Long userId,
                                                     @Param("fileName") String name,
                                                     Pageable pageable,
                                                     @Param("isDirectory") Boolean directoryOnly);

    @Query("SELECT f FROM FileMetaData f WHERE f.parent = :file_parent AND f.user.id = :userId")
    List<FileMetaData> getSubDirectoriesByParentAndUserId(@Param("file_parent") FileMetaData parent,
                                                          @Param("userId") Long userId);

    Optional<FileMetaData> getFileMetaDataByUserIdAndPath(Long userId, String path);

    //    @Query("""
//            SELECT f FROM FileMetaData f
//             WHERE CONCAT_WS('/', f.path, f.name) = CONCAT('/', :absolutePath)
//            AND f.user.id = :userId
//            """)
    @Query("""
            SELECT f FROM FileMetaData f
            WHERE f.path = :path
            AND f.name = :fileName
            AND f.user.id = :userId
            """)
    Optional<FileMetaData> findByUserIdAndPathAndName(@Param("userId") Long userId,
                                                      @Param("path") String path,
                                                      @Param("fileName") String name);

    @Query("""
                SELECT f FROM FileMetaData f
                WHERE f.path = :file_path and f.name IN :file_names AND f.user.id = :userId
            """)
    List<FileMetaData> findByUserIdAndPathAndNames(@Param("userId") Long userId,
                                                   @Param("file_path") String filePath,
                                                   @Param("file_names") List<String> fileNames);


    @Modifying
    @Query("UPDATE FileMetaData f SET f.path = :newFilePath WHERE f.user.id = :userId AND f.id IN :fileIds")
    void updateFilePathsByIdAndUserId(@Param("userId") Long userId,
                                      @Param("fileIds") List<Long> fileIds,
                                      @Param("newFilePath") String path);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE FileMetaData f SET f.path = CONCAT(:newFilePath, SUBSTRING(f.path, LENGTH(:oldFilePath) + 1, 1000))
            WHERE f.user.id = :userId AND f.path LIKE CONCAT(:oldFilePath,'%')
           """)
    void updateFilePath(@Param("userId") Long userId,
                        @Param("oldFilePath") String oldFilePath,
                        @Param("newFilePath") String newFilePath);


    @Query("SELECT f FROM FileMetaData f WHERE CONCAT_WS('/', f.path, f.name) IN :absolutePaths AND f.user.id = :userID")
    List<FileMetaData> getFilesByUserIdAndPaths(@Param("userID") Long id,
                                                @Param("absolutePaths") List<String> absolutePaths);

    List<FileMetaData> getAllFilesByUserId(Long id);
}