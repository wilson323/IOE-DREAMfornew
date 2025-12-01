package net.lab1024.sa.base.module.area.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 区域数据访问层
 * 提供区域数据的基础CRUD操作和复杂查询功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Mapper
public interface AreaDao extends BaseMapper<AreaEntity> {

    /**
     * 查询所有区域
     *
     * @return 所有区域列表
     */
    @Select("SELECT * FROM t_area WHERE deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectAll();

    /**
     * 根据区域编码查询区域
     *
     * @param areaCode 区域编码
     * @return 区域信息
     */
    @Select("SELECT * FROM t_area WHERE area_code = #{areaCode} AND deleted_flag = 0")
    AreaEntity selectByAreaCode(@Param("areaCode") String areaCode);

    /**
     * 检查区域编码是否存在（排除指定ID）
     *
     * @param areaCode 区域编码
     * @param excludeId 排除的ID
     * @return 存在的数量
     */
    @Select("SELECT COUNT(1) FROM t_area WHERE area_code = #{areaCode} AND area_id != #{excludeId} AND deleted_flag = 0")
    int countByAreaCodeExcludeId(@Param("areaCode") String areaCode, @Param("excludeId") Long excludeId);

    /**
     * 查询父级区域下的所有子区域
     *
     * @param parentId 父级区域ID
     * @return 子区域列表
     */
    @Select("SELECT * FROM t_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询指定区域及其所有子区域ID列表
     * 使用递归查询获取所有层级关系
     *
     * @param areaId 区域ID
     * @return 区域ID列表
     */
    @Select("WITH RECURSIVE area_tree AS (" +
            "    SELECT area_id FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "    UNION ALL " +
            "    SELECT a.area_id FROM t_area a " +
            "    INNER JOIN area_tree at ON a.parent_id = at.area_id " +
            "    WHERE a.deleted_flag = 0 " +
            ") " +
            "SELECT area_id FROM area_tree")
    List<Long> selectSelfAndAllChildrenIds(@Param("areaId") Long areaId);

    /**
     * 查询指定区域的所有父级区域ID列表（从根到当前）
     *
     * @param areaId 区域ID
     * @return 父级区域ID列表
     */
    @Select("SELECT parent_id FROM t_area " +
            "WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "UNION ALL " +
            "SELECT " +
            "    CASE " +
            "        WHEN t.parent_id = 0 THEN NULL " +
            "        ELSE t.parent_id " +
            "    END " +
            "FROM t_area t " +
            "WHERE t.area_id = #{areaId} AND t.deleted_flag = 0 " +
            "AND t.parent_id != 0")
    List<Long> selectAllParentIds(@Param("areaId") Long areaId);

    /**
     * 查询所有根区域（parent_id = 0）
     *
     * @return 根区域列表
     */
    @Select("SELECT * FROM t_area WHERE parent_id = 0 AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectRootAreas();

    /**
     * 根据区域类型查询区域
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE area_type = #{areaType} AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectByAreaType(@Param("areaType") Integer areaType);

    /**
     * 根据区域类型列表查询区域
     *
     * @param areaTypes 区域类型列表
     * @return 区域列表
     */
    @Select("<script>" +
            "SELECT * FROM t_area WHERE area_type IN " +
            "<foreach collection='areaTypes' item='type' open='(' separator=',' close=')'>" +
            "#{type}" +
            "</foreach>" +
            " AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC" +
            "</script>")
    List<AreaEntity> selectByAreaTypes(@Param("areaTypes") List<Integer> areaTypes);

    /**
     * 查询指定层级的所有区域
     *
     * @param level 层级深度
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE level = #{level} AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectByLevel(@Param("level") Integer level);

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @return 更新行数
     */
    @Update("UPDATE t_area SET status = #{status}, update_time = NOW() WHERE area_id = #{areaId}")
    int updateStatus(@Param("areaId") Long areaId, @Param("status") Integer status);

    /**
     * 更新区域排序
     *
     * @param areaId 区域ID
     * @param sortOrder 排序号
     * @return 更新行数
     */
    @Update("UPDATE t_area SET sort_order = #{sortOrder}, update_time = NOW() WHERE area_id = #{areaId}")
    int updateSortOrder(@Param("areaId") Long areaId, @Param("sortOrder") Integer sortOrder);

    /**
     * 检查是否存在子区域
     *
     * @param areaId 区域ID
     * @return 子区域数量
     */
    @Select("SELECT COUNT(1) FROM t_area WHERE parent_id = #{areaId} AND deleted_flag = 0")
    int countChildren(@Param("areaId") Long areaId);

    /**
     * 根据名称模糊查询区域
     *
     * @param areaName 区域名称
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE area_name LIKE CONCAT('%', #{areaName}, '%') AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectByNameLike(@Param("areaName") String areaName);

    /**
     * 查询指定区域的完整路径信息
     *
     * @param areaId 区域ID
     * @return 包含路径信息的区域列表
     */
    @Select("WITH RECURSIVE area_path AS (" +
            "    SELECT area_id, area_name, parent_id, 0 as level, CAST(area_name AS CHAR(1000)) as path_name " +
            "    FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "    UNION ALL " +
            "    SELECT a.area_id, a.area_name, a.parent_id, ap.level + 1, " +
            "           CONCAT(ap.path_name, ' > ', a.area_name) " +
            "    FROM t_area a " +
            "    INNER JOIN area_path ap ON a.area_id = ap.parent_id " +
            "    WHERE a.deleted_flag = 0 " +
            ") " +
            "SELECT path_name FROM area_path ORDER BY level")
    List<String> selectAreaPath(@Param("areaId") Long areaId);

    /**
     * 软删除区域（设置删除标记）
     *
     * @param areaId 区域ID
     * @param userId 操作用户ID
     * @return 更新行数
     */
    @Update("UPDATE t_area SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() WHERE area_id = #{areaId}")
    int deleteById(@Param("areaId") Long areaId, @Param("userId") Long userId);

    /**
     * 批量软删除区域
     *
     * @param areaIds 区域ID列表
     * @param userId 操作用户ID
     * @return 更新行数
     */
    @Update("<script>" +
            "UPDATE t_area SET deleted_flag = 1, update_user_id = #{userId}, update_time = NOW() " +
            "WHERE area_id IN " +
            "<foreach collection='areaIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchByIds(@Param("areaIds") List<Long> areaIds, @Param("userId") Long userId);

    /**
     * 根据状态查询区域
     *
     * @param status 状态值
     * @return 区域列表
     */
    @Select("SELECT * FROM t_area WHERE status = #{status} AND deleted_flag = 0 ORDER BY sort_order ASC, area_id ASC")
    List<AreaEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据条件统计区域数量
     *
     * @param areaType 区域类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 区域数量
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM t_area WHERE deleted_flag = 0 " +
            "<if test='areaType != null'>" +
            "AND area_type = #{areaType} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (area_name LIKE CONCAT('%', #{keyword}, '%') OR area_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</script>")
    long countByCondition(@Param("areaType") Integer areaType, @Param("status") Integer status, @Param("keyword") String keyword);

    /**
     * 获取区域的完整层次路径（从根到当前区域的所有父级ID）
     *
     * @param areaId 区域ID
     * @return 父级区域ID列表（不包含当前区域）
     */
    @Select("WITH RECURSIVE area_hierarchy AS (" +
            "    SELECT parent_id, area_id FROM t_area WHERE area_id = #{areaId} AND deleted_flag = 0 " +
            "    UNION ALL " +
            "    SELECT a.parent_id, a.area_id FROM t_area a " +
            "    INNER JOIN area_hierarchy ah ON a.area_id = ah.parent_id " +
            "    WHERE a.deleted_flag = 0 AND ah.parent_id > 0 " +
            ") " +
            "SELECT parent_id FROM area_hierarchy WHERE parent_id > 0 ORDER BY parent_id")
    List<Long> getAreaHierarchyPath(@Param("areaId") Long areaId);

    /**
     * 递归获取区域的所有子区域ID
     *
     * @param parentAreaId 父区域ID
     * @return 所有子区域ID列表
     */
    @Select("WITH RECURSIVE child_areas AS (" +
            "    SELECT area_id FROM t_area WHERE parent_id = #{parentAreaId} AND deleted_flag = 0 " +
            "    UNION ALL " +
            "    SELECT a.area_id FROM t_area a " +
            "    INNER JOIN child_areas ca ON a.parent_id = ca.area_id " +
            "    WHERE a.deleted_flag = 0 " +
            ") " +
            "SELECT area_id FROM child_areas")
    List<Long> getAllChildAreaIds(@Param("parentAreaId") Long parentAreaId);
}