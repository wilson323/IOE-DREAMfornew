package net.lab1024.sa.attendance.engine.rule.validation;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.RuleLoader;
import net.lab1024.sa.attendance.engine.RuleValidator;
import net.lab1024.sa.attendance.engine.model.RuleValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 规则验证服务
 * <p>
 * 负责规则验证、适用性检查和各种范围检查
 * 严格遵循CLAUDE.md全局架构规范,纯Java类
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class RuleValidationService {

    private final RuleLoader ruleLoader;
    private final RuleValidator ruleValidator;

    /**
     * 构造函数注入依赖
     */
    public RuleValidationService(RuleLoader ruleLoader, RuleValidator ruleValidator) {
        this.ruleLoader = ruleLoader;
        this.ruleValidator = ruleValidator;
    }

    /**
     * 验证规则
     *
     * @param ruleId 规则ID
     * @return 验证结果
     */
    public RuleValidationResult validateRule(Long ruleId) {
        log.debug("[规则验证服务] 验证规则, 规则ID: {}", ruleId);
        return ruleValidator.validateRule(ruleId);
    }

    /**
     * 检查规则是否适用
     *
     * @param ruleId  规则ID
     * @param context 规则执行上下文
     * @return 是否适用
     */
    public boolean isRuleApplicable(Long ruleId, RuleExecutionContext context) {
        log.debug("[规则验证服务] 检查规则适用性, 规则ID: {}", ruleId);

        try {
            // 1. 加载规则配置
            Map<String, Object> ruleConfig = ruleLoader.loadRuleConfig(ruleId);
            if (ruleConfig == null) {
                log.debug("[规则验证服务] 规则配置未找到, 规则ID: {}", ruleId);
                return false;
            }

            // 2. 检查各种范围
            if (!checkDepartmentScope(ruleConfig, context)) {
                log.debug("[规则验证服务] 部门范围不匹配, 规则ID: {}", ruleId);
                return false;
            }

            if (!checkUserAttributes(ruleConfig, context)) {
                log.debug("[规则验证服务] 用户属性不匹配, 规则ID: {}", ruleId);
                return false;
            }

            if (!checkTimeScope(ruleConfig, context)) {
                log.debug("[规则验证服务] 时间范围不匹配, 规则ID: {}", ruleId);
                return false;
            }

            if (!checkRuleFilters(ruleConfig, context)) {
                log.debug("[规则验证服务] 规则过滤器不匹配, 规则ID: {}", ruleId);
                return false;
            }

            log.debug("[规则验证服务] 规则适用, 规则ID: {}", ruleId);
            return true;

        } catch (Exception e) {
            log.error("[规则验证服务] 检查规则适用性失败, 规则ID: {}", ruleId, e);
            return false;
        }
    }

    /**
     * 检查部门范围
     *
     * @param ruleConfig 规则配置
     * @param context    规则执行上下文
     * @return 是否在范围内
     */
    public boolean checkDepartmentScope(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object departmentScope = ruleConfig.get("departmentScope");
        if (departmentScope == null) {
            // 未配置部门范围，适用于所有部门
            return true;
        }

        if (context.getDepartmentId() == null) {
            return false;
        }

        if (departmentScope instanceof List) {
            List<?> scopeList = (List<?>) departmentScope;
            for (Object scope : scopeList) {
                if (scope != null && scope.toString().equals(context.getDepartmentId().toString())) {
                    return true;
                }
            }
            return false;
        }

        return departmentScope.toString().equals(context.getDepartmentId().toString());
    }

    /**
     * 检查用户属性
     *
     * @param ruleConfig 规则配置
     * @param context    规则执行上下文
     * @return 是否匹配
     */
    public boolean checkUserAttributes(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object userAttributes = ruleConfig.get("userAttributes");
        if (userAttributes == null) {
            // 未配置用户属性限制
            return true;
        }

        if (!(userAttributes instanceof Map)) {
            return true;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> requiredAttributes = (Map<String, Object>) userAttributes;

        // 检查每个必需的用户属性
        for (Map.Entry<String, Object> entry : requiredAttributes.entrySet()) {
            String attributeName = entry.getKey();
            Object expectedValue = entry.getValue();

            Object actualValue = context.getUserAttribute(attributeName);
            if (actualValue == null || !actualValue.toString().equals(expectedValue.toString())) {
                log.debug("[规则验证服务] 用户属性不匹配: {} (expected: {}, actual: {})",
                        attributeName, expectedValue, actualValue);
                return false;
            }
        }

        return true;
    }

    /**
     * 检查时间范围
     *
     * @param ruleConfig 规则配置
     * @param context    规则执行上下文
     * @return 是否在范围内
     */
    public boolean checkTimeScope(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object startTime = ruleConfig.get("startTime");
        Object endTime = ruleConfig.get("endTime");

        if (startTime == null && endTime == null) {
            // 未配置时间范围限制
            return true;
        }

        // 检查开始时间
        if (startTime != null) {
            // 假设context中有时间相关的属性
            // 这里需要根据实际的上下文对象实现
            log.debug("[规则验证服务] 检查开始时间: {}", startTime);
        }

        // 检查结束时间
        if (endTime != null) {
            log.debug("[规则验证服务] 检查结束时间: {}", endTime);
        }

        return true;
    }

    /**
     * 检查规则过滤器
     *
     * @param ruleConfig 规则配置
     * @param context    规则执行上下文
     * @return 是否通过过滤器
     */
    public boolean checkRuleFilters(Map<String, Object> ruleConfig, RuleExecutionContext context) {
        Object filters = ruleConfig.get("filters");
        if (filters == null) {
            // 未配置过滤器
            return true;
        }

        if (!(filters instanceof List)) {
            return true;
        }

        List<?> filterList = (List<?>) filters;

        // 检查每个过滤器
        for (Object filter : filterList) {
            if (filter instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> filterMap = (Map<String, Object>) filter;

                String filterType = (String) filterMap.get("type");
                Object filterValue = filterMap.get("value");

                if ("EXCLUDE_USER".equals(filterType) &&
                        filterValue != null &&
                        filterValue.toString().equals(context.getUserId().toString())) {
                    log.debug("[规则验证服务] 用户在排除列表中: userId={}", context.getUserId());
                    return false;
                }

                // 可以添加更多过滤器类型
            }
        }

        return true;
    }
}
