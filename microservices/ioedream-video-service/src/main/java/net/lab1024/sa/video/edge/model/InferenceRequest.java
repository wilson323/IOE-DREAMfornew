package net.lab1024.sa.video.edge.model;

import java.util.Map;

/**
 * 推理请求（video-service 边缘计算内部模型）
 * <p>
 * 说明：与公共模型解耦，按当前边缘模块代码使用的字段提供最小实现。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class InferenceRequest {

    private String taskId;
    private String taskType;
    private String modelType;
    private Object data;
    private String deviceId;
    private Map<String, Object> parameters;
    private Integer priority;
    private Long timeout;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}

