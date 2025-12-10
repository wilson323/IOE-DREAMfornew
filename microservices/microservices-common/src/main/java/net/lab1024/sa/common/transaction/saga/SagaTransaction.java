package net.lab1024.sa.common.transaction.saga;

import java.util.List;

/**
 * SAGA事务实体
 * 定义分布式事务的基本属性和状态
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaTransaction {

    /**
     * 事务ID
     */
    private String transactionId;

    /**
     * 业务键
     */
    private String businessKey;

    /**
     * 事务状态
     */
    private SagaTransactionStatus status;

    /**
     * 事务步骤列表
     */
    private List<SagaStep> steps;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 事务结果
     */
    private SagaResult result;

    /**
     * 获取事务ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * 设置事务ID
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * 获取业务键
     */
    public String getBusinessKey() {
        return businessKey;
    }

    /**
     * 设置业务键
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    /**
     * 获取事务状态
     */
    public SagaTransactionStatus getStatus() {
        return status;
    }

    /**
     * 设置事务状态
     */
    public void setStatus(SagaTransactionStatus status) {
        this.status = status;
    }

    /**
     * 获取事务步骤列表
     */
    public List<SagaStep> getSteps() {
        return steps;
    }

    /**
     * 设置事务步骤列表
     */
    public void setSteps(List<SagaStep> steps) {
        this.steps = steps;
    }

    /**
     * 获取创建时间
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    /**
     * 获取事务结果
     */
    public SagaResult getResult() {
        return result;
    }

    /**
     * 设置事务结果
     */
    public void setResult(SagaResult result) {
        this.result = result;
    }
}