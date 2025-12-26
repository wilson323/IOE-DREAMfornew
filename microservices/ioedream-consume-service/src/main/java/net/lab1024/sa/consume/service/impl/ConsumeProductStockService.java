package net.lab1024.sa.consume.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.common.entity.consume.ConsumeProductEntity;
import net.lab1024.sa.consume.exception.ConsumeProductException;

/**
 * 消费产品库存服务
 * <p>
 * 负责产品的库存管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeProductStockService {

    @Resource
    private ConsumeProductDao productDao;

    /**
     * 更新产品库存
     */
    public Boolean updateStock(Long productId, Integer quantity) {
        log.info("[库存管理] 更新产品库存: productId={}, quantity={}", productId, quantity);

        try {
            if (quantity == null || quantity < 0) {
                throw new ConsumeProductException("库存数量不能为空或负数");
            }

            LambdaUpdateWrapper<ConsumeProductEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ConsumeProductEntity::getProductId, productId)
                   .set(ConsumeProductEntity::getStock, quantity)
                   .set(ConsumeProductEntity::getUpdateTime, LocalDateTime.now());

            int affectedRows = productDao.update(null, wrapper);

            log.info("[库存管理] 更新产品库存完成: productId={}, quantity={}, affectedRows={}", productId, quantity, affectedRows);
            return affectedRows > 0;

        } catch (Exception e) {
            log.error("[库存管理] 更新产品库存失败: productId={}, quantity={}, error={}", productId, quantity, e.getMessage(), e);
            throw new ConsumeProductException("更新库存失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新库存
     */
    public Map<String, Object> batchUpdateStock(List<Map<String, Object>> stockUpdates) {
        log.info("[库存管理] 批量更新库存: updateCount={}", stockUpdates.size());

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        List<String> failedProducts = new ArrayList<>();

        try {
            for (Map<String, Object> update : stockUpdates) {
                try {
                    Long productId = Long.valueOf(update.get("productId").toString());
                    Integer quantity = Integer.valueOf(update.get("quantity").toString());

                    Boolean success = updateStock(productId, quantity);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                        failedProducts.add(productId.toString());
                    }

                } catch (Exception e) {
                    failureCount++;
                    failedProducts.add(update.get("productId").toString());
                    log.warn("[库存管理] 批量更新库存时跳过失败项: update={}, error={}", update, e.getMessage());
                }
            }

            result.put("totalCount", stockUpdates.size());
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("failedProducts", failedProducts);

            log.info("[库存管理] 批量更新库存完成: totalCount={}, successCount={}, failureCount={}",
                    stockUpdates.size(), successCount, failureCount);

            return result;

        } catch (Exception e) {
            log.error("[库存管理] 批量更新库存失败: stockUpdates={}, error={}", stockUpdates, e.getMessage(), e);
            throw new ConsumeProductException("批量更新库存失败: " + e.getMessage());
        }
    }

    /**
     * 检查库存
     */
    public Boolean checkStock(Long productId, Integer requiredQuantity) {
        log.info("[库存管理] 检查库存: productId={}, requiredQuantity={}", productId, requiredQuantity);

        try {
            if (requiredQuantity == null || requiredQuantity <= 0) {
                throw new ConsumeProductException("需求数量不能为空或负数");
            }

            ConsumeProductEntity entity = productDao.selectById(productId);
            if (entity == null) {
                throw new ConsumeProductException("产品不存在");
            }

            boolean sufficient = entity.getStock() != null && entity.getStock() >= requiredQuantity;

            log.info("[库存管理] 检查库存完成: productId={}, currentStock={}, requiredQuantity={}, sufficient={}",
                    productId, entity.getStock(), requiredQuantity, sufficient);

            return sufficient;

        } catch (Exception e) {
            log.error("[库存管理] 检查库存失败: productId={}, requiredQuantity={}, error={}", productId, requiredQuantity, e.getMessage(), e);
            throw new ConsumeProductException("检查库存失败: " + e.getMessage());
        }
    }

    /**
     * 扣减库存
     */
    public Boolean deductStock(Long productId, Integer quantity) {
        log.info("[库存管理] 扣减库存: productId={}, quantity={}", productId, quantity);

        try {
            if (quantity == null || quantity <= 0) {
                throw new ConsumeProductException("扣减数量不能为空或负数");
            }

            // 1. 检查库存是否充足
            if (!checkStock(productId, quantity)) {
                throw new ConsumeProductException("库存不足，无法扣减");
            }

            // 2. 扣减库存
            LambdaUpdateWrapper<ConsumeProductEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ConsumeProductEntity::getProductId, productId)
                   .ge(ConsumeProductEntity::getStock, quantity)
                   .setSql("stock = stock - " + quantity)
                   .setSql("sales_count = sales_count + " + quantity)
                   .set(ConsumeProductEntity::getUpdateTime, LocalDateTime.now());

            int affectedRows = productDao.update(null, wrapper);

            log.info("[库存管理] 扣减库存完成: productId={}, quantity={}, affectedRows={}", productId, quantity, affectedRows);
            return affectedRows > 0;

        } catch (Exception e) {
            log.error("[库存管理] 扣减库存失败: productId={}, quantity={}, error={}", productId, quantity, e.getMessage(), e);
            throw new ConsumeProductException("扣减库存失败: " + e.getMessage());
        }
    }

    /**
     * 恢复库存
     */
    public Boolean restoreStock(Long productId, Integer quantity) {
        log.info("[库存管理] 恢复库存: productId={}, quantity={}", productId, quantity);

        try {
            if (quantity == null || quantity <= 0) {
                throw new ConsumeProductException("恢复数量不能为空或负数");
            }

            // 恢复库存
            LambdaUpdateWrapper<ConsumeProductEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ConsumeProductEntity::getProductId, productId)
                   .setSql("stock = stock + " + quantity)
                   .setSql("sales_count = CASE WHEN sales_count >= " + quantity + " THEN sales_count - " + quantity + " ELSE 0 END")
                   .set(ConsumeProductEntity::getUpdateTime, LocalDateTime.now());

            int affectedRows = productDao.update(null, wrapper);

            log.info("[库存管理] 恢复库存完成: productId={}, quantity={}, affectedRows={}", productId, quantity, affectedRows);
            return affectedRows > 0;

        } catch (Exception e) {
            log.error("[库存管理] 恢复库存失败: productId={}, quantity={}, error={}", productId, quantity, e.getMessage(), e);
            throw new ConsumeProductException("恢复库存失败: " + e.getMessage());
        }
    }

    /**
     * 查询低库存产品
     */
    public List<Map<String, Object>> getLowStockProducts() {
        log.info("[库存管理] 查询低库存产品");

        try {
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.le(ConsumeProductEntity::getStock, 10)
                   .eq(ConsumeProductEntity::getStatus, 1)
                   .orderByAsc(ConsumeProductEntity::getStock);

            List<ConsumeProductEntity> entities = productDao.selectList(wrapper);
            List<Map<String, Object>> lowStockProducts = new ArrayList<>();

            for (ConsumeProductEntity entity : entities) {
                Map<String, Object> product = new HashMap<>();
                product.put("productId", entity.getProductId());
                product.put("productName", entity.getProductName());
                product.put("productCode", entity.getProductCode());
                product.put("stock", entity.getStock());
                product.put("status", entity.getStatus());

                // 库存预警级别
                String alertLevel;
                if (entity.getStock() <= 0) {
                    alertLevel = "OUT_OF_STOCK";
                } else if (entity.getStock() <= 5) {
                    alertLevel = "CRITICAL";
                } else {
                    alertLevel = "LOW";
                }
                product.put("alertLevel", alertLevel);

                lowStockProducts.add(product);
            }

            log.info("[库存管理] 查询低库存产品完成: count={}", lowStockProducts.size());
            return lowStockProducts;

        } catch (Exception e) {
            log.error("[库存管理] 查询低库存产品失败: error={}", e.getMessage(), e);
            throw new ConsumeProductException("查询低库存产品失败: " + e.getMessage());
        }
    }

    /**
     * 获取库存统计
     */
    public Map<String, Object> getStockStatistics() {
        log.info("[库存管理] 获取库存统计");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总产品数
            LambdaQueryWrapper<ConsumeProductEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(ConsumeProductEntity::getStatus, 1);
            Long totalCount = productDao.selectCount(totalWrapper);
            statistics.put("totalProducts", totalCount);

            // 库存充足产品数
            LambdaQueryWrapper<ConsumeProductEntity> sufficientWrapper = new LambdaQueryWrapper<>();
            sufficientWrapper.eq(ConsumeProductEntity::getStatus, 1)
                           .ge(ConsumeProductEntity::getStock, 50);
            Long sufficientCount = productDao.selectCount(sufficientWrapper);
            statistics.put("sufficientStockProducts", sufficientCount);

            // 库存预警产品数
            LambdaQueryWrapper<ConsumeProductEntity> warningWrapper = new LambdaQueryWrapper<>();
            warningWrapper.eq(ConsumeProductEntity::getStatus, 1)
                         .gt(ConsumeProductEntity::getStock, 0)
                         .lt(ConsumeProductEntity::getStock, 10);
            Long warningCount = productDao.selectCount(warningWrapper);
            statistics.put("warningStockProducts", warningCount);

            // 零库存产品数
            LambdaQueryWrapper<ConsumeProductEntity> outOfStockWrapper = new LambdaQueryWrapper<>();
            outOfStockWrapper.eq(ConsumeProductEntity::getStatus, 1)
                            .le(ConsumeProductEntity::getStock, 0);
            Long outOfStockCount = productDao.selectCount(outOfStockWrapper);
            statistics.put("outOfStockProducts", outOfStockCount);

            // 计算库存预警率
            double warningRate = totalCount > 0 ? (double) (warningCount + outOfStockCount) / totalCount * 100 : 0;
            statistics.put("warningRate", Math.round(warningRate * 100.0) / 100.0);

            log.info("[库存管理] 获取库存统计完成: statistics={}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error("[库存管理] 获取库存统计失败: error={}", e.getMessage(), e);
            throw new ConsumeProductException("获取库存统计失败: " + e.getMessage());
        }
    }
}