package net.lab1024.sa.admin.module.video.service;

import net.lab1024.sa.admin.module.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.PageParam;

import java.util.List;

/**
 * 视频设备服务接口（占位实现）
 */
public interface VideoDeviceService {

    /**
     * 分页查询视频设备
     */
    PageResult<VideoDeviceEntity> pageVideoDevices(PageParam pageParam, String deviceName, String deviceType, String deviceStatus, Long areaId);

    /**
     * 根据ID获取视频设备
     */
    VideoDeviceEntity getVideoDevice(Long deviceId);

    /**
     * 添加视频设备
     */
    boolean addVideoDevice(VideoDeviceEntity device);

    /**
     * 更新视频设备
     */
    boolean updateVideoDevice(VideoDeviceEntity device);

    /**
     * 删除视频设备
     */
    boolean deleteVideoDevice(Long deviceId);

    /**
     * 获取设备状态
     */
    VideoDeviceEntity getDeviceStatus(Long deviceId);

    /**
     * 批量获取设备状态
     */
    List<VideoDeviceEntity> getDeviceStatusBatch(List<Long> deviceIds);
}