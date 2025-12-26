package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增联动规则表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "新增联动规则表单")
public class AccessLinkageRuleAddForm {

    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    @Schema(description = "规则名称", example = "主门联动侧门")
    private String ruleName;

    @NotBlank(message = "规则编码不能为空")
    @Size(max = 50, message = "规则编码长度不能超过50个字符")
    @Schema(description = "规则编码", example = "LINKAGE_MAIN_TO_SIDE")
    private String ruleCode;

    @NotBlank(message = "联动类型不能为空")
    @Schema(description = "联动类型(DOOR_LINKAGE-门联动, AREA_LINKAGE-区域联动)", example = "DOOR_LINKAGE")
    private String ruleType;

    @NotBlank(message = "触发类型不能为空")
    @Schema(description = "触发类型(OPEN_DOOR-开门, CLOSE_DOOR-关门, ALARM-告警)", example = "OPEN_DOOR")
    private String triggerType;

    @NotNull(message = "触发设备ID不能为空")
    @Schema(description = "触发设备ID", example = "1001")
    private Long triggerDeviceId;

    @Schema(description = "触发门ID", example = "1001")
    private Long triggerDoorId;

    @NotBlank(message = "执行动作不能为空")
    @Schema(description = "执行动作(OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-锁定, UNLOCK-解锁)", example = "OPEN_DOOR")
    private String actionType;

    @NotNull(message = "目标设备ID不能为空")
    @Schema(description = "目标设备ID", example = "1002")
    private Long targetDeviceId;

    @Schema(description = "目标门ID", example = "2001")
    private Long targetDoorId;

    @Schema(description = "延迟执行秒数", example = "3")
    private Integer delaySeconds;

    @Schema(description = "条件表达式(JSON格式)", example = "{\"timeRange\": \"09:00-18:00\"}")
    private String conditionExpression;

    @Schema(description = "优先级(1-100, 数字越小优先级越高)", example = "10")
    private Integer priority;

    @Schema(description = "规则描述", example = "主门打开3秒后自动打开侧门")
    private String description;
}
