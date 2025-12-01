package net.lab1024.sa.admin.module.consume.service;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 密码安全策略
 *
 * 严格遵循repowiki规范:
 * - 统一的数据传输对象设计
 * - 完整的Swagger注解
 * - 标准化的Builder模式
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */

@Schema(description = "密码安全策略")
public class PasswordSecurityPolicy {

    @Schema(description = "最小密码长度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minLength;

    @Schema(description = "最大密码长度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxLength;

    @Schema(description = "是否要求数字", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requireNumbers;

    @Schema(description = "是否要求字母", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requireLetters;

    @Schema(description = "是否要求特殊字符", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requireSpecialChars;

    @Schema(description = "是否要求大小写混合", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean requireMixedCase;

    @Schema(description = "密码过期天数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer expiryDays;

    @Schema(description = "最大连续错误次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxFailedAttempts;

    @Schema(description = "账户锁定时间（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer lockoutDurationMinutes;

    @Schema(description = "特殊字符集合")
    private String specialCharacterSet;

    @Schema(description = "密码历史记录数量（防止重复使用最近的历史密码）")
    private Integer passwordHistoryCount;

    @Schema(description = "是否启用密码强度检查", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enableStrengthCheck;

    @Schema(description = "最低密码强度等级：1-弱，2-中，3-强", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer minStrengthLevel;

    // ========== Getter/Setter方法 ==========

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Boolean getRequireNumbers() {
        return requireNumbers;
    }

    public void setRequireNumbers(Boolean requireNumbers) {
        this.requireNumbers = requireNumbers;
    }

    public Boolean getRequireLetters() {
        return requireLetters;
    }

    public void setRequireLetters(Boolean requireLetters) {
        this.requireLetters = requireLetters;
    }

    public Boolean getRequireSpecialChars() {
        return requireSpecialChars;
    }

    public void setRequireSpecialChars(Boolean requireSpecialChars) {
        this.requireSpecialChars = requireSpecialChars;
    }

    public Boolean getRequireMixedCase() {
        return requireMixedCase;
    }

    public void setRequireMixedCase(Boolean requireMixedCase) {
        this.requireMixedCase = requireMixedCase;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }

    public Integer getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(Integer maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public Integer getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public void setLockoutDurationMinutes(Integer lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }

    public String getSpecialCharacterSet() {
        return specialCharacterSet;
    }

    public void setSpecialCharacterSet(String specialCharacterSet) {
        this.specialCharacterSet = specialCharacterSet;
    }

    public Integer getPasswordHistoryCount() {
        return passwordHistoryCount;
    }

    public void setPasswordHistoryCount(Integer passwordHistoryCount) {
        this.passwordHistoryCount = passwordHistoryCount;
    }

    public Boolean getEnableStrengthCheck() {
        return enableStrengthCheck;
    }

    public void setEnableStrengthCheck(Boolean enableStrengthCheck) {
        this.enableStrengthCheck = enableStrengthCheck;
    }

    public Integer getMinStrengthLevel() {
        return minStrengthLevel;
    }

    public void setMinStrengthLevel(Integer minStrengthLevel) {
        this.minStrengthLevel = minStrengthLevel;
    }

    // ========== Builder模式 ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PasswordSecurityPolicy policy = new PasswordSecurityPolicy();

        public Builder minLength(Integer minLength) {
            policy.minLength = minLength;
            return this;
        }

        public Builder maxLength(Integer maxLength) {
            policy.maxLength = maxLength;
            return this;
        }

        public Builder requireNumbers(Boolean requireNumbers) {
            policy.requireNumbers = requireNumbers;
            return this;
        }

        public Builder requireLetters(Boolean requireLetters) {
            policy.requireLetters = requireLetters;
            return this;
        }

        public Builder requireSpecialChars(Boolean requireSpecialChars) {
            policy.requireSpecialChars = requireSpecialChars;
            return this;
        }

        public Builder requireMixedCase(Boolean requireMixedCase) {
            policy.requireMixedCase = requireMixedCase;
            return this;
        }

        public Builder expiryDays(Integer expiryDays) {
            policy.expiryDays = expiryDays;
            return this;
        }

        public Builder maxFailedAttempts(Integer maxFailedAttempts) {
            policy.maxFailedAttempts = maxFailedAttempts;
            return this;
        }

        public Builder lockoutDurationMinutes(Integer lockoutDurationMinutes) {
            policy.lockoutDurationMinutes = lockoutDurationMinutes;
            return this;
        }

        public Builder specialCharacterSet(String specialCharacterSet) {
            policy.specialCharacterSet = specialCharacterSet;
            return this;
        }

        public Builder passwordHistoryCount(Integer passwordHistoryCount) {
            policy.passwordHistoryCount = passwordHistoryCount;
            return this;
        }

        public Builder enableStrengthCheck(Boolean enableStrengthCheck) {
            policy.enableStrengthCheck = enableStrengthCheck;
            return this;
        }

        public Builder minStrengthLevel(Integer minStrengthLevel) {
            policy.minStrengthLevel = minStrengthLevel;
            return this;
        }

        public PasswordSecurityPolicy build() {
            return policy;
        }
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建默认安全策略
     */
    public static PasswordSecurityPolicy createDefault() {
        return PasswordSecurityPolicy.builder()
                .minLength(6)
                .maxLength(20)
                .requireNumbers(true)
                .requireLetters(true)
                .requireSpecialChars(false)
                .requireMixedCase(false)
                .expiryDays(90)
                .maxFailedAttempts(5)
                .lockoutDurationMinutes(30)
                .specialCharacterSet("!@#$%^&*")
                .passwordHistoryCount(5)
                .enableStrengthCheck(true)
                .minStrengthLevel(2)
                .build();
    }

    /**
     * 创建高安全策略
     */
    public static PasswordSecurityPolicy createHighSecurity() {
        return PasswordSecurityPolicy.builder()
                .minLength(8)
                .maxLength(32)
                .requireNumbers(true)
                .requireLetters(true)
                .requireSpecialChars(true)
                .requireMixedCase(true)
                .expiryDays(60)
                .maxFailedAttempts(3)
                .lockoutDurationMinutes(60)
                .specialCharacterSet("!@#$%^&*()_+-=[]{}|;:,.<>?")
                .passwordHistoryCount(10)
                .enableStrengthCheck(true)
                .minStrengthLevel(3)
                .build();
    }
}
