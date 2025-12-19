package net.lab1024.sa.attendance.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.rule.model.AttendanceRuleConfig.ActionType;

/**
 * 动作执行结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {
    private ActionType actionType;
    private Boolean success;
    private String resultMessage;
    private String errorMessage;
    private long executionTime;
}

