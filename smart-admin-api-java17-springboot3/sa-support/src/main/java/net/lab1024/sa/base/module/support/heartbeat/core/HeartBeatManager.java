package net.lab1024.sa.base.module.support.heartbeat.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 心跳管理器
 *
 * @Author 1024创新实验室-主任: 卓大
 * @Date 2023-10-01 00:00:00
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href"https://1024lab.net">1024创新实验室</a>
 */
@Slf4j
public class HeartBeatManager {

    /**
     * 心跳间隔时间（毫秒）
     */
    private final long intervalMs;

    /**
     * 心跳记录处理器
     */
    private final IHeartBeatRecordHandler heartBeatRecordHandler;

    /**
     * 最后心跳时间
     */
    private volatile long lastHeartBeatTime = 0L;

    public HeartBeatManager(long intervalMs, IHeartBeatRecordHandler heartBeatRecordHandler) {
        this.intervalMs = intervalMs;
        this.heartBeatRecordHandler = heartBeatRecordHandler;
    }

    /**
     * 记录心跳
     *
     * @param clientId 客户端ID
     * @param clientInfo 客户端信息
     */
    public void recordHeartBeat(String clientId, String clientInfo) {
        long currentTime = System.currentTimeMillis();

        // 如果距离上次心跳时间太短，则忽略
        if (currentTime - lastHeartBeatTime < intervalMs / 2) {
            return;
        }

        try {
            heartBeatRecordHandler.handleHeartBeat(clientId, clientInfo);
            lastHeartBeatTime = currentTime;
            log.debug("Heart beat recorded for client: {}", clientId);
        } catch (Exception e) {
            log.error("Failed to record heart beat for client: " + clientId, e);
        }
    }

    /**
     * 定时清理过期心跳记录
     */
    @Scheduled(fixedDelay = 300000) // 5分钟执行一次
    public void cleanExpiredHeartBeats() {
        try {
            heartBeatRecordHandler.cleanExpiredHeartBeats();
            log.debug("Cleaned expired heart beat records");
        } catch (Exception e) {
            log.error("Failed to clean expired heart beat records", e);
        }
    }

    /**
     * 获取活跃客户端数量
     *
     * @return 活跃客户端数量
     */
    public long getActiveClientCount() {
        try {
            return heartBeatRecordHandler.getActiveClientCount();
        } catch (Exception e) {
            log.error("Failed to get active client count", e);
            return 0;
        }
    }

    /**
     * 获取心跳间隔时间
     *
     * @return 心跳间隔时间（毫秒）
     */
    public long getIntervalMs() {
        return intervalMs;
    }
}