package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.entity.SubsidyRuleConditionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 补贴规则条件DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Mapper
public interface SubsidyRuleConditionDao extends BaseMapper<SubsidyRuleConditionEntity> {

    /**
     * 查询规则的所有条件
     */
    List<SubsidyRuleConditionEntity> selectByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询规则的所有启用条件
     */
    List<SubsidyRuleConditionEntity> selectActiveByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 查询规则的所有启用条件
     */
    List<SubsidyRuleConditionEntity> selectActiveByRuleCode(@Param("ruleCode") String ruleCode);

    /**
     * 批量删除规则条件
     */
    int deleteByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 批量查询多个规则的启用条件
     */
    List<SubsidyRuleConditionEntity> selectActiveByRuleIds(@Param("ruleIds") List<Long> ruleIds);
}
