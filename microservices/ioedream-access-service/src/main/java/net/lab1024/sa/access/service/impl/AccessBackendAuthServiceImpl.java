package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.AccessCacheConstants;
import net.lab1024.sa.access.service.AccessBackendAuthService;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 门禁后台验证服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessBackendAuthServiceImpl implements AccessBackendAuthService {

    /**
     * 缓存键前缀和过期时间统一使用AccessCacheConstants
     */

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private AreaDeviceDao areaDeviceDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public String getDeviceIdBySerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            log.warn("[后台验证服务] 设备序列号为空");
            return null;
        }

        try {
            // 1. 从Redis缓存查询
            String cacheKey = AccessCacheConstants.buildDeviceSnKey(serialNumber);
            Object cachedDeviceId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedDeviceId != null) {
                log.debug("[后台验证服务] 从缓存获取设备ID: SN={}, deviceId={}", serialNumber, cachedDeviceId);
                return cachedDeviceId.toString();
            }

            // 2. 从数据库查询（使用DeviceDao直接查询，避免跨服务调用）
            DeviceEntity device = deviceDao.selectBySerialNumber(serialNumber);
            if (device == null) {
                // 尝试通过设备编码查询（某些设备序列号可能等于设备编码）
                device = deviceDao.selectByDeviceCode(serialNumber);
            }

            if (device == null || device.getDeviceId() == null) {
                log.warn("[后台验证服务] 设备不存在: SN={}", serialNumber);
                return null;
            }

            String deviceId = device.getDeviceId();

            // 3. 写入缓存（TTL: 1小时）
            redisTemplate.opsForValue().set(cacheKey, deviceId, AccessCacheConstants.CACHE_EXPIRE_DEVICE);
            log.debug("[后台验证服务] 设备ID已缓存: SN={}, deviceId={}", serialNumber, deviceId);

            return deviceId;

        } catch (Exception e) {
            log.error("[后台验证服务] 获取设备ID失败: SN={}, error={}", serialNumber, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAreaIdByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            log.warn("[后台验证服务] 设备ID为空");
            return null;
        }

        try {
            // 1. 从Redis缓存查询
            String cacheKey = AccessCacheConstants.buildDeviceAreaKey(deviceId);
            Object cachedAreaId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedAreaId != null) {
                log.debug("[后台验证服务] 从缓存获取区域ID: deviceId={}, areaId={}", deviceId, cachedAreaId);
                return parseAreaId(cachedAreaId);
            }

            // 2. 优先从DeviceEntity获取区域ID（如果设备实体有区域ID字段）
            DeviceEntity device = deviceDao.selectById(deviceId);
            if (device != null && device.getAreaId() != null) {
                Long areaId = device.getAreaId();
                // 写入缓存
                redisTemplate.opsForValue().set(cacheKey, areaId, AccessCacheConstants.CACHE_EXPIRE_DEVICE);
                log.debug("[后台验证服务] 从DeviceEntity获取区域ID: deviceId={}, areaId={}", deviceId, areaId);
                return areaId;
            }

            // 3. 从AreaDeviceDao查询设备-区域关联
            List<AreaDeviceEntity> areaDevices = areaDeviceDao.selectByDeviceId(deviceId);
            
            if (areaDevices == null || areaDevices.isEmpty()) {
                log.warn("[后台验证服务] 设备未关联区域: deviceId={}", deviceId);
                return null;
            }

            // 4. 获取第一个关联区域的区域ID（优先获取主设备关联的区域）
            AreaDeviceEntity areaDevice = areaDevices.stream()
                    .filter(ad -> ad.getPriority() != null && ad.getPriority() == 1) // 优先获取主设备
                    .findFirst()
                    .orElse(areaDevices.get(0)); // 如果没有主设备，使用第一个

            Long areaId = areaDevice.getAreaId();
            if (areaId == null) {
                log.warn("[后台验证服务] 区域ID为空: deviceId={}", deviceId);
                return null;
            }

            // 5. 写入缓存（TTL: 1小时）
            redisTemplate.opsForValue().set(cacheKey, areaId, AccessCacheConstants.CACHE_EXPIRE_DEVICE);
            log.debug("[后台验证服务] 区域ID已缓存: deviceId={}, areaId={}", deviceId, areaId);

            return areaId;

        } catch (Exception e) {
            log.error("[后台验证服务] 获取区域ID失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解析区域ID
     *
     * @param areaIdObj 区域ID对象
     * @return 区域ID
     */
    private Long parseAreaId(Object areaIdObj) {
        if (areaIdObj == null) {
            return null;
        }
        if (areaIdObj instanceof Long) {
            return (Long) areaIdObj;
        }
        if (areaIdObj instanceof Number) {
            return ((Number) areaIdObj).longValue();
        }
        try {
            return Long.parseLong(areaIdObj.toString());
        } catch (NumberFormatException e) {
            log.warn("[后台验证服务] 区域ID解析失败: areaIdObj={}", areaIdObj);
            return null;
        }
    }
}
