package net.lab1024.sa.admin.module.video.service;

import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.admin.module.video.domain.entity.MonitorEventEntity;
import net.lab1024.sa.admin.module.video.domain.entity.VideoDeviceEntity;

import java.util.List;

/**
 * 视频监控服务接口（占位实现）
 */
public interface VideoSurveillanceService {

    /**
     * 获取实时视频流
     */
    ResponseDTO<String> getLiveStreamUrl(Long deviceId);

    /**
     * 开始实时监控
     */
    ResponseDTO<Boolean> startLiveMonitor(Long deviceId);

    /**
     * 停止实时监控
     */
    ResponseDTO<Boolean> stopLiveMonitor(Long deviceId);

    /**
     * 分页查询监控事件
     */
    PageResult<MonitorEventEntity> pageMonitorEvents(PageParam pageParam, Long deviceId, String eventType, String eventLevel, String startTime, String endTime, Integer isHandled);

    /**
     * 获取监控事件
     */
    MonitorEventEntity getMonitorEvent(Long eventId);

    /**
     * 处理监控事件
     */
    ResponseDTO<Boolean> handleMonitorEvent(Long eventId, String handleResult);

    /**
     * 分页查询视频设备
     */
    PageResult<VideoDeviceEntity> pageVideoDevices(PageParam pageParam, String deviceName, String deviceType, String deviceStatus, Long areaId);
}