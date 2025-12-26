package net.lab1024.sa.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.ConsumptionAnalysisQueryForm;
import net.lab1024.sa.consume.domain.vo.*;
import net.lab1024.sa.consume.manager.ConsumeAnalysisManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消费分析服务测试
 * <p>
 * 测试消费分析服务的核心功能：
 * - 消费总览分析
 * - 消费趋势分析
 * - 分类统计分析
 * - 消费习惯分析
 * - 智能推荐生成
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@DisplayName("消费分析服务测试")
@ExtendWith(MockitoExtension.class)
class ConsumeAnalysisServiceImplTest {

    @Mock
    private ConsumeAnalysisManager analysisManager;

    @InjectMocks
    private ConsumeAnalysisServiceImpl analysisService;

    @Test
    @DisplayName("测试获取消费总览分析")
    void testGetConsumptionAnalysis() {
        log.info("[测试] 开始测试获取消费总览分析");

        // Given: 准备测试数据
        Long userId = 1L;
        String period = "week";
        ConsumptionAnalysisQueryForm queryForm = new ConsumptionAnalysisQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPeriod(period);

        // Mock时间范围
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(analysisManager.calculateTimeRange(period))
                .thenReturn(new LocalDateTime[]{startTime, endTime});

        // Mock消费总览数据
        Object[] overview = new Object[]{
                new BigDecimal("500.50"),  // totalAmount
                15,                        // totalCount
                5,                         // consumeDays
                new BigDecimal("33.37"),   // averagePerOrder
                new BigDecimal("100.10")   // dailyAverage
        };
        when(analysisManager.getConsumptionOverview(userId, startTime, endTime))
                .thenReturn(overview);

        // Mock趋势数据
        List<Map<String, Object>> trendData = createMockTrendData();
        when(analysisManager.getTrendData(userId, startTime, endTime))
                .thenReturn(trendData);

        // Mock分类统计数据
        List<Map<String, Object>> categoryStats = createMockCategoryStats();
        when(analysisManager.getCategoryStats(userId, startTime, endTime))
                .thenReturn(categoryStats);
        when(analysisManager.calculateCategoryPercents(eq(categoryStats), any(BigDecimal.class)))
                .thenReturn(Arrays.asList(60, 40));

        // Mock消费习惯分析
        when(analysisManager.analyzeMostFrequentTime(userId, startTime, endTime))
                .thenReturn("午餐时段");
        when(analysisManager.analyzeFavoriteCategory(userId, startTime, endTime))
                .thenReturn("中餐");

        // When: 执行测试
        ConsumptionAnalysisVO result = analysisService.getConsumptionAnalysis(queryForm);

        // Then: 验证结果
        assertNotNull(result, "结果不应为空");
        assertEquals(new BigDecimal("500.50"), result.getTotalAmount());
        assertEquals(15, result.getTotalCount());
        assertEquals(5, result.getConsumeDays());
        assertEquals(new BigDecimal("33.37"), result.getAveragePerOrder());
        assertEquals(new BigDecimal("100.10"), result.getDailyAverage());
        assertNotNull(result.getTrend());
        assertNotNull(result.getCategories());
        assertEquals("午餐时段", result.getMostFrequentTime());
        assertEquals("中餐", result.getFavoriteCategory());

        // 验证方法调用
        verify(analysisManager, times(1)).calculateTimeRange(period);
        verify(analysisManager, times(1)).getConsumptionOverview(userId, startTime, endTime);
        verify(analysisManager, times(1)).getTrendData(userId, startTime, endTime);
        verify(analysisManager, times(1)).getCategoryStats(userId, startTime, endTime);

        log.info("[测试] 消费总览分析测试通过");
    }

    @Test
    @DisplayName("测试获取消费趋势")
    void testGetConsumptionTrend() {
        log.info("[测试] 开始测试获取消费趋势");

        // Given
        Long userId = 1L;
        String period = "week";
        ConsumptionAnalysisQueryForm queryForm = new ConsumptionAnalysisQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPeriod(period);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(analysisManager.calculateTimeRange(period))
                .thenReturn(new LocalDateTime[]{startTime, endTime});

        List<Map<String, Object>> trendData = createMockTrendData();
        when(analysisManager.getTrendData(userId, startTime, endTime))
                .thenReturn(trendData);

        // When
        List<ConsumptionTrendVO> result = analysisService.getConsumptionTrend(queryForm);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(7, result.size());

        // 验证第一个数据点
        ConsumptionTrendVO firstDay = result.get(0);
        assertNotNull(firstDay.getDate());
        assertNotNull(firstDay.getAmount());
        assertNotNull(firstDay.getCount());
        assertNotNull(firstDay.getDateLabel());

        log.info("[测试] 消费趋势测试通过");
    }

    @Test
    @DisplayName("测试获取分类统计")
    void testGetCategoryStats() {
        log.info("[测试] 开始测试获取分类统计");

        // Given
        Long userId = 1L;
        String period = "week";
        ConsumptionAnalysisQueryForm queryForm = new ConsumptionAnalysisQueryForm();
        queryForm.setUserId(userId);
        queryForm.setPeriod(period);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(analysisManager.calculateTimeRange(period))
                .thenReturn(new LocalDateTime[]{startTime, endTime});

        List<Map<String, Object>> categoryStats = createMockCategoryStats();
        when(analysisManager.getCategoryStats(userId, startTime, endTime))
                .thenReturn(categoryStats);
        when(analysisManager.calculateCategoryPercents(eq(categoryStats), any(BigDecimal.class)))
                .thenReturn(Arrays.asList(60, 40));

        // When
        List<CategoryStatsVO> result = analysisService.getCategoryStats(queryForm);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        CategoryStatsVO firstCategory = result.get(0);
        assertNotNull(firstCategory.getCategoryName());
        assertNotNull(firstCategory.getAmount());
        assertNotNull(firstCategory.getCount());
        assertNotNull(firstCategory.getPercent());
        assertNotNull(firstCategory.getIcon());

        log.info("[测试] 分类统计测试通过");
    }

    @Test
    @DisplayName("测试获取消费习惯")
    void testGetConsumptionHabits() {
        log.info("[测试] 开始测试获取消费习惯");

        // Given
        Long userId = 1L;
        String period = "week";

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(analysisManager.calculateTimeRange(period))
                .thenReturn(new LocalDateTime[]{startTime, endTime});

        Object[] overview = new Object[]{
                new BigDecimal("500.50"),
                15,
                5,
                new BigDecimal("33.37")
        };
        when(analysisManager.getConsumptionOverview(userId, startTime, endTime))
                .thenReturn(overview);

        when(analysisManager.analyzeMostFrequentTime(userId, startTime, endTime))
                .thenReturn("午餐时段");
        when(analysisManager.analyzeFavoriteCategory(userId, startTime, endTime))
                .thenReturn("中餐");
        when(analysisManager.isHighFrequencyUser(15))
                .thenReturn(true);
        when(analysisManager.isHighValueUser(new BigDecimal("33.37")))
                .thenReturn(false);

        // When
        ConsumptionHabitsVO result = analysisService.getConsumptionHabits(userId, period);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("午餐时段", result.getMostFrequentTime());
        assertEquals("中餐", result.getFavoriteCategory());
        assertEquals(new BigDecimal("33.37"), result.getAveragePerOrder());
        assertEquals(15, result.getTotalCount());
        assertEquals(5, result.getConsumeDays());
        assertTrue(result.getIsHighFrequencyUser());
        assertFalse(result.getIsHighValueUser());

        log.info("[测试] 消费习惯测试通过");
    }

    @Test
    @DisplayName("测试获取智能推荐")
    void testGetSmartRecommendations() {
        log.info("[测试] 开始测试获取智能推荐");

        // Given
        Long userId = 1L;
        String period = "week";

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(analysisManager.calculateTimeRange(period))
                .thenReturn(new LocalDateTime[]{startTime, endTime});

        Object[] overview = new Object[]{
                new BigDecimal("500.50"),
                15,
                5,
                new BigDecimal("33.37")
        };
        when(analysisManager.getConsumptionOverview(userId, startTime, endTime))
                .thenReturn(overview);

        when(analysisManager.analyzeMostFrequentTime(userId, startTime, endTime))
                .thenReturn("午餐时段");
        when(analysisManager.analyzeFavoriteCategory(userId, startTime, endTime))
                .thenReturn("中餐");

        List<Map<String, String>> recommendations = new ArrayList<>();
        Map<String, String> rec1 = new HashMap<>();
        rec1.put("action", "save_money");
        rec1.put("icon", "💰");
        rec1.put("title", "节省开支");
        rec1.put("description", "本周消费偏高");
        rec1.put("priority", "1");
        rec1.put("reason", "超过预算");

        Map<String, String> rec2 = new HashMap<>();
        rec2.put("action", "try_new");
        rec2.put("icon", "🍽️");
        rec2.put("title", "尝试新品");
        rec2.put("description", "发现新美食");
        rec2.put("priority", "2");
        rec2.put("reason", "丰富选择");

        recommendations.add(rec1);
        recommendations.add(rec2);

        when(analysisManager.generateRecommendations(
                eq(userId), any(BigDecimal.class), anyInt(), any(BigDecimal.class), anyString(), anyString()
        )).thenReturn(recommendations);

        // When
        List<SmartRecommendationVO> result = analysisService.getSmartRecommendations(userId, period);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        SmartRecommendationVO firstRec = result.get(0);
        assertEquals("save_money", firstRec.getRecommendType());
        assertEquals("💰", firstRec.getIcon());
        assertEquals("节省开支", firstRec.getTitle());
        assertEquals(1, firstRec.getPriority());
        assertTrue(firstRec.getActionable());

        log.info("[测试] 智能推荐测试通过");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟趋势数据
     */
    private List<Map<String, Object>> createMockTrendData() {
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 6; i >= 0; i--) {
            Map<String, Object> data = new HashMap<>();
            data.put("date", now.minusDays(i).toLocalDate());
            data.put("amount", new BigDecimal(50 + i * 10));
            data.put("count", 2 + i);
            trendData.add(data);
        }

        return trendData;
    }

    /**
     * 创建模拟分类统计数据
     */
    private List<Map<String, Object>> createMockCategoryStats() {
        List<Map<String, Object>> categoryStats = new ArrayList<>();

        Map<String, Object> category1 = new HashMap<>();
        category1.put("categoryId", 1L);
        category1.put("categoryName", "中餐");
        category1.put("amount", new BigDecimal("300.30"));
        category1.put("count", 10);
        categoryStats.add(category1);

        Map<String, Object> category2 = new HashMap<>();
        category2.put("categoryId", 2L);
        category2.put("categoryName", "西餐");
        category2.put("amount", new BigDecimal("200.20"));
        category2.put("count", 5);
        categoryStats.add(category2);

        return categoryStats;
    }
}
