package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 加班记录DAO接口
 * <p>
 * 提供加班记录的数据访问操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceOvertimeRecordDao extends BaseMapper<AttendanceOvertimeRecordEntity> {

    /**
     * 根据申请ID查询加班记录列表
     *
     * @param applyId 申请ID
     * @return 加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE apply_id = #{applyId} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByApplyId(@Param("applyId") Long applyId);

    /**
     * 根据申请编号查询加班记录列表
     *
     * @param applyNo 申请编号
     * @return 加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE apply_no = #{applyNo} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByApplyNo(@Param("applyNo") String applyNo);

    /**
     * 根据员工ID查询加班记录列表
     *
     * @param employeeId 员工ID
     * @return 加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE employee_id = #{employeeId} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * 根据部门ID查询加班记录列表
     *
     * @param departmentId 部门ID
     * @return 加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE department_id = #{departmentId} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据加班类型查询记录列表
     *
     * @param overtimeType 加班类型
     * @return 记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE overtime_type = #{overtimeType} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByOvertimeType(@Param("overtimeType") String overtimeType);

    /**
     * 查询指定日期范围内的加班记录
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByDateRange(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 查询夜班加班记录
     *
     * @return 夜班加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE is_night_shift = 1 AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectNightShiftRecords();

    /**
     * 查询周末加班记录
     *
     * @return 周末加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE is_weekend = 1 AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectWeekendRecords();

    /**
     * 查询法定节假日加班记录
     *
     * @return 法定节假日加班记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE is_holiday = 1 AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectHolidayRecords();

    /**
     * 根据补偿方式查询记录
     *
     * @param compensationType 补偿方式（PAY-加班费 LEAVE-调休）
     * @return 记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE compensation_type = #{compensationType} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByCompensationType(@Param("compensationType") String compensationType);

    /**
     * 查询待补偿的加班记录
     *
     * @return 待补偿记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE compensation_status = 'PENDING' AND deleted_flag = 0 ORDER BY overtime_date ASC")
    List<AttendanceOvertimeRecordEntity> selectPendingCompensation();

    /**
     * 根据补偿状态查询记录
     *
     * @param compensationStatus 补偿状态
     * @return 记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE compensation_status = #{compensationStatus} AND deleted_flag = 0 ORDER BY overtime_date DESC")
    List<AttendanceOvertimeRecordEntity> selectByCompensationStatus(@Param("compensationStatus") String compensationStatus);

    /**
     * 统计员工总加班时长（正常时长）
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 总正常加班时长（小时）
     */
    @Select("SELECT SUM(IFNULL(normal_hours, 0)) FROM t_attendance_overtime_record WHERE employee_id = #{employeeId} AND overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0")
    BigDecimal sumNormalHoursByEmployee(@Param("employeeId") Long employeeId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    /**
     * 统计员工总折算时长（倍率后）
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 总折算加班时长（小时）
     */
    @Select("SELECT SUM(IFNULL(multiplied_hours, 0)) FROM t_attendance_overtime_record WHERE employee_id = #{employeeId} AND overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0")
    BigDecimal sumMultipliedHoursByEmployee(@Param("employeeId") Long employeeId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 统计部门总加班时长
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 总加班时长（小时）
     */
    @Select("SELECT SUM(IFNULL(overtime_hours, 0)) FROM t_attendance_overtime_record WHERE department_id = #{departmentId} AND overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0")
    BigDecimal sumOvertimeHoursByDepartment(@Param("departmentId") Long departmentId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 统计员工夜班加班时长
     *
     * @param employeeId 员工ID
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 夜班加班总时长（小时）
     */
    @Select("SELECT SUM(IFNULL(overtime_hours, 0)) FROM t_attendance_overtime_record WHERE employee_id = #{employeeId} AND is_night_shift = 1 AND overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0")
    BigDecimal sumNightShiftHoursByEmployee(@Param("employeeId") Long employeeId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    /**
     * 查询加班时长超过指定阈值的记录
     *
     * @param thresholdHours 阈值（小时）
     * @return 记录列表
     */
    @Select("SELECT * FROM t_attendance_overtime_record WHERE overtime_hours > #{thresholdHours} AND deleted_flag = 0 ORDER BY overtime_hours DESC")
    List<AttendanceOvertimeRecordEntity> selectByHoursThreshold(@Param("thresholdHours") BigDecimal thresholdHours);

    /**
     * 统计各加班类型的时长汇总
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果（包含加班类型、总时长）
     */
    @Select("SELECT overtime_type, SUM(IFNULL(overtime_hours, 0)) as total_hours FROM t_attendance_overtime_record WHERE overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0 GROUP BY overtime_type")
    List<java.util.Map<String, Object>> statisticsByOvertimeType(@Param("startDate") LocalDate startDate,
                                                                  @Param("endDate") LocalDate endDate);

    /**
     * 统计各部门的加班时长排名
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param limit     返回记录数
     * @return 统计结果（包含部门ID、部门名称、总时长）
     */
    @Select("SELECT department_id, department_name, SUM(IFNULL(overtime_hours, 0)) as total_hours FROM t_attendance_overtime_record WHERE overtime_date BETWEEN #{startDate} AND #{endDate} AND deleted_flag = 0 GROUP BY department_id, department_name ORDER BY total_hours DESC LIMIT #{limit}")
    List<java.util.Map<String, Object>> statisticsOvertimeByDepartment(@Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate,
                                                                       @Param("limit") Integer limit);
}
