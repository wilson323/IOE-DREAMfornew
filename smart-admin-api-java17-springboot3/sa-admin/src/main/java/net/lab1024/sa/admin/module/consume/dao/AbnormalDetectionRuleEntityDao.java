package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.AbnormalDetectionRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 异常检测规则 DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Mapper
public interface AbnormalDetectionRuleEntityDao extends BaseMapper<AbnormalDetectionRuleEntity> {

    /**
     * 根据规则类型查询规则
     */
    List<AbnormalDetectionRuleEntity> selectByRuleType(@Param("ruleType") String ruleType);

    /**
     * 根据状态查询规则
     */
    List<AbnormalDetectionRuleEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 查询启用的规则
     */
    List<AbnormalDetectionRuleEntity> selectEnabledRules();

    /**
     * 根据优先级查询规则
     */
    List<AbnormalDetectionRuleEntity> selectByPriority(@Param("priority") Integer priority);
}