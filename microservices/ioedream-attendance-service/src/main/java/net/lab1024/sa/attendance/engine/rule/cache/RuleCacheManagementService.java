package net.lab1024.sa.attendance.engine.rule.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.cache.RuleCacheManager;

import java.util.List;

/**
 * 规则缓存管理服务
 * <p>
 * 负责规则缓存的清除、预热和状态查询
 * 严格遵循CLAUDE.md全局架构规范,纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class RuleCacheManagementService {

    private final RuleCacheManager cacheManager;

    /**
     * 构造函数注入依赖
     */
    public RuleCacheManagementService(RuleCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 清除规则缓存
     */
    public void clearRuleCache() {
        log.info("[规则缓存管理服务] 开始清除规则缓存");

        try {
            // 委托给RuleCacheManager
            cacheManager.clearCache();

            log.info("[规则缓存管理服务] 规则缓存清除完成");

        } catch (Exception e) {
            log.error("[规则缓存管理服务] 清除规则缓存失败", e);
        }
    }

    /**
     * 预热规则缓存
     *
     * @param ruleIds 规则ID列表
     */
    public void warmUpRuleCache(List<Long> ruleIds) {
        log.info("[规则缓存管理服务] 开始预热规则缓存, 规则数量: {}", ruleIds.size());

        try {
            // 委托给RuleCacheManager
            cacheManager.warmUpCache(ruleIds);

            log.info("[规则缓存管理服务] 规则缓存预热完成");

        } catch (Exception e) {
            log.error("[规则缓存管理服务] 预热规则缓存失败", e);
        }
    }

    /**
     * 获取缓存状态
     *
     * @return 缓存状态
     */
    public CacheStatus getCacheStatus() {
        log.debug("[规则缓存管理服务] 查询缓存状态");

        try {
            // 查询缓存统计信息
            int cacheSize = cacheManager.getCacheSize();
            int hitCount = cacheManager.getHitCount();
            int missCount = cacheManager.getMissCount();

            CacheStatus status = new CacheStatus();
            status.setCacheSize(cacheSize);
            status.setHitCount(hitCount);
            status.setMissCount(missCount);
            status.setHitRate(calculateHitRate(hitCount, missCount));

            log.debug("[规则缓存管理服务] 缓存状态: size={}, hitRate={}%",
                    cacheSize, status.getHitRate());

            return status;

        } catch (Exception e) {
            log.error("[规则缓存管理服务] 查询缓存状态失败", e);

            CacheStatus errorStatus = new CacheStatus();
            errorStatus.setCacheSize(0);
            errorStatus.setHitCount(0);
            errorStatus.setMissCount(0);
            errorStatus.setHitRate(0.0);

            return errorStatus;
        }
    }

    /**
     * 计算缓存命中率
     *
     * @param hitCount  命中次数
     * @param missCount 未命中次数
     * @return 命中率 (百分比)
     */
    private double calculateHitRate(int hitCount, int missCount) {
        int total = hitCount + missCount;
        if (total == 0) {
            return 0.0;
        }
        return (double) hitCount / total * 100;
    }

    // ==================== 内部类 ====================

    /**
     * 缓存状态
     */
    @Data
    public static class CacheStatus {
        /**
         * 缓存大小
         */
        private int cacheSize;

        /**
         * 命中次数
         */
        private int hitCount;

        /**
         * 未命中次数
         */
        private int missCount;

        /**
         * 命中率 (百分比)
         */
        private double hitRate;
    }
}
