package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 局部优化结果
 * <p>
 * 封装局部优化的结果
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
public class LocalOptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 优化范围内的记录
     */
    private List<ScheduleRecord> scopeRecords;

    /**
     * 优化后范围内的记录
     */
    private List<ScheduleRecord> optimizedScopeRecords;

    /**
     * 局部改进率
     */
    private Double localImprovementRate;

    /**
     * 优化是否成功
     */
    private Boolean optimizationSuccessful;

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 最终排班记录（兼容实现层字段名）
     */
    private List<ScheduleRecord> finalRecords;

    /**
     * 优化范围
     */
    private OptimizationScope optimizationScope;

    /**
     * 局部优化评分
     */
    private Double localScore;

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
