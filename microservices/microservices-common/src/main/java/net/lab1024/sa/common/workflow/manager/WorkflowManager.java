package net.lab1024.sa.common.workflow.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.WorkflowException;
import net.lab1024.sa.common.workflow.engine.WorkflowEngine;
import net.lab1024.sa.common.workflow.executor.NodeExecutor;
import net.lab1024.sa.common.workflow.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流管理器
 * <p>
 * 统一管理工作流引擎、流程定义和实例
 * 提供工作流的注册、启动、监控、管理等功能
 * 集成缓存和监控支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class WorkflowManager {

    private final WorkflowEngine workflowEngine;
    private final Map<String, WorkflowDefinition> workflowDefinitions = new HashMap<>();
    private final Map<String, WorkflowTemplate> workflowTemplates = new HashMap<>();

    // 构造函数注入依赖
    public WorkflowManager(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
        initializeBuiltinWorkflows();
        initializeBuiltinTemplates();
    }

    /**
     * 注册工作流定义
     */
    public void registerWorkflowDefinition(WorkflowDefinition definition) {
        workflowDefinitions.put(definition.getId(), definition);
        workflowEngine.registerWorkflow(definition);

        log.info("[工作流管理] 注册工作流定义: {} ({})", definition.getName(), definition.getId());
    }

    /**
     * 启动工作流
     */
    public WorkflowInstance startWorkflow(String workflowId, Map<String, Object> initialData) {
        return workflowEngine.startWorkflow(workflowId, initialData);
    }

    /**
     * 继续工作流
     */
    public void continueWorkflow(String instanceId, Map<String, Object> executionData) {
        workflowEngine.continueWorkflow(instanceId, executionData);
    }

    /**
     * 获取工作流状态
     */
    public WorkflowInstanceStatus getWorkflowStatus(String instanceId) {
        return workflowEngine.getWorkflowStatus(instanceId);
    }

    /**
     * 根据模板创建并启动工作流
     */
    public WorkflowInstance startWorkflowFromTemplate(String templateId, Map<String, Object> templateParams) {
        WorkflowTemplate template = workflowTemplates.get(templateId);
        if (template == null) {
            throw new WorkflowException("工作流模板不存在: " + templateId);
        }

        // 基于模板创建工作流定义
        WorkflowDefinition definition = createWorkflowFromTemplate(template, templateParams);

        // 注册临时工作流定义
        String tempWorkflowId = "temp_" + templateId + "_" + System.currentTimeMillis();
        definition.setId(tempWorkflowId);
        registerWorkflowDefinition(definition);

        // 准备初始数据
        Map<String, Object> initialData = new HashMap<>(templateParams);
        initialData.put("templateId", templateId);
        initialData.put("templateName", template.getName());

        return startWorkflow(tempWorkflowId, initialData);
    }

    /**
     * 基于模板创建工作流定义
     */
    private WorkflowDefinition createWorkflowFromTemplate(WorkflowTemplate template, Map<String, Object> params) {
        WorkflowDefinition definition = new WorkflowDefinition()
                .setName(template.getName() + " - " + LocalDateTime.now())
                .setDescription("基于模板创建: " + template.getDescription())
                .setCategory(template.getCategory())
                .setGlobalConfig(new HashMap<>(template.getGlobalConfig()));

        // 创建工作流节点
        List<WorkflowNode> nodes = new ArrayList<>();

        // 开始节点
        WorkflowNode startNode = createStartNode();
        nodes.add(startNode);

        // 模板定义的节点
        for (WorkflowTemplate.TemplateNode templateNode : template.getNodes()) {
            WorkflowNode node = createNodeFromTemplate(templateNode, params);
            nodes.add(node);
        }

        // 结束节点
        WorkflowNode endNode = createEndNode();
        nodes.add(endNode);

        definition.setNodes(nodes);
        return definition;
    }

    /**
     * 创建开始节点
     */
    private WorkflowNode createStartNode() {
        return new WorkflowNode()
                .setId("start")
                .setName("开始")
                .setType("notification")
                .setDescription("工作流开始节点")
                .setAsync(false)
                .setContinueOnError(false)
                .addConfig("template", "workflow_started");
    }

    /**
     * 创建结束节点
     */
    private WorkflowNode createEndNode() {
        return new WorkflowNode()
                .setId("end")
                .setName("结束")
                .setType("notification")
                .setDescription("工作流结束节点")
                .setAsync(false)
                .setContinueOnError(false)
                .addConfig("template", "workflow_completed");
    }

    /**
     * 从模板节点创建工作流节点
     */
    private WorkflowNode createNodeFromTemplate(WorkflowTemplate.TemplateNode templateNode, Map<String, Object> params) {
        WorkflowNode node = new WorkflowNode()
                .setId(templateNode.getId())
                .setName(templateNode.getName())
                .setType(templateNode.getType())
                .setDescription(templateNode.getDescription())
                .setAsync(templateNode.getAsync() != null ? templateNode.getAsync() : false)
                .setContinueOnError(templateNode.getContinueOnError() != null ? templateNode.getContinueOnError() : false)
                .setTimeoutSeconds(templateNode.getTimeoutSeconds());

        // 复制配置并处理参数替换
        Map<String, Object> config = new HashMap<>(templateNode.getConfig());
        processTemplateParameters(config, params);
        node.setConfig(config);

        return node;
    }

    /**
     * 处理模板参数替换
     */
    private void processTemplateParameters(Map<String, Object> config, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    value = value.replace("${" + param.getKey() + "}", param.getValue().toString());
                }
                entry.setValue(value);
            }
        }
    }

    /**
     * 初始化内置工作流
     */
    private void initializeBuiltinWorkflows() {
        // 用户审批工作流
        registerUserApprovalWorkflow();

        // 数据同步工作流
        registerDataSyncWorkflow();

        // 设备权限下发工作流
        registerDevicePermissionWorkflow();

        log.info("[工作流管理] 内置工作流初始化完成, 数量: {}", workflowDefinitions.size());
    }

    /**
     * 注册用户审批工作流
     */
    private void registerUserApprovalWorkflow() {
        WorkflowDefinition approvalWorkflow = new WorkflowDefinition()
                .setId("user_approval")
                .setName("用户审批工作流")
                .setDescription("用户权限申请和审批流程")
                .setCategory("approval");

        List<WorkflowNode> nodes = new ArrayList<>();

        // 1. 数据验证节点
        WorkflowNode validationNode = new WorkflowNode()
                .setId("data_validation")
                .setName("数据验证")
                .setType("data_process")
                .setDescription("验证申请数据的完整性和合法性")
                .addConfig("processType", "validate")
                .addConfig("validation", Map.of(
                    "userId", Map.of("rule", "required"),
                    "permissionType", Map.of("rule", "required"),
                    "areaId", Map.of("rule", "required")
                ));
        nodes.add(validationNode);

        // 2. 权限检查节点
        WorkflowNode permissionCheck = new WorkflowNode()
                .setId("permission_check")
                .setName("权限检查")
                .setType("condition")
                .setDescription("检查用户是否已有相关权限")
                .addConfig("expression", "data.existingPermission")
                .addConfig("operator", "equals")
                .addConfig("expectedValue", "false")
                .addTransition(new NodeTransition()
                        .setId("to_approval")
                        .setName("去审批")
                        .setTargetNodeId("approval_process")
                        .setPriority(1))
                .addTransition(new NodeTransition()
                        .setId("to_reject")
                        .setName("直接拒绝")
                        .setTargetNodeId("rejection_notification")
                        .setCondition("data.existingPermission.equals.true)")
                        .setPriority(2));
        nodes.add(permissionCheck);

        // 3. 审批处理节点
        WorkflowNode approvalProcess = new WorkflowNode()
                .setId("approval_process")
                .setName("审批处理")
                .setType("service_call")
                .setDescription("执行审批逻辑")
                .addConfig("serviceName", "approvalService")
                .addConfig("methodName", "processApproval")
                .setAsync(true)
                .setTimeoutSeconds(300);
        nodes.add(approvalProcess);

        // 4. 拒绝通知节点
        WorkflowNode rejectionNotification = new WorkflowNode()
                .setId("rejection_notification")
                .setName("拒绝通知")
                .setType("notification")
                .setDescription("发送拒绝通知")
                .addConfig("type", "email")
                .addConfig("template", "approval_rejected");
        nodes.add(rejectionNotification);

        approvalWorkflow.setNodes(nodes);
        registerWorkflowDefinition(approvalWorkflow);
    }

    /**
     * 注册数据同步工作流
     */
    private void registerDataSyncWorkflow() {
        WorkflowDefinition syncWorkflow = new WorkflowDefinition()
                .setId("data_sync")
                .setName("数据同步工作流")
                .setDescription("多系统间数据同步流程")
                .setCategory("sync");

        List<WorkflowNode> nodes = new ArrayList<>();

        // 1. 数据提取节点
        WorkflowNode extractNode = new WorkflowNode()
                .setId("data_extract")
                .setName("数据提取")
                .setType("service_call")
                .setDescription("从源系统提取数据")
                .addConfig("serviceName", "sourceDataService")
                .addConfig("methodName", "extractData");
        nodes.add(extractNode);

        // 2. 数据转换节点
        WorkflowNode transformNode = new WorkflowNode()
                .setId("data_transform")
                .setName("数据转换")
                .setType("data_process")
                .setDescription("转换数据格式")
                .addConfig("processType", "transform")
                .addConfig("transform", Map.of(
                    "standardizedData", Map.of(
                        "source", "extractedData",
                        "type", "format",
                        "params", Map.of("targetFormat", "json")
                    )
                ));
        nodes.add(transformNode);

        // 3. 并行同步节点
        WorkflowNode parallelSync = new WorkflowNode()
                .setId("parallel_sync")
                .setName("并行同步")
                .setType("parallel")
                .setDescription("并行同步到多个目标系统")
                .addConfig("maxConcurrency", 3)
                .addConfig("failFast", false)
                .addConfig("timeoutSeconds", 600)
                .addConfig("tasks", List.of(
                    Map.of(
                        "id", "sync_target1",
                        "name", "同步到目标系统1",
                        "type", "service_call",
                        "config", Map.of(
                            "serviceName", "targetService1",
                            "methodName", "syncData"
                        )
                    ),
                    Map.of(
                        "id", "sync_target2",
                        "name", "同步到目标系统2",
                        "type", "service_call",
                        "config", Map.of(
                            "serviceName", "targetService2",
                            "methodName", "syncData"
                        )
                    )
                ));
        nodes.add(parallelSync);

        syncWorkflow.setNodes(nodes);
        registerWorkflowDefinition(syncWorkflow);
    }

    /**
     * 注册设备权限下发工作流
     */
    private void registerDevicePermissionWorkflow() {
        WorkflowDefinition deviceWorkflow = new WorkflowDefinition()
                .setId("device_permission")
                .setName("设备权限下发工作流")
                .setDescription("设备权限下发和同步流程")
                .setCategory("device");

        List<WorkflowNode> nodes = new ArrayList<>();

        // 1. 获取区域设备列表
        WorkflowNode getDevicesNode = new WorkflowNode()
                .setId("get_area_devices")
                .setName("获取区域设备")
                .setType("service_call")
                .setDescription("获取区域关联的所有设备")
                .addConfig("serviceName", "areaDeviceService")
                .addConfig("methodName", "getAreaDevices");
        nodes.add(getDevicesNode);

        // 2. 并行下发权限
        WorkflowNode parallelSync = new WorkflowNode()
                .setId("parallel_permission_sync")
                .setName("并行权限下发")
                .setType("parallel")
                .setDescription("并行向设备下发权限信息")
                .addConfig("maxConcurrency", 5)
                .addConfig("timeoutSeconds", 120)
                .addConfig("tasks", List.of()); // 动态生成任务列表
        nodes.add(parallelSync);

        deviceWorkflow.setNodes(nodes);
        registerWorkflowDefinition(deviceWorkflow);
    }

    /**
     * 初始化内置工作流模板
     */
    private void initializeBuiltinTemplates() {
        // 审批流程模板
        WorkflowTemplate approvalTemplate = new WorkflowTemplate()
                .setId("approval_template")
                .setName("审批流程模板")
                .setDescription("标准审批流程模板")
                .setCategory("template");

        List<WorkflowTemplate.TemplateNode> templateNodes = new ArrayList<>();

        templateNodes.add(new WorkflowTemplate.TemplateNode()
                .setId("validation")
                .setName("数据验证")
                .setType("data_process")
                .setDescription("验证申请数据"));

        templateNodes.add(new WorkflowTemplate.TemplateNode()
                .setId("approval")
                .setName("审批处理")
                .setType("service_call")
                .setDescription("执行审批逻辑")
                .setAsync(true)
                .setTimeoutSeconds(300));

        templateNodes.add(new WorkflowTemplate.TemplateNode()
                .setId("notification")
                .setName("结果通知")
                .setType("notification")
                .setDescription("发送审批结果通知"));

        approvalTemplate.setNodes(templateNodes);
        workflowTemplates.put(approvalTemplate.getId(), approvalTemplate);

        log.info("[工作流管理] 内置工作流模板初始化完成, 数量: {}", workflowTemplates.size());
    }

    /**
     * 获取工作流定义列表
     */
    public List<WorkflowDefinition> getWorkflowDefinitions() {
        return new ArrayList<>(workflowDefinitions.values());
    }

    /**
     * 获取工作流模板列表
     */
    public List<WorkflowTemplate> getWorkflowTemplates() {
        return new ArrayList<>(workflowTemplates.values());
    }

    /**
     * 获取运行中的工作流实例
     */
    public List<WorkflowInstance> getRunningInstances() {
        return workflowEngine.getRunningInstances();
    }

    /**
     * 注册节点执行器
     */
    public void registerNodeExecutor(String nodeType, NodeExecutor executor) {
        workflowEngine.registerNodeExecutor(nodeType, executor);
    }

    /**
     * 关闭工作流管理器
     */
    public void shutdown() {
        workflowEngine.shutdown();
        log.info("[工作流管理] 工作流管理器已关闭");
    }
}