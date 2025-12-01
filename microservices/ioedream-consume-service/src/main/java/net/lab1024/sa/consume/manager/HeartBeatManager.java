package net.lab1024.sa.consume.manager;

import java.util.Map;

/**
 * 心跳管理器
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface HeartBeatManager {

    /**
     * 向用户设备广播消息
     *
     * @param userId       用户ID
     * @param notification 通知消息
     */
    void broadcastToUserDevices(Long userId, Map<String, Object> notification);
}