package net.lab1024.sa.common.workflow.executor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点执行上下文
 * <p>
 * 封装节点执行时的上下文信息
 * 包含实例ID、执行数据、节点配置等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class NodeExecutionContext {

    /**
     * 流程实例ID
     */
    private String instanceId;

    /**
     * 执行数据
     */
    private Map<String, Object> executionData;

    /**
     * 节点配置
     */
    private Map<String, Object> nodeConfig;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 执行开始时间
     */
    private long startTime = System.currentTimeMillis();

    /**
     * 执行用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 跟踪ID
     */
    private String traceId;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties = new ConcurrentHashMap<>();

    /**
     * 构造函数
     */
    public NodeExecutionContext() {
        this.executionData = new ConcurrentHashMap<>();
        this.extendedProperties = new ConcurrentHashMap<>();
    }

    /**
     * 添加执行数据
     */
    public NodeExecutionContext addExecutionData(String key, Object value) {
        if (this.executionData == null) {
            this.executionData = new ConcurrentHashMap<>();
        }
        this.executionData.put(key, value);
        return this;
    }

    /**
     * 获取执行数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getExecutionData(String key) {
        if (this.executionData == null) {
            return null;
        }
        return (T) this.executionData.get(key);
    }

    /**
     * 添加扩展属性
     */
    public NodeExecutionContext addExtendedProperty(String key, Object value) {
        if (this.extendedProperties == null) {
            this.extendedProperties = new ConcurrentHashMap<>();
        }
        this.extendedProperties.put(key, value);
        return this;
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtendedProperty(String key) {
        if (this.extendedProperties == null) {
            return null;
        }
        return (T) this.extendedProperties.get(key);
    }

    /**
     * 设置执行数据（支持方法链）
     */
    public NodeExecutionContext setExecutionData(Map<String, Object> executionData) {
        this.executionData = executionData != null ? executionData : new ConcurrentHashMap<>();
        return this;
    }

    /**
     * 获取执行耗时
     */
    public long getExecutionDuration() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 是否超时
     */
    public boolean isTimeout(long timeoutMs) {
        return getExecutionDuration() > timeoutMs;
    }
}