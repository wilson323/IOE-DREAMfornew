package net.lab1024.sa.admin.module.oa.document.service;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentPermissionEntity;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文档管理服务接口
 *
 * 提供完整的文档管理功能，包括：
 * - 文档上传、创建、编辑、删除
 * - 版本控制和历史管理
 * - 权限管理和访问控制
 * - 文档搜索和分类
 * - 在线协作和评论
 * - 文档分享和导出
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface DocumentService {

    /**
     * ==================== 文档CRUD操作 ====================
     */

    /**
     * 上传文档
     *
     * @param file 文件
     * @param title 标题
     * @param categoryId 分类ID
     * @param tags 标签列表
     * @param description 描述
     * @param accessPermission 访问权限
     * @return 文档ID
     */
    ResponseDTO<Long> uploadDocument(MultipartFile file, String title, Long categoryId,
                                     List<String> tags, String description, String accessPermission);

    /**
     * 创建文档
     *
     * @param title 标题
     * @param content 内容
     * @param documentType 文档类型
     * @param categoryId 分类ID
     * @param tags 标签列表
     * @param keywords 关键词列表
     * @param accessPermission 访问权限
     * @return 文档ID
     */
    ResponseDTO<Long> createDocument(String title, String content, String documentType,
                                      Long categoryId, List<String> tags, List<String> keywords,
                                      String accessPermission);

    /**
     * 分页查询文档
     *
     * @param pageParam 分页参数
     * @param categoryId 分类ID(可选)
     * @param documentType 文档类型(可选)
     * @param status 状态(可选)
     * @param keyword 关键词(可选)
     * @param tags 标签(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 分页结果
     */
    ResponseDTO<PageResult<DocumentEntity>> pageDocuments(PageParam pageParam,
                                                           Long categoryId, String documentType,
                                                           String status, String keyword,
                                                           String tags, String startDate, String endDate);

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 文档详情
     */
    ResponseDTO<DocumentEntity> getDocument(Long documentId, Long userId);

    /**
     * 更新文档
     *
     * @param documentId 文档ID
     * @param title 标题(可选)
     * @param content 内容(可选)
     * @param tags 标签(可选)
     * @param keywords 关键词(可选)
     * @return 操作结果
     */
    ResponseDTO<String> updateDocument(Long documentId, String title, String content,
                                       List<String> tags, List<String> keywords);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param reason 删除原因
     * @return 操作结果
     */
    ResponseDTO<String> deleteDocument(Long documentId, Long userId, String reason);

    /**
     * 批量删除文档
     *
     * @param documentIds 文档ID列表
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<String> batchDeleteDocuments(List<Long> documentIds, Long userId);

    /**
     * ==================== 版本管理 ====================
     */

    /**
     * 创建新版本
     *
     * @param documentId 文档ID
     * @param content 新内容
     * @param versionName 版本名称
     * @param versionDescription 版本描述
     * @param changeType 变更类型
     * @return 版本ID
     */
    ResponseDTO<Long> createVersion(Long documentId, String content, String versionName,
                                    String versionDescription, String changeType);

    /**
     * 获取文档版本列表
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    ResponseDTO<List<DocumentVersionEntity>> getDocumentVersions(Long documentId);

    /**
     * 获取版本详情
     *
     * @param versionId 版本ID
     * @return 版本详情
     */
    ResponseDTO<DocumentVersionEntity> getVersionDetail(Long versionId);

    /**
     * 恢复到指定版本
     *
     * @param documentId 文档ID
     * @param versionId 版本ID
     * @return 操作结果
     */
    ResponseDTO<String> restoreToVersion(Long documentId, Long versionId);

    /**
     * 对比版本差异
     *
     * @param versionId1 版本ID1
     * @param versionId2 版本ID2
     * @return 差异信息
     */
    ResponseDTO<Map<String, Object>> compareVersions(Long versionId1, Long versionId2);

    /**
     * 删除版本
     *
     * @param versionId 版本ID
     * @return 操作结果
     */
    ResponseDTO<String> deleteVersion(Long versionId);

    /**
     * ==================== 权限管理 ====================
     */

    /**
     * 设置文档权限
     *
     * @param documentId 文档ID
     * @param permissionType 权限类型
     * @param targetId 目标ID
     * @param permissions 权限列表
     * @param effectiveTime 有效期
     * @return 操作结果
     */
    ResponseDTO<String> setDocumentPermission(Long documentId, String permissionType,
                                                Long targetId, List<String> permissions,
                                                LocalDateTime effectiveTime);

    /**
     * 获取文档权限列表
     *
     * @param documentId 文档ID
     * @return 权限列表
     */
    ResponseDTO<List<DocumentPermissionEntity>> getDocumentPermissions(Long documentId);

    /**
     * 检查用户权限
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 是否有权限
     */
    ResponseDTO<Boolean> checkPermission(Long documentId, Long userId, String permission);

    /**
     * 撤销文档权限
     *
     * @param permissionId 权限ID
     * @param reason 撤销原因
     * @return 操作结果
     */
    ResponseDTO<String> revokePermission(Long permissionId, String reason);

    /**
     * 获取用户可访问的文档
     *
     * @param userId 用户ID
     * @param permission 权限类型
     * @return 文档列表
     */
    ResponseDTO<List<DocumentEntity>> getUserAccessibleDocuments(Long userId, String permission);

    /**
     * ==================== 文档搜索 ====================
     */

    /**
     * 全文搜索文档
     *
     * @param keyword 关键词
     * @param categoryId 分类ID(可选)
     * @param documentType 文档类型(可选)
     * @param tags 标签(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @param pageParam 分页参数
     * @return 搜索结果
     */
    ResponseDTO<PageResult<DocumentEntity>> searchDocuments(String keyword, Long categoryId,
                                                            String documentType, String tags,
                                                            String startDate, String endDate,
                                                            PageParam pageParam);

    /**
     * 高级搜索文档
     *
     * @param searchParams 搜索参数
     * @param pageParam 分页参数
     * @return 搜索结果
     */
    ResponseDTO<PageResult<DocumentEntity>> advancedSearch(Map<String, Object> searchParams,
                                                            PageParam pageParam);

    /**
     * 获取文档统计
     *
     * @param categoryId 分类ID(可选)
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 统计信息
     */
    ResponseDTO<Map<String, Object>> getDocumentStatistics(Long categoryId, String startDate, String endDate);

    /**
     * ==================== 文档协作 ====================
     */

    /**
     * 分享文档
     *
     * @param documentId 文档ID
     * @param shareType 分享类型 (LINK-链接, USER-用户, DEPT-部门, PUBLIC-公开)
     * @param targetId 目标ID
     * @param permission 权限
     * @param expireTime 过期时间
     * @return 分享链接或Token
     */
    ResponseDTO<String> shareDocument(Long documentId, String shareType, Long targetId,
                                       String permission, LocalDateTime expireTime);

    /**
     * 取消分享
     *
     * @param shareToken 分享Token
     * @return 操作结果
     */
    ResponseDTO<String> unshareDocument(String shareToken);

    /**
     * 收藏文档
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<String> favoriteDocument(Long documentId, Long userId);

    /**
     * 取消收藏
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 操作结果
     */
    ResponseDTO<String> unfavoriteDocument(Long documentId, Long userId);

    /**
     * 获取用户收藏的文档
     *
     * @param userId 用户ID
     * @param pageParam 分页参数
     * @return 文档列表
     */
    ResponseDTO<PageResult<DocumentEntity>> getUserFavorites(Long userId, PageParam pageParam);

    /**
     * ==================== 文档导出 ====================
     */

    /**
     * 导出文档
     *
     * @param documentId 文档ID
     * @param format 导出格式 (PDF, DOCX, HTML, TXT)
     * @return 导出文件URL
     */
    ResponseDTO<String> exportDocument(Long documentId, String format);

    /**
     * 批量导出文档
     *
     * @param documentIds 文档ID列表
     * @param format 导出格式
     * @return 导出任务ID
     */
    ResponseDTO<String> batchExportDocuments(List<Long> documentIds, String format);

    /**
     * 获取导出状态
     *
     * @param exportTaskId 导出任务ID
     * @return 导出状态
     */
    ResponseDTO<Map<String, Object>> getExportStatus(String exportTaskId);

    /**
     * 下载文档
     *
     * @param documentId 文档ID
     * @param userId 用户ID
     * @return 下载URL
     */
    ResponseDTO<String> downloadDocument(Long documentId, Long userId);
}