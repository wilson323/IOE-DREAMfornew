package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 区域设备关联数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Mapper
@Transactional(readOnly = true)
public interface AreaDeviceDao extends BaseMapper<AreaDeviceEntity> {

    /**
     * 根据区域ID查询设备列表
     *
     * @param areaId 区域ID
     * @return 设备关联列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        """)
    List<AreaDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据设备ID查询所属区域列表
     *
     * @param deviceId 设备ID
     * @return 区域关联列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE device_id = #{deviceId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        """)
    List<AreaDeviceEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据设备编码查询区域关联
     *
     * @param deviceCode 设备编码
     * @return 区域关联列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE device_code = #{deviceCode}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        """)
    List<AreaDeviceEntity> selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据区域ID和设备类型查询设备
     *
     * @param areaId 区域ID
     * @param deviceType 设备类型
     * @return 设备关联列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND device_type = #{deviceType}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        """)
    List<AreaDeviceEntity> selectByAreaIdAndDeviceType(@Param("areaId") Long areaId,
                                                         @Param("deviceType") Integer deviceType);

    /**
     * 根据区域ID和业务模块查询设备
     *
     * @param areaId 区域ID
     * @param businessModule 业务模块
     * @return 设备关联列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND business_module = #{businessModule}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        """)
    List<AreaDeviceEntity> selectByAreaIdAndBusinessModule(@Param("areaId") Long areaId,
                                                               @Param("businessModule") String businessModule);

    /**
     * 查询指定区域的主设备
     *
     * @param areaId 区域ID
     * @return 主设备列表
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND priority = 1
          AND enabled = true
          AND relation_status = 1
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY create_time DESC
        """)
    List<AreaDeviceEntity> selectPrimaryDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询区域的在线设备数量
     *
     * @param areaId 区域ID
     * @return 在线设备数量
     */
    @Select("""
        SELECT COUNT(*)
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND relation_status = 1
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        """)
    int countOnlineDevicesByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询区域的设备总数（按类型分组）
     *
     * @param areaId 区域ID
     * @return 设备数量统计
     */
    @Select("""
        SELECT
            device_type,
            device_sub_type,
            COUNT(*) as device_count,
            SUM(CASE WHEN relation_status = 1 THEN 1 ELSE 0 END) as online_count,
            SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) as primary_count
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        GROUP BY device_type, device_sub_type
        ORDER BY device_type, device_sub_type
        """)
    List<Map<String, Object>> selectDeviceStatisticsByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询业务模块的设备分布统计
     *
     * @param businessModule 业务模块
     * @return 设备分布统计
     */
    @Select("""
        SELECT
            device_type,
            COUNT(*) as device_count,
            COUNT(DISTINCT area_id) as area_count
        FROM t_area_device_relation
        WHERE business_module = #{businessModule}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        GROUP BY device_type
        ORDER BY device_type
        """)
    List<Map<String, Object>> selectDeviceDistributionByModule(@Param("businessModule") String businessModule);

    /**
     * 根据用户权限查询可访问的设备
     *
     * @param userId 用户ID
     * @param businessModule 业务模块（可选）
     * @return 设备列表
     */
    @Select("""
        SELECT DISTINCT adr.*
        FROM t_area_device_relation adr
        INNER JOIN t_user_area_permission uap ON adr.area_id = uap.area_id
        WHERE uap.user_id = #{userId}
          AND uap.enabled = true
          AND adr.enabled = true
          AND (adr.expire_time IS NULL OR adr.expire_time > NOW())
          AND (#{businessModule} IS NULL OR adr.business_module = #{businessModule})
        ORDER BY adr.priority ASC, adr.create_time DESC
        """)
    List<AreaDeviceEntity> selectDevicesByUserPermission(@Param("userId") Long userId,
                                                             @Param("businessModule") String businessModule);

    /**
     * 根据区域ID和设备ID查询设备关联
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 设备关联信息
     */
    @Select("""
        SELECT *
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND device_id = #{deviceId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        ORDER BY priority ASC, create_time DESC
        LIMIT 1
        """)
    AreaDeviceEntity selectByAreaIdAndDeviceId(@Param("areaId") Long areaId,
                                               @Param("deviceId") String deviceId);

    /**
     * 检查设备是否在指定区域中
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 是否存在
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM t_area_device_relation
        WHERE area_id = #{areaId}
          AND device_id = #{deviceId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        """)
    boolean isDeviceInArea(@Param("areaId") Long areaId,
                           @Param("deviceId") String deviceId);

    /**
     * 查询设备的业务属性
     *
     * @param deviceId 设备ID
     * @param areaId 区域ID（可选）
     * @return 业务属性
     */
    @Select("""
        SELECT business_attributes
        FROM t_area_device_relation
        WHERE device_id = #{deviceId}
          AND (#{areaId} IS NULL OR area_id = #{areaId})
          AND enabled = true
          AND business_attributes IS NOT NULL
        LIMIT 1
        """)
    String selectDeviceBusinessAttributes(@Param("deviceId") String deviceId,
                                             @Param("areaId") Long areaId);

    /**
     * 更新设备关联状态
     *
     * @param relationId 关联ID
     * @param status 状态
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_area_device_relation
        SET relation_status = #{status},
            update_time = NOW()
        WHERE relation_id = #{relationId}
        """)
    int updateRelationStatus(@Param("relationId") String relationId,
                              @Param("status") Integer status);

    /**
     * 批量更新设备的关联状态
     *
     * @param areaId 区域ID
     * @param status 状态
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_area_device_relation
        SET relation_status = #{status},
            update_time = NOW()
        WHERE area_id = #{areaId}
          AND enabled = true
        """)
    int batchUpdateRelationStatusByAreaId(@Param("areaId") Long areaId,
                                         @Param("status") Integer status);

    /**
     * 设置设备为过期状态
     *
     * @param deviceId 设备ID
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_area_device_relation
        SET expire_time = NOW(),
            update_time = NOW()
        WHERE device_id = #{deviceId}
          AND enabled = true
        """)
    int expireDeviceRelations(@Param("deviceId") String deviceId);
}
