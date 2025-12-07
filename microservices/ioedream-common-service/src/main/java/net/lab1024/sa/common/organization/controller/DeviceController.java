package net.lab1024.sa.common.organization.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.service.DeviceService;

/**
 * 设备管理控制器
 * <p>
 * 提供设备状态更新等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备协议推送设备状态更新
 * - 设备在线/离线状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device")
@Tag(name = "设备管理", description = "设备状态更新等API")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    /**
     * 更新设备状态
     * <p>
     * 用于设备协议推送设备状态更新
     * </p>
     *
     * @param request 更新请求（包含deviceId、deviceStatus和lastOnlineTime）
     * @return 是否更新成功
     * @apiNote 示例请求：
     * <pre>
     * PUT /api/v1/device/status/update
     * {
     *   "deviceId": 1,
     *   "deviceStatus": "ONLINE",
     *   "lastOnlineTime": "2025-01-30T10:00:00"
     * }
     * </pre>
     */
    @PutMapping("/status/update")
    @Operation(
        summary = "更新设备状态",
        description = "用于设备协议推送设备状态更新，更新设备的在线/离线/维护状态",
        tags = {"设备管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "更新成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Boolean.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "参数错误或设备不存在"
    )
    public ResponseDTO<Boolean> updateDeviceStatus(@RequestBody Map<String, Object> request) {
        log.info("[设备管理] 更新设备状态，request={}", request);

        try {
            // 解析请求参数
            Long deviceId = null;
            Object deviceIdObj = request.get("deviceId");
            if (deviceIdObj instanceof Number) {
                deviceId = ((Number) deviceIdObj).longValue();
            } else if (deviceIdObj instanceof String) {
                deviceId = Long.parseLong((String) deviceIdObj);
            }

            if (deviceId == null) {
                log.warn("[设备管理] 设备ID为空");
                return ResponseDTO.error("DEVICE_ID_REQUIRED", "设备ID不能为空");
            }

            String deviceStatus = (String) request.get("deviceStatus");
            if (deviceStatus == null || deviceStatus.isEmpty()) {
                log.warn("[设备管理] 设备状态为空");
                return ResponseDTO.error("DEVICE_STATUS_REQUIRED", "设备状态不能为空");
            }

            LocalDateTime lastOnlineTime = null;
            if (request.get("lastOnlineTime") != null) {
                Object lastOnlineTimeObj = request.get("lastOnlineTime");
                if (lastOnlineTimeObj instanceof String) {
                    lastOnlineTime = LocalDateTime.parse((String) lastOnlineTimeObj);
                } else if (lastOnlineTimeObj instanceof LocalDateTime) {
                    lastOnlineTime = (LocalDateTime) lastOnlineTimeObj;
                } else if (lastOnlineTimeObj instanceof Number) {
                    // 时间戳（秒）
                    long timestamp = ((Number) lastOnlineTimeObj).longValue();
                    lastOnlineTime = LocalDateTime.ofEpochSecond(timestamp, 0,
                            java.time.ZoneOffset.of("+8"));
                }
            }

            return deviceService.updateDeviceStatus(deviceId, deviceStatus, lastOnlineTime);

        } catch (Exception e) {
            log.error("[设备管理] 更新设备状态异常，request={}", request, e);
            return ResponseDTO.error("UPDATE_DEVICE_STATUS_ERROR", "更新设备状态异常: " + e.getMessage());
        }
    }

    /**
     * 根据设备编码查询设备
     * <p>
     * 用于根据设备序列号（SN）或设备编码查找设备信息
     * </p>
     *
     * @param deviceCode 设备编码（设备序列号SN）
     * @return 设备实体
     * @apiNote 示例请求：
     * <pre>
     * GET /api/v1/device/code/{deviceCode}
     * </pre>
     */
    @org.springframework.web.bind.annotation.GetMapping("/code/{deviceCode}")
    @Operation(
        summary = "根据设备编码查询设备",
        description = "用于根据设备序列号（SN）或设备编码查找设备信息",
        tags = {"设备管理"}
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "查询成功",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = net.lab1024.sa.common.organization.entity.DeviceEntity.class)
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "设备不存在"
    )
    public ResponseDTO<net.lab1024.sa.common.organization.entity.DeviceEntity> getDeviceByCode(
            @io.swagger.v3.oas.annotations.Parameter(description = "设备编码（设备序列号SN）", required = true)
            @org.springframework.web.bind.annotation.PathVariable String deviceCode) {
        log.info("[设备管理] 根据设备编码查询设备，deviceCode={}", deviceCode);
        return deviceService.getDeviceByCode(deviceCode);
    }
}

