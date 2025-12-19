package net.lab1024.sa.oa.workflow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工作流仿真结果视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Builder
@Schema(description = "工作流仿真结果")
public class WorkflowSimulationResultVO {

    @Schema(description = "仿真ID")
    private String simulationId;

    @Schema(description = "仿真名称")
    private String simulationName;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "仿真开始时间")
    private LocalDateTime startTime;

    @Schema(description = "仿真结束时间")
    private LocalDateTime endTime;

    @Schema(description = "仿真状态")
    private SimulationStatus status;

    @Schema(description = "总仿真次数")
    private Integer totalSimulations;

    @Schema(description = "成功仿真次数")
    private Integer successfulSimulations;

    @Schema(description = "失败仿真次数")
    private Integer failedSimulations;

    @Schema(description = "平均执行时间（毫秒）")
    private Double averageExecutionTime;

    @Schema(description = "最短执行时间（毫秒）")
    private Long minExecutionTime;

    @Schema(description = "最长执行时间（毫秒）")
    private Long maxExecutionTime;

    @Schema(description = "路径分析结果")
    private PathAnalysisResult pathAnalysis;

    @Schema(description = "瓶颈分析结果")
    private List<BottleneckAnalysis> bottlenecks;

    @Schema(description = "优化建议")
    private List<OptimizationSuggestion> optimizationSuggestions;

    @Schema(description = "详细指标")
    private DetailedMetrics detailedMetrics;

    @Schema(description = "时间预测")
    private ProcessTimePrediction timePrediction;

    @Schema(description = "任务分配仿真")
    private TaskAssignmentSimulation taskAssignmentSimulation;

    @Schema(description = "流程验证结果")
    private ProcessValidationResult validationResult;

    @Schema(description = "流程报告")
    private ProcessReport processReport;

    /**
     * 仿真状态枚举
     */
    public enum SimulationStatus {
        PENDING("PENDING", "等待中"),
        RUNNING("RUNNING", "运行中"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String description;

        SimulationStatus(String code, String description) {
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
     * 路径分析结果
     */
    @Data
    @Builder
    @Schema(description = "路径分析结果")
    public static class PathAnalysisResult {
        @Schema(description = "可能路径数量")
        private Integer possiblePaths;

        @Schema(description = "最短路径长度")
        private Integer shortestPathLength;

        @Schema(description = "最长路径长度")
        private Integer longestPathLength;

        @Schema(description = "平均路径长度")
        private Double averagePathLength;

        @Schema(description = "关键路径")
        private List<String> criticalPath;

        @Schema(description = "所有可能路径")
        private List<List<PathNode>> allPaths;

        @Schema(description = "路径概率分布")
        private Map<String, Double> pathProbabilities;
    }

    /**
     * 路径节点
     */
    @Data
    @Builder
    @Schema(description = "路径节点")
    public static class PathNode {
        @Schema(description = "节点ID")
        private String nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点类型")
        private String nodeType;

        @Schema(description = "预计执行时间（毫秒）")
        private Long estimatedTime;

        @Schema(description = "执行概率")
        private Double probability;
    }

    /**
     * 瓶颈分析
     */
    @Data
    @Builder
    @Schema(description = "瓶颈分析")
    public static class BottleneckAnalysis {
        @Schema(description = "瓶颈节点ID")
        private String nodeId;

        @Schema(description = "瓶颈节点名称")
        private String nodeName;

        @Schema(description = "瓶颈类型")
        private BottleneckType bottleneckType;

        @Schema(description = "瓶颈严重程度")
        private Severity severity;

        @Schema(description = "平均等待时间（毫秒）")
        private Double averageWaitTime;

        @Schema(description = "影响范围")
        private String impactScope;

        @Schema(description = "建议解决方案")
        private List<String> solutions;

        @Schema(description = "优化后预期改善")
        private String expectedImprovement;
    }

    /**
     * 瓶颈类型
     */
    public enum BottleneckType {
        TASK_OVERLOAD("TASK_OVERLOAD", "任务过载"),
        SEQUENTIAL_DEPENDENCY("SEQUENTIAL_DEPENDENCY", "串行依赖"),
        RESOURCE_CONTENTION("RESOURCE_CONTENTION", "资源竞争"),
        APPROVAL_DELAY("APPROVAL_DELAY", "审批延迟"),
        SYSTEM_PERFORMANCE("SYSTEM_PERFORMANCE", "系统性能");

        private final String code;
        private final String description;

        BottleneckType(String code, String description) {
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
     * 严重程度
     */
    public enum Severity {
        LOW("LOW", "低"),
        MEDIUM("MEDIUM", "中"),
        HIGH("HIGH", "高"),
        CRITICAL("CRITICAL", "严重");

        private final String code;
        private final String description;

        Severity(String code, String description) {
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
     * 优化建议
     */
    @Data
    @Builder
    @Schema(description = "优化建议")
    public static class OptimizationSuggestion {
        @Schema(description = "建议ID")
        private String suggestionId;

        @Schema(description = "建议类型")
        private SuggestionType type;

        @Schema(description = "建议标题")
        private String title;

        @Schema(description = "建议描述")
        private String description;

        @Schema(description = "优先级")
        private Priority priority;

        @Schema(description = "预计改进效果")
        private String expectedImprovement;

        @Schema(description = "实施难度")
        private String implementationDifficulty;

        @Schema(description = "实施步骤")
        private List<String> implementationSteps;
    }

    /**
     * 建议类型
     */
    public enum SuggestionType {
        PARALLEL_EXECUTION("PARALLEL_EXECUTION", "并行执行"),
        AUTOMATION("AUTOMATION", "自动化"),
        RESOURCE_OPTIMIZATION("RESOURCE_OPTIMIZATION", "资源优化"),
        PROCESS_REDESIGN("PROCESS_REDESIGN", "流程重设计"),
        POLICY_ADJUSTMENT("POLICY_ADJUSTMENT", "策略调整");

        private final String code;
        private final String description;

        SuggestionType(String code, String description) {
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
     * 优先级
     */
    public enum Priority {
        LOW("LOW", "低"),
        MEDIUM("MEDIUM", "中"),
        HIGH("HIGH", "高"),
        URGENT("URGENT", "紧急");

        private final String code;
        private final String description;

        Priority(String code, String description) {
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
     * 详细指标
     */
    @Data
    @Builder
    @Schema(description = "详细指标")
    public static class DetailedMetrics {
        @Schema(description = "任务完成率")
        private Double taskCompletionRate;

        @Schema(description = "平均任务处理时间")
        private Double averageTaskProcessingTime;

        @Schema(description = "流程吞吐量（每小时）")
        private Double processThroughput;

        @Schema(description = "资源利用率")
        private Map<String, Double> resourceUtilization;

        @Schema(description = "等待时间统计")
        private Map<String, Double> waitTimeStatistics;

        @Schema(description = "错误率")
        private Double errorRate;

        @Schema(description = "返工率")
        private Double reworkRate;
    }

    /**
     * 时间预测
     */
    @Data
    @Builder
    @Schema(description = "时间预测")
    public static class ProcessTimePrediction {
        @Schema(description = "预计总时间（毫秒）")
        private Long estimatedTotalTime;

        @Schema(description = "最短时间（毫秒）")
        private Long minTime;

        @Schema(description = "最长时间（毫秒）")
        private Long maxTime;

        @Schema(description = "平均时间（毫秒）")
        private Double averageTime;

        @Schema(description = "置信度")
        private Double confidenceLevel;

        @Schema(description = "各阶段预测时间")
        private Map<String, Long> stagePredictions;

        @Schema(description = "影响因素")
        private List<String> influencingFactors;
    }

    /**
     * 任务分配仿真
     */
    @Data
    @Builder
    @Schema(description = "任务分配仿真")
    public static class TaskAssignmentSimulation {
        @Schema(description = "仿真结果")
        private List<TaskAssignmentResult> results;

        @Schema(description = "负载均衡度")
        private Double loadBalanceScore;

        @Schema(description = "推荐分配方案")
        private List<RecommendedAssignment> recommendedAssignments;
    }

    /**
     * 任务分配结果
     */
    @Data
    @Builder
    @Schema(description = "任务分配结果")
    public static class TaskAssignmentResult {
        @Schema(description = "任务ID")
        private String taskId;

        @Schema(description = "任务名称")
        private String taskName;

        @Schema(description = "分配用户ID")
        private Long assignedUserId;

        @Schema(description = "分配用户名")
        private String assignedUserName;

        @Schema(description = "预计完成时间")
        private Long estimatedCompletionTime;

        @Schema(description = "工作负载")
        private Double workload;
    }

    /**
     * 推荐分配
     */
    @Data
    @Builder
    @Schema(description = "推荐分配")
    public static class RecommendedAssignment {
        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String userName;

        @Schema(description = "推荐原因")
        private String reason;

        @Schema(description = "适合度评分")
        private Double suitabilityScore;
    }

    /**
     * 流程验证结果
     */
    @Data
    @Builder
    @Schema(description = "流程验证结果")
    public static class ProcessValidationResult {
        @Schema(description = "是否有效")
        private Boolean isValid;

        @Schema(description = "验证错误")
        private List<ValidationError> errors;

        @Schema(description = "验证警告")
        private List<ValidationWarning> warnings;

        @Schema(description = "验证分数")
        private Double validationScore;

        @Schema(description = "建议改进")
        private List<String> recommendations;
    }

    /**
     * 验证错误
     */
    @Data
    @Builder
    @Schema(description = "验证错误")
    public static class ValidationError {
        @Schema(description = "错误代码")
        private String errorCode;

        @Schema(description = "错误消息")
        private String errorMessage;

        @Schema(description = "错误位置")
        private String errorLocation;

        @Schema(description = "错误类型")
        private String errorType;
    }

    /**
     * 验证警告
     */
    @Data
    @Builder
    @Schema(description = "验证警告")
    public static class ValidationWarning {
        @Schema(description = "警告代码")
        private String warningCode;

        @Schema(description = "警告消息")
        private String warningMessage;

        @Schema(description = "警告位置")
        private String warningLocation;

        @Schema(description = "建议操作")
        private String suggestedAction;
    }

    /**
     * 流程报告
     */
    @Data
    @Builder
    @Schema(description = "流程报告")
    public static class ProcessReport {
        @Schema(description = "报告ID")
        private String reportId;

        @Schema(description = "报告类型")
        private String reportType;

        @Schema(description = "报告标题")
        private String reportTitle;

        @Schema(description = "生成时间")
        private LocalDateTime generatedTime;

        @Schema(description = "报告内容")
        private String reportContent;

        @Schema(description = "报告格式")
        private String reportFormat;

        @Schema(description = "下载链接")
        private String downloadUrl;
    }
}