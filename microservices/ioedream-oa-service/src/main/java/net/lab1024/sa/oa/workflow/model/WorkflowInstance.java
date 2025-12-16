package net.lab1024.sa.oa.workflow.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流实例
 * <p>
 * 工作流运行时的实例对象
 * 包含实例状态、执行数据、历史记录等信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class WorkflowInstance {

    /**
     * 实例ID
     */
    private String id;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 实例状态
     */
    private WorkflowInstanceStatus status;

    /**
     * 当前节点ID
     */
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    private String currentNodeName;

    /**
     * 当前节点索引
     */
    private Integer currentNodeIndex = 0;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行数据
     */
    private Map<String, Object> executionData;

    /**
     * 业务数据
     */
    private Map<String, Object> businessData;

    /**
     * 发起人
     */
    private String initiator;

    /**
     * 发起部门
     */
    private String initiatorDept;

    /**
     * 优先级
     */
    private Integer priority = 1;

    /**
     * 标签
     */
    private java.util.Set<String> tags;

    /**
     * 实例变量
     */
    private Map<String, Object> variables;

    /**
     * 添加执行数据
     */
    public WorkflowInstance addExecutionData(String key, Object value) {
        if (executionData == null) {
            executionData = new java.util.HashMap<>();
        }
        executionData.put(key, value);
        return this;
    }

    /**
     * 添加业务数据
     */
    public WorkflowInstance addBusinessData(String key, Object value) {
        if (businessData == null) {
            businessData = new java.util.HashMap<>();
        }
        businessData.put(key, value);
        return this;
    }

    /**
     * 添加变量
     */
    public WorkflowInstance addVariable(String key, Object value) {
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        variables.put(key, value);
        return this;
    }

    /**
     * 添加标签
     */
    public WorkflowInstance addTag(String tag) {
        if (tags == null) {
            tags = new java.util.HashSet<>();
        }
        tags.add(tag);
        return this;
    }

    /**
     * 获取执行时长（毫秒）
     */
    public Long getDurationMs() {
        if (startTime == null) {
            return null;
        }
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMillis();
    }

    /**
     * 是否为活跃状态
     */
    public boolean isActive() {
        return status == WorkflowInstanceStatus.RUNNING || status == WorkflowInstanceStatus.SUSPENDED;
    }

    /**
     * 是否为完成状态
     */
    public boolean isCompleted() {
        return status == WorkflowInstanceStatus.COMPLETED || status == WorkflowInstanceStatus.TERMINATED;
    }
}



