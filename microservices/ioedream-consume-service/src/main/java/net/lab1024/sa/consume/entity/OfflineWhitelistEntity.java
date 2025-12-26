package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线消费白名单Entity
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_offline_whitelist")
@Schema(description = "离线消费白名单")
public class OfflineWhitelistEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "白名单ID")
    private Long whitelistId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "白名单类型 1-用户白名单 2-设备白名单 3-区域白名单")
    private Integer whitelistType;

    @Schema(description = "单笔最大金额")
    private BigDecimal maxSingleAmount;

    @Schema(description = "每日最大金额")
    private BigDecimal maxDailyAmount;

    @Schema(description = "每日最大次数")
    private Integer maxDailyCount;

    @Schema(description = "生效日期")
    private LocalDateTime effectiveDate;

    @Schema(description = "失效日期")
    private LocalDateTime expireDate;

    @Schema(description = "启用状态 0-禁用 1-启用")
    private Integer enabled;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "更新人ID")
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;
}
