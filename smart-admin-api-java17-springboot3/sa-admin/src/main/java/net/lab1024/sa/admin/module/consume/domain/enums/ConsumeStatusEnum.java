package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费状态枚举
 * <p>
 * 严格遵循repowiki规范:
 * - 使用枚举定义业务状态
 * - 提供中文描述
 * - 包含状态值和含义
 * - 支持业务逻辑判断
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Getter
@AllArgsConstructor
public enum ConsumeStatusEnum {

    /**
     * 待支付
     */
    PENDING(1, "待支付"),

    /**
     * 支付成功
     */
    SUCCESS(2, "支付成功"),

    /**
     * 支付失败
     */
    FAILED(3, "支付失败"),

    /**
     * 已退款
     */
    REFUNDED(4, "已退款"),

    /**
     * 部分退款
     */
    PARTIAL_REFUNDED(5, "部分退款"),

    /**
     * 已取消
     */
    CANCELLED(6, "已取消");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据值获取枚举
     *
     * @param value 状态值
     * @return 枚举实例
     */
    public static ConsumeStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ConsumeStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为成功状态
     *
     * @return 是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为终态
     *
     * @return 是否为终态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == REFUNDED ||
               this == PARTIAL_REFUNDED || this == CANCELLED;
    }

    /**
     * 判断是否可以退款
     *
     * @return 是否可以退款
     */
    public boolean canRefund() {
        return this == SUCCESS || this == PARTIAL_REFUNDED;
    }
}