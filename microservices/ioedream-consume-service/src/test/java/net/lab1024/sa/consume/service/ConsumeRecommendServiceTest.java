package net.lab1024.sa.consume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.cache.CacheService;
import net.lab1024.sa.common.recommend.RecommendationEngine;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.service.ConsumeRecommendService.DishRecommendation;
import net.lab1024.sa.consume.service.ConsumeRecommendService.Location;
import net.lab1024.sa.consume.service.ConsumeRecommendService.RestaurantRecommendation;

/**
 * ConsumeRecommendService单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：推荐服务核心方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsumeRecommendService单元测试")
class ConsumeRecommendServiceTest {

        @Mock
        private RecommendationEngine recommendationEngine;

        @Mock
        private ConsumeTransactionDao consumeTransactionDao;

        @Mock
        private ConsumeProductDao consumeProductDao;

        @Mock
        private ConsumeAreaManager consumeAreaManager;

        @Mock
        private CacheService cacheService;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private ConsumeRecommendService recommendService;

        private Long testUserId;
        private ConsumeTransactionEntity mockTransaction;
        private ConsumeProductEntity mockProduct;

        @BeforeEach
        void setUp() {
                testUserId = 1001L;

                // 准备模拟交易记录
                mockTransaction = new ConsumeTransactionEntity();
                mockTransaction.setId(1L);
                mockTransaction.setUserId(1001L);
                mockTransaction.setAmount(new BigDecimal("15.00"));
                mockTransaction.setTransactionTime(LocalDateTime.now());
                mockTransaction.setTransactionStatus(2); // 成功
                mockTransaction.setProductDetails("[]"); // 空商品明细

                // 准备模拟商品
                mockProduct = new ConsumeProductEntity();
                mockProduct.setId(1L);
                mockProduct.setProductName("测试商品");
                mockProduct.setSalePrice(new BigDecimal("15.00"));
                mockProduct.setRating(new BigDecimal("4.5"));
                mockProduct.setSaleQuantity(100L);
                mockProduct.setViewQuantity(500L);
                mockProduct.setCategoryId(1L);
                mockProduct.setRecommended(true);
                mockProduct.setHotSale(false);
                mockProduct.setIsNew(false);
        }

        @Test
        @DisplayName("测试推荐菜品-成功场景")
        void testRecommendDishes_Success() {
                // Given
                int topN = 5;
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                List<ConsumeProductEntity> products = new ArrayList<>();
                products.add(mockProduct);

                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);
                when(consumeProductDao.selectOnShelfProducts())
                                .thenReturn(products);

                List<RecommendationEngine.RecommendationResult> mockResults = new ArrayList<>();
                RecommendationEngine.RecommendationResult result = mock(
                                RecommendationEngine.RecommendationResult.class);
                when(result.getItemId()).thenReturn(1L);
                when(result.getConfidence()).thenReturn(0.95);
                mockResults.add(result);

                when(recommendationEngine.hybridRecommendation(anyLong(), anyMap(), anyMap(), anyMap(), anyInt()))
                                .thenReturn(mockResults);

                // When
                List<DishRecommendation> recommendations = recommendService.recommendDishes(testUserId, topN);

                // Then
                assertNotNull(recommendations);
                verify(recommendationEngine, times(1)).hybridRecommendation(anyLong(), anyMap(), anyMap(), anyMap(),
                                anyInt());
        }

        @Test
        @DisplayName("测试推荐菜品-异常场景")
        void testRecommendDishes_Exception() {
                // Given
                int topN = 5;
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenThrow(new RuntimeException("数据库连接失败"));

                // When
                List<DishRecommendation> recommendations = recommendService.recommendDishes(testUserId, topN);

                // Then
                assertNotNull(recommendations);
                assertTrue(recommendations.isEmpty());
        }

        @Test
        @DisplayName("测试推荐餐厅-成功场景")
        void testRecommendRestaurants_Success() {
                // Given
                int topN = 3;
                Location userLocation = new Location(39.9042, 116.4074); // 北京坐标

                List<RecommendationEngine.RecommendationResult> mockResults = new ArrayList<>();
                RecommendationEngine.RecommendationResult result = mock(
                                RecommendationEngine.RecommendationResult.class);
                when(result.getItemId()).thenReturn(1L);
                when(result.getConfidence()).thenReturn(0.90);
                mockResults.add(result);

                when(recommendationEngine.hybridRecommendation(anyLong(), anyMap(), anyMap(), anyMap(), anyInt()))
                                .thenReturn(mockResults);

                // When
                List<RestaurantRecommendation> recommendations = recommendService.recommendRestaurants(
                                testUserId, userLocation, topN);

                // Then
                assertNotNull(recommendations);
                verify(recommendationEngine, times(1)).hybridRecommendation(anyLong(), anyMap(), anyMap(), anyMap(),
                                anyInt());
        }

        @Test
        @DisplayName("测试预测消费金额-成功场景")
        void testPredictConsumeAmount_Success() {
                // Given
                String timeOfDay = "LUNCH";
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                ConsumeTransactionEntity lunchTransaction = new ConsumeTransactionEntity();
                lunchTransaction.setUserId(1001L);
                lunchTransaction.setAmount(new BigDecimal("15.00"));
                lunchTransaction.setTransactionTime(LocalDateTime.now().withHour(12).withMinute(30));
                lunchTransaction.setTransactionStatus(2);
                transactions.add(lunchTransaction);

                when(consumeTransactionDao.selectByUserIdAndTimeRange(eq("1001"), any(LocalDateTime.class),
                                any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When
                Double predictedAmount = recommendService.predictConsumeAmount(testUserId, timeOfDay);

                // Then
                assertNotNull(predictedAmount);
                assertTrue(predictedAmount > 0);
                verify(consumeTransactionDao, times(1))
                                .selectByUserIdAndTimeRange(eq("1001"), any(LocalDateTime.class),
                                                any(LocalDateTime.class));
        }

        @Test
        @DisplayName("测试预测消费金额-无历史数据")
        void testPredictConsumeAmount_NoHistory() {
                // Given
                String timeOfDay = "BREAKFAST";
                when(consumeTransactionDao.selectByUserIdAndTimeRange(eq("1001"), any(LocalDateTime.class),
                                any(LocalDateTime.class)))
                                .thenReturn(new ArrayList<>());

                // When
                Double predictedAmount = recommendService.predictConsumeAmount(testUserId, timeOfDay);

                // Then
                assertNotNull(predictedAmount);
                assertEquals(15.0, predictedAmount); // 默认金额
        }

        @Test
        @DisplayName("测试预测消费金额-异常场景")
        void testPredictConsumeAmount_Exception() {
                // Given
                String timeOfDay = "DINNER";
                when(consumeTransactionDao.selectByUserIdAndTimeRange(eq("1001"), any(LocalDateTime.class),
                                any(LocalDateTime.class)))
                                .thenThrow(new RuntimeException("数据库连接失败"));

                // When
                Double predictedAmount = recommendService.predictConsumeAmount(testUserId, timeOfDay);

                // Then
                assertNotNull(predictedAmount);
                assertEquals(15.0, predictedAmount); // 默认金额
        }
}
