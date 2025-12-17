package net.lab1024.sa.oa.workflow.executor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 节点执行结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public class NodeExecutionResult {
    private ExecutionStatus status;
    private Map<String, Object> outputData;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private boolean needRetry = false;
    private int retryCount = 0;

    public enum ExecutionStatus { SUCCESS, FAILED, TIMEOUT, CANCELLED }

    // Chain setters for fluent API
    public NodeExecutionResult setStatus(ExecutionStatus status) { this.status = status; return this; }
    public NodeExecutionResult setOutputData(Map<String, Object> outputData) { this.outputData = outputData; return this; }
    public NodeExecutionResult setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
    public NodeExecutionResult setStartTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
    public NodeExecutionResult setEndTime(LocalDateTime endTime) { this.endTime = endTime; return this; }
    public NodeExecutionResult setDuration(Long duration) { this.duration = duration; return this; }
    public NodeExecutionResult setNeedRetry(boolean needRetry) { this.needRetry = needRetry; return this; }
    public NodeExecutionResult setRetryCount(int retryCount) { this.retryCount = retryCount; return this; }

    // Getters
    public ExecutionStatus getStatus() { return status; }
    public Map<String, Object> getOutputData() { return outputData; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDuration() { return duration; }
    public boolean isNeedRetry() { return needRetry; }
    public int getRetryCount() { return retryCount; }

    public static NodeExecutionResult success() {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.SUCCESS)
                .setOutputData(new java.util.HashMap<>())
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    public static NodeExecutionResult success(Map<String, Object> outputData) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.SUCCESS)
                .setOutputData(outputData != null ? outputData : new java.util.HashMap<>())
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    public static NodeExecutionResult failure(String errorMessage) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.FAILED)
                .setErrorMessage(errorMessage)
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    public static NodeExecutionResult timeout(String errorMessage) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.TIMEOUT)
                .setErrorMessage(errorMessage)
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    public NodeExecutionResult calculateDuration() {
        if (startTime != null && endTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).toMillis();
        }
        return this;
    }

    public boolean isSuccess() { return ExecutionStatus.SUCCESS.equals(status); }
    public boolean isFailed() { return ExecutionStatus.FAILED.equals(status) || ExecutionStatus.TIMEOUT.equals(status); }
}




