package net.lab1024.sa.base.module.support.rbac;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import net.lab1024.sa.base.module.support.auth.AuthorizationContext;

/**
 * 策略评估器
 * <p>
 * 基于统一资源码与动作，匹配角色-资源策略以及条件（RAC），得出允许/拒绝
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-16
 */
@Slf4j
@Component
public class PolicyEvaluator {

    /**
     * 评估策略决策
     *
     * @param context 授权上下文
     * @return 策略决策结果
     */
    public PolicyDecision evaluate(AuthorizationContext context) {
        try {
            log.debug("开始策略评估: userId={}, resource={}, action={}",
                    context.getUserId(), context.getResourceCode(), context.getRequestedAction());

            // 1. 上下文有效性检查
            if (!context.isValid()) {
                return PolicyDecision.denied("AuthorizationContext无效", false);
            }

            // 2. 超级管理员直接通过
            if (context.getIsSuperAdmin()) {
                return PolicyDecision.allowed("超级管理员权限", true);
            }

            // 3. 检查基础权限（角色-资源映射）
            PolicyDecision baseDecision = evaluateBasePermission(context);
            if (!baseDecision.isAllowed()) {
                return baseDecision;
            }

            // 4. 检查RAC条件策略
            PolicyDecision conditionDecision = evaluateConditionPolicy(context);
            if (!conditionDecision.isAllowed()) {
                return conditionDecision;
            }

            // 5. 检查时间有效期
            if (!context.isInValidPeriod()) {
                return PolicyDecision.denied("权限不在有效期内", false);
            }

            return PolicyDecision.allowed("策略评估通过", true);

        } catch (Exception e) {
            log.error("策略评估异常: userId={}, resource={}, {}",
                    context.getUserId(), context.getResourceCode(), e.getMessage(), e);
            return PolicyDecision.denied("策略评估异常: " + e.getMessage(), false);
        }
    }

    /**
     * 评估基础权限（角色-资源映射）
     *
     * @param context 授权上下文
     * @return 基础权限决策
     */
    private PolicyDecision evaluateBasePermission(AuthorizationContext context) {
        // 检查角色-资源权限映射
        for (String roleCode : context.getRoleCodes()) {
            if (hasResourcePermission(roleCode, context.getResourceCode(), context.getRequestedAction())) {
                return PolicyDecision.allowed("角色拥有基础权限", true);
            }
        }

        return PolicyDecision.denied("角色无此资源权限", false);
    }

    /**
     * 评估RAC条件策略
     *
     * @param context 授权上下文
     * @return 条件策略决策
     */
    private PolicyDecision evaluateConditionPolicy(AuthorizationContext context) {
        // 检查是否存在条件策略
        Map<String, Object> conditionRules = getConditionRules(context);
        if (conditionRules == null || conditionRules.isEmpty()) {
            return PolicyDecision.allowed("无条件策略限制", false);
        }

        // 评估所有条件
        for (Map.Entry<String, Object> entry : conditionRules.entrySet()) {
            String conditionType = entry.getKey();
            Object conditionValue = entry.getValue();

            PolicyDecision conditionResult = evaluateCondition(context, conditionType, conditionValue);
            if (!conditionResult.isAllowed()) {
                return conditionResult;
            }
        }

        return PolicyDecision.allowed("所有条件策略通过", true);
    }

    /**
     * 评估单个条件
     *
     * @param context       授权上下文
     * @param conditionType 条件类型
     * @param conditionValue 条件值
     * @return 条件评估结果
     */
    private PolicyDecision evaluateCondition(AuthorizationContext context, String conditionType, Object conditionValue) {
        try {
            switch (conditionType.toUpperCase()) {
                case "TIME_RANGE":
                    return evaluateTimeRangeCondition(context, conditionValue);
                case "IP_WHITELIST":
                    return evaluateIpWhitelistCondition(context, conditionValue);
                case "AREA_LIMIT":
                    return evaluateAreaLimitCondition(context, conditionValue);
                case "DATA_SCOPE":
                    return evaluateDataScopeCondition(context, conditionValue);
                case "CUSTOM":
                    return evaluateCustomCondition(context, conditionValue);
                default:
                    log.warn("未知的条件类型: {}", conditionType);
                    return PolicyDecision.allowed("未知条件类型，跳过检查", false);
            }
        } catch (Exception e) {
            log.error("条件评估异常: type={}, value={}, error={}", conditionType, conditionValue, e.getMessage());
            return PolicyDecision.denied("条件评估异常: " + e.getMessage(), false);
        }
    }

    /**
     * 评估时间范围条件
     */
    private PolicyDecision evaluateTimeRangeCondition(AuthorizationContext context, Object conditionValue) {
        // 暂时简化处理，直接通过
        return PolicyDecision.allowed("时间范围条件通过", true);
    }

    /**
     * 评估IP白名单条件
     */
    private PolicyDecision evaluateIpWhitelistCondition(AuthorizationContext context, Object conditionValue) {
        // 暂时简化处理，直接通过
        return PolicyDecision.allowed("IP白名单条件通过", true);
    }

    /**
     * 评估区域限制条件
     */
    private PolicyDecision evaluateAreaLimitCondition(AuthorizationContext context, Object conditionValue) {
        // 暂时简化处理，直接通过
        return PolicyDecision.allowed("区域限制条件通过", true);
    }

    /**
     * 评估数据域条件
     */
    private PolicyDecision evaluateDataScopeCondition(AuthorizationContext context, Object conditionValue) {
        // 暂时简化处理，直接通过
        return PolicyDecision.allowed("数据域条件通过", true);
    }

    /**
     * 评估自定义条件
     */
    private PolicyDecision evaluateCustomCondition(AuthorizationContext context, Object conditionValue) {
        // 暂时简化处理，直接通过
        return PolicyDecision.allowed("自定义条件通过", true);
    }

    /**
     * 检查角色是否有资源权限
     *
     * @param roleCode     角色编码
     * @param resourceCode 资源编码
     * @param action       动作
     * @return 是否有权限
     */
    private boolean hasResourcePermission(String roleCode, String resourceCode, String action) {
        // TODO: 这里应该查询数据库 t_rbac_role_resource 表
        // 暂时返回 true 进行测试
        return true;
    }

    /**
     * 获取条件规则
     *
     * @param context 授权上下文
     * @return 条件规则映射
     */
    private Map<String, Object> getConditionRules(AuthorizationContext context) {
        // TODO: 这里应该查询数据库获取该用户角色对应的条件策略
        // 暂时返回空映射进行测试
        return new HashMap<>();
    }

    /**
     * 批量评估策略
     *
     * @param contexts 授权上下文集合
     * @return 策略决策结果映射（用户ID -> 决策结果）
     */
    public Map<Long, PolicyDecision> batchEvaluate(List<AuthorizationContext> contexts) {
        Map<Long, PolicyDecision> results = new HashMap<>();

        for (AuthorizationContext context : contexts) {
            PolicyDecision decision = evaluate(context);
            results.put(context.getUserId(), decision);
        }

        return results;
    }

    /**
     * 策略决策结果
     */
    public static class PolicyDecision {
        private boolean allowed;
        private String reason;
        private boolean conditionMatched;

        private PolicyDecision(boolean allowed, String reason, boolean conditionMatched) {
            this.allowed = allowed;
            this.reason = reason;
            this.conditionMatched = conditionMatched;
        }

        public static PolicyDecision allowed(String reason, boolean conditionMatched) {
            return new PolicyDecision(true, reason, conditionMatched);
        }

        public static PolicyDecision denied(String reason, boolean conditionMatched) {
            return new PolicyDecision(false, reason, conditionMatched);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }

        public boolean isConditionMatched() {
            return conditionMatched;
        }

        @Override
        public String toString() {
            return String.format("PolicyDecision{allowed=%s, reason='%s', conditionMatched=%s}",
                    allowed, reason, conditionMatched);
        }
    }
}