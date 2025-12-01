package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发票类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义发票类型，避免魔法数字
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
public enum InvoiceTypeEnum {

    /**
     * 增值税普通发票
     */
    VAT_NORMAL("VAT_NORMAL", "增值税普通发票", "增值税普通发票，适用于一般纳税人和小规模纳税人"),

    /**
     * 增值税专用发票
     */
    VAT_SPECIAL("VAT_SPECIAL", "增值税专用发票", "增值税专用发票，仅适用于一般纳税人"),

    /**
     * 电子发票
     */
    ELECTRONIC("ELECTRONIC", "电子发票", "电子形式的发票，具有法律效力"),

    /**
     * 收据
     */
    RECEIPT("RECEIPT", "收据", "简单的收款凭证，不具备发票法律效力"),

    /**
     * 机动车销售发票
     */
    VEHICLE("VEHICLE", "机动车销售发票", "机动车销售统一发票"),

    /**
     * 不动产发票
     */
    REAL_ESTATE("REAL_ESTATE", "不动产发票", "不动产销售统一发票"),

    /**
     * 海关进口发票
     */
    CUSTOMS("CUSTOMS", "海关进口发票", "海关进口增值税专用缴款书"),

    /**
     * 其他发票
     */
    OTHER("OTHER", "其他发票", "其他类型的发票或凭证");

    /**
     * 发票类型代码
     */
    private final String code;

    /**
     * 发票类型名称
     */
    private final String name;

    /**
     * 发票类型描述
     */
    private final String description;

    /**
     * 根据代码获取发票类型枚举
     *
     * @param code 发票类型代码
     * @return 发票类型枚举，如果未找到返回null
     */
    public static InvoiceTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (InvoiceTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取发票类型枚举
     *
     * @param name 发票类型名称
     * @return 发票类型枚举，如果未找到返回null
     */
    public static InvoiceTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (InvoiceTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为增值税发票
     *
     * @return 是否为增值税发票
     */
    public boolean isVatInvoice() {
        return this == VAT_NORMAL || this == VAT_SPECIAL || this == ELECTRONIC;
    }

    /**
     * 判断是否为专用发票
     *
     * @return 是否为专用发票
     */
    public boolean isSpecialInvoice() {
        return this == VAT_SPECIAL;
    }

    /**
     * 判断是否为电子发票
     *
     * @return 是否为电子发票
     */
    public boolean isElectronicInvoice() {
        return this == ELECTRONIC;
    }

    /**
     * 判断是否为特殊行业发票
     *
     * @return 是否为特殊行业发票
     */
    public boolean isSpecialIndustryInvoice() {
        return this == VEHICLE || this == REAL_ESTATE || this == CUSTOMS;
    }

    /**
     * 判断是否可用于税前扣除
     *
     * @return 是否可用于税前扣除
     */
    public boolean isTaxDeductible() {
        return this == VAT_SPECIAL || this == VAT_NORMAL || this == ELECTRONIC || this == VEHICLE || this == REAL_ESTATE;
    }

    /**
     * 判断是否需要开票资质
     *
     * @return 是否需要开票资质
     */
    public boolean requiresQualification() {
        return this == VAT_SPECIAL || this == VEHICLE || this == REAL_ESTATE || this == CUSTOMS;
    }

    /**
     * 获取发票税率范围
     *
     * @return 税率范围描述
     */
    public String getTaxRateRange() {
        switch (this) {
            case VAT_SPECIAL:
                return "6%、9%、13%";
            case VAT_NORMAL:
                return "3%、6%、9%、13%";
            case ELECTRONIC:
                return "3%、6%、9%、13%";
            case VEHICLE:
                return "13%、16%";
            case REAL_ESTATE:
                return "5%、9%、11%";
            case CUSTOMS:
                return "根据进口税率确定";
            default:
                return "无固定税率";
        }
    }

    /**
     * 获取开具发票所需信息
     *
     * @return 所需信息列表
     */
    public String getRequiredInformation() {
        switch (this) {
            case VAT_SPECIAL:
                return "购买方名称、纳税人识别号、地址电话、开户行及账号";
            case VAT_NORMAL:
                return "购买方名称、纳税人识别号（选填）";
            case ELECTRONIC:
                return "购买方名称、纳税人识别号（选填）、手机号码、邮箱";
            case VEHICLE:
                return "购买方信息、车辆识别代码、合格证号";
            case REAL_ESTATE:
                return "购买方信息、不动产地址、面积、单价";
            default:
                return "基本交易信息";
        }
    }

    /**
     * 获取发票类型优先级（数字越大优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case VAT_SPECIAL:
                return 8;
            case ELECTRONIC:
                return 7;
            case VAT_NORMAL:
                return 6;
            case VEHICLE:
                return 5;
            case REAL_ESTATE:
                return 4;
            case CUSTOMS:
                return 3;
            case RECEIPT:
                return 2;
            case OTHER:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("InvoiceTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}