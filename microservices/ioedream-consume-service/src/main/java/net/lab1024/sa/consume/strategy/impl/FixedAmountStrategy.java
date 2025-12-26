package net.lab1024.sa.consume.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.AreaConfig;
import net.lab1024.sa.consume.domain.config.FixedValueConfig;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.consume.strategy.ConsumeModeStrategy;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

/**
 * 固定金额模式策略
 *
 * 支持的子类型：
 * - SIMPLE: 简单固定金额（所有时段统一价格）
 * - KEYVALUE: 餐别映射（不同餐别不同价格）
 * - SECTION: 时段区间（不同时段不同价格）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class FixedAmountStrategy implements ConsumeModeStrategy {

    @Override
    public String getModeType() {
        return "FIXED_AMOUNT";
    }

    @Override
    public ConsumeResult calculateAmount(ConsumeAccountEntity account,
                                         AreaEntity area,
                                         ModeConfig modeConfig,
                                         Map<String, Object> params) {
        log.info("[固定金额模式] 开始计算: accountId={}, areaId={}", account.getAccountId(), area.getAreaId());

        try {
            // 获取固定金额配置
            ModeConfig.FixedAmountConfig config = modeConfig.getFixedAmount();
            if (config == null) {
                return ConsumeResult.failure("固定金额配置不存在");
            }

            String subType = config.getSubType();
            BigDecimal amount;

            switch (subType) {
                case "SIMPLE":
                    // 简单固定金额
                    amount = config.getAmount();
                    if (amount == null) {
                        return ConsumeResult.failure("简单固定金额配置错误");
                    }
                    log.info("[固定金额模式] 简单固定金额: amount={}", amount);
                    return ConsumeResult.success(amount, "计算成功");

                case "KEYVALUE":
                    // 餐别映射金额
                    String mealType = (String) params.get("mealType");
                    if (mealType == null) {
                        return ConsumeResult.failure("缺少餐别参数");
                    }

                    if (config.getMealAmounts() == null) {
                        // 使用区域定值配置
                        amount = getAreaFixedAmount(area, mealType);
                    } else {
                        amount = config.getMealAmounts().get(mealType);
                    }

                    if (amount == null) {
                        return ConsumeResult.failure("餐别[" + mealType + "]未配置金额");
                    }
                    log.info("[固定金额模式] 餐别映射金额: mealType={}, amount={}", mealType, amount);
                    return ConsumeResult.success(amount, "计算成功");

                case "SECTION":
                    // 时段区间金额
                    LocalTime now = LocalTime.now();
                    String timeSection = getTimeSection(now);

                    if (config.getTimeSectionAmounts() == null) {
                        return ConsumeResult.failure("时段区间配置不存在");
                    }

                    amount = config.getTimeSectionAmounts().get(timeSection);
                    if (amount == null) {
                        return ConsumeResult.failure("时段[" + timeSection + "]未配置金额");
                    }
                    log.info("[固定金额模式] 时段区间金额: timeSection={}, amount={}", timeSection, amount);
                    return ConsumeResult.success(amount, "计算成功");

                default:
                    return ConsumeResult.failure("未知的固定金额子类型: " + subType);
            }

        } catch (Exception e) {
            log.error("[固定金额模式] 计算异常: error={}", e.getMessage(), e);
            return ConsumeResult.failure("计算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params, ModeConfig modeConfig) {
        String subType = modeConfig.getFixedAmount().getSubType();

        switch (subType) {
            case "SIMPLE":
                // 简单固定金额不需要额外参数
                return true;

            case "KEYVALUE":
                // 需要餐别参数
                return params.containsKey("mealType");

            case "SECTION":
                // 时段区间不需要额外参数（自动识别当前时段）
                return true;

            default:
                return false;
        }
    }

    /**
     * 从区域定值配置获取金额
     * 三级优先级：账户类别 > 区域 > 全局默认
     */
    private BigDecimal getAreaFixedAmount(AreaEntity area, String mealType) {
        FixedValueConfig fixedValueConfig = area.getFixedValueConfig();
        if (fixedValueConfig == null) {
            return null;
        }

        // 优先级1: 餐别映射金额
        if (fixedValueConfig.getMealAmounts() != null) {
            BigDecimal amount = fixedValueConfig.getMealAmounts().get(mealType);
            if (amount != null) {
                return amount;
            }
        }

        // 优先级2: 全局默认金额
        return fixedValueConfig.getDefaultAmount();
    }

    /**
     * 根据时间获取时段编码
     */
    private String getTimeSection(LocalTime time) {
        int hour = time.getHour();

        if (hour >= 6 && hour < 11) {
            return "MORNING";    // 早晨
        } else if (hour >= 11 && hour < 14) {
            return "NOON";       // 中午
        } else if (hour >= 14 && hour < 18) {
            return "AFTERNOON";  // 下午
        } else if (hour >= 18 && hour < 22) {
            return "EVENING";    // 晚上
        } else {
            return "NIGHT";      // 夜间
        }
    }
}
