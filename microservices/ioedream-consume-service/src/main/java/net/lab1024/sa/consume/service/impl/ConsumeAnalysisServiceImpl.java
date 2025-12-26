package net.lab1024.sa.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.manager.ConsumeAnalysisManager;
import net.lab1024.sa.consume.service.ConsumeAnalysisService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 消费分析服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Service
public class ConsumeAnalysisServiceImpl implements ConsumeAnalysisService {

    private final ConsumeAnalysisManager analysisManager;

    public ConsumeAnalysisServiceImpl(ConsumeAnalysisManager analysisManager) {
        this.analysisManager = analysisManager;
    }

    @Override
    @Cacheable(value = "consume:analysis", key = "#queryForm.userId + ':' + #queryForm.period", unless = "#result == null")
    public ConsumptionAnalysisVO getConsumptionAnalysis(ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询消费分析: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());

        Long userId = queryForm.getUserId();
        String period = queryForm.getPeriod();

        // 计算时间范围
        LocalDateTime[] timeRange = analysisManager.calculateTimeRange(period);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        log.info("[消费分析] 时间范围: {} - {}", startTime, endTime);

        // 获取消费总览数据
        Object[] overview = analysisManager.getConsumptionOverview(userId, startTime, endTime);
        BigDecimal totalAmount = (BigDecimal) overview[0];
        Integer totalCount = (Integer) overview[1];
        Integer consumeDays = (Integer) overview[2];
        BigDecimal averagePerOrder = (BigDecimal) overview[3];
        BigDecimal dailyAverage = (BigDecimal) overview[4];

        // 获取趋势数据
        List<Map<String, Object>> trendDataList = analysisManager.getTrendData(userId, startTime, endTime);
        List<BigDecimal> trend = new ArrayList<>();
        for (Map<String, Object> data : trendDataList) {
            BigDecimal amount = (BigDecimal) data.get("amount");
            trend.add(amount);
        }

        // 获取分类统计
        List<Map<String, Object>> categoryStatsList = analysisManager.getCategoryStats(userId, startTime, endTime);
        List<Integer> percents = analysisManager.calculateCategoryPercents(categoryStatsList, totalAmount);

        List<ConsumptionAnalysisVO.CategoryConsumptionVO> categories = new ArrayList<>();
        String[] icons = {"🍚", "🍜", "🥐", "🍰"};
        int iconIndex = 0;
        for (int i = 0; i < categoryStatsList.size(); i++) {
            Map<String, Object> stat = categoryStatsList.get(i);
            ConsumptionAnalysisVO.CategoryConsumptionVO category = new ConsumptionAnalysisVO.CategoryConsumptionVO();
            category.setName((String) stat.get("categoryName"));
            category.setAmount((BigDecimal) stat.get("amount"));
            category.setPercent(percents.get(i));
            category.setIcon(icons[i % icons.length]);
            categories.add(category);
        }

        // 分析消费习惯
        String mostFrequentTime = analysisManager.analyzeMostFrequentTime(userId, startTime, endTime);
        String favoriteCategory = analysisManager.analyzeFavoriteCategory(userId, startTime, endTime);

        // 组装结果
        ConsumptionAnalysisVO analysisVO = new ConsumptionAnalysisVO();
        analysisVO.setTotalAmount(totalAmount);
        analysisVO.setTotalCount(totalCount);
        analysisVO.setConsumeDays(consumeDays);
        analysisVO.setAveragePerOrder(averagePerOrder);
        analysisVO.setDailyAverage(dailyAverage);
        analysisVO.setTrend(trend);
        analysisVO.setCategories(categories);
        analysisVO.setMostFrequentTime(mostFrequentTime);
        analysisVO.setFavoriteCategory(favoriteCategory);

        log.info("[消费分析] 查询完成: totalAmount={}, totalCount={}, dailyAverage={}",
                totalAmount, totalCount, dailyAverage);

        return analysisVO;
    }

    @Override
    @Cacheable(value = "consume:trend", key = "#queryForm.userId + ':' + #queryForm.period", unless = "#result == null || #result.isEmpty()")
    public List<ConsumptionTrendVO> getConsumptionTrend(ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询消费趋势: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());

        Long userId = queryForm.getUserId();
        String period = queryForm.getPeriod();

        // 计算时间范围
        LocalDateTime[] timeRange = analysisManager.calculateTimeRange(period);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 获取趋势数据
        List<Map<String, Object>> trendDataList = analysisManager.getTrendData(userId, startTime, endTime);

        // 转换为VO
        List<ConsumptionTrendVO> trendVOList = new ArrayList<>();
        for (Map<String, Object> data : trendDataList) {
            ConsumptionTrendVO trendVO = new ConsumptionTrendVO();
            trendVO.setDate((LocalDate) data.get("date"));
            trendVO.setAmount((BigDecimal) data.get("amount"));
            trendVO.setCount((Integer) data.get("count"));

            // 生成日期标签
            LocalDate date = trendVO.getDate();
            String dateLabel = String.format("%d月%d日", date.getMonthValue(), date.getDayOfMonth());
            trendVO.setDateLabel(dateLabel);

            trendVOList.add(trendVO);
        }

        log.info("[消费分析] 趋势数据查询完成: size={}", trendVOList.size());
        return trendVOList;
    }

    @Override
    @Cacheable(value = "consume:category", key = "#queryForm.userId + ':' + #queryForm.period", unless = "#result == null || #result.isEmpty()")
    public List<CategoryStatsVO> getCategoryStats(ConsumptionAnalysisQueryForm queryForm) {
        log.info("[消费分析] 查询分类统计: userId={}, period={}", queryForm.getUserId(), queryForm.getPeriod());

        Long userId = queryForm.getUserId();
        String period = queryForm.getPeriod();

        // 计算时间范围
        LocalDateTime[] timeRange = analysisManager.calculateTimeRange(period);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 获取分类统计
        List<Map<String, Object>> categoryStatsList = analysisManager.getCategoryStats(userId, startTime, endTime);

        // 获取总金额用于计算占比
        BigDecimal totalAmount = categoryStatsList.stream()
                .map(stat -> (BigDecimal) stat.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Integer> percents = analysisManager.calculateCategoryPercents(categoryStatsList, totalAmount);

        // 转换为VO
        List<CategoryStatsVO> categoryVOList = new ArrayList<>();
        String[] icons = {"🍚", "🍜", "🥐", "🍰"};
        int iconIndex = 0;

        for (int i = 0; i < categoryStatsList.size(); i++) {
            Map<String, Object> stat = categoryStatsList.get(i);
            CategoryStatsVO categoryVO = new CategoryStatsVO();
            categoryVO.setCategoryId(getLong(stat, "categoryId"));
            categoryVO.setCategoryName((String) stat.get("categoryName"));
            categoryVO.setAmount((BigDecimal) stat.get("amount"));
            categoryVO.setCount((Integer) stat.get("count"));
            categoryVO.setPercent(BigDecimal.valueOf(percents.get(i)));
            categoryVO.setIcon(icons[i % icons.length]);
            categoryVO.setSortFlag(i + 1);

            categoryVOList.add(categoryVO);
        }

        log.info("[消费分析] 分类统计查询完成: size={}", categoryVOList.size());
        return categoryVOList;
    }

    @Override
    @Cacheable(value = "consume:habits", key = "#userId + ':' + #period", unless = "#result == null")
    public ConsumptionHabitsVO getConsumptionHabits(Long userId, String period) {
        log.info("[消费分析] 查询消费习惯: userId={}, period={}", userId, period);

        // 计算时间范围
        LocalDateTime[] timeRange = analysisManager.calculateTimeRange(period);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 获取消费总览
        Object[] overview = analysisManager.getConsumptionOverview(userId, startTime, endTime);
        BigDecimal totalAmount = (BigDecimal) overview[0];
        Integer totalCount = (Integer) overview[1];
        Integer consumeDays = (Integer) overview[2];
        BigDecimal averagePerOrder = (BigDecimal) overview[3];

        // 获取最大最小单笔消费
        BigDecimal maxOrderAmount = analysisManager.isHighValueUser(averagePerOrder)
                ? averagePerOrder : BigDecimal.ZERO;
        BigDecimal minOrderAmount = BigDecimal.ZERO;

        // 分析消费习惯
        String mostFrequentTime = analysisManager.analyzeMostFrequentTime(userId, startTime, endTime);
        String favoriteCategory = analysisManager.analyzeFavoriteCategory(userId, startTime, endTime);

        // 计算平均每日消费次数
        BigDecimal averageDailyCount = BigDecimal.ZERO;
        if (consumeDays != null && consumeDays > 0) {
            averageDailyCount = BigDecimal.valueOf(totalCount)
                    .divide(BigDecimal.valueOf(consumeDays), 2, BigDecimal.ROUND_HALF_UP);
        }

        // 组装结果
        ConsumptionHabitsVO habitsVO = new ConsumptionHabitsVO();
        habitsVO.setUserId(userId);
        habitsVO.setMostFrequentTime(mostFrequentTime);
        habitsVO.setFavoriteCategory(favoriteCategory);
        habitsVO.setAveragePerOrder(averagePerOrder);
        habitsVO.setTotalCount(totalCount);
        habitsVO.setConsumeDays(consumeDays);
        habitsVO.setAverageDailyCount(averageDailyCount);
        habitsVO.setMaxOrderAmount(maxOrderAmount);
        habitsVO.setMinOrderAmount(minOrderAmount);
        habitsVO.setIsHighFrequencyUser(analysisManager.isHighFrequencyUser(totalCount));
        habitsVO.setIsHighValueUser(analysisManager.isHighValueUser(averagePerOrder));

        log.info("[消费分析] 消费习惯查询完成: mostFrequentTime={}, favoriteCategory={}",
                mostFrequentTime, favoriteCategory);

        return habitsVO;
    }

    @Override
    @Cacheable(value = "consume:recommendations", key = "#userId + ':' + #period", unless = "#result == null || #result.isEmpty()")
    public List<SmartRecommendationVO> getSmartRecommendations(Long userId, String period) {
        log.info("[消费分析] 生成智能推荐: userId={}, period={}", userId, period);

        // 计算时间范围
        LocalDateTime[] timeRange = analysisManager.calculateTimeRange(period);
        LocalDateTime startTime = timeRange[0];
        LocalDateTime endTime = timeRange[1];

        // 获取消费总览
        Object[] overview = analysisManager.getConsumptionOverview(userId, startTime, endTime);
        BigDecimal totalAmount = (BigDecimal) overview[0];
        Integer totalCount = (Integer) overview[1];
        BigDecimal averagePerOrder = (BigDecimal) overview[3];

        // 分析消费习惯
        String mostFrequentTime = analysisManager.analyzeMostFrequentTime(userId, startTime, endTime);
        String favoriteCategory = analysisManager.analyzeFavoriteCategory(userId, startTime, endTime);

        // 生成推荐
        List<Map<String, String>> recommendations = analysisManager.generateRecommendations(
                userId, totalAmount, totalCount, averagePerOrder, favoriteCategory, mostFrequentTime
        );

        // 转换为VO
        List<SmartRecommendationVO> recommendationVOList = new ArrayList<>();
        for (Map<String, String> recommend : recommendations) {
            SmartRecommendationVO recommendationVO = new SmartRecommendationVO();
            recommendationVO.setRecommendType(recommend.get("action"));
            recommendationVO.setIcon(recommend.get("icon"));
            recommendationVO.setTitle(recommend.get("title"));
            recommendationVO.setDescription(recommend.get("description"));
            recommendationVO.setAction(recommend.get("action"));
            recommendationVO.setPriority(Integer.parseInt(recommend.get("priority")));
            recommendationVO.setReason(recommend.get("reason"));
            recommendationVO.setActionable(true);

            recommendationVOList.add(recommendationVO);
        }

        log.info("[消费分析] 智能推荐生成完成: size={}", recommendationVOList.size());
        return recommendationVOList;
    }

    // ==================== 辅助方法 ====================

    /**
     * 从Map中获取Long值
     */
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        return null;
    }
}
