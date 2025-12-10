package net.lab1024.sa.common.workflow.executor;

/**
 * 节点执行器接口
 * <p>
 * 定义工作流节点的执行逻辑
 * 支持同步和异步执行模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public interface NodeExecutor {

    /**
     * 执行节点
     *
     * @param context 节点执行上下文
     * @return 执行结果
     * @throws Exception 执行异常
     */
    NodeExecutionResult execute(NodeExecutionContext context) throws Exception;

    /**
     * 获取执行器类型
     *
     * @return 执行器类型
     */
    String getType();

    /**
     * 获取执行器描述
     *
     * @return 执行器描述
     */
    String getDescription();
}