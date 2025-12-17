package net.lab1024.sa.oa.workflow.config.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.cmmn.api.runtime.CaseInstanceBuilder;
import org.flowable.cmmn.api.runtime.CaseInstanceQuery;
import org.flowable.cmmn.api.runtime.PlanItemInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * CMMN运行时服务包装器
 * <p>
 * 封装Flowable CMMN运行时服务，提供案例实例管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
public class FlowableCmmnRuntimeService {

    private static final Logger log = LoggerFactory.getLogger(FlowableCmmnRuntimeService.class);
    private final CmmnRuntimeService cmmnRuntimeService;

    public FlowableCmmnRuntimeService(CmmnRuntimeService cmmnRuntimeService) {
        this.cmmnRuntimeService = cmmnRuntimeService;
    }

    /**
     * 创建案例实例构建器
     *
     * @return 案例实例构建器
     */
    public CaseInstanceBuilder createCaseInstanceBuilder() {
        return cmmnRuntimeService.createCaseInstanceBuilder();
    }

    /**
     * 启动案例实例
     *
     * @param caseDefinitionKey 案例定义键
     * @return 案例实例
     */
    public CaseInstance startCaseInstance(String caseDefinitionKey) {
        log.info("[CMMN服务] 启动案例实例: key={}", caseDefinitionKey);
        return cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey(caseDefinitionKey)
                .start();
    }

    /**
     * 启动案例实例（带变量）
     *
     * @param caseDefinitionKey 案例定义键
     * @param variables 变量
     * @return 案例实例
     */
    public CaseInstance startCaseInstance(String caseDefinitionKey, Map<String, Object> variables) {
        log.info("[CMMN服务] 启动案例实例: key={}, variables={}", caseDefinitionKey, variables.keySet());
        return cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionKey(caseDefinitionKey)
                .variables(variables)
                .start();
    }

    /**
     * 创建案例实例查询
     *
     * @return 案例实例查询
     */
    public CaseInstanceQuery createCaseInstanceQuery() {
        return cmmnRuntimeService.createCaseInstanceQuery();
    }

    /**
     * 创建计划项实例查询
     *
     * @return 计划项实例查询
     */
    public PlanItemInstanceQuery createPlanItemInstanceQuery() {
        return cmmnRuntimeService.createPlanItemInstanceQuery();
    }

    /**
     * 终止案例实例
     *
     * @param caseInstanceId 案例实例ID
     */
    public void terminateCaseInstance(String caseInstanceId) {
        log.info("[CMMN服务] 终止案例实例: id={}", caseInstanceId);
        cmmnRuntimeService.terminateCaseInstance(caseInstanceId);
    }

    /**
     * 获取原始服务
     *
     * @return 原始CMMN运行时服务
     */
    public CmmnRuntimeService getNativeService() {
        return cmmnRuntimeService;
    }
}
