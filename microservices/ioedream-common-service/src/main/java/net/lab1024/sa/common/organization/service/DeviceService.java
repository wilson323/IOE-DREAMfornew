package net.lab1024.sa.common.organization.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 设备管理服务接口
 * <p>
 * 提供设备状态更新等功能
 * 严格遵循CLAUDE.md规范:
 * - Service接口定义核心业务方法
 * - 实现类在service.impl包中
 * - 使用@Resource依赖注入
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface DeviceService {

    /**
     * 更新设备状态
     * <p>
     * 用于设备协议推送设备状态更新
     * </p>
     *
     * @param deviceId 设备ID
     * @param deviceStatus 设备状态（ONLINE/OFFLINE/MAINTAIN）
     * @param lastOnlineTime 最后在线时间（可选）
     * @return 是否更新成功
     */
    ResponseDTO<Boolean> updateDeviceStatus(Long deviceId, String deviceStatus, java.time.LocalDateTime lastOnlineTime);

    /**
     * 根据设备编码查询设备
     * <p>
     * 用于根据设备序列号（SN）或设备编码查找设备信息
     * </p>
     *
     * @param deviceCode 设备编码（设备序列号SN）
     * @return 设备实体，如果不存在则返回null
     */
    ResponseDTO<net.lab1024.sa.common.organization.entity.DeviceEntity> getDeviceByCode(String deviceCode);
}

