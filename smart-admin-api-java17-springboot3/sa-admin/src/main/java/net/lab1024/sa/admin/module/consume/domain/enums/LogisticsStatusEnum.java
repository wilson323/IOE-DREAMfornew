package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物流状态枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义物流状态，避免魔法数字
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
public enum LogisticsStatusEnum {

    /**
     * 待发货
     */
    PENDING_SHIPMENT("PENDING_SHIPMENT", "待发货", "订单已确认，等待发货"),

    /**
     * 已发货
     */
    SHIPPED("SHIPPED", "已发货", "商品已从仓库发出，正在运输途中"),

    /**
     * 运输中
     */
    IN_TRANSIT("IN_TRANSIT", "运输中", "商品正在配送过程中"),

    /**
     * 派送中
     */
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "派送中", "商品已到达目的地，正在派送给收件人"),

    /**
     * 已送达
     */
    DELIVERED("DELIVERED", "已送达", "商品已成功送达收件人"),

    /**
     * 自提中
     */
    READY_FOR_PICKUP("READY_FOR_PICKUP", "自提中", "商品已到达自提点，等待收件人提取"),

    /**
     * 已自提
     */
    PICKED_UP("PICKED_UP", "已自提", "收件人已成功提取商品"),

    /**
     * 配送失败
     */
    DELIVERY_FAILED("DELIVERY_FAILED", "配送失败", "因各种原因导致配送失败，需要重新安排"),

    /**
     * 退回中
     */
    RETURNING("RETURNING", "退回中", "商品正在退回发件人"),

    /**
     * 已退回
     */
    RETURNED("RETURNED", "已退回", "商品已成功退回发件人");

    /**
     * 物流状态代码
     */
    private final String code;

    /**
     * 物流状态名称
     */
    private final String name;

    /**
     * 物流状态描述
     */
    private final String description;

    /**
     * 根据代码获取物流状态枚举
     *
     * @param code 物流状态代码
     * @return 物流状态枚举，如果未找到返回null
     */
    public static LogisticsStatusEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (LogisticsStatusEnum status : values()) {
            if (status.getCode().equals(code.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取物流状态枚举
     *
     * @param name 物流状态名称
     * @return 物流状态枚举，如果未找到返回null
     */
    public static LogisticsStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (LogisticsStatusEnum status : values()) {
            if (status.getName().equals(name.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为发货前状态
     *
     * @return 是否为发货前状态
     */
    public boolean isPreShipment() {
        return this == PENDING_SHIPMENT;
    }

    /**
     * 判断是否为运输中状态
     *
     * @return 是否为运输中状态
     */
    public boolean isInTransit() {
        return this == SHIPPED || this == IN_TRANSIT || this == OUT_FOR_DELIVERY;
    }

    /**
     * 判断是否为待取货状态
     *
     * @return 是否为待取货状态
     */
    public boolean isAwaitingPickup() {
        return this == READY_FOR_PICKUP;
    }

    /**
     * 判断是否为已完成状态
     *
     * @return 是否为已完成状态
     */
    public boolean isCompleted() {
        return this == DELIVERED || this == PICKED_UP;
    }

    /**
     * 判断是否为异常状态
     *
     * @return 是否为异常状态
     */
    public boolean isAbnormalStatus() {
        return this == DELIVERY_FAILED || this == RETURNING || this == RETURNED;
    }

    /**
     * 判断是否需要收件人操作
     *
     * @return 是否需要收件人操作
     */
    public boolean requiresRecipientAction() {
        return this == READY_FOR_PICKUP || this == OUT_FOR_DELIVERY;
    }

    /**
     * 判断是否可以取消
     *
     * @return 是否可以取消
     */
    public boolean isCancellable() {
        return this == PENDING_SHIPMENT;
    }

    /**
     * 判断是否可以修改地址
     *
     * @return 是否可以修改地址
     */
    public boolean isAddressModifiable() {
        return this == PENDING_SHIPMENT || this == SHIPPED;
    }

    /**
     * 获取状态进度百分比
     *
     * @return 进度百分比
     */
    public int getProgressPercentage() {
        switch (this) {
            case PENDING_SHIPMENT:
                return 0;
            case SHIPPED:
                return 25;
            case IN_TRANSIT:
                return 50;
            case OUT_FOR_DELIVERY:
                return 75;
            case DELIVERED:
            case PICKED_UP:
                return 100;
            case READY_FOR_PICKUP:
                return 80;
            case DELIVERY_FAILED:
            case RETURNING:
                return 60;
            case RETURNED:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 获取预计时间说明
     *
     * @return 时间说明
     */
    public String getEstimatedTimeDescription() {
        switch (this) {
            case PENDING_SHIPMENT:
                return "预计1-2个工作日内发货";
            case SHIPPED:
                return "预计2-3个工作日内送达";
            case IN_TRANSIT:
                return "预计1-2个工作日内送达";
            case OUT_FOR_DELIVERY:
                return "预计今日内送达";
            case DELIVERED:
                return "已成功送达";
            case PICKED_UP:
                return "已成功自提";
            case READY_FOR_PICKUP:
                return "请在3个工作日内提取";
            case DELIVERY_FAILED:
                return "请等待重新安排配送";
            case RETURNING:
                return "预计3-5个工作日内退回";
            case RETURNED:
                return "已退回发件人";
            default:
                return "处理中";
        }
    }

    /**
     * 获取状态颜色代码（用于前端显示）
     *
     * @return 颜色代码
     */
    public String getColorCode() {
        switch (this) {
            case PENDING_SHIPMENT:
                return "#d9d9d9";  // 灰色
            case SHIPPED:
                return "#1890ff";  // 蓝色
            case IN_TRANSIT:
                return "#52c41a";  // 绿色
            case OUT_FOR_DELIVERY:
                return "#faad14";  // 金色
            case DELIVERED:
            case PICKED_UP:
                return "#52c41a";  // 绿色
            case READY_FOR_PICKUP:
                return "#722ed1";  // 紫色
            case DELIVERY_FAILED:
            case RETURNING:
                return "#fa541c";  // 橙红色
            case RETURNED:
                return "#f5222d";  // 红色
            default:
                return "#d9d9d9";  // 灰色
        }
    }

    @Override
    public String toString() {
        return String.format("LogisticsStatusEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}