package net.lab1024.sa.admin.module.consume.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 索引分析结果
 *
 * @author IOE-DREAM
 * @date 2025-11-21
 */


@Schema(description = "索引分析结果")
public class IndexAnalysisResult {

    /**
     * 分析时间
     */
    @Schema(description = "分析时间")
    private LocalDateTime analysisTime;

    /**
     * 总表数量
     */
    @Schema(description = "总表数量")
    private Integer totalTables;

    /**
     * 已分析表数量
     */
    @Schema(description = "已分析表数量")
    private Integer analyzedTables;

    /**
     * 建议索引列表
     */
    @Schema(description = "建议索引列表")
    private List<IndexSuggestion> suggestions;

    /**
     * 索引使用统计
     */
    @Schema(description = "索引使用统计")
    private Map<String, IndexUsageStats> usageStats;

    /**
     * 性能影响评估
     */
    @Schema(description = "性能影响评估")
    private PerformanceImpact performanceImpact;

    /**
     * 索引建议
     */
    
    
    @Schema(description = "索引建议")
    public static class IndexSuggestion {

        /**
         * 表名
         */
        @Schema(description = "表名")
        private String tableName;

        /**
         * 建议索引类型
         */
        @Schema(description = "建议索引类型")
        private String indexType;

        /**
         * 建议索引字段
         */
        @Schema(description = "建议索引字段")
        private List<String> columns;

        /**
         * 预估性能提升
         */
        @Schema(description = "预估性能提升")
        private String estimatedImprovement;

        /**
         * 建议优先级
         */
        @Schema(description = "建议优先级")
        private Integer priority;
    }

    /**
     * 索引使用统计
     */
    
    
    @Schema(description = "索引使用统计")
    public static class IndexUsageStats {

        /**
         * 索引名称
         */
        @Schema(description = "索引名称")
        private String indexName;

        /**
         * 使用次数
         */
        @Schema(description = "使用次数")
        private Long usageCount;

        /**
         * 最后使用时间
         */
        @Schema(description = "最后使用时间")
        private LocalDateTime lastUsedTime;

        /**
         * 使用率
         */
        @Schema(description = "使用率")
        private Double usageRate;
    }

    /**
     * 性能影响评估
     */
    
    
    @Schema(description = "性能影响评估")
    public static class PerformanceImpact {

        /**
         * 预估查询性能提升
         */
        @Schema(description = "预估查询性能提升")
        private String queryImprovement;

        /**
         * 预估存储空间增加
         */
        @Schema(description = "预估存储空间增加")
        private String storageIncrease;

        /**
         * 预估写入性能影响
         */
        @Schema(description = "预估写入性能影响")
        private String writeImpact;

        /**
         * 总体评分
         */
        @Schema(description = "总体评分")
        private Integer overallScore;
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)











}
