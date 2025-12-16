package net.lab1024.sa.oa.workflow.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 工作流任务实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity提供审计字段
 * - 使用@TableName指定表名
 * - 使用@TableId指定主键
 * - 字段命名遵循数据库规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}




