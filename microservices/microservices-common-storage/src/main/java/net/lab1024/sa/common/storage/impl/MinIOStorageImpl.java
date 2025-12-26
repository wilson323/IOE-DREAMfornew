package net.lab1024.sa.common.storage.impl;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import net.lab1024.sa.common.storage.config.FileStorageProperties;

/**
 * MinIO对象存储实现
 * <p>
 * 提供MinIO对象存储的完整实现，支持：
 * - 文件上传和下载
 * - 文件删除和存在性检查
 * - 预签名URL生成
 * - 文件信息查询
 * - 自动bucket管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Component
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "minio")
@Slf4j
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
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("[MinIO存储] 创建bucket成功: {}", bucketName);
            } else {
                log.info("[MinIO存储] bucket已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("[MinIO存储] 初始化失败", e);
            throw new RuntimeException("MinIO存储初始化失败", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String filePath) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("BUSINESS_ERROR", "文件不能为空");
            }

            String bucketName = storageProperties.getMinio().getBucketName();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info("[MinIO存储] 文件存储成功: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("[MinIO存储] 文件存储失败: {}", filePath, e);
            throw new BusinessException("STORAGE_ERROR", "文件存储失败", e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());
        } catch (Exception e) {
            log.error("[MinIO存储] 获取文件流失败: {}", filePath, e);
            throw new BusinessException("STORAGE_ERROR", "获取文件流失败", e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());

            log.info("[MinIO存储] 文件删除成功: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("[MinIO存储] 文件删除失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(filePath).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getFileInfo(String filePath) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build());

            Map<String, Object> fileInfo = new HashMap<>();
            String fileName = filePath.contains("/") ? filePath.substring(filePath.lastIndexOf("/") + 1) : filePath;
            fileInfo.put("fileName", fileName);
            fileInfo.put("filePath", filePath);
            fileInfo.put("fileSize", statObjectResponse.size());
            fileInfo.put("lastModifiedTime", statObjectResponse.lastModified().toInstant().toEpochMilli());
            fileInfo.put("contentType", statObjectResponse.contentType());
            fileInfo.put("etag", statObjectResponse.etag());
            fileInfo.put("bucket", bucketName);

            return fileInfo;
        } catch (Exception e) {
            log.error("[MinIO存储] 获取文件信息失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 生成文件访问URL
     */
    public String getFileUrl(String filePath, int expireSeconds) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filePath)
                            .expiry(expireSeconds, TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            log.error("[MinIO存储] 生成文件URL失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 获取预签名上传URL
     * <p>
     * 生成用于直接上传文件的预签名URL
     * </p>
     *
     * @param folder        文件夹路径（如 "access/snapshots"）
     * @param fileName      文件名（如 "snapshot.jpg"）
     * @param expireSeconds 过期秒数
     * @return 预签名URL信息Map
     */
    @Override
    public Map<String, String> getPresignedUploadUrl(String folder, String fileName, int expireSeconds) {
        try {
            String bucketName = storageProperties.getMinio().getBucketName();

            // 构建完整文件路径
            String filePath = folder.endsWith("/") ? folder + fileName : folder + "/" + fileName;

            // 生成预签名上传URL（使用PUT方法）
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucketName)
                            .object(filePath)
                            .expiry(expireSeconds, TimeUnit.SECONDS)
                            .build());

            Map<String, String> result = new HashMap<>();
            result.put("uploadUrl", uploadUrl);
            result.put("method", "PUT");
            result.put("expirySeconds", String.valueOf(expireSeconds));
            result.put("filePath", filePath);

            log.info("[MinIO存储] 生成预签名上传URL成功: filePath={}, expireSeconds={}", filePath, expireSeconds);
            return result;
        } catch (Exception e) {
            log.error("[MinIO存储] 生成预签名上传URL失败: folder={}, fileName={}", folder, fileName, e);
            throw new BusinessException("STORAGE_ERROR", "生成预签名上传URL失败", e);
        }
    }

}

