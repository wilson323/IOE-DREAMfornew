package net.lab1024.sa.attendance.rule.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量规则计算结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRuleCalculationResult {
    private boolean success;
    private String message;
    private String errorMessage;
    private String errorCode;
    private int totalRequests;
    private int successfulRequests;
    private List<RuleCalculationResult> results;
    private long executionTime;
}

