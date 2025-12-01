package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费交易类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义交易类型，避免魔法数字
 * - 提供完整的业务信息和描述
 * - 支持根据代码获取枚举实例
 * - 包含友好的显示名称
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Getter
@AllArgsConstructor
public enum ConsumeTransactionTypeEnum {

    /**
     * 餐饮消费
     */
    DINING("DINING", "餐饮消费", "食堂、餐厅等餐饮场所的消费"),

    /**
     * 购物消费
     */
    SHOPPING("SHOPPING", "购物消费", "超市、商店等购物消费"),

    /**
     * 服务消费
     */
    SERVICE("SERVICE", "服务消费", "各类服务费用的支付"),

    /**
     * 交通消费
     */
    TRANSPORT("TRANSPORT", "交通消费", "停车场、公交卡充值等交通相关消费"),

    /**
     * 娱乐消费
     */
    ENTERTAINMENT("ENTERTAINMENT", "娱乐消费", "健身房、娱乐设施等消费"),

    /**
     * 水电费消费
     */
    UTILITIES("UTILITIES", "水电费消费", "水费、电费、燃气费等公共事业费用"),

    /**
     * 通信消费
     */
    COMMUNICATION("COMMUNICATION", "通信消费", "电话费、网络费等通信服务费用"),

    /**
     * 其他消费
     */
    OTHER("OTHER", "其他消费", "其他未分类的消费类型");

    /**
     * 交易类型代码
     */
    private final String code;

    /**
     * 交易类型名称
     */
    private final String name;

    /**
     * 交易类型描述
     */
    private final String description;

    /**
     * 根据代码获取交易类型枚举
     *
     * @param code 交易类型代码
     * @return 交易类型枚举，如果未找到返回null
     */
    public static ConsumeTransactionTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ConsumeTransactionTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取交易类型枚举
     *
     * @param name 交易类型名称
     * @return 交易类型枚举，如果未找到返回null
     */
    public static ConsumeTransactionTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ConsumeTransactionTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为日常生活消费
     *
     * @return 是否为日常生活消费
     */
    public boolean isDailyLife() {
        return this == DINING || this == SHOPPING || this == UTILITIES || this == TRANSPORT;
    }

    /**
     * 判断是否为可选消费
     *
     * @return 是否为可选消费
     */
    public boolean isOptional() {
        return this == ENTERTAINMENT || this == SERVICE || this == COMMUNICATION || this == OTHER;
    }

    /**
     * 判断是否需要特殊审批
     *
     * @return 是否需要特殊审批
     */
    public boolean requiresSpecialApproval() {
        return this == ENTERTAINMENT;
    }

    /**
     * 获取消费优先级（数字越小优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case DINING:
                return 1;
            case UTILITIES:
                return 2;
            case TRANSPORT:
                return 3;
            case SHOPPING:
                return 4;
            case COMMUNICATION:
                return 5;
            case SERVICE:
                return 6;
            case ENTERTAINMENT:
                return 7;
            case OTHER:
                return 8;
            default:
                return 99;
        }
    }

    @Override
    public String toString() {
        return String.format("ConsumeTransactionTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}