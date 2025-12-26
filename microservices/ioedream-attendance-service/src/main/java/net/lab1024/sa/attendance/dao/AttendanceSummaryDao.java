package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import net.lab1024.sa.attendance.entity.AttendanceSummaryEntity;

import java.util.List;

/**
 * 考勤汇总数据访问接口
 * <p>
 * 提供考勤汇总数据的CRUD操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Mapper
public interface AttendanceSummaryDao extends BaseMapper<AttendanceSummaryEntity> {

    /**
     * 查询员工指定月份的汇总记录
     *
     * @param employeeId 员工ID
     * @param summaryMonth 汇总月份
     * @return 汇总记录
     */
    @Select("SELECT * FROM t_attendance_summary " +
            "WHERE employee_id = #{employeeId} " +
            "AND summary_month = #{summaryMonth} " +
            "AND status = 1 " +
            "LIMIT 1")
    AttendanceSummaryEntity selectByEmployeeAndMonth(
            @Param("employeeId") Long employeeId,
            @Param("summaryMonth") String summaryMonth
    );

    /**
     * 查询部门指定月份的所有汇总记录
     *
     * @param departmentId 部门ID
     * @param summaryMonth 汇总月份
     * @return 汇总记录列表
     */
    @Select("SELECT * FROM t_attendance_summary " +
            "WHERE department_id = #{departmentId} " +
            "AND summary_month = #{summaryMonth} " +
            "AND status = 1")
    List<AttendanceSummaryEntity> selectByDepartmentAndMonth(
            @Param("departmentId") Long departmentId,
            @Param("summaryMonth") String summaryMonth
    );

    /**
     * 查询指定月份的所有汇总记录
     *
     * @param summaryMonth 汇总月份
     * @return 汇总记录列表
     */
    @Select("SELECT * FROM t_attendance_summary " +
            "WHERE summary_month = #{summaryMonth} " +
            "AND status = 1")
    List<AttendanceSummaryEntity> selectByMonth(
            @Param("summaryMonth") String summaryMonth
    );
}
