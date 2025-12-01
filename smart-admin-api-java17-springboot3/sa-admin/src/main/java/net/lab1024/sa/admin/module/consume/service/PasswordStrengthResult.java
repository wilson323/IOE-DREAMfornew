package net.lab1024.sa.admin.module.consume.service;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 密码强度检查结果
 *
 * 严格遵循repowiki规范:
 * - 统一的结果类设计模式
 * - 完整的Swagger注解
 * - 标准化的Builder模式
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */




@Schema(description = "密码强度检查结果")
public class PasswordStrengthResult {

    @Schema(description = "密码强度等级：1-弱，2-中，3-强", required = true)
    private Integer strengthLevel;

    @Schema(description = "强度评分（0-100分）", required = true)
    private Integer score;

    @Schema(description = "强度描述", required = true)
    private String strengthDescription;

    @Schema(description = "是否满足最低要求", required = true)
    private boolean meetsMinimumRequirements;

    @Schema(description = "密码长度检查是否通过", required = true)
    private boolean lengthValid;

    @Schema(description = "包含数字检查是否通过", required = true)
    private boolean containsNumbers;

    @Schema(description = "包含字母检查是否通过", required = true)
    private boolean containsLetters;

    @Schema(description = "包含特殊字符检查是否通过", required = true)
    private boolean containsSpecialChars;

    @Schema(description = "大小写混合检查是否通过", required = true)
    private boolean containsMixedCase;

    @Schema(description = "安全问题列表")
    private List<String> securityIssues;

    @Schema(description = "改进建议列表")
    private List<String> suggestions;

    @Schema(description = "检查时间", required = true)
    private LocalDateTime checkTime;

    @Schema(description = "密码模式（如：纯数字、纯字母、字母数字混合等）")
    private String passwordPattern;

    @Schema(description = "是否为常见弱密码", required = true)
    private boolean isCommonPassword;

    // ========== 静态工厂方法 ==========

    /**
     * 创建强密码结果
     */
    public static PasswordStrengthResult strong(Integer score, String description) {
        return PasswordStrengthResult.builder()
                .strengthLevel(3)
                .score(score)
                .strengthDescription(description)
                .meetsMinimumRequirements(true)
                .lengthValid(true)
                .containsNumbers(true)
                .containsLetters(true)
                .containsSpecialChars(true)
                .containsMixedCase(true)
                .isCommonPassword(false)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建中等密码结果
     */
    public static PasswordStrengthResult medium(Integer score, String description) {
        return PasswordStrengthResult.builder()
                .strengthLevel(2)
                .score(score)
                .strengthDescription(description)
                .meetsMinimumRequirements(true)
                .lengthValid(true)
                .containsNumbers(true)
                .containsLetters(true)
                .containsSpecialChars(false)
                .containsMixedCase(false)
                .isCommonPassword(false)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建弱密码结果
     */
    public static PasswordStrengthResult weak(Integer score, String description, List<String> issues) {
        return PasswordStrengthResult.builder()
                .strengthLevel(1)
                .score(score)
                .strengthDescription(description)
                .meetsMinimumRequirements(false)
                .securityIssues(issues)
                .isCommonPassword(true)
                .checkTime(LocalDateTime.now())
                .build();
    }
}