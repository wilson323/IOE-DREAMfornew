package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计量单位枚举
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Getter
@AllArgsConstructor
public enum MeteringUnitEnum {

    /**
     * 千瓦时
     */
    KWH("kwh", "千瓦时"),

    /**
     * 立方米
     */
    M3("m3", "立方米"),

    /**
     * 千克
     */
    KG("kg", "千克"),

    /**
     * 升
     */
    L("l", "升"),

    /**
     * 米
     */
    M("m", "米"),

    /**
     * 吨
     */
    TON("ton", "吨"),

    /**
     * 平方米
     */
    M2("m2", "平方米"),

    /**
     * 立方米每秒
     */
    M3_S("m3/s", "立方米每秒");

    private final String code;
    private final String description;
}