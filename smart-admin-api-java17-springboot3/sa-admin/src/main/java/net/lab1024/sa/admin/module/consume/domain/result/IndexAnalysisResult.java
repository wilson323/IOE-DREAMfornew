package net.lab1024.sa.admin.module.consume.domain.result;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 索引分析结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */




@Schema(description = "索引分析结果")
public class IndexAnalysisResult {

    @Schema(description = "分析ID")
    private Long analysisId;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "总索引数量")
    private Integer totalIndexCount;

    @Schema(description = "使用的索引数量")
    private Integer usedIndexCount;

    @Schema(description = "未使用的索引数量")
    private Integer unusedIndexCount;

    @Schema(description = "重复索引数量")
    private Integer duplicateIndexCount;

    @Schema(description = "优化建议")
    private List<String> optimizationSuggestions;

    @Schema(description = "索引详细信息")
    private List<IndexInfo> indexDetails;

    @Schema(description = "分析耗时(毫秒)")
    private Long analysisTimeMs;

    @Schema(description = "分析时间")
    private LocalDateTime analysisTime;

    @Schema(description = "是否需要优化")
    private Boolean needsOptimization;

    /**
     * 索引信息内部类
     */
    
    
    
    
    @Schema(description = "索引信息")
    public static class IndexInfo {
        @Schema(description = "索引名称")
        private String indexName;

        @Schema(description = "索引列")
        private List<String> columns;

        @Schema(description = "索引类型")
        private String indexType;

        @Schema(description = "是否唯一")
        private Boolean unique;

        @Schema(description = "使用次数")
        private Long usageCount;

        @Schema(description = "大小(KB)")
        private Long sizeKB;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }

    /**
     * 创建分析成功的实例
     */
    public static IndexAnalysisResult success(String tableName, int totalIndexCount, int usedIndexCount) {
        return IndexAnalysisResult.builder()
                .analysisId(System.currentTimeMillis())
                .tableName(tableName)
                .totalIndexCount(totalIndexCount)
                .usedIndexCount(usedIndexCount)
                .unusedIndexCount(totalIndexCount - usedIndexCount)
                .analysisTime(LocalDateTime.now())
                .needsOptimization(usedIndexCount < totalIndexCount)
                .build();
    }

    /**
     * 创建分析失败的实例
     */
    public static IndexAnalysisResult failure(String tableName, String error) {
        return IndexAnalysisResult.builder()
                .tableName(tableName)
                .analysisTime(LocalDateTime.now())
                .needsOptimization(false)
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)




















    public Boolean isNeedsOptimization() {
        return needsOptimization;
    }

}
