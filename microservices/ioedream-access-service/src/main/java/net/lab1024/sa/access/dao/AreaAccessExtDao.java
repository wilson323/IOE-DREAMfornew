package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AreaAccessExtEntity;

/**
 * 区域门禁扩展DAO接口
 * <p>
 * 基于AreaAccessExtEntity提供数据访问方法
 * 命名规范统一：符合{BaseDomain}{Module}ExtDao标准模式
 * 严格遵循项目现有代码风格和架构规范
 * 提供扩展查询和性能优化功能
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface AreaAccessExtDao extends BaseMapper<AreaAccessExtEntity> {

    /**
     * 根据区域ID查询扩展信息
     * 关联查询基础区域信息，确保数据一致性
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.area_id = #{areaId} AND a.deleted_flag = 0")
    AreaAccessExtEntity selectByAreaIdWithBaseInfo(@Param("areaId") Long areaId);

    /**
     * 根据门禁级别查询区域扩展信息
     * 关联基础区域表，支持按门禁级别过滤
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level = #{accessLevel} AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * 根据门禁模式查询区域扩展信息
     * 支持模糊匹配，关联基础区域表
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_mode LIKE CONCAT('%', #{accessMode}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByAccessModeLike(@Param("accessMode") String accessMode);

    /**
     * 查询包含指定验证方式的区域
     * 关联基础区域表，支持验证方式过滤
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_mode LIKE CONCAT('%', #{verificationMode}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByVerificationMode(@Param("verificationMode") String verificationMode);

    /**
     * 查询高门禁级别的区域（access_level >= 2）
     * 关联基础区域表，按门禁级别降序排列
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level >= #{minLevel} AND a.deleted_flag = 0 " +
            "ORDER BY e.access_level DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectByMinAccessLevel(@Param("minLevel") Integer minLevel);

    /**
     * 批量查询区域扩展信息
     * 关联基础区域表，支持多个区域ID
     */
    @Select("<script>" +
            "SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC" +
            "</script>")
    List<AreaAccessExtEntity> selectByAreaIdsWithBaseInfo(@Param("areaIds") List<Long> areaIds);

    /**
     * 查询设备数量大于指定数量的区域
     * 关联基础区域表，按设备数量降序排列
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.device_count > #{minDeviceCount} AND a.deleted_flag = 0 " +
            "ORDER BY e.device_count DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectByMinDeviceCount(@Param("minDeviceCount") Integer minDeviceCount);

    /**
     * 更新区域门禁级别
     *
     * @param areaId      区域ID
     * @param accessLevel 门禁级别
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_access_area_ext SET " +
            "access_level = #{accessLevel}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateAccessLevel(@Param("areaId") Long areaId, @Param("accessLevel") Integer accessLevel, @Param("updateUserId") Long updateUserId);

    /**
     * 更新门禁模式配置
     *
     * @param areaId      区域ID
     * @param accessMode  门禁模式
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_access_area_ext SET " +
            "access_mode = #{accessMode}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateAccessMode(@Param("areaId") Long areaId, @Param("accessMode") String accessMode, @Param("updateUserId") Long updateUserId);

    /**
     * 更新关联设备数量
     *
     * @param areaId      区域ID
     * @param deviceCount 设备数量
     * @param updateUserId 更新用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_access_area_ext SET " +
            "device_count = #{deviceCount}, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateDeviceCount(@Param("areaId") Long areaId, @Param("deviceCount") Integer deviceCount, @Param("updateUserId") Long updateUserId);

    /**
     * 软删除区域扩展信息
     *
     * @param areaId      区域ID
     * @param updateUserId 更新用户ID
     * @return 删除行数
     */
    @Update("UPDATE t_access_area_ext SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id = #{areaId}")
    int softDeleteByAreaId(@Param("areaId") Long areaId, @Param("updateUserId") Long updateUserId);

    /**
     * 批量软删除区域扩展信息
     *
     * @param areaIds     区域ID列表
     * @param updateUserId  更新用户ID
     * @return 删除行数
     */
    @Update("<script>" +
            "UPDATE t_access_area_ext SET " +
            "deleted_flag = 1, " +
            "update_time = NOW(), " +
            "update_user_id = #{updateUserId} " +
            "WHERE area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "</script>")
    int batchSoftDeleteByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("updateUserId") Long updateUserId);

    /**
     * 统计区域扩展信息数量
     *
     * @return 总数量
     */
    @Select("SELECT COUNT(*) FROM t_access_area_ext WHERE deleted_flag = 0")
    long countTotal();

    /**
     * 根据门禁级别统计数量
     *
     * @return 级别统计信息
     */
    @Select("SELECT " +
            "access_level, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY access_level " +
            "ORDER BY access_level")
    List<java.util.Map<String, Object>> countByAccessLevel();

    /**
     * 统计各门禁模式的区域数量
     *
     * @return 模式统计信息
     */
    @Select("SELECT " +
            "access_mode, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 AND access_mode IS NOT NULL " +
            "GROUP BY access_mode " +
            "ORDER BY count DESC")
    List<java.util.Map<String, Object>> countByAccessMode();

    /**
     * 统计设备数量分布情况
     *
     * @return 设备数量统计信息
     */
    @Select("SELECT " +
            "CASE " +
            "WHEN device_count = 0 THEN '无设备' " +
            "WHEN device_count BETWEEN 1 AND 5 THEN '1-5台' " +
            "WHEN device_count BETWEEN 6 AND 10 THEN '6-10台' " +
            "WHEN device_count BETWEEN 11 AND 20 THEN '11-20台' " +
            "ELSE '20台以上' " +
            "END as device_range, " +
            "COUNT(*) as count " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0 " +
            "GROUP BY " +
            "CASE " +
            "WHEN device_count = 0 THEN '无设备' " +
            "WHEN device_count BETWEEN 1 AND 5 THEN '1-5台' " +
            "WHEN device_count BETWEEN 6 AND 10 THEN '6-10台' " +
            "WHEN device_count BETWEEN 11 AND 20 THEN '11-20台' " +
            "ELSE '20台以上' " +
            "END " +
            "ORDER BY MIN(device_count)")
    List<java.util.Map<String, Object>> countByDeviceRange();

    /**
     * 查询高门禁级别的区域（access_level >= 2）
     * 关联基础区域表
     *
     * @return 高级别区域列表
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE e.access_level >= 2 AND a.deleted_flag = 0 " +
            "ORDER BY e.access_level DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectHighAccessLevelAreas();

    /**
     * 查询多验证方式的区域（access_mode包含多个验证方式）
     * 关联基础区域表
     *
     * @return 多验证方式区域列表
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE " +
            "(CHAR_LENGTH(e.access_mode) - CHAR_LENGTH(REPLACE(e.access_mode, ',', '')) + 1) > 1 " +
            "AND a.deleted_flag = 0 " +
            "ORDER BY CHAR_LENGTH(e.access_mode) DESC, a.sort_order ASC")
    List<AreaAccessExtEntity> selectMultiVerificationAreas();

    /**
     * 查询指定父区域下的所有子区域扩展信息
     * 关联基础区域表，支持层级查询
     *
     * @param parentPath 父区域路径
     * @return 子区域扩展信息列表
     */
    @Select("SELECT e.*, a.area_name, a.area_code, a.area_type, a.parent_id, a.path " +
            "FROM t_access_area_ext e " +
            "INNER JOIN t_area a ON e.area_id = a.area_id " +
            "WHERE a.path LIKE CONCAT(#{parentPath}, '%') AND a.deleted_flag = 0 " +
            "ORDER BY a.sort_order ASC")
    List<AreaAccessExtEntity> selectByParentPath(@Param("parentPath") String parentPath);

    /**
     * 统计启用扩展功能的区域数量
     * 基于现有字段判断是否有扩展配置
     *
     * @return 启用数量
     */
    @Select("SELECT COUNT(*) FROM t_access_area_ext " +
            "WHERE (access_level > 0 OR device_count > 0 OR access_mode IS NOT NULL) AND deleted_flag = 0")
    long countEnabled();

    /**
     * 查询区域扩展配置信息汇总统计
     * 提供综合的统计视图
     *
     * @return 统计汇总信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_areas, " +
            "COUNT(CASE WHEN access_level >= 3 THEN 1 END) as high_security_areas, " +
            "COUNT(CASE WHEN access_mode LIKE '%,%' THEN 1 END) as multi_verification_areas, " +
            "COUNT(CASE WHEN device_count > 0 THEN 1 END) as areas_with_devices, " +
            "SUM(device_count) as total_devices, " +
            "MAX(access_level) as max_access_level " +
            "FROM t_access_area_ext " +
            "WHERE deleted_flag = 0")
    java.util.Map<String, Object> getAreaExtensionStatistics();
}