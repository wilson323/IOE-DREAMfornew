package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.video.domain.entity.DeviceAIEventEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备AI事件DAO
 * <p>
 * 边缘计算架构：接收设备上报的结构化AI事件
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Mapper
public interface DeviceAIEventDao extends BaseMapper<DeviceAIEventEntity> {

}
