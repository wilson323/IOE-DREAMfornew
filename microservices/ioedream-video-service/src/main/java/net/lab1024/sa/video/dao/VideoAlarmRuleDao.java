package net.lab1024.sa.video.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.video.entity.VideoAlarmRuleEntity;

/**
 * 视频告警规则数据访问层
 * <p>
 * 提供告警规则数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoAlarmRuleDao extends BaseMapper<VideoAlarmRuleEntity> {

    /**
     * 根据规则状态查询告警规则
     *
     * @param ruleStatus 规则状态
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE rule_status = #{ruleStatus} AND deleted = 0 ORDER BY priority DESC, create_time DESC")
    List<VideoAlarmRuleEntity> selectByRuleStatus(@Param("ruleStatus") Integer ruleStatus);

    /**
     * 根据规则类型查询告警规则
     *
     * @param ruleType 规则类型
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE rule_type = #{ruleType} AND deleted = 0 ORDER BY priority DESC, create_time DESC")
    List<VideoAlarmRuleEntity> selectByRuleType(@Param("ruleType") Integer ruleType);

    /**
     * 根据设备ID查询告警规则
     *
     * @param deviceId 设备ID
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE JSON_CONTAINS(device_ids, #{deviceId}) AND rule_status = 1 AND deleted = 0 ORDER BY priority DESC")
    List<VideoAlarmRuleEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据区域ID查询告警规则
     *
     * @param areaId 区域ID
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE area_id = #{areaId} AND deleted = 0 ORDER BY priority DESC, create_time DESC")
    List<VideoAlarmRuleEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 查询生效的告警规则
     *
     * @return 生效告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE rule_status = 1 AND " +
            "(effective_time IS NULL OR effective_time <= NOW()) AND " +
            "(expire_time IS NULL OR expire_time > NOW()) AND deleted = 0 ORDER BY priority DESC")
    List<VideoAlarmRuleEntity> selectEffectiveRules();

    /**
     * 查询已过期的告警规则
     *
     * @return 过期告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE rule_status = 1 AND expire_time IS NOT NULL AND expire_time <= NOW() AND deleted = 0")
    List<VideoAlarmRuleEntity> selectExpiredRules();

    /**
     * 根据优先级查询告警规则
     *
     * @param priority 优先级
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE priority = #{priority} AND deleted = 0 ORDER BY create_time DESC")
    List<VideoAlarmRuleEntity> selectByPriority(@Param("priority") Integer priority);

    /**
     * 根据告警级别查询告警规则
     *
     * @param alarmLevel 告警级别
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE alarm_level = #{alarmLevel} AND deleted = 0 ORDER BY priority DESC, create_time DESC")
    List<VideoAlarmRuleEntity> selectByAlarmLevel(@Param("alarmLevel") Integer alarmLevel);

    /**
     * 根据审核状态查询告警规则
     *
     * @param auditStatus 审核状态
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE audit_status = #{auditStatus} AND deleted = 0 ORDER BY create_time DESC")
    List<VideoAlarmRuleEntity> selectByAuditStatus(@Param("auditStatus") Integer auditStatus);

    /**
     * 根据创建人查询告警规则
     *
     * @param createdBy 创建人ID
     * @return 告警规则列表
     */
    @Select("SELECT * FROM t_video_alarm_rule WHERE created_by = #{createdBy} AND deleted = 0 ORDER BY create_time DESC")
    List<VideoAlarmRuleEntity> selectByCreatedBy(@Param("createdBy") Long createdBy);

    /**
     * 分页查询告警规则
     *
     * @param page 分页参数
     * @param ruleName 规则名称（可选）
     * @param ruleType 规则类型（可选）
     * @param ruleStatus 规则状态（可选）
     * @param priority 优先级（可选）
     * @param alarmLevel 告警级别（可选）
     * @param areaId 区域ID（可选）
     * @param createdBy 创建人ID（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT * FROM t_video_alarm_rule WHERE deleted = 0 " +
            "<if test='ruleName != null and ruleName != \"\"'> AND rule_name LIKE CONCAT('%', #{ruleName}, '%') </if>" +
            "<if test='ruleType != null'> AND rule_type = #{ruleType} </if>" +
            "<if test='ruleStatus != null'> AND rule_status = #{ruleStatus} </if>" +
            "<if test='priority != null'> AND priority = #{priority} </if>" +
            "<if test='alarmLevel != null'> AND alarm_level = #{alarmLevel} </if>" +
            "<if test='areaId != null'> AND area_id = #{areaId} </if>" +
            "<if test='createdBy != null'> AND created_by = #{createdBy} </if>" +
            "ORDER BY priority DESC, create_time DESC" +
            "</script>")
    IPage<VideoAlarmRuleEntity> selectAlarmRulePage(Page<VideoAlarmRuleEntity> page,
                                                   @Param("ruleName") String ruleName,
                                                   @Param("ruleType") Integer ruleType,
                                                   @Param("ruleStatus") Integer ruleStatus,
                                                   @Param("priority") Integer priority,
                                                   @Param("alarmLevel") Integer alarmLevel,
                                                   @Param("areaId") Long areaId,
                                                   @Param("createdBy") Long createdBy);

    /**
     * 更新告警规则状态
     *
     * @param ruleId 规则ID
     * @param ruleStatus 新状态
     * @return 影响行数
     */
    @Update("UPDATE t_video_alarm_rule SET rule_status = #{ruleStatus}, update_time = NOW() WHERE rule_id = #{ruleId}")
    @Transactional(rollbackFor = Exception.class)
    int updateRuleStatus(@Param("ruleId") Long ruleId, @Param("ruleStatus") Integer ruleStatus);

    /**
     * 更新审核状态
     *
     * @param ruleId 规则ID
     * @param auditStatus 审核状态
     * @param auditBy 审核人ID
     * @param auditByName 审核人姓名
     * @param auditComment 审核意见
     * @return 影响行数
     */
    @Update("UPDATE t_video_alarm_rule SET audit_status = #{auditStatus}, audit_by = #{auditBy}, " +
            "audit_by_name = #{auditByName}, audit_time = NOW(), audit_comment = #{auditComment}, update_time = NOW() " +
            "WHERE rule_id = #{ruleId}")
    @Transactional(rollbackFor = Exception.class)
    int updateAuditStatus(@Param("ruleId") Long ruleId,
                          @Param("auditStatus") Integer auditStatus,
                          @Param("auditBy") Long auditBy,
                          @Param("auditByName") String auditByName,
                          @Param("auditComment") String auditComment);

    /**
     * 批量启用告警规则
     *
     * @param ruleIds 规则ID列表
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_video_alarm_rule SET rule_status = 1, update_time = NOW() WHERE rule_id IN " +
            "<foreach collection='ruleIds' item='ruleId' open='(' separator=',' close=')'>#{ruleId}</foreach>" +
            " AND deleted = 0" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchEnableRules(@Param("ruleIds") List<Long> ruleIds);

    /**
     * 批量禁用告警规则
     *
     * @param ruleIds 规则ID列表
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE t_video_alarm_rule SET rule_status = 2, update_time = NOW() WHERE rule_id IN " +
            "<foreach collection='ruleIds' item='ruleId' open='(' separator=',' close=')'>#{ruleId}</foreach>" +
            " AND deleted = 0" +
            "</script>")
    @Transactional(rollbackFor = Exception.class)
    int batchDisableRules(@Param("ruleIds") List<Long> ruleIds);

    /**
     * 自动过期告警规则
     *
     * @return 影响行数
     */
    @Update("UPDATE t_video_alarm_rule SET rule_status = 2, update_time = NOW() " +
            "WHERE rule_status = 1 AND expire_time IS NOT NULL AND expire_time <= NOW() AND deleted = 0")
    @Transactional(rollbackFor = Exception.class)
    int autoExpireRules();

    /**
     * 统计告警规则数量
     *
     * @param ruleType 规则类型（可选）
     * @param ruleStatus 规则状态（可选）
     * @param areaId 区域ID（可选）
     * @param createdBy 创建人ID（可选）
     * @return 统计数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_video_alarm_rule WHERE deleted = 0 " +
            "<if test='ruleType != null'> AND rule_type = #{ruleType} </if>" +
            "<if test='ruleStatus != null'> AND rule_status = #{ruleStatus} </if>" +
            "<if test='areaId != null'> AND area_id = #{areaId} </if>" +
            "<if test='createdBy != null'> AND created_by = #{createdBy} </if>" +
            "</script>")
    Long countAlarmRules(@Param("ruleType") Integer ruleType,
                         @Param("ruleStatus") Integer ruleStatus,
                         @Param("areaId") Long areaId,
                         @Param("createdBy") Long createdBy);

    /**
     * 统计各规则类型的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT rule_type, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 GROUP BY rule_type ORDER BY count DESC")
    List<java.util.Map<String, Object>> countRulesByType();

    /**
     * 统计各规则状态的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT rule_status, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 GROUP BY rule_status ORDER BY rule_status")
    List<java.util.Map<String, Object>> countRulesByStatus();

    /**
     * 统计各优先级的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT priority, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 GROUP BY priority ORDER BY priority DESC")
    List<java.util.Map<String, Object>> countRulesByPriority();

    /**
     * 统计各告警级别的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT alarm_level, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 GROUP BY alarm_level ORDER BY alarm_level")
    List<java.util.Map<String, Object>> countRulesByAlarmLevel();

    /**
     * 统计各区域的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT area_id, area_name, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 AND area_id IS NOT NULL " +
            "GROUP BY area_id, area_name ORDER BY count DESC")
    List<java.util.Map<String, Object>> countRulesByArea();

    /**
     * 统计各审核状态的告警规则数量
     *
     * @return 统计结果
     */
    @Select("SELECT audit_status, COUNT(*) as count FROM t_video_alarm_rule WHERE deleted = 0 GROUP BY audit_status ORDER BY audit_status")
    List<java.util.Map<String, Object>> countRulesByAuditStatus();

    /**
     * 查询指定时间段内的规则创建统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM t_video_alarm_rule WHERE create_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 " +
            "GROUP BY DATE(create_time) ORDER BY date DESC")
    List<java.util.Map<String, Object>> getRuleCreationStatistics(@Param("startTime") LocalDateTime startTime,
                                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查询规则联动配置统计
     *
     * @return 统计结果
     */
    @Select("SELECT " +
            "SUM(CASE WHEN linkage_record = 1 THEN 1 ELSE 0 END) as record_count, " +
            "SUM(CASE WHEN linkage_snapshot = 1 THEN 1 ELSE 0 END) as snapshot_count, " +
            "SUM(CASE WHEN linkage_alarm = 1 THEN 1 ELSE 0 END) as alarm_count, " +
            "SUM(CASE WHEN linkage_access = 1 THEN 1 ELSE 0 END) as access_count, " +
            "SUM(CASE WHEN linkage_broadcast = 1 THEN 1 ELSE 0 END) as broadcast_count, " +
            "SUM(CASE WHEN linkage_sms = 1 THEN 1 ELSE 0 END) as sms_count, " +
            "SUM(CASE WHEN linkage_email = 1 THEN 1 ELSE 0 END) as email_count, " +
            "SUM(CASE WHEN linkage_push = 1 THEN 1 ELSE 0 END) as push_count " +
            "FROM t_video_alarm_rule WHERE deleted = 0")
    java.util.Map<String, Object> getRuleLinkageStatistics();
}
