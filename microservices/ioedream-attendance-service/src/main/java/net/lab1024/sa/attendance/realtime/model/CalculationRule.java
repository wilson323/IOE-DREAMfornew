package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 计算规则（简化）
 *
 * <p>
 * 用于实时引擎注册/管理计算规则。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRule {

    /**
     * 规则ID
     */
    private String ruleId;

    /**
     * 规则表达式（占位）
     */
    private String ruleExpression;
}
