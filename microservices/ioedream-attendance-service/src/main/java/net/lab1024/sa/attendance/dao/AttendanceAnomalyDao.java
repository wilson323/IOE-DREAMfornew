package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.attendance.entity.AttendanceAnomalyEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤异常记录DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceAnomalyDao extends BaseMapper<AttendanceAnomalyEntity> {

    /**
     * 根据用户ID和考勤日期查询异常记录
     *
     * @param userId 用户ID
     * @param attendanceDate 考勤日期
     * @return 异常记录列表
     */
    List<AttendanceAnomalyEntity> selectByUserIdAndDate(@Param("userId") Long userId,
                                                        @Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 根据异常状态查询异常记录
     *
     * @param status 异常状态
     * @return 异常记录列表
     */
    List<AttendanceAnomalyEntity> selectByStatus(@Param("status") String status);

    /**
     * 根据部门ID和日期范围查询异常记录
     *
     * @param departmentId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 异常记录列表
     */
    List<AttendanceAnomalyEntity> selectByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                                                 @Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期的异常数量（按异常类型分组）
     *
     * @param attendanceDate 考勤日期
     * @return 统计结果
     */
    List<AttendanceAnomalyEntity> statisticsByDate(@Param("attendanceDate") LocalDate attendanceDate);

    /**
     * 查询用户的异常统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 异常统计列表
     */
    List<AttendanceAnomalyEntity> selectUserAnomalyStatistics(@Param("userId") Long userId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
}
