package net.lab1024.sa.oa.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.oa.dao.DocumentDao;
import net.lab1024.sa.oa.dao.DocumentPermissionDao;
import net.lab1024.sa.oa.dao.DocumentVersionDao;
import net.lab1024.sa.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.oa.document.domain.form.DocumentAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentPermissionAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentQueryForm;
import net.lab1024.sa.oa.document.domain.form.DocumentUpdateForm;
import net.lab1024.sa.oa.manager.DocumentManager;
import net.lab1024.sa.oa.service.DocumentService;

/**
 * 文档管理服务实现
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Resource
    private DocumentDao documentDao;

    @Resource
    private DocumentVersionDao documentVersionDao;

    @Resource
    private DocumentPermissionDao documentPermissionDao;

    @Resource
    private DocumentManager documentManager;

    @Override
    public PageResult<DocumentEntity> queryDocuments(DocumentQueryForm queryForm) {
        Page<DocumentEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());

        LambdaQueryWrapper<DocumentEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (queryForm.getCategoryId() != null) {
            queryWrapper.eq(DocumentEntity::getCategoryId, queryForm.getCategoryId());
        }
        if (queryForm.getDocumentType() != null) {
            queryWrapper.eq(DocumentEntity::getDocumentType, queryForm.getDocumentType());
        }
        if (queryForm.getStatus() != null) {
            queryWrapper.eq(DocumentEntity::getStatus, queryForm.getStatus());
        }
        if (queryForm.getKeyword() != null && !queryForm.getKeyword().trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(DocumentEntity::getTitle, queryForm.getKeyword())
                    .or()
                    .like(DocumentEntity::getContent, queryForm.getKeyword()));
        }

        queryWrapper.orderByDesc(DocumentEntity::getCreateTime);

        Page<DocumentEntity> result = documentDao.selectPage(page, queryWrapper);
        return SmartPageUtil.convert2PageResult(result, result.getRecords());
    }

    @Override
    public DocumentEntity getDocumentDetail(Long documentId) {
        return documentDao.selectById(documentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDocument(DocumentAddForm addForm) {
        DocumentEntity entity = new DocumentEntity();
        entity.setTitle(addForm.getTitle());
        entity.setContent(addForm.getContent());
        entity.setDocumentType(addForm.getDocumentType());
        entity.setCategoryId(addForm.getCategoryId());
        entity.setStatus("DRAFT");
        entity.setAccessPermission(addForm.getAccessLevel());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        // 设置创建者信息
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            entity.setCreatedById(userId);
            entity.setCreateUserId(userId);
        }

        documentDao.insert(entity);

        // 创建初始版本
        documentManager.createInitialVersion(entity);

        return entity.getDocumentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadDocument(MultipartFile file, String title, Long categoryId, String tags,
            DocumentTypeEnum documentType) {
        // 处理文件上传
        String fileUrl = documentManager.uploadFile(file);

        DocumentEntity entity = new DocumentEntity();
        entity.setTitle(title);
        // 设置文档类型（枚举值转换为字符串）
        entity.setDocumentType(documentType != null ? (String) documentType.getValue() : null);
        entity.setCategoryId(categoryId);
        entity.setFileUrl(fileUrl);
        entity.setFileSize(file.getSize());
        entity.setStatus("PUBLISHED");
        entity.setTags(tags);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        // 文件名存储在title中或作为备注
        if (title == null || title.isEmpty()) {
            entity.setTitle(file.getOriginalFilename());
        }
        // 设置创建者信息
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            entity.setCreatedById(userId);
            entity.setCreateUserId(userId);
        }

        documentDao.insert(entity);

        // 创建初始版本
        documentManager.createInitialVersion(entity);

        return entity.getDocumentId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDocument(DocumentUpdateForm updateForm) {
        DocumentEntity entity = documentDao.selectById(updateForm.getDocumentId());
        if (entity == null) {
            throw new RuntimeException("文档不存在");
        }

        // 创建版本快照
        documentManager.createVersionSnapshot(entity);

        // 更新文档信息
        LambdaUpdateWrapper<DocumentEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocumentEntity::getDocumentId, updateForm.getDocumentId())
                .set(updateForm.getTitle() != null, DocumentEntity::getTitle, updateForm.getTitle())
                .set(updateForm.getContent() != null, DocumentEntity::getContent, updateForm.getContent())
                .set(DocumentEntity::getUpdateTime, LocalDateTime.now());

        documentDao.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long documentId) {
        // 软删除文档
        LambdaUpdateWrapper<DocumentEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DocumentEntity::getDocumentId, documentId)
                .set(DocumentEntity::getStatus, "DELETED")
                .set(DocumentEntity::getUpdateTime, LocalDateTime.now());

        documentDao.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteDocuments(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return 0;
        }

        LambdaUpdateWrapper<DocumentEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(DocumentEntity::getDocumentId, documentIds)
                .set(DocumentEntity::getStatus, "DELETED")
                .set(DocumentEntity::getUpdateTime, LocalDateTime.now());

        return documentDao.update(null, updateWrapper);
    }

    @Override
    public String downloadDocument(Long documentId, Boolean latest) {
        DocumentEntity document = documentDao.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        return documentManager.generateDownloadUrl(document, latest);
    }

    @Override
    public List<DocumentVersionEntity> getDocumentVersions(Long documentId) {
        LambdaQueryWrapper<DocumentVersionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentVersionEntity::getDocumentId, documentId)
                .orderByDesc(DocumentVersionEntity::getCreateTime);

        return documentVersionDao.selectList(queryWrapper);
    }

    @Override
    public List<DocumentPermissionEntity> getDocumentPermissions(Long documentId) {
        LambdaQueryWrapper<DocumentPermissionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DocumentPermissionEntity::getDocumentId, documentId)
                .orderByDesc(DocumentPermissionEntity::getCreateTime);

        return documentPermissionDao.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDocumentPermission(Long documentId, DocumentPermissionAddForm addForm) {
        DocumentPermissionEntity entity = new DocumentPermissionEntity();
        entity.setDocumentId(documentId);
        entity.setPermissionType(addForm.getPermissionType());
        entity.setTargetId(addForm.getTargetId());
        entity.setCreateTime(LocalDateTime.now());
        // 设置授权人信息（权限的创建者就是授权人）
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            entity.setGrantedById(userId);
            entity.setCreateUserId(userId);
            entity.setGrantedTime(LocalDateTime.now());
        }

        documentPermissionDao.insert(entity);
    }

    @Override
    public PageResult<DocumentEntity> searchDocuments(DocumentQueryForm searchForm) {
        Page<DocumentEntity> page = new Page<>(searchForm.getPageNum(), searchForm.getPageSize());

        LambdaQueryWrapper<DocumentEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (searchForm.getKeyword() != null && !searchForm.getKeyword().trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like(DocumentEntity::getTitle, searchForm.getKeyword())
                    .or()
                    .like(DocumentEntity::getContent, searchForm.getKeyword())
                    .or()
                    .like(DocumentEntity::getTags, searchForm.getKeyword()));
        }

        queryWrapper.eq(DocumentEntity::getStatus, "PUBLISHED")
                .orderByDesc(DocumentEntity::getUpdateTime);

        Page<DocumentEntity> result = documentDao.selectPage(page, queryWrapper);
        return SmartPageUtil.convert2PageResult(result, result.getRecords());
    }

    @Override
    public PageResult<DocumentEntity> getMyDocuments(Integer pageNum, Integer pageSize) {
        Page<DocumentEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<DocumentEntity> queryWrapper = new LambdaQueryWrapper<>();
        // 设置当前用户ID过滤条件
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            queryWrapper.eq(DocumentEntity::getCreatedById, userId);
        }
        queryWrapper.ne(DocumentEntity::getStatus, "DELETED")
                .orderByDesc(DocumentEntity::getUpdateTime);

        Page<DocumentEntity> result = documentDao.selectPage(page, queryWrapper);
        return SmartPageUtil.convert2PageResult(result, result.getRecords());
    }
}
