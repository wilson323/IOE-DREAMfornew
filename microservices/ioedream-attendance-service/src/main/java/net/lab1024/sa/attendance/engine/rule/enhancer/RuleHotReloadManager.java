package net.lab1024.sa.attendance.engine.rule.enhancer;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.rule.cache.RuleCacheManager;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 考勤规则热重载管理器
 * <p>
 * 严格遵循四层架构规范（Controller→Service→Manager→DAO）
 * Manager层负责业务编排和复杂业务逻辑处理
 * </p>
 * <p>
 * 核心职责：
 * - 监控规则配置变更
 * - 自动重新加载规则
 * - 通知相关组件更新
 * - 记录重载历史
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Component
@Slf4j
public class RuleHotReloadManager {

    private final RuleLoader ruleLoader;
    private final RuleCacheManager cacheManager;

    // 规则版本管理
    private final Map<Long, RuleVersionInfo> ruleVersions = new ConcurrentHashMap<>();

    // 重载历史记录
    private final List<ReloadHistoryRecord> reloadHistory = new java.util.ArrayList<>();

    // 定时检查线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "rule-hot-reload-scheduler");
        thread.setDaemon(true);
        return thread;
    });

    // 配置参数
    private static final long CHECK_INTERVAL_MINUTES = 5; // 检查间隔（分钟）
    private static final int MAX_HISTORY_SIZE = 100; // 最大历史记录数

    /**
     * 构造函数注入依赖
     */
    public RuleHotReloadManager(RuleLoader ruleLoader, RuleCacheManager cacheManager) {
        this.ruleLoader = ruleLoader;
        this.cacheManager = cacheManager;

        log.info("[规则热重载] 规则热重载管理器初始化完成");

        // 启动定时检查任务
        startPeriodicCheck();
    }

    /**
     * 启动定时检查任务
     */
    public void startPeriodicCheck() {
        log.info("[规则热重载] 启动定时检查任务，间隔: {}分钟", CHECK_INTERVAL_MINUTES);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndReloadRules();
            } catch (Exception e) {
                log.error("[规则热重载] 定时检查任务执行失败", e);
            }
        }, CHECK_INTERVAL_MINUTES, CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 手动触发重载检查
     */
    public void triggerReloadCheck() {
        log.info("[规则热重录] 手动触发重载检查");
        checkAndReloadRules();
    }

    /**
     * 检查并重新加载规则
     */
    private void checkAndReloadRules() {
        log.debug("[规则热重载] 开始检查规则变更");

        try {
            // 1. 获取所有启用的规则
            List<Long> allRuleIds = ruleLoader.loadAllActiveRules();

            int reloadCount = 0;
            for (Long ruleId : allRuleIds) {
                if (shouldReloadRule(ruleId)) {
                    reloadRule(ruleId);
                    reloadCount++;
                }
            }

            if (reloadCount > 0) {
                log.info("[规则热重载] 规则重载完成，重载数量: {}", reloadCount);
            } else {
                log.debug("[规则热重载] 无规则需要重载");
            }

        } catch (Exception e) {
            log.error("[规则热重载] 检查规则变更失败", e);
        }
    }

    /**
     * 判断规则是否需要重载
     */
    private boolean shouldReloadRule(Long ruleId) {
        try {
            // 1. 获取当前规则配置
            Map<String, Object> currentConfig = ruleLoader.loadRuleConfig(ruleId);
            if (currentConfig == null) {
                return false;
            }

            // 2. 获取版本信息
            Object version = currentConfig.get("version");
            Object updateTime = currentConfig.get("updateTime");

            // 3. 检查是否记录过版本
            RuleVersionInfo versionInfo = ruleVersions.get(ruleId);
            if (versionInfo == null) {
                // 首次加载，记录版本
                recordRuleVersion(ruleId, currentConfig);
                return false;
            }

            // 4. 比较版本或更新时间
            boolean needReload = false;
            if (version != null && !version.equals(versionInfo.getVersion())) {
                log.info("[规则热重载] 检测到规则版本变更: ruleId={}, oldVersion={}, newVersion={}",
                        ruleId, versionInfo.getVersion(), version);
                needReload = true;
            } else if (updateTime != null && !updateTime.equals(versionInfo.getUpdateTime())) {
                log.info("[规则热重载] 检测到规则更新时间变更: ruleId={}, oldTime={}, newTime={}",
                        ruleId, versionInfo.getUpdateTime(), updateTime);
                needReload = true;
            }

            return needReload;

        } catch (Exception e) {
            log.error("[规则热重载] 检查规则是否需要重载失败: ruleId={}", ruleId, e);
            return false;
        }
    }

    /**
     * 重新加载单个规则
     */
    private void reloadRule(Long ruleId) {
        log.info("[规则热重载] 开始重载规则: ruleId={}", ruleId);

        long startTime = System.currentTimeMillis();

        try {
            // 1. 清除缓存
            cacheManager.evictRule(ruleId);
            log.debug("[规则热重载] 规则缓存已清除: ruleId={}", ruleId);

            // 2. 重新加载规则配置
            Map<String, Object> newConfig = ruleLoader.loadRuleConfig(ruleId);
            if (newConfig == null) {
                log.warn("[规则热重载] 规则配置不存在，跳过重载: ruleId={}", ruleId);
                return;
            }

            // 3. 记录新版本
            recordRuleVersion(ruleId, newConfig);

            // 4. 记录重载历史
            long duration = System.currentTimeMillis() - startTime;
            recordReloadHistory(ruleId, true, duration, null);

            log.info("[规则热重载] 规则重载成功: ruleId={}, 耗时: {}ms", ruleId, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[规则热重载] 规则重载失败: ruleId={}, 耗时: {}ms", ruleId, duration, e);

            // 记录失败历史
            recordReloadHistory(ruleId, false, duration, e.getMessage());
        }
    }

    /**
     * 记录规则版本
     */
    private void recordRuleVersion(Long ruleId, Map<String, Object> config) {
        RuleVersionInfo versionInfo = new RuleVersionInfo();
        versionInfo.setRuleId(ruleId);
        versionInfo.setVersion(config.get("version"));
        versionInfo.setUpdateTime(config.get("updateTime"));
        versionInfo.setRecordTime(LocalDateTime.now());

        ruleVersions.put(ruleId, versionInfo);
    }

    /**
     * 记录重载历史
     */
    private synchronized void recordReloadHistory(Long ruleId, boolean success, long duration, String errorMessage) {
        ReloadHistoryRecord record = new ReloadHistoryRecord();
        record.setRuleId(ruleId);
        record.setSuccess(success);
        record.setDuration(duration);
        record.setErrorMessage(errorMessage);
        record.setReloadTime(LocalDateTime.now());

        reloadHistory.add(record);

        // 限制历史记录数量
        if (reloadHistory.size() > MAX_HISTORY_SIZE) {
            reloadHistory.remove(0);
        }
    }

    /**
     * 获取重载历史
     */
    public List<ReloadHistoryRecord> getReloadHistory(int limit) {
        synchronized (reloadHistory) {
            int fromIndex = Math.max(0, reloadHistory.size() - limit);
            return new java.util.ArrayList<>(reloadHistory.subList(fromIndex, reloadHistory.size()));
        }
    }

    /**
     * 获取规则版本信息
     */
    public RuleVersionInfo getRuleVersionInfo(Long ruleId) {
        return ruleVersions.get(ruleId);
    }

    /**
     * 清除所有规则版本信息
     */
    public void clearAllVersions() {
        log.info("[规则热重载] 清除所有规则版本信息");
        ruleVersions.clear();
    }

    /**
     * 销毁管理器
     */
    public void destroy() {
        log.info("[规则热重载] 销毁规则热重载管理器");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== 内部类 ====================

    /**
     * 规则版本信息
     */
    public static class RuleVersionInfo {
        private Long ruleId;
        private Object version;
        private Object updateTime;
        private LocalDateTime recordTime;

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public Object getVersion() {
            return version;
        }

        public void setVersion(Object version) {
            this.version = version;
        }

        public Object getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Object updateTime) {
            this.updateTime = updateTime;
        }

        public LocalDateTime getRecordTime() {
            return recordTime;
        }

        public void setRecordTime(LocalDateTime recordTime) {
            this.recordTime = recordTime;
        }
    }

    /**
     * 重载历史记录
     */
    public static class ReloadHistoryRecord {
        private Long ruleId;
        private Boolean success;
        private Long duration;
        private String errorMessage;
        private LocalDateTime reloadTime;

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public LocalDateTime getReloadTime() {
            return reloadTime;
        }

        public void setReloadTime(LocalDateTime reloadTime) {
            this.reloadTime = reloadTime;
        }
    }
}
