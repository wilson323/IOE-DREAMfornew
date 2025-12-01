package net.lab1024.sa.admin.module.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备维护计划VO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@Schema(description = "设备维护计划")
public class DeviceMaintenancePlanVO {

    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "计划状态")
    private String planStatus;

    @Schema(description = "计划状态描述")
    private String planStatusText;

    @Schema(description = "触发原因")
    private String triggerReason;

    @Schema(description = "触发原因描述")
    private String triggerReasonText;

    @Schema(description = "计划类型")
    private String planType;

    @Schema(description = "计划类型描述")
    private String planTypeText;

    @Schema(description = "优先级")
    private Integer priorityLevel;

    @Schema(description = "优先级描述")
    private String priorityLevelText;

    @Schema(description = "优先级颜色")
    private String priorityColor;

    @Schema(description = "创建时健康评分")
    private BigDecimal scoreOnCreate;

    @Schema(description = "指派给用户ID")
    private Long assignedTo;

    @Schema(description = "指派人姓名")
    private String assignedToName;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleStart;

    @Schema(description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduleEnd;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStart;

    @Schema(description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEnd;

    @Schema(description = "预计耗时(分钟)")
    private Integer estimatedDuration;

    @Schema(description = "实际耗时(分钟)")
    private Integer actualDuration;

    @Schema(description = "维护描述")
    private String description;

    @Schema(description = "维护结果备注")
    private String resultNote;

    @Schema(description = "维护费用")
    private BigDecimal costAmount;

    @Schema(description = "使用的零件")
    private String partsUsed;

    @Schema(description = "完成率(百分比)")
    private BigDecimal completionRate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "更新人姓名")
    private String updateUserName;

    @Schema(description = "是否超期")
    private Boolean isOverdue;

    @Schema(description = "剩余天数")
    private Integer remainingDays;
}