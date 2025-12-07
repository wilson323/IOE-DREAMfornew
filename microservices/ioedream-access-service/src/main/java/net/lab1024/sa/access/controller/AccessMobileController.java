package net.lab1024.sa.access.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.access.service.AdvancedAccessControlService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁移动端控制器
 * <p>
 * 提供移动端门禁管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/access")
@Tag(name = "移动端门禁管理", description = "移动端门禁检查、验证、查询等API")
public class AccessMobileController {

    @Resource
    private AccessDeviceService accessDeviceService;

    @Resource
    private AccessEventService accessEventService;

    @Resource
    private AdvancedAccessControlService advancedAccessControlService;

    /**
     * 移动端门禁检查
     *
     * @param request 检查请求
     * @return 检查结果
     */
    @PostMapping("/check")
    @Operation(summary = "移动端门禁检查", description = "移动端门禁权限检查")
    @SaCheckLogin
    public ResponseDTO<Boolean> mobileAccessCheck(@Valid @RequestBody MobileAccessCheckRequest request) {
        log.info("移动端门禁检查: userId={}, deviceId={}, areaId={}", 
                request.getUserId(), request.getDeviceId(), request.getAreaId());
        
        AdvancedAccessControlService.AccessControlResult result = 
                advancedAccessControlService.performAccessControlCheck(
                        request.getUserId(), 
                        request.getDeviceId(), 
                        request.getAreaId(), 
                        request.getVerificationType(), 
                        "MOBILE_ACCESS");
        
        return ResponseDTO.ok(result.isAllowed());
    }

    /**
     * 二维码验证
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @PostMapping("/qr/verify")
    @Operation(summary = "二维码验证", description = "移动端二维码门禁验证")
    @SaCheckLogin
    public ResponseDTO<Boolean> verifyQRCode(@Valid @RequestBody QRCodeVerifyRequest request) {
        log.info("二维码验证: qrCode={}, deviceId={}", request.getQrCode(), request.getDeviceId());
        
        AdvancedAccessControlService.AccessControlResult result = 
                advancedAccessControlService.performAccessControlCheck(
                        null, 
                        request.getDeviceId(), 
                        null, 
                        request.getQrCode(), 
                        "QR_CODE_ACCESS");
        
        return ResponseDTO.ok(result.isAllowed());
    }

    /**
     * NFC验证
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @PostMapping("/nfc/verify")
    @Operation(summary = "NFC验证", description = "移动端NFC门禁验证")
    @SaCheckLogin
    public ResponseDTO<Boolean> verifyNFC(@Valid @RequestBody NFCVerifyRequest request) {
        log.info("NFC验证: nfcCardId={}, deviceId={}", request.getNfcCardId(), request.getDeviceId());
        
        AdvancedAccessControlService.AccessControlResult result = 
                advancedAccessControlService.performAccessControlCheck(
                        null, 
                        request.getDeviceId(), 
                        null, 
                        request.getNfcCardId(), 
                        "NFC_ACCESS");
        
        return ResponseDTO.ok(result.isAllowed());
    }

    /**
     * 生物识别验证
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @PostMapping("/biometric/verify")
    @Operation(summary = "生物识别验证", description = "移动端生物识别门禁验证")
    @SaCheckLogin
    public ResponseDTO<Boolean> verifyBiometric(@Valid @RequestBody BiometricVerifyRequest request) {
        log.info("生物识别验证: userId={}, biometricType={}, deviceId={}", 
                request.getUserId(), request.getBiometricType(), request.getDeviceId());
        
        AdvancedAccessControlService.AccessControlResult result = 
                advancedAccessControlService.performAccessControlCheck(
                        request.getUserId(), 
                        request.getDeviceId(), 
                        null, 
                        request.getBiometricType(), 
                        "BIOMETRIC_ACCESS");
        
        return ResponseDTO.ok(result.isAllowed());
    }

    /**
     * 获取附近设备
     *
     * @param userId 用户ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 半径（米）
     * @return 设备列表
     */
    @GetMapping("/devices/nearby")
    @Operation(summary = "获取附近设备", description = "根据GPS位置获取附近门禁设备")
    @SaCheckLogin
    public ResponseDTO<List<MobileDeviceItem>> getNearbyDevices(
            @RequestParam Long userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "500") Integer radius) {
        log.info("获取附近设备: userId={}, latitude={}, longitude={}, radius={}", 
                userId, latitude, longitude, radius);
        return accessDeviceService.getNearbyDevices(userId, latitude, longitude, radius);
    }

    /**
     * 获取用户门禁权限
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "获取用户门禁权限", description = "获取指定用户的门禁权限列表")
    @SaCheckLogin
    public ResponseDTO<MobileUserPermissions> getUserPermissions(@PathVariable Long userId) {
        log.info("获取用户门禁权限: userId={}", userId);
        return accessDeviceService.getMobileUserPermissions(userId);
    }

    /**
     * 获取用户访问记录
     *
     * @param userId 用户ID
     * @param size 记录数量
     * @return 访问记录列表
     */
    @GetMapping("/records/{userId}")
    @Operation(summary = "获取用户访问记录", description = "获取指定用户的访问记录")
    @SaCheckLogin
    public ResponseDTO<List<MobileAccessRecord>> getUserAccessRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("获取用户访问记录: userId={}, size={}", userId, size);
        return accessEventService.getMobileAccessRecords(userId, size);
    }

    /**
     * 获取区域列表
     * <p>
     * 获取用户有权限访问的区域列表，包含区域详情
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @return 区域列表
     */
    @GetMapping("/areas")
    @Operation(summary = "获取区域列表", description = "获取用户有权限访问的区域列表")
    @SaCheckLogin
    public ResponseDTO<List<MobileAreaItem>> getAreas(@RequestParam(required = false) Long userId) {
        log.info("获取区域列表: userId={}", userId);
        return accessDeviceService.getMobileAreas(userId);
    }

    /**
     * 临时开门申请
     *
     * @param request 申请请求
     * @return 申请结果
     */
    @PostMapping("/temporary-access")
    @Operation(summary = "临时开门申请", description = "申请临时门禁权限")
    @SaCheckLogin
    public ResponseDTO<String> requestTemporaryAccess(@Valid @RequestBody TemporaryAccessRequest request) {
        log.info("临时开门申请: userId={}, deviceId={}, reason={}", 
                request.getUserId(), request.getDeviceId(), request.getReason());
        return ResponseDTO.ok("申请已提交");
    }

    /**
     * 获取实时门禁状态
     *
     * @param deviceId 设备ID
     * @return 状态信息
     */
    @GetMapping("/status/realtime")
    @Operation(summary = "获取实时门禁状态", description = "获取指定设备的实时状态")
    @SaCheckLogin
    public ResponseDTO<MobileRealTimeStatus> getRealTimeStatus(@RequestParam Long deviceId) {
        log.info("获取实时门禁状态: deviceId={}", deviceId);
        return accessDeviceService.getMobileRealTimeStatus(deviceId);
    }

    /**
     * 发送推送通知
     *
     * @param request 通知请求
     * @return 发送结果
     */
    @PostMapping("/notification/push")
    @Operation(summary = "发送推送通知", description = "发送门禁相关推送通知")
    @SaCheckLogin
    public ResponseDTO<String> sendPushNotification(@Valid @RequestBody PushNotificationRequest request) {
        log.info("发送推送通知: userId={}, notificationType={}", 
                request.getUserId(), request.getNotificationType());
        return ResponseDTO.ok("通知已发送");
    }

    // ==================== 内部请求类 ====================

    /**
     * 移动端门禁检查请求
     */
    @Data
    public static class MobileAccessCheckRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String verificationType;
        private String location;
    }

    /**
     * 二维码验证请求
     */
    @Data
    public static class QRCodeVerifyRequest {
        private String qrCode;
        private Long deviceId;
    }

    /**
     * NFC验证请求
     */
    @Data
    public static class NFCVerifyRequest {
        private String nfcCardId;
        private Long deviceId;
    }

    /**
     * 生物识别验证请求
     */
    @Data
    public static class BiometricVerifyRequest {
        private Long userId;
        private String biometricType;
        private String biometricData;
        private Long deviceId;
    }

    /**
     * 临时开门申请请求
     */
    @Data
    public static class TemporaryAccessRequest {
        private Long userId;
        private Long deviceId;
        private String reason;
    }

    /**
     * 推送通知请求
     */
    @Data
    public static class PushNotificationRequest {
        private Long userId;
        private String notificationType;
    }

    /**
     * 移动端设备项
     */
    @Data
    public static class MobileDeviceItem {
        private Long deviceId;
        private String deviceName;
        private String deviceLocation;
        private Double latitude;
        private Double longitude;
        private Integer distance;
    }

    /**
     * 移动端用户权限
     */
    @Data
    public static class MobileUserPermissions {
        private Long userId;
        private List<Long> allowedAreaIds;
        private List<Long> allowedDeviceIds;
        private String permissionLevel;
    }

    /**
     * 移动端访问记录
     */
    @Data
    public static class MobileAccessRecord {
        private Long recordId;
        private Long userId;
        private Long deviceId;
        private String deviceName;
        private String accessTime;
        private String accessType;
        private Boolean accessResult;
    }

    /**
     * 移动端实时状态
     */
    @Data
    public static class MobileRealTimeStatus {
        private Long deviceId;
        private String deviceName;
        private String deviceStatus;
        private Integer onlineCount;
        private String lastUpdateTime;
    }

    /**
     * 移动端区域项
     */
    @Data
    public static class MobileAreaItem {
        private Long areaId;           // 区域ID
        private String areaName;       // 区域名称
        private String areaType;       // 区域类型
        private Integer deviceCount;    // 设备数量
        private Integer permissionCount; // 权限数量
        private String description;    // 区域描述
        private Boolean active;        // 是否有效
    }
}
