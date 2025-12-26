package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.domain.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.exception.ConsumeProductException;

/**
 * ConsumeProductManager 单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@DisplayName("ConsumeProductManager 单元测试")
class ConsumeProductManagerTest {

    @Mock
    private ConsumeProductDao consumeProductDao;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ConsumeProductManager consumeProductManager;

    private ConsumeProductEntity testProduct;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProduct = createTestProduct();
        testTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
    }

    private ConsumeProductEntity createTestProduct() {
        ConsumeProductEntity product = new ConsumeProductEntity();
        product.setProductId(1L);
        product.setProductCode("TEST_001");
        product.setProductName("测试产品");
        product.setProductCategory(1);
        product.setBasePrice(new BigDecimal("10.00"));
        product.setSalePrice(new BigDecimal("8.00"));
        product.setCostPrice(new BigDecimal("5.00"));
        product.setProductStatus(1); // 上架
        product.setStockQuantity(100);
        product.setAllowDiscount(1);
        product.setMaxDiscountRate(new BigDecimal("0.5"));
        product.setSaleTimePeriods("[\"08:00-18:00\"]");
        product.setProductImage("test.jpg");
        product.setProductDescription("测试产品描述");
        product.setProductTags("测试,美味");
        product.setRatingAverage(new BigDecimal("4.5"));
        product.setRatingCount(100);
        return product;
    }

    @Nested
    @DisplayName("产品编码唯一性验证测试")
    class ProductCodeUniquenessTests {

        @Test
        @DisplayName("测试产品编码唯一 - 返回true")
        void testIsProductCodeUnique_UniqueCode_ReturnsTrue() {
            // Given
            String productCode = "UNIQUE_001";
            Long excludeId = 1L;
            when(consumeProductDao.countByCode(productCode, excludeId)).thenReturn(0);

            // When
            boolean result = consumeProductManager.isProductCodeUnique(productCode, excludeId);

            // Then
            assertTrue(result);
            verify(consumeProductDao).countByCode(productCode, excludeId);
        }

        @Test
        @DisplayName("测试产品编码重复 - 返回false")
        void testIsProductCodeUnique_DuplicateCode_ReturnsFalse() {
            // Given
            String productCode = "DUPLICATE_001";
            Long excludeId = 1L;
            when(consumeProductDao.countByCode(productCode, excludeId)).thenReturn(1);

            // When
            boolean result = consumeProductManager.isProductCodeUnique(productCode, excludeId);

            // Then
            assertFalse(result);
            verify(consumeProductDao).countByCode(productCode, excludeId);
        }

        @Test
        @DisplayName("测试产品编码为null - 返回false")
        void testIsProductCodeUnique_NullCode_ReturnsFalse() {
            // When
            boolean result = consumeProductManager.isProductCodeUnique(null, 1L);

            // Then
            assertFalse(result);
            verify(consumeProductDao, never()).countByCode(anyString(), any());
        }

        @Test
        @DisplayName("测试产品编码为空字符串 - 返回false")
        void testIsProductCodeUnique_EmptyCode_ReturnsFalse() {
            // When
            boolean result = consumeProductManager.isProductCodeUnique("   ", 1L);

            // Then
            assertFalse(result);
            verify(consumeProductDao, never()).countByCode(anyString(), any());
        }
    }

    @Nested
    @DisplayName("价格合理性验证测试")
    class PriceValidationTests {

        @Test
        @DisplayName("测试价格合理 - 返回true")
        void testValidatePriceReasonable_ValidPrice_ReturnsTrue() {
            // When
            boolean result = consumeProductManager.validatePriceReasonable(
                new BigDecimal("10.00"),
                new BigDecimal("8.00"),
                new BigDecimal("5.00")
            );

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试售价高于基础价格 - 返回false")
        void testValidatePriceReasonable_SalePriceHigherThanBase_ReturnsFalse() {
            // When
            boolean result = consumeProductManager.validatePriceReasonable(
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                new BigDecimal("8.00")
            );

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试成本价高于售价 - 返回false")
        void testValidatePriceReasonable_CostPriceHigherThanSale_ReturnsFalse() {
            // When
            boolean result = consumeProductManager.validatePriceReasonable(
                new BigDecimal("10.00"),
                new BigDecimal("8.00"),
                new BigDecimal("9.00")
            );

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试售价为负数 - 返回false")
        void testValidatePriceReasonable_NegativeSalePrice_ReturnsFalse() {
            // When
            boolean result = consumeProductManager.validatePriceReasonable(
                new BigDecimal("10.00"),
                new BigDecimal("-1.00"),
                new BigDecimal("5.00")
            );

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试价格为null - 返回true")
        void testValidatePriceReasonable_NullPrice_ReturnsTrue() {
            // When
            boolean result = consumeProductManager.validatePriceReasonable(
                null,
                new BigDecimal("8.00"),
                null
            );

            // Then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("库存检查测试")
    class StockCheckTests {

        @Test
        @DisplayName("测试库存充足 - 返回true")
        void testCheckStockAvailable_SufficientStock_ReturnsTrue() {
            // Given
            Long productId = 1L;
            Integer requiredQuantity = 50;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);

            // When
            boolean result = consumeProductManager.checkStockAvailable(productId, requiredQuantity);

            // Then
            assertTrue(result);
            verify(consumeProductDao).selectById(productId);
        }

        @Test
        @DisplayName("测试库存不足 - 返回false")
        void testCheckStockAvailable_InsufficientStock_ReturnsFalse() {
            // Given
            Long productId = 1L;
            Integer requiredQuantity = 150;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);

            // When
            boolean result = consumeProductManager.checkStockAvailable(productId, requiredQuantity);

            // Then
            assertFalse(result);
            verify(consumeProductDao).selectById(productId);
        }

        @Test
        @DisplayName("测试产品不存在 - 返回false")
        void testCheckStockAvailable_ProductNotExists_ReturnsFalse() {
            // Given
            Long productId = 999L;
            Integer requiredQuantity = 10;
            when(consumeProductDao.selectById(productId)).thenReturn(null);

            // When
            boolean result = consumeProductManager.checkStockAvailable(productId, requiredQuantity);

            // Then
            assertFalse(result);
            verify(consumeProductDao).selectById(productId);
        }

        @Test
        @DisplayName("测试参数无效 - 返回false")
        void testCheckStockAvailable_InvalidParameters_ReturnsFalse() {
            // When & Then
            assertFalse(consumeProductManager.checkStockAvailable(null, 10));
            assertFalse(consumeProductManager.checkStockAvailable(1L, null));
            assertFalse(consumeProductManager.checkStockAvailable(1L, 0));
            assertFalse(consumeProductManager.checkStockAvailable(1L, -5));
        }
    }

    @Nested
    @DisplayName("产品可销售时间检查测试")
    class AvailabilityTimeTests {

        @Test
        @DisplayName("测试产品在可销售时间内 - 返回true")
        void testIsAvailableAtTime_WithinSaleTime_ReturnsTrue() {
            // Given
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
            when(consumeProductDao.selectById(any())).thenReturn(testProduct);

            // When
            boolean result = consumeProductManager.isAvailableAtTime(testProduct, currentTime);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试产品在可销售时间外 - 返回false")
        void testIsAvailableAtTime_OutsideSaleTime_ReturnsFalse() {
            // Given
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 20, 0, 0);
            testProduct.setSaleTimePeriods("[\"08:00-18:00\"]");

            // When - 使用真实的ObjectMapper解析JSON（不需要mock）
            boolean result = consumeProductManager.isAvailableAtTime(testProduct, currentTime);

            // Then - 20:00不在08:00-18:00范围内
            assertFalse(result);
        }

        @Test
        @DisplayName("测试产品下架 - 返回false")
        void testIsAvailableAtTime_ProductOffSale_ReturnsFalse() {
            // Given
            testProduct.setProductStatus(0); // 下架
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);

            // When
            boolean result = consumeProductManager.isAvailableAtTime(testProduct, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试产品无库存 - 返回false")
        void testIsAvailableAtTime_NoStock_ReturnsFalse() {
            // Given
            testProduct.setStockQuantity(0);
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);

            // When
            boolean result = consumeProductManager.isAvailableAtTime(testProduct, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试产品无时间限制 - 返回true")
        void testIsAvailableAtTime_NoTimeLimit_ReturnsTrue() {
            // Given
            testProduct.setSaleTimePeriods("");
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 22, 0, 0);

            // When
            boolean result = consumeProductManager.isAvailableAtTime(testProduct, currentTime);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试参数为null - 返回false")
        void testIsAvailableAtTime_NullParameters_ReturnsFalse() {
            // When & Then
            assertFalse(consumeProductManager.isAvailableAtTime(null, testTime));
            assertFalse(consumeProductManager.isAvailableAtTime(testProduct, null));
        }
    }

    @Nested
    @DisplayName("实际价格计算测试")
    class ActualPriceTests {

        @Test
        @DisplayName("测试计算实际价格 - 无折扣")
        void testCalculateActualPrice_NoDiscount() {
            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(testProduct, null);

            // Then
            assertEquals(new BigDecimal("8.00"), result);
        }

        @Test
        @DisplayName("测试计算实际价格 - 有折扣")
        void testCalculateActualPrice_WithDiscount() {
            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(
                testProduct, new BigDecimal("0.1")
            );

            // Then
            assertEquals(new BigDecimal("7.20"), result); // 8.00 - 0.80
        }

        @Test
        @DisplayName("测试计算实际价格 - 折扣超过最大限制并触发成本价保护")
        void testCalculateActualPrice_DiscountExceedsMax() {
            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(
                testProduct, new BigDecimal("0.8")
            );

            // Then - 折扣0.8被限制为0.5，但折后价格4.00低于成本价5.00，所以返回成本价
            assertEquals(new BigDecimal("5.00"), result); // 成本价保护
        }

        @Test
        @DisplayName("测试计算实际价格 - 折扣后低于成本价")
        void testCalculateActualPrice_DiscountBelowCost() {
            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(
                testProduct, new BigDecimal("0.8")
            );

            // Then
            assertEquals(new BigDecimal("5.00"), result); // 成本价保护
        }

        @Test
        @DisplayName("测试计算实际价格 - 产品不允许折扣")
        void testCalculateActualPrice_ProductNotAllowDiscount() {
            // Given
            testProduct.setAllowDiscount(0);

            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(
                testProduct, new BigDecimal("0.1")
            );

            // Then
            assertEquals(new BigDecimal("8.00"), result); // 无折扣
        }

        @Test
        @DisplayName("测试计算实际价格 - 产品为null")
        void testCalculateActualPrice_NullProduct() {
            // When
            BigDecimal result = consumeProductManager.calculateActualPrice(null, new BigDecimal("0.1"));

            // Then
            assertEquals(BigDecimal.ZERO, result);
        }
    }

    @Nested
    @DisplayName("产品业务规则验证测试")
    class ProductValidationTests {

        @Test
        @DisplayName("测试产品验证成功 - 无异常")
        void testValidateProductRules_ValidProduct_NoException() {
            // Given
            when(consumeProductDao.countByCode(anyString(), any())).thenReturn(0);

            // When & Then
            assertDoesNotThrow(() -> consumeProductManager.validateProductRules(testProduct));
            verify(consumeProductDao).countByCode(testProduct.getProductCode(), testProduct.getProductId());
        }

        @Test
        @DisplayName("测试产品编码重复 - 抛出异常")
        void testValidateProductRules_DuplicateCode_ThrowsException() {
            // Given
            when(consumeProductDao.countByCode(anyString(), any())).thenReturn(1);

            // When & Then
            ConsumeProductException exception = assertThrows(
                ConsumeProductException.class,
                () -> consumeProductManager.validateProductRules(testProduct)
            );
            assertTrue(exception.getMessage().contains("产品编码已存在"));
        }
    }

    @Nested
    @DisplayName("产品删除检查测试")
    class DeleteCheckTests {

        @Test
        @DisplayName("检查产品可以删除 - 上架产品不可删除")
        void testCheckDeleteProduct_ProductOnSale_CannotDelete() {
            // Given
            Long productId = 1L;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);

            // When
            Map<String, Object> result = consumeProductManager.checkDeleteProduct(productId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("上架产品不能删除，请先下架", result.get("reason"));
        }

        @Test
        @DisplayName("检查产品可以删除 - 下架产品可以删除")
        void testCheckDeleteProduct_ProductOffSale_CanDelete() {
            // Given
            Long productId = 1L;
            testProduct.setProductStatus(0); // 下架
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);

            Map<String, Long> relatedRecords = new HashMap<>();
            relatedRecords.put("consumeRecords", 0L);
            relatedRecords.put("favorites", 5L);
            when(consumeProductDao.countRelatedRecords(productId)).thenReturn(relatedRecords);

            // When
            Map<String, Object> result = consumeProductManager.checkDeleteProduct(productId);

            // Then
            assertTrue((Boolean) result.get("canDelete"));
            assertEquals(relatedRecords, result.get("relatedRecords"));
        }

        @Test
        @DisplayName("检查产品不存在 - 不能删除")
        void testCheckDeleteProduct_ProductNotExists_CannotDelete() {
            // Given
            Long productId = 999L;
            when(consumeProductDao.selectById(productId)).thenReturn(null);

            // When
            Map<String, Object> result = consumeProductManager.checkDeleteProduct(productId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("产品不存在", result.get("reason"));
        }
    }

    @Nested
    @DisplayName("库存更新测试")
    class StockUpdateTests {

        @Test
        @DisplayName("测试更新库存成功 - 增加库存")
        void testUpdateProductStock_IncreaseStock_ReturnsTrue() {
            // Given
            Long productId = 1L;
            Integer quantity = 50;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);
            when(consumeProductDao.updateStock(productId, quantity)).thenReturn(1);

            // When
            boolean result = consumeProductManager.updateProductStock(productId, quantity);

            // Then
            assertTrue(result);
            verify(consumeProductDao).selectById(productId);
            verify(consumeProductDao).updateStock(productId, quantity);
        }

        @Test
        @DisplayName("测试更新库存成功 - 减少库存")
        void testUpdateProductStock_DecreaseStock_ReturnsTrue() {
            // Given
            Long productId = 1L;
            Integer quantity = -30;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);
            when(consumeProductDao.updateStock(productId, quantity)).thenReturn(1);

            // When
            boolean result = consumeProductManager.updateProductStock(productId, quantity);

            // Then
            assertTrue(result);
            verify(consumeProductDao).selectById(productId);
            verify(consumeProductDao).updateStock(productId, quantity);
        }

        @Test
        @DisplayName("测试更新库存失败 - 库存不足")
        void testUpdateProductStock_InsufficientStock_ThrowsException() {
            // Given
            Long productId = 1L;
            Integer quantity = -150;
            when(consumeProductDao.selectById(productId)).thenReturn(testProduct);

            // When & Then
            ConsumeProductException exception = assertThrows(
                ConsumeProductException.class,
                () -> consumeProductManager.updateProductStock(productId, quantity)
            );
            assertTrue(exception.getMessage().contains("库存不足"));
        }

        @Test
        @DisplayName("测试更新库存 - 产品不存在")
        void testUpdateProductStock_ProductNotExists_ThrowsException() {
            // Given
            Long productId = 999L;
            Integer quantity = 10;
            when(consumeProductDao.selectById(productId)).thenReturn(null);

            // When & Then
            ConsumeProductException exception = assertThrows(
                ConsumeProductException.class,
                () -> consumeProductManager.updateProductStock(productId, quantity)
            );
            assertTrue(exception.getMessage().contains("不存在"));
        }

        @Test
        @DisplayName("测试更新库存 - 参数无效")
        void testUpdateProductStock_InvalidParameters_ReturnsFalse() {
            // When & Then
            assertFalse(consumeProductManager.updateProductStock(null, 10));
            assertFalse(consumeProductManager.updateProductStock(1L, null));
            assertFalse(consumeProductManager.updateProductStock(1L, 0));
        }
    }

    @Nested
    @DisplayName("批量库存更新测试")
    class BatchStockUpdateTests {

        @Test
        @DisplayName("测试批量更新库存成功")
        void testBatchUpdateStock_Success() {
            // Given
            List<Map<String, Object>> stockUpdates = new ArrayList<>();
            Map<String, Object> update1 = new HashMap<>();
            update1.put("productId", 1L);
            update1.put("quantity", 10);
            stockUpdates.add(update1);

            when(consumeProductDao.selectById(1L)).thenReturn(testProduct);
            when(consumeProductDao.batchUpdateStock(stockUpdates)).thenReturn(1);

            // When
            Map<String, Object> result = consumeProductManager.batchUpdateStock(stockUpdates);

            // Then
            assertTrue((Boolean) result.get("success"));
            assertEquals("库存更新成功", result.get("message"));
            assertEquals(1, result.get("updatedCount"));
        }

        @Test
        @DisplayName("测试批量更新库存 - 空列表")
        void testBatchUpdateStock_EmptyList_Failure() {
            // When
            Map<String, Object> result = consumeProductManager.batchUpdateStock(new ArrayList<>());

            // Then
            assertFalse((Boolean) result.get("success"));
            assertEquals("更新列表为空", result.get("message"));
        }

        @Test
        @DisplayName("测试批量更新库存 - 数据格式错误")
        void testBatchUpdateStock_InvalidFormat_Failure() {
            // Given
            List<Map<String, Object>> stockUpdates = new ArrayList<>();
            Map<String, Object> update = new HashMap<>();
            update.put("productId", "invalid");
            update.put("quantity", "invalid");
            stockUpdates.add(update);

            // When
            Map<String, Object> result = consumeProductManager.batchUpdateStock(stockUpdates);

            // Then
            assertFalse((Boolean) result.get("success"));
            assertTrue(((List<?>) result.get("errors")).contains("更新数据格式错误"));
        }
    }

    @Nested
    @DisplayName("库存统计测试")
    class StockStatisticsTests {

        @Test
        @DisplayName("测试获取库存统计")
        void testGetStockStatistics_Success() {
            // Given
            Map<String, Object> expectedStats = new HashMap<>();
            expectedStats.put("totalProducts", 100L);
            expectedStats.put("totalStock", 10000L);

            List<ConsumeProductVO> lowStockProducts = new ArrayList<>();
            ConsumeProductVO lowStockProduct = new ConsumeProductVO();
            lowStockProduct.setProductName("低库存产品");
            lowStockProducts.add(lowStockProduct);

            when(consumeProductDao.getStockStatistics()).thenReturn(expectedStats);
            when(consumeProductDao.selectLowStockProducts()).thenReturn(lowStockProducts);

            // When
            Map<String, Object> result = consumeProductManager.getStockStatistics();

            // Then
            assertEquals(100L, result.get("totalProducts"));
            assertEquals(10000L, result.get("totalStock"));
            assertEquals(1, result.get("lowStockCount"));
            assertEquals(lowStockProducts, result.get("lowStockProducts"));
        }

        @Test
        @DisplayName("测试获取库存统计 - 返回null")
        void testGetStockStatistics_ReturnsNull() {
            // Given
            when(consumeProductDao.getStockStatistics()).thenReturn(null);
            when(consumeProductDao.selectLowStockProducts()).thenReturn(new ArrayList<>());

            // When
            Map<String, Object> result = consumeProductManager.getStockStatistics();

            // Then
            assertNotNull(result);
            assertEquals(0, result.get("lowStockCount"));
        }
    }

    @Nested
    @DisplayName("销售统计测试")
    class SalesStatisticsTests {

        @Test
        @DisplayName("测试获取销售统计")
        void testGetSalesStatistics_Success() {
            // Given
            Map<String, Object> productStats = new HashMap<>();
            productStats.put("totalSales", 1000L);
            productStats.put("totalRevenue", new BigDecimal("8000.00"));

            List<Map<String, Object>> categoryStats = new ArrayList<>();
            Map<String, Object> categoryStat = new HashMap<>();
            categoryStat.put("categoryName", "饮料");
            categoryStat.put("productCount", 50L);
            categoryStats.add(categoryStat);

            List<Map<String, Object>> priceStats = new ArrayList<>();
            List<ConsumeProductVO> hotSales = new ArrayList<>();
            List<ConsumeProductVO> highRated = new ArrayList<>();

            when(consumeProductDao.getProductStatistics(anyString(), anyString())).thenReturn(productStats);
            when(consumeProductDao.countProductsByCategory()).thenReturn(categoryStats);
            when(consumeProductDao.countProductsByPriceRange()).thenReturn(priceStats);
            when(consumeProductDao.selectHotSales(10)).thenReturn(hotSales);
            when(consumeProductDao.selectHighRated(10, new BigDecimal("4.0"))).thenReturn(highRated);

            // When
            Map<String, Object> result = consumeProductManager.getSalesStatistics("2025-01-01", "2025-01-31");

            // Then
            assertEquals(1000L, result.get("totalSales"));
            assertEquals(new BigDecimal("8000.00"), result.get("totalRevenue"));
            assertEquals(categoryStats, result.get("categoryStatistics"));
            assertEquals(priceStats, result.get("priceStatistics"));
            assertEquals(hotSales, result.get("hotSales"));
            assertEquals(highRated, result.get("highRated"));
        }
    }

    @Nested
    @DisplayName("产品搜索测试")
    class ProductSearchTests {

        @Test
        @DisplayName("测试搜索产品 - 有效关键词")
        void testSearchProducts_ValidKeyword_ReturnsResults() {
            // Given
            String keyword = "测试";
            List<ConsumeProductVO> expectedResults = new ArrayList<>();
            ConsumeProductVO product = new ConsumeProductVO();
            product.setProductName("测试产品");
            expectedResults.add(product);

            when(consumeProductDao.searchProducts(keyword, 20)).thenReturn(expectedResults);

            // When
            List<ConsumeProductVO> result = consumeProductManager.searchProducts(keyword, null);

            // Then
            assertEquals(expectedResults, result);
            verify(consumeProductDao).searchProducts(keyword, 20);
        }

        @Test
        @DisplayName("测试搜索产品 - 无效关键词")
        void testSearchProducts_InvalidKeyword_ReturnsEmpty() {
            // When
            List<ConsumeProductVO> result = consumeProductManager.searchProducts("   ", null);

            // Then
            assertTrue(result.isEmpty());
            verify(consumeProductDao, never()).searchProducts(anyString(), anyInt());
        }

        @Test
        @DisplayName("测试搜索产品 - 自定义限制")
        void testSearchProducts_CustomLimit() {
            // Given
            String keyword = "测试";
            Integer limit = 5;
            List<ConsumeProductVO> expectedResults = new ArrayList<>();
            when(consumeProductDao.searchProducts(keyword, limit)).thenReturn(expectedResults);

            // When
            List<ConsumeProductVO> result = consumeProductManager.searchProducts(keyword, limit);

            // Then
            assertEquals(expectedResults, result);
            verify(consumeProductDao).searchProducts(keyword, limit);
        }
    }

    @Nested
    @DisplayName("推荐产品测试")
    class RecommendedProductsTests {

        @Test
        @DisplayName("测试获取推荐产品")
        void testGetRecommendedProducts_Success() {
            // Given
            List<ConsumeProductVO> expectedResults = new ArrayList<>();
            ConsumeProductVO product = new ConsumeProductVO();
            product.setProductName("推荐产品");
            expectedResults.add(product);

            when(consumeProductDao.selectRecommended(10)).thenReturn(expectedResults);

            // When
            List<ConsumeProductVO> result = consumeProductManager.getRecommendedProducts(null);

            // Then
            assertEquals(expectedResults, result);
            verify(consumeProductDao).selectRecommended(10);
        }

        @Test
        @DisplayName("测试获取推荐产品 - 自定义限制")
        void testGetRecommendedProducts_CustomLimit() {
            // Given
            Integer limit = 5;
            List<ConsumeProductVO> expectedResults = new ArrayList<>();
            when(consumeProductDao.selectRecommended(limit)).thenReturn(expectedResults);

            // When
            List<ConsumeProductVO> result = consumeProductManager.getRecommendedProducts(limit);

            // Then
            assertEquals(expectedResults, result);
            verify(consumeProductDao).selectRecommended(limit);
        }
    }

    @Nested
    @DisplayName("低库存产品测试")
    class LowStockProductsTests {

        @Test
        @DisplayName("测试获取低库存产品")
        void testGetLowStockProducts_Success() {
            // Given
            List<ConsumeProductVO> expectedResults = new ArrayList<>();
            ConsumeProductVO product = new ConsumeProductVO();
            product.setProductName("低库存产品");
            product.setStockQuantity(5);
            expectedResults.add(product);

            when(consumeProductDao.selectLowStockProducts()).thenReturn(expectedResults);

            // When
            List<ConsumeProductVO> result = consumeProductManager.getLowStockProducts();

            // Then
            assertEquals(expectedResults, result);
            verify(consumeProductDao).selectLowStockProducts();
        }
    }
}