package net.lab1024.sa.enterprise.oa.document.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentVersionEntity;

/**
 * 文档服务接口
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface DocumentService {

    /**
     * 分页查询文档
     */
    ResponseDTO<PageResult<DocumentEntity>> pageDocuments(PageParam pageParam, Long categoryId, String documentType,
            String status, String keyword, String tags,
            String startTime, String endTime);

    /**
     * 获取文档详情
     */
    ResponseDTO<DocumentEntity> getDocument(Long documentId, Long userId);

    /**
     * 创建文档
     */
    ResponseDTO<Long> createDocument(String title, String content, String documentType, Long categoryId,
            String tags, String keywords, String accessLevel);

    /**
     * 上传文档
     */
    ResponseDTO<Long> uploadDocument(MultipartFile file, String title, Long categoryId, List<String> tags,
            String keywords, String accessLevel);

    /**
     * 更新文档
     */
    ResponseDTO<String> updateDocument(Long documentId, String title, String content,
            String tags, String keywords);

    /**
     * 删除文档
     */
    ResponseDTO<String> deleteDocument(Long documentId, Long userId, String reason);

    /**
     * 批量删除文档
     */
    ResponseDTO<String> batchDeleteDocuments(List<Long> documentIds, Long userId);

    /**
     * 下载文档
     */
    ResponseDTO<String> downloadDocument(Long documentId, Long userId);

    /**
     * 获取文档版本列表
     */
    ResponseDTO<List<DocumentVersionEntity>> getDocumentVersions(Long documentId);

    /**
     * 获取版本详情
     */
    ResponseDTO<DocumentVersionEntity> getVersionDetail(Long versionId);

    /**
     * 版本对比
     */
    ResponseDTO<Map<String, Object>> compareVersions(Long sourceVersionId, Long targetVersionId);

    /**
     * 恢复到指定版本
     */
    ResponseDTO<String> restoreToVersion(Long documentId, Long versionId);

    /**
     * 获取文档权限列表
     */
    ResponseDTO<List<DocumentPermissionEntity>> getDocumentPermissions(Long documentId);

    /**
     * 撤销权限
     */
    ResponseDTO<String> revokePermission(Long permissionId, String reason);

    /**
     * 高级搜索
     */
    ResponseDTO<PageResult<DocumentEntity>> advancedSearch(Map<String, Object> params, PageParam pageParam);

    /**
     * 获取文档统计
     */
    ResponseDTO<Map<String, Object>> getDocumentStatistics(Long categoryId, String startDate, String endDate);
}
