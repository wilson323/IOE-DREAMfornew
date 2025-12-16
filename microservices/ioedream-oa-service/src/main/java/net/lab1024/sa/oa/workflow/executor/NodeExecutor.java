package net.lab1024.sa.oa.workflow.executor;

/**
 * 节点执行器接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public interface NodeExecutor {

    NodeExecutionResult execute(NodeExecutionContext context) throws Exception;

    String getType();

    String getDescription();
}




