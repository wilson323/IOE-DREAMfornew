package net.lab1024.sa.oa.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作流任务实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity提供审计字段
 * - 使用@TableName指定表名
 * - 使用@TableId指定主键
 * - 字段命名遵循数据库规范
 * 手动实现getter/setter以解决Lombok编译问题
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@TableName("t_common_workflow_task")
public class WorkflowTaskEntity extends BaseEntity {

    /**
     * 任务ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列task_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程实例ID
     */
    @TableField("instance_id")
    private Long instanceId;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务类型
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 节点ID
     */
    @TableField("node_id")
    private String nodeId;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;

    /**
     * 受理人ID
     */
    @TableField("assignee_id")
    private Long assigneeId;

    /**
     * 受理人姓名
     */
    @TableField("assignee_name")
    private String assigneeName;

    /**
     * 原受理人ID（委派/转交时使用）
     */
    @TableField("original_assignee_id")
    private Long originalAssigneeId;

    /**
     * 原受理人姓名
     */
    @TableField("original_assignee_name")
    private String originalAssigneeName;

    /**
     * 候选用户（JSON数组）
     */
    @TableField("candidate_users")
    private String candidateUsers;

    /**
     * 候选角色（JSON数组）
     */
    @TableField("candidate_groups")
    private String candidateGroups;

    /**
     * 优先级（1-低 2-中 3-高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 截止时间
     */
    @TableField("due_time")
    private LocalDateTime dueTime;

    /**
     * 状态（1-待受理 2-处理中 3-已完成 4-已转交 5-已委派 6-已驳回）
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理结果（1-同意 2-驳回 3-转交 4-委派）
     */
    @TableField("result")
    private Integer result;

    /**
     * 处理意见
     */
    @TableField("comment")
    private String comment;

    /**
     * 任务创建时间
     */
    @TableField("task_create_time")
    private LocalDateTime taskCreateTime;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 委派时间
     */
    @TableField("delegate_time")
    private LocalDateTime delegateTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField("duration")
    private Long duration;

    /**
     * 任务变量（JSON格式）
     */
    @TableField("variables")
    private String variables;

    /**
     * Flowable任务ID
     */
    @TableField("flowable_task_id")
    private String flowableTaskId;

    /**
     * 结果（审批结果代码）
     */
    @TableField("outcome")
    private Integer outcome;

    /**
     * 完成时间
     */
    @TableField("complete_time")
    private LocalDateTime completeTime;

    /**
     * Flowable流程实例ID
     */
    @TableField("flowable_process_instance_id")
    private String flowableProcessInstanceId;

    /**
     * 受理时间
     */
    @TableField("assignee_time")
    private LocalDateTime assigneeTime;

    /**
     * 流程名称（冗余字段，JOIN查询时使用）
     */
    @TableField(exist = false)
    private String processName;

    /**
     * 超时策略（AUTO_TRANSFER-自动转交, AUTO_APPROVE-自动通过, ESCALATE-升级）
     * <p>
     * 注意：此字段可能存储在流程定义或任务变量中，需要在查询时解析
     * </p>
     */
    @TableField(exist = false)
    private String timeoutStrategy;

    // ==================== Getter/Setter Methods ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public Long getOriginalAssigneeId() { return originalAssigneeId; }
    public void setOriginalAssigneeId(Long originalAssigneeId) { this.originalAssigneeId = originalAssigneeId; }
    public String getOriginalAssigneeName() { return originalAssigneeName; }
    public void setOriginalAssigneeName(String originalAssigneeName) { this.originalAssigneeName = originalAssigneeName; }
    public String getCandidateUsers() { return candidateUsers; }
    public void setCandidateUsers(String candidateUsers) { this.candidateUsers = candidateUsers; }
    public String getCandidateGroups() { return candidateGroups; }
    public void setCandidateGroups(String candidateGroups) { this.candidateGroups = candidateGroups; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public LocalDateTime getDueTime() { return dueTime; }
    public void setDueTime(LocalDateTime dueTime) { this.dueTime = dueTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getTaskCreateTime() { return taskCreateTime; }
    public void setTaskCreateTime(LocalDateTime taskCreateTime) { this.taskCreateTime = taskCreateTime; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getDelegateTime() { return delegateTime; }
    public void setDelegateTime(LocalDateTime delegateTime) { this.delegateTime = delegateTime; }
    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    public String getFlowableTaskId() { return flowableTaskId; }
    public void setFlowableTaskId(String flowableTaskId) { this.flowableTaskId = flowableTaskId; }
    public Integer getOutcome() { return outcome; }
    public void setOutcome(Integer outcome) { this.outcome = outcome; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public String getFlowableProcessInstanceId() { return flowableProcessInstanceId; }
    public void setFlowableProcessInstanceId(String flowableProcessInstanceId) { this.flowableProcessInstanceId = flowableProcessInstanceId; }
    public LocalDateTime getAssigneeTime() { return assigneeTime; }
    public void setAssigneeTime(LocalDateTime assigneeTime) { this.assigneeTime = assigneeTime; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getTimeoutStrategy() { return timeoutStrategy; }
    public void setTimeoutStrategy(String timeoutStrategy) { this.timeoutStrategy = timeoutStrategy; }
}




