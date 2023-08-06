package com.kobal.FileStorageApp.filecontroller;


import com.kobal.FileStorageApp.FileMetaData;
import com.kobal.FileStorageApp.exceptions.UserFileBadRequestException;
import com.kobal.FileStorageApp.exceptions.UserFileException;
import com.kobal.FileStorageApp.fileservice.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
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
    @DeleteMapping("/delete-files/**")
    List<String> deleteFiles(Principal principal, @RequestBody List<String> fileNames, HttpServletRequest request) {
        Path path = getPath(request);
        fileService.deleteFilesInDirectory(principal.getName(), path, fileNames);
        return Collections.emptyList();
    }


    @GetMapping("/folder/**")
    String folderTable(Principal principal, Model model, HttpServletRequest request) {
        Path path = getPath(request);

        List<FileMetaData> files = fileService.getFilesinDirectory(principal.getName(), path)
                .stream()
                .map(file -> new FileMetaData(file.getName(), file.length(), file.lastModified(), file.isDirectory()))
                .toList();

        model.addAttribute("folderURL", Path.of("folder").resolve(path));
        model.addAttribute("files", files);
        return "fragments/file-table :: file-table";
    }

    @GetMapping("/home/**")
    String index(Principal principal, Model model, HttpServletRequest request) {
        Path path = getPath(request);

        List<FileMetaData> files = fileService.getFilesinDirectory(principal.getName(), path)
                .stream()
                .map(file -> new FileMetaData(file.getName(), file.length(), file.lastModified(), file.isDirectory()))
                .toList();

        model.addAttribute("files", files);
        model.addAttribute("folderURL", Path.of("folder").resolve(path));

        model.addAttribute("uploadURL", Path.of("upload").resolve(path));
        return "index";
    }


    private String getUrlFromRequest(final HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    private String[] extractWildcardParts(String url) {
        return url.split("/");
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
        String url = getUrlFromRequest(request);
        String[] parts = extractWildcardParts(url);
        Path path = Path.of("");

        // if length is 2, we hit the /home endpoint
        if (parts.length  != 2) {
            for (int i = 2; i < parts.length; i++) {
                path = path.resolve(parts[i]);
            }
        }
        return path;
    }

}

