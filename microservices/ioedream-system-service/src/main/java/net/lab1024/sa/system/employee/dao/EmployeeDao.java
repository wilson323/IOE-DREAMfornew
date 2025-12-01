package net.lab1024.sa.system.employee.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.system.employee.domain.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工DAO
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {
}