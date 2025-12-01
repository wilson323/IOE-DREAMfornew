package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.annotation.SaCheckLogin;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.video.domain.entity.MonitorEventEntity;
import net.lab1024.sa.video.domain.form.FaceSearchForm;
import net.lab1024.sa.video.service.VideoAnalysisService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 视频AI分析控制器
 * <p>
 * 提供视频智能分析功能，包括：
 * </p>
 * <ul>
 * <li>人脸识别搜索</li>
 * <li>目标检测与识别</li>
 * <li>行为分析与异常检测</li>
 * <li>轨迹分析</li>
 * <li>智能告警</li>
 * </ul>
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/video/analytics")
@SaCheckLogin
@Tag(name = "视频AI分析", description = "人脸识别、目标检测、行为分析接口")
public class VideoAnalyticsController {

    @Resource
    private VideoAnalysisService videoAnalysisService;

    /**
     * 人脸搜索
     */
    @PostMapping("/face/search")
    @Operation(summary = "人脸搜索", description = "根据人脸图像搜索相似人员")
    @SaCheckPermission("video:analytics:face:search")
    public ResponseDTO<PageResult<Map<String, Object>>> searchFace(@Valid @RequestBody FaceSearchForm searchForm) {
        log.info("开始人脸搜索: deviceId={}, startTime={}, endTime={}",
                searchForm.getDeviceId(), searchForm.getStartTime(), searchForm.getEndTime());

        PageResult<Map<String, Object>> result = videoAnalysisService.searchFace(searchForm);

        log.info("人脸搜索完成: 总数={}", result.getTotal());
        return ResponseDTO.ok(result);
    }

    /**
     * 批量人脸搜索
     */
    @PostMapping("/face/batch-search")
    @Operation(summary = "批量人脸搜索", description = "批量搜索多个人脸")
    @SaCheckPermission("video:analytics:face:batch-search")
    public ResponseDTO<List<Map<String, Object>>> batchSearchFace(
            @NotNull @RequestBody List<Map<String, Object>> faceList) {
        log.info("开始批量人脸搜索: 数量={}", faceList.size());

        List<Map<String, Object>> result = videoAnalysisService.batchSearchFace(faceList);

        log.info("批量人脸搜索完成: 结果数量={}", result.size());
        return ResponseDTO.ok(result);
    }

    /**
     * 轨迹分析
     */
    @PostMapping("/trajectory/analyze")
    @Operation(summary = "轨迹分析", description = "分析目标移动轨迹")
    @SaCheckPermission("video:analytics:trajectory:analyze")
    public ResponseDTO<Map<String, Object>> analyzeTrajectory(
            @NotNull @RequestParam Long deviceId,
            @NotNull @RequestParam LocalDateTime startTime,
            @NotNull @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String targetType) {

        log.info("开始轨迹分析: deviceId={}, startTime={}, endTime={}, targetType={}",
                deviceId, startTime, endTime, targetType);

        Map<String, Object> result = videoAnalysisService.analyzeTrajectory(deviceId, startTime, endTime, targetType);

        log.info("轨迹分析完成");
        return ResponseDTO.ok(result);
    }

    /**
     * 行为分析
     */
    @PostMapping("/behavior/analyze")
    @Operation(summary = "行为分析", description = "分析视频中的人员行为")
    @SaCheckPermission("video:analytics:behavior:analyze")
    public ResponseDTO<List<Map<String, Object>>> analyzeBehavior(
            @NotNull @RequestParam Long deviceId,
            @NotNull @RequestParam LocalDateTime startTime,
            @NotNull @RequestParam LocalDateTime endTime,
            @RequestParam(required = false) String behaviorType) {

        log.info("开始行为分析: deviceId={}, startTime={}, endTime={}, behaviorType={}",
                deviceId, startTime, endTime, behaviorType);

        List<Map<String, Object>> result = videoAnalysisService.analyzeBehavior(deviceId, startTime, endTime, behaviorType);

        log.info("行为分析完成: 检测到行为数量={}", result.size());
        return ResponseDTO.ok(result);
    }

    /**
     * 异常检测
     */
    @PostMapping("/anomaly/detect")
    @Operation(summary = "异常检测", description = "检测视频中的异常行为")
    @SaCheckPermission("video:analytics:anomaly:detect")
    public ResponseDTO<List<Map<String, Object>>> detectAnomaly(
            @NotNull @RequestParam Long deviceId,
            @NotNull @RequestParam LocalDateTime startTime,
            @NotNull @RequestParam LocalDateTime endTime) {

        log.info("开始异常检测: deviceId={}, startTime={}, endTime={}",
                deviceId, startTime, endTime);

        List<Map<String, Object>> result = videoAnalysisService.detectAnomaly(deviceId, startTime, endTime);

        log.info("异常检测完成: 检测到异常数量={}", result.size());
        return ResponseDTO.ok(result);
    }

    /**
     * 获取AI分析配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取AI分析配置", description = "获取视频AI分析的配置信息")
    @SaCheckPermission("video:analytics:config:view")
    public ResponseDTO<Map<String, Object>> getAnalyticsConfig(@NotNull @RequestParam Long deviceId) {
        log.info("获取AI分析配置: deviceId={}", deviceId);

        Map<String, Object> config = videoAnalysisService.getAnalyticsConfig(deviceId);

        return ResponseDTO.ok(config);
    }

    /**
     * 更新AI分析配置
     */
    @PostMapping("/config/update")
    @Operation(summary = "更新AI分析配置", description = "更新视频AI分析的配置")
    @SaCheckPermission("video:analytics:config:update")
    public ResponseDTO<Void> updateAnalyticsConfig(
            @NotNull @RequestParam Long deviceId,
            @RequestBody Map<String, Object> config) {

        log.info("更新AI分析配置: deviceId={}", deviceId);

        videoAnalysisService.updateAnalyticsConfig(deviceId, config);

        log.info("AI分析配置更新完成");
        return ResponseDTO.ok();
    }

    /**
     * 启动AI分析任务
     */
    @PostMapping("/task/start")
    @Operation(summary = "启动AI分析任务", description = "启动设备的AI分析任务")
    @SaCheckPermission("video:analytics:task:start")
    public ResponseDTO<String> startAnalyticsTask(@NotNull @RequestParam Long deviceId) {
        log.info("启动AI分析任务: deviceId={}", deviceId);

        String taskId = videoAnalysisService.startAnalyticsTask(deviceId);

        log.info("AI分析任务启动成功: taskId={}", taskId);
        return ResponseDTO.ok(taskId, "AI分析任务启动成功");
    }

    /**
     * 停止AI分析任务
     */
    @PostMapping("/task/stop")
    @Operation(summary = "停止AI分析任务", description = "停止设备的AI分析任务")
    @SaCheckPermission("video:analytics:task:stop")
    public ResponseDTO<Void> stopAnalyticsTask(@NotNull @RequestParam Long deviceId) {
        log.info("停止AI分析任务: deviceId={}", deviceId);

        videoAnalysisService.stopAnalyticsTask(deviceId);

        log.info("AI分析任务停止成功");
        return ResponseDTO.ok();
    }

    /**
     * 获取AI分析任务状态
     */
    @GetMapping("/task/status")
    @Operation(summary = "获取AI分析任务状态", description = "获取AI分析任务的运行状态")
    @SaCheckPermission("video:analytics:task:status")
    public ResponseDTO<Map<String, Object>> getAnalyticsTaskStatus(@NotNull @RequestParam Long deviceId) {
        log.info("获取AI分析任务状态: deviceId={}", deviceId);

        Map<String, Object> status = videoAnalysisService.getAnalyticsTaskStatus(deviceId);

        return ResponseDTO.ok(status);
    }

    /**
     * 获取分析结果统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取分析结果统计", description = "获取AI分析结果的统计信息")
    @SaCheckPermission("video:analytics:statistics:view")
    public ResponseDTO<Map<String, Object>> getAnalyticsStatistics(
            @NotNull @RequestParam Long deviceId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        log.info("获取分析结果统计: deviceId={}, startTime={}, endTime={}",
                deviceId, startTime, endTime);

        Map<String, Object> statistics = videoAnalysisService.getAnalyticsStatistics(deviceId, startTime, endTime);

        return ResponseDTO.ok(statistics);
    }
}