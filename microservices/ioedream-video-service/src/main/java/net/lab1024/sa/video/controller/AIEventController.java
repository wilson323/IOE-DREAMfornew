package net.lab1024.sa.video.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.form.AIEventAddForm;
import net.lab1024.sa.video.domain.form.AIEventQueryForm;
import net.lab1024.sa.video.domain.vo.AIEventStatisticsVO;
import net.lab1024.sa.video.domain.vo.AIEventVO;
import net.lab1024.sa.video.service.AIEventService;

/**
 * AI事件管理控制器
 * <p>
 * 提供AI智能分析事件的REST API接口：
 * 1. 事件查询和分页
 * 2. 事件详情获取
 * 3. 事件统计和分析
 * 4. 事件处理操作
 * 5. 批量事件管理
 * 6. 实时事件订阅
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/ai/event")
@Validated
@Tag(name = "AI事件管理", description = "AI智能分析事件管理相关API")
public class AIEventController {

    @Resource
    private AIEventService aiEventService;

    /**
     * 分页查询AI事件列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询AI事件", description = "根据条件分页查询AI智能分析事件列表")
    public ResponseDTO<PageResult<AIEventVO>> pageQueryAIEvents(@Valid AIEventQueryForm queryForm) {
        log.info("[AI事件API] 分页查询AI事件: {}", queryForm);

        PageResult<AIEventVO> pageResult = aiEventService.pageQueryAIEvents(queryForm);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 获取AI事件详情
     */
    @GetMapping("/{eventId}")
    @Operation(summary = "获取AI事件详情", description = "根据事件ID获取AI智能分析事件的详细信息")
    public ResponseDTO<AIEventVO> getAIEventDetail(
            @Parameter(description = "事件ID", required = true) @PathVariable @NotNull Long eventId) {

        log.info("[AI事件API] 获取AI事件详情: eventId={}", eventId);

        AIEventVO eventDetail = aiEventService.getAIEventDetail(eventId);
        if (eventDetail == null) {
            return ResponseDTO.error("EVENT_NOT_FOUND", "AI事件不存在: " + eventId);
        }

        return ResponseDTO.ok(eventDetail);
    }

    /**
     * 手动创建AI事件
     */
    @PostMapping("")
    @Operation(summary = "创建AI事件", description = "手动创建AI智能分析事件")
    public ResponseDTO<Long> createAIEvent(@Valid @RequestBody AIEventAddForm addForm) {
        log.info("[AI事件API] 创建AI事件: {}", addForm);

        Long eventId = aiEventService.createAIEvent(addForm);
        return ResponseDTO.ok(eventId);
    }

    /**
     * 处理AI事件
     */
    @PostMapping("/{eventId}/process")
    @Operation(summary = "处理AI事件", description = "处理指定的AI智能分析事件")
    public ResponseDTO<Void> processAIEvent(
            @Parameter(description = "事件ID", required = true) @PathVariable @NotNull Long eventId,
            @Parameter(description = "处理结果", required = true) @RequestParam @NotNull String processResult) {

        log.info("[AI事件API] 处理AI事件: eventId={}, result={}", eventId, processResult);

        boolean success = aiEventService.processAIEvent(eventId, processResult);
        if (!success) {
            return ResponseDTO.error("PROCESS_FAILED", "AI事件处理失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 批量处理AI事件
     */
    @PostMapping("/batch-process")
    @Operation(summary = "批量处理AI事件", description = "批量处理多个AI智能分析事件")
    public ResponseDTO<Map<Long, Boolean>> batchProcessAIEvents(
            @Parameter(description = "事件ID列表", required = true) @RequestParam @NotNull List<Long> eventIds,
            @Parameter(description = "处理结果", required = true) @RequestParam @NotNull String processResult) {

        log.info("[AI事件API] 批量处理AI事件: eventIds={}, result={}", eventIds, processResult);

        Map<Long, Boolean> processResults = aiEventService.batchProcessAIEvents(eventIds, processResult);
        return ResponseDTO.ok(processResults);
    }

    /**
     * 获取AI事件统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取AI事件统计", description = "获取AI智能分析事件的统计分析数据")
    public ResponseDTO<AIEventStatisticsVO> getAIEventStatistics(
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        log.info("[AI事件API] 获取AI事件统计: startTime={}, endTime={}", startTime, endTime);

        AIEventStatisticsVO statistics = aiEventService.getAIEventStatistics(startTime, endTime);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取实时AI事件列表
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时AI事件", description = "获取最近产生的实时AI智能分析事件")
    public ResponseDTO<List<AIEventVO>> getRealtimeAIEvents(
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType,
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "50") @Min(value = 1, message = "限制数量必须大于0") int limit) {

        log.info("[AI事件API] 获取实时AI事件: eventType={}, limit={}", eventType, limit);

        List<AIEventVO> realtimeEvents = aiEventService.getRealtimeAIEvents(eventType, limit);
        return ResponseDTO.ok(realtimeEvents);
    }

    /**
     * 获取高优先级AI事件
     */
    @GetMapping("/high-priority")
    @Operation(summary = "获取高优先级AI事件", description = "获取需要立即处理的高优先级AI智能分析事件")
    public ResponseDTO<List<AIEventVO>> getHighPriorityAIEvents(
            @Parameter(description = "优先级阈值") @RequestParam(defaultValue = "8") @Min(value = 1, message = "优先级阈值必须大于0") int minPriority) {

        log.info("[AI事件API] 获取高优先级AI事件: minPriority={}", minPriority);

        List<AIEventVO> highPriorityEvents = aiEventService.getHighPriorityAIEvents(minPriority);
        return ResponseDTO.ok(highPriorityEvents);
    }

    /**
     * 异步处理AI事件
     */
    @PostMapping("/{eventId}/async-process")
    @Operation(summary = "异步处理AI事件", description = "异步处理指定的AI智能分析事件")
    public CompletableFuture<ResponseDTO<String>> asyncProcessAIEvent(
            @Parameter(description = "事件ID", required = true) @PathVariable @NotNull Long eventId,
            @Parameter(description = "处理结果", required = true) @RequestParam @NotNull String processResult) {

        log.info("[AI事件API] 异步处理AI事件: eventId={}, result={}", eventId, processResult);

        CompletableFuture<ResponseDTO<String>> future = aiEventService.asyncProcessAIEvent(eventId, processResult)
                .thenApply(success -> {
                    if (success) {
                        return ResponseDTO.ok("AI事件异步处理成功");
                    } else {
                        return ResponseDTO.<String>error("ASYNC_PROCESS_FAILED", "AI事件异步处理失败");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("[AI事件API] AI事件异步处理异常: eventId={}", eventId, throwable);
                    return ResponseDTO.<String>error("ASYNC_PROCESS_ERROR",
                            "AI事件异步处理异常: " + throwable.getMessage());
                });
        return future;
    }

    /**
     * 获取事件趋势分析
     */
    @GetMapping("/trend-analysis")
    @Operation(summary = "获取事件趋势分析", description = "获取AI事件的趋势分析数据")
    public ResponseDTO<Map<String, Object>> getEventTrendAnalysis(
            @Parameter(description = "时间范围(小时)") @RequestParam(defaultValue = "24") @Min(value = 1, message = "时间范围必须大于0") int hours,
            @Parameter(description = "事件类型") @RequestParam(required = false) String eventType) {

        log.info("[AI事件API] 获取事件趋势分析: hours={}, eventType={}", hours, eventType);

        Map<String, Object> trendAnalysis = aiEventService.getEventTrendAnalysis(hours, eventType);
        return ResponseDTO.ok(trendAnalysis);
    }

    /**
     * 获取设备AI事件统计
     */
    @GetMapping("/device/{deviceId}/statistics")
    @Operation(summary = "获取设备AI事件统计", description = "获取指定设备的AI智能分析事件统计")
    public ResponseDTO<Map<String, Object>> getDeviceAIEventStatistics(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @Parameter(description = "开始时间") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) LocalDateTime endTime) {

        log.info("[AI事件API] 获取设备AI事件统计: deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);

        Map<String, Object> deviceStatistics = aiEventService.getDeviceAIEventStatistics(deviceId, startTime, endTime);
        return ResponseDTO.ok(deviceStatistics);
    }

    /**
     * 删除AI事件
     */
    @DeleteMapping("/{eventId}")
    @Operation(summary = "删除AI事件", description = "删除指定的AI智能分析事件")
    public ResponseDTO<Void> deleteAIEvent(
            @Parameter(description = "事件ID", required = true) @PathVariable @NotNull Long eventId) {

        log.info("[AI事件API] 删除AI事件: eventId={}", eventId);

        boolean success = aiEventService.deleteAIEvent(eventId);
        if (!success) {
            return ResponseDTO.error("DELETE_FAILED", "AI事件删除失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 批量删除AI事件
     */
    @DeleteMapping("/batch-delete")
    @Operation(summary = "批量删除AI事件", description = "批量删除多个AI智能分析事件")
    public ResponseDTO<Map<Long, Boolean>> batchDeleteAIEvents(
            @Parameter(description = "事件ID列表", required = true) @RequestParam @NotNull List<Long> eventIds) {

        log.info("[AI事件API] 批量删除AI事件: eventIds={}", eventIds);

        Map<Long, Boolean> deleteResults = aiEventService.batchDeleteAIEvents(eventIds);
        return ResponseDTO.ok(deleteResults);
    }

    /**
     * 获取未处理AI事件数量
     */
    @GetMapping("/unprocessed-count")
    @Operation(summary = "获取未处理AI事件数量", description = "获取当前未处理的AI智能分析事件数量")
    public ResponseDTO<Map<String, Long>> getUnprocessedAIEventCount() {
        log.info("[AI事件API] 获取未处理AI事件数量");

        Map<String, Long> unprocessedCount = aiEventService.getUnprocessedAIEventCount();
        return ResponseDTO.ok(unprocessedCount);
    }

    /**
     * 重新分析AI事件
     */
    @PostMapping("/{eventId}/reanalyze")
    @Operation(summary = "重新分析AI事件", description = "重新分析指定的AI智能分析事件")
    public CompletableFuture<ResponseDTO<String>> reanalyzeAIEvent(
            @Parameter(description = "事件ID", required = true) @PathVariable @NotNull Long eventId) {

        log.info("[AI事件API] 重新分析AI事件: eventId={}", eventId);

        return aiEventService.reanalyzeAIEvent(eventId)
                .thenApply(result -> ResponseDTO.ok("AI事件重新分析完成: " + result))
                .exceptionally(throwable -> {
                    log.error("[AI事件API] AI事件重新分析异常: eventId={}", eventId, throwable);
                    return ResponseDTO.error("REANALYZE_FAILED", "AI事件重新分析失败: " + throwable.getMessage());
                });
    }
}
