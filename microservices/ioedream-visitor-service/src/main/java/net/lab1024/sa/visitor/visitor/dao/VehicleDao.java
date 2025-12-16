package net.lab1024.sa.visitor.visitor.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.visitor.visitor.entity.VehicleEntity;

/**
 * 访客车辆数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-04
 */
@Mapper
public interface VehicleDao extends BaseMapper<VehicleEntity> {
}


