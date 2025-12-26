package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 流程边实体类
 * <p>
 * 表示工作流中连接两个节点的边（Sequence Flow）
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
public class ProcessEdge {

    /**
     * 边ID
     */
    private String edgeId;

    /**
     * 边名称
     */
    private String edgeName;

    /**
     * 源节点ID
     */
    private String sourceNodeId;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 边类型
     * NORMAL - 普通边
     * CONDITIONAL - 条件边
     * DEFAULT - 默认边
     */
    private EdgeType edgeType;

    /**
     * 边条件表达式
     */
    private EdgeCondition condition;

    /**
     * 边描述
     */
    private String edgeDescription;

    /**
     * 边的样式配置（JSON格式）
     */
    private String style;

    /**
     * 是否高亮显示
     */
    private Boolean isHighlighted;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 边类型枚举
     */
    public enum EdgeType {
        /**
         * 普通边
         */
        NORMAL,

        /**
         * 条件边
         */
        CONDITIONAL,

        /**
         * 默认边
         */
        DEFAULT,

        /**
         * 异常边
         */
        EXCEPTION
    }

    /**
     * 边条件配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EdgeCondition {

        /**
         * 条件表达式（SpEL或JUEL）
         */
        private String expression;

        /**
         * 条件类型
         * EXPRESSION - 表达式
         * SCRIPT - 脚本
         * DELEGATE - 委托类
         */
        private String conditionType;

        /**
         * 条件名称
         */
        private String conditionName;

        /**
         * 条件描述
         */
        private String conditionDescription;

        /**
         * 是否为默认条件
         */
        private Boolean isDefault;

        /**
         * 优先级（数字越小优先级越高）
         */
        private Integer priority;
    }
}
