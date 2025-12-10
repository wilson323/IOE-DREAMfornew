package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA步骤实体
 * 定义分布式事务中的单个步骤
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaStep {

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤状态
     */
    private SagaStepStatus status;

    /**
     * 执行动作
     */
    private SagaAction action;

    /**
     * 补偿动作
     */
    private SagaAction compensationAction;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval;

    /**
     * 执行结果
     */
    private SagaStepResult result;

    /**
     * 补偿结果
     */
    private SagaStepResult compensationResult;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 获取步骤名称
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * 设置步骤名称
     */
    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    /**
     * 获取步骤状态
     */
    public SagaStepStatus getStatus() {
        return status;
    }

    /**
     * 设置步骤状态
     */
    public void setStatus(SagaStepStatus status) {
        this.status = status;
    }

    /**
     * 获取执行动作
     */
    public SagaAction getAction() {
        return action;
    }

    /**
     * 设置执行动作
     */
    public void setAction(SagaAction action) {
        this.action = action;
    }

    /**
     * 获取补偿动作
     */
    public SagaAction getCompensationAction() {
        return compensationAction;
    }

    /**
     * 设置补偿动作
     */
    public void setCompensationAction(SagaAction compensationAction) {
        this.compensationAction = compensationAction;
    }

    /**
     * 获取重试次数
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * 设置重试次数
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * 获取重试间隔
     */
    public long getRetryInterval() {
        return retryInterval;
    }

    /**
     * 设置重试间隔
     */
    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    /**
     * 获取执行结果
     */
    public SagaStepResult getResult() {
        return result;
    }

    /**
     * 设置执行结果
     */
    public void setResult(SagaStepResult result) {
        this.result = result;
    }

    /**
     * 获取补偿结果
     */
    public SagaStepResult getCompensationResult() {
        return compensationResult;
    }

    /**
     * 设置补偿结果
     */
    public void setCompensationResult(SagaStepResult compensationResult) {
        this.compensationResult = compensationResult;
    }

    /**
     * 获取开始时间
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}