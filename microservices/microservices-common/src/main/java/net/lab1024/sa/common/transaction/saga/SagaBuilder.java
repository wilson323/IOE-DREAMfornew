package net.lab1024.sa.common.transaction.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * SAGA事务构建器
 * 提供流式API来构建SAGA事务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaBuilder {

    private final SagaManager sagaManager;
    private final String businessKey;
    private final List<SagaStep> steps = new ArrayList<>();
    private int retryCount = 3;
    private long retryInterval = 1000;

    /**
     * 构造函数
     *
     * @param sagaManager SAGA管理器
     * @param businessKey 业务键
     */
    public SagaBuilder(SagaManager sagaManager, String businessKey) {
        this.sagaManager = sagaManager;
        this.businessKey = businessKey;
    }

    /**
     * 添加步骤
     *
     * @param stepName 步骤名称
     * @param action 执行动作
     * @return 构建器
     */
    public SagaBuilder step(String stepName, SagaAction action) {
        return step(stepName, action, () -> SagaStepResult.success());
    }

    /**
     * 添加步骤（带补偿）
     *
     * @param stepName 步骤名称
     * @param action 执行动作
     * @param compensationAction 补偿动作
     * @return 构建器
     */
    public SagaBuilder step(String stepName, SagaAction action, SagaAction compensationAction) {
        SagaStep step = new SagaStep();
        step.setStepName(stepName);
        step.setAction(action);
        step.setCompensationAction(compensationAction);
        step.setRetryCount(retryCount);
        step.setRetryInterval(retryInterval);
        steps.add(step);
        return this;
    }

    /**
     * 设置重试次数
     *
     * @param retryCount 重试次数
     * @return 构建器
     */
    public SagaBuilder retry(int retryCount) {
        this.retryCount = retryCount;
        for (SagaStep step : steps) {
            step.setRetryCount(retryCount);
        }
        return this;
    }

    /**
     * 设置重试间隔
     *
     * @param retryInterval 重试间隔（毫秒）
     * @return 构建器
     */
    public SagaBuilder retryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
        for (SagaStep step : steps) {
            step.setRetryInterval(retryInterval);
        }
        return this;
    }

    /**
     * 构建SAGA事务
     *
     * @return 事务实例
     */
    public SagaTransaction build() {
        SagaTransaction transaction = new SagaTransaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setBusinessKey(businessKey);
        transaction.setSteps(new ArrayList<>(steps));
        transaction.setStatus(SagaTransactionStatus.CREATED);
        transaction.setCreateTime(System.currentTimeMillis());
        return transaction;
    }

    /**
     * 构建并执行SAGA事务
     *
     * @return 执行结果
     */
    public CompletableFuture<SagaResult> execute() {
        SagaTransaction transaction = build();
        transaction.setStatus(SagaTransactionStatus.RUNNING);
        return sagaManager.execute(transaction);
    }

    /**
     * 生成事务ID
     *
     * @return 事务ID
     */
    private String generateTransactionId() {
        return "SAGA-" + businessKey + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}