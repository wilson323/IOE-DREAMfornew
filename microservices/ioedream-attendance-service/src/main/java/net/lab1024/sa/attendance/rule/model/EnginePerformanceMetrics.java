package net.lab1024.sa.attendance.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引擎性能指标
 *
 * <p>用于描述规则引擎的性能统计信息。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnginePerformanceMetrics {

    /**
     * 执行总次数
     */
    private long totalExecutions;

    /**
     * 成功次数
     */
    private long totalSuccess;

    /**
     * 失败次数
     */
    private long totalFailures;

    /**
     * 成功率（0-1）
     */
    private double successRate;

    /**
     * 平均执行时长（毫秒）
     */
    private double averageExecutionTime;

    /**
     * 缓存规则数量
     */
    private int cachedRulesCount;

    /**
     * 内存使用（MB）
     */
    private double memoryUsageMb;

    /**
     * 运行时长（毫秒）
     */
    private long uptime;
}

