/*
 * 批量通知结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量通知结果
 * 封装批量发送通知的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchNotificationResult {

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failureCount;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 批次ID
     */
    private String batchId;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTimeMs;

    /**
     * 成功结果列表
     */
    private List<SecurityNotificationResult> successResults;

    /**
     * 失败结果列表
     */
    private List<SecurityNotificationResult> failureResults;

    /**
     * 汇总消息
     */
    private String summaryMessage;

    /**
     * 获取成功率
     */
    public Double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) successCount / totalCount * 100;
    }

    /**
     * 检查是否全部成功
     */
    public boolean isAllSuccess() {
        return totalCount != null && totalCount.equals(successCount);
    }

    /**
     * 检查是否全部失败
     */
    public boolean isAllFailure() {
        return totalCount != null && totalCount.equals(failureCount);
    }

    /**
     * 检查是否部分成功
     */
    public boolean isPartialSuccess() {
        return !isAllSuccess() && !isAllFailure() && successCount > 0;
    }

    /**
     * 获取汇总描述
     */
    public String getSummaryDescription() {
        return String.format("批量通知发送完成 - 总数: %d, 成功: %d, 失败: %d, 成功率: %.1f%%",
                totalCount != null ? totalCount : 0,
                successCount != null ? successCount : 0,
                failureCount != null ? failureCount : 0,
                getSuccessRate());
    }

    /**
     * 获取失败原因统计
     */
    public String getFailureReasonSummary() {
        if (failureResults == null || failureResults.isEmpty()) {
            return "无失败原因";
        }

        StringBuilder summary = new StringBuilder("失败原因统计:\n");

        // 按失败原因分组统计
        failureResults.stream()
                .filter(result -> result.getResultCode() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        SecurityNotificationResult::getResultCode,
                        java.util.stream.Collectors.counting()
                ))
                .forEach((reason, count) -> {
                    summary.append(String.format("  %s: %d次\n", reason, count));
                });

        return summary.toString();
    }

    /**
     * 获取处理建议
     */
    public String getProcessingSuggestion() {
        if (isAllSuccess()) {
            return "所有通知发送成功，无需进一步处理";
        } else if (isAllFailure()) {
            return "所有通知发送失败，建议检查通知服务配置和网络连接";
        } else if (isPartialSuccess()) {
            return "部分通知发送成功，建议重试失败的通知";
        } else {
            return "批量发送结果异常，建议重新发起";
        }
    }

    /**
     * 计算处理时间
     */
    public void calculateProcessingTime() {
        if (startTime != null && endTime != null) {
            processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    /**
     * 生成批次ID
     */
    public static String generateBatchId() {
        return "BATCH_" + System.currentTimeMillis() + "_" +
               Integer.toHexString((int)(Math.random() * 0xFFFF));
    }
}