package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.attendance.AttendanceRecordEntity;

/**
 * 考勤记录DAO
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
public interface AttendanceRecordDao extends BaseMapper<AttendanceRecordEntity> {

    /**
     * 根据员工ID和日期查询考勤记录
     * <p>
     * 使用MyBatis-Plus的LambdaQueryWrapper实现
     * </p>
     *
     * @param employeeId 员工ID（对应userId）
     * @param date 日期
     * @return 考勤记录列表
     */
    default java.util.List<AttendanceRecordEntity> selectByEmployeeAndDate(Long employeeId, java.time.LocalDate date) {
        return this.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecordEntity>()
                .eq(AttendanceRecordEntity::getUserId, employeeId)
                .eq(AttendanceRecordEntity::getAttendanceDate, date)
                .orderByAsc(AttendanceRecordEntity::getPunchTime)
        );
    }
}



