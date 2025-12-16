package net.lab1024.sa.attendance.realtime;

import net.lab1024.sa.attendance.engine.model.ScheduleData;
import net.lab1024.sa.attendance.realtime.event.AttendanceEvent;
import net.lab1024.sa.attendance.realtime.event.CalculationTriggerEvent;
import net.lab1024.sa.attendance.realtime.model.RealtimeCalculationResult;

import java.util.List;
import java.util.Map;

/**
 * 考勤实时计算引擎接口
 * <p>
 * 负责实时处理考勤数据、计算统计指标、触发业务规则
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface RealtimeCalculationEngine {

    /**
     * 启动实时计算引擎
     *
     * @return 启动结果
     */
    EngineStartupResult startup();

    /**
     * 停止实时计算引擎
     *
     * @return 停止结果
     */
    EngineShutdownResult shutdown();

    /**
     * 处理考勤事件
     *
     * @param attendanceEvent 考勤事件
     * @return 处理结果
     */
    RealtimeCalculationResult processAttendanceEvent(AttendanceEvent attendanceEvent);

    /**
     * 批量处理考勤事件
     *
     * @param attendanceEvents 考勤事件列表
     * @return 批量处理结果
     */
    BatchCalculationResult processBatchEvents(List<AttendanceEvent> attendanceEvents);

    /**
     * 触发实时计算
     *
     * @param triggerEvent 触发事件
     * @return 计算结果
     */
    RealtimeCalculationResult triggerCalculation(CalculationTriggerEvent triggerEvent);

    /**
     * 获取实时统计结果
     *
     * @param queryParameters 查询参数
     * @return 统计结果
     */
    RealtimeStatisticsResult getRealtimeStatistics(StatisticsQueryParameters queryParameters);

    /**
     * 获取员工实时考勤状态
     *
     * @param employeeId 员工ID
     * @param timeRange 时间范围
     * @return 考勤状态
     */
    EmployeeRealtimeStatus getEmployeeRealtimeStatus(Long employeeId, TimeRange timeRange);

    /**
     * 获取部门实时考勤统计
     *
     * @param departmentId 部门ID
     * @param timeRange 时间范围
     * @return 部门统计
     */
    DepartmentRealtimeStatistics getDepartmentRealtimeStatistics(Long departmentId, TimeRange timeRange);

    /**
     * 获取公司整体实时考勤概览
     *
     * @param timeRange 时间范围
     * @return 公司考勤概览
     */
    CompanyRealtimeOverview getCompanyRealtimeOverview(TimeRange timeRange);

    /**
     * 计算异常考勤情况
     *
     * @param timeRange 时间范围
     * @param filterParameters 过滤参数
     * @return 异常情况
     */
    AnomalyDetectionResult calculateAttendanceAnomalies(TimeRange timeRange, AnomalyFilterParameters filterParameters);

    /**
     * 实时预警检测
     *
     * @param monitoringParameters 监控参数
     * @return 预警结果
     */
    RealtimeAlertResult detectRealtimeAlerts(RealtimeMonitoringParameters monitoringParameters);

    /**
     * 与排班引擎集成计算
     *
     * @param scheduleData 排班数据
     * @param integrationParameters 集成参数
     * @return 集成计算结果
     */
    ScheduleIntegrationResult integrateWithScheduleEngine(ScheduleData scheduleData, ScheduleIntegrationParameters integrationParameters);

    /**
     * 注册计算规则
     *
     * @param calculationRule 计算规则
     * @return 注册结果
     */
    RuleRegistrationResult registerCalculationRule(CalculationRule calculationRule);

    /**
     * 注销计算规则
     *
     * @param ruleId 规则ID
     * @return 注销结果
     */
    RuleUnregistrationResult unregisterCalculationRule(String ruleId);

    /**
     * 获取引擎性能指标
     *
     * @return 性能指标
     */
    EnginePerformanceMetrics getPerformanceMetrics();

    /**
     * 验证计算结果
     *
     * @param calculationResult 计算结果
     * @return 验证结果
     */
    boolean validateCalculationResult(RealtimeCalculationResult calculationResult);

    /**
     * 获取引擎状态
     *
     * @return 引擎状态
     */
    EngineStatus getEngineStatus();
}