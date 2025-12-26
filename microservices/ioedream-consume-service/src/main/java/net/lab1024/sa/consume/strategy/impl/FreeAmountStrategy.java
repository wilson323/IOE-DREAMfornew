package net.lab1024.sa.consume.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.consume.entity.PosidAccountEntity;
import net.lab1024.sa.consume.entity.PosidAreaEntity;
import net.lab1024.sa.consume.strategy.ConsumeModeStrategy;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 自由金额模式策略
 *
 * 用户输入消费金额，系统验证金额是否在允许范围内
 *
 * 配置参数：
 * - maxAmount: 最大金额限制
 * - minAmount: 最小金额限制
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class FreeAmountStrategy implements ConsumeModeStrategy {

    @Override
    public String getModeType() {
        return "FREE_AMOUNT";
    }

    @Override
    public ConsumeResult calculateAmount(PosidAccountEntity account,
                                         PosidAreaEntity area,
                                         ModeConfig modeConfig,
                                         Map<String, Object> params) {
        log.info("[自由金额模式] 开始计算: accountId={}, areaId={}", account.getAccountId(), area.getAreaId());

        try {
            // 获取用户输入的金额
            Object inputAmountObj = params.get("amount");
            if (inputAmountObj == null) {
                return ConsumeResult.failure("缺少消费金额参数");
            }

            BigDecimal inputAmount;
            if (inputAmountObj instanceof BigDecimal) {
                inputAmount = (BigDecimal) inputAmountObj;
            } else if (inputAmountObj instanceof Number) {
                inputAmount = BigDecimal.valueOf(((Number) inputAmountObj).doubleValue());
            } else if (inputAmountObj instanceof String) {
                inputAmount = new BigDecimal((String) inputAmountObj);
            } else {
                return ConsumeResult.failure("无效的金额类型");
            }

            // 获取配置
            ModeConfig.FreeAmountConfig config = modeConfig.getFreeAmount();
            if (config == null) {
                return ConsumeResult.failure("自由金额配置不存在");
            }

            // 验证金额范围
            if (config.getMinAmount() != null && inputAmount.compareTo(config.getMinAmount()) < 0) {
                return ConsumeResult.failure("消费金额不能低于最小值: " + config.getMinAmount());
            }

            if (config.getMaxAmount() != null && inputAmount.compareTo(config.getMaxAmount()) > 0) {
                return ConsumeResult.failure("消费金额不能超过最大值: " + config.getMaxAmount());
            }

            log.info("[自由金额模式] 计算成功: amount={}", inputAmount);
            return ConsumeResult.success(inputAmount, "计算成功");

        } catch (NumberFormatException e) {
            log.error("[自由金额模式] 金额格式错误: error={}", e.getMessage());
            return ConsumeResult.failure("金额格式错误");
        } catch (Exception e) {
            log.error("[自由金额模式] 计算异常: error={}", e.getMessage(), e);
            return ConsumeResult.failure("计算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params, ModeConfig modeConfig) {
        // 必须包含金额参数
        return params.containsKey("amount");
    }
}
