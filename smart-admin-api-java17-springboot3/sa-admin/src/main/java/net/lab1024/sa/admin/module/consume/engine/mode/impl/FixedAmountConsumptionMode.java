package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 固定金额消费模式实现
 * 严格遵循repowiki规范：使用预设的固定金额进行消费
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component("fixedAmountConsumptionMode")
public class FixedAmountConsumptionMode implements ConsumptionMode {

    // 预设的固定金额配置
    private static final BigDecimal DEFAULT_FIXED_AMOUNT = new BigDecimal("5.00");
    private static final BigDecimal MIN_FIXED_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_FIXED_AMOUNT = new BigDecimal("100.00");

    @Override
    public String getModeId() {
        return "FIXED_AMOUNT";
    }

    @Override
    public String getModeName() {
        return "固定金额消费模式";
    }

    @Override
    public String getDescription() {
        return "使用预设的固定金额进行消费，适用于固定价格的消费场景";
    }

    @Override
    public boolean validateParameters(Map<String, Object> params) {
        if (params == null) {
            log.error("消费参数为空");
            return false;
        }

        // 检查账户ID
        if (!params.containsKey("accountId")) {
            log.error("缺少账户ID参数");
            return false;
        }

        // 固定金额模式下，amount参数可选，如果提供则验证范围
        if (params.containsKey("amount")) {
            Object amountObj = params.get("amount");
            if (amountObj instanceof BigDecimal) {
                BigDecimal amount = (BigDecimal) amountObj;
                if (amount.compareTo(MIN_FIXED_AMOUNT) < 0 || amount.compareTo(MAX_FIXED_AMOUNT) > 0) {
                    log.error("固定金额超出范围: {}, 范围: {} - {}", amount, MIN_FIXED_AMOUNT, MAX_FIXED_AMOUNT);
                    return false;
                }
            }
        }

        log.debug("固定金额消费模式参数验证通过: params={}", params);
        return true;
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        if (!validateParameters(params)) {
            throw new IllegalArgumentException("消费参数验证失败");
        }

        BigDecimal amount;

        // 如果参数中指定了金额且在允许范围内，使用指定的金额
        if (params.containsKey("amount")) {
            amount = (BigDecimal) params.get("amount");
            if (amount.compareTo(MIN_FIXED_AMOUNT) >= 0 && amount.compareTo(MAX_FIXED_AMOUNT) <= 0) {
                log.debug("固定金额消费模式使用指定金额: {}", amount);
                return amount;
            }
        }

        // 否则使用默认固定金额
        amount = DEFAULT_FIXED_AMOUNT;
        log.debug("固定金额消费模式使用默认金额: {}", amount);
        return amount;
    }

    @Override
    public boolean isAllowed(Map<String, Object> params) {
        // 固定金额模式下，验证参数并检查金额范围
        if (!validateParameters(params)) {
            return false;
        }

        // 可以添加额外的业务规则，比如：
        // - 特殊设备只允许固定金额消费
        // - 特定时间段只允许固定金额消费
        // - 特殊用户组只允许固定金额消费

        return true;
    }

    @Override
    public Map<String, Object> getLimits() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("fixedAmount", DEFAULT_FIXED_AMOUNT);
        limits.put("minAmount", MIN_FIXED_AMOUNT);
        limits.put("maxAmount", MAX_FIXED_AMOUNT);
        limits.put("dailyLimit", new BigDecimal("500.00"));
        limits.put("description", "固定金额消费模式限制");
        return limits;
    }

    @Override
    public Map<String, Object> preProcess(Map<String, Object> params) {
        log.debug("固定金额消费模式预处理: params={}", params);

        Map<String, Object> result = new HashMap<>();
        result.put("mode", getModeId());
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "PRE_PROCESS_SUCCESS");

        // 计算最终使用的金额
        BigDecimal finalAmount = calculateAmount(params);
        result.put("finalAmount", finalAmount);
        result.put("amountType", finalAmount.equals(DEFAULT_FIXED_AMOUNT) ? "DEFAULT" : "CUSTOM");

        return result;
    }

    @Override
    public Map<String, Object> postProcess(Map<String, Object> params, Map<String, Object> result) {
        log.debug("固定金额消费模式后处理: params={}, result={}", params, result);

        Map<String, Object> postResult = new HashMap<>();
        postResult.put("mode", getModeId());
        postResult.put("timestamp", System.currentTimeMillis());
        postResult.put("status", "POST_PROCESS_SUCCESS");

        // 固定金额模式的特殊后处理逻辑
        BigDecimal usedAmount = (BigDecimal) result.get("amount");
        postResult.put("usedAmount", usedAmount);
        postResult.put("isDefaultAmount", usedAmount.equals(DEFAULT_FIXED_AMOUNT));

        return postResult;
    }
}