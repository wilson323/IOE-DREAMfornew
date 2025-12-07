package net.lab1024.sa.video.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.video.domain.vo.VideoDeviceVO;

/**
 * 视频播放服务接口
 * <p>
 * 提供视频播放相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-video-service中
 * - 提供统一的业务接口
 * - 支持视频流和截图功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VideoPlayService {

    /**
     * 获取视频流地址
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @param streamType 流类型（可选）
     * @return 视频流信息
     */
    Map<String, Object> getVideoStream(Long deviceId, Long channelId, String streamType);

    /**
     * 获取视频截图
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @return 截图信息
     */
    Map<String, Object> getSnapshot(Long deviceId, Long channelId);

    /**
     * 获取移动端设备列表
     *
     * @param areaId 区域ID（可选）
     * @param deviceType 设备类型（可选）
     * @param status 设备状态（可选）
     * @return 设备列表
     */
    List<VideoDeviceVO> getMobileDeviceList(String areaId, String deviceType, Integer status);
}

