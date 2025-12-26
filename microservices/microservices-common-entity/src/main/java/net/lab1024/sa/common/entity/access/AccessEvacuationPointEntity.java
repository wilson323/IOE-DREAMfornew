package net.lab1024.sa.common.entity.access;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁疏散点实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_evacuation_point")
@Schema(description = "门禁疏散点实体")
public class AccessEvacuationPointEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "疏散点ID")
    private Long pointId;

    @TableField("point_name")
    @Schema(description = "疏散点名称")
    private String pointName;

    @TableField("point_code")
    @Schema(description = "疏散点编码")
    private String pointCode;

    @TableField("area_id")
    @Schema(description = "关联区域ID")
    private Long areaId;

    @TableField("area_name")
    @Schema(description = "关联区域名称")
    private String areaName;

    @TableField("door_ids")
    @Schema(description = "关联门ID列表(JSON数组)")
    private String doorIds;

    @TableField("door_names")
    @Schema(description = "关联门名称列表(JSON数组)")
    private String doorNames;

    @TableField("device_ids")
    @Schema(description = "关联设备ID列表(JSON数组)")
    private String deviceIds;

    @TableField("evacuation_type")
    @Schema(description = "疏散类型 (FIRE-火灾 EARTHQUAKE-地震 EMERGENCY-紧急)")
    private String evacuationType;

    @TableField("priority")
    @Schema(description = "优先级(1-100)")
    private Integer priority;

    @TableField("enabled")
    @Schema(description = "启用状态 (1-启用 0-禁用)")
    private Integer enabled;

    @TableField("description")
    @Schema(description = "疏散点描述")
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
