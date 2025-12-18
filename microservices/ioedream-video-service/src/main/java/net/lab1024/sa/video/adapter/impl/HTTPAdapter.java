package net.lab1024.sa.video.adapter.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.adapter.IVideoStreamAdapter;
import net.lab1024.sa.video.domain.vo.VideoStream;
import org.springframework.stereotype.Component;

/**
 * HTTP视频流适配器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现HTTP协议的视频流适配器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component
public class HTTPAdapter implements IVideoStreamAdapter {

    @Override
    public String getVendorName() {
        return "HTTP";
    }

    @Override
    public VideoStream createStream(DeviceEntity device) {
        // HTTP流URL构建
        String httpUrl = buildHTTPUrl(device);

        log.debug("[HTTP适配器] 创建HTTP视频流, deviceId={}, url={}", device.getDeviceId(), httpUrl);

        return VideoStream.builder()
                .deviceId(device.getDeviceId())
                .streamUrl(httpUrl)
                .protocol("HTTP")
                .streamId("HTTP_" + device.getDeviceId() + "_" + System.currentTimeMillis())
                .width(1920) // 默认分辨率
                .height(1080)
                .frameRate(25) // 默认帧率
                .bitrate(2048) // 默认码率
                .build();
    }

    @Override
    public void stopStream(String streamId) {
        log.debug("[HTTP适配器] 停止视频流, streamId={}", streamId);
        // TODO: 实现停止HTTP流的逻辑
    }

    @Override
    public int getPriority() {
        return 80; // HTTP适配器优先级较低
    }

    /**
     * 构建HTTP URL
     *
     * @param device 设备实体
     * @return HTTP URL
     */
    private String buildHTTPUrl(DeviceEntity device) {
        // TODO: 根据设备类型和配置构建HTTP URL
        // 示例：http://ip:port/video/stream.m3u8
        String ip = device.getDeviceIp() != null ? device.getDeviceIp() : "127.0.0.1";
        Integer port = device.getDevicePort() != null ? device.getDevicePort() : 80;

        return String.format("http://%s:%d/video/%s/stream.m3u8", ip, port, device.getDeviceId());
    }
}
