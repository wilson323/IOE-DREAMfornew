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
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;

/**
 * 自由金额消费模式引擎
 * 适用于用户可以自由输入消费金额的场景，如超市购物、个性化服务等
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class FreeAmountConsumeEngine implements ConsumeModeEngine {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    // 默认配置
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999.99");
    private static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("500.00");

    @Override
    public ConsumeModeEnum getConsumeMode() {
        return ConsumeModeEnum.FREE_AMOUNT;
    }

    @Override
    public ConsumeValidationResult validateRequest(ConsumeRequestDTO consumeRequest) {
        log.debug("验证自由金额消费请求: userId={}, amount={}", consumeRequest.getUserId(), consumeRequest.getAmount());

        ConsumeValidationResult result = new ConsumeValidationResult();

        try {
            // 1. 验证消费金额是否为空
            if (consumeRequest.getAmount() == null) {
                result.setValid(false);
                result.setErrorMessage("消费金额不能为空");
                return result;
            }

            // 2. 验证金额精度（最多两位小数）
            if (hasMoreThanTwoDecimalPlaces(consumeRequest.getAmount())) {
                result.setValid(false);
                result.setErrorMessage("消费金额最多支持两位小数");
                return result;
            }

            // 3. 验证金额范围
            ConsumeModeConfig config = getModeConfig(consumeRequest.getDeviceId());
            BigDecimal minAmount = config != null ? config.getMinSingleAmount() : MIN_AMOUNT;
            BigDecimal maxAmount = config != null ? config.getMaxSingleAmount() : MAX_AMOUNT;

            if (consumeRequest.getAmount().compareTo(minAmount) < 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额不能少于 ¥" + minAmount);
                return result;
            }

            if (consumeRequest.getAmount().compareTo(maxAmount) > 0) {
                result.setValid(false);
                result.setErrorMessage("消费金额不能超过 ¥" + maxAmount);
                return result;
            }

            // 4. 验证设备是否支持自由金额模式
            if (!isModeAvailable(consumeRequest.getDeviceId())) {
                result.setValid(false);
                result.setErrorMessage("设备不支持自由金额消费模式");
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

            // 6. 验证当日消费限额（如果配置了）
            if (config != null && config.getDailyLimit() != null) {
                BigDecimal todayConsume = consumeCacheService.getTodayConsumeAmount(consumeRequest.getUserId());
                BigDecimal totalAmount = todayConsume.add(consumeRequest.getAmount());
                if (totalAmount.compareTo(config.getDailyLimit()) > 0) {
                    result.setValid(false);
                    result.setErrorMessage("超过当日消费限额，今日已消费: ¥" + todayConsume + "，限额: ¥" + config.getDailyLimit());
                    return result;
                }
            }

            result.setValid(true);
            log.debug("自由金额消费请求验证通过");

        } catch (Exception e) {
            log.error("验证自由金额消费请求失败", e);
            result.setValid(false);
            result.setErrorMessage("验证消费请求时发生错误");
        }

        return result;
    }

    @Override
    public ConsumeResultDTO processConsume(ConsumeRequestDTO consumeRequest) {
        log.info("处理自由金额消费: userId={}, amount={}, deviceId={}",
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

            // 2. 格式化金额（确保两位小数）
            BigDecimal formattedAmount = consumeRequest.getAmount().setScale(2, RoundingMode.HALF_UP);

            // 3. 执行消费处理
            Map<String, Object> consumeData = new HashMap<>();
            consumeData.put("userId", consumeRequest.getUserId());
            consumeData.put("amount", formattedAmount);
            consumeData.put("personName", consumeRequest.getPersonName());
            consumeData.put("payMethod", consumeRequest.getPayMethod());
            consumeData.put("deviceId", consumeRequest.getDeviceId());
            consumeData.put("consumeMode", getConsumeMode().name());
            consumeData.put("remark", consumeRequest.getRemark());

            Map<String, Object> processResult = consumeService.processConsume(consumeData);

            if (Boolean.TRUE.equals(processResult.get("success"))) {
                // 4. 消费成功
                result.setSuccess(true);
                result.setOrderId((Long) processResult.get("orderId"));
                result.setAmount(formattedAmount);
                result.setMessage("自由金额消费成功");
                result.setStatus(ConsumeStatusEnum.SUCCESS);
                result.setConsumeTime((java.time.LocalDateTime) processResult.get("consumeTime"));
                result.setNewBalance((BigDecimal) processResult.get("newBalance"));
                result.setConsumeMode(getConsumeMode());

                // 5. 更新当日消费缓存
                updateDailyConsumeCache(consumeRequest.getUserId(), formattedAmount);

                // 6. 记录成功日志
                log.info("自由金额消费成功: userId={}, orderId={}, amount={}",
                        consumeRequest.getUserId(), result.getOrderId(), result.getAmount());

            } else {
                // 7. 消费失败
                result.setSuccess(false);
                result.setMessage((String) processResult.get("message"));
                result.setStatus(ConsumeStatusEnum.FAILED);
                result.setAmount(formattedAmount);
                result.setConsumeMode(getConsumeMode());

                log.warn("自由金额消费失败: userId={}, message={}",
                        consumeRequest.getUserId(), result.getMessage());
            }

        } catch (Exception e) {
            log.error("处理自由金额消费异常: userId={}", consumeRequest.getUserId(), e);
            result.setSuccess(false);
            result.setMessage("自由金额消费处理异常");
            result.setStatus(ConsumeStatusEnum.FAILED);
            result.setAmount(consumeRequest.getAmount());
            result.setConsumeMode(getConsumeMode());
        }

        return result;
    }

    @Override
    public BigDecimal calculateAmount(ConsumeRequestDTO consumeRequest) {
        // 自由金额模式下，格式化金额到两位小数
        return consumeRequest.getAmount().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getModeDescription() {
        return "自由金额模式 - 支持用户自由输入消费金额，适用于超市、个性化服务等场景";
    }

    @Override
    public boolean isModeAvailable(Long deviceId) {
        // 检查设备是否支持自由金额模式
        ConsumeModeConfig config = getModeConfig(deviceId);
        return config != null && config.isEnabled();
    }

    @Override
    public ConsumeModeConfig getModeConfig(Long deviceId) {
        try {
            // TODO: 从设备配置中获取自由金额模式配置
            // 这里简化实现，返回默认配置
            ConsumeModeConfig config = new ConsumeModeConfig();
            config.setModeType(ConsumeModeEnum.FREE_AMOUNT);
            config.setEnabled(true);
            config.setMaxSingleAmount(MAX_AMOUNT);
            config.setMinSingleAmount(MIN_AMOUNT);
            config.setDailyLimit(DEFAULT_DAILY_LIMIT);
            config.setAllowCustomAmount(true);
            config.setDescription("自由金额消费模式配置");
            return config;
        } catch (Exception e) {
            log.error("获取自由金额模式配置失败: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * 检查金额是否超过两位小数
     */
    private boolean hasMoreThanTwoDecimalPlaces(BigDecimal amount) {
        return amount.scale() > 2;
    }

    /**
     * 更新当日消费缓存
     */
    private void updateDailyConsumeCache(Long userId, BigDecimal amount) {
        try {
            String cacheKey = "daily_consume:" + userId + ":" + java.time.LocalDate.now();

            // 获取当日已消费金额
            BigDecimal currentConsume = consumeCacheService.getCachedValue(cacheKey, BigDecimal.class);
            if (currentConsume == null) {
                currentConsume = BigDecimal.ZERO;
            }

            // 更新当日消费金额
            BigDecimal totalConsume = currentConsume.add(amount);
            consumeCacheService.setCachedValue(cacheKey, totalConsume, 24 * 60 * 60); // 缓存24小时

            log.debug("更新当日消费缓存: userId={}, amount={}, total={}", userId, amount, totalConsume);

        } catch (Exception e) {
            log.error("更新当日消费缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 获取当日消费金额
     */
    public BigDecimal getTodayConsumeAmount(Long userId) {
        try {
            String cacheKey = "daily_consume:" + userId + ":" + java.time.LocalDate.now();
            BigDecimal amount = consumeCacheService.getCachedValue(cacheKey, BigDecimal.class);
            return amount != null ? amount : BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("获取当日消费金额失败: userId={}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 检查金额是否在允许范围内
     */
    public boolean isAmountInRange(BigDecimal amount) {
        ConsumeModeConfig config = getModeConfig(null);
        if (config == null) {
            return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
        }

        return amount.compareTo(config.getMinSingleAmount()) >= 0 &&
               amount.compareTo(config.getMaxSingleAmount()) <= 0 &&
               !hasMoreThanTwoDecimalPlaces(amount);
    }

    /**
     * 格式化金额
     */
    public BigDecimal formatAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取建议金额
     */
    public BigDecimal[] getRecommendedAmounts() {
        return new BigDecimal[]{
            new BigDecimal("1.00"),
            new BigDecimal("5.00"),
            new BigDecimal("10.00"),
            new BigDecimal("20.00"),
            new BigDecimal("50.00"),
            new BigDecimal("100.00")
        };
    }
}