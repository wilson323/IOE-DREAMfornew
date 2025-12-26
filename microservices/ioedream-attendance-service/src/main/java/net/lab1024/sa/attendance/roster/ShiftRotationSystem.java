package net.lab1024.sa.attendance.roster;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.lab1024.sa.attendance.roster.model.RotationSystemConfig;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemCreationResult;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemUpdateResult;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemDeletionResult;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemDetail;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemQueryParam;
import net.lab1024.sa.attendance.roster.model.result.RotationSystemListResult;
import net.lab1024.sa.attendance.roster.model.result.RotationPlanRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationPlanResult;
import net.lab1024.sa.attendance.roster.model.result.EmployeeRotationSchedule;
import net.lab1024.sa.attendance.roster.model.result.RotationOptimizationRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationOptimizationResult;
import net.lab1024.sa.attendance.roster.model.result.RotationConflictValidationRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationConflictValidationResult;
import net.lab1024.sa.attendance.roster.model.result.RotationAdjustmentRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationAdjustmentResult;
import net.lab1024.sa.attendance.roster.model.result.RotationStatisticsRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationStatisticsResult;
import net.lab1024.sa.attendance.roster.model.result.RotationHandoverRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationHandoverResult;
import net.lab1024.sa.attendance.roster.model.result.RotationLeaveManagementRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationLeaveManagementResult;
import net.lab1024.sa.attendance.roster.model.result.RotationAlertRequest;
import net.lab1024.sa.attendance.roster.model.result.RotationAlertResult;

/**
 * 轮班系统接口
 * <p>
 * 定义轮班制度管理的标准接口
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ShiftRotationSystem {

    /**
     * 创建轮班制度
     *
     * @param rotationSystem 轮班制度配置
     * @return 创建结果
     */
    CompletableFuture<RotationSystemCreationResult> createRotationSystem(RotationSystemConfig rotationSystem);

    /**
     * 更新轮班制度
     *
     * @param systemId 轮班制度ID
     * @param rotationSystem 轮班制度配置
     * @return 更新结果
     */
    CompletableFuture<RotationSystemUpdateResult> updateRotationSystem(String systemId, RotationSystemConfig rotationSystem);

    /**
     * 删除轮班制度
     *
     * @param systemId 轮班制度ID
     * @return 删除结果
     */
    CompletableFuture<RotationSystemDeletionResult> deleteRotationSystem(String systemId);

    /**
     * 获取轮班制度详情
     *
     * @param systemId 轮班制度ID
     * @return 轮班制度详情
     */
    CompletableFuture<RotationSystemDetail> getRotationSystemDetail(String systemId);

    /**
     * 获取轮班制度列表
     *
     * @param queryParam 查询参数
     * @return 轮班制度列表
     */
    CompletableFuture<RotationSystemListResult> getRotationSystemList(RotationSystemQueryParam queryParam);

    /**
     * 生成轮班计划
     *
     * @param planRequest 轮班计划生成请求
     * @return 轮班计划
     */
    CompletableFuture<RotationPlanResult> generateRotationPlan(RotationPlanRequest planRequest);

    /**
     * 获取员工轮班安排
     *
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 员工轮班安排
     */
    CompletableFuture<EmployeeRotationSchedule> getEmployeeRotationSchedule(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 批量获取员工轮班安排
     *
     * @param employeeIds 员工ID列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 员工轮班安排列表
     */
    CompletableFuture<List<EmployeeRotationSchedule>> getBatchEmployeeRotationSchedules(List<Long> employeeIds, LocalDate startDate, LocalDate endDate);

    /**
     * 优化轮班计划
     *
     * @param optimizationRequest 轮班优化请求
     * @return 优化结果
     */
    CompletableFuture<RotationOptimizationResult> optimizeRotationPlan(RotationOptimizationRequest optimizationRequest);

    /**
     * 验证轮班冲突
     *
     * @param validationRequest 冲突验证请求
     * @return 验证结果
     */
    CompletableFuture<RotationConflictValidationResult> validateRotationConflict(RotationConflictValidationRequest validationRequest);

    /**
     * 调整员工轮班
     *
     * @param adjustmentRequest 轮班调整请求
     * @return 调整结果
     */
    CompletableFuture<RotationAdjustmentResult> adjustEmployeeRotation(RotationAdjustmentRequest adjustmentRequest);

    /**
     * 获取轮班统计信息
     *
     * @param statisticsRequest 统计请求
     * @return 统计结果
     */
    CompletableFuture<RotationStatisticsResult> getRotationStatistics(RotationStatisticsRequest statisticsRequest);

    /**
     * 轮班交接管理
     *
     * @param handoverRequest 交接请求
     * @return 交接结果
     */
    CompletableFuture<RotationHandoverResult> manageRotationHandover(RotationHandoverRequest handoverRequest);

    /**
     * 轮班假期管理
     *
     * @param leaveRequest 假期管理请求
     * @return 处理结果
     */
    CompletableFuture<RotationLeaveManagementResult> manageRotationLeave(RotationLeaveManagementRequest leaveRequest);

    /**
     * 获取轮班预警信息
     *
     * @param alertRequest 预警请求
     * @return 预警信息
     */
    CompletableFuture<RotationAlertResult> getRotationAlerts(RotationAlertRequest alertRequest);
}
