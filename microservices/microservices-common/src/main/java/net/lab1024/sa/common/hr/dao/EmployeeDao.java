package net.lab1024.sa.common.hr.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.hr.entity.EmployeeEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeDao extends BaseMapper<EmployeeEntity> {
}