package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义优惠券类型，避免魔法数字
 * - 提供完整的业务信息和描述
 * - 支持根据代码获取枚举实例
 * - 包含友好的显示名称
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Getter
@AllArgsConstructor
public enum CouponTypeEnum {

    /**
     * 固定金额优惠券
     */
    FIXED_AMOUNT("FIXED_AMOUNT", "固定金额", "减免固定金额的优惠券"),

    /**
     * 百分比折扣优惠券
     */
    PERCENTAGE_DISCOUNT("PERCENTAGE_DISCOUNT", "百分比折扣", "按百分比折扣的优惠券"),

    /**
     * 满减优惠券
     */
    FULL_REDUCTION("FULL_REDUCTION", "满减", "满足一定金额后减免的优惠券"),

    /**
     * 免费优惠券
     */
    FREE("FREE", "免费", "完全免费的优惠券"),

    /**
     * 买一送一
     */
    BUY_ONE_GET_ONE("BUY_ONE_GET_ONE", "买一送一", "购买一个商品免费获得另一个"),

    /**
     * 积分兑换
     */
    POINTS_EXCHANGE("POINTS_EXCHANGE", "积分兑换", "使用积分兑换的商品或服务"),

    /**
     * 时段优惠
     */
    TIME_DISCOUNT("TIME_DISCOUNT", "时段优惠", "特定时间段内享受优惠"),

    /**
     * 新用户优惠
     */
    NEW_USER("NEW_USER", "新用户优惠", "仅限新用户使用的优惠券");

    /**
     * 优惠券类型代码
     */
    private final String code;

    /**
     * 优惠券类型名称
     */
    private final String name;

    /**
     * 优惠券类型描述
     */
    private final String description;

    /**
     * 根据代码获取优惠券类型枚举
     *
     * @param code 优惠券类型代码
     * @return 优惠券类型枚举，如果未找到返回null
     */
    public static CouponTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (CouponTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取优惠券类型枚举
     *
     * @param name 优惠券类型名称
     * @return 优惠券类型枚举，如果未找到返回null
     */
    public static CouponTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (CouponTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为直接优惠类型
     *
     * @return 是否为直接优惠类型
     */
    public boolean isDirectDiscount() {
        return this == FIXED_AMOUNT || this == PERCENTAGE_DISCOUNT || this == FREE;
    }

    /**
     * 判断是否为条件优惠类型
     *
     * @return 是否为条件优惠类型
     */
    public boolean isConditionalDiscount() {
        return this == FULL_REDUCTION || this == TIME_DISCOUNT || this == NEW_USER;
    }

    /**
     * 判断是否为特殊优惠类型
     *
     * @return 是否为特殊优惠类型
     */
    public boolean isSpecialDiscount() {
        return this == BUY_ONE_GET_ONE || this == POINTS_EXCHANGE;
    }

    /**
     * 判断是否需要验证使用条件
     *
     * @return 是否需要验证使用条件
     */
    public boolean requiresConditionCheck() {
        return this == FULL_REDUCTION || this == TIME_DISCOUNT || this == NEW_USER || this == BUY_ONE_GET_ONE;
    }

    /**
     * 获取优惠类型优先级（数字越小优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case FREE:
                return 1;
            case BUY_ONE_GET_ONE:
                return 2;
            case FIXED_AMOUNT:
                return 3;
            case PERCENTAGE_DISCOUNT:
                return 4;
            case FULL_REDUCTION:
                return 5;
            case NEW_USER:
                return 6;
            case TIME_DISCOUNT:
                return 7;
            case POINTS_EXCHANGE:
                return 8;
            default:
                return 99;
        }
    }

    @Override
    public String toString() {
        return String.format("CouponTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}