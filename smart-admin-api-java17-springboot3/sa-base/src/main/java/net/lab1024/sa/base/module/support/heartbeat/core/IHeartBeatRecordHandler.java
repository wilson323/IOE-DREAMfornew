package net.lab1024.sa.base.module.support.heartbeat.core;

/**
 * 心跳记录处理占位接口。
 */
public interface IHeartBeatRecordHandler {
    default void onBeat(long timestampMs) {}
}
