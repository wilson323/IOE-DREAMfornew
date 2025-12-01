package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单优先级枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义订单优先级，避免魔法数字
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
public enum OrderPriorityEnum {

    /**
     * 低优先级
     */
    LOW(1, "低优先级", "普通订单，正常处理即可"),

    /**
     * 普通优先级
     */
    NORMAL(2, "普通优先级", "标准订单，按正常流程处理"),

    /**
     * 中等优先级
     */
    MEDIUM(3, "中等优先级", "重要订单，需要适当优先处理"),

    /**
     * 高优先级
     */
    HIGH(4, "高优先级", "紧急订单，需要优先处理"),

    /**
     * 紧急优先级
     */
    URGENT(5, "紧急优先级", "非常紧急的订单，需要立即处理"),

    /**
     * VIP优先级
     */
    VIP(6, "VIP优先级", "VIP客户订单，享有最高优先级");

    /**
     * 优先级代码
     */
    private final Integer code;

    /**
     * 优先级名称
     */
    private final String name;

    /**
     * 优先级描述
     */
    private final String description;

    /**
     * 根据代码获取订单优先级枚举
     *
     * @param code 优先级代码
     * @return 订单优先级枚举，如果未找到返回null
     */
    public static OrderPriorityEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OrderPriorityEnum priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return null;
    }

    /**
     * 根据名称获取订单优先级枚举
     *
     * @param name 优先级名称
     * @return 订单优先级枚举，如果未找到返回null
     */
    public static OrderPriorityEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (OrderPriorityEnum priority : values()) {
            if (priority.getName().equals(name.trim())) {
                return priority;
            }
        }
        return null;
    }

    /**
     * 判断是否为高优先级订单
     *
     * @return 是否为高优先级订单
     */
    public boolean isHighPriority() {
        return this == HIGH || this == URGENT || this == VIP;
    }

    /**
     * 判断是否为普通优先级订单
     *
     * @return 是否为普通优先级订单
     */
    public boolean isNormalPriority() {
        return this == LOW || this == NORMAL || this == MEDIUM;
    }

    /**
     * 判断是否需要加急处理
     *
     * @return 是否需要加急处理
     */
    public boolean requiresExpeditedProcessing() {
        return this == URGENT || this == VIP;
    }

    /**
     * 判断是否需要特殊通知
     *
     * @return 是否需要特殊通知
     */
    public boolean requiresSpecialNotification() {
        return this == HIGH || this == URGENT || this == VIP;
    }

    /**
     * 获取处理时间限制（分钟）
     *
     * @return 处理时间限制
     */
    public int getProcessingTimeLimit() {
        switch (this) {
            case VIP:
                return 5;    // 5分钟内处理
            case URGENT:
                return 10;   // 10分钟内处理
            case HIGH:
                return 30;   // 30分钟内处理
            case MEDIUM:
                return 60;   // 1小时内处理
            case NORMAL:
                return 120;  // 2小时内处理
            case LOW:
                return 240;  // 4小时内处理
            default:
                return 120;
        }
    }

    /**
     * 获取颜色代码（用于前端显示）
     *
     * @return 颜色代码
     */
    public String getColorCode() {
        switch (this) {
            case VIP:
                return "#d4380d";  // 红色
            case URGENT:
                return "#fa541c";  // 橙红色
            case HIGH:
                return "#fa8c16";  // 橙色
            case MEDIUM:
                return "#fadb14";  // 黄色
            case NORMAL:
                return "#52c41a";  // 绿色
            case LOW:
                return "#1890ff";  // 蓝色
            default:
                return "#d9d9d9";  // 灰色
        }
    }

    @Override
    public String toString() {
        return String.format("OrderPriorityEnum{code=%d, name='%s', description='%s'}",
                code, name, description);
    }
}