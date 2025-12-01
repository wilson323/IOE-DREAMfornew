package net.lab1024.sa.enterprise.oa.workflow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 工作流引擎
 * 提供工作流定义、执行、监控、管理等功能
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class WorkflowEngine {

    /**
     * 工作流状态枚举
     */
    public enum WorkflowStatus {
        DRAFT("DRAFT", "草稿"),
        ACTIVE("ACTIVE", "激活"),
        SUSPENDED("SUSPENDED", "暂停"),
        COMPLETED("COMPLETED", "已完成"),
        TERMINATED("TERMINATED", "已终止"),
        ERROR("ERROR", "错误");

        private final String code;
        private final String description;

        WorkflowStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工作流类型枚举
     */
    public enum WorkflowType {
        DOCUMENT_APPROVAL("DOCUMENT_APPROVAL", "文档审批"),
        LEAVE_APPLICATION("LEAVE_APPLICATION", "请假申请"),
        EXPENSE_REIMBURSEMENT("EXPENSE_REIMBURSEMENT", "费用报销"),
        CONTRACT_APPROVAL("CONTRACT_APPROVAL", "合同审批"),
        PROJECT_APPROVAL("PROJECT_APPROVAL", "项目审批"),
        PURCHASE_APPROVAL("PURCHASE_APPROVAL", "采购审批"),
        HR_PROCESS("HR_PROCESS", "人事流程"),
        FINANCIAL_APPROVAL("FINANCIAL_APPROVAL", "财务审批");

        private final String code;
        private final String description;

        WorkflowType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        START("START", "开始节点"),
        APPROVAL("APPROVAL", "审批节点"),
        REVIEW("REVIEW", "审核节点"),
        NOTIFICATION("NOTIFICATION", "通知节点"),
        TASK("TASK", "任务节点"),
        DECISION("DECISION", "决策节点"),
        END("END", "结束节点"),
        PARALLEL("PARALLEL", "并行节点"),
        TIMER("TIMER", "定时节点");

        private final String code;
        private final String description;

        NodeType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 工作流定义
     */
    @Data
    public static class WorkflowDefinition {
        private Long workflowId;
        private String workflowName;
        private String workflowCode;
        private WorkflowType workflowType;
        private String description;
        private WorkflowStatus status;
        private String version;
        private Long creatorId;
        private String creatorName;
        private LocalDateTime createTime;
        private LocalDateTime lastUpdateTime;
        private List<WorkflowNode> nodes;
        private List<WorkflowTransition> transitions;
        private Map<String, Object> variables;
        private List<String> participants;
        private Integer timeoutHours;
        private Boolean enableNotifications;
        private String notificationConfig;
        private Map<String, Object> customSettings;
    }

    /**
     * 工作流节点
     */
    @Data
    public static class WorkflowNode {
        private String nodeId;
        private String nodeCode;
        private String nodeName;
        private NodeType nodeType;
        private String description;
        private Integer executionOrder;
        private List<String> approvers; // 审批者列表
        private List<String> reviewers; // 审核者列表
        private String assignedTo; // 分配给谁
        private Boolean isRequired; // 是否必需
        private Integer minimumApprovals; // 最少审批数
        private String conditionExpression; // 条件表达式
        private Map<String, Object> parameters; // 节点参数
        private List<String> notifications; // 通知配置
        private Integer timeoutMinutes; // 超时时间
        private Boolean autoApprove; // 自动批准
        private Map<String, Object> actionConfig; // 动作配置
    }

    /**
     * 工作流转换
     */
    @Data
    public static class WorkflowTransition {
        private String transitionId;
        private String fromNodeId;
        private String toNodeId;
        private String condition; // 转换条件
        private String action; // 转换动作
        private Boolean isDefault; // 是否默认转换
        private String label; // 转换标签
    }

    /**
     * 工作流实例
     */
    @Data
    public static class WorkflowInstance {
        private String instanceId;
        private Long workflowId;
        private String workflowName;
        private WorkflowType workflowType;
        private String initiatorId;
        private String initiatorName;
        private String subject;
        private String description;
        private WorkflowStatus status;
        private String currentNodeId;
        private LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;
        private LocalDateTime endTime;
        private Integer timeoutHours;
        private Map<String, Object> variables;
        private List<WorkflowTask> tasks;
        private List<String> approvers;
        private List<String> watchers;
        private Map<String, Object> attachments;
        private List<String> comments;
        private String priority;
        private Boolean requiresAttention;
        private String errorMessage;
        private List<String> participants;
    }

    /**
     * 工作流任务
     */
    @Data
    public static class WorkflowTask {
        private String taskId;
        private String instanceId;
        private String nodeId;
        private String taskName;
        private String taskType;
        private String assigneeId;
        private String assigneeName;
        private String status; // PENDING, IN_PROGRESS, COMPLETED, SKIPPED, FAILED
        private LocalDateTime createTime;
        private LocalDateTime assignTime;
        private LocalDateTime completeTime;
        private LocalDateTime actionTime;
        private Integer timeoutMinutes;
        private String result;
        private String comment;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private List<String> attachments;
        private Integer retryCount;
        private Boolean requiresReassignment;
    }

    /**
     * 审批操作
     */
    @Data
    public static class ApprovalAction {
        private String actionId;
        private String instanceId;
        private String taskId;
        private String approverId;
        private String approverName;
        private String action; // APPROVE, REJECT, SKIP, RETURN
        private String comment;
        private LocalDateTime actionTime;
        private Map<String, Object> data;
        private Boolean requiresReassignment;
    }

    /**
     * 工作流执行结果
     */
    @Data
    public static class WorkflowExecutionResult {
        private Boolean success;
        private String message;
        private String instanceId;
        private String nextNodeId;
        private List<String> completedTasks;
        private List<String> pendingTasks;
        private List<String> notifications;
        private Map<String, Object> outputData;
        private List<String> warnings;
        private String errorCode;
        private Long executionTimeMs;
    }

    /**
     * 工作流存储（模拟）
     */
    private Map<Long, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();
    private Map<String, WorkflowInstance> workflowInstances = new ConcurrentHashMap<>();
    private Map<String, List<WorkflowTask>> taskStorage = new ConcurrentHashMap<>();

    /**
     * 统计信息
     */
    private Map<String, Object> statistics = new HashMap<>();

    /**
     * 初始化工作流引擎
     */
    public void initializeEngine() {
        log.info("工作流引擎初始化开始");

        // 加载工作流定义
        loadWorkflowDefinitions();

        log.info("工作流引擎初始化完成，加载了{}个工作流定义", workflowDefinitions.size());
    }

    /**
     * 加载工作流定义
     */
    private void loadWorkflowDefinitions() {
        // 文档审批工作流
        WorkflowDefinition documentApprovalWorkflow = createDocumentApprovalWorkflow();
        workflowDefinitions.put(documentApprovalWorkflow.getWorkflowId(), documentApprovalWorkflow);

        // 请假申请工作流
        WorkflowDefinition leaveApplicationWorkflow = createLeaveApplicationWorkflow();
        workflowDefinitions.put(leaveApplicationWorkflow.getWorkflowId(), leaveApplicationWorkflow);

        // 费用报销工作流
        WorkflowDefinition expenseReimbursementWorkflow = createExpenseReimbursementWorkflow();
        workflowDefinitions.put(expenseReimbursementWorkflow.getWorkflowId(), expenseReimbursementWorkflow);

        // 合同审批工作流
        WorkflowDefinition contractApprovalWorkflow = createContractApprovalWorkflow();
        workflowDefinitions.put(contractApprovalWorkflow.getWorkflowId(), contractApprovalWorkflow);

        log.debug("加载了{}个工作流定义", workflowDefinitions.size());
    }

    /**
     * 创建文档审批工作流
     */
    private WorkflowDefinition createDocumentApprovalWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setWorkflowId(1L);
        workflow.setWorkflowName("文档审批流程");
        workflow.setWorkflowCode("DOC_APPROVAL");
        workflow.setWorkflowType(WorkflowType.DOCUMENT_APPROVAL);
        workflow.setDescription("用于审批各类文档的标准化流程");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setVersion("1.0");
        workflow.setCreatorId(1L);
        workflow.setCreatorName("系统管理员");
        workflow.setCreateTime(LocalDateTime.now());
        workflow.setLastUpdateTime(LocalDateTime.now());
        workflow.setTimeoutHours(72);
        workflow.setEnableNotifications(true);

        // 创建节点
        List<WorkflowNode> nodes = new ArrayList<>();

        // 开始节点
        WorkflowNode startNode = new WorkflowNode();
        startNode.setNodeId("START");
        startNode.setNodeCode("START");
        startNode.setNodeName("开始");
        startNode.setNodeType(NodeType.START);
        startNode.setExecutionOrder(1);
        startNode.setDescription("工作流开始");
        nodes.add(startNode);

        // 提交人审核节点
        WorkflowNode submitterReview = new WorkflowNode();
        submitterReview.setNodeId("SUBMITTER_REVIEW");
        submitterReview.setNodeCode("SUBMITTER_REVIEW");
        submitterReview.setNodeName("提交人审核");
        submitterReview.setNodeType(NodeType.REVIEW);
        submitterReview.setExecutionOrder(2);
        submitterReview.setIsRequired(false);
        submitterReview.setTimeoutMinutes(30);
        submitterReview.setDescription("提交人审核文档内容和格式");
        nodes.add(submitterReview);

        // 部门主管审批节点
        WorkflowNode managerApproval = new WorkflowNode();
        managerApproval.setNodeId("MANAGER_APPROVAL");
        managerApproval.setNodeCode("MANAGER_APPROVAL");
        managerApproval.setNodeName("部门主管审批");
        managerApproval.setNodeType(NodeType.APPROVAL);
        managerApproval.setExecutionOrder(3);
        managerApproval.setIsRequired(true);
        managerApproval.setMinimumApprovals(1);
        managerApproval.setTimeoutMinutes(120);
        managerApproval.setDescription("部门主管审批文档");
        nodes.add(managerApproval);

        // 财务审批节点（金额大于10000时需要）
        WorkflowNode financeApproval = new WorkflowNode();
        financeApproval.setNodeId("FINANCE_APPROVAL");
        financeApproval.setNodeCode("FINANCE_APPROVAL");
        financeApproval.setNodeName("财务审批");
        financeApproval.setNodeType(NodeType.APPROVAL);
        financeApproval.setExecutionOrder(4);
        financeApproval.setIsRequired(false);
        financeApproval.setMinimumApprovals(1);
        financeApproval.setTimeoutMinutes(180);
        financeApproval.setConditionExpression("${amount} > 10000");
        financeApproval.setDescription("财务审批大额文档");
        nodes.add(financeApproval);

        // 通知节点
        WorkflowNode notificationNode = new WorkflowNode();
        notificationNode.setNodeId("NOTIFICATION");
        notificationNode.setNodeCode("NOTIFICATION");
        notificationNode.setNodeName("发送通知");
        notificationNode.setNodeType(NodeType.NOTIFICATION);
        notificationNode.setExecutionOrder(5);
        notificationNode.setDescription("向相关人员发送审批结果通知");
        nodes.add(notificationNode);

        // 结束节点
        WorkflowNode endNode = new WorkflowNode();
        endNode.setNodeId("END");
        endNode.setNodeCode("END");
        endNode.setNodeName("结束");
        endNode.setNodeType(NodeType.END);
        endNode.setExecutionOrder(6);
        endNode.setDescription("工作流结束");
        nodes.add(endNode);

        workflow.setNodes(nodes);

        // 创建转换
        List<WorkflowTransition> transitions = new ArrayList<>();

        // START -> SUBMITTER_REVIEW
        transitions.add(createTransition("START", "SUBMITTER_REVIEW", null, null, true));

        // SUBMITTER_REVIEW -> MANAGER_APPROVAL
        transitions.add(createTransition("SUBMITTER_REVIEW", "MANAGER_APPROVAL", null, null, false));

        // SUBMITTER_REVIEW -> END (提交人拒绝)
        transitions.add(createTransition("SUBMITTER_REVIEW", "END", "${submitterAction} == 'REJECT'", "提交人拒绝", false));

        // MANAGER_APPROVAL -> FINANCE_APPROVAL (需要财务审批)
        transitions
                .add(createTransition("MANAGER_APPROVAL", "FINANCE_APPROVAL", "${amount} > 10000", "金额超过10000", false));

        // MANAGER_APPROVAL -> NOTIFICATION (经理批准且不需要财务审批)
        transitions.add(createTransition("MANAGER_APPROVAL", "NOTIFICATION",
                "${amount} <= 10000 AND ${managerAction} == 'APPROVE'", "经理批准", true));

        // MANAGER_APPROVAL -> END (经理拒绝)
        transitions.add(createTransition("MANAGER_APPROVAL", "END", "${managerAction} == 'REJECT'", "经理拒绝", false));

        // FINANCE_APPROVAL -> NOTIFICATION (财务审批)
        transitions.add(createTransition("FINANCE_APPROVAL", "NOTIFICATION",
                "${financeAction} == 'APPROVE' || ${financeAction} == 'SKIP'", "财务处理完成", true));

        // FINANCE_APPROVAL -> END (财务拒绝)
        transitions.add(createTransition("FINANCE_APPROVAL", "END", "${financeAction} == 'REJECT'", "财务拒绝", false));

        // NOTIFICATION -> END
        transitions.add(createTransition("NOTIFICATION", "END", null, null, true));

        workflow.setTransitions(transitions);

        // 设置参与者和变量
        workflow.setParticipants(Arrays.asList("submitter", "manager", "finance"));
        workflow.setVariables(new HashMap<>());
        workflow.getVariables().put("amount", 0);
        workflow.getVariables().put("submitterAction", "");
        workflow.getVariables().put("managerAction", "");
        workflow.getVariables().put("financeAction", "");

        return workflow;
    }

    /**
     * 创建请假申请工作流
     */
    private WorkflowDefinition createLeaveApplicationWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setWorkflowId(2L);
        workflow.setWorkflowName("请假申请流程");
        workflow.setWorkflowCode("LEAVE_APP");
        workflow.setWorkflowType(WorkflowType.LEAVE_APPLICATION);
        workflow.setDescription("员工请假申请审批流程");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setVersion("1.0");
        workflow.setCreatorId(1L);
        workflow.setCreatorName("系统管理员");
        workflow.setCreateTime(LocalDateTime.now());
        workflow.setLastUpdateTime(LocalDateTime.now());
        workflow.setTimeoutHours(48);
        workflow.setEnableNotifications(true);

        // 创建节点
        List<WorkflowNode> nodes = new ArrayList<>();

        // 开始节点
        WorkflowNode startNode = new WorkflowNode();
        startNode.setNodeId("START");
        startNode.setNodeCode("START");
        startNode.setNodeName("开始");
        startNode.setNodeType(NodeType.START);
        startNode.setExecutionOrder(1);
        nodes.add(startNode);

        // 直属主管审批
        WorkflowNode supervisorApproval = new WorkflowNode();
        supervisorApproval.setNodeId("SUPERVISOR_APPROVAL");
        supervisorApproval.setNodeCode("SUPERVISOR_APPROVAL");
        supervisorApproval.setNodeName("直属主管审批");
        supervisorApproval.setNodeType(NodeType.APPROVAL);
        supervisorApproval.setExecutionOrder(2);
        supervisorApproval.setIsRequired(true);
        supervisorApproval.setMinimumApprovals(1);
        nodes.add(supervisorApproval);

        // 人事部门审批（请假天数超过5天时需要）
        WorkflowNode hrApproval = new WorkflowNode();
        hrApproval.setNodeId("HR_APPROVAL");
        hrApproval.setNodeCode("HR_APPROVAL");
        hrApproval.setNodeName("人事部门审批");
        hrApproval.setNodeType(NodeType.APPROVAL);
        hrApproval.setExecutionOrder(3);
        hrApproval.setIsRequired(false);
        hrApproval.setConditionExpression("${leaveDays} > 5");
        nodes.add(hrApproval);

        // 结束节点
        WorkflowNode endNode = new WorkflowNode();
        endNode.setNodeId("END");
        endNode.setNodeCode("END");
        endNode.setNodeName("结束");
        endNode.setNodeType(NodeType.END);
        endNode.setExecutionOrder(4);
        nodes.add(endNode);

        workflow.setNodes(nodes);

        // 创建转换
        List<WorkflowTransition> transitions = new ArrayList<>();
        transitions.add(createTransition("START", "SUPERVISOR_APPROVAL", null, null, true));
        transitions.add(createTransition("SUPERVISOR_APPROVAL", "HR_APPROVAL", "${leaveDays} > 5", "请假超过5天", false));
        transitions.add(createTransition("SUPERVISOR_APPROVAL", "END", "${leaveDays} <= 5", "请假5天内", true));
        transitions.add(createTransition("HR_APPROVAL", "END", null, null, true));

        workflow.setTransitions(transitions);

        // 设置参与者和变量
        workflow.setParticipants(Arrays.asList("applicant", "supervisor", "hr"));
        workflow.setVariables(new HashMap<>());
        workflow.getVariables().put("leaveDays", 0);

        return workflow;
    }

    /**
     * 创建费用报销工作流
     */
    private WorkflowDefinition createExpenseReimbursementWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setWorkflowId(3L);
        workflow.setWorkflowName("费用报销流程");
        workflow.setWorkflowCode("EXPENSE_REIMB");
        workflow.setWorkflowType(WorkflowType.EXPENSE_REIMBURSEMENT);
        workflow.setDescription("员工费用报销申请流程");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setVersion("1.0");
        workflow.setCreatorId(1L);
        workflow.setCreatorName("系统管理员");
        workflow.setCreateTime(LocalDateTime.now());
        workflow.setLastUpdateTime(LocalDateTime.now());
        workflow.setTimeoutHours(96);
        workflow.setEnableNotifications(true);

        // 创建节点和转换
        List<WorkflowNode> nodes = new ArrayList<>();
        List<WorkflowTransition> transitions = new ArrayList<>();

        // 开始节点
        WorkflowNode startNode = new WorkflowNode();
        startNode.setNodeId("START");
        startNode.setNodeCode("START");
        startNode.setNodeName("开始");
        startNode.setNodeType(NodeType.START);
        startNode.setExecutionOrder(1);
        nodes.add(startNode);

        // 部门主管审批
        WorkflowNode deptApproval = new WorkflowNode();
        deptApproval.setNodeId("DEPT_APPROVAL");
        deptApproval.setNodeCode("DEPT_APPROVAL");
        deptApproval.setNodeName("部门主管审批");
        deptApproval.setNodeType(NodeType.APPROVAL);
        deptApproval.setExecutionOrder(2);
        deptApproval.setIsRequired(true);
        deptApproval.setMinimumApprovals(1);
        nodes.add(deptApproval);

        // 财务审批
        WorkflowNode financeApproval = new WorkflowNode();
        financeApproval.setNodeId("FINANCE_APPROVAL");
        financeApproval.setNodeCode("FINANCE_APPROVAL");
        financeApproval.setNodeName("财务部门审批");
        financeApproval.setNodeType(NodeType.APPROVAL);
        financeApproval.setExecutionOrder(3);
        financeApproval.setIsRequired(true);
        financeApproval.setMinimumApprovals(1);
        nodes.add(financeApproval);

        // 结束节点
        WorkflowNode endNode = new WorkflowNode();
        endNode.setNodeId("END");
        endNode.setNodeCode("END");
        endNode.setNodeName("结束");
        endNode.setNodeType(NodeType.END);
        endNode.setExecutionOrder(4);
        nodes.add(endNode);

        workflow.setNodes(nodes);
        transitions.add(createTransition("START", "DEPT_APPROVAL", null, null, true));
        transitions.add(createTransition("DEPT_APPROVAL", "FINANCE_APPROVAL", null, null, true));
        transitions.add(createTransition("FINANCE_APPROVAL", "END", null, null, true));

        workflow.setTransitions(transitions);

        workflow.setParticipants(Arrays.asList("applicant", "deptManager", "finance"));
        workflow.setVariables(new HashMap<>());

        return workflow;
    }

    /**
     * 创建合同审批工作流
     */
    private WorkflowDefinition createContractApprovalWorkflow() {
        WorkflowDefinition workflow = new WorkflowDefinition();
        workflow.setWorkflowId(4L);
        workflow.setWorkflowName("合同审批流程");
        workflow.setWorkflowCode("CONTRACT_APPROVAL");
        workflow.setWorkflowType(WorkflowType.CONTRACT_APPROVAL);
        workflow.setDescription("合同审批流程");
        workflow.setStatus(WorkflowStatus.ACTIVE);
        workflow.setVersion("1.0");
        workflow.setCreatorId(1L);
        workflow.setCreatorName("系统管理员");
        workflow.setCreateTime(LocalDateTime.now());
        workflow.setLastUpdateTime(LocalDateTime.now());
        workflow.setTimeoutHours(168); // 7天
        workflow.setEnableNotifications(true);

        // 创建节点和转换
        List<WorkflowNode> nodes = new ArrayList<>();
        List<WorkflowTransition> transitions = new ArrayList<>();

        // 开始节点
        WorkflowNode startNode = new WorkflowNode();
        startNode.setNodeId("START");
        startNode.setNodeCode("START");
        startNode.setNodeName("开始");
        startNode.setNodeType(NodeType.START);
        startNode.setExecutionOrder(1);
        nodes.add(startNode);

        // 法务审核节点
        WorkflowNode legalReview = new WorkflowNode();
        legalReview.setNodeId("LEGAL_REVIEW");
        legalReview.setNodeCode("LEGAL_REVIEW");
        legalReview.setNodeName("法务审核");
        legalReview.setNodeType(NodeType.REVIEW);
        legalReview.setExecutionOrder(2);
        legalReview.setIsRequired(true);
        nodes.add(legalReview);

        // 业务部门审批
        WorkflowNode businessApproval = new WorkflowNode();
        businessApproval.setNodeId("BUSINESS_APPROVAL");
        businessApproval.setNodeCode("BUSINESS_APPROVAL");
        businessApproval.setNodeName("业务部门审批");
        businessApproval.setNodeType(NodeType.APPROVAL);
        businessApproval.setExecutionOrder(3);
        businessApproval.setIsRequired(true);
        nodes.add(businessApproval);

        // 高级管理审批（金额超过500000时需要）
        WorkflowNode seniorApproval = new WorkflowNode();
        seniorApproval.setNodeId("SENIOR_APPROVAL");
        seniorApproval.setNodeCode("SENIOR_APPROVAL");
        seniorApproval.setNodeName("高级管理审批");
        seniorApproval.setNodeType(NodeType.APPROVAL);
        seniorApproval.setExecutionOrder(4);
        seniorApproval.setIsRequired(false);
        seniorApproval.setConditionExpression("${contractAmount} > 500000");
        nodes.add(seniorApproval);

        // 结束节点
        WorkflowNode endNode = new WorkflowNode();
        endNode.setNodeId("END");
        endNode.setNodeCode("END");
        endNode.setNodeName("结束");
        endNode.setNodeType(NodeType.END);
        endNode.setExecutionOrder(5);
        nodes.add(endNode);

        workflow.setNodes(nodes);
        transitions.add(createTransition("START", "LEGAL_REVIEW", null, null, true));
        transitions.add(createTransition("LEGAL_REVIEW", "BUSINESS_APPROVAL", null, null, true));
        transitions.add(createTransition("BUSINESS_APPROVAL", "SENIOR_APPROVAL", "${contractAmount} > 500000",
                "合同金额超过50万", false));
        transitions.add(createTransition("BUSINESS_APPROVAL", "END", "${contractAmount} <= 500000", "合同金额50万以下", true));
        transitions.add(createTransition("SENIOR_APPROVAL", "END", null, null, true));

        workflow.setTransitions(transitions);

        workflow.setParticipants(Arrays.asList("requester", "legal", "business", "senior"));
        workflow.setVariables(new HashMap<>());
        workflow.getVariables().put("contractAmount", 0);

        return workflow;
    }

    /**
     * 创建转换
     */
    private WorkflowTransition createTransition(String fromNodeId, String toNodeId, String condition, String action,
            Boolean isDefault) {
        WorkflowTransition transition = new WorkflowTransition();
        transition.setTransitionId("TRANS_" + System.currentTimeMillis());
        transition.setFromNodeId(fromNodeId);
        transition.setToNodeId(toNodeId);
        transition.setCondition(condition);
        transition.setAction(action);
        transition.setIsDefault(isDefault != null ? isDefault : false);
        transition.setLabel(transition.getFromNodeId() + " -> " + transition.getToNodeId());
        return transition;
    }

    /**
     * 启动工作流实例
     *
     * @param workflowId    工作流定义ID
     * @param initiatorId   发起人ID
     * @param initiatorName 发起人姓名
     * @param subject       主题
     * @param description   描述
     * @param variables     变量
     * @return 工作流实例
     */
    public WorkflowInstance startWorkflowInstance(Long workflowId, String initiatorId, String initiatorName,
            String subject, String description, Map<String, Object> variables) {
        log.info("启动工作流实例：工作流ID={}，发起人={}，主题={}", workflowId, initiatorName, subject);

        WorkflowDefinition workflowDefinition = workflowDefinitions.get(workflowId);
        if (workflowDefinition == null) {
            log.error("工作流定义不存在：{}", workflowId);
            return null;
        }

        if (!WorkflowStatus.ACTIVE.equals(workflowDefinition.getStatus())) {
            log.error("工作流未激活：{}", workflowId);
            return null;
        }

        WorkflowInstance instance = new WorkflowInstance();
        instance.setInstanceId("INST_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000));
        instance.setWorkflowId(workflowId);
        instance.setWorkflowName(workflowDefinition.getWorkflowName());
        instance.setWorkflowType(workflowDefinition.getWorkflowType());
        instance.setInitiatorId(initiatorId);
        instance.setInitiatorName(initiatorName);
        instance.setSubject(subject);
        instance.setDescription(description);
        instance.setStatus(WorkflowStatus.ACTIVE);
        instance.setCurrentNodeId(findStartNodeId(workflowDefinition));
        instance.setStartTime(LocalDateTime.now());
        instance.setLastUpdateTime(LocalDateTime.now());
        instance.setTimeoutHours(workflowDefinition.getTimeoutHours());
        instance.setVariables(new HashMap<>(variables));
        instance.setTasks(new ArrayList<>());
        instance.setApprovers(Arrays.asList(initiatorId));
        instance.setWatchers(Arrays.asList(initiatorId));
        instance.setAttachments(new HashMap<>());
        instance.setComments(new ArrayList<>());
        instance.setPriority("NORMAL");
        instance.setRequiresAttention(false);

        // 继承工作流定义的参与者
        instance.setParticipants(new ArrayList<>(workflowDefinition.getParticipants()));

        // 存储实例
        workflowInstances.put(instance.getInstanceId(), instance);

        // 创建开始任务
        createTaskForNode(instance, instance.getCurrentNodeId());

        // 发送通知
        sendWorkflowNotification(instance, "WORKFLOW_STARTED", "工作流已启动");

        log.info("工作流实例启动成功：实例ID={}", instance.getInstanceId());

        return instance;
    }

    /**
     * 查找开始节点ID
     */
    private String findStartNodeId(WorkflowDefinition workflowDefinition) {
        return workflowDefinition.getNodes().stream()
                .filter(node -> NodeType.START.equals(node.getNodeType()))
                .map(WorkflowNode::getNodeId)
                .findFirst()
                .orElse("START");
    }

    /**
     * 为节点创建任务
     */
    private void createTaskForNode(WorkflowInstance instance, String nodeId) {
        WorkflowDefinition workflowDefinition = workflowDefinitions.get(instance.getWorkflowId());
        WorkflowNode node = findNodeById(workflowDefinition, nodeId);

        if (node == null) {
            return;
        }

        // 只有审批、审核、任务节点才需要创建任务
        if (!NodeType.APPROVAL.equals(node.getNodeType()) &&
                !NodeType.REVIEW.equals(node.getNodeType()) &&
                !NodeType.TASK.equals(node.getNodeType())) {
            return;
        }

        WorkflowTask task = new WorkflowTask();
        task.setTaskId("TASK_" + System.currentTimeMillis() + "_" + nodeId + "_" + (int) (Math.random() * 1000));
        task.setInstanceId(instance.getInstanceId());
        task.setNodeId(nodeId);
        task.setTaskName(node.getNodeName());
        task.setTaskType(node.getNodeType().getCode());
        task.setStatus("PENDING");

        // 设置任务分配
        if (NodeType.APPROVAL.equals(node.getNodeType())) {
            if (node.getApprovers() != null && !node.getApprovers().isEmpty()) {
                task.setAssigneeId(node.getApprovers().get(0));
                task.setAssigneeName(getUserName(node.getApprovers().get(0)));
            } else {
                task.setAssigneeId(instance.getInitiatorId());
                task.setAssigneeName(instance.getInitiatorName());
            }
        } else {
            task.setAssigneeId(instance.getInitiatorId());
            task.setAssigneeName(instance.getInitiatorName());
        }

        task.setCreateTime(LocalDateTime.now());
        task.setTimeoutMinutes(node.getTimeoutMinutes());
        task.setRetryCount(0);

        // 存储任务
        taskStorage.computeIfAbsent(instance.getInstanceId(), k -> new ArrayList<>()).add(task);

        log.debug("为节点创建任务：实例ID={}, 节点ID={}, 任务ID={}",
                instance.getInstanceId(), nodeId, task.getTaskId());
    }

    /**
     * 根据ID查找节点
     */
    private WorkflowNode findNodeById(WorkflowDefinition workflowDefinition, String nodeId) {
        return workflowDefinition.getNodes().stream()
                .filter(node -> nodeId.equals(node.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取用户名（模拟）
     */
    private String getUserName(String userId) {
        // 模拟从用户服务获取用户名
        return "用户" + userId;
    }

    /**
     * 处理审批操作
     *
     * @param action 审批操作
     * @return 处理结果
     */
    public WorkflowExecutionResult processApproval(ApprovalAction action) {
        long startTime = System.currentTimeMillis();
        log.info("处理审批操作：实例ID={}, 任务ID={}, 审批人={}, 操作={}",
                action.getInstanceId(), action.getTaskId(), action.getApproverName(), action.getAction());

        WorkflowExecutionResult result = new WorkflowExecutionResult();
        result.setSuccess(false);
        result.setCompletedTasks(new ArrayList<>());
        result.setPendingTasks(new ArrayList<>());
        result.setNotifications(new ArrayList<>());
        result.setOutputData(new HashMap<>());
        result.setWarnings(new ArrayList<>());

        try {
            WorkflowInstance instance = workflowInstances.get(action.getInstanceId());
            if (instance == null) {
                result.setMessage("工作流实例不存在：" + action.getInstanceId());
                return result;
            }

            WorkflowTask task = findTaskById(instance.getInstanceId(), action.getTaskId());
            if (task == null) {
                result.setMessage("任务不存在：" + action.getTaskId());
                return result;
            }

            WorkflowDefinition workflowDefinition = workflowDefinitions.get(instance.getWorkflowId());

            // 更新任务状态
            task.setStatus(action.getAction());
            task.setComment(action.getComment());
            task.setActionTime(LocalDateTime.now());
            task.setOutputData(action.getData());

            if (action.getAction().equals("APPROVE")) {
                task.setResult("APPROVED");
            } else if (action.getAction().equals("REJECT")) {
                task.setResult("REJECTED");
                task.setRequiresReassignment(false);
            } else if (action.getAction().equals("SKIP")) {
                task.setResult("SKIPPED");
            } else if (action.getAction().equals("RETURN")) {
                task.setResult("RETURNED");
                task.setRequiresReassignment(true);
            }

            // 更新任务完成时间
            if (!"PENDING".equals(task.getStatus())) {
                task.setCompleteTime(LocalDateTime.now());
            }

            // 添加到已完成任务
            result.getCompletedTasks().add(task.getTaskId());

            // 获取下一个节点
            String nextNodeId = findNextNode(workflowDefinition, task.getNodeId(), action, instance);

            if (nextNodeId != null) {
                // 更新实例当前节点
                instance.setCurrentNodeId(nextNodeId);
                instance.setLastUpdateTime(LocalDateTime.now());

                // 为下一个节点创建任务
                createTaskForNode(instance, nextNodeId);

                // 添加到待处理任务
                List<WorkflowTask> nextNodeTasks = taskStorage.get(instance.getInstanceId()).stream()
                        .filter(t -> nextNodeId.equals(t.getNodeId()))
                        .collect(Collectors.toList());
                result.getPendingTasks()
                        .addAll(nextNodeTasks.stream().map(WorkflowTask::getTaskId).collect(Collectors.toList()));

                result.setNextNodeId(nextNodeId);
            } else {
                // 没有下一个节点，工作流结束
                instance.setStatus(WorkflowStatus.COMPLETED);
                instance.setEndTime(LocalDateTime.now());
                instance.setCurrentNodeId("END");
                result.setNextNodeId("END");

                // 发送完成通知
                sendWorkflowNotification(instance, "WORKFLOW_COMPLETED", "工作流已完成");
            }

            // 更新实例变量
            if (action.getData() != null) {
                instance.getVariables().putAll(action.getData());
            }

            // 添加审批记录到实例
            if (instance.getComments() == null) {
                instance.setComments(new ArrayList<>());
            }
            instance.getComments()
                    .add(action.getApproverName() + " " + action.getAction() + ": " + action.getComment());

            result.setSuccess(true);
            result.setInstanceId(instance.getInstanceId());
            result.setMessage("审批处理完成");

            log.info("审批处理完成：实例ID={}, 结果={}, 下一节点={}",
                    instance.getInstanceId(), action.getAction(), nextNodeId);

        } catch (Exception e) {
            log.error("审批处理失败", e);
            result.setMessage("处理失败：" + e.getMessage());
            result.setErrorCode("APPROVAL_ERROR");
        }

        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

        return result;
    }

    /**
     * 查找任务
     */
    private WorkflowTask findTaskById(String instanceId, String taskId) {
        List<WorkflowTask> tasks = taskStorage.get(instanceId);
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .filter(task -> taskId.equals(task.getTaskId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 查找下一个节点
     */
    private String findNextNode(WorkflowDefinition workflowDefinition, String currentNodeId, ApprovalAction action,
            WorkflowInstance instance) {
        WorkflowNode currentNode = findNodeById(workflowDefinition, currentNodeId);
        if (currentNode == null) {
            return null;
        }

        // 查找从当前节点出发的转换
        List<WorkflowTransition> transitions = workflowDefinition.getTransitions().stream()
                .filter(transition -> transition.getFromNodeId().equals(currentNodeId))
                .collect(Collectors.toList());

        for (WorkflowTransition transition : transitions) {
            // 检查转换条件
            if (evaluateCondition(transition.getCondition(), instance)) {
                // 检查审批动作
                if (evaluateApprovalAction(transition.getAction(), action)) {
                    return transition.getToNodeId();
                }
            } else if (transition.getIsDefault()) {
                return transition.getToNodeId();
            }
        }

        return null;
    }

    /**
     * 评估转换条件
     */
    private boolean evaluateCondition(String condition, WorkflowInstance instance) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }

        // 简化的条件评估逻辑
        // 实际应该使用表达式引擎
        try {
            // 替换变量占位符
            String evaluatedCondition = condition;
            for (Map.Entry<String, Object> entry : instance.getVariables().entrySet()) {
                evaluatedCondition = evaluatedCondition.replace("${" + entry.getKey() + "}",
                        entry.getValue().toString());
            }

            // 简单的条件评估
            return evaluateSimpleCondition(evaluatedCondition);

        } catch (Exception e) {
            log.error("条件评估失败：{}", condition, e);
            return false;
        }
    }

    /**
     * 评估审批动作
     */
    private boolean evaluateApprovalAction(String action, ApprovalAction approvalAction) {
        if (action == null || action.trim().isEmpty()) {
            return true;
        }

        if (approvalAction.getAction() == null) {
            return true;
        }

        // 如果转换指定了动作，需要匹配
        if (action.equals("${" + approvalAction.getAction().toLowerCase() + "}") ||
                action.toLowerCase().contains(approvalAction.getAction().toLowerCase())) {
            return true;
        }

        return true;
    }

    /**
     * 简单条件评估
     */
    private boolean evaluateSimpleCondition(String condition) {
        // 这里应该使用真正的表达式引擎
        // 为了简化，只做一些基本的模式匹配

        // 处理大于比较
        if (condition.contains(">")) {
            String[] parts = condition.split(">");
            if (parts.length == 2) {
                try {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left > right;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        // 处理小于比较
        if (condition.contains("<")) {
            String[] parts = condition.split("<");
            if (parts.length == 2) {
                try {
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left < right;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        // 处理等于比较
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            if (parts.length == 2) {
                return parts[0].trim().equals(parts[1].trim());
            }
        }

        // 处理不等于比较
        if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            if (parts.length == 2) {
                return !parts[0].trim().equals(parts[1].trim());
            }
        }

        return true;
    }

    /**
     * 发送工作流通知
     */
    private void sendWorkflowNotification(WorkflowInstance instance, String eventType, String message) {
        log.info("发送工作流通知：实例ID={}, 事件={}, 消息={}", instance.getInstanceId(), eventType, message);

        // 模拟发送通知
        // 实际应该通过邮件、短信、系统消息等方式发送
    }

    /**
     * 获取工作流实例
     *
     * @param instanceId 实例ID
     * @return 工作流实例
     */
    public WorkflowInstance getWorkflowInstance(String instanceId) {
        return workflowInstances.get(instanceId);
    }

    /**
     * 获取工作流定义
     *
     * @param workflowId 工作流ID
     * @return 工作流定义
     */
    public WorkflowDefinition getWorkflowDefinition(Long workflowId) {
        return workflowDefinitions.get(workflowId);
    }

    /**
     * 获取用户任务
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    public List<WorkflowTask> getUserTasks(String userId) {
        return taskStorage.values().stream()
                .flatMap(List::stream)
                .filter(task -> userId.equals(task.getAssigneeId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有工作流类型
     */
    public List<WorkflowType> getAllWorkflowTypes() {
        return Arrays.asList(WorkflowType.values());
    }

    /**
     * 获取所有工作流定义
     */
    public List<WorkflowDefinition> getAllWorkflowDefinitions() {
        return new ArrayList<>(workflowDefinitions.values());
    }

    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>(statistics);
        stats.put("totalWorkflows", workflowDefinitions.size());
        stats.put("totalInstances", workflowInstances.size());
        stats.put("totalTasks", taskStorage.values().stream().mapToInt(List::size).sum());
        stats.put("activeInstances", workflowInstances.values().stream()
                .filter(instance -> WorkflowStatus.ACTIVE.equals(instance.getStatus())).count());
        stats.put("completedInstances", workflowInstances.values().stream()
                .filter(instance -> WorkflowStatus.COMPLETED.equals(instance.getStatus())).count());
        return stats;
    }
}
