package net.lab1024.sa.admin.module.smart.video.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频录像管理器接口
 * <p>
 * 提供定时录像、事件录像、存储管理等功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
public interface VideoRecordingManager {

    /**
     * 创建定时录像计划
     *
     * @param deviceId      设备ID
     * @param scheduleName  计划名称
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param weekDays      星期几（1-7，1为周一）
     * @param recordType    录像类型（CONTINUOUS、MOTION_DETECTION、ALARM）
     * @return 计划ID
     */
    String createScheduleRecord(Long deviceId, String scheduleName,
                               LocalDateTime startTime, LocalDateTime endTime,
                               List<Integer> weekDays, String recordType);

    /**
     * 启动定时录像
     *
     * @param scheduleId 计划ID
     * @return 是否启动成功
     */
    boolean startScheduleRecord(String scheduleId);

    /**
     * 停止定时录像
     *
     * @param scheduleId 计划ID
     * @return 是否停止成功
     */
    boolean stopScheduleRecord(String scheduleId);

    /**
     * 启动事件录像
     *
     * @param deviceId    设备ID
     * @param eventType   事件类型（MOTION、ALARM、MANUAL）
     * @param description 事件描述
     * @param duration    录像时长（秒）
     * @return 录像ID
     */
    String startEventRecord(Long deviceId, String eventType,
                           String description, Integer duration);

    /**
     * 停止事件录像
     *
     * @param recordId 录像ID
     * @return 是否停止成功
     */
    boolean stopEventRecord(String recordId);

    /**
     * 查询录像列表
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param recordType 录像类型
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 录像列表
     */
    Map<String, Object> queryRecords(Long deviceId, LocalDateTime startTime, LocalDateTime endTime,
                                    String recordType, Integer pageNum, Integer pageSize);

    /**
     * 删除录像文件
     *
     * @param recordId 录像ID
     * @return 是否删除成功
     */
    boolean deleteRecord(String recordId);

    /**
     * 批量删除录像文件
     *
     * @param recordIds 录像ID列表
     * @return 删除结果
     */
    Map<String, Boolean> batchDeleteRecords(List<String> recordIds);

    /**
     * 获取录像存储状态
     *
     * @return 存储状态信息
     */
    Map<String, Object> getStorageStatus();

    /**
     * 清理过期录像
     *
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    Map<String, Object> cleanupExpiredRecords(Integer retentionDays);

    /**
     * 设置存储策略
     *
     * @param strategy 策略配置
     * @return 是否设置成功
     */
    boolean setStorageStrategy(Map<String, Object> strategy);

    /**
     * 获取录像统计信息
     *
     * @param deviceId  设备ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计信息
     */
    Map<String, Object> getRecordingStatistics(Long deviceId,
                                              LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 备份录像文件
     *
     * @param recordId   录像ID
     * @param backupPath 备份路径
     * @return 备份任务ID
     */
    String backupRecord(String recordId, String backupPath);

    /**
     * 恢复录像文件
     *
     * @param backupId   备份ID
     * @param targetPath 目标路径
     * @return 是否恢复成功
     */
    boolean restoreRecord(String backupId, String targetPath);

    /**
     * 获取录像质量信息
     *
     * @param recordId 录像ID
     * @return 质量信息
     */
    Map<String, Object> getRecordQuality(String recordId);

    /**
     * 压缩录像文件
     *
     * @param recordId  录像ID
     * @param quality   目标质量（HIGH、MEDIUM、LOW）
     * @return 压缩任务ID
     */
    String compressRecord(String recordId, String quality);

    /**
     * 导出录像文件
     *
     * @param recordId    录像ID
     * @param exportFormat 导出格式（MP4、AVI、MOV）
     * @param exportPath  导出路径
     * @return 导出任务ID
     */
    String exportRecord(String recordId, String exportFormat, String exportPath);
}