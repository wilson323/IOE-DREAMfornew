package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动端消费结果VO
 * 移动端优化的消费结果数据结构
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端消费结果")
public class MobileConsumeResultVO {

    @Schema(description = "订单ID", example = "MOBILE_20250130001")
    private String orderId;

    @Schema(description = "消费状态", example = "SUCCESS")
    private String status;

    @Schema(description = "消费消息", example = "消费成功")
    private String message;

    @Schema(description = "消费时间", example = "2025-01-30 12:30:00")
    private LocalDateTime consumeTime;

    @Schema(description = "消费金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "消费前余额", example = "1000.00")
    private BigDecimal balanceBefore;

    @Schema(description = "消费后余额", example = "950.00")
    private BigDecimal balanceAfter;

    @Schema(description = "商户名称", example = "食堂一楼")
    private String merchantName;

    @Schema(description = "设备名称", example = "POS机001")
    private String deviceName;

    @Schema(description = "是否需要签名", example = "false")
    private Boolean requireSignature;
}


