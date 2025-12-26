package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费产品实体类
 * <p>
 * 管理消费产品信息，支持超市/餐厅消费场景
 * 提供库存管理、价格统计、销售分析等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 商品资料管理
 * - 商品价格策略
 * - 商品库存管理
 * - 商品销售统计
 * - 商品评分管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_product")
@Schema(description = "消费产品实体")
public class ConsumeProductEntity extends BaseEntity {

    /**
     * 产品ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "产品ID")
    private Long productId;

    /**
     * 产品编码（唯一）
     */
    @Schema(description = "产品编码")
    private String productCode;

    /**
     * 产品名称
     */
    @Schema(description = "产品名称")
    private String productName;

    /**
     * 产品分类ID
     */
    @Schema(description = "产品分类ID")
    private Long categoryId;

    /**
     * 产品描述
     */
    @Schema(description = "产品描述")
    private String description;

    /**
     * 产品图片URL
     */
    @Schema(description = "产品图片URL")
    private String imageUrl;

    /**
     * 产品价格（元）
     */
    @Schema(description = "产品价格")
    private BigDecimal price;

    /**
     * 原价（元）
     */
    @Schema(description = "原价")
    private BigDecimal originalPrice;

    /**
     * 成本价（元）
     */
    @Schema(description = "成本价")
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    @Schema(description = "库存数量")
    private Integer stock;

    /**
     * 最小库存警戒线
     */
    @Schema(description = "最小库存警戒线")
    private Integer minStock;

    /**
     * 最大库存
     */
    @Schema(description = "最大库存")
    private Integer maxStock;

    /**
     * 产品状态（0-下架 1-上架 2-缺货 3-停售）
     */
    @Schema(description = "产品状态（0-下架 1-上架 2-缺货 3-停售）")
    private Integer status;

    /**
     * 是否推荐商品（0-否 1-是）
     */
    @Schema(description = "是否推荐商品")
    private Integer isRecommended;

    /**
     * 推荐序号（越小越靠前）
     */
    @Schema(description = "推荐序号")
    private Integer recommendOrder;

    /**
     * 产品评分（0-5分）
     */
    @Schema(description = "产品评分")
    private BigDecimal rating;

    /**
     * 评分人数
     */
    @Schema(description = "评分人数")
    private Integer ratingCount;

    /**
     * 销量
     */
    @Schema(description = "销量")
    private Integer salesCount;

    /**
     * 浏览量
     */
    @Schema(description = "浏览量")
    private Integer viewCount;

    /**
     * 商品标签（JSON数组，如：["热销", "新品", "优惠"]）
     */
    @Schema(description = "商品标签")
    private String tags;

    /**
     * 商品规格（JSON格式，如：{"size": "大", "color": "红色"}）
     */
    @Schema(description = "商品规格")
    private String specifications;

    /**
     * 商品单位（个、份、瓶等）
     */
    @Schema(description = "商品单位")
    private String unit;

    /**
     * 商品条码
     */
    @Schema(description = "商品条码")
    private String barcode;

    /**
     * 供应商ID
     */
    @Schema(description = "供应商ID")
    private Long supplierId;

    /**
     * 供应周期（天）
     */
    @Schema(description = "供应周期")
    private Integer supplyCycle;

    /**
     * 上架时间
     */
    @Schema(description = "上架时间")
    private LocalDateTime onShelfTime;

    /**
     * 下架时间
     */
    @Schema(description = "下架时间")
    private LocalDateTime offShelfTime;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查库存是否充足
     *
     * @param quantity 需要的数量
     * @return true-库存充足，false-库存不足
     */
    public boolean hasEnoughStock(Integer quantity) {
        return stock != null && stock >= quantity;
    }

    /**
     * 检查是否上架
     *
     * @return true-已上架，false-未上架
     */
    public boolean isOnSale() {
        return status != null && status == 1;
    }

    /**
     * 检查是否推荐
     *
     * @return true-推荐，false-不推荐
     */
    public boolean isRecommended() {
        return isRecommended != null && isRecommended == 1;
    }

    /**
     * 检查是否库存不足
     *
     * @return true-库存不足，false-库存充足
     */
    public boolean isLowStock() {
        return minStock != null && stock != null && stock < minStock;
    }

    /**
     * 获取折扣比例（0-1）
     *
     * @return 折扣比例，如果没有折扣则返回1.0
     */
    public BigDecimal getDiscountRate() {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ONE;
        }
        return price.divide(originalPrice, 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 获取折扣金额
     *
     * @return 折扣金额
     */
    public BigDecimal getDiscountAmount() {
        if (originalPrice == null) {
            return BigDecimal.ZERO;
        }
        return originalPrice.subtract(price);
    }
}
