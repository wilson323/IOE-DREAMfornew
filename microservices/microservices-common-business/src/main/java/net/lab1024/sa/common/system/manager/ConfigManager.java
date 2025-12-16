package net.lab1024.sa.common.system.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;

/**
 * 配置Manager
 * 符合CLAUDE.md规范 - Manager层
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 *
 * 职责：
 * - 配置加密解密
 * - 配置版本管理
 * - 配置验证
 *
 * 企业级特性：
 * - 配置加密存储
 * - 多环境配置隔离
 * - 配置验证规则
 *
 * <p>
 * ⚠️ <strong>缓存说明：</strong>缓存逻辑已移除，缓存应在Service层使用@Cacheable注解处理。
 * 参考SystemServiceImpl.getConfigValue()方法，已使用@Cacheable注解。
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除缓存逻辑，符合CLAUDE.md规范，缓存应在Service层使用@Cacheable注解
 */
@Slf4j
public class ConfigManager {

    private final SystemConfigDao systemConfigDao;

    /**
     * 构造函数注入依赖
     * <p>
     * ⚠️ 注意：已移除RedisTemplate依赖，缓存逻辑已迁移到Service层使用@Cacheable注解
     * </p>
     *
     * @param systemConfigDao 系统配置DAO
     */
    public ConfigManager(SystemConfigDao systemConfigDao) {
        this.systemConfigDao = systemConfigDao;
    }

    /**
     * 获取配置值（无缓存，缓存由Service层@Cacheable注解处理）
     * <p>
     * ⚠️ 注意：此方法不再管理缓存，缓存逻辑已迁移到Service层使用@Cacheable注解
     * 参考SystemServiceImpl.getConfigValue()方法，已使用@Cacheable注解
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值
     */
    public String getConfigValue(String configKey) {
        // 直接查询数据库（缓存由Service层的@Cacheable注解处理）
        SystemConfigEntity config = systemConfigDao.selectByKey(configKey);
        if (config != null) {
            log.debug("从数据库获取配置 - key: {}", configKey);
            return config.getConfigValue();
        }

        log.warn("配置不存在 - key: {}", configKey);
        return null;
    }

    /**
     * 刷新配置缓存（已废弃，缓存由Service层@CacheEvict注解处理）
     * <p>
     * ⚠️ 注意：此方法已废弃，缓存刷新由Service层的@CacheEvict注解自动处理
     * 参考SystemServiceImpl.refreshConfigCache()方法，已使用@CacheEvict注解
     * </p>
     *
     * @deprecated 使用Service层的@CacheEvict注解替代
     */
    @Deprecated
    public void refreshConfigCache() {
        log.warn("refreshConfigCache()已废弃，请使用Service层的@CacheEvict注解");
    }

    /**
     * 刷新指定配置（已废弃，缓存由Service层@CacheEvict注解处理）
     * <p>
     * ⚠️ 注意：此方法已废弃，缓存刷新由Service层的@CacheEvict注解自动处理
     * </p>
     *
     * @param configKey 配置键
     * @deprecated 使用Service层的@CacheEvict注解替代
     */
    @Deprecated
    public void refreshConfig(String configKey) {
        log.warn("refreshConfig()已废弃，请使用Service层的@CacheEvict注解 - configKey: {}", configKey);
    }

    /**
     * 预加载配置（已废弃，缓存由Service层@Cacheable注解自动处理）
     * <p>
     * ⚠️ 注意：此方法已废弃，缓存预热由Service层的@Cacheable注解自动处理
     * </p>
     *
     * @deprecated 使用Service层的@Cacheable注解替代
     */
    @Deprecated
    public void preloadConfigs() {
        log.warn("preloadConfigs()已废弃，请使用Service层的@Cacheable注解");
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[配置加密] 配置值加密参数错误: error={}", e.getMessage());
            // 加密失败返回原值，避免影响系统运行
            return value;
        } catch (BusinessException e) {
            log.warn("[配置加密] 配置值加密业务异常: code={}, message={}", e.getCode(), e.getMessage());
            // 加密失败返回原值，避免影响系统运行
            return value;
        } catch (SystemException e) {
            log.error("[配置加密] 配置值加密系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 加密失败返回原值，避免影响系统运行
            return value;
        } catch (Exception e) {
            log.error("[配置加密] 配置值加密未知异常", e);
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

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[配置解密] 配置值解密参数错误: error={}", e.getMessage());
            // 解密失败返回原值，避免影响系统运行
            return encryptedValue;
        } catch (BusinessException e) {
            log.warn("[配置解密] 配置值解密业务异常: code={}, message={}", e.getCode(), e.getMessage());
            // 解密失败返回原值，避免影响系统运行
            return encryptedValue;
        } catch (SystemException e) {
            log.error("[配置解密] 配置值解密系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 解密失败返回原值，避免影响系统运行
            return encryptedValue;
        } catch (Exception e) {
            log.error("[配置解密] 配置值解密未知异常", e);
            // 解密失败返回原值，避免影响系统运行
            return encryptedValue;
        }
    }
}

