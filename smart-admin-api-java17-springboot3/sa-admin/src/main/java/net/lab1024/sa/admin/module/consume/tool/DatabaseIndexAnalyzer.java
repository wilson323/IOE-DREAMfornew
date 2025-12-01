package net.lab1024.sa.admin.module.consume.tool;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

/**
 * 数据库索引分析工具
 * 用于分析和优化消费模块的数据库索引性能
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Slf4j
@Component
public class DatabaseIndexAnalyzer {

    @Resource
    private JdbcTemplate jdbcTemplate;

    // 需要分析的表
    private static final List<String> TARGET_TABLES = Arrays.asList(
        "t_consume_account",
        "t_consume_record",
        "t_consume_device_config"
    );

    /**
     * 初始化方法 - 系统启动时执行索引分析
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("开始数据库索引分析...");
            IndexAnalysisResult result = analyzeIndexUsage();
            log.info("数据库索引分析完成: {}", result.getSummary());
        } catch (Exception e) {
            log.error("数据库索引分析初始化失败", e);
        }
    }

    /**
     * 分析索引使用情况
     *
     * @return 索引分析结果
     */
    public IndexAnalysisResult analyzeIndexUsage() {
        IndexAnalysisResult result = new IndexAnalysisResult();
        result.setAnalysisTime(new Date());

        try {
            // 1. 分析每个表的索引
            for (String tableName : TARGET_TABLES) {
                TableIndexAnalysis tableAnalysis = analyzeTableIndexes(tableName);
                result.addTableAnalysis(tableAnalysis);
            }

            // 2. 检查重复索引
            List<DuplicateIndex> duplicateIndexes = findDuplicateIndexes();
            result.setDuplicateIndexes(duplicateIndexes);

            // 3. 检查未使用的索引
            List<UnusedIndex> unusedIndexes = findUnusedIndexes();
            result.setUnusedIndexes(unusedIndexes);

            // 4. 生成优化建议
            List<IndexRecommendation> recommendations = generateRecommendations(result);
            result.setRecommendations(recommendations);

            log.info("索引分析完成，共分析 {} 个表，发现 {} 个重复索引，{} 个未使用索引，生成 {} 个优化建议",
                    TARGET_TABLES.size(), duplicateIndexes.size(), unusedIndexes.size(), recommendations.size());

        } catch (Exception e) {
            log.error("索引分析失败", e);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        }

        result.setSuccess(true);
        return result;
    }

    /**
     * 分析单个表的索引情况
     */
    private TableIndexAnalysis analyzeTableIndexes(String tableName) {
        TableIndexAnalysis analysis = new TableIndexAnalysis();
        analysis.setTableName(tableName);

        try {
            List<IndexInfo> indexes = getTableIndexes(tableName);
            analysis.setIndexes(indexes);

            // 分析查询性能
            QueryPerformanceAnalysis performance = analyzeQueryPerformance(tableName);
            analysis.setPerformanceAnalysis(performance);

            // 识别瓶颈索引
            List<BottleneckIndex> bottlenecks = identifyBottleneckIndexes(indexes);
            analysis.setBottlenecks(bottlenecks);

        } catch (Exception e) {
            log.error("分析表索引失败: {}", tableName, e);
            analysis.setSuccess(false);
        }

        return analysis;
    }

    /**
     * 获取表的索引信息
     */
    private List<IndexInfo> getTableIndexes(String tableName) {
        List<IndexInfo> indexes = new ArrayList<>();

        try {
            // 使用数据库元数据获取索引信息
            String sql = """
                SELECT
                    INDEX_NAME as indexName,
                    COLUMN_NAME as columnName,
                    NON_UNIQUE as nonUnique,
                    INDEX_TYPE as indexType,
                    CARDINALITY as cardinality,
                    INDEX_COMMENT as comment
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME = ?
                ORDER BY INDEX_NAME, SEQ_IN_INDEX
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);

            // 按索引名称分组
            Map<String, IndexInfo> indexMap = new LinkedHashMap<>();

            for (Map<String, Object> row : rows) {
                String indexName = (String) row.get("indexName");

                IndexInfo indexInfo = indexMap.computeIfAbsent(indexName, k -> {
                    IndexInfo info = new IndexInfo();
                    info.setIndexName(indexName);
                    info.setTableName(tableName);
                    info.setNonUnique((Boolean) row.get("nonUnique"));
                    info.setIndexType((String) row.get("indexType"));
                    info.setCardinality(((Number) row.get("cardinality")).longValue());
                    info.setComment((String) row.get("comment"));
                    info.setColumns(new ArrayList<>());
                    return info;
                });

                indexInfo.getColumns().add((String) row.get("columnName"));
            }

            indexes.addAll(indexMap.values());

        } catch (Exception e) {
            log.error("获取表索引信息失败: {}", tableName, e);
        }

        return indexes;
    }

    /**
     * 分析查询性能
     */
    private QueryPerformanceAnalysis analyzeQueryPerformance(String tableName) {
        QueryPerformanceAnalysis analysis = new QueryAnalysis();
        analysis.setTableName(tableName);

        try {
            // 模拟常见查询并分析执行计划
            List<String> commonQueries = getCommonQueries(tableName);
            Map<String, QueryExecutionPlan> plans = new HashMap<>();

            for (String query : commonQueries) {
                QueryExecutionPlan plan = analyzeExecutionPlan(query);
                plans.put(query, plan);
            }

            analysis.setExecutionPlans(plans);

            // 计算平均查询成本
            double avgCost = plans.values().stream()
                    .mapToDouble(QueryExecutionPlan::getCost)
                    .average();
            analysis.setAverageQueryCost(avgCost);

            // 识别慢查询
            List<String> slowQueries = plans.entrySet().stream()
                    .filter(entry -> entry.getValue().getCost() > 1000) // 成本>1000认为慢查询
                    .map(Map.Entry::getKey)
                    .toList();
            analysis.setSlowQueries(slowQueries);

        } catch (Exception e) {
            log.error("分析查询性能失败: {}", tableName, e);
            analysis.setSuccess(false);
        }

        return analysis;
    }

    /**
     * 获取常见查询模式
     */
    private List<String> getCommonQueries(String tableName) {
        List<String> queries = new ArrayList<>();

        switch (tableName) {
            case "t_consume_account":
                queries.add("SELECT * FROM t_consume_account WHERE status = 'ACTIVE' AND account_type = ?");
                queries.add("SELECT * FROM t_consume_account WHERE region_id = ? AND deleted_flag = 0");
                queries.add("SELECT * FROM t_consume_account WHERE balance > ? ORDER BY balance DESC LIMIT 100");
                queries.add("SELECT * FROM t_consume_account WHERE last_consume_time >= ? AND status = 'ACTIVE'");
                break;

            case "t_consume_record":
                queries.add("SELECT * FROM t_consume_record WHERE person_id = ? AND pay_time >= ? ORDER BY pay_time DESC");
                queries.add("SELECT * FROM t_consume_record WHERE status = 'SUCCESS' AND pay_time BETWEEN ? AND ?");
                queries.add("SELECT * FROM t_consume_record WHERE region_id = ? AND amount > ?");
                queries.add("SELECT COUNT(*) FROM t_consume_record WHERE DATE(pay_time) = ?");
                break;

            case "t_consume_device_config":
                queries.add("SELECT * FROM t_consume_device_config WHERE status = 'ONLINE' AND region_id = ?");
                queries.add("SELECT * FROM t_consume_device_config WHERE device_type = ? ORDER BY priority DESC");
                queries.add("SELECT * FROM t_consume_device_config WHERE last_heartbeat_time >= ?");
                break;
        }

        return queries;
    }

    /**
     * 分析执行计划
     */
    private QueryExecutionPlan analyzeExecutionPlan(String query) {
        QueryExecutionPlan plan = new QueryExecutionPlan();
        plan.setQuery(query);

        try {
            // 使用EXPLAIN分析查询
            String explainQuery = "EXPLAIN " + query;
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(explainQuery);

            if (!rows.isEmpty()) {
                Map<String, Object> row = rows.get(0);
                plan.setCost(calculateQueryCost(row));
                plan.setRows(((Number) row.getOrDefault("rows", 0L)).longValue());
            }

        } catch (Exception e) {
            log.warn("分析执行计划失败: {}", query, e);
            plan.setCost(1000.0); // 默认成本
        }

        return plan;
    }

    /**
     * 计算查询成本（简化版本）
     */
    private double calculateQueryCost(Map<String, Object> explainRow) {
        try {
            // 基于rows和type估算成本
            Long rows = ((Number) explainRow.getOrDefault("rows", 0L)).longValue();
            String type = (String) explainRow.get("type");

            double baseCost = rows > 0 ? Math.log(rows) : 0;

            // 根据访问类型调整成本
            if (type != null) {
                switch (type.toLowerCase()) {
                    case "all":
                        baseCost *= 10;
                        break;
                    case "range":
                        baseCost *= 3;
                        break;
                    case "ref":
                    case "const":
                        baseCost *= 1;
                        break;
                }
            }

            return baseCost;

        } catch (Exception e) {
            return 1000.0;
        }
    }

    /**
     * 识别瓶颈索引
     */
    private List<BottleneckIndex> identifyBottleneckIndexes(List<IndexInfo> indexes) {
        List<BottleneckIndex> bottlenecks = new ArrayList<>();

        for (IndexInfo index : indexes) {
            // 检查基数是否过低
            if (index.getCardinality() < 10) {
                bottlenecks.add(new BottleneckIndex(
                    index.getIndexName(),
                    "LOW_CARDINALITY",
                    String.format("索引 %s 的基数为 %d，选择性较低", index.getIndexName(), index.getCardinality())
                ));
            }

            // 检查是否为非唯一索引但基数很高
            if (index.isNonUnique() && index.getCardinality() > 10000) {
                bottlenecks.add(new BottleneckIndex(
                    index.getIndexName(),
                    "HIGH_CARDINALITY_NON_UNIQUE",
                    String.format("非唯一索引 %s 的基数为 %d，可能影响写入性能", index.getIndexName(), index.getCardinality())
                ));
            }

            // 检查复合索引的列顺序
            if (index.getColumns().size() > 3) {
                bottlenecks.add(new BottleneckIndex(
                    index.getIndexName(),
                    "TOO_MANY_COLUMNS",
                    String.format("复合索引 %s 有 %d 列，可能影响性能", index.getIndexName(), index.getColumns().size())
                ));
            }
        }

        return bottlenecks;
    }

    /**
     * 查找重复索引
     */
    private List<DuplicateIndex> findDuplicateIndexes() {
        List<DuplicateIndex> duplicates = new ArrayList<>();

        try {
            String sql = """
                SELECT
                    TABLE_NAME,
                    INDEX_NAME,
                    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_IN_INDEX) as columns
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME IN (?, ?, ?)
                    AND INDEX_NAME != 'PRIMARY'
                GROUP BY TABLE_NAME, INDEX_NAME
                HAVING COUNT(*) > 1
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,
                    TARGET_TABLES.get(0), TARGET_TABLES.get(1), TARGET_TABLES.get(2));

            for (Map<String, Object> row : rows) {
                duplicates.add(new DuplicateIndex(
                    (String) row.get("TABLE_NAME"),
                    (String) row.get("INDEX_NAME"),
                    (String) row.get("columns")
                ));
            }

        } catch (Exception e) {
            log.error("查找重复索引失败", e);
        }

        return duplicates;
    }

    /**
     * 查找未使用的索引
     */
    private List<UnusedIndex> findUnusedIndexes() {
        List<UnusedIndex> unusedIndexes = new ArrayList<>();

        try {
            // 这里简化实现，实际项目中应该通过慢查询日志分析
            String sql = """
                SELECT
                    TABLE_NAME,
                    INDEX_NAME,
                    INDEX_TYPE
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_NAME IN (?, ?, ?)
                    AND INDEX_NAME != 'PRIMARY'
                AND INDEX_NAME NOT LIKE 'idx_%'
                ORDER BY TABLE_NAME, INDEX_NAME
                """;

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,
                    TARGET_TABLES.get(0), TARGET_TABLES.get(1), TARGET_TABLES.get(2));

            for (Map<String, Object> row : rows) {
                unusedIndexes.add(new UnusedIndex(
                    (String) row.get("TABLE_NAME"),
                    (String) row.get("INDEX_NAME"),
                    (String) row.get("INDEX_TYPE")
                ));
            }

        } catch (Exception e) {
            log.error("查找未使用索引失败", e);
        }

        return unusedIndexes;
    }

    /**
     * 生成优化建议
     */
    private List<IndexRecommendation> generateRecommendations(IndexAnalysisResult result) {
        List<IndexRecommendation> recommendations = new ArrayList<>();

        // 基于瓶颈分析生成建议
        for (TableIndexAnalysis tableAnalysis : result.getTableAnalyses()) {
            for (BottleneckIndex bottleneck : tableAnalysis.getBottlenecks()) {
                IndexRecommendation recommendation = new IndexRecommendation();
                recommendation.setTableName(tableAnalysis.getTableName());
                recommendation.setIndexName(bottleneck.getIndexName());
                recommendation.setRecommendationType(bottleneck.getType());
                recommendation.setDescription(bottleneck.getDescription());
                recommendation.setSuggestion(generateIndexSuggestion(bottleneck));
                recommendation.setPriority(bottleneck.getPriority());

                recommendations.add(recommendation);
            }
        }

        // 通用优化建议
        recommendations.add(new IndexRecommendation(
                "ALL",
                "ADD_COMPOSITE_INDEXES",
                "OPTIMIZATION",
                "为常用查询组合添加复合索引，提升查询性能",
                "CREATE INDEX idx_composite ON table (column1, column2)",
                "HIGH"
        ));

        recommendations.add(new IndexRecommendation(
                "ALL",
                "ANALYZE_USAGE",
                "MONITORING",
                "定期监控索引使用情况，清理未使用的索引",
                "使用sys.schema_unused_indexes视图识别并删除未使用索引",
                "MEDIUM"
        ));

        return recommendations;
    }

    /**
     * 生成索引优化建议
     */
    private String generateIndexSuggestion(BottleneckIndex bottleneck) {
        switch (bottleneck.getType()) {
            case "LOW_CARDINALITY":
                return "考虑删除此索引或重新设计表结构";
            case "HIGH_CARDINALITY_NON_UNIQUE":
                return "考虑添加分区表或重新设计索引策略";
            case "TOO_MANY_COLUMNS":
                return "减少复合索引的列数，保留最常用的前几列";
            default:
                return "需要进一步分析索引使用情况";
        }
    }

    /**
     * 执行索引优化建议
     *
     * @param recommendations 优化建议列表
     * @return 执行结果
     */
    public IndexOptimizationResult executeOptimizations(List<IndexRecommendation> recommendations) {
        IndexOptimizationResult result = new IndexOptimizationResult();
        result.setOptimizationTime(new Date());
        result.setTotalRecommendations(recommendations.size());

        int successCount = 0;
        int failureCount = 0;

        for (IndexRecommendation recommendation : recommendations) {
            try {
                if ("ADD_COMPOSITE_INDEXES".equals(recommendation.getRecommendationType()) &&
                    recommendation.getSuggestion().startsWith("CREATE INDEX")) {

                    // 执行索引创建
                    jdbcTemplate.update(recommendation.getSuggestion());
                    successCount++;

                    log.info("执行索引优化: {} - {}",
                            recommendation.getIndexName(), recommendation.getDescription());
                }

            } catch (Exception e) {
                log.error("执行索引优化失败: {} - {}",
                        recommendation.getIndexName(), recommendation.getDescription(), e);
                failureCount++;
            }
        }

        result.setSuccessfulOptimizations(successCount);
        result.setFailedOptimizations(failureCount);
        result.setSuccess(failureCount == 0);

        return result;
    }

    /**
     * 生成索引分析报告
     *
     * @param analysisResult 分析结果
     * @return 报告文本
     */
    public String generateReport(IndexAnalysisResult analysisResult) {
        StringBuilder report = new StringBuilder();

        report.append("# 数据库索引分析报告\n\n");
        report.append("生成时间: ").append(analysisResult.getAnalysisTime()).append("\n\n");

        report.append("## 📊 总体概览\n");
        report.append("- 分析表数量: ").append(analysisResult.getTableAnalyses().size()).append("\n");
        report.append("- 发现重复索引: ").append(analysisResult.getDuplicateIndexes().size()).append("\n");
        report.append("- 未使用索引: ").append(analysisResult.getUnusedIndexes().size()).append("\n");
        report.append("- 瓶颈索引: ").append(
                analysisResult.getTableAnalyses().stream()
                        .mapToInt(analysis -> analysis.getBottlenecks().size())
                        .sum()).append("\n\n");

        // 表级分析
        report.append("## 📋 表级分析\n");
        for (TableIndexAnalysis tableAnalysis : analysisResult.getTableAnalyses()) {
            report.append("### ").append(tableAnalysis.getTableName()).append("\n");
            report.append("- 索引数量: ").append(tableAnalysis.getIndexes().size()).append("\n");
            report.append("- 瓶颈索引: ").append(tableAnalysis.getBottlenecks().size()).append("\n");
            report.append("- 平均查询成本: ").append(
                    String.format("%.2f", tableAnalysis.getPerformanceAnalysis().getAverageQueryCost())).append("\n");
            report.append("- 慢查询数量: ").append(tableAnalysis.getPerformanceAnalysis().getSlowQueries().size()).append("\n\n");
        }

        // 重复索引详情
        if (!analysisResult.getDuplicateIndexes().isEmpty()) {
            report.append("## ⚠️ 重复索引\n");
            for (DuplicateIndex duplicate : analysisResult.getDuplicateIndexes()) {
                report.append("- ").append(duplicate.toString()).append("\n");
            }
            report.append("\n");
        }

        // 未使用索引详情
        if (!analysisResult.getUnusedIndexes().isEmpty()) {
            report.append("## 🗑️ 未使用索引\n");
            for (UnusedIndex unused : analysisResult.getUnusedIndexes()) {
                report.append("- ").append(unused.toString()).append("\n");
            }
            report.append("\n");
        }

        // 优化建议
        if (!analysisResult.getRecommendations().isEmpty()) {
            report.append("## 💡 优化建议\n");
            for (IndexRecommendation recommendation : analysisResult.getRecommendations()) {
                report.append("### ").append(recommendation.getRecommendationType())
                        .append(" - ").append(recommendation.getIndexName())
                        .append(" (").append(recommendation.getPriority()).append(")\n");
                report.append("- **问题**: ").append(recommendation.getDescription()).append("\n");
                report.append("- **建议**: ").append(recommendation.getSuggestion()).append("\n\n");
            }
        }

        return report.toString();
    }

    // 内部类定义

    public static class IndexAnalysisResult {
        private Date analysisTime;
        private boolean success;
        private String errorMessage;
        private List<TableIndexAnalysis> tableAnalyses = new ArrayList<>();
        private List<DuplicateIndex> duplicateIndexes = new ArrayList<>();
        private List<UnusedIndex> unusedIndexes = new ArrayList<>();
        private List<IndexRecommendation> recommendations = new ArrayList<>();

        // Getters and Setters
        public Date getAnalysisTime() { return analysisTime; }
        public void setAnalysisTime(Date analysisTime) { this.analysisTime = analysisTime; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<TableIndexAnalysis> getTableAnalyses() { return tableAnalyses; }
        public void setTableAnalyses(List<TableIndexAnalysis> tableAnalyses) { this.tableAnalyses = tableAnalyses; }
        public List<DuplicateIndex> getDuplicateIndexes() { return duplicateIndexes; }
        public void setDuplicateIndexes(List<DuplicateIndex> duplicateIndexes) { this.duplicateIndexes = duplicateIndexes; }
        public List<UnusedIndex> getUnusedIndexes() { return unusedIndexes; }
        public void setUnusedIndexes(List<UnusedIndex> unusedIndexes) { this.unusedIndexes = unusedIndexes; }
        public List<IndexRecommendation> getRecommendations() { return recommendations; }
        public void setRecommendations(List<IndexRecommendation> recommendations) { this.recommendations = recommendations; }
        public String getSummary() {
            return String.format("分析完成于 %s，成功=%s, 表数=%d, 重复索引=%d, 未使用索引=%d, 建议数=%d",
                    analysisTime, success, tableAnalyses.size(),
                    duplicateIndexes.size(), unusedIndexes.size(), recommendations.size());
        }
    }

    public static class TableIndexAnalysis {
        private String tableName;
        private boolean success;
        private List<IndexInfo> indexes = new ArrayList<>();
        private QueryPerformanceAnalysis performanceAnalysis;
        private List<BottleneckIndex> bottlenecks = new ArrayList<>();

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<IndexInfo> getIndexes() { return indexes; }
        public void setIndexes(List<IndexInfo> indexes) { this.indexes = indexes; }
        public QueryPerformanceAnalysis getPerformanceAnalysis() { return performanceAnalysis; }
        public void setPerformanceAnalysis(QueryPerformanceAnalysis performanceAnalysis) { this.performanceAnalysis = performanceAnalysis; }
        public List<BottleneckIndex> getBottlenecks() { return bottlenecks; }
        public void setBottlenecks(List<BottleneckIndex> bottlenecks) { this.bottlenecks = bottlenecks; }
    }

    public static class IndexInfo {
        private String indexName;
        private String tableName;
        private List<String> columns = new ArrayList<>();
        private boolean nonUnique;
        private String indexType;
        private long cardinality;
        private String comment;

        // Getters and Setters
        public String getIndexName() { return indexName; }
        public void setIndexName(String indexName) { this.indexName = indexName; }
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public List<String> getColumns() { return columns; }
        public void setColumns(List<String> columns) { this.columns = columns; }
        public boolean isNonUnique() { return nonUnique; }
        public void setNonUnique(boolean nonUnique) { this.nonUnique = nonUnique; }
        public String getIndexType() { return indexType; }
        public void setIndexType(String indexType) { this.indexType = indexType; }
        public long getCardinality() { return cardinality; }
        public void setCardinality(long cardinality) { this.cardinality = cardinality; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        @Override
        public String toString() {
            return String.format("Index[indexName=%s, table=%s, columns=%s, cardinality=%d]",
                    indexName, tableName, columns, cardinality);
        }
    }

    public static class QueryPerformanceAnalysis {
        private String tableName;
        private boolean success;
        private Map<String, QueryExecutionPlan> executionPlans = new HashMap<>();
        private double averageQueryCost;
        private List<String> slowQueries = new ArrayList<>();

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Map<String, QueryExecutionPlan> getExecutionPlans() { return executionPlans; }
        public void setExecutionPlans(Map<String, QueryExecutionPlan> executionPlans) { this.executionPlans = executionPlans; }
        public double getAverageQueryCost() { return averageQueryCost; }
        public void setAverageQueryCost(double averageQueryCost) { this.averageQueryCost = averageQueryCost; }
        public List<String> getSlowQueries() { return slowQueries; }
        public void setSlowQueries(List<String> slowQueries) { this.slowQueries = slowQueries; }
    }

    public static class QueryExecutionPlan {
        private String query;
        private double cost;
        private long rows;

        // Getters and Setters
        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public double getCost() { return cost; }
        public void setCost(double cost) { this.cost = cost; }
        public long getRows() { return rows; }
        public void setRows(long rows) { this.rows = rows; }
    }

    public static class BottleneckIndex {
        private String indexName;
        private String type;
        private String description;
        private int priority;

        // Getters and Setters
        public String getIndexName() { return indexName; }
        public void setIndexName(String indexName) { this.indexName = indexName; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }

        @Override
        public String toString() {
            return String.format("Bottleneck[index=%s, type=%s, priority=%d] - %s",
                    indexName, type, priority, description);
        }
    }

    public static class DuplicateIndex {
        private String tableName;
        private String indexName;
        private String columns;

        public DuplicateIndex(String tableName, String indexName, String columns) {
            this.tableName = tableName;
            this.indexName = indexName;
            this.columns = columns;
        }

        // Getters
        public String getTableName() { return tableName; }
        public String getIndexName() { return indexName; }
        public String getColumns() { return columns; }

        @Override
        public String toString() {
            return String.format("Duplicate[%s.%s] columns: %s", tableName, indexName, columns);
        }
    }

    public static class UnusedIndex {
        private String tableName;
        private String indexName;
        private String indexType;

        public UnusedIndex(String tableName, String indexName, String indexType) {
            this.tableName = tableName;
            this.indexName = indexName;
            this.indexType = indexType;
        }

        // Getters
        public String getTableName() { return tableName; }
        public String getIndexName() { return indexName; }
        public String getIndexType() { return indexType; }

        @Override
        public String toString() {
            return String.format("Unused[%s.%s] type: %s", tableName, indexName, indexType);
        }
    }

    public static class IndexRecommendation {
        private String tableName;
        private String indexName;
        private String recommendationType;
        private String description;
        private String suggestion;
        private String priority;

        // Getters and Setters
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        public String getIndexName() { return indexName; }
        public void setIndexName(String indexName) { this.indexName = indexName; }
        public String getRecommendationType() { return recommendationType; }
        public void setRecommendationType(String recommendationType) { this.recommendationType = recommendationType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s (优先级: %s)",
                    recommendationType, indexName, description, priority);
        }
    }

    public static class IndexOptimizationResult {
        private Date optimizationTime;
        private int totalRecommendations;
        private int successfulOptimizations;
        private int failedOptimizations;
        private boolean success;

        // Getters and Setters
        public Date getOptimizationTime() { return optimizationTime; }
        public void setOptimizationTime(Date optimizationTime) { this.optimizationTime = optimizationTime; }
        public int getTotalRecommendations() { return totalRecommendations; }
        public void setTotalRecommendations(int totalRecommendations) { this.totalRecommendations = totalRecommendations; }
        public int getSuccessfulOptimizations() { return successfulOptimizations; }
        public void setSuccessfulOptimizations(int successfulOptimizations) { this.successfulOptimizations = successfulOptimizations; }
        public int getFailedOptimizations() { return failedOptimizations; }
        public void setFailedOptimizations(int failedOptimizations) { this.failedOptimizations = failedOptimizations; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
}