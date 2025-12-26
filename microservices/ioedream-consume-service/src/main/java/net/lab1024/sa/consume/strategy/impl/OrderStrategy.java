package net.lab1024.sa.consume.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.ModeConfig;
import net.lab1024.sa.common.entity.consume.ConsumeAccountEntity;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.consume.strategy.ConsumeModeStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订餐模式策略
 *
 * 用户提前订餐，系统记录订餐信息，扣款时间可配置
 *
 * 配置参数：
 * - advanceHours: 提前订餐时长（小时）
 * - pickupTimeRange: 取餐时段
 * - deductTimeStrategy: 扣款时间策略（PICKUP-取餐时扣款, ORDER-下单时扣款）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class OrderStrategy implements ConsumeModeStrategy {

    @Override
    public String getModeType() {
        return "ORDER";
    }

    @Override
    public ConsumeResult calculateAmount(ConsumeAccountEntity account,
                                         AreaEntity area,
                                         ModeConfig modeConfig,
                                         Map<String, Object> params) {
        log.info("[订餐模式] 开始计算: accountId={}, areaId={}", account.getAccountId(), area.getAreaId());

        try {
            // 获取订餐金额
            Object amountObj = params.get("amount");
            if (amountObj == null) {
                return ConsumeResult.failure("缺少订餐金额参数");
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());

            // 获取订餐时间
            Object orderTimeObj = params.get("orderTime");
            LocalDateTime orderTime = orderTimeObj != null
                    ? LocalDateTime.parse(orderTimeObj.toString())
                    : LocalDateTime.now();

            // 获取取餐时间
            Object pickupTimeObj = params.get("pickupTime");
            if (pickupTimeObj == null) {
                return ConsumeResult.failure("缺少取餐时间参数");
            }
            LocalDateTime pickupTime = LocalDateTime.parse(pickupTimeObj.toString());

            // 获取配置
            ModeConfig.OrderConfig config = modeConfig.getOrder();
            if (config == null) {
                return ConsumeResult.failure("订餐模式配置不存在");
            }

            // 验证提前订餐时长
            if (config.getAdvanceHours() != null) {
                long hoursDifference = java.time.Duration.between(orderTime, pickupTime).toHours();
                if (hoursDifference < config.getAdvanceHours()) {
                    return ConsumeResult.failure("订餐时间不足: 需提前" + config.getAdvanceHours() + "小时");
                }
            }

            log.info("[订餐模式] 计算成功: amount={}, orderTime={}, pickupTime={}, deductStrategy={}",
                    amount, orderTime, pickupTime, config.getDeductTimeStrategy());

            ConsumeResult result = ConsumeResult.success(amount, "订餐成功");
            result.getDetails().put("orderTime", orderTime);
            result.getDetails().put("pickupTime", pickupTime);
            result.getDetails().put("deductStrategy", config.getDeductTimeStrategy());

            return result;

        } catch (Exception e) {
            log.error("[订餐模式] 计算异常: error={}", e.getMessage(), e);
            return ConsumeResult.failure("计算异常: " + e.getMessage());
        }
    }

    @Override
    public boolean validateParams(Map<String, Object> params, ModeConfig modeConfig) {
        // 必须包含金额和取餐时间
        return params.containsKey("amount") && params.containsKey("pickupTime");
    }
}
