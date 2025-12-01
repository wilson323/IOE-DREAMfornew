package net.lab1024.sa.admin.module.oa.workflow.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工作流实例实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WorkflowInstanceEntity extends BaseEntity {

    /**
     * 实例ID
     */
    private Long instanceId;

    /**
     * 流程定义ID
     */
    private Long definitionId;

    /**
     * 流程编码
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    
    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 业务主键
     */
    private String businessKey;

    /**
     * 发起人ID
     */
    private Long startUserId;

    /**
     * 发起人姓名
     */
    private String startUserName;

    /**
     * 发起人部门ID
     */
    private Long startDeptId;

    /**
     * 发起人部门名称
     */
    private String startDeptName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 流程状态 (RUNNING-运行中, COMPLETED-已完成, SUSPENDED-已挂起, TERMINATED-已终止)
     */
    private String status;

    /**
     * 当前活动节点ID
     */
    private String currentActivityId;

    /**
     * 当前活动节点名称
     */
    private String currentActivityName;

    /**
     * 流程变量JSON
     */
    private String processVariables;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 流程持续时间(毫秒)
     */
    private Long duration;

    /**
     * 删除原因 (如: 流程被撤销)
     */
    private String deleteReason;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 优先级 (1-低, 2-中, 3-高, 4-紧急)
     */
    private Integer priority;

    /**
     * 预计完成时间
     */
    private LocalDateTime dueDate;

    /**
     * 备注
     */
    private String remark;
}