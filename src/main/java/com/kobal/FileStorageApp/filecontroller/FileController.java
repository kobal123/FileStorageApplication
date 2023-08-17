package com.kobal.FileStorageApp.filecontroller;


import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
import com.kobal.FileStorageApp.exceptions.UserFileException;
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    String uploadFile(Principal principal, @RequestParam("file") MultipartFile multipartFiles, HttpServletRequest request, Model model) {
        Path path = getPath(request);
        List<FileMetaData> addedFiles = new ArrayList<>();
        String fileName = multipartFiles.getOriginalFilename();

        if (fileName == null) {
            throw new UserFileBadRequestException("Bad file name");
        }

        Path filePath = path.resolve(fileName);

        try {
            fileService.uploadFile(principal.getName(), filePath, multipartFiles.getInputStream());
            addedFiles.add(new FileMetaData(fileName, multipartFiles.getSize(), Instant.now().toEpochMilli(), false));

        } catch (IOException exception) {
            throw new UserFileException("Error uploading file");
        }

        model.addAttribute("files", addedFiles);
        return "fragments/file-table-row :: table-row";
    }
    @DeleteMapping(value = "/delete/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> deleteFiles(Principal principal, @RequestBody List<String> fileNames, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();

        Path path = getPath(request);
        List<String> failedDeletions = fileService.deleteFilesInDirectory(principal.getName(), path, fileNames);
        return failedDeletions;
    }

    @PostMapping(value = "/move/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> move(Principal principal, @RequestBody List<String> fileNames, @RequestBody Path targetDirectory, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();

        Path path = getPath(request);
        List<String> failedMove = fileService.moveFilesToDirectory(principal.getName(), path, targetDirectory, fileNames);
        return failedMove;
    }


    @PutMapping(value = "/copy/**", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> copy(Principal principal, @RequestBody List<String> fileNames, @RequestBody Path targetDirectory, HttpServletRequest request) {
        if (fileNames.isEmpty())
            return Collections.emptyList();

        Path path = getPath(request);
        List<String> failedCopy = fileService.copyFilesToDirectory(principal.getName(), path, targetDirectory, fileNames);
        return failedCopy;
    }

    @GetMapping("/folder/**")
    String folderTable(Principal principal, Model model, HttpServletRequest request) throws URISyntaxException {
        Path path = getPath(request);
        List<FileMetaData> files = fileService.getFilesinDirectory(principal.getName(), path)
                .stream()
                .map(file -> new FileMetaData(file.getName(), file.length(), file.lastModified(), file.isDirectory()))
                .toList();
        URI folder = new URIBuilder()
                .appendPath("folder")
                .appendPath(path.toString())
                .build();
        model.addAttribute("folderURL", folder);
        model.addAttribute("files", files);
        return "fragments/file-table :: file-table";
    }

    @GetMapping("/home/**")
    String index(Principal principal, Model model, HttpServletRequest request) throws URISyntaxException{
        System.out.println(Thread.currentThread().getClass());

        Path path = getPath(request);
        List<FileMetaData> files = fileService.getFilesinDirectory(principal.getName(), path)
                .stream()
                .map(file -> new FileMetaData(file.getName(), file.length(), file.lastModified(), file.isDirectory()))
                .toList();
        model.addAttribute("files", files);
        URI folder = new URIBuilder()
                .appendPath("folder")
                .appendPath(path.toString())
                .build();
        URI uploadURI = new URIBuilder()
                .appendPath("upload")
                .appendPath(path.toString())
                .build();
        model.addAttribute("folderURL", folder);
        model.addAttribute("uploadURL", uploadURI);
        return "index";
    }

    @GetMapping("/download/**")
    ResponseEntity<StreamingResponseBody> download(Principal principal, HttpServletRequest request) {

        Path path = getPath(request);
        Optional<File> optionalFile = fileService.getFile(principal.getName(), path);

        if (optionalFile.isEmpty()) {
            throw new UserFileException("Failed to download file.");
        }

        File file = optionalFile.get();
        StreamingResponseBody streamingResponseBody = outputStream -> Files.copy(file.toPath(), outputStream);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        httpHeaders.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
        httpHeaders.add(HttpHeaders.LAST_MODIFIED, String.valueOf(file.lastModified()));
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .body(streamingResponseBody);
    }


    public Path getPath(HttpServletRequest request) {
        String restOfTheUrl = new AntPathMatcher()
                .extractPathWithinPattern(
                        request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString(),
                        request.getRequestURI()
                );
        return Path.of(restOfTheUrl);
    }
}

