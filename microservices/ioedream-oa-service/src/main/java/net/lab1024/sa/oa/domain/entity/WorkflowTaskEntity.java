package net.lab1024.sa.oa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流任务实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@TableName("t_workflow_task")
public class WorkflowTaskEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：START-开始, APPROVAL-审批, COUNTERSIGN-会签, OR_SIGN-或签, END-结束
     */
    private String nodeType;

    /**
     * 审批人ID
     */
    private Long assigneeId;

    /**
     * 审批人姓名
     */
    private String assigneeName;

    /**
     * 任务状态：1-待受理, 2-处理中, 3-已完成, 4-已转交, 5-已委派, 6-已驳回
     */
    private Integer status;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 截止时间
     */
    private LocalDateTime dueTime;

    /**
     * 转办人ID
     */
    private Long transferToId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程变量（JSON格式）
     */
    private String variables;

    // --- Added Fields for Service Compatibility ---

    /**
     * Flowable 任务ID
     */
    private String flowableTaskId;

    /**
     * Flowable 流程实例ID
     */
    private String flowableProcessInstanceId;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 审批结果/输出 (1-同意, 2-驳回, etc.)
     */
    private Integer result;

    /**
     * 任务处理结果 (与result类似，某些逻辑使用)
     */
    private Integer outcome;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 分配时间
     */
    private LocalDateTime assigneeTime;

    /**
     * 耗时(毫秒/秒)
     */
    private Long duration;

    /**
     * 原始审批人ID (转办前)
     */
    private Long originalAssigneeId;

    /**
     * 原始审批人姓名
     */
    private String originalAssigneeName;

    
    // --- Compatibility Methods ---

    public void setTaskType(String taskType) {
        this.nodeType = taskType;
    }

    public String getTaskType() {
        return this.nodeType;
    }

    public void setTaskCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getTaskCreateTime() {
        return this.createTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.endTime = completeTime;
    }

    public LocalDateTime getCompleteTime() {
        return this.endTime;
    }

    // Compatibility for getStatus() returning Integer (already Integer above)

}
