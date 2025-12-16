package net.lab1024.sa.oa.workflow.cmmn;

import lombok.extern.slf4j.Slf4j;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.engine.CmmnEngine;
import org.flowable.cmmn.model.CmmnModel;
import org.flowable.cmmn.model.CaseDefinition;
import org.flowable.cmmn.model.PlanItemDefinition;
import org.flowable.cmmn.model.HumanTask;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Flowable CMMN案例管理服务
 * <p>
 * 提供企业级案例管理功能，支持案例定义、案例实例、任务管理、
 * 里程碑跟踪、案例版本控制等高级功能
 * 集成Flowable 6.8.0 CMMN引擎特性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@Service
public class CaseManagementService {

    @Resource
    private CmmnEngine cmmnEngine;

    @Resource
    private CmmnRepositoryService cmmnRepositoryService;

    @Resource
    private CmmnRuntimeService cmmnRuntimeService;

    @Resource
    private CmmnTaskService cmmnTaskService;

    @Resource
    private CmmnHistoryService cmmnHistoryService;

    /**
     * 部署案例定义
     *
     * @param caseDefinitionXml 案例定义XML
     * @param caseDefinitionKey 案例定义键
     * @param caseDefinitionName 案例定义名称
     * @return 部署结果
     */
    public String deployCaseDefinition(String caseDefinitionXml, String caseDefinitionKey, String caseDefinitionName) {
        try {
            log.info("[CMMN案例管理] 开始部署案例定义: key={}, name={}", caseDefinitionKey, caseDefinitionName);

            InputStream inputStream = new java.io.ByteArrayInputStream(caseDefinitionXml.getBytes());

            org.flowable.cmmn.api.repository.DeploymentBuilder deploymentBuilder =
                cmmnRepositoryService.createDeployment()
                    .addInputStream(caseDefinitionKey + ".cmmn", inputStream)
                    .name(caseDefinitionName)
                    .key(caseDefinitionKey);

            String deploymentId = deploymentBuilder.deploy().getId();

            log.info("[CMMN案例管理] 案例定义部署成功: deploymentId={}, key={}", deploymentId, caseDefinitionKey);
            return deploymentId;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 案例定义部署失败: key={}, error={}", caseDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("案例定义部署失败", e);
        }
    }

    /**
     * 启动案例实例
     *
     * @param caseDefinitionKey 案例定义键
     * @param caseName 案例名称
     * @param businessKey 业务键
     * @param variables 案例变量
     * @param startUserId 启动用户ID
     * @return 案例实例ID
     */
    public String startCaseInstance(String caseDefinitionKey, String caseName, String businessKey,
                                   Map<String, Object> variables, String startUserId) {
        try {
            log.info("[CMMN案例管理] 开始启动案例实例: key={}, caseName={}, businessKey={}",
                    caseDefinitionKey, caseName, businessKey);

            // 查找案例定义
            CaseDefinition caseDefinition = cmmnRepositoryService.createCaseDefinitionQuery()
                    .caseDefinitionKey(caseDefinitionKey)
                    .latestVersion()
                    .singleResult();

            if (caseDefinition == null) {
                throw new RuntimeException("案例定义不存在: " + caseDefinitionKey);
            }

            // 准备启动变量
            if (variables == null) {
                variables = new HashMap<>();
            }

            variables.put("caseName", caseName);
            variables.put("businessKey", businessKey);
            variables.put("startUserId", startUserId);
            variables.put("startTime", LocalDateTime.now());

            // 启动案例实例
            String caseInstanceId = cmmnRuntimeService.createCaseInstanceBuilder()
                    .caseDefinitionId(caseDefinition.getId())
                    .caseName(caseName)
                    .businessKey(businessKey)
                    .variables(variables)
                    .start();

            log.info("[CMMN案例管理] 案例实例启动成功: caseInstanceId={}, caseDefinitionId={}",
                    caseInstanceId, caseDefinition.getId());

            return caseInstanceId;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 案例实例启动失败: key={}, error={}", caseDefinitionKey, e.getMessage(), e);
            throw new RuntimeException("案例实例启动失败", e);
        }
    }

    /**
     * 获取案例任务列表
     *
     * @param caseInstanceId 案例实例ID
     * @param assigneeId 指派人ID
     * @return 任务列表
     */
    public List<Task> getCaseTasks(String caseInstanceId, String assigneeId) {
        try {
            log.debug("[CMMN案例管理] 获取案例任务: caseInstanceId={}, assigneeId={}", caseInstanceId, assigneeId);

            org.flowable.task.api.TaskQuery taskQuery = cmmnTaskService.createTaskQuery()
                    .caseInstanceId(caseInstanceId);

            if (assigneeId != null && !assigneeId.isEmpty()) {
                taskQuery.taskAssignee(assigneeId);
            }

            List<Task> tasks = taskQuery.orderByTaskCreateTime().asc().list();

            log.debug("[CMMN案例管理] 获取案例任务完成: caseInstanceId={}, taskCount={}", caseInstanceId, tasks.size());
            return tasks;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 获取案例任务失败: caseInstanceId={}, error={}", caseInstanceId, e.getMessage(), e);
            throw new RuntimeException("获取案例任务失败", e);
        }
    }

    /**
     * 完成案例任务
     *
     * @param taskId 任务ID
     * @param variables 任务变量
     * @param comment 评论
     * @param userId 用户ID
     * @return 完成结果
     */
    public boolean completeCaseTask(String taskId, Map<String, Object> variables, String comment, String userId) {
        try {
            log.info("[CMMN案例管理] 开始完成案例任务: taskId={}, userId={}", taskId, userId);

            Task task = cmmnTaskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskId);
            }

            // 添加评论
            if (comment != null && !comment.isEmpty()) {
                cmmnTaskService.addComment(taskId, comment);
            }

            // 设置任务变量
            if (variables != null && !variables.isEmpty()) {
                cmmnTaskService.setVariables(taskId, variables);
            }

            // 添加任务完成人
            cmmnTaskService.setOwner(taskId, userId);

            // 完成任务
            cmmnTaskService.complete(taskId);

            log.info("[CMMN案例管理] 案例任务完成成功: taskId={}, caseInstanceId={}", taskId, task.getCaseInstanceId());
            return true;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 完成案例任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new RuntimeException("完成案例任务失败", e);
        }
    }

    /**
     * 获取案例里程碑
     *
     * @param caseInstanceId 案例实例ID
     * @return 里程碑列表
     */
    public Map<String, Object> getCaseMilestones(String caseInstanceId) {
        try {
            log.debug("[CMMN案例管理] 获取案例里程碑: caseInstanceId={}", caseInstanceId);

            // 获取案例实例的里程碑状态
            Map<String, Object> milestones = new HashMap<>();

            // 这里可以根据具体的案例模型获取里程碑信息
            // 示例：获取计划项的状态
            List<org.flowable.cmmn.api.runtime.PlanItemInstance> planItemInstances =
                cmmnRuntimeService.createPlanItemInstanceQuery()
                    .caseInstanceId(caseInstanceId)
                    .list();

            for (org.flowable.cmmn.api.runtime.PlanItemInstance planItem : planItemInstances) {
                if (planItem.getName() != null && planItem.getName().endsWith("Milestone")) {
                    milestones.put(planItem.getName(), Map.of(
                        "id", planItem.getId(),
                        "name", planItem.getName(),
                        "state", planItem.getState(),
                        "createTime", planItem.getCreateTime(),
                        "completedTime", planItem.getCompleteTime()
                    ));
                }
            }

            log.debug("[CMMN案例管理] 获取案例里程碑完成: caseInstanceId={}, milestoneCount={}",
                    caseInstanceId, milestones.size());

            return milestones;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 获取案例里程碑失败: caseInstanceId={}, error={}", caseInstanceId, e.getMessage(), e);
            throw new RuntimeException("获取案例里程碑失败", e);
        }
    }

    /**
     * 获取案例定义列表
     *
     * @return 案例定义列表
     */
    public List<CaseDefinition> getCaseDefinitions() {
        try {
            log.debug("[CMMN案例管理] 获取案例定义列表");

            List<CaseDefinition> caseDefinitions = cmmnRepositoryService.createCaseDefinitionQuery()
                    .latestVersion()
                    .orderByCaseDefinitionVersion().desc()
                    .list();

            log.debug("[CMMN案例管理] 获取案例定义列表完成: count={}", caseDefinitions.size());
            return caseDefinitions;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 获取案例定义列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取案例定义列表失败", e);
        }
    }

    /**
     * 获取案例实例列表
     *
     * @param caseDefinitionKey 案例定义键
     * @param businessKey 业务键
     * @return 案例实例列表
     */
    public List<org.flowable.cmmn.api.runtime.CaseInstance> getCaseInstances(String caseDefinitionKey, String businessKey) {
        try {
            log.debug("[CMMN案例管理] 获取案例实例列表: caseDefinitionKey={}, businessKey={}", caseDefinitionKey, businessKey);

            org.flowable.cmmn.api.runtime.CaseInstanceQuery query = cmmnRuntimeService.createCaseInstanceQuery();

            if (caseDefinitionKey != null && !caseDefinitionKey.isEmpty()) {
                query.caseDefinitionKey(caseDefinitionKey);
            }

            if (businessKey != null && !businessKey.isEmpty()) {
                query.businessKey(businessKey);
            }

            query.orderByStartTime().desc();

            List<org.flowable.cmmn.api.runtime.CaseInstance> caseInstances = query.list();

            log.debug("[CMMN案例管理] 获取案例实例列表完成: count={}", caseInstances.size());
            return caseInstances;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 获取案例实例列表失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取案例实例列表失败", e);
        }
    }

    /**
     * 激活案例实例
     *
     * @param caseInstanceId 案例实例ID
     * @param userId 操作用户ID
     * @return 激活结果
     */
    public boolean activateCaseInstance(String caseInstanceId, String userId) {
        try {
            log.info("[CMMN案例管理] 开始激活案例实例: caseInstanceId={}, userId={}", caseInstanceId, userId);

            cmmnRuntimeService.activateCaseInstance(caseInstanceId);

            log.info("[CMMN案例管理] 案例实例激活成功: caseInstanceId={}", caseInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 案例实例激活失败: caseInstanceId={}, error={}", caseInstanceId, e.getMessage(), e);
            throw new RuntimeException("案例实例激活失败", e);
        }
    }

    /**
     * 暂停案例实例
     *
     * @param caseInstanceId 案例实例ID
     * @param userId 操作用户ID
     * @return 暂停结果
     */
    public boolean suspendCaseInstance(String caseInstanceId, String userId) {
        try {
            log.info("[CMMN案例管理] 开始暂停案例实例: caseInstanceId={}, userId={}", caseInstanceId, userId);

            cmmnRuntimeService.suspendCaseInstance(caseInstanceId);

            log.info("[CMMN案例管理] 案例实例暂停成功: caseInstanceId={}", caseInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 案例实例暂停失败: caseInstanceId={}, error={}", caseInstanceId, e.getMessage(), e);
            throw new RuntimeException("案例实例暂停失败", e);
        }
    }

    /**
     * 终止案例实例
     *
     * @param caseInstanceId 案例实例ID
     * @param terminationReason 终止原因
     * @param userId 操作用户ID
     * @return 终止结果
     */
    public boolean terminateCaseInstance(String caseInstanceId, String terminationReason, String userId) {
        try {
            log.info("[CMMN案例管理] 开始终止案例实例: caseInstanceId={}, reason={}, userId={}",
                    caseInstanceId, terminationReason, userId);

            cmmnRuntimeService.terminateCaseInstance(caseInstanceId, terminationReason);

            log.info("[CMMN案例管理] 案例实例终止成功: caseInstanceId={}", caseInstanceId);
            return true;

        } catch (Exception e) {
            log.error("[CMMN案例管理] 案例实例终止失败: caseInstanceId={}, error={}", caseInstanceId, e.getMessage(), e);
            throw new RuntimeException("案例实例终止失败", e);
        }
    }
}