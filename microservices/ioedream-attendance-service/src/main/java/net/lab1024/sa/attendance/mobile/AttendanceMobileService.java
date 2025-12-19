package net.lab1024.sa.attendance.mobile;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.mobile.model.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤移动端服务接口
 * <p>
 * 定义移动端考勤功能的API接口
 * 严格遵循CLAUDE.md全局架构规范，统一使用ResponseDTO返回类型
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@RestController
@RequestMapping("/api/mobile/v1/attendance")
public interface AttendanceMobileService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    ResponseDTO<MobileLoginResult> login(@RequestBody MobileLoginRequest request);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    @PostMapping("/logout")
    ResponseDTO<MobileLogoutResult> logout(@RequestHeader("Authorization") String token);

    /**
     * 刷新令牌
     *
     * @param request 刷新请求
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    ResponseDTO<MobileTokenRefreshResult> refreshToken(@RequestBody MobileTokenRefreshRequest request);

    /**
     * 获取用户信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    @GetMapping("/user/info")
    ResponseDTO<MobileUserInfoResult> getUserInfo(@RequestHeader("Authorization") String token);

    /**
     * 获取今日考勤状态
     *
     * @param token 访问令牌
     * @return 今日考勤状态
     */
    @GetMapping("/today/status")
    ResponseDTO<MobileTodayStatusResult> getTodayStatus(@RequestHeader("Authorization") String token);

    /**
     * 上班打卡
     *
     * @param request 打卡请求
     * @param token 访问令牌
     * @return 打卡结果
     */
    @PostMapping("/clock-in")
    ResponseDTO<MobileClockInResult> clockIn(
            @RequestBody MobileClockInRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 下班打卡
     *
     * @param request 打卡请求
     * @param token 访问令牌
     * @return 打卡结果
     */
    @PostMapping("/clock-out")
    ResponseDTO<MobileClockOutResult> clockOut(
            @RequestBody MobileClockOutRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取考勤记录列表
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 考勤记录列表
     */
    @GetMapping("/records")
    ResponseDTO<MobileAttendanceRecordsResult> getAttendanceRecords(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileRecordQueryParam queryParam);

    /**
     * 获取考勤统计
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 考勤统计
     */
    @GetMapping("/statistics")
    ResponseDTO<MobileStatisticsResult> getStatistics(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileStatisticsQueryParam queryParam);

    /**
     * 申请请假
     *
     * @param request 请假请求
     * @param token 访问令牌
     * @return 请假申请结果
     */
    @PostMapping("/leave/apply")
    ResponseDTO<MobileLeaveApplicationResult> applyLeave(
            @RequestBody MobileLeaveApplicationRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取请假记录
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 请假记录
     */
    @GetMapping("/leave/records")
    ResponseDTO<MobileLeaveRecordsResult> getLeaveRecords(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileLeaveQueryParam queryParam);

    /**
     * 申请销假
     *
     * @param request 销假申请请求
     * @param token 访问令牌
     * @return 销假申请结果
     */
    @PostMapping("/leave/cancel")
    ResponseDTO<MobileLeaveCancellationResult> cancelLeave(
            @RequestBody MobileLeaveCancellationRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取班次信息
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 班次信息
     */
    @GetMapping("/shifts")
    ResponseDTO<MobileShiftsResult> getShifts(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileShiftQueryParam queryParam);

    /**
     * 获取排班信息
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 排班信息
     */
    @GetMapping("/schedule")
    ResponseDTO<MobileScheduleResult> getSchedule(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileScheduleQueryParam queryParam);

    /**
     * 考勤提醒设置
     *
     * @param request 设置请求
     * @param token 访问令牌
     * @return 设置结果
     */
    @PostMapping("/reminder/settings")
    ResponseDTO<MobileReminderSettingsResult> setReminderSettings(
            @RequestBody MobileReminderSettingsRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取考勤提醒
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 提醒列表
     */
    @GetMapping("/reminders")
    ResponseDTO<MobileRemindersResult> getReminders(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileReminderQueryParam queryParam);

    /**
     * 获取考勤日历
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 考勤日历
     */
    @GetMapping("/calendar")
    ResponseDTO<MobileCalendarResult> getCalendar(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileCalendarQueryParam queryParam);

    /**
     * 上传头像
     *
     * @param request 上传请求
     * @param token 访问令牌
     * @return 上传结果
     */
    @PostMapping("/profile/avatar/upload")
    ResponseDTO<MobileAvatarUploadResult> uploadAvatar(
            @ModelAttribute MobileAvatarUploadRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取用户配置
     *
     * @param token 访问令牌
     * @return 用户配置
     */
    @GetMapping("/profile/settings")
    ResponseDTO<MobileProfileSettingsResult> getProfileSettings(
            @RequestHeader("Authorization") String token);

    /**
     * 更新用户配置
     *
     * @param request 更新请求
     * @param token 访问令牌
     * @return 更新结果
     */
    @PostMapping("/profile/settings")
    ResponseDTO<MobileProfileSettingsUpdateResult> updateProfileSettings(
            @RequestBody MobileProfileSettingsUpdateRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取应用版本信息
     *
     * @return 版本信息
     */
    @GetMapping("/app/version")
    ResponseDTO<MobileAppVersionResult> getAppVersion();

    /**
     * 检查应用更新
     *
     * @param request 版本检查请求
     * @return 更新检查结果
     */
    @PostMapping("/app/update/check")
    ResponseDTO<MobileAppUpdateCheckResult> checkAppUpdate(@RequestBody MobileAppUpdateCheckRequest request);

    /**
     * 获取系统通知
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 通知列表
     */
    @GetMapping("/notifications")
    ResponseDTO<MobileNotificationsResult> getNotifications(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileNotificationQueryParam queryParam);

    /**
     * 标记通知已读
     *
     * @param notificationId 通知ID
     * @param token 访问令牌
     * @return 标记结果
     */
    @PostMapping("/notifications/{notificationId}/read")
    ResponseDTO<MobileNotificationReadResult> markNotificationAsRead(
            @PathVariable String notificationId,
            @RequestHeader("Authorization") String token);

    /**
     * 获取考勤异常
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 异常列表
     */
    @GetMapping("/anomalies")
    ResponseDTO<MobileAnomaliesResult> getAnomalies(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileAnomalyQueryParam queryParam);

    /**
     * 获取考勤排行榜
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 排行榜数据
     */
    @GetMapping("/leaderboard")
    ResponseDTO<MobileLeaderboardResult> getLeaderboard(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileLeaderboardQueryParam queryParam);

    /**
     * 获取考勤统计图表
     *
     * @param token 访问令牌
     * @param queryParam 查询参数
     * @return 图表数据
     */
    @GetMapping("/charts")
    ResponseDTO<MobileChartsResult> getCharts(
            @RequestHeader("Authorization") String token,
            @ModelAttribute MobileChartQueryParam queryParam);

    /**
     * 生物识别验证
     *
     * @param request 生物识别请求
     * @param token 访问令牌
     * @return 验证结果
     */
    @PostMapping("/biometric/verify")
    ResponseDTO<MobileBiometricVerificationResult> verifyBiometric(
            @RequestBody MobileBiometricVerificationRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取位置信息
     *
     * @param token 访问令牌
     * @return 位置信息
     */
    @GetMapping("/location/current")
    ResponseDTO<MobileLocationResult> getCurrentLocation(@RequestHeader("Authorization") String token);

    /**
     * 上报位置
     *
     * @param request 位置上报请求
     * @param token 访问令牌
     * @return 上报结果
     */
    @PostMapping("/location/report")
    ResponseDTO<MobileLocationReportResult> reportLocation(
            @RequestBody MobileLocationReportRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取设备信息
     *
     * @param token 访问令牌
     * @return 设备信息
     */
    @GetMapping("/device/info")
    ResponseDTO<MobileDeviceInfoResult> getDeviceInfo(@RequestHeader("Authorization") String token);

    /**
     * 设备注册
     *
     * @param request 设备注册请求
     * @param token 访问令牌
     * @return 注册结果
     */
    @PostMapping("/device/register")
    ResponseDTO<MobileDeviceRegisterResult> registerDevice(
            @RequestBody MobileDeviceRegisterRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取安全设置
     *
     * @param token 访问令牌
     * @return 安全设置
     */
    @GetMapping("/security/settings")
    ResponseDTO<MobileSecuritySettingsResult> getSecuritySettings(@RequestHeader("Authorization") String token);

    /**
     * 更新安全设置
     *
     * @param request 更新请求
     * @param token 访问令牌
     * @return 更新结果
     */
    @PostMapping("/security/settings")
    ResponseDTO<MobileSecuritySettingsUpdateResult> updateSecuritySettings(
            @RequestBody MobileSecuritySettingsUpdateRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 同步数据
     *
     * @param token 访问令牌
     * @return 同步结果
     */
    @PostMapping("/data/sync")
    ResponseDTO<MobileDataSyncResult> syncData(@RequestHeader("Authorization") String token);

    /**
     * 获取离线数据
     *
     * @param token 访问令牌
     * @return 离线数据
     */
    @GetMapping("/data/offline")
    ResponseDTO<MobileOfflineDataResult> getOfflineData(@RequestHeader("Authorization") String token);

    /**
     * 上传离线数据
     *
     * @param request 上传请求
     * @param token 访问令牌
     * @return 上传结果
     */
    @PostMapping("/data/offline/upload")
    ResponseDTO<MobileOfflineDataUploadResult> uploadOfflineData(
            @RequestBody MobileOfflineDataUploadRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 健康检查
     *
     * @param token 访问令牌
     * @return 健康检查结果
     */
    @GetMapping("/health/check")
    ResponseDTO<MobileHealthCheckResult> healthCheck(@RequestHeader("Authorization") String token);

    /**
     * 性能测试
     *
     * @param request 性能测试请求
     * @param token 访问令牌
     * @return 测试结果
     */
    @PostMapping("/performance/test")
    ResponseDTO<MobilePerformanceTestResult> performanceTest(
            @RequestBody MobilePerformanceTestRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 用户反馈
     *
     * @param request 反馈请求
     * @param token 访问令牌
     * @return 反馈结果
     */
    @PostMapping("/feedback/submit")
    ResponseDTO<MobileFeedbackSubmitResult> submitFeedback(
            @RequestBody MobileFeedbackSubmitRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取帮助信息
     *
     * @param queryParam 查询参数
     * @return 帮助信息
     */
    @GetMapping("/help")
    ResponseDTO<MobileHelpResult> getHelp(@ModelAttribute MobileHelpQueryParam queryParam);

    /**
     * 获取使用统计
     *
     * @param token 访问令牌
     * @return 使用统计
     */
    @GetMapping("/usage/statistics")
    ResponseDTO<MobileUsageStatisticsResult> getUsageStatistics(@RequestHeader("Authorization") String token);
}
