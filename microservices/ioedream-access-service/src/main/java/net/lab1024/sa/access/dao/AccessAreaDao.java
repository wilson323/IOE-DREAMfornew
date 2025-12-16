package net.lab1024.sa.access.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaEntity;

/**
 * 门禁区域DAO
 * <p>
 * 严格遵循四层架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Mapper
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责门禁区域数据访问
 * - 提供Manager层需要的查询方法
 * - 使用公共AreaEntity替代AccessAreaEntity
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 * @updated 2025-12-02 使用公共AreaEntity，遵循repowiki规范
 */
@Mapper
public interface AccessAreaDao extends BaseMapper<AreaEntity> {

    /**
     * 查询根级门禁区域列表
     * 使用公共区域表：t_common_area
     *
     * @return 根级区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE parent_id IS NULL OR parent_id = 0 AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectRootAreas();

    /**
     * 查询指定区域的所有子区域
     *
     * @param parentId 父区域ID
     * @return 子区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询有效的门禁区域列表
     *
     * @return 有效区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE status = 1 AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectActiveAreas();

    /**
     * 根据区域类型查询区域列表
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_common_area WHERE area_type = #{areaType} AND deleted_flag = 0 ORDER BY sort_index")
    List<AreaEntity> selectByAreaType(@Param("areaType") Integer areaType);

    /**
     * 查询指定区域的所有子区域ID
     *
     * @param parentId 父区域ID
     * @return 子区域ID列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT area_id FROM t_common_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_index")
    List<Long> selectChildrenIds(@Param("parentId") Long parentId);
}

