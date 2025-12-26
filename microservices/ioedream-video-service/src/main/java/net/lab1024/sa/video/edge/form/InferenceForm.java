package net.lab1024.sa.video.edge.form;

import jakarta.validation.constraints.NotBlank;

/**
 * 推理表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class InferenceForm {

    private String taskId;

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    private Object data;

    private Integer priority;

    private Long timeout;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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


