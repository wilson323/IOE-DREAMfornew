package net.lab1024.sa.admin.module.consume.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.base.common.cache.CacheService;
import net.lab1024.sa.base.common.cache.RedisUtil;

/**
 * 消费缓存服务
 * 用于管理消费相关的缓存数据
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Service
public class ConsumeCacheService {

    @Resource
    private CacheService cacheService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "consume:";
    private static final String ACCOUNT_PREFIX = CACHE_PREFIX + "account:";
    private static final String BALANCE_PREFIX = CACHE_PREFIX + "balance:";
    private static final String CONFIG_PREFIX = CACHE_PREFIX + "config:";
    private static final String DEVICE_PREFIX = CACHE_PREFIX + "device:";
    private static final String STATS_PREFIX = CACHE_PREFIX + "stats:";

    // 缓存过期时间配置
    private static final Duration ACCOUNT_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration BALANCE_CACHE_TTL = Duration.ofMinutes(5);
    private static final Duration CONFIG_CACHE_TTL = Duration.ofMinutes(60);
    private static final Duration DEVICE_CACHE_TTL = Duration.ofMinutes(15);
    private static final Duration STATS_CACHE_TTL = Duration.ofMinutes(10);

    /**
     * 缓存账户信息（多级缓存）
     */
    public void cacheAccountInfo(Long userId, AccountEntity account) {
        String key = ACCOUNT_PREFIX + userId;
        try {
            // 同时更新L1和L2缓存
            cacheService.set(key, account, ACCOUNT_CACHE_TTL);
            redisUtil.setEx(key, account, ACCOUNT_CACHE_TTL.getSeconds());
            log.debug("缓存账户信息: userId={}", userId);
        } catch (Exception e) {
            log.error("缓存账户信息失败: userId={}", userId, e);
        }
    }

    /**
     * 获取缓存的账户信息（多级缓存）
     */
    public AccountEntity getCachedAccountInfo(Long userId) {
        String key = ACCOUNT_PREFIX + userId;

        try {
            // 先从L1缓存获取
            AccountEntity cachedAccount = cacheService.get(key, AccountEntity.class);
            if (cachedAccount != null) {
                log.debug("从L1缓存获取账户信息: userId={}", userId);
                return cachedAccount;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(key);
            if (redisValue != null) {
                AccountEntity account = (AccountEntity) redisValue;
                // 回填到L1缓存
                cacheService.set(key, account, ACCOUNT_CACHE_TTL);
                log.debug("从L2缓存获取账户信息: userId={}", userId);
                return account;
            }

            log.debug("账户信息缓存未命中: userId={}", userId);
            return null;
        } catch (Exception e) {
            log.error("获取缓存账户信息失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 清除账户信息缓存
     */
    public void evictAccountCache(Long userId) {
        String key = ACCOUNT_PREFIX + userId;
        try {
            cacheService.delete(key);
            redisUtil.delete(key);
            log.debug("清除账户缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除账户缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 缓存账户余额（多级缓存）
     */
    public void cacheAccountBalance(Long userId, BigDecimal balance) {
        String key = BALANCE_PREFIX + userId;
        try {
            // 同时更新L1和L2缓存
            cacheService.set(key, balance, BALANCE_CACHE_TTL);
            redisUtil.setEx(key, balance.toString(), BALANCE_CACHE_TTL.getSeconds());
            log.debug("缓存账户余额: userId={}, balance={}", userId, balance);
        } catch (Exception e) {
            log.error("缓存账户余额失败: userId={}, balance={}", userId, balance, e);
        }
    }

    /**
     * 获取缓存的账户余额（多级缓存）
     */
    public BigDecimal getCachedAccountBalance(Long userId) {
        String key = BALANCE_PREFIX + userId;

        try {
            // 先从L1缓存获取
            BigDecimal cachedBalance = cacheService.get(key, BigDecimal.class);
            if (cachedBalance != null) {
                log.debug("从L1缓存获取账户余额: userId={}", userId);
                return cachedBalance;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(key);
            if (redisValue != null) {
                BigDecimal balance = new BigDecimal(redisValue.toString());
                // 回填到L1缓存
                cacheService.set(key, balance, BALANCE_CACHE_TTL);
                log.debug("从L2缓存获取账户余额: userId={}", userId);
                return balance;
            }

            log.debug("账户余额缓存未命中: userId={}", userId);
            return null;
        } catch (Exception e) {
            log.error("获取缓存账户余额失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 清除余额缓存
     */
    public void evictBalanceCache(Long userId) {
        String key = BALANCE_PREFIX + userId;
        try {
            cacheService.delete(key);
            redisUtil.delete(key);
            log.debug("清除余额缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除余额缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 缓存设备配置（多级缓存）
     */
    public void cacheDeviceConfig(Long deviceId, Map<String, Object> config) {
        String key = DEVICE_PREFIX + deviceId;
        try {
            cacheService.set(key, config, DEVICE_CACHE_TTL);
            redisUtil.setEx(key, config, DEVICE_CACHE_TTL.getSeconds());
            log.debug("缓存设备配置: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("缓存设备配置失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 获取缓存的设备配置（多级缓存）
     */
    public Map<String, Object> getCachedDeviceConfig(Long deviceId) {
        String key = DEVICE_PREFIX + deviceId;

        try {
            // 先从L1缓存获取
            Map<String, Object> cachedConfig = cacheService.get(key, Map.class);
            if (cachedConfig != null) {
                log.debug("从L1缓存获取设备配置: deviceId={}", deviceId);
                return cachedConfig;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(key);
            if (redisValue != null) {
                Map<String, Object> config = (Map<String, Object>) redisValue;
                // 回填到L1缓存
                cacheService.set(key, config, DEVICE_CACHE_TTL);
                log.debug("从L2缓存获取设备配置: deviceId={}", deviceId);
                return config;
            }

            log.debug("设备配置缓存未命中: deviceId={}", deviceId);
            return null;
        } catch (Exception e) {
            log.error("获取缓存设备配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 清除设备配置缓存
     */
    public void evictDeviceConfigCache(Long deviceId) {
        String key = DEVICE_PREFIX + deviceId;
        try {
            cacheService.delete(key);
            redisUtil.delete(key);
            log.debug("清除设备配置缓存: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("清除设备配置缓存失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 缓存消费统计信息（多级缓存）
     */
    public void cacheConsumeStats(String statsKey, Map<String, Object> stats) {
        String key = STATS_PREFIX + statsKey;
        try {
            cacheService.set(key, stats, STATS_CACHE_TTL);
            redisUtil.setEx(key, stats, STATS_CACHE_TTL.getSeconds());
            log.debug("缓存消费统计信息: statsKey={}", statsKey);
        } catch (Exception e) {
            log.error("缓存消费统计信息失败: statsKey={}", statsKey, e);
        }
    }

    /**
     * 获取缓存的消费统计信息（多级缓存）
     */
    public Map<String, Object> getCachedConsumeStats(String statsKey) {
        String key = STATS_PREFIX + statsKey;

        try {
            // 先从L1缓存获取
            Map<String, Object> cachedStats = cacheService.get(key, Map.class);
            if (cachedStats != null) {
                log.debug("从L1缓存获取消费统计信息: statsKey={}", statsKey);
                return cachedStats;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(key);
            if (redisValue != null) {
                Map<String, Object> stats = (Map<String, Object>) redisValue;
                // 回填到L1缓存
                cacheService.set(key, stats, STATS_CACHE_TTL);
                log.debug("从L2缓存获取消费统计信息: statsKey={}", statsKey);
                return stats;
            }

            log.debug("消费统计信息缓存未命中: statsKey={}", statsKey);
            return null;
        } catch (Exception e) {
            log.error("获取缓存消费统计信息失败: statsKey={}", statsKey, e);
            return null;
        }
    }

    /**
     * 清除消费统计信息缓存
     */
    public void evictConsumeStatsCache(String statsKey) {
        String key = STATS_PREFIX + statsKey;
        try {
            cacheService.delete(key);
            redisUtil.delete(key);
            log.debug("清除消费统计信息缓存: statsKey={}", statsKey);
        } catch (Exception e) {
            log.error("清除消费统计信息缓存失败: statsKey={}", statsKey, e);
        }
    }

    /**
     * 缓存消费配置
     */
    public void cacheConsumeConfig(String configType, Object config) {
        String key = CONFIG_PREFIX + configType;
        try {
            cacheService.set(key, config, CONFIG_CACHE_TTL);
            redisUtil.setEx(key, config, CONFIG_CACHE_TTL.getSeconds());
            log.debug("缓存消费配置: configType={}", configType);
        } catch (Exception e) {
            log.error("缓存消费配置失败: configType={}", configType, e);
        }
    }

    /**
     * 获取缓存的消费配置
     */
    public <T> T getCachedConsumeConfig(String configType, Class<T> clazz) {
        String key = CONFIG_PREFIX + configType;
        try {
            // 先从L1缓存获取
            T cachedConfig = cacheService.get(key, clazz);
            if (cachedConfig != null) {
                log.debug("从L1缓存获取消费配置: configType={}", configType);
                return cachedConfig;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(key);
            if (redisValue != null) {
                T config = clazz.cast(redisValue);
                // 回填到L1缓存
                cacheService.set(key, config, CONFIG_CACHE_TTL);
                log.debug("从L2缓存获取消费配置: configType={}", configType);
                return config;
            }

            log.debug("消费配置缓存未命中: configType={}", configType);
            return null;
        } catch (Exception e) {
            log.error("获取缓存消费配置失败: configType={}", configType, e);
            return null;
        }
    }

    /**
     * 清除配置缓存
     */
    public void evictConfigCache(String configType) {
        String key = CONFIG_PREFIX + configType;
        try {
            cacheService.delete(key);
            redisUtil.delete(key);
            log.debug("清除配置缓存: configType={}", configType);
        } catch (Exception e) {
            log.error("清除配置缓存失败: configType={}", configType, e);
        }
    }

    /**
     * 清除用户相关的所有缓存
     */
    public void evictUserAllCache(Long userId) {
        try {
            evictAccountCache(userId);
            evictBalanceCache(userId);
            log.info("清除用户所有缓存: userId={}", userId);
        } catch (Exception e) {
            log.error("清除用户所有缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 通用缓存获取，使用Supplier实现加载回源
     */
    public <T> T getOrLoad(String key, Class<T> clazz, Supplier<T> loader, Duration ttl) {
        String fullKey = CACHE_PREFIX + key;

        try {
            // 先从L1缓存获取
            T cachedValue = cacheService.get(fullKey, clazz);
            if (cachedValue != null) {
                log.debug("从L1缓存命中: key={}", key);
                return cachedValue;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(fullKey);
            if (redisValue != null) {
                T value = clazz.cast(redisValue);
                // 回填到L1缓存
                cacheService.set(fullKey, value, ttl);
                log.debug("从L2缓存命中: key={}", key);
                return value;
            }

            // 缓存未命中，加载回源
            log.debug("缓存未命中，开始加载: key={}", key);
            T value = loader.get();
            if (value != null) {
                // 异步更新缓存
                setCacheAsync(fullKey, value, ttl);
            }

            return value;
        } catch (Exception e) {
            log.error("获取或加载缓存失败: key={}", key, e);
            return loader.get();
        }
    }

    /**
     * 异步设置缓存
     */
    @Async
    public CompletableFuture<Void> setCacheAsync(String key, Object value, Duration ttl) {
        try {
            cacheService.set(key, value, ttl);
            redisUtil.setEx(key, value, ttl.getSeconds());
            log.debug("异步设置缓存成功: key={}, ttl={}s", key, ttl.getSeconds());
        } catch (Exception e) {
            log.error("异步设置缓存失败: key={}", key, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 批量删除缓存
     */
    @Async
    public CompletableFuture<Void> batchEvict(String pattern) {
        try {
            String fullPattern = CACHE_PREFIX + pattern;

            // 删除L1缓存（这里简化处理，实际需要根据具体缓存实现）
            log.debug("清除L1缓存: pattern={}", fullPattern);

            // 删除L2缓存
            Set<Object> keys = redisTemplate.keys(fullPattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("清除L2缓存: 删除了{}个键", keys.size());
            }

        } catch (Exception e) {
            log.error("批量删除缓存失败: pattern={}", pattern, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 预热缓存
     */
    @Async
    public CompletableFuture<Void> warmUpCache() {
        try {
            log.info("开始预热消费模块缓存...");

            // 预热常用设备配置
            // warmUpDeviceConfigs();

            // 预热用户余额（可以预加载活跃用户）
            // warmUpUserBalances();

            log.info("消费模块缓存预热完成");
        } catch (Exception e) {
            log.error("缓存预热失败", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // L1缓存统计（这里简化处理）
            stats.put("l1Cache", Map.of(
                    "name", "Caffeine",
                    "status", "active"));

            // L2缓存统计
            String info = redisUtil.getInfo();
            stats.put("l2Cache", Map.of(
                    "name", "Redis",
                    "status", "active",
                    "info", info));

            // 缓存键统计
            Set<Object> consumeKeys = redisTemplate.keys(CACHE_PREFIX + "*");
            int keyCount = consumeKeys != null ? consumeKeys.size() : 0;
            stats.put("consumeCacheKeyCount", keyCount);

        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * 清除用户相关缓存
     */
    @Async
    public CompletableFuture<Void> evictUserCache(Long userId) {
        String pattern = ACCOUNT_PREFIX + userId + "*" + BALANCE_PREFIX + userId + "*";
        return batchEvict(pattern);
    }

    /**
     * 清除设备相关缓存
     */
    @Async
    public CompletableFuture<Void> evictDeviceCache(Long deviceId) {
        String pattern = DEVICE_PREFIX + deviceId;
        return batchEvict(pattern);
    }

    /**
     * 清除统计相关缓存
     */
    @Async
    public CompletableFuture<Void> evictStatsCache() {
        String pattern = STATS_PREFIX + "*";
        return batchEvict(pattern);
    }

    /**
     * 通用缓存获取方法
     *
     * @param key 缓存键
     * @return 缓存值，如果不存在返回null
     */
    public Object getCachedValue(String key) {
        String fullKey = CACHE_PREFIX + key;
        try {
            // 先从L1缓存获取
            Object cachedValue = cacheService.get(fullKey);
            if (cachedValue != null) {
                log.debug("从L1缓存获取: key={}", key);
                return cachedValue;
            }

            // 从L2缓存获取
            Object redisValue = redisUtil.get(fullKey);
            if (redisValue != null) {
                // 回填到L1缓存（使用默认TTL）
                cacheService.set(fullKey, redisValue);
                log.debug("从L2缓存获取: key={}", key);
                return redisValue;
            }

            log.debug("缓存未命中: key={}", key);
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 通用缓存设置方法
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    public void setCachedValue(String key, Object value, int ttlSeconds) {
        String fullKey = CACHE_PREFIX + key;
        try {
            Duration ttl = Duration.ofSeconds(ttlSeconds);
            // 同时更新L1和L2缓存
            cacheService.set(fullKey, value, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
            redisUtil.setEx(fullKey, value, ttlSeconds);
            log.debug("设置缓存: key={}, ttl={}s", key, ttlSeconds);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }
}
