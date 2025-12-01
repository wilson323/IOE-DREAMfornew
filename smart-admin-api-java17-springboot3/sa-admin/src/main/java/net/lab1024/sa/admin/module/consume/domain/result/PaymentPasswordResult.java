package net.lab1024.sa.admin.module.consume.domain.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付密码验证结果VO (Value Object)
 *
 * <p>支付密码验证、设置、重置等操作的结果数据传输对象。
 * 包含验证结果、错误信息、剩余尝试次数、安全建议等详细信息。</p>
 *
 * <p>严格遵循repowiki规范：</p>
 * <ul>
 *   <li>使用完整的验证注解确保数据完整性</li>
 *   <li>包含详细的Swagger文档注解</li>
 *   <li>使用JsonInclude忽略null值</li>
 *   <li>提供Builder模式支持灵活构造</li>
 *   <li>包含丰富的业务方法和安全策略</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @since 2025-11-28
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "支付密码验证结果VO")
public class PaymentPasswordResult {

    /**
     * 操作是否成功
     */
    @NotNull(message = "操作结果不能为空")
    @Schema(description = "操作是否成功", example = "true", required = true)
    private Boolean success;

    /**
     * 结果消息
     */
    @NotBlank(message = "结果消息不能为空")
    @Size(max = 200, message = "结果消息长度不能超过200个字符")
    @Schema(description = "结果消息", example = "支付密码验证成功", required = true)
    private String message;

    /**
     * 结果代码
     *
     * <p>SUCCESS-成功, INVALID_PASSWORD-密码错误, PASSWORD_EXPIRED-密码过期,
     * ACCOUNT_LOCKED-账户锁定, TOO_MANY_ATTEMPTS-尝试次数过多</p>
     */
    @NotBlank(message = "结果代码不能为空")
    @Pattern(regexp = "^(SUCCESS|INVALID_PASSWORD|PASSWORD_EXPIRED|ACCOUNT_LOCKED|TOO_MANY_ATTEMPTS|" +
                   "PASSWORD_NOT_SET|WEAK_PASSWORD|FORMAT_ERROR|SYSTEM_ERROR)$",
             message = "结果代码无效")
    @Schema(description = "结果代码", example = "SUCCESS", required = true)
    private String resultCode;

    /**
     * 当前尝试次数
     */
    @Min(value = 0, message = "尝试次数不能为负数")
    @Schema(description = "当前尝试次数", example = "2")
    private Integer attempts;

    /**
     * 剩余尝试次数
     */
    @Min(value = 0, message = "剩余尝试次数不能为负数")
    @Schema(description = "剩余尝试次数", example = "3")
    private Integer remainingAttempts;

    /**
     * 最大尝试次数
     */
    @Min(value = 1, message = "最大尝试次数必须大于0")
    @Schema(description = "最大尝试次数", example = "5")
    private Integer maxAttempts;

    /**
     * 账户是否被锁定
     */
    @Schema(description = "账户是否被锁定", example = "false")
    private Boolean accountLocked;

    /**
     * 锁定时间（分钟）
     */
    @Min(value = 0, message = "锁定时间不能为负数")
    @Schema(description = "锁定时间（分钟）", example = "30")
    private Integer lockTime;

    /**
     * 锁定截止时间
     */
    @Schema(description = "锁定截止时间", example = "2025-11-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lockExpireTime;

    /**
     * 上次验证时间
     */
    @Schema(description = "上次验证时间", example = "2025-11-28 10:25:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastVerifyTime;

    /**
     * 操作时间
     */
    @Schema(description = "操作时间", example = "2025-11-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "10086")
    private Long userId;

    /**
     * 操作类型
     *
     * <p>VERIFY-验证, SET-设置, RESET-重置, CHANGE-修改, CHECK-检查</p>
     */
    @Pattern(regexp = "^(VERIFY|SET|RESET|CHANGE|CHECK)$", message = "操作类型无效")
    @Schema(description = "操作类型", example = "VERIFY", allowableValues = {"VERIFY", "SET", "RESET", "CHANGE", "CHECK"})
    private String operationType;

    /**
     * 密码强度评分
     */
    @Min(value = 0, message = "密码强度评分不能为负数")
    @Max(value = 100, message = "密码强度评分不能超过100")
    @Schema(description = "密码强度评分", example = "85")
    private Integer passwordStrength;

    /**
     * 密码安全级别
     *
     * <p>WEAK-弱, MEDIUM-中等, STRONG-强, VERY_STRONG-很强</p>
     */
    @Pattern(regexp = "^(WEAK|MEDIUM|STRONG|VERY_STRONG)$", message = "密码安全级别无效")
    @Schema(description = "密码安全级别", example = "STRONG", allowableValues = {"WEAK", "MEDIUM", "STRONG", "VERY_STRONG"})
    private String securityLevel;

    /**
     * 安全建议列表
     */
    @Schema(description = "安全建议列表")
    private List<String> securitySuggestions;

    /**
     * 错误详情
     */
    @Schema(description = "错误详情")
    private ErrorDetail errorDetail;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private Map<String, Object> extensions;

    /**
     * 客户端IP地址
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
             message = "IP地址格式不正确")
    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    /**
     * 设备标识
     */
    @Size(max = 100, message = "设备标识长度不能超过100个字符")
    @Schema(description = "设备标识", example = "iPhone 13 Pro")
    private String deviceId;

    /**
     * 会话ID
     */
    @Size(max = 100, message = "会话ID长度不能超过100个字符")
    @Schema(description = "会话ID", example = "sess_abc123def456")
    private String sessionId;

    // ========== 兼容旧版本字段 ==========

    @Schema(description = "操作IP地址")
    private String ipAddress;

    @Schema(description = "操作设备标识")
    private String deviceLegacyId;  // 重命名以避免与新字段重复

    @Schema(description = "扩展数据")
    private Object extendData;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "验证方式")
    private String verifyMethod;

    /**
     * 错误详情内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "错误详情")
    public static class ErrorDetail {

        /**
         * 错误代码
         */
        @Schema(description = "错误代码", example = "PASSWORD_EXPIRED")
        private String errorCode;

        /**
         * 错误消息
         */
        @Schema(description = "错误消息", example = "支付密码已过期，请重新设置")
        private String errorMessage;

        /**
         * 错误类型
         *
         * <p>VALIDATION_ERROR-验证错误, BUSINESS_ERROR-业务错误, SYSTEM_ERROR-系统错误</p>
         */
        @Schema(description = "错误类型", example = "BUSINESS_ERROR", allowableValues = {"VALIDATION_ERROR", "BUSINESS_ERROR", "SYSTEM_ERROR"})
        private String errorType;

        /**
         * 堆栈信息（开发环境使用）
         */
        @Schema(description = "堆栈信息", example = "java.lang.IllegalArgumentException: ...")
        private String stackTrace;

        /**
         * 发生时间
         */
        @Schema(description = "发生时间", example = "2025-11-28 10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime errorTime;

        /**
         * 请求参数
         */
        @Schema(description = "请求参数")
        private Map<String, Object> requestParams;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断操作是否成功
     *
     * @return true如果操作成功
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * 判断是否为密码错误
     *
     * @return true如果是密码错误
     */
    public boolean isPasswordError() {
        return "INVALID_PASSWORD".equals(resultCode);
    }

    /**
     * 判断账户是否被锁定
     *
     * @return true如果账户被锁定
     */
    public boolean isAccountLocked() {
        return Boolean.TRUE.equals(accountLocked) || "ACCOUNT_LOCKED".equals(resultCode);
    }

    /**
     * 判断是否达到最大尝试次数
     *
     * @return true如果达到最大尝试次数
     */
    public boolean isMaxAttemptsReached() {
        return maxAttempts != null && attempts != null && attempts >= maxAttempts;
    }

    /**
     * 判断是否需要等待解锁
     *
     * @return true如果需要等待解锁
     */
    public boolean needsWaitForUnlock() {
        return isAccountLocked() && lockExpireTime != null &&
               lockExpireTime.isAfter(LocalDateTime.now());
    }

    /**
     * 获取剩余锁定时间（分钟）
     *
     * @return 剩余锁定时间，如果没有锁定则返回0
     */
    public long getRemainingLockMinutes() {
        if (!needsWaitForUnlock()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), lockExpireTime).toMinutes();
    }

    /**
     * 判断密码强度是否足够
     *
     * @return true如果密码强度足够
     */
    public boolean isPasswordStrongEnough() {
        return passwordStrength != null && passwordStrength >= 60;
    }

    /**
     * 获取密码强度等级描述
     *
     * @return 密码强度等级描述
     */
    public String getStrengthDescription() {
        if (passwordStrength == null) {
            return "未知";
        }
        if (passwordStrength < 30) {
            return "弱";
        } else if (passwordStrength < 60) {
            return "中等";
        } else if (passwordStrength < 80) {
            return "强";
        } else {
            return "很强";
        }
    }

    /**
     * 获取操作类型描述
     *
     * @return 操作类型描述
     */
    public String getOperationTypeDescription() {
        switch (operationType) {
            case "VERIFY":
                return "验证支付密码";
            case "SET":
                return "设置支付密码";
            case "RESET":
                return "重置支付密码";
            case "CHANGE":
                return "修改支付密码";
            case "CHECK":
                return "检查支付密码";
            default:
                return "未知操作";
        }
    }

    /**
     * 获取下一步操作建议
     *
     * @return 操作建议
     */
    public String getNextStepSuggestion() {
        if (isSuccess()) {
            return "操作成功，可以继续进行支付相关操作";
        } else if (isAccountLocked()) {
            return "账户已锁定，请等待解锁或联系客服";
        } else if (isPasswordError()) {
            return remainingAttempts != null && remainingAttempts > 0 ?
                   "密码错误，请重试，剩余尝试次数：" + remainingAttempts :
                   "密码错误，请通过其他方式找回密码";
        } else if ("PASSWORD_NOT_SET".equals(resultCode)) {
            return "尚未设置支付密码，请先设置密码";
        } else if ("PASSWORD_EXPIRED".equals(resultCode)) {
            return "支付密码已过期，请重新设置";
        } else {
            return "操作失败，请稍后重试或联系客服";
        }
    }

    /**
     * 验证结果数据的完整性
     *
     * @return 验证结果
     */
    public boolean isValid() {
        if (success == null || message == null || resultCode == null) {
            return false;
        }
        if (attempts != null && attempts < 0) {
            return false;
        }
        if (remainingAttempts != null && remainingAttempts < 0) {
            return false;
        }
        if (maxAttempts != null && maxAttempts <= 0) {
            return false;
        }
        return true;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建成功结果
     *
     * @param message 成功消息
     * @return 成功结果对象
     */
    public static PaymentPasswordResult success(String message) {
        return PaymentPasswordResult.builder()
                .success(true)
                .message(message)
                .resultCode("SUCCESS")
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建密码错误结果
     *
     * @param attempts 当前尝试次数
     * @param remainingAttempts 剩余尝试次数
     * @param maxAttempts 最大尝试次数
     * @return 密码错误结果对象
     */
    public static PaymentPasswordResult passwordError(int attempts, int remainingAttempts, int maxAttempts) {
        return PaymentPasswordResult.builder()
                .success(false)
                .message("支付密码错误")
                .resultCode("INVALID_PASSWORD")
                .attempts(attempts)
                .remainingAttempts(remainingAttempts)
                .maxAttempts(maxAttempts)
                .accountLocked(remainingAttempts <= 0)
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建账户锁定结果
     *
     * @param lockTime 锁定时间（分钟）
     * @param maxAttempts 最大尝试次数
     * @return 账户锁定结果对象
     */
    public static PaymentPasswordResult accountLocked(int lockTime, int maxAttempts) {
        return PaymentPasswordResult.builder()
                .success(false)
                .message("账户已锁定")
                .resultCode("ACCOUNT_LOCKED")
                .attempts(maxAttempts)
                .remainingAttempts(0)
                .maxAttempts(maxAttempts)
                .accountLocked(true)
                .lockTime(lockTime)
                .lockExpireTime(LocalDateTime.now().plusMinutes(lockTime))
                .operationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建系统错误结果
     *
     * @param errorMessage 错误消息
     * @return 系统错误结果对象
     */
    public static PaymentPasswordResult systemError(String errorMessage) {
        return PaymentPasswordResult.builder()
                .success(false)
                .message(errorMessage)
                .resultCode("SYSTEM_ERROR")
                .operationTime(LocalDateTime.now())
                .build();
    }
}
