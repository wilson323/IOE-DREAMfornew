package net.lab1024.sa.device.comm.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.device.comm.entity.DeviceCommLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备通讯日志Dao
 * <p>
 * 记录所有设备通讯的日志信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Mapper
public interface DeviceCommLogDao extends BaseMapper<DeviceCommLogEntity> {

    // 基础CRUD由MyBatis-Plus提供
    // 此处可添加自定义查询方法
}
