package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消费设备类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义设备类型，避免魔法数字
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
public enum ConsumeDeviceTypeEnum {

    /**
     * POS消费机
     */
    POS("POS", "POS消费机", "传统的POS机消费终端，适用于超市、食堂等场景"),

    /**
     * 自助消费机
     */
    SELF_SERVICE("SELF_SERVICE", "自助消费机", "无人值守的自助消费终端，支持扫码、刷卡等多种支付方式"),

    /**
     * 食堂消费机
     */
    CANTEEN("CANTEEN", "食堂消费机", "专门用于食堂餐厅的消费终端，支持套餐、单品计费"),

    /**
     * 移动消费终端
     */
    MOBILE("MOBILE", "移动消费终端", "手持式移动消费设备，适用于流动摊位、临时场所"),

    /**
     * 智能售货机
     */
    VENDING_MACHINE("VENDING_MACHINE", "智能售货机", "自动售货机，支持多种商品销售和移动支付"),

    /**
     * 扫码支付终端
     */
    QR_TERMINAL("QR_TERMINAL", "扫码支付终端", "专门用于二维码扫描支付的终端设备"),

    /**
     * 人脸识别消费终端
     */
    FACE_RECOGNITION("FACE_RECOGNITION", "人脸识别消费终端", "支持人脸识别的消费终端，无接触支付"),

    /**
     * 指纹消费终端
     */
    FINGERPRINT("FINGERPRINT", "指纹消费终端", "支持指纹识别的消费终端，高安全性");

    /**
     * 设备类型代码
     */
    private final String code;

    /**
     * 设备类型名称
     */
    private final String name;

    /**
     * 设备类型描述
     */
    private final String description;

    /**
     * 根据代码获取设备类型枚举
     *
     * @param code 设备类型代码
     * @return 设备类型枚举，如果未找到返回null
     */
    public static ConsumeDeviceTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ConsumeDeviceTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取设备类型枚举
     *
     * @param name 设备类型名称
     * @return 设备类型枚举，如果未找到返回null
     */
    public static ConsumeDeviceTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ConsumeDeviceTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为固定设备
     *
     * @return 是否为固定设备
     */
    public boolean isFixedDevice() {
        return this == POS || this == CANTEEN || this == VENDING_MACHINE || this == QR_TERMINAL;
    }

    /**
     * 判断是否为移动设备
     *
     * @return 是否为移动设备
     */
    public boolean isMobileDevice() {
        return this == MOBILE;
    }

    /**
     * 判断是否为自助设备
     *
     * @return 是否为自助设备
     */
    public boolean isSelfService() {
        return this == SELF_SERVICE || this == VENDING_MACHINE || this == QR_TERMINAL;
    }

    /**
     * 判断是否支持生物识别
     *
     * @return 是否支持生物识别
     */
    public boolean supportsBiometric() {
        return this == FACE_RECOGNITION || this == FINGERPRINT;
    }

    /**
     * 判断是否支持移动支付
     *
     * @return 是否支持移动支付
     */
    public boolean supportsMobilePayment() {
        return this != POS && this != FINGERPRINT;
    }

    /**
     * 获取设备优先级（数字越小优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case FACE_RECOGNITION:
                return 1;
            case FINGERPRINT:
                return 2;
            case QR_TERMINAL:
                return 3;
            case SELF_SERVICE:
                return 4;
            case MOBILE:
                return 5;
            case VENDING_MACHINE:
                return 6;
            case CANTEEN:
                return 7;
            case POS:
                return 8;
            default:
                return 99;
        }
    }

    @Override
    public String toString() {
        return String.format("ConsumeDeviceTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}