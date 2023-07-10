package com.kobal.FileStorageApp.uploadservice;


import com.kobal.FileStorageApp.fileservice.FileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;

@Controller
public class UploadController {

    private final FileService fileService;

    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }

//    @PostMapping("upload")
    void uploadFile(Principal principal, @RequestParam("file") MultipartFile multipartFile, Path filepath) throws IOException {
        fileService.uploadFile(principal.getName(), filepath, multipartFile.getInputStream());
    }
}
