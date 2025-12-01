package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.video.domain.entity.VideoRecordEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频回放服务接口
 *
 * 提供录像查询、回放控制、下载等回放功能
 * 遵循repowiki架构设计规范: Manager层负责复杂业务逻辑和第三方集成
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface VideoPlaybackService {

    /**
     * 分页查询录像记录
     *
     * @param pageParam 分页参数
     * @param deviceId 设备ID(可选)
     * @param startTime 开始时间(可选)
     * @param endTime 结束时间(可选)
     * @param recordType 录像类型(可选)
     * @return 分页结果
     */
    PageResult<VideoRecordEntity> pageVideoRecords(PageParam pageParam, Long deviceId,
                                                  LocalDateTime startTime, LocalDateTime endTime,
                                                  String recordType);

    /**
     * 获取录像详情
     *
     * @param recordId 录像ID
     * @return 录像详情
     */
    VideoRecordEntity getVideoRecordDetail(Long recordId);

    /**
     * 获取录像回放地址
     *
     * @param recordId 录像ID
     * @param startTime 开始时间(可选,用于片段回放)
     * @param endTime 结束时间(可选,用于片段回放)
     * @return 回放地址
     */
    String getPlaybackUrl(Long recordId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取录像时间轴
     *
     * @param recordId 录像ID
     * @return 时间轴数据
     */
    Object getRecordTimeline(Long recordId);

    /**
     * 获取录像缩略图
     *
     * @param recordId 录像ID
     * @return 缩略图列表
     */
    List<Object> getRecordThumbnails(Long recordId);

    /**
     * 录像片段标记
     *
     * @param recordId 录像ID
     * @param markType 标记类型 (BOOKMARK/EVENT/ALERT)
     * @param markTime 标记时间
     * @param markDesc 标记描述
     * @return 标记结果
     */
    boolean markRecordSegment(Long recordId, String markType, LocalDateTime markTime, String markDesc);

    /**
     * 获取录像标记列表
     *
     * @param recordId 录像ID
     * @return 标记列表
     */
    List<Object> getRecordMarks(Long recordId);

    /**
     * 录像剪辑
     *
     * @param recordId 原始录像ID
     * @param startTime 剪辑开始时间
     * @param endTime 剪辑结束时间
     * @param clipName 剪辑名称
     * @return 剪辑后的录像ID
     */
    Long clipVideoRecord(Long recordId, LocalDateTime startTime, LocalDateTime endTime, String clipName);

    /**
     * 批量下载录像
     *
     * @param recordIds 录像ID列表
     * @param format 下载格式 (MP4/AVI/MOV)
     * @return 下载任务ID
     */
    String batchDownloadRecords(List<Long> recordIds, String format);

    /**
     * 获取下载状态
     *
     * @param downloadTaskId 下载任务ID
     * @return 下载状态信息
     */
    Object getDownloadStatus(String downloadTaskId);

    /**
     * 录像备份
     *
     * @param recordIds 录像ID列表
     * @param backupType 备份类型 (CLOUD/LOCAL)
     * @return 备份任务ID
     */
    String backupRecords(List<Long> recordIds, String backupType);

    /**
     * 获取备份状态
     *
     * @param backupTaskId 备份任务ID
     * @return 备份状态信息
     */
    Object getBackupStatus(String backupTaskId);

    /**
     * 录像导出
     *
     * @param recordIds 录像ID列表
     * @param exportType 导出类型 (REPORT/EVIDENCE/ARCHIVE)
     * @return 导出任务ID
     */
    String exportRecords(List<Long> recordIds, String exportType);

    /**
     * 获取导出状态
     *
     * @param exportTaskId 导出任务ID
     * @return 导出状态信息
     */
    Object getExportStatus(String exportTaskId);

    /**
     * 录像分享
     *
     * @param recordId 录像ID
     * @param shareType 分享类型 (LINK/TOKEN/EMBED)
     * @param expireTime 过期时间
     * @return 分享链接或令牌
     */
    String shareRecord(Long recordId, String shareType, LocalDateTime expireTime);

    /**
     * 取消录像分享
     *
     * @param shareId 分享ID
     * @return 取消结果
     */
    boolean cancelRecordShare(String shareId);

    /**
     * 获取录像统计分析
     *
     * @param deviceId 设备ID(可选)
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计分析数据
     */
    Object getRecordStatistics(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 录像存储空间查询
     *
     * @return 存储空间信息
     */
    Object getStorageSpaceInfo();
}