package net.lab1024.sa.common.permission.domain.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权限验证统计信息
 * <p>
 * 用于监控和分析权限验证的性能和使用情况
 * 支持实时统计和历史统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionValidationStats {

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总验证次数
     */
    private Long totalValidations;

    /**
     * 成功验证次数
     */
    private Long successValidations;

    /**
     * 失败验证次数
     */
    private Long failureValidations;

    /**
     * 权限不足次数
     */
    private Long forbiddenCount;

    /**
     * 未认证次数
     */
    private Long unauthenticatedCount;

    /**
     * 权限不存在次数
     */
    private Long permissionNotFoundCount;

    /**
     * 系统错误次数
     */
    private Long systemErrorCount;

    /**
     * 缓存命中次数
     */
    private Long cacheHitCount;

    /**
     * 缓存未命中次数
     */
    private Long cacheMissCount;

    /**
     * 平均验证耗时（毫秒）
     */
    private Double averageDuration;

    /**
     * 最大验证耗时（毫秒）
     */
    private Long maxDuration;

    /**
     * 最小验证耗时（毫秒）
     */
    private Long minDuration;

    /**
     * 各验证类型统计
     */
    private Map<String, Long> validationTypeStats;

    /**
     * 各权限统计
     */
    private Map<String, Long> permissionStats;

    /**
     * 各角色统计
     */
    private Map<String, Long> roleStats;

    /**
     * 各资源统计
     */
    private Map<String, Long> resourceStats;

    /**
     * 错误统计
     */
    private Map<String, Long> errorStats;

    /**
     * 性能统计
     */
    private Map<String, Object> performanceStats;

    /**
     * 创建空的统计信息
     */
    public static PermissionValidationStats createEmpty() {
        return PermissionValidationStats.builder()
                .startTime(LocalDateTime.now())
                .totalValidations(0L)
                .successValidations(0L)
                .failureValidations(0L)
                .forbiddenCount(0L)
                .unauthenticatedCount(0L)
                .permissionNotFoundCount(0L)
                .systemErrorCount(0L)
                .cacheHitCount(0L)
                .cacheMissCount(0L)
                .averageDuration(0.0)
                .maxDuration(0L)
                .minDuration(Long.MAX_VALUE)
                .validationTypeStats(new ConcurrentHashMap<>())
                .permissionStats(new ConcurrentHashMap<>())
                .roleStats(new ConcurrentHashMap<>())
                .resourceStats(new ConcurrentHashMap<>())
                .errorStats(new ConcurrentHashMap<>())
                .performanceStats(new ConcurrentHashMap<>())
                .build();
    }

    /**
     * 记录成功验证
     */
    public synchronized void recordSuccess(String validationType, Long duration, Boolean cacheHit) {
        totalValidations++;
        successValidations++;

        recordValidationType(validationType);
        recordDuration(duration);
        recordCacheHit(cacheHit);
    }

    /**
     * 记录失败验证
     */
    public synchronized void recordFailure(String validationType, String errorType, Long duration, Boolean cacheHit) {
        totalValidations++;
        failureValidations++;

        recordValidationType(validationType);
        recordError(errorType);
        recordDuration(duration);
        recordCacheHit(cacheHit);
    }

    /**
     * 记录权限使用
     */
    public synchronized void recordPermission(String permission) {
        permissionStats.put(permission, permissionStats.getOrDefault(permission, 0L) + 1);
    }

    /**
     * 记录角色使用
     */
    public synchronized void recordRole(String role) {
        roleStats.put(role, roleStats.getOrDefault(role, 0L) + 1);
    }

    /**
     * 记录资源使用
     */
    public synchronized void recordResource(String resource) {
        resourceStats.put(resource, resourceStats.getOrDefault(resource, 0L) + 1);
    }

    /**
     * 记录验证类型
     */
    private void recordValidationType(String validationType) {
        validationTypeStats.put(validationType, validationTypeStats.getOrDefault(validationType, 0L) + 1);
    }

    /**
     * 记录错误类型
     */
    private void recordError(String errorType) {
        errorStats.put(errorType, errorStats.getOrDefault(errorType, 0L) + 1);

        // 更新具体错误计数
        switch (errorType) {
            case "FORBIDDEN" -> forbiddenCount++;
            case "UNAUTHENTICATED" -> unauthenticatedCount++;
            case "PERMISSION_NOT_FOUND" -> permissionNotFoundCount++;
            case "SYSTEM_ERROR" -> systemErrorCount++;
        }
    }

    /**
     * 记录耗时
     */
    private void recordDuration(Long duration) {
        // 更新最大最小耗时
        if (duration > maxDuration) {
            maxDuration = duration;
        }
        if (duration < minDuration) {
            minDuration = duration;
        }

        // 计算平均耗时
        if (totalValidations > 0) {
            averageDuration = (averageDuration * (totalValidations - 1) + duration) / totalValidations;
        }
    }

    /**
     * 记录缓存命中
     */
    private void recordCacheHit(Boolean cacheHit) {
        if (Boolean.TRUE.equals(cacheHit)) {
            cacheHitCount++;
        } else {
            cacheMissCount++;
        }
    }

    /**
     * 计算成功率
     */
    public Double getSuccessRate() {
        if (totalValidations == 0) {
            return 0.0;
        }
        return (double) successValidations / totalValidations * 100;
    }

    /**
     * 计算失败率
     */
    public Double getFailureRate() {
        if (totalValidations == 0) {
            return 0.0;
        }
        return (double) failureValidations / totalValidations * 100;
    }

    /**
     * 计算缓存命中率
     */
    public Double getCacheHitRate() {
        long totalCacheAttempts = cacheHitCount + cacheMissCount;
        if (totalCacheAttempts == 0) {
            return 0.0;
        }
        return (double) cacheHitCount / totalCacheAttempts * 100;
    }

    /**
     * 更新结束时间
     */
    public void updateEndTime() {
        this.endTime = LocalDateTime.now();
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        PermissionValidationStats empty = createEmpty();
        this.startTime = empty.getStartTime();
        this.totalValidations = empty.getTotalValidations();
        this.successValidations = empty.getSuccessValidations();
        this.failureValidations = empty.getFailureValidations();
        this.forbiddenCount = empty.getForbiddenCount();
        this.unauthenticatedCount = empty.getUnauthenticatedCount();
        this.permissionNotFoundCount = empty.getPermissionNotFoundCount();
        this.systemErrorCount = empty.getSystemErrorCount();
        this.cacheHitCount = empty.getCacheHitCount();
        this.cacheMissCount = empty.getCacheMissCount();
        this.averageDuration = empty.getAverageDuration();
        this.maxDuration = empty.getMaxDuration();
        this.minDuration = empty.getMinDuration();
        this.validationTypeStats.clear();
        this.permissionStats.clear();
        this.roleStats.clear();
        this.resourceStats.clear();
        this.errorStats.clear();
        this.performanceStats.clear();
    }

    /**
     * 合并其他统计信息
     */
    public synchronized void merge(PermissionValidationStats other) {
        if (other == null) {
            return;
        }

        this.totalValidations += other.getTotalValidations();
        this.successValidations += other.getSuccessValidations();
        this.failureValidations += other.getFailureValidations();
        this.forbiddenCount += other.getForbiddenCount();
        this.unauthenticatedCount += other.getUnauthenticatedCount();
        this.permissionNotFoundCount += other.getPermissionNotFoundCount();
        this.systemErrorCount += other.getSystemErrorCount();
        this.cacheHitCount += other.getCacheHitCount();
        this.cacheMissCount += other.getCacheMissCount();

        // 合并平均耗时
        if (other.getAverageDuration() != null && this.totalValidations > 0) {
            this.averageDuration = (this.averageDuration * (this.totalValidations - other.getTotalValidations())
                + other.getAverageDuration() * other.getTotalValidations()) / this.totalValidations;
        }

        // 合并最大最小耗时
        if (other.getMaxDuration() != null && other.getMaxDuration() > this.maxDuration) {
            this.maxDuration = other.getMaxDuration();
        }
        if (other.getMinDuration() != null && other.getMinDuration() < this.minDuration) {
            this.minDuration = other.getMinDuration();
        }

        // 合并各类统计
        mergeStats(this.validationTypeStats, other.getValidationTypeStats());
        mergeStats(this.permissionStats, other.getPermissionStats());
        mergeStats(this.roleStats, other.getRoleStats());
        mergeStats(this.resourceStats, other.getResourceStats());
        mergeStats(this.errorStats, other.getErrorStats());
        this.performanceStats.putAll(other.getPerformanceStats());
    }

    /**
     * 合并统计映射
     */
    private void mergeStats(Map<String, Long> target, Map<String, Long> source) {
        if (source != null) {
            source.forEach((key, value) ->
                target.put(key, target.getOrDefault(key, 0L) + value));
        }
    }
}