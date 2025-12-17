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
 * 工作流定义实体
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
@TableName("t_common_workflow_definition")
public class WorkflowDefinitionEntity extends BaseEntity {

    /**
     * 定义ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程编码（唯一标识）
     */
    @TableField("process_key")
    private String processKey;

    /**
     * 流程名称
     */
    @TableField("process_name")
    private String processName;

    /**
     * 流程分类
     */
    @TableField("category")
    private String category;

    /**
     * 流程版本
     */
    @TableField("version")
    private Integer version;

    /**
     * 是否最新版本（1-是 0-否）
     */
    @TableField("is_latest")
    private Integer isLatest;

    /**
     * 流程描述
     */
    @TableField("description")
    private String description;

    /**
     * 流程定义（BPMN XML）
     */
    @TableField("process_definition")
    private String processDefinition;

    /**
     * 发布状态（DRAFT-草稿 PUBLISHED-已发布 DISABLED-已禁用）
     */
    @TableField("status")
    private String status;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    /**
     * 最后部署时间
     */
    @TableField("last_deploy_time")
    private LocalDateTime lastDeployTime;

    /**
     * 流程实例数量
     */
    @TableField("instance_count")
    private Integer instanceCount;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * Flowable部署ID
     */
    @TableField("flowable_deployment_id")
    private String flowableDeploymentId;

    /**
     * Flowable流程定义ID
     */
    @TableField("flowable_process_def_id")
    private String flowableProcessDefId;
}




