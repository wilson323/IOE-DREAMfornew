package net.lab1024.sa.video.adapter.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.common.gateway.domain.response.DeviceResponse;
import net.lab1024.sa.common.util.TypeUtils;
import net.lab1024.sa.video.adapter.IVideoStreamAdapter;
import net.lab1024.sa.video.domain.vo.VideoStream;

/**
 * RTMP视频流适配器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现RTMP协议的视频流适配器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class RTMPAdapter implements IVideoStreamAdapter {

    @Override
    public String getVendorName() {
        return "RTMP";
    }

    @Override
    public VideoStream createStream(DeviceResponse device) {
        // RTMP流URL构建
        String rtmpUrl = buildRTMPUrl(device);

        log.debug("[RTMP适配器] 创建RTMP视频流, deviceId={}, url={}", device.getDeviceId(), rtmpUrl);

        return VideoStream.builder()
                .deviceId(TypeUtils.toString(device.getDeviceId()))
                .streamUrl(rtmpUrl)
                .protocol("RTMP")
                .streamId("RTMP_" + TypeUtils.toString(device.getDeviceId()) + "_" + System.currentTimeMillis())
                .width(1920) // 默认分辨率
                .height(1080)
                .frameRate(25) // 默认帧率
                .bitrate(2048) // 默认码率
                .build();
    }

    @Override
    public void stopStream(String streamId) {
        log.debug("[RTMP适配器] 停止视频流, streamId={}", streamId);
        // TODO: 实现停止RTMP流的逻辑
    }

    @Override
    public int getPriority() {
        return 90; // RTMP适配器优先级中等
    }

    /**
     * 构建RTMP URL
     *
     * @param device 设备实体
     * @return RTMP URL
     */
    private String buildRTMPUrl(DeviceResponse device) {
        // TODO: 根据设备类型和配置构建RTMP URL
        // 示例：rtmp://ip:port/live/stream
        String ip = device.getIpAddress() != null ? device.getIpAddress() : "127.0.0.1";
        Integer port = device.getPort() != null ? device.getPort() : 1935;

        return String.format("rtmp://%s:%d/live/%s", ip, port, TypeUtils.toString(device.getDeviceId()));
    }
}
