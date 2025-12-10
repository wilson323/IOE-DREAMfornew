package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA步骤结果
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaStepResult {

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
     * 数据（可选）
     */
    private Object data;

    /**
     * 构造函数（成功）
     */
    private SagaStepResult() {
        this.success = true;
        this.errorCode = null;
        this.errorMessage = null;
        this.executionTime = 0;
        this.data = null;
    }

    /**
     * 构造函数（失败）
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    private SagaStepResult(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.executionTime = 0;
        this.data = null;
    }

    /**
     * 创建成功结果
     *
     * @return 成功结果
     */
    public static SagaStepResult success() {
        return new SagaStepResult();
    }

    /**
     * 创建成功结果（带数据）
     *
     * @param data 数据
     * @return 成功结果
     */
    public static SagaStepResult success(Object data) {
        SagaStepResult result = new SagaStepResult();
        result.setData(data);
        return result;
    }

    /**
     * 创建失败结果
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     * @return 失败结果
     */
    public static SagaStepResult failure(String errorCode, String errorMessage) {
        return new SagaStepResult(errorCode, errorMessage);
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

    /**
     * 获取数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置数据
     */
    public void setData(Object data) {
        this.data = data;
    }
}