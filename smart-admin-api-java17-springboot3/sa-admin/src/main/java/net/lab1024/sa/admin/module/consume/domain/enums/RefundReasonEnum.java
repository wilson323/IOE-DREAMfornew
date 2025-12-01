package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款原因枚举
 * <p>
 * 严格遵循repowiki规范:
 * - 使用枚举定义业务原因
 * - 提供中文描述
 * - 包含原因分类
 * - 支持业务逻辑判断
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Getter
@AllArgsConstructor
public enum RefundReasonEnum {

    /**
     * 用户误操作
     */
    USER_MISTAKE(1, "用户误操作", "用户操作"),

    /**
     * 商品质量问题
     */
    PRODUCT_QUALITY(2, "商品质量问题", "商品问题"),

    /**
     * 服务不满意
     */
    SERVICE_DISSATISFACTION(3, "服务不满意", "服务问题"),

    /**
     * 重复支付
     */
    DUPLICATE_PAYMENT(4, "重复支付", "系统问题"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(5, "系统错误", "系统问题"),

    /**
     * 其他原因
     */
    OTHER(99, "其他原因", "其他");

    /**
     * 原因值
     */
    private final Integer value;

    /**
     * 原因描述
     */
    private final String description;

    /**
     * 原因分类
     */
    private final String category;

    /**
     * 根据值获取枚举
     *
     * @param value 原因值
     * @return 枚举实例
     */
    public static RefundReasonEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RefundReasonEnum reason : values()) {
            if (reason.getValue().equals(value)) {
                return reason;
            }
        }
        return null;
    }

    /**
     * 判断是否为系统原因
     *
     * @return 是否为系统原因
     */
    public boolean isSystemReason() {
        return "系统问题".equals(this.category);
    }

    /**
     * 判断是否为用户原因
     *
     * @return 是否为用户原因
     */
    public boolean isUserReason() {
        return "用户操作".equals(this.category);
    }
}