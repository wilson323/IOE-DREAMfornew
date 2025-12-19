package net.lab1024.sa.video.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.video.openapi.domain.request.*;
import net.lab1024.sa.video.openapi.domain.response.*;
import net.lab1024.sa.video.openapi.service.VideoOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台视频监控API控制器
 * 提供实时视频、录像回放、AI分析等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/video")
@RequiredArgsConstructor
@Tag(name = "开放平台视频监控API", description = "提供实时视频、录像回放、AI分析等功能")
@Validated
public class VideoOpenApiController {

    private final VideoOpenApiService videoOpenApiService;

    /**
     * 获取实时视频流地址
     */
    @GetMapping("/stream/live/{deviceId}")
    @Operation(summary = "获取实时视频流地址", description = "获取设备的实时视频流播放地址")
    public ResponseDTO<LiveStreamResponse> getLiveStream(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Parameter(description = "流类型") @RequestParam(defaultValue = "main") String streamType,
            @Parameter(description = "播放协议") @RequestParam(defaultValue = "hls") String protocol,
            @Parameter(description = "清晰度") @RequestParam(required = false) String quality,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取实时视频流: deviceId={}, streamType={}, protocol={}",
                deviceId, streamType, protocol);

        LiveStreamResponse response = videoOpenApiService.getLiveStream(deviceId, streamType, protocol, quality, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 开始实时视频推流
     */
    @PostMapping("/stream/live/{deviceId}/start")
    @Operation(summary = "开始实时视频推流", description = "启动设备的实时视频推流")
    public ResponseDTO<StreamControlResponse> startLiveStream(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Valid @RequestBody StartStreamRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 开始实时推流: deviceId={}, streamType={}, clientIp={}",
                deviceId, request.getStreamType(), clientIp);

        StreamControlResponse response = videoOpenApiService.startLiveStream(deviceId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 停止实时视频推流
     */
    @PostMapping("/stream/live/{deviceId}/stop")
    @Operation(summary = "停止实时视频推流", description = "停止设备的实时视频推流")
    public ResponseDTO<StreamControlResponse> stopLiveStream(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Valid @RequestBody StopStreamRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 停止实时推流: deviceId={}, streamType={}, clientIp={}",
                deviceId, request.getStreamType(), clientIp);

        StreamControlResponse response = videoOpenApiService.stopLiveStream(deviceId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取录像文件列表
     */
    @GetMapping("/recordings/{deviceId}")
    @Operation(summary = "获取录像文件列表", description = "分页获取设备的录像文件列表")
    public ResponseDTO<PageResult<VideoRecordingResponse>> getRecordings(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "开始时间") @RequestParam String startTime,
            @Parameter(description = "结束时间") @RequestParam String endTime,
            @Parameter(description = "录像类型") @RequestParam(required = false) String recordingType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        RecordingQueryRequest queryRequest = RecordingQueryRequest.builder()
                .deviceId(deviceId)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .startTime(startTime)
                .endTime(endTime)
                .recordingType(recordingType)
                .build();

        log.info("[开放API] 查询录像文件: deviceId={}, startTime={}, endTime={}, recordingType={}",
                deviceId, startTime, endTime, recordingType);

        PageResult<VideoRecordingResponse> result = videoOpenApiService.getRecordings(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取录像回放地址
     */
    @GetMapping("/recordings/{recordingId}/playback")
    @Operation(summary = "获取录像回放地址", description = "获取录像文件的回放播放地址")
    public ResponseDTO<PlaybackResponse> getPlaybackUrl(
            @Parameter(description = "录像ID") @PathVariable String recordingId,
            @Parameter(description = "播放协议") @RequestParam(defaultValue = "hls") String protocol,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取录像回放地址: recordingId={}, protocol={}, startTime={}, endTime={}",
                recordingId, protocol, startTime, endTime);

        PlaybackResponse response = videoOpenApiService.getPlaybackUrl(recordingId, protocol, startTime, endTime, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 下载录像文件
     */
    @PostMapping("/recordings/{recordingId}/download")
    @Operation(summary = "下载录像文件", description = "创建录像文件下载任务")
    public ResponseDTO<DownloadTaskResponse> downloadRecording(
            @Parameter(description = "录像ID") @PathVariable String recordingId,
            @Valid @RequestBody DownloadRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 下载录像文件: recordingId={}, format={}, clientIp={}",
                recordingId, request.getFormat(), clientIp);

        DownloadTaskResponse response = videoOpenApiService.downloadRecording(recordingId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 人脸识别检测
     */
    @PostMapping("/ai/face-detection")
    @Operation(summary = "人脸识别检测", description = "AI人脸识别和检测分析")
    public ResponseDTO<FaceDetectionResponse> faceDetection(
            @Valid @RequestBody FaceDetectionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 人脸识别检测: deviceId={}, detectionType={}, clientIp={}",
                request.getDeviceId(), request.getDetectionType(), clientIp);

        FaceDetectionResponse response = videoOpenApiService.faceDetection(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 行为分析检测
     */
    @PostMapping("/ai/behavior-analysis")
    @Operation(summary = "行为分析检测", description = "AI行为分析和异常检测")
    public ResponseDTO<BehaviorAnalysisResponse> behaviorAnalysis(
            @Valid @RequestBody BehaviorAnalysisRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 行为分析检测: deviceId={}, analysisType={}, clientIp={}",
                request.getDeviceId(), request.getAnalysisType(), clientIp);

        BehaviorAnalysisResponse response = videoOpenApiService.behaviorAnalysis(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 目标跟踪分析
     */
    @PostMapping("/ai/object-tracking")
    @Operation(summary = "目标跟踪分析", description = "AI目标跟踪和轨迹分析")
    public ResponseDTO<ObjectTrackingResponse> objectTracking(
            @Valid @RequestBody ObjectTrackingRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 目标跟踪分析: deviceId={}, trackingType={}, clientIp={}",
                request.getDeviceId(), request.getTrackingType(), clientIp);

        ObjectTrackingResponse response = videoOpenApiService.objectTracking(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取AI分析结果列表
     */
    @GetMapping("/ai/analysis-results")
    @Operation(summary = "获取AI分析结果列表", description = "分页获取AI分析结果列表")
    public ResponseDTO<PageResult<AIAnalysisResultResponse>> getAIAnalysisResults(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "分析类型") @RequestParam(required = false) String analysisType,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @Parameter(description = "置信度阈值") @RequestParam(required = false) Double confidenceThreshold,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AIAnalysisQueryRequest queryRequest = AIAnalysisQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .deviceId(deviceId)
                .analysisType(analysisType)
                .startTime(startTime)
                .endTime(endTime)
                .confidenceThreshold(confidenceThreshold)
                .build();

        log.info("[开放API] 查询AI分析结果: deviceId={}, analysisType={}, confidenceThreshold={}",
                deviceId, analysisType, confidenceThreshold);

        PageResult<AIAnalysisResultResponse> result = videoOpenApiService.getAIAnalysisResults(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取视频设备列表
     */
    @GetMapping("/devices")
    @Operation(summary = "获取视频设备列表", description = "获取视频监控设备列表")
    public ResponseDTO<PageResult<VideoDeviceResponse>> getVideoDevices(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "设备名称") @RequestParam(required = false) String deviceName,
            @Parameter(description = "设备状态") @RequestParam(required = false) Integer deviceStatus,
            @Parameter(description = "设备类型") @RequestParam(required = false) String deviceType,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        VideoDeviceQueryRequest queryRequest = VideoDeviceQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .deviceName(deviceName)
                .deviceStatus(deviceStatus)
                .deviceType(deviceType)
                .areaId(areaId)
                .build();

        log.info("[开放API] 查询视频设备: deviceName={}, deviceStatus={}, areaId={}",
                deviceName, deviceStatus, areaId);

        PageResult<VideoDeviceResponse> result = videoOpenApiService.getVideoDevices(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取视频设备详情
     */
    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "获取视频设备详情", description = "根据设备ID获取视频设备详情")
    public ResponseDTO<VideoDeviceDetailResponse> getVideoDeviceDetail(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询视频设备详情: deviceId={}", deviceId);

        VideoDeviceDetailResponse response = videoOpenApiService.getVideoDeviceDetail(deviceId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 云台控制
     */
    @PostMapping("/devices/{deviceId}/ptz/control")
    @Operation(summary = "云台控制", description = "控制设备云台转动")
    public ResponseDTO<Void> ptzControl(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Valid @RequestBody PTZControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 云台控制: deviceId={}, command={}, pan={}, tilt={}, zoom={}, clientIp={}",
                deviceId, request.getCommand(), request.getPan(), request.getTilt(), request.getZoom(), clientIp);

        videoOpenApiService.ptzControl(deviceId, request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 获取视频设备实时状态
     */
    @GetMapping("/devices/{deviceId}/status")
    @Operation(summary = "获取视频设备实时状态", description = "获取视频设备的实时状态信息")
    public ResponseDTO<VideoDeviceStatusResponse> getVideoDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取视频设备状态: deviceId={}", deviceId);

        VideoDeviceStatusResponse response = videoOpenApiService.getVideoDeviceStatus(deviceId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取视频统计分析
     */
    @GetMapping("/statistics/video")
    @Operation(summary = "获取视频统计分析", description = "获取视频监控统计分析数据")
    public ResponseDTO<VideoStatisticsResponse> getVideoStatistics(
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取视频统计分析: statisticsType={}, startDate={}, endDate={}, deviceId={}, areaId={}",
                statisticsType, startDate, endDate, deviceId, areaId);

        VideoStatisticsResponse response = videoOpenApiService.getVideoStatistics(
                statisticsType, startDate, endDate, deviceId, areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取AI分析统计
     */
    @GetMapping("/statistics/ai-analysis")
    @Operation(summary = "获取AI分析统计", description = "获取AI分析功能统计信息")
    public ResponseDTO<AIAnalysisStatisticsResponse> getAIAnalysisStatistics(
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "分析类型") @RequestParam(required = false) String analysisType,
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取AI分析统计: statisticsType={}, startDate={}, endDate={}, analysisType={}, deviceId={}",
                statisticsType, startDate, endDate, analysisType, deviceId);

        AIAnalysisStatisticsResponse response = videoOpenApiService.getAIAnalysisStatistics(
                statisticsType, startDate, endDate, analysisType, deviceId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
