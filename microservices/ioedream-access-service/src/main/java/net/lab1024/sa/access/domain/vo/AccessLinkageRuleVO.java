package net.lab1024.sa.access.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联动规则VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "联动规则VO")
public class AccessLinkageRuleVO {

    @Schema(description = "联动规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "联动类型(DOOR_LINKAGE-门联动, AREA_LINKAGE-区域联动)")
    private String ruleType;

    @Schema(description = "联动类型描述")
    private String ruleTypeDesc;

    @Schema(description = "触发类型(OPEN_DOOR-开门, CLOSE_DOOR-关门, ALARM-告警)")
    private String triggerType;

    @Schema(description = "触发类型描述")
    private String triggerTypeDesc;

    @Schema(description = "触发设备ID")
    private Long triggerDeviceId;

    @Schema(description = "触发门ID")
    private Long triggerDoorId;

    @Schema(description = "执行动作(OPEN_DOOR-开门, CLOSE_DOOR-关门, LOCK-锁定, UNLOCK-解锁)")
    private String actionType;

    @Schema(description = "执行动作描述")
    private String actionTypeDesc;

    @Schema(description = "目标设备ID")
    private Long targetDeviceId;

    @Schema(description = "目标门ID")
    private Long targetDoorId;

    @Schema(description = "延迟执行秒数")
    private Integer delaySeconds;

    @Schema(description = "条件表达式(JSON格式)")
    private String conditionExpression;

    @Schema(description = "优先级(1-100, 数字越小优先级越高)")
    private Integer priority;

    @Schema(description = "启用状态(0-禁用 1-启用)")
    private Integer enabled;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
