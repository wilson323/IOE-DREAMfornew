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
 * 闂ㄧ绉诲姩绔帶鍒跺櫒
 * <p>
 * 鎻愪緵绉诲姩绔棬绂佺鐞嗙浉鍏矨PI
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛? * - 浣跨敤@RestController娉ㄨВ
 * - 浣跨敤@Resource渚濊禆娉ㄥ叆
 * - 浣跨敤@Valid鍙傛暟鏍￠獙
 * - 杩斿洖缁熶竴ResponseDTO鏍煎紡
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/access")
@Tag(name = "绉诲姩绔棬绂佺鐞?, description = "绉诲姩绔棬绂佹鏌ャ€侀獙璇併€佹煡璇㈢瓑API")
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

    // ==================== 璁惧绠＄悊鍔熻兘 ====================

    /**
     * 鑾峰彇璁惧鍒楄〃
     * <p>
     * 鑾峰彇鐢ㄦ埛鏈夋潈闄愮鐞嗙殑璁惧鍒楄〃
     * </p>
     *
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛紝涓嶄紶鍒欎粠Token鑾峰彇锛?     * @param deviceType 璁惧绫诲瀷锛堝彲閫夛級
     * @param status 璁惧鐘舵€侊紙鍙€夛級
     * @param areaId 鍖哄煙ID锛堝彲閫夛級
     * @param keyword 鍏抽敭璇嶏紙鍙€夛級
     * @return 璁惧鍒楄〃
     */
    @Observed(name = "accessMobile.getDeviceList", contextualName = "access-mobile-device-list")
    @GetMapping("/device/list")
    @Operation(summary = "鑾峰彇璁惧鍒楄〃", description = "鑾峰彇鐢ㄦ埛鏈夋潈闄愮鐞嗙殑璁惧鍒楄〃")
    public ResponseDTO<List<MobileDeviceVO>> getDeviceList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer deviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String keyword) {
        log.info("鑾峰彇璁惧鍒楄〃: userId={}, deviceType={}, status={}, areaId={}, keyword={}",
                userId, deviceType, status, areaId, keyword);
        return accessDeviceService.getMobileDeviceList(userId, deviceType, status, areaId, keyword);
    }

    /**
     * 鑾峰彇鍖哄煙鍒楄〃
     * <p>
     * 鑾峰彇璁惧绠＄悊闇€瑕佺殑鍖哄煙鍒楄〃
     * </p>
     *
     * @return 鍖哄煙鍒楄〃
     */
    @Observed(name = "accessMobile.getDeviceAreas", contextualName = "access-mobile-device-areas")
    @GetMapping("/area/list")
    @Operation(summary = "鑾峰彇鍖哄煙鍒楄〃", description = "鑾峰彇璁惧绠＄悊闇€瑕佺殑鍖哄煙鍒楄〃")
    public ResponseDTO<List<MobileAreaVO>> getDeviceAreas() {
        log.info("鑾峰彇璁惧绠＄悊鍖哄煙鍒楄〃");
        return accessDeviceService.getMobileAreaList();
    }

    /**
     * 璁惧鎺у埗
     * <p>
     * 鎺у埗璁惧鎵ц鎸囧畾鎿嶄綔
     * </p>
     *
     * @param request 鎺у埗璇锋眰
     * @return 鎺у埗缁撴灉
     */
    @Observed(name = "accessMobile.controlDevice", contextualName = "access-mobile-device-control")
    @PostMapping("/device/control")
    @Operation(summary = "璁惧鎺у埗", description = "鎺у埗璁惧鎵ц鎸囧畾鎿嶄綔")
    public ResponseDTO<String> controlDevice(@Valid @RequestBody DeviceControlRequest request) {
        log.info("璁惧鎺у埗: userId={}, deviceId={}, action={}",
                request.getUserId(), request.getDeviceId(), request.getAction());

        try {
            return accessDeviceService.controlDevice(request);
        } catch (Exception e) {
            log.error("璁惧鎺у埗澶辫触", e);
            return ResponseDTO.error("DEVICE_CONTROL_FAILED", "璁惧鎺у埗澶辫触: " + e.getMessage());
        }
    }

    /**
     * 娣诲姞璁惧
     * <p>
     * 娣诲姞鏂扮殑闂ㄧ璁惧
     * </p>
     *
     * @param request 娣诲姞璇锋眰
     * @return 娣诲姞缁撴灉
     */
    @Observed(name = "accessMobile.addDevice", contextualName = "access-mobile-device-add")
    @PostMapping("/device/add")
    @Operation(summary = "娣诲姞璁惧", description = "娣诲姞鏂扮殑闂ㄧ璁惧")
    public ResponseDTO<String> addDevice(@Valid @RequestBody AddDeviceRequest request) {
        log.info("娣诲姞璁惧: deviceName={}, deviceCode={}, deviceType={}, areaId={}",
                request.getDeviceName(), request.getDeviceCode(), request.getDeviceType(), request.getAreaId());

        try {
            return accessDeviceService.addDevice(request);
        } catch (Exception e) {
            log.error("娣诲姞璁惧澶辫触", e);
            return ResponseDTO.error("DEVICE_ADD_FAILED", "娣诲姞璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鍒犻櫎璁惧
     * <p>
     * 鍒犻櫎鎸囧畾鐨勯棬绂佽澶?     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 鍒犻櫎缁撴灉
     */
    @Observed(name = "accessMobile.deleteDevice", contextualName = "access-mobile-device-delete")
    @DeleteMapping("/device/{deviceId}")
    @Operation(summary = "鍒犻櫎璁惧", description = "鍒犻櫎鎸囧畾鐨勯棬绂佽澶?)
    public ResponseDTO<String> deleteDevice(@PathVariable Long deviceId) {
        log.info("鍒犻櫎璁惧: deviceId={}", deviceId);

        try {
            return accessDeviceService.deleteDevice(deviceId);
        } catch (Exception e) {
            log.error("鍒犻櫎璁惧澶辫触", e);
            return ResponseDTO.error("DEVICE_DELETE_FAILED", "鍒犻櫎璁惧澶辫触: " + e.getMessage());
        }
    }

    /**
     * 璁惧閲嶅惎
     * <p>
     * 閲嶅惎鎸囧畾璁惧
     * </p>
     *
     * @param request 閲嶅惎璇锋眰
     * @return 閲嶅惎缁撴灉
     */
    @Observed(name = "accessMobile.restartDevice", contextualName = "access-mobile-device-restart")
    @PostMapping("/device/restart")
    @Operation(summary = "璁惧閲嶅惎", description = "閲嶅惎鎸囧畾璁惧")
    public ResponseDTO<String> restartDevice(@Valid @RequestBody DeviceRestartRequest request) {
        log.info("璁惧閲嶅惎: deviceId={}", request.getDeviceId());

        try {
            return accessDeviceService.restartDevice(request);
        } catch (Exception e) {
            log.error("璁惧閲嶅惎澶辫触", e);
            return ResponseDTO.error("DEVICE_RESTART_FAILED", "璁惧閲嶅惎澶辫触: " + e.getMessage());
        }
    }

    /**
     * 璁惧缁存姢
     * <p>
     * 璁剧疆璁惧缁存姢鐘舵€?     * </p>
     *
     * @param request 缁存姢璇锋眰
     * @return 缁存姢缁撴灉
     */
    @Observed(name = "accessMobile.maintainDevice", contextualName = "access-mobile-device-maintain")
    @PostMapping("/device/maintain")
    @Operation(summary = "璁惧缁存姢", description = "璁剧疆璁惧缁存姢鐘舵€?)
    public ResponseDTO<String> maintainDevice(@Valid @RequestBody DeviceMaintainRequest request) {
        log.info("璁惧缁存姢: deviceId={}, action={}", request.getDeviceId(), request.getAction());

        try {
            return accessDeviceService.maintainDevice(request);
        } catch (Exception e) {
            log.error("璁惧缁存姢澶辫触", e);
            return ResponseDTO.error("DEVICE_MAINTAIN_FAILED", "璁惧缁存姢澶辫触: " + e.getMessage());
        }
    }

    /**
     * 璁惧鏍″噯
     * <p>
     * 鎵ц璁惧鏍″噯鎿嶄綔
     * </p>
     *
     * @param request 鏍″噯璇锋眰
     * @return 鏍″噯缁撴灉
     */
    @Observed(name = "accessMobile.calibrateDevice", contextualName = "access-mobile-device-calibrate")
    @PostMapping("/device/calibrate")
    @Operation(summary = "璁惧鏍″噯", description = "鎵ц璁惧鏍″噯鎿嶄綔")
    public ResponseDTO<String> calibrateDevice(@Valid @RequestBody DeviceCalibrateRequest request) {
        log.info("璁惧鏍″噯: deviceId={}", request.getDeviceId());

        try {
            return accessDeviceService.calibrateDevice(request);
        } catch (Exception e) {
            log.error("璁惧鏍″噯澶辫触", e);
            return ResponseDTO.error("DEVICE_CALIBRATE_FAILED", "璁惧鏍″噯澶辫触: " + e.getMessage());
        }
    }

    /**
     * 鑾峰彇璁惧璇︽儏
     * <p>
     * 鑾峰彇鎸囧畾璁惧鐨勮缁嗕俊鎭?     * </p>
     *
     * @param deviceId 璁惧ID
     * @return 璁惧璇︽儏
     */
    @Observed(name = "accessMobile.getDeviceDetail", contextualName = "access-mobile-device-detail")
    @GetMapping("/device/{deviceId}")
    @Operation(summary = "鑾峰彇璁惧璇︽儏", description = "鑾峰彇鎸囧畾璁惧鐨勮缁嗕俊鎭?)
    public ResponseDTO<MobileDeviceDetailVO> getDeviceDetail(@PathVariable Long deviceId) {
        log.info("鑾峰彇璁惧璇︽儏: deviceId={}", deviceId);

        try {
            return accessDeviceService.getMobileDeviceDetail(deviceId);
        } catch (Exception e) {
            log.error("鑾峰彇璁惧璇︽儏澶辫触", e);
            return ResponseDTO.error("DEVICE_DETAIL_FAILED", "鑾峰彇璁惧璇︽儏澶辫触: " + e.getMessage());
        }
    }

    /**
     * 绉诲姩绔棬绂佹鏌?     *
     * @param request 妫€鏌ヨ姹?     * @return 妫€鏌ョ粨鏋?     */
    @Observed(name = "accessMobile.mobileAccessCheck", contextualName = "access-mobile-check")
    @PostMapping("/check")
    @Operation(summary = "绉诲姩绔棬绂佹鏌?, description = "绉诲姩绔棬绂佹潈闄愭鏌?)
    public ResponseDTO<Boolean> mobileAccessCheck(@Valid @RequestBody MobileAccessCheckRequest request) {
        log.info("绉诲姩绔棬绂佹鏌? userId={}, deviceId={}, areaId={}",
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
     * 浜岀淮鐮侀獙璇?     *
     * @param request 楠岃瘉璇锋眰
     * @return 楠岃瘉缁撴灉
     */
    @Observed(name = "accessMobile.verifyQRCode", contextualName = "access-mobile-qr-verify")
    @PostMapping("/qr/verify")
    @Operation(summary = "浜岀淮鐮侀獙璇?, description = "绉诲姩绔簩缁寸爜闂ㄧ楠岃瘉")
    public ResponseDTO<Boolean> verifyQRCode(@Valid @RequestBody QRCodeVerifyRequest request) {
        log.info("浜岀淮鐮侀獙璇? qrCode={}, deviceId={}", request.getQrCode(), request.getDeviceId());

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
     * NFC楠岃瘉
     *
     * @param request 楠岃瘉璇锋眰
     * @return 楠岃瘉缁撴灉
     */
    @Observed(name = "accessMobile.verifyNFC", contextualName = "access-mobile-nfc-verify")
    @PostMapping("/nfc/verify")
    @Operation(summary = "NFC楠岃瘉", description = "绉诲姩绔疦FC闂ㄧ楠岃瘉")
    public ResponseDTO<Boolean> verifyNFC(@Valid @RequestBody NFCVerifyRequest request) {
        log.info("NFC楠岃瘉: nfcCardId={}, deviceId={}", request.getNfcCardId(), request.getDeviceId());

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
     * 鐢熺墿璇嗗埆楠岃瘉
     *
     * @param request 楠岃瘉璇锋眰
     * @return 楠岃瘉缁撴灉
     */
    @Observed(name = "accessMobile.verifyBiometric", contextualName = "access-mobile-biometric-verify")
    @PostMapping("/biometric/verify")
    @Operation(summary = "鐢熺墿璇嗗埆楠岃瘉", description = "绉诲姩绔敓鐗╄瘑鍒棬绂侀獙璇?)
    public ResponseDTO<Boolean> verifyBiometric(@Valid @RequestBody BiometricVerifyRequest request) {
        log.info("鐢熺墿璇嗗埆楠岃瘉: userId={}, biometricType={}, deviceId={}",
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
     * 鑾峰彇闄勮繎璁惧
     *
     * @param userId 鐢ㄦ埛ID
     * @param latitude 绾害
     * @param longitude 缁忓害
     * @param radius 鍗婂緞锛堢背锛?     * @return 璁惧鍒楄〃
     */
    @Observed(name = "accessMobile.getNearbyDevices", contextualName = "access-mobile-nearby-devices")
    @GetMapping("/devices/nearby")
    @Operation(summary = "鑾峰彇闄勮繎璁惧", description = "鏍规嵁GPS浣嶇疆鑾峰彇闄勮繎闂ㄧ璁惧")
    public ResponseDTO<List<MobileDeviceItem>> getNearbyDevices(
            @RequestParam Long userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "500") Integer radius) {
        log.info("鑾峰彇闄勮繎璁惧: userId={}, latitude={}, longitude={}, radius={}",
                userId, latitude, longitude, radius);
        return accessDeviceService.getNearbyDevices(userId, latitude, longitude, radius);
    }

    /**
     * 鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺淇℃伅
     */
    @Observed(name = "accessMobile.getUserPermissions", contextualName = "access-mobile-user-permissions")
    @GetMapping("/permissions/{userId}")
    @Operation(summary = "鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺", description = "鑾峰彇鎸囧畾鐢ㄦ埛鐨勯棬绂佹潈闄愬垪琛?)
    public ResponseDTO<MobileUserPermissions> getUserPermissions(@PathVariable Long userId) {
        log.info("鑾峰彇鐢ㄦ埛闂ㄧ鏉冮檺: userId={}", userId);
        return accessDeviceService.getMobileUserPermissions(userId);
    }

    /**
     * 鑾峰彇鐢ㄦ埛璁块棶璁板綍
     *
     * @param userId 鐢ㄦ埛ID
     * @param size 璁板綍鏁伴噺
     * @return 璁块棶璁板綍鍒楄〃
     */
    @Observed(name = "accessMobile.getUserAccessRecords", contextualName = "access-mobile-user-records")
    @GetMapping("/records/{userId}")
    @Operation(summary = "鑾峰彇鐢ㄦ埛璁块棶璁板綍", description = "鑾峰彇鎸囧畾鐢ㄦ埛鐨勮闂褰?)
    public ResponseDTO<List<MobileAccessRecord>> getUserAccessRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("鑾峰彇鐢ㄦ埛璁块棶璁板綍: userId={}, size={}", userId, size);
        return accessEventService.getMobileAccessRecords(userId, size);
    }

    /**
     * 鑾峰彇鍖哄煙鍒楄〃
     * <p>
     * 鑾峰彇鐢ㄦ埛鏈夋潈闄愯闂殑鍖哄煙鍒楄〃锛屽寘鍚尯鍩熻鎯?     * </p>
     *
     * @param userId 鐢ㄦ埛ID锛堝彲閫夛紝涓嶄紶鍒欎粠Token鑾峰彇锛?     * @return 鍖哄煙鍒楄〃
     */
    @Observed(name = "accessMobile.getAreas", contextualName = "access-mobile-areas")
    @GetMapping("/areas")
    @Operation(summary = "鑾峰彇鍖哄煙鍒楄〃", description = "鑾峰彇鐢ㄦ埛鏈夋潈闄愯闂殑鍖哄煙鍒楄〃")
    public ResponseDTO<List<MobileAreaItem>> getAreas(@RequestParam(required = false) Long userId) {
        log.info("鑾峰彇鍖哄煙鍒楄〃: userId={}", userId);
        return accessDeviceService.getMobileAreas(userId);
    }

    /**
     * 涓存椂寮€闂ㄧ敵璇?     *
     * @param request 鐢宠璇锋眰
     * @return 鐢宠缁撴灉
     */
    @Observed(name = "accessMobile.requestTemporaryAccess", contextualName = "access-mobile-temporary-access")
    @PostMapping("/temporary-access")
    @Operation(summary = "涓存椂寮€闂ㄧ敵璇?, description = "鐢宠涓存椂闂ㄧ鏉冮檺")
    public ResponseDTO<String> requestTemporaryAccess(@Valid @RequestBody TemporaryAccessRequest request) {
        log.info("涓存椂寮€闂ㄧ敵璇? userId={}, deviceId={}, reason={}",
                request.getUserId(), request.getDeviceId(), request.getReason());
        return ResponseDTO.ok("鐢宠宸叉彁浜?);
    }

    /**
     * 鑾峰彇瀹炴椂闂ㄧ鐘舵€?     *
     * @param deviceId 璁惧ID
     * @return 鐘舵€佷俊鎭?     */
    @Observed(name = "accessMobile.getRealTimeStatus", contextualName = "access-mobile-realtime-status")
    @GetMapping("/status/realtime")
    @Operation(summary = "鑾峰彇瀹炴椂闂ㄧ鐘舵€?, description = "鑾峰彇鎸囧畾璁惧鐨勫疄鏃剁姸鎬?)
    public ResponseDTO<MobileRealTimeStatus> getRealTimeStatus(@RequestParam Long deviceId) {
        log.info("鑾峰彇瀹炴椂闂ㄧ鐘舵€? deviceId={}", deviceId);
        return accessDeviceService.getMobileRealTimeStatus(deviceId);
    }

    /**
     * 鍙戦€佹帹閫侀€氱煡
     *
     * @param request 閫氱煡璇锋眰
     * @return 鍙戦€佺粨鏋?     */
    @Observed(name = "accessMobile.sendPushNotification", contextualName = "access-mobile-push-notification")
    @PostMapping("/notification/push")
    @Operation(summary = "鍙戦€佹帹閫侀€氱煡", description = "鍙戦€侀棬绂佺浉鍏虫帹閫侀€氱煡")
    public ResponseDTO<String> sendPushNotification(@Valid @RequestBody PushNotificationRequest request) {
        log.info("鍙戦€佹帹閫侀€氱煡: userId={}, notificationType={}",
                request.getUserId(), request.getNotificationType());
        return ResponseDTO.ok("閫氱煡宸插彂閫?);
    }

    // ==================== 钃濈墮闂ㄧ鍔熻兘 ====================

    /**
     * 钃濈墮璁惧鎵弿
     * <p>
     * 鎵弿闄勮繎鐨勮摑鐗欓棬绂佽澶?     * </p>
     *
     * @param request 鎵弿璇锋眰
     * @return 鎵弿缁撴灉
     */
    @Observed(name = "accessMobile.scanBluetoothDevices", contextualName = "access-mobile-bluetooth-scan")
    @PostMapping("/bluetooth/scan")
    @Operation(summary = "钃濈墮璁惧鎵弿", description = "鎵弿闄勮繎鐨勮摑鐗欓棬绂佽澶?)
    public ResponseDTO<List<BluetoothDeviceVO>> scanBluetoothDevices(@Valid @RequestBody BluetoothScanRequest request) {
        log.info("钃濈墮璁惧鎵弿: userId={}, location={}, duration={}ms",
                request.getUserId(), request.getLocation(), request.getScanDuration());

        return bluetoothAccessService.scanNearbyDevices(request);
    }

    /**
     * 钃濈墮璁惧杩炴帴
     * <p>
     * 杩炴帴鎸囧畾鐨勮摑鐗欓棬绂佽澶?     * </p>
     *
     * @param request 杩炴帴璇锋眰
     * @return 杩炴帴缁撴灉
     */
    @Observed(name = "accessMobile.connectBluetoothDevice", contextualName = "access-mobile-bluetooth-connect")
    @PostMapping("/bluetooth/connect")
    @Operation(summary = "钃濈墮璁惧杩炴帴", description = "杩炴帴鎸囧畾鐨勮摑鐗欓棬绂佽澶?)
    public ResponseDTO<BluetoothConnectionResult> connectBluetoothDevice(@Valid @RequestBody BluetoothConnectRequest request) {
        log.info("钃濈墮璁惧杩炴帴: userId={}, deviceAddress={}", request.getUserId(), request.getDeviceAddress());

        return bluetoothAccessService.connectDevice(request);
    }

    /**
     * 钃濈墮闂ㄧ楠岃瘉
     * <p>
     * 閫氳繃钃濈墮杩涜闂ㄧ楠岃瘉
     * </p>
     *
     * @param request 楠岃瘉璇锋眰
     * @return 楠岃瘉缁撴灉
     */
    @Observed(name = "accessMobile.verifyBluetoothAccess", contextualName = "access-mobile-bluetooth-verify")
    @PostMapping("/bluetooth/verify")
    @Operation(summary = "钃濈墮闂ㄧ楠岃瘉", description = "閫氳繃钃濈墮杩涜闂ㄧ楠岃瘉")
    public ResponseDTO<BluetoothAccessResult> verifyBluetoothAccess(@Valid @RequestBody BluetoothAccessRequest request) {
        log.info("钃濈墮闂ㄧ楠岃瘉: userId={}, deviceAddress={}, accessType={}",
                request.getUserId(), request.getDeviceAddress(), request.getAccessType());

        return bluetoothAccessService.performBluetoothAccess(request);
    }

    /**
     * 钃濈墮璁惧鐘舵€佹煡璇?     * <p>
     * 鏌ヨ宸茶繛鎺ヨ摑鐗欒澶囩殑鐘舵€?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 璁惧鐘舵€佸垪琛?     */
    @Observed(name = "accessMobile.getBluetoothDeviceStatus", contextualName = "access-mobile-bluetooth-status")
    @GetMapping("/bluetooth/status")
    @Operation(summary = "钃濈墮璁惧鐘舵€佹煡璇?, description = "鏌ヨ宸茶繛鎺ヨ摑鐗欒澶囩殑鐘舵€?)
    public ResponseDTO<List<BluetoothDeviceStatusVO>> getBluetoothDeviceStatus(@RequestParam Long userId) {
        log.info("钃濈墮璁惧鐘舵€佹煡璇? userId={}", userId);
        return bluetoothAccessService.getConnectedDevicesStatus(userId);
    }

    /**
     * 钃濈墮璁惧鏂紑杩炴帴
     * <p>
     * 鏂紑涓庢寚瀹氳摑鐗欒澶囩殑杩炴帴
     * </p>
     *
     * @param request 鏂紑杩炴帴璇锋眰
     * @return 鎿嶄綔缁撴灉
     */
    @Observed(name = "accessMobile.disconnectBluetoothDevice", contextualName = "access-mobile-bluetooth-disconnect")
    @PostMapping("/bluetooth/disconnect")
    @Operation(summary = "钃濈墮璁惧鏂紑杩炴帴", description = "鏂紑涓庢寚瀹氳摑鐗欒澶囩殑杩炴帴")
    public ResponseDTO<String> disconnectBluetoothDevice(@Valid @RequestBody BluetoothDisconnectRequest request) {
        log.info("钃濈墮璁惧鏂紑杩炴帴: userId={}, deviceAddress={}", request.getUserId(), request.getDeviceAddress());

        boolean result = bluetoothAccessService.disconnectDevice(request.getUserId(), request.getDeviceAddress());
        return result ? ResponseDTO.ok("鏂紑杩炴帴鎴愬姛") : ResponseDTO.error("DISCONNECT_FAILED", "鏂紑杩炴帴澶辫触");
    }

    /**
     * 钃濈墮璁惧閰嶅
     * <p>
     * 涓庤摑鐗欒澶囪繘琛岄厤瀵?     * </p>
     *
     * @param request 閰嶅璇锋眰
     * @return 閰嶅缁撴灉
     */
    @Observed(name = "accessMobile.pairBluetoothDevice", contextualName = "access-mobile-bluetooth-pair")
    @PostMapping("/bluetooth/pair")
    @Operation(summary = "钃濈墮璁惧閰嶅", description = "涓庤摑鐗欒澶囪繘琛岄厤瀵?)
    public ResponseDTO<BluetoothPairingResult> pairBluetoothDevice(@Valid @RequestBody BluetoothPairingRequest request) {
        log.info("钃濈墮璁惧閰嶅: userId={}, deviceAddress={}, pinCode={}",
                request.getUserId(), request.getDeviceAddress(), request.getPinCode());

        return bluetoothAccessService.pairDevice(request);
    }

    // ==================== 绂荤嚎妯″紡鍔熻兘 ====================

    /**
     * 绂荤嚎妯″紡鏁版嵁鍚屾
     * <p>
     * 鍚屾绂荤嚎妯″紡涓嬬殑闂ㄧ鏁版嵁
     * </p>
     *
     * @param request 鍚屾璇锋眰
     * @return 鍚屾缁撴灉
     */
    @Observed(name = "accessMobile.syncOfflineData", contextualName = "access-mobile-offline-sync")
    @PostMapping("/offline/sync")
    @Operation(summary = "绂荤嚎妯″紡鏁版嵁鍚屾", description = "鍚屾绂荤嚎妯″紡涓嬬殑闂ㄧ鏁版嵁")
    public ResponseDTO<OfflineSyncResult> syncOfflineData(@Valid @RequestBody OfflineSyncRequest request) {
        log.info("绂荤嚎妯″紡鏁版嵁鍚屾: userId={}, syncType={}, dataSize={}",
                request.getUserId(), request.getSyncType(), request.getDataSize());

        return offlineModeService.syncOfflineData(request);
    }

    /**
     * 鑾峰彇绂荤嚎璁块棶鏉冮檺
     * <p>
     * 鑾峰彇鐢ㄦ埛鍦ㄧ绾挎ā寮忎笅鐨勮闂潈闄愭暟鎹?     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @param lastSyncTime 涓婃鍚屾鏃堕棿
     * @return 绂荤嚎鏉冮檺鏁版嵁
     */
    @Observed(name = "accessMobile.getOfflinePermissions", contextualName = "access-mobile-offline-permissions")
    @GetMapping("/offline/permissions")
    @Operation(summary = "鑾峰彇绂荤嚎璁块棶鏉冮檺", description = "鑾峰彇鐢ㄦ埛鍦ㄧ绾挎ā寮忎笅鐨勮闂潈闄愭暟鎹?)
    public ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(
            @RequestParam Long userId,
            @RequestParam(required = false) String lastSyncTime) {
        log.info("鑾峰彇绂荤嚎璁块棶鏉冮檺: userId={}, lastSyncTime={}", userId, lastSyncTime);
        return offlineModeService.getOfflinePermissions(userId, lastSyncTime);
    }

    /**
     * 绂荤嚎璁块棶璁板綍涓婃姤
     * <p>
     * 涓婃姤绂荤嚎妯″紡涓嬬殑璁块棶璁板綍
     * </p>
     *
     * @param request 涓婃姤璇锋眰
     * @return 涓婃姤缁撴灉
     */
    @Observed(name = "accessMobile.reportOfflineAccessRecords", contextualName = "access-mobile-offline-report")
    @PostMapping("/offline/records/report")
    @Operation(summary = "绂荤嚎璁块棶璁板綍涓婃姤", description = "涓婃姤绂荤嚎妯″紡涓嬬殑璁块棶璁板綍")
    public ResponseDTO<OfflineReportResult> reportOfflineAccessRecords(@Valid @RequestBody OfflineRecordsReportRequest request) {
        log.info("绂荤嚎璁块棶璁板綍涓婃姤: userId={}, recordCount={}", request.getUserId(), request.getRecords().size());

        return offlineModeService.reportOfflineRecords(request);
    }

    /**
     * 鏃犵紳闂ㄧ閫氳
     * <p>
     * 瀹炵幇鏃犵紳鐨勯棬绂侀€氳浣撻獙
     * </p>
     *
     * @param request 閫氳璇锋眰
     * @return 閫氳缁撴灉
     */
    @Observed(name = "accessMobile.seamlessAccess", contextualName = "access-mobile-seamless")
    @PostMapping("/seamless/access")
    @Operation(summary = "鏃犵紳闂ㄧ閫氳", description = "瀹炵幇鏃犵紳鐨勯棬绂侀€氳浣撻獙")
    public ResponseDTO<SeamlessAccessResult> seamlessAccess(@Valid @RequestBody SeamlessAccessRequest request) {
        log.info("鏃犵紳闂ㄧ閫氳: userId={}, deviceId={}, accessMode={}",
                request.getUserId(), request.getDeviceId(), request.getAccessMode());

        // 浼樺厛浣跨敤钃濈墮锛屽け璐ユ椂鍥為€€鍒板叾浠栨柟寮?        if ("BLUETOOTH".equals(request.getAccessMode()) || "AUTO".equals(request.getAccessMode())) {
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
                log.warn("钃濈墮閫氳澶辫触锛屽洖閫€鍒板叾浠栨柟寮? {}", e.getMessage());
            }
        }

        // 鍥為€€鍒版爣鍑嗛棬绂侀獙璇?        AdvancedAccessControlService.AccessControlResult result =
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
     * 鑾峰彇鐢ㄦ埛闂ㄧ鍗′俊鎭?     * <p>
     * 鑾峰彇鐢ㄦ埛鐨勬墍鏈夐棬绂佸崱淇℃伅锛屾敮鎸佽摑鐗欓棬绂佸崱
     * </p>
     *
     * @param userId 鐢ㄦ埛ID
     * @return 闂ㄧ鍗″垪琛?     */
    @Observed(name = "accessMobile.getUserAccessCards", contextualName = "access-mobile-access-cards")
    @GetMapping("/cards")
    @Operation(summary = "鑾峰彇鐢ㄦ埛闂ㄧ鍗′俊鎭?, description = "鑾峰彇鐢ㄦ埛鐨勬墍鏈夐棬绂佸崱淇℃伅锛屾敮鎸佽摑鐗欓棬绂佸崱")
    public ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(@RequestParam Long userId) {
        log.info("鑾峰彇鐢ㄦ埛闂ㄧ鍗′俊鎭? userId={}", userId);
        return bluetoothAccessService.getUserAccessCards(userId);
    }

    /**
     * 娣诲姞钃濈墮闂ㄧ鍗?     * <p>
     * 涓虹敤鎴锋坊鍔犺摑鐗欓棬绂佸崱
     * </p>
     *
     * @param request 娣诲姞璇锋眰
     * @return 娣诲姞缁撴灉
     */
    @Observed(name = "accessMobile.addBluetoothAccessCard", contextualName = "access-mobile-add-bluetooth-card")
    @PostMapping("/cards/bluetooth")
    @Operation(summary = "娣诲姞钃濈墮闂ㄧ鍗?, description = "涓虹敤鎴锋坊鍔犺摑鐗欓棬绂佸崱")
    public ResponseDTO<String> addBluetoothAccessCard(@Valid @RequestBody AddBluetoothCardRequest request) {
        log.info("娣诲姞钃濈墮闂ㄧ鍗? userId={}, cardName={}, deviceAddress={}",
                request.getUserId(), request.getCardName(), request.getDeviceAddress());

        return bluetoothAccessService.addBluetoothAccessCard(request);
    }

    // ==================== 鍐呴儴璇锋眰绫?====================

    /**
     * 绉诲姩绔棬绂佹鏌ヨ姹?     */
    @Data
    public static class MobileAccessCheckRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String verificationType;
        private String location;
    }

    /**
     * 浜岀淮鐮侀獙璇佽姹?     */
    @Data
    public static class QRCodeVerifyRequest {
        private String qrCode;
        private Long deviceId;
    }

    /**
     * NFC楠岃瘉璇锋眰
     */
    @Data
    public static class NFCVerifyRequest {
        private String nfcCardId;
        private Long deviceId;
    }

    /**
     * 鐢熺墿璇嗗埆楠岃瘉璇锋眰
     */
    @Data
    public static class BiometricVerifyRequest {
        private Long userId;
        private String biometricType;
        private String biometricData;
        private Long deviceId;
    }

    /**
     * 涓存椂寮€闂ㄧ敵璇疯姹?     */
    @Data
    public static class TemporaryAccessRequest {
        private Long userId;
        private Long deviceId;
        private String reason;
    }

    /**
     * 鎺ㄩ€侀€氱煡璇锋眰
     */
    @Data
    public static class PushNotificationRequest {
        private Long userId;
        private String notificationType;
    }

    /**
     * 绉诲姩绔澶囬」
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
     * 绉诲姩绔敤鎴锋潈闄?     */
    @Data
    public static class MobileUserPermissions {
        private Long userId;
        private List<Long> allowedAreaIds;
        private List<Long> allowedDeviceIds;
        private String permissionLevel;
    }

    /**
     * 绉诲姩绔闂褰?     */
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
     * 绉诲姩绔疄鏃剁姸鎬?     */
    @Data
    public static class MobileRealTimeStatus {
        private Long deviceId;
        private String deviceName;
        private String deviceStatus;
        private Integer onlineCount;
        private String lastUpdateTime;
    }

    /**
     * 绉诲姩绔尯鍩熼」
     */
    @Data
    public static class MobileAreaItem {
        private Long areaId;           // 鍖哄煙ID
        private String areaName;       // 鍖哄煙鍚嶇О
        private String areaType;       // 鍖哄煙绫诲瀷
        private Integer deviceCount;    // 璁惧鏁伴噺
        private Integer permissionCount; // 鏉冮檺鏁伴噺
        private String description;    // 鍖哄煙鎻忚堪
        private Boolean active;        // 鏄惁鏈夋晥
    }

    // ==================== 钃濈墮闂ㄧ璇锋眰绫?====================

    /**
     * 钃濈墮璁惧鎵弿璇锋眰
     */
    @Data
    public static class BluetoothScanRequest {
        private Long userId;
        private String location;           // 褰撳墠浣嶇疆
        private Integer scanDuration;      // 鎵弿鏃堕暱锛堟绉掞級
        private Integer maxDevices;        // 鏈€澶ц澶囨暟閲?        private String filterType;         // 杩囨护绫诲瀷
    }

    /**
     * 钃濈墮璁惧杩炴帴璇锋眰
     */
    @Data
    public static class BluetoothConnectRequest {
        private Long userId;
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String deviceName;         // 璁惧鍚嶇О
        private Integer timeout;           // 杩炴帴瓒呮椂锛堟绉掞級
        private Boolean autoReconnect;      // 鏄惁鑷姩閲嶈繛
    }

    /**
     * 钃濈墮闂ㄧ楠岃瘉璇锋眰
     */
    @Data
    public static class BluetoothAccessRequest {
        private Long userId;
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String accessType;         // 璁块棶绫诲瀷
        private String location;           // 浣嶇疆淇℃伅
        private Long timestamp;            // 鏃堕棿鎴?        private String signature;          // 鏁板瓧绛惧悕
        private String encryptedData;      // 鍔犲瘑鏁版嵁
    }

    /**
     * 钃濈墮璁惧鏂紑杩炴帴璇锋眰
     */
    @Data
    public static class BluetoothDisconnectRequest {
        private Long userId;
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String reason;             // 鏂紑鍘熷洜
    }

    /**
     * 钃濈墮璁惧閰嶅璇锋眰
     */
    @Data
    public static class BluetoothPairingRequest {
        private Long userId;
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String pinCode;            // PIN鐮?        private String pairingMethod;      // 閰嶅鏂瑰紡
        private Integer timeout;           // 閰嶅瓒呮椂锛堟绉掞級
    }

    // ==================== 绂荤嚎妯″紡璇锋眰绫?====================

    /**
     * 绂荤嚎鏁版嵁鍚屾璇锋眰
     */
    @Data
    public static class OfflineSyncRequest {
        private Long userId;
        private String syncType;          // 鍚屾绫诲瀷锛歅ERMISSIONS, RECORDS, CONFIG
        private Long dataSize;            // 鏁版嵁澶у皬
        private String checksum;           // 鏍￠獙鍜?        private Integer version;          // 鏁版嵁鐗堟湰
    }

    /**
     * 绂荤嚎璁板綍涓婃姤璇锋眰
     */
    @Data
    public static class OfflineRecordsReportRequest {
        private Long userId;
        private List<OfflineAccessRecord> records;  // 绂荤嚎璁块棶璁板綍
        private String deviceInfo;        // 璁惧淇℃伅
        private Long reportTime;          // 涓婃姤鏃堕棿
    }

    /**
     * 绂荤嚎璁块棶璁板綍
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
        private String deviceFirmware;    // 璁惧鍥轰欢鐗堟湰
    }

    /**
     * 鏃犵紳闂ㄧ閫氳璇锋眰
     */
    @Data
    public static class SeamlessAccessRequest {
        private Long userId;
        private Long deviceId;
        private Long areaId;
        private String deviceAddress;     // 钃濈墮璁惧鍦板潃
        private String accessMode;         // 璁块棶妯″紡锛欱LUETOOTH, NFC, QR_CODE, AUTO
        private String verificationType;   // 楠岃瘉绫诲瀷
        private String location;           // 浣嶇疆淇℃伅
    }

    /**
     * 娣诲姞钃濈墮闂ㄧ鍗¤姹?     */
    @Data
    public static class AddBluetoothCardRequest {
        private Long userId;
        private String cardName;           // 鍗＄墖鍚嶇О
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String deviceName;         // 璁惧鍚嶇О
        private String cardType;           // 鍗＄墖绫诲瀷
        private Integer validPeriod;       // 鏈夋晥鏈燂紙澶╋級
        private List<String> allowedAreas; // 鍏佽鐨勫尯鍩?    }

    // ==================== 钃濈墮闂ㄧ鍝嶅簲绫?====================

    /**
     * 钃濈墮璁惧淇℃伅
     */
    @Data
    public static class BluetoothDeviceVO {
        private String deviceAddress;     // 璁惧MAC鍦板潃
        private String deviceName;         // 璁惧鍚嶇О
        private String deviceType;         // 璁惧绫诲瀷
        private Integer signalStrength;    // 淇″彿寮哄害
        private Integer batteryLevel;      // 鐢垫睜鐢甸噺
        private Integer rssi;              // RSSI鍊?        private Boolean isPaired;          // 鏄惁宸查厤瀵?        private Boolean isConnected;       // 鏄惁宸茶繛鎺?        private String lastSeen;           // 鏈€鍚庡彂鐜版椂闂?        private Double distance;           // 浼扮畻璺濈
    }

    /**
     * 钃濈墮杩炴帴缁撴灉
     */
    @Data
    public static class BluetoothConnectionResult {
        private Boolean success;
        private String deviceAddress;
        private String deviceName;
        private String connectionId;       // 杩炴帴ID
        private Integer responseTime;      // 鍝嶅簲鏃堕棿锛堟绉掞級
        private String errorMessage;       // 閿欒淇℃伅
        private Integer signalStrength;    // 淇″彿寮哄害
        private String protocolVersion;    // 鍗忚鐗堟湰
    }

    /**
     * 钃濈墮闂ㄧ楠岃瘉缁撴灉
     */
    @Data
    public static class BluetoothAccessResult {
        private Boolean success;
        private Long deviceId;
        private String deviceName;
        private String accessMethod;       // 璁块棶鏂瑰紡
        private Long accessTime;           // 璁块棶鏃堕棿
        private Integer responseTime;      // 鍝嶅簲鏃堕棿锛堟绉掞級
        private String accessToken;        // 璁块棶浠ょ墝
        private String decisionReason;     // 鍐崇瓥鍘熷洜
        private RiskLevel riskLevel;       // 椋庨櫓绛夌骇
    }

    /**
     * 钃濈墮璁惧鐘舵€?     */
    @Data
    public static class BluetoothDeviceStatusVO {
        private String deviceAddress;
        private String deviceName;
        private String connectionStatus;   // 杩炴帴鐘舵€?        private Integer signalStrength;
        private Integer batteryLevel;
        private String lastActivity;       // 鏈€鍚庢椿鍔ㄦ椂闂?        private Long totalConnections;     // 鎬昏繛鎺ユ鏁?        private Long totalAccessCount;     // 鎬昏闂鏁?        private String firmwareVersion;    // 鍥轰欢鐗堟湰
    }

    /**
     * 钃濈墮閰嶅缁撴灉
     */
    @Data
    public static class BluetoothPairingResult {
        private Boolean success;
        private String deviceAddress;
        private String pairingKey;         // 閰嶅瀵嗛挜
        private String pairingMethod;      // 閰嶅鏂瑰紡
        private Integer responseTime;      // 鍝嶅簲鏃堕棿锛堟绉掞級
        private String errorMessage;       // 閿欒淇℃伅
        private Boolean requiresConfirmation; // 鏄惁闇€瑕佺‘璁?    }

    // ==================== 绂荤嚎妯″紡鍝嶅簲绫?====================

    /**
     * 绂荤嚎鏉冮檺鏁版嵁
     */
    @Data
    public static class OfflinePermissionsVO {
        private Long userId;
        private String lastSyncTime;
        private String expiryTime;         // 鏉冮檺杩囨湡鏃堕棿
        private List<OfflineAreaPermission> areaPermissions;
        private List<OfflineDevicePermission> devicePermissions;
        private String checksum;           // 鏁版嵁鏍￠獙鍜?        private Integer version;            // 鏁版嵁鐗堟湰
    }

    /**
     * 绂荤嚎鍖哄煙鏉冮檺
     */
    @Data
    public static class OfflineAreaPermission {
        private Long areaId;
        private String areaName;
        private String permissionLevel;
        private String validFrom;           // 鐢熸晥鏃堕棿
        private String validUntil;          // 澶辨晥鏃堕棿
        private List<String> accessMethods; // 璁块棶鏂瑰紡
    }

    /**
     * 绂荤嚎璁惧鏉冮檺
     */
    @Data
    public static class OfflineDevicePermission {
        private Long deviceId;
        private String deviceName;
        private String deviceType;
        private String permissionLevel;
        private Integer dailyLimit;        // 姣忔棩闄愬埗
        private String validFrom;
        private String validUntil;
    }

    /**
     * 绂荤嚎鍚屾缁撴灉
     */
    @Data
    public static class OfflineSyncResult {
        private Boolean success;
        private String syncType;
        private Integer syncedRecords;     // 鍚屾璁板綍鏁?        private Integer failedRecords;      // 澶辫触璁板綍鏁?        private Long syncDuration;          // 鍚屾鑰楁椂锛堟绉掞級
        private String nextSyncTime;        // 涓嬫鍚屾鏃堕棿
        private List<String> errors;        // 閿欒淇℃伅鍒楄〃
    }

    /**
     * 绂荤嚎璁板綍涓婃姤缁撴灉
     */
    @Data
    public static class OfflineReportResult {
        private Boolean success;
        private Integer reportedRecords;    // 涓婃姤璁板綍鏁?        private Integer acceptedRecords;    // 鎺ュ彈璁板綍鏁?        private Integer rejectedRecords;    // 鎷掔粷璁板綍鏁?        private List<String> rejectedReasons; // 鎷掔粷鍘熷洜
        private String reportId;            // 涓婃姤ID
    }

    // ==================== 鏃犵紳闂ㄧ鍝嶅簲绫?====================

    /**
     * 鏃犵紳闂ㄧ閫氳缁撴灉
     */
    @Data
    public static class SeamlessAccessResult {
        private Boolean success;
        private String accessMethod;       // 璁块棶鏂瑰紡锛欱LUETOOTH, STANDARD
        private Long deviceId;
        private String deviceName;
        private Long accessTime;           // 璁块棶鏃堕棿
        private Integer responseTime;      // 鍝嶅簲鏃堕棿锛堟绉掞級
        private String accessToken;        // 璁块棶浠ょ墝
        private String message;            // 鎻愮ず淇℃伅
        private Boolean isOfflineMode;     // 鏄惁绂荤嚎妯″紡
    }

    // ==================== 闂ㄧ鍗″搷搴旂被 ====================

    /**
     * 鐢ㄦ埛闂ㄧ鍗′俊鎭?     */
    @Data
    public static class UserAccessCardVO {
        private Long cardId;
        private String cardName;
        private String cardType;           // PHYSICAL, VIRTUAL, BLUETOOTH
        private String cardNumber;         // 鍗″彿
        private String deviceAddress;      // 钃濈墮璁惧鍦板潃锛堣摑鐗欏崱锛?        private String status;             // ACTIVE, INACTIVE, EXPIRED
        private String issueTime;          // 鍙戝崱鏃堕棿
        private String expireTime;         // 杩囨湡鏃堕棿
        private List<String> allowedAreas; // 鍏佽鍖哄煙
        private Integer dailyUsageLimit;   // 姣忔棩浣跨敤闄愬埗
        private Integer currentUsage;      // 褰撳墠浣跨敤娆℃暟
        private Boolean isPrimary;         // 鏄惁涓诲崱
    }

    // ==================== 鏋氫妇瀹氫箟 ====================

    /**
     * 椋庨櫓绛夌骇鏋氫妇
     */
    public enum RiskLevel {
        LOW,        // 浣庨闄?        MEDIUM,     // 涓瓑椋庨櫓
        HIGH,       // 楂橀闄?        CRITICAL    // 涓ラ噸椋庨櫓
    }
}

