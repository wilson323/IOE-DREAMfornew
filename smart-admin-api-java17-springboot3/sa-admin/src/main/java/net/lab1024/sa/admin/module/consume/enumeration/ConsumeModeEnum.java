package net.lab1024.sa.admin.module.consume.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费模式枚举
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Getter
@AllArgsConstructor
public enum ConsumeModeEnum {

    /**
     * 固定金额消费
     */
    FIXED_AMOUNT("FIXED_AMOUNT", "固定金额", "按预设的固定金额进行消费"),

    /**
     * 自由金额消费
     */
    FREE_AMOUNT("FREE_AMOUNT", "自由金额", "用户自定义消费金额，支持任意金额输入"),

    /**
     * 计量消费
     */
    METERING("METERING", "计量消费", "按使用量计算费用，如水费、电费等"),

    /**
     * 产品扫码消费
     */
    PRODUCT_SCAN("PRODUCT_SCAN", "产品扫码", "扫描产品条码或二维码进行消费"),

    /**
     * 点餐消费
     */
    ORDERING("ORDERING", "点餐消费", "通过菜单选择商品进行消费"),

    /**
     * 套餐消费
     */
    PACKAGE("PACKAGE", "套餐消费", "选择预设套餐进行消费"),

    /**
     * 会员卡消费
     */
    MEMBER_CARD("MEMBER_CARD", "会员卡消费", "使用会员卡余额或积分进行消费"),

    /**
     * 积分兑换
     */
    POINTS_EXCHANGE("POINTS_EXCHANGE", "积分兑换", "使用积分兑换商品或服务"),

    /**
     * 优惠券消费
     */
    COUPON("COUPON", "优惠券消费", "使用优惠券进行消费抵扣");

    /**
     * 枚举编码
     */
    private final String code;

    /**
     * 枚举显示名称
     */
    private final String name;

    /**
     * 枚举描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举对象
     */
    public static ConsumeModeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ConsumeModeEnum consumeMode : values()) {
            if (consumeMode.getCode().equals(code)) {
                return consumeMode;
            }
        }
        return null;
    }

    /**
     * 是否需要输入金额
     */
    public boolean isAmountInputRequired() {
        return this == FREE_AMOUNT || this == METERING;
    }

    /**
     * 是否需要扫码
     */
    public boolean isScanRequired() {
        return this == PRODUCT_SCAN;
    }

    /**
     * 是否需要选择商品
     */
    public boolean isProductSelectionRequired() {
        return this == ORDERING || this == PACKAGE;
    }

    /**
     * 是否需要身份验证
     */
    public boolean isIdentityVerificationRequired() {
        return this == MEMBER_CARD || this == POINTS_EXCHANGE;
    }

    /**
     * 是否支持优惠券
     */
    public boolean isCouponSupported() {
        return this == FIXED_AMOUNT || this == FREE_AMOUNT || this == ORDERING || this == PACKAGE;
    }

    /**
     * 是否支持会员折扣
     */
    public boolean isMemberDiscountSupported() {
        return this == FIXED_AMOUNT || this == FREE_AMOUNT || this == ORDERING || this == PACKAGE;
    }
}