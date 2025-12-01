package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.RechargeStatusEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.RechargeTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "充值结果")
public class RechargeResultDTO {

    @Schema(description = "订单号", example = "RC1694876400001234")
    private String orderNo;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "充值方式", example = "ALIPAY")
    private RechargeTypeEnum rechargeType;

    @Schema(description = "充值状态", example = "PENDING")
    private RechargeStatusEnum status;

    @Schema(description = "支付链接", example = "https://openapi.alipay.com/gateway.do")
    private String payUrl;

    @Schema(description = "二维码", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String qrCode;

    @Schema(description = "消息", example = "充值申请成功，请尽快完成支付")
    private String message;

    @Schema(description = "创建时间", example = "2023-11-17T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "过期时间", example = "2023-11-17T11:30:00")
    private LocalDateTime expireTime;

    @Schema(description = "第三方订单号", example = "2023111722001234567890")
    private String thirdPartyOrderNo;

    @Schema(description = "手续费", example = "0.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额", example = "100.00")
    private BigDecimal actualAmount;
}