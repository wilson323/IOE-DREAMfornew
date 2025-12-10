package net.lab1024.sa.common.audit.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.audit.entity.ConfigChangeAuditEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 配置变更审计数据访问层
 * <p>
 * 严格遵循本项目技术栈：
 * - 使用MyBatis-Plus作为ORM框架
 * - 遵循四层架构规范：DAO层只负责数据访问
 * - 使用@Mapper注解，禁用@Repository
 * - 统一使用BaseMapper<Entity>模式
 * - 针对企业级审计查询性能优化
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Mapper
public interface ConfigChangeAuditDao extends BaseMapper<ConfigChangeAuditEntity> {

    /**
     * 根据配置键查询变更记录
     *
     * @param configKey 配置键
     * @param limit 限制数量
     * @return 变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE config_key = #{configKey} ORDER BY change_time DESC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectByConfigKey(@Param("configKey") String configKey, @Param("limit") Integer limit);

    /**
     * 根据操作用户查询变更记录
     *
     * @param operatorId 操作用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE operator_id = #{operatorId} " +
            "AND change_time >= #{startTime} AND change_time <= #{endTime} " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectByOperator(@Param("operatorId") Long operatorId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("limit") Integer limit);

    /**
     * 根据变更批次ID查询变更记录
     *
     * @param changeBatchId 变更批次ID
     * @return 变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE change_batch_id = #{changeBatchId} ORDER BY change_time ASC")
    List<ConfigChangeAuditEntity> selectByBatchId(@Param("changeBatchId") String changeBatchId);

    /**
     * 查询高风险变更记录
     *
     * @param hours 最近小时数
     * @param limit 限制数量
     * @return 高风险变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE risk_level IN ('HIGH', 'CRITICAL') " +
            "AND change_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectHighRiskChanges(@Param("hours") Integer hours, @Param("limit") Integer limit);

    /**
     * 查询失败的变更记录
     *
     * @param hours 最近小时数
     * @param limit 限制数量
     * @return 失败变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE change_status = 'FAILED' " +
            "AND change_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectFailedChanges(@Param("hours") Integer hours, @Param("limit") Integer limit);

    /**
     * 查询待审批的变更记录
     *
     * @param limit 限制数量
     * @return 待审批变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE require_approval = 1 " +
            "AND approver_id IS NULL AND change_status = 'PENDING' " +
            "ORDER BY change_time ASC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectPendingApprovals(@Param("limit") Integer limit);

    /**
     * 查询敏感配置变更记录
     *
     * @param days 最近天数
     * @param limit 限制数量
     * @return 敏感配置变更记录列表
     */
    @Select("SELECT * FROM t_config_change_audit WHERE is_sensitive = 1 " +
            "AND change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "ORDER BY change_time DESC LIMIT #{limit}")
    List<ConfigChangeAuditEntity> selectSensitiveChanges(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 根据配置类型查询变更统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 配置类型变更统计
     */
    @Select("SELECT config_type, COUNT(*) as change_count, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(execution_time) / COUNT(*) as avg_execution_time " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= #{startTime} AND change_time <= #{endTime} " +
            "GROUP BY config_type ORDER BY change_count DESC")
    List<Map<String, Object>> selectChangeStatisticsByConfigType(@Param("startTime") LocalDateTime startTime,
                                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作用户查询变更统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 操作用户变更统计
     */
    @Select("SELECT operator_id, operator_name, operator_role, " +
            "COUNT(*) as change_count, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(CASE WHEN risk_level = 'HIGH' OR risk_level = 'CRITICAL' THEN 1 ELSE 0 END) as high_risk_count " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= #{startTime} AND change_time <= #{endTime} " +
            "GROUP BY operator_id, operator_name, operator_role " +
            "ORDER BY change_count DESC LIMIT #{limit}")
    List<Map<String, Object>> selectChangeStatisticsByOperator(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime,
                                                              @Param("limit") Integer limit);

    /**
     * 查询变更趋势统计（按小时）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 变更趋势统计
     */
    @Select("SELECT DATE_FORMAT(change_time, '%Y-%m-%d %H:00:00') as hour_bucket, " +
            "COUNT(*) as change_count, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= #{startTime} AND change_time <= #{endTime} " +
            "GROUP BY DATE_FORMAT(change_time, '%Y-%m-%d %H:00:00') " +
            "ORDER BY hour_bucket ASC")
    List<Map<String, Object>> selectChangeTrendByHour(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询变更趋势统计（按天）
     *
     * @param days 最近天数
     * @return 变更趋势统计
     */
    @Select("SELECT DATE_FORMAT(change_time, '%Y-%m-%d') as day_bucket, " +
            "COUNT(*) as change_count, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(CASE WHEN risk_level = 'HIGH' OR risk_level = 'CRITICAL' THEN 1 ELSE 0 END) as high_risk_count " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE_FORMAT(change_time, '%Y-%m-%d') " +
            "ORDER BY day_bucket ASC")
    List<Map<String, Object>> selectChangeTrendByDay(@Param("days") Integer days);

    /**
     * 查询异常变更模式
     *
     * @param hours 最近小时数
     * @return 异常变更模式列表
     */
    @Select("SELECT operator_id, config_key, COUNT(*) as change_frequency, " +
            "MAX(change_time) as last_change_time " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= DATE_SUB(NOW(), INTERVAL #{hours} HOUR) " +
            "GROUP BY operator_id, config_key " +
            "HAVING COUNT(*) > 5 " +
            "ORDER BY change_frequency DESC")
    List<Map<String, Object>> selectAbnormalChangePatterns(@Param("hours") Integer hours);

    /**
     * 查询变更环境影响统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 环境影响统计
     */
    @Select("SELECT impact_scope, COUNT(*) as change_count, " +
            "SUM(affected_users) as total_affected_users, " +
            "SUM(affected_devices) as total_affected_devices, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= #{startTime} AND change_time <= #{endTime} " +
            "GROUP BY impact_scope ORDER BY change_count DESC")
    List<Map<String, Object>> selectImpactStatistics(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询通知发送统计
     *
     * @param days 最近天数
     * @return 通知发送统计
     */
    @Select("SELECT notification_status, COUNT(*) as count, " +
            "SUM(CASE WHEN notification_status != 'NOT_SENT' THEN 1 ELSE 0 END) as sent_count " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY notification_status ORDER BY count DESC")
    List<Map<String, Object>> selectNotificationStatistics(@Param("days") Integer days);

    /**
     * 更新变更状态
     *
     * @param auditId 审计ID
     * @param status 新状态
     * @param errorMessage 错误信息
     * @param executionTime 执行时间
     * @return 更新行数
     */
    @Update("UPDATE t_config_change_audit SET change_status = #{status}, " +
            "error_message = #{errorMessage}, execution_time = #{executionTime}, " +
            "update_time = NOW() WHERE audit_id = #{auditId}")
    @Transactional(rollbackFor = Exception.class)
    int updateChangeStatus(@Param("auditId") Long auditId,
                         @Param("status") String status,
                         @Param("errorMessage") String errorMessage,
                         @Param("executionTime") Long executionTime);

    /**
     * 更新审批状态
     *
     * @param auditId 审计ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approvalComment 审批意见
     * @return 更新行数
     */
    @Update("UPDATE t_config_change_audit SET approver_id = #{approverId}, " +
            "approver_name = #{approverName}, approval_comment = #{approvalComment}, " +
            "approval_time = NOW(), update_time = NOW() " +
            "WHERE audit_id = #{auditId}")
    @Transactional(rollbackFor = Exception.class)
    int updateApprovalStatus(@Param("auditId") Long auditId,
                            @Param("approverId") Long approverId,
                            @Param("approverName") String approverName,
                            @Param("approvalComment") String approvalComment);

    /**
     * 更新通知状态
     *
     * @param auditId 审计ID
     * @param notificationStatus 通知状态
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    @Update("UPDATE t_config_change_audit SET notification_status = #{notificationStatus}, " +
            "notification_time = NOW(), update_time = NOW() " +
            "WHERE audit_id = #{auditId}")
    @Transactional(rollbackFor = Exception.class)
    int updateNotificationStatus(@Param("auditId") Long auditId,
                                @Param("notificationStatus") String notificationStatus,
                                @Param("errorMessage") String errorMessage);

    /**
     * 批量更新通知状态
     *
     * @param auditIds 审计ID列表
     * @param notificationStatus 通知状态
     * @return 更新行数
     */
    @Update("UPDATE t_config_change_audit SET notification_status = #{notificationStatus}, " +
            "notification_time = NOW(), update_time = NOW() " +
            "WHERE audit_id IN " +
            "<foreach collection='auditIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>")
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateNotificationStatus(@Param("auditIds") List<Long> auditIds,
                                    @Param("notificationStatus") String notificationStatus);

    /**
     * 获取审计记录总数
     *
     * @return 总记录数
     */
    @Select("SELECT COUNT(*) FROM t_config_change_audit")
    Long selectTotalAuditCount();

    /**
     * 获取今日变更统计
     *
     * @return 今日变更统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_changes, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(CASE WHEN risk_level = 'HIGH' OR risk_level = 'CRITICAL' THEN 1 ELSE 0 END) as high_risk_count, " +
            "SUM(CASE WHEN require_approval = 1 THEN 1 ELSE 0 END) as approval_count " +
            "FROM t_config_change_audit " +
            "WHERE DATE(change_time) = CURDATE()")
    Map<String, Object> selectTodayChangeStatistics();

    /**
     * 获取本周变更统计
     *
     * @return 本周变更统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_changes, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(affected_users) as total_affected_users, " +
            "SUM(affected_devices) as total_affected_devices, " +
            "AVG(execution_time) as avg_execution_time " +
            "FROM t_config_change_audit " +
            "WHERE YEARWEEK(change_time, 1) = YEARWEEK(NOW(), 1)")
    Map<String, Object> selectWeeklyChangeStatistics();

    /**
     * 获取本月变更统计
     *
     * @return 本月变更统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_changes, " +
            "SUM(CASE WHEN change_status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
            "SUM(CASE WHEN change_status = 'FAILED' THEN 1 ELSE 0 END) as failed_count, " +
            "SUM(affected_users) as total_affected_users, " +
            "SUM(affected_devices) as total_affected_devices, " +
            "SUM(CASE WHEN risk_level = 'HIGH' OR risk_level = 'CRITICAL' THEN 1 ELSE 0 END) as high_risk_count, " +
            "SUM(CASE WHEN is_sensitive = 1 THEN 1 ELSE 0 END) as sensitive_count " +
            "FROM t_config_change_audit " +
            "WHERE YEAR(change_time) = YEAR(NOW()) AND MONTH(change_time) = MONTH(NOW())")
    Map<String, Object> selectMonthlyChangeStatistics();

    /**
     * 查询热力图数据（按天和小时统计变更数量）
     *
     * @param days 最近天数
     * @return 热力图数据
     */
    @Select("SELECT " +
            "DAYOFWEEK(change_time) - 1 as day_of_week, " +
            "HOUR(change_time) as hour_of_day, " +
            "COUNT(*) as change_count " +
            "FROM t_config_change_audit " +
            "WHERE change_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DAYOFWEEK(change_time), HOUR(change_time) " +
            "ORDER BY day_of_week, hour_of_day")
    List<Map<String, Object>> selectHeatmapData(@Param("days") Integer days);
}