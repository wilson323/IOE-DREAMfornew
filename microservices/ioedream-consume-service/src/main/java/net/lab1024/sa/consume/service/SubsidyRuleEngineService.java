package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.lab1024.sa.consume.entity.SubsidyRuleEntity;
import net.lab1024.sa.consume.domain.dto.SubsidyCalculationDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyResultDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 补贴规则引擎Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
public interface SubsidyRuleEngineService extends IService<SubsidyRuleEntity> {

    /**
     * 计算补贴（核心引擎）
     *
     * @param calculationDTO 计算参数
     * @return 补贴结果
     */
    SubsidyResultDTO calculateSubsidy(SubsidyCalculationDTO calculationDTO);

    /**
     * 执行规则（包含日志记录）
     *
     * @param calculationDTO 计算参数
     * @return 补贴结果
     */
    SubsidyResultDTO executeRule(SubsidyCalculationDTO calculationDTO);

    /**
     * 匹配规则
     *
     * @param calculationDTO 计算参数
     * @return 匹配的规则列表（按优先级排序）
     */
    List<SubsidyRuleEntity> matchRules(SubsidyCalculationDTO calculationDTO);

    /**
     * 验证规则条件
     *
     * @param rule 规则
     * @param calculationDTO 计算参数
     * @return 是否匹配
     */
    boolean validateRule(SubsidyRuleEntity rule, SubsidyCalculationDTO calculationDTO);

    /**
     * 查询有效规则
     *
     * @return 有效规则列表
     */
    List<SubsidyRuleEntity> getEffectiveRules();

    /**
     * 查询指定类型的规则
     *
     * @param subsidyType 补贴类型
     * @return 规则列表
     */
    List<SubsidyRuleEntity> getRulesBySubsidyType(Integer subsidyType);

    /**
     * 启用规则
     *
     * @param ruleId 规则ID
     */
    void enableRule(Long ruleId);

    /**
     * 禁用规则
     *
     * @param ruleId 规则ID
     */
    void disableRule(Long ruleId);

    /**
     * 调整规则优先级
     *
     * @param ruleId 规则ID
     * @param priority 新优先级
     */
    void adjustPriority(Long ruleId, Integer priority);
}
