package net.lab1024.sa.oa.workflow.domain.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 审批任务视图对象
 * <p>
 * 严格遵循CLAUDE.md规范： - VO类用于数据传输，不包含业务逻辑 - 字段命名清晰，符合业务语义 - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class ApprovalTaskVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 受理人ID
     */
    private Long assigneeId;

    /**
     * 受理人姓名
     */
    private String assigneeName;

    /**
     * 原受理人ID
     */
    private Long originalAssigneeId;

    /**
     * 原受理人姓名
     */
    private String originalAssigneeName;

    /**
     * 优先级（1-低 2-中 3-高）
     */
    private Integer priority;

    /**
     * 截止时间
     */
    private LocalDateTime dueTime;

    /**
     * 状态（1-待受理 2-处理中 3-已完成 4-已转交 5-已委派 6-已驳回）
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 处理结果（1-同意 2-驳回 3-转交 4-委派）
     */
    private Integer result;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 任务创建时间
     */
    private LocalDateTime taskCreateTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 持续时间（毫秒）
     */
    private Long duration;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    private String initiatorName;

    public String getTaskName () {
        return taskName;
    }
}
