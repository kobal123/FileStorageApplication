package com.kobal.FileStorageApp.filecontroller;


import com.kobal.FileStorageApp.exceptions.UserFileNotFoundException;
import com.kobal.FileStorageApp.fileservice.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

@Controller
@RequestMapping("/test")

public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
        System.out.println("CONSTURCTOR CALLED");
    }


    @PostMapping("upload")
    void uploadFile(Principal principal, @RequestParam("file") MultipartFile multipartFile, Path filepath) throws IOException {
        fileService.uploadFile(principal.getName(), filepath, multipartFile.getInputStream());
    }

    @GetMapping("/test")
    String asd(){
        System.out.println("CALLED");
        fileService.getFile("asd", Path.of("/kobal01"));
        return "Hello world";
    }

    @GetMapping("/download")
    ResponseEntity<StreamingResponseBody> download(Principal p, Path path) {
        File file = fileService.getFile(p.getName(), path).orElseThrow(UserFileNotFoundException::new);

        StreamingResponseBody streamingResponseBody = outputStream -> {
            Files.copy(file.toPath(), outputStream);
        };

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

}

