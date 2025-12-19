package net.lab1024.sa.attendance.openapi.service;

import net.lab1024.sa.attendance.openapi.domain.request.*;
import net.lab1024.sa.attendance.openapi.domain.response.*;
import net.lab1024.sa.common.openapi.domain.response.PageResult;

import java.util.List;

/**
 * 考勤管理开放API服务接口
 * 提供考勤打卡、排班管理、考勤统计等开放服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AttendanceOpenApiService {

    /**
     * 考勤打卡
     *
     * @param request 打卡请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 打卡响应
     */
    ClockInResponse clockIn(ClockInRequest request, String token, String clientIp);

    /**
     * 获取打卡记录列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 分页打卡记录
     */
    PageResult<AttendanceRecordResponse> getAttendanceRecords(AttendanceRecordQueryRequest request, String token);

    /**
     * 获取打卡记录详情
     *
     * @param recordId 记录ID
     * @param token 访问令牌
     * @return 打卡记录详情
     */
    AttendanceRecordDetailResponse getAttendanceRecordDetail(Long recordId, String token);

    /**
     * 获取用户排班信息
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param token 访问令牌
     * @return 排班列表
     */
    List<ScheduleResponse> getUserSchedule(Long userId, String startDate, String endDate, String token);

    /**
     * 获取排班规则列表
     *
     * @param onlyValid 仅获取有效班次
     * @param token 访问令牌
     * @return 班次列表
     */
    List<ShiftResponse> getShifts(Boolean onlyValid, String token);

    /**
     * 获取用户月度考勤统计
     *
     * @param userId 用户ID
     * @param yearMonth 年月（格式：YYYY-MM）
     * @param token 访问令牌
     * @return 月度考勤统计
     */
    MonthlyAttendanceStatisticsResponse getMonthlyStatistics(Long userId, String yearMonth, String token);

    /**
     * 获取部门考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param statisticsType 统计类型
     * @param token 访问令牌
     * @return 部门考勤统计
     */
    DepartmentAttendanceStatisticsResponse getDepartmentStatistics(Long departmentId, String startDate, String endDate,
                                                                     String statisticsType, String token);

    /**
     * 获取考勤异常记录
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 考勤异常记录
     */
    PageResult<AbnormalAttendanceResponse> getAbnormalAttendance(AbnormalAttendanceQueryRequest request, String token);

    /**
     * 申请考勤补卡
     *
     * @param request 补卡申请请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 补卡申请响应
     */
    SupplementApplicationResponse applySupplement(SupplementApplicationRequest request, String token, String clientIp);

    /**
     * 获取补卡申请列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 补卡申请列表
     */
    PageResult<SupplementApplicationResponse> getSupplementApplications(SupplementApplicationQueryRequest request, String token);

    /**
     * 审批补卡申请
     *
     * @param applicationId 申请ID
     * @param request 审批请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     */
    void approveSupplementApplication(Long applicationId, ApproveSupplementRequest request, String token, String clientIp);

    /**
     * 获取用户考勤画像
     *
     * @param userId 用户ID
     * @param days 统计周期（天）
     * @param token 访问令牌
     * @return 用户考勤画像
     */
    UserAttendanceProfileResponse getUserAttendanceProfile(Long userId, Integer days, String token);
}