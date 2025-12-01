package net.lab1024.sa.admin.module.hr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.hr.domain.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {
}

