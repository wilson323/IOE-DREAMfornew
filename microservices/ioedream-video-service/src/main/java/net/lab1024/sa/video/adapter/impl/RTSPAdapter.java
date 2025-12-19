package net.lab1024.sa.video.adapter.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.adapter.IVideoStreamAdapter;
import net.lab1024.sa.video.domain.vo.VideoStream;
import org.springframework.stereotype.Component;

/**
 * RTSP视频流适配器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现RTSP协议的视频流适配器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class RTSPAdapter implements IVideoStreamAdapter {

    @Override
    public String getVendorName() {
        return "RTSP";
    }

    @Override
    public VideoStream createStream(DeviceEntity device) {
        // RTSP流URL构建
        String rtspUrl = buildRTSPUrl(device);

        log.debug("[RTSP适配器] 创建RTSP视频流, deviceId={}, url={}", device.getDeviceId(), rtspUrl);

        return VideoStream.builder()
                .deviceId(device.getDeviceId())
                .streamUrl(rtspUrl)
                .protocol("RTSP")
                .streamId("RTSP_" + device.getDeviceId() + "_" + System.currentTimeMillis())
                .width(1920) // 默认分辨率
                .height(1080)
                .frameRate(25) // 默认帧率
                .bitrate(2048) // 默认码率
                .build();
    }

    @Override
    public void stopStream(String streamId) {
        log.debug("[RTSP适配器] 停止视频流, streamId={}", streamId);
        // TODO: 实现停止RTSP流的逻辑
    }

    @Override
    public int getPriority() {
        return 100; // RTSP适配器优先级最高
    }

    /**
     * 构建RTSP URL
     *
     * @param device 设备实体
     * @return RTSP URL
     */
    private String buildRTSPUrl(DeviceEntity device) {
        // TODO: 根据设备类型和配置构建RTSP URL
        // 示例：rtsp://username:password@ip:port/Streaming/Channels/101
        // 注意：username和password应从extendedAttributes JSON字段中解析，此处使用默认值
        String username = "admin"; // TODO: 从device.getExtendedAttributes()解析
        String password = "admin"; // TODO: 从device.getExtendedAttributes()解析
        String ip = device.getIpAddress() != null ? device.getIpAddress() : "127.0.0.1";
        Integer port = device.getPort() != null ? device.getPort() : 554;

        return String.format("rtsp://%s:%s@%s:%d/Streaming/Channels/101", username, password, ip, port);
    }
}
