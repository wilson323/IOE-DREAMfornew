package net.lab1024.sa.attendance.engine.optimizer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优化建议
 * <p>
 * 封装排班优化的建议信息
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
public class OptimizationSuggestion {

    /**
     * 建议类型
     */
    private String suggestionType;

    /**
     * 建议描述
     */
    private String description;

    /**
     * 建议优先级
     */
    private Integer priority;

    /**
     * 预期改进效果
     */
    private Double expectedImprovement;

    /**
     * 建议详情
     */
    private Object suggestionDetails;
}
