package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.audit.entity.AuditLogEntity;

/**
 * 门禁事件DAO
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper获得基础CRUD能力
 * - 提供复杂查询的业务方法
 * - 支持分页和统计功能
 * - 使用公共AuditLogEntity替代AuditLogEntity
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 * @updated 2025-12-01 遵循DAO架构规范重构
 * @updated 2025-12-02 使用公共AuditLogEntity，遵循repowiki规范
 */
@Mapper
public interface AccessEventDao extends BaseMapper<AuditLogEntity> {

    /**
     * 统计门禁事件总数
     *
     * @return 事件总数
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_audit_log WHERE module_name = 'ACCESS' AND deleted_flag = 0")
    Long countTotalAccessEvents();

    /**
     * 统计成功的门禁事件数量
     *
     * @return 成功事件数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_audit_log WHERE module_name = 'ACCESS' AND result_status = 1 AND deleted_flag = 0")
    Long countSuccessfulAccessEvents();

    /**
     * 分页查询门禁事件
     *
     * @param page       分页对象
     * @param businessId 业务ID（设备ID或用户ID）
     * @param result     操作结果
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 分页结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT * FROM t_audit_log WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "<if test='businessId != null and businessId != \"\"'> AND resource_id = #{businessId} </if>" +
            "<if test='result != null'> AND result_status = #{result} </if>" +
            "<if test='startTime != null'> AND create_time >= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time <= #{endTime} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    Page<AuditLogEntity> selectAccessEventPage(
            Page<AuditLogEntity> page,
            @Param("businessId") String businessId,
            @Param("result") Integer result,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围查询门禁事件统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("SELECT operation_desc as operation, COUNT(*) as count, result_status as result, " +
            "SUM(CASE WHEN result_status = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "GROUP BY operation_desc, result_status " +
            "ORDER BY count DESC")
    List<Map<String, Object>> selectAccessEventStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定区域的门禁统计
     *
     * @param areaIds   区域ID列表
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("<script>" +
            "SELECT resource_type as area_id, COUNT(*) as count, " +
            "SUM(CASE WHEN result_status = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND resource_type IN " +
            "<foreach collection='areaIds' item='areaId' open='(' separator=',' close=')'>" +
            "#{areaId}" +
            "</foreach>" +
            "GROUP BY resource_type " +
            "ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> selectAreaAccessStatistics(
            @Param("areaIds") List<Long> areaIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询设备门禁统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("SELECT resource_id as device_id, COUNT(*) as count, " +
            "SUM(CASE WHEN result_status = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "GROUP BY resource_id " +
            "ORDER BY count DESC")
    List<Map<String, Object>> selectDeviceAccessStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询验证方式统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计结果
     */
    @Transactional(readOnly = true)
    @Select("SELECT operation_desc as verify_method, COUNT(*) as count, " +
            "SUM(CASE WHEN result_status = 1 THEN 1 ELSE 0 END) as success_count " +
            "FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "GROUP BY operation_desc " +
            "ORDER BY count DESC")
    List<Map<String, Object>> selectVerifyMethodStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的门禁事件记录
     *
     * @param limit 限制数量
     * @return 事件列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND deleted_flag = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<AuditLogEntity> selectRecentAccessEvents(@Param("limit") Integer limit);

    /**
     * 查询用户最近访问记录
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 访问记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<AuditLogEntity> selectUserRecentAccessEvents(
            @Param("userId") Long userId,
            @Param("limit") Integer limit);

    /**
     * 查询设备最近门禁事件
     *
     * @param deviceId 设备ID
     * @param limit    限制数量
     * @return 事件列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND resource_id = #{deviceId} AND deleted_flag = 0 " +
            "ORDER BY create_time DESC " +
            "LIMIT #{limit}")
    List<AuditLogEntity> selectDeviceRecentAccessEvents(
            @Param("deviceId") String deviceId,
            @Param("limit") Integer limit);

    /**
     * 统计用户失败访问次数
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 失败次数
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND user_id = #{userId} AND result_status = 2 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = 0")
    Integer countUserFailedAccess(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询高风险门禁事件记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND risk_level >= 3 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<AuditLogEntity> selectHighRiskAccessEvents(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询异常门禁访问记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 记录列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_audit_log " +
            "WHERE module_name = 'ACCESS' AND result_status = 3 " +
            "AND create_time >= #{startTime} AND create_time <= #{endTime} " +
            "AND deleted_flag = 0 " +
            "ORDER BY create_time DESC")
    List<AuditLogEntity> selectAbnormalAccessEvents(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期的门禁事件记录
     *
     * @param cutoffTime 截止时间
     * @return 删除数量
     */
    @Select("UPDATE t_audit_log SET deleted_flag = 1, update_time = NOW() " +
            "WHERE module_name = 'ACCESS' AND create_time < #{cutoffTime} AND deleted_flag = 0")
    int deleteExpiredAccessEvents(@Param("cutoffTime") LocalDateTime cutoffTime);
}
