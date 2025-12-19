package net.lab1024.sa.attendance.engine.prediction.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预测建议模型
 * <p>
 * 封装预测建议信息 严格遵循CLAUDE.md全局架构规范
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
public class PredictionSuggestion {
    private String suggestionId;
    private String suggestionTitle;
    private String suggestionType;
    private String suggestionContent;
    private String suggestionDescription;
    private Integer priority;
    private Double expectedImpact;
}
