package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订餐状态枚举
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Getter
@AllArgsConstructor
public enum OrderingStatusEnum {

    /**
     * 已确认
     */
    CONFIRMED("CONFIRMED", "已确认"),

    /**
     * 准备中
     */
    PREPARING("PREPARING", "准备中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}