package net.lab1024.sa.consume.util;

import java.math.BigDecimal;

/**
 * 消费账户工具类
 * <p>
 * 提供消费账户相关的工具方法
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public class ConsumeAccountHelper {

    /**
     * 获取账户状态名称
     *
     * @param status 状态码
     * @return 状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "正常";
            case 2:
                return "冻结";
            case 3:
                return "注销";
            case 4:
                return "挂失";
            default:
                return "未知";
        }
    }

    /**
     * 获取账户类型名称
     *
     * @param accountType 类型码
     * @return 类型名称
     */
    public static String getAccountTypeName(Integer accountType) {
        if (accountType == null) {
            return "未知";
        }
        switch (accountType) {
            case 1:
                return "个人账户";
            case 2:
                return "部门账户";
            case 3:
                return "临时账户";
            case 4:
                return "访客账户";
            default:
                return "未知";
        }
    }

    /**
     * 检查账户是否正常
     *
     * @param status 账户状态
     * @return true-正常，false-异常
     */
    public static boolean isNormal(Integer status) {
        return status != null && status == 1;
    }

    /**
     * 检查账户是否冻结
     *
     * @param status 账户状态
     * @return true-冻结，false-未冻结
     */
    public static boolean isFrozen(Integer status) {
        return status != null && status == 2;
    }

    /**
     * 检查余额是否充足
     *
     * @param availableBalance 可用余额
     * @param amount 需要的金额
     * @return true-余额充足，false-余额不足
     */
    public static boolean hasEnoughBalance(BigDecimal availableBalance, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        BigDecimal available = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        return available.compareTo(amount) >= 0;
    }

    /**
     * 检查是否设置了密码
     *
     * @param hasPassword 密码设置标志
     * @return true-已设置，false-未设置
     */
    public static boolean hasPassword(Integer hasPassword) {
        return hasPassword != null && hasPassword == 1;
    }

    /**
     * 计算总可用额度（可用余额 + 可用信用额度）
     *
     * @param availableBalance 可用余额
     * @param availableCredit 可用信用额度
     * @return 总可用额度
     */
    public static BigDecimal getTotalAvailableCredit(BigDecimal availableBalance, BigDecimal availableCredit) {
        BigDecimal available = availableBalance != null ? availableBalance : BigDecimal.ZERO;
        BigDecimal credit = availableCredit != null ? availableCredit : BigDecimal.ZERO;
        return available.add(credit);
    }
}
