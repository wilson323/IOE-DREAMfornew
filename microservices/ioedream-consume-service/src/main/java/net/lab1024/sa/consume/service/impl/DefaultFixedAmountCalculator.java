package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.manager.ConsumeAreaManager;

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
public class DefaultFixedAmountCalculator {

    private static final Logger log = LoggerFactory.getLogger(DefaultFixedAmountCalculator.class);

    private final ConsumeAreaManager consumeAreaManager;

    /**
     * 默认构造函数（向后兼容）
     * <p>
     * 当缺少依赖注入时，返回0（避免按当前时间猜测导致测试/生产不可预期）。
     * </p>
     */
    public DefaultFixedAmountCalculator() {
        this.consumeAreaManager = null;
    }

    /**
     * 构造函数注入依赖
     *
     * @param consumeAreaManager 区域管理器
     */
    public DefaultFixedAmountCalculator(ConsumeAreaManager consumeAreaManager) {
        this.consumeAreaManager = consumeAreaManager;
    }

    /**
     * 计算定值消费金额
     *
     * @param request 消费请求
     * @param account 账户信息
     * @return 消费金额（单位：分）
     */
    public Integer calculate(ConsumeRequestDTO request, AccountEntity account) {
        try {
            if (request == null || request.getAreaId() == null) {
                return 0;
            }
            if (consumeAreaManager == null) {
                return 0;
            }

            // 1) 读取区域定值配置（约定：金额单位为元）
            Map<String, Object> config = consumeAreaManager.parseFixedValueConfig(String.valueOf(request.getAreaId()));
            if (config == null || config.isEmpty()) {
                return 0;
            }

            // 2) 根据当前时间选择餐别配置
            BigDecimal baseAmountYuan = selectMealAmount(config);
            if (baseAmountYuan == null || baseAmountYuan.compareTo(BigDecimal.ZERO) <= 0) {
                return 0;
            }

            // 3) 周末倍率（可选）
            BigDecimal weekendMultiplier = toBigDecimalOrNull(config.get("weekendMultiplier"));
            LocalDate today = LocalDate.now();
            DayOfWeek dayOfWeek = today.getDayOfWeek();
            if (weekendMultiplier != null
                    && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) {
                baseAmountYuan = baseAmountYuan.multiply(weekendMultiplier);
            }

            // 4) 简单会员优惠（可选）：累计消费 >= 10000 元，打 9 折（用于测试场景）
            if (account != null
                    && account.getTotalConsumeAmount() != null
                    && account.getTotalConsumeAmount().compareTo(new BigDecimal("10000.00")) >= 0) {
                baseAmountYuan = baseAmountYuan.multiply(new BigDecimal("0.90"));
            }

            // 5) 元 -> 分（四舍五入）
            int amountFen = baseAmountYuan
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            log.debug("[定值计算器] 计算完成，areaId={}, amountFen={}", request.getAreaId(), amountFen);
            return amountFen;

        } catch (Exception e) {
            log.error("[定值计算器] 计算失败", e);
            return 0;
        }
    }

    /**
     * 选择餐别金额（单位：元）
     *
     * @param config 区域定值配置
     * @return 金额（元）
     */
    private static BigDecimal selectMealAmount(Map<String, Object> config) {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();

        // 早餐 6-10、午餐 11-14、晚餐 17-20；其它时间默认午餐
        if (hour >= 6 && hour < 10) {
            return toBigDecimalOrZero(config.get("breakfastAmount"));
        }
        if (hour >= 11 && hour < 14) {
            return toBigDecimalOrZero(config.get("lunchAmount"));
        }
        if (hour >= 17 && hour < 20) {
            return toBigDecimalOrZero(config.get("dinnerAmount"));
        }
        return toBigDecimalOrZero(config.get("lunchAmount"));
    }

    /**
     * 将对象转换为 BigDecimal（空则返回0）
     *
     * @param value 配置值
     * @return BigDecimal
     */
    private static BigDecimal toBigDecimalOrZero(Object value) {
        BigDecimal converted = toBigDecimalOrNull(value);
        return converted != null ? converted : BigDecimal.ZERO;
    }

    /**
     * 将对象转换为 BigDecimal（无法转换则返回null）
     *
     * @param value 配置值
     * @return BigDecimal 或 null
     */
    private static BigDecimal toBigDecimalOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException ignore) {
            return null;
        }
    }
}
