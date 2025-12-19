package net.lab1024.sa.attendance.openapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.openapi.domain.response.PageResult;
import net.lab1024.sa.attendance.openapi.domain.request.*;
import net.lab1024.sa.attendance.openapi.domain.response.*;
import net.lab1024.sa.attendance.openapi.service.AttendanceOpenApiService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 开放平台考勤管理API控制器
 * 提供考勤打卡、排班管理、考勤统计等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/open/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "开放平台考勤管理API", description = "提供考勤打卡、排班管理、考勤统计等功能")
@Validated
public class AttendanceOpenApiController {

    private final AttendanceOpenApiService attendanceOpenApiService;

    /**
     * 考勤打卡
     */
    @PostMapping("/clock")
    @Operation(summary = "考勤打卡", description = "用户进行考勤打卡")
    public ResponseDTO<ClockInResponse> clockIn(
            @Valid @RequestBody ClockInRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 考勤打卡请求: userId={}, clockType={}, clientIp={}",
                request.getUserId(), request.getClockType(), clientIp);

        ClockInResponse response = attendanceOpenApiService.clockIn(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取打卡记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "获取打卡记录列表", description = "分页获取考勤打卡记录")
    public ResponseDTO<PageResult<AttendanceRecordResponse>> getAttendanceRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "打卡类型") @RequestParam(required = false) String clockType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AttendanceRecordQueryRequest queryRequest = AttendanceRecordQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .clockType(clockType)
                .startDate(startDate)
                .endDate(endDate)
                .departmentId(departmentId)
                .build();

        log.info("[开放API] 查询打卡记录: pageNum={}, pageSize={}, userId={}, startDate={}",
                pageNum, pageSize, userId, startDate);

        PageResult<AttendanceRecordResponse> result = attendanceOpenApiService.getAttendanceRecords(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取打卡记录详情
     */
    @GetMapping("/records/{recordId}")
    @Operation(summary = "获取打卡记录详情", description = "根据记录ID获取打卡记录详情")
    public ResponseDTO<AttendanceRecordDetailResponse> getAttendanceRecordDetail(
            @Parameter(description = "记录ID") @PathVariable Long recordId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询打卡记录详情: recordId={}", recordId);

        AttendanceRecordDetailResponse response = attendanceOpenApiService.getAttendanceRecordDetail(recordId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取用户排班信息
     */
    @GetMapping("/schedule")
    @Operation(summary = "获取用户排班信息", description = "获取用户指定日期范围的排班信息")
    public ResponseDTO<List<ScheduleResponse>> getUserSchedule(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户排班: userId={}, startDate={}, endDate={}",
                userId, startDate, endDate);

        List<ScheduleResponse> schedules = attendanceOpenApiService.getUserSchedule(userId, startDate, endDate, token);
        return ResponseDTO.ok(schedules);
    }

    /**
     * 获取排班规则列表
     */
    @GetMapping("/shifts")
    @Operation(summary = "获取排班规则列表", description = "获取班次规则列表")
    public ResponseDTO<List<ShiftResponse>> getShifts(
            @Parameter(description = "是否仅获取有效班次") @RequestParam(defaultValue = "true") Boolean onlyValid,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询排班规则列表: onlyValid={}", onlyValid);

        List<ShiftResponse> shifts = attendanceOpenApiService.getShifts(onlyValid, token);
        return ResponseDTO.ok(shifts);
    }

    /**
     * 获取用户月度考勤统计
     */
    @GetMapping("/statistics/monthly")
    @Operation(summary = "获取用户月度考勤统计", description = "获取用户指定月份的考勤统计")
    public ResponseDTO<MonthlyAttendanceStatisticsResponse> getMonthlyStatistics(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "年月") @RequestParam String yearMonth,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询月度考勤统计: userId={}, yearMonth={}", userId, yearMonth);

        MonthlyAttendanceStatisticsResponse statistics = attendanceOpenApiService.getMonthlyStatistics(userId, yearMonth, token);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取部门考勤统计
     */
    @GetMapping("/statistics/department")
    @Operation(summary = "获取部门考勤统计", description = "获取部门指定日期范围的考勤统计")
    public ResponseDTO<DepartmentAttendanceStatisticsResponse> getDepartmentStatistics(
            @Parameter(description = "部门ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询部门考勤统计: departmentId={}, startDate={}, endDate={}",
                departmentId, startDate, endDate);

        DepartmentAttendanceStatisticsResponse statistics = attendanceOpenApiService.getDepartmentStatistics(
                departmentId, startDate, endDate, statisticsType, token);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 获取考勤异常记录
     */
    @GetMapping("/abnormal")
    @Operation(summary = "获取考勤异常记录", description = "获取考勤异常记录")
    public ResponseDTO<PageResult<AbnormalAttendanceResponse>> getAbnormalAttendance(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "异常类型") @RequestParam(required = false) String abnormalType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "处理状态") @RequestParam(required = false) String processStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AbnormalAttendanceQueryRequest queryRequest = AbnormalAttendanceQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .abnormalType(abnormalType)
                .startDate(startDate)
                .endDate(endDate)
                .processStatus(processStatus)
                .build();

        log.info("[开放API] 查询考勤异常记录: pageNum={}, pageSize={}, abnormalType={}",
                pageNum, pageSize, abnormalType);

        PageResult<AbnormalAttendanceResponse> result = attendanceOpenApiService.getAbnormalAttendance(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 申请考勤补卡
     */
    @PostMapping("/supplement")
    @Operation(summary = "申请考勤补卡", description = "用户申请补卡")
    public ResponseDTO<SupplementApplicationResponse> applySupplement(
            @Valid @RequestBody SupplementApplicationRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 申请考勤补卡: userId={}, supplementDate={}, clientIp={}",
                request.getUserId(), request.getSupplementDate(), clientIp);

        SupplementApplicationResponse response = attendanceOpenApiService.applySupplement(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取补卡申请列表
     */
    @GetMapping("/supplement/applications")
    @Operation(summary = "获取补卡申请列表", description = "获取补卡申请列表")
    public ResponseDTO<PageResult<SupplementApplicationResponse>> getSupplementApplications(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "申请状态") @RequestParam(required = false) String applicationStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        SupplementApplicationQueryRequest queryRequest = SupplementApplicationQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .userId(userId)
                .applicationStatus(applicationStatus)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        log.info("[开放API] 查询补卡申请列表: pageNum={}, pageSize={}, applicationStatus={}",
                pageNum, pageSize, applicationStatus);

        PageResult<SupplementApplicationResponse> result = attendanceOpenApiService.getSupplementApplications(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 审批补卡申请
     */
    @PostMapping("/supplement/applications/{applicationId}/approve")
    @Operation(summary = "审批补卡申请", description = "审批补卡申请")
    public ResponseDTO<Void> approveSupplementApplication(
            @Parameter(description = "申请ID") @PathVariable Long applicationId,
            @Valid @RequestBody ApproveSupplementRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 审批补卡申请: applicationId={}, approveResult={}, clientIp={}",
                applicationId, request.getApproveResult(), clientIp);

        attendanceOpenApiService.approveSupplementApplication(applicationId, request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 获取用户考勤画像
     */
    @GetMapping("/profile/{userId}")
    @Operation(summary = "获取用户考勤画像", description = "获取用户考勤画像分析")
    public ResponseDTO<UserAttendanceProfileResponse> getUserAttendanceProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "统计周期（天）") @RequestParam(defaultValue = "30") Integer days,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询用户考勤画像: userId={}, days={}", userId, days);

        UserAttendanceProfileResponse profile = attendanceOpenApiService.getUserAttendanceProfile(userId, days, token);
        return ResponseDTO.ok(profile);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
