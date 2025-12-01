package net.lab1024.sa.admin.module.system.heartbeat.core;

/**
 * 心跳记录处理占位接口。
 */
public interface IHeartBeatRecordHandler {
    default void onBeat(long timestampMs) {}

    default void handleHeartBeat(String clientId, String clientInfo) {}

    default void cleanExpiredHeartBeats() {}

    default long getActiveClientCount() {
        return 0;
    }
}
