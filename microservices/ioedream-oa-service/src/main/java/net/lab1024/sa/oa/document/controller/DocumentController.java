package net.lab1024.sa.oa.document.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.common.annotation.SaCheckLogin;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import net.lab1024.sa.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.oa.document.domain.form.DocumentAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentPermissionAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentPermissionUpdateForm;
import net.lab1024.sa.oa.document.domain.form.DocumentQueryForm;
import net.lab1024.sa.oa.document.domain.form.DocumentSearchForm;
import net.lab1024.sa.oa.document.domain.form.DocumentUpdateForm;
import net.lab1024.sa.oa.document.domain.form.DocumentVersionCreateForm;
import net.lab1024.sa.oa.document.domain.vo.DocumentPermissionVO;
import net.lab1024.sa.oa.document.domain.vo.DocumentVO;
import net.lab1024.sa.oa.document.domain.vo.DocumentVersionVO;
import net.lab1024.sa.oa.document.service.DocumentService;

/**
 * 文档管理控制器
 * 提供文档CRUD、版本控制、权限管理等API
 */
@RestController
@RequestMapping("/api/oa/document")
@Tag(name = "文档管理", description = "文档CRUD、版本控制、权限管理API")
@SaCheckLogin
public class DocumentController {

    @Resource
    private DocumentService documentService;

    // ==================== 文档基础操作 ====================

    @Operation(summary = "分页查询文档")
    @PostMapping("/query")
    @SaCheckPermission("oa:document:query")
    public ResponseDTO<PageResult<DocumentVO>> queryDocument(
            @Valid @RequestBody DocumentQueryForm queryForm) {
        PageParam page = new PageParam();
        page.setPageNum(queryForm.getPageNum() == null ? 1L : queryForm.getPageNum().longValue());
        page.setPageSize(queryForm.getPageSize() == null ? 20L : queryForm.getPageSize().longValue());

        String start = null;
        String end = null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (queryForm.getStartDate() != null) {
            start = queryForm.getStartDate().format(dtf);
        }
        if (queryForm.getEndDate() != null) {
            end = queryForm.getEndDate().format(dtf);
        }

        ResponseDTO<PageResult<DocumentEntity>> resp = documentService.pageDocuments(page, queryForm.getCategoryId(),
                queryForm.getDocumentType(), queryForm.getStatus(), queryForm.getKeyword(),
                queryForm.getTags(), start, end);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        PageResult<DocumentVO> voPage = new PageResult<>();
        voPage.setTotal(resp.getData().getTotal());
        voPage.setPageNum(resp.getData().getPageNum());
        voPage.setPageSize(resp.getData().getPageSize());
        List<DocumentVO> voList = new ArrayList<>();
        for (DocumentEntity e : resp.getData().getList()) {
            DocumentVO vo = new DocumentVO();
            vo.setDocumentId(e.getDocumentId());
            vo.setTitle(e.getTitle());
            vo.setDocumentType(e.getDocumentType());
            vo.setStatus(e.getStatus());
            vo.setCategoryId(e.getCategoryId());
            vo.setAuthor(e.getCreatedByName());
            vo.setCreateTime(e.getCreateTime());
            voList.add(vo);
        }
        voPage.setList(voList);
        return ResponseDTO.ok(voPage);
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{documentId}")
    @SaCheckPermission("oa:document:detail")
    public ResponseDTO<DocumentVO> getDocumentDetail(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        ResponseDTO<DocumentEntity> resp = documentService.getDocument(documentId, null);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        DocumentEntity e = resp.getData();
        DocumentVO vo = new DocumentVO();
        if (e != null) {
            vo.setDocumentId(e.getDocumentId());
            vo.setTitle(e.getTitle());
            vo.setDocumentType(e.getDocumentType());
            vo.setStatus(e.getStatus());
            vo.setCategoryId(e.getCategoryId());
            vo.setAuthor(e.getCreatedByName());
            vo.setCreateTime(e.getCreateTime());
        }
        return ResponseDTO.ok(vo);
    }

    @Operation(summary = "新增文档")
    @PostMapping("/add")
    @SaCheckPermission("oa:document:add")
    public ResponseDTO<String> addDocument(@Valid @RequestBody DocumentAddForm addForm) {
        ResponseDTO<Long> resp = documentService.createDocument(addForm.getTitle(),
                addForm.getContent(), addForm.getDocumentType(), addForm.getCategoryId(),
                null, null, addForm.getAccessLevel());
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok(String.valueOf(resp.getData()));
    }

    @Operation(summary = "上传文档文件")
    @PostMapping("/upload")
    @SaCheckPermission("oa:document:upload")
    public ResponseDTO<String> uploadDocument(
            @Parameter(description = "文档标题", required = true) @RequestParam("title") String title,
            @Parameter(description = "文档分类ID", required = true) @RequestParam("categoryId") Long categoryId,
            @Parameter(description = "文档类型", required = true) @RequestParam("documentType") DocumentTypeEnum documentType,
            @Parameter(description = "文档标签", required = false) @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "文档文件", required = true) @RequestParam("file") MultipartFile file) {
        List<String> tagList = new ArrayList<>();
        if (tags != null && !tags.trim().isEmpty()) {
            for (String t : tags.split(",")) {
                if (t != null && !t.trim().isEmpty()) {
                    tagList.add(t.trim());
                }
            }
        }
        ResponseDTO<Long> resp = documentService.uploadDocument(file, title, categoryId, tagList, null, "PRIVATE");
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok(String.valueOf(resp.getData()));
    }

    @Operation(summary = "更新文档")
    @PutMapping("/update")
    @SaCheckPermission("oa:document:update")
    public ResponseDTO<String> updateDocument(@Valid @RequestBody DocumentUpdateForm updateForm) {
        ResponseDTO<String> resp = documentService.updateDocument(updateForm.getDocumentId(), updateForm.getTitle(),
                updateForm.getContent(), null, null);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok("OK");
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{documentId}")
    @SaCheckPermission("oa:document:delete")
    public ResponseDTO<String> deleteDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        ResponseDTO<String> resp = documentService.deleteDocument(documentId, null, "API删除");
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok("OK");
    }

    @Operation(summary = "批量删除文档")
    @DeleteMapping("/batch")
    @SaCheckPermission("oa:document:delete")
    public ResponseDTO<String> batchDeleteDocuments(
            @Parameter(description = "文档ID列表") @RequestParam List<Long> documentIds) {
        ResponseDTO<String> resp = documentService.batchDeleteDocuments(documentIds, null);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok("OK");
    }

    // ==================== 文档内容操作 ====================

    @Operation(summary = "获取文档内容")
    @GetMapping("/{documentId}/content")
    @SaCheckPermission("oa:document:view")
    public ResponseDTO<Object> getDocumentContent(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        return ResponseDTO.ok(null);
    }

    @Operation(summary = "预览文档")
    @GetMapping("/{documentId}/preview")
    @SaCheckPermission("oa:document:view")
    public ResponseDTO<String> previewDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        return ResponseDTO.ok("暂未实现");
    }

    @Operation(summary = "下载文档")
    @GetMapping("/{documentId}/download")
    @SaCheckPermission("oa:document:download")
    public ResponseDTO<String> downloadDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "是否下载最新版本") @RequestParam(value = "latest", defaultValue = "true") Boolean latest) {
        ResponseDTO<String> resp = documentService.downloadDocument(documentId, null);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok(resp.getData());
    }

    // ==================== 版本管理 ====================

    @Operation(summary = "获取文档版本列表")
    @GetMapping("/{documentId}/versions")
    @SaCheckPermission("oa:document:version")
    public ResponseDTO<List<DocumentVersionVO>> getDocumentVersions(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        ResponseDTO<List<DocumentVersionEntity>> resp = documentService.getDocumentVersions(documentId);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        List<DocumentVersionVO> list = new ArrayList<>();
        for (DocumentVersionEntity e : resp.getData()) {
            DocumentVersionVO vo = new DocumentVersionVO();
            vo.setVersionId(e.getVersionId());
            vo.setDocumentId(e.getDocumentId());
            vo.setVersionNumber(e.getVersionNumber());
            vo.setChangeType(e.getChangeType());
            vo.setCreateTime(e.getCreatedTime());
            list.add(vo);
        }
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "获取版本详情")
    @GetMapping("/{documentId}/versions/{versionId}")
    @SaCheckPermission("oa:document:version")
    public ResponseDTO<DocumentVersionVO> getVersionDetail(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        ResponseDTO<DocumentVersionEntity> resp = documentService.getVersionDetail(versionId);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        DocumentVersionEntity e = resp.getData();
        DocumentVersionVO vo = new DocumentVersionVO();
        if (e != null) {
            vo.setVersionId(e.getVersionId());
            vo.setDocumentId(e.getDocumentId());
            vo.setVersionNumber(e.getVersionNumber());
            vo.setChangeType(e.getChangeType());
            vo.setCreateTime(e.getCreatedTime());
        }
        return ResponseDTO.ok(vo);
    }

    @Operation(summary = "创建新版本")
    @PostMapping("/{documentId}/versions")
    @SaCheckPermission("oa:document:version:create")
    public ResponseDTO<String> createVersion(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Valid @RequestBody DocumentVersionCreateForm createForm) {
        return ResponseDTO.ok("暂未实现");
    }

    @Operation(summary = "版本对比")
    @GetMapping("/{documentId}/versions/compare")
    @SaCheckPermission("oa:document:version:compare")
    public ResponseDTO<Map<String, Object>> compareVersions(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "源版本ID") @RequestParam("source") Long sourceVersionId,
            @Parameter(description = "目标版本ID") @RequestParam("target") Long targetVersionId) {
        ResponseDTO<Map<String, Object>> resp = documentService.compareVersions(sourceVersionId, targetVersionId);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok(resp.getData());
    }

    @Operation(summary = "恢复到指定版本")
    @PostMapping("/{documentId}/versions/{versionId}/restore")
    @SaCheckPermission("oa:document:version:restore")
    public ResponseDTO<String> restoreVersion(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "版本ID") @PathVariable Long versionId) {
        ResponseDTO<String> resp = documentService.restoreToVersion(documentId, versionId);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok("OK");
    }

    // ==================== 权限管理 ====================

    @Operation(summary = "获取文档权限列表")
    @GetMapping("/{documentId}/permissions")
    @SaCheckPermission("oa:document:permission:view")
    public ResponseDTO<List<DocumentPermissionVO>> getDocumentPermissions(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        ResponseDTO<List<DocumentPermissionEntity>> resp = documentService.getDocumentPermissions(documentId);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        List<DocumentPermissionVO> list = new ArrayList<>();
        for (DocumentPermissionEntity e : resp.getData()) {
            DocumentPermissionVO vo = new DocumentPermissionVO();
            vo.setPermissionId(e.getPermissionId());
            vo.setDocumentId(e.getDocumentId());
            // 映射精简字段，占位：roleCode/accessLevel 由实际权限模型决定
            vo.setRoleCode(e.getPermissionType());
            vo.setAccessLevel("custom");
            list.add(vo);
        }
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "添加文档权限")
    @PostMapping("/{documentId}/permissions")
    @SaCheckPermission("oa:document:permission:add")
    public ResponseDTO<String> addDocumentPermission(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Valid @RequestBody DocumentPermissionAddForm addForm) {
        return ResponseDTO.ok("暂未实现");
    }

    @Operation(summary = "更新文档权限")
    @PutMapping("/{documentId}/permissions/{permissionId}")
    @SaCheckPermission("oa:document:permission:update")
    public ResponseDTO<String> updateDocumentPermission(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "权限ID") @PathVariable Long permissionId,
            @Valid @RequestBody DocumentPermissionUpdateForm updateForm) {
        return ResponseDTO.ok("暂未实现");
    }

    @Operation(summary = "撤销文档权限")
    @DeleteMapping("/{documentId}/permissions/{permissionId}")
    @SaCheckPermission("oa:document:permission:delete")
    public ResponseDTO<String> revokeDocumentPermission(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "权限ID") @PathVariable Long permissionId,
            @Parameter(description = "撤销原因") @RequestParam(value = "reason", required = false) String reason) {
        ResponseDTO<String> resp = documentService.revokePermission(permissionId, reason);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok("OK");
    }

    // ==================== 搜索和统计 ====================

    @Operation(summary = "搜索文档")
    @PostMapping("/search")
    @SaCheckPermission("oa:document:search")
    public ResponseDTO<PageResult<DocumentVO>> searchDocuments(
            @Valid @RequestBody DocumentSearchForm searchForm) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", searchForm.getKeyword());
        params.put("categoryId", searchForm.getCategoryId());
        params.put("documentType", searchForm.getDocumentType());
        params.put("tags", searchForm.getTags());
        params.put("startDate", searchForm.getStartDate());
        params.put("endDate", searchForm.getEndDate());
        PageParam page = new PageParam();
        page.setPageNum(searchForm.getPageNum() == null ? 1L : searchForm.getPageNum().longValue());
        page.setPageSize(searchForm.getPageSize() == null ? 20L : searchForm.getPageSize().longValue());
        ResponseDTO<PageResult<DocumentEntity>> resp = documentService.advancedSearch(params, page);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        PageResult<DocumentVO> voPage = new PageResult<>();
        voPage.setTotal(resp.getData().getTotal());
        voPage.setPageNum(resp.getData().getPageNum());
        voPage.setPageSize(resp.getData().getPageSize());
        List<DocumentVO> voList = new ArrayList<>();
        for (DocumentEntity e : resp.getData().getList()) {
            DocumentVO vo = new DocumentVO();
            vo.setDocumentId(e.getDocumentId());
            vo.setTitle(e.getTitle());
            vo.setDocumentType(e.getDocumentType());
            vo.setStatus(e.getStatus());
            vo.setCategoryId(e.getCategoryId());
            vo.setAuthor(e.getCreatedByName());
            vo.setCreateTime(e.getCreateTime());
            voList.add(vo);
        }
        voPage.setList(voList);
        return ResponseDTO.ok(voPage);
    }

    @Operation(summary = "获取文档统计")
    @GetMapping("/statistics")
    @SaCheckPermission("oa:document:statistics")
    public ResponseDTO<Map<String, Object>> getDocumentStatistics(
            @Parameter(description = "分类ID") @RequestParam(value = "categoryId", required = false) Long categoryId) {
        ResponseDTO<Map<String, Object>> resp = documentService.getDocumentStatistics(categoryId, null, null);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        return ResponseDTO.ok(resp.getData());
    }

    @Operation(summary = "获取我的文档")
    @GetMapping("/my")
    @SaCheckPermission("oa:document:my")
    public ResponseDTO<PageResult<DocumentVO>> getMyDocuments(
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
            @Parameter(description = "状态") @RequestParam(value = "status", required = false) String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        // 从登录上下文获取当前用户ID
        Long userId = SmartRequestUtil.getRequestUserId();
        if (userId != null) {
            params.put("createdById", userId);
        }
        PageParam page = new PageParam();
        page.setPageNum(pageNum.longValue());
        page.setPageSize(pageSize.longValue());
        ResponseDTO<PageResult<DocumentEntity>> resp = documentService.advancedSearch(params, page);
        if (!resp.getOk()) {
            return ResponseDTO.error(resp.getMsg());
        }
        PageResult<DocumentVO> voPage = new PageResult<>();
        voPage.setTotal(resp.getData().getTotal());
        voPage.setPageNum(resp.getData().getPageNum());
        voPage.setPageSize(resp.getData().getPageSize());
        List<DocumentVO> voList = new ArrayList<>();
        for (DocumentEntity e : resp.getData().getList()) {
            DocumentVO vo = new DocumentVO();
            vo.setDocumentId(e.getDocumentId());
            vo.setTitle(e.getTitle());
            vo.setDocumentType(e.getDocumentType());
            vo.setStatus(e.getStatus());
            vo.setCategoryId(e.getCategoryId());
            vo.setAuthor(e.getCreatedByName());
            vo.setCreateTime(e.getCreateTime());
            voList.add(vo);
        }
        voPage.setList(voList);
        return ResponseDTO.ok(voPage);
    }

    // ==================== 批量操作 ====================

    @Operation(summary = "批量更新文档状态")
    @PutMapping("/batch/status")
    @SaCheckPermission("oa:document:batch:update")
    public ResponseDTO<String> batchUpdateStatus(
            @Parameter(description = "文档ID列表") @RequestParam List<Long> documentIds,
            @Parameter(description = "新状态") @RequestParam String status) {
        return ResponseDTO.ok("暂未实现");
    }

    @Operation(summary = "批量移动文档分类")
    @PutMapping("/batch/category")
    @SaCheckPermission("oa:document:batch:update")
    public ResponseDTO<String> batchUpdateCategory(
            @Parameter(description = "文档ID列表") @RequestParam List<Long> documentIds,
            @Parameter(description = "新分类ID") @RequestParam Long categoryId) {
        return ResponseDTO.ok("暂未实现");
    }
}
