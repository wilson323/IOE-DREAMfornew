package net.lab1024.sa.admin.module.consume.engine.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
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
import net.lab1024.sa.admin.module.consume.domain.enums.OrderingStatusEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ProductStatusEnum;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;

/**
 * 订餐模式消费引擎
 * 适用于食堂订餐、餐厅点餐等场景，支持菜单选择、份数选择、订餐时间限制等
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class OrderingConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private ProductDao productDao;

    // 订餐时间配置
    private static final LocalTime BREAKFAST_START = LocalTime.of(6, 0);
    private static final LocalTime BREAKFAST_END = LocalTime.of(9, 30);
    private static final LocalTime LUNCH_START = LocalTime.of(10, 30);
    private static final LocalTime LUNCH_END = LocalTime.of(14, 0);
    private static final LocalTime DINNER_START = LocalTime.of(16, 30);
    private static final LocalTime DINNER_END = LocalTime.of(20, 0);

    // 最大订餐份数限制
    private static final int MAX_QUANTITY_PER_ITEM = 10;
    private static final int MAX_TOTAL_ITEMS = 20;

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.ORDERING;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证订餐模式消费请求: userId={}, orderItems={}",
                consumeRequest.getUserId(), consumeRequest.getOrderItems());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证订餐时间是否在允许范围内
            if (!isOrderingTimeValid()) {
                result.setValid(false);
                result.setErrorMessage("当前时间不在订餐时间内");
                return result;
            }

            // 2. 验证订餐项目是否为空
            if (consumeRequest.getOrderItems() == null || consumeRequest.getOrderItems().isEmpty()) {
                result.setValid(false);
                result.setErrorMessage("订餐项目不能为空");
                return result;
            }

            // 3. 验证设备是否支持订餐模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持订餐消费模式");
                return result;
            }

            // 4. 验证订餐项目详情
            List<ProductEntity> validItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQuantity = 0;

            for (ConsumeRequestDTO.OrderItem item : consumeRequest.getOrderItems()) {
                // 4.1 验证商品是否存在
                ProductEntity product = getProductById(item.getProductId());
                if (product == null) {
                    result.setValid(false);
                    result.setErrorMessage("商品不存在: " + item.getProductId());
                    return result;
                }

                // 4.2 验证商品状态：1-上架 2-下架 3-停售
                ProductStatusEnum status = ProductStatusEnum.getByValue(product.getStatus());
                if (status == null || !ProductStatusEnum.ON_SALE.equals(status)) {
                    result.setValid(false);
                    result.setErrorMessage("商品不可用: " + product.getProductName());
                    return result;
                }

                // 4.3 验证商品是否为订餐类型
                if (!isOrderingProduct(product)) {
                    result.setValid(false);
                    result.setErrorMessage("非订餐类商品: " + product.getProductName());
                    return result;
                }

                // 4.4 验证份数
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    result.setValid(false);
                    result.setErrorMessage("份数必须大于0: " + product.getProductName());
                    return result;
                }

                if (item.getQuantity() > MAX_QUANTITY_PER_ITEM) {
                    result.setValid(false);
                    result.setErrorMessage("单品份数超过限制: " + product.getProductName() +
                            "，最大份数: " + MAX_QUANTITY_PER_ITEM);
                    return result;
                }

                // 4.5 验证库存（如果有限制）
                if (product.getStock() != null && item.getQuantity() > product.getStock()) {
                    result.setValid(false);
                    result.setErrorMessage("商品库存不足: " + product.getProductName() +
                            "，当前库存: " + product.getStock());
                    return result;
                }

                // 4.6 验证今日订餐限制（从配置中获取，ProductEntity中没有dailyLimit字段）
                // TODO: 从商品配置或限额配置中获取每日订餐限制
                // 这里暂时跳过每日限制验证，后续可以从LimitConfigEntity中获取

                // 累计金额和数量
                BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
                totalAmount = totalAmount.add(itemAmount);
                totalQuantity += item.getQuantity();
                validItems.add(product);
            }

            // 5. 验证总份数限制
            if (totalQuantity > MAX_TOTAL_ITEMS) {
                result.setValid(false);
                result.setErrorMessage("订餐总份数超过限制，最大份数: " + MAX_TOTAL_ITEMS);
                return result;
            }

            // 6. 验证最小消费金额
            ConsumeModeConfig config = getModeConfig(consumeRequest.getDeviceId());
            BigDecimal minAmount = config != null ? config.getMinSingleAmount() : new BigDecimal("1.00");
            if (totalAmount.compareTo(minAmount) < 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额不足，最低: ¥" + minAmount);
                return result;
            }

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

            result.setValid(true);
            result.setValidItems(validItems);
            result.setCalculatedAmount(totalAmount);
            log.debug("订餐模式消费请求验证通过: userId={}, totalAmount={}, itemCount={}",
                    consumeRequest.getUserId(), totalAmount, validItems.size());

        } catch (Exception e) {
            log.error("验证订餐模式消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理订餐模式消费: userId={}, orderItems={}, deviceId={}",
                consumeRequest.getUserId(), consumeRequest.getOrderItems(), consumeRequest.getDeviceId());

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

            BigDecimal totalAmount = validation.getCalculatedAmount();

            // 2. 构建订餐详情
            StringBuilder orderDetails = new StringBuilder();
            for (int i = 0; i < consumeRequest.getOrderItems().size(); i++) {
                ConsumeRequestDTO.OrderItem item = consumeRequest.getOrderItems().get(i);
                ProductEntity product = getProductById(item.getProductId());
                if (i > 0) {
                    orderDetails.append("; ");
                }
                orderDetails.append(product.getProductName())
                        .append(" x").append(item.getQuantity())
                        .append("份");
            }

            // 3. 执行消费处理
            Map<String, Object> consumeData = new HashMap<>();
            consumeData.put("userId", consumeRequest.getUserId());
            consumeData.put("amount", totalAmount);
            consumeData.put("personName", consumeRequest.getPersonName());
            consumeData.put("payMethod", consumeRequest.getPayMethod());
            consumeData.put("deviceId", consumeRequest.getDeviceId());
            consumeData.put("consumeMode", getConsumeMode().name());
            consumeData.put("remark", buildConsumeRemark(orderDetails.toString(), consumeRequest.getRemark()));

            // 订餐相关字段
            consumeData.put("orderDetails", orderDetails.toString());
            consumeData.put("orderItems", consumeRequest.getOrderItems());
            consumeData.put("orderingStatus", OrderingStatusEnum.CONFIRMED.name());
            consumeData.put("mealTime", getCurrentMealTime());

            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                // 4. 消费成功
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount(totalAmount);
                result.setMessage("订餐成功，订单号: " + processResult.get("orderId"));
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 5. 更新商品库存和订餐记录缓存
                updateOrderingCache(consumeRequest.getUserId(), consumeRequest.getOrderItems());

                // 6. 记录成功日志
                log.info("订餐模式消费成功: userId={}, orderId={}, amount={}, details={}",
                        consumeRequest.getUserId(), result.getOrderId(), result.getAmount(), orderDetails.toString());

            } else {
                // 7. 消费失败
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount(totalAmount);
                result.setConsumeMode(getConsumeMode());

                log.warn("订餐模式消费失败: userId={}, message={}",
                        consumeRequest.getUserId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理订餐模式消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("订餐消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        try {
            if (consumeRequest.getOrderItems() == null || consumeRequest.getOrderItems().isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ConsumeRequestDTO.OrderItem item : consumeRequest.getOrderItems()) {
                ProductEntity product = getProductById(item.getProductId());
                if (product != null && item.getQuantity() != null) {
                    BigDecimal itemAmount = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
                    totalAmount = totalAmount.add(itemAmount);
                }
            }

            return totalAmount.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("计算订餐消费金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getModeDescription() {
        return "订餐模式 - 适用于食堂订餐、餐厅点餐等场景，支持菜单选择、份数选择";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持订餐模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取订餐模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.ORDERING);
            config.setEnabled(true);
            config.setMaxSingleAmount(new BigDecimal("200.00"));
            config.setMinSingleAmount(new BigDecimal("1.00"));
            config.setDailyLimit(new BigDecimal("500.00"));
            config.setDescription("订餐消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取订餐模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 检查当前时间是否在订餐时间内
     */
    private boolean isOrderingTimeValid() {
        LocalTime now = LocalTime.now();
        return (now.isAfter(BREAKFAST_START) && now.isBefore(BREAKFAST_END)) ||
                (now.isAfter(LUNCH_START) && now.isBefore(LUNCH_END)) ||
                (now.isAfter(DINNER_START) && now.isBefore(DINNER_END));
    }

    /**
     * 获取当前餐时
     */
    private String getCurrentMealTime() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(BREAKFAST_START) && now.isBefore(BREAKFAST_END)) {
            return "早餐";
        } else if (now.isAfter(LUNCH_START) && now.isBefore(LUNCH_END)) {
            return "午餐";
        } else if (now.isAfter(DINNER_START) && now.isBefore(DINNER_END)) {
            return "晚餐";
        } else {
            return "非用餐时段";
        }
    }

    /**
     * 检查商品是否为订餐类型
     */
    private boolean isOrderingProduct(ProductEntity product) {
        // TODO: 根据商品分类或属性判断是否为订餐商品
        // 这里简化实现，假设所有商品都支持订餐
        return true;
    }

    /**
     * 根据商品ID获取商品信息
     */
    private ProductEntity getProductById(Long productId) {
        try {
            // 先从缓存获取
            String cacheKey = "product:" + productId;
            Object cachedProduct = consumeCacheService.getCachedValue(cacheKey);
            ProductEntity product = null;
            if (cachedProduct instanceof ProductEntity) {
                product = (ProductEntity) cachedProduct;
            }

            if (product == null) {
                // 缓存未命中，从数据库查询
                product = productDao.selectById(productId);
                if (product != null) {
                    // 写入缓存
                    consumeCacheService.setCachedValue(cacheKey, product, 1800); // 30分钟
                }
            }

            return product;
        } catch (Exception e) {
            log.error("根据商品ID获取商品信息失败: productId={}", productId, e);
            return null;
        }
    }

    /**
     * 获取今日已订餐数量
     */
    private int getTodayOrderedCount(Long userId, Long productId) {
        try {
            String cacheKey = "ordering_record:" + userId + ":" + productId + ":" + java.time.LocalDate.now();
            Object cachedCount = consumeCacheService.getCachedValue(cacheKey);
            if (cachedCount instanceof Integer) {
                return (Integer) cachedCount;
            } else if (cachedCount instanceof Number) {
                return ((Number) cachedCount).intValue();
            }
            return 0;
        } catch (Exception e) {
            log.error("获取今日订餐数量失败: userId={}, productId={}", userId, productId, e);
            return 0;
        }
    }

    /**
     * 更新订餐记录缓存
     */
    private void updateOrderingCache(Long userId, List<ConsumeRequestDTO.OrderItem> orderItems) {
        try {
            for (ConsumeRequestDTO.OrderItem item : orderItems) {
                String cacheKey = "ordering_record:" + userId + ":" + item.getProductId() + ":"
                        + java.time.LocalDate.now();
                Object cachedCount = consumeCacheService.getCachedValue(cacheKey);
                int currentCount = 0;
                if (cachedCount instanceof Integer) {
                    currentCount = (Integer) cachedCount;
                } else if (cachedCount instanceof Number) {
                    currentCount = ((Number) cachedCount).intValue();
                }
                consumeCacheService.setCachedValue(cacheKey, currentCount + item.getQuantity(), 24 * 60 * 60); // 缓存24小时

                // 更新商品库存缓存
                ProductEntity product = getProductById(item.getProductId());
                if (product != null && product.getStock() != null) {
                    String stockCacheKey = "product_stock:" + item.getProductId();
                    int newStock = product.getStock() - item.getQuantity();
                    consumeCacheService.setCachedValue(stockCacheKey, newStock, 1800);
                }
            }

            log.debug("更新订餐记录缓存: userId={}, itemCount={}", userId, orderItems.size());
        } catch (Exception e) {
            log.error("更新订餐记录缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 构建消费备注
     */
    private String buildConsumeRemark(String orderDetails, String remark) {
        StringBuilder sb = new StringBuilder();
        sb.append("订餐: ").append(orderDetails);
        sb.append(" (").append(getCurrentMealTime()).append(")");

        if (remark != null && !remark.trim().isEmpty()) {
            sb.append(" 备注: ").append(remark);
        }

        return sb.toString();
    }

    /**
     * 获取今日菜单
     */
    public List<ProductEntity> getTodayMenu() {
        try {
            // TODO: 根据当前时间获取对应餐时的菜单
            // 这里简化实现，返回所有可用的订餐商品
            log.info("获取今日菜单: mealTime={}", getCurrentMealTime());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取今日菜单失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取推荐的订餐套餐
     */
    public List<Map<String, Object>> getRecommendedCombos() {
        try {
            // TODO: 返回推荐的订餐套餐组合
            List<Map<String, Object>> combos = new ArrayList<>();

            // 示例套餐
            Map<String, Object> combo1 = new HashMap<>();
            combo1.put("name", "经济套餐");
            combo1.put("price", new BigDecimal("15.00"));
            combo1.put("description", "米饭+1荤2素");
            combo1.put("items", "米饭, 红烧肉, 青菜, 豆腐");
            combos.add(combo1);

            Map<String, Object> combo2 = new HashMap<>();
            combo2.put("name", "营养套餐");
            combo2.put("price", new BigDecimal("20.00"));
            combo2.put("description", "米饭+1荤3素+汤");
            combo2.put("items", "米饭, 糖醋里脊, 西兰花, 胡萝卜, 紫菜蛋花汤");
            combos.add(combo2);

            return combos;
        } catch (Exception e) {
            log.error("获取推荐套餐失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查是否可以取消订单
     */
    public boolean canCancelOrder(Long orderId, Long userId) {
        try {
            // TODO: 检查订单是否可以取消
            // 规则：下单后15分钟内可取消，且订单状态为已确认
            return true;
        } catch (Exception e) {
            log.error("检查订单取消权限失败: orderId={}, userId={}", orderId, userId, e);
            return false;
        }
    }
}
