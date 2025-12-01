package net.lab1024.sa.admin.module.smart.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.admin.module.smart.video.domain.vo.VideoStreamVO;
import net.lab1024.sa.admin.module.smart.video.service.VideoSurveillanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频监控控制器
 * <p>
 * 提供视频监控的核心功能，包括：
 * </p>
 * <ul>
 * <li>实时视频预览</li>
 * <li>历史录像查询</li>
 * <li>视频下载管理</li>
 * <li>多画面显示</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/video/surveillance")
@SaCheckLogin
@Tag(name = "视频监控", description = "实时视频预览与历史录像接口")
public class VideoSurveillanceController {

    @Resource
    private VideoSurveillanceService videoSurveillanceService;

    /**
     * 获取实时视频流地址
     *
     * @param deviceId 设备ID
     * @param channel 通道号，默认为1
     * @param format 视频格式，支持hls、flv、rtsp
     * @return 视频流信息
     */
    @Operation(summary = "获取实时视频流地址")
    @GetMapping("/live/stream")
    @SaCheckPermission("video:surveillance:view")
    public ResponseDTO<VideoStreamVO> getLiveStream(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "通道号") @RequestParam(defaultValue = "1") Integer channel,
            @Parameter(description = "视频格式") @RequestParam(defaultValue = "hls") String format) {
        try {
            log.info("获取实时视频流地址: deviceId={}, channel={}, format={}", deviceId, channel, format);
            String streamUrl = videoSurveillanceService.getLiveStreamUrl(deviceId);
            VideoStreamVO streamInfo = new VideoStreamVO();
            streamInfo.setDeviceId(deviceId);
            streamInfo.setChannel(channel);
            streamInfo.setFormat(format);
            streamInfo.setStreamUrl(streamUrl);
            log.info("获取实时视频流地址成功: {}", streamInfo.getStreamUrl());
            return ResponseDTO.ok(streamInfo);
        } catch (Exception e) {
            log.error("获取实时视频流地址失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("获取实时视频流地址失败: " + e.getMessage());
        }
    }

    /**
     * 获取多画面视频流
     *
     * @param deviceIds 设备ID列表，逗号分隔
     * @param layout 布局模式：1x1, 2x2, 3x3, 4x4
     * @return 多画面视频流信息
     */
    @Operation(summary = "获取多画面视频流")
    @GetMapping("/multi-stream")
    @SaCheckPermission("video:surveillance:view")
    public ResponseDTO<List<VideoStreamVO>> getMultiStream(
            @Parameter(description = "设备ID列表，逗号分隔") @RequestParam String deviceIds,
            @Parameter(description = "布局模式") @RequestParam(defaultValue = "2x2") String layout) {
        try {
            log.info("获取多画面视频流: deviceIds={}, layout={}", deviceIds, layout);
            // 这里需要实现多画面逻辑，暂时返回空列表
            List<VideoStreamVO> streams = List.of();
            log.info("获取多画面视频流成功，数量: {}", streams.size());
            return ResponseDTO.ok(streams);
        } catch (Exception e) {
            log.error("获取多画面视频流失败: deviceIds={}", deviceIds, e);
            return ResponseDTO.error("获取多画面视频流失败: " + e.getMessage());
        }
    }

    /**
     * 查询历史录像列表
     *
     * @param deviceId 设备ID
     * @param channel 通道号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 历史录像列表
     */
    @Operation(summary = "查询历史录像列表")
    @GetMapping("/records")
    @SaCheckPermission("video:surveillance:view")
    public ResponseDTO<Object> getVideoRecords(
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "通道号") @RequestParam(required = false) Integer channel,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Long pageSize) {
        try {
            log.info("查询历史录像: deviceId={}, channel={}, startTime={}, endTime={}",
                    deviceId, channel, startTime, endTime);

            Map<String, Object> queryParams = Map.of(
                    "deviceId", deviceId,
                    "channel", channel,
                    "startTime", startTime,
                    "endTime", endTime,
                    "pageNum", pageNum,
                    "pageSize", pageSize
            );

            Object result = videoSurveillanceService.pageVideoRecords(
                    createPageParam(pageNum, pageSize), deviceId, startTime, endTime, null);
            log.info("查询历史录像成功");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("查询历史录像失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("查询历史录像失败: " + e.getMessage());
        }
    }

    /**
     * 获取录像文件下载地址
     *
     * @param recordId 录像ID
     * @return 下载地址
     */
    @Operation(summary = "获取录像文件下载地址")
    @GetMapping("/download-url")
    @SaCheckPermission("video:surveillance:download")
    public ResponseDTO<String> getDownloadUrl(
            @Parameter(description = "录像ID") @RequestParam @NotNull(message = "录像ID不能为空") Long recordId) {
        try {
            log.info("获取录像下载地址: recordId={}", recordId);
            Object record = videoSurveillanceService.getVideoRecord(recordId);
            String downloadUrl = "http://example.com/download/" + recordId; // 模拟下载地址
            log.info("获取录像下载地址成功: {}", downloadUrl);
            return ResponseDTO.ok(downloadUrl);
        } catch (Exception e) {
            log.error("获取录像下载地址失败: recordId={}", recordId, e);
            return ResponseDTO.error("获取录像下载地址失败: " + e.getMessage());
        }
    }

    /**
     * 开始录制视频
     *
     * @param deviceId 设备ID
     * @param channel 通道号
     * @param duration 录制时长（秒）
     * @return 操作结果
     */
    @Operation(summary = "开始录制视频")
    @PostMapping("/start-record")
    @SaCheckPermission("video:surveillance:record")
    public ResponseDTO<Long> startRecording(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId,
            @Parameter(description = "通道号") @RequestParam(defaultValue = "1") Integer channel,
            @Parameter(description = "录制时长（秒）") @RequestParam(required = false) Integer duration) {
        try {
            log.info("开始录制视频: deviceId={}, channel={}, duration={}", deviceId, channel, duration);
            Long recordId = videoSurveillanceService.startRecording(deviceId);
            log.info("开始录制视频成功: deviceId={}, recordId={}", deviceId, recordId);
            return ResponseDTO.ok(recordId, "录像启动成功");
        } catch (Exception e) {
            log.error("开始录制视频失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("开始录制视频失败: " + e.getMessage());
        }
    }

    /**
     * 停止录制视频
     *
     * @param recordId 录制记录ID
     * @return 操作结果
     */
    @Operation(summary = "停止录制视频")
    @PostMapping("/stop-record")
    @SaCheckPermission("video:surveillance:record")
    public ResponseDTO<String> stopRecording(
            @Parameter(description = "录制记录ID") @RequestParam @NotNull(message = "录制记录ID不能为空") Long recordId) {
        try {
            log.info("停止录制视频: recordId={}", recordId);
            boolean result = videoSurveillanceService.stopRecording(recordId);
            log.info("停止录制视频成功: recordId={}, result={}", recordId, result);
            return ResponseDTO.ok("录像停止成功");
        } catch (Exception e) {
            log.error("停止录制视频失败: recordId={}", recordId, e);
            return ResponseDTO.error("停止录制视频失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    @Operation(summary = "获取设备实时状态")
    @GetMapping("/device-status")
    @SaCheckPermission("video:surveillance:view")
    public ResponseDTO<Object> getDeviceStatus(
            @Parameter(description = "设备ID") @RequestParam @NotNull(message = "设备ID不能为空") Long deviceId) {
        try {
            log.info("获取设备实时状态: deviceId={}", deviceId);
            Object status = videoSurveillanceService.getDeviceStatus(deviceId);
            log.info("获取设备实时状态成功: deviceId={}", deviceId);
            return ResponseDTO.ok(status);
        } catch (Exception e) {
            log.error("获取设备实时状态失败: deviceId={}", deviceId, e);
            return ResponseDTO.error("获取设备实时状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有在线设备状态
     *
     * @return 在线设备列表
     */
    @Operation(summary = "获取所有在线设备状态")
    @GetMapping("/online-devices")
    @SaCheckPermission("video:surveillance:view")
    public ResponseDTO<Object> getOnlineDevices() {
        try {
            log.info("获取所有在线设备状态");
            // 这里需要实现获取所有在线设备状态的逻辑
            Object result = videoSurveillanceService.pageVideoRecords(
                    createPageParam(1L, 100L), null, null, null, null);
            log.info("获取在线设备状态成功");
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取在线设备状态失败", e);
            return ResponseDTO.error("获取在线设备状态失败: " + e.getMessage());
        }
    }

    /**
     * 创建PageParam对象
     */
    private net.lab1024.sa.base.common.domain.PageParam createPageParam(Long pageNum, Long pageSize) {
        net.lab1024.sa.base.common.domain.PageParam pageParam = new net.lab1024.sa.base.common.domain.PageParam();
        pageParam.setPageNum(pageNum);
        pageParam.setPageSize(pageSize);
        return pageParam;
    }
}