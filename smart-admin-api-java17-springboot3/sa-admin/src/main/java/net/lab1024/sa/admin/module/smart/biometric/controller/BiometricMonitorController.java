package net.lab1024.sa.admin.module.smart.biometric.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricAlertRequestVO;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricAlertResolveRequestVO;
import net.lab1024.sa.admin.module.smart.biometric.domain.vo.BiometricEngineStatusReportVO;
import net.lab1024.sa.admin.module.smart.biometric.service.BiometricMonitorService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * 生物识别监控相关接口.
 *
 * @author AI
 */
@Validated
@SaCheckLogin
@Tag(name = "Biometric Monitor", description = "生物识别引擎监控与告警接口")
@RestController
@RequestMapping("/api/smart/biometric/monitor")
@RequiredArgsConstructor
public class BiometricMonitorController {

    private static final Logger log = LoggerFactory.getLogger(BiometricMonitorController.class);

    private final BiometricMonitorService monitorService;

    /**
     * 上报引擎心跳/状态快照
     *
     * 功能说明：接收来自生物识别引擎的运行状态并进行刷新缓存或存储，以支持监控面板展示与告警判定。
     *
     * @param request 引擎状态上报请求体（已通过 @Valid 进行约束校验）
     * @return ResponseDTO<EngineSnapshot>：返回最新的引擎状态快照
     * @throws jakarta.validation.ConstraintViolationException 当请求体字段不符合校验规则时抛出
     *
     * 使用示例：
     * curl -X POST /api/smart/biometric/monitor/status -H "Content-Type: application/json" -d "{ ... }"
     */
    @Operation(summary = "上报引擎心跳")
    @PostMapping("/status")
    @SaCheckPermission("smart:biometric:monitor:write")
    public ResponseDTO<BiometricMonitorService.EngineSnapshot> reportStatus(
            @Valid @RequestBody BiometricEngineStatusReportVO request) {
        log.debug("收到引擎状态上报, engineId={}", request.getEngineId());
        return ResponseDTO.ok(monitorService.refreshEngineStatus(request.toPayload()));
    }

    /**
     * 获取监控仪表盘数据
     *
     * 功能说明：汇总并返回整体引擎运行概览、核心指标与摘要信息，用于前端监控大盘展示。
     *
     * @return ResponseDTO<BiometricMonitorDashboard>：监控大盘数据
     *
     * 使用示例：
     * curl -X GET /api/smart/biometric/monitor/dashboard
     */
    @Operation(summary = "监控仪表盘")
    @GetMapping("/dashboard")
    @SaCheckPermission("smart:biometric:monitor:read")
    public ResponseDTO<BiometricMonitorService.BiometricMonitorDashboard> dashboard() {
        return ResponseDTO.ok(monitorService.buildDashboard());
    }

    /**
     * 查询当前告警列表
     *
     * 功能说明：返回当前处于未解决状态的告警集合；若无告警，返回空集合。
     *
     * @return ResponseDTO<Collection<BiometricAlert>>：当前告警列表
     *
     * 使用示例：
     * curl -X GET /api/smart/biometric/monitor/alerts
     */
    @Operation(summary = "查询当前告警")
    @GetMapping("/alerts")
    @SaCheckPermission("smart:biometric:monitor:read")
    public ResponseDTO<Collection<BiometricMonitorService.BiometricAlert>> alerts() {
        return ResponseDTO.ok(monitorService.getAlerts());
    }

    /**
     * 触发一条告警
     *
     * 功能说明：根据引擎ID、告警编码、级别与信息创建/触发一条新的告警记录。
     *
     * @param request 告警触发请求体
     * @return ResponseDTO<BiometricAlert>：新创建或触发的告警信息
     *
     * 使用示例：
     * curl -X POST /api/smart/biometric/monitor/alerts -H "Content-Type: application/json" -d "{ ... }"
     */
    @Operation(summary = "触发告警")
    @PostMapping("/alerts")
    @SaCheckPermission("smart:biometric:monitor:alert")
    public ResponseDTO<BiometricMonitorService.BiometricAlert> triggerAlert(
            @Valid @RequestBody BiometricAlertRequestVO request) {
        log.info("触发告警, engineId={}, code={}, level={}", request.getEngineId(), request.getAlertCode(), request.getLevel());
        return ResponseDTO.ok(
                monitorService.triggerAlert(
                        request.getEngineId(),
                        request.getAlertCode(),
                        request.getLevel(),
                        request.getMessage()));
    }

    /**
     * 将指定告警置为已解决
     *
     * 功能说明：根据告警ID与解决说明，标记对应告警为已解决状态。
     *
     * @param alertId 告警ID（不可为空）
     * @param request 解决说明请求体
     * @return ResponseDTO<Void>：统一成功响应
     *
     * 使用示例：
     * curl -X POST /api/smart/biometric/monitor/alerts/{alertId}/resolve -H "Content-Type: application/json" -d "{ ... }"
     */
    @Operation(summary = "解决告警")
    @PostMapping("/alerts/{alertId}/resolve")
    @SaCheckPermission("smart:biometric:monitor:alert")
    public ResponseDTO<Void> resolveAlert(
            @PathVariable @NotBlank(message = "告警ID不能为空") String alertId,
            @Valid @RequestBody BiometricAlertResolveRequestVO request) {
        log.info("解决告警, alertId={}", alertId);
        monitorService.resolveAlert(alertId, request.getResolution());
        return ResponseDTO.ok();
    }
}

