package net.lab1024.sa.attendance.engine.algorithm.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import net.lab1024.sa.attendance.engine.algorithm.AlgorithmMetadata;
import net.lab1024.sa.attendance.engine.algorithm.ScheduleAlgorithm;
import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.engine.model.ScheduleResult;

/**
 * 贪心算法实现类
 * <p>
 * 说明：此前版本引入了与当前 {@link ScheduleData} 不一致的“第二套模型/接口”（例如
 * Employee/Shift/ScheduleConstraint
 * 等），导致编译期出现大量“找不到符号”，并且会在修复前置错误后被逐步暴露，造成“问题忽多忽少”的错觉。
 * </p>
 * <p>
 * 本实现采用“最小可用、真实可运行”的贪心策略：
 * - 以日期为主序，按班次逐个分配
 * - 以员工轮询为主策略，尽量满足班次的 requiredEmployeeCount
 * - 尊重 employee.availableDates（如存在）
 * </p>
 * <p>
 * 严格遵循 CLAUDE.md 架构约束：不引入外部Mock、不依赖额外服务。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Slf4j
public class GreedyAlgorithmImpl implements ScheduleAlgorithm {


    /** 算法参数 */
    private Map<String, Object> parameters;

    /** 算法状态 */
    private volatile AlgorithmStatus status = AlgorithmStatus.INITIALIZED;

    /** 算法回调 */
    private AlgorithmCallback callback;

    /** 执行控制 */
    private volatile boolean shouldStop = false;

    /**
     * 获取算法类型
     */
    @Override
    public String getAlgorithmType() {
        return "GREEDY";
    }

    /**
     * 获取算法名称
     */
    @Override
    public String getAlgorithmName() {
        return "贪心算法";
    }

    /**
     * 获取算法描述
     */
    @Override
    public String getAlgorithmDescription() {
        return "按日期+班次顺序进行员工轮询分配的贪心排班算法（最小可用实现）";
    }

    /**
     * 生成排班方案
     */
    @Override
    public ScheduleResult generateSchedule(ScheduleData scheduleData) {
        long start = System.currentTimeMillis();
        status = AlgorithmStatus.RUNNING;

        try {
            if (scheduleData == null) {
                return buildFailedResult("输入数据为空", start);
            }
            if (scheduleData.getStartDate() == null || scheduleData.getEndDate() == null) {
                return buildFailedResult("排班起止日期不能为空", start);
            }
            if (scheduleData.getEmployees() == null || scheduleData.getEmployees().isEmpty()) {
                return buildFailedResult("员工列表不能为空", start);
            }
            if (scheduleData.getAvailableShifts() == null || scheduleData.getAvailableShifts().isEmpty()) {
                return buildFailedResult("可用班次列表不能为空", start);
            }

            List<ScheduleResult.ScheduleRecord> records = new ArrayList<>();

            LocalDate date = scheduleData.getStartDate();
            int employeeCursor = 0;

            while (!date.isAfter(scheduleData.getEndDate())) {
                if (shouldStop) {
                    status = AlgorithmStatus.STOPPED;
                    return buildFailedResult("算法已停止", start);
                }

                for (ScheduleData.ShiftData shift : scheduleData.getAvailableShifts()) {
                    int required = shift != null && shift.getRequiredEmployeeCount() != null
                            ? Math.max(0, shift.getRequiredEmployeeCount())
                            : 1;

                    // required=0 时跳过
                    if (required <= 0) {
                        continue;
                    }

                    int assigned = 0;
                    int tryCount = 0;
                    while (assigned < required && tryCount < scheduleData.getEmployees().size() * 2) {
                        ScheduleData.EmployeeData employee = scheduleData.getEmployees().get(employeeCursor);
                        employeeCursor = (employeeCursor + 1) % scheduleData.getEmployees().size();
                        tryCount++;

                        if (!isEmployeeAvailableOnDate(employee, date)) {
                            continue;
                        }

                        records.add(ScheduleResult.ScheduleRecord.builder()
                                .recordId(null)
                                .userId(employee.getEmployeeId())
                                .departmentId(employee.getDepartmentId())
                                .shiftId(shift != null ? shift.getShiftId() : null)
                                .scheduleDate(date.toString())
                                .workStartTime(
                                        shift != null && shift.getWorkStartTime() != null ? shift.getWorkStartTime().toString()
                                                : null)
                                .workEndTime(shift != null && shift.getWorkEndTime() != null ? shift.getWorkEndTime().toString()
                                        : null)
                                .workLocation(shift != null ? shift.getWorkLocation() : null)
                                .status("SCHEDULED")
                                .build());

                        assigned++;
                    }
                }

                date = date.plusDays(1);
            }

            ScheduleResult result = ScheduleResult.builder()
                    .resultId(null)
                    .planId(scheduleData.getPlanId())
                    .status("SUCCESS")
                    .message("贪心排班生成成功")
                    .scheduleRecords(records)
                    .conflicts(new ArrayList<>())
                    .statistics(null)
                    .executionTime(System.currentTimeMillis() - start)
                    .algorithmUsed(getAlgorithmType())
                    .optimizationMetrics(new HashMap<>())
                    .qualityScore(0.6)
                    .needsReview(false)
                    .recommendations(Arrays.asList("如需更高质量结果，可尝试遗传算法/启发式算法"))
                    .completedAt(LocalDateTime.now())
                    .extendedAttributes(Map.of("traceId", "greedy-" + UUID.randomUUID()))
                    .build();

            status = AlgorithmStatus.COMPLETED;
            return result;

        } catch (Exception e) {
            log.error("[贪心算法] 生成排班方案异常", e);
            status = AlgorithmStatus.ERROR;
            return buildFailedResult("贪心排班失败: " + e.getMessage(), start);
        }
    }

    /**
     * 初始化算法参数
     */
    @Override
    public void initialize(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * 获取算法参数
     */
    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * 验证算法参数
     */
    @Override
    public AlgorithmValidationResult validateParameters(Map<String, Object> parameters) {
        AlgorithmValidationResult result = new AlgorithmValidationResult();
        result.setValid(true);
        result.addValidationDetail("parameters", parameters);
        return result;
    }

    /**
     * 估算执行时间
     */
    @Override
    public long estimateExecutionTime(ScheduleData scheduleData) {
        if (scheduleData == null || scheduleData.getEmployees() == null || scheduleData.getAvailableShifts() == null
                || scheduleData.getStartDate() == null || scheduleData.getEndDate() == null) {
            return 0;
        }

        int days = Math.max(1,
                (int) (scheduleData.getEndDate().toEpochDay() - scheduleData.getStartDate().toEpochDay() + 1));
        long base = (long) scheduleData.getEmployees().size() * scheduleData.getAvailableShifts().size() * days;
        return Math.min(60_000L, base); // 粗略估计，最多60s
    }

    /**
     * 获取复杂度描述
     */
    @Override
    public AlgorithmComplexity getComplexity() {
        AlgorithmComplexity complexity = new AlgorithmComplexity("O(D*S*E)", "O(E)");
        complexity.setWorstCaseTime("O(D*S*E)");
        complexity.setBestCaseTime("O(E)");
        complexity.setAverageCaseTime("O(D*S*E)");
        return complexity;
    }

    /**
     * 是否适用
     */
    @Override
    public boolean isApplicable(int employeeCount, int shiftCount, int timeRange) {
        // 贪心算法适用范围较广
        return employeeCount >= 1 && shiftCount >= 1 && timeRange >= 1;
    }

    /**
     * 适用场景
     */
    @Override
    public List<String> getApplicationScenarios() {
        return Arrays.asList("快速生成排班", "小规模排班", "基础排班需求");
    }

    /**
     * 设置回调
     */
    @Override
    public void setCallback(AlgorithmCallback callback) {
        this.callback = callback;
    }

    /**
     * 获取状态
     */
    @Override
    public AlgorithmStatus getStatus() {
        return status;
    }

    /**
     * 停止执行
     */
    @Override
    public void stop() {
        shouldStop = true;
        status = AlgorithmStatus.STOPPED;
    }

    /**
     * 暂停执行（贪心算法最小实现不支持暂停，直接标记）
     */
    @Override
    public void pause() {
        status = AlgorithmStatus.PAUSED;
    }

    /**
     * 恢复执行（贪心算法最小实现不支持恢复，直接标记）
     */
    @Override
    public void resume() {
        status = AlgorithmStatus.RUNNING;
    }

    private boolean isEmployeeAvailableOnDate(ScheduleData.EmployeeData employee, LocalDate date) {
        if (employee == null || employee.getEmployeeId() == null) {
            return false;
        }
        if (employee.getAvailableDates() == null || employee.getAvailableDates().isEmpty()) {
            return true;
        }
        return employee.getAvailableDates().contains(date);
    }

    private ScheduleResult buildFailedResult(String message, long start) {
        return ScheduleResult.builder()
                .resultId(null)
                .planId(null)
                .status("FAILED")
                .message(message)
                .scheduleRecords(new ArrayList<>())
                .conflicts(new ArrayList<>())
                .statistics(null)
                .executionTime(System.currentTimeMillis() - start)
                .algorithmUsed(getAlgorithmType())
                .optimizationMetrics(new HashMap<>())
                .qualityScore(0.0)
                .needsReview(true)
                .recommendations(Arrays.asList("检查输入数据与约束配置"))
                .completedAt(LocalDateTime.now())
                .extendedAttributes(new HashMap<>())
                .build();
    }

    @Override
    public AlgorithmMetadata getMetadata() {
        return AlgorithmMetadata.builder()
                .name("GreedyAlgorithm")
                .version("1.0.0")
                .description("贪心算法排班实现")
                .author("IOE-DREAM架构团队")
                .build();
    }
}
