package net.lab1024.sa.common.notification.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import net.lab1024.sa.common.notification.service.NotificationConfigService;

/**
 * 通知配置服务实现类
 * <p>
 * 实现通知配置的CRUD操作
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识
 * - 使用@Resource依赖注入
 * - 事务管理(@Transactional)
 * - 调用Manager层进行业务处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class NotificationConfigServiceImpl implements NotificationConfigService {

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private NotificationConfigDao notificationConfigDao;

    /**
     * 根据配置键获取配置值
     * <p>
     * 自动解密加密配置
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密）
     */
    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey) {
        log.debug("[通知配置] 获取配置值，configKey：{}", configKey);
        return notificationConfigManager.getConfigValue(configKey);
    }

    /**
     * 根据配置键获取配置值（带默认值）
     *
     * @param configKey   配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey, String defaultValue) {
        log.debug("[通知配置] 获取配置值（带默认值），configKey：{}，defaultValue：{}", configKey, defaultValue);
        return notificationConfigManager.getConfigValue(configKey, defaultValue);
    }

    /**
     * 根据配置类型获取所有配置
     *
     * @param configType 配置类型
     * @return 配置Map（key为configKey，value为configValue）
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getConfigsByType(String configType) {
        log.debug("[通知配置] 获取配置列表（按类型），configType：{}", configType);
        return notificationConfigManager.getConfigsByType(configType);
    }

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    @Override
    public boolean updateConfigValue(String configKey, String configValue) {
        log.info("[通知配置] 更新配置值，configKey：{}", configKey);
        boolean result = notificationConfigManager.updateConfigValue(configKey, configValue);
        if (result) {
            log.info("[通知配置] 配置值更新成功，configKey：{}", configKey);
        } else {
            log.warn("[通知配置] 配置值更新失败，configKey：{}", configKey);
        }
        return result;
    }

    /**
     * 更新配置状态
     *
     * @param configKey 配置键
     * @param status    状态（1-启用 2-禁用）
     * @return 是否更新成功
     */
    @Override
    public boolean updateConfigStatus(String configKey, Integer status) {
        log.info("[通知配置] 更新配置状态，configKey：{}，status：{}", configKey, status);
        
        // 查询配置实体
        NotificationConfigEntity config = notificationConfigDao.selectByConfigKey(configKey);
        if (config == null) {
            log.warn("[通知配置] 配置不存在，configKey：{}", configKey);
            return false;
        }

        // 更新配置状态
        int result = notificationConfigDao.updateConfigStatus(config.getConfigId(), status);
        
        // 清除缓存
        notificationConfigManager.evictCache(configKey);
        
        return result > 0;
    }

    /**
     * 获取配置列表（按类型）
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationConfigVO> getConfigListByType(String configType) {
        log.debug("[通知配置] 获取配置列表（按类型），configType：{}", configType);
        
        // 查询配置列表
        List<NotificationConfigEntity> configs = notificationConfigDao.selectByConfigTypeAndStatus(configType, 1);
        
        // 转换为VO列表
        return configs.stream().map(config -> {
            NotificationConfigVO vo = new NotificationConfigVO();
            vo.setConfigId(config.getConfigId());
            vo.setConfigKey(config.getConfigKey());
            // 配置值脱敏处理（如果是加密配置，显示为****）
            if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
                vo.setConfigValue("****");
            } else {
                vo.setConfigValue(config.getConfigValue());
            }
            vo.setConfigType(config.getConfigType());
            vo.setConfigDesc(config.getConfigDesc());
            vo.setIsEncrypted(config.getIsEncrypted());
            vo.setStatus(config.getStatus());
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 清除配置缓存
     *
     * @param configKey 配置键
     */
    @Override
    public void evictCache(String configKey) {
        log.info("[通知配置] 清除配置缓存，configKey：{}", configKey);
        notificationConfigManager.evictCache(configKey);
    }
}
