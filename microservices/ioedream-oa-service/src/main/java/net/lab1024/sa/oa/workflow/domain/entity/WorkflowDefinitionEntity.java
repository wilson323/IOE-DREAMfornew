package net.lab1024.sa.oa.workflow.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工作流定义实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WorkflowDefinitionEntity extends BaseEntity {

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
     * BPMN XML定义
     */
    private String bpmnXml;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程分类 (LEAVE-请假, EXPENSE-报销, PURCHASE-采购, CONTRACT-合同)
     */
    private String category;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 发布状态 (DRAFT-草稿, PUBLISHED-已发布, DISABLED-已禁用)
     */
    private String status;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expiryTime;

    /**
     * 流程图标
     */
    private String icon;

    /**
     * 表单定义JSON
     */
    private String formDefinition;

    /**
     * 流程变量定义JSON
     */
    private String variableDefinition;

    /**
     * 是否为最新版本 (0-否, 1-是)
     */
    private Integer isLatest;

    /**
     * 流程实例数量
     */
    private Integer instanceCount;

    /**
     * 最后部署时间
     */
    private LocalDateTime lastDeployTime;

    /**
     * 备注
     */
    private String remark;
}