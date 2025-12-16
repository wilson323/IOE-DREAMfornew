package net.lab1024.sa.visitor.domain.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.visitor.domain.entity.VisitorAreaEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 访客区域数据访问层
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Mapper
@Transactional(readOnly = true)
public interface VisitorAreaDao extends BaseMapper<VisitorAreaEntity> {

    /**
     * 根据区域ID查询访客区域配置
     *
     * @param areaId 区域ID
     * @return 访客区域配置
     */
    @Select("""
        SELECT *
        FROM t_visitor_area
        WHERE area_id = #{areaId}
          AND enabled = true
          AND (expire_time IS NULL OR expire_time > NOW())
        """)
    VisitorAreaEntity selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据访问类型查询访客区域
     *
     * @param visitType 访问类型
     * @return 访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.visit_type = #{visitType}
          AND va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        ORDER BY a.area_code
        """)
    List<VisitorAreaEntity> selectByVisitType(@Param("visitType") Integer visitType);

    /**
     * 根据访问权限级别查询访客区域
     *
     * @param accessLevel 访问权限级别
     * @return 访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.access_level = #{accessLevel}
          AND va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        ORDER BY a.area_code
        """)
    List<VisitorAreaEntity> selectByAccessLevel(@Param("accessLevel") Integer accessLevel);

    /**
     * 查询需要接待人员的访客区域
     *
     * @return 访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.reception_required = true
          AND va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        ORDER BY a.area_code
        """)
    List<VisitorAreaEntity> selectReceptionRequiredAreas();

    /**
     * 根据接待人员ID查询访客区域
     *
     * @param receptionistId 接待人员ID
     * @return 访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.receptionist_id = #{receptionistId}
          AND va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        ORDER BY a.area_code
        """)
    List<VisitorAreaEntity> selectByReceptionistId(@Param("receptionistId") Long receptionistId);

    /**
     * 查询当前访客数量超限的区域
     *
     * @return 访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.current_visitors >= va.max_visitors
          AND va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        ORDER BY va.current_visitors DESC
        """)
    List<VisitorAreaEntity> selectOverCapacityAreas();

    /**
     * 统计访客区域数量
     *
     * @return 统计结果
     */
    @Select("""
        SELECT
            COUNT(*) as total_areas,
            SUM(CASE WHEN va.access_level = 1 THEN 1 ELSE 0 END) as public_areas,
            SUM(CASE WHEN va.access_level = 2 THEN 1 ELSE 0 END) as restricted_areas,
            SUM(CASE WHEN va.access_level = 3 THEN 1 ELSE 0 END) as confidential_areas,
            SUM(va.max_visitors) as total_capacity,
            SUM(va.current_visitors) as current_visitors
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        """)
    Map<String, Object> selectVisitorAreaStatistics();

    /**
     * 按访问类型统计访客区域
     *
     * @return 统计结果
     */
    @Select("""
        SELECT
            va.visit_type,
            COUNT(*) as area_count,
            SUM(va.max_visitors) as total_capacity,
            SUM(va.current_visitors) as current_visitors
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        GROUP BY va.visit_type
        ORDER BY va.visit_type
        """)
    List<Map<String, Object>> selectAreaStatisticsByVisitType();

    /**
     * 查询当前时段开放的访客区域
     *
     * @param currentTime 当前时间
     * @return 开放的访客区域列表
     */
    @Select("""
        SELECT va.*, a.area_name, a.area_code
        FROM t_visitor_area va
        INNER JOIN t_area a ON va.area_id = a.area_id
        WHERE va.enabled = true
          AND a.status = 1
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
          AND (
              va.open_hours IS NULL
              OR JSON_EXTRACT(va.open_hours, CONCAT('$.',
                CASE DAYOFWEEK(#{currentTime})
                    WHEN 1 THEN 'sunday'
                    WHEN 2 THEN 'monday'
                    WHEN 3 THEN 'tuesday'
                    WHEN 4 THEN 'wednesday'
                    WHEN 5 THEN 'thursday'
                    WHEN 6 THEN 'friday'
                    WHEN 7 THEN 'saturday'
                END)) IS NOT NULL
          )
        ORDER BY a.area_code
        """)
    List<VisitorAreaEntity> selectOpenAreas(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 检查区域是否支持指定访问类型
     *
     * @param areaId 区域ID
     * @param visitType 访问类型
     * @return 是否支持
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM t_visitor_area va
        WHERE va.area_id = #{areaId}
          AND va.visit_type = #{visitType}
          AND va.enabled = true
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        """)
    boolean isSupportVisitType(@Param("areaId") Long areaId, @Param("visitType") Integer visitType);

    /**
     * 检查用户是否有访客区域管理权限
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否有权限
     */
    @Select("""
        SELECT COUNT(*) > 0
        FROM t_visitor_area va
        INNER JOIN t_user_area_permission uap ON va.area_id = uap.area_id
        WHERE uap.user_id = #{userId}
          AND va.area_id = #{areaId}
          AND va.enabled = true
          AND uap.enabled = true
          AND (va.expire_time IS NULL OR va.expire_time > NOW())
        """)
    boolean hasManagePermission(@Param("userId") Long userId, @Param("areaId") Long areaId);

    /**
     * 更新区域当前访客数量
     *
     * @param areaId 区域ID
     * @param visitorCount 访客数量
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_visitor_area
        SET current_visitors = #{visitorCount},
            update_time = NOW()
        WHERE area_id = #{areaId}
        """)
    int updateCurrentVisitors(@Param("areaId") Long areaId, @Param("visitorCount") Integer visitorCount);

    /**
     * 增加区域访客数量
     *
     * @param areaId 区域ID
     * @param increment 增加数量
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_visitor_area
        SET current_visitors = current_visitors + #{increment},
            update_time = NOW()
        WHERE area_id = #{areaId}
          AND current_visitors + #{increment} <= max_visitors
        """)
    int incrementVisitors(@Param("areaId") Long areaId, @Param("increment") Integer increment);

    /**
     * 减少区域访客数量
     *
     * @param areaId 区域ID
     * @param decrement 减少数量
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("""
        UPDATE t_visitor_area
        SET current_visitors = GREATEST(0, current_visitors - #{decrement}),
            update_time = NOW()
        WHERE area_id = #{areaId}
        """)
    int decrementVisitors(@Param("areaId") Long areaId, @Param("decrement") Integer decrement);
}
