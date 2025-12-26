package net.lab1024.sa.video.edge.model;

/**
 * 推理结果（video-service 边缘计算内部模型）
 * <p>
 * 说明：为边缘AI引擎与控制器提供统一的结果结构（最小字段集）。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class InferenceResult {

    private boolean success;
    private double confidence;
    private long costTime;
    private String taskType;
    private String errorMessage;
    private Object data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

