package net.lab1024.sa.attendance.engine.rule.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则验证结果模型
 * <p>
 * 规则验证的详细结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleValidationResult {

    /**
     * 是否验证通过
     */
    private Boolean valid;

    /**
     * 验证错误码
     */
    private String errorCode;

    /**
     * 验证错误消息
     */
    private String errorMessage;

    /**
     * 验证警告信息列表
     */
    private List<String> warningMessages;

    /**
     * 验证类型
     */
    private String validationType;

    /**
     * 验证时间戳
     */
    private LocalDateTime validationTime;

    /**
     * 验证耗时（毫秒）
     */
    private Long validationDuration;

    /**
     * 验证详情
     */
    private Map<String, Object> validationDetails;

    /**
     * 严重级别：INFO-信息 WARN-警告 ERROR-错误 CRITICAL-严重
     */
    private String severity;

    /**
     * 修复建议
     */
    private List<String> fixSuggestions;

    /**
     * 验证器名称
     */
    private String validatorName;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 验证步骤
     */
    private List<ValidationStep> validationSteps;

    /**
     * 验证步骤内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationStep {

        /**
         * 步骤名称
         */
        private String stepName;

        /**
         * 步骤描述
         */
        private String stepDescription;

        /**
         * 是否通过
         */
        private Boolean passed;

        /**
         * 步骤错误消息
         */
        private String errorMessage;

        /**
         * 步骤耗时（毫秒）
         */
        private Long stepDuration;

        /**
         * 步骤详情
         */
        private Map<String, Object> stepDetails;
    }

    /**
     * 创建成功的验证结果
     */
    public static RuleValidationResult success() {
        return RuleValidationResult.builder()
                .valid(true)
                .validationTime(LocalDateTime.now())
                .severity("INFO")
                .build();
    }

    /**
     * 创建成功的验证结果（带详情）
     */
    public static RuleValidationResult success(String validationType, Map<String, Object> details) {
        return RuleValidationResult.builder()
                .valid(true)
                .validationType(validationType)
                .validationDetails(details)
                .validationTime(LocalDateTime.now())
                .severity("INFO")
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static RuleValidationResult failure(String errorCode, String errorMessage) {
        return RuleValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .validationTime(LocalDateTime.now())
                .severity("ERROR")
                .build();
    }

    /**
     * 创建失败的验证结果（带警告）
     */
    public static RuleValidationResult failure(String errorCode, String errorMessage, List<String> warnings) {
        return RuleValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .warningMessages(warnings)
                .validationTime(LocalDateTime.now())
                .severity("ERROR")
                .build();
    }

    /**
     * 创建带警告的验证结果
     */
    public static RuleValidationResult warning(String errorMessage, List<String> warnings) {
        return RuleValidationResult.builder()
                .valid(true)
                .errorMessage(errorMessage)
                .warningMessages(warnings)
                .validationTime(LocalDateTime.now())
                .severity("WARN")
                .build();
    }

    /**
     * 添加警告信息
     */
    public void addWarning(String warning) {
        if (this.warningMessages == null) {
            this.warningMessages = new java.util.ArrayList<>();
        }
        this.warningMessages.add(warning);

        // 如果有警告，严重级别至少是WARN
        if ("INFO".equals(this.severity)) {
            this.severity = "WARN";
        }
    }

    /**
     * 添加修复建议
     */
    public void addFixSuggestion(String suggestion) {
        if (this.fixSuggestions == null) {
            this.fixSuggestions = new java.util.ArrayList<>();
        }
        this.fixSuggestions.add(suggestion);
    }

    /**
     * 添加验证步骤
     */
    public void addValidationStep(ValidationStep step) {
        if (this.validationSteps == null) {
            this.validationSteps = new java.util.ArrayList<>();
        }
        this.validationSteps.add(step);
    }

    /**
     * 设置验证详情
     */
    public void setValidationDetail(String key, Object value) {
        if (this.validationDetails == null) {
            this.validationDetails = new java.util.HashMap<>();
        }
        this.validationDetails.put(key, value);
    }

    /**
     * 检查是否验证通过
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }
}