package com.kobal.FileStorageApp.filecontroller;

import com.kobal.FileStorageApp.file.service.FilePath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class FileControllerTest {
    @Autowired
    MockMvc mockMvc;

    private final String UPLOAD = "/upload";
    private final String DELETE = "/delete";
    private final String DOWNLOAD = "/download";
    private final String MOVE = "/move";
    private final String COPY = "/copy";
    @Test
    void uploadFileShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post(UPLOAD)).andExpect(status().isForbidden());
    }

    @Test
    void deleteFilesShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(delete(UPLOAD)).andExpect(status().isForbidden());
    }

    @Test
    void moveShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(delete(MOVE)).andExpect(status().isForbidden());
    }

    @Test
    void copyShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(delete(COPY)).andExpect(status().isForbidden());
    }

    @Test
    void indexShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadShouldReturn403WhenUserIsNotAuthenticated() throws Exception {
        // given
        String path = new FilePath()
                .addPartRaw(DOWNLOAD)
                .addPartRaw("aFolder123")
                .toString();

        mockMvc.perform(get(path))
                .andExpect(status().isForbidden());
    }
}