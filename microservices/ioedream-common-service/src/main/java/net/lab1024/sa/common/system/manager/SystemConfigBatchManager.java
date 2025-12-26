package net.lab1024.sa.common.system.manager;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;

/**
 * 系统配置批量管理器
 * <p>
 * 职责：
 * - 批量配置操作
 * - 配置导入导出
 * - 配置批量更新
 * </p>
 * <p>
 * 注意：Manager类是纯Java类，不直接依赖Spring注解，通过构造函数注入依赖。
 * 在微服务中通过配置类将Manager注册为Spring Bean。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
public class SystemConfigBatchManager {

    private final SystemConfigDao systemConfigDao;

    public SystemConfigBatchManager(SystemConfigDao systemConfigDao) {
        this.systemConfigDao = systemConfigDao;
    }

    /**
     * 批量更新配置
     *
     * @param configs 配置Map（key为configKey，value为configValue）
     */
    public void batchUpdateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfigEntity config = systemConfigDao.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfigEntity>()
                            .eq(SystemConfigEntity::getConfigKey, entry.getKey())
                            .eq(SystemConfigEntity::getDeletedFlag, 0));
            if (config == null) {
                config = new SystemConfigEntity();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                systemConfigDao.insert(config);
            } else {
                config.setConfigValue(entry.getValue());
                systemConfigDao.updateById(config);
            }
        }
    }

    /**
     * 批量删除配置
     *
     * @param configKeys 配置键列表
     */
    public void batchDeleteConfigs(List<String> configKeys) {
        for (String configKey : configKeys) {
            SystemConfigEntity config = systemConfigDao.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SystemConfigEntity>()
                            .eq(SystemConfigEntity::getConfigKey, configKey)
                            .eq(SystemConfigEntity::getDeletedFlag, 0));
            if (config != null) {
                config.setDeletedFlag(1);
                systemConfigDao.updateById(config);
            }
        }
    }
}
