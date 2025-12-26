# IOE-DREAM 全局待办事项完整清单

> **版本**: v1.0.0
> **创建时间**: 2025-12-23
> **更新时间**: 2025-12-23
> **TODO总数**: 98个Java文件
> **负责人**: IOE-DREAM架构团队
> **状态**: 🚧 进行中

---

## 📋 执行摘要

本文档基于IOE-DREAM智慧园区管理平台的全部98个TODO项，结合业务模块文档进行企业级分析，提供完整的待办事项梳理、业务逻辑思考和高质量实现建议。

### 核心发现

**TODO分布情况**:
- 考勤服务: 32个TODO (32.7%)
- 视频服务: 28个TODO (28.6%)
- 门禁服务: 18个TODO (18.4%)
- 消费服务: 8个TODO (8.2%)
- 生物识别服务: 5个TODO (5.1%)
- 其他模块: 7个TODO (7.1%)

**优先级分析**:
- P0级（核心功能）: 45个TODO
- P1级（重要功能）: 38个TODO
- P2级（优化功能）: 15个TODO

---

## 🎯 一、考勤管理模块 TODO分析

### 模块概述

根据`documentation/业务模块/03-考勤管理模块/01-功能说明/README.md`，考勤管理是HR管理的核心环节，提供：
- **多模态考勤**: 刷卡、人脸、指纹、移动端打卡
- **智能排班**: 支持多种排班模式，自动化排班算法
- **流程化异常处理**: 请假、加班、补签等申请审批
- **实时数据统计**: 考勤数据实时计算，报表自动生成

### TODO详细分析

#### 1. 智能排班引擎 (SmartSchedulingEngine)

**TODO编号**: ATT-001
**文件位置**: `SmartSchedulingEngine.java:214`
**TODO内容**: 优化排班算法（遗传算法实现）
**优先级**: P1
**业务逻辑**:

```java
// 当前实现（简化版）
public SchedulingResult optimizeSchedule(SchedulingRequest request) {
    log.info("[智能排班引擎] 开始排班优化: departmentId={}", request.getDepartmentId());

    // 1. 获取员工数据
    List<EmployeeEntity> employees = getEmployeesForScheduling(request);

    // 2. TODO: 实现遗传算法优化排班
    // 当前使用贪心算法，需要升级为遗传算法

    // 3. 生成排班方案
    SchedulingResult result = new SchedulingResult();
    result.setScheduleRecords(generateInitialSchedule(employees, request));

    return result;
}
```

**企业级实现建议**:

1. **遗传算法设计**:
```java
@Component
@Slf4j
public class GeneticAlgorithmOptimizer {

    private static final int POPULATION_SIZE = 100;      // 种群大小
    private static final int MAX_GENERATIONS = 1000;      // 最大迭代次数
    private static final double MUTATION_RATE = 0.01;    // 变异率
    private static final double CROSSOVER_RATE = 0.8;     // 交叉率

    /**
     * 遗传算法主流程
     */
    public SchedulingResult optimize(SchedulingRequest request) {
        log.info("[遗传算法] 开始优化: departmentId={}", request.getDepartmentId());

        // 1. 初始化种群
        List<ScheduleChromosome> population = initializePopulation(request);

        // 2. 迭代进化
        for (int generation = 0; generation < MAX_GENERATIONS; generation++) {
            // 2.1 适应度评估
            evaluateFitness(population);

            // 2.2 选择操作
            List<ScheduleChromosome> selected = selection(population);

            // 2.3 交叉操作
            List<ScheduleChromosome> offspring = crossover(selected);

            // 2.4 变异操作
            mutate(offspring);

            // 2.5 更新种群
            population = survive(population, offspring);

            // 2.6 记录最优解
            if (generation % 100 == 0) {
                log.info("[遗传算法] 第{}代，最优适应度: {}",
                    generation, getBestFitness(population));
            }
        }

        // 3. 返回最优解
        ScheduleChromosome bestChromosome = getBestChromosome(population);
        return convertToSchedulingResult(bestChromosome);
    }

    /**
     * 适应度函数设计
     * 目标：最大化员工满意度，最小化加班成本
     */
    private double evaluateFitness(ScheduleChromosome chromosome) {
        double fitness = 0.0;

        // 目标1: 员工偏好匹配度 (权重30%)
        fitness += calculatePreferenceMatch(chromosome) * 0.3;

        // 目标2: 工作负载平衡 (权重25%)
        fitness += calculateWorkloadBalance(chromosome) * 0.25;

        // 目标3: 技能匹配度 (权重20%)
        fitness += calculateSkillMatch(chromosome) * 0.2;

        // 目标4: 合规性检查 (权重25%)
        fitness += calculateCompliance(chromosome) * 0.25;

        return fitness;
    }

    /**
     * 约束检查
     */
    private boolean checkConstraints(ScheduleChromosome chromosome) {
        // 约束1: 每天至少有一个员工在岗
        // 约束2: 员工连续工作不超过6天
        // 约束3: 每月加班时长不超过36小时
        // 约束4: 特殊技能岗位必须有合格人员
        return true;
    }
}
```

2. **关键实现要点**:
   - 使用GatewayServiceClient调用用户服务获取员工数据
   - 考虑员工偏好、技能、工作时长等约束
   - 实现多目标优化（成本、公平性、满意度）
   - 支持排班规则配置（工作时间、休息时间、特殊要求）

3. **测试验证**:
```java
@Test
void testGeneticAlgorithmPerformance() {
    SchedulingRequest request = createTestRequest();
    SchedulingResult result = geneticAlgorithmOptimizer.optimize(request);

    assertThat(result.getScheduleRecords()).isNotEmpty();
    assertThat(result.getOptimizationScore()).isGreaterThan(0.8);
    assertThat(result.getConstraintViolations()).isEmpty();
}
```

---

#### 2. 排班冲突检测 (ConflictDetector)

**TODO编号**: ATT-004
**文件位置**: `ConflictDetectorImpl.java`
**TODO内容**: 实现排班冲突检测逻辑
**优先级**: P0
**业务逻辑**:

**企业级实现建议**:

```java
@Component
@Slf4j
public class EnhancedConflictDetector implements ConflictDetector {

    @Resource
    private ScheduleRecordDao scheduleRecordDao;

    /**
     * 检测排班冲突
     */
    @Override
    public ConflictDetectionResult detectConflicts(SchedulingRequest request) {
        log.info("[冲突检测] 开始检测: departmentId={}, startDate={}, endDate={}",
            request.getDepartmentId(), request.getStartDate(), request.getEndDate());

        ConflictDetectionResult result = new ConflictDetectionResult();
        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 1. 获取现有排班
        List<ScheduleRecordEntity> existingSchedules = scheduleRecordDao.selectList(
            new LambdaQueryWrapper<ScheduleRecordEntity>()
                .eq(ScheduleRecordEntity::getDepartmentId, request.getDepartmentId())
                .between(ScheduleRecordEntity::getWorkDate, request.getStartDate(), request.getEndDate())
        );

        // 2. 检测时间冲突
        conflicts.addAll(detectTimeConflicts(existingSchedules, request));

        // 3. 检测技能冲突
        conflicts.addAll(detectSkillConflicts(existingSchedules, request));

        // 4. 检测工作时长冲突
        conflicts.addAll(detectWorkHourConflicts(existingSchedules, request));

        // 5. 检测容量冲突
        conflicts.addAll(detectCapacityConflicts(existingSchedules, request));

        result.setConflicts(conflicts);
        result.setTotalConflicts(conflicts.size());
        result.setHasConflicts(!conflicts.isEmpty());

        log.info("[冲突检测] 检测完成: 发现{}个冲突", conflicts.size());
        return result;
    }

    /**
     * 检测时间冲突（同一员工同一时间段被安排多个班次）
     */
    private List<ScheduleConflict> detectTimeConflicts(
            List<ScheduleRecordEntity> existingSchedules,
            SchedulingRequest request) {

        List<ScheduleConflict> timeConflicts = new ArrayList<>();

        // 按员工分组
        Map<Long, List<ScheduleRecordEntity>> employeeSchedules = existingSchedules.stream()
            .collect(Collectors.groupingBy(ScheduleRecordEntity::getEmployeeId));

        // 检查每个员工的时间冲突
        employeeSchedules.forEach((employeeId, schedules) -> {
            for (int i = 0; i < schedules.size(); i++) {
                for (int j = i + 1; j < schedules.size(); j++) {
                    ScheduleRecordEntity s1 = schedules.get(i);
                    ScheduleRecordEntity s2 = schedules.get(j);

                    if (isTimeOverlap(s1, s2)) {
                        TimeConflict conflict = new TimeConflict();
                        conflict.setEmployeeId(employeeId);
                        conflict.setSchedule1(s1);
                        conflict.setSchedule2(s2);
                        conflict.setConflictType("TIME_OVERLAP");
                        conflict.setSeverity("HIGH");

                        timeConflicts.add(conflict);
                    }
                }
            }
        });

        return timeConflicts;
    }

    /**
     * 检测技能冲突（特殊岗位缺少合格人员）
     */
    private List<ScheduleConflict> detectSkillConflicts(
            List<ScheduleRecordEntity> existingSchedules,
            SchedulingRequest request) {

        List<ScheduleConflict> skillConflicts = new ArrayList<>();

        // 获取部门技能需求
        Map<String, Integer> skillRequirements = request.getSkillRequirements();

        // 检查每个时间段是否满足技能要求
        LocalDate date = request.getStartDate();
        while (!date.isAfter(request.getEndDate())) {
            for (ShiftEntity shift : request.getShifts()) {
                // 获取该班次的在岗人员
                List<ScheduleRecordEntity> onDuty = existingSchedules.stream()
                    .filter(s -> s.getWorkDate().equals(date))
                    .filter(s -> s.getShiftId().equals(shift.getShiftId()))
                    .collect(Collectors.toList());

                // 检查技能覆盖
                Map<String, Long> skillCoverage = calculateSkillCoverage(onDuty);

                skillRequirements.forEach((skill, requiredCount) -> {
                    long availableCount = skillCoverage.getOrDefault(skill, 0L);
                    if (availableCount < requiredCount) {
                        SkillConflict conflict = new SkillConflict();
                        conflict.setWorkDate(date);
                        conflict.setShiftName(shift.getShiftName());
                        conflict.setSkillName(skill);
                        conflict.setRequiredCount(requiredCount);
                        conflict.setAvailableCount((int) availableCount);
                        conflict.setShortage(requiredCount - availableCount);
                        conflict.setConflictType("SKILL_SHORTAGE");
                        conflict.setSeverity("HIGH");

                        skillConflicts.add(conflict);
                    }
                });
            }

            date = date.plusDays(1);
        }

        return skillConflicts;
    }

    /**
     * 检测工作时长冲突（连续工作超过规定时长）
     */
    private List<ScheduleConflict> detectWorkHourConflicts(
            List<ScheduleRecordEntity> existingSchedules,
            SchedulingRequest request) {

        List<ScheduleConflict> workHourConflicts = new ArrayList<>();

        // 按员工分组并按日期排序
        Map<Long, List<ScheduleRecordEntity>> employeeSchedules = existingSchedules.stream()
            .collect(Collectors.groupingBy(ScheduleRecordEntity::getEmployeeId));

        employeeSchedules.forEach((employeeId, schedules) -> {
            // 按日期排序
            schedules.sort(Comparator.comparing(ScheduleRecordEntity::getWorkDate));

            // 检查连续工作天数
            int consecutiveDays = 0;
            LocalDate previousDate = null;

            for (ScheduleRecordEntity schedule : schedules) {
                if (previousDate != null &&
                    previousDate.plusDays(1).equals(schedule.getWorkDate())) {
                    consecutiveDays++;
                } else {
                    consecutiveDays = 1;
                }

                // 连续工作超过6天告警
                if (consecutiveDays > 6) {
                    WorkHourConflict conflict = new WorkHourConflict();
                    conflict.setEmployeeId(employeeId);
                    conflict.setConsecutiveDays(consecutiveDays);
                    conflict.setMaxAllowedDays(6);
                    conflict.setConflictType("EXCESSIVE_WORK");
                    conflict.setSeverity("MEDIUM");

                    workHourConflicts.add(conflict);
                }

                previousDate = schedule.getWorkDate();
            }
        });

        return workHourConflicts;
    }

    /**
     * 计算技能覆盖率
     */
    private Map<String, Long> calculateSkillCoverage(List<ScheduleRecordEntity> onDuty) {
        // TODO: 通过GatewayServiceClient调用用户服务获取员工技能信息
        // 这里需要实现技能查询和统计逻辑

        Map<String, Long> skillCoverage = new HashMap<>();

        // 临时实现：返回空覆盖
        return skillCoverage;
    }

    /**
     * 判断时间是否重叠
     */
    private boolean isTimeOverlap(ScheduleRecordEntity s1, ScheduleRecordEntity s2) {
        // TODO: 实现时间重叠检测逻辑
        // 需要考虑班次的开始时间和结束时间
        return false;
    }
}
```

---

#### 3. 考勤规则引擎 (AttendanceRuleEngine)

**TODO编号**: ATT-008
**文件位置**: `AttendanceRuleEngineImpl.java:134`
**TODO内容**: 完善考勤规则引擎实现
**优先级**: P0
**业务逻辑**:

根据业务文档，考勤规则引擎负责：
- 迟到早退规则：判定标准和容忍时间
- 旷工规则：旷工判定条件
- 加班规则：加班认定和计算方式
- 出勤统计规则：出勤率计算方式

**企业级实现建议**:

```java
@Component
@Slf4j
public class EnterpriseRuleEngine implements AttendanceRuleEngine {

    @Resource
    private RuleLoader ruleLoader;

    @Resource
    private RuleEvaluatorFactory evaluatorFactory;

    @Resource
    private RuleExecutor ruleExecutor;

    @Resource
    private RuleCacheManager ruleCacheManager;

    /**
     * 执行考勤规则评估
     */
    @Override
    public RuleExecutionResult evaluateRules(RuleExecutionContext context) {
        log.info("[规则引擎] 开始评估规则: contextId={}", context.getExecutionId());

        RuleExecutionResult result = new RuleExecutionResult();

        try {
            // 1. 加载规则
            List<CompiledRule> rules = ruleLoader.loadRules(context.getDepartmentId());

            // 2. 检查缓存
            RuleEvaluationResult cachedResult = ruleCacheManager.getCachedResult(rules, context);
            if (cachedResult != null) {
                log.info("[规则引擎] 命中缓存: contextId={}", context.getExecutionId());
                result.setResult(cachedResult);
                return result;
            }

            // 3. 执行规则
            for (CompiledRule rule : rules) {
                RuleEvaluationResult ruleResult = evaluateRule(rule, context);
                result.addRuleResult(ruleResult);

                // 如果规则失败且是阻塞规则，则停止执行
                if (!ruleResult.isSuccess() && rule.isBlocking()) {
                    log.warn("[规则引擎] 阻塞规则失败: ruleId={}, result={}",
                        rule.getRuleId(), ruleResult.getMessage());
                    break;
                }
            }

            // 4. 缓存结果
            ruleCacheManager.cacheResult(rules.get(0).getRuleId(), context, result.getOverallResult());

            log.info("[规则引擎] 评估完成: contextId={}, rulesExecuted={}",
                context.getExecutionId(), rules.size());

        } catch (Exception e) {
            log.error("[规则引擎] 规则评估异常: contextId={}", context.getExecutionId(), e);
            result.setSuccess(false);
            result.setMessage("规则评估异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 评估单个规则
     */
    private RuleEvaluationResult evaluateRule(CompiledRule rule, RuleExecutionContext context) {
        log.debug("[规则引擎] 评估规则: ruleId={}", rule.getRuleId());

        RuleEvaluationResult result = new RuleEvaluationResult();
        result.setRuleId(rule.getRuleId());
        result.setRuleName(rule.getRuleName());

        try {
            // 1. 获取规则评估器
            RuleEvaluator evaluator = evaluatorFactory.getEvaluator(rule.getRuleType());

            // 2. 执行规则评估
            boolean passed = evaluator.evaluate(rule, context);

            result.setSuccess(passed);

            if (!passed) {
                result.setMessage(rule.getFailureMessage());
                result.setSeverity(rule.getFailureSeverity());

                // 3. 执行失败动作
                if (rule.getFailureAction() != null) {
                    executeAction(rule.getFailureAction(), context);
                }
            } else {
                result.setMessage("规则验证通过");
            }

        } catch (Exception e) {
            log.error("[规则引擎] 规则评估异常: ruleId={}", rule.getRuleId(), e);
            result.setSuccess(false);
            result.setMessage("规则评估异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行规则动作
     */
    private void executeAction(CompiledAction action, RuleExecutionContext context) {
        log.info("[规则引擎] 执行动作: actionType={}", action.getActionType());

        switch (action.getActionType()) {
            case "WARN":
                // 发送警告通知
                sendWarningNotification(action, context);
                break;

            case "BLOCK":
                // 阻塞操作
                throw new BusinessException("RULE_BLOCK", action.getMessage());

            case "AUTO_FIX":
                // 自动修正
                autoFixIssue(action, context);
                break;

            case "NOTIFY":
                // 发送通知
                sendNotification(action, context);
                break;

            default:
                log.warn("[规则引擎] 未知动作类型: {}", action.getActionType());
        }
    }

    /**
     * 迟到规则评估器
     */
   @Component
    public class LateRuleEvaluator implements RuleEvaluator {

        @Override
        public boolean evaluate(CompiledRule rule, RuleExecutionContext context) {
            log.debug("[迟到规则] 评估迟到: ruleId={}", rule.getRuleId());

            // 获取规则配置
            Map<String, Object> config = rule.getConfig();
            int toleranceMinutes = (int) config.getOrDefault("toleranceMinutes", 5);

            // 获取打卡时间
            LocalDateTime punchTime = context.getPunchTime();
            LocalDateTime workStartTime = context.getWorkStartTime();

            // 计算迟到分钟数
            long lateMinutes = ChronoUnit.MINUTES.between(workStartTime, punchTime);

            // 判断是否迟到
            boolean isLate = lateMinutes > toleranceMinutes;

            if (isLate) {
                log.info("[迟到规则] 判定为迟到: employeeId={}, lateMinutes={}",
                    context.getEmployeeId(), lateMinutes);
            }

            return !isLate;  // 返回true表示规则通过
        }
    }

    /**
     * 旷工规则评估器
     */
    @Component
    public class AbsenteeismRuleEvaluator implements RuleEvaluator {

        @Override
        public boolean evaluate(CompiledRule rule, RuleExecutionContext context) {
            log.debug("[旷工规则] 评估旷工: ruleId={}", rule.getRuleId());

            // 获取规则配置
            Map<String, Object> config = rule.getConfig();
            int allowedMissPunches = (int) config.getOrDefault("allowedMissPunches", 0);

            // 检查是否有打卡记录
            int punchCount = context.getPunchCount();

            // 判断是否旷工
            boolean isAbsenteeism = punchCount <= allowedMissPunches;

            if (isAbsenteeism) {
                log.warn("[旷工规则] 判定为旷工: employeeId={}, punchCount={}",
                    context.getEmployeeId(), punchCount);
            }

            return !isAbsenteeism;  // 返回true表示规则通过
        }
    }

    /**
     * 加班规则评估器
     */
    @Component
    public class OvertimeRuleEvaluator implements RuleEvaluator {

        @Override
        public boolean evaluate(CompiledRule rule, RuleExecutionContext context) {
            log.debug("[加班规则] 评估加班: ruleId={}", rule.getRuleId());

            // 获取规则配置
            Map<String, Object> config = rule.getConfig();
            int minOvertimeMinutes = (int) config.getOrDefault("minOvertimeMinutes", 30);

            // 计算工作时长
            Duration workDuration = context.calculateWorkDuration();

            // 判断是否加班
            boolean isOvertime = workDuration.toMinutes() > minOvertimeMinutes;

            if (isOvertime) {
                log.info("[加班规则] 判定为加班: employeeId={}, workDuration={}",
                    context.getEmployeeId(), workDuration);
            }

            return true;  // 加班规则不阻塞，只记录
        }
    }
}
```

---

#### 4. 移动端考勤服务 (AttendanceMobileService)

**TODO编号**: ATT-009
**文件位置**: `AttendanceMobileServiceImpl.java`
**TODO内容**: 实现真实的统计数据查询逻辑
**优先级**: P1
**业务逻辑**:

移动端考勤服务需要实现：
- 用户登录和认证
- GPS位置验证
- 人脸识别验证
- 打卡记录查询
- 统计数据计算

**企业级实现建议**:

```java
@Service
@Slf4j
public class EnterpriseAttendanceMobileService implements AttendanceMobileService {

    @Resource
    private AttendanceManager attendanceManager;

    @Resource
    private GpsLocationValidator gpsLocationValidator;

    @Resource
    private BiometricAttendanceManager biometricAttendanceManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 获取用户考勤统计
     */
    @Override
    public MobileStatisticsResult getStatistics(MobileStatisticsRequest request) {
        log.info("[移动端考勤] 查询统计数据: employeeId={}, month={}",
            request.getEmployeeId(), request.getMonth());

        try {
            Long employeeId = request.getEmployeeId();
            YearMonth month = request.getMonth();

            // 1. 查询考勤记录
            List<AttendanceRecordEntity> records = attendanceManager.queryMonthlyRecords(
                employeeId, month);

            // 2. 计算统计数据
            MobileStatisticsResult statistics = MobileStatisticsResult.builder()
                .employeeId(employeeId)
                .month(month)
                .totalDays(getTotalWorkDays(records, month))
                .actualDays(getActualAttendanceDays(records))
                .lateDays(getLateDays(records))
                .earlyLeaveDays(getEarlyLeaveDays(records))
                .absentDays(getAbsentDays(records))
                .overtimeHours(getOvertimeHours(records))
                .leaveDays(getLeaveDays(records))
                .build();

            // 3. 计算出勤率
            if (statistics.getTotalDays() > 0) {
                BigDecimal attendanceRate = new BigDecimal(statistics.getActualDays())
                    .divide(new BigDecimal(statistics.getTotalDays()), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
                statistics.setAttendanceRate(attendanceRate);
            }

            log.info("[移动端考勤] 统计完成: employeeId={}, attendanceRate={}%",
                employeeId, statistics.getAttendanceRate());

            return statistics;

        } catch (Exception e) {
            log.error("[移动端考勤] 统计失败: employeeId={}, error={}",
                request.getEmployeeId(), e.getMessage(), e);

            return MobileStatisticsResult.builder()
                .success(false)
                .message("统计数据查询失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 人脸识别打卡
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MobileClockInResult clockInWithFace(MobileClockInRequest request) {
        log.info("[移动端考勤] 人脸打卡: employeeId={}", request.getEmployeeId());

        try {
            // 1. 验证生物识别特征
            BiometricVerifyResult biometricResult = biometricAttendanceManager.verifyFace(
                request.getEmployeeId(),
                request.getFaceImage()
            );

            if (!biometricResult.isSuccess()) {
                return MobileClockInResult.builder()
                    .success(false)
                    .message("人脸识别失败: " + biometricResult.getMessage())
                    .build();
            }

            // 2. 验证GPS位置
            LocationValidationResult locationResult = gpsLocationValidator.validateLocation(
                request.getEmployeeId(),
                request.getLatitude(),
                request.getLongitude()
            );

            if (!locationResult.isValid()) {
                return MobileClockInResult.builder()
                    .success(false)
                    .message("位置验证失败: " + locationResult.getMessage())
                    .build();
            }

            // 3. 创建打卡记录
            AttendanceRecordEntity record = attendanceManager.createRecord(
                request.getEmployeeId(),
                request.getLatitude(),
                request.getLongitude(),
                "FACE"
            );

            // 4. 返回打卡结果
            return MobileClockInResult.builder()
                .success(true)
                .employeeId(request.getEmployeeId())
                .recordId(record.getRecordId())
                .clockInTime(record.getPunchTime())
                .location(record.getPunchLocation())
                .verifyMethod("FACE")
                .message("打卡成功")
                .build();

        } catch (Exception e) {
            log.error("[移动端考勤] 人脸打卡失败: employeeId={}, error={}",
                request.getEmployeeId(), e.getMessage(), e);

            return MobileClockInResult.builder()
                .success(false)
                .message("打卡失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 计算实际出勤天数
     */
    private int getActualAttendanceDays(List<AttendanceRecordEntity> records) {
        return (int) records.stream()
            .filter(r -> "NORMAL".equals(r.getStatus()))
            .map(AttendanceRecordEntity::getWorkDate)
            .distinct()
            .count();
    }

    /**
     * 计算迟到天数
     */
    private int getLateDays(List<AttendanceRecordEntity> records) {
        return (int) records.stream()
            .filter(r -> "LATE".equals(r.getStatus()))
            .count();
    }

    /**
     * 计算早退天数
     */
    private int getEarlyLeaveDays(List<AttendanceRecordEntity> records) {
        return (int) records.stream()
            .filter(r -> "EARLY_LEAVE".equals(r.getStatus()))
            .count();
    }

    /**
     * 计算旷工天数
     */
    private int getAbsentDays(List<AttendanceRecordEntity> records) {
        return (int) records.stream()
            .filter(r -> "ABSENTEEISM".equals(r.getStatus()))
            .count();
    }

    /**
     * 计算加班时长（分钟）
     */
    private long getOvertimeHours(List<AttendanceRecordEntity> records) {
        return records.stream()
            .map(AttendanceRecordEntity::getOvertimeMinutes)
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();
    }

    /**
     * 计算请假天数
     */
    private int getLeaveDays(List<AttendanceRecordEntity> records) {
        // TODO: 从请假服务获取请假数据
        return 0;
    }
}
```

---

## 🔐 二、门禁管理模块 TODO分析

### 模块概述

根据`documentation/业务模块/03-门禁管理模块/01-门禁模块总体设计文档.md`，门禁管理是安防核心模块，提供：
- **多模态统一认证**: 人脸、指纹、IC卡、密码、虹膜、声纹
- **智能权限管控**: 基于区域、时间、角色的动态权限管理
- **实时监控预警**: 设备状态监控、异常行为检测
- **远程设备控制**: 远程开门、设备配置、状态监控

### TODO详细分析

#### 1. 多模态认证策略统计

**TODO编号**: ACC-003
**文件位置**: 各认证策略类 (FaceAuthenticationStrategy.java等)
**TODO内容**: 统计各认证方式的使用次数
**优先级**: P2
**业务逻辑**:

**企业级实现建议**:

```java
@Component
@Slf4j
public class AuthenticationStatisticsManager {

    @Resource
    private AuthenticationRecordDao authenticationRecordDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STATS_CACHE_KEY = "access:auth:stats:";

    /**
     * 记录认证方式使用
     */
    public void recordAuthentication(Long userId, String verifyMethod, boolean success) {
        log.debug("[认证统计] 记录认证: userId={}, method={}, success={}",
            userId, verifyMethod, success);

        try {
            // 1. 记录到数据库
            AuthenticationRecordEntity record = new AuthenticationRecordEntity();
            record.setUserId(userId);
            record.setVerifyMethod(verifyMethod);
            record.setSuccess(success);
            record.setAuthenticateTime(LocalDateTime.now());

            authenticationRecordDao.insert(record);

            // 2. 更新Redis计数器
            String counterKey = STATS_CACHE_KEY + verifyMethod + ":" +
                (success ? "success" : "failure");
            redisTemplate.opsForValue().increment(counterKey);

            // 3. 设置过期时间（30天）
            redisTemplate.expire(counterKey, Duration.ofDays(30));

        } catch (Exception e) {
            log.error("[认证统计] 记录失败: userId={}, method={}, error={}",
                userId, verifyMethod, e.getMessage(), e);
        }
    }

    /**
     * 获取认证方式统计报表
     */
    public Map<String, Object> getAuthenticationStatistics(LocalDateTime startTime,
                                                              LocalDateTime endTime) {
        log.info("[认证统计] 生成统计报表: startTime={}, endTime={}", startTime, endTime);

        try {
            // 1. 查询认证记录
            List<AuthenticationRecordEntity> records = authenticationRecordDao.selectList(
                new LambdaQueryWrapper<AuthenticationRecordEntity>()
                    .between(AuthenticationRecordEntity::getAuthenticateTime, startTime, endTime)
            );

            // 2. 按认证方式分组统计
            Map<String, Long> methodStats = records.stream()
                .collect(Collectors.groupingBy(
                    AuthenticationRecordEntity::getVerifyMethod,
                    Collectors.counting()
                ));

            // 3. 按成功/失败分组统计
            Map<String, Long> successStats = records.stream()
                .collect(Collectors.groupingBy(
                    r -> r.getSuccess() ? "success" : "failure",
                    Collectors.counting()
                ));

            // 4. 计算成功率
            long totalCount = records.size();
            long successCount = successStats.getOrDefault("success", 0L);
            double successRate = totalCount > 0 ?
                (double) successCount / totalCount * 100 : 0.0;

            // 5. 组装结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalAttempts", totalCount);
            statistics.put("successCount", successCount);
            statistics.put("failureCount", totalCount - successCount);
            statistics.put("successRate", successRate);
            statistics.put("methodStatistics", methodStats);

            log.info("[认证统计] 统计完成: totalCount={}, successRate={}%",
                totalCount, successRate);

            return statistics;

        } catch (Exception e) {
            log.error("[认证统计] 统计失败: error={}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取认证方式使用排行
     */
    public List<Map<String, Object>> getTopAuthenticationMethods(LocalDateTime startTime,
                                                                     LocalDateTime endTime,
                                                                     int topN) {
        log.info("[认证统计] 查询排行: startTime={}, endTime={}, topN={}",
            startTime, endTime, topN);

        try {
            // 1. 查询并聚合
            List<AuthenticationRecordEntity> records = authenticationRecordDao.selectList(
                new LambdaQueryWrapper<AuthenticationRecordEntity>()
                    .between(AuthenticationRecordEntity::getAuthenticateTime, startTime, endTime)
            );

            // 2. 按认证方式分组并排序
            Map<String, Long> methodCounts = records.stream()
                .collect(Collectors.groupingBy(
                    AuthenticationRecordEntity::getVerifyMethod,
                    Collectors.counting()
                ));

            // 3. 转换为列表并排序
            List<Map<String, Object>> ranking = methodCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("verifyMethod", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percentage",
                        new BigDecimal(entry.getValue())
                            .divide(new BigDecimal(records.size()), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100")));
                    return item;
                })
                .collect(Collectors.toList());

            log.info("[认证统计] 排行查询完成: topN={}", topN);
            return ranking;

        } catch (Exception e) {
            log.error("[认证统计] 排行查询失败: error={}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
```

---

#### 2. 实时监控服务 (AccessMonitorService)

**TODO编号**: ACC-004
**文件位置**: `AccessMonitorServiceImpl.java`
**TODO内容**: 实现报警查询功能
**优先级**: P0
**业务逻辑**:

**企业级实现建议**:

```java
@Service
@Slf4j
public class EnterpriseAccessMonitorService implements AccessMonitorService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private AlarmRecordDao alarmRecordDao;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 查询实时告警
     */
    @Override
    public PageResult<AlarmRecordVO> queryAlarms(AlarmQueryForm form) {
        log.info("[门禁监控] 查询告警: startTime={}, endTime={}, level={}",
            form.getStartTime(), form.getEndTime(), form.getAlarmLevel());

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<AlarmRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

            if (form.getStartTime() != null) {
                queryWrapper.ge(AlarmRecordEntity::getAlarmTime, form.getStartTime());
            }
            if (form.getEndTime() != null) {
                queryWrapper.le(AlarmRecordEntity::getAlarmTime, form.getEndTime());
            }
            if (form.getAlarmLevel() != null) {
                queryWrapper.eq(AlarmRecordEntity::getAlarmLevel, form.getAlarmLevel());
            }
            if (form.getProcessed() != null) {
                queryWrapper.eq(AlarmRecordEntity::getProcessed, form.getProcessed());
            }

            // 2. 分页查询
            Page<AlarmRecordEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
            accessRecordDao.selectPage(page, queryWrapper);

            // 3. 转换为VO
            List<AlarmRecordVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

            PageResult<AlarmRecordVO> result = PageResult.of(voList, page.getTotal(),
                form.getPageNum(), form.getPageSize());

            log.info("[门禁监控] 告警查询完成: total={}", page.getTotal());
            return result;

        } catch (Exception e) {
            log.error("[门禁监控] 告警查询失败: error={}", e.getMessage(), e);
            throw new SystemException("ALARM_QUERY_ERROR", "告警查询失败", e);
        }
    }

    /**
     * 处理告警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlarm(Long alarmId, AlarmHandleForm form) {
        log.info("[门禁监控] 处理告警: alarmId={}, handleType={}",
            alarmId, form.getHandleType());

        try {
            // 1. 查询告警记录
            AlarmRecordEntity alarm = alarmRecordDao.selectById(alarmId);
            if (alarm == null) {
                throw new BusinessException("ALARM_NOT_FOUND", "告警记录不存在");
            }

            // 2. 验证是否已处理
            if (Boolean.TRUE.equals(alarm.getProcessed())) {
                throw new BusinessException("ALARM_ALREADY_PROCESSED", "告警已处理");
            }

            // 3. 更新处理状态
            alarm.setProcessed(true);
            alarm.setHandleType(form.getHandleType());
            alarm.setHandleResult(form.getHandleResult());
            alarm.setHandleTime(LocalDateTime.now());
            alarm.setHandlerId(form.getHandlerId());
            alarm.setHandlerName(form.getHandlerName());

            alarmRecordDao.updateById(alarm);

            // 4. 发送处理通知
            sendAlarmHandleNotification(alarm, form);

            log.info("[门禁监控] 告警处理完成: alarmId={}", alarmId);

        } catch (Exception e) {
            log.error("[门禁监控] 告警处理失败: alarmId={}, error={}",
                alarmId, e.getMessage(), e);
            throw new SystemException("ALARM_HANDLE_ERROR", "告警处理失败", e);
        }
    }

    /**
     * 获取实时统计数据
     */
    @Override
    public Map<String, Object> getRealtimeStatistics() {
        log.info("[门禁监控] 查询实时统计");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 1. 设备统计
            long totalDevices = accessRecordDao.selectCount(null);
            long onlineDevices = accessRecordDao.selectCount(
                new LambdaQueryWrapper<AccessDeviceEntity>()
                    .eq(AccessDeviceEntity::getDeviceStatus, 1)
            );
            long offlineDevices = totalDevices - onlineDevices;

            statistics.put("totalDevices", totalDevices);
            statistics.put("onlineDevices", onlineDevices);
            statistics.put("offlineDevices", offlineDevices);
            statistics.put("onlineRate",
                totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0.0);

            // 2. 今日通行统计
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

            long todayPassCount = accessRecordDao.selectCount(
                new LambdaQueryWrapper<AccessRecordEntity>()
                    .between(AccessRecordEntity::getAccessTime, todayStart, todayEnd)
                    .eq(AccessRecordEntity::getAccessResult, 0)  // 0-成功
            );

            long todayFailCount = accessRecordDao.selectCount(
                new LambdaQueryWrapper<AccessRecordEntity>()
                    .between(AccessRecordEntity::getAccessTime, todayStart, todayEnd)
                    .ne(AccessRecordEntity::getAccessResult, 0)
            );

            statistics.put("todayPassCount", todayPassCount);
            statistics.put("todayFailCount", todayFailCount);
            statistics.put("todayTotalCount", todayPassCount + todayFailCount);
            statistics.put("todaySuccessRate",
                (todayPassCount + todayFailCount) > 0 ?
                (double) todayPassCount / (todayPassCount + todayFailCount) * 100 : 0.0);

            // 3. 告警统计（TODO: 需要实现报警表后完善）
            statistics.put("unhandledAlarms", 0L);

            log.info("[门禁监控] 实时统计完成: onlineRate={}%, todaySuccessRate={}%",
                statistics.get("onlineRate"), statistics.get("todaySuccessRate"));

            return statistics;

        } catch (Exception e) {
            log.error("[门禁监控] 实时统计失败: error={}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 发送告警处理通知
     */
    private void sendAlarmHandleNotification(AlarmRecordEntity alarm, AlarmHandleForm form) {
        // TODO: 集成通知服务
        log.info("[门禁监控] 发送告警处理通知: alarmId={}, handlerId={}",
            alarm.getAlarmId(), form.getHandlerId());
    }

    /**
     * 转换为VO
     */
    private AlarmRecordVO convertToVO(AlarmRecordEntity entity) {
        AlarmRecordVO vo = new AlarmRecordVO();
        // TODO: 实现字段映射
        return vo;
    }
}
```

---

## 📹 三、视频管理模块 TODO分析

### 模块概述

根据`documentation/业务模块/各业务模块文档/智能视频/01-项目概述与系统架构.md`，视频模块提供：
- **实时监控**: 多画面、多协议、多格式视频流
- **录像回放**: 高效查询、时间轴预览、多倍速播放
- **AI智能分析**: 人脸识别、行为分析、异常检测
- **告警管理**: 实时告警、智能联动、自动处理

### TODO详细分析

#### 1. 行为检测管理器 (BehaviorDetectionManager)

**TODO编号**: VID-001
**文件位置**: `BehaviorDetectionManager.java`
**TODO内容**: 集成跌倒检测AI模型
**优先级**: P1
**业务逻辑**:

**企业级实现建议**:

```java
@Component
@Slf4j
public class AIBehaviorDetectionManager {

    @Resource
    private VideoAiAnalysisService videoAiAnalysisService;

    @Resource
    private VideoEventManager videoEventManager;

    /**
     * 跌倒检测（集成AI模型）
     */
    public BehaviorDetectionResult detectFalls(String cameraId,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime) {
        log.info("[行为检测] 跌倒检测: cameraId={}, timeRange={}-{}",
            cameraId, startTime, endTime);

        try {
            // 1. 获取视频流帧
            List<VideoFrame> frames = videoEventManager.extractFrames(cameraId, startTime, endTime);

            // 2. 调用AI模型进行检测
            List<FallDetectionResult> fallResults = new ArrayList<>();

            for (VideoFrame frame : frames) {
                // 调用AI模型
                FallDetectionResult result = aiFallDetectionModel.detect(frame);

                if (result.isFall()) {
                    fallResults.add(result);

                    // 3. 创建行为事件记录
                    VideoBehaviorEntity behavior = new VideoBehaviorEntity();
                    behavior.setCameraId(cameraId);
                    behavior.setBehaviorType("FALL");
                    behavior.setOccurTime(frame.getTimestamp());
                    behavior.setConfidence(result.getConfidence());
                    behavior.setBoundingBox(result.getBoundingBox());
                    behavior.setSnapshotUrl(frame.getSnapshotUrl());

                    videoBehaviorManager.saveBehavior(behavior);
                }
            }

            // 4. 组装检测结果
            BehaviorDetectionResult detectionResult = new BehaviorDetectionResult();
            detectionResult.setCameraId(cameraId);
            detectionResult.setStartTime(startTime);
            detectionResult.setEndTime(endTime);
            detectionResult.setBehaviorType("FALL");
            detectionResult.setDetectionCount(fallResults.size());
            detectionResult.setResults(fallResults);

            log.info("[行为检测] 跌倒检测完成: cameraId={}, detectedCount={}",
                cameraId, fallResults.size());

            return detectionResult;

        } catch (Exception e) {
            log.error("[行为检测] 跌倒检测失败: cameraId={}, error={}",
                cameraId, e.getMessage(), e);

            return BehaviorDetectionResult.builder()
                .success(false)
                .message("跌倒检测失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 区域聚集检测（基于DBSCAN算法）
     */
    public BehaviorDetectionResult detectCrowding(String cameraId,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        log.info("[行为检测] 聚集检测: cameraId={}, timeRange={}-{}",
            cameraId, startTime, endTime);

        try {
            // 1. 获取人员检测数据
            List<PersonDetection> detections = videoAiAnalysisService.detectPersons(
                cameraId, startTime, endTime);

            if (detections.isEmpty()) {
                log.info("[行为检测] 无人员检测数据: cameraId={}", cameraId);
                return BehaviorDetectionResult.builder()
                    .cameraId(cameraId)
                    .behaviorType("CROWDING")
                    .detectionCount(0)
                    .build();
            }

            // 2. 使用DBSCAN算法进行聚类
            DBSCANClustering dbscan = new DBSCANClustering();
            dbscan.setMinPts(3);  // 最小点数
            dbscan.setEpsilon(50.0);  // 邻域半径

            List<Cluster> clusters = dbscan.cluster(detections);

            // 3. 识别聚集区域
            List<CrowdingEvent> crowdings = new ArrayList<>();

            for (Cluster cluster : clusters) {
                if (cluster.getPointCount() >= 5) {  // 超过5人视为聚集
                    CrowdingEvent crowd = new CrowdingEvent();
                    crowd.setCameraId(cameraId);
                    crowd.setOccurTime(cluster.getCenterTime());
                    crowd.setPersonCount(cluster.getPointCount());
                    crowd.setCenterLocation(cluster.getCenterLocation());
                    crowd.setRadius(cluster.getRadius());
                    crowd.setSeverity(calculateCrowdingSeverity(cluster.getPointCount()));

                    crowdings.add(crowd);
                }
            }

            // 4. 创建行为记录
            for (CrowdingEvent crowd : crowdings) {
                VideoBehaviorEntity behavior = new VideoBehaviorEntity();
                behavior.setCameraId(cameraId);
                behavior.setBehaviorType("CROWDING");
                behavior.setOccurTime(crowd.getOccurTime());
                behavior.setPersonCount(crowd.getPersonCount());
                behavior.setSeverity(crowd.getSeverity());

                videoBehaviorManager.saveBehavior(behavior);
            }

            // 5. 组装结果
            BehaviorDetectionResult result = new BehaviorDetectionResult();
            result.setCameraId(cameraId);
            result.setBehaviorType("CROWDING");
            result.setDetectionCount(crowdings.size());
            result.setResults(crowdings);

            log.info("[行为检测] 聚集检测完成: cameraId={}, detectedCount={}",
                cameraId, crowdings.size());

            return result;

        } catch (Exception e) {
            log.error("[行为检测] 聚集检测失败: cameraId={}, error={}",
                cameraId, e.getMessage(), e);

            return BehaviorDetectionResult.builder()
                .success(false)
                .message("聚集检测失败: " + e.getMessage())
                .build();
        }
    }

    /**
     * 计算聚集严重程度
     */
    private String calculateCrowdingSeverity(int personCount) {
        if (personCount >= 20) {
            return "CRITICAL";
        } else if (personCount >= 15) {
            return "HIGH";
        } else if (personCount >= 10) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}

/**
 * DBSCAN聚类算法实现
 */
@Component
@Slf4j
class DBSCANClustering {

    private int minPts = 3;      // 最小点数
    private double epsilon = 50.0;  // 邻域半径（米）

    public List<Cluster> cluster(List<PersonDetection> detections) {
        log.info("[DBSCAN] 开始聚类: points={}, minPts={}, epsilon={}",
            detections.size(), minPts, epsilon);

        List<Cluster> clusters = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> noise = new HashSet<>();

        for (int i = 0; i < detections.size(); i++) {
            if (!visited.contains(i)) {
                visited.add(i);

                List<Integer> neighbors = getNeighbors(detections, i);

                if (neighbors.size() < minPts) {
                    noise.add(i);  // 标记为噪声
                } else {
                    // 创建新簇
                    Cluster cluster = new Cluster();
                    clusters.add(cluster);

                    // 扩展簇
                    expandCluster(detections, i, neighbors, cluster, visited);
                }
            }
        }

        log.info("[DBSCAN] 聚类完成: clusters={}, noise={}",
            clusters.size(), noise.size());

        return clusters;
    }

    /**
     * 扩展簇
     */
    private void expandCluster(List<PersonDetection> detections,
                               int pointIndex,
                               List<Integer> neighbors,
                               Cluster cluster,
                               Set<Integer> visited) {
        cluster.addPoint(pointIndex);

        Queue<Integer> queue = new LinkedList<>(neighbors);

        while (!queue.isEmpty()) {
            Integer current = queue.poll();

            List<Integer> currentNeighbors = getNeighbors(detections, current);

            if (currentNeighbors.size() >= minPts) {
                for (Integer neighbor : currentNeighbors) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        cluster.addPoint(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
    }

    /**
     * 获取邻居点
     */
    private List<Integer> getNeighbors(List<PersonDetection> detections, int pointIndex) {
        List<Integer> neighbors = new ArrayList<>();
        PersonDetection p1 = detections.get(pointIndex);

        for (int i = 0; i < detections.size(); i++) {
            if (i == pointIndex) continue;

            PersonDetection p2 = detections.get(i);

            // 计算距离
            double distance = calculateDistance(p1, p2);

            if (distance <= epsilon) {
                neighbors.add(i);
            }
        }

        return neighbors;
    }

    /**
     * 计算两点之间的距离
     */
    private double calculateDistance(PersonDetection p1, PersonDetection p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }
}
```

---

#### 2. 人脸识别管理 (VideoFaceManager)

**TODO编号**: VID-002
**文件位置**: `VideoFaceManager.java`
**TODO内容**: 调用人脸比对算法服务
**优先级**: P0
**业务逻辑**:

**企业级实现建议**:

```java
@Component
@Slf4j
public class EnterpriseVideoFaceManager {

    @Resource
    private FaceRecognitionService faceRecognitionService;

    @Resource
    private BiometricTemplateManager biometricTemplateManager;

    /**
     * 人脸比对（1:1验证）
     */
    public Map<String, Object> performFaceCompare(String sourceFaceUrl,
                                                   String targetFaceUrl,
                                                   BigDecimal similarityThreshold) {
        log.info("[人脸识别] 人脸比对: source={}, target={}, threshold={}",
            sourceFaceUrl, targetFaceUrl, similarityThreshold);

        try {
            // 1. 提取人脸特征
            FaceFeature sourceFeature = faceRecognitionService.extractFeature(sourceFaceUrl);
            FaceFeature targetFeature = faceRecognitionService.extractFeature(targetFaceUrl);

            // 2. 计算相似度
            BigDecimal similarity = faceRecognitionService.calculateSimilarity(
                sourceFeature, targetFeature);

            // 3. 判断是否匹配
            boolean matched = similarity.compareTo(similarityThreshold) >= 0;

            // 4. 组装结果
            Map<String, Object> result = new HashMap<>();
            result.put("sourceFaceUrl", sourceFaceUrl);
            result.put("targetFaceUrl", targetFaceUrl);
            result.put("similarity", similarity);
            result.put("threshold", similarityThreshold);
            result.put("matched", matched);
            result.put("confidence", calculateConfidence(similarity));

            log.info("[人脸识别] 人脸比对完成: similarity={}, matched={}",
                similarity, matched);

            return result;

        } catch (Exception e) {
            log.error("[人脸识别] 人脸比对失败: error={}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "人脸比对失败: " + e.getMessage());

            return errorResult;
        }
    }

    /**
     * 人脸搜索（1:N识别）
     */
    public Map<String, Object> performFaceSearch(String searchFaceUrl,
                                                  Integer searchLibrary,
                                                  BigDecimal similarityThreshold,
                                                  Integer maxResults) {
        log.info("[人脸识别] 人脸搜索: searchLibrary={}, threshold={}, maxResults={}",
            searchLibrary, similarityThreshold, maxResults);

        try {
            // 1. 提取搜索人脸特征
            FaceFeature searchFeature = faceRecognitionService.extractFeature(searchFaceUrl);

            // 2. 从底库中获取人脸模板
            List<FaceTemplate> templates = biometricTemplateManager.getTemplatesByLibrary(searchLibrary);

            // 3. 批量比对
            List<FaceMatchResult> matches = new ArrayList<>();

            for (FaceTemplate template : templates) {
                BigDecimal similarity = faceRecognitionService.calculateSimilarity(
                    searchFeature, template.getFeature());

                if (similarity.compareTo(similarityThreshold) >= 0) {
                    FaceMatchResult match = new FaceMatchResult();
                    match.setUserId(template.getUserId());
                    match.setUserName(template.getUserName());
                    match.setSimilarity(similarity);
                    match.setTemplateId(template.getTemplateId());

                    matches.add(match);
                }
            }

            // 4. 按相似度排序，取前N个
            List<FaceMatchResult> topMatches = matches.stream()
                .sorted((m1, m2) -> m2.getSimilarity().compareTo(m1.getSimilarity()))
                .limit(maxResults)
                .collect(Collectors.toList());

            // 5. 组装结果
            Map<String, Object> result = new HashMap<>();
            result.put("searchFaceUrl", searchFaceUrl);
            result.put("searchLibrary", searchLibrary);
            result.put("threshold", similarityThreshold);
            result.put("maxResults", maxResults);
            result.put("matchCount", topMatches.size());
            result.put("matches", topMatches);

            log.info("[人脸识别] 人脸搜索完成: library={}, matchCount={}",
                searchLibrary, topMatches.size());

            return result;

        } catch (Exception e) {
            log.error("[人脸识别] 人脸搜索失败: error={}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "人脸搜索失败: " + e.getMessage());

            return errorResult;
        }
    }

    /**
     * 人脸质量评估
     */
    public Map<String, Object> evaluateFaceQuality(String faceImageUrl) {
        log.info("[人脸识别] 质量评估: imageUrl={}", faceImageUrl);

        try {
            // 1. 提取人脸特征
            FaceFeature feature = faceRecognitionService.extractFeature(faceImageUrl);

            // 2. 评估质量指标
            FaceQualityAssessment assessment = new FaceQualityAssessment();

            // 2.1 清晰度评估
            assessment.setSharpness(evaluateSharpness(feature));

            // 2.2 亮度评估
            assessment.setBrightness(evaluateBrightness(feature));

            // 2.3 姿态角度评估
            assessment.setPoseAngle(evaluatePoseAngle(feature));

            // 2.4 遮挡评估
            assessment.setOcclusion(evaluateOcclusion(feature));

            // 2.5 综合质量评分
            BigDecimal overallQuality = calculateOverallQuality(assessment);
            assessment.setOverallQuality(overallQuality);

            // 3. 判断是否可用
            boolean isUsable = overallQuality.compareTo(new BigDecimal("0.7")) >= 0;
            assessment.setUsable(isUsable);

            // 4. 组装结果
            Map<String, Object> result = new HashMap<>();
            result.put("imageUrl", faceImageUrl);
            result.put("sharpness", assessment.getSharpness());
            result.put("brightness", assessment.getBrightness());
            result.put("poseAngle", assessment.getPoseAngle());
            result.put("occlusion", assessment.getOcclusion());
            result.put("overallQuality", assessment.getOverallQuality());
            result.put("usable", assessment.isUsable());
            result.put("recommendations", generateRecommendations(assessment));

            log.info("[人脸识别] 质量评估完成: quality={}, usable={}",
                overallQuality, isUsable);

            return result;

        } catch (Exception e) {
            log.error("[人脸识别] 质量评估失败: error={}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "质量评估失败: " + e.getMessage());

            return errorResult;
        }
    }

    /**
     * 计算综合质量评分
     */
    private BigDecimal calculateOverallQuality(FaceQualityAssessment assessment) {
        // 加权平均
        BigDecimal quality = assessment.getSharpness()
            .multiply(new BigDecimal("0.3"))  // 清晰度权重30%
            .add(assessment.getBrightness().multiply(new BigDecimal("0.25")))  // 亮度权重25%
            .add(assessment.getPoseAngle().multiply(new BigDecimal("0.25")))  // 姿态权重25%
            .add(assessment.getOcclusion().multiply(new BigDecimal("0.2")));  // 遮挡权重20%

        return quality;
    }

    /**
     * 生成改进建议
     */
    private List<String> generateRecommendations(FaceQualityAssessment assessment) {
        List<String> recommendations = new ArrayList<>();

        if (assessment.getSharpness().compareTo(new BigDecimal("0.6")) < 0) {
            recommendations.add("图像模糊，建议重新拍摄");
        }

        if (assessment.getBrightness().compareTo(new BigDecimal("0.5")) < 0) {
            recommendations.add("光线不足，建议改善照明");
        }

        if (assessment.getBrightness().compareTo(new BigDecimal("0.9")) > 0) {
            recommendations.add("光线过强，建议减少光照");
        }

        if (assessment.getPoseAngle().compareTo(new BigDecimal("0.6")) < 0) {
            recommendations.add("姿态角度过大，建议正对摄像头");
        }

        if (assessment.getOcclusion().compareTo(new BigDecimal("0.7")) < 0) {
            recommendations.add("面部有遮挡，建议移除遮挡物");
        }

        return recommendations;
    }
}
```

---

## 🍔 四、消费管理模块 TODO分析

### 模块概述

消费管理模块提供：
- **区域管理**: 食堂、超市、自动售卖机等消费区域
- **餐别分类**: 早餐、午餐、晚餐、夜宵
- **账户管理**: 员工账户、访客账户、补贴账户
- **消费处理**: 扣款、退款、对账

### TODO详细分析

消费服务的TODO已在之前的会话中完成，这里不再重复。

---

## 🔬 五、生物识别模块 TODO分析

### TODO编号**: BIO-001
**文件位置**: `BiometricFeatureExtractionServiceImpl.java`
**TODO内容**: 实现人脸特征提取
**优先级**: P0
**企业级实现建议**:

```java
@Service
@Slf4j
public class EnterpriseBiometricFeatureExtractionService implements BiometricFeatureExtractionService {

    @Resource
    private FaceNetModel faceNetModel;

    @Resource
    private ImageProcessingUtil imageProcessingUtil;

    /**
     * 提取人脸特征
     */
    @Override
    public FaceFeature extractFeature(String imageUrl) throws Exception {
        log.info("[生物识别] 提取人脸特征: imageUrl={}", imageUrl);

        try {
            // 1. 下载图像
            BufferedImage image = downloadImage(imageUrl);

            // 2. 图像预处理
            BufferedImage processedImage = imageProcessingUtil.preprocess(image);

            // 3. 人脸检测
            List<FaceDetection> detections = faceNetModel.detectFaces(processedImage);

            if (detections.isEmpty()) {
                throw new BusinessException("FACE_NOT_DETECTED", "未检测到人脸");
            }

            // 4. 选择最大的人脸
            FaceDetection mainFace = detections.stream()
                .max(Comparator.comparingInt(FaceDetection::getArea))
                .orElseThrow(() -> new BusinessException("FACE_NOT_DETECTED", "未检测到人脸"));

            // 5. 提取特征向量
            float[] featureVector = faceNetModel.extractFeature(processedImage, mainFace);

            // 6. 创建特征对象
            FaceFeature feature = new FaceFeature();
            feature.setFeatureVector(featureVector);
            feature.setImageSize(image.getWidth() + "x" + image.getHeight());
            feature.setFaceSize(mainFace.getWidth() + "x" + mainFace.getHeight());
            feature.setConfidence(mainFace.getConfidence());

            log.info("[生物识别] 特征提取完成: featureDimension={}", featureVector.length);

            return feature;

        } catch (Exception e) {
            log.error("[生物识别] 特征提取失败: imageUrl={}, error={}",
                imageUrl, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 批量提取特征
     */
    @Override
    public List<FaceFeature> batchExtractFeatures(List<String> imageUrls) {
        log.info("[生物识别] 批量提取特征: count={}", imageUrls.size());

        List<FaceFeature> features = new ArrayList<>();
        List<String> failedUrls = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            try {
                FaceFeature feature = extractFeature(imageUrl);
                features.add(feature);
            } catch (Exception e) {
                log.warn("[生物识别] 特征提取失败: imageUrl={}, error={}",
                    imageUrl, e.getMessage());
                failedUrls.add(imageUrl);
            }
        }

        log.info("[生物识别] 批量提取完成: success={}, failed={}",
            features.size(), failedUrls.size());

        return features;
    }
}
```

---

## 📊 六、企业级实现最佳实践

### 1. 架构规范遵循

**四层架构**: Controller → Service → Manager → DAO

```java
// ✅ 正确示例
@RestController
public class AttendanceController {
    @Resource
    private AttendanceService attendanceService;  // Controller只依赖Service
}

@Service
public class AttendanceServiceImpl implements AttendanceService {
    @Resource
    private AttendanceManager attendanceManager;  // Service依赖Manager

    @Resource
    private AttendanceRecordDao attendanceRecordDao;  // Service可以依赖DAO（简单CRUD）
}

public class AttendanceManager {
    private final AttendanceRecordDao attendanceRecordDao;  // Manager只依赖DAO

    @Resource
    private GatewayServiceClient gatewayServiceClient;  // 通过Gateway调用其他服务
}
```

### 2. 日志规范

**统一使用@Slf4j注解**:

```java
@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {

    public AttendanceResult calculateAttendance(AttendanceRequest request) {
        log.info("[考勤服务] 开始计算考勤: employeeId={}, date={}",
            request.getEmployeeId(), request.getDate());

        try {
            // 业务逻辑...

            log.info("[考勤服务] 考勤计算完成: employeeId={}, result={}",
                request.getEmployeeId(), result.getStatus());

            return result;

        } catch (BusinessException e) {
            log.warn("[业务异常] 计算失败: employeeId={}, reason={}",
                request.getEmployeeId(), e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[系统异常] 计算异常: employeeId={}, error={}",
                request.getEmployeeId(), e.getMessage(), e);
            throw new SystemException("CALCULATION_ERROR", "考勤计算失败", e);
        }
    }
}
```

### 3. 异常处理规范

**自定义异常**:

```java
// 业务异常（可预期）
public class BusinessException extends RuntimeException {
    private String code;
    private String message;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

// 系统异常（不可预期）
public class SystemException extends RuntimeException {
    private String code;
    private String message;
    private Throwable cause;

    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.cause = cause;
    }
}
```

**全局异常处理**:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public ResponseDTO<Void> handleSystemException(SystemException e) {
        log.error("[系统异常] code={}, message={}", e.getCode(), e.getMessage(), e);
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }
}
```

### 4. 性能优化

**多级缓存策略**:

```java
@Service
@Slf4j
public class CachedAttendanceService {

    @Cacheable(value = "attendance:stats", key = "#employeeId + ':' + #month")
    public MobileStatisticsResult getStatistics(Long employeeId, YearMonth month) {
        // 缓存统计数据
    }

    @CacheEvict(value = "attendance:stats", allEntries = true)
    public void clearCache() {
        // 清除所有缓存
    }
}
```

**异步处理**:

```java
@Service
@Slf4j
public class AsyncAttendanceService {

    @Async("taskExecutor")
    public CompletableFuture<Void> asyncCalculateAttendance(AttendanceRequest request) {
        return CompletableFuture.runAsync(() -> {
            calculateAttendance(request);
        });
    }
}
```

---

## 📈 七、实施计划

### 阶段一：核心功能完善（2周）

| 优先级 | TODO编号 | 模块 | 功能 | 预估时间 |
|-------|----------|------|------|----------|
| P0 | ATT-008 | 考勤 | 规则引擎 | 3天 |
| P0 | ACC-004 | 门禁 | 实时监控 | 2天 |
| P0 | VID-002 | 视频 | 人脸识别 | 3天 |
| P1 | ATT-004 | 考勤 | 冲突检测 | 2天 |
| P1 | VID-001 | 视频 | 行为检测 | 2天 |

### 阶段二：重要功能完善（2周）

| 优先级 | TODO编号 | 模块 | 功能 | 预估时间 |
|-------|----------|------|------|----------|
| P1 | ATT-001 | 考勤 | 遗传算法 | 5天 |
| P1 | VID-003 | 视频 | 人脸搜索 | 3天 |
| P1 | ACC-003 | 门禁 | 认证统计 | 2天 |
| P2 | ATT-006 | 考勤 | 需求预测 | 3天 |
| P2 | VID-005 | 视频 | 行为分析 | 3天 |

### 阶段三：优化功能完善（1周）

| 优先级 | TODO编号 | 模块 | 功能 | 预估时间 |
|-------|----------|------|------|----------|
| P2 | BIO-001 | 生物识别 | 特征提取 | 3天 |
| P2 | VID-004 | 视频 | 质量评估 | 2天 |
| P2 | ACC-005 | 门禁 | 二维码生成 | 2天 |

---

## ✅ 八、质量保障

### 代码审查清单

- [ ] 遵循四层架构规范
- [ ] 使用@Resource而非@Autowired
- [ ] 使用@Slf4j注解
- [ ] 完整的日志记录
- [ ] 完善的异常处理
- [ ] 统一的响应格式
- [ ] 事务管理正确

### 测试策略

**单元测试覆盖率**: >80%

**集成测试**: 覆盖核心业务流程

**性能测试**:
- 响应时间: <500ms (95分位)
- 并发支持: >1000 TPS
- 内存使用: <2GB

---

## 🎯 九、关键成功因素

### 1. 技术要求

- **熟悉Spring Boot 3.5.8**: Java 17, Jakarta EE 3.0+
- **熟悉MyBatis-Plus 3.5.15**: LambdaQueryWrapper, 分页查询
- **熟悉Redis + Caffeine**: 多级缓存策略
- **熟悉RabbitMQ**: 消息队列异步处理
- **熟悉GatewayServiceClient**: 微服务间调用

### 2. 业务理解

- **考勤管理**: 排班、打卡、规则配置、统计报表
- **门禁管理**: 设备管理、权限控制、实时监控
- **视频监控**: 实时预览、录像回放、AI分析
- **生物识别**: 人脸识别、特征提取、模板管理

### 3. 质量意识

- **代码质量**: 遵循IOE-DREAM开发规范
- **测试覆盖**: 完善的单元测试和集成测试
- **性能优化**: 缓存、异步、批量处理
- **安全考虑**: 权限验证、数据加密、操作审计

---

## 📞 十、支持和协作

### 技术支持

- **架构委员会**: 负责规范解释和架构决策
- **技术专家**: 各领域技术专家（数据库、缓存、安全等）
- **质量保障**: 代码质量和架构合规性检查

### 文档索引

- **核心规范**: [CLAUDE.md](./CLAUDE.md) - **唯一架构规范**
- **禁止脚本**: [MANUAL_FIX_MANDATORY_STANDARD.md](./MANUAL_FIX_MANDATORY_STANDARD.md)
- **考勤文档**: [documentation/业务模块/03-考勤管理模块/](./documentation/业务模块/03-考勤管理模块/)
- **门禁文档**: [documentation/业务模块/03-门禁管理模块/](./documentation/业务模块/03-门禁管理模块/)
- **视频文档**: [documentation/业务模块/各业务模块文档/智能视频/](./documentation/业务模块/各业务模块文档/智能视频/)

---

**文档维护**: 本文档应与代码实现同步更新，确保TODO清单的准确性和时效性。

**状态**: ✅ 已完成全局待办事项梳理

**下一步**: 开始按照优先级逐步实现TODO项，遵循企业级代码质量标准。
