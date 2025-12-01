package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.video.domain.entity.VideoRecordEntity;
import net.lab1024.sa.video.domain.entity.MonitorEventEntity;

import java.util.List;

/**
 * 视频监控服务接口
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface VideoSurveillanceService {

    /**
     * 分页查询视频设备
     *
     * @param pageParam    分页参数
     * @param deviceName  设备名称
     * @param deviceType  设备类型
     * @param deviceStatus 设备状态
     * @param areaId      区域ID
     * @return 分页结果
     */
    PageResult<VideoDeviceEntity> pageVideoDevices(PageParam pageParam, String deviceName, String deviceType, String deviceStatus, Long areaId);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    VideoDeviceEntity getVideoDevice(Long deviceId);

    /**
     * 添加视频设备
     *
     * @param device 设备信息
     * @return 添加结果
     */
    boolean addVideoDevice(VideoDeviceEntity device);

    /**
     * 更新视频设备
     *
     * @param device 设备信息
     * @return 更新结果
     */
    boolean updateVideoDevice(VideoDeviceEntity device);

    /**
     * 删除视频设备
     *
     * @param deviceId 设备ID
     * @return 删除结果
     */
    boolean deleteVideoDevice(Long deviceId);

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    VideoDeviceEntity getDeviceStatus(Long deviceId);

    /**
     * 批量获取设备状态
     *
     * @param deviceIds 设备ID列表
     * @return 设备状态列表
     */
    List<VideoDeviceEntity> getDeviceStatusBatch(List<Long> deviceIds);

    /**
     * 分页查询录像记录
     *
     * @param pageParam     分页参数
     * @param deviceId     设备ID
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param recordType   录像类型
     * @return 分页结果
     */
    PageResult<VideoRecordEntity> pageVideoRecords(PageParam pageParam, Long deviceId, String startTime, String endTime, String recordType);

    /**
     * 获取录像详情
     *
     * @param recordId 录像ID
     * @return 录像信息
     */
    VideoRecordEntity getVideoRecord(Long recordId);

    /**
     * 删除录像记录
     *
     * @param recordId 录像ID
     * @return 删除结果
     */
    boolean deleteVideoRecord(Long recordId);

    /**
     * 批量删除录像记录
     *
     * @param recordIds 录像ID列表
     * @return 删除结果
     */
    boolean deleteVideoRecordBatch(List<Long> recordIds);

    /**
     * 分页查询监控事件
     *
     * @param pageParam    分页参数
     * @param deviceId    设备ID
     * @param eventType   事件类型
     * @param eventLevel  事件级别
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param isHandled    是否已处理
     * @return 分页结果
     */
    PageResult<MonitorEventEntity> pageMonitorEvents(PageParam pageParam, Long deviceId, String eventType, String eventLevel, String startTime, String endTime, Integer isHandled);

    /**
     * 获取事件详情
     *
     * @param eventId 事件ID
     * @return 事件信息
     */
    MonitorEventEntity getMonitorEvent(Long eventId);

    /**
     * 处理监控事件
     *
     * @param eventId     事件ID
     * @param handleComment 处理意见
     * @return 处理结果
     */
    boolean handleMonitorEvent(Long eventId, String handleComment);

    /**
     * 批量处理监控事件
     *
     * @param eventIds 事件ID列表
     * @param handleComment 处理意见
     * @return 处理结果
     */
    boolean handleMonitorEventBatch(List<Long> eventIds, String handleComment);

    /**
     * 获取实时视频流地址
     *
     * @param deviceId 设备ID
     * @return 流媒体地址
     */
    String getLiveStreamUrl(Long deviceId);

    /**
     * 云台控制
     *
     * @param deviceId 设备ID
     * @param action   控制动作 (UP-上, DOWN-下, LEFT-左, RIGHT-右, ZOOM_IN-放大, ZOOM_OUT-缩小)
     * @param speed    速度(1-7)
     * @return 控制结果
     */
    boolean ptzControl(Long deviceId, String action, Integer speed);

    /**
     * 获取设备预览截图
     *
     * @param deviceId 设备ID
     * @return 截图路径
     */
    String getDeviceSnapshot(Long deviceId);

    /**
     * 开始录像
     *
     * @param deviceId 设备ID
     * @return 录像ID
     */
    Long startRecording(Long deviceId);

    /**
     * 停止录像
     *
     * @param recordId 录像ID
     * @return 停止结果
     */
    boolean stopRecording(Long recordId);

    /**
     * 获取录像统计信息
     *
     * @param deviceId 设备ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    Object getRecordingStats(Long deviceId, String startDate, String endDate);
}