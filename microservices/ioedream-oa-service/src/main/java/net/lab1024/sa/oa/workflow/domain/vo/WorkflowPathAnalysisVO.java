package net.lab1024.sa.oa.workflow.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 工作流路径分析视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Builder
@Schema(description = "工作流路径分析结果")
public class WorkflowPathAnalysisVO {

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "分析时间")
    private String analysisTime;

    @Schema(description = "总路径数")
    private Integer totalPaths;

    @Schema(description = "有效路径数")
    private Integer validPaths;

    @Schema(description = "关键路径")
    private CriticalPath criticalPath;

    @Schema(description = "所有路径详情")
    private List<ProcessPath> paths;

    @Schema(description = "节点分析")
    private Map<String, NodeAnalysis> nodeAnalysis;

    @Schema(description = "路径概率分布")
    private Map<String, Double> pathProbabilities;

    @Schema(description = "性能指标")
    private PerformanceMetrics performanceMetrics;

    /**
     * 关键路径
     */
    @Data
    @Builder
    @Schema(description = "关键路径")
    public static class CriticalPath {
        @Schema(description = "路径长度")
        private Integer pathLength;

        @Schema(description = "预计总时间（毫秒）")
        private Long estimatedTotalTime;

        @Schema(description = "关键节点")
        private List<String> criticalNodes;

        @Schema(description = "关键活动")
        private List<CriticalActivity> criticalActivities;
    }

    /**
     * 关键活动
     */
    @Data
    @Builder
    @Schema(description = "关键活动")
    public static class CriticalActivity {
        @Schema(description = "活动ID")
        private String activityId;

        @Schema(description = "活动名称")
        private String activityName;

        @Schema(description = "最早开始时间")
        private Long earliestStartTime;

        @Schema(description = "最晚开始时间")
        private Long latestStartTime;

        @Schema(description = "浮动时间")
        private Long floatTime;

        @Schema(description = "是否为关键活动")
        private Boolean isCritical;
    }

    /**
     * 流程路径
     */
    @Data
    @Builder
    @Schema(description = "流程路径")
    public static class ProcessPath {
        @Schema(description = "路径ID")
        private String pathId;

        @Schema(description = "路径名称")
        private String pathName;

        @Schema(description = "路径长度")
        private Integer pathLength;

        @Schema(description = "预计时间（毫秒）")
        private Long estimatedTime;

        @Schema(description = "路径概率")
        private Double probability;

        @Schema(description = "路径节点")
        private List<PathNode> nodes;

        @Schema(description = "路径条件")
        private List<PathCondition> conditions;

        @Schema(description = "是否为最优路径")
        private Boolean isOptimal;
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

        @Schema(description = "并行组ID（如果属于并行网关）")
        private String parallelGroupId;

        @Schema(description = "前置条件")
        private List<String> preConditions;

        @Schema(description = "后置条件")
        private List<String> postConditions;

        @Schema(description = "资源需求")
        private List<String> resourceRequirements;
    }

    /**
     * 路径条件
     */
    @Data
    @Builder
    @Schema(description = "路径条件")
    public static class PathCondition {
        @Schema(description = "条件ID")
        private String conditionId;

        @Schema(description = "条件表达式")
        private String conditionExpression;

        @Schema(description = "条件描述")
        private String conditionDescription;

        @Schema(description = "条件类型")
        private String conditionType;

        @Schema(description = "取值概率")
        private Map<String, Double> valueProbabilities;
    }

    /**
     * 节点分析
     */
    @Data
    @Builder
    @Schema(description = "节点分析")
    public static class NodeAnalysis {
        @Schema(description = "节点ID")
        private String nodeId;

        @Schema(description = "节点名称")
        private String nodeName;

        @Schema(description = "节点类型")
        private String nodeType;

        @Schema(description = "入度")
        private Integer inDegree;

        @Schema(description = "出度")
        private Integer outDegree;

        @Schema(description = "通过该节点的路径数")
        private Integer pathCount;

        @Schema(description = "平均停留时间（毫秒）")
        private Double averageStayTime;

        @Schema(description = "瓶颈风险等级")
        private String bottleneckRiskLevel;

        @Schema(description = "优化建议")
        private List<String> optimizationSuggestions;

        @Schema(description = "并行能力")
        private ParallelCapability parallelCapability;
    }

    /**
     * 并行能力
     */
    @Data
    @Builder
    @Schema(description = "并行能力")
    public static class ParallelCapability {
        @Schema(description = "是否支持并行")
        private Boolean supportsParallel;

        @Schema(description = "最大并行数")
        private Integer maxParallelInstances;

        @Schema(description = "并行组")
        private String parallelGroup;

        @Schema(description = "同步类型")
        private String synchronizationType;
    }

    /**
     * 性能指标
     */
    @Data
    @Builder
    @Schema(description = "性能指标")
    public static class PerformanceMetrics {
        @Schema(description = "平均路径长度")
        private Double averagePathLength;

        @Schema(description = "最短路径长度")
        private Integer shortestPathLength;

        @Schema(description = "最长路径长度")
        private Integer longestPathLength;

        @Schema(description = "路径复杂度")
        private Double pathComplexity;

        @Schema(description = "并行度")
        private Double parallelismDegree;

        @Schema(description = "可扩展性评分")
        private Double scalabilityScore;

        @Schema(description = "可维护性评分")
        private Double maintainabilityScore;

        @Schema(description = "效率评分")
        private Double efficiencyScore;

        @Schema(description = "瓶颈节点数")
        private Integer bottleneckNodeCount;

        @Schema(description = "关键路径占比")
        private Double criticalPathRatio;
    }
}