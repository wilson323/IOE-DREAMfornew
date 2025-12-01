package net.lab1024.sa.admin.module.consume.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户验证结果
 * 用于消费前账户状态和余额的全面验证
 * 严格遵循repowiki规范，包含详细的验证信息和错误提示
 *
 * @author IOE-DREAM Team
 * @date 2025-11-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResult {

    /**
     * 验证是否通过
     */
    private boolean valid;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前余额
     */
    private BigDecimal currentBalance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 请求消费金额
     */
    private BigDecimal requestAmount;

    /**
     * 账户状态
     */
    private String accountStatus;

    /**
     * 账户状态描述
     */
    private String statusDescription;

    /**
     * 验证错误代码
     */
    private String errorCode;

    /**
     * 验证错误消息
     */
    private String errorMessage;

    /**
     * 验证失败类型
     */
    private ValidationFailureType failureType;

    /**
     * 警告信息列表
     */
    private List<String> warnings;

    /**
     * 扩展信息
     */
    private Map<String, Object> extraInfo;

    /**
     * 验证时间戳
     */
    private Long timestamp;

    /**
     * 验证失败类型枚举
     */
    public enum ValidationFailureType {
        /**
         * 账户不存在
         */
        ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "账户不存在"),

        /**
         * 账户已冻结
         */
        ACCOUNT_FROZEN("ACCOUNT_FROZEN", "账户已冻结"),

        /**
         * 账户已关闭
         */
        ACCOUNT_CLOSED("ACCOUNT_CLOSED", "账户已关闭"),

        /**
         * 余额不足
         */
        INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "余额不足"),

        /**
         * 超出日限额
         */
        DAILY_LIMIT_EXCEEDED("DAILY_LIMIT_EXCEEDED", "超出日消费限额"),

        /**
         * 超出月限额
         */
        MONTHLY_LIMIT_EXCEEDED("MONTHLY_LIMIT_EXCEEDED", "超出月消费限额"),

        /**
         * 信用额度不足
         */
        CREDIT_LIMIT_EXCEEDED("CREDIT_LIMIT_EXCEEDED", "超出信用额度"),

        /**
         * 账户状态异常
         */
        INVALID_ACCOUNT_STATUS("INVALID_ACCOUNT_STATUS", "账户状态异常");

        private final String code;
        private final String description;

        ValidationFailureType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 创建成功验证结果
     */
    public static AccountValidationResult success(Long accountId, Long userId, BigDecimal availableBalance) {
        return AccountValidationResult.builder()
                .valid(true)
                .accountId(accountId)
                .userId(userId)
                .availableBalance(availableBalance)
                .accountStatus("ACTIVE")
                .statusDescription("账户正常")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建失败验证结果
     */
    public static AccountValidationResult failure(Long accountId, ValidationFailureType failureType, String errorMessage) {
        return AccountValidationResult.builder()
                .valid(false)
                .accountId(accountId)
                .failureType(failureType)
                .errorCode(failureType.getCode())
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new java.util.ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * 添加扩展信息
     */
    public void addExtraInfo(String key, Object value) {
        if (extraInfo == null) {
            extraInfo = new java.util.HashMap<>();
        }
        extraInfo.put(key, value);
    }

    /**
     * 是否余额充足
     */
    public boolean isBalanceSufficient() {
        return availableBalance != null &&
               requestAmount != null &&
               availableBalance.compareTo(requestAmount) >= 0;
    }

    /**
     * 是否有警告信息
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}