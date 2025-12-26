package net.lab1024.sa.attendance.mobile.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.attendance.mobile.auth.MobileAuthenticationService;
import net.lab1024.sa.attendance.mobile.model.MobileAttendanceRecord;
import net.lab1024.sa.attendance.mobile.model.MobileAttendanceRecordsResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveApplicationResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveCancellationResult;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileLeaveRecordsResult;
import net.lab1024.sa.attendance.util.MobilePaginationHelper;
import net.lab1024.sa.attendance.mobile.model.MobileRecordQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileShiftQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileShiftsResult;
import net.lab1024.sa.attendance.mobile.model.MobileStatisticsQueryParam;
import net.lab1024.sa.attendance.mobile.model.MobileStatisticsResult;
import net.lab1024.sa.attendance.mobile.model.MobileTodayStatusResult;
import net.lab1024.sa.attendance.mobile.model.MobileUserSession;
import net.lab1024.sa.attendance.mobile.model.MobileUsageStatisticsResult;
import net.lab1024.sa.attendance.mobile.model.WorkShiftInfo;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.mobile.clockin.MobileClockInService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端考勤查询服务
 * <p>
 * 负责移动端考勤查询相关的所有功能，包括：
 * - 今日状态查询
 * - 考勤记录查询
 * - 统计数据查询
 * - 请假记录查询
 * - 排班查询
 * - 使用统计查询
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
public class MobileAttendanceQueryService {

    @Resource
    private MobileAuthenticationService authenticationService;

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private MobileClockInService clockInService;

    @Resource
    private MobilePaginationHelper paginationHelper;

    /**
     * 获取今日考勤状态
     * <p>
     * 查询用户今日的考勤状态，包括打卡状态、工作时长、当前排班等
     * </p>
     *
     * @param token 访问令牌
     * @return 今日状态
     */
    public ResponseDTO<MobileTodayStatusResult> getTodayStatus(String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            List<AttendanceRecordEntity> todayRecords = attendanceRecordDao.selectByEmployeeAndDate(employeeId,
                    LocalDate.now());

            // 构造今日状态
            MobileTodayStatusResult status = MobileTodayStatusResult.builder()
                    .employeeId(employeeId)
                    .date(LocalDate.now())
                    .clockInStatus(getClockInStatus(todayRecords))
                    .clockOutStatus(getClockOutStatus(todayRecords))
                    .workHours(clockInService.calculateWorkHours(employeeId))
                    .currentShift(clockInService.getCurrentShift(employeeId))
                    .build();

            return ResponseDTO.ok(status);

        } catch (Exception e) {
            log.error("[移动端获取今日状态] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取今日状态失败，请重试");
        }
    }

    /**
     * 获取考勤记录
     * <p>
     * 分页查询用户的考勤记录
     * </p>
     *
     * @param queryParam 查询参数
     * @param token      访问令牌
     * @return 考勤记录
     */
    public ResponseDTO<MobileAttendanceRecordsResult> getAttendanceRecords(MobileRecordQueryParam queryParam,
            String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            LocalDate startDate = queryParam.getStartDate() != null ? queryParam.getStartDate()
                    : LocalDate.now().minusDays(30);
            LocalDate endDate = queryParam.getEndDate() != null ? queryParam.getEndDate() : LocalDate.now();

            log.info("[移动端获取考勤记录] 开始: employeeId={}, startDate={}, endDate={}, pageNum={}, pageSize={}",
                    employeeId, startDate, endDate, queryParam.getPageNum(), queryParam.getPageSize());

            // 创建分页对象
            Page<AttendanceRecordEntity> page = paginationHelper.createPage(queryParam.getPageNum(),
                    queryParam.getPageSize());

            // 使用MyBatis-Plus分页查询
            Page<AttendanceRecordEntity> pageResult = attendanceRecordDao.selectPage(page,
                    new LambdaQueryWrapper<AttendanceRecordEntity>()
                            .eq(AttendanceRecordEntity::getUserId, employeeId)
                            .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                            .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                            .orderByDesc(AttendanceRecordEntity::getPunchTime));

            // 使用分页辅助工具构建响应结果
            MobilePaginationHelper.MobilePageResult<MobileAttendanceRecord> pageData = paginationHelper
                    .buildPageResult(pageResult, this::convertToMobileRecord);

            // 构造返回结果
            MobileAttendanceRecordsResult result = MobileAttendanceRecordsResult.builder()
                    .employeeId(employeeId)
                    .records(pageData.getRecords())
                    .totalCount(pageData.getTotalCount())
                    .pageNum(pageData.getPageNum())
                    .pageSize(pageData.getPageSize())
                    .hasNext(pageData.getHasNext())
                    .hasPrev(pageData.getHasPrev())
                    .build();

            log.info("[移动端获取考勤记录] 成功: employeeId={}, total={}, pageNum={}, hasNext={}",
                    employeeId, pageData.getTotalCount(), pageData.getPageNum(), pageData.getHasNext());

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤记录] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤记录失败，请重试");
        }
    }

    /**
     * 获取考勤统计
     * <p>
     * 统计用户在指定时间范围内的考勤数据
     * </p>
     *
     * @param queryParam 统计查询参数
     * @param token      访问令牌
     * @return 统计数据
     */
    public ResponseDTO<MobileStatisticsResult> getStatistics(MobileStatisticsQueryParam queryParam, String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            Long employeeId = session.getEmployeeId();
            LocalDate startDate = queryParam.getStartDate() != null ? queryParam.getStartDate()
                    : LocalDate.now().minusDays(30);
            LocalDate endDate = queryParam.getEndDate() != null ? queryParam.getEndDate() : LocalDate.now();

            // 查询考勤记录
            List<AttendanceRecordEntity> records = attendanceRecordDao.selectList(
                    new LambdaQueryWrapper<AttendanceRecordEntity>()
                            .eq(AttendanceRecordEntity::getUserId, employeeId)
                            .ge(AttendanceRecordEntity::getAttendanceDate, startDate)
                            .le(AttendanceRecordEntity::getAttendanceDate, endDate)
                            .orderByDesc(AttendanceRecordEntity::getAttendanceDate));

            // 统计各项数据
            long totalWorkDays = records.stream()
                    .map(AttendanceRecordEntity::getAttendanceDate)
                    .distinct()
                    .count();

            long attendanceDays = records.stream()
                    .filter(r -> r.getAttendanceStatus() != null)
                    .filter(r -> "NORMAL".equals(r.getAttendanceStatus()) || "LATE".equals(r.getAttendanceStatus())
                            || "EARLY_LEAVE".equals(r.getAttendanceStatus()))
                    .map(AttendanceRecordEntity::getAttendanceDate)
                    .distinct()
                    .count();

            long lateDays = records.stream()
                    .filter(r -> "LATE".equals(r.getAttendanceStatus()))
                    .count();

            long earlyLeaveDays = records.stream()
                    .filter(r -> "EARLY_LEAVE".equals(r.getAttendanceStatus()))
                    .count();

            // TODO: 从请假服务获取请假天数
            long leaveDays = 0;

            // 计算总加班时长
            double overtimeHours = records.stream()
                    .filter(r -> r.getWorkDuration() != null)
                    .filter(r -> r.getWorkDuration() > 8.0) // 超过8小时算加班
                    .mapToDouble(r -> r.getWorkDuration() - 8.0)
                    .sum();

            MobileStatisticsResult result = MobileStatisticsResult.builder()
                    .employeeId(employeeId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalWorkDays((int) totalWorkDays)
                    .attendanceDays((int) attendanceDays)
                    .leaveDays((int) leaveDays)
                    .lateDays((int) lateDays)
                    .earlyLeaveDays((int) earlyLeaveDays)
                    .overtimeHours(Math.round(overtimeHours * 100.0) / 100.0)
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取考勤统计] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取考勤统计失败，请重试");
        }
    }

    /**
     * 获取请假记录
     * <p>
     * 查询用户的请假记录
     * </p>
     *
     * @param queryParam 查询参数
     * @param token      访问令牌
     * @return 请假记录
     */
    public ResponseDTO<MobileLeaveRecordsResult> getLeaveRecords(MobileLeaveQueryParam queryParam, String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的请假记录查询逻辑
            // 1. 从请假服务查询请假记录
            // 2. 支持分页查询
            // 3. 支持日期范围筛选

            MobileLeaveRecordsResult result = MobileLeaveRecordsResult.builder()
                    .employeeId(session.getEmployeeId())
                    .records(Collections.emptyList())
                    .totalCount(0)
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取请假记录] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取请假记录失败，请重试");
        }
    }

    /**
     * 获取使用统计
     * <p>
     * 获取用户的使用统计数据
     * </p>
     *
     * @param token 访问令牌
     * @return 使用统计
     */
    public ResponseDTO<MobileUsageStatisticsResult> getUsageStatistics(String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的使用统计逻辑
            // 1. 统计打卡次数
            // 2. 统计请假次数
            // 3. 统计加班时长
            // 4. 统计使用频率

            MobileUsageStatisticsResult result = MobileUsageStatisticsResult.builder()
                    .employeeId(session.getEmployeeId())
                    .statistics(Collections.emptyMap())
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取使用统计] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取使用统计失败，请重试");
        }
    }

    /**
     * 获取排班信息
     * <p>
     * 查询用户的排班信息
     * </p>
     *
     * @param queryParam 查询参数
     * @param token      访问令牌
     * @return 排班信息
     */
    public ResponseDTO<MobileShiftsResult> getShifts(MobileShiftQueryParam queryParam, String token) {
        try {
            MobileUserSession session = authenticationService.getSession(token);
            if (session == null || session.getEmployeeId() == null) {
                return ResponseDTO.error("AUTH_FAILED", "用户身份验证失败");
            }

            // TODO: 实现实际的排班查询逻辑
            // 1. 从排班服务查询排班信息
            // 2. 支持日期范围查询
            // 3. 返回排班列表

            MobileShiftsResult result = MobileShiftsResult.builder()
                    .employeeId(session.getEmployeeId())
                    .shifts(Collections.emptyList())
                    .build();

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[移动端获取排班] 失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取排班失败，请重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取上班打卡状态
     *
     * @param records 考勤记录列表
     * @return 上班打卡状态
     */
    private String getClockInStatus(List<AttendanceRecordEntity> records) {
        boolean hasClockIn = records.stream().anyMatch(r -> r.getPunchType() != null && r.getPunchType() == 0);
        return hasClockIn ? "CLOCKED_IN" : "NOT_CLOCKED_IN";
    }

    /**
     * 获取下班打卡状态
     *
     * @param records 考勤记录列表
     * @return 下班打卡状态
     */
    private String getClockOutStatus(List<AttendanceRecordEntity> records) {
        boolean hasClockOut = records.stream().anyMatch(r -> r.getPunchType() != null && r.getPunchType() == 1);
        return hasClockOut ? "CLOCKED_OUT" : "NOT_CLOCKED_OUT";
    }

    /**
     * 转换为移动端考勤记录
     *
     * @param entity 考勤记录实体
     * @return 移动端考勤记录
     */
    private MobileAttendanceRecord convertToMobileRecord(AttendanceRecordEntity entity) {
        return MobileAttendanceRecord.builder()
                .recordId(entity.getRecordId())
                .attendanceDate(entity.getAttendanceDate())
                .clockInTime(entity.getPunchTime())
                .clockOutTime(null)
                .attendanceStatus(entity.getAttendanceStatus())
                .workHours(entity.getWorkDuration() != null ? entity.getWorkDuration().doubleValue() : null)
                .location(entity.getPunchAddress())
                .build();
    }
}
