package net.lab1024.sa.common.organization.dao;

import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 区域设备关联数据访问层
 * 负责处理区域与设备关联关系的数据操作
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Mapper
public interface AreaDeviceDao {

    /**
     * 插入区域设备关联
     *
     * @param entity 区域设备关联实体
     * @return 插入的行数
     */
    int insert(AreaDeviceEntity entity);

    /**
     * 根据ID更新区域设备关联
     *
     * @param entity 区域设备关联实体
     * @return 更新的行数
     */
    int updateById(AreaEntity entity);

    /**
     * 根据ID删除区域设备关联
     *
     * @param relationId 关联ID
     * @return 删除的行数
     */
    int deleteById(@Param("relationId") String relationId);

    /**
     * 根据区域ID和设备ID查询关联
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 区域设备关联实体
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND device_id = #{deviceId} " +
            "AND deleted_flag = 0 AND enabled = 1")
    AreaDeviceEntity selectByAreaIdAndDeviceId(@Param("areaId") Long areaId, @Param("deviceId") String deviceId);

    /**
     * 根据区域ID查询所有设备关联
     *
     * @param areaId 区域ID
     * @return 区域设备关联列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY priority ASC, create_time DESC")
    List<AreaDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据区域ID和设备类型查询设备关联
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 区域设备关联列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND device_type = #{deviceType} " +
            "AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY priority ASC, create_time DESC")
    List<AreaDeviceEntity> selectByAreaIdAndDeviceType(@Param("areaId") Long areaId, @Param("deviceType") Integer deviceType);

    /**
     * 根据区域ID和业务模块查询设备关联
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 区域设备关联列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND business_module = #{businessModule} " +
            "AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY priority ASC, create_time DESC")
    List<AreaDeviceEntity> selectByAreaIdAndBusinessModule(@Param("areaId") Long areaId, @Param("businessModule") String businessModule);

    /**
     * 查询区域的主设备
     *
     * @param areaId 区域ID
     * @return 主设备列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND priority = 1 " +
            "AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY create_time DESC")
    List<AreaDeviceEntity> selectPrimaryDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据设备ID查询所有区域关联
     *
     * @param deviceId 设备ID
     * @return 区域设备关联列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE device_id = #{deviceId} AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY create_time DESC")
    List<AreaDeviceEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据设备编码查询所有区域关联
     *
     * @param deviceCode 设备编码
     * @return 区域设备关联列表
     */
    @Select("SELECT * FROM t_area_device_relation " +
            "WHERE device_code = #{deviceCode} AND deleted_flag = 0 AND enabled = 1 " +
            "ORDER BY create_time DESC")
    List<AreaDeviceEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param relationStatus 关联状态
     * @return 更新的行数
     */
    @Update("UPDATE t_area_device_relation " +
            "SET relation_status = #{relationStatus}, update_time = NOW() " +
            "WHERE relation_id = #{relationId}")
    int updateRelationStatus(@Param("relationId") String relationId, @Param("relationStatus") Integer relationStatus);

    /**
     * 批量更新区域设备状态
     *
     * @param areaId 区域ID
     * @param relationStatus 关联状态
     * @return 更新的行数
     */
    @Update("UPDATE t_area_device_relation " +
            "SET relation_status = #{relationStatus}, update_time = NOW() " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 AND enabled = 1")
    int batchUpdateRelationStatusByAreaId(@Param("areaId") Long areaId, @Param("relationStatus") Integer relationStatus);

    /**
     * 检查设备是否在区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) > 0 FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND device_id = #{deviceId} " +
            "AND deleted_flag = 0 AND enabled = 1")
    boolean isDeviceInArea(@Param("areaId") Long areaId, @Param("deviceId") String deviceId);

    /**
     * 统计区域设备数量
     *
     * @param areaId 区域ID
     * @return 设备数量
     */
    @Select("SELECT COUNT(*) FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 AND enabled = 1")
    int countDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 统计区域在线设备数量
     *
     * @param areaId 区域ID
     * @return 在线设备数量
     */
    @Select("SELECT COUNT(*) FROM t_area_relation " +
            "WHERE area_id = #{areaId} AND relation_status IN (1, 2) " +
            "AND deleted_flag = 0 AND enabled = 1")
    int countOnlineDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 过期设备关联
     *
     * @param deviceId 设备ID
     * @return 过期的关联数量
     */
    @Update("UPDATE t_area_device_relation " +
            "SET expire_time = NOW(), relation_status = 5, update_time = NOW() " +
            "WHERE device_id = #{deviceId} AND expire_time IS NULL " +
            "AND deleted_flag = 0 AND enabled = 1")
    int expireDeviceRelations(@Param("deviceId") String deviceId);

    /**
     * 查询设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @return 业务属性JSON
     */
    @Select("SELECT business_attributes FROM t_area_device_relation " +
            "WHERE device_id = #{deviceId} AND area_id = #{areaId} " +
            "AND deleted_flag = 0 AND enabled = 1")
    String selectDeviceBusinessAttributes(@Param("deviceId") String deviceId, @Param("areaId") Long areaId);

    /**
     * 根据用户权限查询设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块
     * @return 设备关联列表
     */
    @Select("SELECT adr.* FROM t_area_device_relation adr " +
            "INNER JOIN t_area a ON adr.area_id = a.area_id " +
            "INNER JOIN t_user_area_permission uap ON a.area_id = uap.area_id " +
            "WHERE uap.user_id = #{userId} AND adr.business_module = #{businessModule} " +
            "AND adr.deleted_flag = 0 AND adr.enabled = 1 " +
            "AND a.deleted_flag = 0 AND a.enabled = 1 " +
            "AND uap.deleted_flag = 0 AND uap.enabled = 1 " +
            "ORDER BY adr.priority ASC, adr.create_time DESC")
    List<AreaDeviceEntity> selectDevicesByUserPermission(@Param("userId") Long userId, @Param("businessModule") String businessModule);

    /**
     * 按设备类型统计区域设备
     *
     * @param areaId 区域ID
     * @return 统计结果
     */
    @Select("SELECT device_type, COUNT(*) as device_count, " +
            "SUM(CASE WHEN relation_status = 1 THEN 1 ELSE 0 END) as online_count " +
            "FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 AND enabled = 1 " +
            "GROUP BY device_type")
    List<Map<String, Object>> selectDeviceStatisticsByAreaId(@Param("areaId") Long areaId);

    /**
     * 按业务模块统计设备分布
     *
     * @param businessModule 业务模块
     * @return 分布统计
     */
    @Select("SELECT adr.device_type, COUNT(DISTINCT adr.area_id) as area_count, COUNT(*) as device_count " +
            "FROM t_area_device_relation adr " +
            "INNER JOIN t_area a ON adr.area_id = a.area_id " +
            "WHERE adr.business_module = #{businessModule} " +
            "AND adr.deleted_flag = 0 AND adr.enabled = 1 " +
            "AND a.deleted_flag = 0 AND a.enabled = 1 " +
            "GROUP BY adr.device_type")
    List<Map<String, Object>> selectDeviceDistributionByModule(@Param("businessModule") String businessModule);

    /**
     * 按关联状态统计区域设备
     *
     * @param areaId 区域ID
     * @return 状态统计
     */
    @Select("SELECT relation_status, COUNT(*) as status_count " +
            "FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 AND enabled = 1 " +
            "GROUP BY relation_status")
    List<Map<String, Object>> selectDeviceStatusStatisticsByAreaId(@Param("areaId") Long areaId);

    /**
     * 获取区域设备健康状态统计
     *
     * @param areaId 区域ID
     * @return 健康状态统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_devices, " +
            "SUM(CASE WHEN relation_status = 1 THEN 1 ELSE 0 END) as normal_devices, " +
            "SUM(CASE WHEN relation_status = 2 THEN 1 ELSE 0 END) as maintenance_devices, " +
            "SUM(CASE WHEN relation_status = 3 THEN 1 ELSE 0 END) as fault_devices, " +
            "SUM(CASE WHEN relation_status = 4 THEN 1 ELSE 0 END) as offline_devices, " +
            "SUM(CASE WHEN relation_status = 5 THEN 1 ELSE 0 END) as disabled_devices " +
            "FROM t_area_device_relation " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "GROUP BY area_id")
    Map<String, Object> selectDeviceHealthStatisticsByAreaId(@Param("areaId") Long areaId);

    /**
     * 获取所有模块的设备分布统计
     *
     * @return 各模块设备分布
     */
    @Select("SELECT adr.business_module, " +
            "adr.device_type, " +
            "COUNT(DISTINCT adr.area_id) as area_count, " +
            "COUNT(*) as device_count " +
            "FROM t_area_device_relation adr " +
            "INNER JOIN t_area a ON adr.area_id = a.area_id " +
            "WHERE adr.deleted_flag = 0 AND adr.enabled = 1 " +
            "AND a.deleted_flag = 0 AND a.enabled = 1 " +
            "GROUP BY adr.business_module, adr.device_type " +
            "ORDER BY adr.business_module, adr.device_type")
    List<Map<String, Object>> selectAllModuleDeviceDistribution();

    /**
     * 批量移动设备（区域变更）
     *
     * @param oldAreaId 原区域ID
     * @param newAreaId 新区域ID
     * @param deviceId 设备ID
     * @return 移动的设备数量
     */
    @Update("UPDATE t_area_device_relation " +
            "SET area_id = #{newAreaId}, update_time = NOW() " +
            "WHERE device_id = #{deviceId} AND area_id = #{oldAreaId} " +
            "AND deleted_flag = 0 AND enabled = 1")
    int batchMoveDeviceToNewArea(@Param("oldAreaId") Long oldAreaId, @Param("newAreaId") Long newAreaId, @Param("deviceId") String deviceId);

    /**
     * 设置设备业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID
     * @param businessAttributes 业务属性JSON
     * @return 更新的行数
     */
    @Update("UPDATE t_area_device_relation " +
            "SET business_attributes = #{businessAttributes}, update_time = NOW() " +
            "WHERE device_id = #{deviceId} AND area_id = #{areaId} " +
            "AND deleted_flag = 0 AND enabled = 1")
    int updateDeviceBusinessAttributes(@Param("deviceId") String deviceId, @Param("areaId") Long areaId, @Param("businessAttributes") String businessAttributes);
}