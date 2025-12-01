/*
 * 密码安全策略VO（兼容性类）
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * 密码安全策略VO（兼容性类）
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * 注意：此类仅为向后兼容保留，新代码应使用 net.lab1024.sa.admin.module.consume.domain.policy.PasswordSecurityPolicy
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Deprecated
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "密码安全策略（兼容性类）")
public class PasswordSecurityPolicy {

    /**
     * 密码最小长度
     */
    @Schema(description = "密码最小长度", example = "6")
    @NotNull(message = "密码最小长度不能为空")
    @Min(value = 4, message = "密码最小长度不能少于4位")
    @Max(value = 20, message = "密码最小长度不能超过20位")
    private Integer minLength;

    /**
     * 密码最大长度
     */
    @Schema(description = "密码最大长度", example = "20")
    @Min(value = 6, message = "密码最大长度不能少于6位")
    @Max(value = 50, message = "密码最大长度不能超过50位")
    private Integer maxLength;

    /**
     * 是否要求复杂度（包含字母和数字）
     */
    @Schema(description = "是否要求复杂度", example = "true")
    @NotNull(message = "复杂度要求不能为空")
    private Boolean requireComplex;

    /**
     * 是否要求包含特殊字符
     */
    @Schema(description = "是否要求包含特殊字符", example = "false")
    private Boolean requireSpecialChar;

    /**
     * 是否要求包含大写字母
     */
    @Schema(description = "是否要求包含大写字母", example = "false")
    private Boolean requireUpperCase;

    /**
     * 是否要求包含小写字母
     */
    @Schema(description = "是否要求包含小写字母", example = "false")
    private Boolean requireLowerCase;

    /**
     * 最大尝试次数
     */
    @Schema(description = "最大尝试次数", example = "5")
    @NotNull(message = "最大尝试次数不能为空")
    @Min(value = 1, message = "最大尝试次数不能少于1次")
    @Max(value = 20, message = "最大尝试次数不能超过20次")
    private Integer maxAttempts;

    /**
     * 锁定时长（分钟）
     */
    @Schema(description = "锁定时长（分钟）", example = "30")
    @NotNull(message = "锁定时长不能为空")
    @Min(value = 1, message = "锁定时长不能少于1分钟")
    @Max(value = 1440, message = "锁定时长不能超过1440分钟（24小时）")
    private Integer lockDurationMinutes;

    /**
     * 是否允许常见弱密码
     */
    @Schema(description = "是否允许常见弱密码", example = "false")
    @NotNull(message = "弱密码允许策略不能为空")
    private Boolean allowWeakPassword;

    /**
     * 密码有效期（天）
     */
    @Schema(description = "密码有效期（天）", example = "90")
    @Min(value = 1, message = "密码有效期不能少于1天")
    @Max(value = 365, message = "密码有效期不能超过365天")
    private Integer passwordExpiryDays;

    /**
     * 是否要求密码历史（不能重复使用最近的密码）
     */
    @Schema(description = "是否要求密码历史", example = "false")
    private Boolean requirePasswordHistory;

    /**
     * 密码历史记录数量
     */
    @Schema(description = "密码历史记录数量", example = "5")
    @Min(value = 1, message = "密码历史记录数量不能少于1个")
    @Max(value = 20, message = "密码历史记录数量不能超过20个")
    private Integer passwordHistoryCount;

    /**
     * 是否支持生物特征验证
     */
    @Schema(description = "是否支持生物特征验证", example = "true")
    private Boolean supportBiometric;

    /**
     * 允许的生物特征类型
     */
    @Schema(description = "允许的生物特征类型", example = "FINGERPRINT,FACE,IRIS")
    private String allowedBiometricTypes;

    /**
     * 是否开启双因子认证
     */
    @Schema(description = "是否开启双因子认证", example = "false")
    private Boolean enableTwoFactorAuth;

    /**
     * 密码复杂度要求描述
     */
    @Schema(description = "密码复杂度要求描述", example = "密码长度不能少于6位，必须包含字母和数字")
    private String complexityDescription;

    /**
     * 获取密码复杂度要求描述
     */
    public String getComplexityDescription() {
        if (complexityDescription != null && !complexityDescription.trim().isEmpty()) {
            return complexityDescription;
        }

        StringBuilder description = new StringBuilder();
        description.append("密码长度");

        if (minLength != null && maxLength != null) {
            description.append("必须在").append(minLength).append("-").append(maxLength).append("位之间");
        } else if (minLength != null) {
            description.append("不能少于").append(minLength).append("位");
        } else if (maxLength != null) {
            description.append("不能超过").append(maxLength).append("位");
        }

        if (Boolean.TRUE.equals(requireComplex)) {
            description.append("，必须包含字母和数字");
        }

        if (Boolean.TRUE.equals(requireUpperCase)) {
            description.append("，必须包含大写字母");
        }

        if (Boolean.TRUE.equals(requireLowerCase)) {
            description.append("，必须包含小写字母");
        }

        if (Boolean.TRUE.equals(requireSpecialChar)) {
            description.append("，必须包含特殊字符");
        }

        if (Boolean.FALSE.equals(allowWeakPassword)) {
            description.append("，不能使用常见弱密码");
        }

        return description.toString();
    }

    /**
     * 检查密码长度是否符合要求
     */
    public boolean isValidLength(String password) {
        if (password == null) {
            return false;
        }

        int length = password.length();

        if (minLength != null && length < minLength) {
            return false;
        }

        if (maxLength != null && length > maxLength) {
            return false;
        }

        return true;
    }

    /**
     * 检查密码复杂度是否符合要求
     */
    public boolean isValidComplexity(String password) {
        if (password == null) {
            return false;
        }

        if (!Boolean.TRUE.equals(requireComplex)) {
            return true;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");

        if (Boolean.TRUE.equals(requireUpperCase)) {
            boolean hasUpperCase = password.matches(".*[A-Z].*");
            if (!hasUpperCase) return false;
        }

        if (Boolean.TRUE.equals(requireLowerCase)) {
            boolean hasLowerCase = password.matches(".*[a-z].*");
            if (!hasLowerCase) return false;
        }

        if (Boolean.TRUE.equals(requireSpecialChar)) {
            boolean hasSpecialChar = password.matches(".*[^a-zA-Z0-9].*");
            if (!hasSpecialChar) return false;
        }

        return hasLetter && hasDigit;
    }

    /**
     * 检查是否为弱密码
     */
    public boolean isWeakPassword(String password) {
        if (Boolean.TRUE.equals(allowWeakPassword)) {
            return false;
        }

        if (password == null) {
            return true;
        }

        String lowerPassword = password.toLowerCase();

        // 常见弱密码列表
        String[] weakPasswords = {
            "123456", "123456789", "password", "12345678", "12345",
            "1234567", "1234567890", "1234", "qwerty", "abc123",
            "111111", "000000", "123123", "admin", "letmein",
            "welcome", "monkey", "1234567890", "password123"
        };

        for (String weakPassword : weakPasswords) {
            if (weakPassword.equals(lowerPassword)) {
                return true;
            }
        }

        // 检查是否为重复字符
        if (lowerPassword.matches("(.)\\1{2,}")) {
            return true;
        }

        // 检查是否为连续数字或字母
        if (isSequential(lowerPassword)) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否为连续字符
     */
    private boolean isSequential(String password) {
        if (password.length() < 3) {
            return false;
        }

        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if (c2 == c1 + 1 && c3 == c2 + 1) {
                return true;
            }
            if (c2 == c1 - 1 && c3 == c2 - 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * 综合验证密码
     */
    public boolean isValidPassword(String password) {
        return isValidLength(password) &&
               isValidComplexity(password) &&
               !isWeakPassword(password);
    }

    /**
     * 获取默认安全策略
     */
    public static PasswordSecurityPolicy getDefault() {
        return PasswordSecurityPolicy.builder()
                .minLength(6)
                .maxLength(20)
                .requireComplex(true)
                .requireSpecialChar(false)
                .requireUpperCase(false)
                .requireLowerCase(false)
                .maxAttempts(5)
                .lockDurationMinutes(30)
                .allowWeakPassword(false)
                .passwordExpiryDays(90)
                .requirePasswordHistory(false)
                .passwordHistoryCount(5)
                .supportBiometric(true)
                .allowedBiometricTypes("FINGERPRINT,FACE")
                .enableTwoFactorAuth(false)
                .build();
    }

    /**
     * 获取高安全级别策略
     */
    public static PasswordSecurityPolicy getHighSecurity() {
        return PasswordSecurityPolicy.builder()
                .minLength(8)
                .maxLength(32)
                .requireComplex(true)
                .requireSpecialChar(true)
                .requireUpperCase(true)
                .requireLowerCase(true)
                .maxAttempts(3)
                .lockDurationMinutes(60)
                .allowWeakPassword(false)
                .passwordExpiryDays(60)
                .requirePasswordHistory(true)
                .passwordHistoryCount(10)
                .supportBiometric(true)
                .allowedBiometricTypes("FINGERPRINT,FACE,IRIS")
                .enableTwoFactorAuth(true)
                .build();
    }

    /**
     * 获取低安全级别策略
     */
    public static PasswordSecurityPolicy getLowSecurity() {
        return PasswordSecurityPolicy.builder()
                .minLength(4)
                .maxLength(16)
                .requireComplex(false)
                .requireSpecialChar(false)
                .requireUpperCase(false)
                .requireLowerCase(false)
                .maxAttempts(10)
                .lockDurationMinutes(15)
                .allowWeakPassword(true)
                .passwordExpiryDays(180)
                .requirePasswordHistory(false)
                .passwordHistoryCount(0)
                .supportBiometric(false)
                .allowedBiometricTypes("")
                .enableTwoFactorAuth(false)
                .build();
    }

    /**
     * 密码验证结果内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PasswordValidationResult {
        private boolean valid;
        private String errorMessage;
        private String errorType;
    }

    /**
     * 获取密码验证结果
     */
    public PasswordValidationResult validatePassword(String password) {
        PasswordValidationResult.PasswordValidationResultBuilder builder =
            PasswordValidationResult.builder().valid(false);

        if (password == null) {
            return builder
                .valid(false)
                .errorMessage("密码不能为空")
                .errorType("NULL_PASSWORD")
                .build();
        }

        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (!isValidLength(password)) {
            isValid = false;
            if (minLength != null && maxLength != null) {
                errorMessage.append("密码长度必须在").append(minLength).append("-").append(maxLength).append("位之间；");
            } else if (minLength != null) {
                errorMessage.append("密码长度不能少于").append(minLength).append("位；");
            } else if (maxLength != null) {
                errorMessage.append("密码长度不能超过").append(maxLength).append("位；");
            }
        }

        if (!isValidComplexity(password)) {
            isValid = false;
            errorMessage.append("密码复杂度不符合要求；");
        }

        if (isWeakPassword(password)) {
            isValid = false;
            errorMessage.append("密码过于简单，不能使用常见弱密码；");
        }

        return builder
            .valid(isValid)
            .errorMessage(errorMessage.toString())
            .errorType(isValid ? null : "INVALID_PASSWORD")
            .build();
    }
}