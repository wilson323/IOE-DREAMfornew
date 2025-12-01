package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 充值状态枚举
 *
 * @author SmartAdmin Team
 * @date 2025-11-17
 */
@Getter
@AllArgsConstructor
public enum RechargeStatusEnum {

    /**
     * 待支付
     */
    PENDING(0, "待支付"),

    /**
     * 充值成功
     */
    SUCCESS(1, "充值成功"),

    /**
     * 充值失败
     */
    FAILED(2, "充值失败"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String description;
}