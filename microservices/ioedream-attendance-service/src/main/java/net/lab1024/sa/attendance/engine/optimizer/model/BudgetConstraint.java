package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 预算约束
 * <p>
 * 定义成本优化的预算约束条件
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
public class BudgetConstraint {

    /**
     * 最大预算
     */
    private Double maxBudget;

    /**
     * 最小预算
     */
    private Double minBudget;

    /**
     * 目标预算
     */
    private Double targetBudget;

    /**
     * 预算类型（DAILY-日预算, WEEKLY-周预算, MONTHLY-月预算）
     */
    private String budgetType;

    /**
     * 预算约束参数
     */
    private Map<String, Object> constraintParameters;
}
