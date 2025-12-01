package net.lab1024.sa.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import net.lab1024.sa.attendance.domain.dto.AttendancePunchDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordCreateDTO;
import net.lab1024.sa.attendance.domain.dto.AttendanceRecordUpdateDTO;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.domain.form.MakeupPunchRequest;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordQueryVO;
import net.lab1024.sa.attendance.domain.vo.AttendanceRecordVO;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 考勤服务接口
 *
 * 严格遵循repowiki规范:
 * - 定义业务逻辑接口
 * - 遵循单一职责原则
 * - 统一的方法命名规范
 *
 * 基于现有94个Java文件、37,245行代码的微服务化改造
 * 支持Nacos服务发现和配置中心集成
 * 确保与现有sa-base模块的全局一致性
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
public interface IAttendanceService extends IService<AttendanceRecordEntity> {

    /**
     * 分页查询考勤记录
     *
     * @param attendanceQueryVO 查询条件
     * @param pageParam         分页参数
     * @return 分页结果
     */
    PageResult<AttendanceRecordVO> queryByPage(AttendanceRecordQueryVO attendanceQueryVO, PageParam pageParam);

    /**
     * 根据ID查询考勤记录
     *
     * @param recordId 记录ID
     * @return 考勤记录
     */
    ResponseDTO<AttendanceRecordVO> getById(Long recordId);

    /**
     * 员工打卡
     *
     * @param punchDTO 打卡参数
     * @return 打卡结果
     */
    ResponseDTO<String> punch(AttendancePunchDTO punchDTO);

    /**
     * 创建考勤记录
     *
     * @param createDTO 创建参数
     * @return 创建结果
     */
    ResponseDTO<Long> create(AttendanceRecordCreateDTO createDTO);

    /**
     * 更新考勤记录
     *
     * @param updateDTO 更新参数
     * @return 更新结果
     */
    ResponseDTO<Boolean> update(AttendanceRecordUpdateDTO updateDTO);

    /**
     * 删除考勤记录
     *
     * @param recordId 记录ID
     * @return 删除结果
     */
    ResponseDTO<Boolean> delete(Long recordId);

    /**
     * 批量删除考勤记录
     *
     * @param recordIds 记录ID列表
     * @return 删除结果
     */
    ResponseDTO<Integer> batchDelete(List<Long> recordIds);

    /**
     * 获取员工考勤统计
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getEmployeeStats(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门考勤统计
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计数据
     */
    ResponseDTO<Map<String, Object>> getDepartmentStats(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 批量重新计算考勤记录
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 处理结果
     */
    ResponseDTO<Integer> batchRecalculate(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取考勤异常列表
     *
     * @param employeeId    员工ID（可选）
     * @param departmentId  部门ID（可选）
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param exceptionType 异常类型（可选）
     * @return 异常列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getAbnormalRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate,
            String exceptionType);

    /**
     * 获取迟到记录列表
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 迟到记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getLateRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取早退记录列表
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 早退记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getEarlyLeaveRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取加班记录列表
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 加班记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getOvertimeRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取旷工记录列表
     *
     * @param employeeId   员工ID（可选）
     * @param departmentId 部门ID（可选）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 旷工记录列表
     */
    ResponseDTO<List<AttendanceRecordVO>> getAbsentRecords(Long employeeId,
            Long departmentId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 获取考勤月度汇总
     *
     * @param year         年份
     * @param month        月份
     * @param departmentId 部门ID（可选）
     * @return 月度汇总数据
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
     * @return 同步结果
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
     * @param format       导出格式（excel/pdf/csv）
     * @return 导出文件路径或数据
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
     * 验证打卡合法性
     *
     * @param punchDTO 打卡参数
     * @return 验证结果
     */
    ResponseDTO<Boolean> validatePunch(AttendancePunchDTO punchDTO);

    /**
     * 自动补全考勤记录
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 补全结果
     */
    ResponseDTO<Integer> autoCompleteRecords(Long employeeId,
            LocalDate startDate,
            LocalDate endDate);

    /**
     * 查询今日考勤状态
     *
     * @param employeeId 员工ID
     * @param date       日期
     * @return 今日考勤状态
     */
    ResponseDTO<AttendanceRecordVO> queryTodayAttendance(Long employeeId, LocalDate date);

    /**
     * 申请补卡
     *
     * @param request 补卡申请请求
     * @return 申请结果
     */
    ResponseDTO<String> applyMakeupPunch(MakeupPunchRequest request);

    /**
     * 微服务支持：检查服务健康状态
     * 基于现有项目改造，不创建新的微服务架构
     *
     * @return 服务健康状态信息
     */
    ResponseDTO<Map<String, Object>> checkServiceHealth();

    /**
     * 微服务支持：获取服务配置信息
     * 使用Nacos配置中心动态管理配置
     *
     * @param configKey 配置键
     * @return 配置值
     */
    ResponseDTO<String> getServiceConfig(String configKey);

    /**
     * 微服务支持：远程服务调用适配
     * 替代对sa-base.device服务的直接调用
     *
     * @param serviceName 服务名称
     * @param method      方法名称
     * @param parameters  参数
     * @return 调用结果
     */
    ResponseDTO<Object> callRemoteService(String serviceName, String method, Map<String, Object> parameters);
}
