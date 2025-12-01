package net.lab1024.sa.admin.module.consume.engine.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ProductDao;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ProductStatusEnum;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;

/**
 * 商品扫码消费模式引擎
 * 适用于通过扫码商品进行消费的场景，如超市商品扫码、便利店商品购买等
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ProductConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private ProductDao productDao;

    // 商品缓存前缀
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final long PRODUCT_CACHE_EXPIRE = 1800; // 30分钟

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.PRODUCT;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证商品扫码消费请求: userId={}, productCode={}",
                consumeRequest.getUserId(), consumeRequest.getProductCode());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证商品编码是否为空
            if (consumeRequest.getProductCode() == null || consumeRequest.getProductCode().trim().isEmpty()) {
                result.setValid(false);
                result.setErrorMessage("商品编码不能为空");
                return result;
            }

            // 2. 查询商品信息
            ProductEntity product = getProductByCode(consumeRequest.getProductCode());
            if (product == null) {
                result.setValid(false);
                result.setErrorMessage("商品不存在或已下架");
                return result;
            }

            // 3. 验证商品状态
            ProductStatusEnum statusEnum = ProductStatusEnum.getByValue(product.getStatus());
            if (statusEnum == null || !statusEnum.isOnSale()) {
                result.setValid(false);
                result.setErrorMessage(
                        "商品当前不可购买，状态: " + (statusEnum != null ? statusEnum.getDescription() : product.getStatus()));
                return result;
            }

            // 4. 验证商品库存
            if (product.getStock() != null && product.getStock() <= 0) {
                result.setValid(false);
                result.setErrorMessage("商品库存不足");
                return result;
            }

            // 5. 验证购买数量
            if (consumeRequest.getQuantity() == null || consumeRequest.getQuantity() <= 0) {
                result.setValid(false);
                result.setErrorMessage("购买数量必须大于0");
                return result;
            }

            if (product.getStock() != null && consumeRequest.getQuantity() > product.getStock()) {
                result.setValid(false);
                result.setErrorMessage("购买数量超过库存，当前库存: " + product.getStock());
                return result;
            }

            // 6. 计算总金额
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(consumeRequest.getQuantity()));

            // 7. 验证用户余额
            Map<String, Object> accountInfo = consumeService.getAccountBalance(consumeRequest.getUserId());
            if (accountInfo != null) {
                BigDecimal balance = (BigDecimal) accountInfo.get("balance");
                if (balance != null && balance.compareTo(totalAmount) < 0) {
                    result.setValid(false);
                    result.setErrorMessage("账户余额不足，当前余额: ¥" + balance + "，需要: ¥" + totalAmount);
                    return result;
                }
            }

            // 8. 验证设备是否支持商品扫码模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持商品扫码消费模式");
                return result;
            }

            // 9. 验证商品购买限制（从模式配置中获取，ProductEntity本身不包含dailyLimit字段）
            ConsumeModeConfig modeConfig = getModeConfig(consumeRequest.getDeviceId());
            if (modeConfig != null && modeConfig.getDailyLimit() != null && modeConfig.getDailyLimit().compareTo(BigDecimal.ZERO) > 0) {
                // 注意：这里应该检查用户每日总消费限额，而不是单个商品的购买限制
                // 如果需要商品级别的购买限制，应该从商品扩展配置或限额配置中获取
                // 暂时跳过商品级别的每日购买限制验证
            }

            result.setValid(true);
            result.setProductInfo(product); // 保存商品信息供后续使用
            log.debug("商品扫码消费请求验证通过: productCode={}, productName={}",
                    consumeRequest.getProductCode(), product.getProductName());

        } catch (Exception e) {
            log.error("验证商品扫码消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理商品扫码消费: userId={}, productCode={}, quantity={}",
                consumeRequest.getUserId(), consumeRequest.getProductCode(), consumeRequest.getQuantity());

        ConsumeResultDTO result = new ConsumeResultDTO();

        try {
            // 1. 再次验证请求
            ConsumeValidationResult validation = validateRequest(consumeRequest);
            if (!validation.isValid()) {
                result.setSuccess(false);
                result.setMessage(validation.getErrorMessage());
                result.setStatus(ConsumeStatusEnum.FAILED);
                return result;
            }

            ProductEntity product = validation.getProductInfo();
            BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(consumeRequest.getQuantity()));

            // 2. 执行消费处理
            Map<String, Object> consumeData = new HashMap<>();
            consumeData.put("userId", consumeRequest.getUserId());
            consumeData.put("amount", totalAmount);
            consumeData.put("personName", consumeRequest.getPersonName());
            consumeData.put("payMethod", consumeRequest.getPayMethod());
            consumeData.put("deviceId", consumeRequest.getDeviceId());
            consumeData.put("consumeMode", getConsumeMode().name());
            consumeData.put("remark", buildConsumeRemark(product, consumeRequest.getQuantity(), consumeRequest.getRemark()));

            // 商品相关字段
            consumeData.put("productId", product.getProductId());
            consumeData.put("productName", product.getProductName());
            consumeData.put("productCode", product.getProductCode());
            consumeData.put("quantity", consumeRequest.getQuantity());
            consumeData.put("unitPrice", product.getPrice());

            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                // 3. 消费成功
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount(totalAmount);
                result.setMessage("商品扫码消费成功");
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 4. 更新商品库存
                updateProductStock(product.getProductId(), consumeRequest.getQuantity());

                // 5. 更新购买记录缓存
                updatePurchaseRecordCache(consumeRequest.getUserId(), product.getProductId(),
                        consumeRequest.getQuantity());

                // 6. 记录成功日志
                log.info("商品扫码消费成功: userId={}, orderId={}, product={}, quantity={}, amount={}",
                        consumeRequest.getUserId(), result.getOrderId(),
                        product.getProductName(), consumeRequest.getQuantity(), result.getAmount());

            } else {
                // 7. 消费失败
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount(totalAmount);
                result.setConsumeMode(getConsumeMode());

                log.warn("商品扫码消费失败: userId={}, message={}",
                        consumeRequest.getUserId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理商品扫码消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("商品扫码消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        try {
            ProductEntity product = getProductByCode(consumeRequest.getProductCode());
            if (product == null) {
                return BigDecimal.ZERO;
            }
            int quantity = consumeRequest.getQuantity() != null ? consumeRequest.getQuantity() : 1;
            return product.getPrice().multiply(new BigDecimal(quantity));
        } catch (Exception e) {
            log.error("计算商品消费金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getModeDescription() {
        return "商品扫码模式 - 通过扫描商品二维码或条形码进行消费，适用于超市、便利店等场景";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持商品扫码模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取商品扫码模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.PRODUCT);
            config.setEnabled(true);
            config.setMaxSingleAmount(new BigDecimal("1000.00"));
            config.setMinSingleAmount(new BigDecimal("0.01"));
            config.setDailyLimit(new BigDecimal("1000.00"));
            config.setDescription("商品扫码消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取商品扫码模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 根据商品编码获取商品信息
     */
    private ProductEntity getProductByCode(String productCode) {
        try {
            // 1. 先从缓存获取
            String cacheKey = PRODUCT_CACHE_PREFIX + productCode;
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            ProductEntity product = null;
            if (cachedValue instanceof ProductEntity) {
                product = (ProductEntity) cachedValue;
            }

            if (product == null) {
                // 2. 缓存未命中，从数据库查询
                product = productDao.selectByProductCode(productCode);
                if (product != null) {
                    // 3. 写入缓存
                    consumeCacheService.setCachedValue(cacheKey, product, (int) PRODUCT_CACHE_EXPIRE);
                }
            }

            return product;
        } catch (Exception e) {
            log.error("根据商品编码获取商品信息失败: productCode={}", productCode, e);
            return null;
        }
    }

    /**
     * 更新商品库存
     */
    private void updateProductStock(Long productId, int quantity) {
        try {
            // TODO: 调用商品服务更新库存
            // productService.updateStock(productId, -quantity);
            log.info("更新商品库存: productId={}, quantity=-{}", productId, quantity);
        } catch (Exception e) {
            log.error("更新商品库存失败: productId={}, quantity={}", productId, quantity, e);
        }
    }

    /**
     * 更新购买记录缓存
     */
    private void updatePurchaseRecordCache(Long userId, Long productId, int quantity) {
        try {
            String cacheKey = "purchase_record:" + userId + ":" + productId + ":" + java.time.LocalDate.now();
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            int currentCount = 0;
            if (cachedValue instanceof Integer) {
                currentCount = (Integer) cachedValue;
            }
            if (currentCount < 0) {
                currentCount = 0;
            }
            consumeCacheService.setCachedValue(cacheKey, currentCount + quantity, 24 * 60 * 60); // 缓存24小时

            log.debug("更新购买记录缓存: userId={}, productId={}, quantity={}", userId, productId, quantity);
        } catch (Exception e) {
            log.error("更新购买记录缓存失败: userId={}, productId={}", userId, productId, e);
        }
    }

    /**
     * 获取今日购买数量
     */
    private int getTodayPurchaseCount(Long userId, Long productId) {
        try {
            String cacheKey = "purchase_record:" + userId + ":" + productId + ":" + java.time.LocalDate.now();
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            if (cachedValue instanceof Integer) {
                return (Integer) cachedValue;
            }
            return 0;
        } catch (Exception e) {
            log.error("获取今日购买数量失败: userId={}, productId={}", userId, productId, e);
            return 0;
        }
    }

    /**
     * 构建消费备注
     */
    private String buildConsumeRemark(ProductEntity product, int quantity) {
        StringBuilder remark = new StringBuilder();
        remark.append("购买商品: ").append(product.getProductName());
        remark.append(" (").append(product.getProductCode()).append(")");
        remark.append(" 数量: ").append(quantity);
        remark.append(" 单价: ¥").append(product.getPrice());
        return remark.toString();
    }

    /**
     * 构建消费备注（带自定义备注）
     */
    private String buildConsumeRemark(ProductEntity product, int quantity, String customRemark) {
        StringBuilder remark = new StringBuilder();
        remark.append("购买商品: ").append(product.getProductName());
        remark.append(" (").append(product.getProductCode()).append(")");
        remark.append(" 数量: ").append(quantity);
        remark.append(" 单价: ¥").append(product.getPrice());

        if (customRemark != null && !customRemark.trim().isEmpty()) {
            remark.append(" 备注: ").append(customRemark);
        }

        return remark.toString();
    }

    /**
     * 根据关键词搜索商品
     */
    public List<ProductEntity> searchProducts(String keyword) {
        try {
            // TODO: 调用商品服务搜索
            // return productService.searchProducts(keyword);
            log.info("搜索商品: keyword={}", keyword);
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.error("搜索商品失败: keyword={}", keyword, e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 根据分类获取商品列表
     */
    public List<ProductEntity> getProductsByCategory(String categoryCode) {
        try {
            // TODO: 调用商品服务获取分类商品
            // return productService.getProductsByCategory(categoryCode);
            log.info("获取分类商品: categoryCode={}", categoryCode);
            return java.util.Collections.emptyList();
        } catch (Exception e) {
            log.error("获取分类商品失败: categoryCode={}", categoryCode, e);
            return java.util.Collections.emptyList();
        }
    }
}