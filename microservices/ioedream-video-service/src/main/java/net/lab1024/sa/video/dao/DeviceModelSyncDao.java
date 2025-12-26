package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.DeviceModelSyncEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备模型同步DAO
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Mapper
public interface DeviceModelSyncDao extends BaseMapper<DeviceModelSyncEntity> {
}
