package net.lab1024.sa.admin.module.smart.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 门禁区域DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责区域数据访问
 * - 提供缓存层需要的查询方法
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface AccessAreaDao extends BaseMapper<AccessAreaEntity> {

    /**
     * 查询根级区域列表
     */
    @Select("SELECT * FROM t_smart_access_area WHERE parent_id = 0 AND deleted_flag = 0 ORDER BY sort_order ASC, create_time ASC")
    List<AccessAreaEntity> selectRootAreas();

    /**
     * 查询子区域ID列表
     */
    @Select("SELECT area_id FROM t_smart_access_area WHERE parent_id = #{parentId} AND deleted_flag = 0 ORDER BY sort_order ASC, create_time ASC")
    List<Long> selectChildrenIds(@Param("parentId") Long parentId);

    /**
     * 查询指定路径上的所有区域ID
     */
    @Select("SELECT area_id FROM t_smart_access_area WHERE path LIKE CONCAT('%,', #{areaId}, ',%') AND deleted_flag = 0")
    List<Long> selectDescendantIds(@Param("areaId") Long areaId);

    /**
     * 批量查询区域编码是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_smart_access_area WHERE area_code IN " +
            "<foreach collection='areaCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach>" +
            " AND deleted_flag = 0" +
            "</script>")
    Long countByAreaCodes(@Param("areaCodes") List<String> areaCodes);

    /**
     * 根据编码查询区域
     */
    @Select("SELECT * FROM t_smart_access_area WHERE area_code = #{areaCode} AND deleted_flag = 0 LIMIT 1")
    AccessAreaEntity selectByAreaCode(@Param("areaCode") String areaCode);
}