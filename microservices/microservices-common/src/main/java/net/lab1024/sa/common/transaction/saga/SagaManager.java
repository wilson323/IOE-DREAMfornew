package net.lab1024.sa.common.transaction.saga;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * SAGA分布式事务管理器
 * 提供可靠的分布式事务处理能力，支持长事务和补偿机制
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public class SagaManager {

    /**
     * 线程池执行器
     */
    private final Executor executor;

    /**
     * 事务实例缓存
     */
    private final ConcurrentHashMap<String, SagaTransaction> activeTransactions = new ConcurrentHashMap<>();

    /**
     * 构造函数
     */
    public SagaManager() {
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "saga-transaction-" + System.currentTimeMillis());
            t.setDaemon(false);
            return t;
        });
    }

    /**
     * 创建SAGA事务构建器
     *
     * @param businessKey 业务键
     * @return 事务构建器
     */
    public SagaBuilder createSaga(String businessKey) {
        return new SagaBuilder(this, businessKey);
    }

    /**
     * 获取活跃事务
     *
     * @param transactionId 事务ID
     * @return 事务实例
     */
    public SagaTransaction getActiveTransaction(String transactionId) {
        return activeTransactions.get(transactionId);
    }

    /**
     * 执行SAGA事务
     *
     * @param transaction 事务实例
     * @return 执行结果
     */
    public CompletableFuture<SagaResult> execute(SagaTransaction transaction) {
        activeTransactions.put(transaction.getTransactionId(), transaction);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeSync(transaction);
            } finally {
                activeTransactions.remove(transaction.getTransactionId());
            }
        }, executor);
    }

    /**
     * 同步执行SAGA事务
     *
     * @param transaction 事务实例
     * @return 执行结果
     */
    private SagaResult executeSync(SagaTransaction transaction) {
        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            // 执行所有步骤
            for (SagaStep step : transaction.getSteps()) {
                SagaStepResult result = executeStep(transaction, step);

                if (result.isSuccess()) {
                    completedSteps.add(step);
                } else {
                    // 执行失败，开始补偿
                    compensate(transaction, completedSteps);
                    return SagaResult.failure(result.getErrorCode(), result.getErrorMessage());
                }
            }

            return SagaResult.success();
        } catch (Exception e) {
            // 发生异常，执行补偿
            compensate(transaction, completedSteps);
            return SagaResult.failure("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 执行单个步骤
     *
     * @param transaction 事务实例
     * @param step 步骤
     * @return 执行结果
     */
    private SagaStepResult executeStep(SagaTransaction transaction, SagaStep step) {
        try {
            step.setStatus(SagaStepStatus.EXECUTING);
            SagaStepResult result = step.getAction().run();
            step.setResult(result);
            step.setStatus(result.isSuccess() ? SagaStepStatus.COMPLETED : SagaStepStatus.FAILED);
            return result;
        } catch (Exception e) {
            SagaStepResult result = SagaStepResult.failure("STEP_EXECUTION_ERROR", e.getMessage());
            step.setResult(result);
            step.setStatus(SagaStepStatus.FAILED);
            return result;
        }
    }

    /**
     * 补偿已执行的步骤
     *
     * @param transaction 事务实例
     * @param completedSteps 已完成的步骤
     */
    private void compensate(SagaTransaction transaction, List<SagaStep> completedSteps) {
        for (int i = completedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = completedSteps.get(i);
            try {
                step.setStatus(SagaStepStatus.COMPENSATING);
                SagaStepResult compensateResult = step.getCompensationAction().run();
                step.setCompensationResult(compensateResult);
                step.setStatus(compensateResult.isSuccess() ? SagaStepStatus.COMPENSATED : SagaStepStatus.COMPENSATION_FAILED);
            } catch (Exception e) {
                // 补偿失败，记录但继续执行其他补偿
                SagaStepResult compensateResult = SagaStepResult.failure("COMPENSATION_ERROR", e.getMessage());
                step.setCompensationResult(compensateResult);
                step.setStatus(SagaStepStatus.COMPENSATION_FAILED);
            }
        }
    }
}