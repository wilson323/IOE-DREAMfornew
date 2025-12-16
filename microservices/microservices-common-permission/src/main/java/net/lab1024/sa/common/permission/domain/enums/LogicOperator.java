package net.lab1024.sa.common.permission.domain.enums;

/**
 * 逻辑操作符
 * <p>
 * 用于复合权限验证的逻辑操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public enum LogicOperator {

    /**
     * 逻辑与操作
     * <p>
     * 所有条件都必须满足才算通过
     * </p>
     */
    AND("AND", "逻辑与", "所有条件都必须满足"),

    /**
     * 逻辑或操作
     * <p>
     * 任一条件满足就算通过
     * </p>
     */
    OR("OR", "逻辑或", "任一条件满足就算通过"),

    /**
     * 逻辑非操作
     * <p>
     * 条件不满足才算通过
     * </p>
     */
    NOT("NOT", "逻辑非", "条件不满足才算通过"),

    /**
     * 异或操作
     * <p>
     * 有且仅有一个条件满足才算通过
     * </p>
     */
    XOR("XOR", "异或", "有且仅有一个条件满足就算通过"),

    /**
     * 多数条件满足
     * <p>
     * 超过一半的条件满足就算通过
     * </p>
     */
    MAJORITY("MAJORITY", "多数满足", "超过一半的条件满足就算通过"),

    /**
     * 指定数量满足
     * <p>
     * 需要指定具体满足的数量
     * </p>
     */
    COUNT("COUNT", "指定数量", "需要指定具体满足的数量");

    private final String code;
    private final String name;
    private final String description;

    LogicOperator(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取枚举
     */
    public static LogicOperator fromCode(String code) {
        for (LogicOperator operator : values()) {
            if (operator.getCode().equals(code)) {
                return operator;
            }
        }
        throw new IllegalArgumentException("未知的逻辑操作符代码: " + code);
    }

    /**
     * 判断是否为AND操作
     */
    public boolean isAnd() {
        return this == AND;
    }

    /**
     * 判断是否为OR操作
     */
    public boolean isOr() {
        return this == OR;
    }

    /**
     * 判断是否为NOT操作
     */
    public boolean isNot() {
        return this == NOT;
    }

    /**
     * 判断是否为XOR操作
     */
    public boolean isXor() {
        return this == XOR;
    }

    /**
     * 判断是否为MAJORITY操作
     */
    public boolean isMajority() {
        return this == MAJORITY;
    }

    /**
     * 判断是否为COUNT操作
     */
    public boolean isCount() {
        return this == COUNT;
    }

    /**
     * 计算满足条件的数量阈值
     */
    public int getThreshold(int totalConditions) {
        return switch (this) {
            case AND -> totalConditions;
            case OR -> 1;
            case XOR -> 1;
            case MAJORITY -> (totalConditions / 2) + 1;
            case COUNT -> -1; // 需要外部指定
            case NOT -> 0; // NOT操作不适用
        };
    }

    /**
     * 验证操作参数
     */
    public boolean isValidParameters(int totalConditions, int requiredCount) {
        return switch (this) {
            case AND, OR, NOT, XOR, MAJORITY -> totalConditions > 0;
            case COUNT -> totalConditions > 0 && requiredCount > 0 && requiredCount <= totalConditions;
        };
    }
}