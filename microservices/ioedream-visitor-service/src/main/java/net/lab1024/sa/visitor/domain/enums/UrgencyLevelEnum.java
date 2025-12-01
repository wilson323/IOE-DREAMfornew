package net.lab1024.sa.visitor.domain.enums;

import lombok.Getter;

/**
 * 紧急程度枚举
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Getter
public enum UrgencyLevelEnum {

    /**
     * 一般
     */
    NORMAL(1, "一般"),

    /**
     * 重要
     */
    IMPORTANT(2, "重要"),

    /**
     * 紧急
     */
    URGENT(3, "紧急");

    private final Integer code;
    private final String desc;

    UrgencyLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static UrgencyLevelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UrgencyLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
}