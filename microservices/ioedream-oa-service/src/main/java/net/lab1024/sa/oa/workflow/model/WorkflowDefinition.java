package net.lab1024.sa.oa.workflow.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 工作流定义
 * <p>
 * 定义工作流的完整结构
 * 包含节点、转换、配置等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class WorkflowDefinition {

    /**
     * 流程ID
     */
    private String id;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 流程版本
     */
    private String version = "1.0";

    /**
     * 流程分类
     */
    private String category;

    /**
     * 流程节点列表
     */
    private List<WorkflowNode> nodes;

    /**
     * 全局配置
     */
    private Map<String, Object> globalConfig;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 添加节点
     */
    public WorkflowDefinition addNode(WorkflowNode node) {
        if (nodes == null) {
            nodes = new java.util.ArrayList<>();
        }
        nodes.add(node);
        return this;
    }

    /**
     * 获取开始节点
     */
    public WorkflowNode getStartNode() {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        return nodes.get(0);
    }

    /**
     * 获取结束节点
     */
    public WorkflowNode getEndNode() {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        return nodes.get(nodes.size() - 1);
    }
}



