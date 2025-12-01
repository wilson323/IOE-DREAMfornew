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
 * 工作流实例实体
 * 严格遵循repowiki规范
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_workflow_instance")
public class WorkflowInstanceEntity extends BaseEntity {

    /**
     * 实例ID
     */
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;

    /**
     * 流程定义ID
     */
    private Long processDefinitionId;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程Key
     */
    private String processKey;

    /**
     * 实例标题
     */
    private String title;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    private String initiatorName;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 状态：1-进行中 2-已完成 3-已终止 4-已挂起
     */
    private Integer status;

    /**
     * 优先级：1-低 2-普通 3-高 4-紧急
     */
    private Integer priority;

    /**
     * 业务数据（JSON格式）
     */
    private String businessData;

    /**
     * 表单数据（JSON格式）
     */
    private String formData;

    /**
     * 流程变量（JSON格式）
     */
    private String processVariables;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 挂起时间
     */
    private LocalDateTime suspendTime;

    /**
     * 预计完成时间
     */
    private LocalDateTime estimatedEndTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 原因
     */
    private String reason;

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
                return "进行中";
            case 2:
                return "已完成";
            case 3:
                return "已终止";
            case 4:
                return "已挂起";
            default:
                return "未知状态";
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
     * 是否已完成
     */
    public boolean isCompleted() {
        return status != null && status == 2;
    }

    /**
     * 是否进行中
     */
    public boolean isInProgress() {
        return status != null && status == 1;
    }

    /**
     * 是否已挂起
     */
    public boolean isSuspended() {
        return status != null && status == 4;
    }

    /**
     * 标记为开始
     */
    public void markAsStarted(String currentNodeId, String currentNodeName) {
        this.status = 1;
        this.startTime = LocalDateTime.now();
        this.currentNodeId = currentNodeId;
        this.currentNodeName = currentNodeName;
    }

    /**
     * 标记为完成
     */
    public void markAsCompleted() {
        this.status = 2;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 标记为终止
     */
    public void markAsTerminated(String reason) {
        this.status = 3;
        this.endTime = LocalDateTime.now();
        this.reason = reason;
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 标记为挂起
     */
    public void markAsSuspended(String reason) {
        this.status = 4;
        this.suspendTime = LocalDateTime.now();
        this.reason = reason;
    }

    /**
     * 更新当前节点
     */
    public void updateCurrentNode(String nodeId, String nodeName) {
        this.currentNodeId = nodeId;
        this.currentNodeName = nodeName;
    }
}
