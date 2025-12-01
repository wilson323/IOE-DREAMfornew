package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.RechargeTypeEnum;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 充值请求DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "充值请求参数")
public class RechargeRequestDTO {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @NotNull(message = "充值方式不能为空")
    @Schema(description = "充值方式", example = "ALIPAY")
    private RechargeTypeEnum rechargeType;

    @Schema(description = "支付渠道", example = "ALIPAY_APP")
    private String payChannel;

    @Schema(description = "支付账户", example = "user@example.com")
    private String payAccount;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注", example = "用户账户充值")
    private String remark;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 13")
    private String deviceInfo;

    @Schema(description = "是否快速充值", example = "false")
    private Boolean quickRecharge;
}