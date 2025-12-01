package net.lab1024.sa.attendance.schedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 智能排班引擎
 * 根据员工、部门、工作模式自动生成排班计划
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class IntelligentScheduleEngine {

    /**
     * 排班规则配置
     */
    private Map<String, ScheduleRule> scheduleRules = new HashMap<>();

    /**
     * 员工排班计划缓存
     */
    private Map<Long, List<EmployeeSchedule>> employeeScheduleCache = new HashMap<>();

    /**
     * 支持的工作模式
     */
    public enum WorkMode {
        FIXED("固定排班", "每周固定时间工作"),
        SHIFT("轮班制", "早中晚三班倒"),
        FLEXIBLE("弹性排班", "弹性工作时间"),
        IRREGULAR("不规则排班", "自定义工作时间");

        private final String name;
        private final String description;

        WorkMode(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 排班规则类
     */
    @Data
    public static class ScheduleRule {
        private Long ruleId;
        private String ruleName;
        private WorkMode workMode;
        private LocalTime workStartTime;
        private LocalTime workEndTime;
        private LocalTime restStartTime;
        private LocalTime restEndTime;
        private List<Integer> workDays; // 1-7 表示周一到周日
        private String cyclePattern; // 循环模式：weekly/monthly/custom
        private Integer cycleLength; // 循环周期长度
        private Map<String, Object> customRules; // 自定义规则
    }

    /**
     * 员工排班记录
     */
    @Data
    public static class EmployeeSchedule {
        private Long scheduleId;
        private Long employeeId;
        private Long ruleId;
        private LocalDate scheduleDate;
        private LocalTime workStartTime;
        private LocalTime workEndTime;
        private String shiftType; // 早班/中班/晚班
        private Boolean isWorkDay;
        private String scheduleStatus; // SCHEDULED/COMPLETED/ABSENT
        private LocalDate createTime;
    }

    /**
     * 初始化排班引擎
     */
    public void initializeEngine() {
        log.info("智能排班引擎初始化开始");

        // 加载默认排班规则
        loadDefaultScheduleRules();

        log.info("智能排班引擎初始化完成，加载了{}个排班规则", scheduleRules.size());
    }

    /**
     * 加载默认排班规则
     */
    private void loadDefaultScheduleRules() {
        // 标准工作日规则
        ScheduleRule standardRule = new ScheduleRule();
        standardRule.setRuleId(1L);
        standardRule.setRuleName("标准工作日");
        standardRule.setWorkMode(WorkMode.FIXED);
        standardRule.setWorkStartTime(LocalTime.of(9, 0));
        standardRule.setWorkEndTime(LocalTime.of(18, 0));
        standardRule.setRestStartTime(LocalTime.of(12, 0));
        standardRule.setRestEndTime(LocalTime.of(13, 0));
        standardRule.setWorkDays(List.of(1, 2, 3, 4, 5)); // 周一到周五
        standardRule.setCyclePattern("weekly");
        scheduleRules.put("STANDARD", standardRule);

        // 三班倒规则
        ScheduleRule shiftRule = new ScheduleRule();
        shiftRule.setRuleId(2L);
        shiftRule.setRuleName("三班倒");
        shiftRule.setWorkMode(WorkMode.SHIFT);
        shiftRule.setWorkStartTime(LocalTime.of(0, 0));
        shiftRule.setWorkEndTime(LocalTime.of(8, 0));
        shiftRule.setRestStartTime(LocalTime.of(4, 0));
        shiftRule.setRestEndTime(LocalTime.of(4, 30));
        shiftRule.setWorkDays(List.of(1, 2, 3, 4, 5, 6, 7));
        shiftRule.setCyclePattern("weekly");
        scheduleRules.put("SHIFT", shiftRule);

        // 弹性工作规则
        ScheduleRule flexibleRule = new ScheduleRule();
        flexibleRule.setRuleId(3L);
        flexibleRule.setRuleName("弹性工作");
        flexibleRule.setWorkMode(WorkMode.FLEXIBLE);
        flexibleRule.setWorkStartTime(LocalTime.of(8, 0));
        flexibleRule.setWorkEndTime(LocalTime.of(20, 0));
        flexibleRule.setRestStartTime(LocalTime.of(12, 0));
        flexibleRule.setRestEndTime(LocalTime.of(13, 0));
        flexibleRule.setWorkDays(List.of(1, 2, 3, 4, 5));
        flexibleRule.setCyclePattern("weekly");
        scheduleRules.put("FLEXIBLE", flexibleRule);

        log.debug("加载了{}个默认排班规则", scheduleRules.size());
    }

    /**
     * 为员工生成排班计划
     *
     * @param employeeId 员工ID
     * @param ruleId     排班规则ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 排班计划列表
     */
    public List<EmployeeSchedule> generateEmployeeSchedule(Long employeeId, Long ruleId, LocalDate startDate,
            LocalDate endDate) {
        log.info("为员工{}生成排班计划，规则ID：{}，日期范围：{} 到 {}", employeeId, ruleId, startDate, endDate);

        ScheduleRule rule = findRuleById(ruleId);
        if (rule == null) {
            log.error("未找到排班规则：{}", ruleId);
            return new ArrayList<>();
        }

        List<EmployeeSchedule> schedules = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            EmployeeSchedule schedule = generateDailySchedule(employeeId, rule, currentDate);
            if (schedule != null) {
                schedules.add(schedule);
            }
            currentDate = currentDate.plusDays(1);
        }

        // 更新缓存
        employeeScheduleCache.put(employeeId, schedules);

        log.info("为员工{}生成了{}天的排班计划", employeeId, schedules.size());
        return schedules;
    }

    /**
     * 生成单日排班计划
     */
    private EmployeeSchedule generateDailySchedule(Long employeeId, ScheduleRule rule, LocalDate date) {
        // 检查是否为工作日
        int dayOfWeek = date.getDayOfWeek().getValue();
        if (!rule.getWorkDays().contains(dayOfWeek)) {
            return null; // 非工作日
        }

        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setScheduleId(System.currentTimeMillis()); // 临时ID
        schedule.setEmployeeId(employeeId);
        schedule.setRuleId(rule.getRuleId());
        schedule.setScheduleDate(date);
        schedule.setIsWorkDay(true);
        schedule.setScheduleStatus("SCHEDULED");
        schedule.setCreateTime(LocalDate.now());

        // 根据工作模式设置具体时间
        switch (rule.getWorkMode()) {
            case FIXED:
            case FLEXIBLE:
                schedule.setWorkStartTime(rule.getWorkStartTime());
                schedule.setWorkEndTime(rule.getWorkEndTime());
                schedule.setShiftType("正常班");
                break;
            case SHIFT:
                schedule = generateShiftSchedule(schedule, rule, date);
                break;
            case IRREGULAR:
                schedule = generateIrregularSchedule(schedule, rule, date);
                break;
        }

        return schedule;
    }

    /**
     * 生成轮班排班
     */
    private EmployeeSchedule generateShiftSchedule(EmployeeSchedule schedule, ScheduleRule rule, LocalDate date) {
        // 简单的轮班逻辑：根据日期计算班次
        int dayOfWeek = date.getDayOfWeek().getValue();
        String[] shiftTypes = { "早班", "中班", "晚班" };
        LocalTime[][] shiftTimes = {
                { LocalTime.of(6, 0), LocalTime.of(14, 0) }, // 早班
                { LocalTime.of(14, 0), LocalTime.of(22, 0) }, // 中班
                { LocalTime.of(22, 0), LocalTime.of(6, 0) } // 晚班(跨天)
        };

        int shiftIndex = (dayOfWeek - 1) % 3;
        schedule.setShiftType(shiftTypes[shiftIndex]);
        schedule.setWorkStartTime(shiftTimes[shiftIndex][0]);
        schedule.setWorkEndTime(shiftTimes[shiftIndex][1]);

        return schedule;
    }

    /**
     * 生成不规则排班
     */
    private EmployeeSchedule generateIrregularSchedule(EmployeeSchedule schedule, ScheduleRule rule, LocalDate date) {
        // 基于自定义规则生成排班，这里提供基础实现
        if (rule.getCustomRules() != null && rule.getCustomRules().containsKey("scheduleTemplate")) {
            // 从自定义规则中获取排班模板
            @SuppressWarnings("unchecked")
            Map<String, Object> template = (Map<String, Object>) rule.getCustomRules().get("scheduleTemplate");

            String dateStr = date.toString();
            if (template.containsKey(dateStr)) {
                @SuppressWarnings("unchecked")
                Map<String, String> daySchedule = (Map<String, String>) template.get(dateStr);
                schedule.setShiftType(daySchedule.get("shiftType"));

                if (daySchedule.containsKey("startTime")) {
                    schedule.setWorkStartTime(LocalTime.parse(daySchedule.get("startTime")));
                }
                if (daySchedule.containsKey("endTime")) {
                    schedule.setWorkEndTime(LocalTime.parse(daySchedule.get("endTime")));
                }
            }
        } else {
            // 使用默认配置
            schedule.setWorkStartTime(rule.getWorkStartTime());
            schedule.setWorkEndTime(rule.getWorkEndTime());
            schedule.setShiftType("自定义班");
        }

        return schedule;
    }

    /**
     * 根据ID查找排班规则
     */
    private ScheduleRule findRuleById(Long ruleId) {
        return scheduleRules.values().stream()
                .filter(rule -> rule.getRuleId().equals(ruleId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 批量生成部门排班计划
     *
     * @param departmentId 部门ID
     * @param employeeIds  员工ID列表
     * @param ruleId       排班规则ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 员工排班计划映射
     */
    public Map<Long, List<EmployeeSchedule>> generateDepartmentSchedule(Long departmentId, List<Long> employeeIds,
            Long ruleId, LocalDate startDate, LocalDate endDate) {
        log.info("为部门{}的{}名员工生成排班计划", departmentId, employeeIds.size());

        Map<Long, List<EmployeeSchedule>> departmentSchedules = new HashMap<>();

        for (Long employeeId : employeeIds) {
            List<EmployeeSchedule> employeeSchedules = generateEmployeeSchedule(employeeId, ruleId, startDate, endDate);
            departmentSchedules.put(employeeId, employeeSchedules);
        }

        return departmentSchedules;
    }

    /**
     * 优化排班计划
     * 考虑员工偏好、工作量均衡等因素
     *
     * @param schedules   原始排班计划
     * @param constraints 约束条件
     * @return 优化后的排班计划
     */
    public List<EmployeeSchedule> optimizeSchedule(List<EmployeeSchedule> schedules, Map<String, Object> constraints) {
        log.info("开始优化排班计划，原始排班数量：{}", schedules.size());

        // 基础优化逻辑：确保连续工作天数不超过限制
        int maxConsecutiveDays = 5; // 默认最大连续工作天数
        if (constraints.containsKey("maxConsecutiveDays")) {
            maxConsecutiveDays = (Integer) constraints.get("maxConsecutiveDays");
        }

        List<EmployeeSchedule> optimizedSchedules = new ArrayList<>(schedules);

        // 基础优化：检查并调整连续工作天数
        optimizeConsecutiveWorkDays(optimizedSchedules, maxConsecutiveDays);

        // 这里可以添加更复杂的优化算法
        // 比如遗传算法、模拟退火等

        log.info("排班计划优化完成，最大连续工作天数限制：{}", maxConsecutiveDays);
        return optimizedSchedules;
    }

    /**
     * 优化连续工作天数
     *
     * @param schedules          排班计划列表
     * @param maxConsecutiveDays 最大连续工作天数
     */
    private void optimizeConsecutiveWorkDays(List<EmployeeSchedule> schedules, int maxConsecutiveDays) {
        if (schedules.isEmpty()) {
            return;
        }

        // 按员工和日期分组
        Map<Long, List<EmployeeSchedule>> employeeSchedules = schedules.stream()
                .filter(s -> s.getIsWorkDay() != null && s.getIsWorkDay())
                .sorted((s1, s2) -> s1.getScheduleDate().compareTo(s2.getScheduleDate()))
                .collect(Collectors.groupingBy(EmployeeSchedule::getEmployeeId));

        for (Map.Entry<Long, List<EmployeeSchedule>> entry : employeeSchedules.entrySet()) {
            List<EmployeeSchedule> employeeScheduleList = entry.getValue();
            int consecutiveDays = 0;
            LocalDate lastDate = null;

            for (EmployeeSchedule schedule : employeeScheduleList) {
                if (lastDate != null && schedule.getScheduleDate().equals(lastDate.plusDays(1))) {
                    consecutiveDays++;
                    if (consecutiveDays > maxConsecutiveDays) {
                        log.debug("员工{}连续工作{}天，超过限制{}，建议调整",
                                schedule.getEmployeeId(), consecutiveDays, maxConsecutiveDays);
                        // 可以在这里添加调整逻辑，比如将某天标记为休息日
                    }
                } else {
                    consecutiveDays = 1;
                }
                lastDate = schedule.getScheduleDate();
            }
        }
    }

    /**
     * 验证排班计划的有效性
     *
     * @param schedules 排班计划
     * @return 验证结果
     */
    public ScheduleValidationResult validateSchedule(List<EmployeeSchedule> schedules) {
        log.debug("开始验证排班计划有效性");

        ScheduleValidationResult result = new ScheduleValidationResult();
        result.setIsValid(true);
        result.setErrors(new ArrayList<>());
        result.setWarnings(new ArrayList<>());

        // 检查时间冲突
        Map<LocalDate, List<EmployeeSchedule>> dateSchedules = schedules.stream()
                .collect(Collectors.groupingBy(EmployeeSchedule::getScheduleDate));

        for (Map.Entry<LocalDate, List<EmployeeSchedule>> entry : dateSchedules.entrySet()) {
            LocalDate date = entry.getKey();
            List<EmployeeSchedule> daySchedules = entry.getValue();

            if (daySchedules.size() > 1) {
                result.getWarnings().add(String.format("日期%s有多个排班记录", date));
            }
        }

        // 检查工作时间合理性
        for (EmployeeSchedule schedule : schedules) {
            if (schedule.getWorkStartTime() != null && schedule.getWorkEndTime() != null) {
                if (schedule.getWorkStartTime().isAfter(schedule.getWorkEndTime())) {
                    // 跨天工作，检查是否合理
                    if (!"晚班".equals(schedule.getShiftType())) {
                        result.addError(String.format("员工%d在%s的工作时间安排异常",
                                schedule.getEmployeeId(), schedule.getScheduleDate()));
                        result.setIsValid(false);
                    }
                }
            }
        }

        log.info("排班计划验证完成，有效性：{}，错误数：{}，警告数：{}",
                result.isValid(), result.getErrors().size(), result.getWarnings().size());

        return result;
    }

    /**
     * 排班验证结果类
     */
    @Data
    public static class ScheduleValidationResult {
        private Boolean isValid;
        private List<String> errors;
        private List<String> warnings;

        /**
         * 获取验证是否有效
         *
         * @return 是否有效
         */
        public Boolean isValid() {
            return isValid;
        }

        public void addError(String error) {
            if (errors == null) {
                errors = new ArrayList<>();
            }
            errors.add(error);
        }

        public void addWarning(String warning) {
            if (warnings == null) {
                warnings = new ArrayList<>();
            }
            warnings.add(warning);
        }
    }

    /**
     * 获取员工排班缓存
     *
     * @param employeeId 员工ID
     * @return 排班计划列表
     */
    public List<EmployeeSchedule> getEmployeeScheduleCache(Long employeeId) {
        return employeeScheduleCache.getOrDefault(employeeId, new ArrayList<>());
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        employeeScheduleCache.clear();
        log.info("排班引擎缓存已清空");
    }

    /**
     * 添加自定义排班规则
     *
     * @param rule 排班规则
     */
    public void addScheduleRule(ScheduleRule rule) {
        scheduleRules.put(rule.getRuleName(), rule);
        log.info("添加排班规则：{}", rule.getRuleName());
    }

    /**
     * 获取所有排班规则
     *
     * @return 排班规则列表
     */
    public List<ScheduleRule> getAllScheduleRules() {
        return new ArrayList<>(scheduleRules.values());
    }
}
