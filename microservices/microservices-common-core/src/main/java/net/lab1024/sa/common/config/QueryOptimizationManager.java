package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 企业级查询优化管理器
 * <p>
 * 解决深度分页问题、慢查询检测、查询缓存等性能优化需求
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 无状态设计，无需依赖注入
 * - 在微服务中通过配置类注册为Spring Bean（可选，此类可单例使用）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 3.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除@Component注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class QueryOptimizationManager {

    /**
     * 慢查询阈值（毫秒）
     */
    private static final long SLOW_QUERY_THRESHOLD = 1000L;

    /**
     * 查询统计信息
     */
    private final Map<String, QueryStatistics> queryStats = new ConcurrentHashMap<>();

    /**
     * 慢查询记录
     */
    private final Map<String, List<SlowQueryRecord>> slowQueries = new ConcurrentHashMap<>();

    /**
     * 创建优化的分页对象
     */
    public <T> Page<T> createOptimizedPage(int pageNum, int pageSize, String queryType) {
        // 检查是否为深度分页
        if (isDeepPagination(pageNum, pageSize)) {
            log.warn("[查询优化] 检测到深度分页，建议使用游标分页: queryType={}, pageNum={}, pageSize={}",
                queryType, pageNum, pageSize);
        }

        // 限制最大页面大小，防止内存溢出
        int safePageSize = Math.min(pageSize, 1000);

        Page<T> page = new Page<>(pageNum, safePageSize);

        // 记录查询开始时间
        page.setOptimizeCountSql(true);
        page.setSearchCount(true);

        return page;
    }

    /**
     * 创建基于游标的分页对象（推荐方式）
     */
    public <T> CursorPage<T> createCursorPage(long lastId, int pageSize, String sortField) {
        return new CursorPage<>(lastId, pageSize, sortField);
    }

    /**
     * 构建游标分页查询条件
     */
    public String buildCursorPageCondition(String idField, Long lastId, String additionalCondition) {
        StringBuilder condition = new StringBuilder();

        if (lastId != null && lastId > 0) {
            condition.append(idField).append(" > ").append(lastId);
        }

        if (additionalCondition != null && !additionalCondition.trim().isEmpty()) {
            if (condition.length() > 0) {
                condition.append(" AND ");
            }
            condition.append("(").append(additionalCondition).append(")");
        }

        return condition.toString();
    }

    /**
     * 记录查询性能统计
     */
    public void recordQueryStatistics(String queryType, long startTime, int resultCount, boolean success) {
        long duration = System.currentTimeMillis() - startTime;
        String key = queryType;

        QueryStatistics stats = queryStats.computeIfAbsent(key, k -> new QueryStatistics());
        stats.recordQuery(duration, resultCount, success);

        // 检查是否为慢查询
        if (duration > SLOW_QUERY_THRESHOLD) {
            recordSlowQuery(queryType, duration, resultCount);
        }
    }

    /**
     * 检测是否为深度分页
     */
    public boolean isDeepPagination(int pageNum, int pageSize) {
        // 计算偏移量
        long offset = (long) (pageNum - 1) * pageSize;

        // 偏移量超过10000认为是深度分页
        return offset > 10000;
    }

    /**
     * 获取查询统计信息
     */
    public QueryStatistics getQueryStatistics(String queryType) {
        return queryStats.get(queryType);
    }

    /**
     * 获取所有查询统计信息
     */
    public Map<String, QueryStatistics> getAllQueryStatistics() {
        return new ConcurrentHashMap<>(queryStats);
    }

    /**
     * 获取慢查询记录
     */
    public List<SlowQueryRecord> getSlowQueries(String queryType) {
        return slowQueries.getOrDefault(queryType, List.of());
    }

    /**
     * 清理过期的慢查询记录
     */
    public void cleanExpiredSlowQueries(int hoursToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursToKeep);

        slowQueries.values().forEach(records -> {
            records.removeIf(record -> record.getTimestamp().isBefore(cutoffTime));
        });

        // 清理空记录
        slowQueries.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * 生成查询优化建议
     */
    public QueryOptimizationAdvice generateOptimizationAdvice(String queryType) {
        QueryStatistics stats = queryStats.get(queryType);
        if (stats == null) {
            return QueryOptimizationAdvice.noData(queryType);
        }

        QueryOptimizationAdvice advice = new QueryOptimizationAdvice(queryType);

        // 平均执行时间分析
        if (stats.getAverageDuration() > SLOW_QUERY_THRESHOLD) {
            advice.addSuggestion("查询平均执行时间过长，建议优化SQL或添加索引");
        }

        // 成功率分析
        if (stats.getSuccessRate() < 0.95) {
            advice.addSuggestion("查询成功率较低，建议检查查询条件和数据完整性");
        }

        // 结果集大小分析
        if (stats.getAverageResultCount() > 1000) {
            advice.addSuggestion("平均结果集较大，建议增加分页限制或优化查询条件");
        }

        // 慢查询数量分析
        List<SlowQueryRecord> slowQueryRecords = getSlowQueries(queryType);
        if (slowQueryRecords.size() > 10) {
            advice.addSuggestion("慢查询数量较多，建议重点优化该查询");
        }

        return advice;
    }

    /**
     * 记录慢查询
     */
    private void recordSlowQuery(String queryType, long duration, int resultCount) {
        SlowQueryRecord record = new SlowQueryRecord(queryType, duration, resultCount);

        slowQueries.computeIfAbsent(queryType, k -> new java.util.ArrayList<>()).add(record);

        // 限制每个查询类型的慢查询记录数量
        List<SlowQueryRecord> records = slowQueries.get(queryType);
        if (records.size() > 100) {
            records.remove(0); // 移除最旧的记录
        }

        log.warn("[查询优化] 记录慢查询: queryType={}, duration={}ms, resultCount={}",
            queryType, duration, resultCount);
    }

    /**
     * 查询统计信息类
     */
    public static class QueryStatistics {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong totalDuration = new AtomicLong(0);
        private final AtomicLong totalResultCount = new AtomicLong(0);
        private volatile long lastQueryTime = 0;
        private volatile long lastDuration = 0;

        public void recordQuery(long duration, int resultCount, boolean success) {
            totalCount.incrementAndGet();
            totalDuration.addAndGet(duration);
            totalResultCount.addAndGet(resultCount);

            if (success) {
                successCount.incrementAndGet();
            }

            lastQueryTime = System.currentTimeMillis();
            lastDuration = duration;
        }

        public long getTotalCount() { return totalCount.get(); }
        public long getSuccessCount() { return successCount.get(); }
        public double getSuccessRate() {
            long total = totalCount.get();
            return total > 0 ? (double) successCount.get() / total : 0.0;
        }
        public double getAverageDuration() {
            long total = totalCount.get();
            return total > 0 ? (double) totalDuration.get() / total : 0.0;
        }
        public double getAverageResultCount() {
            long total = totalCount.get();
            return total > 0 ? (double) totalResultCount.get() / total : 0.0;
        }
        public long getLastQueryTime() { return lastQueryTime; }
        public long getLastDuration() { return lastDuration; }
    }

    /**
     * 慢查询记录类
     */
    public static class SlowQueryRecord {
        private final String queryType;
        private final long duration;
        private final int resultCount;
        private final LocalDateTime timestamp;

        public SlowQueryRecord(String queryType, long duration, int resultCount) {
            this.queryType = queryType;
            this.duration = duration;
            this.resultCount = resultCount;
            this.timestamp = LocalDateTime.now();
        }

        public String getQueryType() { return queryType; }
        public long getDuration() { return duration; }
        public int getResultCount() { return resultCount; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * 游标分页对象
     */
    public static class CursorPage<T> {
        private final Long lastId;
        private final int pageSize;
        private final String sortField;
        private final List<T> records;
        private final Long nextLastId;

        public CursorPage(Long lastId, int pageSize, String sortField) {
            this.lastId = lastId;
            this.pageSize = pageSize;
            this.sortField = sortField;
            this.records = new java.util.ArrayList<>();
            this.nextLastId = null;
        }

        public CursorPage(Long lastId, int pageSize, String sortField, List<T> records, Long nextLastId) {
            this.lastId = lastId;
            this.pageSize = pageSize;
            this.sortField = sortField;
            this.records = records;
            this.nextLastId = nextLastId;
        }

        public Long getLastId() { return lastId; }
        public int getPageSize() { return pageSize; }
        public String getSortField() { return sortField; }
        public List<T> getRecords() { return records; }
        public Long getNextLastId() { return nextLastId; }
        public boolean hasNext() { return nextLastId != null; }
    }

    /**
     * 查询优化建议类
     */
    public static class QueryOptimizationAdvice {
        private final String queryType;
        private final List<String> suggestions;
        private final LocalDateTime timestamp;

        private QueryOptimizationAdvice(String queryType) {
            this.queryType = queryType;
            this.suggestions = new java.util.ArrayList<>();
            this.timestamp = LocalDateTime.now();
        }

        public static QueryOptimizationAdvice noData(String queryType) {
            return new QueryOptimizationAdvice(queryType);
        }

        public void addSuggestion(String suggestion) {
            suggestions.add(suggestion);
        }

        public String getQueryType() { return queryType; }
        public List<String> getSuggestions() { return new java.util.ArrayList<>(suggestions); }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean hasSuggestions() { return !suggestions.isEmpty(); }
    }
}
