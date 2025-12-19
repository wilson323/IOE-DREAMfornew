package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;

import net.lab1024.sa.video.domain.form.VideoStreamStartForm;
import net.lab1024.sa.video.domain.form.VideoStreamQueryForm;
import net.lab1024.sa.video.domain.vo.VideoStreamVO;
import net.lab1024.sa.video.domain.vo.VideoStreamSessionVO;
import net.lab1024.sa.video.service.VideoStreamService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频流管理控制器
 * <p>
 * 提供视频流生命周期管理、质量控制、录制等功能
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
@RequestMapping("/api/v1/video/streams")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "视频流管理", description = "视频流生命周期管理、质量控制、录制等功能")
@ConditionalOnProperty(name = "spring.application.name", havingValue = "ioedream-video-service")
@PermissionCheck(value = "VIDEO_STREAM", description = "视频流管理模块权限")
public class VideoStreamController {

    private final VideoStreamService videoStreamService;

    /**
     * 启动视频流
     *
     * @param startForm 启动参数
     * @return 流会话信息
     */
    @PostMapping("/start")
    @Operation(summary = "启动视频流", description = "启动指定设备的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<VideoStreamSessionVO> startStream(@Valid @RequestBody VideoStreamStartForm startForm) {
        log.info("[视频流] 收到启动流请求: {}", startForm);
        return SmartResponseUtil.smartResponse(videoStreamService.startStream(startForm));
    }

    /**
     * 停止视频流
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    @PostMapping("/{streamId}/stop")
    @Operation(summary = "停止视频流", description = "停止指定的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> stopStream(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到停止流请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.stopStream(streamId));
    }

    /**
     * 暂停视频流
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    @PostMapping("/{streamId}/pause")
    @Operation(summary = "暂停视频流", description = "暂停指定的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> pauseStream(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到暂停流请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.pauseStream(streamId));
    }

    /**
     * 恢复视频流
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    @PostMapping("/{streamId}/resume")
    @Operation(summary = "恢复视频流", description = "恢复暂停的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> resumeStream(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到恢复流请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.resumeStream(streamId));
    }

    /**
     * 切换流质量
     *
     * @param streamId 流ID
     * @param quality  质量 (high/medium/low)
     * @return 操作结果
     */
    @PostMapping("/{streamId}/quality")
    @Operation(summary = "切换流质量", description = "动态调整视频流质量")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> switchStreamQuality(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId,
            @Parameter(description = "视频质量", required = true)
            @RequestParam String quality) {
        log.info("[视频流] 收到切换质量请求: streamId={}, quality={}", streamId, quality);
        return SmartResponseUtil.smartResponse(videoStreamService.switchStreamQuality(streamId, quality));
    }

    /**
     * 获取视频流信息
     *
     * @param streamId 流ID
     * @return 流信息
     */
    @GetMapping("/{streamId}")
    @Operation(summary = "获取视频流信息", description = "获取指定视频流的详细信息")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<VideoStreamVO> getStreamById(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到获取流信息请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.getStreamById(streamId));
    }

    /**
     * 根据设备查询视频流
     *
     * @param deviceId 设备ID
     * @return 流列表
     */
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "根据设备查询视频流", description = "获取指定设备的所有视频流")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<List<VideoStreamVO>> getStreamsByDevice(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {
        log.info("[视频流] 收到根据设备查询流请求: deviceId={}", deviceId);
        return SmartResponseUtil.smartResponse(videoStreamService.getStreamsByDevice(deviceId));
    }

    /**
     * 获取活跃视频流
     *
     * @return 活跃流列表
     */
    @GetMapping("/active")
    @Operation(summary = "获取活跃视频流", description = "获取当前所有活跃的视频流")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<List<VideoStreamVO>> getActiveStreams() {
        log.info("[视频流] 收到获取活跃流请求");
        return SmartResponseUtil.smartResponse(videoStreamService.getActiveStreams());
    }

    /**
     * 分页查询视频流
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询视频流", description = "根据条件分页查询视频流")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<Map<String, Object>> queryStreams(@Valid @RequestBody VideoStreamQueryForm queryForm) {
        log.info("[视频流] 收到分页查询请求: {}", queryForm);
        return SmartResponseUtil.smartResponse(videoStreamService.queryStreams(queryForm));
    }

    /**
     * 视频流截图
     *
     * @param deviceId  设备ID
     * @param channelId 通道ID
     * @return 截图信息
     */
    @PostMapping("/capture")
    @Operation(summary = "视频流截图", description = "对指定视频流进行截图")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Map<String, Object>> captureSnapshot(
            @Parameter(description = "设备ID", required = true)
            @RequestParam Long deviceId,
            @Parameter(description = "通道ID")
            @RequestParam(required = false) Long channelId) {
        log.info("[视频流] 收到截图请求: deviceId={}, channelId={}", deviceId, channelId);
        return SmartResponseUtil.smartResponse(videoStreamService.captureSnapshot(deviceId, channelId));
    }

    /**
     * 启用流录制
     *
     * @param streamId 流ID
     * @param duration 录制时长（分钟）
     * @return 操作结果
     */
    @PostMapping("/{streamId}/record/start")
    @Operation(summary = "启用流录制", description = "开始录制指定的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> startStreamRecording(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId,
            @Parameter(description = "录制时长（分钟）")
            @RequestParam(required = false) Integer duration) {
        log.info("[视频流] 收到启用录制请求: streamId={}, duration={}", streamId, duration);
        return SmartResponseUtil.smartResponse(videoStreamService.startStreamRecording(streamId, duration));
    }

    /**
     * 停止流录制
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    @PostMapping("/{streamId}/record/stop")
    @Operation(summary = "停止流录制", description = "停止录制指定的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> stopStreamRecording(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到停止录制请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.stopStreamRecording(streamId));
    }

    /**
     * 获取录制列表
     *
     * @param deviceId 设备ID
     * @return 录制列表
     */
    @GetMapping("/recordings/{deviceId}")
    @Operation(summary = "获取录制列表", description = "获取指定设备的录制文件列表")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<List<Map<String, Object>>> getRecordings(
            @Parameter(description = "设备ID", required = true)
            @PathVariable Long deviceId) {
        log.info("[视频流] 收到获取录制列表请求: deviceId={}", deviceId);
        return SmartResponseUtil.smartResponse(videoStreamService.getRecordings(deviceId));
    }

    /**
     * 检测流状态
     *
     * @param streamId 流ID
     * @return 流状态信息
     */
    @GetMapping("/{streamId}/status")
    @Operation(summary = "检测流状态", description = "检测指定视频流的实时状态")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<Map<String, Object>> checkStreamStatus(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到检测流状态请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.checkStreamStatus(streamId));
    }

    /**
     * 获取流统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取流统计信息", description = "获取视频流的整体统计信息")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Map<String, Object>> getStreamStatistics() {
        log.info("[视频流] 收到获取流统计信息请求");
        return SmartResponseUtil.smartResponse(videoStreamService.getStreamStatistics());
    }

    /**
     * 获取流播放地址
     *
     * @param streamId 流ID
     * @return 播放地址
     */
    @GetMapping("/{streamId}/play-urls")
    @Operation(summary = "获取流播放地址", description = "获取指定视频流的多种协议播放地址")
    @PermissionCheck(value = {"VIDEO_VIEWER", "VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频查看、操作或管理权限")
    public ResponseDTO<Map<String, String>> getStreamPlayUrls(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到获取播放地址请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.getStreamPlayUrls(streamId));
    }

    /**
     * 重连失败的流
     *
     * @param streamId 流ID
     * @return 操作结果
     */
    @PostMapping("/{streamId}/reconnect")
    @Operation(summary = "重连失败的流", description = "尝试重新连接断开的视频流")
    @PermissionCheck(value = {"VIDEO_OPERATOR", "VIDEO_MANAGER"}, description = "视频操作或管理权限")
    public ResponseDTO<Void> reconnectStream(
            @Parameter(description = "流ID", required = true)
            @PathVariable Long streamId) {
        log.info("[视频流] 收到重连流请求: streamId={}", streamId);
        return SmartResponseUtil.smartResponse(videoStreamService.reconnectStream(streamId));
    }
}
