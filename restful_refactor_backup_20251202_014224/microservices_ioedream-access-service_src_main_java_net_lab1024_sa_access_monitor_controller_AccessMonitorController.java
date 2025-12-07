package net.lab1024.sa.access.monitor.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.monitor.service.AccessMonitorService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 门禁实时监控控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/monitor")
@Slf4j
public class AccessMonitorController {

    @Resource
    private AccessMonitorService accessMonitorService;

    /**
     * 获取实时监控大屏数据
     *
     * @return 监控大屏数据
     */
    @GetMapping("/dashboard")
    @SaCheckPermission("access:monitor:dashboard")
    public ResponseDTO<Map<String, Object>> getMonitoringDashboard() {
        log.debug("[AccessMonitorController] 获取实时监控大屏数据");

        Map<String, Object> dashboard = accessMonitorService.getMonitoringDashboard();
        return ResponseDTO.userOk(dashboard);
    }

    /**
     * 获取设备实时状态列表
     *
     * @param areaId 区域ID (可选)
     * @param status 设备状态 (可选)
     * @return 设备状态列表
     */
    @GetMapping("/devices/status")
    @SaCheckPermission("access:monitor:device-status")
    public ResponseDTO<List<Map<String, Object>>> getDeviceStatusList(@RequestParam(required = false) Long areaId,
                                                                   @RequestParam(required = false) String status) {
        log.debug("[AccessMonitorController] 获取设备实时状态: areaId={}, status={}", areaId, status);

        List<Map<String, Object>> devices = accessMonitorService.getDeviceStatusList(areaId, status);
        return ResponseDTO.userOk(devices);
    }

    /**
     * 获取实时事件流
     *
     * @param pageSize 页面大小
     * @return 实时事件
     */
    @GetMapping("/events/realtime")
    @SaCheckPermission("access:monitor:realtime-events")
    public ResponseDTO<List<Map<String, Object>>> getRealtimeEvents(@RequestParam(defaultValue = "50") Integer pageSize) {
        log.debug("[AccessMonitorController] 获取实时事件流: pageSize={}", pageSize);

        List<Map<String, Object>> events = accessMonitorService.getRealtimeEvents(pageSize);
        return ResponseDTO.userOk(events);
    }

    /**
     * 获取报警信息列表
     *
     * @param severity 严重程度 (可选)
     * @param status 报警状态 (可选)
     * @param pageSize 页面大小
     * @return 报警列表
     */
    @GetMapping("/alerts")
    @SaCheckPermission("access:monitor:alerts")
    public ResponseDTO<Map<String, Object>> getAlertList(@RequestParam(required = false) String severity,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "20") Integer pageSize) {
        log.debug("[AccessMonitorController] 获取报警信息: severity={}, status={}, pageSize={}", severity, status, pageSize);

        Map<String, Object> alerts = accessMonitorService.getAlertList(severity, status, pageSize);
        return ResponseDTO.userOk(alerts);
    }

    /**
     * 处理报警
     *
     * @param alertId 报警ID
     * @param action 处理动作 (acknowledge/resolve/close)
     * @param comment 处理意见
     * @return 处理结果
     */
    @PostMapping("/alerts/{alertId}/handle")
    @SaCheckPermission("access:monitor:handle-alert")
    public ResponseDTO<String> handleAlert(@PathVariable Long alertId,
                                            @RequestParam String action,
                                            @RequestParam(required = false) String comment) {
        log.info("[AccessMonitorController] 处理报警: alertId={}, action={}", alertId, action);

        String result = accessMonitorService.handleAlert(alertId, action, comment);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取人员实时位置
     *
     * @param areaId 区域ID (可选)
     * @return 人员位置信息
     */
    @GetMapping("/personnel/locations")
    @SaCheckPermission("access:monitor:personnel-locations")
    public ResponseDTO<List<Map<String, Object>>> getPersonnelLocations(@RequestParam(required = false) Long areaId) {
        log.debug("[AccessMonitorController] 获取人员实时位置: areaId={}", areaId);

        List<Map<String, Object>> locations = accessMonitorService.getPersonnelLocations(areaId);
        return ResponseDTO.userOk(locations);
    }

    /**
     * 获取区域占用情况
     *
     * @return 区域占用统计
     */
    @GetMapping("/areas/occupancy")
    @SaCheckPermission("access:monitor:area-occupancy")
    public ResponseDTO<List<Map<String, Object>>> getAreaOccupancy() {
        log.debug("[AccessMonitorController] 获取区域占用情况");

        List<Map<String, Object>> occupancy = accessMonitorService.getAreaOccupancy();
        return ResponseDTO.userOk(occupancy);
    }

    /**
     * 获取实时统计数据
     *
     * @param timeRange 时间范围 (hour/day/week/month)
     * @return 统计数据
     */
    @GetMapping("/statistics/realtime")
    @SaCheckPermission("access:monitor:statistics")
    public ResponseDTO<Map<String, Object>> getRealtimeStatistics(@RequestParam(defaultValue = "hour") String timeRange) {
        log.debug("[AccessMonitorController] 获取实时统计数据: timeRange={}", timeRange);

        Map<String, Object> statistics = accessMonitorService.getRealtimeStatistics(timeRange);
        return ResponseDTO.userOk(statistics);
    }

    /**
     * 获取系统健康状态
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @SaCheckPermission("access:monitor:health")
    public ResponseDTO<Map<String, Object>> getSystemHealth() {
        log.debug("[AccessMonitorController] 获取系统健康状态");

        Map<String, Object> health = accessMonitorService.getSystemHealth();
        return ResponseDTO.userOk(health);
    }

    /**
     * 获取设备性能指标
     *
     * @param deviceId 设备ID
     * @param hours 小时数
     * @return 性能指标
     */
    @GetMapping("/devices/{deviceId}/performance")
    @SaCheckPermission("access:monitor:device-performance")
    public ResponseDTO<Map<String, Object>> getDevicePerformance(@PathVariable String deviceId,
                                                               @RequestParam(defaultValue = "24") Integer hours) {
        log.debug("[AccessMonitorController] 获取设备性能指标: deviceId={}, hours={}", deviceId, hours);

        Map<String, Object> performance = accessMonitorService.getDevicePerformance(deviceId, hours);
        return ResponseDTO.userOk(performance);
    }

    /**
     * 启动视频联动
     *
     * @param deviceId 设备ID
     * @param eventType 事件类型
     * @return 联动结果
     */
    @PostMapping("/video-linkage")
    @SaCheckPermission("access:monitor:video-linkage")
    public ResponseDTO<String> startVideoLinkage(@RequestParam String deviceId,
                                               @RequestParam String eventType) {
        log.info("[AccessMonitorController] 启动视频联动: deviceId={}, eventType={}", deviceId, eventType);

        String result = accessMonitorService.startVideoLinkage(deviceId, eventType);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取异常事件列表
     *
     * @param areaId 区域ID (可选)
     * @param severity 严重程度 (可选)
     * @param startTime 开始时间 (可选)
     * @param endTime 结束时间 (可选)
     * @return 异常事件列表
     */
    @GetMapping("/events/abnormal")
    @SaCheckPermission("access:monitor:abnormal-events")
    public ResponseDTO<List<Map<String, Object>>> getAbnormalEvents(@RequestParam(required = false) Long areaId,
                                                                  @RequestParam(required = false) String severity,
                                                                  @RequestParam(required = false) String startTime,
                                                                  @RequestParam(required = false) String endTime) {
        log.debug("[AccessMonitorController] 获取异常事件: areaId={}, severity={}", areaId, severity);

        List<Map<String, Object>> events = accessMonitorService.getAbnormalEvents(areaId, severity, startTime, endTime);
        return ResponseDTO.userOk(events);
    }

    /**
     * 获取监控配置信息
     *
     * @return 配置信息
     */
    @GetMapping("/config")
    @SaCheckPermission("access:monitor:config")
    public ResponseDTO<Map<String, Object>> getMonitoringConfig() {
        log.debug("[AccessMonitorController] 获取监控配置信息");

        Map<String, Object> config = accessMonitorService.getMonitoringConfig();
        return ResponseDTO.userOk(config);
    }

    /**
     * 更新监控配置
     *
     * @param config 配置信息
     * @return 更新结果
     */
    @PutMapping("/config")
    @SaCheckPermission("access:monitor:update-config")
    public ResponseDTO<String> updateMonitoringConfig(@RequestBody Map<String, Object> config) {
        log.info("[AccessMonitorController] 更新监控配置");

        String result = accessMonitorService.updateMonitoringConfig(config);
        return ResponseDTO.userOk(result);
    }

    /**
     * 导出监控报告
     *
     * @param reportType 报告类型 (summary/detailed/alarms/performance)
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报告数据
     */
    @GetMapping("/report/export")
    @SaCheckPermission("access:monitor:export-report")
    public ResponseDTO<Map<String, Object>> exportMonitoringReport(@RequestParam String reportType,
                                                                 @RequestParam String startDate,
                                                                 @RequestParam String endDate) {
        log.info("[AccessMonitorController] 导出监控报告: reportType={}, startDate={}, endDate={}",
                reportType, startDate, endDate);

        Map<String, Object> report = accessMonitorService.exportMonitoringReport(reportType, startDate, endDate);
        return ResponseDTO.userOk(report);
    }

    /**
     * 获取监控日志
     *
     * @param level 日志级别
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageSize 页面大小
     * @return 日志列表
     */
    @GetMapping("/logs")
    @SaCheckPermission("access:monitor:logs")
    public ResponseDTO<Map<String, Object>> getMonitoringLogs(@RequestParam(required = false) String level,
                                                           @RequestParam(required = false) String startTime,
                                                           @RequestParam(required = false) String endTime,
                                                           @RequestParam(defaultValue = "50") Integer pageSize) {
        log.debug("[AccessMonitorController] 获取监控日志: level={}, startTime={}, endTime={}",
                level, startTime, endTime);

        Map<String, Object> logs = accessMonitorService.getMonitoringLogs(level, startTime, endTime, pageSize);
        return ResponseDTO.userOk(logs);
    }

    /**
     * 触发系统自检
     *
     * @return 自检结果
     */
    @PostMapping("/self-check")
    @SaCheckPermission("access:monitor:self-check")
    public ResponseDTO<Map<String, Object>> triggerSelfCheck() {
        log.info("[AccessMonitorController] 触发系统自检");

        Map<String, Object> result = accessMonitorService.triggerSelfCheck();
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取实时性能监控数据
     *
     * @return 性能数据
     */
    @GetMapping("/performance")
    @SaCheckPermission("access:monitor:performance")
    public ResponseDTO<Map<String, Object>> getPerformanceMetrics() {
        log.debug("[AccessMonitorController] 获取实时性能监控数据");

        Map<String, Object> metrics = accessMonitorService.getPerformanceMetrics();
        return ResponseDTO.userOk(metrics);
    }
}