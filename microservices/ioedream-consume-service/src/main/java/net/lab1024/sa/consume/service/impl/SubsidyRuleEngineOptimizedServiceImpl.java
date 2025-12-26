package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 补贴规则引擎优化实现（缓存+性能优化）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Service("subsidyRuleEngineOptimizedService")
@Slf4j
public class SubsidyRuleEngineOptimizedServiceImpl extends ServiceImpl<SubsidyRuleDao, SubsidyRuleEntity>
        implements SubsidyRuleEngineService {

    @Resource
    private SubsidyRuleDao subsidyRuleDao;

    @Resource
    private SubsidyRuleConditionDao ruleConditionDao;

    @Resource
    private SubsidyRuleLogDao ruleLogDao;

    @Resource
    private UserSubsidyRecordDao subsidyRecordDao;

    // 本地缓存：规则条件（避免频繁查询数据库）
    private final Map<Long, List<SubsidyRuleConditionEntity>> ruleConditionCache =
            new ConcurrentHashMap<>();

    // 缓存刷新时间（秒）
    private static final long CACHE_REFRESH_SECONDS = 300;

    /**
     * 计算补贴（带缓存优化）
     */
    @Override
    @Cacheable(value = "subsidyCalculation", key = "#calculationDTO.userId + '-' + #calculationDTO.consumeAmount")
    public SubsidyResultDTO calculateSubsidy(SubsidyCalculationDTO calculationDTO) {
        long startTime = System.currentTimeMillis();
        log.debug("[补贴规则引擎-优化] 计算补贴: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());

        // 1. 匹配规则（优先级排序）
        List<SubsidyRuleEntity> matchedRules = matchRules(calculationDTO);

        if (matchedRules.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[补贴规则引擎-优化] 未匹配到规则: 耗时={}ms", duration);
            return SubsidyResultDTO.noMatch();
        }

        // 2. 取优先级最高的规则
        SubsidyRuleEntity rule = matchedRules.get(0);
        log.debug("[补贴规则引擎-优化] 匹配到规则: ruleCode={}, priority={}",
                rule.getRuleCode(), rule.getPriority());

        // 3. 计算补贴
        SubsidyResultDTO result = calculateByRule(rule, calculationDTO);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("[补贴规则引擎-优化] 计算完成: 耗时={}ms", duration);

        return result;
    }

    /**
     * 执行规则（带事务和日志）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "subsidyCalculation", allEntries = true)
    public SubsidyResultDTO executeRule(SubsidyCalculationDTO calculationDTO) {
        log.info("[补贴规则引擎-优化] 执行规则: userId={}, amount={}",
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
        log.debug("[补贴规则引擎-优化] 匹配规则: subsidyType={}", calculationDTO.getSubsidyType());

        // 查询有效规则（已按优先级排序）
        List<SubsidyRuleEntity> rules = subsidyRuleDao.selectByPriority(
                calculationDTO.getSubsidyType(),
                LocalDateTime.now()
        );

        // 快速过滤：基本条件
        List<SubsidyRuleEntity> candidates = new ArrayList<>();
        for (SubsidyRuleEntity rule : rules) {
            // 快速过滤：状态、有效期
            if (rule.getStatus() != 1) {
                continue;
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(rule.getEffectiveDate()) ||
                    (rule.getExpireDate() != null && now.isAfter(rule.getExpireDate()))) {
                continue;
            }

            // 快速过滤：时间、餐别
            if (!validateTimeConditionFast(rule, calculationDTO.getConsumeTime())) {
                continue;
            }

            if (!validateMealTypeConditionFast(rule, calculationDTO.getMealType())) {
                continue;
            }

            candidates.add(rule);
        }

        log.debug("[补贴规则引擎-优化] 快速过滤: 总数={}, 候选={}", rules.size(), candidates.size());

        // 详细验证：自定义条件
        List<SubsidyRuleEntity> matchedRules = new ArrayList<>();
        for (SubsidyRuleEntity rule : candidates) {
            if (validateCustomConditions(rule, calculationDTO)) {
                matchedRules.add(rule);
            }
        }

        log.debug("[补贴规则引擎-优化] 匹配结果: 总数={}, 匹配={}", rules.size(), matchedRules.size());
        return matchedRules;
    }

    @Override
    public boolean validateRule(SubsidyRuleEntity rule, SubsidyCalculationDTO calculationDTO) {
        try {
            // 基本验证
            if (rule.getStatus() != 1) {
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(rule.getEffectiveDate()) ||
                    (rule.getExpireDate() != null && now.isAfter(rule.getExpireDate()))) {
                return false;
            }

            // 详细验证
            if (!validateTimeCondition(rule, calculationDTO)) {
                return false;
            }

            if (!validateMealTypeCondition(rule, calculationDTO)) {
                return false;
            }

            if (!validateCustomConditions(rule, calculationDTO)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("[补贴规则引擎-优化] 验证规则异常: ruleCode={}, error={}",
                    rule.getRuleCode(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Cacheable(value = "effectiveRules", key = "'all'")
    public List<SubsidyRuleEntity> getEffectiveRules() {
        log.debug("[补贴规则引擎-优化] 查询有效规则（缓存）");
        return subsidyRuleDao.selectEffectiveRules(LocalDateTime.now());
    }

    @Override
    public List<SubsidyRuleEntity> getRulesBySubsidyType(Integer subsidyType) {
        log.debug("[补贴规则引擎-优化] 查询类型规则: subsidyType={}", subsidyType);
        return subsidyRuleDao.selectBySubsidyType(subsidyType);
    }

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void enableRule(Long ruleId) {
        log.info("[补贴规则引擎-优化] 启用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(1);
            this.updateById(rule);
            // 清除条件缓存
            clearConditionCache(ruleId);
        }
    }

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void disableRule(Long ruleId) {
        log.info("[补贴规则引擎-优化] 禁用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(0);
            this.updateById(rule);
            // 清除条件缓存
            clearConditionCache(ruleId);
        }
    }

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void adjustPriority(Long ruleId, Integer priority) {
        log.info("[补贴规则引擎-优化] 调整优先级: ruleId={}, priority={}", ruleId, priority);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setPriority(priority);
            this.updateById(rule);
            // 清除条件缓存
            clearConditionCache(ruleId);
        }
    }

    /**
     * 快速时间条件验证
     */
    private boolean validateTimeConditionFast(SubsidyRuleEntity rule, LocalDateTime consumeTime) {
        if (rule.getApplyTimeType() == 1) {
            return true; // 全部
        }

        DayOfWeek dayOfWeek = consumeTime.getDayOfWeek();
        LocalTime time = consumeTime.toLocalTime();

        // 工作日/周末判断
        if (rule.getApplyTimeType() == 2) {
            return dayOfWeek.getValue() >= DayOfWeek.MONDAY.getValue() &&
                    dayOfWeek.getValue() <= DayOfWeek.FRIDAY.getValue();
        } else if (rule.getApplyTimeType() == 3) {
            return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
        }

        return true;
    }

    /**
     * 快速餐别条件验证
     */
    private boolean validateMealTypeConditionFast(SubsidyRuleEntity rule, Integer mealType) {
        if (rule.getApplyMealTypes() == null || rule.getApplyMealTypes().isEmpty()) {
            return true;
        }

        String[] mealTypes = rule.getApplyMealTypes().split(",");
        for (String mealTypeStr : mealTypes) {
            if (Integer.parseInt(mealTypeStr.trim()) == mealType) {
                return true;
            }
        }

        return false;
    }

    /**
     * 详细时间条件验证
     */
    private boolean validateTimeCondition(SubsidyRuleEntity rule,
                                           SubsidyCalculationDTO calculationDTO) {
        return validateTimeConditionFast(rule, calculationDTO.getConsumeTime());
    }

    /**
     * 详细餐别条件验证
     */
    private boolean validateMealTypeCondition(SubsidyRuleEntity rule,
                                              SubsidyCalculationDTO calculationDTO) {
        return validateMealTypeConditionFast(rule, calculationDTO.getMealType());
    }

    /**
     * 验证自定义条件（带缓存）
     */
    private boolean validateCustomConditions(SubsidyRuleEntity rule,
                                             SubsidyCalculationDTO calculationDTO) {
        List<SubsidyRuleConditionEntity> conditions = getRuleConditions(rule.getId());

        for (SubsidyRuleConditionEntity condition : conditions) {
            if (!evaluateCondition(condition, calculationDTO)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取规则条件（带缓存）
     */
    private List<SubsidyRuleConditionEntity> getRuleConditions(Long ruleId) {
        return ruleConditionCache.computeIfAbsent(ruleId, id -> {
            List<SubsidyRuleConditionEntity> conditions =
                    ruleConditionDao.selectActiveByRuleId(id);
            log.debug("[补贴规则引擎-优化] 加载规则条件: ruleId={}, count={}",
                    id, conditions.size());
            return conditions;
        });
    }

    /**
     * 清除条件缓存
     */
    private void clearConditionCache(Long ruleId) {
        ruleConditionCache.remove(ruleId);
        log.debug("[补贴规则引擎-优化] 清除条件缓存: ruleId={}", ruleId);
    }

    /**
     * 按规则计算补贴（优化版）
     */
    private SubsidyResultDTO calculateByRule(SubsidyRuleEntity rule,
                                              SubsidyCalculationDTO calculationDTO) {
        BigDecimal consumeAmount = calculationDTO.getConsumeAmount();
        BigDecimal subsidyAmount;

        try {
            // 边界检查
            if (consumeAmount == null || consumeAmount.compareTo(BigDecimal.ZERO) < 0) {
                return SubsidyResultDTO.error("消费金额无效");
            }

            switch (rule.getRuleType()) {
                case 1: // 固定金额
                    subsidyAmount = calculateFixedAmount(rule);
                    break;

                case 2: // 比例补贴
                    subsidyAmount = calculateRateSubsidy(rule, consumeAmount);
                    break;

                case 3: // 阶梯补贴
                    subsidyAmount = calculateTierSubsidy(rule, consumeAmount);
                    break;

                case 4: // 限时补贴
                    subsidyAmount = calculateTimeLimitedSubsidy(rule, consumeAmount, calculationDTO);
                    break;

                default:
                    return SubsidyResultDTO.error("不支持的规则类型: " + rule.getRuleType());
            }

            // 边界检查：补贴金额不能超过消费金额
            if (subsidyAmount.compareTo(consumeAmount) > 0) {
                subsidyAmount = consumeAmount;
            }

            String detail = String.format("规则: %s, 消费: %.2f, 补贴: %.2f, 类型: %s",
                    rule.getRuleName(), consumeAmount, subsidyAmount,
                    getRuleTypeName(rule.getRuleType()));

            return SubsidyResultDTO.success(
                    rule.getId(),
                    rule.getRuleCode(),
                    rule.getRuleName(),
                    subsidyAmount,
                    detail
            );
        } catch (Exception e) {
            log.error("[补贴规则引擎-优化] 计算补贴异常: ruleCode={}, error={}",
                    rule.getRuleCode(), e.getMessage(), e);
            return SubsidyResultDTO.error("计算失败: " + e.getMessage());
        }
    }

    /**
     * 计算固定金额补贴
     */
    private BigDecimal calculateFixedAmount(SubsidyRuleEntity rule) {
        if (rule.getSubsidyAmount() == null) {
            return BigDecimal.ZERO;
        }
        return rule.getSubsidyAmount();
    }

    /**
     * 计算比例补贴
     */
    private BigDecimal calculateRateSubsidy(SubsidyRuleEntity rule, BigDecimal consumeAmount) {
        if (rule.getSubsidyRate() == null || rule.getSubsidyRate().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal subsidyAmount = consumeAmount.multiply(rule.getSubsidyRate())
                .setScale(2, RoundingMode.HALF_UP);

        // 应用最高限额
        if (rule.getMaxSubsidyAmount() != null && rule.getMaxSubsidyAmount().compareTo(BigDecimal.ZERO) > 0) {
            subsidyAmount = subsidyAmount.min(rule.getMaxSubsidyAmount());
        }

        return subsidyAmount;
    }

    /**
     * 计算阶梯补贴
     */
    private BigDecimal calculateTierSubsidy(SubsidyRuleEntity rule, BigDecimal consumeAmount) {
        if (rule.getTierConfig() == null || rule.getTierConfig().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<TierConfig> tiers = mapper.readValue(rule.getTierConfig(),
                    new TypeReference<List<TierConfig>>() {});

            for (TierConfig tier : tiers) {
                if (consumeAmount.compareTo(tier.getMinAmount()) >= 0) {
                    BigDecimal subsidyAmount = consumeAmount.multiply(tier.getRate())
                            .setScale(2, RoundingMode.HALF_UP);

                    // 应用最高限额
                    if (rule.getMaxSubsidyAmount() != null) {
                        subsidyAmount = subsidyAmount.min(rule.getMaxSubsidyAmount());
                    }

                    return subsidyAmount;
                }
            }
        } catch (Exception e) {
            log.error("[补贴规则引擎-优化] 解析阶梯配置失败: error={}", e.getMessage());
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算限时补贴
     */
    private BigDecimal calculateTimeLimitedSubsidy(SubsidyRuleEntity rule,
                                                   BigDecimal consumeAmount,
                                                   SubsidyCalculationDTO calculationDTO) {
        if (rule.getSubsidyAmount() == null) {
            return BigDecimal.ZERO;
        }

        // 验证时间范围
        if (rule.getApplyStartTime() != null && rule.getApplyEndTime() != null) {
            LocalTime time = calculationDTO.getConsumeTime().toLocalTime();
            if (time.isBefore(rule.getApplyStartTime()) || time.isAfter(rule.getApplyEndTime())) {
                return BigDecimal.ZERO;
            }
        }

        return rule.getSubsidyAmount();
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
            log.error("[补贴规则引擎-优化] 记录日志失败: error={}", e.getMessage(), e);
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
            log.error("[补贴规则引擎-优化] 保存补贴记录失败: error={}", e.getMessage(), e);
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

    private String getRuleTypeName(Integer ruleType) {
        String[] names = {"", "固定金额", "比例补贴", "阶梯补贴", "限时补贴"};
        return ruleType != null && ruleType > 0 && ruleType < names.length
                ? names[ruleType] : "未知";
    }
}
