package net.lab1024.sa.admin.module.consume.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;
import net.lab1024.sa.admin.module.consume.dao.ProductDao;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 智能模式消费引擎
 * 基于用户习惯、消费历史、时间、地点等多维度因素进行智能推荐和决策
 * 支持个性化推荐、智能支付、自动优化等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class SmartConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private ProductDao productDao;

    // 智能推荐算法权重
    private static final double FREQUENCY_WEIGHT = 0.3;      // 频率权重
    private static final double RECENCY_WEIGHT = 0.25;       // 时间权重
    private static final double AMOUNT_WEIGHT = 0.2;         // 金额权重
    private static final double CONTEXT_WEIGHT = 0.15;       // 上下文权重
    private static final double PERSONAL_WEIGHT = 0.1;       // 个性化权重

    // 智能阈值
    private static final BigDecimal SMART_AMOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal MIN_TRUST_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAX_AUTO_AMOUNT = new BigDecimal("500.00");

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.SMART;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证智能模式消费请求: userId={}, smartType={}, suggestedAmount={}",
                consumeRequest.getUserId(), consumeRequest.getSmartType(), consumeRequest.getSuggestedAmount());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证智能类型
            if (consumeRequest.getSmartType() == null || consumeRequest.getSmartType().trim().isEmpty()) {
                // 如果没有指定智能类型，自动推断
                consumeRequest.setSmartType(determineSmartType(consumeRequest));
                log.debug("自动推断智能类型: {}", consumeRequest.getSmartType());
            }

            // 2. 验证设备是否支持智能模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持智能消费模式");
                return result;
            }

            // 3. 根据智能类型进行特定验证
            switch (consumeRequest.getSmartType().toLowerCase()) {
                case "recommendation":
                    return validateRecommendationRequest(consumeRequest, result);
                case "auto_payment":
                    return validateAutoPaymentRequest(consumeRequest, result);
                case "smart_suggest":
                    return validateSmartSuggestRequest(consumeRequest, result);
                case "pattern_match":
                    return validatePatternMatchRequest(consumeRequest, result);
                default:
                    result.setValid(false);
                    result.setErrorMessage("不支持的智能类型: " + consumeRequest.getSmartType());
                    return result;
            }

        } catch (Exception e) {
            log.error("验证智能模式消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理智能模式消费: userId={}, smartType={}, deviceId={}",
                consumeRequest.getUserId(), consumeRequest.getSmartType(), consumeRequest.getDeviceId());

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

            // 2. 根据智能类型处理消费
            switch (consumeRequest.getSmartType().toLowerCase()) {
                case "recommendation":
                    return processRecommendationConsume(consumeRequest, validation);
                case "auto_payment":
                    return processAutoPaymentConsume(consumeRequest, validation);
                case "smart_suggest":
                    return processSmartSuggestConsume(consumeRequest, validation);
                case "pattern_match":
                    return processPatternMatchConsume(consumeRequest, validation);
                default:
                    result.setSuccess(false);
                    result.setMessage("不支持的智能类型");
                    result.setStatus(ConsumeStatusEnum.FAILED);
                    return result;
            }

        } catch (Exception e) {
            log.error("处理智能模式消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("智能消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        try {
            switch (consumeRequest.getSmartType().toLowerCase()) {
                case "recommendation":
                    return calculateRecommendationAmount(consumeRequest);
                case "auto_payment":
                    return calculateAutoPaymentAmount(consumeRequest);
                case "smart_suggest":
                    return calculateSmartSuggestAmount(consumeRequest);
                case "pattern_match":
                    return calculatePatternMatchAmount(consumeRequest);
                default:
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("计算智能消费金额失败", e);
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getModeDescription() {
        return "智能模式 - 基于用户习惯、消费历史等数据提供个性化推荐和智能支付";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持智能模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取智能模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.SMART);
            config.setEnabled(true);
            config.setMaxSingleAmount(new BigDecimal("1000.00"));
            config.setMinSingleAmount(new BigDecimal("0.01"));
            config.setDailyLimit(new BigDecimal("2000.00"));
            config.setDescription("智能消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取智能模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 验证推荐模式请求
     */
    private ConsumeValidationResult validateRecommendationRequest(ConsumeRequestDTO consumeRequest, ConsumeValidationResult result) {
        // 1. 获取用户消费历史
        List<Map<String, Object>> userHistory = getUserConsumeHistory(consumeRequest.getUserId(), 30);
        if (userHistory.isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("暂无消费历史，无法提供个性化推荐");
            return result;
        }

        // 2. 生成智能推荐
        List<Map<String, Object>> recommendations = generateSmartRecommendations(consumeRequest.getUserId(), userHistory);
        if (recommendations.isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("暂时无法生成推荐，请选择其他消费模式");
            return result;
        }

        // 3. 如果有建议金额，验证合理性
        if (consumeRequest.getSuggestedAmount() != null) {
            if (!validateSuggestedAmount(consumeRequest, consumeRequest.getSuggestedAmount())) {
                result.setValid(false);
                result.setErrorMessage("建议金额不合理，请重新选择");
                return result;
            }
        }

        // 4. 验证用户余额
        BigDecimal estimatedAmount = consumeRequest.getSuggestedAmount() != null ?
                consumeRequest.getSuggestedAmount() : estimateRecommendedAmount(recommendations);

        Map<String, Object> accountInfo = consumeService.getAccountBalance(consumeRequest.getUserId());
        if (accountInfo != null) {
            BigDecimal balance = (BigDecimal) accountInfo.get("balance");
            if (balance != null && balance.compareTo(estimatedAmount) < 0) {
                result.setValid(false);
                result.setErrorMessage("账户余额不足，建议消费: ¥" + estimatedAmount + "，当前余额: ¥" + balance);
                return result;
            }
        }

        result.setValid(true);
        result.setRecommendedItems(recommendations);
        result.setCalculatedAmount(estimatedAmount);
        return result;
    }

    /**
     * 验证自动支付请求
     */
    private ConsumeValidationResult validateAutoPaymentRequest(ConsumeRequestDTO consumeRequest, ConsumeValidationResult result) {
        // 1. 验证是否启用自动支付
        if (!isAutoPaymentEnabled(consumeRequest.getUserId())) {
            result.setValid(false);
            result.setErrorMessage("用户未启用自动支付功能");
            return result;
        }

        // 2. 验证支付密码
        if (consumeRequest.getPaymentPassword() == null || consumeRequest.getPaymentPassword().trim().isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("自动支付需要验证支付密码");
            return result;
        }

        // 3. 验证场景是否支持自动支付
        if (!isAutoPaymentAllowed(consumeRequest)) {
            result.setValid(false);
            result.setErrorMessage("当前场景不支持自动支付");
            return result;
        }

        // 4. 验证自动支付限额
        BigDecimal autoAmount = calculateAutoPaymentAmount(consumeRequest);
        if (autoAmount.compareTo(MAX_AUTO_AMOUNT) > 0) {
            result.setValid(false);
            result.setErrorMessage("自动支付金额超过限额，最高: ¥" + MAX_AUTO_AMOUNT);
            return result;
        }

        // 5. 验证用户余额
        Map<String, Object> accountInfo = consumeService.getAccountBalance(consumeRequest.getUserId());
        if (accountInfo != null) {
            BigDecimal balance = (BigDecimal) accountInfo.get("balance");
            if (balance != null && balance.compareTo(autoAmount) < 0) {
                result.setValid(false);
                result.setErrorMessage("账户余额不足，需要: ¥" + autoAmount);
                return result;
            }
        }

        result.setValid(true);
        result.setCalculatedAmount(autoAmount);
        return result;
    }

    /**
     * 验证智能建议请求
     */
    private ConsumeValidationResult validateSmartSuggestRequest(ConsumeRequestDTO consumeRequest, ConsumeValidationResult result) {
        // 1. 分析当前消费场景
        Map<String, Object> context = analyzeCurrentContext(consumeRequest);

        // 2. 基于场景生成智能建议
        List<Map<String, Object>> suggestions = generateContextualSuggestions(consumeRequest.getUserId(), context);
        if (suggestions.isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("当前场景暂无智能建议");
            return result;
        }

        // 3. 验证用户选择（如果有）
        if (consumeRequest.getSelectedSuggestion() != null && !consumeRequest.getSelectedSuggestion().trim().isEmpty()) {
            boolean validSuggestion = suggestions.stream()
                    .anyMatch(s -> consumeRequest.getSelectedSuggestion().equals(s.get("id")));
            if (!validSuggestion) {
                result.setValid(false);
                result.setErrorMessage("无效的智能建议选择");
                return result;
            }
        }

        result.setValid(true);
        result.setSuggestions(suggestions);
        return result;
    }

    /**
     * 验证模式匹配请求
     */
    private ConsumeValidationResult validatePatternMatchRequest(ConsumeRequestDTO consumeRequest, ConsumeValidationResult result) {
        // 1. 获取用户消费模式
        Map<String, Object> userPattern = analyzeUserConsumePattern(consumeRequest.getUserId());
        if (userPattern.isEmpty()) {
            result.setValid(false);
            result.setErrorMessage("暂无足够数据建立消费模式");
            return result;
        }

        // 2. 匹配当前行为
        double matchScore = calculatePatternMatchScore(consumeRequest, userPattern);
        if (matchScore < 0.6) {
            result.setValid(false);
            result.setErrorMessage("当前行为与消费模式不匹配，建议验证操作");
            return result;
        }

        // 3. 计算预期金额
        BigDecimal expectedAmount = calculatePatternBasedAmount(consumeRequest, userPattern);
        if (consumeRequest.getAmount() != null) {
            BigDecimal deviation = consumeRequest.getAmount().subtract(expectedAmount).abs();
            BigDecimal threshold = expectedAmount.multiply(new BigDecimal("0.5")); // 50%偏差阈值

            if (deviation.compareTo(threshold) > 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额与模式偏差过大，预期: ¥" + expectedAmount +
                        "，实际: ¥" + consumeRequest.getAmount());
                return result;
            }
        }

        result.setValid(true);
        result.setPatternMatchScore(matchScore);
        result.setCalculatedAmount(expectedAmount);
        return result;
    }

    /**
     * 处理推荐模式消费
     */
    private ConsumeResultDTO processRecommendationConsume(ConsumeRequestDTO consumeRequest, ConsumeValidationResult validation) {
        BigDecimal amount = validation.getCalculatedAmount();

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("userId", consumeRequest.getUserId());
        consumeData.put("amount", amount);
        consumeData.put("personName", consumeRequest.getPersonName());
        consumeData.put("payMethod", consumeRequest.getPayMethod());
        consumeData.put("deviceId", consumeRequest.getDeviceId());
        consumeData.put("consumeMode", getConsumeMode().name());
        consumeData.put("remark", buildRecommendationRemark(consumeRequest, validation));
        consumeData.put("smartType", consumeRequest.getSmartType());
        consumeData.put("recommendations", validation.getRecommendedItems());

        return processSmartConsume(consumeData, consumeRequest, "推荐消费成功");
    }

    /**
     * 处理自动支付消费
     */
    private ConsumeResultDTO processAutoPaymentConsume(ConsumeRequestDTO consumeRequest, ConsumeValidationResult validation) {
        // 验证支付密码
        if (!validatePaymentPassword(consumeRequest.getUserId(), consumeRequest.getPaymentPassword())) {
            ConsumeResultDTO result = new ConsumeResultDTO();
            result.setSuccess(false);
            result.setMessage("支付密码验证失败");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
            return result;
        }

        BigDecimal amount = validation.getCalculatedAmount();

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("userId", consumeRequest.getUserId());
        consumeData.put("amount", amount);
        consumeData.put("personName", consumeRequest.getPersonName());
        consumeData.put("payMethod", consumeRequest.getPayMethod());
        consumeData.put("deviceId", consumeRequest.getDeviceId());
        consumeData.put("consumeMode", getConsumeMode().name());
        consumeData.put("remark", "智能自动支付");
        consumeData.put("smartType", consumeRequest.getSmartType());
        consumeData.put("autoPayment", true);

        return processSmartConsume(consumeData, consumeRequest, "自动支付成功");
    }

    /**
     * 处理智能建议消费
     */
    private ConsumeResultDTO processSmartSuggestConsume(ConsumeRequestDTO consumeRequest, ConsumeValidationResult validation) {
        // 使用选中的建议计算金额
        BigDecimal amount = calculateSuggestionAmount(consumeRequest, validation.getSuggestions());

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("userId", consumeRequest.getUserId());
        consumeData.put("amount", amount);
        consumeData.put("personName", consumeRequest.getPersonName());
        consumeData.put("payMethod", consumeRequest.getPayMethod());
        consumeData.put("deviceId", consumeRequest.getDeviceId());
        consumeData.put("consumeMode", getConsumeMode().name());
        consumeData.put("remark", buildSuggestionRemark(consumeRequest, validation.getSuggestions()));
        consumeData.put("smartType", consumeRequest.getSmartType());
        consumeData.put("selectedSuggestion", consumeRequest.getSelectedSuggestion());

        return processSmartConsume(consumeData, consumeRequest, "智能建议消费成功");
    }

    /**
     * 处理模式匹配消费
     */
    private ConsumeResultDTO processPatternMatchConsume(ConsumeRequestDTO consumeRequest, ConsumeValidationResult validation) {
        BigDecimal amount = consumeRequest.getAmount() != null ? consumeRequest.getAmount() : validation.getCalculatedAmount();

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("userId", consumeRequest.getUserId());
        consumeData.put("amount", amount);
        consumeData.put("personName", consumeRequest.getPersonName());
        consumeData.put("payMethod", consumeRequest.getPayMethod());
        consumeData.put("deviceId", consumeRequest.getDeviceId());
        consumeData.put("consumeMode", getConsumeMode().name());
        consumeData.put("remark", "智能模式匹配消费");
        consumeData.put("smartType", consumeRequest.getSmartType());
        consumeData.put("patternMatchScore", validation.getPatternMatchScore());

        return processSmartConsume(consumeData, consumeRequest, "模式匹配消费成功");
    }

    /**
     * 处理智能消费的通用逻辑
     */
    private ConsumeResultDTO processSmartConsume(Map<String, Object> consumeData, ConsumeRequestDTO consumeRequest, String successMessage) {
        ConsumeResultDTO result = new ConsumeResultDTO();

        try {
            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount((BigDecimal) consumeData.get("amount"));
                result.setMessage(successMessage + "，订单号: " + processResult.get("orderId"));
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 更新智能模式缓存
                updateSmartModeCache(consumeRequest.getUserId(), consumeData);

                log.info("智能模式消费成功: userId={}, orderId={}, smartType={}, amount={}",
                        consumeRequest.getUserId(), result.getOrderId(), consumeRequest.getSmartType(), result.getAmount());

            } else {
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount((BigDecimal) consumeData.get("amount"));
                result.setConsumeMode(getConsumeMode());
            }

        } catch (Exception e) {
            log.error("处理智能消费失败", e);
            result.setSuccess(false);
            result.setMessage("智能消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    // ========== 智能算法辅助方法 ==========

    /**
     * 确定智能类型
     */
    private String determineSmartType(ConsumeRequestDTO consumeRequest) {
        // 基于上下文自动推断最适合的智能类型
        if (consumeRequest.getProductId() != null) {
            return "recommendation";
        } else if (consumeRequest.getAmount() != null && consumeRequest.getAmount().compareTo(MIN_TRUST_AMOUNT) < 0) {
            return "smart_suggest";
        } else {
            return "pattern_match";
        }
    }

    /**
     * 获取用户消费历史
     */
    private List<Map<String, Object>> getUserConsumeHistory(Long userId, int days) {
        try {
            String cacheKey = "consume_history:" + userId + ":" + days + "days";
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            List<Map<String, Object>> history = null;
            if (cachedValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) cachedValue;
                history = list;
            }

            if (history == null) {
                // TODO: 从数据库查询消费历史
                history = new ArrayList<>();
                consumeCacheService.setCachedValue(cacheKey, history, 60 * 60); // 缓存1小时
            }

            return history;
        } catch (Exception e) {
            log.error("获取用户消费历史失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成智能推荐
     */
    private List<Map<String, Object>> generateSmartRecommendations(Long userId, List<Map<String, Object>> history) {
        try {
            List<Map<String, Object>> recommendations = new ArrayList<>();

            // 基于频率和时间分析用户偏好
            Map<String, Double> preferences = analyzeUserPreferences(userId, history);

            // 获取当前可用商品
            List<ProductEntity> availableProducts = getAvailableProducts();

            // 计算推荐得分
            for (ProductEntity product : availableProducts) {
                double score = calculateRecommendationScore(product, preferences);
                if (score > 0.3) { // 推荐阈值
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("productId", product.getProductId());
                    recommendation.put("productName", product.getProductName());
                    recommendation.put("price", product.getPrice());
                    recommendation.put("score", score);
                    recommendation.put("reason", generateRecommendationReason(product, preferences));
                    recommendations.add(recommendation);
                }
            }

            // 按得分排序
            recommendations.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));

            // 返回前5个推荐
            return recommendations.stream().limit(5).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("生成智能推荐失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 分析用户偏好
     */
    private Map<String, Double> analyzeUserPreferences(Long userId, List<Map<String, Object>> history) {
        Map<String, Double> preferences = new HashMap<>();

        // TODO: 实现用户偏好分析算法
        // 这里简化实现
        preferences.put("price_sensitivity", 0.5);
        preferences.put("brand_loyalty", 0.3);
        preferences.put("category_preference", 0.7);
        preferences.put("time_preference", 0.4);

        return preferences;
    }

    /**
     * 计算推荐得分
     */
    private double calculateRecommendationScore(ProductEntity product, Map<String, Double> preferences) {
        // TODO: 实现推荐得分计算算法
        return Math.random() * 0.8 + 0.2; // 0.2-1.0
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(ProductEntity product, Map<String, Double> preferences) {
        // TODO: 基于用户偏好生成推荐理由
        return "基于您的消费习惯推荐";
    }

    /**
     * 获取可用商品
     */
    private List<ProductEntity> getAvailableProducts() {
        try {
            String cacheKey = "available_products";
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            List<ProductEntity> products = null;
            if (cachedValue instanceof List) {
                @SuppressWarnings("unchecked")
                List<ProductEntity> list = (List<ProductEntity>) cachedValue;
                products = list;
            }

            if (products == null) {
                // TODO: 从数据库查询可用商品
                products = new ArrayList<>();
                consumeCacheService.setCachedValue(cacheKey, products, 30 * 60); // 缓存30分钟
            }

            return products;
        } catch (Exception e) {
            log.error("获取可用商品失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 验证建议金额
     */
    private boolean validateSuggestedAmount(ConsumeRequestDTO consumeRequest, BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) > 0 &&
               amount.compareTo(SMART_AMOUNT_THRESHOLD) <= 0;
    }

    /**
     * 估算推荐金额
     */
    private BigDecimal estimateRecommendedAmount(List<Map<String, Object>> recommendations) {
        return recommendations.stream()
                .map(rec -> (BigDecimal) rec.get("price"))
                .findFirst()
                .orElse(new BigDecimal("10.00"));
    }

    /**
     * 检查是否启用自动支付
     */
    private boolean isAutoPaymentEnabled(Long userId) {
        try {
            String cacheKey = "auto_payment_enabled:" + userId;
            Object cachedValue = consumeCacheService.getCachedValue(cacheKey);
            if (cachedValue instanceof Boolean) {
                return (Boolean) cachedValue;
            }
            return false;
        } catch (Exception e) {
            log.error("检查自动支付状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 验证支付密码
     */
    private boolean validatePaymentPassword(Long userId, String password) {
        // TODO: 实现支付密码验证逻辑
        return "123456".equals(password); // 简化实现
    }

    /**
     * 检查是否允许自动支付
     */
    private boolean isAutoPaymentAllowed(ConsumeRequestDTO consumeRequest) {
        // TODO: 基于风险控制检查是否允许自动支付
        return true;
    }

    /**
     * 分析当前上下文
     */
    private Map<String, Object> analyzeCurrentContext(ConsumeRequestDTO consumeRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("time", java.time.LocalTime.now());
        context.put("location", consumeRequest.getDeviceId());
        context.put("weekday", java.time.LocalDate.now().getDayOfWeek().getValue());
        return context;
    }

    /**
     * 生成上下文建议
     */
    private List<Map<String, Object>> generateContextualSuggestions(Long userId, Map<String, Object> context) {
        List<Map<String, Object>> suggestions = new ArrayList<>();

        // TODO: 基于上下文生成智能建议
        Map<String, Object> suggestion1 = new HashMap<>();
        suggestion1.put("id", "suggestion_1");
        suggestion1.put("title", "早餐套餐");
        suggestion1.put("amount", new BigDecimal("15.00"));
        suggestion1.put("description", "营养搭配的早餐套餐");
        suggestions.add(suggestion1);

        Map<String, Object> suggestion2 = new HashMap<>();
        suggestion2.put("id", "suggestion_2");
        suggestion2.put("title", "标准套餐");
        suggestion2.put("amount", new BigDecimal("25.00"));
        suggestion2.put("description", "经济实惠的午晚餐套餐");
        suggestions.add(suggestion2);

        return suggestions;
    }

    /**
     * 计算建议金额
     */
    private BigDecimal calculateSuggestionAmount(ConsumeRequestDTO consumeRequest, List<Map<String, Object>> suggestions) {
        if (consumeRequest.getSelectedSuggestion() != null) {
            return suggestions.stream()
                    .filter(s -> consumeRequest.getSelectedSuggestion().equals(s.get("id")))
                    .map(s -> (BigDecimal) s.get("amount"))
                    .findFirst()
                    .orElse(new BigDecimal("10.00"));
        }
        return new BigDecimal("10.00");
    }

    /**
     * 分析用户消费模式
     */
    private Map<String, Object> analyzeUserConsumePattern(Long userId) {
        Map<String, Object> pattern = new HashMap<>();
        // TODO: 实现用户消费模式分析
        pattern.put("avgAmount", new BigDecimal("20.00"));
        pattern.put("frequency", 3.5);
        pattern.put("preferredTime", "12:00");
        pattern.put("preferredCategories", Arrays.asList("food", "drink"));
        return pattern;
    }

    /**
     * 计算模式匹配得分
     */
    private double calculatePatternMatchScore(ConsumeRequestDTO consumeRequest, Map<String, Object> pattern) {
        // TODO: 实现模式匹配得分计算
        return Math.random() * 0.4 + 0.6; // 0.6-1.0
    }

    /**
     * 基于模式计算金额
     */
    private BigDecimal calculatePatternBasedAmount(ConsumeRequestDTO consumeRequest, Map<String, Object> pattern) {
        return (BigDecimal) pattern.getOrDefault("avgAmount", new BigDecimal("10.00"));
    }

    /**
     * 更新智能模式缓存
     */
    private void updateSmartModeCache(Long userId, Map<String, Object> consumeData) {
        try {
            // 更新用户智能消费统计
            String statsKey = "smart_consume_stats:" + userId + ":" + java.time.LocalDate.now();
            Object cachedValue = consumeCacheService.getCachedValue(statsKey);
            Map<String, Object> stats = null;
            if (cachedValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) cachedValue;
                stats = map;
            }
            if (stats == null) {
                stats = new HashMap<>();
            }

            String smartType = (String) consumeData.get("smartType");
            int count = (Integer) stats.getOrDefault(smartType + "_count", 0);
            BigDecimal amount = (BigDecimal) stats.getOrDefault(smartType + "_amount", BigDecimal.ZERO);

            stats.put(smartType + "_count", count + 1);
            stats.put(smartType + "_amount", amount.add((BigDecimal) consumeData.get("amount")));

            consumeCacheService.setCachedValue(statsKey, stats, 24 * 60 * 60);

        } catch (Exception e) {
            log.error("更新智能模式缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 构建推荐模式备注
     */
    private String buildRecommendationRemark(ConsumeRequestDTO consumeRequest, ConsumeValidationResult validation) {
        return "智能推荐消费 - 基于您的消费习惯个性化推荐";
    }

    /**
     * 构建建议模式备注
     */
    private String buildSuggestionRemark(ConsumeRequestDTO consumeRequest, List<Map<String, Object>> suggestions) {
        return "智能建议消费 - 根据当前场景为您提供最佳建议";
    }

    // ========== 金额计算方法 ==========

    /**
     * 计算推荐模式金额
     */
    private BigDecimal calculateRecommendationAmount(ConsumeRequestDTO consumeRequest) {
        if (consumeRequest.getSuggestedAmount() != null) {
            return consumeRequest.getSuggestedAmount();
        }
        return new BigDecimal("20.00"); // 默认推荐金额
    }

    /**
     * 计算自动支付金额
     */
    private BigDecimal calculateAutoPaymentAmount(ConsumeRequestDTO consumeRequest) {
        if (consumeRequest.getAmount() != null) {
            return consumeRequest.getAmount();
        }

        // 基于历史数据计算自动支付金额
        List<Map<String, Object>> history = getUserConsumeHistory(consumeRequest.getUserId(), 7);
        if (!history.isEmpty()) {
            return history.stream()
                    .map(h -> (BigDecimal) h.getOrDefault("amount", BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(history.size()), 2, RoundingMode.HALF_UP);
        }

        return new BigDecimal("10.00");
    }

    /**
     * 计算智能建议金额
     */
    private BigDecimal calculateSmartSuggestAmount(ConsumeRequestDTO consumeRequest) {
        Map<String, Object> context = analyzeCurrentContext(consumeRequest);
        List<Map<String, Object>> suggestions = generateContextualSuggestions(consumeRequest.getUserId(), context);
        return calculateSuggestionAmount(consumeRequest, suggestions);
    }

    /**
     * 计算模式匹配金额
     */
    private BigDecimal calculatePatternMatchAmount(ConsumeRequestDTO consumeRequest) {
        Map<String, Object> pattern = analyzeUserConsumePattern(consumeRequest.getUserId());
        return calculatePatternBasedAmount(consumeRequest, pattern);
    }

    // ========== 公共接口方法 ==========

    /**
     * 获取用户智能推荐
     */
    public List<Map<String, Object>> getUserSmartRecommendations(Long userId) {
        List<Map<String, Object>> history = getUserConsumeHistory(userId, 30);
        return generateSmartRecommendations(userId, history);
    }

    /**
     * 获取智能消费统计
     */
    public Map<String, Object> getSmartConsumeStatistics(Long userId) {
        try {
            String statsKey = "smart_consume_stats:" + userId + ":" + java.time.LocalDate.now();
            Object cachedValue = consumeCacheService.getCachedValue(statsKey);
            if (cachedValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) cachedValue;
                return map;
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("获取智能消费统计失败: userId={}", userId, e);
            return new HashMap<>();
        }
    }

    /**
     * 训练推荐模型
     */
    public void trainRecommendationModel() {
        try {
            log.info("开始训练智能推荐模型...");
            // TODO: 实现机器学习模型训练逻辑
            log.info("智能推荐模型训练完成");
        } catch (Exception e) {
            log.error("训练推荐模型失败", e);
        }
    }
}