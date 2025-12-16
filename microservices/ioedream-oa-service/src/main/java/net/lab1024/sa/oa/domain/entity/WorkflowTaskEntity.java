package net.lab1024.sa.oa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
     * 任务状态：PENDING-待处理, APPROVED-已通过, REJECTED-已拒绝, TRANSFERRED-已转办, TIMEOUT-已超时
     */
    private String status;

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
}
