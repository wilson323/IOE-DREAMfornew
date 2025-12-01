package net.lab1024.sa.system.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.domain.entity.UnifiedDeviceEntity;

/**
 * 统一设备数据访问接口
 *
 * @author IOE-DREAM Team
 */
@Mapper
public interface UnifiedDeviceDao extends BaseMapper<UnifiedDeviceEntity> {
}
