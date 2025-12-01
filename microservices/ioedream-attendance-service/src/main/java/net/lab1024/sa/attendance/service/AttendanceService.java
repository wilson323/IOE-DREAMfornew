package net.lab1024.sa.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import net.lab1024.sa.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordUpdateDTO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤业务Service接口
 *
 * <p>
 * 考勤业务逻辑接口，提供考勤记录的查询、打卡、统计等功能
 * 遵循repowiki规范中的Service层标准：
 * - Service层负责业务逻辑处理和事务管理
 * - 提供清晰的业务方法定义
 * - 支持复杂的考勤规则计算和统计
 * - 使用合适的参数和返回值类型
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-16
 */
public interface AttendanceService {

    /**
     * 分页查询考勤记录
     *
     * @param attendanceQueryVO 考勤查询条件
     * @param pageParam         分页参数
     * @return 考勤记录分页查询结果
     */
    PageResult<AttendanceRecordVO> queryByPage(AttendanceRecordQueryVO attendanceQueryVO, PageParam pageParam);

    /**
     * 根据ID获取考勤记录详情
     *
     * @param recordId 考勤记录ID
     * @return 考勤记录详情
     */
    ResponseDTO<AttendanceRecordVO> getById(Long recordId);

    /**
     * 员工打卡
     *
     * @param punchDTO 打卡参数
     * @return 打卡结果
     */
    ResponseDTO<String> punch(@Valid AttendancePunchDTO punchDTO);

    /**
     * 新增考勤记录
     *
     * @param createDTO 新增参数
     * @return 考勤记录ID
     */
    ResponseDTO<Long> create(@Valid AttendanceRecordCreateDTO createDTO);

    /**
     * 更新考勤记录
     *
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    ResponseDTO<Boolean> update(@Valid AttendanceRecordUpdateDTO updateDTO);

    /**
     * 删除考勤记录
     *
     * @param recordId 考勤记录ID
     * @return 删除结果
     */
    ResponseDTO<Boolean> delete(Long recordId);

    /**
     * 批量删除考勤记录
     *
     * @param recordIds 考勤记录ID列表
     * @return 删除数量
     */
    ResponseDTO<Integer> batchDelete(List<Long> recordIds);

    /**
     * 根据员工ID获取考勤统计
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getEmployeeStats(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据部门ID获取考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getDepartmentStats(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 批量重新计算指定员工的考勤记录
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 更新数量
     */
    ResponseDTO<Integer> batchRecalculate(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取异常考勤记录
     *
     * @param employeeId    员工ID（可选）
     * @param departmentId  部门ID（可选）
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param exceptionType 异常类型（可选）
     * @return 异常考勤记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getAbnormalRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String exceptionType);

    /**
     * 获取迟到考勤记录
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 迟到考勤记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getLateRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取早退考勤记录列表
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 早退考勤记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getEarlyLeaveRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取加班考勤记录
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 加班考勤记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getOvertimeRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取缺勤考勤记录
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 缺勤考勤记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getAbsentRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取考勤月度汇总数据
     *
     * @param year         年份
     * @param month        月份
     * @param departmentId 部门ID（可选）
     * @return 月度汇总数据列表
     */
    ResponseDTO<List<Map<String, Object>>> getMonthlySummary(Integer year,
            Integer month,
            Long departmentId);

    /**
     * 同步考勤数据
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 同步数量
     */
    ResponseDTO<Integer> syncAttendanceData(Long employeeId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 导出考勤数据
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param format       导出格式
     * @return 导出文件路径
     */
    ResponseDTO<String> exportAttendanceData(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String format);

    /**
     * 获取考勤日历数据
     *
     * @param employeeId 员工ID
     * @param year       年份
     * @param month      月份
     * @return 日历数据
     */
    ResponseDTO<List<Map<String, Object>>> getCalendarData(Long employeeId,
            Integer year,
            Integer month);

    /**
     * 验证打卡请求
     *
     * @param punchDTO 打卡参数
     * @return 验证结果
     */
    ResponseDTO<Boolean> validatePunch(@Valid AttendancePunchDTO punchDTO);

    /**
     * 自动补全考勤记录
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 补全数量
     */
    ResponseDTO<Integer> autoCompleteRecords(Long employeeId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取考勤趋势数据
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param groupBy      分组方式（day/week/month）
     * @return 趋势数据
     */
    ResponseDTO<List<Map<String, Object>>> getAttendanceTrend(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String groupBy);

    /**
     * 获取员工考勤排名
     *
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @param orderBy      排序方式
     * @param limit        返回数量限制
     * @return 考勤排名数据
     */
    ResponseDTO<List<Map<String, Object>>> getEmployeeRanking(Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String orderBy,
            Integer limit);

    /**
     * 获取考勤异常处理建议
     *
     * @param employeeId 员工ID
     * @param date       考勤日期
     * @return 处理建议
     */
    ResponseDTO<Map<String, Object>> getAbnormalHandlingSuggestions(Long employeeId, LocalDate date);

    /**
     * 分页查询考勤记录（简化版）
     *
     * @param pageParam      分页参数
     * @param employeeId    员工ID（可选）
     * @param startDate      开始日期（可选）
     * @param endDate        结束日期（可选）
     * @param attendanceType 考勤类型（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> queryAttendancePage(PageParam pageParam, Long employeeId, 
            String startDate, String endDate, Integer attendanceType);

    /**
     * 员工打卡（简化版）
     *
     * @param punchData 打卡数据
     */
    void punchAttendance(Map<String, Object> punchData);

    /**
     * 获取今日考勤状态
     *
     * @param employeeId 员工ID
     * @return 今日考勤状态
     */
    Map<String, Object> getTodayAttendanceStatus(Long employeeId);

    /**
     * 获取考勤统计
     *
     * @param startDate    开始日期（可选）
     * @param endDate      结束日期（可选）
     * @param departmentId 部门ID（可选）
     * @return 统计数据
     */
    Map<String, Object> getAttendanceStatistics(String startDate, String endDate, Long departmentId);

    /**
     * 分页查询考勤异常记录
     *
     * @param pageParam     分页参数
     * @param employeeId    员工ID（可选）
     * @param exceptionType 异常类型（可选）
     * @param startDate     开始日期（可选）
     * @param endDate       结束日期（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> queryAttendanceExceptions(PageParam pageParam, Long employeeId,
            Integer exceptionType, String startDate, String endDate);

    /**
     * 处理考勤异常
     *
     * @param exceptionId 异常ID
     * @param processData 处理数据
     */
    void processAttendanceException(Long exceptionId, Map<String, Object> processData);

    /**
     * 批量导入考勤数据
     *
     * @param attendanceData 考勤数据列表
     */
    void batchImportAttendance(List<Map<String, Object>> attendanceData);
}
