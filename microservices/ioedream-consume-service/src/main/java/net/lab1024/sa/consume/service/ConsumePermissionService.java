package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 消费权限服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumePermissionService {

    /**
     * 检查用户是否有消费权限
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @param amount   消费金额
     * @return 是否有权限
     */
    boolean hasConsumePermission(Long userId, Long deviceId, BigDecimal amount);

    /**
     * 获取用户可用的消费设备列表
     *
     * @param userId 用户ID
     * @return 设备ID列表
     */
    List<Long> getUserAvailableDevices(Long userId);

    /**
     * 检查设备是否允许消费
     *
     * @param deviceId 设备ID
     * @return 是否允许消费
     */
    boolean isDeviceAllowedForConsume(Long deviceId);
}
