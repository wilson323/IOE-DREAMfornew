package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 离线消费记录DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "离线消费记录")
public class OfflineConsumeDTO {

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

    @Schema(description = "数字签名")
    private String signature;

    @Schema(description = "校验和")
    private String checkSum;
}
