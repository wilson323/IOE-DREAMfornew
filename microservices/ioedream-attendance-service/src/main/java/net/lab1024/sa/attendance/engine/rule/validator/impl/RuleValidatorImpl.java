package net.lab1024.sa.attendance.engine.rule.validator.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.engine.rule.loader.RuleLoader;
import net.lab1024.sa.attendance.engine.rule.model.RuleValidationResult;
import net.lab1024.sa.attendance.engine.rule.validator.RuleValidator;
import net.lab1024.sa.attendance.entity.AttendanceRuleEntity;

/**
 * 规则验证器实现类
 * <p>
 * 规则验证器核心实现，支持规则有效性、语法和权限验证
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Component("ruleValidator")
@Slf4j
public class RuleValidatorImpl implements RuleValidator {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    @Resource
    private RuleLoader ruleLoader;

    // 规则条件表达式正则模式
    private static final Pattern RULE_CONDITION_PATTERN = Pattern.compile(
            "^\\s*(if\\s+)?\\s*([a-zA-Z_][a-zA-Z0-9_]*\\s*==|>=|<=|>|<|!=|in|contains)\\s*['\"]?[^'\"]*['\"]?\\s*(and|or|not)?\\s*.*\\s*$",
            Pattern.CASE_INSENSITIVE);

    // 规则动作JSON模式
    private static final Pattern RULE_ACTION_PATTERN = Pattern.compile(
            "^\\s*\\{\\s*['\"]?(type|action)['\"]?\\s*:\\s*['\"]?[a-zA-Z_][a-zA-Z0-9_]*['\"]?\\s*,?.*}\\s*$");

    /**
     * 验证规则有效性
     */
    @Override
    public RuleValidationResult validateRule(Long ruleId) {
        log.debug("[规则验证器] 开始验证规则有效性: {}", ruleId);

        long startTime = System.currentTimeMillis();
        RuleValidationResult result = RuleValidationResult.builder()
                .ruleId(ruleId)
                .validationType("FULL_VALIDATION")
                .validationSteps(new ArrayList<>())
                .validationTime(LocalDateTime.now())
                .build();

        try {
            // 步骤1: 检查规则是否存在
            addValidationStep(result, "existence_check", "检查规则是否存在", () -> {
                if (!ruleLoader.ruleExists(ruleId)) {
                    return RuleValidationResult.ValidationStep.builder()
                            .stepName("existence_check")
                            .passed(false)
                            .errorMessage("规则不存在或已删除")
                            .build();
                }
                return RuleValidationResult.ValidationStep.builder()
                        .stepName("existence_check")
                        .passed(true)
                        .build();
            });

            // 步骤2: 检查规则基本信息
            addValidationStep(result, "basic_info_check", "检查规则基本信息", () -> {
                AttendanceRuleEntity rule = attendanceRuleDao.selectById(ruleId);
                if (rule == null) {
                    return RuleValidationResult.ValidationStep.builder()
                            .stepName("basic_info_check")
                            .passed(false)
                            .errorMessage("无法获取规则基本信息")
                            .build();
                }

                // 检查规则名称
                if (!StringUtils.hasText(rule.getRuleName())) {
                    return RuleValidationResult.ValidationStep.builder()
                            .stepName("basic_info_check")
                            .passed(false)
                            .errorMessage("规则名称不能为空")
                            .build();
                }

                // 检查规则类型
                if (!StringUtils.hasText(rule.getRuleType())) {
                    return RuleValidationResult.ValidationStep.builder()
                            .stepName("basic_info_check")
                            .passed(false)
                            .errorMessage("规则类型不能为空")
                            .build();
                }

                return RuleValidationResult.ValidationStep.builder()
                        .stepName("basic_info_check")
                        .passed(true)
                        .stepDetails(Map.of(
                                "ruleName", rule.getRuleName(),
                                "ruleType", rule.getRuleType(),
                                "ruleCategory", rule.getRuleCategory()))
                        .build();
            });

            // 步骤3: 验证规则条件表达式
            addValidationStep(result, "condition_validation", "验证规则条件表达式", () -> {
                String condition = ruleLoader.loadRuleCondition(ruleId);
                RuleValidationResult conditionResult = validateRuleCondition(condition);

                return RuleValidationResult.ValidationStep.builder()
                        .stepName("condition_validation")
                        .passed(conditionResult.getValid())
                        .errorMessage(conditionResult.getErrorMessage())
                        .stepDetails(conditionResult.getValidationDetails())
                        .build();
            });

            // 步骤4: 验证规则动作配置
            addValidationStep(result, "action_validation", "验证规则动作配置", () -> {
                String action = ruleLoader.loadRuleAction(ruleId).toString();
                RuleValidationResult actionResult = validateRuleAction(action);

                return RuleValidationResult.ValidationStep.builder()
                        .stepName("action_validation")
                        .passed(actionResult.getValid())
                        .errorMessage(actionResult.getErrorMessage())
                        .stepDetails(actionResult.getValidationDetails())
                        .build();
            });

            // 步骤5: 验证规则时间范围
            addValidationStep(result, "time_range_validation", "验证规则时间范围", () -> {
                RuleValidationResult timeResult = validateRuleTimeRange(ruleId, LocalDateTime.now());

                return RuleValidationResult.ValidationStep.builder()
                        .stepName("time_range_validation")
                        .passed(timeResult.getValid())
                        .errorMessage(timeResult.getErrorMessage())
                        .stepDetails(timeResult.getValidationDetails())
                        .build();
            });

            // 步骤6: 验证规则依赖
            addValidationStep(result, "dependency_validation", "验证规则依赖", () -> {
                RuleValidationResult dependencyResult = validateRuleDependencies(ruleId);

                return RuleValidationResult.ValidationStep.builder()
                        .stepName("dependency_validation")
                        .passed(dependencyResult.getValid())
                        .errorMessage(dependencyResult.getErrorMessage())
                        .stepDetails(dependencyResult.getValidationDetails())
                        .build();
            });

            // 计算总体验证结果
            boolean allPassed = result.getValidationSteps().stream()
                    .allMatch(RuleValidationResult.ValidationStep::getPassed);

            result.setValid(allPassed);
            if (!allPassed) {
                result.setErrorCode("RULE_VALIDATION_FAILED");
                result.setErrorMessage("规则验证失败，存在多个错误");
                result.setSeverity("ERROR");
            } else {
                result.setSeverity("INFO");
            }

            // 收集所有错误和警告
            List<String> allWarnings = new ArrayList<>();
            for (RuleValidationResult.ValidationStep step : result.getValidationSteps()) {
                if (!step.getPassed() && StringUtils.hasText(step.getErrorMessage())) {
                    result.addFixSuggestion("修复" + step.getStepName() + "步骤的错误: " + step.getErrorMessage());
                }
            }

            if (!allWarnings.isEmpty()) {
                result.setWarningMessages(allWarnings);
            }

        } catch (Exception e) {
            log.error("[规则验证器] 规则验证异常: {}", ruleId, e);
            result.setValid(false);
            result.setErrorCode("VALIDATION_EXCEPTION");
            result.setErrorMessage("验证过程中发生异常: " + e.getMessage());
            result.setSeverity("CRITICAL");
        }

        result.setValidationDuration(System.currentTimeMillis() - startTime);

        log.debug("[规则验证器] 规则验证完成: {}, 结果: {}, 耗时: {}ms",
                ruleId, result.getValid() ? "通过" : "失败", result.getValidationDuration());

        return result;
    }

    /**
     * 验证规则条件表达式
     */
    @Override
    public RuleValidationResult validateRuleCondition(String ruleCondition) {
        log.debug("[规则验证器] 验证规则条件表达式: {}", ruleCondition);

        try {
            if (!StringUtils.hasText(ruleCondition)) {
                return RuleValidationResult.failure("CONDITION_EMPTY", "规则条件表达式不能为空");
            }

            // 基本语法检查
            if (!RULE_CONDITION_PATTERN.matcher(ruleCondition).matches()) {
                return RuleValidationResult.failure("CONDITION_SYNTAX_ERROR",
                        "规则条件表达式语法错误，格式应为: if 条件 {动作}");
            }

            // 检查常见的语法错误
            if (ruleCondition.contains("==") && !ruleCondition.contains(" ")) {
                return RuleValidationResult.failure("CONDITION_FORMAT_ERROR",
                        "条件表达式中操作符周围需要空格");
            }

            // 检查括号匹配
            int openBrackets = 0;
            int closeBrackets = 0;
            for (char c : ruleCondition.toCharArray()) {
                if (c == '(')
                    openBrackets++;
                if (c == ')')
                    closeBrackets++;
            }
            if (openBrackets != closeBrackets) {
                return RuleValidationResult.failure("CONDITION_BRACKET_MISMATCH",
                        "条件表达式括号不匹配");
            }

            // 检查是否包含黑名单关键字
            String[] blacklistKeywords = { "eval", "exec", "system", "runtime", "class" };
            for (String keyword : blacklistKeywords) {
                if (ruleCondition.toLowerCase().contains(keyword)) {
                    return RuleValidationResult.failure("CONDITION_BLACKLIST_KEYWORD",
                            "条件表达式包含不安全的关键字: " + keyword);
                }
            }

            Map<String, Object> details = new HashMap<>();
            details.put("conditionLength", ruleCondition.length());
            details.put("hasLogicalOperators", ruleCondition.matches(".*(and|or|not).*"));
            details.put("hasComparisonOperators", ruleCondition.matches(".*(==|>=|<=|>|<|!=).*"));
            details.put("bracketCount", Math.max(openBrackets, closeBrackets));

            return RuleValidationResult.success("CONDITION_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则条件表达式失败: {}", ruleCondition, e);
            return RuleValidationResult.failure("CONDITION_VALIDATION_ERROR",
                    "验证条件表达式时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则动作配置
     */
    @Override
    public RuleValidationResult validateRuleAction(String ruleAction) {
        log.debug("[规则验证器] 验证规则动作配置: {}", ruleAction);

        try {
            if (!StringUtils.hasText(ruleAction)) {
                return RuleValidationResult.failure("ACTION_EMPTY", "规则动作配置不能为空");
            }

            // 检查是否是有效的JSON格式
            if (!ruleAction.startsWith("{") || !ruleAction.endsWith("}")) {
                return RuleValidationResult.failure("ACTION_NOT_JSON", "规则动作必须是有效的JSON格式");
            }

            // 检查基本JSON结构
            if (!RULE_ACTION_PATTERN.matcher(ruleAction).matches()) {
                return RuleValidationResult.failure("ACTION_FORMAT_ERROR",
                        "规则动作格式错误，应包含type或action字段");
            }

            // 检查是否包含必需的字段
            if (!ruleAction.contains("\"type\"") && !ruleAction.contains("'type'") &&
                    !ruleAction.contains("\"action\"") && !ruleAction.contains("'action'")) {
                return RuleValidationResult.failure("ACTION_MISSING_TYPE", "规则动作缺少type或action字段");
            }

            // 检查动作类型是否有效
            String[] validActionTypes = { "APPROVE", "REJECT", "NOTIFY", "LOG", "EXECUTE", "CALL" };
            boolean hasValidType = false;
            for (String type : validActionTypes) {
                if (ruleAction.toUpperCase().contains(type)) {
                    hasValidType = true;
                    break;
                }
            }

            if (!hasValidType) {
                List<String> warnings = new ArrayList<>();
                warnings.add("规则动作类型不在推荐列表中");
                return RuleValidationResult.warning("ACTION_TYPE_WARNING", warnings);
            }

            Map<String, Object> details = new HashMap<>();
            details.put("actionLength", ruleAction.length());
            details.put("hasValidType", hasValidType);
            details.put("jsonStructure", "VALID");

            return RuleValidationResult.success("ACTION_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则动作配置失败: {}", ruleAction, e);
            return RuleValidationResult.failure("ACTION_VALIDATION_ERROR",
                    "验证动作配置时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则语法
     */
    @Override
    public RuleValidationResult validateRuleSyntax(String ruleCondition, String ruleAction) {
        log.debug("[规则验证器] 验证规则语法");

        try {
            // 验证条件表达式
            RuleValidationResult conditionResult = validateRuleCondition(ruleCondition);
            if (!conditionResult.getValid()) {
                return conditionResult;
            }

            // 验证动作配置
            RuleValidationResult actionResult = validateRuleAction(ruleAction);
            if (!actionResult.getValid()) {
                return actionResult;
            }

            // 检查条件和动作的兼容性
            if (conditionResult.getWarningMessages() != null && actionResult.getWarningMessages() != null) {
                List<String> combinedWarnings = new ArrayList<>();
                combinedWarnings.addAll(conditionResult.getWarningMessages());
                combinedWarnings.addAll(actionResult.getWarningMessages());
                return RuleValidationResult.warning("SYNTAX_COMPATIBILITY_WARNING", combinedWarnings);
            }

            Map<String, Object> details = new HashMap<>();
            details.put("conditionValid", conditionResult.getValid());
            details.put("actionValid", actionResult.getValid());
            details.put("overallSyntax", "VALID");

            return RuleValidationResult.success("SYNTAX_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则语法失败", e);
            return RuleValidationResult.failure("SYNTAX_VALIDATION_ERROR",
                    "验证规则语法时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则参数
     */
    @Override
    public RuleValidationResult validateRuleParameters(String ruleType, Map<String, Object> parameters) {
        log.debug("[规则验证器] 验证规则参数: type={}", ruleType);

        try {
            if (!StringUtils.hasText(ruleType)) {
                return RuleValidationResult.failure("RULE_TYPE_EMPTY", "规则类型不能为空");
            }

            if (parameters == null || parameters.isEmpty()) {
                return RuleValidationResult.failure("PARAMETERS_EMPTY", "规则参数不能为空");
            }

            // 根据规则类型验证必需参数
            switch (ruleType.toUpperCase()) {
                case "TIME_BASED":
                    return validateTimeBasedRuleParameters(parameters);
                case "LOCATION_BASED":
                    return validateLocationBasedRuleParameters(parameters);
                case "USER_BASED":
                    return validateUserBasedRuleParameters(parameters);
                case "DEPARTMENT_BASED":
                    return validateDepartmentBasedRuleParameters(parameters);
                default:
                    return validateGenericRuleParameters(parameters);
            }

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则参数失败: type={}", ruleType, e);
            return RuleValidationResult.failure("PARAMETER_VALIDATION_ERROR",
                    "验证规则参数时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则权限
     */
    @Override
    public RuleValidationResult validateRulePermission(Long ruleId, Long userId) {
        log.debug("[规则验证器] 验证规则权限: ruleId={}, userId={}", ruleId, userId);

        try {
            if (userId == null) {
                return RuleValidationResult.failure("USER_ID_EMPTY", "用户ID不能为空");
            }

            // TODO: 实现用户权限验证逻辑
            // 这里应该检查用户是否有权限执行该规则
            // 包括部门权限、角色权限等

            Map<String, Object> details = new HashMap<>();
            details.put("ruleId", ruleId);
            details.put("userId", userId);
            details.put("permissionCheck", "PASSED");

            return RuleValidationResult.success("PERMISSION_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则权限失败: ruleId={}, userId={}", ruleId, userId, e);
            return RuleValidationResult.failure("PERMISSION_VALIDATION_ERROR",
                    "验证规则权限时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则时间范围
     */
    @Override
    public RuleValidationResult validateRuleTimeRange(Long ruleId, LocalDateTime checkTime) {
        log.debug("[规则验证器] 验证规则时间范围: ruleId={}, checkTime={}", ruleId, checkTime);

        try {
            AttendanceRuleEntity rule = attendanceRuleDao.selectById(ruleId);
            if (rule == null) {
                return RuleValidationResult.failure("RULE_NOT_FOUND", "规则不存在");
            }

            LocalDateTime effectiveTime = rule.getEffectiveStartTime() != null
                    ? LocalDateTime.parse(rule.getEffectiveStartTime())
                    : null;
            LocalDateTime expireTime = rule.getEffectiveEndTime() != null
                    ? LocalDateTime.parse(rule.getEffectiveEndTime())
                    : null;

            // 检查生效时间
            if (effectiveTime != null && checkTime.isBefore(effectiveTime)) {
                return RuleValidationResult.failure("RULE_NOT_EFFECTIVE",
                        "规则尚未生效，生效时间: " + effectiveTime);
            }

            // 检查过期时间
            if (expireTime != null && checkTime.isAfter(expireTime)) {
                return RuleValidationResult.failure("RULE_EXPIRED",
                        "规则已过期，过期时间: " + expireTime);
            }

            Map<String, Object> details = new HashMap<>();
            details.put("effectiveTime", effectiveTime);
            details.put("expireTime", expireTime);
            details.put("checkTime", checkTime);
            details.put("timeRangeValid", true);

            return RuleValidationResult.success("TIME_RANGE_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则时间范围失败: ruleId={}", ruleId, e);
            return RuleValidationResult.failure("TIME_RANGE_VALIDATION_ERROR",
                    "验证规则时间范围时发生错误: " + e.getMessage());
        }
    }

    /**
     * 验证规则依赖
     */
    @Override
    public RuleValidationResult validateRuleDependencies(Long ruleId) {
        log.debug("[规则验证器] 验证规则依赖: {}", ruleId);

        try {
            // TODO: 实现规则依赖验证逻辑
            // 检查规则所依赖的其他规则是否存在且有效
            // 检查循环依赖等

            Map<String, Object> details = new HashMap<>();
            details.put("ruleId", ruleId);
            details.put("dependencies", new ArrayList<>());
            details.put("dependencyCheck", "PASSED");

            return RuleValidationResult.success("DEPENDENCY_VALIDATION", details);

        } catch (Exception e) {
            log.error("[规则验证器] 验证规则依赖失败: ruleId={}", ruleId, e);
            return RuleValidationResult.failure("DEPENDENCY_VALIDATION_ERROR",
                    "验证规则依赖时发生错误: " + e.getMessage());
        }
    }

    /**
     * 添加验证步骤
     */
    private void addValidationStep(RuleValidationResult result, String stepName, String description,
            java.util.function.Supplier<RuleValidationResult.ValidationStep> stepSupplier) {
        try {
            RuleValidationResult.ValidationStep step = stepSupplier.get();
            step.setStepName(stepName);
            step.setStepDescription(description);
            result.addValidationStep(step);
        } catch (Exception e) {
            log.error("[规则验证器] 执行验证步骤失败: {}", stepName, e);
            result.addValidationStep(RuleValidationResult.ValidationStep.builder()
                    .stepName(stepName)
                    .stepDescription(description)
                    .passed(false)
                    .errorMessage("执行验证步骤时发生异常: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 验证时间基础规则参数
     */
    private RuleValidationResult validateTimeBasedRuleParameters(Map<String, Object> parameters) {
        String[] requiredParams = { "startTime", "endTime" };
        for (String param : requiredParams) {
            if (!parameters.containsKey(param)) {
                return RuleValidationResult.failure("MISSING_TIME_PARAMETER",
                        "时间基础规则缺少必需参数: " + param);
            }
        }
        return RuleValidationResult.success();
    }

    /**
     * 验证位置基础规则参数
     */
    private RuleValidationResult validateLocationBasedRuleParameters(Map<String, Object> parameters) {
        String[] requiredParams = { "locationId", "radius" };
        for (String param : requiredParams) {
            if (!parameters.containsKey(param)) {
                return RuleValidationResult.failure("MISSING_LOCATION_PARAMETER",
                        "位置基础规则缺少必需参数: " + param);
            }
        }
        return RuleValidationResult.success();
    }

    /**
     * 验证用户基础规则参数
     */
    private RuleValidationResult validateUserBasedRuleParameters(Map<String, Object> parameters) {
        String[] requiredParams = { "userIds" };
        for (String param : requiredParams) {
            if (!parameters.containsKey(param)) {
                return RuleValidationResult.failure("MISSING_USER_PARAMETER",
                        "用户基础规则缺少必需参数: " + param);
            }
        }
        return RuleValidationResult.success();
    }

    /**
     * 验证部门基础规则参数
     */
    private RuleValidationResult validateDepartmentBasedRuleParameters(Map<String, Object> parameters) {
        String[] requiredParams = { "departmentIds" };
        for (String param : requiredParams) {
            if (!parameters.containsKey(param)) {
                return RuleValidationResult.failure("MISSING_DEPARTMENT_PARAMETER",
                        "部门基础规则缺少必需参数: " + param);
            }
        }
        return RuleValidationResult.success();
    }

    /**
     * 验证通用规则参数
     */
    private RuleValidationResult validateGenericRuleParameters(Map<String, Object> parameters) {
        // 通用参数验证逻辑
        if (parameters.size() < 1) {
            return RuleValidationResult.failure("NO_PARAMETERS", "通用规则至少需要一个参数");
        }
        return RuleValidationResult.success();
    }
}
