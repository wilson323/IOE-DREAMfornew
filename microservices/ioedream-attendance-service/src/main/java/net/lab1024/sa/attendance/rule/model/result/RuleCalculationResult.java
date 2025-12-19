package net.lab1024.sa.attendance.rule.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.rule.model.RuleExecutionResult;

import java.util.List;

/**
 * 规则计算结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCalculationResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
    private int processedRules;
    private List<RuleExecutionResult> executionResults;
    private long executionTime;
}

