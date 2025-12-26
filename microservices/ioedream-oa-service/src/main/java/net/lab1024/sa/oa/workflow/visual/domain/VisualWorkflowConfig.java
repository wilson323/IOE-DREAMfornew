package net.lab1024.sa.oa.workflow.visual.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 可视化工作流配置实体类
 * <p>
 * 存储可视化工作流的配置信息，包括流程定义、节点配置、边配置等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@TableName("t_oa_visual_workflow_config")
public class VisualWorkflowConfig {

    /**
     * 配置ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long configId;

    /**
     * 流程定义ID（对应Flowable的流程定义ID）
     */
    private String processDefinitionId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 配置数据（JSON格式）
     * 存储完整的可视化配置，包括：
     * - 节点列表
     * - 边列表
     * - 布局信息
     * - 样式配置
     */
    private String configData;

    /**
     * 配置版本
     */
    private Integer version;

    /**
     * 是否为主版本
     */
    private Boolean isMainVersion;

    /**
     * 状态：1-草稿 2-发布 3-归档
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展属性（JSON格式）
     */
    private String extendedAttributes;

    /**
     * 是否删除：0-未删除 1-已删除
     */
    private Integer deletedFlag;
}
