package net.lab1024.sa.consume.domain.enums;

import lombok.Getter;

/**
 * 退款状态枚举
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Getter
public enum RefundStatusEnum {

    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(1, "处理中"),

    /**
     * 成功
     */
    SUCCESS(2, "成功"),

    /**
     * 失败
     */
    FAILED(3, "失败"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消");

    private final Integer value;
    private final String description;

    RefundStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取枚举
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
     * 是否可以取消
     */
    public boolean canCancel() {
        return this == PENDING || this == PROCESSING;
    }
}