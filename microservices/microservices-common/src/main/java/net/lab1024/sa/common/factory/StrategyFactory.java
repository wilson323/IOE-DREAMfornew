package net.lab1024.sa.common.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.*;
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
 * - 构造函数注入（Spring 4.3+自动识别，无需注解）
 * </p>
 * <p>
 * 使用场景:
 * - 生物特征提取策略 (IBiometricFeatureExtractionStrategy)
 * - 门禁权限策略 (IAccessPermissionStrategy)
 * - 考勤规则策略 (IAttendanceRuleStrategy)
 * - 消费模式策略 (IConsumeModeStrategy)
 * - 设备适配器策略 (IDeviceAdapterStrategy)
 * </p>
 * <p>
 * 迁移说明：从microservices-common-core迁移到microservices-common
 * 原因：common-core应保持最小稳定内核，不包含Spring组件类
 * </p>
 *
 * @param <T> 策略接口类型
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class StrategyFactory<T> {

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, Map<String, T>> strategyCache = new ConcurrentHashMap<>();

    /**
     * 构造函数注入（Spring 4.3+自动识别，无需@Autowired注解）
     * 严格遵循CLAUDE.md规范：统一使用@Resource，但构造函数注入无需注解
     */
    public StrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化策略缓存
     */
    @PostConstruct
    public void init() {
        log.info("[策略工厂] 初始化策略缓存");
    }

    /**
     * 获取策略实例
     * <p>
     * 根据策略名称获取对应的策略实现
     * </p>
     *
     * @param strategyClass 策略接口类型
     * @param strategyName  策略名称（通常是枚举值或字符串标识）
     * @return 策略实例
     * @throws IllegalArgumentException 如果策略不存在
     */
    @SuppressWarnings("unchecked")
    public T get(Class<T> strategyClass, String strategyName) {
        // 从缓存获取
        Map<String, T> strategies = strategyCache.computeIfAbsent(
            strategyClass,
            k -> loadStrategies(strategyClass)
        );

        T strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException(
                String.format("策略不存在: %s[%s]", strategyClass.getSimpleName(), strategyName)
            );
        }

        return strategy;
    }

    /**
     * 获取所有策略实例
     *
     * @param strategyClass 策略接口类型
     * @return 策略实例列表（按优先级排序）
     */
    @SuppressWarnings("unchecked")
    public List<T> getAll(Class<T> strategyClass) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
            strategyClass,
            k -> loadStrategies(strategyClass)
        );

        return new ArrayList<>(strategies.values());
    }

    /**
     * 获取策略名称列表
     *
     * @param strategyClass 策略接口类型
     * @return 策略名称列表
     */
    public Set<String> getStrategyNames(Class<T> strategyClass) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
            strategyClass,
            k -> loadStrategies(strategyClass)
        );

        return strategies.keySet();
    }

    /**
     * 检查策略是否存在
     *
     * @param strategyClass 策略接口类型
     * @param strategyName  策略名称
     * @return 是否存在
     */
    public boolean exists(Class<T> strategyClass, String strategyName) {
        Map<String, T> strategies = strategyCache.computeIfAbsent(
            strategyClass,
            k -> loadStrategies(strategyClass)
        );

        return strategies.containsKey(strategyName);
    }

    /**
     * 加载策略实现
     * <p>
     * 从Spring容器中加载所有实现指定接口的Bean
     * </p>
     *
     * @param strategyClass 策略接口类型
     * @return 策略名称到策略实例的映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, T> loadStrategies(Class<T> strategyClass) {
        Map<String, T> strategies = new LinkedHashMap<>();

        // 从Spring容器获取所有实现该接口的Bean
        Map<String, T> beans = applicationContext.getBeansOfType(strategyClass);

        // 按优先级排序
        List<Map.Entry<String, T>> sortedEntries = beans.entrySet().stream()
            .sorted((e1, e2) -> {
                int priority1 = getPriority(e1.getValue());
                int priority2 = getPriority(e2.getValue());
                return Integer.compare(priority2, priority1); // 降序
            })
            .collect(Collectors.toList());

        // 构建策略映射
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

    /**
     * 提取策略名称
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 优先级：@StrategyMarker注解 > getSupportedType()方法 > Bean名称
     * </p>
     */
    private String extractStrategyName(String beanName, T strategy) {
        // 1. 优先从@StrategyMarker注解获取名称
        StrategyMarker marker = strategy.getClass().getAnnotation(StrategyMarker.class);
        if (marker != null && !marker.name().isEmpty()) {
            return marker.name();
        }

        // 2. 尝试调用getSupportedType()方法
        try {
            java.lang.reflect.Method method = strategy.getClass().getMethod("getSupportedType");
            Object result = method.invoke(strategy);
            if (result != null) {
                // 如果是枚举，返回枚举名称
                if (result instanceof Enum) {
                    return ((Enum<?>) result).name();
                }
                // 否则返回toString()
                return result.toString();
            }
        } catch (Exception e) {
            // 忽略异常，继续使用Bean名称
        }

        // 3. 使用Bean名称（去除Strategy后缀）
        if (beanName.endsWith("Strategy")) {
            return beanName.substring(0, beanName.length() - "Strategy".length());
        }

        return beanName;
    }

    /**
     * 获取策略优先级
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 优先级：@StrategyMarker注解 > getPriority()方法 > 默认优先级100
     * </p>
     */
    private int getPriority(T strategy) {
        // 1. 优先从@StrategyMarker注解获取优先级
        StrategyMarker marker = strategy.getClass().getAnnotation(StrategyMarker.class);
        if (marker != null && marker.priority() != 100) {
            return marker.priority();
        }

        // 2. 尝试调用getPriority()方法
        try {
            java.lang.reflect.Method method = strategy.getClass().getMethod("getPriority");
            Object result = method.invoke(strategy);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        } catch (Exception e) {
            // 忽略异常，返回默认优先级
        }

        // 3. 如果@StrategyMarker注解存在但未设置priority，使用注解的默认值
        if (marker != null) {
            return marker.priority();
        }

        return 100; // 默认优先级
    }

    /**
     * 刷新策略缓存
     * <p>
     * 重新加载指定类型的策略
     * </p>
     *
     * @param strategyClass 策略接口类型
     */
    public void refresh(Class<T> strategyClass) {
        strategyCache.remove(strategyClass);
        log.info("[策略工厂] 刷新策略缓存: {}", strategyClass.getSimpleName());
    }

    /**
     * 清空所有策略缓存
     */
    public void clear() {
        strategyCache.clear();
        log.info("[策略工厂] 清空所有策略缓存");
    }
}

