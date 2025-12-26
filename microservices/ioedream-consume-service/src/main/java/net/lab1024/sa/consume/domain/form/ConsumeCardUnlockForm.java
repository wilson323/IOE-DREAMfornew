package net.lab1024.sa.consume.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费卡解锁表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Schema(description = "消费卡解锁表单")
public class ConsumeCardUnlockForm {

    @NotNull(message = "卡ID不能为空")
    @Schema(description = "卡ID", example = "1001")
    private Long cardId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "解锁原因", example = "找到丢失的卡片")
    private String unlockReason;

    @NotBlank(message = "卡号不能为空")
    @Schema(description = "消费卡号", example = "CARD20231201001")
    private String cardNo;

    @NotBlank(message = "验证方式不能为空")
    @Schema(description = "验证方式", example = "SMS", allowableValues = {"SMS", "PASSWORD", "BIOMETRIC"})
    private String verifyMethod;

    @Schema(description = "验证码", example = "123456")
    private String verifyCode;

    @Schema(description = "密码", example = "password123")
    private String password;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;
}
