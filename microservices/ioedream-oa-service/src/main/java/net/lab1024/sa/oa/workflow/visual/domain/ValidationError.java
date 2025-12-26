package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 验证错误类
 * <p>
 * 封装工作流配置验证过程中的错误信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 错误字段
     */
    private String errorField;

    /**
     * 错误值
     */
    private Object errorValue;

    /**
     * 错误级别：ERROR-错误 WARN-警告 INFO-信息
     */
    private ErrorLevel errorLevel;

    /**
     * 错误类型
     * REQUIRED - 必填项缺失
     * FORMAT - 格式错误
     * VALUE - 值错误
     * REFERENCE - 引用错误
     * LOGIC - 逻辑错误
     * DUPLICATE - 重复错误
     */
    private ErrorType errorType;

    /**
     * 错误详情
     */
    private String errorDetail;

    /**
     * 修复建议
     */
    private String suggestion;

    /**
     * 是否可自动修复
     */
    private Boolean autoFixable;

    /**
     * 错误发生位置
     */
    private String errorLocation;

    /**
     * 错误发生时间
     */
    private LocalDateTime errorTime;

    /**
     * 扩展信息
     */
    private Object extendedInfo;

    /**
     * 错误级别枚举
     */
    public enum ErrorLevel {
        /**
         * 错误
         */
        ERROR,

        /**
         * 警告
         */
        WARN,

        /**
         * 信息
         */
        INFO
    }

    /**
     * 错误类型枚举
     */
    public enum ErrorType {
        /**
         * 必填项缺失
         */
        REQUIRED,

        /**
         * 格式错误
         */
        FORMAT,

        /**
         * 值错误
         */
        VALUE,

        /**
         * 引用错误
         */
        REFERENCE,

        /**
         * 逻辑错误
         */
        LOGIC,

        /**
         * 重复错误
         */
        DUPLICATE,

        /**
         * 约束错误
         */
        CONSTRAINT,

        /**
         * 权限错误
         */
        PERMISSION
    }

    /**
     * 创建必填项缺失错误
     */
    public static ValidationError requiredError(String field, String location) {
        return ValidationError.builder()
                .errorCode("REQUIRED_FIELD_MISSING")
                .errorMessage(String.format("必填字段 '%s' 不能为空", field))
                .errorField(field)
                .errorLevel(ErrorLevel.ERROR)
                .errorType(ErrorType.REQUIRED)
                .errorLocation(location)
                .errorTime(LocalDateTime.now())
                .suggestion(String.format("请为字段 '%s' 提供有效值", field))
                .build();
    }

    /**
     * 创建引用错误
     */
    public static ValidationError referenceError(String field, Object value, String location) {
        return ValidationError.builder()
                .errorCode("REFERENCE_NOT_FOUND")
                .errorMessage(String.format("字段 '%s' 引用的值 '%s' 不存在", field, value))
                .errorField(field)
                .errorValue(value)
                .errorLevel(ErrorLevel.ERROR)
                .errorType(ErrorType.REFERENCE)
                .errorLocation(location)
                .errorTime(LocalDateTime.now())
                .suggestion(String.format("请确保字段 '%s' 引用的值 '%s' 已定义", field, value))
                .build();
    }

    /**
     * 创建格式错误
     */
    public static ValidationError formatError(String field, Object value, String expectedFormat, String location) {
        return ValidationError.builder()
                .errorCode("INVALID_FORMAT")
                .errorMessage(String.format("字段 '%s' 的值 '%s' 格式不正确，期望格式: %s", field, value, expectedFormat))
                .errorField(field)
                .errorValue(value)
                .errorLevel(ErrorLevel.ERROR)
                .errorType(ErrorType.FORMAT)
                .errorLocation(location)
                .errorTime(LocalDateTime.now())
                .suggestion(String.format("请按照 '%s' 格式提供字段 '%s' 的值", expectedFormat, field))
                .build();
    }
}
