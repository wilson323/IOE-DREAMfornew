package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线消费记录Entity
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@TableName("t_offline_consume_record")
@Schema(description = "离线消费记录")
public class OfflineConsumeRecordEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "消费设备ID")
    private Long deviceId;

    @Schema(description = "消费设备编码")
    private String deviceCode;

    @Schema(description = "消费金额")
    private BigDecimal amount;

    @Schema(description = "消费类型 1-普通消费 2-补贴消费 3-离线白名单消费")
    private Integer consumeType;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "同步状态 0-待同步 1-同步中 2-已同步 3-冲突")
    private Integer syncStatus;

    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    @Schema(description = "同步错误信息")
    private String syncErrorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "冲突类型 1-时间戳冲突 2-余额冲突 3-重复交易")
    private Integer conflictType;

    @Schema(description = "冲突原因")
    private String conflictReason;

    @Schema(description = "是否已解决 0-未解决 1-已自动解决 2-已人工处理")
    private Integer resolved;

    @Schema(description = "解决时间")
    private LocalDateTime resolvedTime;

    @Schema(description = "解决备注")
    private String resolvedRemark;

    @Schema(description = "数字签名")
    private String signature;

    @Schema(description = "校验和")
    private String checkSum;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
