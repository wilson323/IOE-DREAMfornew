package net.lab1024.sa.admin.module.system.heartbeat.core;

/**
 * 心跳管理器占位实现。
 */
public class HeartBeatManager implements IHeartBeatRecordHandler {
    private final long intervalMs;
    private final IHeartBeatRecordHandler handler;

    public HeartBeatManager(long intervalMs, IHeartBeatRecordHandler handler) {
        this.intervalMs = intervalMs;
        this.handler = handler;
    }

    public void start() {
        // no-op placeholder
    }

    @Override
    public void handleHeartBeat(String clientId, String clientInfo) {
        // 占位实现
        if (handler != null) {
            handler.handleHeartBeat(clientId, clientInfo);
        }
    }

    @Override
    public void cleanExpiredHeartBeats() {
        // 占位实现
        if (handler != null) {
            handler.cleanExpiredHeartBeats();
        }
    }

    @Override
    public long getActiveClientCount() {
        // 占位实现
        if (handler != null) {
            return handler.getActiveClientCount();
        }
        return 0;
    }
}
