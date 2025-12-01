package net.lab1024.sa.admin.module.oa.workflow.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工作流任务实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WorkflowTaskEntity extends BaseEntity {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 流程定义ID
     */
    private Long definitionId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务Key
     */
    private String taskKey;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务类型 (USER-用户任务, SERVICE-服务任务, SCRIPT-脚本任务)
     */
    private String taskType;

    /**
     * 处理人ID
     */
    private Long assigneeId;

    /**
     * 处理人姓名
     */
    private String assigneeName;

    /**
     * 候选处理人组 (JSON格式，存储用户ID或部门ID列表)
     */
    private String candidateGroups;

    /**
     * 候选处理人 (JSON格式，存储用户ID列表)
     */
    private String candidateUsers;

    /**
     * 任务状态 (ACTIVE-活跃, SUSPENDED-已挂起, COMPLETED-已完成)
     */
    private String status;

    /**
     * 优先级 (1-低, 2-中, 3-高, 4-紧急)
     */
    private Integer priority;

    
    /**
     * 到期时间
     */
    private LocalDateTime dueTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 持续时间(毫秒)
     */
    private Long duration;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 处理结果 (APPROVE-同意, REJECT-拒绝, TRANSFER-转交, DELEGATE-委派)
     */
    private String outcome;

    /**
     * 处理人IP地址
     */
    private String processUserIp;

    /**
     * 父任务ID (子流程任务)
     */
    private Long parentTaskId;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 任务变量JSON
     */
    private String taskVariables;

    /**
     * 流程变量JSON
     */
    private String processVariables;

    /**
     * 执行ID (用于流程追踪)
     */
    private String executionId;

    /**
     * 流程定义Key
     */
    private String processKey;

    /**
     * 流程实例名称
     */
    private String instanceName;

    /**
     * 发起人ID
     */
    private Long startUserId;

    /**
     * 发起人姓名
     */
    private String startUserName;

    /**
     * 任务表单Key
     */
    private String formKey;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 备注
     */
    private String remark;
}