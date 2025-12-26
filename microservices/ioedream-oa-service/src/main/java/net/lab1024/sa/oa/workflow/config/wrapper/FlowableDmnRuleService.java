package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.flowable.dmn.api.DecisionExecutionAuditContainer;
import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.ExecuteDecisionBuilder;

/**
 * DMN规则执行服务包装器
 * <p>
 * 封装Flowable DMN规则服务，提供决策表执行功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@SuppressWarnings("deprecation")
@Slf4j
public class FlowableDmnRuleService {


    private final DmnDecisionService dmnDecisionService;

    public FlowableDmnRuleService(DmnDecisionService dmnDecisionService) {
        this.dmnDecisionService = dmnDecisionService;
    }

    /**
     * 创建决策执行构建器
     *
     * @return 决策执行构建器
     */
    public ExecuteDecisionBuilder createExecuteDecisionBuilder() {
        return dmnDecisionService.createExecuteDecisionBuilder();
    }

    /**
     * 执行决策表（单结果）
     *
     * @param decisionKey
     *                    决策表键
     * @param variables
     *                    输入变量
     * @return 决策结果
     */
    public Map<String, Object> executeDecisionByKey(String decisionKey, Map<String, Object> variables) {
        log.info("[DMN服务] 执行决策表: key={}, variables={}", decisionKey, variables.keySet());
        return dmnDecisionService.createExecuteDecisionBuilder().decisionKey(decisionKey).variables(variables)
                .executeWithSingleResult();
    }

    /**
     * 执行决策表（多结果）
     *
     * @param decisionKey
     *                    决策表键
     * @param variables
     *                    输入变量
     * @return 决策结果列表
     */
    public List<Map<String, Object>> executeDecisionByKeyMultipleResults(String decisionKey,
            Map<String, Object> variables) {
        log.info("[DMN服务] 执行决策表(多结果): key={}, variables={}", decisionKey, variables.keySet());
        return dmnDecisionService.createExecuteDecisionBuilder().decisionKey(decisionKey).variables(variables)
                .execute();
    }

    /**
     * 执行决策表并获取审计信息
     *
     * @param decisionKey
     *                    决策表键
     * @param variables
     *                    输入变量
     * @return 决策执行审计容器
     */
    public DecisionExecutionAuditContainer executeDecisionWithAuditTrail(String decisionKey,
            Map<String, Object> variables) {
        log.info("[DMN服务] 执行决策表(含审计): key={}", decisionKey);
        return dmnDecisionService.createExecuteDecisionBuilder().decisionKey(decisionKey).variables(variables)
                .executeWithAuditTrail();
    }

    /**
     * 获取原始服务
     *
     * @return 原始DMN规则服务
     */
    public DmnDecisionService getNativeService() {
        return dmnDecisionService;
    }
}
