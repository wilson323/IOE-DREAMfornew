package net.lab1024.sa.common.workflow.executor;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 节点执行结果
 * <p>
 * 封装工作流节点的执行结果信息
 * 包含执行状态、输出数据、错误信息等
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Accessors(chain = true)
public class NodeExecutionResult {

    /**
     * 执行状态
     */
    private ExecutionStatus status;

    /**
     * 输出数据
     */
    private Map<String, Object> outputData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long duration;

    /**
     * 是否需要重试
     */
    private boolean needRetry = false;

    /**
     * 重试次数
     */
    private int retryCount = 0;

    /**
     * 执行状态枚举
     */
    public enum ExecutionStatus {
        SUCCESS,    // 执行成功
        FAILED,     // 执行失败
        TIMEOUT,    // 执行超时
        CANCELLED   // 执行取消
    }

    /**
     * 创建成功结果
     */
    public static NodeExecutionResult success() {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.SUCCESS)
                .setOutputData(new java.util.HashMap<>())
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    /**
     * 创建成功结果（带输出数据）
     */
    public static NodeExecutionResult success(Map<String, Object> outputData) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.SUCCESS)
                .setOutputData(outputData != null ? outputData : new java.util.HashMap<>())
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    /**
     * 创建失败结果
     */
    public static NodeExecutionResult failure(String errorMessage) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.FAILED)
                .setErrorMessage(errorMessage)
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    /**
     * 创建超时结果
     */
    public static NodeExecutionResult timeout(String errorMessage) {
        return new NodeExecutionResult()
                .setStatus(ExecutionStatus.TIMEOUT)
                .setErrorMessage(errorMessage)
                .setStartTime(LocalDateTime.now())
                .setEndTime(LocalDateTime.now());
    }

    /**
     * 计算执行耗时
     */
    public NodeExecutionResult calculateDuration() {
        if (startTime != null && endTime != null) {
            this.duration = java.time.Duration.between(startTime, endTime).toMillis();
        }
        return this;
    }

    /**
     * 是否执行成功
     */
    public boolean isSuccess() {
        return ExecutionStatus.SUCCESS.equals(status);
    }

    /**
     * 是否执行失败
     */
    public boolean isFailed() {
        return ExecutionStatus.FAILED.equals(status) || ExecutionStatus.TIMEOUT.equals(status);
    }
}