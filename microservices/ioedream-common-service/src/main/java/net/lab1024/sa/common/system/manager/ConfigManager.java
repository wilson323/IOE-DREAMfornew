package net.lab1024.sa.common.system.manager;

import java.util.List;

import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;

/**
 * 配置管理器
 * <p>
 * 职责：
 * - 系统配置的CRUD操作
 * - 配置缓存管理
 * - 配置验证和默认值处理
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不直接依赖Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class ConfigManager {

    private final SystemConfigDao systemConfigDao;

    public ConfigManager(SystemConfigDao systemConfigDao) {
        this.systemConfigDao = systemConfigDao;
    }

    /**
     * 获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    public String getConfigValue(String configKey) {
        SystemConfigEntity config = systemConfigDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfigEntity>()
                        .eq(SystemConfigEntity::getConfigKey, configKey)
                        .eq(SystemConfigEntity::getDeletedFlag, 0)
        );
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置值（带默认值）
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 设置配置值
     *
     * @param configKey 配置键
     * @param configValue 配置值
     */
    public void setConfigValue(String configKey, String configValue) {
        SystemConfigEntity config = systemConfigDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfigEntity>()
                        .eq(SystemConfigEntity::getConfigKey, configKey)
                        .eq(SystemConfigEntity::getDeletedFlag, 0)
        );
        if (config == null) {
            config = new SystemConfigEntity();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            systemConfigDao.insert(config);
        } else {
            config.setConfigValue(configValue);
            systemConfigDao.updateById(config);
        }
    }

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    public List<SystemConfigEntity> getAllConfigs() {
        return systemConfigDao.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfigEntity>()
                        .eq(SystemConfigEntity::getDeletedFlag, 0)
        );
    }
}

