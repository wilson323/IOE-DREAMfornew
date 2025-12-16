package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.attendance.entity.AttendanceLeaveEntity;

/**
 * 考勤请假DAO
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AttendanceLeaveDao extends BaseMapper<AttendanceLeaveEntity> {

    /**
     * 根据请假申请编号查询
     *
     * @param leaveNo 请假申请编号
     * @return 请假实体
     */
    AttendanceLeaveEntity selectByLeaveNo(@Param("leaveNo") String leaveNo);
}



