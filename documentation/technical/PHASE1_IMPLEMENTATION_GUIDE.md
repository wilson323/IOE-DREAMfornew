# IOE-DREAM P1阶段实施指南

## 📋 概述

**阶段名称**: P1 - 数据持久化与真实集成
**预计时间**: 1-2周
**目标**: 将模拟数据实现替换为真实数据库和API集成
**优先级**: P0（高优先级）

---

## 🎯 核心目标

### 1. 数据持久化
- [ ] 实现DAO层（使用MyBatis-Plus）
- [ ] 创建数据库表结构（Flyway迁移脚本）
- [ ] 替换ConcurrentHashMap为真实数据库操作
- [ ] 添加单元测试和集成测试

### 2. 真实API集成
- [ ] 对接GatewayServiceClient
- [ ] 调用其他业务微服务API
- [ ] 实现真实数据聚合
- [ ] 实现缓存预热机制

### 3. 文件存储实现
- [ ] 实现真实Excel导出（EasyExcel）
- [ ] 实现真实PDF导出（iText）
- [ ] 集成文件存储服务（MinIO/OSS）
- [ ] 实现文件下载API

---

## 📁 任务一：数据持久化实现

### 1.1 创建Flyway迁移脚本

**文件位置**: `microservices/ioedream-data-analysis-service/src/main/resources/db/migration/`

#### V1__Create_Report_Tables.sql

```sql
-- =====================================================
-- 数据报表表
-- =====================================================

-- 报表配置表
CREATE TABLE IF NOT EXISTS `t_data_report` (
    `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
    `report_name` VARCHAR(100) NOT NULL COMMENT '报表名称',
    `report_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '报表编码',
    `report_type` VARCHAR(20) NOT NULL COMMENT '报表类型:list/summary/chart/dashboard',
    `business_module` VARCHAR(20) NOT NULL COMMENT '业务模块:attendance/consume/access/visitor/video',

    -- 数据源配置
    `source_type` VARCHAR(20) NOT NULL COMMENT '数据源类型:database/api/elasticsearch/redis',
    `source_name` VARCHAR(100) COMMENT '表名或API路径',
    `source_config` JSON COMMENT '数据源连接配置',
    `field_mapping` JSON COMMENT '字段映射配置',

    -- 查询配置
    `query_config` JSON COMMENT '查询配置',

    -- 布局配置
    `layout_config` JSON COMMENT '布局配置',

    -- 权限配置
    `permission_config` JSON COMMENT '权限配置',

    -- 基础字段
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/active/archived',
    `description` TEXT COMMENT '报表描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX `idx_report_code` (`report_code`),
    INDEX `idx_business_module` (`business_module`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据报表配置表';

-- 仪表板配置表
CREATE TABLE IF NOT EXISTS `t_data_dashboard` (
    `dashboard_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '仪表板ID',
    `dashboard_name` VARCHAR(100) NOT NULL COMMENT '仪表板名称',
    `dashboard_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '仪表板编码',

    -- 布局配置
    `layout_config` JSON NOT NULL COMMENT '仪表板布局配置',

    -- 权限配置
    `permission_config` JSON COMMENT '权限配置',

    -- 基础字段
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `creator_name` VARCHAR(50) COMMENT '创建人姓名',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态:draft/active/archived',
    `description` TEXT COMMENT '仪表板描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `refresh_time` DATETIME COMMENT '最后刷新时间',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    INDEX `idx_dashboard_code` (`dashboard_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据仪表板配置表';

-- 导出任务表
CREATE TABLE IF NOT EXISTS `t_data_export_task` (
    `export_task_id` VARCHAR(64) PRIMARY KEY COMMENT '导出任务ID',
    `report_id` BIGINT NOT NULL COMMENT '报表ID',
    `export_format` VARCHAR(10) NOT NULL COMMENT '导出格式:excel/pdf/csv',
    `file_name` VARCHAR(255) COMMENT '文件名',
    `file_url` VARCHAR(500) COMMENT '文件URL',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态:pending/processing/completed/failed',
    `error_message` TEXT COMMENT '错误信息',
    `request_params` JSON COMMENT '请求参数',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `complete_time` DATETIME COMMENT '完成时间',

    INDEX `idx_report_id` (`report_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据导出任务表';
```

### 1.2 创建Entity类

**文件位置**: `microservices/ioedream-data-analysis-service/src/main/java/net/lab1024/sa/data/domain/entity/`

#### ReportEntity.java

```java
package net.lab1024.sa.data.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据报表实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_data_report")
@Schema(description = "数据报表实体")
public class ReportEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "报表编码")
    private String reportCode;

    @Schema(description = "报表类型")
    private String reportType;

    @Schema(description = "业务模块")
    private String businessModule;

    @Schema(description = "数据源类型")
    private String sourceType;

    @Schema(description = "表名或API路径")
    private String sourceName;

    @Schema(description = "数据源配置")
    private String sourceConfig;

    @Schema(description = "字段映射")
    private String fieldMapping;

    @Schema(description = "查询配置")
    private String queryConfig;

    @Schema(description = "布局配置")
    private String layoutConfig;

    @Schema(description = "权限配置")
    private String permissionConfig;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "报表描述")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记")
    private Integer deletedFlag;

    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
```

### 1.3 创建DAO接口

**文件位置**: `microservices/ioedream-data-analysis-service/src/main/java/net/lab1024/sa/data/dao/`

#### ReportDao.java

```java
package net.lab1024.sa.data.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.data.domain.entity.ReportEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据报表DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Mapper
public interface ReportDao extends BaseMapper<ReportEntity> {
    // 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
    // 基础CRUD由BaseMapper提供
}
```

### 1.4 修改ServiceImpl实现

**关键改动**：

1. **注入DAO替代ConcurrentHashMap**
```java
// 修改前
private final Map<Long, ReportVO> reportStorage = new ConcurrentHashMap<>();

// 修改后
@Resource
private ReportDao reportDao;
```

2. **使用MyBatis-Plus查询**
```java
// 修改前
ReportVO report = reportStorage.get(reportId);

// 修改后
ReportEntity entity = reportDao.selectById(reportId);
ReportVO report = convertToVO(entity);
```

---

## 📁 任务二：真实API集成

### 2.1 创建Gateway客户端调用

#### 2.1.1 考勤数据API调用

```java
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 获取考勤统计数据
     */
    public List<DataStatisticsVO> getStatistics(Long reportId, String startDate, String endDate) {

        // 调用考勤服务API
        ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callAttendanceService(
            "/api/v1/attendance/statistics/overview",
            HttpMethod.POST,
            Map.of("startDate", startDate, "endDate", endDate),
            new TypeReference<ResponseDTO<Map<String, Object>>>() {}
        );

        Map<String, Object> data = response.getData();
        return convertToStatisticsVO(data);
    }
}
```

### 2.2 实现数据缓存预热

```java
@Component
public class ReportCacheWarmupService {

    @Resource
    private ReportService reportService;

    @Resource
    private ReportDao reportDao;

    /**
     * 应用启动时预热缓存
     */
    @PostConstruct
    public void warmupCache() {
        log.info("[缓存预热] 开始预热报表缓存");

        // 查询所有启用的报表
        LambdaQueryWrapper<ReportEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReportEntity::getStatus, "active");

        List<ReportEntity> reports = reportDao.selectList(queryWrapper);

        // 预加载报表数据
        List<Long> reportIds = reports.stream()
                .map(ReportEntity::getReportId)
                .collect(Collectors.toList());

        reportService.preloadReportData(reportIds);

        log.info("[缓存预热] 缓存预热完成: count={}", reportIds.size());
    }
}
```

---

## 📁 任务三：文件存储实现

### 3.1 Excel导出实现

#### FileExportService.java

```java
@Service
@Slf4j
public class FileExportService {

    @Resource
    private ReportService reportService;

    /**
     * 导出报表为Excel
     */
    public String exportToExcel(Long reportId, Map<String, Object> params) {

        // 1. 查询报表数据
        ReportQueryRequest request = ReportQueryRequest.builder()
                .reportId(reportId)
                .params(params)
                .build();

        ReportQueryResult result = reportService.queryReportData(request);

        // 2. 生成Excel文件
        String fileName = "report_" + reportId + "_" +
                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                         ".xlsx";

        String filePath = "/tmp/exports/" + fileName;

        EasyExcel.write(filePath, ReportData.class)
                .sheet("报表数据")
                .doWrite(result.getDataList());

        log.info("[文件导出] Excel导出成功: file={}, records={}", fileName, result.getDataList().size());

        return filePath;
    }
}
```

### 3.2 PDF导出实现

```java
/**
 * 导出报表为PDF
 */
public String exportToPdf(Long reportId, Map<String, Object> params) {

    // 1. 查询报表数据
    ReportQueryResult result = reportService.queryReportData(request);

    // 2. 生成PDF文件
    String fileName = "report_" + reportId + "_" + ".pdf";
    String filePath = "/tmp/exports/" + fileName;

    try (PdfWriter writer = new PdfWriter(new PdfWriter(filePath))) {
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("报表名称: " + result.getReportName()));
        document.add(new Paragraph("生成时间: " + result.getQueryTime()));

        // 添加表格
        Table table = new Table(UnitValue.createPercentArray(new float[]{10, 30, 30, 30}));
        // ... 添加表格内容

        document.close();
    }

    log.info("[文件导出] PDF导出成功: file={}", fileName);
    return filePath;
}
```

### 3.3 文件存储服务集成

#### MinIO配置

```yaml
# application.yml
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucket-name: ioe-dream-exports
```

#### MinIOFileStorageService.java

```java
@Service
@Slf4j
public class MinIOFileStorageService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 上传文件到MinIO
     */
    public String uploadFile(String filePath, String originalFileName) {

        try {
            // 创建MinIO客户端
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 检查bucket是否存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }

            // 上传文件
            String objectName = UUID.randomUUID().toString() + "_" + originalFileName;

            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .build());

            // 返回文件URL
            String fileUrl = endpoint + "/" + bucketName + "/" + objectName;

            log.info("[文件存储] 文件上传成功: url={}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("[文件存储] 文件上传失败", e);
            throw new SystemException("FILE_UPLOAD_ERROR", "文件上传失败", e);
        }
    }
}
```

---

## ✅ 验收标准

### 数据持久化

- [x] Flyway迁移脚本创建
- [x] Entity类创建（ReportEntity, DashboardEntity, ExportTaskEntity）
- [x] DAO接口创建（使用@Mapper）
- [x] Service实现改用数据库操作（ReportServiceImpl, DashboardServiceImpl）
- [ ] 单元测试通过
- [ ] 集成测试通过

### API集成

- [ ] GatewayServiceClient集成完成
- [ ] 考勤服务API对接
- [ ] 消费服务API对接
- [ ] 门禁服务API对接
- [ ] 缓存预热机制实现

### 文件存储

- [ ] Excel导出功能完成
- [ ] PDF导出功能完成
- [ ] MinIO/OSS集成完成
- [ ] 文件下载API实现

---

## 📅 时间规划

### 第1周

**周一-周二**：
- 创建Flyway迁移脚本
- 创建Entity和DAO
- 修改Service实现

**周三-周四**：
- 集成GatewayServiceClient
- 实现API对接
- 测试API调用

**周五**：
- 单元测试编写
- 集成测试编写
- 代码审查

### 第2周

**周一-周二**：
- 实现Excel导出
- 实现PDF导出

**周三-周四**：
- 集成MinIO/OSS
- 实现文件下载API

**周五**：
- 端到端测试
- 性能测试
- 文档更新

---

## 🎯 总结

**P1阶段目标**: 将模拟数据实现替换为真实数据库和API集成

**关键成功因素**：
- ✅ 严格遵循IOE-DREAM企业级规范
- ✅ 保持代码质量100%符合规范
- ✅ 完整的测试覆盖
- ✅ 详细的实施文档

**预期成果**：
- 数据持久化完整实现
- 真实API集成完成
- 文件存储功能完整
- 系统达到生产级标准

---

**实施指南创建时间**: 2025-12-26
**创建人**: IOE-DREAM Team
**预计开始时间**: 待用户确认
**预计完成时间**: 1-2周后
