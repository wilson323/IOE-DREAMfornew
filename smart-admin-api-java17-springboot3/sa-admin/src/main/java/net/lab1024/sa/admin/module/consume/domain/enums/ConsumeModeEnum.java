package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费模式枚举
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Getter
@AllArgsConstructor
public enum ConsumeModeEnum {

    /**
     * 固定金额模式
     */
    FIXED_AMOUNT("FIXED_AMOUNT", "固定金额模式"),

    /**
     * 自由金额模式
     */
    FREE_AMOUNT("FREE_AMOUNT", "自由金额模式"),

    /**
     * 商品扫码模式
     */
    PRODUCT("PRODUCT", "商品扫码模式"),

    /**
     * 订餐模式
     */
    ORDERING("ORDERING", "订餐模式"),

    /**
     * 计量模式
     */
    METERING("METERING", "计量模式"),

    /**
     * 智能模式
     */
    SMART("SMART", "智能模式");

    private final String code;
    private final String description;
}