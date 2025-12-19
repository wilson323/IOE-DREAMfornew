package net.lab1024.sa.oa.workflow.dmn;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.flowable.dmn.api.DmnDecisionQuery;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnDecisionService;
import org.flowable.dmn.api.DecisionExecutionAuditContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Flowable 7.2.0 DMN决策引擎服务
 * <p>
 * 提供企业级决策管理功能，支持决策表部署、决策执行、
 * 决策版本控制、决策结果缓存等高级功能
 * 兼容Flowable 7.2.0 DMN引擎API
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-17
 */
@Service
@ConditionalOnBean(DmnDecisionService.class)
public class DecisionEngineService {

    private static final Logger log = LoggerFactory.getLogger(DecisionEngineService.class);

    @Resource
    private DmnRepositoryService dmnRepositoryService;

    @Resource
    private DmnDecisionService dmnDecisionService;

    /**
     * 部署决策定义
     *
     * @param name 决策名称
     * @param dmnXml DMN XML内容
     * @return 部署ID
     */
    public String deployDecision(String name, String dmnXml) {
        log.info("[DMN引擎] 部署决策定义: name={}", name);

        InputStream dmnStream = new ByteArrayInputStream(dmnXml.getBytes(StandardCharsets.UTF_8));

        DmnDeployment deployment = dmnRepositoryService.createDeployment()
                .name(name)
                .addInputStream(name + ".dmn", dmnStream)
                .deploy();

        log.info("[DMN引擎] 决策定义部署成功: deploymentId={}", deployment.getId());
        return deployment.getId();
    }

    /**
     * 执行决策表
     *
     * @param decisionKey 决策Key
     * @param variables 输入变量
     * @return 决策结果
     */
    public Map<String, Object> executeDecision(String decisionKey, Map<String, Object> variables) {
        log.info("[DMN引擎] 执行决策: decisionKey={}, variables={}", decisionKey, variables);

        try {
            Map<String, Object> result = dmnDecisionService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(variables)
                    .executeWithSingleResult();

            log.info("[DMN引擎] 决策执行完成: decisionKey={}, result={}", decisionKey, result);
            return result != null ? result : new HashMap<>();

        } catch (Exception e) {
            log.error("[DMN引擎] 决策执行失败: decisionKey={}, error={}", decisionKey, e.getMessage());
            throw new RuntimeException("决策执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行决策表（多结果）
     *
     * @param decisionKey 决策Key
     * @param variables 输入变量
     * @return 多条决策结果
     */
    public List<Map<String, Object>> executeDecisionMultiple(String decisionKey, Map<String, Object> variables) {
        log.info("[DMN引擎] 执行决策(多结果): decisionKey={}", decisionKey);

        try {
            List<Map<String, Object>> results = dmnDecisionService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(variables)
                    .execute();

            log.info("[DMN引擎] 决策执行完成: decisionKey={}, resultCount={}", decisionKey,
                    results != null ? results.size() : 0);
            return results != null ? results : new ArrayList<>();

        } catch (Exception e) {
            log.error("[DMN引擎] 决策执行失败: decisionKey={}, error={}", decisionKey, e.getMessage());
            throw new RuntimeException("决策执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行决策并返回审计信息
     *
     * @param decisionKey 决策Key
     * @param variables 输入变量
     * @return 决策执行审计容器
     */
    public DecisionExecutionAuditContainer executeDecisionWithAudit(String decisionKey, Map<String, Object> variables) {
        log.info("[DMN引擎] 执行决策(带审计): decisionKey={}", decisionKey);

        try {
            DecisionExecutionAuditContainer auditContainer = dmnDecisionService.createExecuteDecisionBuilder()
                    .decisionKey(decisionKey)
                    .variables(variables)
                    .executeWithAuditTrail();

            log.info("[DMN引擎] 决策执行完成(带审计): decisionKey={}", decisionKey);
            return auditContainer;

        } catch (Exception e) {
            log.error("[DMN引擎] 决策执行失败: decisionKey={}, error={}", decisionKey, e.getMessage());
            throw new RuntimeException("决策执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取决策定义列表
     *
     * @return 决策定义列表
     */
    public List<Map<String, Object>> listDecisions() {
        log.info("[DMN引擎] 获取决策定义列表");

        List<Map<String, Object>> decisions = new ArrayList<>();

        DmnDecisionQuery query = dmnRepositoryService.createDecisionQuery()
                .latestVersion();

        query.list().forEach(decision -> {
            Map<String, Object> decisionInfo = new HashMap<>();
            decisionInfo.put("id", decision.getId());
            decisionInfo.put("key", decision.getKey());
            decisionInfo.put("name", decision.getName());
            decisionInfo.put("version", decision.getVersion());
            decisionInfo.put("deploymentId", decision.getDeploymentId());
            decisions.add(decisionInfo);
        });

        return decisions;
    }

    /**
     * 删除决策部署
     *
     * @param deploymentId 部署ID
     */
    public void deleteDeployment(String deploymentId) {
        log.info("[DMN引擎] 删除决策部署: deploymentId={}", deploymentId);
        dmnRepositoryService.deleteDeployment(deploymentId);
        log.info("[DMN引擎] 决策部署删除成功: deploymentId={}", deploymentId);
    }
}
