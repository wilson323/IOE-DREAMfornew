package net.lab1024.sa.consume.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.consume.strategy.ConsumeModeStrategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品模式策略
 *
 * 用户购买商品，按商品价格列表计算总金额
 *
 * 配置参数：
 * - supportMultiple: 是否支持多商品
 * - maxProductCount: 最大商品数量
 *
 * 业务参数：
 * - products: 商品列表 [{"productId": 1, "productName": "商品A", "price": 15.00, "quantity": 2}]
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class ProductStrategy implements ConsumeModeStrategy {

    @Override
    public String getModeType() {
        return "PRODUCT";
    }

    @Override
    public ConsumeResult calculateAmount(ConsumeAccountEntity account,
                                         AreaEntity area,
                                         ModeConfig modeConfig,
                                         Map<String, Object> params) {
        log.info("[商品模式] 开始计算: accountId={}, areaId={}", account.getAccountId(), area.getAreaId());

        try {
            // 获取商品列表
            Object productsObj = params.get("products");
            if (productsObj == null) {
                return ConsumeResult.failure("缺少商品列表参数");
            }

            List<Map<String, Object>> products = (List<Map<String, Object>>) productsObj;
            if (products == null || products.isEmpty()) {
                return ConsumeResult.failure("商品列表不能为空");
            }

            // 获取配置
            ModeConfig.ProductConfig config = modeConfig.getProduct();
            if (config == null) {
                return ConsumeResult.failure("商品模式配置不存在");
            }

            // 验证商品数量
            if (config.getMaxProductCount() != null && products.size() > config.getMaxProductCount()) {
                return ConsumeResult.failure("商品数量超过限制: " + config.getMaxProductCount());
            }

            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (Map<String, Object> product : products) {
                Object priceObj = product.get("price");
                Object quantityObj = product.get("quantity");

                if (priceObj == null || quantityObj == null) {
                    return ConsumeResult.failure("商品信息不完整: 缺少价格或数量");
                }

                BigDecimal price = new BigDecimal(priceObj.toString());
                int quantity = ((Number) quantityObj).intValue();

                if (price.compareTo(BigDecimal.ZERO) < 0 || quantity <= 0) {
                    return ConsumeResult.failure("商品价格或数量无效");
                }

                BigDecimal itemAmount = price.multiply(BigDecimal.valueOf(quantity));
                totalAmount = totalAmount.add(itemAmount);
            }

            log.info("[商品模式] 计算成功: productCount={}, totalAmount={}", products.size(), totalAmount);
            return ConsumeResult.success(totalAmount, "计算成功");

        } catch (Exception e) {
            log.error("[商品模式] 计算异常: error={}", e.getMessage(), e);
            return ConsumeResult.failure("计算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params, ModeConfig modeConfig) {
        // 必须包含商品列表
        return params.containsKey("products");
    }
}
