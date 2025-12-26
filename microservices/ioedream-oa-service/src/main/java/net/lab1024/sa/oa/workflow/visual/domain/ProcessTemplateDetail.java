package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 流程模板详情类
 * <p>
 * 包含流程模板的完整信息，包括节点、边、配置等
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTemplateDetail {

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 模板Key
     */
    private String templateKey;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String templateDescription;

    /**
     * 模板分类
     */
    private String templateCategory;

    /**
     * 流程定义ID（对应Flowable的流程定义ID）
     */
    private String processDefinitionId;

    /**
     * 流程定义Key
     */
    private String processDefinitionKey;

    /**
     * 流程版本
     */
    private Integer version;

    /**
     * 是否为主版本
     */
    private Boolean isMainVersion;

    /**
     * 节点列表
     */
    private List<ProcessNode> nodes;

    /**
     * 边列表
     */
    private List<ProcessEdge> edges;

    /**
     * 全局表单配置（JSON格式）
     */
    private String globalFormConfig;

    /**
     * 流程变量配置（JSON格式）
     */
    private String processVariablesConfig;

    /**
     * 监听器配置（JSON格式）
     */
    private String listenerConfig;

    /**
     * 权限配置（JSON格式）
     */
    private String permissionConfig;

    /**
     * 通知配置（JSON格式）
     */
    private String notificationConfig;

    /**
     * 状态：1-草稿 2-发布 3-归档 4-停用
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人ID
     */
    private Long publishUserId;

    /**
     * 发布人姓名
     */
    private String publishUserName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 是否可发起：true-可发起 false-不可发起
     */
    private Boolean canStart;

    /**
     * 是否可挂起：true-可挂起 false-不可挂起
     */
    private Boolean canSuspend;

    /**
     * 是否可删除：true-可删除 false-不可删除
     */
    private Boolean canDelete;

    /**
     * 使用次数统计
     */
    private Long usageCount;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedTime;
}
