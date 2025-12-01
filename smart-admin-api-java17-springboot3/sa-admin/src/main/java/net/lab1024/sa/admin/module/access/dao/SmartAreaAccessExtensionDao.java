package net.lab1024.sa.admin.module.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.access.domain.entity.SmartAreaAccessExtensionEntity;

/**
 * 区域门禁扩展DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责区域门禁扩展数据访问
 * - 提供缓存层需要的查询方法
 * - 关联基础区域表查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface SmartAreaAccessExtensionDao extends BaseMapper<SmartAreaAccessExtensionEntity> {

    /**
     * 根据区域ID查询门禁扩展信息
     * 关联查询基础区域信息，确保数据一致性
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.area_id = #{areaId} AND a.deleted_flag = 0")
    SmartAreaAccessExtensionEntity selectByAreaIdWithBaseInfo(@Param("areaId") Long areaId);

    /**
     * 查询指定访问策略的区域列表
     * 关联基础区域表，支持按访问策略过滤
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_policy = #{accessPolicy} AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectByAccessPolicy(@Param("accessPolicy") String accessPolicy);

    /**
     * 查询启用自动权限分配的区域列表
     * 关联基础区域表，过滤启用自动权限分配的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.auto_assign_permission = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectAutoAssignPermissionAreas();

    /**
     * 查询启用视频监控的区域列表
     * 关联基础区域表，过滤启用视频监控的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.video_surveillance_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectVideoSurveillanceAreas();

    /**
     * 查询启用实时追踪的区域列表
     * 关联基础区域表，过滤启用实时追踪的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.real_time_tracking_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectRealTimeTrackingAreas();

    /**
     * 查询启用紧急疏散的区域列表
     * 关联基础区域表，过滤启用紧急疏散的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.emergency_evacuation_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectEmergencyEvacuationAreas();

    /**
     * 查询指定安全等级的区域列表
     * 关联基础区域表，按安全等级过滤区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.security_level = #{securityLevel} AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectBySecurityLevel(@Param("securityLevel") String securityLevel);

    /**
     * 查询指定访问权限级别的区域列表
     * 关联基础区域表，按访问权限级别过滤区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level >= #{minAccessLevel} AND a.deleted_flag = 0 " +
            "ORDER BY e.access_level DESC, a.sort_order ASC")
    List<SmartAreaAccessExtensionEntity> selectByMinAccessLevel(@Param("minAccessLevel") Integer minAccessLevel);

    /**
     * 查询需要特殊授权的区域列表
     * 关联基础区域表，过滤需要特殊授权的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.special_auth_required = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectSpecialAuthRequiredAreas();

    /**
     * 统计门禁区域数量
     * 关联基础区域表，统计启用状态的区域
     */
    @Select("SELECT COUNT(*) FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.deleted_flag = 0")
    Long countTotalAccessAreas();

    /**
     * 根据访问策略统计区域数量
     * 关联基础区域表，按访问策略分组统计
     */
    @Select("SELECT e.access_policy, COUNT(*) as count " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.deleted_flag = 0 " +
            "GROUP BY e.access_policy ORDER BY count DESC")
    List<Object> countAccessAreasByPolicy();

    /**
     * 根据安全等级统计区域数量
     * 关联基础区域表，按安全等级分组统计
     */
    @Select("SELECT e.security_level, COUNT(*) as count " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.deleted_flag = 0 " +
            "GROUP BY e.security_level ORDER BY count DESC")
    List<Object> countAccessAreasBySecurityLevel();

    /**
     * 查询启用反潜规则的区域列表
     * 关联基础区域表，过滤启用反潜规则的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.anti_passback_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectAntiPassbackAreas();

    /**
     * 查询启用容量监控的区域列表
     * 关联基础区域表，过滤启用容量监控的区域
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.capacity_monitoring_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<SmartAreaAccessExtensionEntity> selectCapacityMonitoringAreas();

    /**
     * 批量查询多个区域的扩展信息
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param areaIds 区域ID列表
     * @return 区域扩展信息列表
     */
    @Select("<script>" +
            "SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_smart_area_access_extension e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.deleted_flag = 0 " +
            "AND e.area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY a.sort_order ASC, a.create_time ASC" +
            "</script>")
    List<SmartAreaAccessExtensionEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);
}