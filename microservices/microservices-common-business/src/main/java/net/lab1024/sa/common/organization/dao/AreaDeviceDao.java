package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域设备关联数据访问接口
 * <p>
 * 对应数据库表: t_area_device_relation
 * 提供区域设备关联的CRUD操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Mapper
public interface AreaDeviceDao extends BaseMapper<AreaDeviceEntity> {

    /**
     * 根据区域ID和设备ID查询关联关系
     *
     * @param areaId   区域ID
     * @param deviceId 设备ID
     * @return 区域设备关联实体
     */
    AreaDeviceEntity selectByAreaIdAndDeviceId(@Param("areaId") Long areaId, @Param("deviceId") String deviceId);

    /**
     * 根据区域ID查询该区域的所有设备关联
     *
     * @param areaId 区域ID
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据设备ID查询该设备的所有区域关联
     *
     * @param deviceId 设备ID
     * @return 区域关联列表
     */
    List<AreaDeviceEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据区域ID和设备类型查询设备关联
     *
     * @param areaId    区域ID
     * @param deviceType 设备类型
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> selectByAreaIdAndDeviceType(@Param("areaId") Long areaId, @Param("deviceType") Integer deviceType);

    /**
     * 根据区域ID和业务模块查询设备关联
     *
     * @param areaId        区域ID
     * @param businessModule 业务模块
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> selectByAreaIdAndBusinessModule(@Param("areaId") Long areaId, @Param("businessModule") String businessModule);

    /**
     * 根据区域ID和关联状态查询设备关联
     *
     * @param areaId         区域ID
     * @param relationStatus 关联状态
     * @return 设备关联列表
     */
    List<AreaDeviceEntity> selectByAreaIdAndStatus(@Param("areaId") Long areaId, @Param("relationStatus") Integer relationStatus);
}
