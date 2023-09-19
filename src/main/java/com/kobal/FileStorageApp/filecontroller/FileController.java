package com.kobal.FileStorageApp.filecontroller;


import com.kobal.FileStorageApp.FileMetaDataDTO;
import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.fileservice.FilePath;
import com.kobal.FileStorageApp.fileservice.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @GetMapping("")
    String redirectToHome() {
        return "redirect:/home";
    }

    @PostMapping("/upload/**")
    String uploadFile(Principal principal, @RequestParam("file") MultipartFile multipartFile, HttpServletRequest request, Model model) {
        if (multipartFile.isEmpty())
            throw new UserFileException("File cannot be empty");

        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null)
            throw new UserFileBadRequestException("File name cannot be empty");


        FilePath filePath = getFilePathFromRequest(request);
        FileMetaDataDTO metaDataDTO =  fileService.uploadFile(principal, filePath, multipartFile);

        List<FileMetaDataDTO> addedFiles = new ArrayList<>();
        addedFiles.add(metaDataDTO);
        model.addAttribute("files", addedFiles);
        return "fragments/file-table-row :: table-row";
    }
    @DeleteMapping(value = "/delete/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> deleteFiles(Principal principal, @RequestBody List<String> fileNames, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();

        FilePath filePath = getFilePathFromRequest(request);
        List<String> failedDeletions = fileService.deleteFilesInDirectory(principal, filePath, fileNames);
        return failedDeletions;
    }

    @PostMapping(value = "/move/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> move(Principal principal, @RequestBody List<String> fileNames, @RequestBody String targetDirectory, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();


        FilePath sourceDirectoryPath = getFilePathFromRequest(request);
        FilePath targetDirectoryPath = new FilePath(targetDirectory);

        List<String> failedMove = fileService.moveFilesToDirectory(principal, sourceDirectoryPath, targetDirectoryPath, fileNames);
        return failedMove;
    }


    @PutMapping(value = "/copy/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> copy(Principal principal, @RequestBody List<String> fileNames, @RequestBody String targetDirectory, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();

        FilePath sourceDirectoryPath = getFilePathFromRequest(request);
        FilePath targetDirectoryPath = new FilePath(targetDirectory);

        List<String> failedCopy = fileService.copyFilesToDirectory(principal, sourceDirectoryPath, targetDirectoryPath, fileNames);
        return failedCopy;
    }

    @GetMapping("/home/**")
    String index(Principal principal, Model model, HttpServletRequest request) throws URISyntaxException{
        FilePath filePath = getFilePathFromRequest(request);
        List<FileMetaDataDTO> files = fileService.getFilesInDirectory(principal, filePath);

        model.addAttribute("files", files);
        URI folder = new URIBuilder()
                .appendPath("home")
                .appendPath(filePath.toString())
                .build();
        URI uploadURI = new URIBuilder()
                .appendPath("upload")
                .appendPath(filePath.toString())
                .build();
        model.addAttribute("folderURL", folder);
        model.addAttribute("uploadURL", uploadURI);

        String header = request.getHeader("HX-Request");
        if (header != null) { //request came from frontend htmx event
            return "fragments/file-table :: file-table";
        }
        return "index";

    }

    @GetMapping("/download/**")
    ResponseEntity<StreamingResponseBody> download(Principal principal, HttpServletRequest request) {

        FilePath filePath = getFilePathFromRequest(request);
        FileMetaDataDTO metaData= fileService
                .getFileMetaDataByPath(principal, filePath)
                .orElseThrow(() -> new UserFileNotFoundException("File was not found."));



        StreamingResponseBody streamingResponseBody = outputStream -> {
            try(InputStream inputStream = fileService.download(principal, filePath); outputStream) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                throw new UserFileException("There was an error downloading the requested file");
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


    public FilePath getFilePathFromRequest(HttpServletRequest request) {
        return FilePath.raw(
                new AntPathMatcher()
                .extractPathWithinPattern(
                        request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
                        request.getRequestURI()
                )
        );
    }
}

