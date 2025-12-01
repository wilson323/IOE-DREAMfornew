package net.lab1024.sa.attendance.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.attendance.domain.entity.AttendanceExceptionEntity;

/**
 * 考勤异常 DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供异常查询和处理接口
 * - 支持工作流状态管理
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AttendanceExceptionDao extends BaseMapper<AttendanceExceptionEntity> {

    /**
     * 根据员工ID和日期范围查询异常记录
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据日期查询异常记录
     *
     * @param exceptionDate 异常日期
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByDate(@Param("exceptionDate") LocalDate exceptionDate);

    /**
     * 根据部门ID和日期范围查询异常记录
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByDepartmentAndDateRange(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据异常类型查询异常记录
     *
     * @param exceptionType 异常类型
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByExceptionType(
            @Param("exceptionType") String exceptionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据异常级别查询异常记录
     *
     * @param exceptionLevel 异常级别
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByExceptionLevel(
            @Param("exceptionLevel") String exceptionLevel,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据处理状态查询异常记录
     *
     * @param status    处理状态
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByStatus(
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询待处理的异常记录
     *
     * @param departmentIds  部门ID列表（可选）
     * @param exceptionLevel 异常级别（可选）
     * @return 待处理的异常记录列表
     */
    List<AttendanceExceptionEntity> selectPendingExceptions(
            @Param("departmentIds") List<Long> departmentIds,
            @Param("exceptionLevel") String exceptionLevel);

    /**
     * 查询已处理的异常记录
     *
     * @param startDate   开始日期（可选）
     * @param endDate     结束日期（可选）
     * @param processedBy 处理人ID（可选）
     * @return 已处理的异常记录列表
     */
    List<AttendanceExceptionEntity> selectProcessedExceptions(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("processedBy") Long processedBy);

    /**
     * 按条件查询异常记录
     *
     * @param employeeId     员工ID（可选）
     * @param departmentId   部门ID（可选）
     * @param exceptionType  异常类型（可选）
     * @param exceptionLevel 异常级别（可选）
     * @param status         处理状态（可选）
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @param autoDetected   是否自动检测（可选）
     * @return 异常记录列表
     */
    List<AttendanceExceptionEntity> selectByCondition(
            @Param("employeeId") Long employeeId,
            @Param("departmentId") Long departmentId,
            @Param("exceptionType") String exceptionType,
            @Param("exceptionLevel") String exceptionLevel,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("autoDetected") Integer autoDetected);

    /**
     * 统计员工异常数据
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计数据
     */
    Map<String, Object> selectEmployeeExceptionStats(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 统计部门异常数据
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计数据
     */
    Map<String, Object> selectDepartmentExceptionStats(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询异常趋势分析
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @param groupBy      分组方式：day/week/month
     * @return 趋势数据
     */
    List<Map<String, Object>> selectExceptionTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId,
            @Param("groupBy") String groupBy);

    /**
     * 查询异常类型统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 异常类型统计数据
     */
    List<Map<String, Object>> selectExceptionTypeStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询异常级别统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 异常级别统计数据
     */
    List<Map<String, Object>> selectExceptionLevelStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询处理状态统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 处理状态统计数据
     */
    List<Map<String, Object>> selectStatusStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 批量更新异常状态
     *
     * @param exceptionIds 异常ID列表
     * @param status       新状态
     * @param processType  处理方式（可选）
     * @return 更新行数
     */
    int batchUpdateStatus(
            @Param("exceptionIds") List<Long> exceptionIds,
            @Param("status") String status,
            @Param("processType") String processType);

    /**
     * 批量处理异常
     *
     * @param exceptionIds  异常ID列表
     * @param status        处理结果
     * @param processType   处理方式
     * @param processedBy   处理人ID
     * @param processRemark 处理备注
     * @param isValid       是否有效异常
     * @return 更新行数
     */
    int batchProcessExceptions(
            @Param("exceptionIds") List<Long> exceptionIds,
            @Param("status") String status,
            @Param("processType") String processType,
            @Param("processedBy") Long processedBy,
            @Param("processedByName") String processedByName,
            @Param("processRemark") String processRemark,
            @Param("isValid") Integer isValid);

    /**
     * 自动批准异常
     *
     * @param exceptionIds 符合自动批准条件的异常ID列表
     * @param processedBy  处理人ID（系统用户ID）
     * @return 批准的异常数量
     */
    int autoApproveExceptions(
            @Param("exceptionIds") List<Long> exceptionIds,
            @Param("processedBy") Long processedBy);

    /**
     * 查询需要跟进的异常
     *
     * @param followUpDate 跟进日期
     * @return 需要跟进的异常列表
     */
    List<AttendanceExceptionEntity> selectFollowUpExceptions(@Param("followUpDate") LocalDate followUpDate);

    /**
     * 查询超期未处理的异常
     *
     * @param daysThreshold 超期天数阈值
     * @return 超期异常列表
     */
    List<AttendanceExceptionEntity> selectOverdueExceptions(@Param("daysThreshold") Integer daysThreshold);

    /**
     * 查询员工异常频率分析
     *
     * @param startDate         开始日期
     * @param endDate           结束日期
     * @param departmentId      部门ID（可选）
     * @param minExceptionCount 最少异常次数
     * @return 员工异常频率数据
     */
    List<Map<String, Object>> selectEmployeeExceptionFrequency(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId,
            @Param("minExceptionCount") Integer minExceptionCount);

    /**
     * 查询需要发送通知的异常
     *
     * @return 需要发送通知的异常列表
     */
    List<Map<String, Object>> selectExceptionsToNotify();

    /**
     * 更新异常通知状态
     *
     * @param exceptionIds 异常ID列表
     * @return 更新行数
     */
    int batchUpdateNotificationSent(@Param("exceptionIds") List<Long> exceptionIds);

    /**
     * 查询异常处理时效统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 处理时效统计数据
     */
    List<Map<String, Object>> selectProcessingTimeStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询异常处理人统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 处理人统计数据
     */
    List<Map<String, Object>> selectProcessorStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询异常报表数据
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param dimension    报表维度：department/employee/type/level/processor
     * @param departmentId 部门ID（可选）
     * @return 报表数据
     */
    List<Map<String, Object>> selectExceptionReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("dimension") String dimension,
            @Param("departmentId") Long departmentId);

    /**
     * 清理过期的异常记录
     *
     * @param beforeDate    清理日期
     * @param keepProcessed 是否保留已处理的记录
     * @return 清理行数
     */
    int cleanExpiredExceptions(
            @Param("beforeDate") LocalDate beforeDate,
            @Param("keepProcessed") Boolean keepProcessed);

    /**
     * 查询异常严重程度分布
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 严重程度分布数据
     */
    List<Map<String, Object>> selectSeverityDistribution(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询自动检测vs手动录入统计
     *
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param departmentId 部门ID（可选）
     * @return 检测方式统计数据
     */
    List<Map<String, Object>> selectDetectionMethodStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("departmentId") Long departmentId);

    /**
     * 查询异常关联的考勤记录
     *
     * @param exceptionId 异常ID
     * @return 关联的考勤记录信息
     */
    Map<String, Object> selectRelatedAttendanceRecord(@Param("exceptionId") Long exceptionId);

    /**
     * 查询重复异常记录
     *
     * @param employeeId    员工ID
     * @param exceptionDate 异常日期
     * @param exceptionType 异常类型
     * @param excludeId     排除的异常ID（可选）
     * @return 重复异常数量
     */
    Integer countDuplicateExceptions(
            @Param("employeeId") Long employeeId,
            @Param("exceptionDate") LocalDate exceptionDate,
            @Param("exceptionType") String exceptionType,
            @Param("excludeId") Long excludeId);
}
