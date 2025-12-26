package net.lab1024.sa.consume.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.consume.entity.PosidAccountEntity;
import net.lab1024.sa.consume.entity.PosidAreaEntity;
import net.lab1024.sa.consume.strategy.ConsumeModeStrategy;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 计量计费模式策略
 *
 * 支持的子类型：
 * - TIMING: 计时模式（按时间计费）
 * - COUNTING: 计数模式（按数量计费）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class MeteredStrategy implements ConsumeModeStrategy {

    @Override
    public String getModeType() {
        return "METERED";
    }

    @Override
    public ConsumeResult calculateAmount(PosidAccountEntity account,
                                         PosidAreaEntity area,
                                         ModeConfig modeConfig,
                                         Map<String, Object> params) {
        log.info("[计量计费模式] 开始计算: accountId={}, areaId={}", account.getAccountId(), area.getAreaId());

        try {
            // 获取配置
            ModeConfig.MeteredConfig config = modeConfig.getMetered();
            if (config == null) {
                return ConsumeResult.failure("计量计费配置不存在");
            }

            BigDecimal unitPrice = config.getUnitPrice();
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return ConsumeResult.failure("单价配置错误");
            }

            String subType = config.getSubType();
            BigDecimal amount;

            switch (subType) {
                case "TIMING":
                    // 计时模式：按分钟计费
                    Object minutesObj = params.get("minutes");
                    if (minutesObj == null) {
                        return ConsumeResult.failure("缺少时长参数");
                    }

                    int minutes = ((Number) minutesObj).intValue();
                    if (minutes <= 0) {
                        return ConsumeResult.failure("时长必须大于0");
                    }

                    amount = unitPrice.multiply(BigDecimal.valueOf(minutes));
                    log.info("[计量计费模式] 计时模式: minutes={}, unitPrice={}, amount={}",
                            minutes, unitPrice, amount);
                    return ConsumeResult.success(amount, "计算成功");

                case "COUNTING":
                    // 计数模式：按数量计费
                    Object quantityObj = params.get("quantity");
                    if (quantityObj == null) {
                        return ConsumeResult.failure("缺少数量参数");
                    }

                    int quantity = ((Number) quantityObj).intValue();
                    if (quantity <= 0) {
                        return ConsumeResult.failure("数量必须大于0");
                    }

                    amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    log.info("[计量计费模式] 计数模式: quantity={}, unitPrice={}, amount={}",
                            quantity, unitPrice, amount);
                    return ConsumeResult.success(amount, "计算成功");

                default:
                    return ConsumeResult.failure("未知的计量计费子类型: " + subType);
            }

        } catch (Exception e) {
            log.error("[计量计费模式] 计算异常: error={}", e.getMessage(), e);
            return ConsumeResult.failure("计算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params, ModeConfig modeConfig) {
        String subType = modeConfig.getMetered().getSubType();

        switch (subType) {
            case "TIMING":
                return params.containsKey("minutes");

            case "COUNTING":
                return params.containsKey("quantity");

            default:
                return false;
        }
    }
}
