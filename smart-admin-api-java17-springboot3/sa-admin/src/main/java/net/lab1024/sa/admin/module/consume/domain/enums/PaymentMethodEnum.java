package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.Getter;

/**
 * 支付方式枚举
 *
 * <p>
 * 严格遵循repowiki规范:
 * - 使用枚举定义常量，避免魔法数字
 * - 提供完整的业务信息和描述
 * - 支持根据代码获取枚举实例
 * - 包含友好的显示名称
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Getter
public enum PaymentMethodEnum {

    /**
     * 现金支付
     */
    CASH(1, "现金", "现金支付方式"),

    /**
     * 银行卡支付
     */
    BANK_CARD(2, "银行卡", "银行卡支付方式"),

    /**
     * 微信支付
     */
    WECHAT(3, "微信", "微信支付方式"),

    /**
     * 支付宝支付
     */
    ALIPAY(4, "支付宝", "支付宝支付方式"),

    /**
     * 银行转账
     */
    BANK_TRANSFER(5, "银行转账", "银行转账方式"),

    /**
     * 其他支付方式
     */
    OTHER(99, "其他", "其他支付方式");

    /**
     * 支付方式代码
     */
    private final Integer code;

    /**
     * 支付方式名称
     */
    private final String name;

    /**
     * 支付方式描述
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param code        支付方式代码
     * @param name        支付方式名称
     * @param description 支付方式描述
     */
    PaymentMethodEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据代码获取支付方式枚举
     *
     * @param code 支付方式代码
     * @return 支付方式枚举，如果未找到返回null
     */
    public static PaymentMethodEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaymentMethodEnum method : values()) {
            if (method.getCode().equals(code)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 根据名称获取支付方式枚举
     *
     * @param name 支付方式名称
     * @return 支付方式枚举，如果未找到返回null
     */
    public static PaymentMethodEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (PaymentMethodEnum method : values()) {
            if (method.getName().equals(name.trim())) {
                return method;
            }
        }
        return null;
    }

    /**
     * 根据字符串代码获取支付方式枚举
     *
     * @param codeStr 支付方式代码字符串
     * @return 支付方式枚举，如果未找到返回null
     */
    public static PaymentMethodEnum getByCode(String codeStr) {
        if (codeStr == null || codeStr.trim().isEmpty()) {
            return null;
        }
        try {
            Integer code = Integer.parseInt(codeStr.trim());
            return getByCode(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断是否为线上支付方式
     *
     * @return 是否为线上支付
     */
    public boolean isOnlinePayment() {
        return this == WECHAT || this == ALIPAY || this == BANK_CARD;
    }

    /**
     * 判断是否为线下支付方式
     *
     * @return 是否为线下支付
     */
    public boolean isOfflinePayment() {
        return this == CASH || this == BANK_TRANSFER;
    }

    /**
     * 判断是否需要手续费
     *
     * @return 是否需要手续费
     */
    public boolean hasFee() {
        // 通常线上支付需要手续费
        return this == WECHAT || this == ALIPAY || this == BANK_CARD;
    }

    /**
     * 获取手续费率（示例）
     *
     * @return 手续费率，百分比形式（例如：0.6% 返回 0.006）
     */
    public double getFeeRate() {
        if (!hasFee()) {
            return 0.0;
        }
        switch (this) {
            case WECHAT:
                return 0.006;  // 0.6%
            case ALIPAY:
                return 0.006;  // 0.6%
            case BANK_CARD:
                return 0.008;  // 0.8%
            default:
                return 0.0;
        }
    }

    /**
     * 计算手续费
     *
     * @param amount 金额
     * @return 手续费金额
     */
    public java.math.BigDecimal calculateFee(java.math.BigDecimal amount) {
        if (amount == null || amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return java.math.BigDecimal.ZERO;
        }

        if (!hasFee()) {
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal feeRate = java.math.BigDecimal.valueOf(getFeeRate());
        java.math.BigDecimal fee = amount.multiply(feeRate);

        // 手续费最小为1分
        java.math.BigDecimal minFee = new java.math.BigDecimal("0.01");
        return fee.compareTo(minFee) < 0 ? minFee : fee;
    }

    @Override
    public String toString() {
        return String.format("PaymentMethodEnum{code=%d, name='%s', description='%s'}",
                           code, name, description);
    }
}