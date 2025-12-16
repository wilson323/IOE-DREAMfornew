package net.lab1024.sa.oa.workflow.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 工作流节点
 * <p>
 * 定义工作流中的单个节点
 * 包含节点类型、配置、转换条件等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class WorkflowNode {

    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 节点配置
     */
    private Map<String, Object> config;

    /**
     * 是否异步执行
     */
    private boolean async = false;

    /**
     * 出错时是否继续
     */
    private boolean continueOnError = false;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 节点转换
     */
    private List<NodeTransition> transitions;

    /**
     * 节点位置（用于可视化）
     */
    private NodePosition position;

    /**
     * 添加转换
     */
    public WorkflowNode addTransition(NodeTransition transition) {
        if (transitions == null) {
            transitions = new java.util.ArrayList<>();
        }
        transitions.add(transition);
        return this;
    }

    /**
     * 添加配置
     */
    public WorkflowNode addConfig(String key, Object value) {
        if (config == null) {
            config = new java.util.HashMap<>();
        }
        config.put(key, value);
        return this;
    }

    /**
     * 节点位置信息
     */
    @Data
    @Accessors(chain = true)
    public static class NodePosition {
        private Integer x;
        private Integer y;
        private Integer width = 120;
        private Integer height = 80;
    }
}



