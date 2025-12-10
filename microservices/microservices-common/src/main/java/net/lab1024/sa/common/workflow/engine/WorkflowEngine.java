package net.lab1024.sa.common.workflow.engine;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.WorkflowException;
import net.lab1024.sa.common.workflow.executor.NodeExecutor;
import net.lab1024.sa.common.workflow.executor.NodeExecutionResult;
import net.lab1024.sa.common.workflow.executor.NodeExecutionContext;
import net.lab1024.sa.common.workflow.executor.impl.SystemExecutor;
import net.lab1024.sa.common.workflow.executor.impl.ApprovalExecutor;
import net.lab1024.sa.common.workflow.executor.impl.NotificationExecutor;
import net.lab1024.sa.common.workflow.executor.impl.ConditionExecutor;
import net.lab1024.sa.common.workflow.manager.ExpressionEngineManager;
import net.lab1024.sa.common.workflow.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 业务流程引擎
 * <p>
 * 提供企业级业务流程编排和执行能力
 * 支持流程定义、节点管理、条件路由、异步执行
 * 实现业务逻辑的标准化和可复用性
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class WorkflowEngine {

    // 流程定义缓存
    private final Map<String, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();

    // 运行时实例管理
    private final Map<String, WorkflowInstance> runningInstances = new ConcurrentHashMap<>();

    // 节点执行器注册表
    private final Map<String, NodeExecutor> nodeExecutors = new ConcurrentHashMap<>();

    // 表达式引擎管理器
    private final ExpressionEngineManager expressionEngineManager;

    // 网关服务客户端（可选，用于执行器调用其他服务）
    private final net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    // 异步执行线程池
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(10);

    /**
     * 构造函数（无依赖版本，执行器功能受限）
     */
    public WorkflowEngine() {
        this(null);
    }

    /**
     * 构造函数（带依赖注入）
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端（可选）
     */
    public WorkflowEngine(net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
        this.expressionEngineManager = new ExpressionEngineManager(gatewayServiceClient);
        initializeBuiltInExecutors();
        log.info("[流程引擎] 工作流引擎初始化完成");
    }

    /**
     * 注册流程定义
     */
    public void registerWorkflow(WorkflowDefinition definition) {
        workflowDefinitions.put(definition.getId(), definition);
        log.info("[流程引擎] 注册流程定义: {}", definition.getName());
    }

    /**
     * 启动流程实例
     */
    public WorkflowInstance startWorkflow(String workflowId, Map<String, Object> initialData) {
        WorkflowDefinition definition = workflowDefinitions.get(workflowId);
        if (definition == null) {
            throw new WorkflowException("流程定义不存在: " + workflowId);
        }

        WorkflowInstance instance = createWorkflowInstance(definition, initialData);
        runningInstances.put(instance.getId(), instance);

        // 开始执行第一个节点
        executeNextNode(instance);

        log.info("[流程引擎] 启动流程实例: workflowId={}, instanceId={}",
                workflowId, instance.getId());

        return instance;
    }

    /**
     * 继续执行流程
     */
    public void continueWorkflow(String instanceId, Map<String, Object> executionData) {
        WorkflowInstance instance = runningInstances.get(instanceId);
        if (instance == null) {
            throw new WorkflowException("流程实例不存在: " + instanceId);
        }

        // 更新执行数据
        instance.setExecutionData(executionData);

        // 继续执行下一个节点
        executeNextNode(instance);

        log.debug("[流程引擎] 继续执行流程: instanceId={}", instanceId);
    }

    /**
     * 获取流程实例状态
     */
    public WorkflowInstanceStatus getWorkflowStatus(String instanceId) {
        WorkflowInstance instance = runningInstances.get(instanceId);
        if (instance == null) {
            return WorkflowInstanceStatus.NOT_FOUND;
        }

        return instance.getStatus();
    }

    /**
     * 注册节点执行器
     */
    public void registerNodeExecutor(String nodeType, NodeExecutor executor) {
        nodeExecutors.put(nodeType, executor);
        log.info("[流程引擎] 注册节点执行器: nodeType={}", nodeType);
    }

    /**
     * 创建流程实例
     */
    private WorkflowInstance createWorkflowInstance(WorkflowDefinition definition, Map<String, Object> initialData) {
        return new WorkflowInstance()
                .setId(UUID.randomUUID().toString())
                .setWorkflowId(definition.getId())
                .setWorkflowName(definition.getName())
                .setStatus(WorkflowInstanceStatus.RUNNING)
                .setStartTime(LocalDateTime.now())
                .setExecutionData(new HashMap<>(initialData))
                .setCurrentNodeIndex(0);
    }

    /**
     * 执行下一个节点
     */
    private void executeNextNode(WorkflowInstance instance) {
        WorkflowDefinition definition = workflowDefinitions.get(instance.getWorkflowId());
        List<WorkflowNode> nodes = definition.getNodes();

        if (instance.getCurrentNodeIndex() >= nodes.size()) {
            // 流程结束
            completeWorkflow(instance);
            return;
        }

        WorkflowNode currentNode = nodes.get(instance.getCurrentNodeIndex());

        try {
            executeNode(instance, currentNode);
        } catch (Exception e) {
            log.error("[流程引擎] 节点执行异常: nodeId={}, instanceId={}",
                    currentNode.getId(), instance.getId(), e);

            // 流程异常终止
            terminateWorkflow(instance, e.getMessage());
        }
    }

    /**
     * 执行节点
     */
    private void executeNode(WorkflowInstance instance, WorkflowNode node) {
        log.info("[流程引擎] 执行节点: nodeId={}, instanceId={}, nodeType={}",
                node.getId(), instance.getId(), node.getType());

        instance.setCurrentNodeId(node.getId());
        instance.setCurrentNodeName(node.getName());

        NodeExecutor executor = nodeExecutors.get(node.getType());
        if (executor == null) {
            throw new WorkflowException("节点执行器不存在: " + node.getType());
        }

        // 准备节点执行上下文
        Map<String, Object> executionData = new java.util.HashMap<>();
        NodeExecutionContext context = new NodeExecutionContext()
                .setInstanceId(instance.getId())
                .setExecutionData(executionData)
                .setNodeConfig(node.getConfig());

        // 根据节点类型执行
        if (node.isAsync()) {
            asyncExecutor.execute(() -> {
                try {
                    NodeExecutionResult result = executor.execute(context);
                    handleNodeExecutionComplete(instance, node, context, result);
                } catch (Exception e) {
                    handleNodeExecutionError(instance, node, e);
                }
            });
        } else {
            try {
                NodeExecutionResult result = executor.execute(context);
                handleNodeExecutionComplete(instance, node, context, result);
            } catch (Exception e) {
                handleNodeExecutionError(instance, node, e);
            }
        }
    }

    /**
     * 处理节点执行完成
     */
    private void handleNodeExecutionComplete(WorkflowInstance instance, WorkflowNode node, NodeExecutionContext context, NodeExecutionResult result) {
        log.info("[流程引擎] 节点执行完成: nodeId={}, instanceId={}",
                node.getId(), instance.getId());

        // 更新执行数据
        if (context.getExecutionData() != null) {
            instance.getExecutionData().putAll(context.getExecutionData());
        }
        if (result.getOutputData() != null) {
            instance.getExecutionData().putAll(result.getOutputData());
        }

        // 检查条件路由
        String nextNodeId = evaluateNextNode(instance, node, context);
        if (nextNodeId != null) {
            // 有下一个节点，继续执行
            instance.setCurrentNodeIndex(findNodeIndex(instance.getWorkflowId(), nextNodeId));
            executeNextNode(instance);
        } else {
            // 使用默认的下一个节点
            instance.setCurrentNodeIndex(instance.getCurrentNodeIndex() + 1);
            executeNextNode(instance);
        }
    }

    /**
     * 处理节点执行错误
     */
    private void handleNodeExecutionError(WorkflowInstance instance, WorkflowNode node, Exception e) {
        log.error("[流程引擎] 节点执行失败: nodeId={}, instanceId={}, error={}",
                node.getId(), instance.getId(), e.getMessage());

        // 根据节点配置决定是否继续
        if (node.isContinueOnError()) {
            instance.setCurrentNodeIndex(instance.getCurrentNodeIndex() + 1);
            executeNextNode(instance);
        } else {
            terminateWorkflow(instance, e.getMessage());
        }
    }

    /**
     * 评估下一个节点
     */
    private String evaluateNextNode(WorkflowInstance instance, WorkflowNode node, NodeExecutionContext context) {
        List<NodeTransition> transitions = node.getTransitions();
        if (transitions == null || transitions.isEmpty()) {
            return null;
        }

        for (NodeTransition transition : transitions) {
            if (evaluateCondition(transition.getCondition(), instance.getExecutionData())) {
                return transition.getTargetNodeId();
            }
        }

        return null;
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        // ✅ OpenSpec规范执行：集成Aviator表达式引擎
        // 替换简化的键值比较，使用企业级表达式引擎

        if (condition == null || condition.isEmpty()) {
            return true;
        }

        try {
            // 使用Aviator表达式引擎执行复杂条件
            ExpressionEngineManager expressionManager = getExpressionEngineManager();

            // 安全检查
            if (!expressionManager.isExpressionSafe(condition)) {
                log.warn("[工作流引擎] 检测到不安全的表达式: {}", condition);
                return false;
            }

            return expressionManager.executeBooleanExpression(condition, data);

        } catch (Exception e) {
            log.error("[工作流引擎] 条件表达式评估失败: condition={}, data={}, error={}",
                    condition, data, e.getMessage(), e);

            // 降级处理：尝试简单的字符串匹配
            return fallbackEvaluateCondition(condition, data);
        }
    }

    /**
     * 降级处理：简单的条件评估
     */
    private boolean fallbackEvaluateCondition(String condition, Map<String, Object> data) {
        try {
            // 简化的条件解析：key.operator.value
            String[] parts = condition.split("\\.");
            if (parts.length != 3) {
                return true;
            }

            String key = parts[0];
            String operator = parts[1];
            String expectedValue = parts[2];

            Object actualValue = data.get(key);
            if (actualValue == null) {
                return false;
            }

            return switch (operator) {
                case "equals" -> actualValue.toString().equals(expectedValue);
                case "not_equals" -> !actualValue.toString().equals(expectedValue);
                case "greater" -> Double.parseDouble(actualValue.toString()) > Double.parseDouble(expectedValue);
                case "less" -> Double.parseDouble(actualValue.toString()) < Double.parseDouble(expectedValue);
                default -> true;
            };
        } catch (Exception e) {
            log.warn("[流程引擎] 条件评估失败: condition={}, error={}", condition, e.getMessage());
            return true; // 默认通过
        }
    }

    /**
     * 查找节点索引
     */
    private int findNodeIndex(String workflowId, String nodeId) {
        WorkflowDefinition definition = workflowDefinitions.get(workflowId);
        List<WorkflowNode> nodes = definition.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(nodeId)) {
                return i;
            }
        }

        throw new WorkflowException("节点不存在: " + nodeId);
    }

    /**
     * 完成流程
     */
    private void completeWorkflow(WorkflowInstance instance) {
        instance.setStatus(WorkflowInstanceStatus.COMPLETED);
        instance.setEndTime(LocalDateTime.now());

        runningInstances.remove(instance.getId());

        log.info("[流程引擎] 流程完成: workflowId={}, instanceId={}, duration={}ms",
                instance.getWorkflowId(), instance.getId(),
                java.time.Duration.between(instance.getStartTime(), instance.getEndTime()).toMillis());
    }

    /**
     * 终止流程
     */
    private void terminateWorkflow(WorkflowInstance instance, String reason) {
        instance.setStatus(WorkflowInstanceStatus.TERMINATED);
        instance.setEndTime(LocalDateTime.now());
        instance.setErrorMessage(reason);

        runningInstances.remove(instance.getId());

        log.warn("[流程引擎] 流程终止: workflowId={}, instanceId={}, reason={}",
                instance.getWorkflowId(), instance.getId(), reason);
    }

    /**
     * 初始化内置执行器
     * <p>
     * 注册所有内置节点执行器，支持依赖注入
     * 如果GatewayServiceClient为null，执行器功能可能受限
     * </p>
     */
    private void initializeBuiltInExecutors() {
        try {
            // 注册审批执行器 - 处理审批任务（支持无参构造函数）
            ApprovalExecutor approvalExecutor = gatewayServiceClient != null
                    ? new ApprovalExecutor(gatewayServiceClient)
                    : new ApprovalExecutor();
            registerNodeExecutor(approvalExecutor.getType(), approvalExecutor);
            log.debug("[流程引擎] 注册审批执行器: {}", approvalExecutor.getType());

            // 注册系统执行器 - 处理系统级操作（需要GatewayServiceClient）
            if (gatewayServiceClient != null) {
                SystemExecutor systemExecutor = new SystemExecutor(gatewayServiceClient);
                registerNodeExecutor(systemExecutor.getType(), systemExecutor);
                log.debug("[流程引擎] 注册系统执行器: {}", systemExecutor.getType());
            } else {
                log.warn("[流程引擎] GatewayServiceClient未配置，跳过系统执行器注册");
            }

            // 注册通知执行器 - 处理通知消息（需要GatewayServiceClient）
            if (gatewayServiceClient != null) {
                NotificationExecutor notificationExecutor = new NotificationExecutor(gatewayServiceClient);
                registerNodeExecutor(notificationExecutor.getType(), notificationExecutor);
                log.debug("[流程引擎] 注册通知执行器: {}", notificationExecutor.getType());
            } else {
                log.warn("[流程引擎] GatewayServiceClient未配置，跳过通知执行器注册");
            }

            // 注册条件执行器 - 处理条件判断（需要ExpressionEngineManager和GatewayServiceClient）
            ConditionExecutor conditionExecutor = new ConditionExecutor(expressionEngineManager, gatewayServiceClient);
            registerNodeExecutor(conditionExecutor.getType(), conditionExecutor);
            log.debug("[流程引擎] 注册条件执行器: {}", conditionExecutor.getType());

            log.info("[流程引擎] 内置执行器初始化完成, 数量: {}", nodeExecutors.size());
            if (!nodeExecutors.isEmpty()) {
                log.info("[流程引擎] 可用执行器类型: {}", String.join(", ", nodeExecutors.keySet()));
            } else {
                log.warn("[流程引擎] 未注册任何执行器，请检查依赖配置");
            }

        } catch (Exception e) {
            log.error("[流程引擎] 内置执行器初始化失败: error={}", e.getMessage(), e);
            throw new WorkflowException("内置执行器初始化失败", e);
        }
    }

    /**
     * 关闭流程引擎
     */
    public void shutdown() {
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[流程引擎] 工作流引擎已关闭");
    }

    /**
     * 获取表达式引擎管理器
     *
     * @return 表达式引擎管理器
     */
    public ExpressionEngineManager getExpressionEngineManager() {
        return expressionEngineManager;
    }

    /**
     * 获取运行中的流程实例
     */
    public List<WorkflowInstance> getRunningInstances() {
        return new ArrayList<>(runningInstances.values());
    }

    /**
     * 获取流程定义列表
     */
    public List<WorkflowDefinition> getWorkflowDefinitions() {
        return new ArrayList<>(workflowDefinitions.values());
    }
}
