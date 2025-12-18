package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.BiometricRegisterForm;
import net.lab1024.sa.access.domain.form.BiometricAuthForm;
import net.lab1024.sa.access.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.access.domain.vo.BiometricAuthResultVO;
import net.lab1024.sa.access.service.BiometricAuthService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 鐢熺墿璇嗗埆璁よ瘉鎺у埗锟?
 * 涓ユ牸閬靛惊闂ㄧ璁惧閫氳鍗忚璁捐
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric")
@Tag(name = "鐢熺墿璇嗗埆璁よ瘉", description = "鐢熺墿璇嗗埆璁よ瘉鐩稿叧鎺ュ彛")
public class BiometricAuthController {

    @Resource
    private BiometricAuthService biometricAuthService;

    /**
     * 娉ㄥ唽鐢熺墿璇嗗埆妯℃澘
     * 璁惧鍗忚锛歊EGISTER_TEMPLATE
     */
    @Operation(summary = "娉ㄥ唽鐢熺墿璇嗗埆妯℃澘", description = "鐢ㄦ埛娉ㄥ唽鐢熺墿璇嗗埆鐗瑰緛妯℃澘")
    @PostMapping("/template/register")
    public ResponseDTO<BiometricTemplateVO> registerTemplate(
            @Valid @RequestBody BiometricRegisterForm registerForm,
            HttpServletRequest request) {

        // 璁板綍瀹㈡埛绔俊锟?
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("[鐢熺墿璇嗗埆API] 娉ㄥ唽妯℃澘璇锋眰 userId={}, biometricType={}, deviceId={}, clientIp={}",
                registerForm.getUserId(), registerForm.getBiometricType(), registerForm.getDeviceId(), clientIp);

        return biometricAuthService.registerTemplate(registerForm);
    }

    /**
     * 1:1鐢熺墿璇嗗埆楠岃瘉
     * 璁惧鍗忚锛欰UTHENTICATE
     */
    @Operation(summary = "1:1鐢熺墿璇嗗埆楠岃瘉", description = "楠岃瘉鐢ㄦ埛鐢熺墿璇嗗埆鐗瑰緛")
    @PostMapping("/authenticate")
    public ResponseDTO<BiometricAuthResultVO> authenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 琛ュ厖瀹㈡埛绔俊锟?
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        authForm.setClientIp(clientIp);
        authForm.setClientInfo(userAgent);

        log.info("[鐢熺墿璇嗗埆API] 1:1楠岃瘉璇锋眰 userId={}, biometricType={}, deviceId={}, clientIp={}",
                authForm.getUserId(), authForm.getBiometricType(), authForm.getDeviceId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 1:N鐢熺墿璇嗗埆璇嗗埆
     * 璁惧鍗忚锛欼DENTIFY
     */
    @Operation(summary = "1:N鐢熺墿璇嗗埆璇嗗埆", description = "鍦ㄦ墍鏈夌敤鎴蜂腑璇嗗埆鐢熺墿璇嗗埆鐗瑰緛")
    @PostMapping("/identify")
    public ResponseDTO<List<BiometricAuthResultVO>> identify(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 琛ュ厖瀹㈡埛绔俊锟?
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        authForm.setClientIp(clientIp);
        authForm.setClientInfo(userAgent);

        log.info("[鐢熺墿璇嗗埆API] 1:N璇嗗埆璇锋眰 biometricType={}, deviceId={}, limit={}, clientIp={}",
                authForm.getBiometricType(), authForm.getDeviceId(), authForm.getLimit(), clientIp);

        return biometricAuthService.identify(authForm);
    }

    /**
     * 鑾峰彇鐢ㄦ埛鐢熺墿璇嗗埆妯℃澘鍒楄〃
     * 璁惧鍗忚锛欸ET_USER_TEMPLATES
     */
    @Operation(summary = "鑾峰彇鐢ㄦ埛妯℃澘鍒楄〃", description = "鏌ヨ鐢ㄦ埛鐨勬墍鏈夌敓鐗╄瘑鍒ā锟?)
    @GetMapping("/template/user/{userId}")
    public ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(
            @Parameter(description = "鐢ㄦ埛ID", example = "1001")
            @PathVariable Long userId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[鐢熺墿璇嗗埆API] 鏌ヨ鐢ㄦ埛妯℃澘 userId={}, clientIp={}", userId, clientIp);

        return biometricAuthService.getUserTemplates(userId);
    }

    /**
     * 鏇存柊妯℃澘鐘讹拷?
     * 璁惧鍗忚锛歎PDATE_TEMPLATE_STATUS
     */
    @Operation(summary = "鏇存柊妯℃澘鐘讹拷?, description = "鏇存柊鐢熺墿璇嗗埆妯℃澘鐘讹拷?)
    @PutMapping("/template/{templateId}/status")
    public ResponseDTO<Void> updateTemplateStatus(
            @Parameter(description = "妯℃澘ID", example = "1001")
            @PathVariable Long templateId,
            @Parameter(description = "鐘讹拷?, example = "1")
            @RequestParam Integer status,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[鐢熺墿璇嗗埆API] 鏇存柊妯℃澘鐘讹拷?templateId={}, status={}, clientIp={}",
                templateId, status, clientIp);

        return biometricAuthService.updateTemplateStatus(templateId, status);
    }

    /**
     * 鍒犻櫎鐢ㄦ埛妯℃澘
     * 璁惧鍗忚锛欴ELETE_USER_TEMPLATE
     */
    @Operation(summary = "鍒犻櫎鐢ㄦ埛妯℃澘", description = "鍒犻櫎鐢ㄦ埛鎸囧畾绫诲瀷鐨勭敓鐗╄瘑鍒ā锟?)
    @DeleteMapping("/template/user/{userId}/type/{biometricType}")
    public ResponseDTO<Void> deleteUserTemplate(
            @Parameter(description = "鐢ㄦ埛ID", example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1")
            @PathVariable Integer biometricType,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[鐢熺墿璇嗗埆API] 鍒犻櫎鐢ㄦ埛妯℃澘 userId={}, biometricType={}, clientIp={}",
                userId, biometricType, clientIp);

        return biometricAuthService.deleteUserTemplate(userId, biometricType);
    }

    /**
     * 鑾峰彇鐢ㄦ埛鐢熺墿璇嗗埆缁熻淇℃伅
     * 璁惧鍗忚锛欸ET_USER_STATS
     */
    @Operation(summary = "鑾峰彇鐢ㄦ埛鐢熺墿璇嗗埆缁熻", description = "鏌ヨ鐢ㄦ埛鐨勭敓鐗╄瘑鍒娇鐢ㄧ粺璁′俊锟?)
    @GetMapping("/stats/user/{userId}")
    public ResponseDTO<BiometricTemplateVO> getUserBiometricStats(
            @Parameter(description = "鐢ㄦ埛ID", example = "1001")
            @PathVariable Long userId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[鐢熺墿璇嗗埆API] 鏌ヨ鐢ㄦ埛缁熻 userId={}, clientIp={}", userId, clientIp);

        return biometricAuthService.getUserBiometricStats(userId);
    }

    // ========== 璁惧鍗忚涓撶敤鎺ュ彛 ==========

    /**
     * 闂ㄧ璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉
     * 璁惧鍗忚锛欰CCESS_AUTHENTICATE
     * 涓ユ牸閬靛惊闂ㄧ璁惧閫氳鍗忚
     */
    @Operation(summary = "闂ㄧ璁惧楠岃瘉", description = "闂ㄧ璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉鎺ュ彛")
    @PostMapping("/access/authenticate")
    public ResponseDTO<BiometricAuthResultVO> accessAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 璁剧疆楠岃瘉绫诲瀷涓洪棬绂侀獙锟?
        authForm.setAuthType(2); // 闂ㄧ楠岃瘉

        // 闂ㄧ楠岃瘉寮哄埗娲讳綋妫€锟?
        authForm.setForceLiveness(true);

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Access Device");

        log.info("[闂ㄧ璁惧鍗忚] 闂ㄧ鐢熺墿璇嗗埆楠岃瘉 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 鑰冨嫟璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉
     * 璁惧鍗忚锛欰TTENDANCE_AUTHENTICATE
     * 涓ユ牸閬靛惊鑰冨嫟璁惧閫氳鍗忚
     */
    @Operation(summary = "鑰冨嫟璁惧楠岃瘉", description = "鑰冨嫟璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉鎺ュ彛")
    @PostMapping("/attendance/authenticate")
    public ResponseDTO<BiometricAuthResultVO> attendanceAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 璁剧疆楠岃瘉绫诲瀷涓鸿€冨嫟楠岃瘉
        authForm.setAuthType(3); // 鑰冨嫟楠岃瘉

        // 鑰冨嫟楠岃瘉寮哄埗娲讳綋妫€娴嬶紝闃叉浠ｆ墦锟?
        authForm.setForceLiveness(true);

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Attendance Device");

        log.info("[鑰冨嫟璁惧鍗忚] 鑰冨嫟鐢熺墿璇嗗埆楠岃瘉 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 娑堣垂璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉
     * 璁惧鍗忚锛欳ONSUME_AUTHENTICATE
     * 涓ユ牸閬靛惊娑堣垂璁惧閫氳鍗忚
     */
    @Operation(summary = "娑堣垂璁惧楠岃瘉", description = "娑堣垂璁惧涓撶敤鐢熺墿璇嗗埆楠岃瘉鎺ュ彛")
    @PostMapping("/consume/authenticate")
    public ResponseDTO<BiometricAuthResultVO> consumeAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 璁剧疆楠岃瘉绫诲瀷涓烘秷璐归獙锟?
        authForm.setAuthType(4); // 娑堣垂楠岃瘉

        // 娑堣垂楠岃瘉瑕佹眰蹇€熷搷搴旓紝鍙€傚綋闄嶄綆娲讳綋妫€娴嬭锟?
        if (authForm.getForceLiveness() == null) {
            authForm.setForceLiveness(false);
        }

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Consume Device");

        log.info("[娑堣垂璁惧鍗忚] 娑堣垂鐢熺墿璇嗗埆楠岃瘉 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 璁惧蹇冭烦妫€锟?
     * 璁惧鍗忚锛欻EARTBEAT
     */
    @Operation(summary = "璁惧蹇冭烦", description = "璁惧蹇冭烦妫€娴嬫帴锟?)
    @PostMapping("/device/heartbeat")
    public ResponseDTO<String> heartbeat(
            @Parameter(description = "璁惧ID", example = "DEVICE_001")
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[璁惧鍗忚] 璁惧蹇冭烦 deviceId={}, clientIp={}, timestamp={}",
                deviceId, clientIp, System.currentTimeMillis());

        return ResponseDTO.ok("heartbeat_received");
    }

    /**
     * 璁惧鐘舵€佷笂锟?
     * 璁惧鍗忚锛欴EVICE_STATUS_REPORT
     */
    @Operation(summary = "璁惧鐘舵€佷笂锟?, description = "璁惧鐘舵€佷俊鎭笂锟?)
    @PostMapping("/device/status")
    public ResponseDTO<String> deviceStatusReport(
            @Parameter(description = "璁惧ID", example = "DEVICE_001")
            @RequestParam String deviceId,
            @Parameter(description = "璁惧鐘讹拷?, example = "online")
            @RequestParam String status,
            @Parameter(description = "璁惧璇︽儏", example = "姝ｅ父宸ヤ綔")
            @RequestParam(required = false) String details,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[璁惧鍗忚] 璁惧鐘舵€佷笂锟?deviceId={}, status={}, details={}, clientIp={}",
                deviceId, status, details, clientIp);

        // 杩欓噷鍙互璁板綍璁惧鐘舵€佸埌鏁版嵁锟?
        // deviceStatusService.reportDeviceStatus(deviceId, status, details, clientIp);

        return ResponseDTO.ok("status_received");
    }
}
