package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.ScheduleTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 排班模板数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface ScheduleTemplateDao extends BaseMapper<ScheduleTemplateEntity> {

    /**
     * 根据模板类型查询模板
     */
    @Select("SELECT * FROM t_attendance_schedule_template WHERE template_type = #{templateType} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY template_id DESC")
    List<ScheduleTemplateEntity> selectByTemplateType(@Param("templateType") String templateType);

    /**
     * 根据部门ID查询模板
     */
    @Select("SELECT * FROM t_attendance_schedule_template WHERE department_id = #{departmentId} " +
            "AND status = 1 AND deleted_flag = 0 ORDER BY template_id DESC")
    List<ScheduleTemplateEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    /**
     * 查询所有激活模板
     */
    @Select("SELECT * FROM t_attendance_schedule_template WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY template_type, template_id DESC")
    List<ScheduleTemplateEntity> selectAllActive();

    /**
     * 更新模板应用次数
     */
    @Update("UPDATE t_attendance_schedule_template SET apply_count = apply_count + 1, " +
            "last_applied_time = NOW() WHERE template_id = #{templateId}")
    int incrementApplyCount(@Param("templateId") Long templateId);

    /**
     * 查询部门可用模板
     */
    @Select("SELECT * FROM t_attendance_schedule_template WHERE " +
            "(department_id = #{departmentId} OR department_id IS NULL) " +
            "AND status = 1 AND deleted_flag = 0 " +
            "ORDER BY department_id DESC, template_id DESC")
    List<ScheduleTemplateEntity> selectAvailableForDepartment(@Param("departmentId") Long departmentId);
}
