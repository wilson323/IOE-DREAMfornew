package net.lab1024.sa.base.module.support.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2019-09-02 23:21:10
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Service
public class FileStorageLocalServiceImpl implements IFileStorageService {

    @Value("${file.storage.local.upload-path}")
    private String uploadPath;

    /**
     * 上传映射路径
     */
    public static final String UPLOAD_MAPPING = "/upload/**";

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        String fileKey = generateFileKey(originalFilename);
        String relativePath = generateRelativePath();
        String fullDirPath = uploadPath + "/" + relativePath;
        String fullFilePath = fullDirPath + "/" + fileKey;

        try {
            // 创建目录
            Path dirPath = Paths.get(fullDirPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 保存文件
            Path filePath = Paths.get(fullFilePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully to local storage: {}", relativePath + "/" + fileKey);
            return relativePath + "/" + fileKey;
        } catch (IOException e) {
            log.error("Failed to upload file to local storage: " + fileKey, e);
            throw new RuntimeException("Failed to upload file to local storage", e);
        }
    }

    @Override
    public String store(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("File bytes cannot be null or empty");
        }

        String fileKey = generateFileKey(fileName);
        String relativePath = generateRelativePath();
        String fullDirPath = uploadPath + "/" + relativePath;
        String fullFilePath = fullDirPath + "/" + fileKey;

        try {
            // 创建目录
            Path dirPath = Paths.get(fullDirPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 保存文件
            Path filePath = Paths.get(fullFilePath);
            Files.write(filePath, bytes);

            log.info("File bytes uploaded successfully to local storage: {}", relativePath + "/" + fileKey);
            return relativePath + "/" + fileKey;
        } catch (IOException e) {
            log.error("Failed to upload file bytes to local storage: " + fileKey, e);
            throw new RuntimeException("Failed to upload file bytes to local storage", e);
        }
    }

    @Override
    public byte[] getBytes(String fileKey) {
        String fullFilePath = uploadPath + "/" + fileKey;
        Path filePath = Paths.get(fullFilePath);

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found: " + fileKey);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Failed to get file bytes from local storage: " + fileKey, e);
            throw new RuntimeException("Failed to get file bytes from local storage", e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        // 本地文件无法直接通过URL访问，返回null或使用特定的映射URL
        return null;
    }

    @Override
    public boolean delete(String fileKey) {
        String fullFilePath = uploadPath + "/" + fileKey;
        Path filePath = Paths.get(fullFilePath);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted successfully from local storage: {}", fileKey);
            } else {
                log.warn("File not found for deletion: {}", fileKey);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: " + fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        String fullFilePath = uploadPath + "/" + fileKey;
        Path filePath = Paths.get(fullFilePath);
        return Files.exists(filePath);
    }

    /**
     * 生成文件键名
     */
    private String generateFileKey(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return timestamp + "_" + uuid + extension;
    }

    /**
     * 生成相对路径（按年月日分层）
     */
    private String generateRelativePath() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return now.format(formatter);
    }
}