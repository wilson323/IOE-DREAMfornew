package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 区域门禁扩展数据访问对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识数据访问层
 * - 继承BaseMapper<Entity>使用MyBatis-Plus
 * - 查询方法使用@Transactional(readOnly = true)
 * - 写操作方法使用@Transactional(rollbackFor = Exception.class)
 * - 统一使用Dao后缀命名
 * </p>
 * <p>
 * 核心职责：
 * - 区域门禁扩展信息的CRUD操作
 * - 根据验证模式查询区域
 * - 门禁级别和模式查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AreaAccessExtDao extends BaseMapper<AreaAccessExtEntity> {

    /**
     * 根据区域ID查询扩展信息
     *
     * @param areaId 区域ID
     * @return 区域扩展信息，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_area_ext " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "LIMIT 1")
    AreaAccessExtEntity selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据验证模式查询区域扩展信息
     * <p>
     * 用于查找使用特定验证模式的区域
     * </p>
     *
     * @param verificationMode 验证模式（edge/backend/hybrid）
     * @return 区域扩展信息列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_area_ext " +
            "WHERE verification_mode = #{verificationMode} AND deleted_flag = 0 " +
            "ORDER BY area_id")
    List<AreaAccessExtEntity> selectByVerificationMode(@Param("verificationMode") String verificationMode);

    /**
     * 根据门禁级别查询区域扩展信息
     *
     * @param accessLevel 门禁级别
     * @return 区域扩展信息列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_area_ext " +
            "WHERE access_level = #{accessLevel} AND deleted_flag = 0 " +
            "ORDER BY area_id")
    List<AreaAccessExtEntity> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * 批量查询区域扩展信息
     *
     * @param areaIds 区域ID列表
     * @return 区域扩展信息列表
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_access_area_ext " +
            "WHERE area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "AND deleted_flag = 0 " +
            "ORDER BY area_id" +
            "</script>")
    List<AreaAccessExtEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);

    /**
     * 更新验证模式
     *
     * @param areaId 区域ID
     * @param verificationMode 验证模式
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "verification_mode = #{verificationMode}, " +
            "update_time = NOW() " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateVerificationMode(@Param("areaId") Long areaId, @Param("verificationMode") String verificationMode);

    /**
     * 更新门禁级别
     *
     * @param areaId 区域ID
     * @param accessLevel 门禁级别
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "access_level = #{accessLevel}, " +
            "update_time = NOW() " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateAccessLevel(@Param("areaId") Long areaId, @Param("accessLevel") Integer accessLevel);

    /**
     * 更新设备数量
     *
     * @param areaId 区域ID
     * @param deviceCount 设备数量
     * @return 更新行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_area_ext SET " +
            "device_count = #{deviceCount}, " +
            "update_time = NOW() " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0")
    int updateDeviceCount(@Param("areaId") Long areaId, @Param("deviceCount") Integer deviceCount);
}
