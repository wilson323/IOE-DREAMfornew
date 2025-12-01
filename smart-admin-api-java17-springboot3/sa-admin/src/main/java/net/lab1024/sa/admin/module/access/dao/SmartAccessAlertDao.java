package net.lab1024.sa.admin.module.access.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.admin.module.access.domain.entity.SmartAccessAlertEntity;

/**
 * 门禁告警DAO
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseMapper使用MyBatis-Plus
 * - 命名规范：{Module}Dao
 * - 职责单一：只负责门禁告警数据访问
 * - 提供缓存层需要的查询方法
 * - 关联基础设备、区域、人员表查询
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Mapper
public interface SmartAccessAlertDao extends BaseMapper<SmartAccessAlertEntity> {

    /**
     * 根据告警ID查询详细信息
     * 关联查询基础设备信息、区域信息和人员信息，确保数据一致性
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.alert_id = #{alertId} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL)")
    SmartAccessAlertEntity selectByIdWithDetails(@Param("alertId") Long alertId);

    /**
     * 根据设备ID查询告警列表
     * 关联基础设备表和区域表，查询指定设备的告警记录
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.device_id = #{deviceId} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据区域ID查询告警列表
     * 关联基础设备表和区域表，查询指定区域的告警记录
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.area_id = #{areaId} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据告警类型查询告警列表
     * 关联基础设备表和区域表，按告警类型过滤
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.alert_type = #{alertType} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectByAlertType(@Param("alertType") String alertType);

    /**
     * 根据严重级别查询告警列表
     * 关联基础设备表和区域表，按严重级别过滤
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.severity_level = #{severityLevel} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectBySeverityLevel(@Param("severityLevel") String severityLevel);

    /**
     * 查询待处理的告警列表
     * 关联基础设备表和区域表，过滤待处理状态的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.process_status = 'PENDING' AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectPendingAlerts();

    /**
     * 查询处理中的告警列表
     * 关联基础设备表和区域表，过滤处理中状态的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.process_status = 'PROCESSING' AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectProcessingAlerts();

    /**
     * 查询需要确认的告警列表
     * 关联基础设备表和区域表，过滤需要确认的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.confirmation_required = 1 AND a.confirmation_time IS NULL " +
            "AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectConfirmationRequiredAlerts();

    /**
     * 查询指定时间范围内的告警列表
     * 关联基础设备表和区域表，按时间范围过滤
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.event_time BETWEEN #{startTime} AND #{endTime} " +
            "AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的告警列表
     * 关联基础设备表和区域表，按时间倒序获取最近的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC " +
            "LIMIT #{limit}")
    List<SmartAccessAlertEntity> selectRecentAlerts(@Param("limit") Integer limit);

    /**
     * 查询紧急告警列表
     * 关联基础设备表和区域表，过滤严重级别为紧急的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.severity_level = 'CRITICAL' AND a.process_status IN ('PENDING', 'PROCESSING') " +
            "AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectCriticalAlerts();

    /**
     * 根据告警源查询告警列表
     * 关联基础设备表和区域表，按告警源过滤
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.alert_source = #{alertSource} AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectByAlertSource(@Param("alertSource") String alertSource);

    /**
     * 查询自动处理的告警列表
     * 关联基础设备表和区域表，过滤自动处理的告警
     */
    @Select("SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.auto_process = 1 AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "ORDER BY a.event_time DESC")
    List<SmartAccessAlertEntity> selectAutoProcessedAlerts();

    /**
     * 批量更新告警处理状态
     * 事务操作：批量更新多个告警的处理状态
     */
    @Update("<script>" +
            "UPDATE t_smart_access_alert SET " +
            "process_status = #{processStatus}, " +
            "process_user_id = #{userId}, " +
            "process_user_name = #{userName}, " +
            "process_time = NOW(), " +
            "process_note = #{processNote} " +
            "WHERE alert_id IN " +
            "<foreach collection='alertIds' item='alertId' open='(' separator=',' close=')'>" +
            "#{alertId}" +
            "</foreach>" +
            "</script>")
    int batchUpdateProcessStatus(@Param("alertIds") List<Long> alertIds,
                                 @Param("processStatus") String processStatus,
                                 @Param("userId") Long userId,
                                 @Param("userName") String userName,
                                 @Param("processNote") String processNote);

    /**
     * 批量确认告警
     * 事务操作：批量确认多个告警
     */
    @Update("<script>" +
            "UPDATE t_smart_access_alert SET " +
            "confirmation_user_id = #{userId}, " +
            "confirmation_user_name = #{userName}, " +
            "confirmation_time = NOW() " +
            "WHERE alert_id IN " +
            "<foreach collection='alertIds' item='alertId' open='(' separator=',' close=')'>" +
            "#{alertId}" +
            "</foreach>" +
            "</script>")
    int batchConfirmAlerts(@Param("alertIds") List<Long> alertIds,
                           @Param("userId") Long userId,
                           @Param("userName") String userName);

    /**
     * 统计告警总数
     * 关联基础设备表和区域表，统计有效告警数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL)")
    Long countTotalAlerts();

    /**
     * 统计待处理告警数量
     * 关联基础设备表和区域表，统计待处理状态的告警数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.process_status = 'PENDING' " +
            "AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL)")
    Long countPendingAlerts();

    /**
     * 统计紧急告警数量
     * 关联基础设备表和区域表，统计严重级别为紧急的告警数量
     */
    @Select("SELECT COUNT(*) FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE a.severity_level = 'CRITICAL' AND a.process_status IN ('PENDING', 'PROCESSING') " +
            "AND (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL)")
    Long countCriticalAlerts();

    /**
     * 根据告警类型统计告警数量
     * 关联基础设备表和区域表，按告警类型分组统计
     */
    @Select("SELECT a.alert_type, COUNT(*) as count " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "GROUP BY a.alert_type ORDER BY count DESC")
    List<Object> countAlertsByType();

    /**
     * 根据严重级别统计告警数量
     * 关联基础设备表和区域表，按严重级别分组统计
     */
    @Select("SELECT a.severity_level, COUNT(*) as count " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "GROUP BY a.severity_level ORDER BY count DESC")
    List<Object> countAlertsBySeverityLevel();

    /**
     * 批量查询多个设备的告警列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param deviceIds 设备ID列表
     * @return 告警列表
     */
    @Select("<script>" +
            "SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "AND a.device_id IN " +
            "<foreach collection='deviceIds' item='deviceId' open='(' separator=',' close=')'>" +
            "#{deviceId}" +
            "</foreach>" +
            " ORDER BY a.event_time DESC" +
            "</script>")
    List<SmartAccessAlertEntity> selectByDeviceIds(@Param("deviceIds") List<Long> deviceIds);

    /**
     * 批量查询多个区域的告警列表
     * 性能优化：使用IN查询避免N+1问题
     *
     * @param areaIds 区域ID列表
     * @return 告警列表
     */
    @Select("<script>" +
            "SELECT a.*, " +
            "d.device_name as device_name, d.device_code as device_code, d.device_type as device_type, " +
            "ar.area_name as area_name, ar.area_code as area_code, ar.area_type as area_type " +
            "FROM t_smart_access_alert a " +
            "LEFT JOIN t_smart_device d ON a.device_id = d.device_id " +
            "LEFT JOIN t_area ar ON a.area_id = ar.area_id " +
            "WHERE (d.deleted_flag = 0 OR d.deleted_flag IS NULL) " +
            "AND (ar.deleted_flag = 0 OR ar.deleted_flag IS NULL) " +
            "AND a.area_id IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            " ORDER BY a.event_time DESC" +
            "</script>")
    List<SmartAccessAlertEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);
}