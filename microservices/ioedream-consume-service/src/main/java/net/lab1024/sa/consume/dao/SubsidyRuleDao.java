package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.SubsidyRuleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 补贴规则DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Mapper
public interface SubsidyRuleDao extends BaseMapper<SubsidyRuleEntity> {

    /**
     * 查询启用中的规则
     */
    List<SubsidyRuleEntity> selectActiveRules();

    /**
     * 查询指定类型的规则
     */
    List<SubsidyRuleEntity> selectBySubsidyType(@Param("subsidyType") Integer subsidyType);

    /**
     * 查询优先级最高的规则
     */
    List<SubsidyRuleEntity> selectByPriorityOrder(@Param("limit") Integer limit);

    /**
     * 查询当前生效的规则
     */
    List<SubsidyRuleEntity> selectEffectiveRules(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询规则按优先级
     */
    List<SubsidyRuleEntity> selectByPriority(@Param("subsidyType") Integer subsidyType,
                                               @Param("currentTime") LocalDateTime currentTime);
}
