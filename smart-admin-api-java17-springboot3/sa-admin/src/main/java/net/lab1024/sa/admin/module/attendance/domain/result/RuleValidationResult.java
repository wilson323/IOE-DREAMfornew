package net.lab1024.sa.admin.module.attendance.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则验证结果
 *
 * 严格遵循repowiki规范:
 * - 封装规则验证的结果信息
 * - 提供成功/失败状态标识
 * - 包含验证错误信息和警告
 * - 支持扩展属性和元数据
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RuleValidationResult {

    /**
     * 验证是否成功
     */
    private Boolean success;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 警告信息列表
     */
    private List<String> warnings;

    /**
     * 验证时间
     */
    private LocalDateTime validationTime;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 验证类型
     */
    private String validationType;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static RuleValidationResult success() {
        return RuleValidationResult.builder()
                .success(true)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功结果（带规则信息）
     *
     * @param ruleId 规则ID
     * @param ruleName 规则名称
     * @return 成功结果
     */
    public static RuleValidationResult success(Long ruleId, String ruleName) {
        return RuleValidationResult.builder()
                .success(true)
                .ruleId(ruleId)
                .ruleName(ruleName)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static RuleValidationResult failure(String errorCode, String errorMessage) {
        return RuleValidationResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果（带警告）
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误信息
     * @param warnings 警告列表
     * @return 失败结果
     */
    public static RuleValidationResult failure(String errorCode, String errorMessage, List<String> warnings) {
        return RuleValidationResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .warnings(warnings)
                .validationTime(LocalDateTime.now())
                .build();
    }

    /**
     * 验证是否有效
     * 兼容性方法，返回验证结果
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return success != null && success;
    }

    /**
     * 检查是否有警告
     *
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 添加警告
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new java.util.ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * 添加扩展属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void addExtendedProperty(String key, Object value) {
        if (extendedProperties == null) {
            extendedProperties = new java.util.HashMap<>();
        }
        extendedProperties.put(key, value);
    }

    /**
     * 获取扩展属性
     *
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedProperty(String key, T defaultValue) {
        if (extendedProperties != null && extendedProperties.containsKey(key)) {
            return (T) extendedProperties.get(key);
        }
        return defaultValue;
    }

    /**
     * 获取错误摘要
     *
     * @return 错误摘要
     */
    public String getErrorSummary() {
        StringBuilder summary = new StringBuilder();

        if (!isValid()) {
            summary.append("验证失败");
            if (errorCode != null) {
                summary.append("[").append(errorCode).append("]");
            }
            if (errorMessage != null) {
                summary.append(": ").append(errorMessage);
            }
        }

        if (hasWarnings()) {
            if (summary.length() > 0) {
                summary.append(" ");
            }
            summary.append("警告: ").append(String.join(", ", warnings));
        }

        return summary.toString();
    }
}