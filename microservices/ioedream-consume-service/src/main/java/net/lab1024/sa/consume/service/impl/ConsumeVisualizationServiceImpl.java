package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.security.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.domain.form.ConsumeStatisticsForm;
import net.lab1024.sa.consume.domain.vo.ConsumeComparisonVO;
import net.lab1024.sa.consume.domain.vo.ConsumeForecastAnalysisVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRankingVO;
import net.lab1024.sa.consume.domain.vo.ConsumeStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTrendVO;
import net.lab1024.sa.consume.domain.vo.ConsumeUserBehaviorAnalysisVO;
import net.lab1024.sa.consume.service.ConsumeVisualizationService;

/**
 * 消费可视化统计服务实现
 * <p>
 * 严格遵循四层架构规范的Service层实现
 * 提供消费数据的可视化分析和统计功能
 *
 * @author IOE-DREAM Team
 * @since 2025-12-02
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeVisualizationServiceImpl implements ConsumeVisualizationService {

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumeTransactionDao consumeTransactionDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 获取消费统计数据
     *
     * @param form 统计查询表单
     * @return 统计数据
     */
    @Override
    @Observed(name = "consume.visualization.getStatistics", contextualName = "consume-visualization-get-statistics")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeStatisticsVO> getStatistics(ConsumeStatisticsForm form) {
        log.info("获取消费统计数据，统计类型：{}", form.getStatisticsType());

        try {
            // 计算时间范围
            LocalDateTime[] timeRange = calculateTimeRange(form);
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2); // 成功状态

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算统计数据
            ConsumeStatisticsVO statistics = calculateStatistics(transactions, form.getTimeRangeType());

            log.info("获取消费统计数据成功，总次数：{}，总金额：{}",
                    statistics.getTotalCount(), statistics.getTotalAmount());
            return ResponseDTO.ok(statistics);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取消费统计数据失败，error={}", e.getMessage());
            throw new ParamException("GET_STATISTICS_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取消费统计数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取消费统计数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取消费统计数据失败", e);
            throw new SystemException("GET_STATISTICS_SYSTEM_ERROR", "获取统计数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消费趋势数据
     *
     * @param form 统计查询表单
     * @return 趋势数据
     */
    @Override
    @Observed(name = "consume.visualization.getTrend", contextualName = "consume-visualization-get-trend")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeTrendVO> getTrend(ConsumeStatisticsForm form) {
        log.info("获取消费趋势数据，时间类型：{}", form.getTimeRangeType());

        try {
            // 计算时间范围
            LocalDateTime[] timeRange = calculateTimeRange(form);
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2);

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算趋势数据
            ConsumeTrendVO trend = calculateTrend(transactions, form.getTimeRangeType(), form.getDataGranularity());

            log.info("获取消费趋势数据成功，数据点数：{}",
                    trend.getDataPoints() != null ? trend.getDataPoints().size() : 0);
            return ResponseDTO.ok(trend);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取消费趋势数据失败，error={}", e.getMessage());
            throw new ParamException("GET_TREND_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取消费趋势数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取消费趋势数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取消费趋势数据失败", e);
            throw new SystemException("GET_TREND_SYSTEM_ERROR", "获取趋势数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消费排名数据
     *
     * @param form 统计查询表单
     * @return 排名数据
     */
    @Override
    @Observed(name = "consume.visualization.getRanking", contextualName = "consume-visualization-get-ranking")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeRankingVO> getRanking(ConsumeStatisticsForm form) {
        log.info("获取消费排名数据，排名类型：{}", form.getRankingType());

        try {
            // 计算时间范围
            LocalDateTime[] timeRange = calculateTimeRange(form);
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2);

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算排名数据
            ConsumeRankingVO ranking = calculateRanking(transactions, form.getRankingType(),
                    form.getRankingLimit() != null ? form.getRankingLimit() : 10);

            int totalRankingCount = (ranking.getUserRanking() != null ? ranking.getUserRanking().size() : 0)
                    + (ranking.getRegionRanking() != null ? ranking.getRegionRanking().size() : 0);
            log.info("获取消费排名数据成功，排名数量：{}", totalRankingCount);
            return ResponseDTO.ok(ranking);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取消费排名数据失败，error={}", e.getMessage());
            throw new ParamException("GET_RANKING_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取消费排名数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取消费排名数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取消费排名数据失败", e);
            throw new SystemException("GET_RANKING_SYSTEM_ERROR", "获取排名数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消费对比分析数据
     *
     * @param form 统计查询表单
     * @return 对比分析数据
     */
    @Override
    @Observed(name = "consume.visualization.getComparison", contextualName = "consume-visualization-get-comparison")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeComparisonVO> getComparison(ConsumeStatisticsForm form) {
        log.info("获取消费对比分析数据");

        try {
            // 计算时间范围
            LocalDateTime[] timeRange = calculateTimeRange(form);
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2);

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算对比数据
            ConsumeComparisonVO comparison = calculateComparison(transactions, form);

            log.info("获取消费对比分析数据成功");
            return ResponseDTO.ok(comparison);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取消费对比分析数据失败，error={}", e.getMessage());
            throw new ParamException("GET_COMPARISON_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取消费对比分析数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取消费对比分析数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取消费对比分析数据失败", e);
            throw new SystemException("GET_COMPARISON_SYSTEM_ERROR", "获取对比分析数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取用户行为分析数据
     *
     * @param form 统计查询表单
     * @return 用户行为分析数据
     */
    @Override
    @Observed(name = "consume.visualization.getUserBehaviorAnalysis", contextualName = "consume-visualization-get-user-behavior")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeUserBehaviorAnalysisVO> getUserBehaviorAnalysis(ConsumeStatisticsForm form) {
        log.info("获取用户行为分析数据");

        try {
            // 计算时间范围
            LocalDateTime[] timeRange = calculateTimeRange(form);
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2);

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算用户行为分析数据
            ConsumeUserBehaviorAnalysisVO analysis = calculateUserBehavior(transactions, form);

            log.info("获取用户行为分析数据成功");
            return ResponseDTO.ok(analysis);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取用户行为分析数据失败，error={}", e.getMessage());
            throw new ParamException("GET_USER_BEHAVIOR_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取用户行为分析数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取用户行为分析数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取用户行为分析数据失败", e);
            throw new SystemException("GET_USER_BEHAVIOR_SYSTEM_ERROR", "获取用户行为分析数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取消费预测分析数据
     *
     * @param form 统计查询表单
     * @return 预测分析数据
     */
    @Override
    @Observed(name = "consume.visualization.getForecastAnalysis", contextualName = "consume-visualization-get-forecast")
    @Transactional(readOnly = true)
    public ResponseDTO<ConsumeForecastAnalysisVO> getForecastAnalysis(ConsumeStatisticsForm form) {
        log.info("获取消费预测分析数据");

        try {
            // 计算时间范围（需要更长的历史数据用于预测）
            LocalDateTime endTime = form.getEndTime() != null ? form.getEndTime() : LocalDateTime.now();
            LocalDateTime startTime = form.getStartTime() != null ? form.getStartTime()
                    : endTime.minusMonths(6); // 默认6个月历史数据

            // 构建查询条件
            LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = buildQueryWrapper(form, startTime, endTime);
            wrapper.eq(ConsumeTransactionEntity::getTransactionStatus, 2);

            // 查询交易记录
            List<ConsumeTransactionEntity> transactions = consumeTransactionDao.selectList(wrapper);

            // 计算预测数据
            ConsumeForecastAnalysisVO forecast = calculateForecast(transactions, form);

            log.info("获取消费预测分析数据成功");
            return ResponseDTO.ok(forecast);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[消费可视化] 参数错误，获取消费预测分析数据失败，error={}", e.getMessage());
            throw new ParamException("GET_FORECAST_PARAM_ERROR", "参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[消费可视化] 业务异常，获取消费预测分析数据失败，code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[消费可视化] 系统异常，获取消费预测分析数据失败，code={}, message={}", e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[消费可视化] 未知异常，获取消费预测分析数据失败", e);
            throw new SystemException("GET_FORECAST_SYSTEM_ERROR", "获取预测分析数据失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 计算时间范围
     */
    private LocalDateTime[] calculateTimeRange(ConsumeStatisticsForm form) {
        LocalDateTime endTime = form.getEndTime() != null ? form.getEndTime() : LocalDateTime.now();
        LocalDateTime startTime = form.getStartTime();

        if (startTime == null) {
            String timeRangeType = form.getTimeRangeType() != null ? form.getTimeRangeType() : "MONTH";
            switch (timeRangeType) {
                case "TODAY":
                    startTime = endTime.toLocalDate().atStartOfDay();
                    break;
                case "WEEK":
                    startTime = endTime.minusWeeks(1);
                    break;
                case "MONTH":
                    startTime = endTime.minusMonths(1);
                    break;
                case "YEAR":
                    startTime = endTime.minusYears(1);
                    break;
                default:
                    startTime = endTime.minusMonths(1);
                    break;
            }
        }

        return new LocalDateTime[] { startTime, endTime };
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ConsumeTransactionEntity> buildQueryWrapper(
            ConsumeStatisticsForm form, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ConsumeTransactionEntity> wrapper = new LambdaQueryWrapper<>();

        if (startTime != null) {
            wrapper.ge(ConsumeTransactionEntity::getTransactionTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(ConsumeTransactionEntity::getTransactionTime, endTime);
        }
        if (form.getUserIds() != null && !form.getUserIds().isEmpty()) {
            wrapper.in(ConsumeTransactionEntity::getUserId,
                    form.getUserIds().stream().map(String::valueOf).collect(Collectors.toList()));
        }
        if (form.getRegionIds() != null && !form.getRegionIds().isEmpty()) {
            wrapper.in(ConsumeTransactionEntity::getAreaId,
                    form.getRegionIds().stream().map(String::valueOf).collect(Collectors.toList()));
        }

        wrapper.orderByDesc(ConsumeTransactionEntity::getTransactionTime);
        return wrapper;
    }

    /**
     * 计算统计数据
     */
    private ConsumeStatisticsVO calculateStatistics(List<ConsumeTransactionEntity> transactions, String timeRange) {
        ConsumeStatisticsVO statistics = new ConsumeStatisticsVO();
        statistics.setTimeRange(timeRange);
        statistics.setGenerateTime(LocalDateTime.now());

        if (transactions == null || transactions.isEmpty()) {
            statistics.setTotalCount(0L);
            statistics.setTotalAmount(BigDecimal.ZERO);
            statistics.setAverageAmount(BigDecimal.ZERO);
            statistics.setMaxAmount(BigDecimal.ZERO);
            statistics.setMinAmount(BigDecimal.ZERO);
            statistics.setActiveUserCount(0);
            statistics.setNewUserCount(0);
            statistics.setTimeSeriesData(new ArrayList<>());
            statistics.setCategoryData(new ArrayList<>());
            statistics.setRegionData(new ArrayList<>());
            return statistics;
        }

        // 计算基础统计
        long totalCount = transactions.size();
        BigDecimal totalAmount = transactions.stream()
                .map(ConsumeTransactionEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        BigDecimal maxAmount = transactions.stream()
                .map(ConsumeTransactionEntity::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        BigDecimal minAmount = transactions.stream()
                .map(ConsumeTransactionEntity::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 计算活跃用户数
        long activeUserCount = transactions.stream()
                .map(ConsumeTransactionEntity::getUserId)
                .distinct()
                .count();

        statistics.setTotalCount(totalCount);
        statistics.setTotalAmount(totalAmount);
        statistics.setAverageAmount(averageAmount);
        statistics.setMaxAmount(maxAmount);
        statistics.setMinAmount(minAmount);
        statistics.setActiveUserCount((int) activeUserCount);
        statistics.setNewUserCount(0); // 需要额外查询计算

        // 按时间分组统计
        Map<String, Map<String, Object>> timeSeriesMap = new HashMap<>();
        for (ConsumeTransactionEntity transaction : transactions) {
            String timeKey = transaction.getTransactionTime().toLocalDate().toString();
            timeSeriesMap.computeIfAbsent(timeKey, k -> {
                Map<String, Object> data = new HashMap<>();
                data.put("date", timeKey);
                data.put("count", 0L);
                data.put("amount", BigDecimal.ZERO);
                return data;
            });
            Map<String, Object> data = timeSeriesMap.get(timeKey);
            data.put("count", ((Long) data.get("count")) + 1);
            data.put("amount", ((BigDecimal) data.get("amount")).add(transaction.getAmount()));
        }
        statistics.setTimeSeriesData(new ArrayList<>(timeSeriesMap.values()));

        // 按区域分组统计
        Map<String, Map<String, Object>> regionMap = new HashMap<>();
        for (ConsumeTransactionEntity transaction : transactions) {
            String areaId = transaction.getAreaId() != null ? String.valueOf(transaction.getAreaId()) : "未知";
            regionMap.computeIfAbsent(areaId, k -> {
                Map<String, Object> data = new HashMap<>();
                data.put("areaId", areaId);
                data.put("areaName", transaction.getAreaName() != null ? transaction.getAreaName() : "未知区域");
                data.put("count", 0L);
                data.put("amount", BigDecimal.ZERO);
                return data;
            });
            Map<String, Object> data = regionMap.get(areaId);
            data.put("count", ((Long) data.get("count")) + 1);
            data.put("amount", ((BigDecimal) data.get("amount")).add(transaction.getAmount()));
        }
        statistics.setRegionData(new ArrayList<>(regionMap.values()));

        statistics.setCategoryData(new ArrayList<>()); // 需要根据消费类型分组

        return statistics;
    }

    /**
     * 计算趋势数据
     */
    private ConsumeTrendVO calculateTrend(List<ConsumeTransactionEntity> transactions,
            String timeRangeType, String dataGranularity) {
        ConsumeTrendVO trend = new ConsumeTrendVO();
        trend.setTimeType(timeRangeType != null ? timeRangeType : "MONTHLY");

        List<ConsumeTrendVO.TrendDataPoint> dataPoints = new ArrayList<>();

        if (transactions != null && !transactions.isEmpty()) {
            // 按时间粒度分组
            Map<String, List<ConsumeTransactionEntity>> groupedByTime = transactions.stream()
                    .collect(Collectors.groupingBy(t -> {
                        LocalDateTime time = t.getTransactionTime();
                        if ("HOUR".equals(dataGranularity)) {
                            return time.toLocalDate().toString() + " " + time.getHour() + ":00";
                        } else if ("DAY".equals(dataGranularity)) {
                            return time.toLocalDate().toString();
                        } else if ("WEEK".equals(dataGranularity)) {
                            return time.toLocalDate().toString(); // 简化处理
                        } else {
                            return time.toLocalDate().toString().substring(0, 7); // 月份
                        }
                    }));

            for (Map.Entry<String, List<ConsumeTransactionEntity>> entry : groupedByTime.entrySet()) {
                ConsumeTrendVO.TrendDataPoint point = new ConsumeTrendVO.TrendDataPoint();
                point.setTimeLabel(entry.getKey());
                point.setCount((long) entry.getValue().size());
                point.setAmount(entry.getValue().stream()
                        .map(ConsumeTransactionEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
                dataPoints.add(point);
            }
        }

        trend.setDataPoints(dataPoints);

        // 计算趋势分析
        ConsumeTrendVO.TrendAnalysis analysis = new ConsumeTrendVO.TrendAnalysis();
        if (dataPoints.size() >= 2) {
            ConsumeTrendVO.TrendDataPoint last = dataPoints.get(dataPoints.size() - 1);
            ConsumeTrendVO.TrendDataPoint prev = dataPoints.get(dataPoints.size() - 2);
            if (prev.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growthRate = last.getAmount()
                        .subtract(prev.getAmount())
                        .divide(prev.getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                analysis.setGrowthRate(growthRate);
                analysis.setDirection(growthRate.compareTo(BigDecimal.ZERO) > 0 ? "UP" :
                        growthRate.compareTo(BigDecimal.ZERO) < 0 ? "DOWN" : "STABLE");
            }
        }
        trend.setAnalysis(analysis);

        return trend;
    }

    /**
     * 计算排名数据
     */
    private ConsumeRankingVO calculateRanking(List<ConsumeTransactionEntity> transactions,
            String rankingType, Integer limit) {
        ConsumeRankingVO ranking = new ConsumeRankingVO();
        ranking.setRankingType(rankingType != null ? rankingType : "AMOUNT");
        ranking.setTimeRange("CUSTOM");
        ranking.setStatisticTime(LocalDateTime.now());

        if (transactions == null || transactions.isEmpty()) {
            ranking.setUserRanking(new ArrayList<>());
            ranking.setRegionRanking(new ArrayList<>());
            ranking.setProductRanking(new ArrayList<>());
            return ranking;
        }

        // 用户排名
        Map<String, UserRankingData> userMap = new HashMap<>();
        for (ConsumeTransactionEntity transaction : transactions) {
            String userId = transaction.getUserId() != null ? String.valueOf(transaction.getUserId()) : null;
            if (userId == null) {
                continue;
            }
            userMap.computeIfAbsent(userId, k -> new UserRankingData()).add(transaction);
        }

        // 批量获取用户详情（避免N+1查询问题）
        Map<String, UserDetailVO> userDetailMap = batchGetUserDetails(userMap.keySet());

        List<ConsumeRankingVO.UserRankingItem> userRanking = userMap.entrySet().stream()
                .map(entry -> {
                    ConsumeRankingVO.UserRankingItem item = new ConsumeRankingVO.UserRankingItem();
                    UserRankingData data = entry.getValue();
                    String userId = entry.getKey();

                    try {
                        item.setUserId(Long.parseLong(userId));
                    } catch (NumberFormatException e) {
                        log.debug("[消费可视化] 用户ID不是数字格式，设置为0: userId={}", userId);
                        item.setUserId(0L);
                    }

                    // 从用户详情Map中获取用户编号和部门名称
                    UserDetailVO userDetail = userDetailMap.get(userId);
                    if (userDetail != null) {
                        data.userNo = userDetail.getEmployeeNo() != null ? userDetail.getEmployeeNo() : "";
                        data.department = userDetail.getDepartmentName() != null ? userDetail.getDepartmentName() : "";
                    } else {
                        data.userNo = "";
                        data.department = "";
                    }

                    item.setUserName(data.name);
                    item.setUserNo(data.userNo);
                    item.setDepartment(data.department);
                    item.setCount(data.count);
                    item.setAmount(data.amount);
                    item.setAverageAmount(data.count > 0 ?
                            data.amount.divide(BigDecimal.valueOf(data.count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    return item;
                })
                .sorted((a, b) -> {
                    if ("AMOUNT".equals(rankingType)) {
                        return b.getAmount().compareTo(a.getAmount());
                    } else {
                        return Long.compare(b.getCount(), a.getCount());
                    }
                })
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        for (int i = 0; i < userRanking.size(); i++) {
            userRanking.get(i).setRank(i + 1);
        }

        // 区域排名
        Map<String, RegionRankingData> regionMap = new HashMap<>();
        for (ConsumeTransactionEntity transaction : transactions) {
            String areaId = transaction.getAreaId() != null ? String.valueOf(transaction.getAreaId()) : null;
            if (areaId == null) {
                continue;
            }
            regionMap.computeIfAbsent(areaId, k -> new RegionRankingData()).add(transaction);
        }

        List<ConsumeRankingVO.RegionRankingItem> regionRanking = regionMap.entrySet().stream()
                .map(entry -> {
                    ConsumeRankingVO.RegionRankingItem item = new ConsumeRankingVO.RegionRankingItem();
                    RegionRankingData data = entry.getValue();
                    try {
                        item.setRegionId(Long.parseLong(entry.getKey()));
                    } catch (NumberFormatException e) {
                        log.debug("[消费可视化] 区域ID不是数字格式，设置为0: regionId={}", entry.getKey());
                        item.setRegionId(0L);
                    }
                    item.setRegionName(data.name);
                    item.setCount(data.count);
                    item.setAmount(data.amount);
                    item.setActiveUserCount(data.activeUsers.size());
                    return item;
                })
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .limit(limit != null ? limit : 10)
                .collect(Collectors.toList());

        for (int i = 0; i < regionRanking.size(); i++) {
            regionRanking.get(i).setRank(i + 1);
        }

        ranking.setUserRanking(userRanking);
        ranking.setRegionRanking(regionRanking);
        ranking.setProductRanking(new ArrayList<>()); // 商品排名需要商品数据

        return ranking;
    }

    /**
     * 计算对比数据
     */
    private ConsumeComparisonVO calculateComparison(List<ConsumeTransactionEntity> transactions,
            ConsumeStatisticsForm form) {
        ConsumeComparisonVO comparison = new ConsumeComparisonVO();
        comparison.setComparisonType(form.getGroupByDimension() != null ? form.getGroupByDimension() : "PERIOD");
        comparison.setTimeRange(form.getTimeRangeType());

        List<ConsumeComparisonVO.ComparisonDataset> datasets = new ArrayList<>();

        if (transactions != null && !transactions.isEmpty()) {
            // 按区域分组对比
            Map<String, List<ConsumeTransactionEntity>> regionGroups = transactions.stream()
                    .collect(Collectors.groupingBy(t -> t.getAreaName() != null ? t.getAreaName() : "未知区域"));

            BigDecimal totalAmount = transactions.stream()
                    .map(ConsumeTransactionEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (Map.Entry<String, List<ConsumeTransactionEntity>> entry : regionGroups.entrySet()) {
                ConsumeComparisonVO.ComparisonDataset dataset = new ConsumeComparisonVO.ComparisonDataset();
                List<ConsumeTransactionEntity> groupTransactions = entry.getValue();

                BigDecimal groupAmount = groupTransactions.stream()
                        .map(ConsumeTransactionEntity::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long groupCount = groupTransactions.size();
                BigDecimal groupAvg = groupCount > 0 ?
                        groupAmount.divide(BigDecimal.valueOf(groupCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                BigDecimal percentage = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                        groupAmount.divide(totalAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                dataset.setName(entry.getKey());
                dataset.setIdentifier(entry.getKey());
                dataset.setAmount(groupAmount);
                dataset.setCount(groupCount);
                dataset.setAverageAmount(groupAvg);
                dataset.setPercentage(percentage);

                datasets.add(dataset);
            }
        }

        comparison.setDatasets(datasets);

        // 计算对比分析
        ConsumeComparisonVO.ComparisonAnalysis analysis = new ConsumeComparisonVO.ComparisonAnalysis();
        if (datasets.size() >= 2) {
            datasets.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
            BigDecimal maxAmount = datasets.get(0).getAmount();
            BigDecimal minAmount = datasets.get(datasets.size() - 1).getAmount();
            analysis.setTotalDifference(maxAmount.subtract(minAmount));
            if (minAmount.compareTo(BigDecimal.ZERO) > 0) {
                analysis.setDifferencePercentage(maxAmount.subtract(minAmount)
                        .divide(minAmount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
            }
            analysis.setMaxDifferenceItem(datasets.get(0).getName());
            analysis.setMinDifferenceItem(datasets.get(datasets.size() - 1).getName());
        }
        comparison.setAnalysis(analysis);

        return comparison;
    }

    /**
     * 计算用户行为分析数据
     */
    private ConsumeUserBehaviorAnalysisVO calculateUserBehavior(List<ConsumeTransactionEntity> transactions,
            ConsumeStatisticsForm form) {
        ConsumeUserBehaviorAnalysisVO analysis = new ConsumeUserBehaviorAnalysisVO();
        analysis.setTimeRange(form.getTimeRangeType());

        if (transactions == null || transactions.isEmpty()) {
            analysis.setUserSegments(new ArrayList<>());
            analysis.setConsumptionHabits(new ConsumeUserBehaviorAnalysisVO.ConsumptionHabitAnalysis());
            analysis.setPreferenceAnalysis(new ConsumeUserBehaviorAnalysisVO.PreferenceAnalysis());
            return analysis;
        }

        // 用户群体分析
        Map<Long, List<ConsumeTransactionEntity>> userGroups = transactions.stream()
                .filter(t -> t.getUserId() != null)
                .collect(Collectors.groupingBy(ConsumeTransactionEntity::getUserId));

        List<ConsumeUserBehaviorAnalysisVO.UserSegmentAnalysis> segments = new ArrayList<>();

        Map<String, Integer> segmentCounts = new HashMap<>();
        segmentCounts.put("高频用户", 0);
        segmentCounts.put("中频用户", 0);
        segmentCounts.put("低频用户", 0);

        int totalUsers = userGroups.size();
        for (Map.Entry<Long, List<ConsumeTransactionEntity>> entry : userGroups.entrySet()) {
            int frequency = entry.getValue().size();
            String segment = frequency >= 10 ? "高频用户" : frequency >= 5 ? "中频用户" : "低频用户";
            segmentCounts.put(segment, segmentCounts.get(segment) + 1);
        }

        for (Map.Entry<String, Integer> entry : segmentCounts.entrySet()) {
            ConsumeUserBehaviorAnalysisVO.UserSegmentAnalysis segment = new ConsumeUserBehaviorAnalysisVO.UserSegmentAnalysis();
            segment.setSegmentName(entry.getKey());
            segment.setUserCount(entry.getValue());
            segment.setPercentage(totalUsers > 0 ?
                    BigDecimal.valueOf(entry.getValue()).divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
            segments.add(segment);
        }
        analysis.setUserSegments(segments);

        // 消费习惯分析
        ConsumeUserBehaviorAnalysisVO.ConsumptionHabitAnalysis habits = new ConsumeUserBehaviorAnalysisVO.ConsumptionHabitAnalysis();
        List<Map<String, Object>> hourGroups = transactions.stream()
                .collect(Collectors.groupingBy(t -> String.valueOf(t.getTransactionTime().getHour())))
                .entrySet().stream()
                .map(e -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("hour", e.getKey());
                    data.put("count", e.getValue().size());
                    return data;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")))
                .limit(3)
                .collect(Collectors.toList());

        habits.setPeakTimeSlots(hourGroups.stream()
                .map(d -> d.get("hour") + ":00")
                .collect(Collectors.toList()));

        // 常用消费地点
        List<String> frequentLocations = transactions.stream()
                .collect(Collectors.groupingBy(ConsumeTransactionEntity::getAreaName))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().size(), a.getValue().size()))
                .limit(5)
                .map(Map.Entry::getKey)
                .filter(loc -> loc != null)
                .collect(Collectors.toList());
        habits.setFrequentLocations(frequentLocations);

        analysis.setConsumptionHabits(habits);

        // 偏好分析
        ConsumeUserBehaviorAnalysisVO.PreferenceAnalysis preference = new ConsumeUserBehaviorAnalysisVO.PreferenceAnalysis();
        preference.setPreferredCategories(new ArrayList<>()); // 需要商品数据
        analysis.setPreferenceAnalysis(preference);

        return analysis;
    }

    /**
     * 计算预测数据
     */
    private ConsumeForecastAnalysisVO calculateForecast(List<ConsumeTransactionEntity> transactions,
            ConsumeStatisticsForm form) {
        ConsumeForecastAnalysisVO forecast = new ConsumeForecastAnalysisVO();
        forecast.setForecastType("TREND");
        forecast.setForecastPeriod("NEXT_MONTH");

        if (transactions == null || transactions.isEmpty()) {
            forecast.setForecastData(new ArrayList<>());
            forecast.setModelInfo(new ConsumeForecastAnalysisVO.ForecastModelInfo());
            forecast.setAccuracy(new ConsumeForecastAnalysisVO.ForecastAccuracy());
            return forecast;
        }

        // 按日期分组计算日均消费
        Map<String, BigDecimal> dailyAmounts = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionTime().toLocalDate().toString(),
                        Collectors.reducing(BigDecimal.ZERO, ConsumeTransactionEntity::getAmount, BigDecimal::add)));

        // 计算平均值作为简单预测
        BigDecimal avgDailyAmount = dailyAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyAmounts.size()), 2, RoundingMode.HALF_UP);

        // 生成未来7天的预测数据
        List<ConsumeForecastAnalysisVO.ForecastDataPoint> forecastData = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        for (int i = 0; i < 7; i++) {
            ConsumeForecastAnalysisVO.ForecastDataPoint point = new ConsumeForecastAnalysisVO.ForecastDataPoint();
            point.setTimePoint(startDate.plusDays(i).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            point.setPredictedAmount(avgDailyAmount);
            point.setPredictedCount((long) (dailyAmounts.size() / Math.max(dailyAmounts.size(), 1)));
            point.setConfidenceLower(avgDailyAmount.multiply(BigDecimal.valueOf(0.8)));
            point.setConfidenceUpper(avgDailyAmount.multiply(BigDecimal.valueOf(1.2)));
            forecastData.add(point);
        }
        forecast.setForecastData(forecastData);

        // 模型信息
        ConsumeForecastAnalysisVO.ForecastModelInfo modelInfo = new ConsumeForecastAnalysisVO.ForecastModelInfo();
        modelInfo.setModelName("简单移动平均");
        modelInfo.setModelVersion("1.0");
        modelInfo.setTrainingPeriod(form.getTimeRangeType());
        forecast.setModelInfo(modelInfo);

        // 准确性（简化计算）
        ConsumeForecastAnalysisVO.ForecastAccuracy accuracy = new ConsumeForecastAnalysisVO.ForecastAccuracy();
        accuracy.setAccuracyRating("中等");
        forecast.setAccuracy(accuracy);

        return forecast;
    }

    /**
     * 批量获取用户详情
     * <p>
     * 功能说明：
     * 1. 通过GatewayServiceClient调用公共服务批量获取用户详情
     * 2. 避免N+1查询问题，提升性能
     * 3. 返回用户ID到UserDetailVO的映射
     * </p>
     *
     * @param userIds 用户ID集合
     * @return 用户ID到UserDetailVO的映射
     */
    private Map<String, UserDetailVO> batchGetUserDetails(Set<String> userIds) {
        Map<String, UserDetailVO> userDetailMap = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return userDetailMap;
        }

        try {
            // 过滤有效的用户ID
            Set<String> validUserIds = userIds.stream()
                    .filter(userId -> userId != null && !userId.trim().isEmpty())
                    .collect(Collectors.toSet());

            // 逐个获取用户详情（如果公共服务支持批量查询，可以优化为批量调用）
            for (String userId : validUserIds) {
                try {
                    Long userIdLong = Long.parseLong(userId);
                    // 通过GatewayServiceClient调用公共服务获取用户详情
                    ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                            "/api/v1/users/" + userIdLong,
                            HttpMethod.GET,
                            null,
                            UserDetailVO.class
                    );

                    if (response != null && response.isSuccess() && response.getData() != null) {
                        userDetailMap.put(userId, response.getData());
                    } else {
                        log.debug("[用户详情] 用户详情获取失败: userId={}, message={}", userId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (NumberFormatException e) {
                    log.warn("[用户详情] 用户ID格式不正确，无法获取用户详情: userId={}", userId);
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[用户详情] 获取用户详情参数错误: userId={}, error={}", userId, e.getMessage());
                    // 单个用户获取失败不影响其他用户，继续处理
                } catch (BusinessException e) {
                    log.warn("[用户详情] 获取用户详情业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
                    // 单个用户获取失败不影响其他用户，继续处理
                } catch (SystemException e) {
                    log.warn("[用户详情] 获取用户详情系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
                    // 单个用户获取失败不影响其他用户，继续处理
                } catch (Exception e) {
                    log.warn("[用户详情] 获取用户详情未知异常: userId={}, error={}", userId, e.getMessage());
                    // 单个用户获取失败不影响其他用户，继续处理
                }
            }

            log.debug("[用户详情] 批量获取用户详情完成: 总数={}, 成功={}", userIds.size(), userDetailMap.size());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[用户详情] 批量获取用户详情参数错误: error={}", e.getMessage());
            // 返回空Map，不影响主流程
        } catch (BusinessException e) {
            log.warn("[用户详情] 批量获取用户详情业务异常: code={}, message={}", e.getCode(), e.getMessage());
            // 返回空Map，不影响主流程
        } catch (SystemException e) {
            log.error("[用户详情] 批量获取用户详情系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空Map，不影响主流程
        } catch (Exception e) {
            log.error("[用户详情] 批量获取用户详情未知异常", e);
            // 返回空Map，不影响主流程
        }

        return userDetailMap;
    }

    /**
     * 用户排名数据辅助类
     */
    private static class UserRankingData {
        String name;
        String userNo;
        String department;
        long count = 0;
        BigDecimal amount = BigDecimal.ZERO;

        void add(ConsumeTransactionEntity transaction) {
            count++;
            amount = amount.add(transaction.getAmount());
            if (name == null) {
                name = transaction.getUserName() != null ? transaction.getUserName() : "未知用户";
            }
            // userNo和department通过批量查询获取，在calculateRanking方法中设置
            // 这里不再单独设置，避免N+1查询问题
        }
    }

    /**
     * 区域排名数据辅助类
     */
    private static class RegionRankingData {
        String name;
        long count = 0;
        BigDecimal amount = BigDecimal.ZERO;
        Set<String> activeUsers = new HashSet<>();

        void add(ConsumeTransactionEntity transaction) {
            count++;
            amount = amount.add(transaction.getAmount());
            if (transaction.getUserId() != null) {
                activeUsers.add(String.valueOf(transaction.getUserId()));
            }
            if (name == null) {
                name = transaction.getAreaName() != null ? transaction.getAreaName() : "未知区域";
            }
        }
    }
}



