package net.lab1024.sa.admin.module.attendance.domain.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 报表配置验证结果
 *
 * <p>
 * 考勤模块报表配置验证的返回结果，包含验证结果和错误信息
 * 严格遵循repowiki编码规范：使用Lombok注解、标准POJO设计
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportConfigValidationResult {

    /**
     * 验证是否通过
     */
    private Boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 错误信息列表
     */
    private List<String> errors;

    /**
     * 警告信息列表
     */
    private List<String> warnings;

    /**
     * 创建验证通过结果
     *
     * @return 验证通过结果
     */
    public static ReportConfigValidationResult success() {
        ReportConfigValidationResult result = new ReportConfigValidationResult();
        result.setValid(true);
        result.setMessage("配置验证通过");
        return result;
    }

    /**
     * 创建验证通过结果
     *
     * @param warnings 警告信息
     * @return 验证通过结果
     */
    public static ReportConfigValidationResult success(List<String> warnings) {
        ReportConfigValidationResult result = success();
        result.setWarnings(warnings);
        return result;
    }

    /**
     * 创建验证失败结果
     *
     * @param message 失败消息
     * @param errors 错误信息列表
     * @return 验证失败结果
     */
    public static ReportConfigValidationResult failure(String message, List<String> errors) {
        ReportConfigValidationResult result = new ReportConfigValidationResult();
        result.setValid(false);
        result.setMessage(message);
        result.setErrors(errors);
        return result;
    }

    /**
     * 创建验证失败结果
     *
     * @param message 失败消息
     * @return 验证失败结果
     */
    public static ReportConfigValidationResult failure(String message) {
        return failure(message, null);
    }

    /**
     * 检查验证是否通过
     *
     * @return 是否通过
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }

    /**
     * 检查是否有错误
     *
     * @return 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 检查是否有警告
     *
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}