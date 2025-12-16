package net.lab1024.sa.attendance.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 内存优化器
 *
 * 提供内存使用监控、优化和管理功能，减少资源消耗
 * 使用弱引用、软引用、对象池等技术优化内存使用
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class MemoryOptimizer {

    // 内存使用统计
    private final AtomicLong totalMemoryUsed = new AtomicLong(0);
    private final AtomicLong cachedObjectsCount = new AtomicLong(0);
    private final AtomicLong memoryOptimizationsCount = new AtomicLong(0);

    // 内存使用阈值配置
    private static final long MEMORY_WARNING_THRESHOLD = 100 * 1024 * 1024; // 100MB
    private static final long MEMORY_CRITICAL_THRESHOLD = 200 * 1024 * 1024; // 200MB
    private static final int MAX_CACHE_SIZE = 1000;

    // 对象池
    private final Map<String, ObjectPool> objectPools = new ConcurrentHashMap<>();

    // LRU缓存
    private final Map<String, Object> lruCache = Collections.synchronizedMap(
        new LinkedHashMap<String, Object>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                boolean shouldRemove = size() > MAX_CACHE_SIZE;
                if (shouldRemove) {
                    cachedObjectsCount.decrementAndGet();
                    log.debug("LRU缓存淘汰对象: key={}", eldest.getKey());
                }
                return shouldRemove;
            }
        }
    );

    // 软引用缓存 - 内存不足时自动回收
    private final Map<String, SoftReference<Object>> softCache = new ConcurrentHashMap<>();

    // 弱引用缓存 - GC时自动回收
    private final Map<String, WeakReference<Object>> weakCache = new ConcurrentHashMap<>();

    /**
     * 内存优化配置
     */
    public static class MemoryConfig {
        private long warningThreshold = MEMORY_WARNING_THRESHOLD;
        private long criticalThreshold = MEMORY_CRITICAL_THRESHOLD;
        private int maxCacheSize = MAX_CACHE_SIZE;
        private boolean enableSoftCache = true;
        private boolean enableWeakCache = true;
        private boolean enableObjectPool = true;
        private int gcTriggerInterval = 60; // 秒

        // Getters and Setters
        public long getWarningThreshold() { return warningThreshold; }
        public void setWarningThreshold(long warningThreshold) { this.warningThreshold = warningThreshold; }
        public long getCriticalThreshold() { return criticalThreshold; }
        public void setCriticalThreshold(long criticalThreshold) { this.criticalThreshold = criticalThreshold; }
        public int getMaxCacheSize() { return maxCacheSize; }
        public void setMaxCacheSize(int maxCacheSize) { this.maxCacheSize = maxCacheSize; }
        public boolean isEnableSoftCache() { return enableSoftCache; }
        public void setEnableSoftCache(boolean enableSoftCache) { this.enableSoftCache = enableSoftCache; }
        public boolean isEnableWeakCache() { return enableWeakCache; }
        public void setEnableWeakCache(boolean enableWeakCache) { this.enableWeakCache = enableWeakCache; }
        public boolean isEnableObjectPool() { return enableObjectPool; }
        public void setEnableObjectPool(boolean enableObjectPool) { this.enableObjectPool = enableObjectPool; }
        public int getGcTriggerInterval() { return gcTriggerInterval; }
        public void setGcTriggerInterval(int gcTriggerInterval) { this.gcTriggerInterval = gcTriggerInterval; }
    }

    private final MemoryConfig config;

    public MemoryOptimizer() {
        this(new MemoryConfig());
    }

    public MemoryOptimizer(MemoryConfig config) {
        this.config = config;
        initializeObjectPools();
        startMemoryMonitoring();
    }

    /**
     * 初始化对象池
     */
    private void initializeObjectPools() {
        if (!config.isEnableObjectPool()) {
            return;
        }

        // 初始化常用对象池
        objectPools.put("StringBuffer", new ObjectPool<>(() -> new StringBuffer(256), obj -> ((StringBuffer) obj).setLength(0)));
        objectPools.put("StringBuilder", new ObjectPool<>(() -> new StringBuilder(256), obj -> ((StringBuilder) obj).setLength(0)));
        objectPools.put("ArrayList", new ObjectPool<>(() -> new ArrayList<>(16), List::clear));
        objectPools.put("HashMap", new ObjectPool<>(() -> new HashMap<>(16), Map::clear));
        objectPools.put("ConcurrentHashMap", new ObjectPool<>(() -> new ConcurrentHashMap<>(16), Map::clear));
        objectPools.put("LinkedHashMap", new ObjectPool<>(() -> new LinkedHashMap<>(16), Map::clear));

        log.info("初始化对象池完成，池数量: {}", objectPools.size());
    }

    /**
     * 启动内存监控
     */
    private void startMemoryMonitoring() {
        Thread monitoringThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(config.getGcTriggerInterval() * 1000);
                    checkMemoryUsage();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("内存监控异常", e);
                }
            }
        }, "Memory-Monitoring-Thread");

        monitoringThread.setDaemon(true);
        monitoringThread.start();

        log.info("内存监控线程已启动，监控间隔: {}秒", config.getGcTriggerInterval());
    }

    /**
     * 检查内存使用情况
     */
    public void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        // 更新统计
        totalMemoryUsed.set(usedMemory);

        // 内存使用率计算
        double memoryUsageRatio = (double) usedMemory / maxMemory;

        log.debug("内存使用情况 - 已用: {}MB, 总计: {}MB, 使用率: {:.1f}%, 缓存对象数: {}",
            usedMemory / 1024 / 1024,
            maxMemory / 1024 / 1024,
            memoryUsageRatio * 100,
            cachedObjectsCount.get());

        // 内存警告
        if (usedMemory > config.getCriticalThreshold()) {
            log.warn("内存使用超过临界阈值: {}MB，触发紧急内存优化", usedMemory / 1024 / 1024);
            performEmergencyMemoryOptimization();
        } else if (usedMemory > config.getWarningThreshold()) {
            log.warn("内存使用超过警告阈值: {}MB，触发常规内存优化", usedMemory / 1024 / 1024);
            performRegularMemoryOptimization();
        }
    }

    /**
     * 执行常规内存优化
     */
    private void performRegularMemoryOptimization() {
        memoryOptimizationsCount.incrementAndGet();

        // 清理LRU缓存中过期或较少使用的对象
        cleanupLRUCache();

        // 清理软引用缓存
        if (config.isEnableSoftCache()) {
            cleanupSoftCache();
        }

        // 清理弱引用缓存
        if (config.isEnableWeakCache()) {
            cleanupWeakCache();
        }

        // 建议GC
        System.gc();

        log.info("完成常规内存优化，优化次数: {}", memoryOptimizationsCount.get());
    }

    /**
     * 执行紧急内存优化
     */
    private void performEmergencyMemoryOptimization() {
        memoryOptimizationsCount.incrementAndGet();

        // 清空所有缓存
        clearAllCaches();

        // 强制清理对象池
        clearObjectPools();

        // 多次GC建议
        for (int i = 0; i < 3; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        log.warn("完成紧急内存优化，优化次数: {}", memoryOptimizationsCount.get());
    }

    /**
     * 清理LRU缓存
     */
    private void cleanupLRUCache() {
        synchronized (lruCache) {
            // 清理超过一半大小的缓存
            int targetSize = lruCache.size() / 2;
            lruCache.entrySet().removeIf(entry -> {
                boolean shouldRemove = lruCache.size() > targetSize;
                if (shouldRemove) {
                    cachedObjectsCount.decrementAndGet();
                }
                return shouldRemove;
            });
        }
    }

    /**
     * 清理软引用缓存
     */
    private void cleanupSoftCache() {
        softCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    /**
     * 清理弱引用缓存
     */
    private void cleanupWeakCache() {
        weakCache.entrySet().removeIf(entry -> entry.getValue().get() == null);
    }

    /**
     * 清空所有缓存
     */
    private void clearAllCaches() {
        // 清空LRU缓存
        synchronized (lruCache) {
            cachedObjectsCount.addAndGet(-lruCache.size());
            lruCache.clear();
        }

        // 清空软引用缓存
        softCache.clear();

        // 清空弱引用缓存
        weakCache.clear();

        log.info("已清空所有缓存，释放对象数: {}", cachedObjectsCount.get());
        cachedObjectsCount.set(0);
    }

    /**
     * 清理对象池
     */
    private void clearObjectPools() {
        objectPools.values().forEach(ObjectPool::clear);
    }

    /**
     * 获取对象池中的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getObjectFromPool(String poolName, Class<T> type) {
        if (!config.isEnableObjectPool()) {
            return createNewInstance(type);
        }

        ObjectPool<?> pool = objectPools.get(poolName);
        if (pool != null) {
            try {
                return (T) pool.borrowObject();
            } catch (Exception e) {
                log.warn("从对象池获取对象失败: poolName={}", poolName, e);
            }
        }

        return createNewInstance(type);
    }

    /**
     * 归还对象到对象池
     */
    public <T> void returnObjectToPool(String poolName, T object) {
        if (!config.isEnableObjectPool() || object == null) {
            return;
        }

        ObjectPool<?> pool = objectPools.get(poolName);
        if (pool != null) {
            try {
                pool.returnObject(object);
            } catch (Exception e) {
                log.warn("归还对象到池失败: poolName={}", poolName, e);
            }
        }
    }

    /**
     * 创建新实例
     */
    @SuppressWarnings("unchecked")
    private <T> T createNewInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("创建实例失败: type={}", type.getName(), e);
            return null;
        }
    }

    /**
     * LRU缓存存储
     */
    public void putToLRUCache(String key, Object value) {
        if (value == null) {
            return;
        }

        synchronized (lruCache) {
            Object existing = lruCache.put(key, value);
            if (existing == null) {
                cachedObjectsCount.incrementAndGet();
            }
        }
    }

    /**
     * LRU缓存获取
     */
    public Object getFromLRUCache(String key) {
        synchronized (lruCache) {
            return lruCache.get(key);
        }
    }

    /**
     * 软引用缓存存储
     */
    public void putToSoftCache(String key, Object value) {
        if (!config.isEnableSoftCache() || value == null) {
            return;
        }

        softCache.put(key, new SoftReference<>(value));
    }

    /**
     * 软引用缓存获取
     */
    public Object getFromSoftCache(String key) {
        if (!config.isEnableSoftCache()) {
            return null;
        }

        SoftReference<?> ref = softCache.get(key);
        return ref != null ? ref.get() : null;
    }

    /**
     * 弱引用缓存存储
     */
    public void putToWeakCache(String key, Object value) {
        if (!config.isEnableWeakCache() || value == null) {
            return;
        }

        weakCache.put(key, new WeakReference<>(value));
    }

    /**
     * 弱引用缓存获取
     */
    public Object getFromWeakCache(String key) {
        if (!config.isEnableWeakCache()) {
            return null;
        }

        WeakReference<?> ref = weakCache.get(key);
        return ref != null ? ref.get() : null;
    }

    /**
     * 内存统计信息
     */
    public MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        return MemoryStats.builder()
            .totalMemory(totalMemory)
            .usedMemory(usedMemory)
            .freeMemory(freeMemory)
            .maxMemory(maxMemory)
            .memoryUsageRatio((double) usedMemory / maxMemory)
            .cachedObjectsCount(cachedObjectsCount.get())
            .memoryOptimizationsCount(memoryOptimizationsCount.get())
            .lruCacheSize(lruCache.size())
            .softCacheSize(softCache.size())
            .weakCacheSize(weakCache.size())
            .objectPoolCount(objectPools.size())
            .build();
    }

    /**
     * 内存统计信息类
     */
    @lombok.Builder
    @lombok.Data
    public static class MemoryStats {
        private long totalMemory;
        private long usedMemory;
        private long freeMemory;
        private long maxMemory;
        private double memoryUsageRatio;
        private long cachedObjectsCount;
        private long memoryOptimizationsCount;
        private int lruCacheSize;
        private int softCacheSize;
        private int weakCacheSize;
        private int objectPoolCount;

        public String getMemoryUsageMB() {
            return String.format("%.1f MB", usedMemory / 1024.0 / 1024.0);
        }

        public String getMaxMemoryMB() {
            return String.format("%.1f MB", maxMemory / 1024.0 / 1024.0);
        }

        public String getMemoryUsagePercent() {
            return String.format("%.1f%%", memoryUsageRatio * 100);
        }
    }

    /**
     * 对象池类
     */
    private static class ObjectPool<T> {
        private final java.util.Queue<T> pool = new java.util.concurrent.ConcurrentLinkedQueue<>();
        private final java.util.function.Supplier<T> factory;
        private final java.util.function.Consumer<T> resetFunction;
        private final java.util.concurrent.atomic.AtomicInteger createdCount = new java.util.concurrent.atomic.AtomicInteger(0);
        private final java.util.concurrent.atomic.AtomicInteger borrowedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        public ObjectPool(java.util.function.Supplier<T> factory, java.util.function.Consumer<T> resetFunction) {
            this.factory = factory;
            this.resetFunction = resetFunction;
        }

        public T borrowObject() {
            T obj = pool.poll();
            if (obj == null) {
                obj = factory.get();
                createdCount.incrementAndGet();
            }
            borrowedCount.incrementAndGet();
            return obj;
        }

        public void returnObject(T obj) {
            if (obj != null) {
                try {
                    resetFunction.accept(obj);
                    pool.offer(obj);
                } catch (Exception e) {
                    // 忽略重置异常，避免污染对象池
                }
            }
        }

        public void clear() {
            pool.clear();
        }

        public int size() {
            return pool.size();
        }

        public int getCreatedCount() {
            return createdCount.get();
        }

        public int getBorrowedCount() {
            return borrowedCount.get();
        }
    }
}