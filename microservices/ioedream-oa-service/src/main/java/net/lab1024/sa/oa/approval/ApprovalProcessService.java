package net.lab1024.sa.oa.approval;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 审批流程管理服务
 * 支持多种审批流程类型、审批节点管理和流程监控
 *
 * @author IOE-DREAM
 * @since 2025-11-28
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalProcessService {

    @Resource
    private net.lab1024.sa.oa.workflow.WorkflowEngine workflowEngine;

    /**
     * 审批流程类型枚举
     */
    public enum ApprovalType {
        DOCUMENT_APPROVAL("文档审批", "需要审批的文档流程"),
        LEAVE_APPLICATION("请假申请", "员工请假审批流程"),
        EXPENSE_REIMBURSEMENT("费用报销", "费用报销审批流程"),
        PURCHASE_REQUEST("采购申请", "采购审批流程"),
        CONTRACT_APPROVAL("合同审批", "合同签订审批流程"),
        PERFORMANCE_REVIEW("绩效评估", "员工绩效考核流程"),
        DISMISSAL_APPLICATION("离职申请", "员工离职审批流程"),
        SALARY_ADJUSTMENT("薪资调整", "薪资变更审批流程");

        private final String description;
        private final String comment;

        ApprovalType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 审批状态枚举
     */
    public enum ApprovalStatus {
        PENDING("待审批", "等待审批人员处理"),
        IN_PROGRESS("审批中", "正在审批流程中"),
        APPROVED("已通过", "审批已通过"),
        REJECTED("已拒绝", "审批被拒绝"),
        CANCELLED("已取消", "申请人取消审批"),
        DELEGATED("已转交", "审批权限已转交");

        private final String description;
        private final String comment;

        ApprovalStatus(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 审批优先级枚举
     */
    public enum ApprovalPriority {
        LOW("低优先级", 1),
        NORMAL("普通优先级", 2),
        HIGH("高优先级", 3),
        URGENT("紧急", 4),
        SUPER_URGENT("特急", 5);

        private final String description;
        private final int level;

        ApprovalPriority(String description, int level) {
            this.description = description;
            this.level = level;
        }

        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * 审批节点信息
     */
    public static class ApprovalNode {
        private String nodeId;
        private String nodeName;
        private String approverId;
        private String approverName;
        private ApprovalStatus status;
        private LocalDateTime approvalTime;
        private String comments;
        private Integer approvalOrder;
        private boolean isCurrentNode;
        private boolean canDelegate;
        private String delegatedTo;

        // 构造函数
        public ApprovalNode(String nodeId, String nodeName, String approverId,
                Integer approvalOrder, boolean isCurrentNode) {
            this.nodeId = nodeId;
            this.nodeName = nodeName;
            this.approverId = approverId;
            this.approvalOrder = approvalOrder;
            this.isCurrentNode = isCurrentNode;
            this.status = ApprovalStatus.PENDING;
            this.canDelegate = true;
        }

        // Getter和Setter方法
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getApproverId() {
            return approverId;
        }

        public void setApproverId(String approverId) {
            this.approverId = approverId;
        }

        public String getApproverName() {
            return approverName;
        }

        public void setApproverName(String approverName) {
            this.approverName = approverName;
        }

        public ApprovalStatus getStatus() {
            return status;
        }

        public void setStatus(ApprovalStatus status) {
            this.status = status;
            if (status != ApprovalStatus.PENDING) {
                this.approvalTime = LocalDateTime.now();
            }
        }

        public LocalDateTime getApprovalTime() {
            return approvalTime;
        }

        public void setApprovalTime(LocalDateTime approvalTime) {
            this.approvalTime = approvalTime;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public Integer getApprovalOrder() {
            return approvalOrder;
        }

        public void setApprovalOrder(Integer approvalOrder) {
            this.approvalOrder = approvalOrder;
        }

        public boolean isCurrentNode() {
            return isCurrentNode;
        }

        public void setCurrentNode(boolean currentNode) {
            isCurrentNode = currentNode;
        }

        public boolean isCanDelegate() {
            return canDelegate;
        }

        public void setCanDelegate(boolean canDelegate) {
            this.canDelegate = canDelegate;
        }

        public String getDelegatedTo() {
            return delegatedTo;
        }

        public void setDelegatedTo(String delegatedTo) {
            this.delegatedTo = delegatedTo;
        }
    }

    /**
     * 审批流程信息
     */
    public static class ApprovalProcess {
        private String processId;
        private String processName;
        private ApprovalType approvalType;
        private String applicantId;
        private String applicantName;
        private ApprovalStatus status;
        private ApprovalPriority priority;
        private LocalDateTime createTime;
        private LocalDateTime finishTime;
        private String title;
        private String content;
        private List<ApprovalNode> approvalNodes;
        private Map<String, Object> formData;
        private String attachments;
        private Integer currentNodeIndex;
        private String processInstanceId;

        // 构造函数
        public ApprovalProcess(String processId, String processName, ApprovalType approvalType,
                String applicantId, String title) {
            this.processId = processId;
            this.processName = processName;
            this.approvalType = approvalType;
            this.applicantId = applicantId;
            this.title = title;
            this.status = ApprovalStatus.PENDING;
            this.priority = ApprovalPriority.NORMAL;
            this.createTime = LocalDateTime.now();
            this.currentNodeIndex = 0;
        }

        // Getter和Setter方法
        public String getProcessId() {
            return processId;
        }

        public void setProcessId(String processId) {
            this.processId = processId;
        }

        public String getProcessName() {
            return processName;
        }

        public void setProcessName(String processName) {
            this.processName = processName;
        }

        public ApprovalType getApprovalType() {
            return approvalType;
        }

        public void setApprovalType(ApprovalType approvalType) {
            this.approvalType = approvalType;
        }

        public String getApplicantId() {
            return applicantId;
        }

        public void setApplicantId(String applicantId) {
            this.applicantId = applicantId;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public void setApplicantName(String applicantName) {
            this.applicantName = applicantName;
        }

        public ApprovalStatus getStatus() {
            return status;
        }

        public void setStatus(ApprovalStatus status) {
            this.status = status;
            if (status == ApprovalStatus.APPROVED || status == ApprovalStatus.REJECTED ||
                    status == ApprovalStatus.CANCELLED) {
                this.finishTime = LocalDateTime.now();
            }
        }

        public ApprovalPriority getPriority() {
            return priority;
        }

        public void setPriority(ApprovalPriority priority) {
            this.priority = priority;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getFinishTime() {
            return finishTime;
        }

        public void setFinishTime(LocalDateTime finishTime) {
            this.finishTime = finishTime;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<ApprovalNode> getApprovalNodes() {
            return approvalNodes;
        }

        public void setApprovalNodes(List<ApprovalNode> approvalNodes) {
            this.approvalNodes = approvalNodes;
        }

        public Map<String, Object> getFormData() {
            return formData;
        }

        public void setFormData(Map<String, Object> formData) {
            this.formData = formData;
        }

        public String getAttachments() {
            return attachments;
        }

        public void setAttachments(String attachments) {
            this.attachments = attachments;
        }

        public Integer getCurrentNodeIndex() {
            return currentNodeIndex;
        }

        public void setCurrentNodeIndex(Integer currentNodeIndex) {
            this.currentNodeIndex = currentNodeIndex;
        }

        public String getProcessInstanceId() {
            return processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId) {
            this.processInstanceId = processInstanceId;
        }
    }

    /**
     * 创建审批流程
     */
    public ApprovalProcess createApprovalProcess(ApprovalType approvalType, String applicantId,
            String applicantName, String title, String content) {
        log.info("创建审批流程: type={}, applicant={}, title={}", approvalType, applicantId, title);

        ApprovalProcess process = new ApprovalProcess(
                "APPROVAL_" + System.currentTimeMillis(),
                approvalType.getDescription() + "流程",
                approvalType,
                applicantId,
                title);

        process.setApplicantName(applicantName);
        process.setContent(content);
        process.setStatus(ApprovalStatus.PENDING);

        // 根据审批类型初始化审批节点
        List<ApprovalNode> nodes = initializeApprovalNodes(approvalType, applicantId);
        process.setApprovalNodes(nodes);

        log.info("审批流程创建成功: processId={}, nodes={}", process.getProcessId(), nodes.size());
        return process;
    }

    /**
     * 处理审批
     */
    public boolean processApproval(String processId, String approverId, String action,
            String comments, boolean delegated) {
        log.info("处理审批: processId={}, approver={}, action={}", processId, approverId, action);

        ApprovalProcess process = getApprovalProcess(processId);
        if (process == null) {
            log.error("审批流程不存在: {}", processId);
            return false;
        }

        if (process.getStatus() != ApprovalStatus.PENDING && process.getStatus() != ApprovalStatus.IN_PROGRESS) {
            log.warn("审批流程状态异常: {}", process.getStatus());
            return false;
        }

        List<ApprovalNode> nodes = process.getApprovalNodes();
        if (nodes == null || nodes.isEmpty()) {
            log.error("审批节点为空");
            return false;
        }

        ApprovalNode currentNode = nodes.get(process.getCurrentNodeIndex());
        if (!currentNode.getApproverId().equals(approverId) &&
                !approverId.equals(currentNode.getDelegatedTo())) {
            log.warn("审批人权限不匹配: expected={}, actual={}",
                    currentNode.getApproverId(), approverId);
            return false;
        }

        // 处理审批动作
        boolean success = false;
        switch (action.toLowerCase()) {
            case "approve":
                currentNode.setStatus(ApprovalStatus.APPROVED);
                currentNode.setComments(comments);
                success = moveToNextNode(process);
                break;
            case "reject":
                currentNode.setStatus(ApprovalStatus.REJECTED);
                currentNode.setComments(comments);
                process.setStatus(ApprovalStatus.REJECTED);
                success = true;
                break;
            case "delegate":
                if (currentNode.isCanDelegate() && !delegated) {
                    // 实际转交逻辑需要目标审批人ID
                    currentNode.setDelegatedTo(approverId); // 这里简化处理
                    currentNode.setStatus(ApprovalStatus.DELEGATED);
                    success = true;
                }
                break;
            default:
                log.warn("未知的审批动作: {}", action);
                break;
        }

        if (success) {
            log.info("审批处理成功: processId={}, action={}", processId, action);
        }

        return success;
    }

    /**
     * 撤回审批申请
     */
    public boolean withdrawApproval(String processId, String applicantId) {
        log.info("撤回审批申请: processId={}, applicant={}", processId, applicantId);

        ApprovalProcess process = getApprovalProcess(processId);
        if (process == null) {
            log.error("审批流程不存在: {}", processId);
            return false;
        }

        if (!process.getApplicantId().equals(applicantId)) {
            log.warn("只有申请人可以撤回申请");
            return false;
        }

        if (process.getStatus() != ApprovalStatus.PENDING &&
                process.getStatus() != ApprovalStatus.IN_PROGRESS) {
            log.warn("当前状态不允许撤回: {}", process.getStatus());
            return false;
        }

        process.setStatus(ApprovalStatus.CANCELLED);
        log.info("审批申请已撤回: processId={}", processId);
        return true;
    }

    /**
     * 获取待审批列表
     */
    @Cacheable(value = "pendingApprovals", key = "#approverId")
    public List<ApprovalProcess> getPendingApprovals(String approverId) {
        log.info("获取待审批列表: approverId={}", approverId);

        // 模拟数据 - 实际应该从数据库查询
        return Arrays.asList(
                createSampleApproval("APPROVAL_001", "请假申请", ApprovalType.LEAVE_APPLICATION, "EMP001"),
                createSampleApproval("APPROVAL_002", "费用报销", ApprovalType.EXPENSE_REIMBURSEMENT, "EMP002")).stream()
                .filter(process -> hasPendingApproval(process, approverId))
                .collect(Collectors.toList());
    }

    /**
     * 获取我的申请列表
     */
    @Cacheable(value = "myApplications", key = "#applicantId")
    public List<ApprovalProcess> getMyApplications(String applicantId) {
        log.info("获取我的申请列表: applicantId={}", applicantId);

        // 模拟数据 - 实际应该从数据库查询
        return Arrays.asList(
                createSampleApproval("APPROVAL_003", "采购申请", ApprovalType.PURCHASE_REQUEST, applicantId),
                createSampleApproval("APPROVAL_004", "合同审批", ApprovalType.CONTRACT_APPROVAL, applicantId));
    }

    /**
     * 获取审批流程详情
     */
    @Cacheable(value = "approvalProcess", key = "#processId")
    public ApprovalProcess getApprovalProcess(String processId) {
        log.info("获取审批流程详情: processId={}", processId);

        // 模拟数据 - 实际应该从数据库查询
        return createSampleApproval(processId, "文档审批", ApprovalType.DOCUMENT_APPROVAL, "EMP001");
    }

    /**
     * 获取审批统计信息
     */
    public Map<String, Object> getApprovalStatistics(String userId, String timeRange) {
        log.info("获取审批统计信息: userId={}, timeRange={}", userId, timeRange);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("pendingCount", 5);
        statistics.put("approvedCount", 23);
        statistics.put("rejectedCount", 3);
        statistics.put("totalProcessed", 31);
        statistics.put("averageProcessingTime", "2.5天");

        Map<String, Integer> typeStatistics = new HashMap<>();
        typeStatistics.put("LEAVE_APPLICATION", 8);
        typeStatistics.put("EXPENSE_REIMBURSEMENT", 12);
        typeStatistics.put("PURCHASE_REQUEST", 6);
        typeStatistics.put("CONTRACT_APPROVAL", 5);
        statistics.put("typeStatistics", typeStatistics);

        return statistics;
    }

    /**
     * 设置审批代理
     */
    @CacheEvict(value = { "pendingApprovals", "myApplications" }, allEntries = true)
    public boolean setApprovalDelegate(String approverId, String delegateId, String delegateName,
            LocalDateTime startTime, LocalDateTime endTime) {
        log.info("设置审批代理: approverId={}, delegateId={}, timeRange={} to {}",
                approverId, delegateId, startTime, endTime);

        // 实际应该在数据库中保存代理关系
        // 这里简化处理，只记录日志
        log.info("审批代理设置成功: {} -> {} ({} to {})", approverId, delegateId, startTime, endTime);
        return true;
    }

    /**
     * 批量审批
     */
    @CacheEvict(value = { "pendingApprovals", "myApplications" }, allEntries = true)
    public Map<String, Boolean> batchApprove(List<String> processIds, String approverId,
            String action, String comments) {
        log.info("批量审批: processIds={}, approver={}, action={}", processIds, approverId, action);

        Map<String, Boolean> results = new HashMap<>();

        for (String processId : processIds) {
            boolean success = processApproval(processId, approverId, action, comments, false);
            results.put(processId, success);
        }

        long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        log.info("批量审批完成: 总数={}, 成功={}", processIds.size(), successCount);

        return results;
    }

    // 私有辅助方法

    /**
     * 初始化审批节点
     */
    private List<ApprovalNode> initializeApprovalNodes(ApprovalType approvalType, String applicantId) {
        List<ApprovalNode> nodes = new java.util.ArrayList<>();

        switch (approvalType) {
            case LEAVE_APPLICATION:
                nodes.add(new ApprovalNode("NODE_001", "直接主管审批", "SUPVISOR_001", 1, true));
                nodes.add(new ApprovalNode("NODE_002", "部门经理审批", "MANAGER_001", 2, false));
                nodes.add(new ApprovalNode("NODE_003", "HR审批", "HR_001", 3, false));
                break;
            case EXPENSE_REIMBURSEMENT:
                nodes.add(new ApprovalNode("NODE_001", "财务初审", "FINANCE_001", 1, true));
                nodes.add(new ApprovalNode("NODE_002", "财务经理审批", "FINANCE_MGR", 2, false));
                break;
            case PURCHASE_REQUEST:
                nodes.add(new ApprovalNode("NODE_001", "部门主管审批", "DEPT_HEAD", 1, true));
                nodes.add(new ApprovalNode("NODE_002", "采购部审批", "PURCHASE_MGR", 2, false));
                nodes.add(new ApprovalNode("NODE_003", "财务审批", "FINANCE_MGR", 3, false));
                break;
            case DOCUMENT_APPROVAL:
                nodes.add(new ApprovalNode("NODE_001", "文档审核", "DOC_REVIEWER", 1, true));
                nodes.add(new ApprovalNode("NODE_002", "法务审批", "LEGAL_MGR", 2, false));
                break;
            default:
                // 默认简单流程
                nodes.add(new ApprovalNode("NODE_001", "审批人", "APPROVER_001", 1, true));
                break;
        }

        return nodes;
    }

    /**
     * 移动到下一个审批节点
     */
    private boolean moveToNextNode(ApprovalProcess process) {
        List<ApprovalNode> nodes = process.getApprovalNodes();
        int currentIndex = process.getCurrentNodeIndex();

        if (currentIndex < nodes.size() - 1) {
            // 移动到下一个节点
            currentIndex++;
            process.setCurrentNodeIndex(currentIndex);

            ApprovalNode nextNode = nodes.get(currentIndex);
            nextNode.setCurrentNode(true);
            nodes.get(currentIndex - 1).setCurrentNode(false);

            process.setStatus(ApprovalStatus.IN_PROGRESS);
            log.info("审批流程进入下一节点: processId={}, nodeIndex={}",
                    process.getProcessId(), currentIndex);
            return true;
        } else {
            // 所有节点审批完成
            process.setStatus(ApprovalStatus.APPROVED);
            log.info("审批流程完成: processId={}", process.getProcessId());
            return true;
        }
    }

    /**
     * 检查是否有待审批节点
     */
    private boolean hasPendingApproval(ApprovalProcess process, String approverId) {
        if (process.getApprovalNodes() == null) {
            return false;
        }

        return process.getApprovalNodes().stream()
                .anyMatch(node -> node.isCurrentNode() &&
                        (node.getApproverId().equals(approverId) ||
                                approverId.equals(node.getDelegatedTo())));
    }

    /**
     * 创建示例审批流程
     */
    private ApprovalProcess createSampleApproval(String processId, String title,
            ApprovalType approvalType, String applicantId) {
        ApprovalProcess process = new ApprovalProcess(processId, title, approvalType, applicantId, title);
        process.setApplicantName("员工" + applicantId);
        process.setContent("这是" + title + "的详细内容");

        List<ApprovalNode> nodes = initializeApprovalNodes(approvalType, applicantId);
        process.setApprovalNodes(nodes);

        return process;
    }

    /**
     * 获取审批类型列表
     */
    public List<Map<String, Object>> getApprovalTypes() {
        return Arrays.stream(ApprovalType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取审批状态列表
     */
    public List<Map<String, Object>> getApprovalStatuses() {
        return Arrays.stream(ApprovalStatus.values())
                .map(status -> {
                    Map<String, Object> statusInfo = new HashMap<>();
                    statusInfo.put("code", status.name());
                    statusInfo.put("description", status.getDescription());
                    statusInfo.put("comment", status.getComment());
                    return statusInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取审批优先级列表
     */
    public List<Map<String, Object>> getApprovalPriorities() {
        return Arrays.stream(ApprovalPriority.values())
                .sorted((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel())) // 按优先级降序
                .map(priority -> {
                    Map<String, Object> priorityInfo = new HashMap<>();
                    priorityInfo.put("code", priority.name());
                    priorityInfo.put("description", priority.getDescription());
                    priorityInfo.put("level", priority.getLevel());
                    return priorityInfo;
                })
                .collect(Collectors.toList());
    }
}
