package net.lab1024.sa.consume.strategy.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.ConsumeAreaEntity;
import net.lab1024.sa.consume.entity.ConsumeProductEntity;
import net.lab1024.sa.common.factory.StrategyMarker;
import net.lab1024.sa.consume.client.AccountKindConfigClient;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;
import net.lab1024.sa.consume.strategy.ConsumeAmountCalculator;

/**
 * 商品模式计算器策略实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 用于计算商品消费模式的消费金额
 * </p>
 * <p>
 * 业务场景：
 * - 商品扫码消费
 * - 多商品消费
 * - 商品折扣计算
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
@StrategyMarker(name = "PRODUCT", type = "CONSUME_MODE", priority = 90)
public class ProductAmountCalculator implements ConsumeAmountCalculator {

    @Resource
    private ConsumeProductDao consumeProductDao;

    @Resource
    private ConsumeAreaManager consumeAreaManager;

    @Resource
    private AccountManager accountManager;

    @Resource
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Resource
    private AccountKindConfigClient accountKindConfigClient;

    /**
     * 计算商品模式消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @param request 消费请求对象（应包含商品ID和数量）
     * @return 商品总价（单位：元）
     */
    @Override
    public BigDecimal calculate(Long accountId, String areaId, AccountEntity account, Object request) {
        log.debug("[商品策略] 计算商品模式消费金额，accountId={}, areaId={}", accountId, areaId);

        try {
            // 1. 验证区域是否支持商品模式
            ConsumeAreaEntity area = consumeAreaManager.getAreaById(areaId);
            if (area == null || area.getManageMode() == null || area.getManageMode() != 2) {
                log.warn("[商品策略] 区域不支持商品模式，areaId={}, manageMode={}",
                        areaId, area != null ? area.getManageMode() : null);
                return BigDecimal.ZERO;
            }

            // 2. 从请求对象中提取商品信息
            BigDecimal totalPrice = BigDecimal.ZERO;

            if (request instanceof ConsumeTransactionForm) {
                ConsumeTransactionForm form = (ConsumeTransactionForm) request;
                totalPrice = calculateFromForm(form, accountId, areaId);
            } else if (request instanceof ConsumeRequest) {
                ConsumeRequest consumeRequest = (ConsumeRequest) request;
                totalPrice = calculateFromRequest(consumeRequest, accountId, areaId);
            } else {
                log.warn("[商品策略] 请求对象不包含商品信息，accountId={}, areaId={}", accountId, areaId);
                return BigDecimal.ZERO;
            }

            if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[商品策略] 商品总价计算失败或为0，accountId={}, areaId={}", accountId, areaId);
                return BigDecimal.ZERO;
            }

            log.debug("[商品策略] 商品总价计算完成，accountId={}, areaId={}, totalPrice={}",
                    accountId, areaId, totalPrice);
            return totalPrice;

        } catch (Exception e) {
            log.error("[商品策略] 计算商品模式消费金额失败，accountId={}, areaId={}", accountId, areaId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 从Form对象计算商品总价
     *
     * @param form 消费表单
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 商品总价
     */
    private BigDecimal calculateFromForm(ConsumeTransactionForm form, Long accountId, String areaId) {
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 单商品模式
        if (form.getProductId() != null && !form.getProductId().isEmpty() && form.getQuantity() != null) {
            BigDecimal productPrice = calculateProductTotalPrice(
                    form.getProductId(), form.getQuantity(), accountId, areaId);
            if (productPrice.compareTo(BigDecimal.ZERO) > 0) {
                totalPrice = totalPrice.add(productPrice);
            }
        }

        // 多商品模式
        if (form.getProductIds() != null && !form.getProductIds().isEmpty()
                && form.getProductQuantities() != null) {
            for (String productId : form.getProductIds()) {
                Integer quantity = form.getProductQuantities().get(productId);
                if (quantity != null && quantity > 0) {
                    BigDecimal productPrice = calculateProductTotalPrice(productId, quantity, accountId, areaId);
                    if (productPrice.compareTo(BigDecimal.ZERO) > 0) {
                        totalPrice = totalPrice.add(productPrice);
                    }
                }
            }
        }

        return totalPrice;
    }

    /**
     * 从Request对象计算商品总价
     *
     * @param consumeRequest 消费请求
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 商品总价
     */
    private BigDecimal calculateFromRequest(ConsumeRequest consumeRequest, Long accountId, String areaId) {
        if (consumeRequest.getProductId() != null && consumeRequest.getQuantity() != null) {
            return calculateProductTotalPrice(
                    consumeRequest.getProductId(), consumeRequest.getQuantity(), accountId, areaId);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 计算单个商品总价
     *
     * @param productId 商品ID
     * @param quantity 商品数量
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @return 商品总价
     */
    private BigDecimal calculateProductTotalPrice(String productId, Integer quantity, Long accountId, String areaId) {
        try {
            // 1. 查询商品信息
            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                log.warn("[商品策略] 商品不存在，productId={}", productId);
                return BigDecimal.ZERO;
            }

            // 2. 验证商品状态
            // 修复类型兼容：getAvailable()返回Integer类型（1-可用 0-不可用）
            Integer available = product.getAvailable();
            if (available == null || available != 1) {
                log.warn("[商品策略] 商品未上架，productId={}", productId);
                return BigDecimal.ZERO;
            }

            // 3. 验证商品可售区域
            if (product.getAreaIds() != null && !product.getAreaIds().isEmpty()) {
                try {
                    List<String> allowedAreas = objectMapper.readValue(
                            product.getAreaIds(),
                            new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                    );
                    if (!allowedAreas.contains(areaId)) {
                        log.warn("[商品策略] 商品不在该区域销售，productId={}, areaId={}", productId, areaId);
                        return BigDecimal.ZERO;
                    }
                } catch (Exception e) {
                    log.warn("[商品策略] 解析商品可售区域失败，productId={}", productId, e);
                }
            }

            // 4. 获取商品价格
            BigDecimal unitPrice = product.getPrice();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("[商品策略] 商品价格无效，productId={}, price={}", productId, unitPrice);
                return BigDecimal.ZERO;
            }

            // 5. 计算商品总价 = 单价 * 数量
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity != null ? quantity : 1));

            // 6. 应用折扣规则（从账户类别获取商品折扣率）
            BigDecimal discountRate = getProductDiscountRate(accountId);
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = totalPrice.multiply(discountRate);
                BigDecimal finalPrice = totalPrice.subtract(discountAmount);
                log.debug("[商品策略] 应用商品折扣，productId={}, totalPrice={}, discountRate={}, finalPrice={}",
                        productId, totalPrice, discountRate, finalPrice);
                return finalPrice;
            }

            return totalPrice;

        } catch (Exception e) {
            log.error("[商品策略] 计算商品总价失败，productId={}, quantity={}", productId, quantity, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取商品模式折扣率
     *
     * @param accountId 账户ID
     * @return 折扣率（0-1之间）
     */
    private BigDecimal getProductDiscountRate(Long accountId) {
        try {
            // 1. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null || account.getAccountKindId() == null) {
                return BigDecimal.ZERO;
            }

            // 2. 获取账户类别信息（热路径：默认经网关，直连启用时走直连）
            net.lab1024.sa.common.dto.ResponseDTO<java.util.Map<String, Object>> accountKindResponse =
                    accountKindConfigClient.getAccountKind(account.getAccountKindId());

            if (accountKindResponse == null || !accountKindResponse.isSuccess()
                    || accountKindResponse.getData() == null) {
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> accountKind = accountKindResponse.getData();

            // 3. 获取mode_config JSON字段
            Object modeConfigObj = accountKind.get("mode_config");
            if (modeConfigObj == null) {
                return BigDecimal.ZERO;
            }

            // 4. 解析mode_config
            java.util.Map<String, Object> modeConfig;
            if (modeConfigObj instanceof String) {
                modeConfig = objectMapper.readValue((String) modeConfigObj,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (modeConfigObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) modeConfigObj;
                modeConfig = map;
            } else {
                return BigDecimal.ZERO;
            }

            // 5. 获取PRODUCT配置
            Object productObj = modeConfig.get("PRODUCT");
            if (productObj == null) {
                return BigDecimal.ZERO;
            }

            java.util.Map<String, Object> productConfig;
            if (productObj instanceof String) {
                productConfig = objectMapper.readValue((String) productObj,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            } else if (productObj instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) productObj;
                productConfig = map;
            } else {
                return BigDecimal.ZERO;
            }

            // 6. 获取discountRate（折扣率，0-1之间）
            Object discountRateObj = productConfig.get("discountRate");
            if (discountRateObj == null) {
                return BigDecimal.ZERO;
            }

            BigDecimal discountRate;
            if (discountRateObj instanceof Number) {
                discountRate = BigDecimal.valueOf(((Number) discountRateObj).doubleValue());
            } else if (discountRateObj instanceof String) {
                discountRate = new BigDecimal((String) discountRateObj);
            } else {
                return BigDecimal.ZERO;
            }

            // 7. 验证折扣率范围（0-1之间）
            if (discountRate.compareTo(BigDecimal.ZERO) < 0
                    || discountRate.compareTo(BigDecimal.ONE) > 0) {
                log.warn("[商品策略] 折扣率超出范围，accountId={}, discountRate={}", accountId, discountRate);
                return BigDecimal.ZERO;
            }

            return discountRate;

        } catch (Exception e) {
            log.error("[商品策略] 获取商品模式折扣率失败，accountId={}", accountId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 获取策略支持的消费模式
     *
     * @return 消费模式：PRODUCT
     */
    @Override
    public String getConsumeMode() {
        return "PRODUCT";
    }

    /**
     * 验证商品模式是否支持
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param account 账户信息
     * @return 是否支持
     */
    @Override
    public boolean isSupported(Long accountId, String areaId, AccountEntity account) {
        try {
            // 1. 验证区域是否存在
            if (areaId == null || areaId.isEmpty()) {
                return false;
            }

            ConsumeAreaEntity area = consumeAreaManager.getAreaById(areaId);
            if (area == null) {
                log.warn("[商品策略] 区域不存在，areaId={}", areaId);
                return false;
            }

            // 2. 验证区域经营模式是否支持商品模式
            // 超市制(2)和混合模式(3)支持商品模式
            Integer manageMode = area.getManageMode();
            if (manageMode == null) {
                return false;
            }

            boolean supported = manageMode == 2 || manageMode == 3;
            log.debug("[商品策略] 商品模式支持验证，areaId={}, manageMode={}, supported={}",
                    areaId, manageMode, supported);
            return supported;

        } catch (Exception e) {
            log.error("[商品策略] 验证商品模式支持失败，accountId={}, areaId={}", accountId, areaId, e);
            return false;
        }
    }
}




