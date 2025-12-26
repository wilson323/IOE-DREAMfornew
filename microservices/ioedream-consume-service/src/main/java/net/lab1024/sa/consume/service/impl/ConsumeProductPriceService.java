package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.entity.ConsumeProductEntity;
import net.lab1024.sa.consume.exception.ConsumeProductException;

import lombok.extern.slf4j.Slf4j;

/**
 * 消费产品价格服务
 * <p>
 * 负责产品的价格管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeProductPriceService {

    @Resource
    private ConsumeProductDao productDao;

    /**
     * 计算实际价格
     */
    public BigDecimal calculateActualPrice (Long productId, BigDecimal discountRate) {
        log.info ("[价格管理] 计算实际价格: productId={}, discountRate={}", productId, discountRate);

        try {
            if (productId == null) {
                throw ConsumeProductException.invalidParameter ("产品ID不能为空");
            }
            if (discountRate == null || discountRate.compareTo (BigDecimal.ZERO) < 0
                    || discountRate.compareTo (BigDecimal.valueOf (100)) > 0) {
                throw ConsumeProductException.invalidParameter ("折扣率必须在0-100之间");
            }

            ConsumeProductEntity entity = productDao.selectById (productId);
            if (entity == null) {
                throw ConsumeProductException.notFound (productId);
            }

            BigDecimal salePrice = entity.getSalePrice ();
            if (salePrice == null) {
                salePrice = BigDecimal.ZERO;
            }

            BigDecimal discountAmount = salePrice.multiply (discountRate).divide (BigDecimal.valueOf (100));
            BigDecimal actualPrice = salePrice.subtract (discountAmount);

            // 确保价格不小于成本价
            BigDecimal costPrice = entity.getCostPrice () != null ? entity.getCostPrice () : BigDecimal.ZERO;
            if (actualPrice.compareTo (costPrice) < 0) {
                actualPrice = costPrice;
            }

            // 确保价格不为负数
            if (actualPrice.compareTo (BigDecimal.ZERO) < 0) {
                actualPrice = BigDecimal.ZERO;
            }

            log.info ("[价格管理] 计算实际价格完成: productId={}, salePrice={}, discountRate={}, actualPrice={}", productId,
                    salePrice, discountRate, actualPrice);

            return actualPrice.setScale (2, BigDecimal.ROUND_HALF_UP);

        } catch (Exception e) {
            log.error ("[价格管理] 计算实际价格失败: productId={}, discountRate={}, error={}", productId, discountRate,
                    e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("计算实际价格失败: " + e.getMessage ());
        }
    }

    /**
     * 批量更新价格
     */
    public Map<String, Object> batchUpdatePrice (List<Map<String, Object>> priceUpdates) {
        log.info ("[价格管理] 批量更新价格: updateCount={}", priceUpdates.size ());

        Map<String, Object> result = new HashMap<> ();
        int successCount = 0;
        int failureCount = 0;
        List<String> failedProducts = new ArrayList<> ();

        try {
            for (Map<String, Object> update : priceUpdates) {
                try {
                    Long productId = Long.valueOf (update.get ("productId").toString ());
                    BigDecimal basePrice = new BigDecimal (update.get ("basePrice").toString ());
                    BigDecimal salePrice = new BigDecimal (update.get ("salePrice").toString ());
                    BigDecimal costPrice = new BigDecimal (update.get ("costPrice").toString ());

                    Boolean success = updateProductPrice (productId, basePrice, salePrice, costPrice);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                        failedProducts.add (productId.toString ());
                    }

                } catch (Exception e) {
                    failureCount++;
                    failedProducts.add (update.get ("productId").toString ());
                    log.warn ("[价格管理] 批量更新价格时跳过失败项: update={}, error={}", update, e.getMessage ());
                }
            }

            result.put ("totalCount", priceUpdates.size ());
            result.put ("successCount", successCount);
            result.put ("failureCount", failureCount);
            result.put ("failedProducts", failedProducts);

            log.info ("[价格管理] 批量更新价格完成: totalCount={}, successCount={}, failureCount={}", priceUpdates.size (),
                    successCount, failureCount);

            return result;

        } catch (Exception e) {
            log.error ("[价格管理] 批量更新价格失败: priceUpdates={}, error={}", priceUpdates, e.getMessage (), e);
            throw ConsumeProductException.businessRuleViolation ("批量更新价格失败: " + e.getMessage ());
        }
    }

    /**
     * 验证价格
     */
    public Boolean validatePrice (Long productId, BigDecimal basePrice, BigDecimal salePrice, BigDecimal costPrice) {
        log.info ("[价格管理] 验证价格: productId={}, basePrice={}, salePrice={}, costPrice={}", productId, basePrice,
                salePrice, costPrice);

        try {
            // 1. 检查产品是否存在
            ConsumeProductEntity entity = productDao.selectById (productId);
            if (entity == null) {
                throw new ConsumeProductException ("产品不存在");
            }

            // 2. 检查价格基本规则
            if (basePrice != null && basePrice.compareTo (BigDecimal.ZERO) <= 0) {
                throw new ConsumeProductException ("原价必须大于0");
            }
            if (salePrice != null && salePrice.compareTo (BigDecimal.ZERO) <= 0) {
                throw new ConsumeProductException ("售价必须大于0");
            }
            if (costPrice != null && costPrice.compareTo (BigDecimal.ZERO) < 0) {
                throw new ConsumeProductException ("成本价不能为负数");
            }

            // 3. 检查价格关系
            if (basePrice != null && salePrice != null && salePrice.compareTo (basePrice) > 0) {
                throw new ConsumeProductException ("售价不能高于原价");
            }
            if (costPrice != null && salePrice != null && salePrice.compareTo (costPrice) < 0) {
                throw new ConsumeProductException ("售价不能低于成本价");
            }

            // 4. 检查折扣率合理性
            if (basePrice != null && salePrice != null && basePrice.compareTo (BigDecimal.ZERO) > 0) {
                BigDecimal discountRate = BigDecimal.valueOf (100).subtract (
                        salePrice.multiply (BigDecimal.valueOf (100)).divide (basePrice, 2, BigDecimal.ROUND_HALF_UP));
                if (discountRate.compareTo (BigDecimal.valueOf (90)) > 0) {
                    throw new ConsumeProductException ("折扣率不能超过90%");
                }
            }

            log.info ("[价格管理] 价格验证通过: productId={}", productId);
            return true;

        } catch (Exception e) {
            log.error ("[价格管理] 价格验证失败: productId={}, error={}", productId, e.getMessage (), e);
            if (e instanceof ConsumeProductException) {
                throw e;
            }
            throw new ConsumeProductException ("价格验证失败: " + e.getMessage ());
        }
    }

    /**
     * 更新产品价格
     */
    private Boolean updateProductPrice (Long productId, BigDecimal basePrice, BigDecimal salePrice,
            BigDecimal costPrice) {
        log.info ("[价格管理] 更新产品价格: productId={}, basePrice={}, salePrice={}, costPrice={}", productId, basePrice,
                salePrice, costPrice);

        try {
            // 1. 验证价格
            validatePrice (productId, basePrice, salePrice, costPrice);

            // 2. 更新价格
            LambdaUpdateWrapper<ConsumeProductEntity> wrapper = new LambdaUpdateWrapper<> ();
            wrapper.eq (ConsumeProductEntity::getProductId, productId);

            if (basePrice != null) {
                wrapper.set (ConsumeProductEntity::getBasePrice, basePrice);
            }
            if (salePrice != null) {
                wrapper.set (ConsumeProductEntity::getSalePrice, salePrice);
            }
            if (costPrice != null) {
                wrapper.set (ConsumeProductEntity::getCostPrice, costPrice);
            }

            wrapper.set (ConsumeProductEntity::getUpdateTime, LocalDateTime.now ());

            int affectedRows = productDao.update (null, wrapper);

            log.info ("[价格管理] 更新产品价格完成: productId={}, affectedRows={}", productId, affectedRows);
            return affectedRows > 0;

        } catch (Exception e) {
            log.error ("[价格管理] 更新产品价格失败: productId={}, error={}", productId, e.getMessage (), e);
            throw new ConsumeProductException ("更新产品价格失败: " + e.getMessage ());
        }
    }

    /**
     * 获取价格统计
     */
    public Map<String, Object> getPriceStatistics () {
        log.info ("[价格管理] 获取价格统计");

        try {
            Map<String, Object> statistics = new HashMap<> ();

            // 查询所有在售产品
            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<> ();
            wrapper.eq (ConsumeProductEntity::getStatus, 1);
            List<ConsumeProductEntity> products = productDao.selectList (wrapper);

            if (products.isEmpty ()) {
                statistics.put ("totalProducts", 0);
                statistics.put ("avgSalePrice", 0);
                statistics.put ("maxSalePrice", 0);
                statistics.put ("minSalePrice", 0);
                return statistics;
            }

            // 计算价格统计
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal maxPrice = BigDecimal.ZERO;
            BigDecimal minPrice = BigDecimal.valueOf (Long.MAX_VALUE);
            int validPriceCount = 0;

            for (ConsumeProductEntity product : products) {
                BigDecimal salePrice = product.getSalePrice ();
                if (salePrice != null && salePrice.compareTo (BigDecimal.ZERO) > 0) {
                    totalPrice = totalPrice.add (salePrice);
                    maxPrice = maxPrice.compareTo (salePrice) > 0 ? maxPrice : salePrice;
                    minPrice = minPrice.compareTo (salePrice) > 0 ? salePrice : minPrice;
                    validPriceCount++;
                }
            }

            statistics.put ("totalProducts", products.size ());
            statistics.put ("validPriceProducts", validPriceCount);
            statistics.put ("avgSalePrice",
                    validPriceCount > 0
                            ? totalPrice.divide (BigDecimal.valueOf (validPriceCount), 2, BigDecimal.ROUND_HALF_UP)
                            : BigDecimal.ZERO);
            statistics.put ("maxSalePrice",
                    maxPrice.compareTo (BigDecimal.valueOf (Long.MAX_VALUE)) == 0 ? BigDecimal.ZERO : maxPrice);
            statistics.put ("minSalePrice",
                    minPrice.compareTo (BigDecimal.valueOf (Long.MAX_VALUE)) == 0 ? BigDecimal.ZERO : minPrice);

            // 价格区间统计
            List<Map<String, Object>> priceRangeStatistics = getPriceRangeStatistics (products);
            statistics.put ("priceRangeStatistics", priceRangeStatistics);

            log.info ("[价格管理] 获取价格统计完成: statistics={}", statistics);
            return statistics;

        } catch (Exception e) {
            log.error ("[价格管理] 获取价格统计失败: error={}", e.getMessage (), e);
            throw new ConsumeProductException ("获取价格统计失败: " + e.getMessage ());
        }
    }

    /**
     * 价格区间统计
     */
    private List<Map<String, Object>> getPriceRangeStatistics (List<ConsumeProductEntity> products) {
        List<Map<String, Object>> statistics = new ArrayList<> ();

        // 定义价格区间
        Map<String, BigDecimal> priceRanges = Map.of ("0-50", BigDecimal.valueOf (50), "50-100",
                BigDecimal.valueOf (100), "100-200", BigDecimal.valueOf (200), "200-500", BigDecimal.valueOf (500),
                "500+", BigDecimal.valueOf (Long.MAX_VALUE));

        for (Map.Entry<String, BigDecimal> entry : priceRanges.entrySet ()) {
            String range = entry.getKey ();
            BigDecimal maxValue = entry.getValue ();

            int count = 0;
            for (ConsumeProductEntity product : products) {
                BigDecimal salePrice = product.getSalePrice ();
                if (salePrice != null && salePrice.compareTo (BigDecimal.ZERO) > 0) {
                    if (maxValue.compareTo (BigDecimal.valueOf (Long.MAX_VALUE)) == 0) {
                        if (salePrice.compareTo (BigDecimal.valueOf (500)) > 0) {
                            count++;
                        }
                    } else {
                        if (salePrice.compareTo (maxValue) <= 0) {
                            count++;
                        }
                    }
                }
            }

            Map<String, Object> rangeStat = new HashMap<> ();
            rangeStat.put ("range", range);
            rangeStat.put ("count", count);
            rangeStat.put ("percentage", products.size () > 0 ? (double) count / products.size () * 100 : 0);
            statistics.add (rangeStat);
        }

        return statistics;
    }
}
