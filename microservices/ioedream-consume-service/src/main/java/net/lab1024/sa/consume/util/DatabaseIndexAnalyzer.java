package net.lab1024.sa.consume.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.SystemException;

/**
 * 数据库索引分析工具类
 * 提供数据库索引分析、性能监控和优化建议功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Resource依赖注入（符合架构规范）
 * - 完整的异常处理和日志记录
 * - 企业级代码质量标准
 *
 * @author IOE-DREAM Team
 * @version 3.0.0
 * @since 2025-12-02
 */
@Slf4j
@Component
public class DatabaseIndexAnalyzer {

    @Resource
    private DataSource dataSource;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.database-type:mysql}")
    private String databaseType;

    @Value("${spring.datasource.database-name:ioe_dream}")
    private String databaseName;

    /**
     * 分析指定表的索引使用情况
     *
     * @param tableName 表名
     * @return 索引分析结果
     */
    public IndexAnalysisResult analyzeTableIndex(String tableName) {
        try {
            log.info("[索引分析] 开始分析表索引: tableName={}", tableName);

            // 1. 获取表结构信息
            IndexAnalysisResult.TableStructureInfo tableInfo = getTableStructure(tableName);

            // 2. 获取现有索引信息
            List<IndexAnalysisResult.IndexInfo> existingIndexes = getTableIndexes(tableName);

            // 3. 分析字段类型映射
            List<IndexAnalysisResult.FieldMappingInfo> fieldMappings = analyzeFieldMappings(tableName);

            // 4. 生成索引优化建议
            List<IndexAnalysisResult.IndexSuggestion> suggestions = generateIndexSuggestions(
                    tableInfo, existingIndexes);

            // 5. 分析索引使用情况（MySQL特有）
            Map<String, IndexAnalysisResult.IndexUsageStats> usageStats = new HashMap<>();
            if ("mysql".equalsIgnoreCase(databaseType)) {
                usageStats = getIndexUsageStats(tableName);
            }

            IndexAnalysisResult result = IndexAnalysisResult.builder()
                    .tableName(tableName)
                    .tableStructure(tableInfo)
                    .existingIndexes(existingIndexes)
                    .fieldMappings(fieldMappings)
                    .suggestions(suggestions)
                    .usageStats(usageStats)
                    .analysisTime(LocalDateTime.now())
                    .success(true)
                    .build();

            log.info("[索引分析] 表索引分析完成: tableName={}, indexes={}, suggestions={}",
                    tableName, existingIndexes.size(), suggestions.size());
            return result;

        } catch (Exception e) {
            log.error("[索引分析] 分析表索引失败: tableName={}", tableName, e);
            return IndexAnalysisResult.builder()
                    .tableName(tableName)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .analysisTime(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 获取表结构信息
     *
     * @param tableName 表名
     * @return 表结构信息
     */
    private IndexAnalysisResult.TableStructureInfo getTableStructure(String tableName) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            IndexAnalysisResult.TableStructureInfo.TableStructureInfoBuilder builder =
                    IndexAnalysisResult.TableStructureInfo.builder()
                            .tableName(tableName);

            // 获取字段信息
            List<IndexAnalysisResult.ColumnInfo> columns = new ArrayList<>();
            ResultSet columnsRs = null;
            try {
                columnsRs = metaData.getColumns(databaseName, null, tableName, null);
                while (columnsRs.next()) {
                    IndexAnalysisResult.ColumnInfo column = IndexAnalysisResult.ColumnInfo.builder()
                            .columnName(columnsRs.getString("COLUMN_NAME"))
                            .dataType(columnsRs.getString("TYPE_NAME"))
                            .nullable(columnsRs.getInt("NULLABLE") == DatabaseMetaData.columnNullable)
                            .defaultValue(columnsRs.getString("COLUMN_DEF"))
                            .comment(columnsRs.getString("REMARKS"))
                            .build();
                    columns.add(column);
                }
            } catch (SQLException e) {
                log.error("[索引分析] 获取字段信息失败: tableName={}", tableName, e);
            } finally {
                if (columnsRs != null) {
                    try {
                        columnsRs.close();
                    } catch (SQLException e) {
                        log.warn("[索引分析] 关闭ResultSet失败", e);
                    }
                }
            }
            builder.columns(columns);

            // 获取主键信息
            List<String> primaryKeys = new ArrayList<>();
            ResultSet pkRs = null;
            try {
                pkRs = metaData.getPrimaryKeys(databaseName, null, tableName);
                while (pkRs.next()) {
                    primaryKeys.add(pkRs.getString("COLUMN_NAME"));
                }
            } catch (SQLException e) {
                log.error("[索引分析] 获取主键信息失败: tableName={}", tableName, e);
            } finally {
                if (pkRs != null) {
                    try {
                        pkRs.close();
                    } catch (SQLException e) {
                        log.warn("[索引分析] 关闭ResultSet失败", e);
                    }
                }
            }
            builder.primaryKeys(primaryKeys);

            // 获取表行数和大小（MySQL）
            if ("mysql".equalsIgnoreCase(databaseType)) {
                String sql = "SELECT " +
                        "TABLE_ROWS as row_count, " +
                        "DATA_LENGTH + INDEX_LENGTH as table_size " +
                        "FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
                try {
                    Map<String, Object> stats = jdbcTemplate.queryForMap(sql, databaseName, tableName);
                    builder.rowCount(((Number) stats.get("row_count")).longValue());
                    builder.tableSize(((Number) stats.get("table_size")).longValue());
                } catch (Exception e) {
                    log.warn("[索引分析] 获取表统计信息失败: tableName={}", tableName, e);
                }
            }

            return builder.build();
        } catch (SQLException e) {
            log.error("[索引分析] 获取表结构失败: tableName={}", tableName, e);
            throw new SystemException("DATABASE_TABLE_STRUCTURE_ERROR", "获取表结构失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("[索引分析] 关闭Connection失败", e);
                }
            }
        }
    }

    /**
     * 获取表的索引信息
     *
     * @param tableName 表名
     * @return 索引列表
     */
    private List<IndexAnalysisResult.IndexInfo> getTableIndexes(String tableName) {
        List<IndexAnalysisResult.IndexInfo> indexes = new ArrayList<>();
        Map<String, IndexAnalysisResult.IndexInfo> indexMap = new HashMap<>();

        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            rs = metaData.getIndexInfo(databaseName, null, tableName, false, false);
            while (rs.next()) {
                try {
                    String indexName = rs.getString("INDEX_NAME");
                    if (indexName == null) {
                        continue; // 跳过统计信息
                    }

                    // 获取索引基本信息
                    boolean unique = !rs.getBoolean("NON_UNIQUE");
                    String typeStr = unique ? "UNIQUE" : (indexName.equals("PRIMARY") ? "PRIMARY" : "INDEX");
                    long cardinality = rs.getLong("CARDINALITY");
                    String columnName = rs.getString("COLUMN_NAME");

                    // 获取或创建索引信息（避免在lambda中使用外部变量）
                    IndexAnalysisResult.IndexInfo index = indexMap.get(indexName);
                    if (index == null) {
                        index = IndexAnalysisResult.IndexInfo.builder()
                                .indexName(indexName)
                                .indexType(typeStr)
                                .unique(unique)
                                .columns(new ArrayList<>())
                                .cardinality(cardinality)
                                .build();
                        indexMap.put(indexName, index);
                    }

                    // 添加字段到索引
                    if (columnName != null && !index.getColumns().contains(columnName)) {
                        index.getColumns().add(columnName);
                    }
                } catch (SQLException e) {
                    log.warn("[索引分析] 解析索引信息失败", e);
                }
            }
        } catch (SQLException e) {
            log.error("[索引分析] 获取表索引失败: tableName={}", tableName, e);
            throw new SystemException("GET_TABLE_INDEX_ERROR", "获取表索引失败: " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.warn("[索引分析] 关闭ResultSet失败", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("[索引分析] 关闭Connection失败", e);
                }
            }
        }

        indexes.addAll(indexMap.values());
        return indexes;
    }

    /**
     * 分析字段类型映射
     *
     * @param tableName 表名
     * @return 字段映射列表
     */
    private List<IndexAnalysisResult.FieldMappingInfo> analyzeFieldMappings(String tableName) {
        List<IndexAnalysisResult.FieldMappingInfo> mappings = new ArrayList<>();

        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            rs = metaData.getColumns(databaseName, null, tableName, null);
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dbType = rs.getString("TYPE_NAME");
                String javaType = mapDbTypeToJavaType(dbType);

                IndexAnalysisResult.FieldMappingInfo mapping = IndexAnalysisResult.FieldMappingInfo.builder()
                        .fieldName(convertColumnNameToFieldName(columnName))
                        .columnName(columnName)
                        .javaType(javaType)
                        .dbType(dbType)
                        .build();
                mappings.add(mapping);
            }
        } catch (SQLException e) {
            log.error("[索引分析] 分析字段映射失败: tableName={}", tableName, e);
            throw new SystemException("ANALYZE_FIELD_MAPPING_ERROR", "分析字段映射失败: " + e.getMessage(), e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.warn("[索引分析] 关闭ResultSet失败", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("[索引分析] 关闭Connection失败", e);
                }
            }
        }

        return mappings;
    }

    /**
     * 生成索引优化建议
     *
     * @param tableInfo      表结构信息
     * @param existingIndexes 现有索引列表
     * @return 优化建议列表
     */
    private List<IndexAnalysisResult.IndexSuggestion> generateIndexSuggestions(
            IndexAnalysisResult.TableStructureInfo tableInfo,
            List<IndexAnalysisResult.IndexInfo> existingIndexes) {

        List<IndexAnalysisResult.IndexSuggestion> suggestions = new ArrayList<>();

        // 检查是否有主键
        boolean hasPrimaryKey = existingIndexes.stream()
                .anyMatch(idx -> "PRIMARY".equals(idx.getIndexType()));

        if (!hasPrimaryKey && tableInfo.getPrimaryKeys() != null && !tableInfo.getPrimaryKeys().isEmpty()) {
            IndexAnalysisResult.IndexSuggestion suggestion = IndexAnalysisResult.IndexSuggestion.builder()
                    .suggestionType("CREATE")
                    .indexName("PRIMARY")
                    .columns(tableInfo.getPrimaryKeys())
                    .reason("表缺少主键索引，建议创建主键索引以提升查询性能")
                    .priority("HIGH")
                    .expectedImprovement(50.0)
                    .build();
            suggestions.add(suggestion);
        }

        // 检查常用查询字段是否有索引
        // 这里简化处理，实际应该分析SQL查询日志
        List<String> commonQueryFields = List.of("create_time", "update_time", "deleted_flag", "status");
        for (String field : commonQueryFields) {
            boolean hasIndex = existingIndexes.stream()
                    .anyMatch(idx -> idx.getColumns().contains(field));

            if (!hasIndex) {
                IndexAnalysisResult.IndexSuggestion suggestion = IndexAnalysisResult.IndexSuggestion.builder()
                        .suggestionType("CREATE")
                        .indexName("idx_" + tableInfo.getTableName() + "_" + field)
                        .columns(List.of(field))
                        .reason("字段 " + field + " 常用于查询条件，建议创建索引")
                        .priority("MEDIUM")
                        .expectedImprovement(30.0)
                        .build();
                suggestions.add(suggestion);
            }
        }

        return suggestions;
    }

    /**
     * 获取索引使用统计（MySQL特有）
     *
     * @param tableName 表名
     * @return 索引使用统计
     */
    private Map<String, IndexAnalysisResult.IndexUsageStats> getIndexUsageStats(String tableName) {
        Map<String, IndexAnalysisResult.IndexUsageStats> stats = new HashMap<>();

        if (!"mysql".equalsIgnoreCase(databaseType)) {
            return stats;
        }

        try {
            // 查询索引使用统计（需要开启performance_schema）
            String sql = "SELECT " +
                    "OBJECT_SCHEMA, " +
                    "OBJECT_NAME, " +
                    "INDEX_NAME, " +
                    "COUNT_FETCH, " +
                    "COUNT_INSERT, " +
                    "COUNT_UPDATE, " +
                    "COUNT_DELETE " +
                    "FROM performance_schema.table_io_waits_summary_by_index_usage " +
                    "WHERE OBJECT_SCHEMA = ? AND OBJECT_NAME = ?";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, databaseName, tableName);
            for (Map<String, Object> row : results) {
                String indexName = (String) row.get("INDEX_NAME");
                if (indexName == null) {
                    continue;
                }

                long usageCount = ((Number) row.getOrDefault("COUNT_FETCH", 0)).longValue() +
                        ((Number) row.getOrDefault("COUNT_INSERT", 0)).longValue() +
                        ((Number) row.getOrDefault("COUNT_UPDATE", 0)).longValue() +
                        ((Number) row.getOrDefault("COUNT_DELETE", 0)).longValue();

                IndexAnalysisResult.IndexUsageStats usageStat = IndexAnalysisResult.IndexUsageStats.builder()
                        .indexName(indexName)
                        .usageCount(usageCount)
                        .selectivity(0.0) // 需要额外查询计算
                        .avgRowsScanned(0L) // 需要额外查询计算
                        .build();
                stats.put(indexName, usageStat);
            }
        } catch (Exception e) {
            log.warn("[索引分析] 获取索引使用统计失败: tableName={}", tableName, e);
        }

        return stats;
    }

    /**
     * 将数据库类型映射为Java类型
     *
     * @param dbType 数据库类型
     * @return Java类型
     */
    private String mapDbTypeToJavaType(String dbType) {
        if (dbType == null) {
            return "String";
        }

        String typeLower = dbType.toLowerCase();
        if (typeLower.contains("int")) {
            return "Long";
        } else if (typeLower.contains("decimal") || typeLower.contains("numeric") || typeLower.contains("float")
                || typeLower.contains("double")) {
            return "BigDecimal";
        } else if (typeLower.contains("date") || typeLower.contains("time")) {
            return "LocalDateTime";
        } else if (typeLower.contains("bool") || typeLower.contains("bit")) {
            return "Boolean";
        } else {
            return "String";
        }
    }

    /**
     * 将列名转换为字段名（下划线转驼峰）
     *
     * @param columnName 列名
     * @return 字段名
     */
    private String convertColumnNameToFieldName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return columnName;
        }

        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
}



