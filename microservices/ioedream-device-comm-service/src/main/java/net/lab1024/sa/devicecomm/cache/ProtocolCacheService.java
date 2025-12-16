package net.lab1024.sa.devicecomm.cache;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 协议缓存服务接口
 * <p>
 * 使用Spring Cache注解（@Cacheable、@CacheEvict、@CachePut）替代ProtocolCacheManager
 * 严格遵循CLAUDE.md规范：
 * - 统一使用Spring Cache + Caffeine + Redis
 * - 禁止使用自定义CacheManager
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-01-30
 */
public interface ProtocolCacheService {

    /**
     * 根据设备ID获取设备信息（使用Spring Cache）
     *
     * @param deviceId 设备ID
     * @return 设备实体，如果不存在返回null
     */
    DeviceEntity getDeviceById(Long deviceId);

    /**
     * 根据设备编码获取设备信息（使用Spring Cache）
     *
     * @param deviceCode 设备编码（SN）
     * @return 设备实体，如果不存在返回null
     */
    DeviceEntity getDeviceByCode(String deviceCode);

    /**
     * 缓存设备信息（使用Spring Cache）
     *
     * @param device 设备实体
     */
    DeviceEntity cacheDevice(DeviceEntity device);

    /**
     * 根据卡号获取用户ID（使用Spring Cache）
     *
     * @param cardNumber 卡号
     * @return 用户ID，如果不存在返回null
     */
    Long getUserIdByCardNumber(String cardNumber);

    /**
     * 缓存卡号到用户ID的映射（使用Spring Cache）
     *
     * @param cardNumber 卡号
     * @param userId 用户ID
     */
    Long cacheUserCardMapping(String cardNumber, Long userId);

    /**
     * 清除设备缓存
     *
     * @param deviceId 设备ID
     */
    void evictDevice(Long deviceId);

    /**
     * 清除设备缓存（根据设备编码）
     *
     * @param deviceCode 设备编码
     */
    void evictDeviceByCode(String deviceCode);
}

