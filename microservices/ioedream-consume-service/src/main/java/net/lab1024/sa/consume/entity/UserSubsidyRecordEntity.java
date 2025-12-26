package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户补贴记录实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user_subsidy_record")
@Schema(description = "用户补贴记录实体")
public class UserSubsidyRecordEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "记录UUID")
    private String recordUuid;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "补贴类型 1-餐补 2-交通补 3-其他")
    private Integer subsidyType;

    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    @Schema(description = "原消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "应用规则ID")
    private Long ruleId;

    @Schema(description = "应用规则编码")
    private String ruleCode;

    @Schema(description = "消费记录ID")
    private Long consumeId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "餐别 1-早餐 2-午餐 3-晚餐")
    private Integer mealType;

    @Schema(description = "补贴日期")
    private LocalDate subsidyDate;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "状态 0-失效 1-有效 2-已冲回")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
