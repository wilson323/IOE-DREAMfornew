package net.lab1024.sa.attendance.mobile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import net.lab1024.sa.attendance.mobile.model.MobileAnomaliesResult;
import net.lab1024.sa.attendance.mobile.model.MobileAnomalyQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileAppUpdateCheckRequest;
import net.lab1024.sa.attendance.mobile.model.MobileAppUpdateCheckResult;
import net.lab1024.sa.attendance.mobile.model.MobileAppVersionResult;
import net.lab1024.sa.attendance.mobile.model.MobileAttendanceRecordsResult;
import net.lab1024.sa.attendance.mobile.model.MobileAvatarUploadRequest;
import net.lab1024.sa.attendance.mobile.model.MobileAvatarUploadResult;
import net.lab1024.sa.attendance.mobile.model.MobileBiometricVerificationRequest;
import net.lab1024.sa.attendance.mobile.model.MobileBiometricVerificationResult;
import net.lab1024.sa.attendance.mobile.model.MobileCalendarQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileCalendarResult;
import net.lab1024.sa.attendance.mobile.model.MobileChartQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileChartsResult;
import net.lab1024.sa.attendance.mobile.model.MobileClockInRequest;
import net.lab1024.sa.attendance.mobile.model.MobileClockInResult;
import net.lab1024.sa.attendance.mobile.model.MobileClockOutRequest;
import net.lab1024.sa.attendance.mobile.model.MobileClockOutResult;
import net.lab1024.sa.attendance.mobile.model.MobileDataSyncResult;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceInfoResult;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceRegisterRequest;
import net.lab1024.sa.attendance.mobile.model.MobileDeviceRegisterResult;
import net.lab1024.sa.attendance.mobile.model.MobileFeedbackSubmitRequest;
import net.lab1024.sa.attendance.mobile.model.MobileFeedbackSubmitResult;
import net.lab1024.sa.attendance.mobile.model.MobileHealthCheckResult;
import net.lab1024.sa.attendance.mobile.model.MobileHelpQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileHelpResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaderboardQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileLeaderboardResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveApplicationRequest;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveApplicationResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveCancellationRequest;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveCancellationResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveRecordsResult;
import net.lab1024.sa.attendance.mobile.model.MobileLocationReportRequest;
import net.lab1024.sa.attendance.mobile.model.MobileLocationReportResult;
import net.lab1024.sa.attendance.mobile.model.MobileLocationResult;
import net.lab1024.sa.attendance.mobile.model.MobileLoginRequest;
import net.lab1024.sa.attendance.mobile.model.MobileLoginResult;
import net.lab1024.sa.attendance.mobile.model.MobileLogoutResult;
import net.lab1024.sa.attendance.mobile.model.MobileNotificationQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileNotificationReadResult;
import net.lab1024.sa.attendance.mobile.model.MobileNotificationsResult;
import net.lab1024.sa.attendance.mobile.model.MobileOfflineDataResult;
import net.lab1024.sa.attendance.mobile.model.MobileOfflineDataUploadRequest;
import net.lab1024.sa.attendance.mobile.model.MobileOfflineDataUploadResult;
import net.lab1024.sa.attendance.mobile.model.MobilePerformanceTestRequest;
import net.lab1024.sa.attendance.mobile.model.MobilePerformanceTestResult;
import net.lab1024.sa.attendance.mobile.model.MobileProfileSettingsResult;
import net.lab1024.sa.attendance.mobile.model.MobileProfileSettingsUpdateRequest;
import net.lab1024.sa.attendance.mobile.model.MobileProfileSettingsUpdateResult;
import net.lab1024.sa.attendance.mobile.model.MobileRecordQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileReminderQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileReminderSettingsRequest;
import net.lab1024.sa.attendance.mobile.model.MobileReminderSettingsResult;
import net.lab1024.sa.attendance.mobile.model.MobileRemindersResult;
import net.lab1024.sa.attendance.mobile.model.MobileScheduleQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileScheduleResult;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsResult;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsUpdateRequest;
import net.lab1024.sa.attendance.mobile.model.MobileSecuritySettingsUpdateResult;
import net.lab1024.sa.attendance.mobile.model.MobileShiftQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileShiftsResult;
import net.lab1024.sa.attendance.mobile.model.MobileStatisticsQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileStatisticsResult;
import net.lab1024.sa.attendance.mobile.model.MobileTodayStatusResult;
import net.lab1024.sa.attendance.mobile.model.MobileTokenRefreshRequest;
import net.lab1024.sa.attendance.mobile.model.MobileTokenRefreshResult;
import net.lab1024.sa.attendance.mobile.model.MobileUsageStatisticsResult;
import net.lab1024.sa.attendance.mobile.model.MobileUserInfoResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 考勤移动端服务接口
 * <p>
 * 定义移动端考勤功能的API接口
 * 严格遵循CLAUDE.md全局架构规范，统一使用ResponseDTO返回类型
 * </p>
 * <p>
 * 注意：这是Service接口，不应该包含@RestController注解
 * Controller层应该单独实现，通过调用此Service接口提供服务
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AttendanceMobileService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录结果
     */
    ResponseDTO<MobileLoginResult> login(MobileLoginRequest request);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    ResponseDTO<MobileLogoutResult> logout(String token);

    /**
     * 刷新令牌
     *
     * @param request 刷新请求
     * @return 刷新结果
     */
    ResponseDTO<MobileTokenRefreshResult> refreshToken(MobileTokenRefreshRequest request);

    /**
     * 获取用户信息
     *
     * @param token 访问令牌
     * @return 用户信息
     */
    ResponseDTO<MobileUserInfoResult> getUserInfo(String token);

    /**
     * 获取今日考勤状态
     *
     * @param token 访问令牌
     * @return 今日考勤状态
     */
    ResponseDTO<MobileTodayStatusResult> getTodayStatus(String token);

    /**
     * 上班打卡
     *
     * @param request 打卡请求
     * @param token   访问令牌
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
     * @param token   访问令牌
     * @return 打卡结果
     */
    @PostMapping("/clock-out")
    ResponseDTO<MobileClockOutResult> clockOut(
            @RequestBody MobileClockOutRequest request,
            @RequestHeader("Authorization") String token);

    /**
     * 获取考勤记录列表
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 考勤记录列表
     */
    ResponseDTO<MobileAttendanceRecordsResult> getAttendanceRecords(
            String token,
            MobileRecordQueryParam queryParam);

    /**
     * 获取考勤统计
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 考勤统计
     */
    ResponseDTO<MobileStatisticsResult> getStatistics(
            String token,
            MobileStatisticsQueryParam queryParam);

    /**
     * 申请请假
     *
     * @param request 请假请求
     * @param token   访问令牌
     * @return 请假申请结果
     */
    ResponseDTO<MobileLeaveApplicationResult> applyLeave(
            MobileLeaveApplicationRequest request,
            String token);

    /**
     * 获取请假记录
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 请假记录
     */
    ResponseDTO<MobileLeaveRecordsResult> getLeaveRecords(
            String token,
            MobileLeaveQueryParam queryParam);

    /**
     * 申请销假
     *
     * @param request 销假申请请求
     * @param token   访问令牌
     * @return 销假申请结果
     */
    ResponseDTO<MobileLeaveCancellationResult> cancelLeave(
            MobileLeaveCancellationRequest request,
            String token);

    /**
     * 获取班次信息
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 班次信息
     */
    ResponseDTO<MobileShiftsResult> getShifts(
            String token,
            MobileShiftQueryParam queryParam);

    /**
     * 获取排班信息
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 排班信息
     */
    ResponseDTO<MobileScheduleResult> getSchedule(
            String token,
            MobileScheduleQueryParam queryParam);

    /**
     * 考勤提醒设置
     *
     * @param request 设置请求
     * @param token   访问令牌
     * @return 设置结果
     */
    ResponseDTO<MobileReminderSettingsResult> setReminderSettings(
            MobileReminderSettingsRequest request,
            String token);

    /**
     * 获取考勤提醒
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 提醒列表
     */
    ResponseDTO<MobileRemindersResult> getReminders(
            String token,
            MobileReminderQueryParam queryParam);

    /**
     * 获取考勤日历
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 考勤日历
     */
    ResponseDTO<MobileCalendarResult> getCalendar(
            String token,
            MobileCalendarQueryParam queryParam);

    /**
     * 上传头像
     *
     * @param request 上传请求
     * @param token   访问令牌
     * @return 上传结果
     */
    ResponseDTO<MobileAvatarUploadResult> uploadAvatar(
            MobileAvatarUploadRequest request,
            String token);

    /**
     * 获取用户配置
     *
     * @param token 访问令牌
     * @return 用户配置
     */
    ResponseDTO<MobileProfileSettingsResult> getProfileSettings(
            String token);

    /**
     * 更新用户配置
     *
     * @param request 更新请求
     * @param token   访问令牌
     * @return 更新结果
     */
    ResponseDTO<MobileProfileSettingsUpdateResult> updateProfileSettings(
            MobileProfileSettingsUpdateRequest request,
            String token);

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
    ResponseDTO<MobileAppUpdateCheckResult> checkAppUpdate(MobileAppUpdateCheckRequest request);

    /**
     * 获取系统通知
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 通知列表
     */
    ResponseDTO<MobileNotificationsResult> getNotifications(
            String token,
            MobileNotificationQueryParam queryParam);

    /**
     * 标记通知已读
     *
     * @param notificationId 通知ID
     * @param token          访问令牌
     * @return 标记结果
     */
    ResponseDTO<MobileNotificationReadResult> markNotificationAsRead(
            String notificationId,
            String token);

    /**
     * 获取考勤异常
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 异常列表
     */
    ResponseDTO<MobileAnomaliesResult> getAnomalies(
            String token,
            MobileAnomalyQueryParam queryParam);

    /**
     * 获取考勤排行榜
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 排行榜数据
     */
    ResponseDTO<MobileLeaderboardResult> getLeaderboard(
            String token,
            MobileLeaderboardQueryParam queryParam);

    /**
     * 获取考勤统计图表
     *
     * @param token      访问令牌
     * @param queryParam 查询参数
     * @return 图表数据
     */
    ResponseDTO<MobileChartsResult> getCharts(
            String token,
            MobileChartQueryParam queryParam);

    /**
     * 生物识别验证
     *
     * @param request 生物识别请求
     * @param token   访问令牌
     * @return 验证结果
     */
    ResponseDTO<MobileBiometricVerificationResult> verifyBiometric(
            MobileBiometricVerificationRequest request,
            String token);

    /**
     * 获取位置信息
     *
     * @param token 访问令牌
     * @return 位置信息
     */
    ResponseDTO<MobileLocationResult> getCurrentLocation(String token);

    /**
     * 上报位置
     *
     * @param request 位置上报请求
     * @param token   访问令牌
     * @return 上报结果
     */
    ResponseDTO<MobileLocationReportResult> reportLocation(
            MobileLocationReportRequest request,
            String token);

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
     * @param token   访问令牌
     * @return 注册结果
     */
    ResponseDTO<MobileDeviceRegisterResult> registerDevice(
            MobileDeviceRegisterRequest request,
            String token);

    /**
     * 获取安全设置
     *
     * @param token 访问令牌
     * @return 安全设置
     */
    ResponseDTO<MobileSecuritySettingsResult> getSecuritySettings(String token);

    /**
     * 更新安全设置
     *
     * @param request 更新请求
     * @param token   访问令牌
     * @return 更新结果
     */
    ResponseDTO<MobileSecuritySettingsUpdateResult> updateSecuritySettings(
            MobileSecuritySettingsUpdateRequest request,
            String token);

    /**
     * 同步数据
     *
     * @param token 访问令牌
     * @return 同步结果
     */
    ResponseDTO<MobileDataSyncResult> syncData(String token);

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
     * @param token   访问令牌
     * @return 上传结果
     */
    ResponseDTO<MobileOfflineDataUploadResult> uploadOfflineData(
            MobileOfflineDataUploadRequest request,
            String token);

    /**
     * 健康检查
     *
     * @param token 访问令牌
     * @return 健康检查结果
     */
    ResponseDTO<MobileHealthCheckResult> healthCheck(String token);

    /**
     * 性能测试
     *
     * @param request 性能测试请求
     * @param token   访问令牌
     * @return 测试结果
     */
    ResponseDTO<MobilePerformanceTestResult> performanceTest(
            MobilePerformanceTestRequest request,
            String token);

    /**
     * 用户反馈
     *
     * @param request 反馈请求
     * @param token   访问令牌
     * @return 反馈结果
     */
    ResponseDTO<MobileFeedbackSubmitResult> submitFeedback(
            MobileFeedbackSubmitRequest request,
            String token);

    /**
     * 获取帮助信息
     *
     * @param queryParam 查询参数
     * @return 帮助信息
     */
    ResponseDTO<MobileHelpResult> getHelp(MobileHelpQueryParam queryParam);

    /**
     * 获取使用统计
     *
     * @param token 访问令牌
     * @return 使用统计
     */
    ResponseDTO<MobileUsageStatisticsResult> getUsageStatistics(String token);
}
