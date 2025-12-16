package net.lab1024.sa.common.consume.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 商品实体类
 * <p>
 * 用于管理商品信息，支持超市制区域的商品消费
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 商品资料管理
 * - 商品价格策略
 * - 商品库存管理
 * - 商品条码管理
 * </p>
 * <p>
 * 数据库表：POSID_PRODUCT（业务文档中定义的表名）
 * 注意：根据CLAUDE.md规范，表名应使用t_consume_*格式，但业务文档中使用POSID_*格式
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_product")
public class ConsumeProductEntity extends BaseEntity {

    /**
     * 商品ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 商品编号（唯一）
     */
    private String code;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品分类ID
     */
    private String categoryId;

    /**
     * 商品单位
     */
    private String unit;

    /**
     * 默认价格（单位：分）
     */
    private Integer defaultPrice;

    /**
     * 价格策略
     * <p>
     * UNIFIED-统一定价
     * AREA-区域定价
     * TIME-时段定价
     * COMBINED-组合定价
     * </p>
     */
    private String priceStrategy;

    /**
     * 是否启用库存管理
     */
    private Boolean stockEnabled;

    /**
     * 商品图片URL
     */
    private String imageUrl;

    /**
     * 是否上架
     */
    private Boolean available;

    /**
     * 可售区域ID列表（JSON数组）
     * <p>
     * 示例：["area001", "area002", "area003"]
     * </p>
     */
    private String areaIds;

    /**
     * 商品条码（默认条码）
     */
    private String barcode;

    /**
     * 商品规格
     */
    private String spec;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 价格（BigDecimal类型，用于计算）
     * <p>
     * 从defaultPrice转换而来
     * </p>
     */
    public BigDecimal getPrice() {
        return defaultPrice != null ? BigDecimal.valueOf(defaultPrice).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 销售价格（BigDecimal类型）
     * <p>
     * 从defaultPrice转换而来，与getPrice()相同
     * </p>
     */
    public BigDecimal getSalePrice() {
        return getPrice();
    }

    /**
     * 设置销售价格
     *
     * @param salePrice 销售价格
     */
    public void setSalePrice(BigDecimal salePrice) {
        this.defaultPrice = salePrice != null ? salePrice.multiply(BigDecimal.valueOf(100)).intValue() : null;
    }

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 销售数量
     */
    private Integer saleQuantity;

    /**
     * 浏览次数
     */
    private Integer viewQuantity;

    /**
     * 是否推荐
     */
    private Boolean isRecommended;

    /**
     * 是否热销
     */
    private Boolean isHotSale;

    /**
     * 是否新品
     */
    private Boolean isNew;
}



