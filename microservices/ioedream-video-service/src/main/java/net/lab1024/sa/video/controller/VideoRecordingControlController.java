package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.VideoRecordingControlForm;
import net.lab1024.sa.video.domain.form.VideoRecordingPlanQueryForm;
import net.lab1024.sa.video.domain.vo.VideoRecordingTaskVO;
import net.lab1024.sa.video.service.VideoRecordingControlService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 视频录像控制Controller
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/v1/video/recording/control")
@Tag(name = "视频录像控制")
@Slf4j
public class VideoRecordingControlController {

    @Resource
    private VideoRecordingControlService videoRecordingControlService;

    @Operation(summary = "根据计划启动录像")
    @PostMapping("/start/plan/{planId}")
    public ResponseDTO<Long> startRecordingByPlan(@PathVariable Long planId) {
        log.info("[录像控制] 根据计划启动录像: planId={}", planId);
        Long taskId = videoRecordingControlService.startRecordingByPlan(planId);
        return ResponseDTO.ok(taskId);
    }

    @Operation(summary = "手动启动录像")
    @PostMapping("/start/manual")
    public ResponseDTO<Long> startManualRecording(@Valid @RequestBody VideoRecordingControlForm controlForm) {
        log.info("[录像控制] 手动启动录像: deviceId={}", controlForm.getDeviceId());
        Long taskId = videoRecordingControlService.startManualRecording(controlForm);
        return ResponseDTO.ok(taskId);
    }

    @Operation(summary = "停止录像")
    @PostMapping("/stop/{taskId}")
    public ResponseDTO<Integer> stopRecording(@PathVariable Long taskId) {
        log.info("[录像控制] 停止录像: taskId={}", taskId);
        Integer rows = videoRecordingControlService.stopRecording(taskId);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "获取录像任务状态")
    @GetMapping("/status/{taskId}")
    public ResponseDTO<VideoRecordingTaskVO> getRecordingStatus(@PathVariable Long taskId) {
        log.info("[录像控制] 获取录像任务状态: taskId={}", taskId);
        VideoRecordingTaskVO task = videoRecordingControlService.getRecordingStatus(taskId);
        return ResponseDTO.ok(task);
    }

    @Operation(summary = "获取设备当前录像状态")
    @GetMapping("/status/device/{deviceId}")
    public ResponseDTO<VideoRecordingTaskVO> getDeviceRecordingStatus(@PathVariable String deviceId) {
        log.info("[录像控制] 获取设备当前录像状态: deviceId={}", deviceId);
        VideoRecordingTaskVO task = videoRecordingControlService.getDeviceRecordingStatus(deviceId);
        return ResponseDTO.ok(task);
    }

    @Operation(summary = "分页查询录像任务")
    @PostMapping("/query")
    public ResponseDTO<PageResult<VideoRecordingTaskVO>> queryTasks(@Valid @RequestBody VideoRecordingPlanQueryForm queryForm) {
        log.info("[录像控制] 分页查询录像任务: pageNum={}, pageSize={}",
                queryForm.getPageNum(), queryForm.getPageSize());
        PageResult<VideoRecordingTaskVO> pageResult = videoRecordingControlService.queryTasks(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "查询设备的录像任务列表")
    @GetMapping("/device/{deviceId}/tasks")
    public ResponseDTO<List<VideoRecordingTaskVO>> getTasksByDevice(@PathVariable String deviceId) {
        log.info("[录像控制] 查询设备的录像任务: deviceId={}", deviceId);
        List<VideoRecordingTaskVO> tasks = videoRecordingControlService.getTasksByDevice(deviceId);
        return ResponseDTO.ok(tasks);
    }

    @Operation(summary = "查询计划的录像任务列表")
    @GetMapping("/plan/{planId}/tasks")
    public ResponseDTO<List<VideoRecordingTaskVO>> getTasksByPlan(@PathVariable Long planId) {
        log.info("[录像控制] 查询计划的录像任务: planId={}", planId);
        List<VideoRecordingTaskVO> tasks = videoRecordingControlService.getTasksByPlan(planId);
        return ResponseDTO.ok(tasks);
    }

    @Operation(summary = "查询时间范围的录像任务")
    @GetMapping("/tasks/time-range")
    public ResponseDTO<List<VideoRecordingTaskVO>> getTasksByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        log.info("[录像控制] 查询时间范围的录像任务: startTime={}, endTime={}", startTime, endTime);
        List<VideoRecordingTaskVO> tasks = videoRecordingControlService.getTasksByTimeRange(startTime, endTime);
        return ResponseDTO.ok(tasks);
    }

    @Operation(summary = "重试失败的录像任务")
    @PostMapping("/retry/{taskId}")
    public ResponseDTO<Long> retryFailedTask(@PathVariable Long taskId) {
        log.info("[录像控制] 重试失败的录像任务: taskId={}", taskId);
        Long newTaskId = videoRecordingControlService.retryFailedTask(taskId);
        return ResponseDTO.ok(newTaskId);
    }

    @Operation(summary = "处理事件触发的录像")
    @PostMapping("/event/{deviceId}")
    public ResponseDTO<Long> handleEventRecording(@PathVariable String deviceId,
                                                 @RequestParam String eventType) {
        log.info("[录像控制] 处理事件触发录像: deviceId={}, eventType={}", deviceId, eventType);
        Long taskId = videoRecordingControlService.handleEventRecording(deviceId, eventType);
        return ResponseDTO.ok(taskId);
    }

    @Operation(summary = "统计录像文件存储大小")
    @GetMapping("/storage/size")
    public ResponseDTO<Long> sumRecordingFileSize(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        log.info("[录像控制] 统计录像文件存储大小: startTime={}, endTime={}", startTime, endTime);
        Long totalSize = videoRecordingControlService.sumRecordingFileSize(startTime, endTime);
        return ResponseDTO.ok(totalSize);
    }

    @Operation(summary = "清理过期的已完成录像任务")
    @DeleteMapping("/clean")
    public ResponseDTO<Integer> cleanExpiredTasks(@RequestParam LocalDateTime beforeDate) {
        log.info("[录像控制] 清理过期录像任务: beforeDate={}", beforeDate);
        Integer rows = videoRecordingControlService.cleanExpiredTasks(beforeDate);
        return ResponseDTO.ok(rows);
    }

    @Operation(summary = "检查设备是否正在录像")
    @GetMapping("/device/{deviceId}/recording")
    public ResponseDTO<Boolean> isDeviceRecording(@PathVariable String deviceId) {
        log.info("[录像控制] 检查设备是否正在录像: deviceId={}", deviceId);
        Boolean isRecording = videoRecordingControlService.isDeviceRecording(deviceId);
        return ResponseDTO.ok(isRecording);
    }
}
