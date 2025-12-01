package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体
 * <p>
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用@TableName指定表名
 * - 使用@TableId标记主键
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的产品业务字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_product")
public class ProductEntity extends BaseEntity {

    /**
     * 产品ID
     */
    @TableId
    private Long productId;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品分类ID
     */
    private Long categoryId;

    /**
     * 产品分类名称
     */
    private String categoryName;

    /**
     * 产品价格
     */
    private BigDecimal price;

    /**
     * 产品库存
     */
    private Integer stock;

    /**
     * 产品状态：1-上架 2-下架 3-停售
     */
    private Integer status;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 产品图片URL
     */
    private String imageUrl;

    /**
     * 产品规格
     */
    private String specifications;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 最小购买数量
     */
    private Integer minPurchaseQuantity;

    /**
     * 最大购买数量
     */
    private Integer maxPurchaseQuantity;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 建议零售价
     */
    private BigDecimal retailPrice;

    /**
     * 产品标签
     */
    private String tags;

    /**
     * 产品重量（克）
     */
    private BigDecimal weight;

    /**
     * 产品体积（立方厘米）
     */
    private BigDecimal volume;

    /**
     * 是否推荐：0-否 1-是
     */
    private Integer isRecommended;

    /**
     * 是否新品：0-否 1-是
     */
    private Integer isNew;

    /**
     * 是否热销：0-否 1-是
     */
    private Integer isHotSale;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}