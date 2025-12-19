package net.lab1024.sa.common.storage.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import net.lab1024.sa.common.storage.config.FileCleanupProperties;
import net.lab1024.sa.common.storage.config.FileStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 本地文件系统存储实现
 * <p>
 * 适用场景:
 * - 单机部署
 * - 开发测试环境
 * - 小规模应用(<1000张照片/天)
 * </p>
 * <p>
 * 优点:
 * - 简单快速,无需额外服务
 * - 内存占用极小
 * - 无网络开销
 * </p>
 * <p>
 * 缺点:
 * - 不支持分布式
 * - 无法水平扩展
 * - 需要定期清理
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageImpl implements FileStorageStrategy {

    @Resource
    private FileStorageProperties storageProperties;

    /**
     * 初始化时创建必要的目录结构
     */
    @PostConstruct
    public void init() {
        try {
            // 创建根目录
            Path rootPath = Paths.get(storageProperties.getLocal().getBasePath());
            Files.createDirectories(rootPath);

            log.info("[本地存储] 初始化成功, basePath={}", rootPath);
        } catch (Exception e) {
            log.error("[本地存储] 初始化失败", e);
        }
    }

    /**
     * 定时清理过期文件
     */
    @Scheduled(cron = "${file.storage.local.cleanup.schedule:0 3 * * *}")
    public void cleanupExpiredFiles() {
        if (!storageProperties.getLocal().getCleanup().getEnabled()) {
            return;
        }

        log.info("[本地存储] 开始执行文件清理任务");
        int totalDeleted = 0;

        for (var rule : storageProperties.getLocal().getCleanup().getRules()) {
            try {
                int deleted = cleanupByRule(rule);
                totalDeleted += deleted;
                log.info("[本地存储] 清理规则: path={}, retentionDays={}, deleted={}",
                        rule.getPath(), rule.getRetentionDays(), deleted);
            } catch (Exception e) {
                log.error("[本地存储] 清理规则执行失败: {}", rule.getPath(), e);
            }
        }

        log.info("[本地存储] 文件清理任务完成, 总共删除{}个文件", totalDeleted);
    }

    /**
     * 根据规则清理文件
     */
    private int cleanupByRule(FileCleanupProperties.CleanupRule rule) {
        Path targetPath = Paths.get(storageProperties.getLocal().getBasePath(), rule.getPath());
        if (!Files.exists(targetPath)) {
            return 0;
        }

        long cutoffTime = System.currentTimeMillis() -
                          (rule.getRetentionDays() * 24L * 60 * 60 * 1000);
        int[] deletedCount = {0};

        try {
            Files.walk(targetPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        long lastModified = Files.getLastModifiedTime(file).toMillis();
                        if (lastModified < cutoffTime) {
                            Files.delete(file);
                            deletedCount[0]++;
                        }
                    } catch (Exception e) {
                        log.warn("[本地存储] 删除文件失败: {}", file, e);
                    }
                });
        } catch (Exception e) {
            log.error("[本地存储] 遍历目录失败: {}", targetPath, e);
        }

        return deletedCount[0];
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 1. 创建目录
            Path folderPath = Paths.get(storageProperties.getLocal().getBasePath(), folder);
            Files.createDirectories(folderPath);

            // 2. 生成文件名
            String fileName = generateFileName(file.getOriginalFilename());
            Path filePath = folderPath.resolve(fileName);

            // 3. 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[本地存储] 文件上传成功: {}, size={}KB",
                    filePath, file.getSize() / 1024);

            // 4. 返回URL
            return getFileUrl(folder + "/" + fileName);

        } catch (Exception e) {
            log.error("[本地存储] 文件上传失败: folder={}", folder, e);
            throw new BusinessException("FILE_UPLOAD_ERROR", "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadStream(InputStream inputStream, String fileName,
                               String contentType, long size, String folder) {
        try {
            Path folderPath = Paths.get(storageProperties.getLocal().getBasePath(), folder);
            Files.createDirectories(folderPath);

            String newFileName = generateFileName(fileName);
            Path filePath = folderPath.resolve(newFileName);

            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("[本地存储] 流上传成功: {}, size={}KB", filePath, size / 1024);
            return getFileUrl(folder + "/" + newFileName);

        } catch (Exception e) {
            log.error("[本地存储] 流上传失败: folder={}", folder, e);
            throw new BusinessException("STREAM_UPLOAD_ERROR", "流上传失败");
        }
    }

    @Override
    public Map<String, String> getPresignedUploadUrl(String folder, String fileName, int expirySeconds) {
        // 本地存储不支持预签名,返回空Map
        log.warn("[本地存储] 不支持预签名URL,请使用uploadFile方法");
        Map<String, String> result = new HashMap<>();
        result.put("message", "本地存储不支持预签名URL,请使用POST /upload接口");
        return result;
    }

    @Override
    public String getFileUrl(String filePath) {
        return storageProperties.getLocal().getUrlPrefix() + "/" + filePath;
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
            Files.deleteIfExists(path);
            log.info("[本地存储] 文件删除成功: {}", filePath);
            return true;
        } catch (Exception e) {
            log.error("[本地存储] 文件删除失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        Path path = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
        return Files.exists(path);
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString() + extension;
    }
}
