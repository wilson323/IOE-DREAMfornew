package net.lab1024.sa.attendance.engine.rule.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 编译后的规则模型
 * <p>
 * 规则条件表达式编译后的可执行对象
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
public class CompiledRule {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则条件表达式
     */
    private String conditionExpression;

    /**
     * 编译后的条件对象
     */
    private Object compiledCondition;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则分类
     */
    private String ruleCategory;

    /**
     * 规则优先级
     */
    private Integer rulePriority;

    /**
     * 是否已编译
     */
    private Boolean compiled;

    /**
     * 编译时间
     */
    private LocalDateTime compileTime;

    /**
     * 编译耗时（毫秒）
     */
    private Long compileDuration;

    /**
     * 编译错误信息
     */
    private String compileError;

    /**
     * 编译警告信息
     */
    private java.util.List<String> compileWarnings;

    /**
     * 编译统计信息
     */
    private Map<String, Object> compileStatistics;

    /**
     * 编译器版本
     */
    private String compilerVersion;

    /**
     * 编译器名称
     */
    private String compilerName;

    /**
     * 优化级别
     */
    private String optimizationLevel;

    /**
     * 是否需要重新编译
     */
    private Boolean needsRecompile;

    /**
     * 规则参数定义
     */
    private Map<String, Object> parameterDefinitions;

    /**
     * 变量映射
     */
    private Map<String, String> variableMapping;

    /**
     * 创建成功的编译结果
     */
    public static CompiledRule success(Long ruleId, String conditionExpression, Object compiledCondition) {
        return CompiledRule.builder()
                .ruleId(ruleId)
                .conditionExpression(conditionExpression)
                .compiledCondition(compiledCondition)
                .compiled(true)
                .compileTime(LocalDateTime.now())
                .compilerName("DefaultRuleCompiler")
                .compilerVersion("1.0.0")
                .needsRecompile(false)
                .build();
    }

    /**
     * 创建失败的编译结果
     */
    public static CompiledRule failure(Long ruleId, String conditionExpression, String compileError) {
        return CompiledRule.builder()
                .ruleId(ruleId)
                .conditionExpression(conditionExpression)
                .compiled(false)
                .compileError(compileError)
                .compileTime(LocalDateTime.now())
                .compilerName("DefaultRuleCompiler")
                .compilerVersion("1.0.0")
                .needsRecompile(false)
                .build();
    }

    /**
     * 检查编译是否成功
     */
    public boolean isCompileSuccess() {
        return Boolean.TRUE.equals(compiled) && compiledCondition != null;
    }

    /**
     * 检查是否有编译错误
     */
    public boolean hasCompileError() {
        return !Boolean.TRUE.equals(compiled) && compileError != null;
    }

    /**
     * 添加编译警告
     */
    public void addCompileWarning(String warning) {
        if (this.compileWarnings == null) {
            this.compileWarnings = new java.util.ArrayList<>();
        }
        this.compileWarnings.add(warning);
    }

    /**
     * 设置编译统计信息
     */
    public void setCompileStatistic(String key, Object value) {
        if (this.compileStatistics == null) {
            this.compileStatistics = new java.util.HashMap<>();
        }
        this.compileStatistics.put(key, value);
    }

    /**
     * 设置参数定义
     */
    public void setParameterDefinition(String paramName, Object definition) {
        if (this.parameterDefinitions == null) {
            this.parameterDefinitions = new java.util.HashMap<>();
        }
        this.parameterDefinitions.put(paramName, definition);
    }

    /**
     * 设置变量映射
     */
    public void setVariableMapping(String variableName, String mapping) {
        if (this.variableMapping == null) {
            this.variableMapping = new java.util.HashMap<>();
        }
        this.variableMapping.put(variableName, mapping);
    }

    /**
     * 获取编译结果摘要
     */
    public String getCompileSummary() {
        if (isCompileSuccess()) {
            return String.format("编译成功 - 规则ID: %d, 耗时: %dms", ruleId, compileDuration);
        } else if (hasCompileError()) {
            return String.format("编译失败 - 规则ID: %d, 错误: %s", ruleId, compileError);
        } else {
            return String.format("编译状态未知 - 规则ID: %d", ruleId);
        }
    }
}
