package net.lab1024.sa.consume.manager;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 消费设备管理Manager接口
 * <p>
 * 用于设备相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中不使用Spring注解
 * - Manager类通过构造函数注入依赖
 * - 保持为纯Java类
 * </p>
 * <p>
 * 业务场景：
 * - 设备信息查询
 * - 设备状态管理
 * - 设备权限验证
 * - 设备消费模式配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeDeviceManager {

    /**
     * 根据设备ID获取设备信息
     * <p>
     * 类型安全改进：返回具体类型DeviceEntity，而非Object
     * 提升代码可读性和类型安全性
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备信息，如果不存在则返回null
     */
    DeviceEntity getDeviceById(String deviceId);

    /**
     * 验证设备是否在线
     *
     * @param deviceId 设备ID
     * @return 是否在线
     */
    boolean isDeviceOnline(String deviceId);

    /**
     * 验证设备是否支持指定的消费模式
     *
     * @param deviceId 设备ID
     * @param consumeMode 消费模式（FIXED/AMOUNT/PRODUCT/COUNT）
     * @return 是否支持
     */
    boolean isConsumeModeSupported(String deviceId, String consumeMode);

    /**
     * 根据设备ID获取消费设备信息
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    net.lab1024.sa.common.organization.entity.DeviceEntity getConsumeDeviceById(Long deviceId);

    /**
     * 获取消费设备列表
     *
     * @param areaId 区域ID
     * @param status 设备状态（可选）
     * @return 设备列表
     */
    java.util.List<net.lab1024.sa.common.organization.entity.DeviceEntity> getConsumeDevices(String areaId, Integer status);
}




