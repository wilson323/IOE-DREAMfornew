package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付密码验证结果VO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "支付密码验证结果")
public class PaymentPasswordResult {

    @Schema(description = "验证结果", example = "true")
    private Boolean success;

    @Schema(description = "验证消息", example = "密码验证成功")
    private String message;

    @Schema(description = "验证时间", example = "2023-11-17T10:30:00")
    private LocalDateTime verifyTime;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "验证类型", example = "PASSWORD_CHECK")
    private String verifyType;

    @Schema(description = "剩余尝试次数", example = "3")
    private Integer remainingAttempts;

    @Schema(description = "是否锁定", example = "false")
    private Boolean isLocked;

    @Schema(description = "锁定时间", example = "2023-11-17T10:35:00")
    private LocalDateTime lockTime;

    @Schema(description = "验证方式", example = "BIOMETRIC")
    private String verifyMethod;
}