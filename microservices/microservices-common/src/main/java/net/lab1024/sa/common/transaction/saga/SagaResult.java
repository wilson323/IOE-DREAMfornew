package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA事务结果
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行时间（毫秒）
     */
    private long executionTime;

    /**
     * 构造函数（成功）
     */
    private SagaResult() {
        this.success = true;
        this.errorCode = null;
        this.errorMessage = null;
        this.executionTime = 0;
    }

    /**
     * 构造函数（失败）
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    private SagaResult(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.executionTime = 0;
    }

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static SagaResult success() {
        return new SagaResult();
    }

    /**
     * 创建成功结果（带执行时间）
     *
     * @param executionTime 执行时间
     * @return 成功结果
     */
    public static SagaResult success(long executionTime) {
        SagaResult result = new SagaResult();
        result.setExecutionTime(executionTime);
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static SagaResult failure(String errorCode, String errorMessage) {
        return new SagaResult(errorCode, errorMessage);
    }

    /**
     * 获取是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 获取执行时间
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * 设置执行时间
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}