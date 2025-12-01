package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AccessAreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门禁区域DAO（重构版）
 * <p>
 * 严格遵循repowiki规范和扩展表架构：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 使用扩展表架构：t_area (基础表) + t_access_area_ext (扩展表)
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责门禁区域数据访问
 * - 提供缓存层需要的查询方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @updated 2025-11-25 重构为扩展表架构，修复表名和JOIN查询
 */
@Mapper
public interface AccessAreaDao extends BaseMapper<AccessAreaEntity> {

    /**
     * 查询根级门禁区域列表
     * 使用扩展表架构：JOIN基础区域表和门禁扩展表
     */
    @Select("SELECT " +
            "a.area_id, a.area_code, a.area_name, a.area_type, a.parent_id, a.path, a.level, " +
            "a.sort_order, a.status, a.longitude, a.latitude, a.area_size, a.capacity, " +
            "a.description, a.map_image, a.remark, a.create_time, a.update_time, " +
            "a.create_user_id, a.update_user_id, a.deleted_flag, a.version, " +
            "e.access_enabled, e.access_level, e.access_mode, e.special_auth_required, " +
            "e.valid_time_start, e.valid_time_end, e.valid_weekdays, e.device_count, " +
            "e.guard_required, e.time_restrictions, e.visitor_allowed, e.emergency_access, " +
            "e.monitoring_enabled, e.alert_config " +
            "FROM t_area a " +
            "LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id " +
            "WHERE a.parent_id = 0 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<AccessAreaEntity> selectRootAreas();

    /**
     * 查询子区域ID列表
     */
    @Select("SELECT area_id FROM t_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Long> selectChildrenIds(@Param("parentId") Long parentId);

    /**
     * 查询指定路径上的所有区域ID
     */
    @Select("SELECT area_id FROM t_area WHERE path LIKE CONCAT('%,', #{areaId}, ',%') AND deleted_flag = 0")
    List<Long> selectDescendantIds(@Param("areaId") Long areaId);

    /**
     * 批量查询区域编码是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_area WHERE area_code IN " +
            "<foreach collection='areaCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND deleted_flag = 0" +
            "</script>")
    Long countByAreaCodes(@Param("areaCodes") List<String> areaCodes);

    /**
     * 根据编码查询门禁区域（包含扩展信息）
     */
    @Select("SELECT " +
            "a.area_id, a.area_code, a.area_name, a.area_type, a.parent_id, a.path, a.level, " +
            "a.sort_order, a.status, a.longitude, a.latitude, a.area_size, a.capacity, " +
            "a.description, a.map_image, a.remark, a.create_time, a.update_time, " +
            "a.create_user_id, a.update_user_id, a.deleted_flag, a.version, " +
            "e.access_enabled, e.access_level, e.access_mode, e.special_auth_required, " +
            "e.valid_time_start, e.valid_time_end, e.valid_weekdays, e.device_count, " +
            "e.guard_required, e.time_restrictions, e.visitor_allowed, e.emergency_access, " +
            "e.monitoring_enabled, e.alert_config " +
            "FROM t_area a " +
            "LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id " +
            "WHERE a.area_code = #{areaCode} AND a.deleted_flag = 0 LIMIT 1")
    AccessAreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 根据门禁级别查询区域列表
     */
    @Select("SELECT " +
            "a.area_id, a.area_code, a.area_name, a.area_type, a.parent_id, a.path, a.level, " +
            "a.sort_order, a.status, a.longitude, a.latitude, a.area_size, a.capacity, " +
            "a.description, a.map_image, a.remark, a.create_time, a.update_time, " +
            "a.create_user_id, a.update_user_id, a.deleted_flag, a.version, " +
            "e.access_enabled, e.access_level, e.access_mode, e.special_auth_required, " +
            "e.valid_time_start, e.valid_time_end, e.valid_weekdays, e.device_count, " +
            "e.guard_required, e.time_restrictions, e.visitor_allowed, e.emergency_access, " +
            "e.monitoring_enabled, e.alert_config " +
            "FROM t_area a " +
            "LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id " +
            "WHERE e.access_level = #{accessLevel} AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<AccessAreaEntity> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * 查询启用了门禁的区域列表
     */
    @Select("SELECT " +
            "a.area_id, a.area_code, a.area_name, a.area_type, a.parent_id, a.path, a.level, " +
            "a.sort_order, a.status, a.longitude, a.latitude, a.area_size, a.capacity, " +
            "a.description, a.map_image, a.remark, a.create_time, a.update_time, " +
            "a.create_user_id, a.update_user_id, a.deleted_flag, a.version, " +
            "e.access_enabled, e.access_level, e.access_mode, e.special_auth_required, " +
            "e.valid_time_start, e.valid_time_end, e.valid_weekdays, e.device_count, " +
            "e.guard_required, e.time_restrictions, e.visitor_allowed, e.emergency_access, " +
            "e.monitoring_enabled, e.alert_config " +
            "FROM t_area a " +
            "LEFT JOIN t_access_area_ext e ON a.area_id = e.area_id " +
            "WHERE e.access_enabled = 1 AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC, a.create_time ASC")
    List<AccessAreaEntity> selectEnabledAreas();
}