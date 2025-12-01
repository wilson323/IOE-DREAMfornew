package net.lab1024.sa.admin.module.smart.device.manager;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.device.dao.SmartDeviceDao;
import net.lab1024.sa.admin.module.smart.device.domain.entity.SmartDeviceEntity;
import net.lab1024.sa.base.common.manager.BaseCacheManager;

/**
 * 设备Manager - 缓存管理和业务协调
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Component
@Slf4j
public class SmartDeviceManager extends BaseCacheManager {

    @Resource
    private SmartDeviceDao smartDeviceDao;

    private static final String INFO_SUFFIX = ":info";
    private static final String LIST_SUFFIX = ":list";
    private static final String STATUS_SUFFIX = ":status";

    @Override
    protected String getCachePrefix() {
        return "device:";
    }

    /**
     * 获取设备信息(多级缓存)
     */
    public SmartDeviceEntity getDeviceInfo(Long deviceId) {
        if (deviceId == null) {
            return null;
        }

        String cacheKey = buildCacheKey(deviceId, INFO_SUFFIX);

        return getCache(cacheKey, () -> {
            SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
            if (entity == null || entity.getDeletedFlag() == 1) {
                return null;
            }
            return entity;
        });
    }

    /**
     * 清除设备缓存
     */
    public void clearDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        String infoCacheKey = buildCacheKey(deviceId, INFO_SUFFIX);
        String statusCacheKey = buildCacheKey(deviceId, STATUS_SUFFIX);

        removeCache(infoCacheKey);
        removeCache(statusCacheKey);

        log.info("设备缓存清除完成, deviceId: {}", deviceId);
    }

    /**
     * 批量清除设备缓存
     */
    public void clearAllDeviceCache() {
        String pattern = getCachePrefix() + "*";
        removeCacheByPattern(pattern);
        log.info("设备缓存批量清除完成");
    }

    /**
     * 预热设备缓存
     */
    public void warmupDeviceCache(Long deviceId) {
        if (deviceId == null) {
            return;
        }

        SmartDeviceEntity entity = smartDeviceDao.selectById(deviceId);
        if (entity != null && entity.getDeletedFlag() != 1) {
            String cacheKey = buildCacheKey(deviceId, INFO_SUFFIX);
            setCache(cacheKey, entity);
            log.info("设备缓存预热完成, deviceId: {}", deviceId);
        }
    }
}
