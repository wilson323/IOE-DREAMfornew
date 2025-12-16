package net.lab1024.sa.common.notification.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;

import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.common.util.AESUtil;
import net.lab1024.sa.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知配置管理器
 * <p>
 * 负责通知配置的获取、解密等管理功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 保持为纯Java类，不依赖Spring框架
 * - Manager类不处理缓存，缓存逻辑在Service层使用@Cacheable注解
 * </p>
 * <p>
 * 企业级特性：
 * - 配置加密解密
 * - 配置验证
 * - 配置JSON解析
 * </p>
 * <p>
 * 缓存说明：
 * - 缓存逻辑已迁移到Service层，使用Spring Cache标准方案
 * - Service层使用@Cacheable/@CacheEvict注解管理缓存
 * - 参考 {@link net.lab1024.sa.common.notification.service.NotificationConfigService}
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-01-30 移除UnifiedCacheManager依赖，缓存逻辑迁移到Service层
 */
@Slf4j
public class NotificationConfigManager {

    /**
     * 配置DAO
     */
    private final NotificationConfigDao notificationConfigDao;

    /**
     * JSON对象映射器
     */
    private final ObjectMapper objectMapper;

    /**
     * AES加密工具
     */
    private final AESUtil aesUtil;

    /**
     * 构造函数
     * <p>
     * 通过构造函数注入依赖，保持为纯Java类
     * 已移除UnifiedCacheManager依赖，缓存逻辑在Service层处理
     * </p>
     *
     * @param notificationConfigDao 通知配置DAO
     * @param objectMapper          JSON对象映射器
     * @param aesUtil              AES加密工具
     */
    public NotificationConfigManager(
            NotificationConfigDao notificationConfigDao,
            ObjectMapper objectMapper,
            AESUtil aesUtil) {
        this.notificationConfigDao = notificationConfigDao;
        this.objectMapper = objectMapper;
        this.aesUtil = aesUtil;
    }

    /**
     * 根据配置键获取配置值
     * <p>
     * 从数据库查询配置，自动解密加密配置
     * 注意：缓存逻辑在Service层使用@Cacheable注解处理
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密）
     */
    public String getConfigValue(String configKey) {
        // 从数据库查询配置（缓存在Service层处理）
        NotificationConfigEntity config = notificationConfigDao.selectByConfigKey(configKey);

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
                log.error("[通知配置管理] 配置解密失败: configKey={}", configKey, e);
                throw new SystemException("CONFIG_DECRYPT_ERROR", "配置解密失败: " + configKey, e);
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
     * 注意：缓存逻辑在Service层使用@Cacheable注解处理
     * </p>
     *
     * @param configType 配置类型
     * @return 配置Map（key为configKey，value为configValue）
     */
    public Map<String, String> getConfigsByType(String configType) {
        // 从数据库查询配置（缓存在Service层处理）
        List<NotificationConfigEntity> configs = notificationConfigDao.selectByConfigTypeAndStatus(configType, 1);

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
                                    log.error("[通知配置管理] 配置解密失败: configKey={}", config.getConfigKey(), e);
                                    throw new SystemException("CONFIG_DECRYPT_ERROR", "配置解密失败: " + config.getConfigKey(), e);
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
    @SuppressWarnings("null")
    @Nullable
    public <T> T getConfigObject(String configKey, Class<T> clazz) {
        String configValue = getConfigValue(configKey);
        if (configValue == null) {
            return null;
        }

        try {
            return objectMapper.readValue(configValue, clazz);
        } catch (Exception e) {
            log.error("[通知配置管理] 配置JSON解析失败: configKey={}", configKey, e);
            throw new SystemException("CONFIG_JSON_PARSE_ERROR", "配置JSON解析失败: " + configKey, e);
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
    @SuppressWarnings("null")
    @Nullable
    public <T> T getConfigObject(String configKey, TypeReference<T> typeReference) {
        String configValue = getConfigValue(configKey);
        if (configValue == null) {
            return null;
        }

        try {
            return objectMapper.readValue(configValue, typeReference);
        } catch (Exception e) {
            log.error("[通知配置管理] 配置JSON解析失败: configKey={}", configKey, e);
            throw new SystemException("CONFIG_JSON_PARSE_ERROR", "配置JSON解析失败: " + configKey, e);
        }
    }

    /**
     * 更新配置值
     * <p>
     * 更新配置值
     * 注意：缓存清除逻辑在Service层使用@CacheEvict注解处理
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

        String valueToSave = configValue;

        // 如果配置需要加密，先加密
        if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            try {
                valueToSave = aesUtil.encrypt(configValue);
            } catch (Exception e) {
                log.error("[通知配置管理] 配置加密失败: configKey={}", configKey, e);
                throw new SystemException("CONFIG_ENCRYPT_ERROR", "配置加密失败: " + configKey, e);
            }
        }

        // 更新配置值（缓存清除在Service层处理）
        int result = notificationConfigDao.updateConfigValue(config.getId(), valueToSave);

        return result > 0;
    }

    /**
     * 获取配置实体（用于Service层缓存）
     * <p>
     * 此方法用于Service层获取配置实体，以便使用@Cacheable注解缓存
     * </p>
     *
     * @param configKey 配置键
     * @return 配置实体
     */
    public NotificationConfigEntity getConfigEntity(String configKey) {
        return notificationConfigDao.selectByConfigKey(configKey);
    }

    /**
     * 获取配置实体列表（用于Service层缓存）
     * <p>
     * 此方法用于Service层获取配置实体列表，以便使用@Cacheable注解缓存
     * </p>
     *
     * @param configType 配置类型
     * @return 配置实体列表
     */
    public List<NotificationConfigEntity> getConfigEntitiesByType(String configType) {
        return notificationConfigDao.selectByConfigTypeAndStatus(configType, 1);
    }
}
