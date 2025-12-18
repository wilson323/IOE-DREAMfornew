package net.lab1024.sa.access.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
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
 * 寮€鏀惧钩鍙伴棬绂佺鐞咥PI鎺у埗鍣?
 * 鎻愪緵闂ㄧ鎺у埗銆侀€氳璁板綍鏌ヨ銆侀棬绂佽澶囩鐞嗙瓑寮€鏀炬帴鍙?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/access")
@RequiredArgsConstructor
@Tag(name = "寮€鏀惧钩鍙伴棬绂佺鐞咥PI", description = "鎻愪緵闂ㄧ鎺у埗銆侀€氳璁板綍鏌ヨ銆侀棬绂佽澶囩鐞嗙瓑鍔熻兘")
@Validated
public class AccessOpenApiController {

    private final AccessOpenApiService accessOpenApiService;

    /**
     * 闂ㄧ閫氳楠岃瘉
     */
    @PostMapping("/verify")
    @Operation(summary = "闂ㄧ閫氳楠岃瘉", description = "楠岃瘉鐢ㄦ埛鏄惁鏈夐€氳鏉冮檺")
    public ResponseDTO<AccessVerifyResponse> verifyAccess(
            @Valid @RequestBody AccessVerifyRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 闂ㄧ閫氳楠岃瘉: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        AccessVerifyResponse response = accessOpenApiService.verifyAccess(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 杩滅▼寮€闂?
     */
    @PostMapping("/control/open")
    @Operation(summary = "杩滅▼寮€闂?, description = "杩滅▼鎺у埗闂ㄧ璁惧寮€闂?)
    public ResponseDTO<AccessControlResponse> remoteOpenDoor(
            @Valid @RequestBody AccessControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 杩滅▼寮€闂? deviceId={}, clientIp={}", request.getDeviceId(), clientIp);

        AccessControlResponse response = accessOpenApiService.remoteControl(
                request.getDeviceId(), "OPEN", request.getReason(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 杩滅▼鍏抽棬
     */
    @PostMapping("/control/close")
    @Operation(summary = "杩滅▼鍏抽棬", description = "杩滅▼鎺у埗闂ㄧ璁惧鍏抽棬")
    public ResponseDTO<AccessControlResponse> remoteCloseDoor(
            @Valid @RequestBody AccessControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 杩滅▼鍏抽棬: deviceId={}, clientIp={}", request.getDeviceId(), clientIp);

        AccessControlResponse response = accessOpenApiService.remoteControl(
                request.getDeviceId(), "CLOSE", request.getReason(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 鑾峰彇閫氳璁板綍鍒楄〃
     */
    @GetMapping("/records")
    @Operation(summary = "鑾峰彇閫氳璁板綍鍒楄〃", description = "鍒嗛〉鑾峰彇闂ㄧ閫氳璁板綍")
    public ResponseDTO<PageResult<AccessRecordResponse>> getAccessRecords(
            @Parameter(description = "椤电爜") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "椤靛ぇ灏?) @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "鐢ㄦ埛ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "璁惧ID") @RequestParam(required = false) String deviceId,
            @Parameter(description = "閫氳鐘舵€?) @RequestParam(required = false) Integer accessStatus,
            @Parameter(description = "寮€濮嬫椂闂?) @RequestParam(required = false) String startTime,
            @Parameter(description = "缁撴潫鏃堕棿") @RequestParam(required = false) String endTime,
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

        log.info("[寮€鏀続PI] 鏌ヨ閫氳璁板綍: pageNum={}, pageSize={}, userId={}, deviceId={}",
                pageNum, pageSize, userId, deviceId);

        PageResult<AccessRecordResponse> result = accessOpenApiService.getAccessRecords(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 鑾峰彇閫氳璁板綍璇︽儏
     */
    @GetMapping("/records/{recordId}")
    @Operation(summary = "鑾峰彇閫氳璁板綍璇︽儏", description = "鏍规嵁璁板綍ID鑾峰彇閫氳璁板綍璇︽儏")
    public ResponseDTO<AccessRecordDetailResponse> getAccessRecordDetail(
            @Parameter(description = "璁板綍ID") @PathVariable Long recordId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鏌ヨ閫氳璁板綍璇︽儏: recordId={}", recordId);

        AccessRecordDetailResponse response = accessOpenApiService.getAccessRecordDetail(recordId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 鑾峰彇闂ㄧ璁惧鍒楄〃
     */
    @GetMapping("/devices")
    @Operation(summary = "鑾峰彇闂ㄧ璁惧鍒楄〃", description = "鑾峰彇闂ㄧ璁惧鍒楄〃")
    public ResponseDTO<List<AccessDeviceResponse>> getAccessDevices(
            @Parameter(description = "鍖哄煙ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "璁惧鐘舵€?) @RequestParam(required = false) Integer deviceStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鏌ヨ闂ㄧ璁惧鍒楄〃: areaId={}, deviceStatus={}", areaId, deviceStatus);

        List<AccessDeviceResponse> devices = accessOpenApiService.getAccessDevices(areaId, deviceStatus, token);
        return ResponseDTO.ok(devices);
    }

    /**
     * 鑾峰彇闂ㄧ璁惧璇︽儏
     */
    @GetMapping("/devices/{deviceId}")
    @Operation(summary = "鑾峰彇闂ㄧ璁惧璇︽儏", description = "鏍规嵁璁惧ID鑾峰彇闂ㄧ璁惧璇︽儏")
    public ResponseDTO<AccessDeviceDetailResponse> getAccessDeviceDetail(
            @Parameter(description = "璁惧ID") @PathVariable String deviceId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鏌ヨ闂ㄧ璁惧璇︽儏: deviceId={}", deviceId);

        AccessDeviceDetailResponse response = accessOpenApiService.getAccessDeviceDetail(deviceId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 鎺у埗闂ㄧ璁惧
     */
    @PostMapping("/devices/{deviceId}/control")
    @Operation(summary = "鎺у埗闂ㄧ璁惧", description = "鎺у埗闂ㄧ璁惧鐨勫悇绉嶆搷浣?)
    public ResponseDTO<AccessControlResponse> controlAccessDevice(
            @Parameter(description = "璁惧ID") @PathVariable String deviceId,
            @Valid @RequestBody AccessDeviceControlRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 鎺у埗闂ㄧ璁惧: deviceId={}, action={}, clientIp={}",
                deviceId, request.getAction(), clientIp);

        AccessControlResponse response = accessOpenApiService.controlDevice(
                deviceId, request.getAction(), request.getParameters(), token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺
     */
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺", description = "鑾峰彇鎸囧畾鐢ㄦ埛鐨勯棬绂侀€氳鏉冮檺")
    public ResponseDTO<UserAccessPermissionResponse> getUserAccessPermissions(
            @Parameter(description = "鐢ㄦ埛ID") @PathVariable Long userId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鏌ヨ鐢ㄦ埛闂ㄧ鏉冮檺: userId={}", userId);

        UserAccessPermissionResponse response = accessOpenApiService.getUserAccessPermissions(userId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 鎺堜簣鐢ㄦ埛闂ㄧ鏉冮檺
     */
    @PostMapping("/permissions/grant")
    @Operation(summary = "鎺堜簣鐢ㄦ埛闂ㄧ鏉冮檺", description = "涓虹敤鎴锋巿浜堥棬绂侀€氳鏉冮檺")
    public ResponseDTO<Void> grantUserAccessPermission(
            @Valid @RequestBody GrantAccessPermissionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 鎺堜簣鐢ㄦ埛闂ㄧ鏉冮檺: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        accessOpenApiService.grantAccessPermission(request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 鎾ら攢鐢ㄦ埛闂ㄧ鏉冮檺
     */
    @PostMapping("/permissions/revoke")
    @Operation(summary = "鎾ら攢鐢ㄦ埛闂ㄧ鏉冮檺", description = "鎾ら攢鐢ㄦ埛鐨勯棬绂侀€氳鏉冮檺")
    public ResponseDTO<Void> revokeUserAccessPermission(
            @Valid @RequestBody RevokeAccessPermissionRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[寮€鏀続PI] 鎾ら攢鐢ㄦ埛闂ㄧ鏉冮檺: userId={}, deviceId={}, clientIp={}",
                request.getUserId(), request.getDeviceId(), clientIp);

        accessOpenApiService.revokeAccessPermission(request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 鑾峰彇瀹炴椂閫氳鐘舵€?
     */
    @GetMapping("/realtime/status")
    @Operation(summary = "鑾峰彇瀹炴椂閫氳鐘舵€?, description = "鑾峰彇闂ㄧ绯荤粺鐨勫疄鏃堕€氳鐘舵€?)
    public ResponseDTO<AccessRealtimeStatusResponse> getRealtimeAccessStatus(
            @Parameter(description = "鍖哄煙ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鑾峰彇瀹炴椂閫氳鐘舵€? areaId={}", areaId);

        AccessRealtimeStatusResponse response = accessOpenApiService.getRealtimeAccessStatus(areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 鑾峰彇闂ㄧ缁熻淇℃伅
     */
    @GetMapping("/statistics")
    @Operation(summary = "鑾峰彇闂ㄧ缁熻淇℃伅", description = "鑾峰彇闂ㄧ閫氳缁熻淇℃伅")
    public ResponseDTO<AccessStatisticsResponse> getAccessStatistics(
            @Parameter(description = "缁熻绫诲瀷") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "寮€濮嬫棩鏈?) @RequestParam(required = false) String startDate,
            @Parameter(description = "缁撴潫鏃ユ湡") @RequestParam(required = false) String endDate,
            @Parameter(description = "鍖哄煙ID") @RequestParam(required = false) Long areaId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[寮€鏀続PI] 鑾峰彇闂ㄧ缁熻: statisticsType={}, startDate={}, endDate={}",
                statisticsType, startDate, endDate);

        AccessStatisticsResponse response = accessOpenApiService.getAccessStatistics(
                statisticsType, startDate, endDate, areaId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 浠嶢uthorization澶翠腑鎻愬彇璁块棶浠ょ墝
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 鑾峰彇瀹㈡埛绔湡瀹濱P鍦板潃
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
