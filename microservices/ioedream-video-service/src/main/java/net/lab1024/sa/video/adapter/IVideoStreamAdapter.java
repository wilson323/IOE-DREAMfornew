package net.lab1024.sa.video.adapter;

import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.video.domain.vo.VideoStream;

/**
 * 视频流适配器接口
 * <p>
 * 使用工厂模式实现不同厂商的视频流适配器
 * 严格遵循CLAUDE.md规范：
 * - 接口化设计，支持依赖倒置
 * - 支持动态加载适配器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IVideoStreamAdapter {

    /**
     * 获取厂商名称
     *
     * @return 厂商名称（如HIKVISION、DAHUA等）
     */
    String getVendorName();

    /**
     * 创建视频流
     * <p>
     * 根据设备信息创建对应的视频流URL
     * </p>
     *
     * @param device 设备实体
     * @return 视频流对象
     */
    VideoStream createStream(DeviceEntity device);

    /**
     * 停止视频流
     * <p>
     * 停止指定视频流的播放
     * </p>
     *
     * @param streamId 视频流ID
     */
    void stopStream(String streamId);

    /**
     * 获取适配器优先级
     * <p>
     * 用于工厂排序，优先级高的适配器优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }
}
