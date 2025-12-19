package net.lab1024.sa.access.service;

/**
 * 门禁后台验证服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service层负责业务逻辑和事务管理
 * - 调用Manager层进行复杂流程编排
 * - 遵循四层架构：Controller → Service → Manager → DAO
 * </p>
 * <p>
 * 核心职责：
 * - 设备信息查询（通过序列号）
 * - 区域信息查询（通过设备ID）
 * - 协议响应构建
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessBackendAuthService {

    /**
     * 根据设备序列号获取设备ID
     * <p>
     * 实现逻辑：
     * 1. 从Redis缓存查询（L2缓存）
     * 2. 缓存未命中时，从数据库查询（使用DeviceDao）
     * 3. 查询结果写入缓存（TTL: 1小时）
     * </p>
     *
     * @param serialNumber 设备序列号
     * @return 设备ID（String类型），如果设备不存在则返回null
     */
    String getDeviceIdBySerialNumber(String serialNumber);

    /**
     * 根据设备ID获取区域ID
     * <p>
     * 实现逻辑：
     * 1. 从Redis缓存查询（L2缓存）
     * 2. 缓存未命中时，优先从DeviceEntity.areaId获取（如果设备实体有区域ID）
     * 3. 如果DeviceEntity没有区域ID，则从AreaDeviceDao查询设备-区域关联
     * 4. 查询结果写入缓存（TTL: 1小时）
     * </p>
     *
     * @param deviceId 设备ID（String类型）
     * @return 区域ID，如果查询失败则返回null
     */
    Long getAreaIdByDeviceId(String deviceId);
}
