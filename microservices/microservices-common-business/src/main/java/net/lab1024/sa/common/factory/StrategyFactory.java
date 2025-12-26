package net.lab1024.sa.common.factory;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 策略工厂 - 统一管理所有策略实现
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用策略模式实现
 * - 支持动态加载策略
 * - 支持策略优先级排序
 * - 支持策略热插拔
 * - 纯Java类设计，通过构造函数注入依赖
 * </p>
 * <p>
 * 迁移说明：从microservices-common迁移到microservices-common-business
 * 原因：避免业务服务依赖microservices-common聚合模块，符合架构规范
 * </p>
 *
 * @param <T> 策略接口类型
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-18
 * @updated 2025-12-20 移除@Component注解，改为纯Java类
 * @updated 2025-01-30 迁移到common-business模块
 */
@Slf4j
public class StrategyFactory<T> {

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, Map<String, T>> strategyCache = new ConcurrentHashMap<>();

    public StrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        log.info("[策略工厂] 初始化策略缓存");
    }

    @SuppressWarnings("unchecked")
    public T get(Class<T> strategyClass, String strategyName) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
                strategyClass,
                key -> loadStrategies(strategyClass)
        );

        T strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    String.format("策略不存在: %s[%s]", strategyClass.getSimpleName(), strategyName)
            );
        }

        return strategy;
    }

    @SuppressWarnings("unchecked")
    public List<T> getAll(Class<T> strategyClass) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
                strategyClass,
                key -> loadStrategies(strategyClass)
        );

        return new ArrayList<>(strategies.values());
    }

    public Set<String> getStrategyNames(Class<T> strategyClass) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
                strategyClass,
                key -> loadStrategies(strategyClass)
        );

        return strategies.keySet();
    }

    public boolean exists(Class<T> strategyClass, String strategyName) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
                strategyClass,
                key -> loadStrategies(strategyClass)
        );

        return strategies.containsKey(strategyName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, T> loadStrategies(Class<T> strategyClass) {
        Map<String, T> strategies = new LinkedHashMap<>();

        Map<String, T> beans = applicationContext.getBeansOfType(strategyClass);

        List<Map.Entry<String, T>> sortedEntries = beans.entrySet().stream()
                .sorted((e1, e2) -> {
                    int priority1 = getPriority(e1.getValue());
                    int priority2 = getPriority(e2.getValue());
                    return Integer.compare(priority2, priority1);
                })
                .collect(Collectors.toList());

        for (Map.Entry<String, T> entry : sortedEntries) {
            String strategyName = extractStrategyName(entry.getKey(), entry.getValue());
            strategies.put(strategyName, entry.getValue());
            log.debug("[策略工厂] 加载策略: {} -> {}", strategyName, entry.getKey());
        }

        log.info("[策略工厂] 加载策略完成: {}, 数量: {}",
                strategyClass.getSimpleName(),
                strategies.size());

        return strategies;
    }

    private String extractStrategyName(String beanName, T strategy) {
        StrategyMarker marker = strategy.getClass().getAnnotation(StrategyMarker.class);
        if (marker != null && !marker.name().isEmpty()) {
            return marker.name();
        }

        try {
            java.lang.reflect.Method method = strategy.getClass().getMethod("getSupportedType");
            Object result = method.invoke(strategy);
            if (result != null) {
                if (result instanceof Enum) {
                    return ((Enum<?>) result).name();
                }
                return result.toString();
            }
        } catch (Exception e) {
            log.debug("[策略工厂] 读取getSupportedType失败: {}", e.getMessage());
        }

        if (beanName.endsWith("Strategy")) {
            return beanName.substring(0, beanName.length() - "Strategy".length());
        }

        return beanName;
    }

    private int getPriority(T strategy) {
        StrategyMarker marker = strategy.getClass().getAnnotation(StrategyMarker.class);
        if (marker != null && marker.priority() != 100) {
            return marker.priority();
        }

        try {
            java.lang.reflect.Method method = strategy.getClass().getMethod("getPriority");
            Object result = method.invoke(strategy);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
            log.debug("[策略工厂] 读取getPriority失败: {}", e.getMessage());
        }

        if (marker != null) {
            return marker.priority();
        }

        return 100;
    }

    public void refresh(Class<T> strategyClass) {
        strategyCache.remove(strategyClass);
        log.info("[策略工厂] 刷新策略缓存: {}", strategyClass.getSimpleName());
    }

    public void clear() {
        strategyCache.clear();
        log.info("[策略工厂] 清空所有策略缓存");
    }
}

