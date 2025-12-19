package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;

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
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 优化范围
     */
    private OptimizationScope optimizationScope;

    /**
     * 局部优化评分
     */
    private Double localScore;
}
