package net.lab1024.sa.consume.util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据库索引分析结果
 * 用于存储数据库索引分析的详细结果信息
 *
 * @author IOE-DREAM Team
 * @version 3.0.0
 * @since 2025-12-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexAnalysisResult {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 分析是否成功
     */
    private Boolean success;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 表结构信息
     */
    private TableStructureInfo tableStructure;

    /**
     * 现有索引列表
     */
    private List<IndexInfo> existingIndexes;

    /**
     * 字段映射信息
     */
    private List<FieldMappingInfo> fieldMappings;

    /**
     * 索引优化建议列表
     */
    private List<IndexSuggestion> suggestions;

    /**
     * 索引使用统计
     */
    private Map<String, IndexUsageStats> usageStats;

    /**
     * 错误信息（如果分析失败）
     */
    private String errorMessage;

    /**
     * 表结构信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableStructureInfo {
        /**
         * 表名
         */
        private String tableName;

        /**
         * 字段列表
         */
        private List<ColumnInfo> columns;

        /**
         * 主键字段
         */
        private List<String> primaryKeys;

        /**
         * 表行数
         */
        private Long rowCount;

        /**
         * 表大小（字节）
         */
        private Long tableSize;
    }

    /**
     * 字段信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfo {
        /**
         * 字段名
         */
        private String columnName;

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 是否可为空
         */
        private Boolean nullable;

        /**
         * 默认值
         */
        private String defaultValue;

        /**
         * 字段注释
         */
        private String comment;
    }

    /**
     * 索引信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexInfo {
        /**
         * 索引名
         */
        private String indexName;

        /**
         * 索引类型（PRIMARY, UNIQUE, INDEX）
         */
        private String indexType;

        /**
         * 索引字段列表
         */
        private List<String> columns;

        /**
         * 是否唯一索引
         */
        private Boolean unique;

        /**
         * 索引基数
         */
        private Long cardinality;
    }

    /**
     * 字段映射信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldMappingInfo {
        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 数据库字段名
         */
        private String columnName;

        /**
         * Java类型
         */
        private String javaType;

        /**
         * 数据库类型
         */
        private String dbType;
    }

    /**
     * 索引优化建议
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexSuggestion {
        /**
         * 建议类型（CREATE, DROP, MODIFY）
         */
        private String suggestionType;

        /**
         * 建议的索引名
         */
        private String indexName;

        /**
         * 建议的索引字段
         */
        private List<String> columns;

        /**
         * 建议原因
         */
        private String reason;

        /**
         * 优先级（HIGH, MEDIUM, LOW）
         */
        private String priority;

        /**
         * 预期性能提升（百分比）
         */
        private Double expectedImprovement;
    }

    /**
     * 索引使用统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexUsageStats {
        /**
         * 索引名
         */
        private String indexName;

        /**
         * 使用次数
         */
        private Long usageCount;

        /**
         * 选择率
         */
        private Double selectivity;

        /**
         * 平均扫描行数
         */
        private Long avgRowsScanned;
    }
}
