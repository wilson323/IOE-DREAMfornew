package net.lab1024.sa.attendance.engine.rule.cache;

import net.lab1024.sa.attendance.engine.rule.model.RuleExecutionContext;
import net.lab1024.sa.attendance.engine.rule.model.RuleEvaluationResult;

import java.util.List;

/**
 * 规则缓存管理器接口
 * <p>
 * 负责管理规则评估结果的缓存，提升性能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RuleCacheManager {

    /**
     * 缓存规则评估结果
     *
     * @param ruleId 规则ID
     * @param context 执行上下文
     * @param result 评估结果
     */
    void cacheResult(Long ruleId, RuleExecutionContext context, RuleEvaluationResult result);

    /**
     * 获取缓存的规则评估结果
     *
     * @param ruleId 规则ID
     * @param context 执行上下文
     * @return 缓存的评估结果，null表示未命中
     */
    RuleEvaluationResult getCachedResult(Long ruleId, RuleExecutionContext context);

    /**
     * 批量缓存规则评估结果
     *
     * @param results 评估结果列表
     */
    void cacheBatchResults(List<RuleEvaluationResult> results);

    /**
     * 批量获取缓存的规则评估结果
     *
     * @param ruleIds 规则ID列表
     * @param context 执行上下文
     * @return 缓存的评估结果列表
     */
    List<RuleEvaluationResult> getCachedBatchResults(List<Long> ruleIds, RuleExecutionContext context);

    /**
     * 清除指定规则的缓存
     *
     * @param ruleId 规则ID
     */
    void evictRule(Long ruleId);

    /**
     * 清除指定用户的所有规则缓存
     *
     * @param userId 用户ID
     */
    void evictUserRules(Long userId);

    /**
     * 清除指定部门的所有规则缓存
     *
     * @param departmentId 部门ID
     */
    void evictDepartmentRules(Long departmentId);

    /**
     * 清除所有规则缓存
     */
    void clearCache();

    /**
     * 预加载规则缓存
     *
     * @param ruleId 规则ID
     */
    void preloadRule(Long ruleId);

    /**
     * 批量预加载规则缓存
     *
     * @param ruleIds 规则ID列表
     */
    void preloadRules(List<Long> ruleIds);

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStatistics getCacheStatistics();

    /**
     * 设置缓存过期时间
     *
     * @param ttlSeconds 过期时间（秒）
     */
    void setCacheTTL(long ttlSeconds);

    /**
     * 检查缓存是否命中
     *
     * @param ruleId 规则ID
     * @param context 执行上下文
     * @return 是否命中缓存
     */
    boolean isCacheHit(Long ruleId, RuleExecutionContext context);

    /**
     * 获取缓存键
     *
     * @param ruleId 规则ID
     * @param context 执行上下文
     * @return 缓存键
     */
    String getCacheKey(Long ruleId, RuleExecutionContext context);

    /**
     * 缓存统计信息内部类
     */
    class CacheStatistics {
        private long totalRequests;
        private long cacheHits;
        private long cacheMisses;
        private long evictions;
        private double hitRate;
        private long cacheSize;

        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getCacheHits() { return cacheHits; }
        public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }

        public long getCacheMisses() { return cacheMisses; }
        public void setCacheMisses(long cacheMisses) { this.cacheMisses = cacheMisses; }

        public long getEvictions() { return evictions; }
        public void setEvictions(long evictions) { this.evictions = evictions; }

        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }

        public long getCacheSize() { return cacheSize; }
        public void setCacheSize(long cacheSize) { this.cacheSize = cacheSize; }

        @Override
        public String toString() {
            return String.format(
                "CacheStatistics{total=%d, hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, size=%d}",
                totalRequests, cacheHits, cacheMisses, hitRate * 100, evictions, cacheSize
            );
        }
    }
}