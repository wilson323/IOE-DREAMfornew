/*
 * 消费限额配置服务实现
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ConsumeLimitConfigDao;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeLimitConfigEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.domain.vo.*;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeLimitConfigService;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartIpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消费限额配置服务实现
 * 提供动态消费限额配置和管理功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Service
@Slf4j
public class ConsumeLimitConfigServiceImpl implements ConsumeLimitConfigService {

    @Resource
    private ConsumeLimitConfigDao consumeLimitConfigDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountService accountService;

    @Value("${consume.limit.cache-minutes:30}")
    private Integer cacheMinutes;

    @Value("${consume.limit.enable-temporary:true}")
    private Boolean enableTemporaryLimit;

    private static final String LIMIT_CONFIG_PREFIX = "consume_limit_config:";
    private static final String TEMPORARY_LIMIT_PREFIX = "temporary_limit:";
    private static final String STATISTICS_PREFIX = "consume_statistics:";
    private static final String USAGE_REPORT_PREFIX = "usage_report:";

    @Override
    public ConsumeLimitConfig getUserLimitConfig(@NotNull Long personId) {
        try {
            // 1. 尝试从缓存获取
            String cacheKey = LIMIT_CONFIG_PREFIX + "user:" + personId;
            ConsumeLimitConfig cachedConfig = RedisUtil.get(cacheKey, ConsumeLimitConfig.class);
            if (cachedConfig != null) {
                return cachedConfig;
            }

            // 2. 检查临时限额
            ConsumeLimitConfig tempConfig = getTemporaryUserLimit(personId);
            if (tempConfig != null) {
                // 缓存临时配置（较短时间）
                RedisUtil.set(cacheKey, tempConfig, 5, TimeUnit.MINUTES);
                return tempConfig;
            }

            // 3. 从数据库获取用户限额配置
            List<ConsumeLimitConfigEntity> entities = consumeLimitConfigDao.selectByTargetAndType(
                personId.toString(), "USER");

            if (entities.isEmpty()) {
                // 使用默认配置
                ConsumeLimitConfig defaultConfig = ConsumeLimitConfig.createDefault();
                defaultConfig.setConfigType("USER");
                defaultConfig.setConfigName("默认用户限额");

                // 缓存默认配置
                RedisUtil.set(cacheKey, defaultConfig, cacheMinutes, TimeUnit.MINUTES);
                return defaultConfig;
            }

            // 4. 合并多个配置（按优先级）
            ConsumeLimitConfig mergedConfig = mergeLimitConfigs(entities);
            mergedConfig.setConfigType("USER");
            mergedConfig.setConfigName("用户限额配置");

            // 缓存配置
            RedisUtil.set(cacheKey, mergedConfig, cacheMinutes, TimeUnit.MINUTES);

            return mergedConfig;

        } catch (Exception e) {
            log.error("获取用户限额配置异常: personId={}, error={}", personId, e.getMessage(), e);
            // 返回默认配置
            return ConsumeLimitConfig.createDefault();
        }
    }

    @Override
    public ConsumeLimitConfig getDeviceLimitConfig(@NotNull Long deviceId) {
        try {
            String cacheKey = LIMIT_CONFIG_PREFIX + "device:" + deviceId;
            ConsumeLimitConfig cachedConfig = RedisUtil.get(cacheKey, ConsumeLimitConfig.class);
            if (cachedConfig != null) {
                return cachedConfig;
            }

            List<ConsumeLimitConfigEntity> entities = consumeLimitConfigDao.selectByTargetAndType(
                deviceId.toString(), "DEVICE");

            if (entities.isEmpty()) {
                return null;
            }

            ConsumeLimitConfig config = mergeLimitConfigs(entities);
            config.setConfigType("DEVICE");
            config.setConfigName("设备限额配置");

            RedisUtil.set(cacheKey, config, cacheMinutes, TimeUnit.MINUTES);
            return config;

        } catch (Exception e) {
            log.error("获取设备限额配置异常: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ConsumeLimitConfig getRegionLimitConfig(@NotNull String regionId) {
        try {
            String cacheKey = LIMIT_CONFIG_PREFIX + "region:" + regionId;
            ConsumeLimitConfig cachedConfig = RedisUtil.get(cacheKey, ConsumeLimitConfig.class);
            if (cachedConfig != null) {
                return cachedConfig;
            }

            List<ConsumeLimitConfigEntity> entities = consumeLimitConfigDao.selectByTargetAndType(
                regionId, "REGION");

            if (entities.isEmpty()) {
                return null;
            }

            ConsumeLimitConfig config = mergeLimitConfigs(entities);
            config.setConfigType("REGION");
            config.setConfigName("区域限额配置");

            RedisUtil.set(cacheKey, config, cacheMinutes, TimeUnit.MINUTES);
            return config;

        } catch (Exception e) {
            log.error("获取区域限额配置异常: regionId={}, error={}", regionId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public ConsumeLimitConfig getModeLimitConfig(@NotNull String consumptionMode) {
        try {
            String cacheKey = LIMIT_CONFIG_PREFIX + "mode:" + consumptionMode;
            ConsumeLimitConfig cachedConfig = RedisUtil.get(cacheKey, ConsumeLimitConfig.class);
            if (cachedConfig != null) {
                return cachedConfig;
            }

            List<ConsumeLimitConfigEntity> entities = consumeLimitConfigDao.selectByTargetAndType(
                consumptionMode, "MODE");

            if (entities.isEmpty()) {
                return null;
            }

            ConsumeLimitConfig config = mergeLimitConfigs(entities);
            config.setConfigType("MODE");
            config.setConfigName("消费模式限额配置");

            RedisUtil.set(cacheKey, config, cacheMinutes, TimeUnit.MINUTES);
            return config;

        } catch (Exception e) {
            log.error("获取消费模式限额配置异常: mode={}, error={}", consumptionMode, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setUserLimitConfig(@NotNull Long personId, @NotNull ConsumeLimitConfig limitConfig, String reason) {
        try {
            // 1. 参数验证
            validateLimitConfig(limitConfig);

            // 2. 清除缓存
            clearUserLimitCache(personId);

            // 3. 保存配置到数据库
            ConsumeLimitConfigEntity entity = convertToEntity(limitConfig);
            entity.setTargetId(personId.toString());
            entity.setTargetType("USER");
            entity.setConfigName("用户限额配置");
            entity.setReason(reason);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            // 4. 检查是否已存在配置
            ConsumeLimitConfigEntity existingEntity = consumeLimitConfigDao.selectByTargetAndTypeAndPriority(
                personId.toString(), "USER", limitConfig.getPriority());

            int result;
            if (existingEntity != null) {
                // 更新现有配置
                entity.setConfigId(existingEntity.getConfigId());
                entity.setUpdateTime(LocalDateTime.now());
                result = consumeLimitConfigDao.updateById(entity);
            } else {
                // 插入新配置
                result = consumeLimitConfigDao.insert(entity);
            }

            // 5. 记录变更历史
            recordLimitChange(personId, "USER", limitConfig, reason);

            log.info("用户限额配置设置成功: personId={}, priority={}", personId, limitConfig.getPriority());
            return result > 0;

        } catch (Exception e) {
            log.error("设置用户限额配置异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public LimitValidationResult validateConsumeLimit(@NotNull Long personId, @NotNull BigDecimal amount,
                                                     Long deviceId, String regionId, String consumptionMode) {
        try {
            log.debug("开始验证消费限额: personId={}, amount={}, deviceId={}, regionId={}, mode={}",
                personId, amount, deviceId, regionId, consumptionMode);

            // 1. 获取相关限额配置
            ConsumeLimitConfig userConfig = getUserLimitConfig(personId);
            ConsumeLimitConfig deviceConfig = deviceId != null ? getDeviceLimitConfig(deviceId) : null;
            ConsumeLimitConfig regionConfig = StringUtils.hasText(regionId) ? getRegionLimitConfig(regionId) : null;
            ConsumeLimitConfig modeConfig = StringUtils.hasText(consumptionMode) ? getModeLimitConfig(consumptionMode) : null;

            // 2. 获取当前消费统计
            ConsumeStatistics statistics = getUserConsumeStatistics(personId);

            // 3. 获取有效单次限额
            BigDecimal effectiveSingleLimit = getEffectiveSingleLimit(userConfig, deviceConfig, regionConfig, modeConfig, consumptionMode, deviceId, regionId);

            // 4. 验证单次限额
            if (effectiveSingleLimit != null && amount.compareTo(effectiveSingleLimit) > 0) {
                return LimitValidationResult.singleLimitExceeded(effectiveSingleLimit, amount);
            }

            // 5. 验证日度限额
            LimitValidationResult dailyResult = validateDailyLimit(userConfig, deviceConfig, regionConfig, statistics, amount);
            if (!dailyResult.isPassed()) {
                return dailyResult;
            }

            // 6. 验证周度限额
            LimitValidationResult weeklyResult = validateWeeklyLimit(userConfig, deviceConfig, regionConfig, statistics, amount);
            if (!weeklyResult.isPassed()) {
                return weeklyResult;
            }

            // 7. 验证月度限额
            LimitValidationResult monthlyResult = validateMonthlyLimit(userConfig, deviceConfig, regionConfig, statistics, amount);
            if (!monthlyResult.isPassed()) {
                return monthlyResult;
            }

            // 8. 验证次数限制
            LimitValidationResult countResult = validateCountLimit(userConfig, deviceConfig, regionConfig, statistics);
            if (!countResult.isPassed()) {
                return countResult;
            }

            // 9. 验证时间段限制
            LimitValidationResult timeResult = validateTimeSlotLimit(userConfig, consumptionMode);
            if (!timeResult.isPassed()) {
                return timeResult;
            }

            // 10. 验证通过，返回剩余额度信息
            BigDecimal remainingDaily = calculateRemainingDaily(userConfig, deviceConfig, regionConfig, statistics);
            return LimitValidationResult.success(remainingDaily, null);

        } catch (Exception e) {
            log.error("验证消费限额异常: personId={}, amount={}, error={}", personId, amount, e.getMessage(), e);
            return LimitValidationResult.failure("SYSTEM_ERROR", "系统异常，限额验证失败");
        }
    }

    @Override
    public ConsumeStatistics getUserConsumeStatistics(@NotNull Long personId) {
        try {
            String cacheKey = STATISTICS_PREFIX + personId + ":" + LocalDateTime.now().toLocalDate();
            ConsumeStatistics cachedStats = RedisUtil.get(cacheKey, ConsumeStatistics.class);
            if (cachedStats != null) {
                return cachedStats;
            }

            // 获取今日消费统计
            LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime todayEnd = LocalDateTime.now().with(LocalTime.MAX);

            List<ConsumeRecordEntity> todayRecords = consumeRecordDao.selectByPersonIdAndTimeRange(
                personId, todayStart, todayEnd);

            BigDecimal dailyAmount = todayRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .map(ConsumeRecordEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            int dailyCount = (int) todayRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .count();

            // 获取本周消费统计
            LocalDateTime weekStart = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1)
                .with(LocalTime.MIN);
            List<ConsumeRecordEntity> weekRecords = consumeRecordDao.selectByPersonIdAndTimeRange(
                personId, weekStart, todayEnd);

            BigDecimal weeklyAmount = weekRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .map(ConsumeRecordEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            int weeklyCount = (int) weekRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .count();

            // 获取本月消费统计
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).with(LocalTime.MIN);
            List<ConsumeRecordEntity> monthRecords = consumeRecordDao.selectByPersonIdAndTimeRange(
                personId, monthStart, todayEnd);

            BigDecimal monthlyAmount = monthRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .map(ConsumeRecordEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            int monthlyCount = (int) monthRecords.stream()
                .filter(record -> "SUCCESS".equals(record.getStatus()))
                .count();

            ConsumeStatistics statistics = ConsumeStatistics.builder()
                .personId(personId)
                .dailyAmount(dailyAmount)
                .dailyCount(dailyCount)
                .weeklyAmount(weeklyAmount)
                .weeklyCount(weeklyCount)
                .monthlyAmount(monthlyAmount)
                .monthlyCount(monthlyCount)
                .lastUpdateTime(LocalDateTime.now())
                .build();

            // 缓存统计信息（5分钟）
            RedisUtil.set(cacheKey, statistics, 5, TimeUnit.MINUTES);

            return statistics;

        } catch (Exception e) {
            log.error("获取用户消费统计异常: personId={}, error={}", personId, e.getMessage(), e);
            return ConsumeStatistics.builder()
                .personId(personId)
                .dailyAmount(BigDecimal.ZERO)
                .dailyCount(0)
                .weeklyAmount(BigDecimal.ZERO)
                .weeklyCount(0)
                .monthlyAmount(BigDecimal.ZERO)
                .monthlyCount(0)
                .lastUpdateTime(LocalDateTime.now())
                .build();
        }
    }

    @Override
    public LimitCheckResult checkUserLimit(@NotNull Long personId, @NotNull BigDecimal amount) {
        try {
            ConsumeLimitConfig userConfig = getUserLimitConfig(personId);
            ConsumeStatistics statistics = getUserConsumeStatistics(personId);

            // 检查单次限额
            BigDecimal singleLimit = userConfig.getSingleLimit();
            if (singleLimit != null && amount.compareTo(singleLimit) > 0) {
                return LimitCheckResult.builder()
                    .passed(false)
                    .limitType("SINGLE")
                    .limitValue(singleLimit)
                    .currentValue(BigDecimal.ZERO)
                    .attemptValue(amount)
                    .exceededAmount(amount.subtract(singleLimit))
                    .message("超出单次消费限额")
                    .build();
            }

            // 检查日度限额
            BigDecimal dailyLimit = userConfig.getDailyLimit();
            if (dailyLimit != null && statistics.getDailyAmount().add(amount).compareTo(dailyLimit) > 0) {
                return LimitCheckResult.builder()
                    .passed(false)
                    .limitType("DAILY")
                    .limitValue(dailyLimit)
                    .currentValue(statistics.getDailyAmount())
                    .attemptValue(amount)
                    .exceededAmount(statistics.getDailyAmount().add(amount).subtract(dailyLimit))
                    .message("超出日度消费限额")
                    .build();
            }

            // 检查次数限制
            Integer dailyCountLimit = userConfig.getDailyCountLimit();
            if (dailyCountLimit != null && statistics.getDailyCount() >= dailyCountLimit) {
                return LimitCheckResult.builder()
                    .passed(false)
                    .limitType("DAILY_COUNT")
                    .limitValue(new BigDecimal(dailyCountLimit.toString()))
                    .currentValue(new BigDecimal(statistics.getDailyCount()))
                    .attemptValue(BigDecimal.ONE)
                    .exceededAmount(BigDecimal.ONE)
                    .message("超出日度消费次数限制")
                    .build();
            }

            // 验证通过
            return LimitCheckResult.builder()
                .passed(true)
                .message("限额检查通过")
                .build();

        } catch (Exception e) {
            log.error("检查用户限额异常: personId={}, amount={}, error={}", personId, amount, e.getMessage(), e);
            return LimitCheckResult.builder()
                .passed(false)
                .message("系统异常，限额检查失败")
                .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean adjustUserLimit(@NotNull Long personId, @NotNull String adjustType, Double adjustRatio, String reason) {
        try {
            if (adjustRatio == null || adjustRatio <= 0) {
                log.warn("调整比例无效: personId={}, adjustRatio={}", personId, adjustRatio);
                return false;
            }

            ConsumeLimitConfig currentConfig = getUserLimitConfig(personId);
            ConsumeLimitConfig adjustedConfig = createAdjustedConfig(currentConfig, adjustType, adjustRatio);

            return setUserLimitConfig(personId, adjustedConfig, reason);

        } catch (Exception e) {
            log.error("调整用户限额异常: personId={}, adjustType={}, adjustRatio={}, error={}",
                personId, adjustType, adjustRatio, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setTemporaryLimit(@NotNull Long personId, @NotNull ConsumeLimitConfig temporaryLimits,
                                    Integer expireMinutes, String reason) {
        try {
            if (!Boolean.TRUE.equals(enableTemporaryLimit)) {
                log.warn("临时限额功能未启用: personId={}", personId);
                return false;
            }

            // 清除现有缓存
            clearUserLimitCache(personId);

            // 设置临时限额到Redis
            String tempKey = TEMPORARY_LIMIT_PREFIX + personId;
            int duration = expireMinutes != null && expireMinutes > 0 ? expireMinutes : 60;

            temporaryLimits.setIsTemporary(true);
            temporaryLimits.setTemporaryDuration(duration);
            temporaryLimits.setExpireTime(LocalDateTime.now().plusMinutes(duration));

            RedisUtil.set(tempKey, temporaryLimits, duration, TimeUnit.MINUTES);

            // 记录临时限额设置
            recordTemporaryLimitSet(personId, temporaryLimits, duration, reason);

            log.info("临时限额设置成功: personId={}, duration={}分钟", personId, duration);
            return true;

        } catch (Exception e) {
            log.error("设置临时限额异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<LimitChangeHistory> getLimitChangeHistory(@NotNull Long personId, Integer limit) {
        try {
            // TODO: 实现从数据库获取限额变更历史
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("获取限额变更历史异常: personId={}, error={}", personId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public BatchLimitSetResult batchSetLimits(@NotNull List<BatchLimitSetting> limitSettings, String reason) {
        try {
            BatchLimitSetResult result = BatchLimitSetResult.builder()
                .totalSettings(limitSettings.size())
                .successCount(0)
                .failureCount(0)
                .failureDetails(new ArrayList<>())
                .build();

            for (BatchLimitSetting setting : limitSettings) {
                try {
                    boolean success = setUserLimitConfig(setting.getPersonId(), setting.getLimitConfig(), reason);
                    if (success) {
                        result.setSuccessCount(result.getSuccessCount() + 1);
                    } else {
                        result.setFailureCount(result.getFailureCount() + 1);
                        result.getFailureDetails().add("人员ID " + setting.getPersonId() + ": 设置失败");
                    }
                } catch (Exception e) {
                    result.setFailureCount(result.getFailureCount() + 1);
                    result.getFailureDetails().add("人员ID " + setting.getPersonId() + ": " + e.getMessage());
                }
            }

            log.info("批量设置限额完成: total={}, success={}, failure={}",
                result.getTotalSettings(), result.getSuccessCount(), result.getFailureCount());

            return result;

        } catch (Exception e) {
            log.error("批量设置限额异常: error={}", e.getMessage(), e);
            return BatchLimitSetResult.builder()
                .totalSettings(limitSettings.size())
                .successCount(0)
                .failureCount(limitSettings.size())
                .failureDetails(List.of("系统异常: " + e.getMessage()))
                .build();
        }
    }

    @Override
    public GlobalLimitConfig getGlobalLimitConfig() {
        try {
            // TODO: 实现获取全局限额配置
            return GlobalLimitConfig.createDefault();

        } catch (Exception e) {
            log.error("获取全局限额配置异常: error={}", e.getMessage(), e);
            return GlobalLimitConfig.createDefault();
        }
    }

    @Override
    public boolean setGlobalLimitConfig(@NotNull GlobalLimitConfig globalConfig) {
        try {
            // TODO: 实现设置全局限额配置
            return true;

        } catch (Exception e) {
            log.error("设置全局限额配置异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean recalculateUserLimit(@NotNull Long personId) {
        try {
            // TODO: 实现基于规则引擎重新计算用户限额
            clearUserLimitCache(personId);
            return true;

        } catch (Exception e) {
            log.error("重新计算用户限额异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public LimitUsageReport getLimitUsageReport(@NotNull Long personId) {
        try {
            ConsumeLimitConfig config = getUserLimitConfig(personId);
            ConsumeStatistics statistics = getUserConsumeStatistics(personId);

            return LimitUsageReport.builder()
                .personId(personId)
                .singleLimit(config.getSingleLimit())
                .dailyLimit(config.getDailyLimit())
                .weeklyLimit(config.getWeeklyLimit())
                .monthlyLimit(config.getMonthlyLimit())
                .dailyUsed(statistics.getDailyAmount())
                .weeklyUsed(statistics.getWeeklyAmount())
                .monthlyUsed(statistics.getMonthlyAmount())
                .dailyUsageRate(calculateUsageRate(statistics.getDailyAmount(), config.getDailyLimit()))
                .weeklyUsageRate(calculateUsageRate(statistics.getWeeklyAmount(), config.getWeeklyLimit()))
                .monthlyUsageRate(calculateUsageRate(statistics.getMonthlyAmount(), config.getMonthlyLimit()))
                .dailyCount(statistics.getDailyCount())
                .weeklyCount(statistics.getWeeklyCount())
                .monthlyCount(statistics.getMonthlyCount())
                .reportTime(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("获取限额使用率报告异常: personId={}, error={}", personId, e.getMessage(), e);
            return LimitUsageReport.builder().personId(personId).build();
        }
    }

    @Override
    public LimitConflictCheckResult checkLimitConflict(@NotNull Long personId, @NotNull ConsumeLimitConfig limitConfig) {
        try {
            ConsumeLimitConfig currentConfig = getUserLimitConfig(personId);

            List<String> conflicts = new ArrayList<>();

            // 检查单次限额冲突
            if (currentConfig.getSingleLimit() != null && limitConfig.getSingleLimit() != null &&
                limitConfig.getSingleLimit().compareTo(currentConfig.getSingleLimit()) > 0) {
                conflicts.add("新单次限额(" + limitConfig.getSingleLimit() + ")高于当前限额(" + currentConfig.getSingleLimit() + ")");
            }

            // 检查日度限额冲突
            if (currentConfig.getDailyLimit() != null && limitConfig.getDailyLimit() != null &&
                limitConfig.getDailyLimit().compareTo(currentConfig.getDailyLimit()) > 0) {
                conflicts.add("新日度限额(" + limitConfig.getDailyLimit() + ")高于当前限额(" + currentConfig.getDailyLimit() + ")");
            }

            // 检查优先级冲突
            if (currentConfig.getPriority() != null && limitConfig.getPriority() != null &&
                !Objects.equals(currentConfig.getPriority(), limitConfig.getPriority())) {
                conflicts.add("优先级变更: " + currentConfig.getPriority() + " -> " + limitConfig.getPriority());
            }

            return LimitConflictCheckResult.builder()
                .hasConflict(!conflicts.isEmpty())
                .conflicts(conflicts)
                .currentConfig(currentConfig)
                .newConfig(limitConfig)
                .checkTime(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("检查限额冲突异常: personId={}, error={}", personId, e.getMessage(), e);
            return LimitConflictCheckResult.builder()
                .hasConflict(true)
                .conflicts(List.of("系统异常: " + e.getMessage()))
                .checkTime(LocalDateTime.now())
                .build();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 合并多个限额配置
     */
    private ConsumeLimitConfig mergeLimitConfigs(List<ConsumeLimitConfigEntity> entities) {
        if (entities.isEmpty()) {
            return ConsumeLimitConfig.createDefault();
        }

        // 按优先级排序
        entities.sort(Comparator.comparing(ConsumeLimitConfigEntity::getPriority, Comparator.nullsLast(Comparator.reverseOrder())));

        ConsumeLimitConfig merged = ConsumeLimitConfig.builder().build();

        for (ConsumeLimitConfigEntity entity : entities) {
            if (entity.getSingleLimit() != null && merged.getSingleLimit() == null) {
                merged.setSingleLimit(entity.getSingleLimit());
            }
            if (entity.getDailyLimit() != null && merged.getDailyLimit() == null) {
                merged.setDailyLimit(entity.getDailyLimit());
            }
            if (entity.getWeeklyLimit() != null && merged.getWeeklyLimit() == null) {
                merged.setWeeklyLimit(entity.getWeeklyLimit());
            }
            if (entity.getMonthlyLimit() != null && merged.getMonthlyLimit() == null) {
                merged.setMonthlyLimit(entity.getMonthlyLimit());
            }
            if (entity.getDailyCountLimit() != null && merged.getDailyCountLimit() == null) {
                merged.setDailyCountLimit(entity.getDailyCountLimit());
            }
            if (entity.getWeeklyCountLimit() != null && merged.getWeeklyCountLimit() == null) {
                merged.setWeeklyCountLimit(entity.getWeeklyCountLimit());
            }
            if (entity.getMonthlyCountLimit() != null && merged.getMonthlyCountLimit() == null) {
                merged.setMonthlyCountLimit(entity.getMonthlyCountLimit());
            }
            if (entity.getMinAmount() != null && merged.getMinAmount() == null) {
                merged.setMinAmount(entity.getMinAmount());
            }
            if (entity.getMaxAmount() != null && merged.getMaxAmount() == null) {
                merged.setMaxAmount(entity.getMaxAmount());
            }
            if (entity.getEnabled() != null && merged.getEnabled() == null) {
                merged.setEnabled(entity.getEnabled());
            }
            if (entity.getPriority() != null && merged.getPriority() == null) {
                merged.setPriority(entity.getPriority());
            }
        }

        return merged;
    }

    /**
     * 获取临时用户限额
     */
    private ConsumeLimitConfig getTemporaryUserLimit(Long personId) {
        String tempKey = TEMPORARY_LIMIT_PREFIX + personId;
        return RedisUtil.get(tempKey, ConsumeLimitConfig.class);
    }

    /**
     * 清除用户限额缓存
     */
    private void clearUserLimitCache(Long personId) {
        String cacheKey = LIMIT_CONFIG_PREFIX + "user:" + personId;
        RedisUtil.delete(cacheKey);
    }

    /**
     * 验证限额配置
     */
    private void validateLimitConfig(ConsumeLimitConfig limitConfig) {
        if (limitConfig == null) {
            throw new SmartException("限额配置不能为空");
        }

        if (limitConfig.getSingleLimit() != null && limitConfig.getSingleLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("单次限额必须大于0");
        }

        if (limitConfig.getDailyLimit() != null && limitConfig.getDailyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("日度限额必须大于0");
        }

        if (limitConfig.getWeeklyLimit() != null && limitConfig.getWeeklyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("周度限额必须大于0");
        }

        if (limitConfig.getMonthlyLimit() != null && limitConfig.getMonthlyLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("月度限额必须大于0");
        }
    }

    /**
     * 转换为实体对象
     */
    private ConsumeLimitConfigEntity convertToEntity(ConsumeLimitConfig config) {
        ConsumeLimitConfigEntity entity = new ConsumeLimitConfigEntity();
        entity.setSingleLimit(config.getSingleLimit());
        entity.setDailyLimit(config.getDailyLimit());
        entity.setWeeklyLimit(config.getWeeklyLimit());
        entity.setMonthlyLimit(config.getMonthlyLimit());
        entity.setDailyCountLimit(config.getDailyCountLimit());
        entity.setWeeklyCountLimit(config.getWeeklyCountLimit());
        entity.setMonthlyCountLimit(config.getMonthlyCountLimit());
        entity.setMinAmount(config.getMinAmount());
        entity.setMaxAmount(config.getMaxAmount());
        entity.setEnabled(config.getEnabled());
        entity.setPriority(config.getPriority());
        entity.setEffectiveTime(config.getEffectiveTime());
        entity.setExpireTime(config.getExpireTime());
        entity.setExtendProperties(config.getExtendProperties());
        return entity;
    }

    /**
     * 记录限额变更
     */
    private void recordLimitChange(Long personId, String targetType, ConsumeLimitConfig config, String reason) {
        // TODO: 实现记录限额变更历史
    }

    /**
     * 记录临时限额设置
     */
    private void recordTemporaryLimitSet(Long personId, ConsumeLimitConfig config, int duration, String reason) {
        // TODO: 实现记录临时限额设置历史
    }

    /**
     * 计算使用率
     */
    private BigDecimal calculateUsageRate(BigDecimal used, BigDecimal limit) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return used.divide(limit, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
    }

    /**
     * 获取有效的单次限额
     */
    private BigDecimal getEffectiveSingleLimit(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                              ConsumeLimitConfig regionConfig, ConsumeLimitConfig modeConfig,
                                              String consumptionMode, Long deviceId, String regionId) {
        BigDecimal effectiveLimit = userConfig != null ? userConfig.getSingleLimit() : null;

        if (deviceConfig != null && deviceConfig.getSingleLimit() != null &&
            (effectiveLimit == null || deviceConfig.getSingleLimit().compareTo(effectiveLimit) < 0)) {
            effectiveLimit = deviceConfig.getSingleLimit();
        }

        if (regionConfig != null && regionConfig.getSingleLimit() != null &&
            (effectiveLimit == null || regionConfig.getSingleLimit().compareTo(effectiveLimit) < 0)) {
            effectiveLimit = regionConfig.getSingleLimit();
        }

        if (modeConfig != null && modeConfig.getSingleLimit() != null &&
            (effectiveLimit == null || modeConfig.getSingleLimit().compareTo(effectiveLimit) < 0)) {
            effectiveLimit = modeConfig.getSingleLimit();
        }

        return effectiveLimit;
    }

    /**
     * 验证日度限额
     */
    private LimitValidationResult validateDailyLimit(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                                    ConsumeLimitConfig regionConfig, ConsumeStatistics statistics,
                                                    BigDecimal amount) {
        BigDecimal dailyLimit = userConfig != null ? userConfig.getDailyLimit() : null;
        if (deviceConfig != null && deviceConfig.getDailyLimit() != null &&
            (dailyLimit == null || deviceConfig.getDailyLimit().compareTo(dailyLimit) < 0)) {
            dailyLimit = deviceConfig.getDailyLimit();
        }
        if (regionConfig != null && regionConfig.getDailyLimit() != null &&
            (dailyLimit == null || regionConfig.getDailyLimit().compareTo(dailyLimit) < 0)) {
            dailyLimit = regionConfig.getDailyLimit();
        }

        if (dailyLimit != null && statistics.getDailyAmount().add(amount).compareTo(dailyLimit) > 0) {
            return LimitValidationResult.dailyLimitExceeded(dailyLimit, statistics.getDailyAmount(), amount);
        }

        return LimitValidationResult.success();
    }

    /**
     * 验证周度限额
     */
    private LimitValidationResult validateWeeklyLimit(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                                     ConsumeLimitConfig regionConfig, ConsumeStatistics statistics,
                                                     BigDecimal amount) {
        BigDecimal weeklyLimit = userConfig != null ? userConfig.getWeeklyLimit() : null;
        if (deviceConfig != null && deviceConfig.getWeeklyLimit() != null &&
            (weeklyLimit == null || deviceConfig.getWeeklyLimit().compareTo(weeklyLimit) < 0)) {
            weeklyLimit = deviceConfig.getWeeklyLimit();
        }
        if (regionConfig != null && regionConfig.getWeeklyLimit() != null &&
            (weeklyLimit == null || regionConfig.getWeeklyLimit().compareTo(weeklyLimit) < 0)) {
            weeklyLimit = regionConfig.getWeeklyLimit();
        }

        if (weeklyLimit != null && statistics.getWeeklyAmount().add(amount).compareTo(weeklyLimit) > 0) {
            return LimitValidationResult.failure("WEEKLY", "超出周度消费限额");
        }

        return LimitValidationResult.success();
    }

    /**
     * 验证月度限额
     */
    private LimitValidationResult validateMonthlyLimit(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                                      ConsumeLimitConfig regionConfig, ConsumeStatistics statistics,
                                                      BigDecimal amount) {
        BigDecimal monthlyLimit = userConfig != null ? userConfig.getMonthlyLimit() : null;
        if (deviceConfig != null && deviceConfig.getMonthlyLimit() != null &&
            (monthlyLimit == null || deviceConfig.getMonthlyLimit().compareTo(monthlyLimit) < 0)) {
            monthlyLimit = deviceConfig.getMonthlyLimit();
        }
        if (regionConfig != null && regionConfig.getMonthlyLimit() != null &&
            (monthlyLimit == null || regionConfig.getMonthlyLimit().compareTo(monthlyLimit) < 0)) {
            monthlyLimit = regionConfig.getMonthlyLimit();
        }

        if (monthlyLimit != null && statistics.getMonthlyAmount().add(amount).compareTo(monthlyLimit) > 0) {
            return LimitValidationResult.failure("MONTHLY", "超出月度消费限额");
        }

        return LimitValidationResult.success();
    }

    /**
     * 验证次数限制
     */
    private LimitValidationResult validateCountLimit(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                                   ConsumeLimitConfig regionConfig, ConsumeStatistics statistics) {
        Integer dailyCountLimit = userConfig != null ? userConfig.getDailyCountLimit() : null;
        if (deviceConfig != null && deviceConfig.getDailyCountLimit() != null &&
            (dailyCountLimit == null || deviceConfig.getDailyCountLimit() < dailyCountLimit)) {
            dailyCountLimit = deviceConfig.getDailyCountLimit();
        }
        if (regionConfig != null && regionConfig.getDailyCountLimit() != null &&
            (dailyCountLimit == null || regionConfig.getDailyCountLimit() < dailyCountLimit)) {
            dailyCountLimit = regionConfig.getDailyCountLimit();
        }

        if (dailyCountLimit != null && statistics.getDailyCount() >= dailyCountLimit) {
            return LimitValidationResult.countLimitExceeded("DAILY_COUNT", dailyCountLimit);
        }

        return LimitValidationResult.success();
    }

    /**
     * 验证时间段限制
     */
    private LimitValidationResult validateTimeSlotLimit(ConsumeLimitConfig userConfig, String consumptionMode) {
        if (userConfig == null || userConfig.getTimeSlotLimits() == null) {
            return LimitValidationResult.success();
        }

        for (TimeSlotLimit timeSlot : userConfig.getTimeSlotLimits().values()) {
            if (timeSlot.isCurrentlyApplicable()) {
                return LimitValidationResult.success();
            }
        }

        return LimitValidationResult.timeLimitExceeded("当前时间", "不允许消费");
    }

    /**
     * 计算剩余日度额度
     */
    private BigDecimal calculateRemainingDaily(ConsumeLimitConfig userConfig, ConsumeLimitConfig deviceConfig,
                                               ConsumeLimitConfig regionConfig, ConsumeStatistics statistics) {
        BigDecimal dailyLimit = userConfig != null ? userConfig.getDailyLimit() : null;
        if (deviceConfig != null && deviceConfig.getDailyLimit() != null &&
            (dailyLimit == null || deviceConfig.getDailyLimit().compareTo(dailyLimit) < 0)) {
            dailyLimit = deviceConfig.getDailyLimit();
        }
        if (regionConfig != null && regionConfig.getDailyLimit() != null &&
            (dailyLimit == null || regionConfig.getDailyLimit().compareTo(dailyLimit) < 0)) {
            dailyLimit = regionConfig.getDailyLimit();
        }

        if (dailyLimit == null) {
            return null;
        }

        BigDecimal remaining = dailyLimit.subtract(statistics.getDailyAmount());
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    /**
     * 创建调整后的配置
     */
    private ConsumeLimitConfig createAdjustedConfig(ConsumeLimitConfig original, String adjustType, Double adjustRatio) {
        ConsumeLimitConfig adjusted = ConsumeLimitConfig.builder().build();

        switch (adjustType.toUpperCase()) {
            case "INCREASE":
                adjusted.setSingleLimit(multiplyByRatio(original.getSingleLimit(), 1 + adjustRatio));
                adjusted.setDailyLimit(multiplyByRatio(original.getDailyLimit(), 1 + adjustRatio));
                adjusted.setWeeklyLimit(multiplyByRatio(original.getWeeklyLimit(), 1 + adjustRatio));
                adjusted.setMonthlyLimit(multiplyByRatio(original.getMonthlyLimit(), 1 + adjustRatio));
                break;
            case "DECREASE":
                adjusted.setSingleLimit(multiplyByRatio(original.getSingleLimit(), 1 - adjustRatio));
                adjusted.setDailyLimit(multiplyByRatio(original.getDailyLimit(), 1 - adjustRatio));
                adjusted.setWeeklyLimit(multiplyByRatio(original.getWeeklyLimit(), 1 - adjustRatio));
                adjusted.setMonthlyLimit(multiplyByRatio(original.getMonthlyLimit(), 1 - adjustRatio));
                break;
            case "RESET":
                return ConsumeLimitConfig.createDefault();
            default:
                throw new SmartException("不支持的调整类型: " + adjustType);
        }

        // 复制其他属性
        adjusted.setDailyCountLimit(original.getDailyCountLimit());
        adjusted.setWeeklyCountLimit(original.getWeeklyCountLimit());
        adjusted.setMonthlyCountLimit(original.getMonthlyCountLimit());
        adjusted.setMinAmount(original.getMinAmount());
        adjusted.setMaxAmount(original.getMaxAmount());
        adjusted.setEnabled(original.getEnabled());
        adjusted.setPriority(original.getPriority());

        return adjusted;
    }

    /**
     * 金额乘以比例
     */
    private BigDecimal multiplyByRatio(BigDecimal amount, Double ratio) {
        if (amount == null || ratio == null) {
            return null;
        }
        return amount.multiply(new BigDecimal(ratio)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    // 其他实现方法省略...
}