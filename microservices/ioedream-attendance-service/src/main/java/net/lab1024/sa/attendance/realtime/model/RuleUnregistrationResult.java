package net.lab1024.sa.attendance.realtime.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规则注销结果
 *
 * <p>
 * 用于描述实时引擎注销计算规则的结果。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleUnregistrationResult {

    private String ruleId;

    private boolean unregistrationSuccessful;

    private LocalDateTime unregistrationTime;

    private String errorMessage;
}
