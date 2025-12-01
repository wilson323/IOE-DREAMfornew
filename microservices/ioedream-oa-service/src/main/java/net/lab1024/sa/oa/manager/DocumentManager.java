package net.lab1024.sa.oa.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.common.util.SmartStringUtil;
import net.lab1024.sa.oa.dao.DocumentVersionDao;
import net.lab1024.sa.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentVersionEntity;

/**
 * 文档管理器
 * 负责文档的业务逻辑处理，如文件上传、版本管理等
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class DocumentManager {

    @Resource
    private DocumentVersionDao documentVersionDao;

    /**
     * 文件上传路径配置
     */
    @Value("${oa.document.upload-path:/data/oa/documents}")
    private String uploadPath;

    /**
     * 文件访问基础URL
     */
    @Value("${oa.document.base-url:http://localhost:8084/oa-service/files}")
    private String baseUrl;

    /**
     * 最大文件大小(默认50MB)
     */
    @Value("${oa.document.max-file-size:52428800}")
    private Long maxFileSize;

    /**
     * 允许的文件类型
     */
    @Value("${oa.document.allowed-types:doc,docx,xls,xlsx,ppt,pptx,pdf,txt,md,jpg,jpeg,png,gif,bmp}")
    private String allowedTypes;

    /**
     * 上传文件
     *
     * 该方法负责处理文件上传的完整流程，包括：
     * 1. 文件验证（大小、类型、非空）
     * 2. 生成唯一文件名避免冲突
     * 3. 创建存储目录（按年月日分层）
     * 4. 保存文件到本地存储
     * 5. 返回文件访问URL
     *
     * @param file 待上传的文件对象，包含文件内容和元数据
     * @return 文件访问URL，格式为：{baseUrl}/documents/{yyyy/MM/dd}/{filename}
     *
     * @throws BusinessException 当以下情况时抛出：
     *                           - 文件为空或不存在
     *                           - 文件大小超过限制
     *                           - 文件类型不允许
     *                           - 文件保存失败
     *
     * @example
     *
     *          <pre>
     * MultipartFile file = ...;
     * String fileUrl = documentManager.uploadFile(file);
     * // 返回: http://localhost:8084/oa-service/files/documents/2024/01/15/1705320000_abc123def456.pdf
     *          </pre>
     */
    public String uploadFile(MultipartFile file) {
        // 1. 验证文件非空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 2. 验证文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制，最大允许：" + (maxFileSize / 1024 / 1024) + "MB");
        }

        // 3. 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isAllowedFileType(fileExtension)) {
            throw new BusinessException("不支持的文件类型：" + fileExtension + "，允许的类型：" + allowedTypes);
        }

        try {
            // 4. 生成唯一文件名（时间戳_UUID.扩展名）
            String fileName = generateUniqueFileName(originalFilename);

            // 5. 生成存储路径（按年月日分层：yyyy/MM/dd）
            String datePath = generateDatePath();
            String relativePath = "documents/" + datePath + "/" + fileName;
            String fullDirPath = uploadPath + "/" + datePath;
            String fullFilePath = fullDirPath + "/" + fileName;

            // 6. 创建目录（如果不存在）
            Path dirPath = Paths.get(fullDirPath);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                log.debug("创建文件存储目录: {}", fullDirPath);
            }

            // 7. 保存文件
            Path filePath = Paths.get(fullFilePath);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 8. 生成文件访问URL
            String fileUrl = baseUrl + "/" + relativePath;

            log.info("文件上传成功: 原始文件名={}, 存储路径={}, 文件大小={}字节",
                    originalFilename, relativePath, file.getSize());

            return fileUrl;

        } catch (IOException e) {
            log.error("文件上传失败: 原始文件名={}, 错误信息={}", originalFilename, e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名（不含点号），如果无扩展名则返回空字符串
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 检查文件类型是否允许
     *
     * @param fileExtension 文件扩展名
     * @return 是否允许
     */
    private boolean isAllowedFileType(String fileExtension) {
        if (SmartStringUtil.isEmpty(fileExtension) || SmartStringUtil.isEmpty(allowedTypes)) {
            return false;
        }
        String[] allowedTypeArray = allowedTypes.split(",");
        for (String allowedType : allowedTypeArray) {
            if (fileExtension.equalsIgnoreCase(allowedType.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成唯一文件名
     * 格式：{时间戳}_{UUID}.{扩展名}
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = getFileExtension(originalFilename);

        if (SmartStringUtil.isEmpty(extension)) {
            return timestamp + "_" + uuid;
        }
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * 生成日期路径（按年月日分层）
     * 格式：yyyy/MM/dd
     *
     * @return 日期路径字符串
     */
    private String generateDatePath() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return now.format(formatter);
    }

    /**
     * 创建初始版本
     *
     * @param document 文档实体
     */
    public void createInitialVersion(DocumentEntity document) {
        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(1);
        version.setVersionName("1.0");
        version.setChangeType("CREATE");
        version.setContent(document.getContent());
        version.setCreateTime(LocalDateTime.now());
        // 设置创建者信息
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            version.setCreatedBy(userId);
        }

        documentVersionDao.insert(version);
    }

    /**
     * 创建版本快照
     *
     * @param document 文档实体
     */
    public void createVersionSnapshot(DocumentEntity document) {
        // 获取当前最新版本号
        String latestVersionNumber = getLatestVersionNumber(document.getDocumentId());
        String nextVersionNumber = generateNextVersionNumber(latestVersionNumber);

        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocumentId(document.getDocumentId());
        version.setVersionName(nextVersionNumber);
        version.setChangeType("UPDATE");
        version.setContent(document.getContent());
        version.setCreateTime(LocalDateTime.now());
        // 解析版本号为整数
        try {
            String[] parts = nextVersionNumber.split("\\.");
            if (parts.length > 0) {
                version.setVersionNumber(Integer.parseInt(parts[0]));
            }
        } catch (NumberFormatException e) {
            // 如果解析失败，使用默认值
            version.setVersionNumber(1);
        }
        // 设置创建者信息
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            version.setCreatedBy(userId);
        }

        documentVersionDao.insert(version);
    }

    /**
     * 生成下载URL
     *
     * 根据文档实体和版本要求生成文件下载URL，支持：
     * 1. 如果latest为true，返回最新版本的下载URL
     * 2. 如果文档有fileUrl，直接返回
     * 3. 否则生成标准的API下载URL
     *
     * @param document 文档实体，包含文档ID和文件URL等信息
     * @param latest   是否最新版本，true表示返回最新版本URL，false表示返回当前文档URL
     * @return 文件下载URL，格式为：
     *         - 最新版本: {baseUrl}/documents/{path}/{filename} 或
     *         /api/oa/document/{documentId}/download?version=latest
     *         - 当前版本: {fileUrl} 或 /api/oa/document/{documentId}/download
     *
     * @throws BusinessException 当文档ID为空时抛出
     *
     * @example
     *
     *          <pre>
     * DocumentEntity doc = ...;
     * // 获取最新版本下载URL
     * String latestUrl = documentManager.generateDownloadUrl(doc, true);
     * // 获取当前版本下载URL
     * String currentUrl = documentManager.generateDownloadUrl(doc, false);
     *          </pre>
     */
    public String generateDownloadUrl(DocumentEntity document, Boolean latest) {
        if (document == null || document.getDocumentId() == null) {
            throw new BusinessException("文档信息不能为空");
        }

        // 如果需要最新版本，尝试获取最新版本的文件URL
        if (Boolean.TRUE.equals(latest)) {
            String latestVersionNumber = getLatestVersionNumber(document.getDocumentId());
            if (!"1.0".equals(latestVersionNumber)) {
                // 获取最新版本的版本实体
                DocumentVersionEntity latestVersion = getLatestVersionEntity(document.getDocumentId());
                if (latestVersion != null && latestVersion.getFileUrl() != null) {
                    return latestVersion.getFileUrl();
                }
            }
        }

        // 如果文档有文件URL，直接返回
        if (document.getFileUrl() != null && !document.getFileUrl().isEmpty()) {
            return document.getFileUrl();
        }

        // 生成标准的API下载URL
        String downloadUrl = baseUrl.replace("/files", "/api/oa/document")
                + "/" + document.getDocumentId() + "/download";

        // 如果是最新版本，添加版本参数
        if (Boolean.TRUE.equals(latest)) {
            downloadUrl += "?version=latest";
        }

        log.debug("生成文档下载URL: documentId={}, latest={}, url={}",
                document.getDocumentId(), latest, downloadUrl);

        return downloadUrl;
    }

    /**
     * 获取最新版本号
     *
     * 从数据库查询指定文档的最新版本号，按创建时间倒序排列获取第一条记录。
     * 如果文档没有版本记录，返回默认版本号"1.0"。
     *
     * @param documentId 文档ID，不能为空
     * @return 最新版本号，格式为"major.minor"（如"1.0", "2.5"），如果没有版本记录则返回"1.0"
     *
     * @throws BusinessException 当documentId为空时抛出
     *
     * @example
     *
     *          <pre>
     *          Long docId = 123L;
     *          String version = documentManager.getLatestVersionNumber(docId);
     *          // 可能返回: "2.3"
     *          </pre>
     */
    private String getLatestVersionNumber(Long documentId) {
        if (documentId == null) {
            log.warn("获取最新版本号失败：文档ID为空");
            return "1.0";
        }

        try {
            // 查询该文档的所有版本，按创建时间倒序排列
            LambdaQueryWrapper<DocumentVersionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocumentVersionEntity::getDocumentId, documentId)
                    .orderByDesc(DocumentVersionEntity::getCreateTime)
                    .last("LIMIT 1");

            DocumentVersionEntity latestVersion = documentVersionDao.selectOne(queryWrapper);

            if (latestVersion != null && latestVersion.getVersionName() != null) {
                log.debug("获取文档最新版本号: documentId={}, version={}",
                        documentId, latestVersion.getVersionName());
                return latestVersion.getVersionName();
            }

            log.debug("文档没有版本记录，返回默认版本号: documentId={}", documentId);
            return "1.0";
        } catch (Exception e) {
            log.error("获取文档最新版本号失败: documentId={}", documentId, e);
            // 发生异常时返回默认版本号，不中断业务流程
            return "1.0";
        }
    }

    /**
     * 获取最新版本实体
     *
     * 从数据库查询指定文档的最新版本实体对象，用于获取版本详细信息。
     *
     * @param documentId 文档ID，不能为空
     * @return 最新版本的DocumentVersionEntity对象，如果没有版本记录则返回null
     */
    private DocumentVersionEntity getLatestVersionEntity(Long documentId) {
        if (documentId == null) {
            return null;
        }

        try {
            LambdaQueryWrapper<DocumentVersionEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DocumentVersionEntity::getDocumentId, documentId)
                    .orderByDesc(DocumentVersionEntity::getCreateTime)
                    .last("LIMIT 1");

            return documentVersionDao.selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("获取文档最新版本实体失败: documentId={}", documentId, e);
            return null;
        }
    }

    /**
     * 生成下一个版本号
     *
     * @param currentVersion 当前版本号
     * @return 下一个版本号
     */
    private String generateNextVersionNumber(String currentVersion) {
        try {
            String[] parts = currentVersion.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            return (major) + "." + (minor + 1);
        } catch (Exception e) {
            return "2.0";
        }
    }
}
