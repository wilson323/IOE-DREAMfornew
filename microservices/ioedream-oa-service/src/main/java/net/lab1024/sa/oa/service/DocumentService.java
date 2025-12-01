package net.lab1024.sa.oa.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.oa.document.domain.entity.DocumentEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentPermissionEntity;
import net.lab1024.sa.oa.document.domain.entity.DocumentVersionEntity;
import net.lab1024.sa.oa.document.domain.enums.DocumentTypeEnum;
import net.lab1024.sa.oa.document.domain.form.DocumentAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentPermissionAddForm;
import net.lab1024.sa.oa.document.domain.form.DocumentQueryForm;
import net.lab1024.sa.oa.document.domain.form.DocumentUpdateForm;

/**
 * 文档管理服务接口
 *
 * @author IOE-DREAM Team
 */
public interface DocumentService {

    /**
     * 分页查询文档
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<DocumentEntity> queryDocuments(DocumentQueryForm queryForm);

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @return 文档详情
     */
    DocumentEntity getDocumentDetail(Long documentId);

    /**
     * 创建文档
     *
     * @param addForm 文档信息
     * @return 文档ID
     */
    Long createDocument(DocumentAddForm addForm);

    /**
     * 上传文档文件
     *
     * @param file         文件
     * @param title        标题
     * @param categoryId   分类ID
     * @param tags         标签
     * @param documentType 文档类型
     * @return 文档ID
     */
    Long uploadDocument(MultipartFile file, String title, Long categoryId, String tags, DocumentTypeEnum documentType);

    /**
     * 更新文档
     *
     * @param updateForm 更新信息
     */
    void updateDocument(DocumentUpdateForm updateForm);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     */
    void deleteDocument(Long documentId);

    /**
     * 批量删除文档
     *
     * @param documentIds 文档ID列表
     * @return 删除数量
     */
    int batchDeleteDocuments(List<Long> documentIds);

    /**
     * 下载文档
     *
     * @param documentId 文档ID
     * @param latest     是否最新版本
     * @return 下载URL
     */
    String downloadDocument(Long documentId, Boolean latest);

    /**
     * 获取文档版本列表
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    List<DocumentVersionEntity> getDocumentVersions(Long documentId);

    /**
     * 获取文档权限列表
     *
     * @param documentId 文档ID
     * @return 权限列表
     */
    List<DocumentPermissionEntity> getDocumentPermissions(Long documentId);

    /**
     * 添加文档权限
     *
     * @param documentId 文档ID
     * @param addForm    权限信息
     */
    void addDocumentPermission(Long documentId, DocumentPermissionAddForm addForm);

    /**
     * 搜索文档
     *
     * @param searchForm 搜索条件
     * @return 搜索结果
     */
    PageResult<DocumentEntity> searchDocuments(DocumentQueryForm searchForm);

    /**
     * 获取我的文档
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 我的文档
     */
    PageResult<DocumentEntity> getMyDocuments(Integer pageNum, Integer pageSize);
}
