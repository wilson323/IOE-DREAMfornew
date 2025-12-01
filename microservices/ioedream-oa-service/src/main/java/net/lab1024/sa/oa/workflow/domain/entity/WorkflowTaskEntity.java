package net.lab1024.sa.oa.workflow.domain.entity;

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
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}
