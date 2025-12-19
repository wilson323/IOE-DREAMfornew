package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 工作负载优化结果
 * <p>
 * 封装工作负载均衡优化的结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadOptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 原始工作负载分布
     */
    private Map<Long, Double> originalWorkloadDistribution;

    /**
     * 优化后工作负载分布
     */
    private Map<Long, Double> optimizedWorkloadDistribution;

    /**
     * 原始标准差
     */
    private Double originalStdDeviation;

    /**
     * 优化后标准差
     */
    private Double optimizedStdDeviation;

    /**
     * 优化是否成功
     */
    private Boolean optimizationSuccessful;

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 工作负载统计
     */
    private Map<String, Double> workloadStatistics;

    /**
     * 负载均衡度
     */
    private Double balanceScore;

    /**
     * 负载改进率
     */
    private Double improvementRate;

    /**
     * 优化指标
     */
    private Map<String, Object> optimizedMetrics;

    public boolean isOptimizationSuccessful() {
        return Boolean.TRUE.equals(optimizationSuccessful);
    }

    public Map<String, Object> getOptimizedMetrics() {
        return optimizedMetrics;
    }
}
