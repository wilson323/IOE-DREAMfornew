package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费设备状态枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义设备状态，避免魔法数字
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
public enum ConsumeDeviceStatusEnum {

    /**
     * 正常运行
     */
    NORMAL(1, "正常运行", "设备正常工作，可以进行消费交易"),

    /**
     * 离线状态
     */
    OFFLINE(2, "离线状态", "设备与服务器断开连接，无法进行交易"),

    /**
     * 故障状态
     */
    FAULT(3, "故障状态", "设备发生故障，需要维修"),

    /**
     * 维护中
     */
    MAINTENANCE(4, "维护中", "设备正在进行维护，暂时无法使用"),

    /**
     * 已停用
     */
    DISABLED(5, "已停用", "设备已被管理员停用，不能使用"),

    /**
     * 待激活
     */
    PENDING_ACTIVATION(6, "待激活", "新设备已添加但未激活，需要激活后才能使用"),

    /**
     * 网络异常
     */
    NETWORK_ERROR(7, "网络异常", "设备网络连接异常，可能影响交易"),

    /**
     * 缺纸/缺墨
     */
    SUPPLY_LOW(8, "缺纸/缺墨", "设备耗材不足，影响打印功能");

    /**
     * 状态代码
     */
    private final Integer code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据代码获取设备状态枚举
     *
     * @param code 状态代码
     * @return 设备状态枚举，如果未找到返回null
     */
    public static ConsumeDeviceStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ConsumeDeviceStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取设备状态枚举
     *
     * @param name 状态名称
     * @return 设备状态枚举，如果未找到返回null
     */
    public static ConsumeDeviceStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ConsumeDeviceStatusEnum status : values()) {
            if (status.getName().equals(name.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断设备是否可用（可以进行交易）
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return this == NORMAL;
    }

    /**
     * 判断设备是否不可用（不能进行交易）
     *
     * @return 是否不可用
     */
    public boolean isUnavailable() {
        return this == FAULT || this == MAINTENANCE || this == DISABLED || this == SUPPLY_LOW;
    }

    /**
     * 判断设备是否在线
     *
     * @return 是否在线
     */
    public boolean isOnline() {
        return this == NORMAL || this == FAULT || this == MAINTENANCE || this == NETWORK_ERROR || this == SUPPLY_LOW;
    }

    /**
     * 判断设备是否离线
     *
     * @return 是否离线
     */
    public boolean isOffline() {
        return this == OFFLINE;
    }

    /**
     * 判断是否需要管理员关注
     *
     * @return 是否需要关注
     */
    public boolean needsAttention() {
        return this == FAULT || this == NETWORK_ERROR || this == SUPPLY_LOW || this == OFFLINE;
    }

    /**
     * 判断是否为临时状态
     *
     * @return 是否为临时状态
     */
    public boolean isTemporaryStatus() {
        return this == MAINTENANCE || this == NETWORK_ERROR || this == SUPPLY_LOW || this == PENDING_ACTIVATION;
    }

    /**
     * 判断是否为永久状态
     *
     * @return 是否为永久状态
     */
    public boolean isPermanentStatus() {
        return this == DISABLED;
    }

    /**
     * 获取状态严重程度（数字越大越严重）
     *
     * @return 严重程度数值
     */
    public int getSeverityLevel() {
        switch (this) {
            case NORMAL:
                return 0;
            case PENDING_ACTIVATION:
                return 1;
            case MAINTENANCE:
                return 2;
            case SUPPLY_LOW:
                return 3;
            case NETWORK_ERROR:
                return 4;
            case OFFLINE:
                return 5;
            case FAULT:
                return 6;
            case DISABLED:
                return 7;
            default:
                return 99;
        }
    }

    /**
     * 获取建议的解决方案
     *
     * @return 建议解决方案
     */
    public String getRecommendedAction() {
        switch (this) {
            case NORMAL:
                return "设备运行正常，无需操作";
            case OFFLINE:
                return "检查网络连接和设备电源";
            case FAULT:
                return "联系技术人员进行维修";
            case MAINTENANCE:
                return "等待维护完成";
            case DISABLED:
                return "如需使用，请联系管理员启用";
            case PENDING_ACTIVATION:
                return "完成设备激活流程";
            case NETWORK_ERROR:
                return "检查网络配置和连接";
            case SUPPLY_LOW:
                return "及时补充打印纸或墨盒";
            default:
                return "联系管理员处理";
        }
    }

    @Override
    public String toString() {
        return String.format("ConsumeDeviceStatusEnum{code=%d, name='%s', description='%s'}",
                code, name, description);
    }
}