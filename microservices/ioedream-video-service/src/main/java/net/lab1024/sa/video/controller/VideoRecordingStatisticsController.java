package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoRecordingStatisticsQueryForm;
import net.lab1024.sa.video.service.VideoRecordingStatisticsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/video/recording/statistics")
@Tag(name = "视频录像统计报表")
@Slf4j
public class VideoRecordingStatisticsController {

    @Resource
    private VideoRecordingStatisticsService videoRecordingStatisticsService;

    @GetMapping("/overview")
    @Operation(summary = "获取总览统计", description = "包括任务总数、成功率、总大小、总时长等")
    public ResponseDTO<Map<String, Object>> getOverviewStatistics(
            @Parameter(description = "开始时间", required = false)
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = false)
            @RequestParam(required = false) LocalDateTime endTime) {

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        log.info("[录像统计] 查询总览统计: startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> statistics = videoRecordingStatisticsService.getOverviewStatistics(startTime, endTime);

        return ResponseDTO.ok(statistics);
    }

    @GetMapping("/device")
    @Operation(summary = "按设备统计", description = "统计指定设备的录像任务情况")
    public ResponseDTO<Map<String, Object>> getDeviceStatistics(
            @Parameter(description = "设备ID", required = true)
            @RequestParam String deviceId,
            @Parameter(description = "开始时间", required = false)
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = false)
            @RequestParam(required = false) LocalDateTime endTime) {

        log.info("[录像统计] 查询设备统计: deviceId={}", deviceId);

        Map<String, Object> statistics = videoRecordingStatisticsService.getDeviceStatistics(deviceId, startTime, endTime);

        return ResponseDTO.ok(statistics);
    }

    @GetMapping("/time")
    @Operation(summary = "按时间统计", description = "按时间粒度统计录像任务")
    public ResponseDTO<Map<String, Object>> getTimeStatistics(
            @Parameter(description = "开始时间", required = false)
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = false)
            @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "时间粒度: 1-小时 2-天 3-周 4-月", required = false)
            @RequestParam(required = false, defaultValue = "2") Integer granularity) {

        log.info("[录像统计] 查询时间统计: granularity={}", granularity);

        Map<String, Object> statistics = videoRecordingStatisticsService.getTimeStatistics(startTime, endTime, granularity);

        return ResponseDTO.ok(statistics);
    }

    @GetMapping("/quality")
    @Operation(summary = "按质量统计", description = "按录像质量统计任务分布")
    public ResponseDTO<Map<String, Object>> getQualityStatistics(
            @Parameter(description = "开始时间", required = false)
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = false)
            @RequestParam(required = false) LocalDateTime endTime) {

        log.info("[录像统计] 查询质量统计");

        Map<String, Object> statistics = videoRecordingStatisticsService.getQualityStatistics(startTime, endTime);

        return ResponseDTO.ok(statistics);
    }

    @GetMapping("/status")
    @Operation(summary = "按状态统计", description = "按任务状态统计数量分布")
    public ResponseDTO<Map<String, Object>> getStatusStatistics(
            @Parameter(description = "开始时间", required = false)
            @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间", required = false)
            @RequestParam(required = false) LocalDateTime endTime) {

        log.info("[录像统计] 查询状态统计");

        Map<String, Object> statistics = videoRecordingStatisticsService.getStatusStatistics(startTime, endTime);

        return ResponseDTO.ok(statistics);
    }

    @GetMapping("/storage")
    @Operation(summary = "存储统计", description = "统计存储空间使用情况")
    public ResponseDTO<Map<String, Object>> getStorageStatistics() {

        log.info("[录像统计] 查询存储统计");

        Map<String, Object> statistics = videoRecordingStatisticsService.getStorageStatistics();

        return ResponseDTO.ok(statistics);
    }

    @PostMapping("/custom")
    @Operation(summary = "自定义统计", description = "根据查询条件自定义统计")
    public ResponseDTO<Map<String, Object>> getCustomStatistics(
            @RequestBody VideoRecordingStatisticsQueryForm queryForm) {

        log.info("[录像统计] 自定义统计: queryForm={}", queryForm);

        Map<String, Object> statistics = videoRecordingStatisticsService.getCustomStatistics(queryForm);

        return ResponseDTO.ok(statistics);
    }
}
