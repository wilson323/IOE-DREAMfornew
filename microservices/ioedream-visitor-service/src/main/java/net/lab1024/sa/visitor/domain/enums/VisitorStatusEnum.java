package net.lab1024.sa.visitor.domain.enums;

import lombok.Getter;

/**
 * 访客状态枚举
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Getter
public enum VisitorStatusEnum {

    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 已通过
     */
    APPROVED(1, "已通过"),

    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝"),

    /**
     * 已撤销
     */
    CANCELLED(3, "已撤销"),

    /**
     * 已过期
     */
    EXPIRED(4, "已过期"),

    /**
     * 访问中
     */
    VISITING(5, "访问中"),

    /**
     * 已完成
     */
    COMPLETED(6, "已完成");

    private final Integer code;
    private final String desc;

    VisitorStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static VisitorStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (VisitorStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}