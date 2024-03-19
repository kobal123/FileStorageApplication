package com.kobal.FileStorageApp.file.filecontroller;

import com.kobal.FileStorageApp.file.model.filemetadata.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.file.service.BatchOperationResult;
import com.kobal.FileStorageApp.file.service.FilePath;
import com.kobal.FileStorageApp.file.service.FileService;
import com.kobal.FileStorageApp.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@CrossOrigin(origins = "http://localhost:3000")
public class RestFileController {
    private final FileService fileService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(RestFileController.class);

    public RestFileController(FileService fileService, UserService userService) {
        this.fileService = fileService;
        this.userService = userService;
    }

    @GetMapping(value = "/list_directory", params = {"path"})
        List<FileMetaDataDTO> listDirectory(JwtAuthenticationToken token, @RequestParam("path") String path){
        Long userId = userService.getUserIdFromJwt(token.getToken());
        FilePath filePath = new FilePath().addPartEncoded(path);
        System.out.println("FIle path " + filePath);
        return fileService.getFilesInDirectory(userId, filePath);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/create_directory")
    void createDirectory(JwtAuthenticationToken token,
                                          @RequestParam("path") String path){
        Long userId = userService.getUserIdFromJwt(token.getToken());
        FilePath filePath = new FilePath().addPartRaw(path);
        fileService.createDirectory(userId, filePath);
    }

    @GetMapping(value = "/search", params = {"q", "directory-only"})
    List<FileMetaDataDTO> searchFiles(JwtAuthenticationToken token,
                                      @RequestParam("q") String filename,
                                      @RequestParam(
                                              value = "directory-only",
                                              required = false,
                                              defaultValue = "false") Boolean directoryOnly) {
        Long userId = userService.getUserIdFromJwt(token.getToken());
        return fileService.searchFiles(userId, filename, directoryOnly);
    }


    @PostMapping("/upload")
     ResponseEntity<String> uploadFile(JwtAuthenticationToken token,
                      @RequestParam("file") MultipartFile multipartFile,
                      @RequestParam("directoryPath") String directoryPath) {
        Long userId = userService.getUserIdFromJwt(token.getToken());
        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null) {
            throw new UserFileBadRequestException("File name cannot be empty");
        }

        FilePath filePath = new FilePath(directoryPath);
        FileMetaDataDTO metaDataDTO = fileService.uploadFile(userId, filePath, multipartFile);

        return new ResponseEntity<>(metaDataDTO.getAbsolutePath(), HttpStatus.CREATED);
    }
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    BatchOperationResult deleteFiles(JwtAuthenticationToken token,
                                     @RequestBody List<String> files) {
        Long userId = userService.getUserIdFromJwt(token.getToken());
        List<FilePath> filePath = files.stream().map(FilePath::raw).toList();
        return fileService.deleteFilesInDirectory(userId, filePath);
    }

    @PostMapping(value = "/move", produces = MediaType.APPLICATION_JSON_VALUE)
    BatchOperationResult move(JwtAuthenticationToken token,
                               @RequestBody List<String> filePaths,
                               @RequestBody String targetDirectory) {
        Long userId = userService.getUserIdFromJwt(token.getToken());
//        FilePath sourceDirectoryPath = new FilePath(sourceDirectory);
        List<FilePath> paths = filePaths.stream().map(FilePath::raw).toList();
        FilePath targetDirectoryPath = new FilePath(targetDirectory);
        return fileService.moveFilesToDirectory(userId, targetDirectoryPath, paths);
    }


    @PutMapping(value = "/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    BatchOperationResult copy(JwtAuthenticationToken token,
                               @RequestBody List<String> filePaths,
                               @RequestBody String targetDirectory) {

        Long userId = userService.getUserIdFromJwt(token.getToken());
//        FilePath sourceDirectoryPath = new FilePath(sourceDirectory);
        FilePath targetDirectoryPath = new FilePath(targetDirectory);
        List<FilePath> paths = filePaths.stream().map(FilePath::raw).toList();

        return fileService.copyFilesToDirectory(userId, paths, targetDirectoryPath);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    void rename(JwtAuthenticationToken token,
                               @RequestParam("path") String absoluteFilePath,
                               @RequestParam("newName") String newName) {

        Long userId = userService.getUserIdFromJwt(token.getToken());
        FilePath absPath = FilePath.raw(absoluteFilePath);
        fileService.rename(userId, absPath, newName);
    }
    
    @GetMapping(value = "/download", params = {"path"})
    ResponseEntity<StreamingResponseBody> download(JwtAuthenticationToken token,
                                                   @RequestParam String path) {
        Long userId = userService.getUserIdFromJwt(token.getToken());
        FilePath filePath = new FilePath(path);
        FileMetaDataDTO metaData= fileService
                .getFileMetaDataByPath(userId, filePath)
                .orElseThrow(() -> new UserFileNotFoundException("Could not find file with path %s".formatted(filePath)));



        StreamingResponseBody streamingResponseBody = outputStream -> {
            try(InputStream inputStream = fileService.download(userId, filePath); outputStream) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                logger.error("Failed to download file. %s".formatted(e));
                throw new UserFileException("There was an error downloading the requested file with path %s".formatted(filePath));
            }
        };
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + metaData.getName());
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(metaData.getSize()));
        httpHeaders.add(HttpHeaders.LAST_MODIFIED, String.valueOf(metaData.getModified()));
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(streamingResponseBody);
    }
}

