package net.lab1024.sa.common.workflow.executor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.workflow.manager.ExpressionEngineManager;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * 工作流执行器注册表
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-09
 */
@Slf4j
public class WorkflowExecutorRegistry {

    private final Map<String, NodeExecutor> executors = new ConcurrentHashMap<>();

    /**
     * 网关服务客户端
     * <p>
     * 预留字段，用于未来扩展：
     * - 执行器可能需要调用其他微服务
     * - 支持服务间通信的工作流节点
     * </p>
     */
    @SuppressWarnings("unused")
    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 表达式引擎管理器
     * <p>
     * 预留字段，用于未来扩展：
     * - 支持动态表达式计算的工作流节点
     * - 条件判断和规则引擎集成
     * </p>
     */
    @SuppressWarnings("unused")
    private final ExpressionEngineManager expressionEngineManager;

    public WorkflowExecutorRegistry(
            GatewayServiceClient gatewayServiceClient,
            ExpressionEngineManager expressionEngineManager) {
        this.gatewayServiceClient = Objects.requireNonNull(gatewayServiceClient, "gatewayServiceClient不能为null");
        this.expressionEngineManager = Objects.requireNonNull(expressionEngineManager, "expressionEngineManager不能为null");
        initializeExecutors();
    }

    public void initializeExecutors() {
        log.info("[执行器注册表] 初始化工作流执行器");
        try {
            registerBuiltinExecutors();
            log.info("[执行器注册表] 执行器注册完成，共注册{}个执行器", executors.size());
            executors.keySet().forEach(type ->
                log.debug("[执行器注册表] 已注册执行器类型: {}", type));
        } catch (Exception e) {
            log.error("[执行器注册表] 执行器初始化失败: error={}", e.getMessage(), e);
            throw new SystemException("WORKFLOW_EXECUTOR_INIT_ERROR", "执行器初始化失败", e);
        }
    }

    private void registerBuiltinExecutors() {
        // 内置执行器注册（具体实现可根据需要添加）
    }

    public void registerExecutor(String type, NodeExecutor executor) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("执行器类型不能为空");
        }
        if (executor == null) {
            throw new IllegalArgumentException("执行器实例不能为空");
        }

        String normalizedType = type.toLowerCase().trim();

        if (executors.containsKey(normalizedType)) {
            log.warn("[执行器注册表] 执行器类型已存在，将被覆盖: type={}", normalizedType);
        }

        executors.put(normalizedType, executor);
        log.info("[执行器注册表] 注册执行器: type={}, class={}",
                normalizedType, executor.getClass().getSimpleName());
    }

    public NodeExecutor getExecutor(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }

        String normalizedType = type.toLowerCase().trim();
        NodeExecutor executor = executors.get(normalizedType);

        if (executor == null) {
            log.warn("[执行器注册表] 未找到指定类型的执行器: type={}", normalizedType);
        }

        return executor;
    }

    public boolean hasExecutor(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }

        return executors.containsKey(type.toLowerCase().trim());
    }

    public NodeExecutor removeExecutor(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }

        String normalizedType = type.toLowerCase().trim();
        NodeExecutor removed = executors.remove(normalizedType);

        if (removed != null) {
            log.info("[执行器注册表] 移除执行器: type={}", normalizedType);
        }

        return removed;
    }

    public java.util.Set<String> getRegisteredTypes() {
        return new java.util.HashSet<>(executors.keySet());
    }

    public Map<String, String> getExecutorInfo() {
        Map<String, String> info = new HashMap<>();
        executors.forEach((type, executor) -> {
            info.put(type, executor.getDescription());
        });
        return info;
    }

    public Map<String, Object> getExecutorStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalExecutors", executors.size());
        statistics.put("registeredTypes", getRegisteredTypes());
        statistics.put("executorInfo", getExecutorInfo());
        return statistics;
    }

    public void clearExecutors() {
        int count = executors.size();
        executors.clear();
        log.info("[执行器注册表] 清空所有执行器，共清空{}个", count);
    }

    public void reinitializeExecutors() {
        log.info("[执行器注册表] 重新初始化执行器");
        clearExecutors();
        initializeExecutors();
    }

    public boolean validateExecutorConfig(String type, Map<String, Object> config) {
        NodeExecutor executor = getExecutor(type);
        if (executor == null) {
            return false;
        }

        try {
            if (config == null) {
                log.warn("[执行器注册表] 执行器配置为空: type={}", type);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[执行器注册表] 执行器配置验证失败: type={}, error={}", type, e.getMessage(), e);
            return false;
        }
    }
}
