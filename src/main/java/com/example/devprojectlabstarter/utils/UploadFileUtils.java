package com.example.devprojectlabstarter.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class UploadFileUtils {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    @Value("${fcm.credentials.file.path}")
    private String credentialsFilePath;

    private Storage storage;

    @PostConstruct
    private void initializeStorage() {
        try {
            storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(
                    new ClassPathResource(credentialsFilePath).getInputStream())).build().getService();
        } catch (IOException e) {
            throw new RuntimeException("Storage exception");
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + "_" + originalFileName;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        if (!fileExtension.equalsIgnoreCase("jpg") && !fileExtension.equalsIgnoreCase("jpeg")) {
            originalFileName = originalFileName.replaceAll("\\.jpeg$", ".jpg");
        }

        String fileName = generateUniqueFileName(originalFileName);
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        try (WriteChannel writer = storage.writer(blobInfo);
             InputStream resizedImageStream = resizeImage(file.getInputStream())) {
            byte[] buffer = new byte[10 * 1024 * 1024]; // 10 MB buffer
            int limit;
            long totalBytes = resizedImageStream.available();
            long uploadedBytes = 0;

            while ((limit = resizedImageStream.read(buffer)) >= 0) {
                writer.write(ByteBuffer.wrap(buffer, 0, limit));
                uploadedBytes += limit;
                double progress = (double) uploadedBytes / totalBytes * 100;
                log.info("Progress: " + progress);
            }
            return fileName;
        } catch (StorageException e) {
            throw new IOException(e);
        }
    }

    public String getSignedImageUrl(String baseImageUrl) throws IOException {
        URL signedUrl = storage.signUrl(
                BlobInfo.newBuilder(BlobId.of(bucketName, baseImageUrl)).build(),
                15, TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature());
        return signedUrl.toString();
    }

    private InputStream resizeImage(InputStream inputStream) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > 800 || originalHeight > 800) { // Chỉnh sửa kích thước tối đa tại đây
            if (originalWidth > originalHeight) {
                newWidth = 800;
                newHeight = (newWidth * originalHeight) / originalWidth;
            } else {
                newHeight = 800;
                newWidth = (newHeight * originalWidth) / originalHeight;
            }
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}
