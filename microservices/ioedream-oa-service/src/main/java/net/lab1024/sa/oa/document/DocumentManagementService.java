package net.lab1024.sa.oa.document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档管理服务
 * 提供文档上传、下载、版本控制、权限管理等功能
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class DocumentManagementService {

    /**
     * 文档类型枚举
     */
    public enum DocumentType {
        CONTRACT("CONTRACT", "合同文档"),
        REPORT("REPORT", "报告文档"),
        PROPOSAL("PROPOSAL", "提案文档"),
        MEETING("MEETING", "会议文档"),
        POLICY("POLICY", "政策文档"),
        MANUAL("MANUAL", "手册文档"),
        DESIGN("DESIGN", "设计文档"),
        TECHNICAL("TECHNICAL", "技术文档"),
        FINANCIAL("FINANCIAL", "财务文档"),
        LEGAL("LEGAL", "法律文档"),
        PERSONNEL("PERSONNEL", "人事文档"),
        OTHER("OTHER", "其他文档");

        private final String code;
        private final String description;

        DocumentType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 文档状态枚举
     */
    public enum DocumentStatus {
        DRAFT("DRAFT", "草稿"),
        PENDING_REVIEW("PENDING_REVIEW", "待审核"),
        APPROVED("APPROVED", "已批准"),
        REJECTED("REJECTED", "已拒绝"),
        PUBLISHED("PUBLISHED", "已发布"),
        ARCHIVED("ARCHIVED", "已归档"),
        DELETED("DELETED", "已删除");

        private final String code;
        private final String description;

        DocumentStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 访问权限级别
     */
    public enum AccessLevel {
        PUBLIC("PUBLIC", "公开"),
        INTERNAL("INTERNAL", "内部"),
        CONFIDENTIAL("CONFIDENTIAL", "机密"),
        SECRET("SECRET", "绝密");

        private final String code;
        private final String description;

        AccessLevel(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 文档实体
     */
    @Data
    public static class Document {
        private Long documentId;
        private String documentNumber; // 文档编号
        private String title; // 文档标题
        private String description; // 文档描述
        private DocumentType documentType; // 文档类型
        private DocumentStatus status; // 文档状态
        private AccessLevel accessLevel; // 访问级别
        private Long authorId; // 创建者ID
        private String authorName; // 创建者姓名
        private String departmentId; // 部门ID
        private String departmentName; // 部门名称
        private LocalDateTime createTime; // 创建时间
        private LocalDateTime lastUpdateTime; // 最后更新时间
        private LocalDateTime publishTime; // 发布时间
        private LocalDateTime expireTime; // 过期时间
        private String filePath; // 文件路径
        private String fileName; // 文件名
        private String fileExtension; // 文件扩展名
        private Long fileSize; // 文件大小(字节)
        private String mimeType; // MIME类型
        private String checksum; // 文件校验和
        private Integer version; // 版本号
        private String versionDescription; // 版本描述
        private Long parentDocumentId; // 父文档ID(用于版本管理)
        private List<String> tags; // 标签
        private List<Long> attachments; // 附件ID列表
        private Map<String, Object> metadata; // 元数据
        private List<String> viewers; // 查看者列表
        private List<String> editors; // 编辑者列表
        private String approvalWorkflowId; // 审批工作流ID
        private Boolean isTemplate; // 是否模板
        private Boolean isArchived; // 是否已归档
        private String archiveLocation; // 归档位置
        private Integer downloadCount; // 下载次数
        private LocalDateTime lastDownloadTime; // 最后下载时间
    }

    /**
     * 文档版本信息
     */
    @Data
    public static class DocumentVersion {
        private Long versionId;
        private Long documentId;
        private Integer versionNumber;
        private String versionDescription;
        private Long authorId;
        private String authorName;
        private LocalDateTime createTime;
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String checksum;
        private List<String> changeLog; // 变更记录
        private Boolean isCurrent; // 是否当前版本
        private Map<String, Object> metadata;
    }

    /**
     * 文档上传请求
     */
    @Data
    public static class DocumentUploadRequest {
        private String title;
        private String description;
        private DocumentType documentType;
        private AccessLevel accessLevel;
        private String departmentId;
        private List<String> tags;
        private Map<String, Object> metadata;
        private String filePath;
        private String fileName;
        private Long fileSize;
        private String mimeType;
        private Boolean isTemplate;
        private LocalDateTime expireTime;
        private List<String> viewers;
        private List<String> editors;
    }

    /**
     * 文档搜索条件
     */
    @Data
    public static class DocumentSearchCriteria {
        private String keyword;
        private DocumentType documentType;
        private DocumentStatus status;
        private AccessLevel accessLevel;
        private String authorId;
        private String departmentId;
        private LocalDateTime createTimeFrom;
        private LocalDateTime createTimeTo;
        private List<String> tags;
        private Boolean includeArchived;
        private Boolean includeDeleted;
        private Integer pageSize;
        private Integer pageNumber;
        private String sortBy;
        private String sortOrder; // ASC, DESC
    }

    /**
     * 文档搜索结果
     */
    @Data
    public static class DocumentSearchResult {
        private List<Document> documents;
        private Integer totalCount;
        private Integer pageSize;
        private Integer pageNumber;
        private Integer totalPages;
        private Boolean hasMore;
        private Map<String, Object> aggregations; // 聚合统计信息
    }

    /**
     * 文档操作结果
     */
    @Data
    public static class DocumentOperationResult {
        private Boolean success;
        private String message;
        private Long documentId;
        private String documentNumber;
        private Map<String, Object> details;
        private List<String> warnings;
        private String errorCode;
        private Object data;
    }

    /**
     * 文档存储（模拟）
     */
    private Map<Long, Document> documentStorage = new ConcurrentHashMap<>();
    private Map<Long, List<DocumentVersion>> versionStorage = new ConcurrentHashMap<>();
    private Map<String, List<Long>> indexStorage = new ConcurrentHashMap<>(); // 各种索引

    /**
     * 统计信息
     */
    private Map<String, Object> statistics = new HashMap<>();

    /**
     * 初始化文档管理服务
     */
    public void initializeService() {
        log.info("文档管理服务初始化开始");

        // 加载初始数据
        loadInitialDocuments();

        // 建立索引
        buildIndexes();

        log.info("文档管理服务初始化完成，加载了{}个文档", documentStorage.size());
    }

    /**
     * 加载初始文档
     */
    private void loadInitialDocuments() {
        // 模拟加载一些初始文档
        for (int i = 1; i <= 50; i++) {
            Document document = createMockDocument(i);
            documentStorage.put(document.getDocumentId(), document);

            // 创建版本历史
            List<DocumentVersion> versions = new ArrayList<>();
            for (int v = 1; v <= (i % 3) + 1; v++) {
                DocumentVersion version = createMockVersion(document, v);
                versions.add(version);
            }
            versionStorage.put(document.getDocumentId(), versions);
        }

        log.debug("加载了{}个模拟文档", documentStorage.size());
    }

    /**
     * 创建模拟文档
     */
    private Document createMockDocument(int index) {
        Document document = new Document();
        document.setDocumentId((long) index);
        document.setDocumentNumber("DOC-" + String.format("%06d", index));

        DocumentType[] types = DocumentType.values();
        document.setDocumentType(types[index % types.length]);

        document.setTitle("文档" + index + " - " + document.getDocumentType().getDescription());
        document.setDescription("这是文档" + index + "的描述信息");

        DocumentStatus[] statuses = DocumentStatus.values();
        document.setStatus(statuses[index % (statuses.length - 1)]); // 避免删除状态

        AccessLevel[] levels = AccessLevel.values();
        document.setAccessLevel(levels[index % levels.length]);

        document.setAuthorId((long) (1 + index % 20));
        document.setAuthorName("用户" + ((index % 20) + 1));
        document.setDepartmentId("DEPT" + String.format("%03d", (index % 10) + 1));
        document.setDepartmentName("部门" + ((index % 10) + 1));

        LocalDateTime now = LocalDateTime.now();
        document.setCreateTime(now.minusDays(index % 30));
        document.setLastUpdateTime(now.minusHours(index % 24));

        if (DocumentStatus.PUBLISHED.equals(document.getStatus())) {
            document.setPublishTime(now.minusDays(index % 15));
        }

        if (index % 10 == 0) {
            document.setExpireTime(now.plusMonths(6));
        }

        document.setFilePath("/documents/" + document.getDocumentId() + "/" + document.getDocumentNumber() + ".pdf");
        document.setFileName(document.getDocumentNumber() + ".pdf");
        document.setFileExtension(".pdf");
        document.setFileSize(1024L * 1024 * (index % 10 + 1)); // 1-10MB
        document.setMimeType("application/pdf");
        document.setChecksum("checksum_" + document.getDocumentId());
        document.setVersion((index % 3) + 1);
        document.setVersionDescription("版本" + document.getVersion() + "的描述");

        if (index % 5 == 0) {
            document.setParentDocumentId((long) (index - 1));
        }

        document.setTags(Arrays.asList("标签" + (index % 5 + 1), "项目" + (index % 3 + 1)));

        document.setViewers(Arrays.asList("viewer" + ((index % 10) + 1), "viewer" + ((index % 10) + 2)));

        if (index % 3 == 0) {
            document.setEditors(Arrays.asList("editor" + ((index % 5) + 1)));
        }

        document.setIsTemplate(index % 20 == 0);
        document.setIsArchived(index % 15 == 0);

        if (Boolean.TRUE.equals(document.getIsArchived())) {
            document.setArchiveLocation("/archive/" + document.getDocumentNumber());
        }

        document.setDownloadCount((int) (Math.random() * 100));
        if (document.getDownloadCount() > 0) {
            document.setLastDownloadTime(now.minusHours((int) (Math.random() * 72)));
        }

        return document;
    }

    /**
     * 创建模拟版本
     */
    private DocumentVersion createMockVersion(Document document, int versionNumber) {
        DocumentVersion version = new DocumentVersion();
        version.setVersionId(System.currentTimeMillis() + versionNumber);
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(versionNumber);
        version.setVersionDescription("第" + versionNumber + "版本的变更描述");
        version.setAuthorId(document.getAuthorId());
        version.setAuthorName(document.getAuthorName());
        version.setCreateTime(document.getCreateTime().plusDays(versionNumber - 1));
        version.setFilePath(document.getFilePath() + ".v" + versionNumber);
        version.setFileName(document.getDocumentNumber() + "_v" + versionNumber + ".pdf");
        version.setFileSize(document.getFileSize());
        version.setChecksum(document.getChecksum() + "_v" + versionNumber);
        version.setIsCurrent(versionNumber == document.getVersion());

        version.setChangeLog(Arrays.asList(
                "创建了第" + versionNumber + "版本",
                "更新了文档内容",
                "修改了格式和样式"));

        version.setMetadata(new HashMap<>());

        return version;
    }

    /**
     * 建立索引
     */
    private void buildIndexes() {
        // 类型索引
        for (DocumentType type : DocumentType.values()) {
            indexStorage.put("type_" + type.getCode(), new ArrayList<>());
        }

        // 状态索引
        for (DocumentStatus status : DocumentStatus.values()) {
            indexStorage.put("status_" + status.getCode(), new ArrayList<>());
        }

        // 部门索引
        for (int i = 1; i <= 10; i++) {
            indexStorage.put("dept_DEPT" + String.format("%03d", i), new ArrayList<>());
        }

        // 更新索引
        for (Document document : documentStorage.values()) {
            updateIndexes(document);
        }

        log.debug("建立文档索引完成");
    }

    /**
     * 更新索引
     */
    private void updateIndexes(Document document) {
        // 更新类型索引
        indexStorage.computeIfAbsent("type_" + document.getDocumentType().getCode(), k -> new ArrayList<>())
                .add(document.getDocumentId());

        // 更新状态索引
        indexStorage.computeIfAbsent("status_" + document.getStatus().getCode(), k -> new ArrayList<>())
                .add(document.getDocumentId());

        // 更新部门索引
        if (document.getDepartmentId() != null) {
            indexStorage.computeIfAbsent("dept_" + document.getDepartmentId(), k -> new ArrayList<>())
                    .add(document.getDocumentId());
        }

        // 更新标签索引
        if (document.getTags() != null) {
            for (String tag : document.getTags()) {
                indexStorage.computeIfAbsent("tag_" + tag, k -> new ArrayList<>())
                        .add(document.getDocumentId());
            }
        }
    }

    /**
     * 上传文档
     *
     * @param request 上传请求
     * @return 操作结果
     */
    public DocumentOperationResult uploadDocument(DocumentUploadRequest request) {
        log.info("上传文档：标题={}, 类型={}", request.getTitle(), request.getDocumentType());

        DocumentOperationResult result = new DocumentOperationResult();
        result.setSuccess(false);
        result.setDetails(new HashMap<>());
        result.setWarnings(new ArrayList<>());

        try {
            // 1. 验证上传请求
            String validationError = validateUploadRequest(request);
            if (validationError != null) {
                result.setMessage("上传请求验证失败：" + validationError);
                return result;
            }

            // 2. 创建文档对象
            Document document = createDocumentFromRequest(request);

            // 3. 生成文档编号
            document.setDocumentNumber(generateDocumentNumber(document.getDocumentType()));

            // 4. 保存文档
            saveDocument(document);

            // 5. 创建版本记录
            DocumentVersion version = createInitialVersion(document);
            saveDocumentVersion(version);

            // 6. 更新索引
            updateIndexes(document);

            // 7. 返回结果
            result.setSuccess(true);
            result.setDocumentId(document.getDocumentId());
            result.setDocumentNumber(document.getDocumentNumber());
            result.setMessage("文档上传成功");
            result.getDetails().put("fileSize", document.getFileSize());
            result.getDetails().put("documentId", document.getDocumentId());
            result.getDetails().put("version", document.getVersion());

            log.info("文档上传成功：ID={}, 编号={}", document.getDocumentId(), document.getDocumentNumber());

        } catch (Exception e) {
            log.error("文档上传失败", e);
            result.setMessage("上传失败：" + e.getMessage());
            result.setErrorCode("UPLOAD_ERROR");
        }

        return result;
    }

    /**
     * 验证上传请求
     */
    private String validateUploadRequest(DocumentUploadRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return "文档标题不能为空";
        }

        if (request.getDocumentType() == null) {
            return "文档类型不能为空";
        }

        if (request.getAccessLevel() == null) {
            return "访问级别不能为空";
        }

        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            return "文件名不能为空";
        }

        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            return "文件大小必须大于0";
        }

        // 检查文件大小限制（100MB）
        if (request.getFileSize() > 100 * 1024 * 1024L) {
            return "文件大小不能超过100MB";
        }

        return null;
    }

    /**
     * 从请求创建文档对象
     */
    private Document createDocumentFromRequest(DocumentUploadRequest request) {
        Document document = new Document();
        document.setDocumentId(System.currentTimeMillis());
        document.setTitle(request.getTitle());
        document.setDescription(request.getDescription());
        document.setDocumentType(request.getDocumentType());
        document.setStatus(DocumentStatus.DRAFT);
        document.setAccessLevel(request.getAccessLevel());
        document.setAuthorId(1L); // 当前用户ID（简化处理）
        document.setAuthorName("当前用户");
        document.setDepartmentId(request.getDepartmentId());
        document.setCreateTime(LocalDateTime.now());
        document.setLastUpdateTime(LocalDateTime.now());
        document.setFilePath(request.getFilePath());
        document.setFileName(request.getFileName());
        document.setFileExtension(getFileExtension(request.getFileName()));
        document.setFileSize(request.getFileSize());
        document.setMimeType(request.getMimeType());
        document.setChecksum(generateChecksum(request.getFilePath()));
        document.setVersion(1);
        document.setVersionDescription("初始版本");
        document.setTags(request.getTags());
        document.setMetadata(request.getMetadata());
        document.setViewers(request.getViewers());
        document.setEditors(request.getEditors());
        document.setIsTemplate(Boolean.TRUE.equals(request.getIsTemplate()));
        document.setExpireTime(request.getExpireTime());
        document.setDownloadCount(0);
        document.setIsArchived(false);

        return document;
    }

    /**
     * 生成文档编号
     */
    private String generateDocumentNumber(DocumentType documentType) {
        String prefix = "";
        switch (documentType) {
            case CONTRACT:
                prefix = "CTR";
                break;
            case REPORT:
                prefix = "RPT";
                break;
            case PROPOSAL:
                prefix = "PRP";
                break;
            case MEETING:
                prefix = "MTG";
                break;
            default:
                prefix = "DOC";
                break;
        }

        return prefix + "-" + LocalDateTime.now().toString().replace("-", "").replace(":", "").substring(0, 12) + "-"
                + (int) (Math.random() * 1000);
    }

    /**
     * 生成文件校验和（模拟）
     */
    private String generateChecksum(String filePath) {
        return "checksum_" + System.currentTimeMillis() + "_" + filePath.hashCode();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 保存文档
     */
    private void saveDocument(Document document) {
        documentStorage.put(document.getDocumentId(), document);
    }

    /**
     * 保存文档版本
     */
    private void saveDocumentVersion(DocumentVersion version) {
        versionStorage.computeIfAbsent(version.getDocumentId(), k -> new ArrayList<>()).add(version);
    }

    /**
     * 创建初始版本
     * 为新创建的文档创建第一个版本记录
     *
     * @param document 文档对象
     * @return 创建的文档版本对象
     */
    private DocumentVersion createInitialVersion(Document document) {
        DocumentVersion version = new DocumentVersion();
        version.setVersionId(System.currentTimeMillis()); // 临时ID，实际应该使用数据库自增ID
        version.setDocumentId(document.getDocumentId());
        version.setVersionNumber(1);
        version.setVersionDescription("初始版本");
        version.setAuthorId(document.getAuthorId());
        version.setAuthorName(document.getAuthorName());
        version.setCreateTime(LocalDateTime.now());
        version.setFilePath(document.getFilePath());
        version.setFileName(document.getFileName());
        version.setFileSize(document.getFileSize());
        version.setChecksum(document.getChecksum());
        version.setIsCurrent(true);
        version.setChangeLog(new ArrayList<>());
        version.setMetadata(new HashMap<>());

        return version;
    }

    /**
     * 搜索文档
     *
     * @param criteria 搜索条件
     * @return 搜索结果
     */
    public DocumentSearchResult searchDocuments(DocumentSearchCriteria criteria) {
        log.debug("搜索文档：关键字={}, 类型={}", criteria.getKeyword(), criteria.getDocumentType());

        DocumentSearchResult result = new DocumentSearchResult();
        result.setDocuments(new ArrayList<>());

        try {
            List<Document> allDocuments = new ArrayList<>(documentStorage.values());

            // 应用搜索条件
            List<Document> filteredDocuments = allDocuments.stream()
                    .filter(doc -> matchesCriteria(doc, criteria))
                    .sorted((d1, d2) -> compareDocuments(d1, d2, criteria))
                    .collect(Collectors.toList());

            // 应用分页
            int totalCount = filteredDocuments.size();
            int pageSize = criteria.getPageSize() != null ? criteria.getPageSize() : 20;
            int pageNumber = criteria.getPageNumber() != null ? criteria.getPageNumber() : 1;

            int startIndex = (pageNumber - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalCount);

            List<Document> pagedDocuments = filteredDocuments.subList(startIndex, endIndex);

            result.setDocuments(pagedDocuments);
            result.setTotalCount(totalCount);
            result.setPageSize(pageSize);
            result.setPageNumber(pageNumber);
            result.setTotalPages((int) Math.ceil((double) totalCount / pageSize));
            result.setHasMore(endIndex < totalCount);

            // 添加聚合统计
            result.setAggregations(generateAggregations(filteredDocuments));

        } catch (Exception e) {
            log.error("文档搜索失败", e);
        }

        return result;
    }

    /**
     * 检查文档是否匹配搜索条件
     */
    private boolean matchesCriteria(Document document, DocumentSearchCriteria criteria) {
        // 关键词搜索
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            String keyword = criteria.getKeyword().toLowerCase();
            boolean matchesKeyword = document.getTitle().toLowerCase().contains(keyword) ||
                    (document.getDescription() != null && document.getDescription().toLowerCase().contains(keyword)) ||
                    (document.getTags() != null
                            && document.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(keyword)));

            if (!matchesKeyword) {
                return false;
            }
        }

        // 类型过滤
        if (criteria.getDocumentType() != null && !criteria.getDocumentType().equals(document.getDocumentType())) {
            return false;
        }

        // 状态过滤
        if (criteria.getStatus() != null && !criteria.getStatus().equals(document.getStatus())) {
            return false;
        }

        // 访问级别过滤
        if (criteria.getAccessLevel() != null && !criteria.getAccessLevel().equals(document.getAccessLevel())) {
            return false;
        }

        // 作者过滤
        if (criteria.getAuthorId() != null && !criteria.getAuthorId().equals(document.getAuthorId().toString())) {
            return false;
        }

        // 部门过滤
        if (criteria.getDepartmentId() != null && !criteria.getDepartmentId().equals(document.getDepartmentId())) {
            return false;
        }

        // 创建时间过滤
        if (criteria.getCreateTimeFrom() != null && document.getCreateTime().isBefore(criteria.getCreateTimeFrom())) {
            return false;
        }

        if (criteria.getCreateTimeTo() != null && document.getCreateTime().isAfter(criteria.getCreateTimeTo())) {
            return false;
        }

        // 标签过滤
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            if (document.getTags() == null || document.getTags().stream().noneMatch(criteria.getTags()::contains)) {
                return false;
            }
        }

        // 是否包含已归档文档
        if (criteria.getIncludeArchived() != null
                && !criteria.getIncludeArchived().equals(Boolean.TRUE.equals(document.getIsArchived()))) {
            return false;
        }

        // 是否包含已删除文档
        if (criteria.getIncludeDeleted() != null
                && !criteria.getIncludeDeleted().equals(DocumentStatus.DELETED.equals(document.getStatus()))) {
            return false;
        }

        return true;
    }

    /**
     * 比较文档排序
     */
    private int compareDocuments(Document d1, Document d2, DocumentSearchCriteria criteria) {
        String sortBy = criteria.getSortBy() != null ? criteria.getSortBy() : "createTime";
        String sortOrder = criteria.getSortOrder() != null ? criteria.getSortOrder() : "DESC";
        boolean ascending = "ASC".equals(sortOrder);

        int comparison = 0;

        switch (sortBy) {
            case "title":
                comparison = d1.getTitle().compareTo(d2.getTitle());
                break;
            case "createTime":
                comparison = d1.getCreateTime().compareTo(d2.getCreateTime());
                break;
            case "updateTime":
                comparison = d1.getLastUpdateTime().compareTo(d2.getLastUpdateTime());
                break;
            case "fileSize":
                comparison = d1.getFileSize().compareTo(d2.getFileSize());
                break;
            default:
                comparison = d1.getDocumentId().compareTo(d2.getDocumentId());
                break;
        }

        return ascending ? comparison : -comparison;
    }

    /**
     * 生成聚合统计信息
     */
    private Map<String, Object> generateAggregations(List<Document> documents) {
        Map<String, Object> aggregations = new HashMap<>();

        // 按类型统计
        Map<String, Long> typeStats = documents.stream()
                .collect(Collectors.groupingBy(doc -> doc.getDocumentType().getDescription(), Collectors.counting()));
        aggregations.put("typeDistribution", typeStats);

        // 按状态统计
        Map<String, Long> statusStats = documents.stream()
                .collect(Collectors.groupingBy(doc -> doc.getStatus().getDescription(), Collectors.counting()));
        aggregations.put("statusDistribution", statusStats);

        // 按访问级别统计
        Map<String, Long> accessLevelStats = documents.stream()
                .collect(Collectors.groupingBy(doc -> doc.getAccessLevel().getDescription(), Collectors.counting()));
        aggregations.put("accessLevelDistribution", accessLevelStats);

        // 按部门统计
        Map<String, Long> deptStats = documents.stream()
                .collect(Collectors.groupingBy(doc -> doc.getDepartmentName(), Collectors.counting()));
        aggregations.put("departmentDistribution", deptStats);

        // 文件大小统计
        Long totalSize = documents.stream().mapToLong(Document::getFileSize).sum();
        aggregations.put("totalFileSize", totalSize);
        aggregations.put("averageFileSize", documents.isEmpty() ? 0L : totalSize / documents.size());

        return aggregations;
    }

    /**
     * 获取文档详情
     *
     * @param documentId 文档ID
     * @return 文档对象
     */
    public Document getDocument(Long documentId) {
        return documentStorage.get(documentId);
    }

    /**
     * 获取文档版本历史
     *
     * @param documentId 文档ID
     * @return 版本列表
     */
    public List<DocumentVersion> getDocumentVersions(Long documentId) {
        return versionStorage.getOrDefault(documentId, new ArrayList<>());
    }

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     * @return 操作结果
     */
    public DocumentOperationResult deleteDocument(Long documentId) {
        log.info("删除文档：{}", documentId);

        DocumentOperationResult result = new DocumentOperationResult();
        result.setSuccess(false);

        try {
            Document document = documentStorage.get(documentId);
            if (document == null) {
                result.setMessage("文档不存在");
                return result;
            }

            // 检查删除权限（简化处理）
            if (!hasDeletePermission(document)) {
                result.setMessage("无删除权限");
                return result;
            }

            // 标记为已删除
            document.setStatus(DocumentStatus.DELETED);
            document.setLastUpdateTime(LocalDateTime.now());
            documentStorage.put(documentId, document);

            result.setSuccess(true);
            result.setDocumentId(documentId);
            result.setMessage("文档删除成功");

            log.info("文档删除成功：{}", documentId);

        } catch (Exception e) {
            log.error("删除文档失败：{}", documentId, e);
            result.setMessage("删除失败：" + e.getMessage());
            result.setErrorCode("DELETE_ERROR");
        }

        return result;
    }

    /**
     * 检查删除权限
     */
    private boolean hasDeletePermission(Document document) {
        // 简化权限检查，实际应该根据用户角色和文档权限判断
        return true;
    }

    /**
     * 更新文档
     *
     * @param documentId 文档ID
     * @param updates    更新内容
     * @return 操作结果
     */
    public DocumentOperationResult updateDocument(Long documentId, Map<String, Object> updates) {
        log.info("更新文档：{}", documentId);

        DocumentOperationResult result = new DocumentOperationResult();
        result.setSuccess(false);

        try {
            Document document = documentStorage.get(documentId);
            if (document == null) {
                result.setMessage("文档不存在");
                return result;
            }

            // 检查编辑权限
            if (!hasEditPermission(document)) {
                result.setMessage("无编辑权限");
                return result;
            }

            // 应用更新
            if (updates.containsKey("title")) {
                document.setTitle((String) updates.get("title"));
            }
            if (updates.containsKey("description")) {
                document.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("status")) {
                document.setStatus((DocumentStatus) updates.get("status"));
            }
            if (updates.containsKey("accessLevel")) {
                document.setAccessLevel((AccessLevel) updates.get("accessLevel"));
            }
            if (updates.containsKey("tags")) {
                @SuppressWarnings("unchecked")
                List<String> newTags = (List<String>) updates.get("tags");
                document.setTags(newTags);
            }

            document.setLastUpdateTime(LocalDateTime.now());

            // 更新索引
            updateIndexes(document);

            result.setSuccess(true);
            result.setDocumentId(documentId);
            result.setMessage("文档更新成功");

            log.info("文档更新成功：{}", documentId);

        } catch (Exception e) {
            log.error("更新文档失败：{}", documentId, e);
            result.setMessage("更新失败：" + e.getMessage());
            result.setErrorCode("UPDATE_ERROR");
        }

        return result;
    }

    /**
     * 检查编辑权限
     */
    private boolean hasEditPermission(Document document) {
        // 简化权限检查
        return true;
    }

    /**
     * 获取所有文档类型
     */
    public List<DocumentType> getAllDocumentTypes() {
        return Arrays.asList(DocumentType.values());
    }

    /**
     * 获取所有文档状态
     */
    public List<DocumentStatus> getAllDocumentStatuses() {
        return Arrays.asList(DocumentStatus.values());
    }

    /**
     * 获取所有访问级别
     */
    public List<AccessLevel> getAllAccessLevels() {
        return Arrays.asList(AccessLevel.values());
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>(statistics);
        stats.put("totalDocuments", documentStorage.size());
        stats.put("totalVersions", versionStorage.values().stream().mapToInt(List::size).sum());
        stats.put("documentCountByType", documentStorage.values().stream()
                .collect(Collectors.groupingBy(Document::getDocumentType, Collectors.counting())));
        stats.put("documentCountByStatus", documentStorage.values().stream()
                .collect(Collectors.groupingBy(Document::getStatus, Collectors.counting())));
        stats.put("totalFileSize", documentStorage.values().stream().mapToLong(Document::getFileSize).sum());
        stats.put("downloadCount", documentStorage.values().stream().mapToInt(Document::getDownloadCount).sum());
        return stats;
    }
}
