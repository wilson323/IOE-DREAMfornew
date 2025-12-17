package net.lab1024.sa.video.controller;

import io.github.resilience4j.annotation.CircuitBreaker;
import io.github.resilience4j.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoDevicePTZService;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 视频设备PTZ控制器
 * <p>
 * 内存优化设计：
 * - 使用异步处理，提高并发性能
 * - 合理的参数验证，避免内存溢出
 * - 熔断器保护，防止级联故障
 * - 批量操作支持，减少网络开销
 * </p>
 * <p>
 * 业务场景：
 * - 云台上下左右移动控制
 * - 变焦控制（放大/缩小）
 * - 预设位管理
 * - 巡航路径控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@PermissionCheck(value = "VIDEO_MANAGE", description = "视频PTZ控制权限")
@RequestMapping("/api/v1/video/ptz")
@Tag(name = "视频PTZ控制", description = "云台控制、变焦控制、预设位管理、巡航控制等API")
@Validated
@CircuitBreaker(name = "videoPTZController")
public class VideoPTZController {

    @Resource
    private VideoDevicePTZService ptzService;

    // ==================== 云台移动控制 ====================

    /**
     * 云台上移
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "云台上移",
        description = "控制云台向上移动"
    )
    @PostMapping("/{deviceId}/up")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> ptzUp(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "移动速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 云台上移, deviceId={}, speed={}", deviceId, speed);
        return ptzService.ptzUp(deviceId, speed);
    }

    /**
     * 云台下移
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "云台下移",
        description = "控制云台向下移动"
    )
    @PostMapping("/{deviceId}/down")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> ptzDown(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "移动速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 云台下移, deviceId={}, speed={}", deviceId, speed);
        return ptzService.ptzDown(deviceId, speed);
    }

    /**
     * 云台左移
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "云台左移",
        description = "控制云台向左移动"
    )
    @PostMapping("/{deviceId}/left")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> ptzLeft(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "移动速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 云台左移, deviceId={}, speed={}", deviceId, speed);
        return ptzService.ptzLeft(deviceId, speed);
    }

    /**
     * 云台右移
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "云台右移",
        description = "控制云台向右移动"
    )
    @PostMapping("/{deviceId}/right")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> ptzRight(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "移动速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 云台右移, deviceId={}, speed={}", deviceId, speed);
        return ptzService.ptzRight(deviceId, speed);
    }

    /**
     * 云台停止移动
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "云台停止移动",
        description = "停止云台当前移动"
    )
    @PostMapping("/{deviceId}/stop")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> ptzStop(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 云台停止移动, deviceId={}", deviceId);
        return ptzService.ptzStop(deviceId);
    }

    // ==================== 变焦控制 ====================

    /**
     * 放大变焦
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "放大变焦",
        description = "控制镜头放大变焦"
    )
    @PostMapping("/{deviceId}/zoom-in")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> zoomIn(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "变焦速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 放大变焦, deviceId={}, speed={}", deviceId, speed);
        return ptzService.zoomIn(deviceId, speed);
    }

    /**
     * 缩小变焦
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "缩小变焦",
        description = "控制镜头缩小变焦"
    )
    @PostMapping("/{deviceId}/zoom-out")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> zoomOut(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "变焦速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 缩小变焦, deviceId={}, speed={}", deviceId, speed);
        return ptzService.zoomOut(deviceId, speed);
    }

    /**
     * 停止变焦
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "停止变焦",
        description = "停止当前变焦操作"
    )
    @PostMapping("/{deviceId}/zoom-stop")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> zoomStop(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 停止变焦, deviceId={}", deviceId);
        return ptzService.zoomStop(deviceId);
    }

    // ==================== 预设位管理 ====================

    /**
     * 设置预设位
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "设置预设位",
        description = "将当前位置设置为预设位"
    )
    @PostMapping("/{deviceId}/preset")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "PTZ管理操作")
    public CompletableFuture<ResponseDTO<Void>> setPreset(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "预设位编号(1-255)", required = true, example = "1")
            @RequestParam @Min(1) @Max(255) Integer presetId,
            @Parameter(description = "预设位名称", example = "入口位置")
            @RequestParam(required = false) String presetName
    ) {
        log.info("[PTZ控制] 设置预设位, deviceId={}, presetId={}, presetName={}", deviceId, presetId, presetName);
        return ptzService.setPreset(deviceId, presetId, presetName);
    }

    /**
     * 调用预设位
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "调用预设位",
        description = "移动云台到指定预设位"
    )
    @PostMapping("/{deviceId}/preset/{presetId}/call")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> callPreset(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "预设位编号", required = true, example = "1")
            @PathVariable Integer presetId,
            @Parameter(description = "移动速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed
    ) {
        log.info("[PTZ控制] 调用预设位, deviceId={}, presetId={}, speed={}", deviceId, presetId, speed);
        return ptzService.callPreset(deviceId, presetId, speed);
    }

    /**
     * 删除预设位
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "删除预设位",
        description = "删除指定的预设位"
    )
    @DeleteMapping("/{deviceId}/preset/{presetId}")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "PTZ管理操作")
    public CompletableFuture<ResponseDTO<Void>> deletePreset(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "预设位编号", required = true, example = "1")
            @PathVariable Integer presetId
    ) {
        log.info("[PTZ控制] 删除预设位, deviceId={}, presetId={}", deviceId, presetId);
        return ptzService.deletePreset(deviceId, presetId);
    }

    /**
     * 获取预设位列表
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "获取预设位列表",
        description = "查询设备所有预设位信息"
    )
    @GetMapping("/{deviceId}/presets")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ查看权限")
    public CompletableFuture<ResponseDTO<Object>> getPresetList(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 获取预设位列表, deviceId={}", deviceId);
        return ptzService.getPresetList(deviceId);
    }

    // ==================== 巡航控制 ====================

    /**
     * 开始巡航
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "开始巡航",
        description = "开始按预设位巡航"
    )
    @PostMapping("/{deviceId}/patrol/start")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> startPatrol(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "预设位列表，用逗号分隔", example = "1,2,3")
            @RequestParam(required = false) String presetIds,
            @Parameter(description = "巡航速度(1-10)", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(10) Integer speed,
            @Parameter(description = "停留时间(秒)", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(60) Integer dwellTime
    ) {
        log.info("[PTZ控制] 开始巡航, deviceId={}, presetIds={}, speed={}, dwellTime={}", deviceId, presetIds, speed, dwellTime);
        return ptzService.startPatrol(deviceId, presetIds, speed, dwellTime);
    }

    /**
     * 停止巡航
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "停止巡航",
        description = "停止当前巡航"
    )
    @PostMapping("/{deviceId}/patrol/stop")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> stopPatrol(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 停止巡航, deviceId={}", deviceId);
        return ptzService.stopPatrol(deviceId);
    }

    /**
     * 暂停巡航
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "暂停巡航",
        description = "暂停当前巡航，可以恢复"
    )
    @PostMapping("/{deviceId}/patrol/pause")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> pausePatrol(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 暂停巡航, deviceId={}", deviceId);
        return ptzService.pausePatrol(deviceId);
    }

    /**
     * 恢复巡航
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "恢复巡航",
        description = "恢复暂停的巡航"
    )
    @PostMapping("/{deviceId}/patrol/resume")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> resumePatrol(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 恢复巡航, deviceId={}", deviceId);
        return ptzService.resumePatrol(deviceId);
    }

    // ==================== 状态查询 ====================

    /**
     * 获取PTZ状态
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "获取PTZ状态",
        description = "查询设备PTZ功能当前状态"
    )
    @GetMapping("/{deviceId}/status")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ查看权限")
    public CompletableFuture<ResponseDTO<Object>> getPTZStatus(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 获取PTZ状态, deviceId={}", deviceId);
        return ptzService.getPTZStatus(deviceId);
    }

    /**
     * 获取当前位置
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "获取当前位置",
        description = "查询云台当前的位置坐标"
    )
    @GetMapping("/{deviceId}/position")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ查看权限")
    public CompletableFuture<ResponseDTO<Object>> getCurrentPosition(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 获取当前位置, deviceId={}", deviceId);
        return ptzService.getCurrentPosition(deviceId);
    }

    /**
     * 重置PTZ
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "重置PTZ",
        description = "重置PTZ到初始状态"
    )
    @PostMapping("/{deviceId}/reset")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "PTZ管理操作")
    public CompletableFuture<ResponseDTO<Void>> resetPTZ(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 重置PTZ, deviceId={}", deviceId);
        return ptzService.resetPTZ(deviceId);
    }

    /**
     * 自动校准
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "自动校准",
        description = "自动校准PTZ位置"
    )
    @PostMapping("/{deviceId}/calibrate")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "PTZ管理操作")
    public CompletableFuture<ResponseDTO<Map<String, Object>>> autoCalibrate(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 自动校准, deviceId={}", deviceId);
        return ptzService.autoCalibrate(deviceId);
    }

    // ==================== 3D定位控制 ====================

    /**
     * 3D定位控制
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "3D定位控制",
        description = "通过点击画面控制云台定位到指定位置"
    )
    @PostMapping("/{deviceId}/3d-position")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ操作权限")
    public CompletableFuture<ResponseDTO<Void>> setPosition3D(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId,
            @Parameter(description = "X坐标", required = true, example = "100")
            @RequestParam @NotNull Integer x,
            @Parameter(description = "Y坐标", required = true, example = "100")
            @RequestParam @NotNull Integer y,
            @Parameter(description = "缩放级别", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) @Max(10) Integer zoom
    ) {
        log.info("[PTZ控制] 3D定位控制, deviceId={}, x={}, y={}, zoom={}", deviceId, x, y, zoom);
        return ptzService.setPosition3D(deviceId, x, y, zoom);
    }

    // ==================== 辅助功能 ====================

    /**
     * 获取PTZ能力
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "获取PTZ能力",
        description = "查询设备支持的PTZ功能"
    )
    @GetMapping("/{deviceId}/capabilities")
    @PermissionCheck(value = {"VIDEO_USE", "VIDEO_MANAGE"}, description = "PTZ查看权限")
    public CompletableFuture<ResponseDTO<Object>> getPTZCapabilities(
            @Parameter(description = "设备ID", required = true, example = "1001")
            @PathVariable Long deviceId
    ) {
        log.info("[PTZ控制] 获取PTZ能力, deviceId={}", deviceId);
        return ptzService.getPTZCapabilities(deviceId);
    }

    /**
     * 批量PTZ控制
     */
    @TimeLimiter(name = "videoPTZController")
    @Operation(
        summary = "批量PTZ控制",
        description = "同时控制多个设备的PTZ"
    )
    @PostMapping("/batch-control")
    @PermissionCheck(value = "VIDEO_MANAGE", description = "PTZ管理操作")
    public CompletableFuture<ResponseDTO<Map<Long, Object>>> batchPTZControl(
            @Parameter(description = "设备ID列表", required = true)
            @RequestParam String deviceIds,
            @Parameter(description = "控制命令", required = true, example = "up")
            @RequestParam String command,
            @Parameter(description = "控制参数", example = "speed=5")
            @RequestParam(required = false) Map<String, Object> params
    ) {
        log.info("[PTZ控制] 批量PTZ控制, deviceIds={}, command={}, params={}", deviceIds, command, params);
        return ptzService.batchPTZControl(deviceIds, command, params);
    }
}
