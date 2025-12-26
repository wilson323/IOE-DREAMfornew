package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.domain.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.exception.ConsumeProductException;

/**
 * 消费产品业务管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的业务逻辑编排
 * - 处理库存管理、价格计算和业务规则验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeProductManager {

    private final ConsumeProductDao consumeProductDao;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     *
     * @param consumeProductDao 产品数据访问对象
     * @param objectMapper JSON处理工具
     */
    public ConsumeProductManager(ConsumeProductDao consumeProductDao, ObjectMapper objectMapper) {
        this.consumeProductDao = consumeProductDao;
        this.objectMapper = objectMapper;
    }

    /**
     * 验证产品编码唯一性
     *
     * @param productCode 产品编码
     * @param excludeId 排除的产品ID
     * @return 是否唯一
     */
    public boolean isProductCodeUnique(String productCode, Long excludeId) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }

        int count = consumeProductDao.countByCode(productCode.trim(), excludeId);
        return count == 0;
    }

    /**
     * 验证产品价格合理性
     *
     * @param basePrice 基础价格
     * @param salePrice 售价
     * @param costPrice 成本价
     * @return 验证结果
     */
    public boolean validatePriceReasonable(BigDecimal basePrice, BigDecimal salePrice, BigDecimal costPrice) {
        // 售价不能高于基础价格
        if (basePrice != null && salePrice != null && salePrice.compareTo(basePrice) > 0) {
            return false;
        }

        // 成本价不能高于售价
        if (costPrice != null && salePrice != null && costPrice.compareTo(salePrice) > 0) {
            return false;
        }

        // 价格不能为负数
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return true;
    }

    /**
     * 检查产品库存是否充足
     *
     * @param productId 产品ID
     * @param requiredQuantity 需要数量
     * @return 是否充足
     */
    public boolean checkStockAvailable(Long productId, Integer requiredQuantity) {
        if (productId == null || requiredQuantity == null || requiredQuantity <= 0) {
            return false;
        }

        ConsumeProductEntity product = consumeProductDao.selectById(productId);
        if (product == null) {
            return false;
        }

        return product.hasStock() && product.getStockQuantity() >= requiredQuantity;
    }

    /**
     * 检查产品是否在当前时间可销售
     *
     * @param product 产品信息
     * @param currentTime 当前时间
     * @return 是否可销售
     */
    public boolean isAvailableAtTime(ConsumeProductEntity product, LocalDateTime currentTime) {
        if (product == null || currentTime == null) {
            return false;
        }

        // 检查产品状态
        if (!product.isOnSale()) {
            return false;
        }

        // 检查库存
        if (!product.hasStock()) {
            return false;
        }

        // 检查销售时间段
        if (product.getSaleTimePeriods() == null || product.getSaleTimePeriods().trim().isEmpty()) {
            return true; // 没有时间限制，一直可销售
        }

        try {
            List<String> timePeriods = parseTimePeriods(product.getSaleTimePeriods());
            String currentTimeStr = currentTime.toLocalTime().toString();

            for (String timePeriod : timePeriods) {
                if (isTimeInRange(currentTimeStr, timePeriod)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("解析销售时间段失败: product={}, timePeriods={}, error={}",
                product.getProductId(), product.getSaleTimePeriods(), e.getMessage(), e);
            return true; // 解析失败时默认可销售，避免影响业务
        }
    }

    /**
     * 计算产品实际售价（考虑折扣）
     *
     * @param product 产品信息
     * @param discountRate 折扣比例（可选）
     * @return 实际售价
     */
    public BigDecimal calculateActualPrice(ConsumeProductEntity product, BigDecimal discountRate) {
        if (product == null || product.getSalePrice() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal actualPrice = product.getSalePrice();

        // 检查是否允许折扣
        if (discountRate != null && product.canDiscount()) {
            // 不能超过最大折扣比例
            BigDecimal maxDiscount = product.getMaxDiscountRate();
            if (maxDiscount != null && discountRate.compareTo(maxDiscount) > 0) {
                discountRate = maxDiscount;
            }

            BigDecimal discountAmount = actualPrice.multiply(discountRate);
            actualPrice = actualPrice.subtract(discountAmount);

            // 确保折后价格不低于成本价
            if (product.getCostPrice() != null && actualPrice.compareTo(product.getCostPrice()) < 0) {
                actualPrice = product.getCostPrice();
            }
        }

        return actualPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 解析时间段JSON
     */
    private List<String> parseTimePeriods(String timePeriodsJson) throws JsonProcessingException {
        return objectMapper.readValue(timePeriodsJson, new TypeReference<List<String>>() {});
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(String currentTime, String timeRange) {
        try {
            String[] times = timeRange.split("-");
            if (times.length != 2) {
                return false;
            }

            String startTime = times[0].trim();
            String endTime = times[1].trim();

            // 简单的时间范围检查
            return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
        } catch (Exception e) {
            log.error("解析时间范围失败: timeRange={}, error={}", timeRange, e.getMessage());
            return false;
        }
    }

    /**
     * 验证产品业务规则
     *
     * @param product 产品信息
     * @throws ConsumeProductException 业务规则验证失败时抛出
     */
    public void validateProductRules(ConsumeProductEntity product) {
        List<String> errors = product.validateBusinessRules();

        if (!errors.isEmpty()) {
            throw ConsumeProductException.validationFailed(errors);
        }

        // 验证产品编码唯一性
        if (!isProductCodeUnique(product.getProductCode(), product.getProductId())) {
            throw ConsumeProductException.duplicateCode(product.getProductCode());
        }

        // 验证产品分类是否存在
        // 这里应该调用分类服务，暂时跳过
    }

    /**
     * 检查产品是否可以删除
     *
     * @param productId 产品ID
     * @return 检查结果，包含是否可删除和相关记录数
     */
    public Map<String, Object> checkDeleteProduct(Long productId) {
        Map<String, Object> result = new HashMap<>();

        ConsumeProductEntity product = consumeProductDao.selectById(productId);
        if (product == null) {
            result.put("canDelete", false);
            result.put("reason", "产品不存在");
            return result;
        }

        // 上架产品不能删除
        if (product.isOnSale()) {
            result.put("canDelete", false);
            result.put("reason", "上架产品不能删除，请先下架");
            return result;
        }

        // 检查是否有关联的业务记录
        Map<String, Long> relatedRecords = consumeProductDao.countRelatedRecords(productId);
        result.put("canDelete", true);
        result.put("relatedRecords", relatedRecords);

        return result;
    }

    /**
     * 更新产品库存
     *
     * @param productId 产品ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @return 更新是否成功
     */
    public boolean updateProductStock(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity == 0) {
            return false;
        }

        ConsumeProductEntity product = consumeProductDao.selectById(productId);
        if (product == null) {
            throw ConsumeProductException.notFound(productId);
        }

        // 检查库存是否足够（减少库存时）
        if (quantity < 0 && product.getStockQuantity() + quantity < 0) {
            throw ConsumeProductException.insufficientStock(productId, product.getStockQuantity());
        }

        int updatedRows = consumeProductDao.updateStock(productId, quantity);
        return updatedRows > 0;
    }

    /**
     * 批量更新产品库存
     *
     * @param stockUpdates 库存更新列表
     * @return 更新结果
     */
    public Map<String, Object> batchUpdateStock(List<Map<String, Object>> stockUpdates) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();

        if (stockUpdates == null || stockUpdates.isEmpty()) {
            result.put("success", false);
            result.put("message", "更新列表为空");
            result.put("errors", errors);
            return result;
        }

        // 验证更新数据
        for (Map<String, Object> update : stockUpdates) {
            Object productIdObj = update.get("productId");
            Object quantityObj = update.get("quantity");

            if (!(productIdObj instanceof Long) || !(quantityObj instanceof Integer)) {
                errors.add("更新数据格式错误");
                continue;
            }

            Long productId = (Long) productIdObj;
            Integer quantity = (Integer) quantityObj;

            try {
                // 检查产品是否存在
                ConsumeProductEntity product = consumeProductDao.selectById(productId);
                if (product == null) {
                    errors.add("产品不存在: " + productId);
                    continue;
                }

                // 检查库存是否足够（减少库存时）
                if (quantity < 0 && !product.hasStock() && product.getStockQuantity() + quantity < 0) {
                    errors.add("库存不足: " + product.getProductName());
                    continue;
                }
            } catch (Exception e) {
                errors.add("验证产品失败: " + productId);
            }
        }

        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("message", "库存更新验证失败");
            result.put("errors", errors);
            return result;
        }

        // 执行批量更新
        try {
            int updatedCount = consumeProductDao.batchUpdateStock(stockUpdates);
            result.put("success", true);
            result.put("message", "库存更新成功");
            result.put("updatedCount", updatedCount);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "库存更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取产品库存统计信息
     *
     * @return 库存统计
     */
    public Map<String, Object> getStockStatistics() {
        Map<String, Object> statistics = consumeProductDao.getStockStatistics();

        if (statistics == null) {
            statistics = new HashMap<>();
        }

        // 获取库存不足的产品列表
        List<ConsumeProductVO> lowStockProducts = consumeProductDao.selectLowStockProducts();
        statistics.put("lowStockCount", lowStockProducts.size());
        statistics.put("lowStockProducts", lowStockProducts);

        return statistics;
    }

    /**
     * 获取产品销售统计信息
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售统计
     */
    public Map<String, Object> getSalesStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();

        // 基础统计
        Map<String, Object> productStats = consumeProductDao.getProductStatistics(startDate, endDate);
        if (productStats != null) {
            statistics.putAll(productStats);
        }

        // 分类统计
        List<Map<String, Object>> categoryStats = consumeProductDao.countProductsByCategory();
        statistics.put("categoryStatistics", categoryStats);

        // 价格区间统计
        List<Map<String, Object>> priceStats = consumeProductDao.countProductsByPriceRange();
        statistics.put("priceStatistics", priceStats);

        // 热销产品
        List<ConsumeProductVO> hotSales = consumeProductDao.selectHotSales(10);
        statistics.put("hotSales", hotSales);

        // 高评分产品
        List<ConsumeProductVO> highRated = consumeProductDao.selectHighRated(10, new BigDecimal("4.0"));
        statistics.put("highRated", highRated);

        return statistics;
    }

    /**
     * 搜索产品
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索结果
     */
    public List<ConsumeProductVO> searchProducts(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return consumeProductDao.searchProducts(keyword.trim(), limit != null ? limit : 20);
    }

    /**
     * 获取推荐产品列表
     *
     * @param limit 限制数量
     * @return 推荐产品列表
     */
    public List<ConsumeProductVO> getRecommendedProducts(Integer limit) {
        return consumeProductDao.selectRecommended(limit != null ? limit : 10);
    }

    /**
     * 获取低库存产品列表
     *
     * @return 低库存产品列表
     */
    public List<ConsumeProductVO> getLowStockProducts() {
        return consumeProductDao.selectLowStockProducts();
    }
}