package net.lab1024.sa.access.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.common.domain.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.access.openapi.domain.request.*;
import net.lab1024.sa.access.openapi.domain.response.*;
import net.lab1024.sa.access.openapi.service.AccessOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台门禁管理API控制器
 * 提供门禁控制、通行记录查询、门禁设备管理等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/access")
@RequiredArgsConstructor
@Tag(name = "开放平台门禁管理API", description = "提供门禁控制、通行记录查询、门禁设备管理等功能")
@Validated
public class AccessOpenApiController {

    private final AccessOpenApiService accessOpenApiService;

    /**
     * 门禁通行验证
     */
    @PostMapping("/verify")
    @Operation(summary = "门禁通行验证", description = "验证用户是否有通行权限")
    public ResponseDTO<AccessVerifyResponse> verifyAccess(
            @Valid @RequestBody AccessVerifyRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 门禁通行验证: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        AccessVerifyResponse response = accessOpenApiService.verifyAccess(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 远程开门
     */
    @PostMapping("/control/open")
    @Operation(summary = "远程开门", description = "远程控制门禁设备开门")
    public ResponseDTO<AccessControlResponse> remoteOpenDoor(
            @Valid @RequestBody AccessControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 远程开门: deviceId={}, clientIp={}", request.getDeviceId(), clientIp);

        AccessControlResponse response = accessOpenApiService.remoteControl(
                request.getDeviceId(), "OPEN", request.getReason(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 远程关门
     */
    @PostMapping("/control/close")
    @Operation(summary = "远程关门", description = "远程控制门禁设备关门")
    public ResponseDTO<AccessControlResponse> remoteCloseDoor(
            @Valid @RequestBody AccessControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 远程关门: deviceId={}, clientIp={}", request.getDeviceId(), clientIp);

        AccessControlResponse response = accessOpenApiService.remoteControl(
                request.getDeviceId(), "CLOSE", request.getReason(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取通行记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "获取通行记录列表", description = "分页获取门禁通行记录")
    public ResponseDTO<PageResult<AccessRecordResponse>> getAccessRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "设备ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "通行状态") @RequestParam(required = false) Integer accessStatus,
            @Parameter(description = "开始时间") @RequestParam(required = false) String startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) String endTime,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AccessRecordQueryRequest queryRequest = AccessRecordQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .deviceId(deviceId)
                .accessStatus(accessStatus)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        log.info("[开放API] 查询通行记录: pageNum={}, pageSize={}, userId={}, deviceId={}",
                pageNum, pageSize, userId, deviceId);

        PageResult<AccessRecordResponse> result = accessOpenApiService.getAccessRecords(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取通行记录详情
     */
    @GetMapping("/records/{recordId}")
    @Operation(summary = "获取通行记录详情", description = "根据记录ID获取通行记录详情")
    public ResponseDTO<AccessRecordDetailResponse> getAccessRecordDetail(
            @Parameter(description = "记录ID") @PathVariable Long recordId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询通行记录详情: recordId={}", recordId);

        AccessRecordDetailResponse response = accessOpenApiService.getAccessRecordDetail(recordId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取门禁设备列表
     */
    @GetMapping("/devices")
    @Operation(summary = "获取门禁设备列表", description = "获取门禁设备列表")
    public ResponseDTO<List<AccessDeviceResponse>> getAccessDevices(
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "设备状态") @RequestParam(required = false) Integer deviceStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询门禁设备列表: areaId={}, deviceStatus={}", areaId, deviceStatus);

        List<AccessDeviceResponse> devices = accessOpenApiService.getAccessDevices(areaId, deviceStatus, token);
        return ResponseDTO.ok(devices);
    }

    /**
     * 获取门禁设备详情
     */
    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "获取门禁设备详情", description = "根据设备ID获取门禁设备详情")
    public ResponseDTO<AccessDeviceDetailResponse> getAccessDeviceDetail(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询门禁设备详情: deviceId={}", deviceId);

        AccessDeviceDetailResponse response = accessOpenApiService.getAccessDeviceDetail(deviceId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 控制门禁设备
     */
    @PostMapping("/devices/{deviceId}/control")
    @Operation(summary = "控制门禁设备", description = "控制门禁设备的各种操作")
    public ResponseDTO<AccessControlResponse> controlAccessDevice(
            @Parameter(description = "设备ID") @PathVariable String deviceId,
            @Valid @RequestBody AccessDeviceControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 控制门禁设备: deviceId={}, action={}, clientIp={}",
                deviceId, request.getAction(), clientIp);

        AccessControlResponse response = accessOpenApiService.controlDevice(
                deviceId, request.getAction(), request.getParameters(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取用户门禁权限
     */
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "获取用户门禁权限", description = "获取指定用户的门禁通行权限")
    public ResponseDTO<UserAccessPermissionResponse> getUserAccessPermissions(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户门禁权限: userId={}", userId);

        UserAccessPermissionResponse response = accessOpenApiService.getUserAccessPermissions(userId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 授予用户门禁权限
     */
    @PostMapping("/permissions/grant")
    @Operation(summary = "授予用户门禁权限", description = "为用户授予门禁通行权限")
    public ResponseDTO<Void> grantUserAccessPermission(
            @Valid @RequestBody GrantAccessPermissionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 授予用户门禁权限: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        accessOpenApiService.grantAccessPermission(request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 撤销用户门禁权限
     */
    @PostMapping("/permissions/revoke")
    @Operation(summary = "撤销用户门禁权限", description = "撤销用户的门禁通行权限")
    public ResponseDTO<Void> revokeUserAccessPermission(
            @Valid @RequestBody RevokeAccessPermissionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 撤销用户门禁权限: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        accessOpenApiService.revokeAccessPermission(request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 获取实时通行状态
     */
    @GetMapping("/realtime/status")
    @Operation(summary = "获取实时通行状态", description = "获取门禁系统的实时通行状态")
    public ResponseDTO<AccessRealtimeStatusResponse> getRealtimeAccessStatus(
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取实时通行状态: areaId={}", areaId);

        AccessRealtimeStatusResponse response = accessOpenApiService.getRealtimeAccessStatus(areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取门禁统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取门禁统计信息", description = "获取门禁通行统计信息")
    public ResponseDTO<AccessStatisticsResponse> getAccessStatistics(
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取门禁统计: statisticsType={}, startDate={}, endDate={}",
                statisticsType, startDate, endDate);

        AccessStatisticsResponse response = accessOpenApiService.getAccessStatistics(
                statisticsType, startDate, endDate, areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}