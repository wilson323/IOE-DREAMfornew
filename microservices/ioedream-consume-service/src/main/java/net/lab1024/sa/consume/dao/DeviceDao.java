package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备数据访问对象
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {
}
