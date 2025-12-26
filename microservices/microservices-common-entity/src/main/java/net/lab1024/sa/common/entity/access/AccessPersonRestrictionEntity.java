package net.lab1024.sa.common.entity.access;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门禁人员限制实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_access_person_restriction")
@Schema(description = "门禁人员限制实体")
public class AccessPersonRestrictionEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "限制规则ID")
    private Long restrictionId;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("user_name")
    @Schema(description = "用户姓名")
    private String userName;

    @TableField("user_phone")
    @Schema(description = "用户手机号")
    private String userPhone;

    @TableField("restriction_type")
    @Schema(description = "限制类型 (BLACKLIST-黑名单 WHITELIST-白名单 TIME_BASED-时段限制)")
    private String restrictionType;

    @TableField("area_ids")
    @Schema(description = "限制区域ID列表(JSON数组)")
    private String areaIds;

    @TableField("area_names")
    @Schema(description = "限制区域名称列表(JSON数组)")
    private String areaNames;

    @TableField("door_ids")
    @Schema(description = "限制门ID列表(JSON数组,空表示所有门)")
    private String doorIds;

    @TableField("time_start")
    @Schema(description = "限制开始时间(HH:mm:ss)")
    private String timeStart;

    @TableField("time_end")
    @Schema(description = "限制结束时间(HH:mm:ss)")
    private String timeEnd;

    @TableField("effective_date")
    @Schema(description = "生效日期")
    private LocalDate effectiveDate;

    @TableField("expire_date")
    @Schema(description = "失效日期")
    private LocalDate expireDate;

    @TableField("reason")
    @Schema(description = "限制原因")
    private String reason;

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
