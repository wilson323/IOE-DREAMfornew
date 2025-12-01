package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
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
public enum RefundStatusEnum {

    /**
     * 待处理
     */
    PENDING(1, "待处理"),

    /**
     * 退款中
     */
    PROCESSING(2, "退款中"),

    /**
     * 退款成功
     */
    SUCCESS(3, "退款成功"),

    /**
     * 退款失败
     */
    FAILED(4, "退款失败"),

    /**
     * 已取消
     */
    CANCELLED(5, "已取消");

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
    public static RefundStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (RefundStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态
     *
     * @return 是否为终态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }

    /**
     * 判断是否可以取消
     *
     * @return 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING || this == PROCESSING;
    }
}