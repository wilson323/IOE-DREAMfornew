package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 成本优化结果
 * <p>
 * 封装成本优化的结果
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
public class CostOptimizationResult {

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 原始成本
     */
    private Double originalCost;

    /**
     * 优化后成本
     */
    private Double optimizedCost;

    /**
     * 成本节省
     */
    private Double costSaving;

    /**
     * 成本节省率
     */
    private Double costSavingRate;

    /**
     * 成本明细
     */
    private Map<String, Double> costBreakdown;
}
