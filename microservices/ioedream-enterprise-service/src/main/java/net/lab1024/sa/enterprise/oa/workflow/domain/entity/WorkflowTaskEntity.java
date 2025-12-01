package net.lab1024.sa.enterprise.oa.workflow.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作流任务实体
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_workflow_task")
public class WorkflowTaskEntity extends BaseEntity {

    /**
     * 任务ID
     */
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    /**
     * 实例ID
     */
    private Long instanceId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务Key
     */
    private String taskKey;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：1-用户任务 2-审批任务 3-通知任务 4-系统任务
     */
    private Integer nodeType;

    /**
     * 处理人ID
     */
    private Long assigneeId;

    /**
     * 处理人姓名
     */
    private String assigneeName;

    /**
     * 候选处理人（JSON数组格式）
     */
    private String candidateUsers;

    /**
     * 候选处理组（JSON数组格式）
     */
    private String candidateGroups;

    /**
     * 状态：1-待处理 2-处理中 3-已完成 4-已转办 5-已委派 6-已驳回
     */
    private Integer status;

    /**
     * 优先级：1-低 2-普通 3-高 4-紧急
     */
    private Integer priority;

    /**
     * 任务数据（JSON格式）
     */
    private String taskData;

    /**
     * 表单数据（JSON格式）
     */
    private String formData;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 处理结果：1-同意 2-驳回 3-转办 4-委派 5-终止
     */
    private Integer result;

    /**
     * 任务创建时间（区别于BaseEntity的系统创建时间）
     */
    private LocalDateTime taskCreateTime;

    /**
     * 开始处理时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 截止时间
     */
    private LocalDateTime dueTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 原处理人ID（转办/委派时使用）
     */
    private Long originalAssigneeId;

    /**
     * 原处理人姓名（转办/委派时使用）
     */
    private String originalAssigneeName;

    /**
     * 转办/委派时间
     */
    private LocalDateTime delegateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        switch (status) {
            case 1:
                return "待处理";
            case 2:
                return "处理中";
            case 3:
                return "已完成";
            case 4:
                return "已转办";
            case 5:
                return "已委派";
            case 6:
                return "已驳回";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取节点类型描述
     */
    public String getNodeTypeDesc() {
        switch (nodeType) {
            case 1:
                return "用户任务";
            case 2:
                return "审批任务";
            case 3:
                return "通知任务";
            case 4:
                return "系统任务";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取处理结果描述
     */
    public String getResultDesc() {
        switch (result) {
            case 1:
                return "同意";
            case 2:
                return "驳回";
            case 3:
                return "转办";
            case 4:
                return "委派";
            case 5:
                return "终止";
            default:
                return "未处理";
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDesc() {
        switch (priority) {
            case 1:
                return "低";
            case 2:
                return "普通";
            case 3:
                return "高";
            case 4:
                return "紧急";
            default:
                return "普通";
        }
    }

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return status != null && status == 1;
    }

    /**
     * 是否处理中
     */
    public boolean isInProgress() {
        return status != null && status == 2;
    }

    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return status != null && status == 3;
    }

    /**
     * 是否需要处理
     */
    public boolean needsProcessing() {
        return isPending() || isInProgress();
    }

    /**
     * 是否已过期
     */
    public boolean isOverdue() {
        return dueTime != null && dueTime.isBefore(LocalDateTime.now());
    }

    /**
     * 标记为开始处理
     */
    public void markAsStarted() {
        this.status = 2;
        this.startTime = LocalDateTime.now();
    }

    /**
     * 标记为完成
     */
    public void markAsCompleted(Integer result, String comment) {
        this.status = 3;
        this.endTime = LocalDateTime.now();
        this.result = result;
        this.comment = comment;
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        } else if (this.taskCreateTime != null) {
            this.duration = java.time.Duration.between(this.taskCreateTime, this.endTime).toMillis();
        }
    }

    /**
     * 标记为转办
     */
    public void markAsDelegated(Long newAssigneeId, String newAssigneeName) {
        this.originalAssigneeId = this.assigneeId;
        this.originalAssigneeName = this.assigneeName;
        this.assigneeId = newAssigneeId;
        this.assigneeName = newAssigneeName;
        this.status = 4;
        this.delegateTime = LocalDateTime.now();
        this.result = 3;
    }

    /**
     * 标记为驳回
     */
    public void markAsRejected(String comment) {
        this.status = 6;
        this.endTime = LocalDateTime.now();
        this.result = 2;
        this.comment = comment;
    }

    /**
     * 获取任务完整描述
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getNodeTypeDesc()).append("]");
        sb.append(" ").append(taskName);
        if (assigneeName != null) {
            sb.append(" (处理人: ").append(assigneeName).append(")");
        }
        sb.append(" - ").append(getStatusDesc());
        return sb.toString();
    }
}
