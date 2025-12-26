package net.lab1024.sa.attendance.mobile.device;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import net.lab1024.sa.attendance.mobile.auth.MobileAuthenticationService;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceInfo;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceInfoResult;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceRegisterRequest;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceRegisterResult;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsResult;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsUpdateRequest;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsUpdateResult;
import net.lab1024.sa.attendance.mobile.model.MobileUserSession;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端设备管理服务
 * <p>
 * 负责移动端设备管理相关的所有功能，包括：
 * - 设备信息查询
 * - 设备注册
 * - 安全设置管理
 * </p>
 * <p>
 * 从AttendanceMobileServiceImpl中抽取，遵循单一职责原则
 * </p>
 *
 * @author IOE-DREAM Refactoring Team
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MobileDeviceManagementService {

    @Resource
    private MobileAuthenticationService authenticationService;

    /**
     * 设备信息缓存（由AttendanceMobileServiceImpl传入）
     * key格式: "device:{employeeId}"
     * value: MobileDeviceInfo
     */
    private final Map<String, MobileDeviceInfo> deviceInfoCache = new ConcurrentHashMap<>();

    /**
     * 获取设备信息
     * <p>
     * 从缓存中获取用户的设备信息
     * </p>
     *
     * @param token 访问令牌
     * @return 设备信息
     */
    public ResponseDTO<MobileDeviceInfoResult> getDeviceInfo(String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // 从缓存获取设备信息
            MobileDeviceInfo deviceInfo = deviceInfoCache.get("device:" + session.getEmployeeId());
            MobileDeviceInfoResult result = MobileDeviceInfoResult.builder()
                    .employeeId(session.getEmployeeId())
                    .deviceInfo(deviceInfo != null ? deviceInfo.getDeviceInfo() : Collections.emptyMap())
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取设备信息] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备信息失败，请重试");
        }
    }

    /**
     * 注册设备
     * <p>
     * 注册移动端设备，生成设备ID
     * </p>
     *
     * @param request 设备注册请求
     * @param token   访问令牌
     * @return 注册结果
     */
    public ResponseDTO<MobileDeviceRegisterResult> registerDevice(MobileDeviceRegisterRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的设备注册逻辑
            // 1. 保存设备信息到数据库
            // 2. 将设备信息缓存到deviceInfoCache
            // 3. 关联设备与用户
            // 4. 返回设备ID

            String deviceId = UUID.randomUUID().toString();
            MobileDeviceRegisterResult result = MobileDeviceRegisterResult.builder()
                    .deviceId(deviceId)
                    .success(true)
                    .message("设备注册成功")
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端设备注册] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "设备注册失败，请重试");
        }
    }

    /**
     * 获取安全设置
     * <p>
     * 获取用户的安全设置配置
     * </p>
     *
     * @param token 访问令牌
     * @return 安全设置
     */
    public ResponseDTO<MobileSecuritySettingsResult> getSecuritySettings(String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的安全设置获取逻辑
            // 1. 从数据库获取用户安全设置
            // 2. 返回设置信息（生物识别开关、定位开关、通知开关等）

            MobileSecuritySettingsResult result = MobileSecuritySettingsResult.builder()
                    .employeeId(session.getEmployeeId())
                    .settings(Collections.emptyMap())
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取安全设置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取安全设置失败，请重试");
        }
    }

    /**
     * 更新安全设置
     * <p>
     * 更新用户的安全设置配置
     * </p>
     *
     * @param request 安全设置更新请求
     * @param token   访问令牌
     * @return 更新结果
     */
    public ResponseDTO<MobileSecuritySettingsUpdateResult> updateSecuritySettings(
            MobileSecuritySettingsUpdateRequest request, String token) {
        try {
            // 验证用户会话
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的安全设置更新逻辑
            // 1. 验证设置参数
            // 2. 保存设置到数据库
            // 3. 更新缓存
            // 4. 返回更新结果

            MobileSecuritySettingsUpdateResult result = MobileSecuritySettingsUpdateResult.builder()
                    .success(true)
                    .message("安全设置更新成功")
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端更新安全设置] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "更新安全设置失败，请重试");
        }
    }

    /**
     * 清除设备信息缓存
     * <p>
     * 用于用户登出时清除设备信息
     * </p>
     *
     * @param employeeId 员工ID
     */
    public void clearDeviceInfoCache(Long employeeId) {
        if (employeeId != null) {
            deviceInfoCache.remove("device:" + employeeId);
            log.debug("[设备管理] 清除设备信息缓存: employeeId={}", employeeId);
        }
    }

    /**
     * 获取设备信息缓存（供其他模块访问）
     *
     * @return 设备信息缓存
     */
    public Map<String, MobileDeviceInfo> getDeviceInfoCache() {
        return deviceInfoCache;
    }
}
