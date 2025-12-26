package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
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
import net.lab1024.sa.consume.exception.ConsumeProductException;

/**
 * 消费产品验证服务
 * <p>
 * 负责产品的数据验证功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ConsumeProductValidationService {

    @Resource
    private ConsumeProductDao productDao;

    /**
     * 检查产品编码唯一性
     */
    public Boolean checkCodeUnique(String productCode, Long excludeId) {
        log.info("[产品验证] 检查产品编码唯一性: productCode={}, excludeId={}", productCode, excludeId);

        try {
            if (productCode == null || productCode.trim().isEmpty()) {
                throw new ConsumeProductException("产品编码不能为空");
            }

            LambdaQueryWrapper<ConsumeProductEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeProductEntity::getProductCode, productCode.trim());

            if (excludeId != null) {
                wrapper.ne(ConsumeProductEntity::getProductId, excludeId);
            }

            Long count = productDao.selectCount(wrapper);
            boolean isUnique = count == 0;

            log.info("[产品验证] 产品编码唯一性检查完成: productCode={}, unique={}", productCode, isUnique);
            return isUnique;

        } catch (Exception e) {
            log.error("[产品验证] 检查产品编码唯一性失败: productCode={}, excludeId={}, error={}", productCode, excludeId, e.getMessage(), e);
            throw new ConsumeProductException("检查产品编码唯一性失败: " + e.getMessage());
        }
    }

    /**
     * 检查产品是否可以删除
     */
    public Map<String, Object> checkCanDelete(Long productId) {
        log.info("[产品验证] 检查产品是否可以删除: {}", productId);

        Map<String, Object> result = new HashMap<>();

        try {
            if (productId == null) {
                throw new ConsumeProductException("产品ID不能为空");
            }

            ConsumeProductEntity entity = productDao.selectById(productId);
            if (entity == null) {
                throw new ConsumeProductException("产品不存在");
            }

            result.put("canDelete", true);
            result.put("reason", "产品可以删除");

            // 这里可以添加更多的删除前检查逻辑
            // 例如：
            // 1. 检查是否有未完成的订单
            // 2. 检查是否有库存记录
            // 3. 检查是否有关联的促销活动

            log.info("[产品验证] 产品删除检查完成: productId={}, canDelete={}", productId, result.get("canDelete"));
            return result;

        } catch (Exception e) {
            log.error("[产品验证] 检查产品是否可以删除失败: productId={}, error={}", productId, e.getMessage(), e);

            result.put("canDelete", false);
            result.put("reason", "检查删除条件时发生错误: " + e.getMessage());

            return result;
        }
    }

    /**
     * 检查产品是否可以销售
     */
    public Boolean checkCanSale(Long productId) {
        log.info("[产品验证] 检查产品是否可以销售: {}", productId);

        try {
            if (productId == null) {
                throw new ConsumeProductException("产品ID不能为空");
            }

            ConsumeProductEntity entity = productDao.selectById(productId);
            if (entity == null) {
                throw new ConsumeProductException("产品不存在");
            }

            // 检查状态
            if (entity.getStatus() == null || entity.getStatus() != 1) {
                log.warn("[产品验证] 产品状态不允许销售: productId={}, status={}", productId, entity.getStatus());
                return false;
            }

            // 检查库存
            if (entity.getStock() == null || entity.getStock() <= 0) {
                log.warn("[产品验证] 产品库存不足: productId={}, stock={}", productId, entity.getStock());
                return false;
            }

            // 检查价格
            if (entity.getSalePrice() == null || entity.getSalePrice().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[产品验证] 产品价格无效: productId={}, salePrice={}", productId, entity.getSalePrice());
                return false;
            }

            log.info("[产品验证] 产品销售检查通过: productId={}", productId);
            return true;

        } catch (Exception e) {
            log.error("[产品验证] 检查产品是否可以销售失败: productId={}, error={}", productId, e.getMessage(), e);
            throw new ConsumeProductException("检查产品销售条件失败: " + e.getMessage());
        }
    }

    /**
     * 检查产品是否可以打折
     */
    public Boolean checkCanDiscount(Long productId, BigDecimal discountRate) {
        log.info("[产品验证] 检查产品是否可以打折: productId={}, discountRate={}", productId, discountRate);

        try {
            if (productId == null) {
                throw new ConsumeProductException("产品ID不能为空");
            }
            if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ConsumeProductException("折扣率必须在0-100之间");
            }

            ConsumeProductEntity entity = productDao.selectById(productId);
            if (entity == null) {
                throw new ConsumeProductException("产品不存在");
            }

            // 检查产品状态
            if (entity.getStatus() == null || entity.getStatus() != 1) {
                throw new ConsumeProductException("只有上架状态的产品才能打折");
            }

            // 检查价格
            BigDecimal basePrice = entity.getBasePrice();
            BigDecimal salePrice = entity.getSalePrice();
            if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ConsumeProductException("产品原价无效，无法计算折扣");
            }

            // 检查折扣率
            if (discountRate.compareTo(BigDecimal.valueOf(90)) > 0) {
                throw new ConsumeProductException("折扣率不能超过90%");
            }

            // 检查折扣后价格
            BigDecimal discountedPrice = basePrice.multiply(BigDecimal.valueOf(100).subtract(discountRate)).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            if (discountedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ConsumeProductException("折扣后价格不能为0或负数");
            }

            // 检查成本价
            BigDecimal costPrice = entity.getCostPrice();
            if (costPrice != null && discountedPrice.compareTo(costPrice) < 0) {
                throw new ConsumeProductException("折扣后价格不能低于成本价");
            }

            log.info("[产品验证] 产品折扣检查通过: productId={}, discountRate={}", productId, discountRate);
            return true;

        } catch (Exception e) {
            log.error("[产品验证] 检查产品是否可以打折失败: productId={}, discountRate={}, error={}", productId, discountRate, e.getMessage(), e);
            if (e instanceof ConsumeProductException) {
                throw e;
            }
            throw new ConsumeProductException("检查产品折扣条件失败: " + e.getMessage());
        }
    }

    /**
     * 验证产品数据完整性
     */
    public Map<String, Object> validateProductData(Map<String, Object> productData) {
        log.info("[产品验证] 验证产品数据完整性: {}", productData.keySet());

        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // 1. 必填字段检查
            if (!productData.containsKey("productName") || productData.get("productName") == null ||
                ((String) productData.get("productName")).trim().isEmpty()) {
                errors.add("产品名称不能为空");
            }

            if (!productData.containsKey("productCode") || productData.get("productCode") == null ||
                ((String) productData.get("productCode")).trim().isEmpty()) {
                errors.add("产品编码不能为空");
            }

            if (!productData.containsKey("categoryId") || productData.get("categoryId") == null) {
                errors.add("产品分类不能为空");
            }

            // 2. 数值字段检查
            try {
                if (productData.containsKey("basePrice")) {
                    BigDecimal basePrice = new BigDecimal(productData.get("basePrice").toString());
                    if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("原价必须大于0");
                    }
                }

                if (productData.containsKey("salePrice")) {
                    BigDecimal salePrice = new BigDecimal(productData.get("salePrice").toString());
                    if (salePrice.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("售价必须大于0");
                    }

                    // 检查售价与原价的关系
                    if (productData.containsKey("basePrice")) {
                        BigDecimal basePrice = new BigDecimal(productData.get("basePrice").toString());
                        if (salePrice.compareTo(basePrice) > 0) {
                            warnings.add("售价高于原价，建议检查");
                        }
                    }
                }

                if (productData.containsKey("costPrice")) {
                    BigDecimal costPrice = new BigDecimal(productData.get("costPrice").toString());
                    if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                        errors.add("成本价不能为负数");
                    }

                    // 检查售价与成本价的关系
                    if (productData.containsKey("salePrice")) {
                        BigDecimal salePrice = new BigDecimal(productData.get("salePrice").toString());
                        if (costPrice.compareTo(BigDecimal.ZERO) > 0 && salePrice.compareTo(costPrice) < 0) {
                            errors.add("售价不能低于成本价");
                        }
                    }
                }

                if (productData.containsKey("stock")) {
                    Integer stock = Integer.valueOf(productData.get("stock").toString());
                    if (stock < 0) {
                        errors.add("库存不能为负数");
                    }
                }

            } catch (NumberFormatException e) {
                errors.add("数值字段格式错误: " + e.getMessage());
            }

            // 3. 业务逻辑检查
            String productCode = (String) productData.get("productCode");
            if (productCode != null && !productCode.trim().isEmpty()) {
                boolean isUnique = checkCodeUnique(productCode,
                        productData.containsKey("productId") ? Long.valueOf(productData.get("productId").toString()) : null);
                if (!isUnique) {
                    errors.add("产品编码已存在");
                }
            }

            // 4. 构建验证结果
            validation.put("isValid", errors.isEmpty());
            validation.put("errors", errors);
            validation.put("warnings", warnings);
            validation.put("errorCount", errors.size());
            validation.put("warningCount", warnings.size());

            log.info("[产品验证] 产品数据验证完成: isValid={}, errorCount={}, warningCount={}",
                    validation.get("isValid"), validation.get("errorCount"), validation.get("warningCount"));

        } catch (Exception e) {
            log.error("[产品验证] 验证产品数据完整性失败: error={}", e.getMessage(), e);
            errors.add("验证过程发生异常: " + e.getMessage());
            validation.put("isValid", false);
            validation.put("errors", errors);
            validation.put("warnings", warnings);
            validation.put("errorCount", errors.size());
            validation.put("warningCount", warnings.size());
        }

        return validation;
    }
}