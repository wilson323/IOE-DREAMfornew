package net.lab1024.sa.common.monitor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.service.AlertService;

/**
 * 报警管理控制器
 * <p>
 * 提供报警记录的创建、查询等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备协议推送报警记录
 * - 系统监控报警
 * - 告警通知
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/alarm")
@Tag(name = "报警管理", description = "报警记录的创建、查询等API")
public class AlertController {

    @Resource
    private AlertService alertService;

    /**
     * 创建报警记录
     * <p>
     * 用于设备协议推送报警记录
     * </p>
     *
     * @param alert 报警实体
     * @return 创建的报警ID
     * @apiNote 示例请求：
     * <pre>
     * POST /api/v1/alarm/record/create
     * {
     *   "deviceId": 1,
     *   "deviceCode": "DEV001",
     *   "alarmType": "DOOR_FORCED_OPEN",
     *   "alarmLevel": "HIGH",
     *   "alarmTitle": "门禁设备报警",
     *   "alarmDescription": "门被强制打开",
     *   "alertLevel": "WARNING",
     *   "alertType": "ACCESS_ALARM",
     *   "serviceName": "ioedream-access-service"
     * }
     * </pre>
     */
    @PostMapping("/record/create")
    @Operation(
        summary = "创建报警记录",
        description = "用于设备协议推送报警记录，创建报警记录并返回报警ID",
        tags = {"报警管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "创建成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Long.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "参数错误"
    )
    public ResponseDTO<Long> createAlert(@Valid @RequestBody AlertEntity alert) {
        log.info("[报警管理] 创建报警记录，alarmTitle={}, alertLevel={}, alertType={}",
                alert.getAlertTitle(), alert.getAlertLevel(), alert.getAlertType());

        try {
            Long alertId = alertService.createAlert(alert);
            log.info("[报警管理] 报警记录创建成功，alertId={}", alertId);
            return ResponseDTO.ok(alertId);
        } catch (Exception e) {
            log.error("[报警管理] 报警记录创建失败，alarmTitle={}", alert.getAlertTitle(), e);
            return ResponseDTO.error("CREATE_ALERT_ERROR", "创建报警记录失败: " + e.getMessage());
        }
    }
}

