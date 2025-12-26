package net.lab1024.sa.consume.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.dao.SubsidyRuleConditionDao;
import net.lab1024.sa.consume.dao.SubsidyRuleDao;
import net.lab1024.sa.consume.dao.SubsidyRuleLogDao;
import net.lab1024.sa.consume.dao.UserSubsidyRecordDao;
import net.lab1024.sa.consume.domain.dto.SubsidyCalculationDTO;
import net.lab1024.sa.consume.domain.dto.SubsidyResultDTO;
import net.lab1024.sa.common.entity.consume.SubsidyRuleConditionEntity;
import net.lab1024.sa.common.entity.consume.SubsidyRuleEntity;
import net.lab1024.sa.common.entity.consume.SubsidyRuleLogEntity;
import net.lab1024.sa.common.entity.consume.UserSubsidyRecordEntity;
import net.lab1024.sa.consume.service.SubsidyRuleEngineService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 补贴规则引擎高级优化实现（批量预加载+规则编译）
 *
 * 优化策略：
 * 1. 批量预加载：启动时预加载所有有效规则到内存
 * 2. 规则编译：将规则条件编译为Predicate函数对象
 * 3. 索引优化：按补贴类型建立规则索引
 * 4. 并行匹配：使用并行流加速规则匹配
 * 5. 定时刷新：每5分钟自动刷新规则缓存
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Service("subsidyRuleEngineAdvancedOptimizedService")
@Slf4j
public class SubsidyRuleEngineAdvancedOptimizedServiceImpl extends ServiceImpl<SubsidyRuleDao, SubsidyRuleEntity>
        implements SubsidyRuleEngineService {

    @Resource
    private SubsidyRuleDao subsidyRuleDao;

    @Resource
    private SubsidyRuleConditionDao ruleConditionDao;

    @Resource
    private SubsidyRuleLogDao ruleLogDao;

    @Resource
    private UserSubsidyRecordDao subsidyRecordDao;

    // ===== 内存缓存结构 =====

    /**
     * 所有有效规则（按ID索引）
     */
    private final Map<Long, CompiledRule> compiledRules = new ConcurrentHashMap<>();

    /**
     * 按补贴类型分组的规则（按优先级排序）
     */
    private final Map<Integer, List<CompiledRule>> rulesBySubsidyType = new ConcurrentHashMap<>();

    /**
     * 规则条件缓存
     */
    private final Map<Long, List<SubsidyRuleConditionEntity>> ruleConditions = new ConcurrentHashMap<>();

    /**
     * 最后刷新时间
     */
    private volatile long lastRefreshTime = 0;

    /**
     * 刷新间隔（毫秒）
     */
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5分钟

    // ===== 生命周期管理 =====

    /**
     * 启动时预加载所有规则
     */
    @PostConstruct
    public void init() {
        log.info("[补贴规则引擎-高级优化] 初始化规则引擎，开始预加载规则...");
        long startTime = System.currentTimeMillis();
        refreshAllRules();
        long duration = System.currentTimeMillis() - startTime;
        log.info("[补贴规则引擎-高级优化] 规则预加载完成: 规则数={}, 耗时={}ms",
                compiledRules.size(), duration);
    }

    /**
     * 定时刷新规则缓存（每5分钟）
     */
    @Scheduled(fixedRate = REFRESH_INTERVAL_MS)
    public void scheduledRefresh() {
        log.debug("[补贴规则引擎-高级优化] 定时刷新规则缓存...");
        refreshAllRules();
    }

    /**
     * 刷新所有规则
     */
    @CacheEvict(value = {"effectiveRules", "subsidyCalculation"}, allEntries = true)
    public synchronized void refreshAllRules() {
        try {
            // 1. 查询所有有效规则
            List<SubsidyRuleEntity> allRules = subsidyRuleDao.selectEffectiveRules(LocalDateTime.now());

            // 2. 查询所有规则条件
            List<Long> ruleIds = allRules.stream()
                    .map(SubsidyRuleEntity::getId)
                    .collect(Collectors.toList());

            Map<Long, List<SubsidyRuleConditionEntity>> conditionsMap = new HashMap<>();
            if (!ruleIds.isEmpty()) {
                List<SubsidyRuleConditionEntity> allConditions =
                        ruleConditionDao.selectActiveByRuleIds(ruleIds);
                conditionsMap = allConditions.stream()
                        .collect(Collectors.groupingBy(SubsidyRuleConditionEntity::getRuleId));
            }

            // 3. 编译规则
            Map<Long, CompiledRule> newCompiledRules = new HashMap<>();
            Map<Integer, List<CompiledRule>> newRulesBySubsidyType = new HashMap<>();

            for (SubsidyRuleEntity rule : allRules) {
                List<SubsidyRuleConditionEntity> conditions =
                        conditionsMap.getOrDefault(rule.getId(), new ArrayList<>());

                CompiledRule compiledRule = compileRule(rule, conditions);
                newCompiledRules.put(rule.getId(), compiledRule);

                // 按补贴类型分组
                newRulesBySubsidyType
                        .computeIfAbsent(rule.getSubsidyType(), k -> new ArrayList<>())
                        .add(compiledRule);
            }

            // 4. 按优先级排序
            newRulesBySubsidyType.forEach((type, rules) ->
                    rules.sort(Comparator.comparingInt(CompiledRule::getPriority).reversed())
            );

            // 5. 原子替换缓存
            compiledRules.clear();
            compiledRules.putAll(newCompiledRules);

            rulesBySubsidyType.clear();
            rulesBySubsidyType.putAll(newRulesBySubsidyType);

            ruleConditions.clear();
            ruleConditions.putAll(conditionsMap);

            lastRefreshTime = System.currentTimeMillis();

            log.debug("[补贴规则引擎-高级优化] 规则刷新完成: 规则数={}, 条件数={}",
                    newCompiledRules.size(), conditionsMap.size());
        } catch (Exception e) {
            log.error("[补贴规则引擎-高级优化] 刷新规则缓存失败: error={}", e.getMessage(), e);
        }
    }

    // ===== 核心计算方法 =====

    @Override
    @Cacheable(value = "subsidyCalculation", key = "#calculationDTO.userId + '-' + #calculationDTO.consumeAmount")
    public SubsidyResultDTO calculateSubsidy(SubsidyCalculationDTO calculationDTO) {
        long startTime = System.currentTimeMillis();
        log.debug("[补贴规则引擎-高级优化] 计算补贴: userId={}, amount={}",
                calculationDTO.getUserId(), calculationDTO.getConsumeAmount());

        // 1. 快速匹配规则（使用预编译规则）
        List<CompiledRule> matchedRules = matchRulesFast(calculationDTO);

        if (matchedRules.isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("[补贴规则引擎-高级优化] 未匹配到规则: 耗时={}ms", duration);
            return SubsidyResultDTO.noMatch();
        }

        // 2. 取优先级最高的规则
        CompiledRule compiledRule = matchedRules.get(0);
        log.debug("[补贴规则引擎-高级优化] 匹配到规则: ruleCode={}, priority={}",
                compiledRule.getRuleCode(), compiledRule.getPriority());

        // 3. 计算补贴
        SubsidyResultDTO result = calculateByCompiledRule(compiledRule, calculationDTO);

        long duration = System.currentTimeMillis() - startTime;
        log.debug("[补贴规则引擎-高级优化] 计算完成: 耗时={}ms", duration);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "subsidyCalculation", allEntries = true)
    public SubsidyResultDTO executeRule(SubsidyCalculationDTO calculationDTO) {
        log.info("[补贴规则引擎-高级优化] 执行规则: userId={}, amount={}",
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

    // ===== 规则匹配（优化版）=====

    @Override
    public List<SubsidyRuleEntity> matchRules(SubsidyCalculationDTO calculationDTO) {
        log.debug("[补贴规则引擎-高级优化] 匹配规则: subsidyType={}", calculationDTO.getSubsidyType());

        // 1. 从内存获取候选规则（已按优先级排序）
        List<CompiledRule> candidates = rulesBySubsidyType.getOrDefault(
                calculationDTO.getSubsidyType(), new ArrayList<>()
        );

        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 使用并行流快速验证（适用于大量规则场景）
        List<CompiledRule> matchedRules = candidates.parallelStream()
                .filter(compiledRule -> compiledRule.test(calculationDTO))
                .collect(Collectors.toList());

        // 3. 转换为实体对象
        List<SubsidyRuleEntity> result = matchedRules.stream()
                .map(CompiledRule::getRule)
                .collect(Collectors.toList());

        log.debug("[补贴规则引擎-高级优化] 匹配结果: 候选数={}, 匹配数={}",
                candidates.size(), result.size());

        return result;
    }

    /**
     * 快速匹配（返回编译规则）
     */
    private List<CompiledRule> matchRulesFast(SubsidyCalculationDTO calculationDTO) {
        // 从内存获取候选规则（已按优先级排序）
        List<CompiledRule> candidates = rulesBySubsidyType.getOrDefault(
                calculationDTO.getSubsidyType(), new ArrayList<>()
        );

        if (candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 快速验证（使用预编译谓词）
        return candidates.stream()
                .filter(compiledRule -> compiledRule.test(calculationDTO))
                .collect(Collectors.toList());
    }

    // ===== 规则验证 =====

    @Override
    public boolean validateRule(SubsidyRuleEntity rule, SubsidyCalculationDTO calculationDTO) {
        CompiledRule compiledRule = compiledRules.get(rule.getId());
        if (compiledRule == null) {
            log.warn("[补贴规则引擎-高级优化] 规则未编译: ruleId={}", rule.getId());
            return false;
        }
        return compiledRule.test(calculationDTO);
    }

    // ===== 规则查询 =====

    @Override
    @Cacheable(value = "effectiveRules", key = "'all'")
    public List<SubsidyRuleEntity> getEffectiveRules() {
        log.debug("[补贴规则引擎-高级优化] 查询有效规则（缓存）");
        return compiledRules.values().stream()
                .map(CompiledRule::getRule)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubsidyRuleEntity> getRulesBySubsidyType(Integer subsidyType) {
        log.debug("[补贴规则引擎-高级优化] 查询类型规则: subsidyType={}", subsidyType);
        return rulesBySubsidyType.getOrDefault(subsidyType, new ArrayList<>()).stream()
                .map(CompiledRule::getRule)
                .collect(Collectors.toList());
    }

    // ===== 规则管理 =====

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void enableRule(Long ruleId) {
        log.info("[补贴规则引擎-高级优化] 启用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(1);
            this.updateById(rule);
            // 立即刷新缓存
            refreshAllRules();
        }
    }

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void disableRule(Long ruleId) {
        log.info("[补贴规则引擎-高级优化] 禁用规则: ruleId={}", ruleId);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setStatus(0);
            this.updateById(rule);
            // 立即刷新缓存
            refreshAllRules();
        }
    }

    @Override
    @CacheEvict(value = "effectiveRules", allEntries = true)
    public void adjustPriority(Long ruleId, Integer priority) {
        log.info("[补贴规则引擎-高级优化] 调整优先级: ruleId={}, priority={}", ruleId, priority);
        SubsidyRuleEntity rule = this.getById(ruleId);
        if (rule != null) {
            rule.setPriority(priority);
            this.updateById(rule);
            // 立即刷新缓存
            refreshAllRules();
        }
    }

    // ===== 核心计算方法 =====

    /**
     * 按编译规则计算补贴
     */
    private SubsidyResultDTO calculateByCompiledRule(CompiledRule compiledRule,
                                                     SubsidyCalculationDTO calculationDTO) {
        SubsidyRuleEntity rule = compiledRule.getRule();
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
            log.error("[补贴规则引擎-高级优化] 计算补贴异常: ruleCode={}, error={}",
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
            log.error("[补贴规则引擎-高级优化] 解析阶梯配置失败: error={}", e.getMessage());
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

    // ===== 规则编译 =====

    /**
     * 编译规则为可执行的Predicate
     */
    private CompiledRule compileRule(SubsidyRuleEntity rule,
                                    List<SubsidyRuleConditionEntity> conditions) {
        // 构建验证谓词链
        Predicate<SubsidyCalculationDTO> predicate = dto -> true;

        // 1. 基本状态检查
        predicate = predicate.and(dto -> rule.getStatus() == 1);

        // 2. 有效期检查
        LocalDateTime now = LocalDateTime.now();
        predicate = predicate.and(dto -> {
            if (now.isBefore(rule.getEffectiveDate())) {
                return false;
            }
            if (rule.getExpireDate() != null && now.isAfter(rule.getExpireDate())) {
                return false;
            }
            return true;
        });

        // 3. 时间条件编译
        Predicate<SubsidyCalculationDTO> timePredicate = compileTimePredicate(rule);
        predicate = predicate.and(timePredicate);

        // 4. 餐别条件编译
        Predicate<SubsidyCalculationDTO> mealTypePredicate = compileMealTypePredicate(rule);
        predicate = predicate.and(mealTypePredicate);

        // 5. 自定义条件编译
        for (SubsidyRuleConditionEntity condition : conditions) {
            Predicate<SubsidyCalculationDTO> conditionPredicate =
                    compileConditionPredicate(condition);
            predicate = predicate.and(conditionPredicate);
        }

        return new CompiledRule(rule, predicate);
    }

    /**
     * 编译时间条件谓词
     */
    private Predicate<SubsidyCalculationDTO> compileTimePredicate(SubsidyRuleEntity rule) {
        return dto -> {
            LocalDateTime consumeTime = dto.getConsumeTime();
            DayOfWeek dayOfWeek = consumeTime.getDayOfWeek();
            LocalTime time = consumeTime.toLocalTime();

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
        };
    }

    /**
     * 编译餐别条件谓词
     */
    private Predicate<SubsidyCalculationDTO> compileMealTypePredicate(SubsidyRuleEntity rule) {
        if (rule.getApplyMealTypes() == null || rule.getApplyMealTypes().isEmpty()) {
            return dto -> true;
        }

        String[] mealTypes = rule.getApplyMealTypes().split(",");
        Set<Integer> allowedTypes = Arrays.stream(mealTypes)
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        return dto -> allowedTypes.contains(dto.getMealType());
    }

    /**
     * 编译自定义条件谓词
     */
    private Predicate<SubsidyCalculationDTO> compileConditionPredicate(SubsidyRuleConditionEntity condition) {
        // TODO: 实现复杂条件编译逻辑
        return dto -> true;
    }

    // ===== 日志和记录 =====

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
            log.error("[补贴规则引擎-高级优化] 记录日志失败: error={}", e.getMessage(), e);
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
            log.error("[补贴规则引擎-高级优化] 保存补贴记录失败: error={}", e.getMessage(), e);
        }
    }

    // ===== 内部类 =====

    /**
     * 编译后的规则（包含验证谓词）
     */
    @lombok.Data
    private static class CompiledRule {
        private final SubsidyRuleEntity rule;
        private final Predicate<SubsidyCalculationDTO> validationPredicate;

        public CompiledRule(SubsidyRuleEntity rule,
                           Predicate<SubsidyCalculationDTO> validationPredicate) {
            this.rule = rule;
            this.validationPredicate = validationPredicate;
        }

        public Integer getPriority() {
            return rule.getPriority();
        }

        public String getRuleCode() {
            return rule.getRuleCode();
        }

        public boolean test(SubsidyCalculationDTO dto) {
            return validationPredicate.test(dto);
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
