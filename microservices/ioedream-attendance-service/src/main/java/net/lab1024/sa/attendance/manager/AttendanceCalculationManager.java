package net.lab1024.sa.attendance.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.ScheduleRecordDao;
import net.lab1024.sa.attendance.strategy.IAttendanceRuleStrategy;
import net.lab1024.sa.attendance.entity.AttendanceRecordEntity;
import net.lab1024.sa.common.factory.StrategyFactory;
import net.lab1024.sa.attendance.domain.entity.ScheduleRecordEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 考勤计算管理器
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现排班联动计算：打卡记录 + 排班计划 + 考勤规则 = 考勤结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public class AttendanceCalculationManager {

    private final AttendanceRecordDao attendanceRecordDao;
    private final ScheduleRecordDao scheduleRecordDao;
    private final StrategyFactory<IAttendanceRuleStrategy> strategyFactory;

    /**
     * 构造函数注入依赖
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * Manager类在microservices-common中是纯Java类，不使用Spring注解
     * </p>
     *
     * @param attendanceRecordDao 考勤记录DAO
     * @param scheduleRecordDao 排班记录DAO
     * @param strategyFactory 策略工厂
     */
    public AttendanceCalculationManager(
            AttendanceRecordDao attendanceRecordDao,
            ScheduleRecordDao scheduleRecordDao,
            StrategyFactory<IAttendanceRuleStrategy> strategyFactory) {
        this.attendanceRecordDao = attendanceRecordDao;
        this.scheduleRecordDao = scheduleRecordDao;
        this.strategyFactory = strategyFactory;
    }

    /**
     * 计算考勤结果（排班联动）
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 三要素结合计算：打卡记录 + 排班计划 + 考勤规则 = 考勤结果
     * </p>
     *
     * @param userId 用户ID
     * @param date 考勤日期
     * @return 考勤结果
     */
    public AttendanceResultVO calculateAttendance(Long userId, LocalDate date) {
        log.debug("[考勤计算管理器] 开始计算考勤结果, userId={}, date={}", userId, date);

        // 步骤1：查询排班计划
        ScheduleRecordEntity schedule = getScheduleRecord(userId, date);
        if (schedule == null) {
            log.warn("[考勤计算管理器] 未找到排班计划, userId={}, date={}", userId, date);
            AttendanceResultVO result = new AttendanceResultVO();
            result.setUserId(userId);
            result.setDate(date);
            result.setStatus("ABSENT");
            result.setRemark("未排班");
            return result;
        }

        // 步骤2：查询打卡记录
        List<AttendanceRecordEntity> punchRecords = getPunchRecords(userId, date);
        if (punchRecords.isEmpty()) {
            log.warn("[考勤计算管理器] 未找到打卡记录, userId={}, date={}", userId, date);
            AttendanceResultVO result = new AttendanceResultVO();
            result.setUserId(userId);
            result.setDate(date);
            result.setStatus("ABSENT");
            result.setRemark("未打卡");
            return result;
        }

        // 步骤3：使用策略模式计算考勤结果
        AttendanceRecordEntity firstRecord = punchRecords.get(0);
        List<IAttendanceRuleStrategy> strategies = strategyFactory.getAll(IAttendanceRuleStrategy.class);
        strategies.sort(Comparator.comparingInt(IAttendanceRuleStrategy::getPriority).reversed());

        // 使用第一个策略（优先级最高）计算考勤结果
        if (!strategies.isEmpty()) {
            IAttendanceRuleStrategy strategy = strategies.get(0);
            log.debug("[考勤计算管理器] 使用策略计算考勤: {}", strategy.getRuleName());
            AttendanceResultVO result = strategy.calculate(firstRecord, schedule);

            // 计算实际工作时长
            result.setWorkingMinutes(calculateWorkingMinutes(punchRecords));

            return result;
        }

        // 默认计算
        return calculateDefaultAttendance(userId, date, schedule, punchRecords);
    }

    /**
     * 查询排班记录
     *
     * @param userId 用户ID（与employeeId相同）
     * @param date 日期
     * @return 排班记录
     */
    private ScheduleRecordEntity getScheduleRecord(Long userId, LocalDate date) {
        log.debug("[考勤计算管理器] 查询排班记录, userId={}, date={}", userId, date);
        try {
            // 根据userId（作为employeeId）和date查询排班记录
            List<ScheduleRecordEntity> schedules = scheduleRecordDao.selectByEmployeeIdAndDateRange(
                    userId, date, date);
            if (schedules != null && !schedules.isEmpty()) {
                return schedules.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("[考勤计算管理器] 查询排班记录异常: userId={}, date={}, error={}",
                    userId, date, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询打卡记录
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 打卡记录列表
     */
    private List<AttendanceRecordEntity> getPunchRecords(Long userId, LocalDate date) {
        log.debug("[考勤计算管理器] 查询打卡记录, userId={}, date={}", userId, date);
        try {
            // 根据userId和date查询打卡记录
            return attendanceRecordDao.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecordEntity>()
                            .eq(AttendanceRecordEntity::getUserId, userId)
                            .eq(AttendanceRecordEntity::getAttendanceDate, date)
                            .orderByAsc(AttendanceRecordEntity::getPunchTime)
            );
        } catch (Exception e) {
            log.error("[考勤计算管理器] 查询打卡记录异常: userId={}, date={}, error={}",
                    userId, date, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 计算实际工作时长
     *
     * @param punchRecords 打卡记录列表
     * @return 工作时长（分钟）
     */
    private Long calculateWorkingMinutes(List<AttendanceRecordEntity> punchRecords) {
        if (punchRecords.size() < 2) {
            return 0L;
        }

        // 找到上班和下班打卡记录
        AttendanceRecordEntity clockIn = punchRecords.stream()
                .filter(r -> r.getPunchType() != null && r.getPunchType() == 0)
                .findFirst()
                .orElse(null);

        AttendanceRecordEntity clockOut = punchRecords.stream()
                .filter(r -> r.getPunchType() != null && r.getPunchType() == 1)
                .findFirst()
                .orElse(null);

        if (clockIn == null || clockOut == null) {
            return 0L;
        }

        // 计算工作时长
        Duration duration = Duration.between(clockIn.getPunchTime(), clockOut.getPunchTime());
        return duration.toMinutes();
    }

    /**
     * 默认考勤计算
     *
     * @param userId 用户ID
     * @param date 日期
     * @param schedule 排班记录
     * @param punchRecords 打卡记录
     * @return 考勤结果
     */
    private AttendanceResultVO calculateDefaultAttendance(
            Long userId, LocalDate date, ScheduleRecordEntity schedule, List<AttendanceRecordEntity> punchRecords) {

        AttendanceResultVO result = new AttendanceResultVO();
        result.setUserId(userId);
        result.setDate(date);
        result.setStatus("NORMAL");
        result.setWorkingMinutes(calculateWorkingMinutes(punchRecords));

        log.debug("[考勤计算管理器] 默认考勤计算完成, userId={}, date={}", userId, date);
        return result;
    }
}
