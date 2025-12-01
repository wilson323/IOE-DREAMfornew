/*
 * 通知统计
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知统计
 * 通知发送的统计信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatistics {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 统计开始时间
     */
    private LocalDateTime startTime;

    /**
     * 统计结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总发送数量
     */
    private Integer totalCount;

    /**
     * 成功发送数量
     */
    private Integer successCount;

    /**
     * 失败发送数量
     */
    private Integer failureCount;

    /**
     * 成功率
     */
    private BigDecimal successRate;

    /**
     * 邮件发送数量
     */
    private Integer emailCount;

    /**
     * 短信发送数量
     */
    private Integer smsCount;

    /**
     * 推送发送数量
     */
    private Integer pushCount;

    /**
     * 微信发送数量
     */
    private Integer wechatCount;

    /**
     * 各类型通知数量
     */
    private Map<String, Integer> notificationTypeCount;

    /**
     * 各小时发送分布
     */
    private Map<Integer, Integer> hourlyDistribution;

    /**
     * 平均响应时间（毫秒）
     */
    private Long averageResponseTimeMs;

    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTimeMs;

    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTimeMs;

    /**
     * 重试次数统计
     */
    private Integer totalRetryCount;

    /**
     * 计算成功率
     */
    public BigDecimal calculateSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return BigDecimal.ZERO;
        }

        if (successCount == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(successCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取主要通知渠道
     */
    public String getPrimaryChannel() {
        int max = 0;
        String primaryChannel = "UNKNOWN";

        if (emailCount != null && emailCount > max) {
            max = emailCount;
            primaryChannel = "EMAIL";
        }

        if (smsCount != null && smsCount > max) {
            max = smsCount;
            primaryChannel = "SMS";
        }

        if (pushCount != null && pushCount > max) {
            max = pushCount;
            primaryChannel = "PUSH";
        }

        if (wechatCount != null && wechatCount > max) {
            max = wechatCount;
            primaryChannel = "WECHAT";
        }

        return primaryChannel;
    }

    /**
     * 获取发送高峰时段
     */
    public String getPeakHour() {
        if (hourlyDistribution == null || hourlyDistribution.isEmpty()) {
            return "无数据";
        }

        Map.Entry<Integer, Integer> peakEntry = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (peakEntry != null) {
            return String.format("%02d:00-%02d:00", peakEntry.getKey(), peakEntry.getKey() + 1);
        }

        return "无数据";
    }

    /**
     * 获取统计摘要
     */
    public String getStatisticsSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("通知发送统计 (%s):\n", timeRange != null ? timeRange : "未知时间段"));
        summary.append(String.format("  总发送量: %d\n", totalCount != null ? totalCount : 0));
        summary.append(String.format("  成功数量: %d\n", successCount != null ? successCount : 0));
        summary.append(String.format("  失败数量: %d\n", failureCount != null ? failureCount : 0));
        summary.append(String.format("  成功率: %.2f%%\n", calculateSuccessRate()));

        summary.append("  分渠道统计:\n");
        if (emailCount != null && emailCount > 0) {
            summary.append(String.format("    邮件: %d\n", emailCount));
        }
        if (smsCount != null && smsCount > 0) {
            summary.append(String.format("    短信: %d\n", smsCount));
        }
        if (pushCount != null && pushCount > 0) {
            summary.append(String.format("    推送: %d\n", pushCount));
        }
        if (wechatCount != null && wechatCount > 0) {
            summary.append(String.format("    微信: %d\n", wechatCount));
        }

        if (averageResponseTimeMs != null) {
            summary.append(String.format("  平均响应时间: %d ms\n", averageResponseTimeMs));
        }

        if (totalRetryCount != null && totalRetryCount > 0) {
            summary.append(String.format("  总重试次数: %d\n", totalRetryCount));
        }

        summary.append(String.format("  高峰时段: %s\n", getPeakHour()));

        return summary.toString();
    }

    /**
     * 获取性能评级
     */
    public String getPerformanceRating() {
        if (totalCount == null || totalCount == 0) {
            return "无数据";
        }

        BigDecimal successRate = calculateSuccessRate();
        Long avgResponseTime = averageResponseTimeMs != null ? averageResponseTimeMs : 0L;

        // 综合评分
        if (successRate.compareTo(BigDecimal.valueOf(95)) >= 0 && avgResponseTime < 1000) {
            return "优秀";
        } else if (successRate.compareTo(BigDecimal.valueOf(90)) >= 0 && avgResponseTime < 2000) {
            return "良好";
        } else if (successRate.compareTo(BigDecimal.valueOf(80)) >= 0 && avgResponseTime < 5000) {
            return "一般";
        } else {
            return "需要改进";
        }
    }

    /**
     * 获取优化建议
     */
    public String getOptimizationSuggestions() {
        StringBuilder suggestions = new StringBuilder();
        BigDecimal successRate = calculateSuccessRate();
        Long avgResponseTime = averageResponseTimeMs != null ? averageResponseTimeMs : 0L;

        if (successRate.compareTo(BigDecimal.valueOf(90)) < 0) {
            suggestions.append("成功率偏低，建议检查网络连接和第三方服务稳定性\n");
        }

        if (avgResponseTime > 3000) {
            suggestions.append("响应时间较长，建议优化服务性能或考虑异步发送\n");
        }

        if (totalRetryCount != null && totalRetryCount > 0) {
            BigDecimal retryRate = BigDecimal.valueOf(totalRetryCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalCount), 2, BigDecimal.ROUND_HALF_UP);

            if (retryRate.compareTo(BigDecimal.valueOf(10)) > 0) {
                suggestions.append("重试率较高，建议检查配置参数和服务稳定性\n");
            }
        }

        if (getPrimaryChannel().equals("EMAIL")) {
            suggestions.append("主要使用邮件通知，建议启用更多即时通知渠道\n");
        }

        return suggestions.length() > 0 ? suggestions.toString() : "当前表现良好，无特别建议";
    }

    /**
     * 渠道统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelCount {
        /**
         * 渠道名称
         */
        private String channel;

        /**
         * 发送数量
         */
        private Integer count;
    }

    /**
     * 类型统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeCount {
        /**
         * 通知类型
         */
        private String type;

        /**
         * 发送数量
         */
        private Integer count;
    }
}
