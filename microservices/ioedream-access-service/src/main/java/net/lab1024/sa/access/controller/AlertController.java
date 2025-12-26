package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.DeviceAlertHandleForm;
import net.lab1024.sa.access.domain.form.DeviceAlertQueryForm;
import net.lab1024.sa.access.domain.entity.DeviceAlertEntity;
import net.lab1024.sa.access.domain.vo.AlertStatisticsVO;
import net.lab1024.sa.access.domain.vo.DeviceAlertVO;
import net.lab1024.sa.access.manager.AlertManager;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警管理控制器
 * <p>
 * 提供设备告警的REST API接口：
 * - 告警查询（分页、筛选）
 * - 告警处理（确认、处理、忽略）
 * - 告警统计
 * - 批量操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/alerts")
@Tag(name = "告警管理", description = "设备告警管理相关接口")
@Validated
public class AlertController {

    @Resource
    private AlertManager alertManager;

    /**
     * 手动创建告警
     */
    @Operation(summary = "手动创建告警", description = "手动创建设备告警记录")
    @PostMapping
    public ResponseDTO<Long> createAlert(@RequestBody DeviceAlertEntity alert) {
        log.info("[告警管理] 手动创建告警: deviceId={}, alertType={}", alert.getDeviceId(), alert.getAlertType());

        ResponseDTO<Long> response = alertManager.createAlert(alert);

        if (response.getData() != null) {
            log.info("[告警管理] 告警创建成功: alertId={}", response.getData());
        }

        return response;
    }

    /**
     * 检测并创建告警（根据设备状态）
     */
    @Operation(summary = "检测并创建告警", description = "根据设备状态和告警规则，自动检测是否需要创建告警")
    @PostMapping("/detect")
    public ResponseDTO<Boolean> detectAndCreateAlert(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId,
            @Parameter(description = "设备状态", required = true) @RequestParam String deviceStatus,
            @Parameter(description = "设备数据（JSON格式）") @RequestParam(required = false) Map<String, Object> deviceData) {
        log.info("[告警管理] 检测并创建告警: deviceId={}, deviceStatus={}", deviceId, deviceStatus);

        ResponseDTO<Boolean> response = alertManager.detectAndCreateAlert(deviceId, deviceStatus, deviceData);

        log.info("[告警管理] 告警检测完成: deviceId={}, alertCreated={}", deviceId, response.getData());

        return response;
    }

    /**
     * 分页查询告警列表
     */
    @Operation(summary = "分页查询告警列表", description = "支持多条件筛选的分页查询")
    @GetMapping
    public ResponseDTO<PageResult<DeviceAlertVO>> queryAlerts(DeviceAlertQueryForm queryForm) {
        log.info("[告警管理] 查询告警列表: alertType={}, alertLevel={}, alertStatus={}, pageNum={}, pageSize={}",
                queryForm.getAlertType(), queryForm.getAlertLevel(), queryForm.getAlertStatus(),
                queryForm.getPageNum(), queryForm.getPageSize());

        ResponseDTO<PageResult<DeviceAlertVO>> response = alertManager.queryAlerts(queryForm);

        if (response.getData() != null) {
            log.info("[告警管理] 查询成功: total={}", response.getData().getTotal());
        }

        return response;
    }

    /**
     * 查询告警详情
     */
    @Operation(summary = "查询告警详情", description = "根据告警ID查询详细信息")
    @GetMapping("/{alertId}")
    public ResponseDTO<DeviceAlertVO> getAlertDetail(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alertId) {
        log.info("[告警管理] 查询告警详情: alertId={}", alertId);

        ResponseDTO<DeviceAlertVO> response = alertManager.getAlertDetail(alertId);

        if (response.getData() != null) {
            log.info("[告警管理] 查询成功: alertType={}, alertLevel={}",
                    response.getData().getAlertType(), response.getData().getAlertLevel());
        }

        return response;
    }

    /**
     * 处理告警（确认、处理、忽略）
     */
    @Operation(summary = "处理告警", description = "对告警进行确认、处理或忽略操作")
    @PutMapping("/{alertId}/handle")
    public ResponseDTO<Void> handleAlert(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alertId,
            @RequestBody DeviceAlertHandleForm handleForm) {
        log.info("[告警管理] 处理告警: alertId={}, actionType={}, remark={}",
                alertId, handleForm.getActionType(), handleForm.getRemark());

        // 设置告警ID
        handleForm.setAlertId(alertId);

        ResponseDTO<Void> response = alertManager.handleAlert(handleForm);

        if (response.getCode() == 200) {
            log.info("[告警管理] 告警处理成功: alertId={}", alertId);
        }

        return response;
    }

    /**
     * 批量确认告警
     */
    @Operation(summary = "批量确认告警", description = "批量确认多个告警")
    @PostMapping("/batch-confirm")
    public ResponseDTO<Integer> batchConfirmAlerts(
            @Parameter(description = "告警ID列表", required = true) @RequestBody List<Long> alertIds,
            @Parameter(description = "确认备注") @RequestParam(required = false) String remark) {
        log.info("[告警管理] 批量确认告警: count={}", alertIds.size());

        ResponseDTO<Integer> response = alertManager.batchConfirmAlerts(alertIds, remark);

        log.info("[告警管理] 批量确认完成: total={}, success={}", alertIds.size(), response.getData());

        return response;
    }

    /**
     * 批量处理告警
     */
    @Operation(summary = "批量处理告警", description = "批量处理多个告警")
    @PostMapping("/batch-handle")
    public ResponseDTO<Integer> batchHandleAlerts(
            @Parameter(description = "告警ID列表", required = true) @RequestBody List<Long> alertIds,
            @Parameter(description = "处理结果", required = true) @RequestParam String result) {
        log.info("[告警管理] 批量处理告警: count={}, result={}", alertIds.size(), result);

        ResponseDTO<Integer> response = alertManager.batchHandleAlerts(alertIds, result);

        log.info("[告警管理] 批量处理完成: total={}, success={}", alertIds.size(), response.getData());

        return response;
    }

    /**
     * 获取告警统计数据
     */
    @Operation(summary = "获取告警统计数据", description = "获取告警统计信息，用于仪表盘展示")
    @GetMapping("/statistics")
    public ResponseDTO<AlertStatisticsVO> getAlertStatistics() {
        log.info("[告警管理] 获取告警统计数据");

        ResponseDTO<AlertStatisticsVO> response = alertManager.getAlertStatistics();

        if (response.getData() != null) {
            log.info("[告警管理] 统计数据获取成功: totalAlerts={}, unconfirmedAlerts={}, criticalAlerts={}",
                    response.getData().getTotalAlerts(),
                    response.getData().getUnconfirmedAlerts(),
                    response.getData().getCriticalAlerts());
        }

        return response;
    }

    /**
     * 查询紧急告警（需要立即处理的告警）
     */
    @Operation(summary = "查询紧急告警", description = "查询紧急级别且未处理的告警列表")
    @GetMapping("/critical")
    public ResponseDTO<List<DeviceAlertVO>> queryCriticalAlerts(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("[告警管理] 查询紧急告警: limit={}", limit);

        ResponseDTO<List<DeviceAlertVO>> response = alertManager.queryCriticalAlerts(limit);

        if (response.getData() != null) {
            log.info("[告警管理] 紧急告警查询成功: count={}", response.getData().size());
        }

        return response;
    }

    /**
     * 获取告警趋势数据（最近7天）
     */
    @Operation(summary = "获取告警趋势", description = "获取最近7天的告警趋势数据")
    @GetMapping("/trend")
    public ResponseDTO<Map<String, Long>> getAlertTrend() {
        log.info("[告警管理] 获取告警趋势数据");

        ResponseDTO<Map<String, Long>> response = alertManager.getAlertTrend();

        if (response.getData() != null) {
            log.info("[告警管理] 趋势数据获取成功: dataPoints={}", response.getData().size());
        }

        return response;
    }

    /**
     * 清理过期告警记录
     */
    @Operation(summary = "清理过期告警", description = "清理指定天数之前的已处理告警记录")
    @DeleteMapping("/cleanup")
    public ResponseDTO<Integer> cleanupExpiredAlerts(
            @Parameter(description = "保留天数", required = true) @RequestParam Integer daysToKeep) {
        log.info("[告警管理] 清理过期告警: daysToKeep={}", daysToKeep);

        if (daysToKeep < 30) {
            log.warn("[告警管理] 保留天数过少，可能误删数据: daysToKeep={}", daysToKeep);
            return ResponseDTO.error("DAYS_TOO_FEW", "保留天数不能少于30天");
        }

        ResponseDTO<Integer> response = alertManager.cleanupExpiredAlerts(daysToKeep);

        log.info("[告警管理] 过期告警清理完成: deletedCount={}", response.getData());

        return response;
    }

    /**
     * 获取设备未确认告警数量
     */
    @Operation(summary = "获取设备未确认告警数量", description = "查询指定设备的未确认告警数量")
    @GetMapping("/unconfirmed-count")
    public ResponseDTO<Long> getUnconfirmedCount(
            @Parameter(description = "设备ID", required = true) @RequestParam Long deviceId) {
        log.info("[告警管理] 查询设备未确认告警数量: deviceId={}", deviceId);

        // 通过查询表单获取数量
        DeviceAlertQueryForm queryForm = new DeviceAlertQueryForm();
        queryForm.setDeviceId(deviceId);
        queryForm.setAlertStatus(0); // 未确认
        queryForm.setPageNum(1);
        queryForm.setPageSize(1); // 只需要数量

        ResponseDTO<PageResult<DeviceAlertVO>> response = alertManager.queryAlerts(queryForm);

        Long count = response.getData() != null ? response.getData().getTotal() : 0L;

        log.info("[告警管理] 设备未确认告警数量: deviceId={}, count={}", deviceId, count);

        return ResponseDTO.ok(count);
    }

    /**
     * 获取今日告警数量
     */
    @Operation(summary = "获取今日告警数量", description = "查询今日告警总数")
    @GetMapping("/today-count")
    public ResponseDTO<Long> getTodayAlertCount() {
        log.info("[告警管理] 查询今日告警数量");

        // 获取统计数据
        ResponseDTO<AlertStatisticsVO> statsResponse = alertManager.getAlertStatistics();

        Long todayCount = statsResponse.getData() != null ? statsResponse.getData().getTodayAlerts() : 0L;

        log.info("[告警管理] 今日告警数量: count={}", todayCount);

        return ResponseDTO.ok(todayCount);
    }

    /**
     * 健康检查接口
     */
    @Operation(summary = "告警系统健康检查", description = "检查告警系统运行状态")
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> healthCheck() {
        log.info("[告警管理] 告警系统健康检查");

        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "access-alert-service",
                "version", "1.0.0"
        );

        log.info("[告警管理] 健康检查完成: status={}", health.get("status"));

        return ResponseDTO.ok(health);
    }
}
