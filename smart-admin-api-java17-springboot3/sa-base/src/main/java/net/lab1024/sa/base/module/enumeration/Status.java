package net.lab1024.sa.base.module.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * @Author SmartAdmin Team
 * @Date 2025-11-14
 * @Copyright SmartAdmin v3
 */
@Getter
@AllArgsConstructor
public enum Status {

    /**
     * 启用
     */
    ENABLE(1, "启用"),

    /**
     * 禁用
     */
    DISABLE(0, "禁用");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String desc;
}
