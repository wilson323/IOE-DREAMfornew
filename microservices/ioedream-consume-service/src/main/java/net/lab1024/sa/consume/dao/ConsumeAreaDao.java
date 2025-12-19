package net.lab1024.sa.consume.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.entity.ConsumeAreaEntity;

/**
 * 消费区域DAO接口
 * <p>
 * 用于区域的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface ConsumeAreaDao extends BaseMapper<ConsumeAreaEntity> {

    /**
     * 根据区域编号查询区域信息
     *
     * @param areaCode 区域编号
     * @return 区域信息
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_consume_area WHERE area_code = #{areaCode} AND deleted_flag = 0")
    ConsumeAreaEntity selectByCode(@Param("areaCode") String areaCode);

    /**
     * 根据父级区域ID查询子区域列表
     *
     * @param parentId 父级区域ID（0或null表示查询顶级区域）
     * @return 子区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_consume_area WHERE (parent_id = #{parentId} OR (#{parentId} IS NULL AND (parent_id = 0 OR parent_id IS NULL))) "
            +
            "AND deleted_flag = 0 ORDER BY sort_order ASC, create_time ASC")
    List<ConsumeAreaEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据区域类型查询区域列表
     *
     * @param areaType 区域类型
     * @return 区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_consume_area WHERE area_type = #{areaType} AND deleted_flag = 0 AND status = 1 " +
            "ORDER BY sort_order ASC, create_time ASC")
    List<ConsumeAreaEntity> selectByAreaType(@Param("areaType") Integer areaType);

    /**
     * 根据经营模式查询区域列表
     *
     * @param manageMode 经营模式
     * @return 区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_consume_area WHERE manage_mode = #{manageMode} AND deleted_flag = 0 AND status = 1 " +
            "ORDER BY sort_order ASC, create_time ASC")
    List<ConsumeAreaEntity> selectByManageMode(@Param("manageMode") Integer manageMode);

    /**
     * 查询所有启用的区域列表
     *
     * @return 区域列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_consume_area WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY sort_order ASC, create_time ASC")
    List<ConsumeAreaEntity> selectAllEnabled();
}
