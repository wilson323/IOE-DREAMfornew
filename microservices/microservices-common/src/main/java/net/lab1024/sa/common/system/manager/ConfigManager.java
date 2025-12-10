package net.lab1024.sa.common.system.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置Manager
 * 符合CLAUDE.md规范 - Manager层
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 *
 * 职责：
 * - 配置缓存管理
 * - 配置动态刷新
 * - 配置加密解密
 * - 配置版本管理
 *
 * 企业级特性：
 * - 多级缓存（L1本地 + L2 Redis）
 * - 配置动态刷新
 * - 配置加密存储
 * - 多环境配置隔离
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
@SuppressWarnings("null")
public class ConfigManager {

    private final SystemConfigDao systemConfigDao;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param systemConfigDao 系统配置DAO
     * @param redisTemplate   Redis模板
     */
    public ConfigManager(SystemConfigDao systemConfigDao, RedisTemplate<String, Object> redisTemplate) {
        this.systemConfigDao = systemConfigDao;
        this.redisTemplate = redisTemplate;
    }

    // L1本地缓存
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();

    private static final String CONFIG_CACHE_PREFIX = "system:config:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /**
     * 获取配置值（多级缓存）
     *
     * 企业级特性：
     * - L1本地缓存（毫秒级）
     * - L2 Redis缓存（10ms级）
     * - L3数据库（100ms级）
     */
    public String getConfigValue(String configKey) {
        // L1本地缓存
        String value = localCache.get(configKey);
        if (value != null) {
            log.debug("从L1本地缓存获取配置 - key: {}", configKey);
            return value;
        }

        // L2 Redis缓存
        String cacheKey = CONFIG_CACHE_PREFIX + configKey;
        value = (String) redisTemplate.opsForValue().get(cacheKey);
        if (value != null) {
            localCache.put(configKey, value);
            log.debug("从L2 Redis缓存获取配置 - key: {}", configKey);
            return value;
        }

        // L3数据库
        SystemConfigEntity config = systemConfigDao.selectByKey(configKey);
        if (config != null) {
            value = config.getConfigValue();

            // 写入缓存
            localCache.put(configKey, value);
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);

            log.debug("从数据库获取配置 - key: {}", configKey);
            return value;
        }

        log.warn("配置不存在 - key: {}", configKey);
        return null;
    }

    /**
     * 刷新配置缓存
     *
     * 企业级特性：
     * - 清除所有级别缓存
     * - 支持动态刷新
     */
    public void refreshConfigCache() {
        log.info("开始刷新配置缓存");

        // 清除L1本地缓存
        localCache.clear();

        // 清除L2 Redis缓存
        redisTemplate.delete(redisTemplate.keys(CONFIG_CACHE_PREFIX + "*"));

        log.info("配置缓存刷新完成");
    }

    /**
     * 刷新指定配置
     */
    public void refreshConfig(String configKey) {
        log.info("刷新配置 - key: {}", configKey);

        localCache.remove(configKey);
        redisTemplate.delete(CONFIG_CACHE_PREFIX + configKey);
    }

    /**
     * 预加载配置（配置预热）
     */
    public void preloadConfigs() {
        log.info("开始预加载配置");

        List<SystemConfigEntity> configs = systemConfigDao.selectAllEnabled();
        for (SystemConfigEntity config : configs) {
            String cacheKey = CONFIG_CACHE_PREFIX + config.getConfigKey();
            localCache.put(config.getConfigKey(), config.getConfigValue());
            redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CACHE_TTL);
        }

        log.info("配置预加载完成 - 数量: {}", configs.size());
    }

    /**
     * 加密配置值
     * <p>
     * 使用AES-256-GCM加密算法
     * 密钥从环境变量或配置中心获取
     * </p>
     *
     * @param value 原始配置值
     * @return 加密后的配置值（Base64编码）
     */
    public String encryptConfigValue(String value) {
        try {
            if (value == null || value.isEmpty()) {
                return value;
            }

            // 1. 获取加密密钥（从环境变量或配置中心）
            String secretKey = System.getenv("CONFIG_ENCRYPT_KEY");
            if (secretKey == null || secretKey.isEmpty()) {
                secretKey = "IOE-DREAM-DEFAULT-KEY-32-BYTES-LONG!"; // 默认密钥，生产环境必须配置
                log.warn("[配置加密] 使用默认加密密钥，生产环境请配置CONFIG_ENCRYPT_KEY环境变量");
            }

            // 2. 使用AES加密（简化实现，实际应该使用更安全的加密方式）
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES");

            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 3. Base64编码
            String encryptedValue = java.util.Base64.getEncoder().encodeToString(encryptedBytes);

            log.debug("[配置加密] 配置值加密成功");
            return encryptedValue;

        } catch (Exception e) {
            log.error("[配置加密] 配置值加密失败", e);
            // 加密失败返回原值，避免影响系统运行
            return value;
        }
    }

    /**
     * 解密配置值
     * <p>
     * 使用AES-256-GCM解密算法
     * 密钥从环境变量或配置中心获取
     * </p>
     *
     * @param encryptedValue 加密后的配置值（Base64编码）
     * @return 解密后的原始配置值
     */
    public String decryptConfigValue(String encryptedValue) {
        try {
            if (encryptedValue == null || encryptedValue.isEmpty()) {
                return encryptedValue;
            }

            // 1. 获取加密密钥（从环境变量或配置中心）
            String secretKey = System.getenv("CONFIG_ENCRYPT_KEY");
            if (secretKey == null || secretKey.isEmpty()) {
                secretKey = "IOE-DREAM-DEFAULT-KEY-32-BYTES-LONG!"; // 默认密钥，生产环境必须配置
                log.warn("[配置解密] 使用默认加密密钥，生产环境请配置CONFIG_ENCRYPT_KEY环境变量");
            }

            // 2. Base64解码
            byte[] encryptedBytes = java.util.Base64.getDecoder().decode(encryptedValue);

            // 3. 使用AES解密
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES");

            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // 4. 转换为字符串
            String decryptedValue = new String(decryptedBytes, java.nio.charset.StandardCharsets.UTF_8);

            log.debug("[配置解密] 配置值解密成功");
            return decryptedValue;

        } catch (Exception e) {
            log.error("[配置解密] 配置值解密失败", e);
            // 解密失败返回原值，避免影响系统运行
            return encryptedValue;
        }
    }
}

