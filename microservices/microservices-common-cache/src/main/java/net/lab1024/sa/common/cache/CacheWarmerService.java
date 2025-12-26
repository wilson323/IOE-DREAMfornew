package net.lab1024.sa.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 缓存预热服务
 *
 * 职责：在应用启动后预热关键缓存数据
 *
 * 预热策略：
 * 1. 应用启动完成后自动触发预热
 * 2. 异步预热，不阻塞应用启动
 * 3. 预热失败不影响应用正常运行
 * 4. 支持手动触发预热
 *
 * 预热数据：
 * - 用户权限数据（高权限用户）
 * - 字典数据（全部字典类型）
 * - 系统配置（全部配置项）
 * - 区域数据（常用区域）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-26
 */
@Slf4j
public class CacheWarmerService implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * 缓存预热任务列表
     */
    private final List<CacheWarmUpTask> warmUpTasks = new ArrayList<>();

    /**
     * Redis模板（用于预热Redis缓存）
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 是否启用自动预热
     */
    private final boolean enableAutoWarmUp;

    /**
     * 构造函数
     */
    public CacheWarmerService(RedisTemplate<String, Object> redisTemplate, boolean enableAutoWarmUp) {
        this.redisTemplate = redisTemplate;
        this.enableAutoWarmUp = enableAutoWarmUp;
        log.info("[缓存预热服务] 初始化完成: enableAutoWarmUp={}", enableAutoWarmUp);
    }

    /**
     * 应用启动完成后自动预热
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!enableAutoWarmUp) {
            log.info("[缓存预热服务] 自动预热已禁用");
            return;
        }

        // 异步预热，不阻塞应用启动
        new Thread(() -> {
            try {
                Thread.sleep(5000); // 等待5秒，确保应用完全启动
                performWarmUp();
            } catch (Exception e) {
                log.error("[缓存预热服务] 自动预热失败", e);
            }
        }, "CacheWarmerThread").start();
    }

    /**
     * 执行缓存预热
     */
    public void performWarmUp() {
        log.info("[缓存预热服务] 开始执行缓存预热: taskCount={}", warmUpTasks.size());

        long startTime = System.currentTimeMillis();
        int totalSuccess = 0;
        int totalFailure = 0;

        for (CacheWarmUpTask task : warmUpTasks) {
            try {
                log.info("[缓存预热服务] 执行预热任务: taskName={}", task.getTaskName());
                task.execute();
                totalSuccess++;
                log.info("[缓存预热服务] 预热任务成功: taskName={}", task.getTaskName());
            } catch (Exception e) {
                totalFailure++;
                log.error("[缓存预热服务] 预热任务失败: taskName={}, error={}",
                        task.getTaskName(), e.getMessage(), e);
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[缓存预热服务] 缓存预热完成: totalTasks={}, success={}, failure={}, duration={}ms",
                warmUpTasks.size(), totalSuccess, totalFailure, duration);
    }

    /**
     * 添加预热任务
     */
    public void addWarmUpTask(CacheWarmUpTask task) {
        if (task != null) {
            warmUpTasks.add(task);
            log.info("[缓存预热服务] 添加预热任务: taskName={}", task.getTaskName());
        }
    }

    /**
     * 预热用户权限数据
     */
    public void warmUpUserPermissions(List<Long> userIds, Supplier<Map<Long, Object>> loader) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        log.info("[缓存预热服务] 预热用户权限: userCount={}", userIds.size());

        Map<String, Supplier<Object>> dataLoaders = new HashMap<>();
        for (Long userId : userIds) {
            String key = "user:permission:" + userId;
            dataLoaders.put(key, () -> loader.get().get(userId));
        }

        warmUpData("user-permission", dataLoaders);
    }

    /**
     * 预热字典数据
     */
    public void warmUpDictionary(List<String> dictTypes, Supplier<Map<String, Object>> loader) {
        if (dictTypes == null || dictTypes.isEmpty()) {
            return;
        }

        log.info("[缓存预热服务] 预热字典: dictCount={}", dictTypes.size());

        Map<String, Supplier<Object>> dataLoaders = new HashMap<>();
        for (String dictType : dictTypes) {
            String key = "dict:" + dictType;
            dataLoaders.put(key, () -> loader.get().get(dictType));
        }

        warmUpData("dictionary", dataLoaders);
    }

    /**
     * 预热系统配置
     */
    public void warmUpSystemConfig(List<String> configKeys, Supplier<Map<String, Object>> loader) {
        if (configKeys == null || configKeys.isEmpty()) {
            return;
        }

        log.info("[缓存预热服务] 预热系统配置: configCount={}", configKeys.size());

        Map<String, Supplier<Object>> dataLoaders = new HashMap<>();
        for (String configKey : configKeys) {
            String key = "config:" + configKey;
            dataLoaders.put(key, () -> loader.get().get(configKey));
        }

        warmUpData("system-config", dataLoaders);
    }

    /**
     * 预热区域数据
     */
    public void warmUpAreas(List<Long> areaIds, Supplier<Map<Long, Object>> loader) {
        if (areaIds == null || areaIds.isEmpty()) {
            return;
        }

        log.info("[缓存预热服务] 预热区域数据: areaCount={}", areaIds.size());

        Map<String, Supplier<Object>> dataLoaders = new HashMap<>();
        for (Long areaId : areaIds) {
            String key = "area:" + areaId;
            dataLoaders.put(key, () -> loader.get().get(areaId));
        }

        warmUpData("area", dataLoaders);
    }

    /**
     * 通用数据预热
     */
    public <K, V> void warmUpData(String cacheName, Map<K, Supplier<V>> dataLoaders) {
        if (dataLoaders == null || dataLoaders.isEmpty()) {
            log.warn("[缓存预热服务] 预热数据为空: cacheName={}", cacheName);
            return;
        }

        log.info("[缓存预热服务] 开始预热: cacheName={}, count={}", cacheName, dataLoaders.size());

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        for (Map.Entry<K, Supplier<V>> entry : dataLoaders.entrySet()) {
            try {
                K key = entry.getKey();
                Supplier<V> loader = entry.getValue();
                V value = loader.get();

                if (value != null) {
                    // 写入Redis缓存
                    String fullKey = cacheName + ":" + key;
                    redisTemplate.opsForValue().set(fullKey, value);
                    successCount++;
                } else {
                    log.debug("[缓存预热服务] 预热数据为空: cacheName={}, key={}", cacheName, key);
                }

            } catch (Exception e) {
                failureCount++;
                log.error("[缓存预热服务] 预热失败: cacheName={}, key={}, error={}",
                        cacheName, entry.getKey(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[缓存预热服务] 预热完成: cacheName={}, success={}, failure={}, duration={}ms",
                cacheName, successCount, failureCount, duration);
    }

    /**
     * 缓存预热任务接口
     */
    public interface CacheWarmUpTask {
        /**
         * 获取任务名称
         */
        String getTaskName();

        /**
         * 执行预热任务
         */
        void execute() throws Exception;
    }

    /**
     * 抽象预热任务基类
     */
    public abstract static class AbstractWarmUpTask implements CacheWarmUpTask {
        @Override
        public String getTaskName() {
            return this.getClass().getSimpleName();
        }
    }

    /**
     * 预热任务构建器
     */
    public static class WarmUpTaskBuilder {
        private final String taskName;
        private final Runnable task;

        public WarmUpTaskBuilder(String taskName, Runnable task) {
            this.taskName = taskName;
            this.task = task;
        }

        public CacheWarmUpTask build() {
            return new AbstractWarmUpTask() {
                @Override
                public String getTaskName() {
                    return taskName;
                }

                @Override
                public void execute() throws Exception {
                    task.run();
                }
            };
        }
    }

    /**
     * 创建预热任务构建器
     */
    public static WarmUpTaskBuilder builder(String taskName, Runnable task) {
        return new WarmUpTaskBuilder(taskName, task);
    }
}
