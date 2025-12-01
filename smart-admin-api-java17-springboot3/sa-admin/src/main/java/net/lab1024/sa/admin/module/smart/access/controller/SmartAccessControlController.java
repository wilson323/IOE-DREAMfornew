package net.lab1024.sa.admin.module.smart.access.controller;

import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.Resource;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessRecordEntity;
import net.lab1024.sa.admin.module.smart.access.service.SmartAccessControlService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

/**
 * 门禁控制控制器
 * 
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@RestController
@RequestMapping("/api/smart/access/control")
@Tag(name = "门禁控制管理", description = "门禁控制相关接口")
@Validated
public class SmartAccessControlController {

    @Resource
    private SmartAccessControlService accessControlService;

    @Operation(summary = "门禁通行校验", description = "校验人员门禁通行权限")
    @PostMapping("/verify")
    @SaCheckPermission("smart:access:verify")
    @RequireResource(code = "smart:access:verify", scope = "SELF")
    public ResponseDTO<Map<String, Object>> verifyAccess(@RequestParam @NotNull Long personId,
            @RequestParam @NotNull Long deviceId, @RequestParam @NotNull String accessType,
            @RequestParam @NotNull String credential) {
        return accessControlService.verifyAccess(personId, deviceId, accessType, credential);
    }

    @Operation(summary = "刷卡校验", description = "通过刷卡校验门禁权限")
    @PostMapping("/verify/card")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Map<String, Object>> verifyCardAccess(
            @RequestParam @NotNull String cardNumber, @RequestParam @NotNull Long deviceId) {
        return accessControlService.verifyCardAccess(cardNumber, deviceId);
    }

    @Operation(summary = "人脸识别校验", description = "通过人脸识别校验门禁权限")
    @PostMapping("/verify/face")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Map<String, Object>> verifyFaceAccess(
            @RequestParam @NotNull String faceFeatureId, @RequestParam @NotNull Long deviceId) {
        return accessControlService.verifyFaceAccess(faceFeatureId, deviceId);
    }

    @Operation(summary = "指纹校验", description = "通过指纹校验门禁权限")
    @PostMapping("/verify/fingerprint")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Map<String, Object>> verifyFingerprintAccess(
            @RequestParam @NotNull String fingerprintId, @RequestParam @NotNull Long deviceId) {
        return accessControlService.verifyFingerprintAccess(fingerprintId, deviceId);
    }

    @Operation(summary = "密码校验", description = "通过密码校验门禁权限")
    @PostMapping("/verify/password")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Map<String, Object>> verifyPasswordAccess(
            @RequestParam @NotNull String password, @RequestParam @NotNull Long deviceId) {
        return accessControlService.verifyPasswordAccess(password, deviceId);
    }

    @Operation(summary = "远程开门", description = "远程控制门禁设备开门")
    @PostMapping("/remote-open")
    @SaCheckPermission("smart:access:device:control")
    @RequireResource(code = "smart:access:device:control", scope = "AREA")
    public ResponseDTO<String> remoteOpenDoor(@RequestParam @NotNull Long deviceId,
            @RequestParam @NotNull Long operatorId, @RequestParam(required = false) String reason) {
        return accessControlService.remoteOpenDoor(deviceId, operatorId, reason);
    }

    @Operation(summary = "记录通行事件", description = "手动记录门禁通行事件")
    @PostMapping("/record")
    @SaCheckPermission("access:record")
    public ResponseDTO<String> recordAccessEvent(
            @RequestBody @NotNull SmartAccessRecordEntity record) {
        return accessControlService.recordAccessEvent(record);
    }

    @Operation(summary = "生成访问令牌", description = "生成临时访问令牌")
    @PostMapping("/token/generate")
    @SaCheckPermission("access:token:generate")
    public ResponseDTO<String> generateAccessToken(@RequestParam @NotNull Long personId,
            @RequestParam @NotNull Long deviceId,
            @RequestParam(defaultValue = "30") Integer expireMinutes) {
        String token = accessControlService.generateAccessToken(personId, deviceId, expireMinutes);
        return SmartResponseUtil.success(token);
    }

    @Operation(summary = "校验访问令牌", description = "校验临时访问令牌的有效性")
    @PostMapping("/token/verify")
    @SaCheckPermission("access:token:verify")
    public ResponseDTO<Map<String, Object>> verifyAccessToken(
            @RequestParam @NotNull String accessToken) {
        return accessControlService.verifyAccessToken(accessToken);
    }

    @Operation(summary = "获取人员当日通行统计", description = "获取指定人员当日通行统计信息")
    @GetMapping("/statistics/person/{personId}")
    @SaCheckPermission("access:statistics:person")
    public ResponseDTO<Map<String, Object>> getPersonTodayStatistics(
            @PathVariable @NotNull Long personId) {
        Map<String, Object> statistics = accessControlService.getPersonTodayStatistics(personId);
        return SmartResponseUtil.success(statistics);
    }

    @Operation(summary = "获取设备当日通行统计", description = "获取指定设备当日通行统计信息")
    @GetMapping("/statistics/device/{deviceId}")
    @SaCheckPermission("access:statistics:device")
    public ResponseDTO<Map<String, Object>> getDeviceTodayStatistics(
            @PathVariable @NotNull Long deviceId) {
        Map<String, Object> statistics = accessControlService.getDeviceTodayStatistics(deviceId);
        return SmartResponseUtil.success(statistics);
    }

    @Operation(summary = "检查强制开门", description = "检查指定设备在指定时间是否为强制开门")
    @GetMapping("/check-forced-open")
    @SaCheckPermission("access:check-forced-open")
    public ResponseDTO<Boolean> checkForcedOpen(@RequestParam @NotNull Long deviceId,
            @RequestParam(required = false) Long timestamp) {
        java.time.LocalDateTime accessTime =
                timestamp != null
                        ? java.time.LocalDateTime.ofEpochSecond(timestamp / 1000, 0,
                                java.time.ZoneOffset.ofHours(8))
                        : java.time.LocalDateTime.now();
        boolean isForcedOpen = accessControlService.isForcedOpen(deviceId, accessTime);
        return SmartResponseUtil.success(isForcedOpen);
    }
}

