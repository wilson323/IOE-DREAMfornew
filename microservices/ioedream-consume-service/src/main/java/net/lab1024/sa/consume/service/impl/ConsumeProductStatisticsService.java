package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.common.entity.consume.ConsumeProductEntity;

/**
 * 消费产品统计分析服务
 * <p>
 * 负责产品的统计分析功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ConsumeProductStatisticsService {

    @Resource
    private ConsumeProductDao productDao;

    /**
     * 获取产品统计
     */
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        log.info("[产品统计] 获取产品统计: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 基础统计
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<>();
            List<ConsumeProductEntity> allProducts = productDao.selectList(wrapper);

            // 总产品数
            statistics.put("totalProducts", allProducts.size());

            // 在售产品数
            LambdaQueryWrapper<ConsumeProductEntity> onSaleWrapper = new LambdaQueryWrapper<>();
            onSaleWrapper.eq(ConsumeProductEntity::getStatus, 1);
            Long onSaleCount = productDao.selectCount(onSaleWrapper);
            statistics.put("onSaleProducts", onSaleCount);

            // 下架产品数
            LambdaQueryWrapper<ConsumeProductEntity> offSaleWrapper = new LambdaQueryWrapper<>();
            offSaleWrapper.eq(ConsumeProductEntity::getStatus, 0);
            Long offSaleCount = productDao.selectCount(offSaleWrapper);
            statistics.put("offSaleProducts", offSaleCount);

            // 推荐产品数
            LambdaQueryWrapper<ConsumeProductEntity> recommendedWrapper = new LambdaQueryWrapper<>();
            recommendedWrapper.eq(ConsumeProductEntity::getStatus, 1)
                            .eq(ConsumeProductEntity::getIsRecommended, 1);
            Long recommendedCount = productDao.selectCount(recommendedWrapper);
            statistics.put("recommendedProducts", recommendedCount);

            // 销售统计
            Map<String, Object> salesStatistics = getSalesStatistics(startDate, endDate);
            statistics.put("salesStatistics", salesStatistics);

            // 分类统计
            List<Map<String, Object>> categoryStatistics = getCategoryStatistics();
            statistics.put("categoryStatistics", categoryStatistics);

            log.info("[产品统计] 获取产品统计完成: statistics={}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error("[产品统计] 获取产品统计失败: startDate={}, endDate={}, error={}", startDate, endDate, e.getMessage(), e);
            throw new RuntimeException("获取产品统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售统计
     */
    public Map<String, Object> getSalesStatistics(String startDate, String endDate) {
        log.info("[产品统计] 获取销售统计: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> salesStats = new HashMap<>();

            // 查询所有产品
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeProductEntity::getStatus, 1);
            List<ConsumeProductEntity> products = productDao.selectList(wrapper);

            if (products.isEmpty()) {
                salesStats.put("totalSalesCount", 0);
                salesStats.put("totalSalesAmount", BigDecimal.ZERO);
                salesStats.put("avgSalesPerProduct", BigDecimal.ZERO);
                salesStats.put("bestSellingProduct", null);
                return salesStats;
            }

            // 计算销售统计
            long totalSalesCount = 0;
            BigDecimal totalSalesAmount = BigDecimal.ZERO;
            String bestSellingProduct = null;
            long maxSalesCount = 0;

            for (ConsumeProductEntity product : products) {
                long salesCount = product.getSalesCount() != null ? product.getSalesCount() : 0;
                BigDecimal salesAmount = product.getPrice() != null ?
                        product.getPrice().multiply(BigDecimal.valueOf(salesCount)) : BigDecimal.ZERO;

                totalSalesCount += salesCount;
                totalSalesAmount = totalSalesAmount.add(salesAmount);

                if (salesCount > maxSalesCount) {
                    maxSalesCount = salesCount;
                    bestSellingProduct = product.getProductName();
                }
            }

            salesStats.put("totalSalesCount", totalSalesCount);
            salesStats.put("totalSalesAmount", totalSalesAmount);
            salesStats.put("avgSalesPerProduct", products.size() > 0 ?
                    totalSalesAmount.divide(BigDecimal.valueOf(products.size()), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
            salesStats.put("bestSellingProduct", bestSellingProduct);

            // 销售排行
            List<Map<String, Object>> salesRanking = getSalesRanking(products);
            salesStats.put("salesRanking", salesRanking);

            log.info("[产品统计] 获取销售统计完成: salesStats={}", salesStats);
            return salesStats;

        } catch (Exception e) {
            log.error("[产品统计] 获取销售统计失败: startDate={}, endDate={}, error={}", startDate, endDate, e.getMessage(), e);
            throw new RuntimeException("获取销售统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类统计
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        log.info("[产品统计] 获取分类统计");

        try {
            // 按分类分组查询
            Map<Long, List<ConsumeProductEntity>> categoryGroups = new HashMap<>();
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeProductEntity::getStatus, 1);
            List<ConsumeProductEntity> products = productDao.selectList(wrapper);

            for (ConsumeProductEntity product : products) {
                Long categoryId = product.getCategoryId();
                if (categoryId != null) {
                    categoryGroups.computeIfAbsent(categoryId, k -> new ArrayList<>()).add(product);
                }
            }

            List<Map<String, Object>> categoryStats = new ArrayList<>();
            for (Map.Entry<Long, List<ConsumeProductEntity>> entry : categoryGroups.entrySet()) {
                Long categoryId = entry.getKey();
                List<ConsumeProductEntity> categoryProducts = entry.getValue();

                Map<String, Object> stat = new HashMap<>();
                stat.put("categoryId", categoryId);
                stat.put("categoryName", getCategoryName(categoryId)); // 这里可以根据实际情况获取分类名称
                stat.put("productCount", categoryProducts.size());
                stat.put("totalStock", categoryProducts.stream()
                        .mapToInt(p -> p.getStock() != null ? p.getStock() : 0).sum());
                stat.put("totalSalesCount", categoryProducts.stream()
                        .mapToLong(p -> p.getSalesCount() != null ? p.getSalesCount() : 0).sum());
                stat.put("totalSalesAmount", categoryProducts.stream()
                        .map(p -> p.getPrice() != null ?
                                p.getPrice().multiply(BigDecimal.valueOf(p.getSalesCount() != null ? p.getSalesCount() : 0)) : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

                categoryStats.add(stat);
            }

            // 按产品数量排序
            categoryStats.sort((a, b) -> {
                Integer countA = (Integer) a.get("productCount");
                Integer countB = (Integer) b.get("productCount");
                return countB.compareTo(countA);
            });

            log.info("[产品统计] 获取分类统计完成: categoryCount={}", categoryStats.size());
            return categoryStats;

        } catch (Exception e) {
            log.error("[产品统计] 获取分类统计失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取分类统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售排行
     */
    private List<Map<String, Object>> getSalesRanking(List<ConsumeProductEntity> products) {
        // 按销售数量排序
        products.sort((a, b) -> {
            Long salesCountA = a.getSalesCount() != null ? a.getSalesCount() : 0;
            Long salesCountB = b.getSalesCount() != null ? b.getSalesCount() : 0;
            return salesCountB.compareTo(salesCountA);
        });

        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (ConsumeProductEntity product : products) {
            if (product.getSalesCount() == null || product.getSalesCount() == 0) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("rank", rank++);
            item.put("productId", product.getProductId());
            item.put("productName", product.getProductName());
            item.put("productCode", product.getProductCode());
            item.put("salesCount", product.getSalesCount());
            item.put("salesAmount", product.getPrice() != null ?
                    product.getPrice().multiply(BigDecimal.valueOf(product.getSalesCount())) : BigDecimal.ZERO);

            ranking.add(item);

            // 只返回前20名
            if (ranking.size() >= 20) {
                break;
            }
        }

        return ranking;
    }

    /**
     * 获取分类名称（模拟方法，实际应该从分类表查询）
     */
    private String getCategoryName(Long categoryId) {
        // 这里应该从分类表查询分类名称，暂时返回模拟数据
        if (categoryId == null) {
            return "未分类";
        }
        switch (categoryId.intValue()) {
            case 1: return "套餐类";
            case 2: return "饮品";
            case 3: return "小吃";
            case 4: return "主食";
            case 5: return "甜品";
            default: return "其他";
        }
    }
}