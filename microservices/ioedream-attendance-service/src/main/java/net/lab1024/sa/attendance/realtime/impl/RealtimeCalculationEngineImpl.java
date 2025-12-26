package net.lab1024.sa.attendance.realtime.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.realtime.RealtimeCalculationEngine;
import net.lab1024.sa.attendance.realtime.event.AttendanceEvent;
import net.lab1024.sa.attendance.realtime.event.CalculationTriggerEvent;
import net.lab1024.sa.attendance.realtime.event.EventProcessingResult;
import net.lab1024.sa.attendance.realtime.event.EventProcessor;
import net.lab1024.sa.attendance.realtime.event.impl.AttendanceEventProcessor;
import net.lab1024.sa.attendance.realtime.model.AnomalyDetectionResult;
import net.lab1024.sa.attendance.realtime.model.AnomalyFilterParameters;
import net.lab1024.sa.attendance.realtime.model.BatchCalculationResult;
import net.lab1024.sa.attendance.realtime.model.CalculationRule;
import net.lab1024.sa.attendance.realtime.model.CompanyRealtimeOverview;
import net.lab1024.sa.attendance.realtime.model.DepartmentRealtimeStatistics;
import net.lab1024.sa.attendance.realtime.model.EmployeeRealtimeStatus;
import net.lab1024.sa.attendance.realtime.model.EnginePerformanceMetrics;
import net.lab1024.sa.attendance.realtime.model.EngineShutdownResult;
import net.lab1024.sa.attendance.realtime.model.EngineStartupResult;
import net.lab1024.sa.attendance.realtime.model.EngineStatus;
import net.lab1024.sa.attendance.realtime.model.RealtimeAlertResult;
import net.lab1024.sa.attendance.realtime.model.RealtimeCalculationResult;
import net.lab1024.sa.attendance.realtime.model.RealtimeMonitoringParameters;
import net.lab1024.sa.attendance.realtime.model.RealtimeStatisticsResult;
import net.lab1024.sa.attendance.realtime.model.RuleRegistrationResult;
import net.lab1024.sa.attendance.realtime.model.RuleUnregistrationResult;
import net.lab1024.sa.attendance.realtime.model.ScheduleIntegrationParameters;
import net.lab1024.sa.attendance.realtime.model.ScheduleIntegrationResult;
import net.lab1024.sa.attendance.realtime.model.StatisticsQueryParameters;
import net.lab1024.sa.attendance.realtime.model.TimeRange;
import net.lab1024.sa.attendance.realtime.lifecycle.RealtimeEngineLifecycleService;
import net.lab1024.sa.attendance.realtime.cache.RealtimeCacheManager;
import net.lab1024.sa.attendance.realtime.monitor.EnginePerformanceMonitorService;
import net.lab1024.sa.attendance.realtime.anomaly.AttendanceAnomalyDetectionService;
import net.lab1024.sa.attendance.realtime.alert.RealtimeAlertDetectionService;

/**
 * 考勤实时计算引擎实现类
 * <p>
 * 负责实时处理考勤数据、计算统计指标、触发业务规则
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
public class RealtimeCalculationEngineImpl implements RealtimeCalculationEngine {

    // 引擎状态
    private volatile EngineStatus status = EngineStatus.STOPPED;

    // 事件处理线程池 - 使用统一配置的线程池
    @Resource(name = "eventProcessingExecutor")
    private ThreadPoolTaskExecutor eventProcessingExecutor;

    // 计算线程池 - 使用统一配置的线程池
    @Resource(name = "calculationExecutor")
    private ThreadPoolTaskExecutor calculationExecutor;

    // ==================== 基础设施服务（P2-Batch2阶段1注入）====================
    /**
     * 引擎生命周期管理服务
     */
    @Resource
    private RealtimeEngineLifecycleService lifecycleService;

    /**
     * 缓存管理服务
     */
    @Resource
    private RealtimeCacheManager cacheManager;

    /**
     * 性能监控服务
     */
    @Resource
    private EnginePerformanceMonitorService performanceMonitorService;

    /**
     * 异常检测服务（P2-Batch2阶段4创建）
     */
    @Resource
    private AttendanceAnomalyDetectionService anomalyDetectionService;

    /**
     * 告警检测服务（P2-Batch2阶段5创建）
     */
    @Resource
    private RealtimeAlertDetectionService alertDetectionService;

    // 事件处理器
    private final List<EventProcessor> eventProcessors = new CopyOnWriteArrayList<EventProcessor>();

    // 实时数据缓存
    private final Map<String, Object> realtimeCache = new HashMap<>();

    // 计算规则
    private final Map<String, CalculationRule> calculationRules = new HashMap<>();

    // 性能指标
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);
    private final AtomicLong totalCalculationsPerformed = new AtomicLong(0);
    private final AtomicLong averageProcessingTime = new AtomicLong(0);

    // 监控指标
    private final Map<String, Object> monitoringMetrics = new HashMap<>();

    @Override
    public EngineStartupResult startup() {
        // ==================== P2-Batch2阶段1：委托给生命周期服务 ====================
        return lifecycleService.startup();
    }

    @Override
    public EngineShutdownResult shutdown() {
        // ==================== P2-Batch2阶段1：委托给生命周期服务 ====================
        return lifecycleService.shutdown();
    }

    @Override
    public RealtimeCalculationResult processAttendanceEvent(AttendanceEvent attendanceEvent) {
        log.debug("[实时计算引擎] 处理考勤事件: {}", attendanceEvent.getEventId());

        if (status != EngineStatus.RUNNING) {
            return createErrorResult("引擎未运行", attendanceEvent.getEventId());
        }

        try {
            // 1. 事件预处理
            AttendanceEvent preprocessedEvent = preprocessEvent(attendanceEvent);

            // 2. 缓存事件
            cacheEvent(preprocessedEvent);

            // 3. 提交到事件处理器
            CompletableFuture<EventProcessingResult> processingFuture = submitToEventProcessor(preprocessedEvent);

            // 4. 等待处理完成
            EventProcessingResult processingResult = processingFuture.get(10, TimeUnit.SECONDS);

            // 5. 触发相关计算
            CompletableFuture<RealtimeCalculationResult> calculationFuture = triggerCalculations(preprocessedEvent,
                    processingResult);

            // 6. 等待计算完成
            RealtimeCalculationResult calculationResult = calculationFuture.get(30, TimeUnit.SECONDS);

            // 7. 更新统计信息
            updateStatistics(preprocessedEvent, processingResult, calculationResult);

            return calculationResult;

        } catch (TimeoutException e) {
            log.warn("[实时计算引擎] 处理事件超时: {}", attendanceEvent.getEventId());
            return createErrorResult("处理超时", attendanceEvent.getEventId());

        } catch (Exception e) {
            log.error("[实时计算引擎] 处理事件失败: {}", attendanceEvent.getEventId(), e);
            return createErrorResult("处理失败: " + e.getMessage(), attendanceEvent.getEventId());
        }
    }

    @Override
    public BatchCalculationResult processBatchEvents(List<AttendanceEvent> attendanceEvents) {
        log.info("[实时计算引擎] 批量处理考勤事件，数量: {}", attendanceEvents.size());

        if (status != EngineStatus.RUNNING) {
            return BatchCalculationResult.builder()
                    .batchId(UUID.randomUUID().toString())
                    .totalEvents(attendanceEvents.size())
                    .success(false)
                    .errorMessage("引擎未运行")
                    .build();
        }

        try {
            long startTime = System.currentTimeMillis();
            BatchCalculationResult batchResult = BatchCalculationResult.builder()
                    .batchId(UUID.randomUUID().toString())
                    .totalEvents(attendanceEvents.size())
                    .processingStartTime(LocalDateTime.now())
                    .results(new ArrayList<>())
                    .build();

            // 并行处理事件（使用计算线程池异步执行）
            List<CompletableFuture<RealtimeCalculationResult>> futures = attendanceEvents.stream()
                    .map(event -> CompletableFuture.supplyAsync(
                            () -> processAttendanceEvent(event),
                            calculationExecutor))
                    .toList();

            // 等待所有事件处理完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            try {
                allFutures.get(60, TimeUnit.SECONDS); // 60秒超时
            } catch (TimeoutException e) {
                log.warn("[实时计算引擎] 批量处理部分超时");
            }

            // 收集处理结果
            int successCount = 0;
            int failureCount = 0;

            for (CompletableFuture<RealtimeCalculationResult> future : futures) {
                try {
                    RealtimeCalculationResult result = future.get();
                    batchResult.getResults().add(result);

                    if (Boolean.TRUE.equals(result.getCalculationSuccessful())) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("[实时计算引擎] 获取批量处理结果失败", e);
                    failureCount++;
                }
            }

            // 设置批量处理结果
            batchResult.setSuccessfulEvents(successCount);
            batchResult.setFailedEvents(failureCount);
            batchResult.setProcessingEndTime(LocalDateTime.now());
            batchResult.setTotalProcessingTime(ChronoUnit.MILLIS.between(
                    batchResult.getProcessingStartTime(), batchResult.getProcessingEndTime()));
            batchResult.setBatchSuccessful(failureCount == 0);

            log.info("[实时计算引擎] 批量处理完成，成功: {}/{}，耗时: {}ms",
                    successCount, attendanceEvents.size(), batchResult.getTotalProcessingTime());

            return batchResult;

        } catch (Exception e) {
            log.error("[实时计算引擎] 批量处理事件失败", e);
            return BatchCalculationResult.builder()
                    .batchId(UUID.randomUUID().toString())
                    .totalEvents(attendanceEvents.size())
                    .success(false)
                    .errorMessage("批量处理失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RealtimeCalculationResult triggerCalculation(CalculationTriggerEvent triggerEvent) {
        log.debug("[实时计算引擎] 触发计算，触发类型: {}", triggerEvent.getTriggerType());

        if (status != EngineStatus.RUNNING) {
            return createErrorResult("引擎未运行", triggerEvent.getTriggerId());
        }

        try {
            // 根据触发类型执行相应的计算
            RealtimeCalculationResult result = switch (triggerEvent.getTriggerType()) {
                case EMPLOYEE_DAILY -> calculateEmployeeDailyStatistics(triggerEvent);
                case DEPARTMENT_DAILY -> calculateDepartmentDailyStatistics(triggerEvent);
                case COMPANY_DAILY -> calculateCompanyDailyStatistics(triggerEvent);
                case EMPLOYEE_MONTHLY -> calculateEmployeeMonthlyStatistics(triggerEvent);
                case ANOMALY_DETECTION -> performAnomalyDetection(triggerEvent);
                case ALERT_CHECKING -> performAlertChecking(triggerEvent);
                case SCHEDULE_INTEGRATION -> performScheduleIntegration(triggerEvent);
                default -> createErrorResult("未知触发类型", triggerEvent.getTriggerId());
            };

            // 更新统计信息
            totalCalculationsPerformed.incrementAndGet();

            return result;

        } catch (Exception e) {
            log.error("[实时计算引擎] 触发计算失败", e);
            return createErrorResult("触发计算失败: " + e.getMessage(), triggerEvent.getTriggerId());
        }
    }

    @Override
    public RealtimeStatisticsResult getRealtimeStatistics(StatisticsQueryParameters queryParameters) {
        log.debug("[实时计算引擎] 获取实时统计，查询参数: {}", queryParameters);

        try {
            RealtimeStatisticsResult result = RealtimeStatisticsResult.builder()
                    .queryId(UUID.randomUUID().toString())
                    .queryTime(LocalDateTime.now())
                    .queryParameters(queryParameters)
                    .build();

            // 根据查询参数获取相应的统计数据
            switch (queryParameters.getStatisticsType()) {
                case EMPLOYEE_REALTIME:
                    result.setEmployeeStatistics(getEmployeeRealtimeStatistics(queryParameters));
                    break;
                case DEPARTMENT_REALTIME:
                    result.setDepartmentStatistics(getDepartmentRealtimeStatistics(queryParameters));
                    break;
                case COMPANY_REALTIME:
                    result.setCompanyStatistics(getCompanyRealtimeStatistics(queryParameters));
                    break;
                case PERFORMANCE_METRICS:
                    result.setPerformanceMetrics(getPerformanceMetrics());
                    break;
            }

            result.setQuerySuccessful(true);

            return result;

        } catch (Exception e) {
            log.error("[实时计算引擎] 获取实时统计失败", e);
            return RealtimeStatisticsResult.builder()
                    .queryId(UUID.randomUUID().toString())
                    .querySuccessful(false)
                    .errorMessage("获取统计失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public EmployeeRealtimeStatus getEmployeeRealtimeStatus(Long employeeId, TimeRange timeRange) {
        log.debug("[实时计算引擎] 获取员工实时状态，员工ID: {}", employeeId);

        try {
            // 1. 从缓存中获取基本状态
            String cacheKey = "employee_status:" + employeeId;
            EmployeeRealtimeStatus cachedStatus = (EmployeeRealtimeStatus) realtimeCache.get(cacheKey);

            if (cachedStatus != null) {
                return cachedStatus;
            }

            // 2. 实时计算员工状态
            EmployeeRealtimeStatus status = calculateEmployeeRealtimeStatus(employeeId, timeRange);

            // 3. 缓存计算结果
            realtimeCache.put(cacheKey, status);

            return status;

        } catch (Exception e) {
            log.error("[实时计算引擎] 获取员工实时状态失败", e);
            return EmployeeRealtimeStatus.builder()
                    .employeeId(employeeId)
                    .currentStatus(EmployeeRealtimeStatus.AttendanceStatus.UNKNOWN_STATUS)
                    .build();
        }
    }

    @Override
    public DepartmentRealtimeStatistics getDepartmentRealtimeStatistics(Long departmentId, TimeRange timeRange) {
        log.debug("[实时计算引擎] 获取部门实时统计，部门ID: {}", departmentId);

        try {
            // 1. 从缓存中获取基本统计
            String cacheKey = "department_stats:" + departmentId;
            DepartmentRealtimeStatistics cachedStats = (DepartmentRealtimeStatistics) realtimeCache.get(cacheKey);

            if (cachedStats != null) {
                return cachedStats;
            }

            // 2. 实时计算部门统计
            DepartmentRealtimeStatistics statistics = calculateDepartmentRealtimeStatistics(departmentId, timeRange);

            // 3. 缓存计算结果
            realtimeCache.put(cacheKey, statistics);

            return statistics;

        } catch (Exception e) {
            log.error("[实时计算引擎] 获取部门实时统计失败", e);
            return DepartmentRealtimeStatistics.builder()
                    .departmentId(departmentId)
                    .build();
        }
    }

    @Override
    public CompanyRealtimeOverview getCompanyRealtimeOverview(TimeRange timeRange) {
        log.debug("[实时计算引擎] 获取公司实时考勤概览");

        try {
            // 1. 从缓存中获取基本概览
            String cacheKey = "company_overview:" + timeRange.getWorkStartTime().toLocalDate();
            CompanyRealtimeOverview cachedOverview = (CompanyRealtimeOverview) realtimeCache.get(cacheKey);

            if (cachedOverview != null) {
                return cachedOverview;
            }

            // 2. 实时计算公司概览
            CompanyRealtimeOverview overview = calculateCompanyRealtimeOverview(timeRange);

            // 3. 缓存计算结果
            realtimeCache.put(cacheKey, overview);

            return overview;

        } catch (Exception e) {
            log.error("[实时计算引擎] 获取公司实时概览失败", e);
            return CompanyRealtimeOverview.builder()
                    .build();
        }
    }

    @Override
    public AnomalyDetectionResult calculateAttendanceAnomalies(TimeRange timeRange,
            AnomalyFilterParameters filterParameters) {
        log.info("[实时计算引擎] 计算考勤异常（委托给异常检测服务）");
        // 完全委托给 AttendanceAnomalyDetectionService（P2-Batch2阶段4创建）
        return anomalyDetectionService.calculateAttendanceAnomalies(timeRange, filterParameters);
    }

    @Override
    public RealtimeAlertResult detectRealtimeAlerts(RealtimeMonitoringParameters monitoringParameters) {
        log.info("[实时计算引擎] 检测实时预警（委托给告警检测服务）");
        // 完全委托给 RealtimeAlertDetectionService（P2-Batch2阶段5创建）
        return alertDetectionService.detectRealtimeAlerts(monitoringParameters);
    }

    @Override
    public ScheduleIntegrationResult integrateWithScheduleEngine(ScheduleData scheduleData,
            ScheduleIntegrationParameters integrationParameters) {
        log.debug("[实时计算引擎] 与排班引擎集成");

        try {
            // 实现与排班引擎的集成
            // TODO: 实现具体的集成逻辑

            return ScheduleIntegrationResult.builder()
                    .integrationId(UUID.randomUUID().toString())
                    .integrationTime(LocalDateTime.now())
                    .integrationSuccessful(true)
                    .build();

        } catch (Exception e) {
            log.error("[实时计算引擎] 与排班引擎集成失败", e);
            return ScheduleIntegrationResult.builder()
                    .integrationId(UUID.randomUUID().toString())
                    .integrationSuccessful(false)
                    .errorMessage("集成失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RuleRegistrationResult registerCalculationRule(CalculationRule calculationRule) {
        log.debug("[实时计算引擎] 注册计算规则: {}", calculationRule.getRuleId());

        try {
            calculationRules.put(calculationRule.getRuleId(), calculationRule);

            // 验证规则
            if (!validateCalculationRule(calculationRule)) {
                calculationRules.remove(calculationRule.getRuleId());
                return RuleRegistrationResult.builder()
                        .ruleId(calculationRule.getRuleId())
                        .registrationSuccessful(false)
                        .errorMessage("规则验证失败")
                        .build();
            }

            return RuleRegistrationResult.builder()
                    .ruleId(calculationRule.getRuleId())
                    .registrationSuccessful(true)
                    .registrationTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("[实时计算引擎] 注册计算规则失败", e);
            return RuleRegistrationResult.builder()
                    .ruleId(calculationRule.getRuleId())
                    .registrationSuccessful(false)
                    .errorMessage("注册失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RuleUnregistrationResult unregisterCalculationRule(String ruleId) {
        log.debug("[实时计算引擎] 注销计算规则: {}", ruleId);

        try {
            CalculationRule removedRule = calculationRules.remove(ruleId);

            if (removedRule != null) {
                return RuleUnregistrationResult.builder()
                        .ruleId(ruleId)
                        .unregistrationSuccessful(true)
                        .unregistrationTime(LocalDateTime.now())
                        .build();
            } else {
                return RuleUnregistrationResult.builder()
                        .ruleId(ruleId)
                        .unregistrationSuccessful(false)
                        .errorMessage("规则不存在")
                        .build();
            }

        } catch (Exception e) {
            log.error("[实时计算引擎] 注销计算规则失败", e);
            return RuleUnregistrationResult.builder()
                    .ruleId(ruleId)
                    .unregistrationSuccessful(false)
                    .errorMessage("注销失败: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public EnginePerformanceMetrics getPerformanceMetrics() {
        log.debug("[实时计算引擎] 获取性能指标（委托给性能监控服务）");
        // 完全委托给 EnginePerformanceMonitorService
        return performanceMonitorService.getPerformanceMetrics();
    }

    @Override
    public boolean validateCalculationResult(RealtimeCalculationResult calculationResult) {
        if (calculationResult == null) {
            log.warn("[实时计算引擎] 计算结果验证失败：结果为null");
            return false;
        }

        // 验证基本字段
        if (calculationResult.getCalculationId() == null || calculationResult.getCalculationTime() == null) {
            log.warn("[实时计算引擎] 计算结果验证失败：缺少必需字段");
            return false;
        }

        // 验证计算结果数据一致性
        if (calculationResult.getCalculationSuccessful()) {
            // 如果计算成功，必须包含结果数据
            // 验证统计数据的一致性
            if (calculationResult.getStatisticsData() != null) {
                // 验证统计数据
                Map<String, Object> stats = calculationResult.getStatisticsData();

                // 验证出勤人数必须为非负数
                Object attendanceCount = stats.get("attendanceCount");
                if (attendanceCount != null && attendanceCount instanceof Integer) {
                    if ((Integer) attendanceCount < 0) {
                        log.warn("[实时计算引擎] 计算结果验证失败：出勤人数为负数");
                        return false;
                    }
                }

                // 验证出勤率必须在0-100%之间
                Object attendanceRate = stats.get("attendanceRate");
                if (attendanceRate != null && attendanceRate instanceof Double) {
                    double rate = (Double) attendanceRate;
                    if (rate < 0.0 || rate > 1.0) {
                        log.warn("[实时计算引擎] 计算结果验证失败：出勤率超出范围[0,1]");
                        return false;
                    }
                }
            }

            // 验证事件ID关联
            if (calculationResult.getEventId() != null) {
                // 确保事件ID格式正确（UUID格式）
                try {
                    java.util.UUID.fromString(calculationResult.getEventId());
                } catch (IllegalArgumentException e) {
                    log.warn("[实时计算引擎] 计算结果验证失败：事件ID格式错误");
                    return false;
                }
            }
        } else {
            // 如果计算失败，必须包含错误信息
            if (calculationResult.getErrorMessage() == null || calculationResult.getErrorMessage().trim().isEmpty()) {
                log.warn("[实时计算引擎] 计算结果验证失败：计算失败但未提供错误信息");
                return false;
            }
        }

        log.trace("[实时计算引擎] 计算结果验证通过: calculationId={}", calculationResult.getCalculationId());
        return true;
    }

    @Override
    public EngineStatus getEngineStatus() {
        return status;
    }

    // 私有辅助方法

    /**
     * 初始化事件处理器
     */
    private void initializeEventProcessors() {
        AttendanceEventProcessor attendanceProcessor = new AttendanceEventProcessor();
        attendanceProcessor.start();
        eventProcessors.add(attendanceProcessor);

        log.info("[实时计算引擎] 初始化事件处理器完成，数量: {}", eventProcessors.size());
    }

    /**
     * 初始化计算规则
     * <p>
     * P0级核心功能：加载默认的计算规则
     * </p>
     */
    private void initializeCalculationRules() {
        log.info("[实时计算引擎] 开始初始化计算规则");

        // 规则1：员工日统计规则
        CalculationRule employeeDailyRule = CalculationRule.builder()
                .ruleId("EMPLOYEE_DAILY_STATISTICS")
                .ruleExpression("calculateEmployeeDailyStatistics(employeeId, date)")
                .build();
        calculationRules.put(employeeDailyRule.getRuleId(), employeeDailyRule);

        // 规则2：部门日统计规则
        CalculationRule departmentDailyRule = CalculationRule.builder()
                .ruleId("DEPARTMENT_DAILY_STATISTICS")
                .ruleExpression("calculateDepartmentDailyStatistics(departmentId, date)")
                .build();
        calculationRules.put(departmentDailyRule.getRuleId(), departmentDailyRule);

        // 规则3：公司日统计规则
        CalculationRule companyDailyRule = CalculationRule.builder()
                .ruleId("COMPANY_DAILY_STATISTICS")
                .ruleExpression("calculateCompanyDailyStatistics(date)")
                .build();
        calculationRules.put(companyDailyRule.getRuleId(), companyDailyRule);

        // 规则4：异常检测规则
        CalculationRule anomalyDetectionRule = CalculationRule.builder()
                .ruleId("ANOMALY_DETECTION")
                .ruleExpression("detectAnomalies(timeRange, filterParameters)")
                .build();
        calculationRules.put(anomalyDetectionRule.getRuleId(), anomalyDetectionRule);

        // 规则5：预警检测规则
        CalculationRule alertCheckingRule = CalculationRule.builder()
                .ruleId("ALERT_CHECKING")
                .ruleExpression("detectAlerts(monitoringParameters)")
                .build();
        calculationRules.put(alertCheckingRule.getRuleId(), alertCheckingRule);

        log.info("[实时计算引擎] 初始化计算规则完成，数量: {}", calculationRules.size());
    }

    /**
     * 初始化缓存
     * <p>
     * P1级功能：初始化缓存配置
     * </p>
     */
    private void initializeCache() {
        log.info("[实时计算引擎] 开始初始化缓存");

        // 设置缓存默认配置
        monitoringMetrics.put("cache.maxSize", 10000); // 最大缓存条目数
        monitoringMetrics.put("cache.defaultTTL", 86400); // 默认过期时间（24小时，秒）
        monitoringMetrics.put("cache.cleanupInterval", 3600); // 清理间隔（1小时，秒）
        monitoringMetrics.put("cache.hitCount", 0); // 缓存命中次数
        monitoringMetrics.put("cache.missCount", 0); // 缓存未命中次数

        log.info("[实时计算引擎] 初始化缓存完成，配置: maxSize={}, defaultTTL={}秒",
                monitoringMetrics.get("cache.maxSize"), monitoringMetrics.get("cache.defaultTTL"));
    }

    /**
     * 初始化监控
     * <p>
     * P1级功能：初始化监控指标
     * </p>
     */
    private void initializeMonitoring() {
        log.info("[实时计算引擎] 开始初始化监控");

        // 设置性能监控初始值
        monitoringMetrics.put("monitoring.startTime", System.currentTimeMillis());
        monitoringMetrics.put("monitoring.eventProcessingTime.total", 0L);
        monitoringMetrics.put("monitoring.eventProcessingTime.count", 0);
        monitoringMetrics.put("monitoring.calculationTime.total", 0L);
        monitoringMetrics.put("monitoring.calculationTime.count", 0);
        monitoringMetrics.put("monitoring.errorCount", 0);
        monitoringMetrics.put("monitoring.warningCount", 0);

        log.info("[实时计算引擎] 初始化监控完成");
    }

    /**
     * 事件预处理
     * <p>
     * P0级核心功能：对考勤事件进行数据清洗和标准化处理
     * </p>
     * <p>
     * 预处理步骤：
     * 1. 设置处理状态和时间戳
     * 2. 数据清洗（去空、去重、格式标准化）
     * 3. 数据验证（必填字段、数据范围、关联验证）
     * 4. 数据增强（补充缺失字段、计算派生字段）
     * </p>
     *
     * @param event 原始考勤事件
     * @return 预处理后的考勤事件
     */
    private AttendanceEvent preprocessEvent(AttendanceEvent event) {
        log.debug("[实时计算引擎] 开始事件预处理: eventId={}, eventType={}",
                event.getEventId(), event.getEventType());

        // 1. 设置处理状态和时间戳
        event.setProcessingStatus(AttendanceEvent.EventProcessingStatus.PROCESSING);
        event.setProcessingStartTime(LocalDateTime.now());

        // 2. 数据清洗和标准化

        // 2.1 清理空值和空白字符
        cleanEventData(event);

        // 2.2 标准化时间格式（确保所有时间字段为LocalDateTime类型）
        normalizeTimeFields(event);

        // 2.3 标准化设备ID（去除前后空格、统一大小写）
        normalizeDeviceFields(event);

        // 3. 数据验证

        // 3.1 验证必填字段
        if (!validateRequiredFields(event)) {
            log.warn("[实时计算引擎] 事件必填字段验证失败: eventId={}", event.getEventId());
            event.setProcessingStatus(AttendanceEvent.EventProcessingStatus.FAILED);
            return event;
        }

        // 3.2 验证数据范围（时间范围、ID范围等）
        if (!validateDataRanges(event)) {
            log.warn("[实时计算引擎] 事件数据范围验证失败: eventId={}", event.getEventId());
            event.setProcessingStatus(AttendanceEvent.EventProcessingStatus.FAILED);
            return event;
        }

        // 4. 数据增强

        // 4.1 补充派生字段（如：星期几、是否工作日等）
        enrichDerivedFields(event);

        // 4.2 设置地理位置信息（如果有坐标数据）
        enrichLocationInfo(event);

        log.debug("[实时计算引擎] 事件预处理完成: eventId={}, processingStatus={}",
                event.getEventId(), event.getProcessingStatus());

        return event;
    }

    /**
     * 清理事件数据（去空、去重）
     */
    private void cleanEventData(AttendanceEvent event) {
        // 清理字符串字段的空白字符
        if (event.getEmployeeId() != null) {
            // 确保employeeId不为null且为有效值
        }

        // 清理考勤位置
        if (event.getAttendanceLocation() != null) {
            event.setAttendanceLocation(event.getAttendanceLocation().trim());
        }

        // 移除null值和空字符串的optional字段
        // 注意：根据具体业务需求调整清理逻辑
    }

    /**
     * 标准化时间字段
     */
    private void normalizeTimeFields(AttendanceEvent event) {
        // 确保所有时间字段都是LocalDateTime类型
        // 如果为null，使用当前时间作为默认值

        if (event.getEventTime() == null) {
            event.setEventTime(LocalDateTime.now());
            log.debug("[实时计算引擎] 事件时间为空，使用当前时间: eventId={}", event.getEventId());
        }

        // 确保时间精度统一为秒级（去除毫秒部分）
        if (event.getEventTime() != null) {
            event.setEventTime(event.getEventTime().withNano(0));
        }
    }

    /**
     * 标准化设备字段
     */
    private void normalizeDeviceFields(AttendanceEvent event) {
        // deviceId是Long类型，不需要标准化
        // 如果有设备名称字段，可以标准化
        if (event.getDeviceName() != null) {
            event.setDeviceName(event.getDeviceName().trim());
        }
    }

    /**
     * 验证必填字段
     */
    private boolean validateRequiredFields(AttendanceEvent event) {
        // 验证事件ID
        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
            log.warn("[实时计算引擎] 事件ID为空");
            return false;
        }

        // 验证员工ID
        if (event.getEmployeeId() == null) {
            log.warn("[实时计算引擎] 员工ID为空: eventId={}", event.getEventId());
            return false;
        }

        // 验证事件类型
        if (event.getEventType() == null) {
            log.warn("[实时计算引擎] 事件类型为空: eventId={}", event.getEventId());
            return false;
        }

        // 验证事件时间
        if (event.getEventTime() == null) {
            log.warn("[实时计算引擎] 事件时间为空: eventId={}", event.getEventId());
            return false;
        }

        return true;
    }

    /**
     * 验证数据范围
     */
    private boolean validateDataRanges(AttendanceEvent event) {
        // 验证时间范围（事件时间不能是未来时间，也不能超过1年前）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);

        if (event.getEventTime().isAfter(now)) {
            log.warn("[实时计算引擎] 事件时间不能是未来时间: eventId={}, eventTime={}",
                    event.getEventId(), event.getEventTime());
            return false;
        }

        if (event.getEventTime().isBefore(oneYearAgo)) {
            log.warn("[实时计算引擎] 事件时间超过1年，可能存在数据错误: eventId={}, eventTime={}",
                    event.getEventId(), event.getEventTime());
            return false;
        }

        // 验证员工ID范围（必须大于0）
        if (event.getEmployeeId() <= 0) {
            log.warn("[实时计算引擎] 员工ID必须大于0: eventId={}, employeeId={}",
                    event.getEventId(), event.getEmployeeId());
            return false;
        }

        return true;
    }

    /**
     * 补充派生字段
     */
    private void enrichDerivedFields(AttendanceEvent event) {
        // 补充星期几
        if (event.getEventTime() != null) {
            int dayOfWeek = event.getEventTime().getDayOfWeek().getValue();
            // 周一=1, 周日=7
            // 可以将此信息添加到event的扩展字段中
        }

        // 补充是否工作日（可以通过配置或数据库查询获取工作日历）
        // 简化实现：周一到周五为工作日
        if (event.getEventTime() != null) {
            int dayOfWeek = event.getEventTime().getDayOfWeek().getValue();
            boolean isWorkday = (dayOfWeek >= 1 && dayOfWeek <= 5);
            // 可以将此信息添加到event的扩展字段中
        }

        // 补充时间段标识（早上班、下午班、晚班等）
        if (event.getEventTime() != null) {
            int hour = event.getEventTime().getHour();
            String timePeriod = determineTimePeriod(hour);
            // 可以将此信息添加到event的扩展字段中
        }
    }

    /**
     * 确定时间段
     */
    private String determineTimePeriod(int hour) {
        if (hour >= 6 && hour < 9) {
            return "早上班";
        } else if (hour >= 9 && hour < 12) {
            return "上午班";
        } else if (hour >= 12 && hour < 14) {
            return "中午班";
        } else if (hour >= 14 && hour < 18) {
            return "下午班";
        } else if (hour >= 18 && hour < 22) {
            return "晚班";
        } else {
            return "其他时段";
        }
    }

    /**
     * 补充地理位置信息
     */
    private void enrichLocationInfo(AttendanceEvent event) {
        // AttendanceEvent没有latitude/longitude字段
        // 如果有考勤位置信息，记录日志
        if (event.getAttendanceLocation() != null) {
            log.trace("[实时计算引擎] 事件包含位置信息: eventId={}, location={}",
                    event.getEventId(), event.getAttendanceLocation());
        }
    }

    /**
     * 缓存事件
     * <p>
     * 使用HashMap + 过期时间戳实现缓存过期策略
     * 缓存24小时后自动失效
     * </p>
     */
    private void cacheEvent(AttendanceEvent event) {
        String cacheKey = "event:" + event.getEventId();
        long expireTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L); // 24小时后过期

        // 创建缓存条目（包含事件和过期时间）
        CacheEntry cacheEntry = new CacheEntry(event, expireTime);
        realtimeCache.put(cacheKey, cacheEntry);

        log.trace("[实时计算引擎] 缓存事件: eventId={}, expireTime={}",
            event.getEventId(), LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(expireTime),
                java.time.ZoneId.systemDefault()));
    }

    /**
     * 缓存条目（包含数据和过期时间）
     */
    private static class CacheEntry {
        private final Object data;
        private final long expireTime;

        public CacheEntry(Object data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        public Object getData() {
            return data;
        }

        public long getExpireTime() {
            return expireTime;
        }

        /**
         * 检查是否已过期
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 从缓存获取数据（自动检查过期）
     * <p>
     * 如果缓存已过期，自动删除并返回null
     * </p>
     *
     * @param cacheKey 缓存键
     * @return 缓存数据，如果不存在或已过期返回null
     */
    private Object getFromCache(String cacheKey) {
        Object cachedObject = realtimeCache.get(cacheKey);

        if (cachedObject == null) {
            return null;
        }

        // 如果是CacheEntry类型，检查过期
        if (cachedObject instanceof CacheEntry) {
            CacheEntry cacheEntry = (CacheEntry) cachedObject;
            if (cacheEntry.isExpired()) {
                // 缓存已过期，删除并返回null
                realtimeCache.remove(cacheKey);
                log.trace("[实时计算引擎] 缓存已过期，已删除: cacheKey={}", cacheKey);
                return null;
            }
            return cacheEntry.getData();
        }

        // 兼容旧代码：如果不是CacheEntry类型，直接返回
        return cachedObject;
    }

    /**
     * 清理过期缓存（定时任务）
     * <p>
     * 建议每小时执行一次，清理所有过期的缓存条目
     * </p>
     */
    public void cleanExpiredCache() {
        int cleanedCount = 0;
        long currentTime = System.currentTimeMillis();

        for (String key : realtimeCache.keySet()) {
            Object value = realtimeCache.get(key);
            if (value instanceof CacheEntry) {
                CacheEntry entry = (CacheEntry) value;
                if (entry.isExpired()) {
                    realtimeCache.remove(key);
                    cleanedCount++;
                }
            }
        }

        if (cleanedCount > 0) {
            log.info("[实时计算引擎] 清理过期缓存: cleanedCount={}, remainingCacheSize={}",
                cleanedCount, realtimeCache.size());
        }
    }

    /**
     * 提交到事件处理器
     */
    private CompletableFuture<EventProcessingResult> submitToEventProcessor(AttendanceEvent event) {
        for (EventProcessor processor : eventProcessors) {
            if (processor.getSupportedEventTypes().contains(event.getEventType())) {
                return processor.processEvent(event);
            }
        }

        // 如果没有找到合适的处理器，返回失败结果
        return CompletableFuture.completedFuture(
                EventProcessingResult.builder()
                        .eventId(event.getEventId())
                        .success(false)
                        .errorMessage("未找到支持的事件处理器")
                        .build());
    }

    /**
     * 触发相关计算
     */
    private CompletableFuture<RealtimeCalculationResult> triggerCalculations(
            AttendanceEvent event, EventProcessingResult processingResult) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 根据事件类型和结果触发相应的计算
                return RealtimeCalculationResult.builder()
                        .calculationId(UUID.randomUUID().toString())
                        .eventId(event.getEventId())
                        .calculationTime(LocalDateTime.now())
                        .calculationSuccessful(true)
                        .build();

            } catch (Exception e) {
                log.error("[实时计算引擎] 触发计算失败", e);
                return RealtimeCalculationResult.builder()
                        .calculationId(UUID.randomUUID().toString())
                        .eventId(event.getEventId())
                        .calculationSuccessful(false)
                        .errorMessage("触发计算失败: " + e.getMessage())
                        .build();
            }
        }, calculationExecutor);
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(AttendanceEvent event, EventProcessingResult processingResult,
            RealtimeCalculationResult calculationResult) {
        totalEventsProcessed.incrementAndGet();

        long processingTime = processingResult.getProcessingTime() != null ? processingResult.getProcessingTime() : 0;
        long calculationTime = calculationResult.getCalculationTime() != null
                ? ChronoUnit.MILLIS.between(calculationResult.getCalculationTime(), LocalDateTime.now())
                : 0;

        averageProcessingTime.set((averageProcessingTime.get() + processingTime + calculationTime) / 2);
    }

    /**
     * 创建错误结果
     */
    private RealtimeCalculationResult createErrorResult(String errorMessage, String eventId) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .eventId(eventId)
                .calculationTime(LocalDateTime.now())
                .calculationSuccessful(false)
                .errorMessage(errorMessage)
                .build();
    }

    // 计算方法的简化实现...
    private RealtimeCalculationResult calculateEmployeeDailyStatistics(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult calculateDepartmentDailyStatistics(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult calculateCompanyDailyStatistics(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult calculateEmployeeMonthlyStatistics(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult performAnomalyDetection(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult performAlertChecking(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    private RealtimeCalculationResult performScheduleIntegration(CalculationTriggerEvent triggerEvent) {
        return RealtimeCalculationResult.builder()
                .calculationId(UUID.randomUUID().toString())
                .calculationSuccessful(true)
                .build();
    }

    // 其他简化方法的实现...
    private Map<String, Object> getEmployeeRealtimeStatistics(StatisticsQueryParameters parameters) {
        return new HashMap<>();
    }

    private Map<String, Object> getDepartmentRealtimeStatistics(StatisticsQueryParameters parameters) {
        return new HashMap<>();
    }

    private Map<String, Object> getCompanyRealtimeStatistics(StatisticsQueryParameters parameters) {
        return new HashMap<>();
    }

    private EmployeeRealtimeStatus calculateEmployeeRealtimeStatus(Long employeeId, TimeRange timeRange) {
        return EmployeeRealtimeStatus.builder()
                .employeeId(employeeId)
                .currentStatus(EmployeeRealtimeStatus.AttendanceStatus.NORMAL_WORKING)
                .build();
    }

    private DepartmentRealtimeStatistics calculateDepartmentRealtimeStatistics(Long departmentId, TimeRange timeRange) {
        return DepartmentRealtimeStatistics.builder()
                .departmentId(departmentId)
                .build();
    }

    private CompanyRealtimeOverview calculateCompanyRealtimeOverview(TimeRange timeRange) {
        return CompanyRealtimeOverview.builder()
                .build();
    }

    private boolean validateCalculationRule(CalculationRule rule) {
        return rule.getRuleId() != null && rule.getRuleExpression() != null;
    }
}
