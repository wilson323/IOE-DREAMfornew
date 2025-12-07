package net.lab1024.sa.common.monitor.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 告警规则DAO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解（禁止@Repository）
 * - 使用Dao后缀（禁止Repository后缀）
 * - 继承BaseMapper提供基础CRUD
 * - 使用MyBatis-Plus（禁止JPA）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Mapper
public interface AlertRuleDao extends BaseMapper<AlertRuleEntity> {

    /**
     * 查询启用的告警规则
     *
     * @return 启用的告警规则列表
     */
    List<AlertRuleEntity> selectEnabledRules();

    /**
     * 根据监控指标查询告警规则
     *
     * @param metricName 监控指标名称
     * @return 告警规则列表
     */
    List<AlertRuleEntity> selectByMetricName(@Param("metricName") String metricName);

    /**
     * 查询需要检查的告警规则
     *
     * @param currentTime 当前时间
     * @return 需要检查的告警规则列表
     */
    List<AlertRuleEntity> selectRulesForCheck(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 更新规则最后检查时间
     *
     * @param ruleId        规则ID
     * @param lastCheckTime 最后检查时间
     * @return 影响行数
     */
    int updateLastCheckTime(@Param("ruleId") Long ruleId, @Param("lastCheckTime") LocalDateTime lastCheckTime);

    /**
     * 统计告警规则数量按类型
     *
     * @return 统计结果
     */
    List<Map<String, Object>> countRulesByMetricName();

    /**
     * 查询近期触发的告警规则
     *
     * @param hours 小时数
     * @return 触发的告警规则列表
     */
    List<AlertRuleEntity> selectRecentlyTriggeredRules(@Param("hours") Integer hours);
}
