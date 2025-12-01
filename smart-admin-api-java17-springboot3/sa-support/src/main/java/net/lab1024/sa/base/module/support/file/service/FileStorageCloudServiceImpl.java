package net.lab1024.sa.base.module.support.file.service;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 文件云存储服务实现
 *
 * @Author 1024创新实验室
 * @Date 2019-09-02 23:21:10
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "cloud", matchIfMissing = false)
public class FileStorageCloudServiceImpl implements IFileStorageService {

    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    @Value("${file.storage.cloud.region}")
    private String region;

    @Value("${file.storage.cloud.endpoint}")
    private String endpoint;

    @Value("${file.storage.cloud.bucket-name}")
    private String bucketName;

    @Value("${file.storage.cloud.access-key}")
    private String accessKey;

    @Value("${file.storage.cloud.secret-key}")
    private String secretKey;

    @Value("${file.storage.cloud.url-prefix}")
    private String urlPrefix;

    @Resource
    private S3Client s3Client;

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFilename = file.getOriginalFilename();
        String fileKey = generateFileKey(originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("File uploaded successfully to cloud storage: {}", fileKey);
            return fileKey;
        } catch (Exception e) {
            log.error("Failed to upload file to cloud storage: " + fileKey, e);
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    @Override
    public String store(byte[] bytes, String fileName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("File bytes cannot be null or empty");
        }

        String fileKey = generateFileKey(fileName);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            log.info("File bytes uploaded successfully to cloud storage: {}", fileKey);
            return fileKey;
        } catch (Exception e) {
            log.error("Failed to upload file bytes to cloud storage: " + fileKey, e);
            throw new RuntimeException("Failed to upload file bytes to cloud storage", e);
        }
    }

    @Override
    public byte[] getBytes(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                return inputStream.readAllBytes();
            }
        } catch (Exception e) {
            log.error("Failed to get file bytes from cloud storage: {}", fileKey, e);
            throw new RuntimeException("Failed to get file bytes from cloud storage", e);
        }
    }

    @Override
    public String getUrl(String fileKey) {
        try {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            return s3Client.utilities().getUrl(getUrlRequest).toExternalForm();
        } catch (Exception e) {
            log.error("Failed to get URL for file: {}", fileKey, e);
            return null;
        }
    }

    @Override
    public boolean delete(String fileKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully from cloud storage: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete file from cloud storage: {}", fileKey, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if file exists: {}", fileKey, e);
            return false;
        }
    }

    /**
     * 生成文件閿悕
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
}
