package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 多目标优化配置
 * <p>
 * 定义多目标优化的目标配置
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
public class MultiObjectiveGoals {

    /**
     * 优化目标列表
     */
    private List<OptimizationGoal> goals;

    /**
     * 优化算法类型
     */
    private String algorithmType;

    /**
     * 是否使用帕累托优化
     */
    private Boolean usePareto;
}
