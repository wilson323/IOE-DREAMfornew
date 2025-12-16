package net.lab1024.sa.common.workflow.executor;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点执行上下文
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class NodeExecutionContext {

    private String instanceId;
    private Map<String, Object> executionData;
    private Map<String, Object> nodeConfig;
    private String nodeId;
    private String nodeName;
    private long startTime = System.currentTimeMillis();
    private Long userId;
    private String tenantId;
    private String traceId;
    private Map<String, Object> extendedProperties = new ConcurrentHashMap<>();

    public NodeExecutionContext() {
        this.executionData = new ConcurrentHashMap<>();
        this.extendedProperties = new ConcurrentHashMap<>();
    }

    public NodeExecutionContext addExecutionData(String key, Object value) {
        if (this.executionData == null) {
            this.executionData = new ConcurrentHashMap<>();
        }
        this.executionData.put(key, value);
        return this;
    }

    @SuppressWarnings({"unchecked", "null"})
    @Nullable
    public <T> T getExecutionData(String key) {
        if (this.executionData == null) {
            return null;
        }
        return (T) this.executionData.get(key);
    }

    public NodeExecutionContext addExtendedProperty(String key, Object value) {
        if (this.extendedProperties == null) {
            this.extendedProperties = new ConcurrentHashMap<>();
        }
        this.extendedProperties.put(key, value);
        return this;
    }

    @SuppressWarnings({"unchecked", "null"})
    @Nullable
    public <T> T getExtendedProperty(String key) {
        if (this.extendedProperties == null) {
            return null;
        }
        return (T) this.extendedProperties.get(key);
    }

    public NodeExecutionContext setExecutionData(Map<String, Object> executionData) {
        this.executionData = executionData != null ? executionData : new ConcurrentHashMap<>();
        return this;
    }

    public long getExecutionDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public boolean isTimeout(long timeoutMs) {
        return getExecutionDuration() > timeoutMs;
    }
}
