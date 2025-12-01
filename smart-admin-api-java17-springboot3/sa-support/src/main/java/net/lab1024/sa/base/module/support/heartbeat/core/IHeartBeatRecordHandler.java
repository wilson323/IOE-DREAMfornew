package net.lab1024.sa.base.module.support.heartbeat.core;

/**
 * 心跳记录处理器接口
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
public interface IHeartBeatRecordHandler {

    /**
     * 处理心跳记录
     *
     * @param clientId 客户端ID
     * @param clientInfo 客户端信息
     */
    void handleHeartBeat(String clientId, String clientInfo);

    /**
     * 清理过期心跳记录
     */
    void cleanExpiredHeartBeats();

    /**
     * 获取当前活跃的客户端数量
     *
     * @return 活跃客户端数量
     */
    long getActiveClientCount();
}