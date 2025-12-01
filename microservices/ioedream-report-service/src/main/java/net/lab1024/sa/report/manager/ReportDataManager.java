package net.lab1024.sa.report.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 报表数据管理器
 * <p>
 * 四层架构中的Manager层，负责复杂的数据操作和业务规则管理
 * 严格遵循repowiki编码规范：
 * - 使用jakarta包名
 * - 使用@Resource依赖注入
 * - 使用SLF4J日志框架
 * - 四层架构：Controller → Service → Manager → DAO/Repository
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ReportDataManager {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 执行报表数据查询
     *
     * @param sql    查询SQL
     * @param params 查询参数
     * @return 查询结果
     */
    public List<Map<String, Object>> executeReportQuery(String sql, Map<String, Object> params) {
        try {
            log.debug("执行报表查询: {}", sql);

            // 构建最终SQL
            String finalSql = buildParameterizedSql(sql, params);

            // 执行查询
            List<Map<String, Object>> result = jdbcTemplate.queryForList(finalSql);

            log.debug("查询完成，返回{}条记录", result.size());
            return result;

        } catch (Exception e) {
            log.error("报表查询执行失败: {}", sql, e);
            throw new RuntimeException("报表查询执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 记录报表生成日志
     *
     * @param templateId    模板ID
     * @param params        查询参数
     * @param dataCount     数据行数
     * @param executionTime 执行时间(毫秒)
     */
    public void recordReportGeneration(Long templateId, Map<String, Object> params, int dataCount, long executionTime) {
        try {
            String sql = """
                    INSERT INTO t_report_generation_log (
                        template_id, parameters, data_count, execution_time, generation_time, status
                    ) VALUES (?, ?, ?, ?, ?, ?)
                    """;

            String paramsJson = objectMapper.writeValueAsString(params);
            String status = executionTime > 5000 ? "SLOW" : "SUCCESS";

            int result = jdbcTemplate.update(sql, templateId, paramsJson, dataCount, executionTime, LocalDateTime.now(),
                    status);

            if (result > 0) {
                log.debug("报表生成日志记录成功: templateId={}, dataCount={}, executionTime={}ms",
                        templateId, dataCount, executionTime);
            } else {
                log.warn("报表生成日志记录失败: templateId={}", templateId);
            }

        } catch (Exception e) {
            log.error("记录报表生成日志失败: templateId={}", templateId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 获取报表统计信息
     *
     * @param templateId 模板ID
     * @param days       统计天数
     * @return 统计信息
     */
    public Map<String, Object> getReportStatistics(Long templateId, int days) {
        try {
            String sql = """
                    SELECT
                        COUNT(*) as total_generations,
                        AVG(data_count) as avg_data_count,
                        MAX(data_count) as max_data_count,
                        AVG(execution_time) as avg_execution_time,
                        SUM(CASE WHEN status = 'SLOW' THEN 1 ELSE 0 END) as slow_count,
                        SUM(CASE WHEN status = 'ERROR' THEN 1 ELSE 0 END) as error_count
                    FROM t_report_generation_log
                    WHERE template_id = ?
                    AND generation_time >= DATE_SUB(NOW(), INTERVAL ? DAY)
                    """;

            return jdbcTemplate.queryForMap(sql, templateId, days);

        } catch (Exception e) {
            log.error("获取报表统计信息失败: templateId={}, days={}", templateId, days, e);
            return createEmptyStatistics();
        }
    }

    /**
     * 清理过期的报表生成日志
     *
     * @param daysToKeep 保留天数
     * @return 清理的记录数
     */
    public int cleanupExpiredLogs(int daysToKeep) {
        try {
            String sql = """
                    DELETE FROM t_report_generation_log
                    WHERE generation_time < DATE_SUB(NOW(), INTERVAL ? DAY)
                    """;

            int deletedCount = jdbcTemplate.update(sql, daysToKeep);

            log.info("清理过期报表日志完成，删除{}条记录", deletedCount);
            return deletedCount;

        } catch (Exception e) {
            log.error("清理过期报表日志失败: daysToKeep={}", daysToKeep, e);
            return 0;
        }
    }

    /**
     * 验证报表SQL安全性
     *
     * @param sql SQL语句
     * @return 验证结果
     */
    public SqlValidationResult validateReportSql(String sql) {
        try {
            if (sql == null || sql.trim().isEmpty()) {
                return SqlValidationResult.invalid("SQL不能为空");
            }

            String upperSql = sql.toUpperCase().trim();

            // 检查是否为SELECT语句
            if (!upperSql.startsWith("SELECT")) {
                return SqlValidationResult.invalid("只允许SELECT查询语句");
            }

            // 检查危险操作
            String[] dangerousKeywords = {
                    "DROP", "DELETE", "UPDATE", "INSERT", "CREATE", "ALTER",
                    "TRUNCATE", "EXEC", "EXECUTE", "CALL", "LOAD_FILE"
            };

            for (String keyword : dangerousKeywords) {
                if (upperSql.contains(keyword + " ")) {
                    return SqlValidationResult.invalid("SQL包含危险操作: " + keyword);
                }
            }

            // 检查系统表访问
            String[] systemTables = {
                    "INFORMATION_SCHEMA", "MYSQL", "PERFORMANCE_SCHEMA", "SYS"
            };

            for (String table : systemTables) {
                if (upperSql.contains(table)) {
                    return SqlValidationResult.invalid("不允许访问系统表: " + table);
                }
            }

            log.debug("SQL验证通过: {}", sql.substring(0, Math.min(sql.length(), 100)));
            return SqlValidationResult.valid();

        } catch (Exception e) {
            log.error("SQL验证异常", e);
            return SqlValidationResult.invalid("SQL验证异常: " + e.getMessage());
        }
    }

    /**
     * 构建参数化SQL
     *
     * @param templateSql 模板SQL
     * @param params      参数
     * @return 最终SQL
     */
    private String buildParameterizedSql(String templateSql, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return templateSql;
        }

        String sql = templateSql;

        // 替换命名参数
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (sql.contains(placeholder)) {
                String value = String.valueOf(entry.getValue());
                // 简单的SQL注入防护
                if (value.contains("'") || value.contains("\"") || value.contains(";")) {
                    log.warn("检测到潜在的SQL注入，参数值: {}", value);
                    continue;
                }
                sql = sql.replace(placeholder, value);
            }
        }

        // 处理分页参数
        if (params.containsKey("pageNum") && params.containsKey("pageSize")) {
            int pageNum = (Integer) params.get("pageNum");
            int pageSize = (Integer) params.get("pageSize");
            int offset = (pageNum - 1) * pageSize;

            // 避免重复添加LIMIT
            if (!sql.toUpperCase().contains(" LIMIT ")) {
                sql += " LIMIT " + offset + ", " + pageSize;
            }
        }

        return sql;
    }

    /**
     * 创建空的统计信息
     *
     * @return 空统计信息
     */
    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_generations", 0L);
        stats.put("avg_data_count", 0.0);
        stats.put("max_data_count", 0L);
        stats.put("avg_execution_time", 0.0);
        stats.put("slow_count", 0L);
        stats.put("error_count", 0L);
        return stats;
    }

    /**
     * 记录报表生成日志（简化版本，兼容旧接口）
     *
     * @param templateId 模板ID
     * @param params     查询参数
     * @param dataCount  数据行数
     */
    public void recordReportGenerationLog(Long templateId, Map<String, Object> params, int dataCount) {
        recordReportGeneration(templateId, params, dataCount, 0L);
    }

    /**
     * 获取今日报表生成数量
     *
     * @return 今日报表生成数量
     */
    public long getTodayReportCount() {
        try {
            String sql = """
                    SELECT COUNT(*) as count
                    FROM t_report_generation_log
                    WHERE DATE(generation_time) = CURDATE()
                    """;
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ((Number) result.get("count")).longValue();
        } catch (Exception e) {
            log.error("获取今日报表数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取本月报表生成数量
     *
     * @return 本月报表生成数量
     */
    public long getMonthReportCount() {
        try {
            String sql = """
                    SELECT COUNT(*) as count
                    FROM t_report_generation_log
                    WHERE YEAR(generation_time) = YEAR(CURDATE())
                    AND MONTH(generation_time) = MONTH(CURDATE())
                    """;
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ((Number) result.get("count")).longValue();
        } catch (Exception e) {
            log.error("获取本月报表数量失败", e);
            return 0L;
        }
    }

    /**
     * 获取热门报表模板
     *
     * @param limit 返回数量限制
     * @return 热门模板列表
     */
    public List<Map<String, Object>> getPopularTemplates(int limit) {
        try {
            String sql = """
                    SELECT
                        template_id,
                        COUNT(*) as generation_count,
                        AVG(data_count) as avg_data_count,
                        MAX(generation_time) as last_generation_time
                    FROM t_report_generation_log
                    GROUP BY template_id
                    ORDER BY generation_count DESC
                    LIMIT ?
                    """;
            return jdbcTemplate.queryForList(sql, limit);
        } catch (Exception e) {
            log.error("获取热门模板失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取报表类型分布
     *
     * @return 报表类型分布统计（状态 -> 数量）
     */
    public Map<String, Long> getReportTypeDistribution() {
        try {
            String sql = """
                    SELECT
                        status,
                        COUNT(*) as count
                    FROM t_report_generation_log
                    GROUP BY status
                    """;
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            Map<String, Long> distribution = new HashMap<>();
            for (Map<String, Object> row : results) {
                String status = (String) row.get("status");
                Long count = ((Number) row.get("count")).longValue();
                distribution.put(status, count);
            }
            return distribution;
        } catch (Exception e) {
            log.error("获取报表类型分布失败", e);
            return new HashMap<>();
        }
    }

    /**
     * SQL验证结果
     */
    public static class SqlValidationResult {
        private final boolean valid;
        private final String message;

        private SqlValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static SqlValidationResult valid() {
            return new SqlValidationResult(true, "SQL验证通过");
        }

        public static SqlValidationResult invalid(String message) {
            return new SqlValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}