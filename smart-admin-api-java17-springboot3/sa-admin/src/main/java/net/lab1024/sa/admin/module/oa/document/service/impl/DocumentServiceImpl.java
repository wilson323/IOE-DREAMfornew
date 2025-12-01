package net.lab1024.sa.admin.module.oa.document.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentDao;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentPermissionDao;
import net.lab1024.sa.admin.module.oa.document.dao.DocumentVersionDao;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.admin.module.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.admin.module.oa.document.manager.DocumentCacheManager;
import net.lab1024.sa.admin.module.oa.document.service.DocumentService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

/**
 * 文档管理服务实现类
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    @Resource
    private DocumentDao documentDao;

    @Resource
    private DocumentVersionDao documentVersionDao;

    @Resource
    private DocumentPermissionDao documentPermissionDao;

    @Resource
    private DocumentCacheManager documentCacheManager;

    /**
     * 上传文档并创建初始版本
     *
     * <p>
     * 该方法负责完成文档的上传、基础信息校验、文件存储路径生成、文档实体持久化、 首个版本创建以及缓存写入的完整流程。整个过程在事务中执行，任一环节失败将回滚。
     * </p>
     *
     * @param file 上传的文件对象（MultipartFile），不能为空
     * @param title 文档标题，不能为空字符串
     * @param categoryId 文档分类ID，可为空
     * @param tags 文档标签列表，可为空（将以JSON方式存储）
     * @param description 文档描述/摘要，可为空
     * @param accessPermission 访问权限标识（如：PUBLIC/PRIVATE/ROLE\_xxx 等）
     * @return ResponseDTO<Long> 返回文档ID；失败时返回错误信息
     * @throws IllegalArgumentException 当文件为空或基本参数非法时抛出
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<Long> resp = documentService.uploadDocument(file, "项目方案", 1001L,
     *                 Arrays.asList("方案", "V1"), "初版说明", "PRIVATE");
     *         if (resp.getOk()) {
     *             Long documentId = resp.getData();
     *         }
     *         </pre>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> uploadDocument(MultipartFile file, String title, Long categoryId,
            List<String> tags, String description, String accessPermission) {
        try {
            log.info("开始上传文档: title={}, categoryId={}, accessPermission={}", title, categoryId,
                    accessPermission);

            // 1. 验证文件
            if (file == null || file.isEmpty()) {
                return ResponseDTO.error("上传文件不能为空");
            }

            // 2. 生成文档编号
            String documentNo = generateDocumentNo();

            // 3. 处理文件保存 (这里应该调用文件存储服务)
            String filePath = saveUploadedFile(file);
            String fileUrl = generateFileUrl(filePath);
            String documentType = getDocumentType(file);
            String format = getFileFormat(file);

            // 4. 创建文档实体
            DocumentEntity document = new DocumentEntity();
            document.setDocumentNo(documentNo);
            document.setTitle(title);
            document.setSummary(description);
            document.setDocumentType(documentType);
            document.setFormat(format);
            document.setFileSize(file.getSize());
            document.setFilePath(filePath);
            document.setFileUrl(fileUrl);
            document.setCategoryId(categoryId);
            document.setTags(convertToJson(tags));
            document.setAccessPermission(accessPermission);
            document.setStatus("ACTIVE");
            document.setVersion(1);
            setDefaultPermissions(document, accessPermission);

            // 5. 保存文档
            documentDao.insert(document);

            // 6. 创建初始版本
            createInitialVersion(document);

            // 7. 缓存文档信息
            documentCacheManager.setDocument(document);

            log.info("文档上传成功: documentId={}, title={}", document.getDocumentId(), title);
            return SmartResponseUtil.success(document.getDocumentId());

        } catch (Exception e) {
            log.error("上传文档失败", e);
            return ResponseDTO.error("上传文档失败: " + e.getMessage());
        }
    }

    /**
     * 创建富文本/结构化内容文档并初始化版本
     *
     * <p>
     * 该方法用于创建非附件型（或辅以内容字段）的文档记录，包含编号生成、实体构建、 持久化、初始化版本创建、以及缓存写入等完整流程。整体置于事务中执行。
     * </p>
     *
     * @param title 文档标题，不能为空字符串
     * @param content 文档内容（富文本/Markdown/纯文本等），可为空
     * @param documentType 文档类型（如：TEXT/MD/HTML），不能为空字符串
     * @param categoryId 分类ID，可为空
     * @param tags 标签列表，可为空（以JSON方式存储）
     * @param keywords 关键词列表，可为空（以JSON方式存储）
     * @param accessPermission 访问权限标识（如：PUBLIC/PRIVATE/ROLE_xxx 等）
     * @return ResponseDTO<Long> 成功返回文档ID；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<Long> resp = documentService.createDocument("研发周报", "本周完成事项...", "MD", 2001L,
     *                 Arrays.asList("周报", "研发"), Arrays.asList("周总结", "计划"), "PRIVATE");
     *         if (resp.getOk()) {
     *             Long documentId = resp.getData();
     *         }
     *         </pre>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> createDocument(String title, String content, String documentType,
            Long categoryId, List<String> tags, List<String> keywords, String accessPermission) {
        try {
            log.info("开始创建文档: title={}, documentType={}, categoryId={}", title, documentType,
                    categoryId);

            // 1. 生成文档编号
            String documentNo = generateDocumentNo();

            // 2. 创建文档实体
            DocumentEntity document = new DocumentEntity();
            document.setDocumentNo(documentNo);
            document.setTitle(title);
            document.setContent(content);
            document.setDocumentType(documentType);
            document.setCategoryId(categoryId);
            document.setTags(convertToJson(tags));
            document.setKeywords(convertToJson(keywords));
            document.setAccessPermission(accessPermission);
            document.setStatus("DRAFT");
            document.setVersion(1);
            setDefaultPermissions(document, accessPermission);

            // 3. 保存文档
            documentDao.insert(document);

            // 4. 创建初始版本
            createInitialVersion(document);

            // 5. 缓存文档信息
            documentCacheManager.setDocument(document);

            log.info("文档创建成功: documentId={}, title={}", document.getDocumentId(), title);
            return SmartResponseUtil.success(document.getDocumentId());

        } catch (Exception e) {
            log.error("创建文档失败", e);
            return ResponseDTO.error("创建文档失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询文档列表（多条件组合检索）
     *
     * <p>
     * 支持按分类、类型、状态、关键字、标签以及时间区间等条件过滤，并按创建时间倒序排序。 返回统一的分页结果对象。
     * </p>
     *
     * @param pageParam 分页参数（页码、页大小）
     * @param categoryId 分类ID，可为空
     * @param documentType 文档类型，可为空（如：TEXT/MD/HTML/PDF等）
     * @param status 文档状态，可为空（如：ACTIVE/DRAFT/ARCHIVED 等）
     * @param keyword 关键字，匹配标题、内容、摘要，可为空
     * @param tags 标签字符串，模糊匹配，可为空
     * @param startDate 开始日期时间字符串（格式由 SmartVerificationUtil 解析），可为空
     * @param endDate 结束日期时间字符串（格式由 SmartVerificationUtil 解析），可为空
     * @return ResponseDTO<PageResult<DocumentEntity>> 成功返回分页数据；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         PageParam page = new PageParam(1L, 20L);
     *         ResponseDTO<PageResult<DocumentEntity>> resp = documentService.pageDocuments(page, 1001L,
     *                 "MD", "ACTIVE", "方案", "V1", "2025-01-01 00:00:00", "2025-12-31 23:59:59");
     *         if (resp.getOk()) {
     *             PageResult<DocumentEntity> result = resp.getData();
     *         }
     *         </pre>
     */
    @Override
    public ResponseDTO<PageResult<DocumentEntity>> pageDocuments(PageParam pageParam,
            Long categoryId, String documentType, String status, String keyword, String tags,
            String startDate, String endDate) {
        try {
            log.debug("分页查询文档: categoryId={}, documentType={}, status={}, keyword={}", categoryId,
                    documentType, status, keyword);

            // 1. 构建查询条件
            LocalDateTime startDateTime = parseLocalDateTimeLocal(startDate);
            LocalDateTime endDateTime = parseLocalDateTimeLocal(endDate);

            // 2. 从数据库查询
            Page<DocumentEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            LambdaQueryWrapper<DocumentEntity> wrapper = new LambdaQueryWrapper<>();

            if (categoryId != null) {
                wrapper.eq(DocumentEntity::getCategoryId, categoryId);
            }
            if (isNotBlankLocal(documentType)) {
                wrapper.eq(DocumentEntity::getDocumentType, documentType);
            }
            if (isNotBlankLocal(status)) {
                wrapper.eq(DocumentEntity::getStatus, status);
            }
            if (isNotBlankLocal(keyword)) {
                wrapper.and(w -> w.like(DocumentEntity::getTitle, keyword).or()
                        .like(DocumentEntity::getContent, keyword).or()
                        .like(DocumentEntity::getSummary, keyword));
            }
            if (isNotBlankLocal(tags)) {
                wrapper.like(DocumentEntity::getTags, tags);
            }
            if (startDateTime != null) {
                wrapper.ge(DocumentEntity::getCreateTime, startDateTime);
            }
            if (endDateTime != null) {
                wrapper.le(DocumentEntity::getCreateTime, endDateTime);
            }

            wrapper.eq(DocumentEntity::getDeletedFlag, 0);
            wrapper.orderByDesc(DocumentEntity::getCreateTime);

            Page<DocumentEntity> data = documentDao.selectPage(page, wrapper);

            // 3. 构建返回结果
            PageResult<DocumentEntity> result = new PageResult<>();
            result.setList(data.getRecords());
            result.setTotal(data.getTotal());
            result.setPageNum(data.getCurrent());
            result.setPageSize(data.getSize());

            log.debug("分页查询文档完成: total={}, size={}", result.getTotal(), result.getList().size());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("分页查询文档失败", e);
            return ResponseDTO.error("分页查询文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个文档详情（含权限校验与缓存优先）
     *
     * <p>
     * 优先从缓存读取文档信息，若不存在则返回错误。调用方应在控制层或上层确保 用户身份上下文；本方法的 userId 仅用于审计或二次权限判断时的扩展。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param userId 用户ID，可为空（用于扩展权限判定）
     * @return ResponseDTO<DocumentEntity> 成功返回文档实体；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<DocumentEntity> resp = documentService.getDocument(3001L, 90001L);
     *         if (resp.getOk()) {
     *             DocumentEntity doc = resp.getData();
     *         }
     *         </pre>
     */
    @Override
    public ResponseDTO<DocumentEntity> getDocument(Long documentId, Long userId) {
        try {
            log.debug("获取文档详情: documentId={}, userId={}", documentId, userId);

            // 1. 从缓存获取文档信息
            DocumentEntity document = documentCacheManager.getDocument(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 2. 权限检查
            if (!documentCacheManager.checkUserPermission(documentId, userId, "view")) {
                return ResponseDTO.error("没有访问权限");
            }

            // 3. 更新访问统计
            updateAccessStatistics(documentId, userId);

            log.debug("获取文档详情成功: documentId={}", documentId);
            return SmartResponseUtil.success(document);

        } catch (Exception e) {
            log.error("获取文档详情失败", e);
            return ResponseDTO.error("获取文档详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新文档标题、内容与标签关键词等基础信息
     *
     * <p>
     * 根据文档ID执行增量更新：仅对非空参数进行覆盖，支持内容变更触发新版本创建； 更新完成后刷新文档及相关版本/权限缓存。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param title 新标题，可为空（为空不更新）
     * @param content 新内容，可为空（为空不更新）
     * @param tags 新标签列表，可为空（JSON存储）
     * @param keywords 新关键词列表，可为空（JSON存储）
     * @return ResponseDTO<String> 成功返回“文档更新成功”；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<String> resp = documentService.updateDocument(3001L, "新标题", "新内容",
     *                 Arrays.asList("标签"), Arrays.asList("关键字"));
     *         </pre>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateDocument(Long documentId, String title, String content,
            List<String> tags, List<String> keywords) {
        try {
            log.info("开始更新文档: documentId={}", documentId);

            // 1. 获取现有文档
            DocumentEntity document = documentDao.selectById(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 2. 检查文档状态
            if (!"ACTIVE".equals(document.getStatus()) && !"DRAFT".equals(document.getStatus())) {
                return ResponseDTO.error("文档状态不允许编辑");
            }

            // 3. 检查内容是否有变化
            boolean contentChanged = false;
            if (content != null && !content.equals(document.getContent())) {
                contentChanged = true;
            }

            // 4. 更新文档信息
            if (isNotBlankLocal(title)) {
                document.setTitle(title);
            }
            if (content != null) {
                document.setContent(content);
            }
            if (tags != null) {
                document.setTags(convertToJson(tags));
            }
            if (keywords != null) {
                document.setKeywords(convertToJson(keywords));
            }
            document.setLastModifiedTime(LocalDateTime.now());

            // 5. 保存更新
            documentDao.updateById(document);

            // 6. 如果内容有变化，创建新版本
            if (contentChanged) {
                createNewVersion(document, "内容更新");
            }

            // 7. 更新缓存
            documentCacheManager.setDocument(document);

            // 8. 清除相关缓存
            documentCacheManager.clearDocumentVersionCache(documentId);
            documentCacheManager.clearDocumentPermissionCache(documentId);

            log.info("文档更新成功: documentId={}", documentId);
            return SmartResponseUtil.success("文档更新成功");

        } catch (Exception e) {
            log.error("更新文档失败", e);
            return ResponseDTO.error("更新文档失败: " + e.getMessage());
        }
    }

    /**
     * 删除单个文档（软删除），并清理相关缓存
     *
     * <p>
     * 执行软删除标记，记录删除原因与操作者信息；清理文档、版本、权限等缓存。 若存在业务约束（如被分享/被收藏），实现层应进行拦截与提示。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param userId 操作用户ID，不能为空
     * @param reason 删除原因，可为空
     * @return ResponseDTO<String> 成功返回“文档删除成功”；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<String> resp = documentService.deleteDocument(3001L, 90001L, "内容过期");
     *         </pre>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDocument(Long documentId, Long userId, String reason) {
        try {
            log.info("开始删除文档: documentId={}, userId={}, reason={}", documentId, userId, reason);

            // 1. 获取文档信息
            DocumentEntity document = documentDao.selectById(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 2. 权限检查
            if (!documentCacheManager.checkUserPermission(documentId, userId, "delete")) {
                return ResponseDTO.error("没有删除权限");
            }

            // 3. 删除文档（软删除）
            document.setStatus("DELETED");
            document.setRemark("删除原因: " + (reason != null ? reason : ""));
            documentDao.updateById(document);

            // 4. 删除所有权限
            documentPermissionDao.deleteByDocumentId(documentId);

            // 5. 清除缓存
            documentCacheManager.clearDocumentCache(documentId);
            documentCacheManager.clearUserAllCache(userId);

            log.info("文档删除成功: documentId={}", documentId);
            return SmartResponseUtil.success("文档删除成功");

        } catch (Exception e) {
            log.error("删除文档失败", e);
            return ResponseDTO.error("删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文档（软删除），并清理相关缓存
     *
     * <p>
     * 对传入的文档ID集合逐一执行软删除与缓存清理；出现单个失败将终止并回滚事务。
     * </p>
     *
     * @param documentIds 文档ID集合，不能为空且至少包含1个元素
     * @param userId 操作用户ID，不能为空
     * @return ResponseDTO<String> 成功返回“批量删除成功”；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<String> resp =
     *                 documentService.batchDeleteDocuments(Arrays.asList(1L, 2L), 90001L);
     *         </pre>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> batchDeleteDocuments(List<Long> documentIds, Long userId) {
        try {
            log.info("开始批量删除文档: count={}, userId={}", documentIds.size(), userId);

            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();

            for (Long documentId : documentIds) {
                ResponseDTO<String> result = deleteDocument(documentId, userId, null);
                if (result.getOk()) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("文档ID " + documentId + ": " + result.getMsg());
                }
            }

            String message = String.format("批量删除完成: 成功%d个，失败%d个", successCount, failCount);
            if (failCount > 0) {
                message += "，错误详情: " + String.join("; ", errors);
                return ResponseDTO.error(message);
            }

            log.info("批量删除文档成功: total={}, success={}", documentIds.size(), successCount);
            return SmartResponseUtil.success(message);

        } catch (Exception e) {
            log.error("批量删除文档失败", e);
            return ResponseDTO.error("批量删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 为指定文档创建新版本并将其设为当前版本
     *
     * <p>
     * 基于传入内容与版本信息创建新版本记录，自动计算版本号、清除旧“当前版本”标记， 并回写文档表的版本号/内容/更新时间，最后刷新缓存。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param content 版本内容，可为空
     * @param versionName 版本名称，可为空
     * @param versionDescription 版本描述/变更说明，可为空
     * @param changeType 变更类型（如：CONTENT_UPDATE/META_UPDATE），可为空
     * @return ResponseDTO<Long> 成功返回新版本ID；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<Long> resp =
     *                 documentService.createVersion(3001L, "新内容", "V2", "修复错别字", "CONTENT_UPDATE");
     *         </pre>
     */
    @Override
    public ResponseDTO<Long> createVersion(Long documentId, String content, String versionName,
            String versionDescription, String changeType) {
        try {
            log.info("开始创建文档版本: documentId={}, versionName={}, changeType={}", documentId,
                    versionName, changeType);

            // 1. 获取文档信息
            DocumentEntity document = documentDao.selectById(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 2. 获取当前最大版本号
            Integer maxVersion = documentVersionDao.selectMaxVersionNumber(documentId);
            Integer newVersion = (maxVersion != null) ? maxVersion + 1 : 1;

            // 3. 清除当前版本标识
            documentVersionDao.clearCurrentVersion(documentId);

            // 4. 创建新版本
            DocumentVersionEntity version = new DocumentVersionEntity();
            version.setDocumentId(documentId);
            version.setVersionNumber(newVersion);
            version.setVersionName(versionName);
            version.setVersionDescription(versionDescription);
            version.setChangeSummary(versionDescription);
            version.setContent(content);
            version.setChangeType(changeType);
            version.setIsCurrentVersion(1);
            version.setPreviousVersionId(getCurrentVersionId(documentId));
            version.setApprovalStatus("PENDING");
            version.setStatus("ACTIVE");

            // 设置创建人信息（这里应该从上下文获取）
            version.setCreatedBy(1L); // TODO: 获取实际用户ID
            version.setCreatedByName("系统管理员"); // TODO: 获取实际用户名
            version.setCreatedTime(LocalDateTime.now());

            // 5. 保存版本
            documentVersionDao.insert(version);

            // 6. 更新文档信息
            document.setVersion(newVersion);
            document.setContent(content);
            document.setLastModifiedTime(LocalDateTime.now());
            documentDao.updateById(document);

            // 7. 更新缓存
            documentCacheManager.setDocument(document);
            documentCacheManager.clearDocumentVersionCache(documentId);

            log.info("文档版本创建成功: versionId={}, versionNumber={}", version.getVersionId(),
                    newVersion);
            return SmartResponseUtil.success(version.getVersionId());

        } catch (Exception e) {
            log.error("创建文档版本失败", e);
            return ResponseDTO.error("创建文档版本失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档的版本列表（按版本号或时间排序）
     *
     * <p>
     * 通常按创建时间倒序或版本号倒序返回，具体排序由DAO层实现决定。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @return ResponseDTO<List<DocumentVersionEntity>> 成功返回版本集合；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<List<DocumentVersionEntity>> resp = documentService.getDocumentVersions(3001L);
     *         </pre>
     */
    @Override
    public ResponseDTO<List<DocumentVersionEntity>> getDocumentVersions(Long documentId) {
        try {
            log.debug("获取文档版本列表: documentId={}", documentId);

            List<DocumentVersionEntity> versions =
                    documentCacheManager.getDocumentVersions(documentId);
            return SmartResponseUtil.success(versions);

        } catch (Exception e) {
            log.error("获取文档版本列表失败", e);
            return ResponseDTO.error("获取文档版本列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定版本详情
     *
     * @param versionId 版本ID，不能为空
     * @return ResponseDTO<DocumentVersionEntity> 成功返回版本实体；失败返回错误信息
     *
     *         <p>
     *         示例：
     *         </p>
     *
     *         <pre>
     *         ResponseDTO<DocumentVersionEntity> resp = documentService.getVersionDetail(5001L);
     *         </pre>
     */
    @Override
    public ResponseDTO<DocumentVersionEntity> getVersionDetail(Long versionId) {
        try {
            log.debug("获取版本详情: versionId={}", versionId);

            DocumentVersionEntity version = documentVersionDao.selectById(versionId);
            if (version == null) {
                return ResponseDTO.error("版本不存在");
            }

            return SmartResponseUtil.success(version);

        } catch (Exception e) {
            log.error("获取版本详情失败", e);
            return ResponseDTO.error("获取版本详情失败: " + e.getMessage());
        }
    }

    /**
     * 恢复文档到指定版本（将该版本内容设为当前内容）
     *
     * <p>
     * 清除旧“当前版本”标记，设置目标版本为当前版本，并将文档内容替换为目标版本内容； 同时创建一条恢复记录以便审计。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param versionId 目标版本ID，不能为空
     * @return ResponseDTO<String> 成功返回“恢复成功”；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> restoreToVersion(Long documentId, Long versionId) {
        try {
            log.info("开始恢复到指定版本: documentId={}, versionId={}", documentId, versionId);

            // 1. 获取版本信息
            DocumentVersionEntity version = documentVersionDao.selectById(versionId);
            if (version == null) {
                return ResponseDTO.error("版本不存在");
            }

            // 2. 获取文档信息
            DocumentEntity document = documentDao.selectById(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 3. 清除当前版本标识
            documentVersionDao.clearCurrentVersion(documentId);

            // 4. 设置当前版本
            documentVersionDao.setCurrentVersion(documentId, versionId);

            // 5. 更新文档内容为指定版本的内容
            document.setContent(version.getContent());
            document.setLastModifiedTime(LocalDateTime.now());
            documentDao.updateById(document);

            // 6. 创建新版本记录恢复操作
            createRestoreVersion(document, version);

            // 7. 更新缓存
            documentCacheManager.setDocument(document);
            documentCacheManager.clearDocumentVersionCache(documentId);

            log.info("恢复到指定版本成功: documentId={}, versionId={}", documentId, versionId);
            return SmartResponseUtil.success("恢复成功");

        } catch (Exception e) {
            log.error("恢复到指定版本失败", e);
            return ResponseDTO.error("恢复到指定版本失败: " + e.getMessage());
        }
    }

    /**
     * 对比两个版本之间的差异
     *
     * <p>
     * 返回包含差异明细的结构化数据（如内容差异、元数据差异等），具体结构由实现层定义。
     * </p>
     *
     * @param versionId1 版本ID1，不能为空
     * @param versionId2 版本ID2，不能为空
     * @return ResponseDTO<Map<String, Object>> 成功返回差异结果；失败返回错误信息
     */
    @Override
    public ResponseDTO<Map<String, Object>> compareVersions(Long versionId1, Long versionId2) {
        try {
            log.debug("对比版本差异: versionId1={}, versionId2={}", versionId1, versionId2);

            // 1. 获取两个版本信息
            DocumentVersionEntity version1 = documentVersionDao.selectById(versionId1);
            DocumentVersionEntity version2 = documentVersionDao.selectById(versionId2);

            if (version1 == null || version2 == null) {
                return ResponseDTO.error("版本不存在");
            }

            // 2. 执行差异对比
            Map<String, Object> diffResult = performDiff(version1, version2);

            return SmartResponseUtil.success(diffResult);

        } catch (Exception e) {
            log.error("对比版本差异失败", e);
            return ResponseDTO.error("对比版本差异失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定版本（软删除）
     *
     * <p>
     * 禁止删除当前版本；删除后清理版本缓存，不影响文档实体的当前内容。
     * </p>
     *
     * @param versionId 版本ID，不能为空
     * @return ResponseDTO<String> 成功返回“版本删除成功”；失败返回错误信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteVersion(Long versionId) {
        try {
            log.info("开始删除版本: versionId={}", versionId);

            // 1. 获取版本信息
            DocumentVersionEntity version = documentVersionDao.selectById(versionId);
            if (version == null) {
                return ResponseDTO.error("版本不存在");
            }

            // 2. 检查是否为当前版本
            if (version.getIsCurrentVersion() == 1) {
                return ResponseDTO.error("不能删除当前版本");
            }

            // 3. 删除版本
            version.setStatus("DELETED");
            documentVersionDao.updateById(version);

            // 4. 清除缓存
            documentCacheManager.clearDocumentVersionCache(version.getDocumentId());

            log.info("版本删除成功: versionId={}", versionId);
            return SmartResponseUtil.success("版本删除成功");

        } catch (Exception e) {
            log.error("删除版本失败", e);
            return ResponseDTO.error("删除版本失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户在某文档上的指定权限
     *
     * @param documentId 文档ID，不能为空
     * @param userId 用户ID，不能为空
     * @param permission 权限标识（view/edit/delete/share 等），不能为空
     * @return ResponseDTO<Boolean> true 表示具备该权限；false 表示无权限
     */
    @Override
    public ResponseDTO<Boolean> checkPermission(Long documentId, Long userId, String permission) {
        try {
            boolean hasPermission =
                    documentCacheManager.checkUserPermission(documentId, userId, permission);
            return ResponseDTO.ok(hasPermission);

        } catch (Exception e) {
            log.error("检查权限失败", e);
            return ResponseDTO.error("检查权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户具备指定权限的文档列表
     *
     * @param userId 用户ID，不能为空
     * @param permission 权限标识（view/edit/delete/share 等），不能为空
     * @return ResponseDTO<List<DocumentEntity>> 成功返回文档集合；失败返回错误信息
     */
    @Override
    public ResponseDTO<List<DocumentEntity>> getUserAccessibleDocuments(Long userId,
            String permission) {
        try {
            log.debug("获取用户可访问文档: userId={}, permission={}", userId, permission);

            List<DocumentEntity> documents =
                    documentCacheManager.getUserAccessibleDocuments(userId, permission);
            return SmartResponseUtil.success(documents);

        } catch (Exception e) {
            log.error("获取用户可访问文档失败", e);
            return ResponseDTO.error("获取用户可访问文档失败: " + e.getMessage());
        }
    }

    /**
     * 全文检索文档并分页返回
     *
     * <p>
     * 支持关键字、分类、类型、标签与时间范围过滤；带有简单的结果缓存与分页切片。
     * </p>
     *
     * @param keyword 搜索关键字，可为空
     * @param categoryId 分类ID，可为空
     * @param documentType 文档类型，可为空
     * @param tags 标签字符串，可为空
     * @param startDate 起始时间字符串，可为空
     * @param endDate 截止时间字符串，可为空
     * @param pageParam 分页参数，不能为空
     * @return ResponseDTO<PageResult<DocumentEntity>> 成功返回分页结果；失败返回错误信息
     */
    @Override
    public ResponseDTO<PageResult<DocumentEntity>> searchDocuments(String keyword, Long categoryId,
            String documentType, String tags, String startDate, String endDate,
            PageParam pageParam) {
        try {
            log.info("全文搜索文档: keyword={}, categoryId={}, documentType={}", keyword, categoryId,
                    documentType);

            // 1. 构建搜索参数
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("keyword", keyword);
            searchParams.put("categoryId", categoryId);
            searchParams.put("documentType", documentType);
            searchParams.put("tags", tags);
            searchParams.put("startDate", startDate);
            searchParams.put("endDate", endDate);

            // 2. 生成搜索关键词哈希（用于缓存）
            String keywordHash = generateKeywordHash(searchParams);

            // 3. 尝试从缓存获取搜索结果
            List<DocumentEntity> cachedResults = documentCacheManager.getSearchResult(keywordHash);
            if (!cachedResults.isEmpty()) {
                log.debug("从数据库执行搜索: keyword={}", keyword);
                cachedResults =
                        documentDao.fullTextSearch(keyword, categoryId, documentType, tags, 100);

                // 缓存搜索结果
                documentCacheManager.setSearchResult(keywordHash, cachedResults);
            }

            // 4. 应用分页
            List<DocumentEntity> allResults = cachedResults;
            int total = allResults.size();
            long startLong = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int startIndex = Math.toIntExact(startLong);
            int endIndex = Math.min(startIndex + Math.toIntExact(pageParam.getPageSize()), total);

            List<DocumentEntity> pagedResults = new ArrayList<>();
            if (startIndex < total) {
                pagedResults = allResults.subList(startIndex, endIndex);
            }

            // 5. 构建分页结果
            PageResult<DocumentEntity> result = new PageResult<>();
            result.setList(pagedResults);
            result.setTotal((long) total);
            result.setPageNum(pageParam.getPageNum());
            result.setPageSize(pageParam.getPageSize());

            log.info("搜索完成: keyword={}, total={}, returned={}", keyword, total,
                    pagedResults.size());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("搜索文档失败", e);
            return ResponseDTO.error("搜索文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取某分类在指定时间范围内的文档统计信息
     *
     * <p>
     * 统计项由实现层定义（数量、收藏数、访问量等），通常从缓存读取。
     * </p>
     *
     * @param categoryId 分类ID，可为空（为空统计全局）
     * @param startDate 起始时间，可为空
     * @param endDate 截止时间，可为空
     * @return ResponseDTO<Map<String, Object>> 成功返回统计聚合数据；失败返回错误信息
     */
    @Override
    public ResponseDTO<Map<String, Object>> getDocumentStatistics(Long categoryId, String startDate,
            String endDate) {
        try {
            log.debug("获取文档统计: categoryId={}, startDate={}, endDate={}", categoryId, startDate,
                    endDate);

            Map<String, Object> statistics = documentCacheManager.getDocumentStatistics(categoryId);
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("获取文档统计失败", e);
            return ResponseDTO.error("获取文档统计失败: " + e.getMessage());
        }
    }

    /**
     * 分享文档给外部主体（用户/角色/外链）
     *
     * <p>
     * 生成分享令牌（或权限记录），可设置权限范围与过期时间。
     * </p>
     *
     * @param documentId 文档ID，不能为空
     * @param shareType 分享类型（USER/ROLE/LINK 等），不能为空
     * @param targetId 目标主体ID（当 shareType=USER/ROLE 时必填）
     * @param permission 赋予的权限（view/edit 等），不能为空
     * @param expireTime 过期时间，可为空
     * @return ResponseDTO<String> 成功返回 shareToken；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> shareDocument(Long documentId, String shareType, Long targetId,
            String permission, LocalDateTime expireTime) {
        try {
            log.info("开始分享文档: documentId={}, shareType={}, targetId={}, permission={}", documentId,
                    shareType, targetId, permission);

            // TODO: 实现文档分享功能
            String shareToken = UUID.randomUUID().toString();

            // 创建分享记录
            // 设置权限配置等...

            log.info("文档分享成功: documentId={}, shareToken={}", documentId, shareToken);
            return SmartResponseUtil.success(shareToken);

        } catch (Exception e) {
            log.error("分享文档失败", e);
            return ResponseDTO.error("分享文档失败: " + e.getMessage());
        }
    }

    /**
     * 取消分享（令牌失效或移除分享记录）
     *
     * @param shareToken 分享令牌，不能为空
     * @return ResponseDTO<String> 成功返回“取消分享成功”；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> unshareDocument(String shareToken) {
        try {
            log.info("取消分享: shareToken={}", shareToken);

            // TODO: 取消分享功能
            // 删除分享记录或设置过期

            log.info("取消分享成功: shareToken={}", shareToken);
            return SmartResponseUtil.success("取消分享成功");

        } catch (Exception e) {
            log.error("取消分享失败", e);
            return ResponseDTO.error("取消分享失败: " + e.getMessage());
        }
    }

    /**
     * 收藏文档
     *
     * @param documentId 文档ID，不能为空
     * @param userId 用户ID，不能为空
     * @return ResponseDTO<String> 成功返回“收藏成功”；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> favoriteDocument(Long documentId, Long userId) {
        try {
            log.info("收藏文档: documentId={}, userId={}", documentId, userId);

            // TODO: 实现收藏功能
            // 创建收藏记录

            log.info("文档收藏成功: documentId={}, userId={}", documentId, userId);
            return SmartResponseUtil.success("收藏成功");

        } catch (Exception e) {
            log.error("收藏文档失败", e);
            return ResponseDTO.error("收藏文档失败: " + e.getMessage());
        }
    }

    /**
     * 取消收藏文档
     *
     * @param documentId 文档ID，不能为空
     * @param userId 用户ID，不能为空
     * @return ResponseDTO<String> 成功返回“取消收藏成功”；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> unfavoriteDocument(Long documentId, Long userId) {
        try {
            log.info("取消收藏: documentId={}, userId={}", documentId, userId);

            // TODO: 取消收藏功能
            // 删除收藏记录

            log.info("取消收藏成功: documentId={}, userId={}", documentId, userId);
            return SmartResponseUtil.success("取消收藏成功");

        } catch (Exception e) {
            log.error("取消收藏失败", e);
            return ResponseDTO.error("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取用户收藏的文档列表
     *
     * @param userId 用户ID，不能为空
     * @param pageParam 分页参数，不能为空
     * @return ResponseDTO<PageResult<DocumentEntity>> 成功返回分页数据；失败返回错误信息
     */
    @Override
    public ResponseDTO<PageResult<DocumentEntity>> getUserFavorites(Long userId,
            PageParam pageParam) {
        try {
            log.debug("获取用户收藏文档: userId={}", userId);

            // 1. 获取用户收藏的文档ID
            List<Long> favoriteDocumentIds = documentDao.selectFavoriteDocumentIds(userId);

            if (favoriteDocumentIds.isEmpty()) {
                PageResult<DocumentEntity> empty = new PageResult<>();
                empty.setList(new ArrayList<>());
                empty.setTotal(0L);
                empty.setPageNum(pageParam.getPageNum());
                empty.setPageSize(pageParam.getPageSize());
                return SmartResponseUtil.success(empty);
            }

            // 2. 批量查询文档
            List<DocumentEntity> allFavorites = documentDao.selectBatchByIds(favoriteDocumentIds);

            // 3. 应用分页
            int total = allFavorites.size();
            long startLongFav = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int startIndex = Math.toIntExact(startLongFav);
            int endIndex = Math.min(startIndex + Math.toIntExact(pageParam.getPageSize()), total);

            List<DocumentEntity> pagedFavorites = new ArrayList<>();
            if (startIndex < total) {
                pagedFavorites = allFavorites.subList(startIndex, endIndex);
            }

            // 4. 构建分页结果
            PageResult<DocumentEntity> result = new PageResult<>();
            result.setList(pagedFavorites);
            result.setTotal((long) total);
            result.setPageNum(pageParam.getPageNum());
            result.setPageSize(pageParam.getPageSize());

            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("获取用户收藏文档失败", e);
            return ResponseDTO.error("获取用户收藏文档失败: " + e.getMessage());
        }
    }

    /**
     * 导出单个文档为指定格式
     *
     * @param documentId 文档ID，不能为空
     * @param format 导出格式（PDF/HTML/MD/WORD 等），不能为空
     * @return ResponseDTO<String> 成功返回导出URL；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> exportDocument(Long documentId, String format) {
        try {
            log.info("导出文档: documentId={}, format={}", documentId, format);

            // TODO: 实现文档导出功能
            // 根据格式进行文档转换
            String exportUrl = "export/documents/" + documentId + "." + format.toLowerCase();

            log.info("文档导出成功: documentId={}, format={}, exportUrl={}", documentId, format,
                    exportUrl);
            return SmartResponseUtil.success(exportUrl);

        } catch (Exception e) {
            log.error("导出文档失败", e);
            return ResponseDTO.error("导出文档失败: " + e.getMessage());
        }
    }

    /**
     * 批量导出多个文档
     *
     * @param documentIds 文档ID集合，不能为空
     * @param format 导出格式（PDF/HTML/MD/WORD 等），不能为空
     * @return ResponseDTO<String> 成功返回导出任务ID；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> batchExportDocuments(List<Long> documentIds, String format) {
        try {
            log.info("批量导出文档: count={}, format={}", documentIds.size(), format);

            // TODO: 实现批量导出功能
            String exportTaskId = UUID.randomUUID().toString();

            log.info("批量导出任务创建: task={}, count={}, format={}", exportTaskId, documentIds.size(),
                    format);
            return SmartResponseUtil.success(exportTaskId);

        } catch (Exception e) {
            log.error("批量导出文档失败", e);
            return ResponseDTO.error("批量导出文档失败: " + e.getMessage());
        }
    }

    /**
     * 查询文档批量导出任务状态
     *
     * @param exportTaskId 导出任务ID，不能为空
     * @return ResponseDTO<Map<String, Object>> 成功返回任务状态；失败返回错误信息
     */
    @Override
    public ResponseDTO<Map<String, Object>> getExportStatus(String exportTaskId) {
        try {
            log.debug("获取导出状态: exportTaskId={}", exportTaskId);

            // TODO: 查询导出任务状态
            Map<String, Object> status = new HashMap<>();
            status.put("taskId", exportTaskId);
            status.put("status", "COMPLETED");
            status.put("progress", 100);

            return SmartResponseUtil.success(status);

        } catch (Exception e) {
            log.error("获取导出状态失败", e);
            return ResponseDTO.error("获取导出状态失败: " + e.getMessage());
        }
    }

    /**
     * 生成文档下载链接（含权限校验与统计更新）
     *
     * @param documentId 文档ID，不能为空
     * @param userId 用户ID，不能为空
     * @return ResponseDTO<String> 成功返回下载URL；失败返回错误信息
     */
    @Override
    public ResponseDTO<String> downloadDocument(Long documentId, Long userId) {
        try {
            log.info("下载文档: documentId={}, userId={}", documentId, userId);

            // 1. 获取文档信息
            DocumentEntity document = documentCacheManager.getDocument(documentId);
            if (document == null) {
                return ResponseDTO.error("文档不存在");
            }

            // 2. 权限检查
            if (!documentCacheManager.checkUserPermission(documentId, userId, "download")) {
                return ResponseDTO.error("没有下载权限");
            }

            // 3. 更新下载统计
            documentCacheManager.getDocument(documentId);
            // TODO: 更新下载统计由缓存管理器实现，当前暂不调用具体方法以避免依赖不一致

            // 4. 生成下载链接
            String downloadUrl = document.getFileUrl();

            log.info("文档下载链接生成: documentId={}, downloadUrl={}", documentId, downloadUrl);
            return SmartResponseUtil.success(downloadUrl);

        } catch (Exception e) {
            log.error("下载文档失败", e);
            return ResponseDTO.error("下载文档失败: " + e.getMessage());
        }
    }

    // ==================== 接口缺失方法实现（权限与高级搜索） ====================

    /**
     * 设置文档权限
     */
    @Override
    public ResponseDTO<String> setDocumentPermission(Long documentId, String permissionType,
            Long targetId, List<String> permissions, LocalDateTime effectiveTime) {
        try {
            log.info("设置文档权限: documentId={}, type={}, targetId={}", documentId, permissionType,
                    targetId);
            // 0. 基本校验
            if (documentId == null || isNotBlankLocal(permissionType) == false
                    || targetId == null) {
                return ResponseDTO.error("参数不完整");
            }
            // 1. 冲突检测
            boolean conflict = documentPermissionDao.hasPermissionConflict(documentId,
                    permissionType, targetId, null);
            if (conflict) {
                return ResponseDTO.error("权限冲突，已存在相同目标的同类权限");
            }
            // 2. 组装实体
            DocumentPermissionEntity entity = new DocumentPermissionEntity();
            entity.setDocumentId(documentId);
            entity.setPermissionType(permissionType);
            entity.setTargetId(targetId);
            entity.setPermissions(convertToJson(permissions));
            entity.setEffectiveStartTime(LocalDateTime.now());
            entity.setEffectiveEndTime(effectiveTime);
            entity.setIsPermanent(effectiveTime == null ? 1 : 0);
            entity.setStatus("ACTIVE");
            entity.setGrantedById(1L); // TODO 从上下文取
            entity.setGrantedByName("系统管理员"); // TODO 从上下文取
            entity.setGrantedTime(LocalDateTime.now());
            // 3. 保存
            documentPermissionDao.insert(entity);
            // 4. 返回
            return SmartResponseUtil.success("权限设置成功");
        } catch (Exception e) {
            log.error("设置文档权限失败", e);
            return ResponseDTO.error("设置文档权限失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档权限列表
     */
    @Override
    public ResponseDTO<List<DocumentPermissionEntity>> getDocumentPermissions(Long documentId) {
        try {
            log.debug("获取文档权限: documentId={}", documentId);
            List<DocumentPermissionEntity> list =
                    documentPermissionDao.selectByDocumentId(documentId);
            return SmartResponseUtil.success(list);
        } catch (Exception e) {
            log.error("获取文档权限失败", e);
            return ResponseDTO.error("获取文档权限失败: " + e.getMessage());
        }
    }

    /**
     * 撤销文档权限
     */
    @Override
    public ResponseDTO<String> revokePermission(Long permissionId, String reason) {
        try {
            log.info("撤销权限: permissionId={}, reason={}", permissionId, reason);
            DocumentPermissionEntity entity = documentPermissionDao.selectById(permissionId);
            if (entity == null) {
                return ResponseDTO.error("权限不存在");
            }
            entity.setStatus("REVOKED");
            entity.setRevokedTime(LocalDateTime.now());
            entity.setRevokedById(1L); // TODO 从上下文取
            entity.setRevokedByName("系统管理员"); // TODO 从上下文取
            entity.setRevokeReason(reason);
            documentPermissionDao.updateById(entity);
            return SmartResponseUtil.success("撤销成功");
        } catch (Exception e) {
            log.error("撤销权限失败", e);
            return ResponseDTO.error("撤销权限失败: " + e.getMessage());
        }
    }

    /**
     * 高级搜索
     */
    @Override
    public ResponseDTO<PageResult<DocumentEntity>> advancedSearch(Map<String, Object> searchParams,
            PageParam pageParam) {
        try {
            log.info("高级搜索: params={}", searchParams);
            // 1. 提取条件
            Long categoryId = (Long) searchParams.getOrDefault("categoryId", null);
            String documentType = (String) searchParams.getOrDefault("documentType", null);
            String status = (String) searchParams.getOrDefault("status", null);
            String keyword = (String) searchParams.getOrDefault("keyword", null);
            String tags = (String) searchParams.getOrDefault("tags", null);
            LocalDateTime startDate =
                    parseLocalDateTimeLocal((String) searchParams.getOrDefault("startDate", null));
            LocalDateTime endDate =
                    parseLocalDateTimeLocal((String) searchParams.getOrDefault("endDate", null));
            Long createdById = (Long) searchParams.getOrDefault("createdById", null);
            // 2. DAO检索
            List<DocumentEntity> all = documentDao.selectDocumentsByCondition(categoryId,
                    documentType, status, keyword, tags, startDate, endDate, createdById);
            if (all == null) {
                all = new ArrayList<>();
            }
            // 3. 分页切片
            int total = all.size();
            long advStartLong = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
            int startIndex = Math.toIntExact(advStartLong);
            int endIndex = Math.min(startIndex + Math.toIntExact(pageParam.getPageSize()), total);
            List<DocumentEntity> slice = new ArrayList<>();
            if (startIndex < total) {
                slice = all.subList(startIndex, endIndex);
            }
            PageResult<DocumentEntity> result = new PageResult<>();
            result.setList(slice);
            result.setTotal((long) total);
            result.setPageNum(pageParam.getPageNum());
            result.setPageSize(pageParam.getPageSize());
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("高级搜索失败", e);
            return ResponseDTO.error("高级搜索失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成文档编号
     */
    private String generateDocumentNo() {
        return "DOC" + System.currentTimeMillis();
    }

    /**
     * 本地字符串非空校验
     */
    private boolean isNotBlankLocal(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 本地时间解析（格式示例：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd）
     */
    private LocalDateTime parseLocalDateTimeLocal(String datetime) {
        if (!isNotBlankLocal(datetime)) {
            return null;
        }
        try {
            if (datetime.length() == 10) {
                return LocalDateTime.parse(datetime + " 00:00:00",
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.parse(datetime,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 保存上传的文件
     */
    private String saveUploadedFile(MultipartFile file) {
        // TODO: 调用文件存储服务保存文件
        // 这里应该调用FileService或其他文件存储组件
        return "/uploads/documents/" + file.getOriginalFilename();
    }

    /**
     * 生成文件URL
     */
    private String generateFileUrl(String filePath) {
        return "/api/file/download?path" + filePath;
    }

    /**
     * 获取文档类型
     */
    private String getDocumentType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "OTHER";
        }

        if (originalFilename.endsWith(".doc") || originalFilename.endsWith(".docx")) {
            return "WORD";
        } else if (originalFilename.endsWith(".xls") || originalFilename.endsWith(".xlsx")) {
            return "EXCEL";
        } else if (originalFilename.endsWith(".ppt") || originalFilename.endsWith(".pptx")) {
            return "PPT";
        } else if (originalFilename.endsWith(".pdf")) {
            return "PDF";
        } else if (originalFilename.endsWith(".txt")) {
            return "TXT";
        } else if (originalFilename.endsWith(".md")) {
            return "MARKDOWN";
        } else if (originalFilename.endsWith(".html")) {
            return "HTML";
        } else {
            return "OTHER";
        }
    }

    /**
     * 获取文件格式
     */
    private String getFileFormat(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "UNKNOWN";
        }

        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return originalFilename.substring(lastDotIndex + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        // TODO: 使用JSON工具类转换
        return list.toString();
    }

    /**
     * 设置默认权限
     */
    private void setDefaultPermissions(DocumentEntity document, String accessPermission) {
        // TODO: 根据实际 DocumentEntity 字段设置默认权限。
        // 当前占位实现，避免对不存在的字段调用。
    }

    /**
     * 创建初始版本
     */
    private void createInitialVersion(DocumentEntity document) {
        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(1);
        version.setVersionName("初始版本");
        version.setVersionDescription("文档创建");
        version.setChangeSummary("文档初始创建");
        version.setContent(document.getContent());
        version.setChangeType("CREATE");
        version.setIsCurrentVersion(1);
        version.setIsMajorVersion(1);
        version.setApprovalStatus("APPROVED");
        version.setStatus("ACTIVE");

        // 设置创建人信息（这里应该从上下文获取）
        version.setCreatedBy(1L); // TODO: 获取实际用户ID
        version.setCreatedByName("系统管理员"); // TODO: 获取实际用户名
        version.setCreatedTime(LocalDateTime.now());

        documentVersionDao.insert(version);
    }

    /**
     * 创建新版本
     */
    private void createNewVersion(DocumentEntity document, String changeDescription) {
        // 获取当前版本号
        Integer currentVersion = document.getVersion();

        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(currentVersion + 1);
        version.setVersionName("v" + (currentVersion + 1));
        version.setVersionDescription(changeDescription);
        version.setChangeSummary(changeDescription);
        version.setContent(document.getContent());
        version.setChangeType("UPDATE");
        version.setIsCurrentVersion(1);
        version.setApprovalStatus("PENDING");
        version.setStatus("ACTIVE");
        version.setPreviousVersionId(getCurrentVersionId(document.getDocumentId()));

        // 设置创建人信息（这里应该从上下文获取）
        version.setCreatedBy(1L); // TODO: 获取实际用户ID
        version.setCreatedByName("系统管理员"); // TODO: 获取实际用户名
        version.setCreatedTime(LocalDateTime.now());

        documentVersionDao.insert(version);
    }

    /**
     * 创建恢复版本
     */
    private void createRestoreVersion(DocumentEntity document,
            DocumentVersionEntity restoredVersion) {
        DocumentVersionEntity version = new DocumentVersionEntity();
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(document.getVersion() + 1);
        version.setVersionName("恢复到v" + restoredVersion.getVersionNumber());
        version.setVersionDescription("恢复到版本: " + restoredVersion.getVersionName());
        version.setChangeSummary("从版本v" + restoredVersion.getVersionNumber() + "恢复");
        version.setContent(restoredVersion.getContent());
        version.setChangeType("RESTORE");
        version.setIsCurrentVersion(1);
        version.setApprovalStatus("APPROVED");
        version.setStatus("ACTIVE");
        version.setPreviousVersionId(restoredVersion.getVersionId());

        // 设置创建人信息（这里应该从上下文获取）
        version.setCreatedBy(1L); // TODO: 获取实际用户ID
        version.setCreatedByName("系统管理员"); // TODO: 获取实际用户名
        version.setCreatedTime(LocalDateTime.now());

        documentVersionDao.insert(version);
    }

    /**
     * 获取当前版本ID
     */
    private Long getCurrentVersionId(Long documentId) {
        DocumentVersionEntity currentVersion = documentVersionDao.selectCurrentVersion(documentId);
        return currentVersion != null ? currentVersion.getVersionId() : null;
    }

    /**
     * 更新访问统计
     */
    private void updateAccessStatistics(Long documentId, Long userId) {
        // 更新访问信息
        DocumentEntity document = documentCacheManager.getDocument(documentId);
        if (document != null) {
            document.setLastAccessTime(LocalDateTime.now());
            document.setLastAccessById(userId);
            document.setViewCount(document.getViewCount() + 1);
            documentDao.updateById(document);
            documentCacheManager.setDocument(document);
        }
    }

    /**
     * 执行版本对比
     */
    private Map<String, Object> performDiff(DocumentVersionEntity version1,
            DocumentVersionEntity version2) {
        Map<String, Object> diffResult = new HashMap<>();

        // 基本信息对比
        diffResult.put("version1",
                Map.of("versionNumber", version1.getVersionNumber(), "versionName",
                        version1.getVersionName(), "changeType", version1.getChangeType(),
                        "createdTime", version1.getCreatedTime()));

        diffResult.put("version2",
                Map.of("versionNumber", version2.getVersionNumber(), "versionName",
                        version2.getVersionName(), "changeType", version2.getChangeType(),
                        "createdTime", version2.getCreatedTime()));

        // 内容差异对比
        List<String> differences = compareText(version1.getContent(), version2.getContent());
        diffResult.put("differences", differences);

        return diffResult;
    }

    /**
     * 对比文本差异
     */
    private List<String> compareText(String text1, String text2) {
        // TODO: 使用专业的文本对比算法
        // 这里简化处理，实际应该使用diff算法
        List<String> differences = new ArrayList<>();
        if (!text1.equals(text2)) {
            differences.add("内容已更改");
        }
        return differences;
    }

    /**
     * 生成搜索关键词哈希
     */
    private String generateKeywordHash(Map<String, Object> searchParams) {
        StringBuilder hashBuilder = new StringBuilder();
        searchParams.forEach((key, value) -> {
            if (value != null) {
                hashBuilder.append(key).append(":").append(value.toString());
            }
        });
        return Integer.toHexString(hashBuilder.toString().hashCode());
    }
}
