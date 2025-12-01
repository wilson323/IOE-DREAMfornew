package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商品属性类型枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义商品属性类型，避免魔法数字
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
public enum ProductAttributeTypeEnum {

    /**
     * 基本属性
     */
    BASIC("BASIC", "基本属性", "商品的基本信息属性，如名称、描述、条码等"),

    /**
     * 规格属性
     */
    SPECIFICATION("SPECIFICATION", "规格属性", "商品的规格参数，如尺寸、重量、容量等"),

    /**
     * 价格属性
     */
    PRICE("PRICE", "价格属性", "商品的价格相关信息，如售价、进价、折扣价等"),

    /**
     * 库存属性
     */
    INVENTORY("INVENTORY", "库存属性", "商品的库存管理属性，如库存量、安全库存、补货点等"),

    /**
     * 分类属性
     */
    CATEGORY("CATEGORY", "分类属性", "商品的分类归属属性，如大类、小类、品牌等"),

    /**
     * 质量属性
     */
    QUALITY("QUALITY", "质量属性", "商品的质量相关属性，如保质期、生产日期、检验标准等"),

    /**
     * 销售属性
     */
    SALES("SALES", "销售属性", "商品的销售相关属性，如销量、评分、推荐度等"),

    /**
     * 物理属性
     */
    PHYSICAL("PHYSICAL", "物理属性", "商品的物理特征，如颜色、材质、包装等");

    /**
     * 属性类型代码
     */
    private final String code;

    /**
     * 属性类型名称
     */
    private final String name;

    /**
     * 属性类型描述
     */
    private final String description;

    /**
     * 根据代码获取商品属性类型枚举
     *
     * @param code 商品属性类型代码
     * @return 商品属性类型枚举，如果未找到返回null
     */
    public static ProductAttributeTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (ProductAttributeTypeEnum type : values()) {
            if (type.getCode().equals(code.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据名称获取商品属性类型枚举
     *
     * @param name 商品属性类型名称
     * @return 商品属性类型枚举，如果未找到返回null
     */
    public static ProductAttributeTypeEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ProductAttributeTypeEnum type : values()) {
            if (type.getName().equals(name.trim())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为核心属性
     *
     * @return 是否为核心属性
     */
    public boolean isCoreAttribute() {
        return this == BASIC || this == SPECIFICATION || this == PRICE;
    }

    /**
     * 判断是否为管理属性
     *
     * @return 是否为管理属性
     */
    public boolean isManagementAttribute() {
        return this == INVENTORY || this == CATEGORY || this == SALES;
    }

    /**
     * 判断是否为可变属性
     *
     * @return 是否为可变属性
     */
    public boolean isVariableAttribute() {
        return this == PRICE || this == INVENTORY || this == SALES || this == QUALITY;
    }

    /**
     * 判断是否为静态属性
     *
     * @return 是否为静态属性
     */
    public boolean isStaticAttribute() {
        return this == BASIC || this == SPECIFICATION || this == CATEGORY || this == PHYSICAL;
    }

    /**
     * 判断是否需要定期更新
     *
     * @return 是否需要定期更新
     */
    public boolean requiresPeriodicUpdate() {
        return this == INVENTORY || this == PRICE || this == QUALITY || this == SALES;
    }

    /**
     * 获取数据类型建议
     *
     * @return 推荐的数据类型
     */
    public String getRecommendedDataType() {
        switch (this) {
            case BASIC:
                return "String";
            case SPECIFICATION:
                return "String|Number";
            case PRICE:
                return "BigDecimal";
            case INVENTORY:
                return "Integer";
            case CATEGORY:
                return "String|Long";
            case QUALITY:
                return "String|Date";
            case SALES:
                return "Integer|BigDecimal";
            case PHYSICAL:
                return "String";
            default:
                return "String";
        }
    }

    /**
     * 获取优先级（数字越小优先级越高）
     *
     * @return 优先级数值
     */
    public int getPriority() {
        switch (this) {
            case BASIC:
                return 1;
            case SPECIFICATION:
                return 2;
            case PRICE:
                return 3;
            case CATEGORY:
                return 4;
            case INVENTORY:
                return 5;
            case QUALITY:
                return 6;
            case PHYSICAL:
                return 7;
            case SALES:
                return 8;
            default:
                return 99;
        }
    }

    @Override
    public String toString() {
        return String.format("ProductAttributeTypeEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}