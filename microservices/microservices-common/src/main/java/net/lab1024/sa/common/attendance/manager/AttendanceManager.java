package net.lab1024.sa.common.attendance.manager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;

/**
 * 考勤管理Manager
 * <p>
 * 负责考勤相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 考勤审批后处理逻辑编排
 * - 用户信息获取（通过GatewayServiceClient）
 * - 年假余额扣除（通过用户服务）
 * - 考勤统计更新
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AttendanceManager {

    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     */
    public AttendanceManager(GatewayServiceClient gatewayServiceClient) {
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 获取用户详情
     * <p>
     * 通过GatewayServiceClient调用公共服务获取用户详情
     * 支持缓存（由GatewayServiceClient内部实现）
     * </p>
     *
     * @param userId 用户ID
     * @return 用户详情VO，如果获取失败则返回null
     */
    public UserDetailVO getUserDetail(Long userId) {
        if (userId == null) {
            log.warn("[考勤管理] 用户ID为空，无法获取用户详情");
            return null;
        }

        try {
            ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    UserDetailVO.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.debug("[考勤管理] 获取用户详情成功，userId={}, realName={}", userId, response.getData().getRealName());
                return response.getData();
            } else {
                log.warn("[考勤管理] 获取用户详情失败，userId={}, message={}", userId,
                        response != null ? response.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 获取用户详情异常，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 获取用户姓名
     * <p>
     * 优先使用realName，如果为空则使用username
     * </p>
     *
     * @param userId 用户ID
     * @return 用户姓名，如果获取失败则返回"未知用户"
     */
    public String getUserName(Long userId) {
        UserDetailVO userDetail = getUserDetail(userId);
        if (userDetail != null) {
            String name = userDetail.getRealName();
            if (name == null || name.trim().isEmpty()) {
                name = userDetail.getUsername();
            }
            return name != null && !name.trim().isEmpty() ? name : "未知用户";
        }
        return "未知用户";
    }

    /**
     * 扣除年假余额
     * <p>
     * 通过GatewayServiceClient调用用户服务扣除年假余额
     * 参考钉钉等竞品：年假自动扣除，无需手动操作
     * </p>
     *
     * @param userId 用户ID
     * @param leaveDays 请假天数
     * @return 是否扣除成功
     */
    public boolean deductAnnualLeaveBalance(Long userId, Double leaveDays) {
        if (userId == null || leaveDays == null || leaveDays <= 0) {
            log.warn("[考勤管理] 扣除年假余额参数无效，userId={}, leaveDays={}", userId, leaveDays);
            return false;
        }

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("leaveDays", leaveDays);
            requestParams.put("leaveType", "ANNUAL"); // 年假类型

            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId + "/annual-leave/deduct",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[考勤管理] 扣除年假余额成功，userId={}, leaveDays={}", userId, leaveDays);
                return true;
            } else {
                log.error("[考勤管理] 扣除年假余额失败，userId={}, leaveDays={}, message={}", userId, leaveDays,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 扣除年假余额异常，userId={}, leaveDays={}", userId, leaveDays, e);
            return false;
        }
    }

    /**
     * 更新考勤统计
     * <p>
     * 通过GatewayServiceClient调用考勤服务的统计更新接口
     * 参考钉钉等竞品：自动更新考勤统计，实时反映考勤状态
     * </p>
     *
     * @param userId 用户ID
     * @param date 日期
     * @param attendanceType 考勤类型（LEAVE-请假, OVERTIME-加班, TRAVEL-出差, SUPPLEMENT-补签, SHIFT-调班）
     * @param value 数值（请假天数、加班小时数等）
     * @return 是否更新成功
     */
    public boolean updateAttendanceStatistics(Long userId, LocalDate date, String attendanceType, BigDecimal value) {
        if (userId == null || date == null || attendanceType == null || value == null) {
            log.warn("[考勤管理] 更新考勤统计参数无效，userId={}, date={}, attendanceType={}, value={}",
                    userId, date, attendanceType, value);
            return false;
        }

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("date", date.toString());
            requestParams.put("attendanceType", attendanceType);
            requestParams.put("value", value);

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/statistics/update",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[考勤管理] 更新考勤统计成功，userId={}, date={}, attendanceType={}, value={}",
                        userId, date, attendanceType, value);
                return true;
            } else {
                log.error("[考勤管理] 更新考勤统计失败，userId={}, date={}, attendanceType={}, value={}, message={}",
                        userId, date, attendanceType, value,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 更新考勤统计异常，userId={}, date={}, attendanceType={}, value={}",
                    userId, date, attendanceType, value, e);
            return false;
        }
    }

    /**
     * 处理请假审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：请假审批通过后自动扣除年假余额、更新考勤统计
     * </p>
     *
     * @param userId 用户ID
     * @param leaveType 请假类型（ANNUAL-年假, SICK-病假, PERSONAL-事假等）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param leaveDays 请假天数
     * @return 是否处理成功
     */
    public boolean processLeaveApproval(Long userId, String leaveType, LocalDate startDate, LocalDate endDate, Double leaveDays) {
        log.info("[考勤管理] 处理请假审批通过，userId={}, leaveType={}, startDate={}, endDate={}, leaveDays={}",
                userId, leaveType, startDate, endDate, leaveDays);

        // 1. 如果是年假，扣除年假余额
        if ("ANNUAL".equals(leaveType)) {
            boolean deductSuccess = deductAnnualLeaveBalance(userId, leaveDays);
            if (!deductSuccess) {
                log.warn("[考勤管理] 扣除年假余额失败，但继续处理其他逻辑，userId={}, leaveDays={}", userId, leaveDays);
                // 不阻断流程，继续处理其他逻辑
            }
        }

        // 2. 更新考勤统计（按日期更新）
        LocalDate currentDate = startDate;
        boolean allSuccess = true;
        while (!currentDate.isAfter(endDate)) {
            boolean updateSuccess = updateAttendanceStatistics(
                    userId,
                    currentDate,
                    "LEAVE",
                    BigDecimal.valueOf(leaveDays)
            );
            if (!updateSuccess) {
                allSuccess = false;
                log.warn("[考勤管理] 更新考勤统计失败，userId={}, date={}", userId, currentDate);
            }
            currentDate = currentDate.plusDays(1);
        }

        if (allSuccess) {
            log.info("[考勤管理] 请假审批通过处理完成，userId={}, leaveDays={}", userId, leaveDays);
        } else {
            log.warn("[考勤管理] 请假审批通过处理部分失败，userId={}, leaveDays={}", userId, leaveDays);
        }

        return allSuccess;
    }

    /**
     * 处理调班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：调班审批通过后自动更新排班记录
     * </p>
     *
     * @param userId 用户ID
     * @param shiftDate 调班日期
     * @param originalShiftId 原班次ID
     * @param targetShiftId 目标班次ID
     * @return 是否处理成功
     */
    public boolean processShiftApproval(Long userId, LocalDate shiftDate, Long originalShiftId, Long targetShiftId) {
        log.info("[考勤管理] 处理调班审批通过，userId={}, shiftDate={}, originalShiftId={}, targetShiftId={}",
                userId, shiftDate, originalShiftId, targetShiftId);

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("shiftDate", shiftDate.toString());
            requestParams.put("originalShiftId", originalShiftId);
            requestParams.put("targetShiftId", targetShiftId);

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/schedule/shift",
                    HttpMethod.PUT,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                log.info("[考勤管理] 调班审批通过处理成功，userId={}, shiftDate={}", userId, shiftDate);
                return true;
            } else {
                log.error("[考勤管理] 调班审批通过处理失败，userId={}, shiftDate={}, message={}", userId, shiftDate,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 调班审批通过处理异常，userId={}, shiftDate={}", userId, shiftDate, e);
            return false;
        }
    }

    /**
     * 处理补签审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：补签审批通过后自动创建考勤记录
     * </p>
     *
     * @param userId 用户ID
     * @param supplementDate 补签日期
     * @param supplementTime 补签时间
     * @param supplementType 补签类型（CHECK_IN-补签上班, CHECK_OUT-补签下班）
     * @return 是否处理成功
     */
    public boolean processSupplementApproval(Long userId, LocalDate supplementDate, String supplementTime, String supplementType) {
        log.info("[考勤管理] 处理补签审批通过，userId={}, supplementDate={}, supplementTime={}, supplementType={}",
                userId, supplementDate, supplementTime, supplementType);

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("supplementDate", supplementDate.toString());
            requestParams.put("supplementTime", supplementTime);
            requestParams.put("supplementType", supplementType);

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/record/supplement",
                    HttpMethod.POST,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                // 更新考勤统计
                updateAttendanceStatistics(userId, supplementDate, "SUPPLEMENT", BigDecimal.ONE);
                log.info("[考勤管理] 补签审批通过处理成功，userId={}, supplementDate={}", userId, supplementDate);
                return true;
            } else {
                log.error("[考勤管理] 补签审批通过处理失败，userId={}, supplementDate={}, message={}", userId, supplementDate,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 补签审批通过处理异常，userId={}, supplementDate={}", userId, supplementDate, e);
            return false;
        }
    }

    /**
     * 处理加班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：加班审批通过后自动创建加班记录、计算加班费
     * </p>
     *
     * @param userId 用户ID
     * @param overtimeDate 加班日期
     * @param overtimeHours 加班小时数
     * @return 是否处理成功
     */
    public boolean processOvertimeApproval(Long userId, LocalDate overtimeDate, Double overtimeHours) {
        log.info("[考勤管理] 处理加班审批通过，userId={}, overtimeDate={}, overtimeHours={}",
                userId, overtimeDate, overtimeHours);

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("overtimeDate", overtimeDate.toString());
            requestParams.put("overtimeHours", overtimeHours);

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/overtime/record",
                    HttpMethod.POST,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                // 更新考勤统计
                updateAttendanceStatistics(userId, overtimeDate, "OVERTIME", BigDecimal.valueOf(overtimeHours));
                log.info("[考勤管理] 加班审批通过处理成功，userId={}, overtimeDate={}, overtimeHours={}",
                        userId, overtimeDate, overtimeHours);
                return true;
            } else {
                log.error("[考勤管理] 加班审批通过处理失败，userId={}, overtimeDate={}, message={}", userId, overtimeDate,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 加班审批通过处理异常，userId={}, overtimeDate={}", userId, overtimeDate, e);
            return false;
        }
    }

    /**
     * 处理出差审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：出差审批通过后自动更新考勤状态（出差期间不计入考勤）
     * </p>
     *
     * @param userId 用户ID
     * @param travelStartDate 出差开始日期
     * @param travelEndDate 出差结束日期
     * @return 是否处理成功
     */
    public boolean processTravelApproval(Long userId, LocalDate travelStartDate, LocalDate travelEndDate) {
        log.info("[考勤管理] 处理出差审批通过，userId={}, travelStartDate={}, travelEndDate={}",
                userId, travelStartDate, travelEndDate);

        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("userId", userId);
            requestParams.put("travelStartDate", travelStartDate.toString());
            requestParams.put("travelEndDate", travelEndDate.toString());

            ResponseDTO<Void> response = gatewayServiceClient.callAttendanceService(
                    "/api/v1/attendance/travel/record",
                    HttpMethod.POST,
                    requestParams,
                    Void.class
            );

            if (response != null && response.isSuccess()) {
                // 更新考勤统计（按日期更新）
                LocalDate currentDate = travelStartDate;
                while (!currentDate.isAfter(travelEndDate)) {
                    updateAttendanceStatistics(userId, currentDate, "TRAVEL", BigDecimal.ONE);
                    currentDate = currentDate.plusDays(1);
                }
                log.info("[考勤管理] 出差审批通过处理成功，userId={}, travelStartDate={}, travelEndDate={}",
                        userId, travelStartDate, travelEndDate);
                return true;
            } else {
                log.error("[考勤管理] 出差审批通过处理失败，userId={}, travelStartDate={}, message={}", userId, travelStartDate,
                        response != null ? response.getMessage() : "响应为空");
                return false;
            }
        } catch (Exception e) {
            log.error("[考勤管理] 出差审批通过处理异常，userId={}, travelStartDate={}", userId, travelStartDate, e);
            return false;
        }
    }
}

