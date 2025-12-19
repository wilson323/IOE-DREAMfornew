package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 覆盖率目标
 * <p>
 * 定义班次覆盖率的目标配置
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
public class CoverageTargets {

    /**
     * 班次覆盖率目标（班次ID -> 目标覆盖率）
     */
    private Map<Long, Double> shiftCoverageTargets;

    /**
     * 时间段覆盖率目标（时间段 -> 目标覆盖率）
     */
    private Map<String, Double> timeSlotCoverageTargets;

    /**
     * 最小覆盖率要求
     */
    private Double minCoverage;
}
