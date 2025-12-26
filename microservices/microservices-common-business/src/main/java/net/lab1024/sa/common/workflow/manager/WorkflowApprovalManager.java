package net.lab1024.sa.common.workflow.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流审批管理器
 * <p>
 * 负责工作流审批流程的启动和管理
 * 严格遵循CLAUDE.md规范：Manager类是纯Java类，通过构造函数注入依赖
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class WorkflowApprovalManager {

    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 构造函数注入依赖
     *
     * @param gatewayServiceClient  网关服务客户端
     */
    public WorkflowApprovalManager(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 启动审批流程
     *
     * @param processDefinitionId 流程定义ID（Long类型，来自WorkflowDefinitionConstants）
     * @param businessCode         业务编码
     * @param businessTitle        业务标题
     * @param userId               用户ID
     * @param businessType         业务类型（BusinessTypeEnum.name()）
     * @param formData             表单数据
     * @param variables            流程变量
     * @return 流程实例ID
     */
    public Long startApprovalProcess(
            Long processDefinitionId,
            String businessCode,
            String businessTitle,
            Long userId,
            String businessType,
            Map<String, Object> formData,
            Map<String, Object> variables) {

        log.info("[工作流审批] 启动审批流程: processDefinitionId={}, businessCode={}, businessTitle={}, userId={}, businessType={}",
                processDefinitionId, businessCode, businessTitle, userId, businessType);

        try {
            // 构建请求参数
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("definitionId", processDefinitionId);
            requestData.put("businessKey", businessCode);
            requestData.put("instanceName", businessTitle);
            requestData.put("businessType", businessType);
            requestData.put("formData", formData != null ? formData : new HashMap<>());

            // 合并流程变量
            Map<String, Object> mergedVariables = new HashMap<>();
            if (variables != null) {
                mergedVariables.putAll(variables);
            }
            mergedVariables.put("initiatorId", userId);
            mergedVariables.put("businessCode", businessCode);
            mergedVariables.put("businessType", businessType);
            requestData.put("variables", mergedVariables);

            // 调用OA服务的流程启动接口
            Object result = gatewayServiceClient.callOAService(
                    "/api/v1/workflow/engine/start",
                    HttpMethod.POST,
                    requestData,
                    Long.class
            );

            if (result != null) {
                Long instanceId = (Long) result;
                log.info("[工作流审批] 审批流程启动成功: processDefinitionId={}, businessCode={}, instanceId={}",
                        processDefinitionId, businessCode, instanceId);
                return instanceId;
            } else {
                log.error("[工作流审批] 审批流程启动失败: processDefinitionId={}, businessCode={}, message=未知错误",
                        processDefinitionId, businessCode);
                throw new RuntimeException("启动审批流程失败: 未知错误");
            }

        } catch (Exception e) {
            log.error("[工作流审批] 启动审批流程异常: processDefinitionId={}, businessCode={}, error={}",
                    processDefinitionId, businessCode, e.getMessage(), e);
            throw new RuntimeException("启动审批流程失败: " + e.getMessage(), e);
        }
    }
}