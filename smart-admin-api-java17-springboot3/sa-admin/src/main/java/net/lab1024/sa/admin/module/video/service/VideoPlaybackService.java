package net.lab1024.sa.admin.module.video.service;

import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 视频回放服务接口（占位实现）
 */
public interface VideoPlaybackService {

    /**
     * 获取回放地址
     */
    ResponseDTO<String> getPlaybackUrl(Long deviceId, String startTime, String endTime);

    /**
     * 开始回放
     */
    ResponseDTO<Boolean> startPlayback(Long deviceId, String startTime);

    /**
     * 停止回放
     */
    ResponseDTO<Boolean> stopPlayback(Long deviceId);

    /**
     * 暂停回放
     */
    ResponseDTO<Boolean> pausePlayback(Long deviceId);

    /**
     * 恢复回放
     */
    ResponseDTO<Boolean> resumePlayback(Long deviceId);
}