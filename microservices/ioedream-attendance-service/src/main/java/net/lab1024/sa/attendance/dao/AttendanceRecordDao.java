package net.lab1024.sa.attendance.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;

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
}

