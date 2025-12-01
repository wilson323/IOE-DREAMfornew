package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存状态枚举
 *
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举定义库存状态，避免魔法数字
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
public enum InventoryStatusEnum {

    /**
     * 正常库存
     */
    NORMAL("NORMAL", "正常库存", "库存充足，正常销售"),

    /**
     * 库存不足
     */
    LOW_STOCK("LOW_STOCK", "库存不足", "库存数量较少，需要及时补货"),

    /**
     * 缺货
     */
    OUT_OF_STOCK("OUT_OF_STOCK", "缺货", "库存为零，暂时无法销售"),

    /**
     * 预售中
     */
    PRE_SALE("PRE_SALE", "预售中", "商品可以预订，暂时缺货"),

    /**
     * 停售
     */
    STOP_SALE("STOP_SALE", "停售", "暂停销售，可能存在质量问题"),

    /**
     * 清仓
     */
    CLEARANCE("CLEARANCE", "清仓", "清仓处理，特价销售"),

    /**
     * 季节性下架
     */
    SEASONAL_OFF("SEASONAL_OFF", "季节性下架", "季节性商品暂时下架"),

    /**
     * 滞销品
     */
    SLOW_MOVING("SLOW_MOVING", "滞销品", "销售缓慢，需要促销");

    /**
     * 库存状态代码
     */
    private final String code;

    /**
     * 库存状态名称
     */
    private final String name;

    /**
     * 库存状态描述
     */
    private final String description;

    /**
     * 根据代码获取库存状态枚举
     *
     * @param code 库存状态代码
     * @return 库存状态枚举，如果未找到返回null
     */
    public static InventoryStatusEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        for (InventoryStatusEnum status : values()) {
            if (status.getCode().equals(code.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据名称获取库存状态枚举
     *
     * @param name 库存状态名称
     * @return 库存状态枚举，如果未找到返回null
     */
    public static InventoryStatusEnum getByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (InventoryStatusEnum status : values()) {
            if (status.getName().equals(name.trim())) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否可以销售
     *
     * @return 是否可以销售
     */
    public boolean isSaleable() {
        return this == NORMAL || this == LOW_STOCK || this == PRE_SALE || this == CLEARANCE;
    }

    /**
     * 判断是否不可销售
     *
     * @return 是否不可销售
     */
    public boolean isNotSaleable() {
        return this == OUT_OF_STOCK || this == STOP_SALE || this == SEASONAL_OFF;
    }

    /**
     * 判断是否需要补货
     *
     * @return 是否需要补货
     */
    public boolean needsRestock() {
        return this == LOW_STOCK || this == OUT_OF_STOCK || this == PRE_SALE;
    }

    /**
     * 判断是否为特殊状态
     *
     * @return 是否为特殊状态
     */
    public boolean isSpecialStatus() {
        return this == PRE_SALE || this == CLEARANCE || this == SEASONAL_OFF || this == SLOW_MOVING;
    }

    /**
     * 判断是否需要管理员关注
     *
     * @return 是否需要管理员关注
     */
    public boolean needsAttention() {
        return this == LOW_STOCK || this == OUT_OF_STOCK || this == SLOW_MOVING || this == STOP_SALE;
    }

    /**
     * 获取库存状态紧急程度（数字越大越紧急）
     *
     * @return 紧急程度数值
     */
    public int getUrgencyLevel() {
        switch (this) {
            case OUT_OF_STOCK:
                return 8;
            case STOP_SALE:
                return 7;
            case LOW_STOCK:
                return 6;
            case SLOW_MOVING:
                return 5;
            case PRE_SALE:
                return 4;
            case CLEARANCE:
                return 3;
            case SEASONAL_OFF:
                return 2;
            case NORMAL:
                return 1;
            default:
                return 0;
        }
    }

    /**
     * 获取建议的处理措施
     *
     * @return 建议处理措施
     */
    public String getRecommendedAction() {
        switch (this) {
            case NORMAL:
                return "正常库存监控";
            case LOW_STOCK:
                return "及时补货，避免缺货";
            case OUT_OF_STOCK:
                return "立即补货或设置预售";
            case PRE_SALE:
                return "尽快到货，满足预订需求";
            case STOP_SALE:
                return "查明原因，解决问题后恢复销售";
            case CLEARANCE:
                return "加大促销力度，快速清仓";
            case SEASONAL_OFF:
                return "等待合适季节重新上架";
            case SLOW_MOVING:
                return "制定促销方案，提高销量";
            default:
                return "联系管理员处理";
        }
    }

    @Override
    public String toString() {
        return String.format("InventoryStatusEnum{code='%s', name='%s', description='%s'}",
                code, name, description);
    }
}