package net.lab1024.sa.common.example;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 统一配置使用示例
 * 
 * <p>本类演示如何使用:</p>
 * <ul>
 *   <li>1. 统一线程池配置</li>
 *   <li>2. 统一Caffeine缓存配置</li>
 *   <li>3. 最佳实践和注意事项</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Slf4j
@Service
public class UnifiedConfigurationExample {

    // ==================== 注入统一线程池 ====================
    
    /**
     * 异步任务线程池(IO密集型)
     * 适用场景: 数据库操作、HTTP调用、文件IO等
     */
    @Resource(name = "asyncExecutor")
    private ThreadPoolTaskExecutor asyncExecutor;

    /**
     * 业务处理线程池(CPU密集型)
     * 适用场景: 复杂计算、数据处理、加密解密等
     */
    @Resource(name = "businessExecutor")
    private ThreadPoolTaskExecutor businessExecutor;

    /**
     * 定时任务线程池
     * 适用场景: 定时任务、周期性任务等
     */
    @Resource(name = "scheduledExecutor")
    private ThreadPoolTaskExecutor scheduledExecutor;

    // ==================== 注入统一缓存 ====================
    
    /**
     * 热数据缓存
     * 适用场景: 用户信息、权限数据、菜单数据等
     */
    @Resource(name = "hotDataCache")
    private Cache<String, Object> hotDataCache;

    /**
     * 冷数据缓存
     * 适用场景: 字典数据、系统配置等
     */
    @Resource(name = "coldDataCache")
    private Cache<String, Object> coldDataCache;

    /**
     * 临时数据缓存
     * 适用场景: 验证码、临时令牌等
     */
    @Resource(name = "tempDataCache")
    private Cache<String, Object> tempDataCache;

    // ==================== 线程池使用示例 ====================

    /**
     * 示例1: 使用异步线程池执行IO密集型任务
     */
    public void asyncIoTaskExample() {
        asyncExecutor.execute(() -> {
            try {
                log.info("[异步任务] 开始执行数据库查询...");
                // 模拟数据库查询
                Thread.sleep(100);
                log.info("[异步任务] 数据库查询完成");
            } catch (Exception e) {
                log.error("[异步任务] 执行异常", e);
            }
        });
    }

    /**
     * 示例2: 使用CompletableFuture + 异步线程池
     */
    public CompletableFuture<String> asyncTaskWithResultExample() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[异步任务] 开始处理...");
                Thread.sleep(100);
                return "处理结果";
            } catch (Exception e) {
                log.error("[异步任务] 执行异常", e);
                return "处理失败";
            }
        }, asyncExecutor);
    }

    /**
     * 示例3: 使用业务线程池执行CPU密集型任务
     */
    public void cpuIntensiveTaskExample() {
        businessExecutor.execute(() -> {
            try {
                log.info("[业务任务] 开始执行复杂计算...");
                // 模拟复杂计算
                long result = fibonacci(30);
                log.info("[业务任务] 计算完成, 结果: {}", result);
            } catch (Exception e) {
                log.error("[业务任务] 执行异常", e);
            }
        });
    }

    // ==================== 缓存使用示例 ====================

    /**
     * 示例4: 使用热数据缓存存储用户信息
     */
    public Object getUserFromCache(String userId) {
        return hotDataCache.get(userId, key -> {
            log.info("[热数据缓存] 缓存未命中, 从数据库加载用户: {}", key);
            // 模拟从数据库加载
            return loadUserFromDatabase(key);
        });
    }

    /**
     * 示例5: 使用冷数据缓存存储字典数据
     */
    public Object getDictFromCache(String dictCode) {
        return coldDataCache.get(dictCode, key -> {
            log.info("[冷数据缓存] 缓存未命中, 从数据库加载字典: {}", key);
            // 模拟从数据库加载
            return loadDictFromDatabase(key);
        });
    }

    /**
     * 示例6: 使用临时数据缓存存储验证码
     */
    public void setVerificationCode(String phone, String code) {
        tempDataCache.put(phone, code);
        log.info("[临时数据缓存] 存储验证码: phone={}, code={}", phone, code);
    }

    /**
     * 示例7: 手动清除缓存
     */
    public void clearCache(String key) {
        hotDataCache.invalidate(key);
        log.info("[缓存操作] 已清除热数据缓存: {}", key);
    }

    /**
     * 示例8: 批量预热缓存
     */
    public void warmupCache() {
        asyncExecutor.execute(() -> {
            log.info("[缓存预热] 开始预热用户数据...");
            // 模拟批量加载
            for (int i = 1; i <= 100; i++) {
                String userId = "user_" + i;
                hotDataCache.put(userId, loadUserFromDatabase(userId));
            }
            log.info("[缓存预热] 完成, 已预热100条用户数据");
        });
    }

    // ==================== 最佳实践示例 ====================

    /**
     * 示例9: 结合线程池和缓存的最佳实践
     * 异步加载数据并缓存
     */
    public CompletableFuture<Object> loadDataAsyncWithCache(String key) {
        return CompletableFuture.supplyAsync(() -> {
            // 先查缓存
            Object cached = hotDataCache.getIfPresent(key);
            if (cached != null) {
                log.info("[最佳实践] 缓存命中: {}", key);
                return cached;
            }
            
            // 缓存未命中,从数据库加载
            log.info("[最佳实践] 缓存未命中, 异步加载: {}", key);
            Object data = loadUserFromDatabase(key);
            
            // 存入缓存
            hotDataCache.put(key, data);
            
            return data;
        }, asyncExecutor);
    }

    /**
     * 示例10: 多级缓存策略
     */
    public Object getDataWithMultiLevelCache(String key) {
        // 先查临时缓存(最快过期)
        Object temp = tempDataCache.getIfPresent(key);
        if (temp != null) {
            return temp;
        }
        
        // 再查热数据缓存
        Object hot = hotDataCache.getIfPresent(key);
        if (hot != null) {
            return hot;
        }
        
        // 最后查冷数据缓存
        return coldDataCache.get(key, k -> loadUserFromDatabase(k));
    }

    // ==================== 辅助方法 ====================

    /**
     * 模拟从数据库加载用户
     */
    private Object loadUserFromDatabase(String userId) {
        try {
            Thread.sleep(50); // 模拟数据库查询耗时
            return "User[" + userId + "]";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 模拟从数据库加载字典
     */
    private Object loadDictFromDatabase(String dictCode) {
        try {
            Thread.sleep(30); // 模拟数据库查询耗时
            return "Dict[" + dictCode + "]";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 斐波那契数列计算(模拟CPU密集型任务)
     */
    private long fibonacci(int n) {
        if (n <= 1) return n;
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}
