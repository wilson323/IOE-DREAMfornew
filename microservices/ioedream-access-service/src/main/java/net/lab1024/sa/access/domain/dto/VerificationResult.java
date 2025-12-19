package net.lab1024.sa.access.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证结果DTO
 * <p>
 * 统一验证结果返回格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证结果")
public class VerificationResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private boolean success;

    /**
     * 验证状态
     * SUCCEED - 验证成功
     * FAILED - 验证失败
     * TIMEOUT - 验证超时（多人验证等待中）
     */
    @Schema(description = "验证状态", example = "SUCCEED", allowableValues = {"SUCCEED", "FAILED", "TIMEOUT"})
    private String authStatus;

    /**
     * 错误码（验证失败时）
     */
    @Schema(description = "错误码", example = "ANTI_PASSBACK_VIOLATION")
    private String errorCode;

    /**
     * 提示消息
     */
    @Schema(description = "提示消息", example = "验证通过,欢迎进入")
    private String message;

    /**
     * 控制指令（验证成功时）
     * 格式：CONTROL DEVICE AABBCCDDEE
     */
    @Schema(description = "控制指令", example = "0101000300")
    private String controlCommand;

    /**
     * 验证模式
     * edge - 设备端验证
     * backend - 后台验证
     */
    @Schema(description = "验证模式", example = "backend")
    private String verificationMode;

    /**
     * 创建成功结果
     *
     * @param message 提示消息
     * @param controlCommand 控制指令
     * @param verificationMode 验证模式
     * @return 验证结果
     */
    public static VerificationResult success(String message, String controlCommand, String verificationMode) {
        return VerificationResult.builder()
                .success(true)
                .authStatus("SUCCEED")
                .message(message)
                .controlCommand(controlCommand)
                .verificationMode(verificationMode)
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误码
     * @param message 提示消息
     * @return 验证结果
     */
    public static VerificationResult failed(String errorCode, String message) {
        return VerificationResult.builder()
                .success(false)
                .authStatus("FAILED")
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 创建等待结果（多人验证等待中）
     *
     * @param errorCode 错误码
     * @param message 提示消息
     * @return 验证结果
     */
    public static VerificationResult waiting(String errorCode, String message) {
        return VerificationResult.builder()
                .success(false)
                .authStatus("TIMEOUT")
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}
