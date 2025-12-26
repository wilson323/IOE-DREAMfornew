package net.lab1024.sa.attendance.monitor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * MyBatis慢查询监控组件
 * <p>
 * 监控数据库慢查询并记录日志
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Component
@Slf4j
public class SlowQueryMonitor {


    // 慢查询阈值（毫秒）
    private static final long SLOW_QUERY_THRESHOLD = 1000;

    // SQL统计
    private final ConcurrentHashMap<String, SqlStatistics> sqlStatisticsMap = new ConcurrentHashMap<>();

    /**
     * 记录SQL执行
     *
     * @param sqlId SQL ID
     * @param sql SQL语句
     * @param duration 执行时长（毫秒）
     */
    public void recordQuery(String sqlId, String sql, long duration) {
        try {
            // 记录慢查询
            if (duration > SLOW_QUERY_THRESHOLD) {
                log.warn("[慢查询监控] 检测到慢查询: sqlId={}, duration={}ms, threshold={}ms",
                        sqlId, duration, SLOW_QUERY_THRESHOLD);
                log.warn("[慢查询监控] SQL语句: {}", sql);
            }

            // 更新SQL统计
            updateSqlStatistics(sqlId, sql, duration);

        } catch (Exception e) {
            log.error("[慢查询监控] 记录失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 更新SQL统计
     */
    private void updateSqlStatistics(String sqlId, String sql, long duration) {
        SqlStatistics statistics = sqlStatisticsMap.computeIfAbsent(sqlId, k -> new SqlStatistics(sql));
        statistics.recordExecution(duration);
    }

    /**
     * 获取SQL统计
     *
     * @param sqlId SQL ID
     * @return SQL统计数据
     */
    public SqlStatistics getSqlStatistics(String sqlId) {
        return sqlStatisticsMap.get(sqlId);
    }

    /**
     * 获取所有SQL统计
     *
     * @return 所有SQL统计数据
     */
    public ConcurrentHashMap<String, SqlStatistics> getAllSqlStatistics() {
        return sqlStatisticsMap;
    }

    /**
     * SQL统计数据
     */
    public static class SqlStatistics {
        private final String sql;
        private long executionCount = 0;
        private long totalDuration = 0;
        private long maxDuration = 0;
        private long slowQueryCount = 0;
        private LocalDateTime lastExecutionTime;

        public SqlStatistics(String sql) {
            this.sql = sql;
        }

        public synchronized void recordExecution(long duration) {
            executionCount++;
            totalDuration += duration;

            if (duration > maxDuration) {
                maxDuration = duration;
            }

            if (duration > SLOW_QUERY_THRESHOLD) {
                slowQueryCount++;
            }

            lastExecutionTime = LocalDateTime.now();
        }

        public double getAverageDuration() {
            return executionCount > 0 ? (double) totalDuration / executionCount : 0;
        }

        public double getSlowQueryRate() {
            return executionCount > 0 ? (double) slowQueryCount / executionCount * 100 : 0;
        }

        // Getters
        public String getSql() { return sql; }
        public long getExecutionCount() { return executionCount; }
        public long getTotalDuration() { return totalDuration; }
        public long getMaxDuration() { return maxDuration; }
        public long getSlowQueryCount() { return slowQueryCount; }
        public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
    }
}
