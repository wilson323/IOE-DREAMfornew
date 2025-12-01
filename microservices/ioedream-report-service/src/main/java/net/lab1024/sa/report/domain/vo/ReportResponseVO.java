package net.lab1024.sa.report.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业级报表响应VO
 *
 * 统一报表响应数据结构，包含报表基本信息、状态、数据和元数据
 * 支持多种报表类型和数据格式，提供完整的报表生成结果信息
 * 遵循企业级VO设计规范，提供完整的字段说明和验证
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "报表响应数据传输对象")
public class ReportResponseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报表唯一标识
     */
    @Schema(description = "报表唯一标识", example = "report_20251130_001")
    private String reportId;

    /**
     * 报表名称
     */
    @Schema(description = "报表名称", example = "消费月度统计报表")
    private String reportName;

    /**
     * 报表类型
     * 枚举值：DATA, STATISTICS, ANALYSIS, SUMMARY
     */
    @Schema(description = "报表类型", example = "STATISTICS", allowableValues = { "DATA", "STATISTICS", "ANALYSIS",
            "SUMMARY" })
    private String reportType;

    /**
     * 报表状态
     * 枚举值：PENDING, PROCESSING, COMPLETED, FAILED, EXPIRED
     */
    @Schema(description = "报表状态", example = "COMPLETED", allowableValues = { "PENDING", "PROCESSING", "COMPLETED",
            "FAILED", "EXPIRED" })
    private String status;

    /**
     * 模板ID
     */
    @Schema(description = "报表模板ID", example = "template_monthly_consume")
    private String templateId;

    /**
     * 模板名称
     */
    @Schema(description = "报表模板名称", example = "消费月度统计模板")
    private String templateName;

    /**
     * 生成开始时间
     */
    @Schema(description = "报表生成开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 生成完成时间
     */
    @Schema(description = "报表生成完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedTime;

    /**
     * 处理耗时（毫秒）
     */
    @Schema(description = "报表处理耗时（毫秒）", example = "3500")
    private Long processingTime;

    /**
     * 数据总条数
     */
    @Schema(description = "数据总条数", example = "1586")
    private Long totalCount;

    /**
     * 成功处理条数
     */
    @Schema(description = "成功处理条数", example = "1586")
    private Long successCount;

    /**
     * 失败处理条数
     */
    @Schema(description = "失败处理条数", example = "0")
    private Long failureCount;

    /**
     * 分页信息
     */
    @Schema(description = "分页信息")
    private PageInfoVO pageInfo;

    /**
     * 报表数据
     */
    @Schema(description = "报表数据内容")
    private Map<String, Object> data;

    /**
     * 报表列定义
     */
    @Schema(description = "报表列定义信息")
    private List<ColumnInfoVO> columns;

    /**
     * 文件下载信息
     */
    @Schema(description = "文件下载信息")
    private List<DownloadInfoVO> downloadFiles;

    /**
     * 统计摘要信息
     */
    @Schema(description = "统计摘要信息")
    private Map<String, Object> summary;

    /**
     * 生成参数
     */
    @Schema(description = "报表生成参数")
    private Map<String, Object> parameters;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "数据源连接失败")
    private String errorMessage;

    /**
     * 错误详情
     */
    @Schema(description = "错误详细信息")
    private String errorDetails;

    /**
     * 创建用户
     */
    @Schema(description = "报表创建用户", example = "admin")
    private String createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 过期时间
     */
    @Schema(description = "报表过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 报表文件大小（字节）
     */
    @Schema(description = "报表文件大小（字节）", example = "2048576")
    private Long fileSize;

    /**
     * 报表格式
     */
    @Schema(description = "报表文件格式", example = "PDF", allowableValues = { "PDF", "EXCEL", "WORD", "JSON" })
    private String fileFormat;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "MySQL:ioedream_consume")
    private String dataSource;

    /**
     * 生成方式
     */
    @Schema(description = "生成方式", example = "ASYNC", allowableValues = { "SYNC", "ASYNC", "SCHEDULED" })
    private String generationMode;

    /**
     * 优先级
     */
    @Schema(description = "报表优先级", example = "NORMAL", allowableValues = { "LOW", "NORMAL", "HIGH", "URGENT" })
    private String priority;

    /**
     * 是否缓存
     */
    @Schema(description = "是否启用缓存", example = "true")
    private Boolean cached;

    /**
     * 缓存过期时间（秒）
     */
    @Schema(description = "缓存过期时间（秒）", example = "3600")
    private Long cacheExpireTime;

    /**
     * 标签信息
     */
    @Schema(description = "报表标签")
    private List<String> tags;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "月度消费统计报表，包含所有消费类型的详细数据")
    private String remarks;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性")
    private Map<String, Object> extensions;

    /**
     * 是否成功
     * 用于标识操作是否成功
     */
    @Schema(description = "操作是否成功", example = "true")
    private Boolean success;

    /**
     * 消息信息
     * 用于返回操作结果消息
     */
    @Schema(description = "操作结果消息", example = "报表生成成功")
    private String message;

    /**
     * 图表数据
     * 用于存储报表相关的图表数据
     */
    @Schema(description = "图表数据")
    private Map<String, Object> charts;

    // ========== 内部类定义 ==========

    /**
     * 分页信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PageInfoVO implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "当前页码", example = "1")
        private Integer pageNum;

        @Schema(description = "每页大小", example = "20")
        private Integer pageSize;

        @Schema(description = "总页数", example = "80")
        private Integer totalPages;

        @Schema(description = "总记录数", example = "1586")
        private Long totalRecords;

        @Schema(description = "是否有下一页", example = "true")
        private Boolean hasNext;

        @Schema(description = "是否有上一页", example = "false")
        private Boolean hasPrevious;
    }

    /**
     * 列信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ColumnInfoVO implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "列名", example = "consume_amount")
        private String columnName;

        @Schema(description = "列标题", example = "消费金额")
        private String columnTitle;

        @Schema(description = "数据类型", example = "DECIMAL")
        private String dataType;

        @Schema(description = "列宽度", example = "120")
        private Integer width;

        @Schema(description = "是否可排序", example = "true")
        private Boolean sortable;

        @Schema(description = "是否可筛选", example = "true")
        private Boolean filterable;

        @Schema(description = "显示格式", example = "#,##0.00")
        private String format;

        @Schema(description = "对齐方式", example = "RIGHT", allowableValues = { "LEFT", "CENTER", "RIGHT" })
        private String alignment;
    }

    /**
     * 下载信息VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DownloadInfoVO implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "文件ID", example = "file_001")
        private String fileId;

        @Schema(description = "文件名", example = "消费月度统计报表_202511.pdf")
        private String fileName;

        @Schema(description = "文件格式", example = "PDF")
        private String format;

        @Schema(description = "文件大小（字节）", example = "2048576")
        private Long fileSize;

        @Schema(description = "下载路径", example = "/api/report/download/report_001")
        private String downloadUrl;

        @Schema(description = "下载次数", example = "5")
        private Integer downloadCount;

        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTime;
    }

    // ========== 业务方法 ==========

    /**
     * 获取处理成功率
     */
    public BigDecimal getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(successCount != null ? successCount : 0)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    /**
     * 是否处理失败
     */
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }

    /**
     * 是否正在处理中
     */
    public boolean isProcessing() {
        return "PROCESSING".equals(this.status);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return "EXPIRED".equals(this.status) ||
                (expireTime != null && expireTime.isBefore(LocalDateTime.now()));
    }

    /**
     * 获取处理时长描述
     */
    public String getProcessingTimeDescription() {
        if (processingTime == null) {
            return "未知";
        }
        if (processingTime < 1000) {
            return processingTime + "毫秒";
        } else if (processingTime < 60000) {
            return (processingTime / 1000.0) + "秒";
        } else {
            return (processingTime / 60000.0) + "分钟";
        }
    }

    /**
     * 获取文件大小描述
     */
    public String getFileSizeDescription() {
        if (fileSize == null) {
            return "未知";
        }
        if (fileSize < 1024) {
            return fileSize + "B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2fKB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2fGB", fileSize / (1024.0 * 1024 * 1024));
        }
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建成功响应
     *
     * @return 成功响应对象
     */
    public static ReportResponseVO success() {
        ReportResponseVO response = new ReportResponseVO();
        response.setSuccess(true);
        response.setStatus("COMPLETED");
        response.setMessage("操作成功");
        return response;
    }

    /**
     * 创建错误响应
     *
     * @param errorMessage 错误消息
     * @return 错误响应对象
     */
    public static ReportResponseVO error(String errorMessage) {
        ReportResponseVO response = new ReportResponseVO();
        response.setSuccess(false);
        response.setStatus("FAILED");
        response.setMessage(errorMessage);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
