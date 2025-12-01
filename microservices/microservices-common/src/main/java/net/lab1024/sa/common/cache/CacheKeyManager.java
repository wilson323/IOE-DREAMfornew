package net.lab1024.sa.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存键管理器
 * <p>
 * 提供统一的缓存键生成和管理功能
 * 支持命名空间、版本控制、键前缀等功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@Component
public class CacheKeyManager {

    private static final String DEFAULT_SEPARATOR = ":";
    private static final String VERSION_PREFIX = "v";

    /**
     * 缓存键计数器，用于生成唯一键
     */
    private final ConcurrentHashMap<String, AtomicLong> keyCounters = new ConcurrentHashMap<>();

    /**
     * 生成缓存键
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @return 完整的缓存键
     */
    public String generateKey(CacheNamespace namespace, String key) {
        if (namespace == null) {
            throw new IllegalArgumentException("缓存命名空间不能为空");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("缓存键不能为空");
        }
        return namespace.getPrefix() + DEFAULT_SEPARATOR + key;
    }

    /**
     * 生成带版本的缓存键
     *
     * @param namespace 缓存命名空间
     * @param key       缓存键
     * @param version   版本号
     * @return 带版本的完整缓存键
     */
    public String generateKey(CacheNamespace namespace, String key, int version) {
        String baseKey = generateKey(namespace, key);
        return baseKey + DEFAULT_SEPARATOR + VERSION_PREFIX + version;
    }

    /**
     * 生成多级缓存键
     *
     * @param namespace 缓存命名空间
     * @param keys      多级键数组
     * @return 多级完整缓存键
     */
    public String generateMultiLevelKey(CacheNamespace namespace, String... keys) {
        if (namespace == null) {
            throw new IllegalArgumentException("缓存命名空间不能为空");
        }
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("缓存键不能为空");
        }

        StringBuilder keyBuilder = new StringBuilder(namespace.getPrefix());
        for (String key : keys) {
            if (StringUtils.hasText(key)) {
                keyBuilder.append(DEFAULT_SEPARATOR).append(key);
            }
        }

        return keyBuilder.toString();
    }

    /**
     * 生成用户相关缓存键
     *
     * @param namespace 缓存命名空间
     * @param userId    用户ID
     * @param key       缓存键
     * @return 用户相关的完整缓存键
     */
    public String generateUserKey(CacheNamespace namespace, Long userId, String key) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return generateMultiLevelKey(namespace, "user", userId.toString(), key);
    }

    /**
     * 生成唯一缓存键
     *
     * @param namespace 缓存命名空间
     * @param prefix    键前缀
     * @return 唯一的完整缓存键
     */
    public String generateUniqueKey(CacheNamespace namespace, String prefix) {
        String counterKey = namespace.getPrefix() + DEFAULT_SEPARATOR + prefix;
        AtomicLong counter = keyCounters.computeIfAbsent(counterKey, k -> new AtomicLong(0));
        long uniqueId = counter.incrementAndGet();

        return generateMultiLevelKey(namespace, prefix, String.valueOf(uniqueId));
    }

    /**
     * 解析缓存键，提取命名空间
     *
     * @param fullKey 完整缓存键
     * @return 缓存命名空间，如果无法解析则返回null
     */
    public CacheNamespace parseNamespace(String fullKey) {
        if (!StringUtils.hasText(fullKey)) {
            return null;
        }

        int separatorIndex = fullKey.indexOf(DEFAULT_SEPARATOR);
        if (separatorIndex == -1) {
            return null;
        }

        String prefix = fullKey.substring(0, separatorIndex);
        try {
            return CacheNamespace.valueOfPrefix(prefix);
        } catch (IllegalArgumentException e) {
            log.debug("无法解析缓存命名空间: {}", prefix);
            return null;
        }
    }

    /**
     * 解析缓存键，提取业务键
     *
     * @param fullKey 完整缓存键
     * @return 业务键，如果无法解析则返回null
     */
    public String parseBusinessKey(String fullKey) {
        if (!StringUtils.hasText(fullKey)) {
            return null;
        }

        int separatorIndex = fullKey.indexOf(DEFAULT_SEPARATOR);
        if (separatorIndex == -1 || separatorIndex == fullKey.length() - 1) {
            return null;
        }

        return fullKey.substring(separatorIndex + 1);
    }

    /**
     * 检查缓存键是否匹配指定命名空间
     *
     * @param fullKey    完整缓存键
     * @param namespace  缓存命名空间
     * @return 是否匹配
     */
    public boolean isMatchNamespace(String fullKey, CacheNamespace namespace) {
        CacheNamespace parsedNamespace = parseNamespace(fullKey);
        return parsedNamespace == namespace;
    }

    /**
     * 重置键计数器
     *
     * @param namespace 缓存命名空间
     * @param prefix    键前缀
     */
    public void resetCounter(CacheNamespace namespace, String prefix) {
        String counterKey = namespace.getPrefix() + DEFAULT_SEPARATOR + prefix;
        keyCounters.remove(counterKey);
    }

    /**
     * 获取键计数器当前值
     *
     * @param namespace 缓存命名空间
     * @param prefix    键前缀
     * @return 当前计数器值
     */
    public long getCounterValue(CacheNamespace namespace, String prefix) {
        String counterKey = namespace.getPrefix() + DEFAULT_SEPARATOR + prefix;
        AtomicLong counter = keyCounters.get(counterKey);
        return counter != null ? counter.get() : 0;
    }
}