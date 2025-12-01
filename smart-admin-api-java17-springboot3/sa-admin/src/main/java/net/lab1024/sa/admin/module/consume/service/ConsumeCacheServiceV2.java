package net.lab1024.sa.admin.module.consume.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import net.lab1024.sa.base.common.cache.BusinessDataType;

/**
 * 消费缓存服务 V2版本
 * 基于统一缓存架构的消费相关缓存管理服务
 * 支持批量操作、异步操作和更高级的缓存策略
 *
 * @author SmartAdmin Team
 * @date 2025-11-23
 */
@Service
public class ConsumeCacheServiceV2 {
    private static final Logger log = LoggerFactory.getLogger(ConsumeCacheServiceV2.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CACHE_PREFIX = "consume:v2:";
    private static final String ACCOUNT_PREFIX = CACHE_PREFIX + "account:";
    private static final String BALANCE_PREFIX = CACHE_PREFIX + "balance:";
    private static final String DEVICE_PREFIX = CACHE_PREFIX + "device:";

    /**
     * 缓存账户信息
     */
    public void cacheAccountInfo(Long userId, String accountInfo) {
        if (userId == null || accountInfo == null) {
            log.warn("缓存账户信息参数为空: userId={}, accountInfo={}", userId, accountInfo);
            return;
        }

        String key = buildAccountKey(userId);
        try {
            redisTemplate.opsForValue().set(key, accountInfo);
            log.debug("缓存账户信息成功: userId={}", userId);
        } catch (Exception e) {
            log.error("缓存账户信息失败: userId={}", userId, e);
        }
    }

    /**
     * 缓存账户信息（带数据类型标识）
     */
    public void cacheAccountInfo(Long userId, String accountInfo, BusinessDataType dataType) {
        if (userId == null || accountInfo == null) {
            log.warn("缓存账户信息参数为空: userId={}, accountInfo={}", userId, accountInfo);
            return;
        }

        String key = buildAccountKey(userId);
        try {
            redisTemplate.opsForValue().set(key, accountInfo);
            log.debug("缓存账户信息成功: userId={}, dataType={}", userId, dataType);
        } catch (Exception e) {
            log.error("缓存账户信息失败: userId={}, dataType={}", userId, dataType, e);
        }
    }

    /**
     * 缓存账户余额
     */
    public void cacheAccountBalance(Long userId, BigDecimal balance) {
        if (userId == null || balance == null) {
            log.warn("缓存账户余额参数为空: userId={}, balance={}", userId, balance);
            return;
        }

        String key = buildBalanceKey(userId);
        try {
            redisTemplate.opsForValue().set(key, balance);
            log.debug("缓存账户余额成功: userId={}, balance={}", userId, balance);
        } catch (Exception e) {
            log.error("缓存账户余额失败: userId={}", userId, e);
        }
    }

    /**
     * 获取缓存的账户信息
     */
    public String getCachedAccountInfo(Long userId) {
        if (userId == null) {
            return null;
        }

        String key = buildAccountKey(userId);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("命中账户信息缓存: userId={}", userId);
                return value.toString();
            } else {
                log.debug("未命中账户信息缓存: userId={}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("获取账户信息缓存失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取缓存的账户余额
     */
    public BigDecimal getCachedAccountBalance(Long userId) {
        if (userId == null) {
            return null;
        }

        String key = buildBalanceKey(userId);
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("命中账户余额缓存: userId={}, balance={}", userId, value);
                return (BigDecimal) value;
            } else {
                log.debug("未命中账户余额缓存: userId={}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("获取账户余额缓存失败: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 缓存设备配置
     */
    public void cacheDeviceConfig(String deviceId, Map<String, Object> config) {
        if (deviceId == null || config == null) {
            log.warn("缓存设备配置参数为空: deviceId={}, config={}", deviceId, config);
            return;
        }

        String key = buildDeviceKey(deviceId);
        try {
            redisTemplate.opsForHash().putAll(key, config);
            log.debug("缓存设备配置成功: deviceId={}", deviceId);
        } catch (Exception e) {
            log.error("缓存设备配置失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 缓存设备配置（支持Long类型设备ID）
     */
    public void cacheDeviceConfig(Long deviceId, Map<String, Object> config) {
        if (deviceId == null || config == null) {
            log.warn("缓存设备配置参数为空: deviceId={}, config={}", deviceId, config);
            return;
        }

        cacheDeviceConfig(deviceId.toString(), config);
    }

    /**
     * 获取缓存的设备配置
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedDeviceConfig(String deviceId) {
        if (deviceId == null) {
            return null;
        }

        String key = buildDeviceKey(deviceId);
        try {
            Map<Object, Object> values = redisTemplate.opsForHash().entries(key);
            if (values != null && !values.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                for (Map.Entry<Object, Object> entry : values.entrySet()) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
                log.debug("命中设备配置缓存: deviceId={}", deviceId);
                return result;
            } else {
                log.debug("未命中设备配置缓存: deviceId={}", deviceId);
                return null;
            }
        } catch (Exception e) {
            log.error("获取设备配置缓存失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 获取缓存的设备配置（支持Long类型设备ID）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCachedDeviceConfig(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return getCachedDeviceConfig(deviceId.toString());
    }

    /**
     * 批量缓存设备配置（String类型设备ID）
     */
    public void batchCacheDeviceConfigsString(Map<String, Map<String, Object>> deviceConfigs) {
        if (deviceConfigs == null || deviceConfigs.isEmpty()) {
            log.warn("批量缓存设备配置参数为空");
            return;
        }

        try {
            for (Map.Entry<String, Map<String, Object>> entry : deviceConfigs.entrySet()) {
                String deviceId = entry.getKey();
                Map<String, Object> config = entry.getValue();
                if (deviceId != null && config != null) {
                    cacheDeviceConfig(deviceId, config);
                }
            }
            log.info("批量缓存设备配置完成: {} 个设备", deviceConfigs.size());
        } catch (Exception e) {
            log.error("批量缓存设备配置失败", e);
        }
    }

    /**
     * 批量缓存设备配置（支持Long类型设备ID）
     */
    public void batchCacheDeviceConfigs(Map<Long, Map<String, Object>> deviceConfigs) {
        if (deviceConfigs == null || deviceConfigs.isEmpty()) {
            log.warn("批量缓存设备配置参数为空");
            return;
        }

        try {
            for (Map.Entry<Long, Map<String, Object>> entry : deviceConfigs.entrySet()) {
                Long deviceId = entry.getKey();
                Map<String, Object> config = entry.getValue();
                if (deviceId != null && config != null) {
                    cacheDeviceConfig(deviceId, config);
                }
            }
            log.info("批量缓存设备配置完成: {} 个设备", deviceConfigs.size());
        } catch (Exception e) {
            log.error("批量缓存设备配置失败", e);
        }
    }

    /**
     * 批量获取账户信息
     */
    public Map<Long, String> batchGetAccountInfos(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, String> resultMap = new HashMap<>();
        try {
            for (Long userId : userIds) {
                String info = getCachedAccountInfo(userId);
                if (info != null) {
                    resultMap.put(userId, info);
                }
            }
            log.debug("批量获取账户信息完成: {} 个用户，命中 {} 个", userIds.size(), resultMap.size());
        } catch (Exception e) {
            log.error("批量获取账户信息失败", e);
        }
        return resultMap;
    }

    /**
     * 异步缓存账户余额
     */
    @Async
    public CompletableFuture<Void> cacheAccountBalanceAsync(Long userId, BigDecimal balance) {
        return CompletableFuture.runAsync(() -> {
            try {
                cacheAccountBalance(userId, balance);
                log.debug("异步缓存账户余额完成: userId={}, balance={}", userId, balance);
            } catch (Exception e) {
                log.error("异步缓存账户余额失败: userId={}, balance={}", userId, balance, e);
            }
        });
    }

    /**
     * 异步清理用户所有缓存
     */
    @Async
    public CompletableFuture<Void> evictUserAllCacheAsync(Long userId) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (userId == null) {
                    return;
                }

                // 清理账户信息
                String accountKey = buildAccountKey(userId);
                redisTemplate.delete(accountKey);

                // 清理账户余额
                String balanceKey = buildBalanceKey(userId);
                redisTemplate.delete(balanceKey);

                log.info("异步清理用户所有缓存完成: userId={}", userId);
            } catch (Exception e) {
                log.error("异步清理用户所有缓存失败: userId={}", userId, e);
            }
        });
    }

    /**
     * 构建账户信息缓存键
     */
    private String buildAccountKey(Long userId) {
        return ACCOUNT_PREFIX + userId;
    }

    /**
     * 构建账户余额缓存键
     */
    private String buildBalanceKey(Long userId) {
        return BALANCE_PREFIX + userId;
    }

    /**
     * 构建设备配置缓存键
     */
    private String buildDeviceKey(String deviceId) {
        return DEVICE_PREFIX + deviceId;
    }
}