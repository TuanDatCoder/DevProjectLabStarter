package com.example.devprojectlabstarter.controller;


import com.example.devprojectlabstarter.service.ImageService;
import com.example.devprojectlabstarter.service.UrlShortenerService;
import com.example.devprojectlabstarter.utils.UploadFileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final UploadFileUtils uploadFileUtils;
    private final ImageService imageService;
    private final UrlShortenerService urlShortenerService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = uploadFileUtils.uploadFile(file);
            String url = uploadFileUtils.getSignedImageUrl(fileName);

            // Rút gọn URL
            String shortenedUrl = urlShortenerService.shortenUrl(url);

            // Lưu URL rút gọn vào cơ sở dữ liệu
            imageService.saveImage(shortenedUrl);

            return ResponseEntity.ok(shortenedUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred");
        }
    }
}