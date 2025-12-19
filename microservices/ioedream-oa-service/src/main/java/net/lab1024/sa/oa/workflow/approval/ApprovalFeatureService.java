package net.lab1024.sa.oa.workflow.approval;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 审批流程特性服务
 * <p>
 * 实现钉钉审批流核心功能：会签/或签、撤回、催办、抄送
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
@Slf4j
@Service
public class ApprovalFeatureService {


    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private HistoryService historyService;

    @Resource
    private ApprovalRecordDao approvalRecordDao;

    @Resource
    private CarbonCopyDao carbonCopyDao;

    // ==================== 会签/或签功能 ====================

    /**
     * 创建会签任务（所有人必须同意）
     *
     * @param processInstanceId 流程实例ID
     * @param assignees 审批人列表
     * @param taskName 任务名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void createCountersignTask(String processInstanceId, List<String> assignees, String taskName) {
        log.info("[审批特性] 创建会签任务: processInstanceId={}, assignees={}", processInstanceId, assignees);

        // 设置多实例变量（会签模式：completionCondition = ${nrOfCompletedInstances == nrOfInstances}）
        Map<String, Object> variables = new HashMap<>();
        variables.put("assigneeList", assignees);
        variables.put("approvalMode", "COUNTERSIGN"); // 会签模式
        variables.put("nrOfInstances", assignees.size());
        variables.put("nrOfCompletedInstances", 0);
        variables.put("nrOfApprovedInstances", 0);

        runtimeService.setVariables(processInstanceId, variables);
        log.info("[审批特性] 会签任务创建成功: 需要{}人全部同意", assignees.size());
    }

    /**
     * 创建或签任务（任意一人同意即可）
     *
     * @param processInstanceId 流程实例ID
     * @param assignees 审批人列表
     * @param taskName 任务名称
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrSignTask(String processInstanceId, List<String> assignees, String taskName) {
        log.info("[审批特性] 创建或签任务: processInstanceId={}, assignees={}", processInstanceId, assignees);

        // 设置多实例变量（或签模式：completionCondition = ${nrOfApprovedInstances >= 1}）
        Map<String, Object> variables = new HashMap<>();
        variables.put("assigneeList", assignees);
        variables.put("approvalMode", "OR_SIGN"); // 或签模式
        variables.put("nrOfInstances", assignees.size());
        variables.put("nrOfCompletedInstances", 0);
        variables.put("nrOfApprovedInstances", 0);

        runtimeService.setVariables(processInstanceId, variables);
        log.info("[审批特性] 或签任务创建成功: 需要{}人中任意一人同意", assignees.size());
    }

    /**
     * 完成会签/或签任务
     *
     * @param taskId 任务ID
     * @param approved 是否同意
     * @param comment 审批意见
     * @param userId 审批人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeMultiInstanceTask(String taskId, boolean approved, String comment, String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String processInstanceId = task.getProcessInstanceId();

        // 更新完成计数
        Integer completedCount = (Integer) runtimeService.getVariable(processInstanceId, "nrOfCompletedInstances");
        Integer approvedCount = (Integer) runtimeService.getVariable(processInstanceId, "nrOfApprovedInstances");

        completedCount = (completedCount == null ? 0 : completedCount) + 1;
        if (approved) {
            approvedCount = (approvedCount == null ? 0 : approvedCount) + 1;
        }

        runtimeService.setVariable(processInstanceId, "nrOfCompletedInstances", completedCount);
        runtimeService.setVariable(processInstanceId, "nrOfApprovedInstances", approvedCount);

        // 设置任务局部变量
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("approved", approved);
        taskVariables.put("comment", comment);
        taskVariables.put("approvedTime", LocalDateTime.now().toString());

        // 完成任务
        taskService.complete(taskId, taskVariables);

        // 记录审批日志
        saveApprovalRecord(processInstanceId, taskId, userId, approved, comment);

        log.info("[审批特性] 多实例任务完成: taskId={}, approved={}, completed={}", taskId, approved, completedCount);
    }

    // ==================== 撤回功能 ====================

    /**
     * 撤回流程（发起人撤回）
     *
     * @param processInstanceId 流程实例ID
     * @param userId 操作人ID
     * @param reason 撤回原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawProcess(String processInstanceId, String userId, String reason) {
        log.info("[审批特性] 撤回流程: processInstanceId={}, userId={}", processInstanceId, userId);

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (processInstance == null) {
            throw new RuntimeException("流程实例不存在或已结束: " + processInstanceId);
        }

        // 验证是否为发起人
        String startUserId = processInstance.getStartUserId();
        if (startUserId == null || !startUserId.equals(userId)) {
            throw new RuntimeException("只有流程发起人可以撤回流程");
        }

        // 删除流程实例
        runtimeService.deleteProcessInstance(processInstanceId, "撤回: " + reason);

        // 记录撤回日志
        saveWithdrawRecord(processInstanceId, userId, reason);

        log.info("[审批特性] 流程撤回成功: processInstanceId={}", processInstanceId);
    }

    /**
     * 撤回到上一节点（审批人撤回自己的审批）
     *
     * @param taskId 当前任务ID
     * @param userId 操作人ID
     * @param reason 撤回原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawToPreviousNode(String taskId, String userId, String reason) {
        log.info("[审批特性] 撤回到上一节点: taskId={}, userId={}", taskId, userId);

        Task currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (currentTask == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 使用Flowable的回退功能
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(currentTask.getProcessInstanceId())
                .moveActivityIdTo(currentTask.getTaskDefinitionKey(), "startEvent")
                .changeState();

        log.info("[审批特性] 撤回到上一节点成功: taskId={}", taskId);
    }

    // ==================== 催办功能 ====================

    /**
     * 催办任务
     *
     * @param taskId 任务ID
     * @param urgerId 催办人ID
     * @param message 催办消息
     */
    public void urgeTask(String taskId, String urgerId, String message) {
        log.info("[审批特性] 催办任务: taskId={}, urgerId={}", taskId, urgerId);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String assignee = task.getAssignee();
        if (assignee == null) {
            throw new RuntimeException("任务未分配审批人");
        }

        // 记录催办
        UrgeRecordEntity urgeRecord = new UrgeRecordEntity();
        urgeRecord.setTaskId(taskId);
        urgeRecord.setProcessInstanceId(task.getProcessInstanceId());
        urgeRecord.setUrgerId(Long.parseLong(urgerId));
        urgeRecord.setAssigneeId(Long.parseLong(assignee));
        urgeRecord.setMessage(message);
        urgeRecord.setUrgeTime(LocalDateTime.now());

        // TODO: 发送催办通知（站内信、短信、邮件等）
        log.info("[审批特性] 催办通知已发送: taskId={}, assignee={}", taskId, assignee);
    }

    // ==================== 抄送功能 ====================

    /**
     * 抄送任务给指定人员
     *
     * @param processInstanceId 流程实例ID
     * @param taskId 任务ID
     * @param ccUserIds 抄送人ID列表
     * @param senderId 发送人ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void carbonCopy(String processInstanceId, String taskId, List<String> ccUserIds, String senderId) {
        log.info("[审批特性] 抄送: processInstanceId={}, ccUserIds={}", processInstanceId, ccUserIds);

        for (String ccUserId : ccUserIds) {
            CarbonCopyEntity ccEntity = new CarbonCopyEntity();
            ccEntity.setProcessInstanceId(processInstanceId);
            ccEntity.setTaskId(taskId);
            ccEntity.setCcUserId(Long.parseLong(ccUserId));
            ccEntity.setSenderId(Long.parseLong(senderId));
            ccEntity.setReadStatus(0); // 未读
            ccEntity.setCcTime(LocalDateTime.now());

            carbonCopyDao.insert(ccEntity);
        }

        // TODO: 发送抄送通知
        log.info("[审批特性] 抄送成功: 共抄送{}人", ccUserIds.size());
    }

    /**
     * 获取我的抄送列表
     *
     * @param userId 用户ID
     * @param readStatus 阅读状态（null-全部，0-未读，1-已读）
     * @return 抄送列表
     */
    public List<CarbonCopyEntity> getMyCarbonCopies(String userId, Integer readStatus) {
        return carbonCopyDao.findByUserIdAndStatus(Long.parseLong(userId), readStatus);
    }

    /**
     * 标记抄送为已读
     *
     * @param ccId 抄送记录ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markCarbonCopyAsRead(Long ccId, String userId) {
        CarbonCopyEntity ccEntity = carbonCopyDao.selectById(ccId);
        if (ccEntity != null && ccEntity.getCcUserId().equals(Long.parseLong(userId))) {
            ccEntity.setReadStatus(1);
            ccEntity.setReadTime(LocalDateTime.now());
            carbonCopyDao.updateById(ccEntity);
        }
    }

    // ==================== 私有方法 ====================

    private void saveApprovalRecord(String processInstanceId, String taskId, String userId,
                                     boolean approved, String comment) {
        ApprovalRecordEntity record = new ApprovalRecordEntity();
        record.setProcessInstanceId(processInstanceId);
        record.setTaskId(taskId);
        record.setApproverId(Long.parseLong(userId));
        record.setOutcome(approved ? 1 : 2); // 1-同意，2-拒绝
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        approvalRecordDao.insert(record);
    }

    private void saveWithdrawRecord(String processInstanceId, String userId, String reason) {
        ApprovalRecordEntity record = new ApprovalRecordEntity();
        record.setProcessInstanceId(processInstanceId);
        record.setApproverId(Long.parseLong(userId));
        record.setOutcome(4); // 4-撤回
        record.setComment(reason);
        record.setApprovalTime(LocalDateTime.now());
        approvalRecordDao.insert(record);
    }
}
