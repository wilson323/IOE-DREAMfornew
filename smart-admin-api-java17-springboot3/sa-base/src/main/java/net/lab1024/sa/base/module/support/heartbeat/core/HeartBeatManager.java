package net.lab1024.sa.base.module.support.heartbeat.core;

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
    public void onBeat(long timestampMs) {
        // 默认实现：委托给具体的处理器
        if (handler != null) {
            handler.onBeat(timestampMs);
        }
    }
}
