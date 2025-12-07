package net.lab1024.sa.common.notification.manager;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.cache.UnifiedCacheManager;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.common.util.AESUtil;

/**
 * 通知配置管理器
 * <p>
 * 负责通知配置的获取、缓存、解密等管理功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 保持为纯Java类，不依赖Spring框架
 * </p>
 * <p>
 * 企业级特性：
 * - 多级缓存支持（L1本地 + L2Redis）
 * - 配置加密解密
 * - 配置热更新
 * - 配置验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class NotificationConfigManager {

    /**
     * 配置DAO
     */
    private final NotificationConfigDao notificationConfigDao;

    /**
     * 统一缓存管理器
     */
    private final UnifiedCacheManager cacheManager;

    /**
     * JSON对象映射器
     */
    private final ObjectMapper objectMapper;

    /**
     * AES加密工具
     */
    private final AESUtil aesUtil;

    /**
     * 配置缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "notification:config:";

    /**
     * 配置缓存过期时间（30分钟）
     */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 构造函数
     * <p>
     * 通过构造函数注入依赖，保持为纯Java类
     * </p>
     *
     * @param notificationConfigDao 通知配置DAO
     * @param cacheManager          统一缓存管理器
     * @param objectMapper          JSON对象映射器
     * @param aesUtil              AES加密工具
     */
    public NotificationConfigManager(
            NotificationConfigDao notificationConfigDao,
            UnifiedCacheManager cacheManager,
            ObjectMapper objectMapper,
            AESUtil aesUtil) {
        this.notificationConfigDao = notificationConfigDao;
        this.cacheManager = cacheManager;
        this.objectMapper = objectMapper;
        this.aesUtil = aesUtil;
    }

    /**
     * 根据配置键获取配置值
     * <p>
     * 支持多级缓存，自动解密加密配置
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密）
     */
    public String getConfigValue(String configKey) {
        String cacheKey = CACHE_KEY_PREFIX + configKey;

        // 从缓存获取
        NotificationConfigEntity config = cacheManager.getWithRefresh(
                cacheKey,
                () -> notificationConfigDao.selectByConfigKey(configKey),
                CACHE_TTL);

        if (config == null) {
            return null;
        }

        // 检查配置状态
        if (config.getStatus() != 1) {
            return null; // 配置已禁用
        }

        // 解密配置值
        String configValue = config.getConfigValue();
        if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            try {
                configValue = aesUtil.decrypt(configValue);
            } catch (Exception e) {
                throw new RuntimeException("配置解密失败: " + configKey, e);
            }
        }

        return configValue;
    }

    /**
     * 根据配置键获取配置值（带默认值）
     * <p>
     * 如果配置不存在或已禁用，返回默认值
     * </p>
     *
     * @param configKey   配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 根据配置类型获取所有配置
     * <p>
     * 返回指定类型的所有启用配置
     * </p>
     *
     * @param configType 配置类型
     * @return 配置Map（key为configKey，value为configValue）
     */
    public Map<String, String> getConfigsByType(String configType) {
        String cacheKey = CACHE_KEY_PREFIX + "type:" + configType;

        // 从缓存获取
        List<NotificationConfigEntity> configs = cacheManager.getWithRefresh(
                cacheKey,
                () -> notificationConfigDao.selectByConfigTypeAndStatus(configType, 1),
                CACHE_TTL);

        // 转换为Map并解密
        return configs.stream()
                .collect(Collectors.toMap(
                        NotificationConfigEntity::getConfigKey,
                        config -> {
                            String value = config.getConfigValue();
                            if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
                                try {
                                    return aesUtil.decrypt(value);
                                } catch (Exception e) {
                                    throw new RuntimeException("配置解密失败: " + config.getConfigKey(), e);
                                }
                            }
                            return value;
                        }));
    }

    /**
     * 获取配置对象（JSON格式）
     * <p>
     * 将配置值解析为JSON对象
     * </p>
     *
     * @param configKey 配置键
     * @param clazz     目标类型
     * @param <T>       目标类型泛型
     * @return 配置对象
     */
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        if (configValue == null) {
            return null;
        }

        try {
            return objectMapper.readValue(configValue, clazz);
        } catch (Exception e) {
            throw new RuntimeException("配置JSON解析失败: " + configKey, e);
        }
    }

    /**
     * 获取配置对象（复杂类型）
     * <p>
     * 支持List、Map等复杂类型
     * </p>
     *
     * @param configKey      配置键
     * @param typeReference 类型引用
     * @param <T>           目标类型泛型
     * @return 配置对象
     */
    public <T> T getConfigObject(String configKey, TypeReference<T> typeReference) {
        String configValue = getConfigValue(configKey);
        if (configValue == null) {
            return null;
        }

        try {
            return objectMapper.readValue(configValue, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("配置JSON解析失败: " + configKey, e);
        }
    }

    /**
     * 更新配置值
     * <p>
     * 更新配置值并清除缓存
     * </p>
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    public boolean updateConfigValue(String configKey, String configValue) {
        NotificationConfigEntity config = notificationConfigDao.selectByConfigKey(configKey);
        if (config == null) {
            return false;
        }

        // 如果配置需要加密，先加密
        if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            try {
                configValue = aesUtil.encrypt(configValue);
            } catch (Exception e) {
                throw new RuntimeException("配置加密失败: " + configKey, e);
            }
        }

        // 更新配置值
        int result = notificationConfigDao.updateConfigValue(config.getConfigId(), configValue);

        // 清除缓存
        String cacheKey = CACHE_KEY_PREFIX + configKey;
        cacheManager.evict(cacheKey);
        cacheManager.evict(CACHE_KEY_PREFIX + "type:" + config.getConfigType());

        return result > 0;
    }

    /**
     * 清除配置缓存
     * <p>
     * 清除指定配置的缓存，用于配置热更新
     * </p>
     *
     * @param configKey 配置键
     */
    public void evictCache(String configKey) {
        NotificationConfigEntity config = notificationConfigDao.selectByConfigKey(configKey);
        if (config != null) {
            String cacheKey = CACHE_KEY_PREFIX + configKey;
            cacheManager.evict(cacheKey);
            cacheManager.evict(CACHE_KEY_PREFIX + "type:" + config.getConfigType());
        }
    }
}
