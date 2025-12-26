package net.lab1024.sa.video.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoRecordingQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingSearchForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingDetailVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlaybackVO;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 录像回放服务接口
 * <p>
 * 提供录像文件管理、回放控制、下载等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用Service后缀命名
 * - 定义完整的业务方法
 * - 支持录像全生命周期管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface VideoRecordingService {

    /**
     * 分页查询录像列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<VideoRecordingVO>> queryRecordings(@Valid VideoRecordingQueryForm queryForm);

    /**
     * 搜索录像文件
     *
     * @param searchForm 搜索条件
     * @return 搜索结果
     */
    ResponseDTO<List<VideoRecordingVO>> searchRecordings(@Valid VideoRecordingSearchForm searchForm);

    /**
     * 获取录像详情
     *
     * @param recordingId 录像ID
     * @return 录像详情
     */
    ResponseDTO<VideoRecordingDetailVO> getRecordingDetail(Long recordingId);

    /**
     * 获取录像播放地址
     *
     * @param recordingId 录像ID
     * @return 播放地址和元数据
     */
    ResponseDTO<VideoRecordingPlaybackVO> getRecordingPlaybackUrl(Long recordingId);

    /**
     * 获取录像时间轴
     *
     * @param deviceId    设备ID
     * @param channelId   通道ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 时间轴数据
     */
    ResponseDTO<Map<String, Object>> getRecordingTimeline(
            Long deviceId,
            Long channelId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 按时间范围查询录像
     *
     * @param deviceId    设备ID
     * @param channelId   通道ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 录像列表
     */
    ResponseDTO<List<VideoRecordingVO>> getRecordingsByTimeRange(
            Long deviceId,
            Long channelId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * 删除录像文件
     *
     * @param recordingId 录像ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteRecording(Long recordingId);

    /**
     * 批量删除录像文件
     *
     * @param recordingIds 录像ID列表
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteRecordings(List<Long> recordingIds);

    /**
     * 下载录像文件
     *
     * @param recordingId 录像ID
     * @return 下载信息
     */
    ResponseDTO<Map<String, Object>> downloadRecording(Long recordingId);

    /**
     * 获取录像统计信息
     *
     * @param deviceId 设备ID（可选）
     * @return 统计信息
     */
    ResponseDTO<Map<String, Object>> getRecordingStatistics(Long deviceId);

    /**
     * 检查录像文件完整性
     *
     * @param recordingId 录像ID
     * @return 检查结果
     */
    ResponseDTO<Map<String, Object>> checkRecordingIntegrity(Long recordingId);

    /**
     * 修复录像文件
     *
     * @param recordingId 录像ID
     * @return 修复结果
     */
    ResponseDTO<Map<String, Object>> repairRecording(Long recordingId);

    /**
     * 获取设备的录像列表
     *
     * @param deviceId 设备ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    ResponseDTO<PageResult<VideoRecordingVO>> getDeviceRecordings(
            Long deviceId,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 获取通道的录像列表
     *
     * @param deviceId  设备ID
     * @param channelId 通道ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    ResponseDTO<PageResult<VideoRecordingVO>> getChannelRecordings(
            Long deviceId,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 获取重要事件录像
     *
     * @param eventType 事件类型
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    ResponseDTO<PageResult<VideoRecordingVO>> getEventRecordings(
            String eventType,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 标记录像为重要
     *
     * @param recordingId 录像ID
     * @param remark      备注
     * @return 操作结果
     */
    ResponseDTO<Void> markRecordingAsimportant(Long recordingId, String remark);

    /**
     * 取消录像重要标记
     *
     * @param recordingId 录像ID
     * @return 操作结果
     */
    ResponseDTO<Void> unmarkRecordingAsimportant(Long recordingId);

    /**
     * 获取重要录像列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 录像列表
     */
    ResponseDTO<PageResult<VideoRecordingVO>> getimportantRecordings(
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 录像文件转码
     *
     * @param recordingId    录像ID
     * @param targetFormat   目标格式
     * @param targetQuality  目标质量
     * @return 转码任务信息
     */
    ResponseDTO<Map<String, Object>> transcodeRecording(
            Long recordingId,
            String targetFormat,
            String targetQuality
    );

    /**
     * 获取转码任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    ResponseDTO<Map<String, Object>> getTranscodeTaskStatus(String taskId);

    /**
     * 取消转码任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    ResponseDTO<Void> cancelTranscodeTask(String taskId);

    /**
     * 录像文件备份
     *
     * @param recordingIds 录像ID列表
     * @param backupType   备份类型
     * @return 备份任务信息
     */
    ResponseDTO<Map<String, Object>> backupRecordings(
            List<Long> recordingIds,
            String backupType
    );

    /**
     * 获取备份任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    ResponseDTO<Map<String, Object>> getBackupTaskStatus(String taskId);

    /**
     * 清理过期录像
     *
     * @param deviceId  设备ID（可选）
     * @param days      保留天数
     * @return 清理结果
     */
    ResponseDTO<Map<String, Object>> cleanupExpiredRecordings(Long deviceId, Integer days);

    /**
     * 获取存储使用情况
     *
     * @param deviceId 设备ID（可选）
     * @return 存储信息
     */
    ResponseDTO<Map<String, Object>> getStorageUsage(Long deviceId);
}
