package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;

/**
 * 更新联动规则表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "更新联动规则表单")
public class AccessLinkageRuleUpdateForm {

    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "执行动作(OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-锁定, UNLOCK-解锁)")
    private String actionType;

    @Schema(description = "延迟执行秒数")
    private Integer delaySeconds;

    @Schema(description = "条件表达式(JSON格式)")
    private String conditionExpression;

    @Schema(description = "优先级(1-100, 数字越小优先级越高)")
    private Integer priority;

    @Schema(description = "规则描述")
    private String description;
}
