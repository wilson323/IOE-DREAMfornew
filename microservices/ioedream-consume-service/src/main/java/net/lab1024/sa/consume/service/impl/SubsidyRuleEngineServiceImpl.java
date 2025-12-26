package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.SubsidyRuleConditionDao;
import net.lab1024.sa.consume.dao.SubsidyRuleDao;
import net.lab1024.sa.consume.dao.SubsidyRuleLogDao;
import net.lab1024.sa.consume.dao.UserSubsidyRecordDao;
import net.lab1024.sa.consume.domain.dto.SubsidyCalculationDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyResultDTO;
import net.lab1024.sa.consume.entity.SubsidyRuleConditionEntity;
import net.lab1024.sa.consume.entity.SubsidyRuleEntity;
import net.lab1024.sa.consume.entity.SubsidyRuleLogEntity;
import net.lab1024.sa.consume.entity.UserSubsidyRecordEntity;
import net.lab1024.sa.consume.service.SubsidyRuleEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 补贴规则引擎Service实现（核心引擎）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Service
@Slf4j
public class SubsidyRuleEngineServiceImpl extends ServiceImpl<SubsidyRuleDao, SubsidyRuleEntity>
        implements SubsidyRuleEngineService {

    @Resource
    private SubsidyRuleDao subsidyRuleDao;

    @Resource
    private SubsidyRuleConditionDao ruleConditionDao;

    @Resource
    private SubsidyRuleLogDao ruleLogDao;

    @Resource
    private UserSubsidyRecordDao subsidyRecordDao;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public SubsidyResultDTO calculateSubsidy(SubsidyCalculationDTO calculationDTO) {
        log.debug("[补贴规则引擎] 计算补贴: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());

        // 1. 匹配规则
        List<SubsidyRuleEntity> matchedRules = matchRules(calculationDTO);

        if (matchedRules.isEmpty()) {
            log.debug("[补贴规则引擎] 未匹配到规则");
            return SubsidyResultDTO.noMatch();
        }

        // 2. 取优先级最高的规则
        SubsidyRuleEntity rule = matchedRules.get(0);
        log.debug("[补贴规则引擎] 匹配到规则: ruleCode={}, priority={}",
                rule.getRuleCode(), rule.getPriority());

        // 3. 计算补贴
        return calculateByRule(rule, calculationDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubsidyResultDTO executeRule(SubsidyCalculationDTO calculationDTO) {
        log.info("[补贴规则引擎] 执行规则: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());

        SubsidyResultDTO result = calculateSubsidy(calculationDTO);

        // 记录执行日志
        recordExecutionLog(calculationDTO, result);

        // 保存补贴记录
        if (result.getMatched() && result.getSuccess()) {
            saveSubsidyRecord(calculationDTO, result);
        }

        return result;
    }

    @Override
    public List<SubsidyRuleEntity> matchRules(SubsidyCalculationDTO calculationDTO) {
        log.debug("[补贴规则引擎] 匹配规则: subsidyType={}", calculationDTO.getSubsidyType());

        // 查询有效规则
        List<SubsidyRuleEntity> rules = subsidyRuleDao.selectByPriority(
                calculationDTO.getSubsidyType(),
                LocalDateTime.now()
        );

        // 验证每个规则
        List<SubsidyRuleEntity> matchedRules = new ArrayList<>();
        for (SubsidyRuleEntity rule : rules) {
            if (validateRule(rule, calculationDTO)) {
                matchedRules.add(rule);
            }
        }

        log.debug("[补贴规则引擎] 匹配结果: total={}, matched={}", rules.size(), matchedRules.size());
        return matchedRules;
    }

    @Override
    public boolean validateRule(SubsidyRuleEntity rule, SubsidyCalculationDTO calculationDTO) {
        try {
            // 1. 验证状态
            if (rule.getStatus() != 1) {
                return false;
            }

            // 2. 验证有效期
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(rule.getEffectiveDate()) ||
                    (rule.getExpireDate() != null && now.isAfter(rule.getExpireDate()))) {
                return false;
            }

            // 3. 验证时间条件
            if (!validateTimeCondition(rule, calculationDTO)) {
                return false;
            }

            // 4. 验证餐别条件
            if (!validateMealTypeCondition(rule, calculationDTO)) {
                return false;
            }

            // 5. 验证自定义条件
            if (!validateCustomConditions(rule, calculationDTO)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("[补贴规则引擎] 验证规则异常: ruleCode={}, error={}",
                    rule.getRuleCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<SubsidyRuleEntity> getEffectiveRules() {
        return subsidyRuleDao.selectEffectiveRules(LocalDateTime.now());
    }

    @Override
    public List<SubsidyRuleEntity> getRulesBySubsidyType(Integer subsidyType) {
        return subsidyRuleDao.selectBySubsidyType(subsidyType);
    }

    @Override
    public void enableRule(Long ruleId) {
        log.info("[补贴规则引擎] 启用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(1);
            this.updateById(rule);
        }
    }

    @Override
    public void disableRule(Long ruleId) {
        log.info("[补贴规则引擎] 禁用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(0);
            this.updateById(rule);
        }
    }

    @Override
    public void adjustPriority(Long ruleId, Integer priority) {
        log.info("[补贴规则引擎] 调整优先级: ruleId={}, priority={}", ruleId, priority);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setPriority(priority);
            this.updateById(rule);
        }
    }

    /**
     * 按规则计算补贴
     */
    private SubsidyResultDTO calculateByRule(SubsidyRuleEntity rule,
                                              SubsidyCalculationDTO calculationDTO) {
        BigDecimal consumeAmount = calculationDTO.getConsumeAmount();
        BigDecimal subsidyAmount;

        try {
            switch (rule.getRuleType()) {
                case 1: // 固定金额
                    subsidyAmount = rule.getSubsidyAmount();
                    break;

                case 2: // 比例补贴
                    subsidyAmount = consumeAmount.multiply(rule.getSubsidyRate())
                            .setScale(2, RoundingMode.HALF_UP);
                    if (rule.getMaxSubsidyAmount() != null) {
                        subsidyAmount = subsidyAmount.min(rule.getMaxSubsidyAmount());
                    }
                    break;

                case 3: // 阶梯补贴
                    subsidyAmount = calculateTierSubsidy(rule, consumeAmount);
                    break;

                case 4: // 限时补贴
                    subsidyAmount = rule.getSubsidyAmount();
                    break;

                default:
                    return SubsidyResultDTO.error("不支持的规则类型: " + rule.getRuleType());
            }

            String detail = String.format("规则: %s, 消费: %.2f, 补贴: %.2f",
                    rule.getRuleName(), consumeAmount, subsidyAmount);

            return SubsidyResultDTO.success(
                    rule.getId(),
                    rule.getRuleCode(),
                    rule.getRuleName(),
                    subsidyAmount,
                    detail
            );
        } catch (Exception e) {
            log.error("[补贴规则引擎] 计算补贴异常: ruleCode={}, error={}",
                    rule.getRuleCode(), e.getMessage(), e);
            return SubsidyResultDTO.error("计算失败: " + e.getMessage());
        }
    }

    /**
     * 计算阶梯补贴
     */
    private BigDecimal calculateTierSubsidy(SubsidyRuleEntity rule, BigDecimal consumeAmount) {
        if (rule.getTierConfig() == null || rule.getTierConfig().isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<TierConfig> tiers;
        try {
            tiers = objectMapper.readValue(rule.getTierConfig(),
                    new TypeReference<List<TierConfig>>() {}
            );
        } catch (Exception e) {
            log.error("[补贴规则引擎] 解析阶梯配置失败: tierConfig={}", rule.getTierConfig(), e);
            return BigDecimal.ZERO;
        }

        BigDecimal subsidyAmount = BigDecimal.ZERO;
        for (TierConfig tier : tiers) {
            if (consumeAmount.compareTo(tier.getMinAmount()) >= 0) {
                subsidyAmount = consumeAmount.multiply(tier.getRate())
                        .setScale(2, RoundingMode.HALF_UP);
                break;
            }
        }

        if (rule.getMaxSubsidyAmount() != null) {
            subsidyAmount = subsidyAmount.min(rule.getMaxSubsidyAmount());
        }

        return subsidyAmount;
    }

    /**
     * 验证时间条件
     */
    private boolean validateTimeCondition(SubsidyRuleEntity rule,
                                           SubsidyCalculationDTO calculationDTO) {
        LocalDateTime consumeTime = calculationDTO.getConsumeTime();
        DayOfWeek dayOfWeek = consumeTime.getDayOfWeek();
        LocalTime time = consumeTime.toLocalTime();

        // 验证时间类型
        switch (rule.getApplyTimeType()) {
            case 1: // 全部
                return true;

            case 2: // 工作日
                return dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue() &&
                        dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue();

            case 3: // 周末
                return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;

            case 4: // 自定义
                if (rule.getApplyDays() != null) {
                    String[] days = rule.getApplyDays().split(",");
                    for (String day : days) {
                        if (Integer.parseInt(day.trim()) == dayOfWeek.getValue()) {
                            // 验证时段
                            if (rule.getApplyStartTime() != null && rule.getApplyEndTime() != null) {
                                return !time.isBefore(rule.getApplyStartTime()) &&
                                        !time.isAfter(rule.getApplyEndTime());
                            }
                            return true;
                        }
                    }
                    return false;
                }
                return false;

            default:
                return true;
        }
    }

    /**
     * 验证餐别条件
     */
    private boolean validateMealTypeCondition(SubsidyRuleEntity rule,
                                              SubsidyCalculationDTO calculationDTO) {
        if (rule.getApplyMealTypes() == null || rule.getApplyMealTypes().isEmpty()) {
            return true; // 不限制餐别
        }

        String[] mealTypes = rule.getApplyMealTypes().split(",");
        for (String mealType : mealTypes) {
            if (Integer.parseInt(mealType.trim()) == calculationDTO.getMealType()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证自定义条件
     */
    private boolean validateCustomConditions(SubsidyRuleEntity rule,
                                             SubsidyCalculationDTO calculationDTO) {
        List<SubsidyRuleConditionEntity> conditions =
                ruleConditionDao.selectActiveByRuleId(rule.getId());

        for (SubsidyRuleConditionEntity condition : conditions) {
            if (!evaluateCondition(condition, calculationDTO)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 评估单个条件
     */
    private boolean evaluateCondition(SubsidyRuleConditionEntity condition,
                                      SubsidyCalculationDTO calculationDTO) {
        // TODO: 实现复杂条件评估逻辑
        // 支持 user_group/department/area/device/consume_amount 等条件类型
        return true;
    }

    /**
     * 记录执行日志
     */
    private void recordExecutionLog(SubsidyCalculationDTO calculationDTO,
                                     SubsidyResultDTO result) {
        try {
            SubsidyRuleLogEntity log = new SubsidyRuleLogEntity();
            log.setLogUuid("LOG_" + System.currentTimeMillis());
            log.setRuleId(result.getRuleId());
            log.setRuleCode(result.getRuleCode());
            log.setRuleName(result.getRuleName());
            log.setConsumeId(calculationDTO.getConsumeId());
            log.setUserId(calculationDTO.getUserId());
            log.setDeviceId(calculationDTO.getDeviceId());
            log.setConsumeAmount(calculationDTO.getConsumeAmount());
            log.setConsumeTime(calculationDTO.getConsumeTime());
            log.setSubsidyAmount(result.getSubsidyAmount());
            log.setCalculationDetail(result.getCalculationDetail());
            log.setExecutionStatus(result.getSuccess() ? 1 : 2);
            log.setErrorMessage(result.getErrorMessage());

            ruleLogDao.insert(log);
        } catch (Exception e) {
            log.error("[补贴规则引擎] 记录日志失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 保存补贴记录
     */
    private void saveSubsidyRecord(SubsidyCalculationDTO calculationDTO,
                                    SubsidyResultDTO result) {
        try {
            UserSubsidyRecordEntity record = new UserSubsidyRecordEntity();
            record.setRecordUuid("REC_" + System.currentTimeMillis());
            record.setUserId(calculationDTO.getUserId());
            record.setSubsidyType(calculationDTO.getSubsidyType());
            record.setSubsidyAmount(result.getSubsidyAmount());
            record.setConsumeAmount(calculationDTO.getConsumeAmount());
            record.setRuleId(result.getRuleId());
            record.setRuleCode(result.getRuleCode());
            record.setConsumeId(calculationDTO.getConsumeId());
            record.setDeviceId(calculationDTO.getDeviceId());
            record.setMealType(calculationDTO.getMealType());
            record.setSubsidyDate(calculationDTO.getConsumeTime().toLocalDate());
            record.setConsumeTime(calculationDTO.getConsumeTime());
            record.setStatus(1);

            subsidyRecordDao.insert(record);
        } catch (Exception e) {
            log.error("[补贴规则引擎] 保存补贴记录失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 阶梯配置内部类
     */
    @lombok.Data
    private static class TierConfig {
        private java.math.BigDecimal minAmount;
        private java.math.BigDecimal rate;
    }
}
