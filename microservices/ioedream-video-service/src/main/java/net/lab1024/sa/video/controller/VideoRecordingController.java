package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;

import net.lab1024.sa.video.domain.form.VideoRecordingQueryForm;
import net.lab1024.sa.video.domain.form.VideoRecordingSearchForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingDetailVO;
import net.lab1024.sa.video.domain.vo.VideoRecordingPlaybackVO;
import net.lab1024.sa.video.service.VideoRecordingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 录像回放控制器
 * <p>
 * 提供录像文件管理、回放控制、下载等功能
 * 严格遵循CLAUDE.md规范：
 * - RESTful API设计
 * - 统一响应格式
 * - 安全权限控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@RestController
@PermissionCheck(value = "VIDEO_MANAGE", description = "录像回放管理权限")
@RequestMapping("/api/v1/video/recordings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "录像回放管理", description = "录像文件管理、回放控制、下载等功能")
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ioedream-video-service")
public class VideoRecordingController {

    private final VideoRecordingService videoRecordingService;

    /**
     * 分页查询录像列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询录像列表", description = "根据条件分页查询录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> queryRecordings(@Valid @RequestBody VideoRecordingQueryForm queryForm) {
        log.info("[录像回放] 收到分页查询录像请求: {}", queryForm);
        return SmartResponseUtil.smartResponse(videoRecordingService.queryRecordings(queryForm));
    }

    /**
     * 搜索录像文件
     *
     * @param searchForm 搜索条件
     * @return 搜索结果
     */
    @PostMapping("/search")
    @Operation(summary = "搜索录像文件", description = "根据关键词搜索录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<List<VideoRecordingVO>> searchRecordings(@Valid @RequestBody VideoRecordingSearchForm searchForm) {
        log.info("[录像回放] 收到搜索录像请求: {}", searchForm);
        return SmartResponseUtil.smartResponse(videoRecordingService.searchRecordings(searchForm));
    }

    /**
     * 获取录像详情
     *
     * @param recordingId 录像ID
     * @return 录像详情
     */
    @GetMapping("/{recordingId}")
    @Operation(summary = "获取录像详情", description = "获取指定录像文件的详细信息")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<VideoRecordingDetailVO> getRecordingDetail(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到获取录像详情请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getRecordingDetail(recordingId));
    }

    /**
     * 获取录像播放地址
     *
     * @param recordingId 录像ID
     * @return 播放地址和元数据
     */
    @GetMapping("/{recordingId}/playback")
    @Operation(summary = "获取录像播放地址", description = "获取指定录像文件的播放地址")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<VideoRecordingPlaybackVO> getRecordingPlaybackUrl(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到获取播放地址请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getRecordingPlaybackUrl(recordingId));
    }

    /**
     * 获取录像时间轴
     *
     * @param deviceId    设备ID
     * @param channelId   通道ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 时间轴数据
     */
    @GetMapping("/timeline")
    @Operation(summary = "获取录像时间轴", description = "获取指定设备和时间范围的录像时间轴")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getRecordingTimeline(
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "通道ID")
            @RequestParam(required = false) Long channelId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam LocalDateTime endTime) {
        log.info("[录像回放] 收到获取时间轴请求: deviceId={}, channelId={}, startTime={}, endTime={}",
                deviceId, channelId, startTime, endTime);
        return SmartResponseUtil.smartResponse(videoRecordingService.getRecordingTimeline(deviceId, channelId, startTime, endTime));
    }

    /**
     * 按时间范围查询录像
     *
     * @param deviceId    设备ID
     * @param channelId   通道ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @return 录像列表
     */
    @GetMapping("/time-range")
    @Operation(summary = "按时间范围查询录像", description = "查询指定时间范围内的录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<List<VideoRecordingVO>> getRecordingsByTimeRange(
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "通道ID")
            @RequestParam(required = false) Long channelId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam LocalDateTime endTime) {
        log.info("[录像回放] 收到按时间范围查询录像请求: deviceId={}, channelId={}, startTime={}, endTime={}",
                deviceId, channelId, startTime, endTime);
        return SmartResponseUtil.smartResponse(videoRecordingService.getRecordingsByTimeRange(deviceId, channelId, startTime, endTime));
    }

    /**
     * 删除录像文件
     *
     * @param recordingId 录像ID
     * @return 操作结果
     */
    @DeleteMapping("/{recordingId}")
    @Operation(summary = "删除录像文件", description = "删除指定的录像文件")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Void> deleteRecording(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到删除录像请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.deleteRecording(recordingId));
    }

    /**
     * 批量删除录像文件
     *
     * @param recordingIds 录像ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除录像文件", description = "批量删除多个录像文件")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Void> batchDeleteRecordings(@RequestBody List<Long> recordingIds) {
        log.info("[录像回放] 收到批量删除录像请求: recordingIds={}", recordingIds);
        return SmartResponseUtil.smartResponse(videoRecordingService.batchDeleteRecordings(recordingIds));
    }

    /**
     * 下载录像文件
     *
     * @param recordingId 录像ID
     * @return 下载信息
     */
    @GetMapping("/{recordingId}/download")
    @Operation(summary = "下载录像文件", description = "生成录像文件下载地址")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> downloadRecording(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到下载录像请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.downloadRecording(recordingId));
    }

    /**
     * 获取录像统计信息
     *
     * @param deviceId 设备ID（可选）
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取录像统计信息", description = "获取录像文件的统计信息")
    @PreAuthorize("hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getRecordingStatistics(
            @Parameter(description = "设备ID")
            @RequestParam(required = false) Long deviceId) {
        log.info("[录像回放] 收到获取统计信息请求: deviceId={}", deviceId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getRecordingStatistics(deviceId));
    }

    /**
     * 检查录像文件完整性
     *
     * @param recordingId 录像ID
     * @return 检查结果
     */
    @PostMapping("/{recordingId}/check-integrity")
    @Operation(summary = "检查录像文件完整性", description = "检查指定录像文件的完整性")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> checkRecordingIntegrity(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到检查完整性请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.checkRecordingIntegrity(recordingId));
    }

    /**
     * 修复录像文件
     *
     * @param recordingId 录像ID
     * @return 修复结果
     */
    @PostMapping("/{recordingId}/repair")
    @Operation(summary = "修复录像文件", description = "修复损坏的录像文件")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> repairRecording(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到修复录像请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.repairRecording(recordingId));
    }

    /**
     * 获取设备的录像列表
     *
     * @param deviceId 设备ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备的录像列表", description = "获取指定设备的所有录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getDeviceRecordings(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[录像回放] 收到获取设备录像请求: deviceId={}, pageNum={}, pageSize={}", deviceId, pageNum, pageSize);
        return SmartResponseUtil.smartResponse(videoRecordingService.getDeviceRecordings(deviceId, pageNum, pageSize));
    }

    /**
     * 获取通道的录像列表
     *
     * @param deviceId  设备ID
     * @param channelId 通道ID
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    @GetMapping("/device/{deviceId}/channel/{channelId}")
    @Operation(summary = "获取通道的录像列表", description = "获取指定设备和通道的录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getChannelRecordings(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "通道ID", required = true)
            @PathVariable Long channelId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[录像回放] 收到获取通道录像请求: deviceId={}, channelId={}, pageNum={}, pageSize={}",
                deviceId, channelId, pageNum, pageSize);
        return SmartResponseUtil.smartResponse(videoRecordingService.getChannelRecordings(deviceId, channelId, pageNum, pageSize));
    }

    /**
     * 获取重要事件录像
     *
     * @param eventType 事件类型
     * @param pageNum   页码
     * @param pageSize  页大小
     * @return 录像列表
     */
    @GetMapping("/events/{eventType}")
    @Operation(summary = "获取重要事件录像", description = "获取指定事件类型的录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getEventRecordings(
            @Parameter(description = "事件类型", required = true)
            @PathVariable String eventType,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[录像回放] 收到获取事件录像请求: eventType={}, pageNum={}, pageSize={}", eventType, pageNum, pageSize);
        return SmartResponseUtil.smartResponse(videoRecordingService.getEventRecordings(eventType, pageNum, pageSize));
    }

    /**
     * 标记录像为重要
     *
     * @param recordingId 录像ID
     * @param remark      备注
     * @return 操作结果
     */
    @PostMapping("/{recordingId}/mark-important")
    @Operation(summary = "标记录像为重要", description = "将指定录像标记为重要")
    @PreAuthorize("hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Void> markRecordingAsImportant(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId,
            @Parameter(description = "备注")
            @RequestParam(required = false) String remark) {
        log.info("[录像回放] 收到标记重要录像请求: recordingId={}, remark={}", recordingId, remark);
        return SmartResponseUtil.smartResponse(videoRecordingService.markRecordingAsImportant(recordingId, remark));
    }

    /**
     * 取消录像重要标记
     *
     * @param recordingId 录像ID
     * @return 操作结果
     */
    @PostMapping("/{recordingId}/unmark-important")
    @Operation(summary = "取消录像重要标记", description = "取消录像的重要标记")
    @PreAuthorize("hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Void> unmarkRecordingAsImportant(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId) {
        log.info("[录像回放] 收到取消重要标记请求: recordingId={}", recordingId);
        return SmartResponseUtil.smartResponse(videoRecordingService.unmarkRecordingAsImportant(recordingId));
    }

    /**
     * 获取重要录像列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 录像列表
     */
    @GetMapping("/important")
    @Operation(summary = "获取重要录像列表", description = "获取所有标记为重要的录像文件")
    @PreAuthorize("hasRole('VIDEO_VIEWER') or hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getImportantRecordings(
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[录像回放] 收到获取重要录像请求: pageNum={}, pageSize={}", pageNum, pageSize);
        return SmartResponseUtil.smartResponse(videoRecordingService.getImportantRecordings(pageNum, pageSize));
    }

    /**
     * 录像文件转码
     *
     * @param recordingId    录像ID
     * @param targetFormat   目标格式
     * @param targetQuality  目标质量
     * @return 转码任务信息
     */
    @PostMapping("/{recordingId}/transcode")
    @Operation(summary = "录像文件转码", description = "将录像文件转码为指定格式和质量")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> transcodeRecording(
            @Parameter(description = "录像ID", required = true)
            @PathVariable Long recordingId,
            @Parameter(description = "目标格式", required = true)
            @RequestParam String targetFormat,
            @Parameter(description = "目标质量", required = true)
            @RequestParam String targetQuality) {
        log.info("[录像回放] 收到转码请求: recordingId={}, targetFormat={}, targetQuality={}",
                recordingId, targetFormat, targetQuality);
        return SmartResponseUtil.smartResponse(videoRecordingService.transcodeRecording(recordingId, targetFormat, targetQuality));
    }

    /**
     * 获取转码任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/transcode/{taskId}/status")
    @Operation(summary = "获取转码任务状态", description = "查询转码任务的执行状态")
    @PreAuthorize("hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getTranscodeTaskStatus(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId) {
        log.info("[录像回放] 收到获取转码任务状态请求: taskId={}", taskId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getTranscodeTaskStatus(taskId));
    }

    /**
     * 取消转码任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    @PostMapping("/transcode/{taskId}/cancel")
    @Operation(summary = "取消转码任务", description = "取消正在执行的转码任务")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Void> cancelTranscodeTask(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId) {
        log.info("[录像回放] 收到取消转码任务请求: taskId={}", taskId);
        return SmartResponseUtil.smartResponse(videoRecordingService.cancelTranscodeTask(taskId));
    }

    /**
     * 录像文件备份
     *
     * @param recordingIds 录像ID列表
     * @param backupType   备份类型
     * @return 备份任务信息
     */
    @PostMapping("/backup")
    @Operation(summary = "录像文件备份", description = "将指定的录像文件进行备份")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> backupRecordings(
            @Parameter(description = "录像ID列表", required = true)
            @RequestBody List<Long> recordingIds,
            @Parameter(description = "备份类型", required = true)
            @RequestParam String backupType) {
        log.info("[录像回放] 收到备份录像请求: recordingIds={}, backupType={}", recordingIds, backupType);
        return SmartResponseUtil.smartResponse(videoRecordingService.backupRecordings(recordingIds, backupType));
    }

    /**
     * 获取备份任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/backup/{taskId}/status")
    @Operation(summary = "获取备份任务状态", description = "查询备份任务的执行状态")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> getBackupTaskStatus(
            @Parameter(description = "任务ID", required = true)
            @PathVariable String taskId) {
        log.info("[录像回放] 收到获取备份任务状态请求: taskId={}", taskId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getBackupTaskStatus(taskId));
    }

    /**
     * 清理过期录像
     *
     * @param deviceId  设备ID（可选）
     * @param days      保留天数
     * @return 清理结果
     */
    @PostMapping("/cleanup-expired")
    @Operation(summary = "清理过期录像", description = "清理指定天数之前的过期录像文件")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "录像管理操作")
    public ResponseDTO<Map<String, Object>> cleanupExpiredRecordings(
            @Parameter(description = "设备ID")
            @RequestParam(required = false) Long deviceId,
            @Parameter(description = "保留天数", required = true)
            @RequestParam Integer days) {
        log.info("[录像回放] 收到清理过期录像请求: deviceId={}, days={}", deviceId, days);
        return SmartResponseUtil.smartResponse(videoRecordingService.cleanupExpiredRecordings(deviceId, days));
    }

    /**
     * 获取存储使用情况
     *
     * @param deviceId 设备ID（可选）
     * @return 存储信息
     */
    @GetMapping("/storage-usage")
    @Operation(summary = "获取存储使用情况", description = "获取录像存储空间使用情况")
    @PreAuthorize("hasRole('VIDEO_OPERATOR') or hasRole('VIDEO_MANAGER')")
    public ResponseDTO<Map<String, Object>> getStorageUsage(
            @Parameter(description = "设备ID")
            @RequestParam(required = false) Long deviceId) {
        log.info("[录像回放] 收到获取存储使用情况请求: deviceId={}", deviceId);
        return SmartResponseUtil.smartResponse(videoRecordingService.getStorageUsage(deviceId));
    }
}