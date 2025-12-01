package net.lab1024.sa.admin.module.consume.service;

/**
 * 智能门禁控制服务接口
 * 门禁系统与消费系统的集成服务
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
public interface SmartAccessControlService {

    /**
     * 检查门禁权限
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 是否有权限
     */
    boolean hasAccessPermission(Long userId, Long deviceId);

    /**
     * 记录门禁事件
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @return 是否成功
     */
    boolean recordAccessEvent(Long userId, Long deviceId, String eventType);

    /**
     * 检查用户状态
     *
     * @param userId 用户ID
     * @return 用户状态
     */
    String checkAccountStatus(Long userId);

    /**
     * 同步用户权限
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean syncUserPermissions(Long userId);
}