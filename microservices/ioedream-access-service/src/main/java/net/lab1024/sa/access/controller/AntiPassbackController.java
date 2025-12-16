package net.lab1024.sa.access.controller;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.response.ResponseDTO;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 门禁反潜回控制器
 * <p>
 * 内存优化设计：
 * - 使用异步处理，提高并发性能
 * - 合理的参数验证，避免内存溢出
 * - 熔断器保护，防止级联故障
 * - 批量操作支持，减少网络开销
 * </p>
 * <p>
 * 业务场景：
 * - 硬反潜回：防止重复刷卡进入
 * - 软反潜回：允许重复刷卡但记录异常
 * - 区域反潜回：防止绕行进入
 * - 全局反潜回：跨区域联动检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "ACCESS_MANAGE", description = "门禁反潜回模块权限")
@RequestMapping("/api/v1/access/anti-passback")
@Tag(name = "门禁反潜回", description = "反潜回检查、配置管理、异常处理、统计分析等API")
@Validated
@CircuitBreaker(name = "antiPassbackController")
public class AntiPassbackController {

    @Resource
    private AntiPassbackService antiPassbackService;

    // ==================== 反潜回验证 ====================

    /**
     * 执行反潜回检查
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "执行反潜回检查",
        description = "检查用户是否存在潜回行为，支持多种反潜回模式"
    )
    @PostMapping("/check")
    @PermissionCheck(value = {"ACCESS_OPERATOR", "ACCESS_MANAGER"}, description = "执行反潜回检查")
    public CompletableFuture<ResponseDTO<AntiPassbackService.AntiPassbackResult>> performAntiPassbackCheck(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "区域ID", required = true, example = "1001")
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "验证数据", example = "{\"cardId\":\"CARD001\",\"faceData\":\"...\"}")
            @RequestParam @NotNull String verificationData
    ) {
        log.info("[反潜回] 执行反潜回检查, userId={}, deviceId={}, areaId={}", userId, deviceId, areaId);
        return antiPassbackService.performAntiPassbackCheck(userId, deviceId, areaId, verificationData);
    }

    /**
     * 检查区域反潜回
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "检查区域反潜回",
        description = "检查用户是否从区域A进入后未从区域B离开"
    )
    @PostMapping("/area-check")
    @PermissionCheck(value = {"ACCESS_OPERATOR", "ACCESS_MANAGER"}, description = "检查区域反潜回")
    public CompletableFuture<ResponseDTO<AntiPassbackService.AntiPassbackResult>> checkAreaAntiPassback(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "进入区域ID", required = true, example = "1001")
            @RequestParam @NotNull Long entryAreaId,
            @Parameter(description = "离开区域ID", required = true, example = "1002")
            @RequestParam @NotNull Long exitAreaId,
            @Parameter(description = "进出方向", required = true, example = "in")
            @RequestParam @NotNull String direction
    ) {
        log.info("[反潜回] 检查区域反潜回, userId={}, entryAreaId={}, exitAreaId={}, direction={}",
                 userId, entryAreaId, exitAreaId, direction);
        return antiPassbackService.checkAreaAntiPassback(userId, entryAreaId, exitAreaId, direction);
    }

    // ==================== 反潜回配置管理 ====================

    /**
     * 设置设备反潜回策略
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "设置设备反潜回策略",
        description = "为设备配置反潜回策略和参数"
    )
    @PostMapping("/{deviceId}/policy")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Void>> setAntiPassbackPolicy(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "反潜回类型", required = true, example = "hard")
            @RequestParam @NotNull String antiPassbackType,
            @Parameter(description = "配置参数", required = true)
            @RequestBody @NotNull Map<String, Object> config
    ) {
        log.info("[反潜回] 设置设备反潜回策略, deviceId={}, antiPassbackType={}, config={}",
                 deviceId, antiPassbackType, config);
        return antiPassbackService.setAntiPassbackPolicy(deviceId, antiPassbackType, config);
    }

    /**
     * 获取设备反潜回策略
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "获取设备反潜回策略",
        description = "查询设备的反潜回策略配置"
    )
    @GetMapping("/{deviceId}/policy")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "管理或操作权限")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackPolicy(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[反潜回] 获取设备反潜回策略, deviceId={}", deviceId);
        return antiPassbackService.getAntiPassbackPolicy(deviceId);
    }

    /**
     * 更新反潜回配置
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "更新反潜回配置",
        description = "更新设备的反潜回配置参数"
    )
    @PutMapping("/{deviceId}/config")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Void>> updateAntiPassbackConfig(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "配置参数", required = true)
            @RequestBody @NotNull Map<String, Object> config
    ) {
        log.info("[反潜回] 更新反潜回配置, deviceId={}, config={}", deviceId, config);
        return antiPassbackService.updateAntiPassbackConfig(deviceId, config);
    }

    // ==================== 反潜回记录管理 ====================

    /**
     * 记录通行事件
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "记录通行事件",
        description = "记录用户通行事件，用于反潜回分析"
    )
    @PostMapping("/record-access")
    @PreAuthorize("hasRole('ACCESS_OPERATOR') or hasRole('ACCESS_MANAGER')")
    public CompletableFuture<ResponseDTO<Void>> recordAccessEvent(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "区域ID", required = true, example = "1001")
            @RequestParam @NotNull Long areaId,
            @Parameter(description = "进出方向", required = true, example = "in")
            @RequestParam @NotNull String direction,
            @Parameter(description = "验证数据", required = true)
            @RequestParam @NotNull String verificationData,
            @Parameter(description = "通行结果", required = true, example = "true")
            @RequestParam @NotNull Boolean result
    ) {
        log.info("[反潜回] 记录通行事件, userId={}, deviceId={}, areaId={}, direction={}, result={}",
                 userId, deviceId, areaId, direction, result);
        return antiPassbackService.recordAccessEvent(userId, deviceId, areaId, direction, verificationData, result);
    }

    /**
     * 获取用户反潜回状态
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "获取用户反潜回状态",
        description = "查询用户的反潜回状态信息"
    )
    @GetMapping("/user/{userId}/status")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "管理或操作权限")
    public CompletableFuture<ResponseDTO<Object>> getUserAntiPassbackStatus(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId
    ) {
        log.info("[反潜回] 获取用户反潜回状态, userId={}", userId);
        return antiPassbackService.getUserAntiPassbackStatus(userId);
    }

    /**
     * 清理用户反潜回记录
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "清理用户反潜回记录",
        description = "清理用户的反潜回记录，通常在正常完成进出流程后调用"
    )
    @DeleteMapping("/user/{userId}/records")
    @PreAuthorize("hasRole('ACCESS_OPERATOR') or hasRole('ACCESS_MANAGER')")
    public CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "设备ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId
    ) {
        log.info("[反潜回] 清理用户反潜回记录, userId={}, deviceId={}", userId, deviceId);
        return antiPassbackService.clearUserAntiPassbackRecords(userId, deviceId);
    }

    // ==================== 反潜回异常处理 ====================

    /**
     * 重置用户反潜回状态
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "重置用户反潜回状态",
        description = "管理员手动重置用户的反潜回状态，用于解决异常情况或误报"
    )
    @PostMapping("/user/{userId}/reset")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Void>> resetUserAntiPassbackStatus(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "操作员ID", required = true, example = "1001")
            @RequestParam @NotNull Long operatorId,
            @Parameter(description = "重置原因", required = true, example = "误报异常")
            @RequestParam @NotNull String reason
    ) {
        log.info("[反潜回] 重置用户反潜回状态, userId={}, operatorId={}, reason={}", userId, operatorId, reason);
        return antiPassbackService.resetUserAntiPassbackStatus(userId, operatorId, reason);
    }

    /**
     * 手动处理反潜回异常
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "手动处理反潜回异常",
        description = "管理员手动处理检测到的反潜回异常"
    )
    @PostMapping("/violation/handle")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Void>> handleAntiPassbackViolation(
            @Parameter(description = "用户ID", required = true, example = "1001")
            @RequestParam @NotNull Long userId,
            @Parameter(description = "设备ID", required = true, example = "1001")
            @RequestParam @NotNull Long deviceId,
            @Parameter(description = "处理方式", required = true, example = "ignore")
            @RequestParam @NotNull String action,
            @Parameter(description = "处理备注")
            @RequestParam(required = false) String remarks
    ) {
        log.info("[反潜回] 手动处理反潜回异常, userId={}, deviceId={}, action={}", userId, deviceId, action);

        AntiPassbackService.AntiPassbackResult result = new AntiPassbackService.AntiPassbackResult();
        result.setPassed(false);
        result.setDenyReason("Manual violation handling: " + action);
        result.setViolationLevel("MANUAL");
        result.setRecommendedAction(action);

        return antiPassbackService.handleAntiPassbackViolation(userId, deviceId, result);
    }

    // ==================== 反潜回统计分析 ====================

    /**
     * 获取反潜回统计信息
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "获取反潜回统计信息",
        description = "获取反潜回系统的统计数据和分析报告"
    )
    @GetMapping("/statistics")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackStatistics(
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[反潜回] 获取反潜回统计信息, startTime={}, endTime={}", startTime, endTime);
        return antiPassbackService.getAntiPassbackStatistics(startTime, endTime);
    }

    /**
     * 获取反潜回异常报告
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "获取反潜回异常报告",
        description = "查询指定时间段内的反潜回异常报告"
    )
    @GetMapping("/violation-report")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Object>> getAntiPassbackViolationReport(
            @Parameter(description = "设备ID", example = "1001")
            @RequestParam(required = false) Long deviceId,
            @Parameter(description = "开始时间", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) String endTime
    ) {
        log.info("[反潜回] 获取反潜回异常报告, deviceId={}, startTime={}, endTime={}", deviceId, startTime, endTime);
        return antiPassbackService.getAntiPassbackViolationReport(deviceId, startTime, endTime);
    }

    // ==================== 批量操作 ====================

    /**
     * 批量检查反潜回状态
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "批量检查反潜回状态",
        description = "同时检查多个用户的反潜回状态"
    )
    @PostMapping("/batch-check")
    @PermissionCheck(value = {"ACCESS_MANAGER", "ACCESS_OPERATOR"}, description = "管理或操作权限")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchCheckAntiPassbackStatus(
            @Parameter(description = "用户ID列表，用逗号分隔", required = true)
            @RequestParam @NotNull String userIds
    ) {
        log.info("[反潜回] 批量检查反潜回状态, userIds={}", userIds);
        return antiPassbackService.batchCheckAntiPassbackStatus(userIds);
    }

    /**
     * 批量清理反潜回记录
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "批量清理反潜回记录",
        description = "同时清理多个用户的反潜回记录"
    )
    @DeleteMapping("/batch-clear")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchClearAntiPassbackRecords(
            @Parameter(description = "用户ID列表，用逗号分隔", required = true)
            @RequestParam @NotNull String userIds
    ) {
        log.info("[反潜回] 批量清理反潜回记录, userIds={}", userIds);
        return antiPassbackService.batchClearAntiPassbackRecords(userIds);
    }

    // ==================== 配置模板 ====================

    /**
     * 获取反潜回配置模板
     */
    @TimeLimiter(name = "antiPassbackController")
    @Operation(
        summary = "获取反潜回配置模板",
        description = "获取各种反潜回类型的配置模板"
    )
    @GetMapping("/config-templates")
    @PermissionCheck(value = "ACCESS_MANAGE", description = "管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> getAntiPassbackConfigTemplates() {
        log.info("[反潜回] 获取反潜回配置模板");

        Map<String, Object> templates = Map.of(
            "hard", Map.of(
                "type", "hard",
                "description", "硬反潜回：完全禁止重复进入",
                "config", Map.of(
                    "enableDuplicateCheck", true,
                    "maxDuplicateInterval", 30,
                    "strictMode", true,
                    "autoLockDuration", 300
                )
            ),
            "soft", Map.of(
                "type", "soft",
                "description", "软反潜回：允许重复但记录异常",
                "config", Map.of(
                    "enableDuplicateCheck", true,
                    "maxDuplicateInterval", 60,
                    "strictMode", false,
                    "warningThreshold", 3
                )
            ),
            "area", Map.of(
                "type", "area",
                "description", "区域反潜回：区域进出配对检查",
                "config", Map.of(
                    "enableAreaPairCheck", true,
                    "allowAreaCrossing", false,
                    "pairTimeout", 3600
                )
            ),
            "global", Map.of(
                "type", "global",
                "description", "全局反潜回：跨区域联动检查",
                "config", Map.of(
                    "enableGlobalCheck", true,
                    "globalTimeout", 7200,
                    "syncAcrossDevices", true
                )
            )
        );

        return CompletableFuture.completedFuture(ResponseDTO.ok(templates));
    }
}