package net.lab1024.sa.common.workflow.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 节点转换
 * <p>
 * 定义节点之间的转换关系
 * 包含条件表达式、目标节点等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class NodeTransition {

    /**
     * 转换ID
     */
    private String id;

    /**
     * 转换名称
     */
    private String name;

    /**
     * 条件表达式
     */
    private String condition;

    /**
     * 目标节点ID
     */
    private String targetNodeId;

    /**
     * 转换描述
     */
    private String description;

    /**
     * 转换优先级（数字越大优先级越高）
     */
    private Integer priority = 0;

    /**
     * 是否为默认转换
     */
    private Boolean defaultTransition = false;

    /**
     * 转换执行时的数据转换
     */
    private java.util.Map<String, Object> dataTransformation;
}