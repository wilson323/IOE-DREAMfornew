package net.lab1024.sa.base.common.device.domain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 报表配置验证结果类
 * 用于封装报表配置验证的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportConfigValidationResult {

    /**
     * 验证是否通过
     */
    @Builder.Default
    private boolean valid = false;

    /**
     * 验证错误信息列表
     */
    @Builder.Default
    private List<String> errors = List.of();

    /**
     * 验证警告信息列表
     */
    @Builder.Default
    private List<String> warnings = List.of();

    /**
     * 验证信息详情
     */
    @Builder.Default
    private Map<String, String> details = Map.of();

    /**
     * 配置数据源是否有效
     */
    @Builder.Default
    private boolean dataSourceValid = false;

    /**
     * 配置字段是否有效
     */
    @Builder.Default
    private boolean fieldsValid = false;

    /**
     * 配置过滤条件是否有效
     */
    @Builder.Default
    private boolean filtersValid = false;

    /**
     * 配置分组是否有效
     */
    @Builder.Default
    private boolean groupingValid = false;

    /**
     * 验证耗时（毫秒）
     */
    @Builder.Default
    private long validationTime = 0L;

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static ReportConfigValidationResult failure(String errorMessage) {
        return ReportConfigValidationResult.builder()
                .valid(false)
                .errors(List.of(errorMessage))
                .build();
    }

    /**
     * 创建失败结果（多错误）
     *
     * @param errors 错误信息列表
     * @return 失败结果
     */
    public static ReportConfigValidationResult failure(List<String> errors) {
        return ReportConfigValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static ReportConfigValidationResult success() {
        return ReportConfigValidationResult.builder()
                .valid(true)
                .dataSourceValid(true)
                .fieldsValid(true)
                .filtersValid(true)
                .groupingValid(true)
                .build();
    }

    /**
     * 创建成功结果（带警告）
     *
     * @param warnings 警告信息列表
     * @return 成功结果
     */
    public static ReportConfigValidationResult success(List<String> warnings) {
        return ReportConfigValidationResult.builder()
                .valid(true)
                .warnings(warnings)
                .dataSourceValid(true)
                .fieldsValid(true)
                .filtersValid(true)
                .groupingValid(true)
                .build();
    }

    /**
     * 创建成功结果（完整验证信息）
     *
     * @param dataSourceValid 数据源有效性
     * @param fieldsValid 字段有效性
     * @param filtersValid 过滤条件有效性
     * @param groupingValid 分组有效性
     * @param warnings 警告信息
     * @param details 验证详情
     * @return 成功结果
     */
    public static ReportConfigValidationResult success(boolean dataSourceValid, boolean fieldsValid,
                                                     boolean filtersValid, boolean groupingValid,
                                                     List<String> warnings, Map<String, String> details) {
        return ReportConfigValidationResult.builder()
                .valid(dataSourceValid && fieldsValid && filtersValid && groupingValid)
                .dataSourceValid(dataSourceValid)
                .fieldsValid(fieldsValid)
                .filtersValid(filtersValid)
                .groupingValid(groupingValid)
                .warnings(warnings)
                .details(details)
                .build();
    }

    /**
     * 是否有任何错误
     *
     * @return 是否有错误
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 是否有任何警告
     *
     * @return 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 添加错误信息
     *
     * @param error 错误信息
     */
    public void addError(String error) {
        if (errors.isEmpty()) {
            errors = List.of(error);
        }
    }

    /**
     * 添加警告信息
     *
     * @param warning 警告信息
     */
    public void addWarning(String warning) {
        if (warnings.isEmpty()) {
            warnings = List.of(warning);
        }
    }

    /**
     * 获取验证结果摘要
     *
     * @return 结果摘要
     */
    public String getSummary() {
        if (valid) {
            if (hasWarnings()) {
                return String.format("验证通过，但有 %d 个警告", warnings.size());
            } else {
                return "验证完全通过";
            }
        } else {
            return String.format("验证失败，有 %d 个错误", errors.size());
        }
    }

    /**
     * 获取验证消息（兼容方法）
     * 返回第一个错误或验证摘要
     *
     * @return 验证消息
     */
    public String getMessage() {
        if (hasErrors()) {
            return errors.get(0);
        }
        return getSummary();
    }

    /**
     * 检查验证是否有效（兼容方法）
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return valid;
    }
}