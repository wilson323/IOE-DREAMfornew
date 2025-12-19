package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 优化范围
 * <p>
 * 定义局部优化的范围配置
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
public class OptimizationScope {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 员工ID列表（为空表示所有员工）
     */
    private List<Long> employeeIds;

    /**
     * 班次ID列表（为空表示所有班次）
     */
    private List<Long> shiftIds;

    /**
     * 部门ID列表（为空表示所有部门）
     */
    private List<Long> departmentIds;
}
