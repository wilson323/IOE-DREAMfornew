package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 权限统计数据对象
 * <p>
 * 权限验证系统统计和分析的核心数据结构，提供：
 * - 权限验证性能统计（响应时间、吞吐量、成功率）
 * - 权限缓存统计（命中率、大小、效率）
 * - 权限使用统计（热点权限、使用频率、趋势分析）
 * - 权限异常统计（失败次数、错误类型、安全事件）
 * - 用户权限分布统计（角色分布、权限密度、活跃度）
 * - 权限变更统计（变更频率、变更类型、影响范围）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限统计数据对象")
public class PermissionStatsVO {

    /**
     * 统计时间范围开始
     */
    @Schema(description = "统计时间范围开始", example = "2025-12-16T00:00:00")
    private LocalDateTime statisticsStartTime;

    /**
     * 统计时间范围结束
     */
    @Schema(description = "统计时间范围结束", example = "2025-12-16T23:59:59")
    private LocalDateTime statisticsEndTime;

    /**
     * 统计生成时间
     */
    @Schema(description = "统计生成时间", example = "2025-12-16T23:59:59")
    private LocalDateTime generatedTime;

    /**
     * 权限验证性能统计
     */
    @Schema(description = "权限验证性能统计")
    private ValidationPerformanceStats validationStats;

    /**
     * 权限缓存统计
     */
    @Schema(description = "权限缓存统计")
    private CachePerformanceStats cacheStats;

    /**
     * 权限使用统计
     */
    @Schema(description = "权限使用统计")
    private PermissionUsageStats usageStats;

    /**
     * 权限异常统计
     */
    @Schema(description = "权限异常统计")
    private PermissionExceptionStats exceptionStats;

    /**
     * 用户权限分布统计
     */
    @Schema(description = "用户权限分布统计")
    private UserPermissionDistributionStats userStats;

    /**
     * 权限变更统计
     */
    @Schema(description = "权限变更统计")
    private PermissionChangeStats changeStats;

    /**
     * 系统健康度评分
     */
    @Schema(description = "系统健康度评分", example = "95.5")
    private BigDecimal healthScore;

    /**
     * 性能评级
     */
    @Schema(description = "性能评级", example = "EXCELLENT")
    private String performanceGrade; // POOR, FAIR, GOOD, EXCELLENT

    /**
     * 统计说明和建议
     */
    @Schema(description = "统计说明和建议", example = "系统运行良好，建议继续监控缓存命中率")
    private String summary;

    /**
     * 趋势分析数据
     */
    @Schema(description = "趋势分析数据")
    private List<TrendData> trendData;

    /**
     * 权限验证性能统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限验证性能统计")
    public static class ValidationPerformanceStats {

        /**
         * 总验证次数
         */
        @Schema(description = "总验证次数", example = "1250000")
        private Long totalValidations;

        /**
         * 成功验证次数
         */
        @Schema(description = "成功验证次数", example = "1237500")
        private Long successfulValidations;

        /**
         * 失败验证次数
         */
        @Schema(description = "失败验证次数", example = "12500")
        private Long failedValidations;

        /**
         * 成功率
         */
        @Schema(description = "成功率", example = "99.00")
        private BigDecimal successRate;

        /**
         * 平均响应时间（毫秒）
         */
        @Schema(description = "平均响应时间（毫秒）", example = "45.5")
        private BigDecimal averageResponseTime;

        /**
         * 最小响应时间（毫秒）
         */
        @Schema(description = "最小响应时间（毫秒）", example = "12.3")
        private BigDecimal minResponseTime;

        /**
         * 最大响应时间（毫秒）
         */
        @Schema(description = "最大响应时间（毫秒）", example = "1250.7")
        private BigDecimal maxResponseTime;

        /**
         * P50响应时间（毫秒）
         */
        @Schema(description = "P50响应时间（毫秒）", example = "35.2")
        private BigDecimal p50ResponseTime;

        /**
         * P95响应时间（毫秒）
         */
        @Schema(description = "P95响应时间（毫秒）", example = "89.6")
        private BigDecimal p95ResponseTime;

        /**
         * P99响应时间（毫秒）
         */
        @Schema(description = "P99响应时间（毫秒）", example = "156.8")
        private BigDecimal p99ResponseTime;

        /**
         * QPS（每秒查询数）
         */
        @Schema(description = "QPS（每秒查询数）", example = "1446.76")
        private BigDecimal qps;

        /**
         * TPS（每秒事务数）
         */
        @Schema(description = "TPS（每秒事务数）", example = "1389.45")
        private BigDecimal tps;

        /**
         * 并发峰值
         */
        @Schema(description = "并发峰值", example = "2500")
        private Integer peakConcurrency;
    }

    /**
     * 权限缓存统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限缓存统计")
    public static class CachePerformanceStats {

        /**
         * L1本地缓存命中率
         */
        @Schema(description = "L1本地缓存命中率", example = "0.85")
        private BigDecimal l1HitRate;

        /**
         * L2 Redis缓存命中率
         */
        @Schema(description = "L2 Redis缓存命中率", example = "0.92")
        private BigDecimal l2HitRate;

        /**
         * 整体缓存命中率
         */
        @Schema(description = "整体缓存命中率", example = "0.95")
        private BigDecimal overallHitRate;

        /**
         * L1缓存大小（条目数）
         */
        @Schema(description = "L1缓存大小（条目数）", example = "50000")
        private Integer l1CacheSize;

        /**
         * L2缓存大小（条目数）
         */
        @Schema(description = "L2缓存大小（条目数）", example = "100000")
        private Integer l2CacheSize;

        /**
         * L1缓存使用率
         */
        @Schema(description = "L1缓存使用率", example = "0.75")
        private BigDecimal l1CacheUsageRate;

        /**
         * L2缓存使用率
         */
        @Schema(description = "L2缓存使用率", example = "0.65")
        private BigDecimal l2CacheUsageRate;

        /**
         * 缓存命中平均耗时（微秒）
         */
        @Schema(description = "缓存命中平均耗时（微秒）", example = "125.5")
        private BigDecimal cacheHitAvgTime;

        /**
         * 缓存未命中平均耗时（毫秒）
         */
        @Schema(description = "缓存未命中平均耗时（毫秒）", example = "45.8")
        private BigDecimal cacheMissAvgTime;

        /**
         * 缓存淘汰次数
         */
        @Schema(description = "缓存淘汰次数", example = "1250")
        private Long evictionCount;

        /**
         * 缓存预热次数
         */
        @Schema(description = "缓存预热次数", example = "150")
        private Long warmupCount;
    }

    /**
     * 权限使用统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限使用统计")
    public static class PermissionUsageStats {

        /**
         * 活跃权限数
         */
        @Schema(description = "活跃权限数", example = "89")
        private Integer activePermissions;

        /**
         * 总权限数
         */
        @Schema(description = "总权限数", example = "156")
        private Integer totalPermissions;

        /**
         * 权限使用率
         */
        @Schema(description = "权限使用率", example = "0.57")
        private BigDecimal permissionUsageRate;

        /**
         * 热点权限列表（按使用频率排序）
         */
        @Schema(description = "热点权限列表")
        private List<HotPermission> hotPermissions;

        /**
         * 冷门权限列表（按使用频率排序）
         */
        @Schema(description = "冷门权限列表")
        private List<ColdPermission> coldPermissions;

        /**
         * 权限使用分布
         */
        @Schema(description = "权限使用分布")
        private Map<String, Long> permissionDistribution;

        /**
         * 功能模块权限统计
         */
        @Schema(description = "功能模块权限统计")
        private Map<String, ModulePermissionStats> moduleStats;
    }

    /**
     * 权限异常统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限异常统计")
    public static class PermissionExceptionStats {

        /**
         * 总异常次数
         */
        @Schema(description = "总异常次数", example = "1250")
        private Long totalExceptions;

        /**
         * 权限拒绝次数
         */
        @Schema(description = "权限拒绝次数", example = "890")
        private Long permissionDeniedCount;

        /**
         * 系统异常次数
         */
        @Schema(description = "系统异常次数", example = "280")
        private Long systemExceptionCount;

        /**
         * 权限超时次数
         */
        @Schema(description = "权限超时次数", example = "50")
        private Long timeoutExceptionCount;

        /**
         * 缓存异常次数
         */
        @Schema(description = "缓存异常次数", example = "30")
        private Long cacheExceptionCount;

        /**
         * 异常类型分布
         */
        @Schema(description = "异常类型分布")
        private Map<String, Long> exceptionTypeDistribution;

        /**
         * 可疑访问模式统计
         */
        @Schema(description = "可疑访问模式统计")
        private List<SuspiciousAccessPattern> suspiciousPatterns;

        /**
         * 安全事件统计
         */
        @Schema(description = "安全事件统计")
        private List<SecurityEvent> securityEvents;
    }

    /**
     * 用户权限分布统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户权限分布统计")
    public static class UserPermissionDistributionStats {

        /**
         * 总用户数
         */
        @Schema(description = "总用户数", example = "10000")
        private Long totalUsers;

        /**
         * 活跃用户数
         */
        @Schema(description = "活跃用户数", example = "8500")
        private Long activeUsers;

        /**
         * 用户活跃率
         */
        @Schema(description = "用户活跃率", example = "0.85")
        private BigDecimal userActiveRate;

        /**
         * 角色分布统计
         */
        @Schema(description = "角色分布统计")
        private Map<String, Long> roleDistribution;

        /**
         * 权限密度分布
         */
        @Schema(description = "权限密度分布")
        private Map<String, Long> permissionDensityDistribution;

        /**
         * 平均每用户权限数
         */
        @Schema(description = "平均每用户权限数", example = "25.5")
        private BigDecimal avgPermissionsPerUser;

        /**
         * 权限最多的用户TOP10
         */
        @Schema(description = "权限最多的用户TOP10")
        private List<UserPermissionCount> topPermissionUsers;

        /**
         * 权限最少的管理员用户
         */
        @Schema(description = "权限最少的管理员用户")
        private List<UserPermissionCount> minPermissionAdminUsers;
    }

    /**
     * 权限变更统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限变更统计")
    public static class PermissionChangeStats {

        /**
         * 总变更次数
         */
        @Schema(description = "总变更次数", example = "2500")
        private Long totalChanges;

        /**
         * 用户权限变更次数
         */
        @Schema(description = "用户权限变更次数", example = "1500")
        private Long userPermissionChanges;

        /**
         * 角色权限变更次数
         */
        @Schema(description = "角色权限变更次数", example = "800")
        private Long rolePermissionChanges;

        /**
         * 菜单权限变更次数
         */
        @Schema(description = "菜单权限变更次数", example = "200")
        private Long menuPermissionChanges;

        /**
         * 变更类型分布
         */
        @Schema(description = "变更类型分布")
        private Map<String, Long> changeTypeDistribution;

        /**
         * 变更来源分布
         */
        @Schema(description = "变更来源分布")
        private Map<String, Long> changeSourceDistribution;

        /**
         * 平均变更处理时间（毫秒）
         */
        @Schema(description = "平均变更处理时间（毫秒）", example = "1250.5")
        private BigDecimal avgChangeProcessTime;

        /**
         * 变更成功率
         */
        @Schema(description = "变更成功率", example = "0.99")
        private BigDecimal changeSuccessRate;
    }

    /**
     * 趋势数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "趋势数据")
    public static class TrendData {

        /**
         * 时间点
         */
        @Schema(description = "时间点", example = "2025-12-16T10:00:00")
        private LocalDateTime timePoint;

        /**
         * QPS值
         */
        @Schema(description = "QPS值", example = "1450.25")
        private BigDecimal qps;

        /**
         * 响应时间（毫秒）
         */
        @Schema(description = "响应时间（毫秒）", example = "42.3")
        private BigDecimal responseTime;

        /**
         * 缓存命中率
         */
        @Schema(description = "缓存命中率", example = "0.94")
        private BigDecimal cacheHitRate;

        /**
         * 错误率
         */
        @Schema(description = "错误率", example = "0.01")
        private BigDecimal errorRate;
    }

    // 其他内部类定义（简化展示）
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热点权限")
    public static class HotPermission {
        private String permission;
        private Long useCount;
        private BigDecimal useRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "冷门权限")
    public static class ColdPermission {
        private String permission;
        private Long useCount;
        private BigDecimal useRate;
        private Integer lastUsedDaysAgo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "模块权限统计")
    public static class ModulePermissionStats {
        private String moduleName;
        private Integer totalPermissions;
        private Integer activePermissions;
        private BigDecimal usageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "可疑访问模式")
    public static class SuspiciousAccessPattern {
        private String pattern;
        private Long occurrenceCount;
        private String riskLevel;
        private LocalDateTime lastOccurrence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "安全事件")
    public static class SecurityEvent {
        private String eventType;
        private String description;
        private LocalDateTime eventTime;
        private Long userId;
        private String ipAddress;
        private String riskLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户权限统计")
    public static class UserPermissionCount {
        private Long userId;
        private String username;
        private Integer permissionCount;
        private Integer roleCount;
    }

    /**
     * 计算综合健康度评分
     */
    public void calculateHealthScore() {
        if (validationStats == null || cacheStats == null || usageStats == null) {
            this.healthScore = BigDecimal.ZERO;
            return;
        }

        // 成功率权重 30%
        BigDecimal successScore = validationStats.getSuccessRate().multiply(BigDecimal.valueOf(30));

        // 响应时间权重 25%（50ms为满分）
        BigDecimal responseScore = BigDecimal.valueOf(50).divide(validationStats.getAverageResponseTime(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(25));

        // 缓存命中率权重 25%
        BigDecimal cacheScore = cacheStats.getOverallHitRate().multiply(BigDecimal.valueOf(25));

        // 使用率权重 20%
        BigDecimal usageScore = usageStats.getPermissionUsageRate().multiply(BigDecimal.valueOf(20));

        this.healthScore = successScore.add(responseScore).add(cacheScore).add(usageScore)
                .min(BigDecimal.valueOf(100));

        // 设置性能评级
        if (this.healthScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            this.performanceGrade = "EXCELLENT";
        } else if (this.healthScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            this.performanceGrade = "GOOD";
        } else if (this.healthScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            this.performanceGrade = "FAIR";
        } else {
            this.performanceGrade = "POOR";
        }
    }

    /**
     * 获取性能摘要
     */
    public String getPerformanceSummary() {
        if (validationStats == null) return "数据不足";

        return String.format("总验证: %d次, 成功率: %s%%, 平均响应: %sms, QPS: %s",
                validationStats.getTotalValidations(),
                validationStats.getSuccessRate().multiply(BigDecimal.valueOf(100)).toString(),
                validationStats.getAverageResponseTime().toString(),
                validationStats.getQps().toString());
    }

    /**
     * 获取缓存摘要
     */
    public String getCacheSummary() {
        if (cacheStats == null) return "数据不足";

        return String.format("整体命中率: %s%%, L1: %s条, L2: %s条",
                cacheStats.getOverallHitRate().multiply(BigDecimal.valueOf(100)).toString(),
                cacheStats.getL1CacheSize().toString(),
                cacheStats.getL2CacheSize().toString());
    }
}