package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.util.List;
import java.util.Map;

/**
 * 覆盖率优化结果
 * <p>
 * 封装班次覆盖率优化的结果
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
public class CoverageOptimizationResult {

    /**
     * 优化后的排班记录
     */
    private List<ScheduleRecord> optimizedRecords;

    /**
     * 覆盖率统计
     */
    private Map<String, Double> coverageStatistics;

    /**
     * 覆盖率达成率
     */
    private Double coverageAchievementRate;

    /**
     * 覆盖率改进率
     */
    private Double improvementRate;
}
