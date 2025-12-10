package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA动作接口
 * 定义SAGA步骤的执行动作
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@FunctionalInterface
public interface SagaAction {

    /**
     * 执行动作
     *
     * @return 执行结果
     */
    SagaStepResult run();
}