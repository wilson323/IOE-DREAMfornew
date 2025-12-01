package net.lab1024.sa.enterprise.oa.manager;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import net.lab1024.sa.enterprise.oa.dao.DocumentVersionDao;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentVersionEntity;

/**
 * 文档管理器
 * 负责文档的业务逻辑处理，如文件上传、版本管理等
 *
 * @author IOE-DREAM Team
 */
@Component
public class DocumentManager {

    @Resource
    private DocumentVersionDao documentVersionDao;

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件URL
     */
    public String uploadFile(MultipartFile file) {
        // TODO: 实现文件上传逻辑
        // 这里应该调用文件服务或对象存储服务
        // 返回文件访问URL
        return "/files/documents/" + file.getOriginalFilename();
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
        version.setChangeType("CREATE");
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setCreatedTime(LocalDateTime.now());
        // TODO: 设置创建者信息
        // version.setCreatedById(SmartRequestUtil.getRequestUserId());

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
        version.setVersionNumber(Integer.parseInt(nextVersionNumber));
        version.setChangeType("UPDATE");
        version.setTitle(document.getTitle());
        version.setContent(document.getContent());
        version.setCreatedTime(LocalDateTime.now());
        // TODO: 设置创建者信息
        // version.setCreatedById(SmartRequestUtil.getRequestUserId());

        documentVersionDao.insert(version);
    }

    /**
     * 生成下载URL
     *
     * @param document 文档实体
     * @param latest   是否最新版本
     * @return 下载URL
     */
    public String generateDownloadUrl(DocumentEntity document, Boolean latest) {
        // TODO: 实现下载URL生成逻辑
        if (document.getFileUrl() != null) {
            return document.getFileUrl();
        }
        return "/api/oa/document/" + document.getDocumentId() + "/download";
    }

    /**
     * 获取最新版本号
     *
     * @param documentId 文档ID
     * @return 最新版本号
     */
    private String getLatestVersionNumber(Long documentId) {
        // TODO: 从数据库获取最新版本号
        return "1.0";
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
