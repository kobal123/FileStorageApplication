package com.kobal.FileStorageApp.downloadservice;

import com.kobal.FileStorageApp.fileservice.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;


@RestController
public class DownloadController {
    private final int BYTES_TO_READ = 2048;
    private FileService fileService;

    public DownloadController(FileService fileService) {
        this.fileService = fileService;
    }


//    @GetMapping("/download")
//    ResponseEntity<StreamingResponseBody> download(Principal p, String path) {
//
//
//
//        boolean isFileValid = fileService.validate(path);
//
//        StreamingResponseBody streamingResponseBody = outputStream -> {
//            Files.copy(Path.of(path), outputStream);
//        };
//
//        return ResponseEntity
//                .ok()
//                .h
//    }

}
