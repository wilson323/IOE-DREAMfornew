/*
 * 异常操作检测服务实现
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.*;
import net.lab1024.sa.admin.module.consume.domain.entity.*;
import net.lab1024.sa.admin.module.consume.domain.vo.*;
import net.lab1024.sa.admin.module.consume.domain.result.*;
import net.lab1024.sa.admin.module.consume.domain.pattern.ConsumptionPattern;
import net.lab1024.sa.admin.module.consume.domain.pattern.TimePattern;
import net.lab1024.sa.admin.module.consume.domain.pattern.LocationPattern;
import net.lab1024.sa.admin.module.consume.domain.pattern.DevicePattern;
import net.lab1024.sa.admin.module.consume.domain.analysis.BehaviorAnalysis;
import net.lab1024.sa.admin.module.consume.service.AbnormalDetectionService;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartIpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 异常操作检测服务实现
 * 提供全面的异常行为检测和分析功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Service
@Slf4j
public class AbnormalDetectionServiceImpl implements AbnormalDetectionService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountDao accountDao;

    @Resource
    private AbnormalDetectionRuleEntityDao abnormalDetectionRuleDao;

    @Resource
    private UserBehaviorBaselineDao userBehaviorBaselineDao;

    @Resource
    private AbnormalOperationLogDao abnormalOperationLogDao;

    @Value("${abnormal.detection.enabled:true}")
    private Boolean detectionEnabled;

    @Value("${abnormal.detection.ml.enabled:false}")
    private Boolean mlDetectionEnabled;

    @Value("${abnormal.detection.cache-hours:1}")
    private Integer cacheHours;

    private static final String BASELINE_PREFIX = "behavior_baseline:";
    private static final String RISK_SCORE_PREFIX = "risk_score:";
    private static final String DETECTION_RULE_PREFIX = "detection_rule:";

    @Override
    public AbnormalDetectionResult detectAbnormalOperation(@NotNull Long personId, @NotNull BigDecimal amount,
                                                           Long deviceId, String regionId, String clientIp,
                                                           String consumptionMode) {
        try {
            if (!Boolean.TRUE.equals(detectionEnabled)) {
                return AbnormalDetectionResult.normal();
            }

            log.debug("开始检测异常操作: personId={}, amount={}, deviceId={}", personId, amount, deviceId);

            // 1. 获取用户行为基线
            UserBehaviorBaseline baseline = getUserBehaviorBaseline(personId);
            if (baseline == null) {
                // 首次检测，建立基线
                baseline = createUserBaseline(personId);
            }

            // 2. 执行多维度异常检测
            List<AbnormalDetectionResult> results = new ArrayList<>();

            // 金额异常检测
            AmountAnomalyResult amountResult = detectAmountAnomaly(personId, amount, "CONSUME");
            results.add(convertToDetectionResult(amountResult));

            // 频率异常检测
            FrequencyAnomalyResult frequencyResult = detectFrequencyAnomaly(personId, "CONSUME", 60);
            results.add(convertToDetectionResult(frequencyResult));

            // 时间异常检测
            TimeAnomalyResult timeResult = detectTimeAnomaly(personId, LocalDateTime.now(), "CONSUME");
            results.add(convertToDetectionResult(timeResult));

            // 设备异常检测（如果有设备ID）
            if (deviceId != null) {
                DeviceAnomalyResult deviceResult = detectDeviceAnomaly(personId, deviceId, "CONSUME");
                results.add(convertToDetectionResult(deviceResult));
            }

            // 地理位置异常检测（如果有IP地址）
            if (StringUtils.hasText(clientIp)) {
                LocationAnomalyResult locationResult = detectLocationAnomaly(personId, clientIp, deviceId);
                results.add(convertToDetectionResult(locationResult));
            }

            // 3. 综合评估异常等级
            AbnormalDetectionResult finalResult = evaluateOverallAnomaly(results);

            // 4. 记录检测日志
            recordDetectionLog(personId, amount, deviceId, finalResult);

            // 5. 触发相应的告警
            if (finalResult.isAbnormal() && Boolean.TRUE.equals(finalResult.getTriggerAlert())) {
                generateDetectionAlert(personId, finalResult);
            }

            log.debug("异常检测完成: personId={}, isAbnormal={}, riskScore={}",
                personId, finalResult.isAbnormal(), finalResult.getRiskScore());

            return finalResult;

        } catch (Exception e) {
            log.error("异常检测异常: personId={}, amount={}, error={}", personId, amount, e.getMessage(), e);
            return AbnormalDetectionResult.abnormal("SYSTEM_ERROR", "MEDIUM", "系统异常，检测失败");
        }
    }

    @Override
    public BehaviorMonitoringResult monitorUserBehavior(@NotNull Long personId, @NotNull String operationType,
                                                        Object operationData) {
        try {
            // 获取用户当前风险评分
            UserRiskScore currentRiskScore = getUserRiskScore(personId);

            // 实时行为分析
            BehaviorAnalysis analysis = analyzeBehaviorPattern(personId, operationType, operationData);

            // 判断是否需要干预
            boolean needIntervention = determineNeedIntervention(analysis, currentRiskScore);

            return BehaviorMonitoringResult.builder()
                    .personId(personId)
                    .operationType(operationType)
                    .riskScore(currentRiskScore.getScore())
                    .riskLevel(currentRiskScore.getLevel())
                    .behaviorAnalysis(analysis)
                    .needIntervention(needIntervention)
                    .monitoringTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("用户行为监控异常: personId={}, operationType={}, error={}", personId, operationType, e.getMessage(), e);
            return BehaviorMonitoringResult.builder()
                    .personId(personId)
                    .operationType(operationType)
                    .riskScore(0)
                    .riskLevel("UNKNOWN")
                    .needIntervention(false)
                    .monitoringTime(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public UserPatternAnalysis analyzeUserPattern(@NotNull Long personId, Integer analysisDays) {
        try {
            int days = analysisDays != null ? analysisDays : 30;
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);

            // 获取用户操作历史
            List<ConsumeRecordEntity> records = consumeRecordDao.selectByPersonIdAndTimeRange(
                personId, startTime, LocalDateTime.now());

            if (records.isEmpty()) {
                return UserPatternAnalysis.builder()
                        .personId(personId)
                        .hasEnoughData(false)
                        .message("数据不足，无法进行模式分析")
                        .build();
            }

            // 分析消费模式
            ConsumptionPattern consumptionPattern = analyzeConsumptionPattern(records);

            // 分析时间模式
            TimePattern timePattern = analyzeTimePattern(records);

            // 分析地点模式
            LocationPattern locationPattern = analyzeLocationPattern(records);

            // 分析设备模式
            DevicePattern devicePattern = analyzeDevicePattern(records);

            // 生成综合模式分析
            return UserPatternAnalysis.builder()
                    .personId(personId)
                    .analysisPeriod(days)
                    .totalOperations(records.size())
                    .consumptionPattern(consumptionPattern)
                    .timePattern(timePattern)
                    .locationPattern(locationPattern)
                    .devicePattern(devicePattern)
                    .hasEnoughData(true)
                    .analysisTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("用户模式分析异常: personId={}, error={}", personId, e.getMessage(), e);
            return UserPatternAnalysis.builder()
                    .personId(personId)
                    .hasEnoughData(false)
                    .message("分析异常: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public LocationAnomalyResult detectLocationAnomaly(@NotNull Long personId, @NotNull String currentLocation,
                                                     Long deviceId) {
        try {
            // 获取用户历史位置信息
            List<ConsumeRecordEntity> locationHistory = consumeRecordDao.selectRecentByPersonId(personId, 50);

            if (locationHistory.size() < 3) {
                return LocationAnomalyResult.normal("历史位置数据不足");
            }

            // 分析常用位置
            Map<String, Integer> locationFrequency = locationHistory.stream()
                .filter(record -> StringUtils.hasText(record.getClientIp()))
                .collect(Collectors.groupingBy(
                    ConsumeRecordEntity::getClientIp,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            // 获取最常用的位置
            String mostFrequentLocation = locationFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

            // 判断当前位置是否异常
            boolean isAbnormal = !locationFrequency.containsKey(currentLocation);

            // 计算异常程度
            double anomalyScore = 0.0;
            if (isAbnormal) {
                anomalyScore = 80.0; // 未知位置给予高风险评分
            } else {
                int frequency = locationFrequency.get(currentLocation);
                anomalyScore = Math.max(0, 100 - (frequency * 10)); // 频率越低，风险越高
            }

            String message = isAbnormal ?
                "检测到新的消费位置，存在异常风险" :
                "消费位置正常，符合历史模式";

            return LocationAnomalyResult.builder()
                    .isAbnormal(isAbnormal)
                    .currentLocation(currentLocation)
                    .mostFrequentLocation(mostFrequentLocation)
                    .locationFrequency(locationFrequency)
                    .anomalyScore(anomalyScore)
                    .message(message)
                    .detectionTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("位置异常检测异常: personId={}, location={}, error={}", personId, currentLocation, e.getMessage(), e);
            return LocationAnomalyResult.abnormal("SYSTEM_ERROR", "系统异常，位置检测失败");
        }
    }

    @Override
    public TimeAnomalyResult detectTimeAnomaly(@NotNull Long personId, LocalDateTime operationTime,
                                                String operationType) {
        try {
            // 获取用户操作时间历史
            List<ConsumeRecordEntity> timeHistory = consumeRecordDao.selectRecentByPersonId(personId, 100);

            if (timeHistory.size() < 10) {
                return TimeAnomalyResult.normal("历史时间数据不足");
            }

            // 分析时间分布
            Map<Integer, Integer> hourDistribution = timeHistory.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getConsumeTime().getHour(),
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            Map<Integer, Integer> dayOfWeekDistribution = timeHistory.stream()
                .collect(Collectors.groupingBy(
                    record -> record.getConsumeTime().getDayOfWeek().getValue(),
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            // 计算当前时间的异常程度
            int currentHour = operationTime.getHour();
            int currentDayOfWeek = operationTime.getDayOfWeek().getValue();

            int hourFrequency = hourDistribution.getOrDefault(currentHour, 0);
            int dayFrequency = dayOfWeekDistribution.getOrDefault(currentDayOfWeek, 0);

            // 异常时间检测（深夜、凌晨等）
            boolean isAbnormalHour = currentHour >= 23 || currentHour <= 5;

            // 计算时间异常评分
            double timeAnomalyScore = 0.0;
            if (isAbnormalHour) {
                timeAnomalyScore = 70.0; // 深夜操作给予较高风险评分
            } else {
                int totalOperations = timeHistory.size();
                double expectedFrequency = (double) totalOperations / 24; // 平均每小时频率
                timeAnomalyScore = Math.max(0, 100 - (hourFrequency / expectedFrequency * 100));
            }

            String message = isAbnormalHour ?
                "检测到异常时间操作，可能存在风险" :
                "操作时间正常，符合用户历史模式";

            return TimeAnomalyResult.builder()
                    .isAbnormal(isAbnormalHour || timeAnomalyScore > 60)
                    .operationTime(operationTime)
                    .hourDistribution(hourDistribution)
                    .dayOfWeekDistribution(dayOfWeekDistribution)
                    .currentHourFrequency(hourFrequency)
                    .timeAnomalyScore(timeAnomalyScore)
                    .message(message)
                    .detectionTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("时间异常检测异常: personId={}, time={}, error={}", personId, operationTime, e.getMessage(), e);
            return TimeAnomalyResult.abnormal("SYSTEM_ERROR", "系统异常，时间检测失败");
        }
    }

    @Override
    public AmountAnomalyResult detectAmountAnomaly(@NotNull Long personId, @NotNull BigDecimal amount,
                                                    String operationType) {
        try {
            // 获取用户历史消费金额
            List<ConsumeRecordEntity> amountHistory = consumeRecordDao.selectRecentByPersonId(personId, 50);

            if (amountHistory.size() < 5) {
                return AmountAnomalyResult.normal("历史金额数据不足");
            }

            // 计算统计指标
            List<BigDecimal> amounts = amountHistory.stream()
                .map(ConsumeRecordEntity::getAmount)
                .collect(Collectors.toList());

            BigDecimal mean = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);

            BigDecimal max = amounts.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = amounts.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            // 计算标准差
            double variance = amounts.stream()
                .mapToDouble(a -> a.subtract(mean).doubleValue() * a.subtract(mean).doubleValue())
                .average()
                .orElse(0.0);
            double stdDev = Math.sqrt(variance);

            // 异常金额检测（超出均值3个标准差）
            double zScore = amount.subtract(mean).doubleValue() / stdDev;
            boolean isAbnormal = Math.abs(zScore) > 3.0;

            // 大额交易检测
            boolean isLargeAmount = amount.compareTo(mean.multiply(BigDecimal.valueOf(5))) > 0;

            // 计算异常评分
            double anomalyScore = Math.min(100, Math.abs(zScore) * 20);
            if (isLargeAmount) {
                anomalyScore = Math.max(anomalyScore, 80.0);
            }

            String message = isAbnormal ?
                "检测到异常消费金额，可能存在风险" :
                "消费金额正常，符合用户历史模式";

            return AmountAnomalyResult.builder()
                    .isAbnormal(isAbnormal || isLargeAmount)
                    .currentAmount(amount)
                    .meanAmount(mean)
                    .maxAmount(max)
                    .minAmount(min)
                    .standardDeviation(BigDecimal.valueOf(stdDev))
                    .zScore(zScore)
                    .anomalyScore(anomalyScore)
                    .isLargeAmount(isLargeAmount)
                    .message(message)
                    .detectionTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("金额异常检测异常: personId={}, amount={}, error={}", personId, amount, e.getMessage(), e);
            return AmountAnomalyResult.abnormal("SYSTEM_ERROR", "系统异常，金额检测失败");
        }
    }

    @Override
    public DeviceAnomalyResult detectDeviceAnomaly(@NotNull Long personId, @NotNull Long deviceId,
                                                    String operationType) {
        try {
            // 获取用户设备使用历史
            List<ConsumeRecordEntity> deviceHistory = consumeRecordDao.selectRecentByPersonId(personId, 100);

            if (deviceHistory.size() < 5) {
                return DeviceAnomalyResult.normal("历史设备数据不足");
            }

            // 统计设备使用频率
            Map<Long, Integer> deviceFrequency = deviceHistory.stream()
                .filter(record -> record.getDeviceId() != null)
                .collect(Collectors.groupingBy(
                    ConsumeRecordEntity::getDeviceId,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            int currentDeviceFrequency = deviceFrequency.getOrDefault(deviceId, 0);
            int totalOperations = deviceHistory.size();
            double usageRate = (double) currentDeviceFrequency / totalOperations;

            // 新设备检测
            boolean isNewDevice = currentDeviceFrequency == 0;

            // 低频设备检测
            boolean isLowFrequencyDevice = usageRate < 0.1 && currentDeviceFrequency > 0;

            // 计算异常评分
            double anomalyScore = 0.0;
            if (isNewDevice) {
                anomalyScore = 60.0; // 新设备给予中等风险评分
            } else if (isLowFrequencyDevice) {
                anomalyScore = 40.0; // 低频设备给予较低风险评分
            } else {
                anomalyScore = Math.max(0, 100 - usageRate * 100);
            }

            String message = isNewDevice ?
                "检测到新设备使用，需要关注" :
                "设备使用正常，符合用户历史模式";

            return DeviceAnomalyResult.builder()
                    .isAbnormal(isNewDevice)
                    .currentDeviceId(deviceId)
                    .deviceFrequency(deviceFrequency)
                    .currentDeviceFrequency(currentDeviceFrequency)
                    .usageRate(usageRate)
                    .anomalyScore(anomalyScore)
                    .isNewDevice(isNewDevice)
                    .isLowFrequencyDevice(isLowFrequencyDevice)
                    .message(message)
                    .detectionTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("设备异常检测异常: personId={}, deviceId={}, error={}", personId, deviceId, e.getMessage(), e);
            return DeviceAnomalyResult.abnormal("SYSTEM_ERROR", "系统异常，设备检测失败");
        }
    }

    @Override
    public FrequencyAnomalyResult detectFrequencyAnomaly(@NotNull Long personId, @NotNull String operationType,
                                                           Integer timeWindowMinutes) {
        try {
            int windowMinutes = timeWindowMinutes != null ? timeWindowMinutes : 60;
            LocalDateTime windowStart = LocalDateTime.now().minusMinutes(windowMinutes);

            // 获取时间窗口内的操作记录
            List<ConsumeRecordEntity> windowOperations = consumeRecordDao.selectByPersonIdAndTimeRange(
                personId, windowStart, LocalDateTime.now());

            int operationCount = windowOperations.size();

            // 获取用户历史操作频率基线
            UserBehaviorBaseline baseline = getUserBehaviorBaseline(personId);
            double baselineFrequency = baseline != null ? baseline.getHourlyOperationCount() : 2.0;

            // 计算预期操作数
            double expectedOperations = baselineFrequency * (windowMinutes / 60.0);

            // 计算频率偏差
            double frequencyDeviation = Math.abs(operationCount - expectedOperations) / expectedOperations;

            // 高频操作检测
            boolean isHighFrequency = operationCount > expectedOperations * 3;

            // 异常频率评分
            double anomalyScore = Math.min(100, frequencyDeviation * 100);
            if (isHighFrequency) {
                anomalyScore = Math.max(anomalyScore, 80.0);
            }

            boolean isAbnormal = frequencyDeviation > 2.0 || isHighFrequency;

            String message = isAbnormal ?
                "检测到异常操作频率，可能存在风险" :
                "操作频率正常，符合用户历史模式";

            return FrequencyAnomalyResult.builder()
                    .isAbnormal(isAbnormal)
                    .operationCount(operationCount)
                    .timeWindowMinutes(windowMinutes)
                    .baselineFrequency(baselineFrequency)
                    .expectedOperations(expectedOperations)
                    .frequencyDeviation(frequencyDeviation)
                    .anomalyScore(anomalyScore)
                    .isHighFrequency(isHighFrequency)
                    .message(message)
                    .detectionTime(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("频率异常检测异常: personId={}, operationType={}, error={}", personId, operationType, e.getMessage(), e);
            return FrequencyAnomalyResult.abnormal("SYSTEM_ERROR", "系统异常，频率检测失败");
        }
    }

    @Override
    public UserRiskScore getUserRiskScore(@NotNull Long personId) {
        try {
            // 从缓存获取风险评分
            String cacheKey = RISK_SCORE_PREFIX + personId;
            UserRiskScore cachedScore = RedisUtil.get(cacheKey, UserRiskScore.class);
            if (cachedScore != null) {
                return cachedScore;
            }

            // 计算风险评分
            UserRiskScore riskScore = calculateUserRiskScore(personId);

            // 缓存风险评分（较短时间，以便及时更新）
            RedisUtil.set(cacheKey, riskScore, 15, TimeUnit.MINUTES);

            return riskScore;

        } catch (Exception e) {
            log.error("获取用户风险评分异常: personId={}, error={}", personId, e.getMessage(), e);
            return UserRiskScore.builder()
                    .personId(personId)
                    .score(0)
                    .level("UNKNOWN")
                    .lastUpdateTime(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRiskScore(@NotNull Long personId, @NotNull AbnormalDetectionResult operationResult) {
        try {
            // 获取当前风险评分
            UserRiskScore currentScore = getUserRiskScore(personId);

            // 根据操作结果更新风险评分
            int newScore = calculateUpdatedRiskScore(currentScore.getScore(), operationResult);

            String riskLevel = determineRiskLevel(newScore);

            UserRiskScore updatedScore = UserRiskScore.builder()
                    .personId(personId)
                    .score(newScore)
                    .level(riskLevel)
                    .lastUpdateTime(LocalDateTime.now())
                    .build();

            // 更新缓存
            String cacheKey = RISK_SCORE_PREFIX + personId;
            RedisUtil.set(cacheKey, updatedScore, cacheHours, TimeUnit.HOURS);

            // 记录风险评分变更历史
            recordRiskScoreChange(personId, currentScore.getScore(), newScore, operationResult);

            log.info("用户风险评分更新: personId={}, oldScore={}, newScore={}, level={}",
                personId, currentScore.getScore(), newScore, riskLevel);

            return true;

        } catch (Exception e) {
            log.error("更新用户风险评分异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    // 继续实现其他方法...
    @Override
    public AbnormalOperationReport getAbnormalOperationReport(@NotNull Long personId, @NotNull String reportType,
                                                              String timeRange) {
        // TODO: 实现异常操作报告生成
        return AbnormalOperationReport.builder()
                .personId(personId)
                .reportType(reportType)
                .timeRange(timeRange)
                .reportTime(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean generateAlert(@NotNull String alertLevel, @NotNull String alertType, @NotNull String alertMessage,
                                Object relatedData) {
        // TODO: 实现异常告警生成
        log.info("生成异常告警: level={}, type={}, message={}", alertLevel, alertType, alertMessage);
        return true;
    }

    @Override
    public UserBehaviorBaseline getUserBehaviorBaseline(@NotNull Long personId) {
        try {
            String cacheKey = BASELINE_PREFIX + personId;
            UserBehaviorBaseline cachedBaseline = RedisUtil.get(cacheKey, UserBehaviorBaseline.class);
            if (cachedBaseline != null) {
                return cachedBaseline;
            }

            // 从数据库获取基线
            UserBehaviorBaselineEntity entity = userBehaviorBaselineDao.selectByPersonId(personId);
            if (entity == null) {
                return null;
            }

            UserBehaviorBaseline baseline = convertToBaseline(entity);

            // 缓存基线数据
            RedisUtil.set(cacheKey, baseline, cacheHours, TimeUnit.HOURS);

            return baseline;

        } catch (Exception e) {
            log.error("获取用户行为基线异常: personId={}, error={}", personId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserBehaviorBaseline(@NotNull Long personId, @NotNull UserBehaviorBaseline baseline) {
        try {
            // 清除缓存
            String cacheKey = BASELINE_PREFIX + personId;
            RedisUtil.delete(cacheKey);

            // 更新数据库
            UserBehaviorBaselineEntity entity = convertToEntity(baseline);
            entity.setPersonId(personId);
            entity.setUpdateTime(LocalDateTime.now());

            UserBehaviorBaselineEntity existing = userBehaviorBaselineDao.selectByPersonId(personId);
            int result;
            if (existing != null) {
                entity.setBaselineId(existing.getBaselineId());
                result = userBehaviorBaselineDao.updateById(entity);
            } else {
                entity.setCreateTime(LocalDateTime.now());
                result = userBehaviorBaselineDao.insert(entity);
            }

            return result > 0;

        } catch (Exception e) {
            log.error("更新用户行为基线异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    // 其他接口的默认实现
    @Override
    public SequenceAnomalyResult detectSequenceAnomaly(@NotNull Long personId, @NotNull List<OperationEventEntity> operations) {
        return SequenceAnomalyResult.normal("序列异常检测功能待实现");
    }

    @Override
    public AbnormalTrendAnalysis getAbnormalTrendAnalysis(@NotNull String analysisType, @NotNull String timeRange) {
        return AbnormalTrendAnalysis.builder()
                .analysisType(analysisType)
                .timeRange(timeRange)
                .analysisTime(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean setDetectionRuleEntitys(@NotNull String ruleType, @NotNull List<DetectionRuleEntity> rules) {
        log.info("设置检测规则: type={}, count={}", ruleType, rules.size());
        return true;
    }

    @Override
    public List<DetectionRuleEntity> getDetectionRuleEntitys(@NotNull String ruleType) {
        return new ArrayList<>();
    }

    @Override
    public BatchDetectionResult batchDetectAbnormalOperations(@NotNull List<OperationEventEntity> operations) {
        return BatchDetectionResult.builder()
                .totalOperations(operations.size())
                .successCount(0)
                .abnormalCount(0)
                .build();
    }

    @Override
    public ModelTrainingResult trainMachineLearningModel(@NotNull String modelType, @NotNull Object trainingData) {
        return ModelTrainingResult.builder()
                .modelType(modelType)
                .trainingTime(LocalDateTime.now())
                .success(false)
                .message("机器学习训练功能待实现")
                .build();
    }

    @Override
    public MLDetectionResult detectAnomalyWithML(@NotNull String modelType, @NotNull Object inputData) {
        return MLDetectionResult.builder()
                .modelType(modelType)
                .isAbnormal(false)
                .confidence(0.0)
                .build();
    }

    @Override
    public DetectionPerformanceMetrics getDetectionPerformanceMetrics(@NotNull String timeRange) {
        return DetectionPerformanceMetrics.builder()
                .timeRange(timeRange)
                .build();
    }

    @Override
    public ExportResult exportDetectionData(@NotNull String exportType, @NotNull Object filterConditions) {
        return ExportResult.builder()
                .exportType(exportType)
                .success(false)
                .message("导出功能待实现")
                .build();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建用户基线
     */
    private UserBehaviorBaseline createUserBaseline(Long personId) {
        try {
            // 获取用户历史操作数据
            List<ConsumeRecordEntity> records = consumeRecordDao.selectRecentByPersonId(personId, 100);

            if (records.size() < 10) {
                // 数据不足，返回默认基线
                UserBehaviorBaseline defaultBaseline = UserBehaviorBaseline.createDefault();
                defaultBaseline.setPersonId(personId);
                updateUserBehaviorBaseline(personId, defaultBaseline);
                return defaultBaseline;
            }

            // 计算基线指标
            double hourlyOperationCount = records.size() / 7.0; // 假设7天的数据
            BigDecimal averageAmount = records.stream()
                .map(ConsumeRecordEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);

            // 常用设备
            Map<Long, Integer> deviceFrequency = records.stream()
                .filter(record -> record.getDeviceId() != null)
                .collect(Collectors.groupingBy(
                    ConsumeRecordEntity::getDeviceId,
                    Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
                ));

            Long mostUsedDevice = deviceFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

            UserBehaviorBaseline baseline = UserBehaviorBaseline.builder()
                    .personId(personId)
                    .hourlyOperationCount(hourlyOperationCount)
                    .averageAmount(averageAmount)
                    .mostUsedDeviceId(mostUsedDevice)
                    .deviceFrequency(deviceFrequency)
                    .baselinePeriod(30)
                    .createTime(LocalDateTime.now())
                    .build();

            updateUserBehaviorBaseline(personId, baseline);
            return baseline;

        } catch (Exception e) {
            log.error("创建用户基线异常: personId={}, error={}", personId, e.getMessage(), e);
            return UserBehaviorBaseline.createDefault();
        }
    }

    /**
     * 评估整体异常等级
     */
    private AbnormalDetectionResult evaluateOverallAnomaly(List<AbnormalDetectionResult> results) {
        if (results.isEmpty()) {
            return AbnormalDetectionResult.normal();
        }

        // 统计异常结果
        long abnormalCount = results.stream().filter(AbnormalDetectionResult::isAbnormal).count();

        // 计算综合风险评分
        int maxRiskScore = results.stream()
            .mapToInt(r -> r.getRiskScore() != null ? r.getRiskScore() : 0)
            .max()
            .orElse(0);

        // 计算平均置信度
        double avgConfidence = results.stream()
            .filter(r -> r.getConfidence() != null)
            .mapToDouble(AbnormalDetectionResult::getConfidence)
            .average()
            .orElse(0.0);

        // 确定异常级别
        String anomalyLevel = "SAFE";
        if (abnormalCount > 0) {
            if (maxRiskScore >= 80) {
                anomalyLevel = "HIGH";
            } else if (maxRiskScore >= 60) {
                anomalyLevel = "MEDIUM";
            } else {
                anomalyLevel = "LOW";
            }
        }

        // 合并异常特征
        List<String> allFeatures = results.stream()
            .filter(r -> r.getDetectedFeatures() != null)
            .flatMap(r -> r.getDetectedFeatures().stream())
            .distinct()
            .collect(Collectors.toList());

        return AbnormalDetectionResult.builder()
                .isAbnormal(abnormalCount > 0)
                .anomalyType("MULTIPLE")
                .anomalyLevel(anomalyLevel)
                .confidence(avgConfidence)
                .riskScore(maxRiskScore)
                .detectedFeatures(allFeatures)
                .message(abnormalCount > 0 ?
                    String.format("检测到%d项异常，建议人工审核", abnormalCount) :
                    "综合检测正常")
                .detectionTime(LocalDateTime.now())
                .build();
    }

    /**
     * 记录检测日志
     */
    private void recordDetectionLog(Long personId, BigDecimal amount, Long deviceId, AbnormalDetectionResult result) {
        // TODO: 实现检测日志记录
    }

    /**
     * 生成检测告警
     */
    private void generateDetectionAlert(Long personId, AbnormalDetectionResult result) {
        String alertLevel = result.getAnomalyLevel();
        String alertType = result.getAnomalyType();
        String message = String.format("用户%d检测到%s异常，风险评分%d", personId, alertType, result.getRiskScore());

        generateAlert(alertLevel, alertType, message, result);
    }

    /**
     * 分析消费模式
     */
    private ConsumptionPattern analyzeConsumptionPattern(List<ConsumeRecordEntity> records) {
        BigDecimal totalAmount = records.stream()
            .map(ConsumeRecordEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);

        BigDecimal maxAmount = records.stream()
            .map(ConsumeRecordEntity::getAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return ConsumptionPattern.builder()
                .totalAmount(totalAmount)
                .averageAmount(averageAmount)
                .maxAmount(maxAmount)
                .operationCount(records.size())
                .build();
    }

    /**
     * 分析时间模式
     */
    private TimePattern analyzeTimePattern(List<ConsumeRecordEntity> records) {
        Map<Integer, Integer> hourDistribution = records.stream()
            .collect(Collectors.groupingBy(
                record -> record.getConsumeTime().getHour(),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        Map<Integer, Integer> dayDistribution = records.stream()
            .collect(Collectors.groupingBy(
                record -> record.getConsumeTime().getDayOfWeek().getValue(),
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        return TimePattern.builder()
                .hourDistribution(hourDistribution)
                .dayOfWeekDistribution(dayDistribution)
                .peakHour(hourDistribution.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(12))
                .build();
    }

    /**
     * 分析位置模式
     */
    private LocationPattern analyzeLocationPattern(List<ConsumeRecordEntity> records) {
        Map<String, Integer> locationFrequency = records.stream()
            .filter(record -> StringUtils.hasText(record.getClientIp()))
            .collect(Collectors.groupingBy(
                ConsumeRecordEntity::getClientIp,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        return LocationPattern.builder()
                .locationFrequency(locationFrequency)
                .uniqueLocationCount(locationFrequency.size())
                .mostFrequentLocation(locationFrequency.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("未知"))
                .build();
    }

    /**
     * 分析设备模式
     */
    private DevicePattern analyzeDevicePattern(List<ConsumeRecordEntity> records) {
        Map<Long, Integer> deviceFrequency = records.stream()
            .filter(record -> record.getDeviceId() != null)
            .collect(Collectors.groupingBy(
                ConsumeRecordEntity::getDeviceId,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        return DevicePattern.builder()
                .deviceFrequency(deviceFrequency)
                .uniqueDeviceCount(deviceFrequency.size())
                .mostUsedDevice(deviceFrequency.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(0L))
                .build();
    }

    /**
     * 分析行为模式
     */
    private BehaviorAnalysis analyzeBehaviorPattern(Long personId, String operationType, Object operationData) {
        // TODO: 实现行为模式分析
        return BehaviorAnalysis.builder()
                .personId(personId)
                .operationType(operationType)
                .riskLevel("UNKNOWN")
                .build();
    }

    /**
     * 判断是否需要干预
     */
    private boolean determineNeedIntervention(BehaviorAnalysis analysis, UserRiskScore currentRiskScore) {
        return "HIGH".equals(analysis.getRiskLevel()) || currentRiskScore.getScore() >= 70;
    }

    /**
     * 计算用户风险评分
     */
    private UserRiskScore calculateUserRiskScore(Long personId) {
        // 基础风险评分
        int baseScore = 30;

        // TODO: 基于用户历史行为计算风险评分
        return UserRiskScore.builder()
                .personId(personId)
                .score(baseScore)
                .level("LOW")
                .lastUpdateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 计算更新后的风险评分
     */
    private int calculateUpdatedRiskScore(int currentScore, AbnormalDetectionResult operationResult) {
        if (!operationResult.isAbnormal()) {
            // 正常操作，降低风险评分
            return Math.max(0, currentScore - 5);
        }

        // 异常操作，根据风险等级增加评分
        int increment = 0;
        switch (operationResult.getAnomalyLevel()) {
            case "LOW":
                increment = 10;
                break;
            case "MEDIUM":
                increment = 20;
                break;
            case "HIGH":
                increment = 35;
                break;
            case "CRITICAL":
                increment = 50;
                break;
            default:
                increment = 15;
        }

        return Math.min(100, currentScore + increment);
    }

    /**
     * 确定风险等级
     */
    private String determineRiskLevel(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 60) return "MEDIUM";
        if (score >= 30) return "LOW";
        return "SAFE";
    }

    /**
     * 记录风险评分变更
     */
    private void recordRiskScoreChange(Long personId, int oldScore, int newScore, AbnormalDetectionResult result) {
        // TODO: 实现风险评分变更记录
    }

    /**
     * 转换检测结果
     */
    private AbnormalDetectionResult convertToDetectionResult(Object specificResult) {
        if (specificResult instanceof AbnormalDetectionResult) {
            return (AbnormalDetectionResult) specificResult;
        }
        return AbnormalDetectionResult.normal();
    }

    /**
     * 转换为基线对象
     */
    private UserBehaviorBaseline convertToBaseline(UserBehaviorBaselineEntity entity) {
        return UserBehaviorBaseline.builder()
                .personId(entity.getPersonId())
                .hourlyOperationCount(entity.getHourlyOperationCount())
                .averageAmount(entity.getAverageAmount())
                .mostUsedDeviceId(entity.getMostUsedDeviceId())
                .baselinePeriod(entity.getBaselinePeriod())
                .createTime(entity.getCreateTime())
                .build();
    }

    /**
     * 转换为实体对象
     */
    private UserBehaviorBaselineEntity convertToEntity(UserBehaviorBaseline baseline) {
        UserBehaviorBaselineEntity entity = new UserBehaviorBaselineEntity();
        entity.setPersonId(baseline.getPersonId());
        entity.setHourlyOperationCount(baseline.getHourlyOperationCount());
        entity.setAverageAmount(baseline.getAverageAmount());
        entity.setMostUsedDeviceId(baseline.getMostUsedDeviceId());
        entity.setBaselinePeriod(baseline.getBaselinePeriod());
        return entity;
    }

    // 字符串工具方法
    private static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}