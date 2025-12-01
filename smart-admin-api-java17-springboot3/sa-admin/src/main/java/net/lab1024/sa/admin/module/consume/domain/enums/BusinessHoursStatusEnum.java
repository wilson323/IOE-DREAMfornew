package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

/**
 * 营业时间状态枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义营业时间状态，避免魔法数字
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
public enum BusinessHoursStatusEnum {

    /**
     * 正常营业
     */
    OPEN("OPEN", "正常营业", "在营业时间内，正常提供服务"),

    /**
     * 休息中
     */
    CLOSED("CLOSED", "休息中", "非营业时间，暂停服务"),

    /**
     * 即将营业
     */
    OPENING_SOON("OPENING_SOON", "即将营业", "即将开始营业，正在准备中"),

    /**
     * 即将休息
     */
    CLOSING_SOON("CLOSING_SOON", "即将休息", "即将结束营业，准备打烊"),

    /**
     * 临时关闭
     */
    TEMPORARILY_CLOSED("TEMPORARILY_CLOSED", "临时关闭", "临时暂停营业，会重新开放"),

    /**
     * 维护中
     */
    MAINTENANCE("MAINTENANCE", "维护中", "系统维护或设备维护，暂停服务"),

    /**
     * 节假日休息
     */
    HOLIDAY("HOLIDAY", "节假日休息", "法定节假日或特殊假期休息"),

    /**
     * 全天营业
     */
    TWENTY_FOUR_HOURS("TWENTY_FOUR_HOURS", "全天营业", "24小时不间断营业");

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据代码获取营业时间状态枚举
     *
     * @param code 营业时间状态代码
     * @return 营业时间状态枚举，如果未找到返回null
     */
    public static BusinessHoursStatusEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (BusinessHoursStatusEnum status : values()) {
            if (status.getCode().equals(code.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取营业时间状态枚举
     *
     * @param name 营业时间状态名称
     * @return 营业时间状态枚举，如果未找到返回null
     */
    public static BusinessHoursStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (BusinessHoursStatusEnum status : values()) {
            if (status.getName().equals(name.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据当前时间判断营业状态
     *
     * @param currentTime 当前时间
     * @param openTime 营业开始时间
     * @param closeTime 营业结束时间
     * @return 营业状态
     */
    public static BusinessHoursStatusEnum getCurrentStatus(LocalTime currentTime, LocalTime openTime, LocalTime closeTime) {
        if (openTime == null || closeTime == null) {
            return CLOSED;
        }

        // 处理跨天营业的情况（比如18:00 - 次日06:00）
        if (closeTime.isBefore(openTime)) {
            if (currentTime.isAfter(openTime) || currentTime.isBefore(closeTime)) {
                return OPEN;
            }
        } else {
            // 正常情况营业时间内
            if (currentTime.isAfter(openTime) && currentTime.isBefore(closeTime)) {
                return OPEN;
            }
        }

        return CLOSED;
    }

    /**
     * 判断是否正在营业
     *
     * @return 是否正在营业
     */
    public boolean isOpen() {
        return this == OPEN || this == OPENING_SOON || this == TWENTY_FOUR_HOURS;
    }

    /**
     * 判断是否休息
     *
     * @return 是否休息
     */
    public boolean isClosed() {
        return this == CLOSED || this == CLOSING_SOON || this == HOLIDAY;
    }

    /**
     * 判断是否为临时状态
     *
     * @return 是否为临时状态
     */
    public boolean isTemporaryStatus() {
        return this == TEMPORARILY_CLOSED || this == MAINTENANCE || this == OPENING_SOON || this == CLOSING_SOON;
    }

    /**
     * 判断是否需要预约
     *
     * @return 是否需要预约
     */
    public boolean requiresAppointment() {
        return this == CLOSED || this == TEMPORARILY_CLOSED || this == MAINTENANCE;
    }

    /**
     * 判断是否可以提供紧急服务
     *
     * @return 是否可以提供紧急服务
     */
    public boolean canProvideEmergencyService() {
        return this == TWENTY_FOUR_HOURS || this == OPEN;
    }

    /**
     * 获取状态优先级（数字越大优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case TWENTY_FOUR_HOURS:
                return 7;
            case OPEN:
                return 6;
            case OPENING_SOON:
                return 5;
            case TEMPORARILY_CLOSED:
                return 4;
            case MAINTENANCE:
                return 3;
            case CLOSING_SOON:
                return 2;
            case CLOSED:
                return 1;
            case HOLIDAY:
                return 0;
            default:
                return 0;
        }
    }

    /**
     * 获取颜色代码（用于前端显示）
     *
     * @return 颜色代码
     */
    public String getColorCode() {
        switch (this) {
            case OPEN:
            case TWENTY_FOUR_HOURS:
                return "#52c41a";  // 绿色
            case OPENING_SOON:
                return "#faad14";  // 金色
            case CLOSING_SOON:
                return "#fa8c16";  // 橙色
            case TEMPORARILY_CLOSED:
                return "#fa541c";  // 橙红色
            case MAINTENANCE:
                return "#722ed1";  // 紫色
            case CLOSED:
            case HOLIDAY:
                return "#f5222d";  // 红色
            default:
                return "#d9d9d9";  // 灰色
        }
    }

    @Override
    public String toString() {
        return String.format("BusinessHoursStatusEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}