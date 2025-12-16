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
import net.lab1024.sa.common.core.domain.ResponseDTO;
import net.lab1024.sa.common.core.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 生物识别认证控制器
 * 严格遵循门禁设备通讯协议设计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/biometric")
@Tag(name = "生物识别认证", description = "生物识别认证相关接口")
public class BiometricAuthController {

    @Resource
    private BiometricAuthService biometricAuthService;

    /**
     * 注册生物识别模板
     * 设备协议：REGISTER_TEMPLATE
     */
    @Operation(summary = "注册生物识别模板", description = "用户注册生物识别特征模板")
    @PostMapping("/template/register")
    public ResponseDTO<BiometricTemplateVO> registerTemplate(
            @Valid @RequestBody BiometricRegisterForm registerForm,
            HttpServletRequest request) {

        // 记录客户端信息
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("[生物识别API] 注册模板请求 userId={}, biometricType={}, deviceId={}, clientIp={}",
                registerForm.getUserId(), registerForm.getBiometricType(), registerForm.getDeviceId(), clientIp);

        return biometricAuthService.registerTemplate(registerForm);
    }

    /**
     * 1:1生物识别验证
     * 设备协议：AUTHENTICATE
     */
    @Operation(summary = "1:1生物识别验证", description = "验证用户生物识别特征")
    @PostMapping("/authenticate")
    public ResponseDTO<BiometricAuthResultVO> authenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 补充客户端信息
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        authForm.setClientIp(clientIp);
        authForm.setClientInfo(userAgent);

        log.info("[生物识别API] 1:1验证请求 userId={}, biometricType={}, deviceId={}, clientIp={}",
                authForm.getUserId(), authForm.getBiometricType(), authForm.getDeviceId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 1:N生物识别识别
     * 设备协议：IDENTIFY
     */
    @Operation(summary = "1:N生物识别识别", description = "在所有用户中识别生物识别特征")
    @PostMapping("/identify")
    public ResponseDTO<List<BiometricAuthResultVO>> identify(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 补充客户端信息
        String clientIp = SmartRequestUtil.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        authForm.setClientIp(clientIp);
        authForm.setClientInfo(userAgent);

        log.info("[生物识别API] 1:N识别请求 biometricType={}, deviceId={}, limit={}, clientIp={}",
                authForm.getBiometricType(), authForm.getDeviceId(), authForm.getLimit(), clientIp);

        return biometricAuthService.identify(authForm);
    }

    /**
     * 获取用户生物识别模板列表
     * 设备协议：GET_USER_TEMPLATES
     */
    @Operation(summary = "获取用户模板列表", description = "查询用户的所有生物识别模板")
    @GetMapping("/template/user/{userId}")
    public ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long userId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[生物识别API] 查询用户模板 userId={}, clientIp={}", userId, clientIp);

        return biometricAuthService.getUserTemplates(userId);
    }

    /**
     * 更新模板状态
     * 设备协议：UPDATE_TEMPLATE_STATUS
     */
    @Operation(summary = "更新模板状态", description = "更新生物识别模板状态")
    @PutMapping("/template/{templateId}/status")
    public ResponseDTO<Void> updateTemplateStatus(
            @Parameter(description = "模板ID", example = "1001")
            @PathVariable Long templateId,
            @Parameter(description = "状态", example = "1")
            @RequestParam Integer status,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[生物识别API] 更新模板状态 templateId={}, status={}, clientIp={}",
                templateId, status, clientIp);

        return biometricAuthService.updateTemplateStatus(templateId, status);
    }

    /**
     * 删除用户模板
     * 设备协议：DELETE_USER_TEMPLATE
     */
    @Operation(summary = "删除用户模板", description = "删除用户指定类型的生物识别模板")
    @DeleteMapping("/template/user/{userId}/type/{biometricType}")
    public ResponseDTO<Void> deleteUserTemplate(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long userId,
            @Parameter(description = "生物识别类型", example = "1")
            @PathVariable Integer biometricType,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[生物识别API] 删除用户模板 userId={}, biometricType={}, clientIp={}",
                userId, biometricType, clientIp);

        return biometricAuthService.deleteUserTemplate(userId, biometricType);
    }

    /**
     * 获取用户生物识别统计信息
     * 设备协议：GET_USER_STATS
     */
    @Operation(summary = "获取用户生物识别统计", description = "查询用户的生物识别使用统计信息")
    @GetMapping("/stats/user/{userId}")
    public ResponseDTO<BiometricTemplateVO> getUserBiometricStats(
            @Parameter(description = "用户ID", example = "1001")
            @PathVariable Long userId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[生物识别API] 查询用户统计 userId={}, clientIp={}", userId, clientIp);

        return biometricAuthService.getUserBiometricStats(userId);
    }

    // ========== 设备协议专用接口 ==========

    /**
     * 门禁设备专用生物识别验证
     * 设备协议：ACCESS_AUTHENTICATE
     * 严格遵循门禁设备通讯协议
     */
    @Operation(summary = "门禁设备验证", description = "门禁设备专用生物识别验证接口")
    @PostMapping("/access/authenticate")
    public ResponseDTO<BiometricAuthResultVO> accessAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 设置验证类型为门禁验证
        authForm.setAuthType(2); // 门禁验证

        // 门禁验证强制活体检测
        authForm.setForceLiveness(true);

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Access Device");

        log.info("[门禁设备协议] 门禁生物识别验证 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 考勤设备专用生物识别验证
     * 设备协议：ATTENDANCE_AUTHENTICATE
     * 严格遵循考勤设备通讯协议
     */
    @Operation(summary = "考勤设备验证", description = "考勤设备专用生物识别验证接口")
    @PostMapping("/attendance/authenticate")
    public ResponseDTO<BiometricAuthResultVO> attendanceAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 设置验证类型为考勤验证
        authForm.setAuthType(3); // 考勤验证

        // 考勤验证强制活体检测，防止代打卡
        authForm.setForceLiveness(true);

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Attendance Device");

        log.info("[考勤设备协议] 考勤生物识别验证 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 消费设备专用生物识别验证
     * 设备协议：CONSUME_AUTHENTICATE
     * 严格遵循消费设备通讯协议
     */
    @Operation(summary = "消费设备验证", description = "消费设备专用生物识别验证接口")
    @PostMapping("/consume/authenticate")
    public ResponseDTO<BiometricAuthResultVO> consumeAuthenticate(
            @Valid @RequestBody BiometricAuthForm authForm,
            HttpServletRequest request) {

        // 设置验证类型为消费验证
        authForm.setAuthType(4); // 消费验证

        // 消费验证要求快速响应，可适当降低活体检测要求
        if (authForm.getForceLiveness() == null) {
            authForm.setForceLiveness(false);
        }

        String clientIp = SmartRequestUtil.getClientIp(request);
        authForm.setClientIp(clientIp);
        authForm.setClientInfo("Consume Device");

        log.info("[消费设备协议] 消费生物识别验证 deviceId={}, userId={}, clientIp={}",
                authForm.getDeviceId(), authForm.getUserId(), clientIp);

        return biometricAuthService.authenticate(authForm);
    }

    /**
     * 设备心跳检测
     * 设备协议：HEARTBEAT
     */
    @Operation(summary = "设备心跳", description = "设备心跳检测接口")
    @PostMapping("/device/heartbeat")
    public ResponseDTO<String> heartbeat(
            @Parameter(description = "设备ID", example = "DEVICE_001")
            @RequestParam String deviceId,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[设备协议] 设备心跳 deviceId={}, clientIp={}, timestamp={}",
                deviceId, clientIp, System.currentTimeMillis());

        return ResponseDTO.ok("heartbeat_received");
    }

    /**
     * 设备状态上报
     * 设备协议：DEVICE_STATUS_REPORT
     */
    @Operation(summary = "设备状态上报", description = "设备状态信息上报")
    @PostMapping("/device/status")
    public ResponseDTO<String> deviceStatusReport(
            @Parameter(description = "设备ID", example = "DEVICE_001")
            @RequestParam String deviceId,
            @Parameter(description = "设备状态", example = "online")
            @RequestParam String status,
            @Parameter(description = "设备详情", example = "正常工作")
            @RequestParam(required = false) String details,
            HttpServletRequest request) {

        String clientIp = SmartRequestUtil.getClientIp(request);

        log.info("[设备协议] 设备状态上报 deviceId={}, status={}, details={}, clientIp={}",
                deviceId, status, details, clientIp);

        // 这里可以记录设备状态到数据库
        // deviceStatusService.reportDeviceStatus(deviceId, status, details, clientIp);

        return ResponseDTO.ok("status_received");
    }
}