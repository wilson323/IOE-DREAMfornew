package net.lab1024.sa.oa.workflow.cmmn;

import jakarta.annotation.Resource;
import org.flowable.cmmn.api.CmmnRepositoryService;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.repository.CaseDefinition;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Flowable 7.2.0 CMMN案例管理服务
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-17
 */
@Service
public class CaseManagementService {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementService.class);

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
     */
    public String deployCaseDefinition(String caseDefinitionXml, String caseDefinitionKey, String caseDefinitionName) {
        log.info("[CMMN案例管理] 部署案例定义: key={}, name={}", caseDefinitionKey, caseDefinitionName);

        var deployment = cmmnRepositoryService.createDeployment()
                .name(caseDefinitionName)
                .key(caseDefinitionKey)
                .addString(caseDefinitionKey + ".cmmn", caseDefinitionXml)
                .deploy();

        log.info("[CMMN案例管理] 案例定义部署成功: deploymentId={}", deployment.getId());
        return deployment.getId();
    }

    /**
     * 启动案例实例
     */
    public String startCaseInstance(String caseDefinitionKey, String caseName, String businessKey,
                                    Map<String, Object> variables, String startUserId) {
        log.info("[CMMN案例管理] 启动案例实例: key={}, businessKey={}", caseDefinitionKey, businessKey);

        CaseDefinition caseDefinition = cmmnRepositoryService.createCaseDefinitionQuery()
                .caseDefinitionKey(caseDefinitionKey)
                .latestVersion()
                .singleResult();

        if (caseDefinition == null) {
            throw new RuntimeException("案例定义不存在: " + caseDefinitionKey);
        }

        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("caseName", caseName);
        variables.put("businessKey", businessKey);
        variables.put("startUserId", startUserId);
        variables.put("startTime", LocalDateTime.now());

        CaseInstance caseInstance = cmmnRuntimeService.createCaseInstanceBuilder()
                .caseDefinitionId(caseDefinition.getId())
                .name(caseName)
                .businessKey(businessKey)
                .variables(variables)
                .start();

        log.info("[CMMN案例管理] 案例实例启动成功: caseInstanceId={}", caseInstance.getId());
        return caseInstance.getId();
    }

    /**
     * 获取案例任务
     */
    public List<Task> getCaseTasks(String caseInstanceId) {
        return cmmnTaskService.createTaskQuery()
                .caseInstanceId(caseInstanceId)
                .orderByTaskCreateTime().desc()
                .list();
    }

    /**
     * 完成案例任务
     */
    public void completeCaseTask(String taskId, Map<String, Object> variables) {
        log.info("[CMMN案例管理] 完成案例任务: taskId={}", taskId);
        cmmnTaskService.complete(taskId, variables);
        log.info("[CMMN案例管理] 案例任务完成成功");
    }

    /**
     * 获取案例定义列表
     */
    public List<CaseDefinition> getCaseDefinitions() {
        return cmmnRepositoryService.createCaseDefinitionQuery()
                .latestVersion()
                .orderByCaseDefinitionName().asc()
                .list();
    }

    /**
     * 终止案例实例
     */
    public void terminateCaseInstance(String caseInstanceId) {
        log.info("[CMMN案例管理] 终止案例实例: caseInstanceId={}", caseInstanceId);
        cmmnRuntimeService.terminateCaseInstance(caseInstanceId);
        log.info("[CMMN案例管理] 案例实例已终止");
    }

    /**
     * 删除案例部署
     */
    public void deleteDeployment(String deploymentId) {
        log.info("[CMMN案例管理] 删除案例部署: deploymentId={}", deploymentId);
        cmmnRepositoryService.deleteDeployment(deploymentId, true);
    }
}
