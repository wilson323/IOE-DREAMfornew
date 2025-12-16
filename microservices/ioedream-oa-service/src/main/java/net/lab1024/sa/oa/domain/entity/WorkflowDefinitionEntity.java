package net.lab1024.sa.oa.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流定义实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@TableName("t_workflow_definition")
public class WorkflowDefinitionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流程编码
     */
    private String code;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程类型：APPROVAL-审批流程, NOTIFY-通知流程
     */
    private String type;

    /**
     * 流程分类：ACCESS-门禁, VISITOR-访客, LEAVE-请假, EXPENSE-报销
     */
    private String category;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程定义JSON（节点配置）
     */
    private String definitionJson;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：DRAFT-草稿, ACTIVE-启用, DISABLED-禁用
     */
    private String status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
