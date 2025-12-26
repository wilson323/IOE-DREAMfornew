package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排班模板实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@TableName("t_attendance_schedule_template")
@Schema(description = "排班模板")
public class ScheduleTemplateEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "模板类型：1-个人模板 2-部门模板 3-全局模板")
    private Integer templateType;

    @Schema(description = "适用对象ID（员工ID/部门ID）")
    private Long targetId;

    @Schema(description = "周期类型：1-每天 2-每周 3-每月")
    private Integer cycleType;

    @Schema(description = "周期配置（JSON格式）")
    private String cycleConfig;

    @Schema(description = "班次配置（JSON格式）")
    private String shiftConfig;

    @Schema(description = "生效开始日期")
    private String effectiveStartDate;

    @Schema(description = "生效结束日期")
    private String effectiveEndDate;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记：0-未删除 1-已删除")
    private Integer deletedFlag;
}
