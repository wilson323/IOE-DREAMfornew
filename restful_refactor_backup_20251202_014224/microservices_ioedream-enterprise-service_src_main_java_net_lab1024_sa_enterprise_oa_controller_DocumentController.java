package net.lab1024.sa.enterprise.oa.controller;

import java.util.List;

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

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartPageUtil;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.enterprise.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.enterprise.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.enterprise.oa.document.domain.form.DocumentAddForm;
import net.lab1024.sa.enterprise.oa.document.domain.form.DocumentPermissionAddForm;
import net.lab1024.sa.enterprise.oa.document.domain.form.DocumentQueryForm;
import net.lab1024.sa.enterprise.oa.document.domain.form.DocumentUpdateForm;
import net.lab1024.sa.enterprise.oa.document.domain.vo.DocumentPermissionVO;
import net.lab1024.sa.enterprise.oa.document.domain.vo.DocumentVO;
import net.lab1024.sa.enterprise.oa.document.domain.vo.DocumentVersionVO;
import net.lab1024.sa.enterprise.oa.service.DocumentService;

/**
 * 文档管理控制器
 * 支持文档CRUD、版本控制、权限管理
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@SaCheckLogin
@SaCheckPermission("oa:document")
@RestController
@RequestMapping("/api/oa/document")
@Tag(name = "文档管理", description = "文档CRUD、版本控制、权限管理API")
public class DocumentController {

    @Resource
    private DocumentService documentService;

    @Operation(summary = "分页查询文档")
    @PostMapping("/query")
    public ResponseDTO<PageResult<DocumentVO>> queryDocuments(@RequestBody @Valid DocumentQueryForm queryForm) {
        try {
            log.info("分页查询文档，查询条件：{}", queryForm);
            PageResult<DocumentEntity> pageResult = documentService.queryDocuments(queryForm);
            PageResult<DocumentVO> voPageResult = SmartPageUtil.convert2PageResult(pageResult, DocumentVO.class);
            return ResponseDTO.ok(voPageResult);
        } catch (Exception e) {
            log.error("查询文档列表失败", e);
            return ResponseDTO.userErrorParam("查询文档列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取文档详情")
    @GetMapping("/{documentId}")
    public ResponseDTO<DocumentVO> getDocumentDetail(@Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            log.info("获取文档详情，ID：{}", documentId);
            DocumentEntity document = documentService.getDocumentDetail(documentId);
            DocumentVO documentVO = new DocumentVO();
            SmartBeanUtil.copyProperties(document, documentVO);
            documentVO.setAuthor(document.getCreatedByName());
            return ResponseDTO.ok(documentVO);
        } catch (Exception e) {
            log.error("获取文档详情失败，ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("获取文档详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "新增文档")
    @PostMapping("/add")
    public ResponseDTO<Long> addDocument(@RequestBody @Valid DocumentAddForm addForm) {
        try {
            log.info("新增文档，标题：{}", addForm.getTitle());
            Long documentId = documentService.createDocument(addForm);
            log.info("新增文档成功，ID：{}", documentId);
            return ResponseDTO.ok(documentId);
        } catch (Exception e) {
            log.error("新增文档失败", e);
            return ResponseDTO.userErrorParam("新增文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "上传文档文件")
    @PostMapping("/upload")
    public ResponseDTO<Long> uploadDocument(
            @Parameter(description = "文档标题", required = true) @RequestParam("title") String title,
            @Parameter(description = "文档分类ID", required = true) @RequestParam("categoryId") Long categoryId,
            @Parameter(description = "文档类型", required = true) @RequestParam("documentType") DocumentTypeEnum documentType,
            @Parameter(description = "文档标签", required = false) @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "文档文件", required = true) @RequestParam("file") MultipartFile file) {
        try {
            log.info("上传文档文件，标题：{}, 文件：{}", title, file.getOriginalFilename());
            Long documentId = documentService.uploadDocument(file, title, categoryId, tags, documentType);
            log.info("文档上传成功，ID：{}", documentId);
            return ResponseDTO.ok(documentId);
        } catch (Exception e) {
            log.error("上传文档失败", e);
            return ResponseDTO.userErrorParam("上传文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新文档")
    @PutMapping("/update")
    public ResponseDTO<String> updateDocument(@RequestBody @Valid DocumentUpdateForm updateForm) {
        try {
            log.info("更新文档，ID：{}", updateForm.getDocumentId());
            documentService.updateDocument(updateForm);
            log.info("更新文档成功，ID：{}", updateForm.getDocumentId());
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("更新文档失败，ID：{}", updateForm.getDocumentId(), e);
            return ResponseDTO.userErrorParam("更新文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除文档")
    @DeleteMapping("/{documentId}")
    public ResponseDTO<String> deleteDocument(@Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            log.info("删除文档，ID：{}", documentId);
            documentService.deleteDocument(documentId);
            log.info("删除文档成功，ID：{}", documentId);
            return ResponseDTO.ok("删除成功");
        } catch (Exception e) {
            log.error("删除文档失败，ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("删除文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量删除文档")
    @DeleteMapping("/batch")
    public ResponseDTO<String> batchDeleteDocuments(
            @Parameter(description = "文档ID列表") @RequestParam List<Long> documentIds) {
        try {
            log.info("批量删除文档，IDs：{}", documentIds);
            int count = documentService.batchDeleteDocuments(documentIds);
            log.info("批量删除文档成功，删除数量：{}", count);
            return ResponseDTO.ok("成功删除 " + count + " 个文档");
        } catch (Exception e) {
            log.error("批量删除文档失败，IDs：{}", documentIds, e);
            return ResponseDTO.userErrorParam("批量删除文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "下载文档")
    @GetMapping("/{documentId}/download")
    public ResponseDTO<String> downloadDocument(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @Parameter(description = "是否下载最新版本") @RequestParam(value = "latest", defaultValue = "true") Boolean latest) {
        try {
            log.info("下载文档，ID：{}，最新版本：{}", documentId, latest);
            String downloadUrl = documentService.downloadDocument(documentId, latest);
            return ResponseDTO.ok(downloadUrl);
        } catch (Exception e) {
            log.error("下载文档失败，ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("下载文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取文档版本列表")
    @GetMapping("/{documentId}/versions")
    public ResponseDTO<List<DocumentVersionVO>> getDocumentVersions(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            log.info("获取文档版本列表，文档ID：{}", documentId);
            List<DocumentVersionEntity> versions = documentService.getDocumentVersions(documentId);
            if (versions == null) {
                versions = List.of();
            }
            List<DocumentVersionVO> versionVOs = SmartBeanUtil.copyList(versions, DocumentVersionVO.class);
            return ResponseDTO.ok(versionVOs);
        } catch (Exception e) {
            log.error("获取文档版本列表失败，文档ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("获取文档版本列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取文档权限列表")
    @GetMapping("/{documentId}/permissions")
    public ResponseDTO<List<DocumentPermissionVO>> getDocumentPermissions(
            @Parameter(description = "文档ID") @PathVariable Long documentId) {
        try {
            log.info("获取文档权限列表，文档ID：{}", documentId);
            List<DocumentPermissionEntity> permissions = documentService.getDocumentPermissions(documentId);
            if (permissions == null) {
                permissions = List.of();
            }
            List<DocumentPermissionVO> permissionVOs = SmartBeanUtil.copyList(permissions, DocumentPermissionVO.class);
            return ResponseDTO.ok(permissionVOs);
        } catch (Exception e) {
            log.error("获取文档权限列表失败，文档ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("获取文档权限列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "添加文档权限")
    @PostMapping("/{documentId}/permissions")
    public ResponseDTO<String> addDocumentPermission(
            @Parameter(description = "文档ID") @PathVariable Long documentId,
            @RequestBody @Valid DocumentPermissionAddForm addForm) {
        try {
            log.info("添加文档权限，文档ID：{}", documentId);
            documentService.addDocumentPermission(documentId, addForm);
            log.info("添加文档权限成功，文档ID：{}", documentId);
            return ResponseDTO.ok("权限添加成功");
        } catch (Exception e) {
            log.error("添加文档权限失败，文档ID：{}", documentId, e);
            return ResponseDTO.userErrorParam("添加文档权限失败：" + e.getMessage());
        }
    }

    @Operation(summary = "搜索文档")
    @PostMapping("/search")
    public ResponseDTO<PageResult<DocumentVO>> searchDocuments(@RequestBody DocumentQueryForm searchForm) {
        try {
            log.info("搜索文档，关键词：{}", searchForm.getKeyword());
            PageResult<DocumentEntity> searchResult = documentService.searchDocuments(searchForm);
            PageResult<DocumentVO> voPageResult = SmartPageUtil.convert2PageResult(searchResult, DocumentVO.class);
            return ResponseDTO.ok(voPageResult);
        } catch (Exception e) {
            log.error("搜索文档失败", e);
            return ResponseDTO.userErrorParam("搜索文档失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取我的文档")
    @GetMapping("/my")
    public ResponseDTO<PageResult<DocumentVO>> getMyDocuments(
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            log.info("获取我的文档，页码：{}，页大小：{}", pageNum, pageSize);
            PageResult<DocumentEntity> myDocuments = documentService.getMyDocuments(pageNum, pageSize);
            PageResult<DocumentVO> voPageResult = SmartPageUtil.convert2PageResult(myDocuments, DocumentVO.class);
            return ResponseDTO.ok(voPageResult);
        } catch (Exception e) {
            log.error("获取我的文档失败", e);
            return ResponseDTO.userErrorParam("获取我的文档失败：" + e.getMessage());
        }
    }
}
