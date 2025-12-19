package net.lab1024.sa.attendance.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.attendance.entity.AttendanceRuleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤规则DAO
 * <p>
 * 负责考勤规则（t_attendance_rule 等）的数据访问。
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Mapper
public interface AttendanceRuleDao extends BaseMapper<AttendanceRuleEntity> {
}


