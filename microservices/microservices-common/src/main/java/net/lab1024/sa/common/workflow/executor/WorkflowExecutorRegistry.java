package net.lab1024.sa.common.workflow.executor;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.executor.impl.*;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.workflow.manager.ExpressionEngineManager;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

/**
 * 工作流执行器注册表
 * <p>
 * 企业级工作流执行器管理器，负责注册、获取和管理所有工作流执行器
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class WorkflowExecutorRegistry {

    private final Map<String, NodeExecutor> executors = new ConcurrentHashMap<>();
    private final GatewayServiceClient gatewayServiceClient;
    private final ExpressionEngineManager expressionEngineManager;

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param expressionEngineManager 表达式引擎管理器
     */
    public WorkflowExecutorRegistry(
            GatewayServiceClient gatewayServiceClient,
            ExpressionEngineManager expressionEngineManager) {
        this.gatewayServiceClient = Objects.requireNonNull(gatewayServiceClient, "gatewayServiceClient不能为null");
        this.expressionEngineManager = Objects.requireNonNull(expressionEngineManager, "expressionEngineManager不能为null");
        initializeExecutors();
    }

    /**
     * 初始化执行器注册表
     * <p>
     * 移除@PostConstruct，改为普通方法，由构造函数调用
     * </p>
     */
    public void initializeExecutors() {
        log.info("[执行器注册表] 初始化工作流执行器");

        try {
            // 注册内置执行器
            registerBuiltinExecutors();

            log.info("[执行器注册表] 执行器注册完成，共注册{}个执行器", executors.size());

            // 记录注册的执行器类型
            executors.keySet().forEach(type ->
                log.debug("[执行器注册表] 已注册执行器类型: {}", type));

        } catch (Exception e) {
            log.error("[执行器注册表] 执行器初始化失败: error={}", e.getMessage(), e);
            throw new RuntimeException("执行器初始化失败", e);
        }
    }

    /**
     * 注册内置执行器
     */
    private void registerBuiltinExecutors() {
        // 审批执行器
        registerExecutor("approval", new ApprovalExecutor(gatewayServiceClient));

        // 系统执行器
        registerExecutor("system", new SystemExecutor(gatewayServiceClient));

        // 条件执行器（需要GatewayServiceClient用于调用业务服务）
        registerExecutor("condition", new ConditionExecutor(expressionEngineManager, gatewayServiceClient));

        // 通知执行器
        registerExecutor("notification", new NotificationExecutor(gatewayServiceClient));

        // 可扩展：注册其他内置执行器
        // registerExecutor("script", new ScriptExecutor());
        // registerExecutor("timer", new TimerExecutor());
        // registerExecutor("subprocess", new SubprocessExecutor());
    }

    /**
     * 注册执行器
     *
     * @param type 执行器类型
     * @param executor 执行器实例
     */
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

    /**
     * 获取执行器
     *
     * @param type 执行器类型
     * @return 执行器实例，如果不存在返回null
     */
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

    /**
     * 检查执行器是否存在
     *
     * @param type 执行器类型
     * @return 是否存在
     */
    public boolean hasExecutor(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }

        return executors.containsKey(type.toLowerCase().trim());
    }

    /**
     * 移除执行器
     *
     * @param type 执行器类型
     * @return 被移除的执行器实例，如果不存在返回null
     */
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

    /**
     * 获取所有已注册的执行器类型
     *
     * @return 执行器类型集合
     */
    public java.util.Set<String> getRegisteredTypes() {
        return new java.util.HashSet<>(executors.keySet());
    }

    /**
     * 获取执行器信息
     *
     * @return 执行器信息映射
     */
    public Map<String, String> getExecutorInfo() {
        Map<String, String> info = new HashMap<>();

        executors.forEach((type, executor) -> {
            info.put(type, executor.getDescription());
        });

        return info;
    }

    /**
     * 获取执行器统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getExecutorStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("totalExecutors", executors.size());
        statistics.put("registeredTypes", getRegisteredTypes());
        statistics.put("executorInfo", getExecutorInfo());

        // 按类型分组统计
        Map<String, Long> typeStats = executors.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        executor -> executor.getClass().getSimpleName(),
                        java.util.stream.Collectors.counting()
                ));
        statistics.put("typeStatistics", typeStats);

        return statistics;
    }

    /**
     * 清空所有执行器
     */
    public void clearExecutors() {
        int count = executors.size();
        executors.clear();
        log.info("[执行器注册表] 清空所有执行器，共清空{}个", count);
    }

    /**
     * 重新初始化执行器
     */
    public void reinitializeExecutors() {
        log.info("[执行器注册表] 重新初始化执行器");

        clearExecutors();
        initializeExecutors();
    }

    /**
     * 验证执行器配置
     *
     * @param type 执行器类型
     * @param config 执行器配置
     * @return 验证结果
     */
    public boolean validateExecutorConfig(String type, Map<String, Object> config) {
        NodeExecutor executor = getExecutor(type);
        if (executor == null) {
            return false;
        }

        try {
            // 基本配置验证
            if (config == null) {
                log.warn("[执行器注册表] 执行器配置为空: type={}", type);
                return false;
            }

            // 执行器类型特定的验证逻辑可以在这里添加
            // 目前只做基本验证

            return true;
        } catch (Exception e) {
            log.error("[执行器注册表] 执行器配置验证失败: type={}, error={}", type, e.getMessage(), e);
            return false;
        }
    }
}
