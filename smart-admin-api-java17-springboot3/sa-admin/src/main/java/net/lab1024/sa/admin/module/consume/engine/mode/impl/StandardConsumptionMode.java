package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import net.lab1024.sa.admin.module.consume.engine.mode.ConsumptionMode;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 标准消费模式实现
 * 严格遵循repowiki规范：默认的消费模式，直接使用传入的金额
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Slf4j
@Component("standardConsumptionMode")
public class StandardConsumptionMode implements ConsumptionMode {

    @Override
    public String getModeId() {
        return "STANDARD";
    }

    @Override
    public String getModeName() {
        return "标准消费模式";
    }

    @Override
    public String getDescription() {
        return "默认的消费模式，直接使用传入的金额进行消费";
    }

    @Override
    public boolean validateParameters(Map<String, Object> params) {
        if (params == null) {
            log.error("消费参数为空");
            return false;
        }

        // 检查必需参数
        if (!params.containsKey("amount")) {
            log.error("缺少消费金额参数");
            return false;
        }

        Object amountObj = params.get("amount");
        if (!(amountObj instanceof BigDecimal)) {
            log.error("消费金额类型错误");
            return false;
        }

        BigDecimal amount = (BigDecimal) amountObj;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("消费金额必须大于0: {}", amount);
            return false;
        }

        if (!params.containsKey("accountId")) {
            log.error("缺少账户ID参数");
            return false;
        }

        log.debug("标准消费模式参数验证通过: params={}", params);
        return true;
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        if (!validateParameters(params)) {
            throw new IllegalArgumentException("消费参数验证失败");
        }

        BigDecimal amount = (BigDecimal) params.get("amount");
        log.debug("标准消费模式计算金额: {}", amount);
        return amount;
    }

    @Override
    public boolean isAllowed(Map<String, Object> params) {
        // 标准模式下，只要参数验证通过就允许消费
        boolean allowed = validateParameters(params);
        log.debug("标准消费模式允许消费: {}", allowed);
        return allowed;
    }

    @Override
    public Map<String, Object> getLimits() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("minAmount", BigDecimal.ZERO);
        limits.put("maxAmount", new BigDecimal("999999.99"));
        limits.put("dailyLimit", new BigDecimal("999999.99"));
        limits.put("monthlyLimit", new BigDecimal("9999999.99"));
        limits.put("description", "标准消费模式限制");
        return limits;
    }

    @Override
    public Map<String, Object> preProcess(Map<String, Object> params) {
        log.debug("标准消费模式预处理: params={}", params);

        Map<String, Object> result = new HashMap<>();
        result.put("mode", getModeId());
        result.put("timestamp", System.currentTimeMillis());
        result.put("status", "PRE_PROCESS_SUCCESS");

        // 可以在这里添加预处理逻辑，比如：
        // - 检查账户状态
        // - 验证设备权限
        // - 检查时间窗口等

        return result;
    }

    @Override
    public Map<String, Object> postProcess(Map<String, Object> params, Map<String, Object> result) {
        log.debug("标准消费模式后处理: params={}, result={}", params, result);

        Map<String, Object> postResult = new HashMap<>();
        postResult.put("mode", getModeId());
        postResult.put("timestamp", System.currentTimeMillis());
        postResult.put("status", "POST_PROCESS_SUCCESS");

        // 可以在这里添加后处理逻辑，比如：
        // - 发送通知
        // - 更新统计信息
        // - 记录审计日志等

        return postResult;
    }
}