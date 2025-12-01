package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 免费消费模式实现
 * 严格遵循repowiki规范：消费金额为0，适用于免费服务场景
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component("freeAmountConsumptionMode")
public class FreeAmountConsumptionMode implements ConsumptionMode {

    @Override
    public String getModeId() {
        return "FREE_AMOUNT";
    }

    @Override
    public String getModeName() {
        return "免费消费模式";
    }

    @Override
    public String getDescription() {
        return "消费金额为0，适用于免费服务、体验服务或特殊优惠场景";
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

        // 免费模式下，如果提供了amount参数，必须为0
        if (params.containsKey("amount")) {
            Object amountObj = params.get("amount");
            if (amountObj instanceof BigDecimal) {
                BigDecimal amount = (BigDecimal) amountObj;
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
                    log.error("免费消费模式下金额必须为0: {}", amount);
                    return false;
                }
            } else {
                log.error("免费消费模式下金额类型错误或非零");
                return false;
            }
        }

        log.debug("免费消费模式参数验证通过: params={}", params);
        return true;
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        if (!validateParameters(params)) {
            throw new IllegalArgumentException("消费参数验证失败");
        }

        // 免费模式始终返回0金额
        log.debug("免费消费模式计算金额: {}", BigDecimal.ZERO);
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isAllowed(Map<String, Object> params) {
        if (!validateParameters(params)) {
            return false;
        }

        // 可以添加免费消费的业务规则，比如：
        // - 检查用户是否有免费额度
        // - 检查是否在免费时间段内
        // - 检查设备是否有免费权限
        // - 检查服务类型是否支持免费消费

        // 目前允许所有通过验证的免费消费
        log.debug("免费消费模式允许消费");
        return true;
    }

    @Override
    public Map<String, Object> getLimits() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("fixedAmount", BigDecimal.ZERO);
        limits.put("minAmount", BigDecimal.ZERO);
        limits.put("maxAmount", BigDecimal.ZERO);
        limits.put("dailyLimit", Integer.MAX_VALUE); // 无限制
        limits.put("description", "免费消费模式限制 - 金额必须为0");
        return limits;
    }

    @Override
    public Map<String, Object> preProcess(Map<String, Object> params) {
        log.debug("免费消费模式预处理: params={}", params);

        Map<String, Object> result = new HashMap<>();
        result.put("mode", getModeId());
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "PRE_PROCESS_SUCCESS");
        result.put("amount", BigDecimal.ZERO);
        result.put("isFree", true);

        // 免费消费的特殊预处理逻辑
        result.put("requiresPayment", false);
        result.put("balanceCheckRequired", false);

        return result;
    }

    @Override
    public Map<String, Object> postProcess(Map<String, Object> params, Map<String, Object> result) {
        log.debug("免费消费模式后处理: params={}, result={}", params, result);

        Map<String, Object> postResult = new HashMap<>();
        postResult.put("mode", getModeId());
        postResult.put("timestamp", System.currentTimeMillis());
        postResult.put("status", "POST_PROCESS_SUCCESS");
        postResult.put("usedAmount", BigDecimal.ZERO);

        // 免费消费的特殊后处理逻辑
        postResult.put("paymentSkipped", true);
        postResult.put("balanceUnchanged", true);
        postResult.put("recordedForStatistics", true); // 仍需要记录统计

        return postResult;
    }
}