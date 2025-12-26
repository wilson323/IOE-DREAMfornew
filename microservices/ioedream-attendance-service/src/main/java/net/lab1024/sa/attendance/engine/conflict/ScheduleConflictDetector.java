package net.lab1024.sa.attendance.engine.conflict;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.engine.model.Chromosome;
import net.lab1024.sa.attendance.engine.model.OptimizationConfig;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排班冲突检测器
 * <p>
 * 检测排班方案中的各类冲突：
 * - 员工相关冲突（连续工作超标、休息天数不足等）
 * - 班次相关冲突（人数不足、重复排班等）
 * - 日期相关冲突（周末加班、节假日加班等）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class ScheduleConflictDetector {

    /**
     * 检测排班方案中的所有冲突
     *
     * @param chromosome 染色体（排班方案）
     * @param config     优化配置
     * @return 冲突列表
     */
    public List<ScheduleConflict> detectConflicts(Chromosome chromosome, OptimizationConfig config) {
        log.info("[冲突检测] 开始检测排班冲突: employees={}, days={}",
                config.getEmployeeCount(), config.getPeriodDays());

        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 1. 检测员工相关冲突
        conflicts.addAll(detectEmployeeConflicts(chromosome, config));

        // 2. 检测班次相关冲突
        conflicts.addAll(detectShiftConflicts(chromosome, config));

        // 3. 检测日期相关冲突
        conflicts.addAll(detectDateConflicts(chromosome, config));

        log.info("[冲突检测] 检测完成: 发现{}个冲突", conflicts.size());

        return conflicts;
    }

    /**
     * 检测员工相关冲突
     */
    public List<ScheduleConflict> detectEmployeeConflicts(Chromosome chromosome, OptimizationConfig config) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 生成日期列表
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = config.getStartDate();
        while (!current.isAfter(config.getEndDate())) {
            dates.add(current);
            current = current.plusDays(1);
        }

        for (Long employeeId : config.getEmployeeIds()) {
            int consecutiveWorkDays = 0;
            int consecutiveRestDays = 0;
            int totalWorkDays = 0;
            boolean lastDayWasWorkDay = false;

            // 遍历每一天
            for (int day = 0; day < dates.size(); day++) {
                LocalDate currentDate = dates.get(day);
                Long shiftId = chromosome.getShift(employeeId, currentDate);
                boolean isWorkDay = shiftId != null && shiftId > 0;

                if (isWorkDay) {
                    // 工作日
                    totalWorkDays++;
                    if (lastDayWasWorkDay) {
                        consecutiveWorkDays++;
                    } else {
                        consecutiveWorkDays = 1;
                    }
                    consecutiveRestDays = 0;
                    lastDayWasWorkDay = true;

                    // 检查连续工作天数是否超标
                    if (consecutiveWorkDays > config.getMaxConsecutiveWorkDays()) {
                        String employeeName = getEmployeeName(employeeId);
                        conflicts.add(ScheduleConflict.consecutiveWorkViolation(
                                employeeId,
                                employeeName,
                                consecutiveWorkDays,
                                config.getMaxConsecutiveWorkDays()
                        ));
                    }
                } else {
                    // 休息日
                    if (!lastDayWasWorkDay) {
                        consecutiveRestDays++;
                    } else {
                        consecutiveRestDays = 1;
                    }
                    consecutiveWorkDays = 0;
                    lastDayWasWorkDay = false;

                    // 检查休息天数是否充足（在休息日结束时检查）
                    if (consecutiveRestDays < config.getMinRestDays() &&
                            (day == dates.size() - 1 ||
                                    chromosome.getShift(employeeId, dates.get(day + 1)) != null &&
                                            chromosome.getShift(employeeId, dates.get(day + 1)) > 0)) {
                        String employeeName = getEmployeeName(employeeId);
                        conflicts.add(ScheduleConflict.restDaysInsufficient(
                                employeeId,
                                employeeName,
                                consecutiveRestDays,
                                config.getMinRestDays()
                        ));
                    }
                }
            }

            // TODO: 检查月工作天数是否超标（需要添加maxMonthlyWorkDays字段到OptimizationConfig）
            // if (totalWorkDays > config.getMaxMonthlyWorkDays()) {
            //     String employeeName = getEmployeeName(employeeId);
            //     conflicts.add(ScheduleConflict.builder()
            //             .conflictType(ScheduleConflict.ConflictType.EMPLOYEE_MONTHLY_WORK_DAYS_EXCEEDED)
            //             .employeeId(employeeId)
            //             .employeeName(employeeName)
            //             .severity(2)
            //             .title(String.format("员工%s月工作天数超标", employeeName))
            //             .description(String.format("员工%s月工作天数%d天，超过最大允许的%d天",
            //                     employeeName, totalWorkDays, config.getMaxMonthlyWorkDays()))
            //             .suggestion("建议减少部分工作日安排")
            //             .suggestedAction(1)
            //             .status(0)
            //             .build());
            // }
        }

        return conflicts;
    }

    /**
     * 检测班次相关冲突
     */
    public List<ScheduleConflict> detectShiftConflicts(Chromosome chromosome, OptimizationConfig config) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 生成日期列表
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = config.getStartDate();
        while (!current.isAfter(config.getEndDate())) {
            dates.add(current);
            current = current.plusDays(1);
        }

        // 1. 检测每日在岗人数是否达标
        for (int day = 0; day < dates.size(); day++) {
            int staffOnDay = chromosome.countStaffOnDay(day);
            LocalDate date = dates.get(day);

            if (staffOnDay < config.getMinDailyStaff()) {
                conflicts.add(ScheduleConflict.shiftCoverageInsufficient(
                        date,
                        null,
                        "所有班次",
                        staffOnDay,
                        config.getMinDailyStaff()
                ));
            }

            // TODO: 检查每日在岗人数是否过多（需要添加maxDailyStaff字段到OptimizationConfig）
            // if (staffOnDay > config.getMaxDailyStaff()) {
            //     conflicts.add(ScheduleConflict.builder()
            //             .conflictType(ScheduleConflict.ConflictType.SHIFT_COVERAGE_EXCESS)
            //             .scheduleDate(date)
            //             .severity(1)
            //             .title("每日在岗人数过多")
            //             .description(String.format("%s实际在岗%d人，超过最多允许的%d人",
            //                     date, staffOnDay, config.getMaxDailyStaff()))
            //             .impact("人员冗余，成本浪费")
            //             .suggestion("建议减少部分人员排班")
            //             .suggestedAction(1)
            //             .status(0)
            //             .build());
            // }
        }

        // 2. 检测重复排班（员工同一天被分配多个班次）
        for (Long employeeId : config.getEmployeeIds()) {
            for (int day = 0; day < dates.size(); day++) {
                LocalDate currentDate = dates.get(day);
                Long shiftId = chromosome.getShift(employeeId, currentDate);

                if (shiftId != null && shiftId > 0) {
                    // 检查是否在同一天有其他班次（这里简化处理，实际可能需要更复杂的逻辑）
                    // 当前数据结构下，一个员工一天只能有一个班次ID
                    // 如果需要支持一天多个班次，需要修改Chromosome的数据结构
                }
            }
        }

        return conflicts;
    }

    /**
     * 检测日期相关冲突
     */
    public List<ScheduleConflict> detectDateConflicts(Chromosome chromosome, OptimizationConfig config) {
        List<ScheduleConflict> conflicts = new ArrayList<>();

        LocalDate startDate = config.getStartDate();

        // 1. 检测周末加班
        for (int day = 0; day < config.getPeriodDays(); day++) {
            LocalDate date = startDate.plusDays(day);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                // 统计当天在岗人数
                int staffOnDay = chromosome.countStaffOnDay(day);

                if (staffOnDay > 0) {
                    conflicts.add(ScheduleConflict.builder()
                            .conflictType(ScheduleConflict.ConflictType.DATE_WEEKEND_OVERTIME)
                            .scheduleDate(date)
                            .severity(1)
                            .title("周末安排工作班次")
                            .description(String.format("%s为周末，有%d人排班工作", date, staffOnDay))
                            .impact("员工周末工作，需要支付加班费")
                            .suggestion("建议评估是否必须周末工作，或调整工作日安排")
                            .suggestedAction(1)
                            .status(0)
                            .build());
                }
            }
        }

        // 2. 检测某些日期是否无人排班
        for (int day = 0; day < config.getPeriodDays(); day++) {
            int staffOnDay = chromosome.countStaffOnDay(day);

            if (staffOnDay == 0) {
                LocalDate date = startDate.plusDays(day);
                conflicts.add(ScheduleConflict.builder()
                        .conflictType(ScheduleConflict.ConflictType.DATE_UNSTAFFED)
                        .scheduleDate(date)
                        .severity(4)
                        .title("日期无人排班")
                        .description(String.format("%s没有任何员工排班", date))
                        .impact("该日期完全无人值守，可能影响运营")
                        .suggestion("必须安排至少" + config.getMinDailyStaff() + "人排班")
                        .suggestedAction(2)
                        .status(0)
                        .build());
            }
        }

        return conflicts;
    }

    /**
     * 统计冲突严重程度分布
     */
    public Map<Integer, Integer> getConflictSeverityDistribution(List<ScheduleConflict> conflicts) {
        Map<Integer, Integer> distribution = new HashMap<>();
        distribution.put(1, 0); // 低
        distribution.put(2, 0); // 中
        distribution.put(3, 0); // 高
        distribution.put(4, 0); // 严重

        for (ScheduleConflict conflict : conflicts) {
            Integer severity = conflict.getSeverity();
            if (severity != null && severity >= 1 && severity <= 4) {
                distribution.put(severity, distribution.get(severity) + 1);
            }
        }

        return distribution;
    }

    /**
     * 统计冲突类型分布
     */
    public Map<ScheduleConflict.ConflictType, Integer> getConflictTypeDistribution(List<ScheduleConflict> conflicts) {
        Map<ScheduleConflict.ConflictType, Integer> distribution = new HashMap<>();

        for (ScheduleConflict conflict : conflicts) {
            ScheduleConflict.ConflictType type = conflict.getConflictType();
            distribution.put(type, distribution.getOrDefault(type, 0) + 1);
        }

        return distribution;
    }

    /**
     * 生成冲突摘要
     */
    public String generateConflictSummary(List<ScheduleConflict> conflicts) {
        if (conflicts.isEmpty()) {
            return "未发现冲突";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("发现%d个冲突:\n", conflicts.size()));

        Map<Integer, Integer> severityDist = getConflictSeverityDistribution(conflicts);
        sb.append(String.format("  - 严重冲突: %d个\n", severityDist.get(4)));
        sb.append(String.format("  - 高危冲突: %d个\n", severityDist.get(3)));
        sb.append(String.format("  - 中等冲突: %d个\n", severityDist.get(2)));
        sb.append(String.format("  - 低危冲突: %d个\n", severityDist.get(1)));

        return sb.toString();
    }

    /**
     * 获取员工姓名（简化实现）
     */
    private String getEmployeeName(Long employeeId) {
        // 实际实现中应该从数据库或缓存中查询
        return "员工" + employeeId;
    }
}
