package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 充值订单视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Builder
@Schema(description = "充值订单视图对象")
public class ConsumeRechargeOrderVO {

    @Schema(description = "充值订单ID", example = "1001")
    private Long orderId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal rechargeAmount;

    @Schema(description = "实际到账金额", example = "100.00")
    private BigDecimal actualAmount;

    @Schema(description = "支付方式", example = "WECHAT")
    private String paymentMethod;

    @Schema(description = "支付方式名称", example = "微信支付")
    private String paymentMethodName;

    @Schema(description = "订单状态", example = "SUCCESS")
    private String orderStatus;

    @Schema(description = "订单状态名称", example = "支付成功")
    private String orderStatusName;

    @Schema(description = "第三方交易号", example = "WX20251224103000123456")
    private String transactionId;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;

    @Schema(description = "下单时间", example = "2025-12-24T10:30:00")
    private LocalDateTime orderTime;

    @Schema(description = "支付时间", example = "2025-12-24T10:31:00")
    private LocalDateTime payTime;

    @Schema(description = "创建时间", example = "2025-12-24T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-24T10:31:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "月度充值")
    private String remark;

    @Schema(description = "版本号", example = "1")
    private Integer version;
}
