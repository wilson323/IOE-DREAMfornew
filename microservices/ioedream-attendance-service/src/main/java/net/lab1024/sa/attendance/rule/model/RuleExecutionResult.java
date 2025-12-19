package net.lab1024.sa.attendance.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 规则执行结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionResult {
    private String ruleId;
    private String ruleName;
    private Boolean success;
    private Boolean conditionsMet;
    private List<ActionResult> actionResults;
    private String errorMessage;
    private long executionTime;
}

