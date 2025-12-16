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
 * 工作流实例实体
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
@TableName("t_common_workflow_instance")
public class WorkflowInstanceEntity extends BaseEntity {

    /**
     * 实例ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列instance_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long id;

    /**
     * 流程定义ID
     */
    @TableField("process_definition_id")
    private Long processDefinitionId;

    /**
     * 流程编码
     */
    @TableField("process_key")
    private String processKey;

    /**
     * 流程名称
     */
    @TableField("process_name")
    private String processName;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 业务ID
     */
    @TableField("business_id")
    private Long businessId;

    /**
     * 发起人ID
     */
    @TableField("initiator_id")
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    @TableField("initiator_name")
    private String initiatorName;

    /**
     * 当前节点ID
     */
    @TableField("current_node_id")
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    @TableField("current_node_name")
    private String currentNodeName;

    /**
     * 状态（1-运行中 2-已完成 3-已终止 4-已挂起）
     */
    @TableField("status")
    private Integer status;

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
     * 挂起时间
     */
    @TableField("suspend_time")
    private LocalDateTime suspendTime;

    /**
     * 持续时间（毫秒）
     */
    @TableField("duration")
    private Long duration;

    /**
     * 原因/备注
     */
    @TableField("reason")
    private String reason;

    /**
     * 流程变量（JSON格式）
     */
    @TableField("variables")
    private String variables;
}




