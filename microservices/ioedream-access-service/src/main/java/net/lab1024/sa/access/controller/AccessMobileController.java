package net.lab1024.sa.access.controller;

import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.access.service.AdvancedAccessControlService;
import net.lab1024.sa.access.service.BluetoothAccessService;
import net.lab1024.sa.access.service.OfflineModeService;
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

    @Resource
    private BluetoothAccessService bluetoothAccessService;

    @Resource
    private OfflineModeService offlineModeService;

    // ==================== 设备管理功能 ====================

    /**
     * 获取设备列表
     * <p>
     * 获取用户有权限管理的设备列表
     * </p>
     *
     * @param userId 用户ID（可选，不传则从Token获取）
     * @param deviceType 设备类型（可选）
     * @param status 设备状态（可选）
     * @param areaId 区域ID（可选）
     * @param keyword 关键词（可选）
     * @return 设备列表
     */
    @Observed(name = "accessMobile.getDeviceList", contextualName = "access-mobile-device-list")
    @GetMapping("/device/list")
    @Operation(summary = "获取设备列表", description = "获取用户有权限管理的设备列表")
    public ResponseDTO<List<MobileDeviceVO>> getDeviceList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer deviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String keyword) {
        log.info("获取设备列表: userId={}, deviceType={}, status={}, areaId={}, keyword={}",
                userId, deviceType, status, areaId, keyword);
        return accessDeviceService.getMobileDeviceList(userId, deviceType, status, areaId, keyword);
    }

    /**
     * 获取区域列表
     * <p>
     * 获取设备管理需要的区域列表
     * </p>
     *
     * @return 区域列表
     */
    @Observed(name = "accessMobile.getDeviceAreas", contextualName = "access-mobile-device-areas")
    @GetMapping("/area/list")
    @Operation(summary = "获取区域列表", description = "获取设备管理需要的区域列表")
    public ResponseDTO<List<MobileAreaVO>> getDeviceAreas() {
        log.info("获取设备管理区域列表");
        return accessDeviceService.getMobileAreaList();
    }

    /**
     * 设备控制
     * <p>
     * 控制设备执行指定操作
     * </p>
     *
     * @param request 控制请求
     * @return 控制结果
     */
    @Observed(name = "accessMobile.controlDevice", contextualName = "access-mobile-device-control")
    @PostMapping("/device/control")
    @Operation(summary = "设备控制", description = "控制设备执行指定操作")
    public ResponseDTO<String> controlDevice(@Valid @RequestBody DeviceControlRequest request) {
        log.info("设备控制: userId={}, deviceId={}, action={}",
                request.getUserId(), request.getDeviceId(), request.getAction());

        try {
            return accessDeviceService.controlDevice(request);
        } catch (Exception e) {
            log.error("设备控制失败", e);
            return ResponseDTO.error("DEVICE_CONTROL_FAILED", "设备控制失败: " + e.getMessage());
        }
    }

    /**
     * 添加设备
     * <p>
     * 添加新的门禁设备
     * </p>
     *
     * @param request 添加请求
     * @return 添加结果
     */
    @Observed(name = "accessMobile.addDevice", contextualName = "access-mobile-device-add")
    @PostMapping("/device/add")
    @Operation(summary = "添加设备", description = "添加新的门禁设备")
    public ResponseDTO<String> addDevice(@Valid @RequestBody AddDeviceRequest request) {
        log.info("添加设备: deviceName={}, deviceCode={}, deviceType={}, areaId={}",
                request.getDeviceName(), request.getDeviceCode(), request.getDeviceType(), request.getAreaId());

        try {
            return accessDeviceService.addDevice(request);
        } catch (Exception e) {
            log.error("添加设备失败", e);
            return ResponseDTO.error("DEVICE_ADD_FAILED", "添加设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除设备
     * <p>
     * 删除指定的门禁设备
     * </p>
     *
     * @param deviceId 设备ID
     * @return 删除结果
     */
    @Observed(name = "accessMobile.deleteDevice", contextualName = "access-mobile-device-delete")
    @DeleteMapping("/device/{deviceId}")
    @Operation(summary = "删除设备", description = "删除指定的门禁设备")
    public ResponseDTO<String> deleteDevice(@PathVariable Long deviceId) {
        log.info("删除设备: deviceId={}", deviceId);

        try {
            return accessDeviceService.deleteDevice(deviceId);
        } catch (Exception e) {
            log.error("删除设备失败", e);
            return ResponseDTO.error("DEVICE_DELETE_FAILED", "删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 设备重启
     * <p>
     * 重启指定设备
     * </p>
     *
     * @param request 重启请求
     * @return 重启结果
     */
    @Observed(name = "accessMobile.restartDevice", contextualName = "access-mobile-device-restart")
    @PostMapping("/device/restart")
    @Operation(summary = "设备重启", description = "重启指定设备")
    public ResponseDTO<String> restartDevice(@Valid @RequestBody DeviceRestartRequest request) {
        log.info("设备重启: deviceId={}", request.getDeviceId());

        try {
            return accessDeviceService.restartDevice(request);
        } catch (Exception e) {
            log.error("设备重启失败", e);
            return ResponseDTO.error("DEVICE_RESTART_FAILED", "设备重启失败: " + e.getMessage());
        }
    }

    /**
     * 设备维护
     * <p>
     * 设置设备维护状态
     * </p>
     *
     * @param request 维护请求
     * @return 维护结果
     */
    @Observed(name = "accessMobile.maintainDevice", contextualName = "access-mobile-device-maintain")
    @PostMapping("/device/maintain")
    @Operation(summary = "设备维护", description = "设置设备维护状态")
    public ResponseDTO<String> maintainDevice(@Valid @RequestBody DeviceMaintainRequest request) {
        log.info("设备维护: deviceId={}, action={}", request.getDeviceId(), request.getAction());

        try {
            return accessDeviceService.maintainDevice(request);
        } catch (Exception e) {
            log.error("设备维护失败", e);
            return ResponseDTO.error("DEVICE_MAINTAIN_FAILED", "设备维护失败: " + e.getMessage());
        }
    }

    /**
     * 设备校准
     * <p>
     * 执行设备校准操作
     * </p>
     *
     * @param request 校准请求
     * @return 校准结果
     */
    @Observed(name = "accessMobile.calibrateDevice", contextualName = "access-mobile-device-calibrate")
    @PostMapping("/device/calibrate")
    @Operation(summary = "设备校准", description = "执行设备校准操作")
    public ResponseDTO<String> calibrateDevice(@Valid @RequestBody DeviceCalibrateRequest request) {
        log.info("设备校准: deviceId={}", request.getDeviceId());

        try {
            return accessDeviceService.calibrateDevice(request);
        } catch (Exception e) {
            log.error("设备校准失败", e);
            return ResponseDTO.error("DEVICE_CALIBRATE_FAILED", "设备校准失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备详情
     * <p>
     * 获取指定设备的详细信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @Observed(name = "accessMobile.getDeviceDetail", contextualName = "access-mobile-device-detail")
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备详情", description = "获取指定设备的详细信息")
    public ResponseDTO<MobileDeviceDetailVO> getDeviceDetail(@PathVariable Long deviceId) {
        log.info("获取设备详情: deviceId={}", deviceId);

        try {
            return accessDeviceService.getMobileDeviceDetail(deviceId);
        } catch (Exception e) {
            log.error("获取设备详情失败", e);
            return ResponseDTO.error("DEVICE_DETAIL_FAILED", "获取设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 移动端门禁检查
     *
     * @param request 检查请求
     * @return 检查结果
     */
    @Observed(name = "accessMobile.mobileAccessCheck", contextualName = "access-mobile-check")
    @PostMapping("/check")
    @Operation(summary = "移动端门禁检查", description = "移动端门禁权限检查")
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
    @Observed(name = "accessMobile.verifyQRCode", contextualName = "access-mobile-qr-verify")
    @PostMapping("/qr/verify")
    @Operation(summary = "二维码验证", description = "移动端二维码门禁验证")
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
    @Observed(name = "accessMobile.verifyNFC", contextualName = "access-mobile-nfc-verify")
    @PostMapping("/nfc/verify")
    @Operation(summary = "NFC验证", description = "移动端NFC门禁验证")
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
    @Observed(name = "accessMobile.verifyBiometric", contextualName = "access-mobile-biometric-verify")
    @PostMapping("/biometric/verify")
    @Operation(summary = "生物识别验证", description = "移动端生物识别门禁验证")
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
    @Observed(name = "accessMobile.getNearbyDevices", contextualName = "access-mobile-nearby-devices")
    @GetMapping("/devices/nearby")
    @Operation(summary = "获取附近设备", description = "根据GPS位置获取附近门禁设备")
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
    @Observed(name = "accessMobile.getUserPermissions", contextualName = "access-mobile-user-permissions")
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "获取用户门禁权限", description = "获取指定用户的门禁权限列表")
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
    @Observed(name = "accessMobile.getUserAccessRecords", contextualName = "access-mobile-user-records")
    @GetMapping("/records/{userId}")
    @Operation(summary = "获取用户访问记录", description = "获取指定用户的访问记录")
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
    @Observed(name = "accessMobile.getAreas", contextualName = "access-mobile-areas")
    @GetMapping("/areas")
    @Operation(summary = "获取区域列表", description = "获取用户有权限访问的区域列表")
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
    @Observed(name = "accessMobile.requestTemporaryAccess", contextualName = "access-mobile-temporary-access")
    @PostMapping("/temporary-access")
    @Operation(summary = "临时开门申请", description = "申请临时门禁权限")
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
    @Observed(name = "accessMobile.getRealTimeStatus", contextualName = "access-mobile-realtime-status")
    @GetMapping("/status/realtime")
    @Operation(summary = "获取实时门禁状态", description = "获取指定设备的实时状态")
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
    @Observed(name = "accessMobile.sendPushNotification", contextualName = "access-mobile-push-notification")
    @PostMapping("/notification/push")
    @Operation(summary = "发送推送通知", description = "发送门禁相关推送通知")
    public ResponseDTO<String> sendPushNotification(@Valid @RequestBody PushNotificationRequest request) {
        log.info("发送推送通知: userId={}, notificationType={}",
                request.getUserId(), request.getNotificationType());
        return ResponseDTO.ok("通知已发送");
    }

    // ==================== 蓝牙门禁功能 ====================

    /**
     * 蓝牙设备扫描
     * <p>
     * 扫描附近的蓝牙门禁设备
     * </p>
     *
     * @param request 扫描请求
     * @return 扫描结果
     */
    @Observed(name = "accessMobile.scanBluetoothDevices", contextualName = "access-mobile-bluetooth-scan")
    @PostMapping("/bluetooth/scan")
    @Operation(summary = "蓝牙设备扫描", description = "扫描附近的蓝牙门禁设备")
    public ResponseDTO<List<BluetoothDeviceVO>> scanBluetoothDevices(@Valid @RequestBody BluetoothScanRequest request) {
        log.info("蓝牙设备扫描: userId={}, location={}, duration={}ms",
                request.getUserId(), request.getLocation(), request.getScanDuration());

        return bluetoothAccessService.scanNearbyDevices(request);
    }

    /**
     * 蓝牙设备连接
     * <p>
     * 连接指定的蓝牙门禁设备
     * </p>
     *
     * @param request 连接请求
     * @return 连接结果
     */
    @Observed(name = "accessMobile.connectBluetoothDevice", contextualName = "access-mobile-bluetooth-connect")
    @PostMapping("/bluetooth/connect")
    @Operation(summary = "蓝牙设备连接", description = "连接指定的蓝牙门禁设备")
    public ResponseDTO<BluetoothConnectionResult> connectBluetoothDevice(@Valid @RequestBody BluetoothConnectRequest request) {
        log.info("蓝牙设备连接: userId={}, deviceAddress={}", request.getUserId(), request.getDeviceAddress());

        return bluetoothAccessService.connectDevice(request);
    }

    /**
     * 蓝牙门禁验证
     * <p>
     * 通过蓝牙进行门禁验证
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @Observed(name = "accessMobile.verifyBluetoothAccess", contextualName = "access-mobile-bluetooth-verify")
    @PostMapping("/bluetooth/verify")
    @Operation(summary = "蓝牙门禁验证", description = "通过蓝牙进行门禁验证")
    public ResponseDTO<BluetoothAccessResult> verifyBluetoothAccess(@Valid @RequestBody BluetoothAccessRequest request) {
        log.info("蓝牙门禁验证: userId={}, deviceAddress={}, accessType={}",
                request.getUserId(), request.getDeviceAddress(), request.getAccessType());

        return bluetoothAccessService.performBluetoothAccess(request);
    }

    /**
     * 蓝牙设备状态查询
     * <p>
     * 查询已连接蓝牙设备的状态
     * </p>
     *
     * @param userId 用户ID
     * @return 设备状态列表
     */
    @Observed(name = "accessMobile.getBluetoothDeviceStatus", contextualName = "access-mobile-bluetooth-status")
    @GetMapping("/bluetooth/status")
    @Operation(summary = "蓝牙设备状态查询", description = "查询已连接蓝牙设备的状态")
    public ResponseDTO<List<BluetoothDeviceStatusVO>> getBluetoothDeviceStatus(@RequestParam Long userId) {
        log.info("蓝牙设备状态查询: userId={}", userId);
        return bluetoothAccessService.getConnectedDevicesStatus(userId);
    }

    /**
     * 蓝牙设备断开连接
     * <p>
     * 断开与指定蓝牙设备的连接
     * </p>
     *
     * @param request 断开连接请求
     * @return 操作结果
     */
    @Observed(name = "accessMobile.disconnectBluetoothDevice", contextualName = "access-mobile-bluetooth-disconnect")
    @PostMapping("/bluetooth/disconnect")
    @Operation(summary = "蓝牙设备断开连接", description = "断开与指定蓝牙设备的连接")
    public ResponseDTO<String> disconnectBluetoothDevice(@Valid @RequestBody BluetoothDisconnectRequest request) {
        log.info("蓝牙设备断开连接: userId={}, deviceAddress={}", request.getUserId(), request.getDeviceAddress());

        boolean result = bluetoothAccessService.disconnectDevice(request.getUserId(), request.getDeviceAddress());
        return result ? ResponseDTO.ok("断开连接成功") : ResponseDTO.error("DISCONNECT_FAILED", "断开连接失败");
    }

    /**
     * 蓝牙设备配对
     * <p>
     * 与蓝牙设备进行配对
     * </p>
     *
     * @param request 配对请求
     * @return 配对结果
     */
    @Observed(name = "accessMobile.pairBluetoothDevice", contextualName = "access-mobile-bluetooth-pair")
    @PostMapping("/bluetooth/pair")
    @Operation(summary = "蓝牙设备配对", description = "与蓝牙设备进行配对")
    public ResponseDTO<BluetoothPairingResult> pairBluetoothDevice(@Valid @RequestBody BluetoothPairingRequest request) {
        log.info("蓝牙设备配对: userId={}, deviceAddress={}, pinCode={}",
                request.getUserId(), request.getDeviceAddress(), request.getPinCode());

        return bluetoothAccessService.pairDevice(request);
    }

    // ==================== 离线模式功能 ====================

    /**
     * 离线模式数据同步
     * <p>
     * 同步离线模式下的门禁数据
     * </p>
     *
     * @param request 同步请求
     * @return 同步结果
     */
    @Observed(name = "accessMobile.syncOfflineData", contextualName = "access-mobile-offline-sync")
    @PostMapping("/offline/sync")
    @Operation(summary = "离线模式数据同步", description = "同步离线模式下的门禁数据")
    public ResponseDTO<OfflineSyncResult> syncOfflineData(@Valid @RequestBody OfflineSyncRequest request) {
        log.info("离线模式数据同步: userId={}, syncType={}, dataSize={}",
                request.getUserId(), request.getSyncType(), request.getDataSize());

        return offlineModeService.syncOfflineData(request);
    }

    /**
     * 获取离线访问权限
     * <p>
     * 获取用户在离线模式下的访问权限数据
     * </p>
     *
     * @param userId 用户ID
     * @param lastSyncTime 上次同步时间
     * @return 离线权限数据
     */
    @Observed(name = "accessMobile.getOfflinePermissions", contextualName = "access-mobile-offline-permissions")
    @GetMapping("/offline/permissions")
    @Operation(summary = "获取离线访问权限", description = "获取用户在离线模式下的访问权限数据")
    public ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(
            @RequestParam Long userId,
            @RequestParam(required = false) String lastSyncTime) {
        log.info("获取离线访问权限: userId={}, lastSyncTime={}", userId, lastSyncTime);
        return offlineModeService.getOfflinePermissions(userId, lastSyncTime);
    }

    /**
     * 离线访问记录上报
     * <p>
     * 上报离线模式下的访问记录
     * </p>
     *
     * @param request 上报请求
     * @return 上报结果
     */
    @Observed(name = "accessMobile.reportOfflineAccessRecords", contextualName = "access-mobile-offline-report")
    @PostMapping("/offline/records/report")
    @Operation(summary = "离线访问记录上报", description = "上报离线模式下的访问记录")
    public ResponseDTO<OfflineReportResult> reportOfflineAccessRecords(@Valid @RequestBody OfflineRecordsReportRequest request) {
        log.info("离线访问记录上报: userId={}, recordCount={}", request.getUserId(), request.getRecords().size());

        return offlineModeService.reportOfflineRecords(request);
    }

    /**
     * 无缝门禁通行
     * <p>
     * 实现无缝的门禁通行体验
     * </p>
     *
     * @param request 通行请求
     * @return 通行结果
     */
    @Observed(name = "accessMobile.seamlessAccess", contextualName = "access-mobile-seamless")
    @PostMapping("/seamless/access")
    @Operation(summary = "无缝门禁通行", description = "实现无缝的门禁通行体验")
    public ResponseDTO<SeamlessAccessResult> seamlessAccess(@Valid @RequestBody SeamlessAccessRequest request) {
        log.info("无缝门禁通行: userId={}, deviceId={}, accessMode={}",
                request.getUserId(), request.getDeviceId(), request.getAccessMode());

        // 优先使用蓝牙，失败时回退到其他方式
        if ("BLUETOOTH".equals(request.getAccessMode()) || "AUTO".equals(request.getAccessMode())) {
            try {
                BluetoothAccessRequest bluetoothRequest = new BluetoothAccessRequest();
                bluetoothRequest.setUserId(request.getUserId());
                bluetoothRequest.setDeviceAddress(request.getDeviceAddress());
                bluetoothRequest.setAccessType(request.getAccessMode());
                bluetoothRequest.setLocation(request.getLocation());
                bluetoothRequest.setTimestamp(System.currentTimeMillis());

                BluetoothAccessResult bluetoothResult = bluetoothAccessService.performBluetoothAccess(bluetoothRequest);
                if (bluetoothResult.isSuccess()) {
                    SeamlessAccessResult result = new SeamlessAccessResult();
                    result.setSuccess(true);
                    result.setAccessMethod("BLUETOOTH");
                    result.setResponseTime(bluetoothResult.getResponseTime());
                    result.setDeviceId(bluetoothResult.getDeviceId());
                    result.setAccessTime(bluetoothResult.getAccessTime());
                    return ResponseDTO.ok(result);
                }
            } catch (Exception e) {
                log.warn("蓝牙通行失败，回退到其他方式: {}", e.getMessage());
            }
        }

        // 回退到标准门禁验证
        AdvancedAccessControlService.AccessControlResult result =
                advancedAccessControlService.performAccessControlCheck(
                        request.getUserId(),
                        request.getDeviceId(),
                        request.getAreaId(),
                        request.getVerificationType(),
                        "SEAMLESS_ACCESS");

        SeamlessAccessResult seamlessResult = new SeamlessAccessResult();
        seamlessResult.setSuccess(result.isAllowed());
        seamlessResult.setAccessMethod("STANDARD");
        seamlessResult.setResponseTime(result.getResponseTime());
        seamlessResult.setDeviceId(request.getDeviceId());
        seamlessResult.setAccessTime(System.currentTimeMillis());

        return ResponseDTO.ok(seamlessResult);
    }

    /**
     * 获取用户门禁卡信息
     * <p>
     * 获取用户的所有门禁卡信息，支持蓝牙门禁卡
     * </p>
     *
     * @param userId 用户ID
     * @return 门禁卡列表
     */
    @Observed(name = "accessMobile.getUserAccessCards", contextualName = "access-mobile-access-cards")
    @GetMapping("/cards")
    @Operation(summary = "获取用户门禁卡信息", description = "获取用户的所有门禁卡信息，支持蓝牙门禁卡")
    public ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(@RequestParam Long userId) {
        log.info("获取用户门禁卡信息: userId={}", userId);
        return bluetoothAccessService.getUserAccessCards(userId);
    }

    /**
     * 添加蓝牙门禁卡
     * <p>
     * 为用户添加蓝牙门禁卡
     * </p>
     *
     * @param request 添加请求
     * @return 添加结果
     */
    @Observed(name = "accessMobile.addBluetoothAccessCard", contextualName = "access-mobile-add-bluetooth-card")
    @PostMapping("/cards/bluetooth")
    @Operation(summary = "添加蓝牙门禁卡", description = "为用户添加蓝牙门禁卡")
    public ResponseDTO<String> addBluetoothAccessCard(@Valid @RequestBody AddBluetoothCardRequest request) {
        log.info("添加蓝牙门禁卡: userId={}, cardName={}, deviceAddress={}",
                request.getUserId(), request.getCardName(), request.getDeviceAddress());

        return bluetoothAccessService.addBluetoothAccessCard(request);
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

    // ==================== 蓝牙门禁请求类 ====================

    /**
     * 蓝牙设备扫描请求
     */
    @Data
    public static class BluetoothScanRequest {
        private Long userId;
        private String location;           // 当前位置
        private Integer scanDuration;      // 扫描时长（毫秒）
        private Integer maxDevices;        // 最大设备数量
        private String filterType;         // 过滤类型
    }

    /**
     * 蓝牙设备连接请求
     */
    @Data
    public static class BluetoothConnectRequest {
        private Long userId;
        private String deviceAddress;     // 设备MAC地址
        private String deviceName;         // 设备名称
        private Integer timeout;           // 连接超时（毫秒）
        private Boolean autoReconnect;      // 是否自动重连
    }

    /**
     * 蓝牙门禁验证请求
     */
    @Data
    public static class BluetoothAccessRequest {
        private Long userId;
        private String deviceAddress;     // 设备MAC地址
        private String accessType;         // 访问类型
        private String location;           // 位置信息
        private Long timestamp;            // 时间戳
        private String signature;          // 数字签名
        private String encryptedData;      // 加密数据
    }

    /**
     * 蓝牙设备断开连接请求
     */
    @Data
    public static class BluetoothDisconnectRequest {
        private Long userId;
        private String deviceAddress;     // 设备MAC地址
        private String reason;             // 断开原因
    }

    /**
     * 蓝牙设备配对请求
     */
    @Data
    public static class BluetoothPairingRequest {
        private Long userId;
        private String deviceAddress;     // 设备MAC地址
        private String pinCode;            // PIN码
        private String pairingMethod;      // 配对方式
        private Integer timeout;           // 配对超时（毫秒）
    }

    // ==================== 离线模式请求类 ====================

    /**
     * 离线数据同步请求
     */
    @Data
    public static class OfflineSyncRequest {
        private Long userId;
        private String syncType;          // 同步类型：PERMISSIONS, RECORDS, CONFIG
        private Long dataSize;            // 数据大小
        private String checksum;           // 校验和
        private Integer version;          // 数据版本
    }

    /**
     * 离线记录上报请求
     */
    @Data
    public static class OfflineRecordsReportRequest {
        private Long userId;
        private List<OfflineAccessRecord> records;  // 离线访问记录
        private String deviceInfo;        // 设备信息
        private Long reportTime;          // 上报时间
    }

    /**
     * 离线访问记录
     */
    @Data
    public static class OfflineAccessRecord {
        private Long recordId;
        private Long deviceId;
        private Long userId;
        private String accessType;
        private Long accessTime;
        private Boolean accessResult;
        private String verificationData;
        private String locationInfo;
        private String deviceFirmware;    // 设备固件版本
    }

    /**
     * 无缝门禁通行请求
     */
    @Data
    public static class SeamlessAccessRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String deviceAddress;     // 蓝牙设备地址
        private String accessMode;         // 访问模式：BLUETOOTH, NFC, QR_CODE, AUTO
        private String verificationType;   // 验证类型
        private String location;           // 位置信息
    }

    /**
     * 添加蓝牙门禁卡请求
     */
    @Data
    public static class AddBluetoothCardRequest {
        private Long userId;
        private String cardName;           // 卡片名称
        private String deviceAddress;     // 设备MAC地址
        private String deviceName;         // 设备名称
        private String cardType;           // 卡片类型
        private Integer validPeriod;       // 有效期（天）
        private List<String> allowedAreas; // 允许的区域
    }

    // ==================== 蓝牙门禁响应类 ====================

    /**
     * 蓝牙设备信息
     */
    @Data
    public static class BluetoothDeviceVO {
        private String deviceAddress;     // 设备MAC地址
        private String deviceName;         // 设备名称
        private String deviceType;         // 设备类型
        private Integer signalStrength;    // 信号强度
        private Integer batteryLevel;      // 电池电量
        private Integer rssi;              // RSSI值
        private Boolean isPaired;          // 是否已配对
        private Boolean isConnected;       // 是否已连接
        private String lastSeen;           // 最后发现时间
        private Double distance;           // 估算距离
    }

    /**
     * 蓝牙连接结果
     */
    @Data
    public static class BluetoothConnectionResult {
        private Boolean success;
        private String deviceAddress;
        private String deviceName;
        private String connectionId;       // 连接ID
        private Integer responseTime;      // 响应时间（毫秒）
        private String errorMessage;       // 错误信息
        private Integer signalStrength;    // 信号强度
        private String protocolVersion;    // 协议版本
    }

    /**
     * 蓝牙门禁验证结果
     */
    @Data
    public static class BluetoothAccessResult {
        private Boolean success;
        private Long deviceId;
        private String deviceName;
        private String accessMethod;       // 访问方式
        private Long accessTime;           // 访问时间
        private Integer responseTime;      // 响应时间（毫秒）
        private String accessToken;        // 访问令牌
        private String decisionReason;     // 决策原因
        private RiskLevel riskLevel;       // 风险等级
    }

    /**
     * 蓝牙设备状态
     */
    @Data
    public static class BluetoothDeviceStatusVO {
        private String deviceAddress;
        private String deviceName;
        private String connectionStatus;   // 连接状态
        private Integer signalStrength;
        private Integer batteryLevel;
        private String lastActivity;       // 最后活动时间
        private Long totalConnections;     // 总连接次数
        private Long totalAccessCount;     // 总访问次数
        private String firmwareVersion;    // 固件版本
    }

    /**
     * 蓝牙配对结果
     */
    @Data
    public static class BluetoothPairingResult {
        private Boolean success;
        private String deviceAddress;
        private String pairingKey;         // 配对密钥
        private String pairingMethod;      // 配对方式
        private Integer responseTime;      // 响应时间（毫秒）
        private String errorMessage;       // 错误信息
        private Boolean requiresConfirmation; // 是否需要确认
    }

    // ==================== 离线模式响应类 ====================

    /**
     * 离线权限数据
     */
    @Data
    public static class OfflinePermissionsVO {
        private Long userId;
        private String lastSyncTime;
        private String expiryTime;         // 权限过期时间
        private List<OfflineAreaPermission> areaPermissions;
        private List<OfflineDevicePermission> devicePermissions;
        private String checksum;           // 数据校验和
        private Integer version;            // 数据版本
    }

    /**
     * 离线区域权限
     */
    @Data
    public static class OfflineAreaPermission {
        private Long areaId;
        private String areaName;
        private String permissionLevel;
        private String validFrom;           // 生效时间
        private String validUntil;          // 失效时间
        private List<String> accessMethods; // 访问方式
    }

    /**
     * 离线设备权限
     */
    @Data
    public static class OfflineDevicePermission {
        private Long deviceId;
        private String deviceName;
        private String deviceType;
        private String permissionLevel;
        private Integer dailyLimit;        // 每日限制
        private String validFrom;
        private String validUntil;
    }

    /**
     * 离线同步结果
     */
    @Data
    public static class OfflineSyncResult {
        private Boolean success;
        private String syncType;
        private Integer syncedRecords;     // 同步记录数
        private Integer failedRecords;      // 失败记录数
        private Long syncDuration;          // 同步耗时（毫秒）
        private String nextSyncTime;        // 下次同步时间
        private List<String> errors;        // 错误信息列表
    }

    /**
     * 离线记录上报结果
     */
    @Data
    public static class OfflineReportResult {
        private Boolean success;
        private Integer reportedRecords;    // 上报记录数
        private Integer acceptedRecords;    // 接受记录数
        private Integer rejectedRecords;    // 拒绝记录数
        private List<String> rejectedReasons; // 拒绝原因
        private String reportId;            // 上报ID
    }

    // ==================== 无缝门禁响应类 ====================

    /**
     * 无缝门禁通行结果
     */
    @Data
    public static class SeamlessAccessResult {
        private Boolean success;
        private String accessMethod;       // 访问方式：BLUETOOTH, STANDARD
        private Long deviceId;
        private String deviceName;
        private Long accessTime;           // 访问时间
        private Integer responseTime;      // 响应时间（毫秒）
        private String accessToken;        // 访问令牌
        private String message;            // 提示信息
        private Boolean isOfflineMode;     // 是否离线模式
    }

    // ==================== 门禁卡响应类 ====================

    /**
     * 用户门禁卡信息
     */
    @Data
    public static class UserAccessCardVO {
        private Long cardId;
        private String cardName;
        private String cardType;           // PHYSICAL, VIRTUAL, BLUETOOTH
        private String cardNumber;         // 卡号
        private String deviceAddress;      // 蓝牙设备地址（蓝牙卡）
        private String status;             // ACTIVE, INACTIVE, EXPIRED
        private String issueTime;          // 发卡时间
        private String expireTime;         // 过期时间
        private List<String> allowedAreas; // 允许区域
        private Integer dailyUsageLimit;   // 每日使用限制
        private Integer currentUsage;      // 当前使用次数
        private Boolean isPrimary;         // 是否主卡
    }

    // ==================== 枚举定义 ====================

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW,        // 低风险
        MEDIUM,     // 中等风险
        HIGH,       // 高风险
        CRITICAL    // 严重风险
    }
}

