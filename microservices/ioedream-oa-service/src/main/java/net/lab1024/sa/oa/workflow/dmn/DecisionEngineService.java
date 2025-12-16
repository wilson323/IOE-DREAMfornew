package net.lab1024.sa.oa.workflow.dmn;

import lombok.extern.slf4j.Slf4j;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.api.DmnRuntimeService;
import org.flowable.dmn.api.DmnHistoryService;
import org.flowable.dmn.engine.DmnEngine;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.dmn.model.DmnDecision;
import org.flowable.dmn.model.DmnDecisionTable;
import org.flowable.dmn.api.delegate.DmnDecisionLogicExecution;
import org.flowable.dmn.api.delegate.DmnDecisionTableOutputEvent;
import org.flowable.decision.model.DecisionResult;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Flowable DMN决策引擎服务
 * <p>
 * 提供企业级决策管理功能，支持决策表部署、决策执行、
 * 决策版本控制、决策结果缓存、决策历史跟踪等高级功能
 * 集成Flowable 6.8.0 DMN引擎特性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
public class DecisionEngineService {

    @Resource
    private DmnEngine dmnEngine;

    @Resource
    private DmnRepositoryService dmnRepositoryService;

    @Resource
    private DmnRuntimeService dmnRuntimeService;

    @Resource
    private DmnHistoryService dmnHistoryService;

    /**
     * 部署决策定义
     *
     * @param decisionDefinitionXml 决策定义XML
     * @param decisionDefinitionKey 决策定义键
     * @param decisionDefinitionName 决策定义名称
     * @return 部署结果
     */
    public String deployDecisionDefinition(String decisionDefinitionXml, String decisionDefinitionKey, String decisionDefinitionName) {
        try {
            log.info("[DMN决策引擎] 开始部署决策定义: key={}, name={}", decisionDefinitionKey, decisionDefinitionName);

            InputStream inputStream = new java.io.ByteArrayInputStream(decisionDefinitionXml.getBytes());

            org.flowable.dmn.api.repository.DeploymentBuilder deploymentBuilder =
                dmnRepositoryService.createDeployment()
                    .addInputStream(decisionDefinitionKey + ".dmn", inputStream)
                    .name(decisionDefinitionName)
                    .key(decisionDefinitionKey);

            String deploymentId = deploymentBuilder.deploy().getId();

            log.info("[DMN决策引擎] 决策定义部署成功: deploymentId={}, key={}", deploymentId, decisionDefinitionKey);
            return deploymentId;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 决策定义部署失败: key={}, error={}", decisionDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("决策定义部署失败", e);
        }
    }

    /**
     * 执行决策
     *
     * @param decisionKey 决策键
     * @param variables 输入变量
     * @return 决策结果
     */
    public DecisionResult executeDecision(String decisionKey, Map<String, Object> variables) {
        try {
            log.info("[DMN决策引擎] 开始执行决策: decisionKey={}", decisionKey);

            // 查找决策定义
            DmnDecision decision = dmnRepositoryService.createDecisionQuery()
                    .decisionKey(decisionKey)
                    .latestVersion()
                    .singleResult();

            if (decision == null) {
                throw new RuntimeException("决策定义不存在: " + decisionKey);
            }

            // 准备输入变量
            if (variables == null) {
                variables = new HashMap<>();
            }

            // 添加执行时间戳
            variables.put("executionTime", LocalDateTime.now());

            // 执行决策
            Map<String, Object> resultVariables = dmnRuntimeService.executeDecisionByKey(
                    decisionKey,
                    decision.getVersion(),
                    variables
            );

            // 创建决策结果
            DecisionResult decisionResult = new DecisionResult();
            decisionResult.setDecisionKey(decisionKey);
            decisionResult.setDecisionName(decision.getName());
            decisionResult.setVariables(resultVariables);

            log.info("[DMN决策引擎] 决策执行成功: decisionKey={}, resultSize={}", decisionKey, resultVariables.size());
            return decisionResult;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 决策执行失败: decisionKey={}, error={}", decisionKey, e.getMessage(), e);
            throw new RuntimeException("决策执行失败", e);
        }
    }

    /**
     * 执行决策表决策
     *
     * @param decisionTableKey 决策表键
     * @param variables 输入变量
     * @return 决策结果
     */
    public Map<String, Object> executeDecisionTable(String decisionTableKey, Map<String, Object> variables) {
        try {
            log.debug("[DMN决策引擎] 开始执行决策表: decisionTableKey={}", decisionTableKey);

            // 查找决策表定义
            DmnDecisionTable decisionTable = dmnRepositoryService.createDecisionTableQuery()
                    .decisionTableKey(decisionTableKey)
                    .latestVersion()
                    .singleResult();

            if (decisionTable == null) {
                throw new RuntimeException("决策表不存在: " + decisionTableKey);
            }

            // 准备输入变量
            if (variables == null) {
                variables = new HashMap<>();
            }

            // 执行决策表
            Map<String, Object> result = dmnRuntimeService.executeDecisionTableByKey(
                    decisionTableKey,
                    decisionTable.getVersion(),
                    variables
            );

            log.debug("[DMN决策引擎] 决策表执行成功: decisionTableKey={}, resultSize={}", decisionTableKey, result.size());
            return result;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 决策表执行失败: decisionTableKey={}, error={}", decisionTableKey, e.getMessage(), e);
            throw new RuntimeException("决策表执行失败", e);
        }
    }

    /**
     * 批量执行决策
     *
     * @param decisionKey 决策键
     * @param batchVariables 批量输入变量列表
     * @return 批量决策结果列表
     */
    public List<DecisionResult> executeBatchDecision(String decisionKey, List<Map<String, Object>> batchVariables) {
        try {
            log.info("[DMN决策引擎] 开始批量执行决策: decisionKey={}, batchSize={}", decisionKey, batchVariables.size());

            List<DecisionResult> results = new ArrayList<>();

            for (int i = 0; i < batchVariables.size(); i++) {
                Map<String, Object> variables = batchVariables.get(i);

                try {
                    DecisionResult result = executeDecision(decisionKey, variables);
                    results.add(result);

                    log.debug("[DMN决策引擎] 批量决策执行成功: decisionKey={}, index={}", decisionKey, i);
                } catch (Exception e) {
                    log.error("[DMN决策引擎] 批量决策执行失败: decisionKey={}, index={}, error={}", decisionKey, i, e.getMessage(), e);

                    // 创建失败的决策结果
                    DecisionResult failedResult = new DecisionResult();
                    failedResult.setDecisionKey(decisionKey);
                    failedResult.setErrorMessage(e.getMessage());
                    failedResult.setSuccess(false);
                    results.add(failedResult);
                }
            }

            log.info("[DMN决策引擎] 批量决策执行完成: decisionKey={}, total={}, success={}, failed={}",
                    decisionKey, results.size(),
                    results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum(),
                    results.stream().mapToInt(r -> r.isSuccess() ? 0 : 1).sum());

            return results;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 批量决策执行失败: decisionKey={}, error={}", decisionKey, e.getMessage(), e);
            throw new RuntimeException("批量决策执行失败", e);
        }
    }

    /**
     * 获取决策定义列表
     *
     * @return 决策定义列表
     */
    public List<DmnDecision> getDecisionDefinitions() {
        try {
            log.debug("[DMN决策引擎] 获取决策定义列表");

            List<DmnDecision> decisions = dmnRepositoryService.createDecisionQuery()
                    .latestVersion()
                    .orderByDecisionVersion().desc()
                    .list();

            log.debug("[DMN决策引擎] 获取决策定义列表完成: count={}", decisions.size());
            return decisions;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 获取决策定义列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取决策定义列表失败", e);
        }
    }

    /**
     * 获取决策表定义列表
     *
     * @return 决策表定义列表
     */
    public List<DmnDecisionTable> getDecisionTableDefinitions() {
        try {
            log.debug("[DMN决策引擎] 获取决策表定义列表");

            List<DmnDecisionTable> decisionTables = dmnRepositoryService.createDecisionTableQuery()
                    .latestVersion()
                    .orderByDecisionTableVersion().desc()
                    .list();

            log.debug("[DMN决策引擎] 获取决策表定义列表完成: count={}", decisionTables.size());
            return decisionTables;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 获取决策表定义列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取决策表定义列表失败", e);
        }
    }

    /**
     * 验证决策定义
     *
     * @param decisionDefinitionXml 决策定义XML
     * @return 验证结果
     */
    public boolean validateDecisionDefinition(String decisionDefinitionXml) {
        try {
            log.debug("[DMN决策引擎] 开始验证决策定义");

            // 使用DMN引擎验证决策定义
            DmnDefinition dmnDefinition = dmnEngine.parseDecisionDefinition(
                    new java.io.InputStreamReader(new java.io.ByteArrayInputStream(decisionDefinitionXml.getBytes()))
            );

            boolean isValid = dmnDefinition != null && !dmnDefinition.getDecisions().isEmpty();

            log.debug("[DMN决策引擎] 决策定义验证完成: valid={}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 决策定义验证失败: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取决策执行历史
     *
     * @param decisionKey 决策键
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 决策执行历史列表
     */
    public List<org.flowable.dmn.api.history.HistoricDecisionExecution> getDecisionHistory(
            String decisionKey, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("[DMN决策引擎] 获取决策执行历史: decisionKey={}, startTime={}, endTime={}",
                    decisionKey, startTime, endTime);

            org.flowable.dmn.api.history.HistoricDecisionExecutionQuery query = dmnHistoryService.createHistoricDecisionExecutionQuery();

            if (decisionKey != null && !decisionKey.isEmpty()) {
                query.decisionKey(decisionKey);
            }

            if (startTime != null) {
                query.executedAfter(java.util.Date.from(startTime));
            }

            if (endTime != null) {
                query.executedBefore(java.util.Date.from(endTime));
            }

            query.orderByExecutionStartTime().desc();

            List<org.flowable.dmn.api.history.HistoricDecisionExecution> history = query.list();

            log.debug("[DMN决策引擎] 获取决策执行历史完成: decisionKey={}, count={}", decisionKey, history.size());
            return history;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 获取决策执行历史失败: decisionKey={}, error={}", decisionKey, e.getMessage(), e);
            throw new RuntimeException("获取决策执行历史失败", e);
        }
    }

    /**
     * 获取决策统计信息
     *
     * @param decisionKey 决策键
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public Map<String, Object> getDecisionStatistics(String decisionKey, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.debug("[DMN决策引擎] 获取决策统计信息: decisionKey={}", decisionKey);

            Map<String, Object> statistics = new HashMap<>();

            // 获取执行历史
            List<org.flowable.dmn.api.history.HistoricDecisionExecution> history =
                getDecisionHistory(decisionKey, startTime, endTime);

            // 统计执行次数
            statistics.put("executionCount", history.size());

            // 统计平均执行时间
            if (!history.isEmpty()) {
                double avgExecutionTime = history.stream()
                        .mapToLong(h -> h.getExecutionDuration() != null ? h.getExecutionDuration() : 0L)
                        .average()
                        .orElse(0.0);
                statistics.put("averageExecutionTime", avgExecutionTime);

                // 统计最大执行时间
                long maxExecutionTime = history.stream()
                        .mapToLong(h -> h.getExecutionDuration() != null ? h.getExecutionDuration() : 0L)
                        .max()
                        .orElse(0L);
                statistics.put("maxExecutionTime", maxExecutionTime);

                // 统计最小执行时间
                long minExecutionTime = history.stream()
                        .mapToLong(h -> h.getExecutionDuration() != null ? h.getExecutionDuration() : 0L)
                        .min()
                        .orElse(0L);
                statistics.put("minExecutionTime", minExecutionTime);
            }

            // 统计执行成功率
            long successCount = history.stream()
                    .mapToLong(h -> h.getExecutionResult() != null ? 1 : 0)
                    .sum();
            double successRate = history.isEmpty() ? 0.0 : (double) successCount / history.size() * 100;
            statistics.put("successRate", successRate);

            log.debug("[DMN决策引擎] 获取决策统计信息完成: decisionKey={}, stats={}", decisionKey, statistics);
            return statistics;

        } catch (Exception e) {
            log.error("[DMN决策引擎] 获取决策统计信息失败: decisionKey={}, error={}", decisionKey, e.getMessage(), e);
            throw new RuntimeException("获取决策统计信息失败", e);
        }
    }
}