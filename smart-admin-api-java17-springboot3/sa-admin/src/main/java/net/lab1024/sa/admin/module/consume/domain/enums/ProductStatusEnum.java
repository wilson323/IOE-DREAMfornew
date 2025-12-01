package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 产品状态枚举
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
public enum ProductStatusEnum {

    /**
     * 上架
     */
    ON_SALE(1, "上架"),

    /**
     * 下架
     */
    OFF_SALE(2, "下架"),

    /**
     * 停售
     */
    DISCONTINUED(3, "停售");

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
    public static ProductStatusEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ProductStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为可销售状态
     *
     * @return 是否为可销售状态
     */
    public boolean isOnSale() {
        return this == ON_SALE;
    }

    /**
     * 判断是否为不可销售状态
     *
     * @return 是否为不可销售状态
     */
    public boolean isNotOnSale() {
        return this == OFF_SALE || this == DISCONTINUED;
    }

    /**
     * 判断是否可以上架
     *
     * @return 是否可以上架
     */
    public boolean canOnSale() {
        return this == OFF_SALE;
    }
}