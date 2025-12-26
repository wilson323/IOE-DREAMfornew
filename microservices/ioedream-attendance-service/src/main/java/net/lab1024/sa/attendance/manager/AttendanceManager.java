package net.lab1024.sa.attendance.manager;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.gateway.domain.response.UserInfoResponse;
/**
 * 考勤管理Manager
 * <p>
 * 负责考勤相关的复杂业务逻辑编排 严格遵循CLAUDE.md规范： - Manager类在microservices-common中是纯Java类，不使用Spring注解 - 通过构造函数注入依赖 -
 * 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责： - 考勤审批后处理逻辑编排 - 用户信息获取（通过GatewayServiceClient） - 年假余额扣除（通过用户服务） - 考勤统计更新
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class AttendanceManager {
    private static final String UNKNOWN_USER = "未知用户";
    private static final String RESPONSE_NULL_MSG = "响应为空";

    private final GatewayServiceClient gatewayClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解， 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param gatewayClient
     *            网关服务客户端
     */
    public AttendanceManager (final GatewayServiceClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    /**
     * 获取用户详情
     * <p>
     * 通过GatewayServiceClient调用公共服务获取用户详情 支持缓存（由GatewayServiceClient内部实现）
     * </p>
     *
     * @param userId
     *            用户ID
     * @return 用户详情VO，如果获取失败则返回null
     */
    @SuppressWarnings("null")
    public UserInfoResponse getUserDetail (final Long userId) {
        if (userId == null) {
            if (log.isWarnEnabled ()) {
                log.warn ("[考勤管理] 用户ID为空，无法获取用户详情");
            }
            return null;
        }

        UserInfoResponse result = null;
        try {
            final ResponseDTO<UserInfoResponse> response = gatewayClient.callCommonService ("/api/v1/users/" + userId,
                    HttpMethod.GET, null, new TypeReference<ResponseDTO<UserInfoResponse>>() {});

            if (response != null && response.isSuccess ()) {
                final UserInfoResponse data = response.getData ();
                if (data != null) {
                    if (log.isDebugEnabled ()) {
                        log.debug ("[考勤管理] 获取用户详情成功，userId={}, realName={}", userId, data.getRealName ());
                    }
                    result = data;
                } else {
                    if (log.isWarnEnabled ()) {
                        log.warn ("[考勤管理] 获取用户详情失败，userId={}, message={}", userId, RESPONSE_NULL_MSG);
                    }
                }
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isWarnEnabled ()) {
                    log.warn ("[考勤管理] 获取用户详情失败，userId={}, message={}", userId, message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 获取用户详情异常，userId={}", userId, e);
            }
        }
        return result;
    }

    /**
     * 获取用户姓名
     * <p>
     * 优先使用realName，如果为空则使用username
     * </p>
     *
     * @param userId
     *            用户ID
     * @return 用户姓名，如果获取失败则返回"未知用户"
     */
    public String getUserName (final Long userId) {
        final UserInfoResponse userDetail = getUserDetail (userId);
        if (userDetail == null) {
            return UNKNOWN_USER;
        }

        String name = userDetail.getRealName ();
        if (isBlank (name)) {
            name = userDetail.getUsername ();
        }
        return isBlank (name) ? UNKNOWN_USER : name;
    }

    /**
     * 检查字符串是否为空（空白）
     *
     * @param str
     *            待检查的字符串
     * @return 如果字符串为null或空白则返回true
     */
    private boolean isBlank (final String str) {
        return str == null || str.trim ().isEmpty ();
    }

    /**
     * 扣除年假余额
     * <p>
     * 通过GatewayServiceClient调用用户服务扣除年假余额 参考钉钉等竞品：年假自动扣除，无需手动操作
     * </p>
     *
     * @param userId
     *            用户ID
     * @param leaveDays
     *            请假天数
     * @return 是否扣除成功
     */
    @SuppressWarnings("null")
    public boolean deductAnnualLeaveBalance (final Long userId, final Double leaveDays) {
        if (userId == null || leaveDays == null || leaveDays <= 0) {
            if (log.isWarnEnabled ()) {
                log.warn ("[考勤管理] 扣除年假余额参数无效，userId={}, leaveDays={}", userId, leaveDays);
            }
            return false;
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("leaveDays", leaveDays);
            requestParams.put ("leaveType", "ANNUAL"); // 年假类型

            final ResponseDTO<Void> response = gatewayClient.callCommonService (
                    "/api/v1/users/" + userId + "/annual-leave/deduct", HttpMethod.PUT, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 扣除年假余额成功，userId={}, leaveDays={}", userId, leaveDays);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 扣除年假余额失败，userId={}, leaveDays={}, message={}", userId, leaveDays, message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 扣除年假余额异常，userId={}, leaveDays={}", userId, leaveDays, e);
            }
        }
        return success;
    }

    /**
     * 更新考勤统计
     * <p>
     * 通过GatewayServiceClient调用考勤服务的统计更新接口 参考钉钉等竞品：自动更新考勤统计，实时反映考勤状态
     * </p>
     *
     * @param userId
     *            用户ID
     * @param date
     *            日期
     * @param attendanceType
     *            考勤类型（LEAVE-请假, OVERTIME-加班, TRAVEL-出差, SUPPLEMENT-补签, SHIFT-调班）
     * @param value
     *            数值（请假天数、加班小时数等）
     * @return 是否更新成功
     */
    @SuppressWarnings("null")
    public boolean updateAttendanceStatistics (final Long userId, final LocalDate date, final String attendanceType,
            final BigDecimal value) {
        if (userId == null || date == null || attendanceType == null || value == null) {
            if (log.isWarnEnabled ()) {
                log.warn ("[考勤管理] 更新考勤统计参数无效，userId={}, date={}, attendanceType={}, value={}", userId, date,
                        attendanceType, value);
            }
            return false;
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("date", date.toString ());
            requestParams.put ("attendanceType", attendanceType);
            requestParams.put ("value", value);

            final ResponseDTO<Void> response = gatewayClient.callCommonService (
                    "/attendance/api/v1/attendance/statistics/update", HttpMethod.PUT, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 更新考勤统计成功，userId={}, date={}, attendanceType={}, value={}", userId, date,
                            attendanceType, value);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 更新考勤统计失败，userId={}, date={}, attendanceType={}, value={}, message={}", userId,
                            date, attendanceType, value, message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 更新考勤统计异常，userId={}, date={}, attendanceType={}, value={}", userId, date,
                        attendanceType, value, e);
            }
        }
        return success;
    }

    /**
     * 处理请假审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：请假审批通过后自动扣除年假余额、更新考勤统计
     * </p>
     *
     * @param userId
     *            用户ID
     * @param leaveType
     *            请假类型（ANNUAL-年假, SICK-病假, PERSONAL-事假等）
     * @param startDate
     *            开始日期
     * @param endDate
     *            结束日期
     * @param leaveDays
     *            请假天数
     * @return 是否处理成功
     */
    public boolean processLeaveApproval (final Long userId, final String leaveType, final LocalDate startDate,
            final LocalDate endDate, final Double leaveDays) {
        if (log.isInfoEnabled ()) {
            log.info ("[考勤管理] 处理请假审批通过，userId={}, leaveType={}, startDate={}, endDate={}, leaveDays={}", userId,
                    leaveType, startDate, endDate, leaveDays);
        }

        // 1. 如果是年假，扣除年假余额
        if ("ANNUAL".equals (leaveType)) {
            final boolean deductSuccess = deductAnnualLeaveBalance (userId, leaveDays);
            if (!deductSuccess && log.isWarnEnabled ()) {
                log.warn ("[考勤管理] 扣除年假余额失败，但继续处理其他逻辑，userId={}, leaveDays={}", userId, leaveDays);
            }
            // 不阻断流程，继续处理其他逻辑
        }

        // 2. 更新考勤统计（按日期更新）
        LocalDate currentDate = startDate;
        boolean allSuccess = true;
        while (!currentDate.isAfter (endDate)) {
            final boolean updateSuccess = updateAttendanceStatistics (userId, currentDate, "LEAVE",
                    BigDecimal.valueOf (leaveDays));
            if (!updateSuccess) {
                allSuccess = false;
                if (log.isWarnEnabled ()) {
                    log.warn ("[考勤管理] 更新考勤统计失败，userId={}, date={}", userId, currentDate);
                }
            }
            currentDate = currentDate.plusDays (1);
        }

        if (allSuccess) {
            if (log.isInfoEnabled ()) {
                log.info ("[考勤管理] 请假审批通过处理完成，userId={}, leaveDays={}", userId, leaveDays);
            }
        } else {
            if (log.isWarnEnabled ()) {
                log.warn ("[考勤管理] 请假审批通过处理部分失败，userId={}, leaveDays={}", userId, leaveDays);
            }
        }

        return allSuccess;
    }

    /**
     * 处理调班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：调班审批通过后自动更新排班记录
     * </p>
     *
     * @param userId
     *            用户ID
     * @param shiftDate
     *            调班日期
     * @param originalShiftId
     *            原班次ID
     * @param targetShiftId
     *            目标班次ID
     * @return 是否处理成功
     */
    @SuppressWarnings("null")
    public boolean processShiftApproval (final Long userId, final LocalDate shiftDate, final Long originalShiftId,
            final Long targetShiftId) {
        if (log.isInfoEnabled ()) {
            log.info ("[考勤管理] 处理调班审批通过，userId={}, shiftDate={}, originalShiftId={}, targetShiftId={}", userId,
                    shiftDate, originalShiftId, targetShiftId);
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("shiftDate", shiftDate.toString ());
            requestParams.put ("originalShiftId", originalShiftId);
            requestParams.put ("targetShiftId", targetShiftId);

            ResponseDTO<Void> response = gatewayClient.callCommonService("/attendance/api/v1/attendance/schedule/shift",
                    HttpMethod.PUT, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 调班审批通过处理成功，userId={}, shiftDate={}", userId, shiftDate);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 调班审批通过处理失败，userId={}, shiftDate={}, message={}", userId, shiftDate, message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 调班审批通过处理异常，userId={}, shiftDate={}", userId, shiftDate, e);
            }
        }
        return success;
    }

    /**
     * 处理补签审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：补签审批通过后自动创建考勤记录
     * </p>
     *
     * @param userId
     *            用户ID
     * @param supplementDate
     *            补签日期
     * @param supplementTime
     *            补签时间
     * @param supplementType
     *            补签类型（CHECK_IN-补签上班, CHECK_OUT-补签下班）
     * @return 是否处理成功
     */
    @SuppressWarnings("null")
    public boolean processSupplementApproval (final Long userId, final LocalDate supplementDate,
            final String supplementTime, final String supplementType) {
        if (log.isInfoEnabled ()) {
            log.info ("[考勤管理] 处理补签审批通过，userId={}, supplementDate={}, supplementTime={}, supplementType={}", userId,
                    supplementDate, supplementTime, supplementType);
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("supplementDate", supplementDate.toString ());
            requestParams.put ("supplementTime", supplementTime);
            requestParams.put ("supplementType", supplementType);

            ResponseDTO<Void> response = gatewayClient.callCommonService (
                    "/attendance/api/v1/attendance/record/supplement", HttpMethod.POST, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                // 更新考勤统计
                updateAttendanceStatistics (userId, supplementDate, "SUPPLEMENT", BigDecimal.ONE);
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 补签审批通过处理成功，userId={}, supplementDate={}", userId, supplementDate);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 补签审批通过处理失败，userId={}, supplementDate={}, message={}", userId, supplementDate,
                            message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 补签审批通过处理异常，userId={}, supplementDate={}", userId, supplementDate, e);
            }
        }
        return success;
    }

    /**
     * 处理加班审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：加班审批通过后自动创建加班记录、计算加班费
     * </p>
     *
     * @param userId
     *            用户ID
     * @param overtimeDate
     *            加班日期
     * @param overtimeHours
     *            加班小时数
     * @return 是否处理成功
     */
    @SuppressWarnings("null")
    public boolean processOvertimeApproval (final Long userId, final LocalDate overtimeDate,
            final Double overtimeHours) {
        if (log.isInfoEnabled ()) {
            log.info ("[考勤管理] 处理加班审批通过，userId={}, overtimeDate={}, overtimeHours={}", userId, overtimeDate,
                    overtimeHours);
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("overtimeDate", overtimeDate.toString ());
            requestParams.put ("overtimeHours", overtimeHours);

            ResponseDTO<Void> response = gatewayClient.callCommonService (
                    "/attendance/api/v1/attendance/overtime/record", HttpMethod.POST, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                // 更新考勤统计
                updateAttendanceStatistics (userId, overtimeDate, "OVERTIME", BigDecimal.valueOf (overtimeHours));
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 加班审批通过处理成功，userId={}, overtimeDate={}, overtimeHours={}", userId, overtimeDate,
                            overtimeHours);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 加班审批通过处理失败，userId={}, overtimeDate={}, message={}", userId, overtimeDate,
                            message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 加班审批通过处理异常，userId={}, overtimeDate={}", userId, overtimeDate, e);
            }
        }
        return success;
    }

    /**
     * 处理出差审批通过后的逻辑
     * <p>
     * 参考钉钉等竞品：出差审批通过后自动更新考勤状态（出差期间不计入考勤）
     * </p>
     *
     * @param userId
     *            用户ID
     * @param travelStartDate
     *            出差开始日期
     * @param travelEndDate
     *            出差结束日期
     * @return 是否处理成功
     */
    @SuppressWarnings("null")
    public boolean processTravelApproval (final Long userId, final LocalDate travelStartDate,
            final LocalDate travelEndDate) {
        if (log.isInfoEnabled ()) {
            log.info ("[考勤管理] 处理出差审批通过，userId={}, travelStartDate={}, travelEndDate={}", userId, travelStartDate,
                    travelEndDate);
        }

        boolean success = false;
        try {
            final Map<String, Object> requestParams = new HashMap<> ();
            requestParams.put ("userId", userId);
            requestParams.put ("travelStartDate", travelStartDate.toString ());
            requestParams.put ("travelEndDate", travelEndDate.toString ());

            final ResponseDTO<Void> response = gatewayClient.callCommonService("/attendance/api/v1/attendance/travel/record",
                    HttpMethod.POST, requestParams, new TypeReference<ResponseDTO<Void>>() {});

            if (response != null && response.isSuccess ()) {
                // 更新考勤统计（按日期更新）
                LocalDate currentDate = travelStartDate;
                while (!currentDate.isAfter (travelEndDate)) {
                    updateAttendanceStatistics (userId, currentDate, "TRAVEL", BigDecimal.ONE);
                    currentDate = currentDate.plusDays (1);
                }
                if (log.isInfoEnabled ()) {
                    log.info ("[考勤管理] 出差审批通过处理成功，userId={}, travelStartDate={}, travelEndDate={}", userId,
                            travelStartDate, travelEndDate);
                }
                success = true;
            } else {
                final String message = response != null ? response.getMessage () : RESPONSE_NULL_MSG;
                if (log.isErrorEnabled ()) {
                    log.error ("[考勤管理] 出差审批通过处理失败，userId={}, travelStartDate={}, message={}", userId, travelStartDate,
                            message);
                }
            }
        } catch (RuntimeException e) {
            if (log.isErrorEnabled ()) {
                log.error ("[考勤管理] 出差审批通过处理异常，userId={}, travelStartDate={}", userId, travelStartDate, e);
            }
        }
        return success;
    }
}

