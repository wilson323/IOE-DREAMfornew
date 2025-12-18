package net.lab1024.sa.common.storage.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import net.lab1024.sa.common.storage.config.FileStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MinIO对象存储实现
 *
 * 适用场景:
 * - 大型企业 (>20000人)
 * - 多园区部署
 * - 日增文件 >200GB/天
 * - 需要分布式存储
 *
 * 优势:
 * - 支持分布式,可水平扩展
 * - S3兼容,生态丰富
 * - 支持预签名URL,设备直传
 * - 对象生命周期管理
 * - 支持大文件分块上传
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "minio")
public class MinIOStorageImpl implements FileStorageStrategy {

    @Resource
    private FileStorageProperties storageProperties;

    @Resource
    private MinioClient minioClient;

    /**
     * 初始化时检查并创建bucket
     */
    @PostConstruct
    public void init() {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("[MinIO存储] 创建bucket成功: {}", bucketName);
            }

            log.info("[MinIO存储] 初始化成功, endpoint={}, bucket={}",
                    storageProperties.getMinio().getEndpoint(), bucketName);
        } catch (Exception e) {
            log.error("[MinIO存储] 初始化失败", e);
            throw new BusinessException("MINIO_INIT_ERROR", "MinIO初始化失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 1. 生成对象路径
            String objectName = buildObjectName(folder, file.getOriginalFilename());

            // 2. 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            log.info("[MinIO存储] 文件上传成功: {}, size={}KB",
                    objectName, file.getSize() / 1024);

            // 3. 返回访问URL
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("[MinIO存储] 文件上传失败: folder={}", folder, e);
            throw new BusinessException("FILE_UPLOAD_ERROR", "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadStream(InputStream inputStream, String fileName,
                               String contentType, long size, String folder) {
        try {
            // 1. 生成对象路径
            String objectName = buildObjectName(folder, fileName);

            // 2. 上传流
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build()
            );

            log.info("[MinIO存储] 流上传成功: {}, size={}KB",
                    objectName, size / 1024);

            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("[MinIO存储] 流上传失败: folder={}", folder, e);
            throw new BusinessException("STREAM_UPLOAD_ERROR", "流上传失败: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public Map<String, String> getPresignedUploadUrl(String folder, String fileName, int expirySeconds) {
        try {
            // 1. 生成对象路径
            String objectName = buildObjectName(folder, fileName);

            // 2. 生成预签名上传URL
            String uploadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .expiry(expirySeconds)
                    .build()
            );

            Map<String, String> result = new HashMap<>();
            result.put("uploadUrl", uploadUrl);
            result.put("objectName", objectName);
            result.put("method", "PUT");
            result.put("expirySeconds", String.valueOf(expirySeconds));

            log.info("[MinIO存储] 生成预签名上传URL: {}, expiry={}s", objectName, expirySeconds);

            return result;
        } catch (Exception e) {
            log.error("[MinIO存储] 生成预签名URL失败: folder={}, fileName={}", folder, fileName, e);
            throw new BusinessException("PRESIGNED_URL_ERROR", "生成预签名URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            // 生成预签名下载URL (默认7天有效期)
            int expiry = storageProperties.getMinio().getUrlExpiry();

            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .expiry(expiry)
                    .build()
            );
        } catch (Exception e) {
            log.error("[MinIO存储] 获取文件URL失败: {}", objectName, e);
            throw new BusinessException("GET_URL_ERROR", "获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .build()
            );

            log.info("[MinIO存储] 文件删除成功: {}", objectName);
            return true;
        } catch (Exception e) {
            log.error("[MinIO存储] 文件删除失败: {}", objectName, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(storageProperties.getMinio().getBucketName())
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getStorageType() {
        return "minio";
    }

    /**
     * 构建对象名称
     * 格式: folder/yyyy/MM/dd/uuid_filename
     */
    private String buildObjectName(String folder, String originalFilename) {
        // 1. 获取当前日期
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 2. 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = uuid + extension;

        // 3. 组合完整路径
        return String.format("%s/%s/%s", folder, datePath, fileName);
    }
}
