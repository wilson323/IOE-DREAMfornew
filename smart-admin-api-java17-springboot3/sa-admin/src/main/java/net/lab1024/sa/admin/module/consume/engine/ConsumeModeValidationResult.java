package net.lab1024.sa.admin.module.consume.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 消费模式验证结果对象
 * 包含参数验证的结果和错误信息
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumeModeValidationResult {

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
    private List<String> warnings;

    /**
     * 验证后的参数
     */
    private Map<String, Object> validatedParams;

    /**
     * 扩展信息
     */
    private String extendInfo;

    /**
     * 创建验证成功结果
     */
    public static ConsumeModeValidationResult success() {
        return ConsumeModeValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * 创建验证成功结果（带参数）
     */
    public static ConsumeModeValidationResult success(Map<String, Object> validatedParams) {
        return ConsumeModeValidationResult.builder()
                .valid(true)
                .validatedParams(validatedParams)
                .build();
    }

    /**
     * 创建验证失败结果
     */
    public static ConsumeModeValidationResult failure(String errorCode, String errorMessage) {
        return ConsumeModeValidationResult.builder()
                .valid(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建带警告的验证成功结果
     */
    public static ConsumeModeValidationResult successWithWarnings(List<String> warnings) {
        return ConsumeModeValidationResult.builder()
                .valid(true)
                .warnings(warnings)
                .build();
    }

    /**
     * 添加警告
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new java.util.ArrayList<>();
        }
        this.warnings.add(warning);
    }

    /**
     * 设置验证参数
     */
    public void setValidatedParam(String key, Object value) {
        if (this.validatedParams == null) {
            this.validatedParams = new java.util.HashMap<>();
        }
        this.validatedParams.put(key, value);
    }

    /**
     * 获取验证参数
     */
    @SuppressWarnings("unchecked")
    public <T> T getValidatedParam(String key, Class<T> type) {
        if (validatedParams == null || !validatedParams.containsKey(key)) {
            return null;
        }

        Object value = validatedParams.get(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        throw new IllegalArgumentException("无法将验证参数 " + key + " 转换为类型 " + type.getSimpleName());
    }
}