package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义通知类型，避免魔法数字
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
public enum NotificationTypeEnum {

    /**
     * 消费提醒
     */
    CONSUMPTION_REMINDER("CONSUMPTION_REMINDER", "消费提醒", "消费成功后的通知提醒"),

    /**
     * 余额不足
     */
    LOW_BALANCE("LOW_BALANCE", "余额不足", "账户余额不足时的提醒"),

    /**
     * 充值成功
     */
    RECHARGE_SUCCESS("RECHARGE_SUCCESS", "充值成功", "账户充值成功的通知"),

    /**
     * 退款通知
     */
    REFUND_NOTICE("REFUND_NOTICE", "退款通知", "退款处理完成的通知"),

    /**
     * 促销活动
     */
    PROMOTION("PROMOTION", "促销活动", "各类促销活动和优惠信息"),

    /**
     * 系统维护
     */
    SYSTEM_MAINTENANCE("SYSTEM_MAINTENANCE", "系统维护", "系统维护升级的通知"),

    /**
     * 安全警告
     */
    SECURITY_ALERT("SECURITY_ALERT", "安全警告", "账户安全相关的警告信息"),

    /**
     * 订单状态
     */
    ORDER_STATUS("ORDER_STATUS", "订单状态", "订单状态变更的通知"),

    /**
     * 会员权益
     */
    MEMBER_BENEFITS("MEMBER_BENEFITS", "会员权益", "会员权益和优惠信息"),

    /**
     * 设备故障
     */
    DEVICE_FAULT("DEVICE_FAULT", "设备故障", "消费设备故障的提醒");

    /**
     * 通知类型代码
     */
    private final String code;

    /**
     * 通知类型名称
     */
    private final String name;

    /**
     * 通知类型描述
     */
    private final String description;

    /**
     * 根据代码获取通知类型枚举
     *
     * @param code 通知类型代码
     * @return 通知类型枚举，如果未找到返回null
     */
    public static NotificationTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (NotificationTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取通知类型枚举
     *
     * @param name 通知类型名称
     * @return 通知类型枚举，如果未找到返回null
     */
    public static NotificationTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (NotificationTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为交易相关通知
     *
     * @return 是否为交易相关通知
     */
    public boolean isTransactionRelated() {
        return this == CONSUMPTION_REMINDER || this == RECHARGE_SUCCESS || this == REFUND_NOTICE || this == ORDER_STATUS;
    }

    /**
     * 判断是否为系统相关通知
     *
     * @return 是否为系统相关通知
     */
    public boolean isSystemRelated() {
        return this == SYSTEM_MAINTENANCE || this == SECURITY_ALERT || this == DEVICE_FAULT;
    }

    /**
     * 判断是否为营销相关通知
     *
     * @return 是否为营销相关通知
     */
    public boolean isMarketingRelated() {
        return this == PROMOTION || this == MEMBER_BENEFITS;
    }

    /**
     * 判断是否为紧急通知
     *
     * @return 是否为紧急通知
     */
    public boolean isUrgent() {
        return this == SECURITY_ALERT || this == DEVICE_FAULT || this == LOW_BALANCE;
    }

    /**
     * 判断是否需要立即处理
     *
     * @return 是否需要立即处理
     */
    public boolean requiresImmediateAction() {
        return this == SECURITY_ALERT || this == DEVICE_FAULT;
    }

    /**
     * 获取通知优先级（数字越大优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case SECURITY_ALERT:
                return 9;
            case DEVICE_FAULT:
                return 8;
            case LOW_BALANCE:
                return 7;
            case RECHARGE_SUCCESS:
                return 6;
            case REFUND_NOTICE:
                return 5;
            case CONSUMPTION_REMINDER:
                return 4;
            case ORDER_STATUS:
                return 3;
            case SYSTEM_MAINTENANCE:
                return 2;
            case PROMOTION:
                return 1;
            case MEMBER_BENEFITS:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 获取推荐的推送渠道
     *
     * @return 推荐的推送渠道
     */
    public String getRecommendedChannel() {
        switch (this) {
            case SECURITY_ALERT:
            case DEVICE_FAULT:
                return "SMS,APP_PUSH,EMAIL";  // 多渠道推送
            case LOW_BALANCE:
                return "SMS,APP_PUSH";
            case RECHARGE_SUCCESS:
            case REFUND_NOTICE:
                return "APP_PUSH,EMAIL";
            case PROMOTION:
                return "EMAIL,APP_PUSH";
            default:
                return "APP_PUSH";
        }
    }

    @Override
    public String toString() {
        return String.format("NotificationTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}