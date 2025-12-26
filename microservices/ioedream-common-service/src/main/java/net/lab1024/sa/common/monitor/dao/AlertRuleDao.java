package net.lab1024.sa.common.monitor.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;

@Mapper
public interface AlertRuleDao extends BaseMapper<AlertRuleEntity> {

    List<AlertRuleEntity> selectEnabledRules();

    List<AlertRuleEntity> selectByMetricName(String metricName);

    List<AlertRuleEntity> selectRulesForCheck(LocalDateTime checkTime);

    int updateLastCheckTime(Long ruleId, LocalDateTime lastCheckTime);

    List<Map<String, Object>> countRulesByMetricName();

    List<AlertRuleEntity> selectRecentlyTriggeredRules(Integer limit);
}
