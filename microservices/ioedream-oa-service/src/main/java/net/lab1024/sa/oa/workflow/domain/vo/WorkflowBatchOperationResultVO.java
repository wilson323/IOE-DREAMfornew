package net.lab1024.sa.oa.workflow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流批量操作结果视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "工作流批量操作结果")
public class WorkflowBatchOperationResultVO {

    @Schema(description = "批量操作ID")
    private String batchId;

    @Schema(description = "批量操作名称")
    private String batchName;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作描述")
    private String description;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "操作状态")
    private BatchOperationStatus status;

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "跳过数量")
    private Integer skippedCount;

    @Schema(description = "成功率")
    private Double successRate;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTime;

    @Schema(description = "操作进度")
    private BatchProgress progress;

    @Schema(description = "成功详情")
    private List<OperationResult> successResults;

    @Schema(description = "失败详情")
    private List<OperationResult> failedResults;

    @Schema(description = "跳过详情")
    private List<OperationResult> skippedResults;

    @Schema(description = "操作项结果")
    private List<BatchItemResult> itemResults;

    @Schema(description = "错误消息")
    private String errorMessage;

    @Schema(description = "统计信息")
    private BatchStatistics statistics;

    @Schema(description = "操作历史")
    private List<BatchOperationHistory> operationHistory;

    @Schema(description = "导出结果")
    private ExportResult exportResult;

    /**
     * 批量操作状态枚举
     */
    public enum BatchOperationStatus {
        PENDING("PENDING", "等待中"),
        RUNNING("RUNNING", "运行中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消"),
        PARTIAL_SUCCESS("PARTIAL_SUCCESS", "部分成功"),
        ROLLBACK("ROLLBACK", "回滚中"),
        ROLLED_BACK("ROLLED_BACK", "已回滚");

        private final String code;
        private final String description;

        BatchOperationStatus(String code, String description) {
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
     * 操作进度
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "操作进度")
    public static class BatchProgress {
        @Schema(description = "批次ID")
        private String batchId;

        @Schema(description = "进度百分比")
        private Double progressPercentage;

        @Schema(description = "已处理数量")
        private Integer processedCount;

        @Schema(description = "已完成数量")
        private Integer completedCount;

        @Schema(description = "成功数量")
        private Integer successCount;

        @Schema(description = "失败数量")
        private Integer failedCount;

        @Schema(description = "总数量")
        private Integer totalCount;

        @Schema(description = "当前处理项")
        private String currentItem;

        @Schema(description = "预估剩余时间（秒）")
        private Long estimatedRemainingTime;

        @Schema(description = "处理速度（每秒）")
        private Double processingSpeed;

        @Schema(description = "开始时间")
        private LocalDateTime startTime;

        @Schema(description = "预计完成时间")
        private LocalDateTime estimatedCompletionTime;

        @Schema(description = "操作状态")
        private BatchOperationStatus status;
    }

    /**
     * 操作结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "操作结果")
    public static class OperationResult {
        @Schema(description = "项目ID")
        private String itemId;

        @Schema(description = "项目名称")
        private String itemName;

        @Schema(description = "操作状态")
        private OperationStatus status;

        @Schema(description = "操作结果")
        private String result;

        @Schema(description = "结果消息")
        private String message;

        @Schema(description = "操作时间")
        private LocalDateTime operationTime;

        @Schema(description = "执行耗时（毫秒）")
        private Long executionTime;

        @Schema(description = "错误代码")
        private String errorCode;

        @Schema(description = "错误详情")
        private String errorDetail;

        @Schema(description = "重试次数")
        private Integer retryCount;

        @Schema(description = "返回数据")
        private Map<String, Object> returnData;
    }

    /**
     * 操作状态
     */
    public enum OperationStatus {
        SUCCESS("SUCCESS", "成功"),
        FAILED("FAILED", "失败"),
        SKIPPED("SKIPPED", "跳过"),
        TIMEOUT("TIMEOUT", "超时"),
        CANCELLED("CANCELLED", "取消");

        private final String code;
        private final String description;

        OperationStatus(String code, String description) {
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
     * 批量统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量统计信息")
    public static class BatchStatistics {
        @Schema(description = "平均执行时间（毫秒）")
        private Double averageExecutionTime;

        @Schema(description = "最短执行时间（毫秒）")
        private Long minExecutionTime;

        @Schema(description = "最长执行时间（毫秒）")
        private Long maxExecutionTime;

        @Schema(description = "总执行时间（毫秒）")
        private Long totalExecutionTime;

        @Schema(description = "吞吐量（每秒）")
        private Double throughput;

        @Schema(description = "错误率")
        private Double errorRate;

        @Schema(description = "重试率")
        private Double retryRate;

        @Schema(description = "超时率")
        private Double timeoutRate;

        @Schema(description = "状态分布")
        private Map<String, Integer> statusDistribution;

        @Schema(description = "错误类型分布")
        private Map<String, Integer> errorTypeDistribution;

        @Schema(description = "性能指标")
        private Map<String, Object> performanceMetrics;
    }

    /**
     * 批量操作历史
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量操作历史")
    public static class BatchOperationHistory {
        @Schema(description = "历史ID")
        private String historyId;

        @Schema(description = "批量操作ID")
        private String batchId;

        @Schema(description = "操作类型")
        private String operationType;

        @Schema(description = "操作名称")
        private String operationName;

        @Schema(description = "操作人ID")
        private Long operatorId;

        @Schema(description = "操作人姓名")
        private String operatorName;

        @Schema(description = "操作时间")
        private LocalDateTime operationTime;

        @Schema(description = "操作状态")
        private BatchOperationStatus status;

        @Schema(description = "总数量")
        private Integer totalCount;

        @Schema(description = "成功数量")
        private Integer successCount;

        @Schema(description = "失败数量")
        private Integer failedCount;

        @Schema(description = "操作耗时（毫秒）")
        private Long executionTime;

        @Schema(description = "操作描述")
        private String description;

        @Schema(description = "操作参数")
        private Map<String, Object> operationParameters;

        @Schema(description = "备注")
        private String remarks;
    }

    /**
     * 导出结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "导出结果")
    public static class ExportResult {
        @Schema(description = "导出ID")
        private String exportId;

        @Schema(description = "导出格式")
        private String exportFormat;

        @Schema(description = "导出文件名")
        private String fileName;

        @Schema(description = "文件大小（字节）")
        private Long fileSize;

        @Schema(description = "下载链接")
        private String downloadUrl;

        @Schema(description = "过期时间")
        private LocalDateTime expireTime;

        @Schema(description = "导出时间")
        private LocalDateTime exportTime;

        @Schema(description = "导出状态")
        private ExportStatus status;

        @Schema(description = "导出进度")
        private Double exportProgress;

        @Schema(description = "导出记录数")
        private Integer recordCount;

        @Schema(description = "导出字段")
        private List<String> exportFields;
    }

    /**
     * 导出状态
     */
    public enum ExportStatus {
        GENERATING("GENERATING", "生成中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        EXPIRED("EXPIRED", "已过期");

        private final String code;
        private final String description;

        ExportStatus(String code, String description) {
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
     * 批量操作项结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量操作项结果")
    public static class BatchItemResult {
        @Schema(description = "项目ID")
        private String itemId;

        @Schema(description = "项目名称")
        private String itemName;

        @Schema(description = "目标ID")
        private String targetId;

        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "结果消息")
        private String message;

        @Schema(description = "错误代码")
        private String errorCode;

        @Schema(description = "执行耗时（毫秒）")
        private Long executionTime;

        @Schema(description = "操作时间戳")
        private LocalDateTime timestamp;

        @Schema(description = "返回数据")
        private Map<String, Object> data;
    }
}
