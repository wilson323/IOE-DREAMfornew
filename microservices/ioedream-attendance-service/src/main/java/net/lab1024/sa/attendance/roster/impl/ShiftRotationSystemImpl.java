package net.lab1024.sa.attendance.roster.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.roster.ShiftRotationSystem;
import net.lab1024.sa.attendance.roster.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 轮班系统实现类
 * <p>
 * 实现轮班制度管理的核心功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component("shiftRotationSystem")
public class ShiftRotationSystemImpl implements ShiftRotationSystem {

    /**
     * 轮班制度存储
     */
    private final Map<String, RotationSystemConfig> rotationSystems = new ConcurrentHashMap<>();

    /**
     * 轮班安排存储
     */
    private final Map<String, List<RotationSchedule>> rotationSchedules = new ConcurrentHashMap<>();

    /**
     * 员工轮班索引
     */
    private final Map<Long, List<RotationSchedule>> employeeScheduleIndex = new ConcurrentHashMap<>();

    /**
     * 日期轮班索引
     */
    private final Map<LocalDate, List<RotationSchedule>> dateScheduleIndex = new ConcurrentHashMap<>();

    /**
     * 执行器服务
     */
    private final ExecutorService executorService;

    /**
     * 构造函数
     */
    public ShiftRotationSystemImpl(ExecutorService executorService) {
        this.executorService = executorService;
        initializeDefaultRotationSystems();
    }

    @Override
    public CompletableFuture<RotationSystemCreationResult> createRotationSystem(RotationSystemConfig rotationSystem) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[轮班系统] 创建轮班制度: {}", rotationSystem.getSystemName());

                // 验证配置
                if (!rotationSystem.isValid()) {
                    return RotationSystemCreationResult.builder()
                            .success(false)
                            .errorMessage("轮班制度配置无效")
                            .errorCode("INVALID_CONFIG")
                            .build();
                }

                // 生成系统ID
                String systemId = generateSystemId();
                rotationSystem.setEffectiveDate(LocalDateTime.now());

                // 存储轮班制度
                rotationSystems.put(systemId, rotationSystem);

                // 初始化安排存储
                rotationSchedules.put(systemId, new ArrayList<>());

                log.info("[轮班系统] 轮班制度创建成功: systemId={}, name={}", systemId, rotationSystem.getSystemName());

                return RotationSystemCreationResult.builder()
                        .success(true)
                        .systemId(systemId)
                        .message("轮班制度创建成功")
                        .creationTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 创建轮班制度失败", e);
                return RotationSystemCreationResult.builder()
                        .success(false)
                        .errorMessage("创建轮班制度时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<RotationSystemUpdateResult> updateRotationSystem(String systemId, RotationSystemConfig rotationSystem) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[轮班系统] 更新轮班制度: systemId={}", systemId);

                // 检查制度是否存在
                if (!rotationSystems.containsKey(systemId)) {
                    return RotationSystemUpdateResult.builder()
                            .success(false)
                            .errorMessage("轮班制度不存在")
                            .errorCode("SYSTEM_NOT_FOUND")
                            .build();
                }

                // 验证配置
                if (!rotationSystem.isValid()) {
                    return RotationSystemUpdateResult.builder()
                            .success(false)
                            .errorMessage("轮班制度配置无效")
                            .errorCode("INVALID_CONFIG")
                            .build();
                }

                // 更新制度
                RotationSystemConfig existingSystem = rotationSystems.get(systemId);
                rotationSystem.setUpdatedBy(existingSystem.getUpdatedBy());
                rotationSystem.setUpdateTime(LocalDateTime.now());
                rotationSystems.put(systemId, rotationSystem);

                log.info("[轮班系统] 轮班制度更新成功: systemId={}", systemId);

                return RotationSystemUpdateResult.builder()
                        .success(true)
                        .message("轮班制度更新成功")
                        .updateTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 更新轮班制度失败: systemId={}", systemId, e);
                return RotationSystemUpdateResult.builder()
                        .success(false)
                        .errorMessage("更新轮班制度时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<RotationSystemDeletionResult> deleteRotationSystem(String systemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[轮班系统] 删除轮班制度: systemId={}", systemId);

                // 检查制度是否存在
                if (!rotationSystems.containsKey(systemId)) {
                    return RotationSystemDeletionResult.builder()
                            .success(false)
                            .errorMessage("轮班制度不存在")
                            .errorCode("SYSTEM_NOT_FOUND")
                            .build();
                }

                // 检查是否有已生成的安排
                List<RotationSchedule> schedules = rotationSchedules.get(systemId);
                if (schedules != null && !schedules.isEmpty()) {
                    return RotationSystemDeletionResult.builder()
                            .success(false)
                            .errorMessage("轮班制度已有安排记录，无法删除")
                            .errorCode("HAS_SCHEDULES")
                            .scheduleCount(schedules.size())
                            .build();
                }

                // 删除制度
                rotationSystems.remove(systemId);
                rotationSchedules.remove(systemId);

                log.info("[轮班系统] 轮班制度删除成功: systemId={}", systemId);

                return RotationSystemDeletionResult.builder()
                        .success(true)
                        .message("轮班制度删除成功")
                        .deletionTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 删除轮班制度失败: systemId={}", systemId, e);
                return RotationSystemDeletionResult.builder()
                        .success(false)
                        .errorMessage("删除轮班制度时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<RotationSystemDetail> getRotationSystemDetail(String systemId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                RotationSystemConfig config = rotationSystems.get(systemId);
                if (config == null) {
                    return RotationSystemDetail.builder()
                            .systemId(systemId)
                            .exists(false)
                            .build();
                }

                List<RotationSchedule> schedules = rotationSchedules.getOrDefault(systemId, Collections.emptyList());

                return RotationSystemDetail.builder()
                        .systemId(systemId)
                        .exists(true)
                        .config(config)
                        .scheduleCount(schedules.size())
                        .lastUpdated(config.getUpdateTime())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 获取轮班制度详情失败: systemId={}", systemId, e);
                return RotationSystemDetail.builder()
                        .systemId(systemId)
                        .exists(false)
                        .errorMessage("获取详情时发生异常: " + e.getMessage())
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<RotationSystemListResult> getRotationSystemList(RotationSystemQueryParam queryParam) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<RotationSystemSummary> summaries = new ArrayList<>();

                for (Map.Entry<String, RotationSystemConfig> entry : rotationSystems.entrySet()) {
                    String systemId = entry.getKey();
                    RotationSystemConfig config = entry.getValue();

                    // 应用查询过滤条件
                    if (queryParam.getSystemType() != null && !queryParam.getSystemType().equals(config.getSystemType())) {
                        continue;
                    }

                    if (queryParam.getStatus() != null && !queryParam.getStatus().equals(config.getStatus())) {
                        continue;
                    }

                    List<RotationSchedule> schedules = rotationSchedules.getOrDefault(systemId, Collections.emptyList());

                    RotationSystemSummary summary = RotationSystemSummary.builder()
                            .systemId(systemId)
                            .systemName(config.getSystemName())
                            .systemType(config.getSystemType())
                            .cycleType(config.getCycleType())
                            .cycleDays(config.getCycleDays())
                            .shiftCount(config.getShiftConfigs() != null ? config.getShiftConfigs().size() : 0)
                            .employeeCount(config.getEmployeeIds() != null ? config.getEmployeeIds().size() : 0)
                            .scheduleCount(schedules.size())
                            .status(config.getStatus())
                            .effectiveDate(config.getEffectiveDate())
                            .expiryDate(config.getExpiryDate())
                            .build();

                    summaries.add(summary);
                }

                // 排序
                if (queryParam.getSortBy() != null) {
                    summaries.sort(createComparator(queryParam.getSortBy(), queryParam.isAsc()));
                }

                // 分页
                int total = summaries.size();
                int start = queryParam.getPageNum() * queryParam.getPageSize();
                int end = Math.min(start + queryParam.getPageSize(), total);

                List<RotationSystemSummary> pageData = start < total ? summaries.subList(start, end) : Collections.emptyList();

                return RotationSystemListResult.builder()
                        .success(true)
                        .data(pageData)
                        .total(total)
                        .pageNum(queryParam.getPageNum())
                        .pageSize(queryParam.getPageSize())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 获取轮班制度列表失败", e);
                return RotationSystemListResult.builder()
                        .success(false)
                        .errorMessage("获取列表时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<RotationPlanResult> generateRotationPlan(RotationPlanRequest planRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("[轮班系统] 生成轮班计划: systemId={}, startDate={}, endDate={}",
                        planRequest.getSystemId(), planRequest.getStartDate(), planRequest.getEndDate());

                RotationSystemConfig config = rotationSystems.get(planRequest.getSystemId());
                if (config == null) {
                    return RotationPlanResult.builder()
                            .success(false)
                            .errorMessage("轮班制度不存在")
                            .errorCode("SYSTEM_NOT_FOUND")
                            .build();
                }

                List<RotationSchedule> generatedSchedules = new ArrayList<>();
                LocalDate currentDate = planRequest.getStartDate();
                LocalDate endDate = planRequest.getEndDate();

                while (!currentDate.isAfter(endDate)) {
                    // 为每个员工生成当天的安排
                    for (Long employeeId : config.getEmployeeIds()) {
                        RotationSchedule schedule = generateEmployeeSchedule(config, employeeId, currentDate);
                        if (schedule != null) {
                            generatedSchedules.add(schedule);
                        }
                    }

                    currentDate = currentDate.plusDays(1);
                }

                // 存储生成的安排
                List<RotationSchedule> existingSchedules = rotationSchedules.getOrDefault(planRequest.getSystemId(), new ArrayList<>());
                existingSchedules.addAll(generatedSchedules);
                rotationSchedules.put(planRequest.getSystemId(), existingSchedules);

                // 更新索引
                updateIndexes(generatedSchedules);

                log.info("[轮班系统] 轮班计划生成成功: scheduleCount={}", generatedSchedules.size());

                return RotationPlanResult.builder()
                        .success(true)
                        .schedules(generatedSchedules)
                        .totalGenerated(generatedSchedules.size())
                        .generationTime(LocalDateTime.now())
                        .message("轮班计划生成成功")
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 生成轮班计划失败", e);
                return RotationPlanResult.builder()
                        .success(false)
                        .errorMessage("生成轮班计划时发生异常: " + e.getMessage())
                        .errorCode("SYSTEM_ERROR")
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<EmployeeRotationSchedule> getEmployeeRotationSchedule(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<RotationSchedule> employeeSchedules = employeeScheduleIndex.getOrDefault(employeeId, Collections.emptyList());

                List<RotationSchedule> filteredSchedules = employeeSchedules.stream()
                        .filter(schedule -> !schedule.getScheduleDate().isBefore(startDate) && !schedule.getScheduleDate().isAfter(endDate))
                        .sorted(Comparator.comparing(RotationSchedule::getScheduleDate))
                        .collect(Collectors.toList());

                // 统计信息
                RotationScheduleStats stats = calculateScheduleStats(filteredSchedules);

                return EmployeeRotationSchedule.builder()
                        .employeeId(employeeId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .schedules(filteredSchedules)
                        .stats(stats)
                        .queryTime(LocalDateTime.now())
                        .build();

            } catch (Exception e) {
                log.error("[轮班系统] 获取员工轮班安排失败: employeeId={}", employeeId, e);
                return EmployeeRotationSchedule.builder()
                        .employeeId(employeeId)
                        .startDate(startDate)
                        .endDate(endDate)
                        .schedules(Collections.emptyList())
                        .errorMessage("获取员工轮班安排时发生异常: " + e.getMessage())
                        .build();
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<List<EmployeeRotationSchedule>> getBatchEmployeeRotationSchedules(List<Long> employeeIds, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> {
            List<EmployeeRotationSchedule> results = new ArrayList<>();

            for (Long employeeId : employeeIds) {
                try {
                    EmployeeRotationSchedule schedule = getEmployeeRotationSchedule(employeeId, startDate, endDate).get();
                    results.add(schedule);
                } catch (Exception e) {
                    log.error("[轮班系统] 获取批量员工轮班安排失败: employeeId={}", employeeId, e);
                    // 添加错误记录，不影响其他员工的查询
                    results.add(EmployeeRotationSchedule.builder()
                            .employeeId(employeeId)
                            .startDate(startDate)
                            .endDate(endDate)
                            .schedules(Collections.emptyList())
                            .errorMessage("查询失败: " + e.getMessage())
                            .build());
                }
            }

            return results;
        }, executorService);
    }

    // 其他接口方法的实现...
    @Override
    public CompletableFuture<RotationOptimizationResult> optimizeRotationPlan(RotationOptimizationRequest optimizationRequest) {
        return CompletableFuture.completedFuture(
                RotationOptimizationResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationConflictValidationResult> validateRotationConflict(RotationConflictValidationRequest validationRequest) {
        return CompletableFuture.completedFuture(
                RotationConflictValidationResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationAdjustmentResult> adjustEmployeeRotation(RotationAdjustmentRequest adjustmentRequest) {
        return CompletableFuture.completedFuture(
                RotationAdjustmentResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationStatisticsResult> getRotationStatistics(RotationStatisticsRequest statisticsRequest) {
        return CompletableFuture.completedFuture(
                RotationStatisticsResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationHandoverResult> manageRotationHandover(RotationHandoverRequest handoverRequest) {
        return CompletableFuture.completedFuture(
                RotationHandoverResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationLeaveManagementResult> manageRotationLeave(RotationLeaveManagementRequest leaveRequest) {
        return CompletableFuture.completedFuture(
                RotationLeaveManagementResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    @Override
    public CompletableFuture<RotationAlertResult> getRotationAlerts(RotationAlertRequest alertRequest) {
        return CompletableFuture.completedFuture(
                RotationAlertResult.builder()
                        .success(false)
                        .errorMessage("功能待实现")
                        .errorCode("NOT_IMPLEMENTED")
                        .build()
        );
    }

    /**
     * 生成系统ID
     */
    private String generateSystemId() {
        return "ROT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成安排ID
     */
    private String generateScheduleId() {
        return "SCH-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 生成员工当天安排
     */
    private RotationSchedule generateEmployeeSchedule(RotationSystemConfig config, Long employeeId, LocalDate date) {
        // 根据轮班制度和日期计算班次
        RotationSystemConfig.ShiftType shiftType = calculateShiftType(config, employeeId, date);
        if (shiftType == null) {
            return null; // 休息日
        }

        // 查找对应的班次配置
        RotationSystemConfig.ShiftConfig shiftConfig = config.getShiftConfigs().stream()
                .filter(shift -> shift.getShiftType().equals(shiftType))
                .findFirst()
                .orElse(null);

        if (shiftConfig == null) {
            return null;
        }

        // 创建安排
        LocalDateTime workStartTime = date.atTime(shiftConfig.getWorkStartTime());
        LocalDateTime workEndTime = date.atTime(shiftConfig.getWorkEndTime());

        // 处理跨天情况
        if (workEndTime.isBefore(workStartTime)) {
            workEndTime = workEndTime.plusDays(1);
        }

        return RotationSchedule.builder()
                .scheduleId(generateScheduleId())
                .rotationSystemId("system-id-placeholder") // 实际使用中需要传入systemId
                .employeeId(employeeId)
                .scheduleDate(date)
                .shiftId(shiftConfig.getShiftId())
                .shiftName(shiftConfig.getShiftName())
                .shiftType(shiftConfig.getShiftType())
                .workStartTime(workStartTime)
                .workEndTime(workEndTime)
                .status(RotationSchedule.ScheduleStatus.SCHEDULED)
                .attendanceStatus(RotationSchedule.AttendanceStatus.PENDING)
                .createTime(LocalDateTime.now())
                .priority(shiftConfig.getPriority())
                .build();
    }

    /**
     * 计算员工在指定日期的班次类型
     */
    private RotationSystemConfig.ShiftType calculateShiftType(RotationSystemConfig config, Long employeeId, LocalDate date) {
        // 简化的轮班算法，实际实现需要更复杂的逻辑

        if (config.getSystemType() == RotationSystemConfig.RotationSystemType.THREE_SHIFT) {
            // 三班倒：早班、中班、晚班循环
            long daysSinceStart = java.time.Duration.between(config.getEffectiveDate().toLocalDate(), date).toDays();
            int cycleDay = (int) (daysSinceStart % 3);

            switch (cycleDay) {
                case 0: return RotationSystemConfig.ShiftType.MORNING;
                case 1: return RotationSystemConfig.ShiftType.AFTERNOON;
                case 2: return RotationSystemConfig.ShiftType.NIGHT;
                default: return null;
            }
        } else if (config.getSystemType() == RotationSystemConfig.RotationSystemType.FOUR_SHIFT) {
            // 四班三倒：三天工作，一天休息
            long daysSinceStart = java.time.Duration.between(config.getEffectiveDate().toLocalDate(), date).toDays();
            int cycleDay = (int) (daysSinceStart % 4);

            switch (cycleDay) {
                case 0: return RotationSystemConfig.ShiftType.MORNING;
                case 1: return RotationSystemConfig.ShiftType.AFTERNOON;
                case 2: return RotationSystemConfig.ShiftType.NIGHT;
                case 3: return null; // 休息日
                default: return null;
            }
        }

        return null;
    }

    /**
     * 更新索引
     */
    private void updateIndexes(List<RotationSchedule> schedules) {
        for (RotationSchedule schedule : schedules) {
            // 更新员工索引
            employeeScheduleIndex.computeIfAbsent(schedule.getEmployeeId(), k -> new ArrayList<>()).add(schedule);

            // 更新日期索引
            dateScheduleIndex.computeIfAbsent(schedule.getScheduleDate(), k -> new ArrayList<>()).add(schedule);
        }
    }

    /**
     * 计算安排统计信息
     */
    private RotationScheduleStats calculateScheduleStats(List<RotationSchedule> schedules) {
        int totalSchedules = schedules.size();
        long workDays = schedules.stream()
                .filter(s -> s.getShiftType() != null)
                .count();
        long restDays = totalSchedules - workDays;

        Map<RotationSystemConfig.ShiftType, Long> shiftTypeCount = schedules.stream()
                .filter(s -> s.getShiftType() != null)
                .collect(Collectors.groupingBy(RotationSchedule::getShiftType, Collectors.counting()));

        return RotationScheduleStats.builder()
                .totalDays(totalSchedules)
                .workDays((int) workDays)
                .restDays((int) restDays)
                .shiftTypeDistribution(shiftTypeCount)
                .build();
    }

    /**
     * 创建比较器
     */
    private Comparator<RotationSystemSummary> createComparator(String sortBy, boolean asc) {
        Comparator<RotationSystemSummary> comparator;

        switch (sortBy.toLowerCase()) {
            case "name":
                comparator = Comparator.comparing(RotationSystemSummary::getSystemName);
                break;
            case "type":
                comparator = Comparator.comparing(RotationSystemSummary::getSystemType);
                break;
            case "status":
                comparator = Comparator.comparing(RotationSystemSummary::getStatus);
                break;
            case "created":
                comparator = Comparator.comparing(RotationSystemSummary::getEffectiveDate);
                break;
            default:
                comparator = Comparator.comparing(RotationSystemSummary::getSystemName);
                break;
        }

        return asc ? comparator : comparator.reversed();
    }

    /**
     * 初始化默认轮班制度
     */
    private void initializeDefaultRotationSystems() {
        log.info("[轮班系统] 初始化默认轮班制度");
        // 可以在这里添加一些默认的轮班制度示例
    }
}