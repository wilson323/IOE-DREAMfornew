package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.SmartScheduleResultEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 智能排班结果DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface SmartScheduleResultDao extends BaseMapper<SmartScheduleResultEntity> {
}
