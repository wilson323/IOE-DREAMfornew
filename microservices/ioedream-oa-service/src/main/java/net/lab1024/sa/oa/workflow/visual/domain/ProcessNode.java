package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 流程节点实体类
 * <p>
 * 表示工作流中的一个节点，包含节点的基本信息、配置和样式
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
public class ProcessNode {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型
     * START_EVENT - 开始事件
     * END_EVENT - 结束事件
     * USER_TASK - 用户任务
     * SERVICE_TASK - 服务任务
     * EXCLUSIVE_GATEWAY - 排他网关
     * PARALLEL_GATEWAY - 并行网关
     */
    private NodeType nodeType;

    /**
     * 节点描述
     */
    private String nodeDescription;

    /**
     * 节点配置Schema
     */
    private NodeConfigSchema config;

    /**
     * 节点位置X坐标
     */
    private Double positionX;

    /**
     * 节点位置Y坐标
     */
    private Double positionY;

    /**
     * 节点宽度
     */
    private Double width;

    /**
     * 节点高度
     */
    private Double height;

    /**
     * 节点样式（JSON格式）
     */
    private String style;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 节点类型枚举
     */
    public enum NodeType {
        /**
         * 开始事件
         */
        START_EVENT,

        /**
         * 结束事件
         */
        END_EVENT,

        /**
         * 用户任务
         */
        USER_TASK,

        /**
         * 服务任务
         */
        SERVICE_TASK,

        /**
         * 排他网关
         */
        EXCLUSIVE_GATEWAY,

        /**
         * 并行网关
         */
        PARALLEL_GATEWAY,

        /**
         * 包容网关
         */
        INCLUSIVE_GATEWAY,

        /**
         * 定时器事件
         */
        TIMER_EVENT,

        /**
         * 信号事件
         */
        SIGNAL_EVENT,

        /**
         * 边界事件
         */
        BOUNDARY_EVENT
    }

    /**
     * 节点配置Schema
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeConfigSchema {

        /**
         * 是否允许自动跳过
         */
        private Boolean allowSkip;

        /**
         * 是否需要审批
         */
        private Boolean requireApproval;

        /**
         * 审批人类型
         */
        private String approverType;

        /**
         * 审批人配置
         */
        private String approverConfig;

        /**
         * 表单Key
         */
        private String formKey;

        /**
         * 是否多实例
         */
        private Boolean isMultiInstance;

        /**
         * 多实例类型：SEQUENTIAL-并行 PARALLEL-串行
         */
        private String multiInstanceType;

        /**
         * 完成条件
         */
        private String completionCondition;

        /**
         * 脚本配置
         */
        private String scriptConfig;

        /**
         * 任务监听器配置
         */
        private String taskListenerConfig;

        /**
         * 扩展配置
         */
        private Map<String, Object> extendedConfig;
    }
}
