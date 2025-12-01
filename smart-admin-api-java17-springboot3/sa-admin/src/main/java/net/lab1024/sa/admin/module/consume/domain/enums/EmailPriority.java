package net.lab1024.sa.admin.module.consume.domain.enums;

/**
 * 邮件优先级枚举
 * 严格遵循repowiki规范：枚举类独立文件
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public enum EmailPriority {
    LOW(1), // 低优先级
    NORMAL(3), // 普通优先级
    HIGH(5), // 高优先级
    URGENT(9); // 紧急

    private final int value;

    EmailPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
