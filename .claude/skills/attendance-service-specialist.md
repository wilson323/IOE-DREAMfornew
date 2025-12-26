# 考勤服务专家技能
## Attendance Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区考勤管理业务专家，精通考勤规则、排班管理、统计分析等核心业务

**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: 考勤服务开发、排班系统建设、统计分析报表、异常处理优化
**📊 技能覆盖**: 考勤打卡 | 排班管理 | 请假审批 | 统计分析 | 异常处理 | 报表系统
**🔧 技术栈**: Spring Boot 3.5.8 + MyBatis-Plus + Redis + ClickHouse

---

## 📋 技能概述

### **核心专长**
- **考勤规则引擎**: 复杂考勤规则设计和实现（弹性工作制、轮班制、计件制等）
- **智能排班系统**: 自动化排班算法、人力资源优化、冲突检测
- **多设备数据融合**: 人脸识别、指纹、工牌、APP等多考勤方式数据统一处理
- **异常检测和处理**: 考勤异常智能检测、自动处理、人工审核流程
- **统计分析系统**: 多维度考勤数据统计、可视化报表、趋势分析
- **合规性管理**: 劳动法规合规性检查、工时统计、加班管控

### **解决能力**
- **考勤服务架构**: 高可用、高性能的考勤服务架构设计和实现
- **考勤算法优化**: 考勤计算算法优化，确保准确性和性能
- **数据一致性**: 多数据源的考勤数据一致性保证和冲突解决
- **异常处理机制**: 智能异常检测、自动处理流程、人工审核系统
- **报表系统设计**: 灵活的考勤报表系统，支持自定义报表和数据导出

---

## 🎯 业务场景覆盖

### ⏰ 考勤打卡处理
```java
// 考勤打卡处理 (Spring Boot 3.5.8 + MyBatis-Plus)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

// Controller层 - REST接口
@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "考勤打卡", description = "考勤打卡和记录处理")
public class AttendanceController {

    @Resource
    private AttendanceService attendanceService;

    /**
     * 考勤打卡接口
     */
    @PostMapping("/check")
    @RateLimiter(name = "attendance-check", fallbackMethod = "checkFallback")
    @ApiOperation(value = "考勤打卡", notes = "员工考勤打卡接口")
    public ResponseDTO<AttendanceCheckResultDTO> checkIn(
            @Valid @RequestBody AttendanceCheckRequestDTO request,
            HttpServletRequest httpRequest) {

        log.info("[考勤打卡] 开始处理, employeeId={}, checkType={}",
                request.getEmployeeId(), request.getCheckType());

        // 请求来源验证
        validateRequestSource(httpRequest);

        AttendanceCheckResultDTO result = attendanceService.processAttendanceCheck(request);

        log.info("[考勤打卡] 处理完成, employeeId={}, recordId={}, status={}",
                request.getEmployeeId(), result.getRecordId(), result.getStatus());

        return ResponseDTO.ok(result);
    }

    // 服务降级处理
    public ResponseDTO<AttendanceCheckResultDTO> checkFallback(AttendanceCheckRequestDTO request, Exception ex) {
        log.error("[考勤打卡] 服务降级, employeeId={}", request.getEmployeeId(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }
}

// Service层 - 业务逻辑实现
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceServiceImpl implements AttendanceService {

    @Resource
    private AttendanceManager attendanceManager;

    @Override
    public AttendanceCheckResultDTO processAttendanceCheck(AttendanceCheckRequestDTO request) {
        try {
            // 参数验证
            validateCheckRequest(request);

            // 委托给Manager层处理复杂业务逻辑
            AttendanceCheckResult result = attendanceManager.processAttendanceCheck(request);

            return convertToDTO(result);
        } catch (BusinessException e) {
            log.warn("[考勤打卡] 业务异常, employeeId={}, error={}", request.getEmployeeId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[考勤打卡] 系统异常, employeeId={}", request.getEmployeeId(), e);
            throw new BusinessException("ATTENDANCE_CHECK_ERROR", "考勤打卡处理失败");
        }
    }

    private void validateCheckRequest(AttendanceCheckRequestDTO request) {
        if (request.getEmployeeId() == null) {
            throw new BusinessException("EMPLOYEE_ID_REQUIRED", "员工ID不能为空");
        }
        if (request.getCheckType() == null) {
            throw new BusinessException("CHECK_TYPE_REQUIRED", "打卡类型不能为空");
        }
        if (request.getCheckTime() == null) {
            request.setCheckTime(LocalDateTime.now());
        }
    }
}

// Manager层 - 复杂业务流程编排
public class AttendanceManagerImpl implements AttendanceManager {

    private final AttendanceRuleEngine ruleEngine;
    private final DeviceDataService deviceDataService;
    private final AttendanceCalculator calculator;
    private final AttendanceRecordDao attendanceRecordDao;
    private final AnomalyDetector anomalyDetector;
    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public AttendanceManagerImpl(
            AttendanceRuleEngine ruleEngine,
            DeviceDataService deviceDataService,
            AttendanceCalculator calculator,
            AttendanceRecordDao attendanceRecordDao,
            AnomalyDetector anomalyDetector,
            GatewayServiceClient gatewayServiceClient) {
        this.ruleEngine = ruleEngine;
        this.deviceDataService = deviceDataService;
        this.calculator = calculator;
        this.attendanceRecordDao = attendanceRecordDao;
        this.anomalyDetector = anomalyDetector;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceCheckResult processAttendanceCheck(AttendanceCheckRequestDTO request) {
        // 1. 多设备数据融合
        DeviceData deviceData = deviceDataService.getUnifiedData(request);

        // 2. 考勤规则应用
        AttendanceRules rules = ruleEngine.getRulesForEmployee(request.getEmployeeId());

        // 3. 考勤记录计算
        AttendanceRecord record = calculator.calculateAttendance(deviceData, rules);

        // 4. 异常检测
        List<AttendanceAnomaly> anomalies = anomalyDetector.detectAnomalies(record);

        // 5. 持久化考勤记录
        saveAttendanceRecord(record, anomalies);

        // 6. 实时通知处理
        sendRealTimeNotification(record, anomalies);

        return buildCheckResult(record, anomalies);
    }

    private void saveAttendanceRecord(AttendanceRecord record, List<AttendanceAnomaly> anomalies) {
        // 持久化考勤记录
        attendanceRecordDao.insert(record);

        // 持久化异常记录
        if (!anomalies.isEmpty()) {
            for (AttendanceAnomaly anomaly : anomalies) {
                attendanceRecordDao.insertAnomaly(anomaly);
            }
        }
    }

    private void sendRealTimeNotification(AttendanceRecord record, List<AttendanceAnomaly> anomalies) {
        // 异步发送实时通知
        CompletableFuture.runAsync(() -> {
            try {
                // 发送考勤成功通知
                if (anomalies.isEmpty()) {
                    sendAttendanceNotification(record, "SUCCESS");
                } else {
                    sendAttendanceNotification(record, "ANOMALY_DETECTED");
                }
            } catch (Exception e) {
                log.error("[实时通知] 发送失败, recordId={}", record.getId(), e);
            }
        });
    }
}

// DAO层 - 数据访问
@Mapper
public interface AttendanceRecordDao extends BaseMapper<AttendanceRecordEntity> {

    @Transactional(readOnly = true)
    List<AttendanceRecordEntity> selectByEmployeeIdAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Transactional(readOnly = true)
    AttendanceRecordEntity selectLastCheckIn(@Param("employeeId") Long employeeId, @Param("checkDate") LocalDate checkDate);

    @Transactional(rollbackFor = Exception.class)
    int insertAnomaly(@Param("anomaly") AttendanceAnomalyEntity anomaly);

    @Transactional(readOnly = true)
    List<AttendanceStatisticsEntity> selectMonthlyStatistics(
        @Param("employeeId") Long employeeId,
        @Param("yearMonth") String yearMonth
    );
}

// 实体类 - 考勤记录
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_record")
public class AttendanceRecordEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String recordId;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("check_type")
    private Integer checkType;  // 1-上班 2-下班

    @TableField("check_time")
    private LocalDateTime checkTime;

    @TableField("device_id")
    private String deviceId;

    @TableField("location")
    private String location;

    @TableField("biometric_data")
    private String biometricData;

    @TableField("work_shift_id")
    private Long workShiftId;

    @TableField("schedule_start_time")
    private LocalDateTime scheduleStartTime;

    @TableField("schedule_end_time")
    private LocalDateTime scheduleEndTime;

    @TableField("is_late")
    private Boolean isLate;

    @TableField("is_early_leave")
    private Boolean isEarlyLeave;

    @TableField("late_minutes")
    private Integer lateMinutes;

    @TableField("early_leave_minutes")
    private Integer earlyLeaveMinutes;

    @TableField("overtime_minutes")
    private Integer overtimeMinutes;

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("attendance_status")
    private Integer attendanceStatus;  // 1-正常 2-迟到 3-早退 4-旷工 5-异常

    @TableField("remark")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    @Version
    private Integer version;
}
```

### 📅 智能排班管理
```java
// 智能排班管理 (Jakarta EE 3.0+)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;

// Controller层 - REST接口
@RestController
@RequestMapping("/api/v1/attendance/scheduling")
@Tag(name = "智能排班", description = "智能排班和工时管理")
public class SchedulingController {

    @Resource
    private SchedulingService schedulingService;

    /**
     * 生成最优排班
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('SCHEDULE_ADMIN')")
    @RateLimiter(name = "scheduling-generate", fallbackMethod = "generateFallback")
    @ApiOperation(value = "生成排班", notes = "智能生成最优排班方案")
    public ResponseDTO<ScheduleResultDTO> generateOptimalSchedule(
            @Valid @RequestBody SchedulingRequestDTO request) {

        log.info("[智能排班] 开始生成排班, departmentId={}, startDate={}, endDate={}",
                request.getDepartmentId(), request.getStartDate(), request.getEndDate());

        ScheduleResultDTO result = schedulingService.generateOptimalSchedule(request);

        log.info("[智能排班] 排班生成完成, departmentId={}, scheduleCount={}, conflictsResolved={}",
                request.getDepartmentId(), result.getScheduleCount(), result.getConflictsResolved());

        return ResponseDTO.ok(result);
    }

    /**
     * 排班冲突检测
     */
    @PostMapping("/conflict-detect")
    @PreAuthorize("hasRole('SCHEDULE_ADMIN')")
    @ApiOperation(value = "冲突检测", notes = "检测排班冲突")
    public ResponseDTO<List<ScheduleConflictDTO>> detectConflicts(
            @Valid @RequestBody ConflictDetectionRequestDTO request) {

        List<ScheduleConflictDTO> conflicts = schedulingService.detectScheduleConflicts(request);

        return ResponseDTO.ok(conflicts);
    }

    // 服务降级处理
    public ResponseDTO<ScheduleResultDTO> generateFallback(SchedulingRequestDTO request, Exception ex) {
        log.error("[智能排班] 服务降级, departmentId={}", request.getDepartmentId(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }
}

// Service层 - 业务逻辑实现
@Service
@Transactional(rollbackFor = Exception.class)
public class SchedulingServiceImpl implements SchedulingService {

    @Resource
    private SchedulingManager schedulingManager;

    @Override
    public ScheduleResultDTO generateOptimalSchedule(SchedulingRequestDTO request) {
        try {
            // 参数验证
            validateSchedulingRequest(request);

            // 委托给Manager层处理复杂业务逻辑
            ScheduleResult result = schedulingManager.generateOptimalSchedule(request);

            return convertToDTO(result);
        } catch (BusinessException e) {
            log.warn("[智能排班] 业务异常, departmentId={}, error={}", request.getDepartmentId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[智能排班] 系统异常, departmentId={}", request.getDepartmentId(), e);
            throw new BusinessException("SCHEDULING_ERROR", "排班生成失败");
        }
    }

    @Override
    public List<ScheduleConflictDTO> detectScheduleConflicts(ConflictDetectionRequestDTO request) {
        try {
            List<ScheduleConflict> conflicts = schedulingManager.detectScheduleConflicts(request);
            return conflicts.stream()
                    .map(this::convertConflictToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[冲突检测] 检测失败", e);
            throw new BusinessException("CONFLICT_DETECTION_ERROR", "排班冲突检测失败");
        }
    }

    private void validateSchedulingRequest(SchedulingRequestDTO request) {
        if (request.getDepartmentId() == null) {
            throw new BusinessException("DEPARTMENT_ID_REQUIRED", "部门ID不能为空");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("DATE_RANGE_REQUIRED", "开始时间和结束时间不能为空");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("INVALID_DATE_RANGE", "开始时间不能晚于结束时间");
        }
    }
}

// Manager层 - 复杂业务流程编排
public class SchedulingManagerImpl implements SchedulingManager {

    private final BusinessRulesAnalyzer businessRulesAnalyzer;
    private final EmployeeMatchingService employeeMatchingService;
    private final SchedulingOptimizer schedulingOptimizer;
    private final ConflictResolver conflictResolver;
    private final WorkShiftDao workShiftDao;
    private final EmployeeDao employeeDao;
    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public SchedulingManagerImpl(
            BusinessRulesAnalyzer businessRulesAnalyzer,
            EmployeeMatchingService employeeMatchingService,
            SchedulingOptimizer schedulingOptimizer,
            ConflictResolver conflictResolver,
            WorkShiftDao workShiftDao,
            EmployeeDao employeeDao,
            GatewayServiceClient gatewayServiceClient) {
        this.businessRulesAnalyzer = businessRulesAnalyzer;
        this.employeeMatchingService = employeeMatchingService;
        this.schedulingOptimizer = schedulingOptimizer;
        this.conflictResolver = conflictResolver;
        this.workShiftDao = workShiftDao;
        this.employeeDao = employeeDao;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ScheduleResult generateOptimalSchedule(SchedulingRequestDTO request) {
        // 1. 业务规则分析
        BusinessRules rules = analyzeBusinessRules(request.getDepartmentId());

        // 2. 员工技能和可用性匹配
        List<Employee> availableEmployees = findAvailableEmployees(request);

        // 3. 智能排班算法
        Schedule schedule = optimizeScheduling(availableEmployees, rules, request);

        // 4. 冲突检测和解决
        ConflictResolutionResult conflictResult = resolveScheduleConflicts(schedule);

        // 5. 排班结果持久化
        saveScheduleResult(schedule, conflictResult);

        // 6. 发送排班通知
        sendScheduleNotification(schedule);

        return buildScheduleResult(schedule, conflictResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ScheduleConflict> detectScheduleConflicts(ConflictDetectionRequestDTO request) {
        // 获取待检测的排班数据
        List<WorkShiftEntity> workShifts = getWorkShiftsForConflictDetection(request);

        List<ScheduleConflict> conflicts = new ArrayList<>();

        // 1. 员工时间冲突检测
        conflicts.addAll(detectEmployeeTimeConflicts(workShifts));

        // 2. 技能匹配冲突检测
        conflicts.addAll(detectSkillMatchingConflicts(workShifts));

        // 3. 工时合规冲突检测
        conflicts.addAll(detectWorkHourComplianceConflicts(workShifts));

        // 4. 资源分配冲突检测
        conflicts.addAll(detectResourceAllocationConflicts(workShifts));

        return conflicts;
    }

    private BusinessRules analyzeBusinessRules(Long departmentId) {
        // 通过网关调用OA服务获取部门业务规则
        ResponseDTO<DepartmentRulesDTO> result = gatewayServiceClient.callOAService(
            "/api/v1/department/" + departmentId + "/scheduling-rules",
            HttpMethod.GET,
            null,
            DepartmentRulesDTO.class
        );

        if (result.getCode() == 200) {
            return businessRulesAnalyzer.analyzeRules(result.getData());
        }

        throw new BusinessException("DEPARTMENT_RULES_NOT_FOUND", "部门排班规则未找到");
    }

    private List<Employee> findAvailableEmployees(SchedulingRequestDTO request) {
        // 通过网关调用公共服务获取员工信息
        ResponseDTO<List<EmployeeDTO>> result = gatewayServiceClient.callCommonService(
            "/api/v1/employee/available-for-scheduling",
            HttpMethod.POST,
            Map.of(
                "departmentId", request.getDepartmentId(),
                "startDate", request.getStartDate(),
                "endDate", request.getEndDate(),
                "requiredSkills", request.getRequiredSkills()
            ),
            new ParameterizedTypeReference<ResponseDTO<List<EmployeeDTO>>>() {}
        );

        if (result.getCode() == 200) {
            return employeeMatchingService.matchEmployees(result.getData(), request.getRequiredSkills());
        }

        throw new BusinessException("EMPLOYEE_QUERY_FAILED", "员工信息查询失败");
    }

    private Schedule optimizeScheduling(List<Employee> employees, BusinessRules rules, SchedulingRequestDTO request) {
        SchedulingOptimizationRequest optimizationRequest = SchedulingOptimizationRequest.builder()
            .employees(employees)
            .businessRules(rules)
            .timeRange(TimeRange.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build())
            .constraints(request.getConstraints())
            .build();

        return schedulingOptimizer.optimize(optimizationRequest);
    }

    private ConflictResolutionResult resolveScheduleConflicts(Schedule schedule) {
        List<ScheduleConflict> conflicts = detectScheduleConflicts(
            ConflictDetectionRequestDTO.builder()
                .workShiftIds(schedule.getWorkShiftIds())
                .build()
        );

        if (conflicts.isEmpty()) {
            return ConflictResolutionResult.success();
        }

        return conflictResolver.resolveConflicts(schedule, conflicts);
    }

    private void saveScheduleResult(Schedule schedule, ConflictResolutionResult conflictResult) {
        // 批量保存班次信息
        List<WorkShiftEntity> workShifts = schedule.getWorkShifts();
        if (!workShifts.isEmpty()) {
            workShiftDao.insertBatch(workShifts);
        }

        // 保存冲突解决记录
        if (!conflictResult.getResolvedConflicts().isEmpty()) {
            workShiftDao.insertConflictResolutionBatch(conflictResult.getResolvedConflicts());
        }
    }

    private void sendScheduleNotification(Schedule schedule) {
        // 异步发送排班通知
        CompletableFuture.runAsync(() -> {
            try {
                // 发送排班通知给相关员工
                for (Employee employee : schedule.getAssignedEmployees()) {
                    sendScheduleNotificationToEmployee(employee, schedule);
                }
            } catch (Exception e) {
                log.error("[排班通知] 发送失败, scheduleId={}", schedule.getId(), e);
            }
        });
    }
}

// DAO层 - 数据访问
@Mapper
public interface WorkShiftDao extends BaseMapper<WorkShiftEntity> {

    @Transactional(readOnly = true)
    List<WorkShiftEntity> selectByDepartmentIdAndDateRange(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Transactional(readOnly = true)
    List<WorkShiftEntity> selectByEmployeeIdAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Transactional(rollbackFor = Exception.class)
    int insertBatch(@Param("workShifts") List<WorkShiftEntity> workShifts);

    @Transactional(rollbackFor = Exception.class)
    int insertConflictResolutionBatch(@Param("resolutions") List<ConflictResolutionEntity> resolutions);

    @Transactional(readOnly = true)
    List<ConflictStatisticsEntity> selectConflictStatistics(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

// 实体类 - 工作班次
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_work_shift")
public class WorkShiftEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String shiftId;

    @TableField("shift_name")
    private String shiftName;

    @TableField("department_id")
    private Long departmentId;

    @TableField("employee_id")
    private Long employeeId;

    @TableField("shift_date")
    private LocalDate shiftDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("break_duration")
    private Integer breakDuration;  // 休息时长(分钟)

    @TableField("work_hours")
    private BigDecimal workHours;

    @TableField("shift_type")
    private Integer shiftType;  // 1-正常班 2-夜班 3-加班 4-临时

    @TableField("skill_requirements")
    private String skillRequirements;  // 技能要求(JSON格式)

    @TableField("priority_level")
    private Integer priorityLevel;  // 优先级

    @TableField("auto_generated")
    private Boolean autoGenerated;  // 是否自动生成

    @TableField("conflict_resolved")
    private Boolean conflictResolved;  // 冲突是否已解决

    @TableField("status")
    private Integer status;  // 1-正常 2-已取消 3-已修改

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    @Version
    private Integer version;
}
```

### 📊 统计分析报表
```java
@Service
public class AttendanceReportService {

    public AttendanceStatistics generateStatistics(ReportRequest request) {
        // 1. 数据聚合计算
        Map<String, Object> rawData = aggregateAttendanceData(request);

        // 2. 多维度分析
        MultiDimensionAnalysis analysis = performMultiDimensionAnalysis(rawData);

        // 3. 趋势分析
        TrendAnalysis trends = calculateTrends(rawData, request.getTimeRange());

        // 4. 合规性检查
        ComplianceCheck compliance = checkLaborLawCompliance(rawData);

        return buildComprehensiveReport(analysis, trends, compliance);
    }
}
```

---

## 🔧 技术栈和工具

### 核心技术
- **Spring Boot 3.x**: 微服务框架
- **Spring Batch**: 批处理框架（考勤数据批量处理）
- **Quartz**: 定时任务调度
- **Redis**: 缓存和分布式锁
- **Elasticsearch**: 大数据搜索和分析

### 数据处理
- **Apache Kafka**: 考勤事件流处理
- **Apache Flink**: 实时流计算
- **Apache Spark**: 大数据批量分析
- **ClickHouse**: 时序数据库（考勤数据存储）

### 算法库
- **时间序列分析**: 考勤趋势分析和预测
- **优化算法**: 排班优化和资源分配
- **机器学习**: 异常检测和模式识别
- **统计计算**: 多维度统计分析和报表

---

## 📊 性能指标

### 响应时间要求
- **考勤打卡处理**: ≤ 1s (95%分位)
- **排班计算**: ≤ 30s (95%分位)
- **报表生成**: ≤ 10s (95%分位)
- **数据导出**: ≤ 60s (95%分位)

### 数据处理能力
- **并发打卡数**: ≥ 50,000/分钟
- **排班算法处理**: ≥ 10,000员工/次
- **报表数据量**: ≥ 1亿条考勤记录
- **实时分析延迟**: ≤ 5s

### 存储和查询
- **考勤记录存储**: 支持至少5年数据
- **查询响应时间**: ≤ 2s (复杂查询)
- **数据压缩率**: ≥ 70%
- **备份恢复时间**: ≤ 2h

---

## 📋 核心业务规则

### 考勤规则引擎
```java
@Component
public class AttendanceRuleEngine {

    public AttendanceRules getRulesForEmployee(Long employeeId) {
        // 1. 获取员工基础信息
        Employee employee = employeeService.getById(employeeId);

        // 2. 获取部门考勤制度
        DepartmentAttendanceConfig config = departmentService.getAttendanceConfig(employee.getDepartmentId());

        // 3. 构建个性化规则
        return AttendanceRules.builder()
                .workSchedule(config.getWorkSchedule())
                .flexibleRules(config.getFlexibleRules())
                .overtimeRules(config.getOvertimeRules())
                .leaveRules(config.getLeaveRules())
                .build();
    }

    public boolean validateAttendance(AttendanceRecord record, AttendanceRules rules) {
        // 1. 工作时间验证
        if (!isWithinWorkHours(record.getCheckTime(), rules.getWorkSchedule())) {
            return false;
        }

        // 2. 地理位置验证
        if (rules.isLocationRestrictionEnabled()) {
            if (!isValidLocation(record.getLocation(), rules.getAllowedLocations())) {
                return false;
            }
        }

        // 3. 设备有效性验证
        if (!isValidDevice(record.getDeviceId(), rules.getAllowedDevices())) {
            return false;
        }

        return true;
    }
}
```

### 异常检测算法
```java
@Component
public class AnomalyDetector {

    public List<AttendanceAnomaly> detectAnomalies(AttendanceRecord record) {
        List<AttendanceAnomaly> anomalies = new ArrayList<>();

        // 1. 时间异常检测
        if (isTimeAnomaly(record)) {
            anomalies.add(new TimeAnomaly(record));
        }

        // 2. 行为异常检测
        if (isBehaviorAnomaly(record)) {
            anomalies.add(new BehaviorAnomaly(record));
        }

        // 3. 设备异常检测
        if (isDeviceAnomaly(record)) {
            anomalies.add(new DeviceAnomaly(record));
        }

        return anomalies;
    }

    private boolean isTimeAnomaly(AttendanceRecord record) {
        // 使用机器学习模型检测时间异常
        double anomalyScore = timeAnomalyModel.predict(record);
        return anomalyScore > ANOMALY_THRESHOLD;
    }
}
```

---

## 📈 统计分析功能

### 多维度分析
```java
@Service
public class AttendanceAnalysisService {

    public AnalysisResult performMultiDimensionAnalysis(AnalysisRequest request) {
        // 1. 时间维度分析（日、周、月、季、年）
        TimeAnalysis timeAnalysis = analyzeTimeDimension(request);

        // 2. 组织维度分析（部门、团队、个人）
        OrganizationAnalysis orgAnalysis = analyzeOrganizationDimension(request);

        // 3. 业务维度分析（出勤率、加班率、请假率）
        BusinessAnalysis businessAnalysis = analyzeBusinessDimension(request);

        // 4. 趋势维度分析（同比、环比、预测）
        TrendAnalysis trendAnalysis = analyzeTrendDimension(request);

        return AnalysisResult.builder()
                .timeAnalysis(timeAnalysis)
                .organizationAnalysis(orgAnalysis)
                .businessAnalysis(businessAnalysis)
                .trendAnalysis(trendAnalysis)
                .build();
    }
}
```

### 可视化报表
```java
@RestController
@RequestMapping("/api/v1/attendance/reports")
public class AttendanceReportController {

    @PostMapping("/dashboard")
    public ResponseDTO<DashboardData> generateDashboard(@Valid @RequestBody DashboardRequest request) {
        DashboardData dashboard = reportService.generateDashboard(request);
        return ResponseDTO.ok(dashboard);
    }

    @PostMapping("/export")
    public ResponseDTO<byte[]> exportReport(@Valid @RequestBody ExportRequest request) {
        byte[] reportData = reportService.exportReport(request);
        return ResponseDTO.ok(reportData);
    }
}
```

---

## 🛡️ 数据安全和合规

### 数据隐私保护
```java
@Entity
public class AttendanceRecord {

    @Convert(converter = EncryptedStringConverter.class)
    private String deviceId;        // 设备ID加密

    @Column(columnDefinition = "POINT")
    private Point location;          // 地理位置脱敏存储

    @Convert(converter = EncryptedStringConverter.class)
    private String employeePhoto;     // 员工照片加密
}

// API数据脱敏
@RestController
public class AttendanceController {

    @GetMapping("/records")
    public ResponseDTO<List<AttendanceRecordDTO>> getRecords(AttendanceQueryRequest request) {
        List<AttendanceRecord> records = attendanceService.queryRecords(request);

        // 数据脱敏处理
        List<AttendanceRecordDTO> dtoRecords = records.stream()
                .map(this::maskSensitiveData)
                .collect(Collectors.toList());

        return ResponseDTO.ok(dtoRecords);
    }

    private AttendanceRecordDTO maskSensitiveData(AttendanceRecord record) {
        AttendanceRecordDTO dto = new AttendanceRecordDTO(record);
        dto.setDeviceId(maskDeviceId(record.getDeviceId()));
        dto.setLocation(maskLocation(record.getLocation()));
        return dto;
    }
}
```

### 合规性检查
```java
@Service
public class ComplianceCheckService {

    public ComplianceReport checkLaborLawCompliance(ComplianceCheckRequest request) {
        // 1. 工时合规检查
        WorkHourCompliance workHourCompliance = checkWorkHourCompliance(request);

        // 2. 加班时间合规检查
        OvertimeCompliance overtimeCompliance = checkOvertimeCompliance(request);

        // 3. 休假权益合规检查
        LeaveCompliance leaveCompliance = checkLeaveCompliance(request);

        return ComplianceReport.builder()
                .workHourCompliance(workHourCompliance)
                .overtimeCompliance(overtimeCompliance)
                .leaveCompliance(leaveCompliance)
                .overallScore(calculateOverallComplianceScore(workHourCompliance, overtimeCompliance, leaveCompliance))
                .build();
    }
}
```

---

## 📋 开发检查清单

### 功能开发检查
- [ ] 考勤规则引擎实现
- [ ] 多设备数据融合
- [ ] 智能排班算法
- [ ] 异常检测系统
- [ ] 统计分析报表

### 性能检查
- [ ] 大数据量处理优化
- [ ] 实时计算性能
- [ ] 数据库索引优化
- [ ] 缓存策略实现
- [ ] 并发处理能力

### 合规性检查
- [ ] 数据脱敏实现
- [ ] 隐私保护措施
- [ ] 劳动法规合规
- [ ] 审计日志记录
- [ ] 数据加密存储

---

## 🔗 相关技能文档

- **scheduling-algorithm-specialist**: 排班算法专家
- **data-processing-specialist**: 数据处理专家
- **security-protection-specialist**: 安全防护专家
- **performance-optimization-specialist**: 性能优化专家
- **compliance-check-specialist**: 合规检查专家

---

## 📞 联系和支持

**技能负责人**: 考勤服务开发团队
**技术支持**: 架构师团队 + 合规团队
**问题反馈**: 通过项目管理系统提交

**版本信息**:
- **创建时间**: 2025-12-02
- **最后更新**: 2025-12-02
- **版本**: v1.0.0

---

**💡 重要提醒**: 本技能专注于考勤管理的核心业务，需要结合排班算法、数据处理、性能优化等相关技能一起使用，确保系统的准确性和高性能。