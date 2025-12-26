package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.video.entity.DeviceHealthEntity;
import net.lab1024.sa.video.service.DeviceHealthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备健康检查控制器
 * <p>
 * 提供视频设备健康检查相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备健康度评分
 * - 健康指标监控
 * - 告警记录查询
 * - 健康趋势分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/device/health")
@Tag(name = "设备健康检查管理", description = "设备健康评分、指标监控、告警记录API")
@PermissionCheck(value = "VIDEO_DEVICE", description = "视频设备管理模块权限")
public class DeviceHealthController {

    @Resource
    private DeviceHealthService deviceHealthService;

    /**
     * 执行设备健康检查
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    @PostMapping("/{deviceId}/check")
    @Operation(summary = "执行设备健康检查", description = "对指定设备执行健康检查并返回评分结果")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<DeviceHealthEntity> performHealthCheck(@PathVariable @NotNull Long deviceId) {
        log.info("[设备健康] 执行健康检查: deviceId={}", deviceId);

        try {
            DeviceHealthEntity health = deviceHealthService.performHealthCheck(deviceId);
            return ResponseDTO.ok(health);
        } catch (Exception e) {
            log.error("[设备健康] 执行健康检查异常", e);
            return ResponseDTO.error("PERFORM_HEALTH_CHECK_ERROR", "执行健康检查失败");
        }
    }

    /**
     * 获取设备最新健康状态
     *
     * @param deviceId 设备ID
     * @return 健康状态
     */
    @GetMapping("/{deviceId}/latest")
    @Operation(summary = "获取设备最新健康状态", description = "查询指定设备的最新健康状态和评分")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<DeviceHealthEntity> getLatestHealth(@PathVariable @NotNull Long deviceId) {
        log.info("[设备健康] 查询最新健康状态: deviceId={}", deviceId);

        try {
            DeviceHealthEntity health = deviceHealthService.getLatestHealth(deviceId);
            return ResponseDTO.ok(health);
        } catch (Exception e) {
            log.error("[设备健康] 查询最新健康状态异常", e);
            return ResponseDTO.error("GET_LATEST_HEALTH_ERROR", "查询最新健康状态失败");
        }
    }

    /**
     * 获取设备健康历史
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 健康历史记录
     */
    @GetMapping("/{deviceId}/history")
    @Operation(summary = "获取设备健康历史", description = "查询指定设备的健康检查历史记录")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<DeviceHealthEntity>> getHealthHistory(
            @PathVariable @NotNull Long deviceId,
            @RequestParam(defaultValue = "100") Integer limit) {
        log.info("[设备健康] 查询健康历史: deviceId={}, limit={}", deviceId, limit);

        try {
            List<DeviceHealthEntity> history = deviceHealthService.getHealthHistory(deviceId, limit);
            return ResponseDTO.ok(history);
        } catch (Exception e) {
            log.error("[设备健康] 查询健康历史异常", e);
            return ResponseDTO.error("GET_HEALTH_HISTORY_ERROR", "查询健康历史失败");
        }
    }

    /**
     * 获取健康统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取健康统计信息", description = "获取所有设备的健康统计、健康率等数据")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<Map<String, Object>> getHealthStatistics() {
        log.info("[设备健康] 查询健康统计信息");

        try {
            Map<String, Object> statistics = deviceHealthService.getHealthStatistics();
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[设备健康] 查询健康统计异常", e);
            return ResponseDTO.error("GET_HEALTH_STATISTICS_ERROR", "获取健康统计信息失败");
        }
    }

    /**
     * 获取告警记录
     *
     * @param alarmLevel 告警级别
     * @return 告警记录列表
     */
    @GetMapping("/alarms")
    @Operation(summary = "获取告警记录", description = "查询指定级别的设备健康告警记录")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<DeviceHealthEntity>> getAlarmRecords(
            @RequestParam(required = false, defaultValue = "1") Integer alarmLevel) {
        log.info("[设备健康] 查询告警记录: alarmLevel={}", alarmLevel);

        try {
            List<DeviceHealthEntity> alarms = deviceHealthService.getAlarmRecords(alarmLevel);
            return ResponseDTO.ok(alarms);
        } catch (Exception e) {
            log.error("[设备健康] 查询告警记录异常", e);
            return ResponseDTO.error("GET_ALARM_RECORDS_ERROR", "查询告警记录失败");
        }
    }

    /**
     * 批量执行健康检查
     *
     * @param deviceIds 设备ID列表
     * @return 健康检查结果列表
     */
    @PostMapping("/batch")
    @Operation(summary = "批量执行健康检查", description = "对多个设备批量执行健康检查")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<List<DeviceHealthEntity>> batchPerformHealthCheck(
            @RequestBody @NotNull List<Long> deviceIds) {
        log.info("[设备健康] 批量执行健康检查: deviceCount={}", deviceIds.size());

        try {
            List<DeviceHealthEntity> results = deviceHealthService.batchPerformHealthCheck(deviceIds);
            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("[设备健康] 批量执行健康检查异常", e);
            return ResponseDTO.error("BATCH_HEALTH_CHECK_ERROR", "批量执行健康检查失败");
        }
    }

    /**
     * 分页查询健康记录
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param deviceId 设备ID（可选）
     * @param healthStatus 健康状态（可选）
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询健康记录", description = "根据条件分页查询设备健康检查记录")
    @PermissionCheck(value = "VIDEO_MANAGER", description = "视频管理权限")
    public ResponseDTO<PageResult<DeviceHealthEntity>> queryPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer healthStatus) {
        log.info("[设备健康] 分页查询健康记录: pageNum={}, pageSize={}, deviceId={}, status={}",
                pageNum, pageSize, deviceId, healthStatus);

        try {
            PageResult<DeviceHealthEntity> pageResult = deviceHealthService.queryPage(
                    pageNum, pageSize, deviceId, healthStatus);
            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("[设备健康] 分页查询异常", e);
            return ResponseDTO.error("QUERY_HEALTH_RECORDS_ERROR", "分页查询健康记录失败");
        }
    }
}
