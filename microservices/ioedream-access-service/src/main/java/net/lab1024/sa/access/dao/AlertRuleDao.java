package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AlertRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 告警规则DAO接口
 * <p>
 * 提供告警规则数据的访问操作：
 * - 基础CRUD（继承BaseMapper）
 * - 查询启用的规则
 * - 按规则类型查询
 * - 按优先级排序查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AlertRuleDao extends BaseMapper<AlertRuleEntity> {

    /**
     * 查询所有启用的规则，按优先级降序排列
     *
     * @return 启用的规则列表
     */
    @Select("SELECT * FROM t_alert_rule WHERE enabled = 1 AND deleted_flag = 0 ORDER BY priority DESC, rule_id ASC")
    List<AlertRuleEntity> selectEnabledRules();

    /**
     * 根据规则类型查询启用的规则
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    @Select("SELECT * FROM t_alert_rule WHERE rule_type = #{ruleType} AND enabled = 1 AND deleted_flag = 0 ORDER BY priority DESC")
    List<AlertRuleEntity> selectByRuleType(@Param("ruleType") String ruleType);

    /**
     * 根据规则编码查询规则
     *
     * @param ruleCode 规则编码
     * @return 规则实体
     */
    @Select("SELECT * FROM t_alert_rule WHERE rule_code = #{ruleCode} AND deleted_flag = 0")
    AlertRuleEntity selectByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 查询指定告警级别的规则
     *
     * @param alertLevel 告警级别
     * @return 规则列表
     */
    @Select("SELECT * FROM t_alert_rule WHERE alert_level = #{alertLevel} AND enabled = 1 AND deleted_flag = 0 ORDER BY priority DESC")
    List<AlertRuleEntity> selectByAlertLevel(@Param("alertLevel") Integer alertLevel);

    /**
     * 查询启用告警聚合的规则
     *
     * @return 规则列表
     */
    @Select("SELECT * FROM t_alert_rule WHERE alert_aggregation_enabled = 1 AND enabled = 1 AND deleted_flag = 0")
    List<AlertRuleEntity> selectAggregationEnabledRules();

    /**
     * 查询启用告警升级的规则
     *
     * @return 规则列表
     */
    @Select("SELECT * FROM t_alert_rule WHERE alert_escalation_enabled = 1 AND enabled = 1 AND deleted_flag = 0")
    List<AlertRuleEntity> selectEscalationEnabledRules();
}
