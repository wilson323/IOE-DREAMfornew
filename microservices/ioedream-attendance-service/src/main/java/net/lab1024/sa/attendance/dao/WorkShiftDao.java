package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.WorkShiftEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 班次配置数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface WorkShiftDao extends BaseMapper<WorkShiftEntity> {

    /**
     * 根据班次类型查询班次
     */
    @Select("SELECT * FROM t_attendance_work_shift WHERE shift_type = #{shiftType} " +
            "AND deleted_flag = 0 ORDER BY sort_order ASC, shift_id ASC")
    List<WorkShiftEntity> selectByShiftType(@Param("shiftType") Integer shiftType);

    /**
     * 查询所有激活班次
     */
    @Select("SELECT * FROM t_attendance_work_shift WHERE deleted_flag = 0 ORDER BY sort_order ASC, shift_id ASC")
    List<WorkShiftEntity> selectAllActive();

    /**
     * 根据部门查询可用班次
     */
    @Select("SELECT ws.* FROM t_attendance_work_shift ws " +
            "LEFT JOIN t_attendance_department_shift ds ON ws.shift_id = ds.shift_id " +
            "WHERE ds.department_id = #{departmentId} AND ws.deleted_flag = 0 " +
            "ORDER BY ws.sort_order ASC, ws.shift_id ASC")
    List<WorkShiftEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 根据时间查找班次
     */
    @Select("SELECT * FROM t_attendance_work_shift " +
            "WHERE deleted_flag = 0 AND " +
            "( " +
            "  (is_overnight = 0 AND #{currentTime} >= start_time AND #{currentTime} <= end_time) OR " +
            "  (is_overnight = 1 AND (#{currentTime} >= start_time OR #{currentTime} <= end_time)) " +
            ") " +
            "ORDER BY sort_order ASC, shift_id ASC")
    List<WorkShiftEntity> selectByTime(@Param("currentTime") String currentTime);
}
