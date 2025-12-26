# P0级统一报表中心完整实施指南

**📅 创建时间**: 2025-12-26
**👯‍♂️ 工作量**: 8人天
**⭐ 优先级**: P0级核心功能
**🎯 目标**: 完整实现企业级统一报表中心，支持所有业务模块报表需求

---

## 📊 功能概述

### 核心功能模块

1. **报表定义管理** - 报表CRUD、分类管理、权限控制
2. **报表模板管理** - Excel/PDF模板上传、版本管理
3. **报表数据源管理** - SQL查询、API调用、静态数据
4. **报表生成引擎** - 动态生成、参数化查询、数据填充
5. **报表导出服务** - Excel、PDF、Word、CSV多格式导出
6. **报表调度服务** - 定时生成、邮件推送、消息通知
7. **报表权限管理** - 角色权限、数据权限、操作权限

### 技术栈

- **后端**: Spring Boot 3.5.8 + MyBatis-Plus 3.5.15
- **Excel**: EasyExcel 3.3+（阿里开源）
- **PDF**: iText Core 8.0+（商业许可证）
- **模板**: JasperReports 7.0+（可选，复杂报表）
- **前端**: Vue 3.4 + Ant Design Vue 4
- **图表**: ECharts 5.4+（数据可视化）

---

## 🗄️ 数据库设计

### 核心表结构

```sql
-- 1. 报表定义表
CREATE TABLE t_report_definition (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    report_code VARCHAR(100) NOT NULL COMMENT '报表编码',
    report_type TINYINT NOT NULL COMMENT '报表类型（1-列表 2-汇总 3-图表 4-交叉表）',
    business_module VARCHAR(50) COMMENT '业务模块（access/attendance/consume等）',
    category_id BIGINT COMMENT '分类ID',
    data_source_type TINYINT NOT NULL COMMENT '数据源类型（1-SQL 2-API 3-静态）',
    data_source_config TEXT COMMENT '数据源配置（JSON）',
    template_type TINYINT COMMENT '模板类型（1-Excel 2-PDF 3-Word）',
    template_config TEXT COMMENT '模板配置（JSON）',
    export_formats VARCHAR(100) COMMENT '导出格式（excel,pdf,word,csv）',
    description TEXT COMMENT '报表描述',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_report_code (report_code),
    KEY idx_business_module (business_module),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表定义表';

-- 2. 报表分类表
CREATE TABLE t_report_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表分类表';

-- 3. 报表参数表
CREATE TABLE t_report_parameter (
    parameter_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    parameter_name VARCHAR(100) NOT NULL COMMENT '参数名称',
    parameter_code VARCHAR(50) NOT NULL COMMENT '参数编码',
    parameter_type VARCHAR(50) NOT NULL COMMENT '参数类型（String/Integer/Date等）',
    default_value VARCHAR(500) COMMENT '默认值',
    required TINYINT DEFAULT 0 COMMENT '是否必填（1-是 0-否）',
    validation_rule VARCHAR(500) COMMENT '验证规则（正则表达式）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表参数表';

-- 4. 报表模板表
CREATE TABLE t_report_template (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_type TINYINT NOT NULL COMMENT '模板类型（1-Excel 2-PDF 3-Word）',
    file_path VARCHAR(500) NOT NULL COMMENT '模板文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    version VARCHAR(50) COMMENT '版本号',
    description TEXT COMMENT '模板描述',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_report_id (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板表';

-- 5. 报表生成记录表
CREATE TABLE t_report_generation (
    generation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '生成记录ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    report_name VARCHAR(200) COMMENT '报表名称',
    parameters TEXT COMMENT '请求参数（JSON）',
    generate_type TINYINT COMMENT '生成方式（1-手动 2-定时 3-API）',
    file_type VARCHAR(20) COMMENT '文件类型（excel/pdf/word/csv）',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    status TINYINT COMMENT '状态（1-生成中 2-成功 3-失败）',
    error_message TEXT COMMENT '错误信息',
    generate_time DATETIME COMMENT '生成时间',
    create_user_id BIGINT COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_report_id (report_id),
    KEY idx_generate_time (generate_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表生成记录表';

-- 6. 报表调度任务表
CREATE TABLE t_report_schedule (
    schedule_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调度ID',
    report_id BIGINT NOT NULL COMMENT '报表ID',
    schedule_name VARCHAR(200) NOT NULL COMMENT '调度名称',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    parameters TEXT COMMENT '调度参数（JSON）',
    notification_config TEXT COMMENT '通知配置（邮件、消息等）',
    status TINYINT DEFAULT 1 COMMENT '状态（1-启用 0-禁用）',
    last_execute_time DATETIME COMMENT '最后执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_report_id (report_id),
    KEY idx_status (status),
    KEY idx_next_execute_time (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表调度任务表';

-- 初始化报表分类数据
INSERT INTO t_report_category (category_name, category_code, parent_id, sort_order, status) VALUES
('门禁报表', 'ACCESS', 0, 1, 1),
('考勤报表', 'ATTENDANCE', 0, 2, 1),
('消费报表', 'CONSUME', 0, 3, 1),
('访客报表', 'VISITOR', 0, 4, 1),
('视频报表', 'VIDEO', 0, 5, 1),
('综合报表', 'COMPREHENSIVE', 0, 6, 1);

-- 初始化示例报表定义
INSERT INTO t_report_definition (report_name, report_code, report_type, business_module, category_id,
    data_source_type, data_source_config, template_type, export_formats, description, status) VALUES
('每日考勤汇总表', 'DAILY_ATTENDANCE_SUMMARY', 2, 'attendance', 2,
    1, '{"sql":"SELECT * FROM t_attendance_record WHERE record_date = #{date}"}',
    1, 'excel,pdf', '统计每日考勤打卡情况', 1),
('月度消费统计表', 'MONTHLY_CONSUME_STATS', 2, 'consume', 3,
    1, '{"sql":"SELECT * FROM t_consume_record WHERE MONTH(consume_time) = #{month}"}',
    1, 'excel,pdf', '统计月度消费数据', 1),
('门禁通行记录表', 'ACCESS_RECORD_LIST', 1, 'access', 1,
    1, '{"sql":"SELECT * FROM t_access_record WHERE access_time BETWEEN #{startTime} AND #{endTime}"}',
    1, 'excel,csv', '查询门禁通行记录', 1);
```

---

## 📐 Entity实体类设计

### 1. ReportDefinitionEntity.java（报表定义实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表定义实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_definition")
@Schema(description = "报表定义实体")
public class ReportDefinitionEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "报表ID")
    private Long reportId;

    @Schema(description = "报表名称", required = true)
    private String reportName;

    @Schema(description = "报表编码", required = true)
    private String reportCode;

    @Schema(description = "报表类型（1-列表 2-汇总 3-图表 4-交叉表）")
    private Integer reportType;

    @Schema(description = "业务模块")
    private String businessModule;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "数据源类型（1-SQL 2-API 3-静态）")
    private Integer dataSourceType;

    @Schema(description = "数据源配置（JSON）")
    private String dataSourceConfig;

    @Schema(description = "模板类型（1-Excel 2-PDF 3-Word）")
    private Integer templateType;

    @Schema(description = "模板配置（JSON）")
    private String templateConfig;

    @Schema(description = "导出格式")
    private String exportFormats;

    @Schema(description = "报表描述")
    private String description;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
```

### 2. ReportCategoryEntity.java（报表分类实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表分类实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_category")
@Schema(description = "报表分类实体")
public class ReportCategoryEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称", required = true)
    private String categoryName;

    @Schema(description = "分类编码", required = true)
    private String categoryCode;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;
}
```

### 3. ReportParameterEntity.java（报表参数实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表参数实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_parameter")
@Schema(description = "报表参数实体")
public class ReportParameterEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "参数ID")
    private Long parameterId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "参数名称", required = true)
    private String parameterName;

    @Schema(description = "参数编码", required = true)
    private String parameterCode;

    @Schema(description = "参数类型", required = true)
    private String parameterType;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填（1-是 0-否）")
    private Integer required;

    @Schema(description = "验证规则")
    private String validationRule;

    @Schema(description = "排序号")
    private Integer sortOrder;
}
```

### 4. ReportTemplateEntity.java（报表模板实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报表模板实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_template")
@Schema(description = "报表模板实体")
public class ReportTemplateEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "模板名称", required = true)
    private String templateName;

    @Schema(description = "模板类型（1-Excel 2-PDF 3-Word）")
    private Integer templateType;

    @Schema(description = "模板文件路径", required = true)
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;
}
```

### 5. ReportGenerationEntity.java（报表生成记录实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表生成记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_generation")
@Schema(description = "报表生成记录实体")
public class ReportGenerationEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "生成记录ID")
    private Long generationId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "报表名称")
    private String reportName;

    @Schema(description = "请求参数（JSON）")
    private String parameters;

    @Schema(description = "生成方式（1-手动 2-定时 3-API）")
    private Integer generateType;

    @Schema(description = "文件类型（excel/pdf/word/csv）")
    private String fileType;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "状态（1-生成中 2-成功 3-失败）")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "生成时间")
    private LocalDateTime generateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;
}
```

### 6. ReportScheduleEntity.java（报表调度任务实体）

```java
package net.lab1024.sa.common.entity.report;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表调度任务实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_schedule")
@Schema(description = "报表调度任务实体")
public class ReportScheduleEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "调度ID")
    private Long scheduleId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "调度名称", required = true)
    private String scheduleName;

    @Schema(description = "Cron表达式", required = true)
    private String cronExpression;

    @Schema(description = "调度参数（JSON）")
    private String parameters;

    @Schema(description = "通知配置（邮件、消息等）")
    private String notificationConfig;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecuteTime;
}
```

---

## 🎯 实施步骤

### 步骤1: 创建数据库迁移脚本（30分钟）
- ✅ 创建6张核心表
- ✅ 初始化分类数据
- ✅ 初始化示例报表

### 步骤2: 创建Entity实体类（1小时）
- ✅ ReportDefinitionEntity
- ✅ ReportCategoryEntity
- ✅ ReportParameterEntity
- ✅ ReportTemplateEntity
- ✅ ReportGenerationEntity
- ✅ ReportScheduleEntity

### 步骤3: 创建DAO层（30分钟）
- ReportDefinitionDao
- ReportCategoryDao
- ReportParameterDao
- ReportTemplateDao
- ReportGenerationDao
- ReportScheduleDao

### 步骤4: 创建Manager层（2小时）
- ReportDefinitionManager（报表定义管理）
- ReportGenerateManager（报表生成引擎）
- ReportExportManager（报表导出服务）
- ReportScheduleManager（报表调度管理）

### 步骤5: 创建Service层（2小时）
- ReportDefinitionService
- ReportGenerateService
- ReportExportService
- ReportScheduleService

### 步骤6: 创建Controller层（1.5小时）
- ReportDefinitionController
- ReportGenerateController
- ReportExportController
- ReportScheduleController

### 步骤7: 创建前端页面（1小时）
- 报表列表页面
- 报表设计器页面
- 报表预览页面
- 报表调度页面

---

## 📋 验收标准

### 功能验收
- ✅ 报表定义CRUD功能完整
- ✅ 支持Excel导出（EasyExcel）
- ✅ 支持PDF导出（iText）
- ✅ 支持参数化查询
- ✅ 支持定时调度
- ✅ 支持权限控制

### 技术验收
- ✅ 四层架构规范
- ✅ 事务管理
- ✅ 日志记录
- ✅ 异常处理
- ✅ API文档完整

---

**👯‍♂️ 实施人**: IOE-DREAM开发团队
**📅 完成时间**: 预计8人天
**🎯 目标**: 企业级统一报表中心
