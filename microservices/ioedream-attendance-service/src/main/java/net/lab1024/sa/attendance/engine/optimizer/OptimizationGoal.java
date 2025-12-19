package net.lab1024.sa.attendance.engine.optimizer;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 优化目标模型
 * <p>
 * 定义排班优化的目标配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationGoal {

    /**
     * 目标类型：BALANCE-均衡 COST-成本 PREFERENCE-偏好 EFFICIENCY-效率
     */
    private String goalType;

    /**
     * 目标名称
     */
    private String goalName;

    /**
     * 目标权重（0-1）
     */
    private Double weight;

    /**
     * 目标值
     */
    private Double targetValue;

    /**
     * 目标优先级（1-10，数字越大优先级越高）
     */
    private Integer priority;

    /**
     * 是否必须达成
     */
    private Boolean isRequired;

    /**
     * 容忍度
     */
    private Double tolerance;

    /**
     * 目标参数
     */
    private Map<String, Object> parameters;

    /**
     * 获取目标类型（兼容getType方法）
     */
    public String getType() {
        return goalType;
    }
}
