package net.lab1024.sa.consume.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.domain.entity.DeviceEntity;

/**
 * 设备DAO接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {

    /**
     * 根据设备ID查询设备
     *
     * @param deviceId 设备ID
     * @return 设备信息
     */
    @Select("SELECT * FROM device WHERE device_id = #{deviceId} AND deleted = 0")
    DeviceEntity selectById(@Param("deviceId") Long deviceId);

    /**
     * 统计设备数量
     *
     * @param condition 查询条件
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM device WHERE deleted = 0")
    Integer selectCount(@Param("condition") Object condition);
}