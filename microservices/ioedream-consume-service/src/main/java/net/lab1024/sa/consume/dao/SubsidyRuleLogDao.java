package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.SubsidyRuleLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 补贴规则执行日志DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Mapper
public interface SubsidyRuleLogDao extends BaseMapper<SubsidyRuleLogEntity> {

    /**
     * 查询规则的执行日志
     */
    List<SubsidyRuleLogEntity> selectByRuleId(@Param("ruleId") Long ruleId,
                                               @Param("limit") Integer limit);

    /**
     * 查询用户的执行日志
     */
    List<SubsidyRuleLogEntity> selectByUserId(@Param("userId") Long userId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计规则使用次数
     */
    Long countByRuleId(@Param("ruleId") Long ruleId);

    /**
     * 统计规则补贴总额
 */
    java.math.BigDecimal sumSubsidyByRuleId(@Param("ruleId") Long ruleId);
}
