package net.lab1024.sa.admin.module.consume.engine.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.engine.ConsumeModeEngine;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeResultDTO;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeValidationResult;
import net.lab1024.sa.admin.module.consume.domain.dto.ConsumeModeConfig;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeModeEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.ConsumeStatusEnum;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * 固定金额消费模式引擎
 * 适用于标准固定价格的消费场景，如食堂标准餐、固定收费项目等
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class FixedAmountConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    // 支持的固定金额档位
    private static final BigDecimal[] STANDARD_AMOUNTS = {
        new BigDecimal("5.00"),
        new BigDecimal("8.00"),
        new BigDecimal("10.00"),
        new BigDecimal("12.00"),
        new BigDecimal("15.00"),
        new BigDecimal("18.00"),
        new BigDecimal("20.00"),
        new BigDecimal("25.00")
    };

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.FIXED_AMOUNT;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证固定金额消费请求: userId={}, amount={}", consumeRequest.getUserId(), consumeRequest.getAmount());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证消费金额是否为空
            if (consumeRequest.getAmount() == null) {
                result.setValid(false);
                result.setErrorMessage("消费金额不能为空");
                return result;
            }

            // 2. 验证金额是否为标准档位
            if (!isStandardAmount(consumeRequest.getAmount())) {
                result.setValid(false);
                result.setErrorMessage("消费金额不符合标准档位，请选择: " + getStandardAmountsDisplay());
                return result;
            }

            // 3. 验证金额是否为正数
            if (consumeRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额必须大于0");
                return result;
            }

            // 4. 验证设备是否支持固定金额模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持固定金额消费模式");
                return result;
            }

            // 5. 验证用户余额
            Map<String, Object> accountInfo = consumeService.getAccountBalance(consumeRequest.getUserId());
            if (accountInfo != null) {
                BigDecimal balance = (BigDecimal) accountInfo.get("balance");
                if (balance != null && balance.compareTo(consumeRequest.getAmount()) < 0) {
                    result.setValid(false);
                    result.setErrorMessage("账户余额不足，当前余额: ¥" + balance);
                    return result;
                }
            }

            result.setValid(true);
            log.debug("固定金额消费请求验证通过");

        } catch (Exception e) {
            log.error("验证固定金额消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理固定金额消费: userId={}, amount={}, deviceId={}",
                consumeRequest.getUserId(), consumeRequest.getAmount(), consumeRequest.getDeviceId());

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

            // 2. 执行消费处理
            Map<String, Object> consumeData = new HashMap<>();
            consumeData.put("userId", consumeRequest.getUserId());
            consumeData.put("amount", consumeRequest.getAmount());
            consumeData.put("personName", consumeRequest.getPersonName());
            consumeData.put("payMethod", consumeRequest.getPayMethod());
            consumeData.put("deviceId", consumeRequest.getDeviceId());
            consumeData.put("consumeMode", getConsumeMode().name());
            consumeData.put("remark", consumeRequest.getRemark());

            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                // 3. 消费成功
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount(consumeRequest.getAmount());
                result.setMessage("固定金额消费成功");
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 4. 记录成功日志
                log.info("固定金额消费成功: userId={}, orderId={}, amount={}",
                        consumeRequest.getUserId(), result.getOrderId(), result.getAmount());

            } else {
                // 5. 消费失败
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount(consumeRequest.getAmount());
                result.setConsumeMode(getConsumeMode());

                log.warn("固定金额消费失败: userId={}, message={}",
                        consumeRequest.getUserId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理固定金额消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("固定金额消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setAmount(consumeRequest.getAmount());
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        // 固定金额模式下，直接使用请求中的金额
        return consumeRequest.getAmount();
    }

    @Override
    public String getModeDescription() {
        return "固定金额模式 - 适用于标准定价的消费场景，如食堂标准餐等";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持固定金额模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取固定金额模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.FIXED_AMOUNT);
            config.setEnabled(true);
            config.setMaxSingleAmount(new BigDecimal("50.00"));
            config.setMinSingleAmount(new BigDecimal("1.00"));
            config.setDailyLimit(new BigDecimal("200.00"));
            config.setSupportedAmounts(STANDARD_AMOUNTS);
            config.setDescription("固定金额消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取固定金额模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 判断是否为标准金额档位
     */
    private boolean isStandardAmount(BigDecimal amount) {
        for (BigDecimal standardAmount : STANDARD_AMOUNTS) {
            if (amount.compareTo(standardAmount) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取标准金额档位显示文本
     */
    private String getStandardAmountsDisplay() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < STANDARD_AMOUNTS.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("¥").append(STANDARD_AMOUNTS[i]);
        }
        return sb.toString();
    }

    /**
     * 获取标准金额档位列表
     */
    public List<BigDecimal> getStandardAmounts() {
        return java.util.Arrays.asList(STANDARD_AMOUNTS);
    }

    /**
     * 检查金额是否在支持范围内
     */
    public boolean isAmountSupported(BigDecimal amount) {
        ConsumeModeConfig config = getModeConfig(null);
        if (config == null) {
            return false;
        }

        BigDecimal minAmount = config.getMinSingleAmount();
        BigDecimal maxAmount = config.getMaxSingleAmount();

        return amount.compareTo(minAmount) >= 0 &&
               amount.compareTo(maxAmount) <= 0 &&
               isStandardAmount(amount);
    }

    /**
     * 获取推荐的金额档位
     */
    public BigDecimal getRecommendedAmount() {
        // 返回最受欢迎的标准档位
        return STANDARD_AMOUNTS[2]; // ¥10.00
    }
}