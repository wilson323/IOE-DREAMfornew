package net.lab1024.sa.common.storage.impl;

import lombok.extern.slf4j.Slf4j;


import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import net.lab1024.sa.common.storage.config.FileStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地文件存储实现
 * <p>
 * 提供本地文件系统的存储实现，支持：
 * - 文件存储和读取
 * - 文件删除和存在性检查
 * - 自动目录创建
 * - 文件信息查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Component
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalFileStorageImpl implements FileStorageStrategy {


    @Resource
    private FileStorageProperties storageProperties;

    /**
     * 初始化时创建必要的目录结构
     */
    @PostConstruct
    public void init() {
        try {
            Path rootPath = Paths.get(storageProperties.getLocal().getBasePath());
            Files.createDirectories(rootPath);
            log.info("[本地存储] 初始化成功, basePath={}", rootPath);
        } catch (Exception e) {
            log.error("[本地存储] 初始化失败", e);
            throw new RuntimeException("本地存储初始化失败", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String filePath) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException("BUSINESS_ERROR", "文件不能为空");
            }

            Path targetPath = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
            Path parentPath = targetPath.getParent();

            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            Files.copy(file.getInputStream(), targetPath);
            log.info("[本地存储] 文件存储成功: {}", filePath);

            return filePath;
        } catch (IOException e) {
            log.error("[本地存储] 文件存储失败: {}", filePath, e);
            throw new BusinessException("STORAGE_ERROR", "文件存储失败", e);
        }
    }

    @Override
    public InputStream getFileStream(String filePath) {
        try {
            Path path = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
            if (!Files.exists(path)) {
                throw new BusinessException("文件不存在: " + filePath);
            }
            return Files.newInputStream(path);
        } catch (IOException e) {
            log.error("[本地存储] 获取文件流失败: {}", filePath, e);
            throw new BusinessException("STORAGE_ERROR", "获取文件流失败", e);
        }
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
    public Map<String, Object> getFileInfo(String filePath) {
        try {
            Path path = Paths.get(storageProperties.getLocal().getBasePath(), filePath);
            if (!Files.exists(path)) {
                return null;
            }

            java.nio.file.attribute.BasicFileAttributes attrs = Files.readAttributes(path, java.nio.file.attribute.BasicFileAttributes.class);
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", path.getFileName().toString());
            fileInfo.put("filePath", filePath);
            fileInfo.put("fileSize", attrs.size());
            fileInfo.put("lastModifiedTime", attrs.lastModifiedTime().toMillis());
            fileInfo.put("isDirectory", attrs.isDirectory());
            fileInfo.put("isRegularFile", attrs.isRegularFile());

            return fileInfo;
        } catch (Exception e) {
            log.error("[本地存储] 获取文件信息失败: {}", filePath, e);
            return null;
        }
    }

}

