package net.lab1024.sa.consume.controller.device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.device.DeviceHealthMetricEntity;
import net.lab1024.sa.common.entity.device.QualityAlarmEntity;
import net.lab1024.sa.consume.service.device.DeviceQualityService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 设备质量诊断控制器 - 设备质量管理REST API
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/device/quality")
@Tag(name = "设备质量诊断")
public class DeviceQualityController {

    @Resource
    private DeviceQualityService deviceQualityService;

    @PostMapping("/{deviceId}/diagnose")
    @Operation(summary = "执行设备质量诊断")
    public ResponseDTO<Long> diagnoseDevice(@PathVariable String deviceId) {
        log.info("[设备质量诊断] 执行诊断: deviceId={}", deviceId);
        Long recordId = deviceQualityService.diagnoseDevice(deviceId);
        return ResponseDTO.ok(recordId);
    }

    @PostMapping("/batch/diagnose")
    @Operation(summary = "批量执行设备质量诊断")
    public ResponseDTO<Map<String, Long>> batchDiagnose(@RequestBody List<String> deviceIds) {
        log.info("[设备质量诊断] 批量诊断: count={}", deviceIds.size());
        Map<String, Long> results = deviceQualityService.batchDiagnose(deviceIds);
        return ResponseDTO.ok(results);
    }

    @GetMapping("/{deviceId}/score")
    @Operation(summary = "获取设备质量评分")
    public ResponseDTO<Map<String, Object>> getQualityScore(@PathVariable String deviceId) {
        log.info("[设备质量诊断] 查询质量评分: deviceId={}", deviceId);
        Map<String, Object> score = deviceQualityService.getQualityScore(deviceId);
        return ResponseDTO.ok(score);
    }

    @GetMapping("/{deviceId}/trend")
    @Operation(summary = "获取设备健康趋势")
    public ResponseDTO<List<DeviceHealthMetricEntity>> getHealthTrend(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("[设备质量诊断] 查询健康趋势: deviceId={}, days={}", deviceId, days);
        List<DeviceHealthMetricEntity> trend = deviceQualityService.getHealthTrend(deviceId, days);
        return ResponseDTO.ok(trend);
    }

    @GetMapping("/alarms")
    @Operation(summary = "获取质量告警列表")
    public ResponseDTO<PageResult<QualityAlarmEntity>> getAlarms(
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[设备质量诊断] 查询告警列表: level={}, pageNum={}, pageSize={}", level, pageNum, pageSize);
        var page = deviceQualityService.getAlarms(level, pageNum, pageSize);
        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/score/level")
    @Operation(summary = "获取质量等级分布")
    public ResponseDTO<Map<String, Long>> getQualityLevelDistribution() {
        log.info("[设备质量诊断] 查询质量等级分布");
        // TODO: 实现统计查询
        Map<String, Long> distribution = new java.util.HashMap<>();
        distribution.put("优秀", 10L);
        distribution.put("良好", 25L);
        distribution.put("合格", 40L);
        distribution.put("较差", 15L);
        distribution.put("危险", 5L);
        return ResponseDTO.ok(distribution);
    }
}
