package net.lab1024.sa.attendance.mobile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.mobile.AttendanceMobileService;
import net.lab1024.sa.attendance.mobile.model.*;
import net.lab1024.sa.common.dto.ResponseDTO;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 考勤移动端控制器
 * <p>
 * 提供移动端考勤功能的REST API接口
 * 高性能移动端适配，支持异步处理和内存优化
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "移动端考勤管理", description = "移动端考勤相关API接口")
@Validated
public class AttendanceMobileController {

    private final AttendanceMobileService attendanceMobileService;

    // ==================== 用户认证相关 ====================

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "移动端用户登录")
    public ResponseDTO<MobileLoginResult> login(@Valid @RequestBody MobileLoginRequest request) {
        try {
            MobileLoginResult result = attendanceMobileService.login(request).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 用户登录失败", e);
            return ResponseDTO.error("登录失败，请稍后重试");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "移动端用户登出")
    public ResponseDTO<MobileLogoutResult> logout(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileLogoutResult result = attendanceMobileService.logout(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 用户登出失败", e);
            return ResponseDTO.error("登出失败，请稍后重试");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "刷新访问令牌")
    public ResponseDTO<MobileTokenRefreshResult> refreshToken(@Valid @RequestBody MobileTokenRefreshRequest request) {
        try {
            MobileTokenRefreshResult result = attendanceMobileService.refreshToken(request).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 刷新令牌失败", e);
            return ResponseDTO.error("刷新令牌失败，请稍后重试");
        }
    }

    // ==================== 用户信息相关 ====================

    @GetMapping("/user/info")
    @Operation(summary = "获取用户信息", description = "获取当前用户信息")
    public ResponseDTO<MobileUserInfoResult> getUserInfo(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileUserInfoResult result = attendanceMobileService.getUserInfo(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取用户信息失败", e);
            return ResponseDTO.error("获取用户信息失败，请稍后重试");
        }
    }

    // ==================== 今日考勤状态 ====================

    @GetMapping("/today/status")
    @Operation(summary = "获取今日考勤状态", description = "获取今日考勤状态信息")
    public ResponseDTO<MobileTodayStatusResult> getTodayStatus(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileTodayStatusResult result = attendanceMobileService.getTodayStatus(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取今日考勤状态失败", e);
            return ResponseDTO.error("获取今日考勤状态失败，请稍后重试");
        }
    }

    // ==================== 打卡相关 ====================

    @PostMapping("/clock-in")
    @Operation(summary = "上班打卡", description = "用户上班打卡")
    public ResponseDTO<MobileClockInResult> clockIn(
            @Valid @RequestBody MobileClockInRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileClockInResult result = attendanceMobileService.clockIn(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 上班打卡失败", e);
            return ResponseDTO.error("上班打卡失败，请稍后重试");
        }
    }

    @PostMapping("/clock-out")
    @Operation(summary = "下班打卡", description = "用户下班打卡")
    public ResponseDTO<MobileClockOutResult> clockOut(
            @Valid @RequestBody MobileClockOutRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileClockOutResult result = attendanceMobileService.clockOut(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 下班打卡失败", e);
            return ResponseDTO.error("下班打卡失败，请稍后重试");
        }
    }

    // ==================== 考勤记录查询 ====================

    @GetMapping("/records")
    @Operation(summary = "获取考勤记录列表", description = "分页查询考勤记录")
    public ResponseDTO<MobileAttendanceRecordsResult> getAttendanceRecords(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileRecordQueryParam queryParam) {
        try {
            MobileAttendanceRecordsResult result = attendanceMobileService.getAttendanceRecords(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 查询考勤记录失败", e);
            return ResponseDTO.error("查询考勤记录失败，请稍后重试");
        }
    }

    // ==================== 考勤统计 ====================

    @GetMapping("/statistics")
    @Operation(summary = "获取考勤统计", description = "查询考勤统计数据")
    public ResponseDTO<MobileStatisticsResult> getStatistics(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileStatisticsQueryParam queryParam) {
        try {
            MobileStatisticsResult result = attendanceMobileService.getStatistics(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 查询考勤统计失败", e);
            return ResponseDTO.error("查询考勤统计失败，请稍后重试");
        }
    }

    // ==================== 请假相关 ====================

    @PostMapping("/leave/apply")
    @Operation(summary = "申请请假", description = "用户申请请假")
    public ResponseDTO<MobileLeaveApplicationResult> applyLeave(
            @Valid @RequestBody MobileLeaveApplicationRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileLeaveApplicationResult result = attendanceMobileService.applyLeave(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 请假申请失败", e);
            return ResponseDTO.error("请假申请失败，请稍后重试");
        }
    }

    @GetMapping("/leave/records")
    @Operation(summary = "获取请假记录", description = "查询用户请假记录")
    public ResponseDTO<MobileLeaveRecordsResult> getLeaveRecords(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileLeaveQueryParam queryParam) {
        try {
            MobileLeaveRecordsResult result = attendanceMobileService.getLeaveRecords(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 查询请假记录失败", e);
            return ResponseDTO.error("查询请假记录失败，请稍后重试");
        }
    }

    @PostMapping("/leave/cancel")
    @Operation(summary = "申请销假", description = "用户申请销假")
    public ResponseDTO<MobileLeaveCancellationResult> cancelLeave(
            @Valid @RequestBody MobileLeaveCancellationRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileLeaveCancellationResult result = attendanceMobileService.cancelLeave(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 销假申请失败", e);
            return ResponseDTO.error("销假申请失败，请稍后重试");
        }
    }

    // ==================== 班次信息 ====================

    @GetMapping("/shifts")
    @Operation(summary = "获取班次信息", description = "查询班次信息")
    public ResponseDTO<MobileShiftsResult> getShifts(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileShiftQueryParam queryParam) {
        try {
            MobileShiftsResult result = attendanceMobileService.getShifts(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 查询班次信息失败", e);
            return ResponseDTO.error("查询班次信息失败，请稍后重试");
        }
    }

    @GetMapping("/schedule")
    @Operation(summary = "获取排班信息", description = "查询排班信息")
    public ResponseDTO<MobileScheduleResult> getSchedule(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileScheduleQueryParam queryParam) {
        try {
            MobileScheduleResult result = attendanceMobileService.getSchedule(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 查询排班信息失败", e);
            return ResponseDTO.error("查询排班信息失败，请稍后重试");
        }
    }

    // ==================== 生物识别验证 ====================

    @PostMapping("/biometric/verify")
    @Operation(summary = "生物识别验证", description = "验证用户生物识别信息")
    public ResponseDTO<MobileBiometricVerificationResult> verifyBiometric(
            @Valid @RequestBody MobileBiometricVerificationRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileBiometricVerificationResult result = attendanceMobileService.verifyBiometric(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 生物识别验证失败", e);
            return ResponseDTO.error("生物识别验证失败，请稍后重试");
        }
    }

    // ==================== 位置相关 ====================

    @GetMapping("/location/current")
    @Operation(summary = "获取当前位置", description = "获取用户当前位置信息")
    public ResponseDTO<MobileLocationResult> getCurrentLocation(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileLocationResult result = attendanceMobileService.getCurrentLocation(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取位置信息失败", e);
            return ResponseDTO.error("获取位置信息失败，请稍后重试");
        }
    }

    @PostMapping("/location/report")
    @Operation(summary = "上报位置", description = "上报用户位置信息")
    public ResponseDTO<MobileLocationReportResult> reportLocation(
            @Valid @RequestBody MobileLocationReportRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileLocationReportResult result = attendanceMobileService.reportLocation(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 位置上报失败", e);
            return ResponseDTO.error("位置上报失败，请稍后重试");
        }
    }

    // ==================== 健康检查 ====================

    @GetMapping("/health/check")
    @Operation(summary = "健康检查", description = "检查移动端服务健康状态")
    public ResponseDTO<MobileHealthCheckResult> healthCheck(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileHealthCheckResult result = attendanceMobileService.healthCheck(token).get();
            if (result.isHealthy()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 健康检查失败", e);
            return ResponseDTO.error("健康检查失败，请稍后重试");
        }
    }

    // ==================== 以下接口提供基础控制器方法，具体实现可根据业务需求扩展 ====================

    @PostMapping("/reminder/settings")
    @Operation(summary = "考勤提醒设置", description = "设置考勤提醒")
    public ResponseDTO<MobileReminderSettingsResult> setReminderSettings(
            @Valid @RequestBody MobileReminderSettingsRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileReminderSettingsResult result = attendanceMobileService.setReminderSettings(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 设置考勤提醒失败", e);
            return ResponseDTO.error("设置考勤提醒失败，请稍后重试");
        }
    }

    @GetMapping("/reminders")
    @Operation(summary = "获取考勤提醒", description = "获取考勤提醒列表")
    public ResponseDTO<MobileRemindersResult> getReminders(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileReminderQueryParam queryParam) {
        try {
            MobileRemindersResult result = attendanceMobileService.getReminders(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取考勤提醒失败", e);
            return ResponseDTO.error("获取考勤提醒失败，请稍后重试");
        }
    }

    @GetMapping("/calendar")
    @Operation(summary = "获取考勤日历", description = "获取考勤日历数据")
    public ResponseDTO<MobileCalendarResult> getCalendar(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileCalendarQueryParam queryParam) {
        try {
            MobileCalendarResult result = attendanceMobileService.getCalendar(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取考勤日历失败", e);
            return ResponseDTO.error("获取考勤日历失败，请稍后重试");
        }
    }

    @PostMapping("/profile/avatar/upload")
    @Operation(summary = "上传头像", description = "上传用户头像")
    public ResponseDTO<MobileAvatarUploadResult> uploadAvatar(
            @Valid @ModelAttribute MobileAvatarUploadRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileAvatarUploadResult result = attendanceMobileService.uploadAvatar(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 上传头像失败", e);
            return ResponseDTO.error("上传头像失败，请稍后重试");
        }
    }

    @GetMapping("/profile/settings")
    @Operation(summary = "获取用户配置", description = "获取用户个人配置")
    public ResponseDTO<MobileProfileSettingsResult> getProfileSettings(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileProfileSettingsResult result = attendanceMobileService.getProfileSettings(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取用户配置失败", e);
            return ResponseDTO.error("获取用户配置失败，请稍后重试");
        }
    }

    @PostMapping("/profile/settings")
    @Operation(summary = "更新用户配置", description = "更新用户个人配置")
    public ResponseDTO<MobileProfileSettingsUpdateResult> updateProfileSettings(
            @Valid @RequestBody MobileProfileSettingsUpdateRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileProfileSettingsUpdateResult result = attendanceMobileService.updateProfileSettings(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 更新用户配置失败", e);
            return ResponseDTO.error("更新用户配置失败，请稍后重试");
        }
    }

    @GetMapping("/app/version")
    @Operation(summary = "获取应用版本信息", description = "获取应用版本和更新信息")
    public ResponseDTO<MobileAppVersionResult> getAppVersion() {
        try {
            MobileAppVersionResult result = attendanceMobileService.getAppVersion().get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取应用版本信息失败", e);
            return ResponseDTO.error("获取应用版本信息失败，请稍后重试");
        }
    }

    @PostMapping("/app/update/check")
    @Operation(summary = "检查应用更新", description = "检查应用是否有更新版本")
    public ResponseDTO<MobileAppUpdateCheckResult> checkAppUpdate(@Valid @RequestBody MobileAppUpdateCheckRequest request) {
        try {
            MobileAppUpdateCheckResult result = attendanceMobileService.checkAppUpdate(request).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 检查应用更新失败", e);
            return ResponseDTO.error("检查应用更新失败，请稍后重试");
        }
    }

    @GetMapping("/notifications")
    @Operation(summary = "获取系统通知", description = "获取用户系统通知列表")
    public ResponseDTO<MobileNotificationsResult> getNotifications(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileNotificationQueryParam queryParam) {
        try {
            MobileNotificationsResult result = attendanceMobileService.getNotifications(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取系统通知失败", e);
            return ResponseDTO.error("获取系统通知失败，请稍后重试");
        }
    }

    @PostMapping("/notifications/{notificationId}/read")
    @Operation(summary = "标记通知已读", description = "标记通知为已读状态")
    public ResponseDTO<MobileNotificationReadResult> markNotificationAsRead(
            @Parameter(description = "通知ID", required = true)
            @PathVariable String notificationId,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileNotificationReadResult result = attendanceMobileService.markNotificationAsRead(notificationId, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 标记通知已读失败", e);
            return ResponseDTO.error("标记通知已读失败，请稍后重试");
        }
    }

    @GetMapping("/anomalies")
    @Operation(summary = "获取考勤异常", description = "获取用户考勤异常记录")
    public ResponseDTO<MobileAnomaliesResult> getAnomalies(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileAnomalyQueryParam queryParam) {
        try {
            MobileAnomaliesResult result = attendanceMobileService.getAnomalies(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取考勤异常失败", e);
            return ResponseDTO.error("获取考勤异常失败，请稍后重试");
        }
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "获取考勤排行榜", description = "获取考勤排行榜数据")
    public ResponseDTO<MobileLeaderboardResult> getLeaderboard(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileLeaderboardQueryParam queryParam) {
        try {
            MobileLeaderboardResult result = attendanceMobileService.getLeaderboard(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取考勤排行榜失败", e);
            return ResponseDTO.error("获取考勤排行榜失败，请稍后重试");
        }
    }

    @GetMapping("/charts")
    @Operation(summary = "获取考勤统计图表", description = "获取考勤统计图表数据")
    public ResponseDTO<MobileChartsResult> getCharts(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token,
            @Valid @ModelAttribute MobileChartQueryParam queryParam) {
        try {
            MobileChartsResult result = attendanceMobileService.getCharts(token, queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取考勤统计图表失败", e);
            return ResponseDTO.error("获取考勤统计图表失败，请稍后重试");
        }
    }

    @GetMapping("/device/info")
    @Operation(summary = "获取设备信息", description = "获取移动端设备信息")
    public ResponseDTO<MobileDeviceInfoResult> getDeviceInfo(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileDeviceInfoResult result = attendanceMobileService.getDeviceInfo(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取设备信息失败", e);
            return ResponseDTO.error("获取设备信息失败，请稍后重试");
        }
    }

    @PostMapping("/device/register")
    @Operation(summary = "设备注册", description = "注册移动端设备")
    public ResponseDTO<MobileDeviceRegisterResult> registerDevice(
            @Valid @RequestBody MobileDeviceRegisterRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileDeviceRegisterResult result = attendanceMobileService.registerDevice(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 设备注册失败", e);
            return ResponseDTO.error("设备注册失败，请稍后重试");
        }
    }

    @GetMapping("/security/settings")
    @Operation(summary = "获取安全设置", description = "获取用户安全设置")
    public ResponseDTO<MobileSecuritySettingsResult> getSecuritySettings(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileSecuritySettingsResult result = attendanceMobileService.getSecuritySettings(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取安全设置失败", e);
            return ResponseDTO.error("获取安全设置失败，请稍后重试");
        }
    }

    @PostMapping("/security/settings")
    @Operation(summary = "更新安全设置", description = "更新用户安全设置")
    public ResponseDTO<MobileSecuritySettingsUpdateResult> updateSecuritySettings(
            @Valid @RequestBody MobileSecuritySettingsUpdateRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileSecuritySettingsUpdateResult result = attendanceMobileService.updateSecuritySettings(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 更新安全设置失败", e);
            return ResponseDTO.error("更新安全设置失败，请稍后重试");
        }
    }

    @PostMapping("/data/sync")
    @Operation(summary = "同步数据", description = "同步用户数据到移动端")
    public ResponseDTO<MobileDataSyncResult> syncData(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileDataSyncResult result = attendanceMobileService.syncData(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 数据同步失败", e);
            return ResponseDTO.error("数据同步失败，请稍后重试");
        }
    }

    @GetMapping("/data/offline")
    @Operation(summary = "获取离线数据", description = "获取离线使用的考勤数据")
    public ResponseDTO<MobileOfflineDataResult> getOfflineData(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileOfflineDataResult result = attendanceMobileService.getOfflineData(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取离线数据失败", e);
            return ResponseDTO.error("获取离线数据失败，请稍后重试");
        }
    }

    @PostMapping("/data/offline/upload")
    @Operation(summary = "上传离线数据", description = "上传离线期间产生的考勤数据")
    public ResponseDTO<MobileOfflineDataUploadResult> uploadOfflineData(
            @Valid @RequestBody MobileOfflineDataUploadRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileOfflineDataUploadResult result = attendanceMobileService.uploadOfflineData(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 上传离线数据失败", e);
            return ResponseDTO.error("上传离线数据失败，请稍后重试");
        }
    }

    @PostMapping("/performance/test")
    @Operation(summary = "性能测试", description = "移动端性能测试")
    public ResponseDTO<MobilePerformanceTestResult> performanceTest(
            @Valid @RequestBody MobilePerformanceTestRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobilePerformanceTestResult result = attendanceMobileService.performanceTest(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 性能测试失败", e);
            return ResponseDTO.error("性能测试失败，请稍后重试");
        }
    }

    @PostMapping("/feedback/submit")
    @Operation(summary = "用户反馈", description = "提交用户反馈意见")
    public ResponseDTO<MobileFeedbackSubmitResult> submitFeedback(
            @Valid @RequestBody MobileFeedbackSubmitRequest request,
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileFeedbackSubmitResult result = attendanceMobileService.submitFeedback(request, token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 用户反馈失败", e);
            return ResponseDTO.error("用户反馈失败，请稍后重试");
        }
    }

    @GetMapping("/help")
    @Operation(summary = "获取帮助信息", description = "获取移动端使用帮助信息")
    public ResponseDTO<MobileHelpResult> getHelp(@Valid @ModelAttribute MobileHelpQueryParam queryParam) {
        try {
            MobileHelpResult result = attendanceMobileService.getHelp(queryParam).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取帮助信息失败", e);
            return ResponseDTO.error("获取帮助信息失败，请稍后重试");
        }
    }

    @GetMapping("/usage/statistics")
    @Operation(summary = "获取使用统计", description = "获取移动端使用统计信息")
    public ResponseDTO<MobileUsageStatisticsResult> getUsageStatistics(
            @Parameter(description = "访问令牌", required = true)
            @RequestHeader("Authorization") String token) {
        try {
            MobileUsageStatisticsResult result = attendanceMobileService.getUsageStatistics(token).get();
            if (result.isSuccess()) {
                return ResponseDTO.ok(result);
            } else {
                return ResponseDTO.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("[移动端] 获取使用统计失败", e);
            return ResponseDTO.error("获取使用统计失败，请稍后重试");
        }
    }
}