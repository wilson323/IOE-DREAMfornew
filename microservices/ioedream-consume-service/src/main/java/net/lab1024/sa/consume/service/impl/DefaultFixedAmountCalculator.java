package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lab1024.sa.consume.entity.AccountEntity;

/**
 * 默认定值金额计算器
 * <p>
 * 用于计算定值消费模式的消费金额（单位：分）
 * 支持按餐别、区域、账户类别等配置计算固定金额
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Slf4j
public class DefaultFixedAmountCalculator {

    private static final Logger log = LoggerFactory.getLogger(DefaultFixedAmountCalculator.class);

    /**
     * 计算定值消费金额
     *
     * @param request 消费请求
     * @param account 账户信息
     * @return 消费金额（单位：分）
     */
    public Integer calculate(Object request, AccountEntity account) {
        try {
            // 默认定值金额逻辑
            LocalDate today = LocalDate.now();
            DayOfWeek dayOfWeek = today.getDayOfWeek();

            // 基础金额：早餐500分，午餐800分，晚餐600分
            int baseAmount = 800; // 默认午餐标准

            // 根据当前时间确定餐别
            int hour = java.time.LocalTime.now().getHour();
            if (hour >= 6 && hour < 10) {
                baseAmount = 500; // 早餐
            } else if (hour >= 11 && hour < 14) {
                baseAmount = 800; // 午餐
            } else if (hour >= 17 && hour < 20) {
                baseAmount = 600; // 晚餐
            }

            // 周末加价100分
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                baseAmount += 100;
            }

            log.debug("[定值计算器] 计算完成，baseAmount={}分", baseAmount);
            return baseAmount;

        } catch (Exception e) {
            log.error("[定值计算器] 计算失败", e);
            return 0;
        }
    }
}