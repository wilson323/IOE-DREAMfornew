package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁人数控制实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_capacity_control")
@Schema(description = "门禁人数控制实体")
public class AccessCapacityControlEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "控制规则ID")
    private Long controlId;

    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;

    @TableField("door_ids")
    @Schema(description = "关联门ID列表(JSON数组)")
    private String doorIds;

    @TableField("max_capacity")
    @Schema(description = "最大容量")
    private Integer maxCapacity;

    @TableField("current_count")
    @Schema(description = "当前人数")
    private Integer currentCount;

    @TableField("control_mode")
    @Schema(description = "控制模式 (STRICT-严格模式 WARNING-警告模式)")
    private String controlMode;

    @TableField("entry_blocked")
    @Schema(description = "是否禁止进入 (1-禁止 0-允许)")
    private Integer entryBlocked;

    @TableField("alert_threshold")
    @Schema(description = "告警阈值(百分比)")
    private Integer alertThreshold;

    @TableField("auto_reset")
    @Schema(description = "是否自动重置 (1-是 0-否)")
    private Integer autoReset;

    @TableField("reset_time")
    @Schema(description = "重置时间(HH:mm:ss)")
    private String resetTime;

    @TableField("priority")
    @Schema(description = "优先级(1-100)")
    private Integer priority;

    @TableField("enabled")
    @Schema(description = "启用状态 (1-启用 0-禁用)")
    private Integer enabled;

    @TableField("description")
    @Schema(description = "规则描述")
    private String description;

    @TableField("create_time")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("create_user_id")
    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField("update_user_id")
    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableField("deleted_flag")
    @Schema(description = "删除标记 (0-未删除 1-已删除)")
    private Integer deletedFlag;
}
