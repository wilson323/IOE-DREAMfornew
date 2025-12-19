package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费产品实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_product")
@Schema(description = "消费产品实体")
public class ConsumeProductEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 产品ID
     */
    @TableField("product_id")
    @Schema(description = "产品ID")
    private Long productId;
    /**
     * 产品编码
     */
    @TableField("product_code")
    @Schema(description = "产品编码")
    private String productCode;
    /**
     * 产品名称
     */
    @TableField("product_name")
    @Schema(description = "产品名称")
    private String productName;
    /**
     * 产品分类
     */
    @TableField("category")
    @Schema(description = "产品分类")
    private String category;
    /**
     * 价格
     */
    @TableField("price")
    @Schema(description = "价格")
    private java.math.BigDecimal price;
    /**
     * 库存
     */
    @TableField("stock")
    @Schema(description = "库存")
    private Integer stock;
    /**
     * 状态
     */
    @TableField("status")
    @Schema(description = "状态")
    private Integer status;

    // 缺失字段 - 根据错误日志添加
    /**
     * 产品描述
     */
    @TableField("description")
    @Schema(description = "产品描述")
    private String description;

    /**
     * 产品图片URL
     */
    @TableField("image_url")
    @Schema(description = "产品图片URL")
    private String imageUrl;

    /**
     * 成本价格
     */
    @TableField("cost_price")
    @Schema(description = "成本价格")
    private BigDecimal costPrice;

    /**
     * 销售价格
     */
    @TableField("sale_price")
    @Schema(description = "销售价格")
    private BigDecimal salePrice;

    /**
     * 会员价格
     */
    @TableField("member_price")
    @Schema(description = "会员价格")
    private BigDecimal memberPrice;

    /**
     * 产品单位
     */
    @TableField("unit")
    @Schema(description = "产品单位")
    private String unit;

    /**
     * 最小库存预警
     */
    @TableField("min_stock")
    @Schema(description = "最小库存预警")
    private Integer minStock;

    /**
     * 最大库存
     */
    @TableField("max_stock")
    @Schema(description = "最大库存")
    private Integer maxStock;

    /**
     * 供应商ID
     */
    @TableField("supplier_id")
    @Schema(description = "供应商ID")
    private Long supplierId;

    /**
     * 供应商名称
     */
    @TableField("supplier_name")
    @Schema(description = "供应商名称")
    private String supplierName;

    /**
     * 产品标签
     */
    @TableField("tags")
    @Schema(description = "产品标签")
    private String tags;

    /**
     * 是否推荐
     */
    @TableField("is_recommended")
    @Schema(description = "是否推荐")
    private Boolean recommended;

    /**
     * 是否热销
     * <p>
     * 兼容推荐/热销/新品等运营标签（ConsumeRecommendService依赖）
     * </p>
     */
    @TableField("is_hot_sale")
    @Schema(description = "是否热销")
    private Boolean hotSale;

    /**
     * 是否新品
     */
    @TableField("is_new")
    @Schema(description = "是否新品")
    private Boolean isNew;

    // ==================== 测试/旧代码兼容方法 ====================

    /**
     * 设置产品ID（兼容：int -> Long）
     *
     * @param productId 产品ID
     */
    public void setProductId(int productId) {
        this.productId = (long) productId;
    }

    /**
     * 分类ID
     * <p>
     * 用于推荐算法按分类聚合（ConsumeRecommendService依赖）
     * </p>
     */
    @TableField("category_id")
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 浏览量
     */
    @TableField("view_quantity")
    @Schema(description = "浏览量")
    private Long viewQuantity;

    /**
     * 销量
     */
    @TableField("sale_quantity")
    @Schema(description = "销量")
    private Long saleQuantity;

    /**
     * 评分（0~5）
     */
    @TableField("rating")
    @Schema(description = "评分")
    private BigDecimal rating;

    /**
     * 是否上架
     */
    @TableField("is_online")
    @Schema(description = "是否上架")
    private Boolean online;

    /**
     * 是否可用
     * <p>
     * 根据chonggou.txt要求添加
     * 兼容Boolean和Integer类型
     * </p>
     */
    @TableField("available")
    @Schema(description = "是否可用")
    private Integer available;

    /**
     * 关联区域ID列表（JSON格式）
     * <p>
     * 根据chonggou.txt要求添加
     * 存储格式：["areaId1", "areaId2", ...]
     * </p>
     */
    @TableField("area_ids")
    @Schema(description = "关联区域ID列表")
    private String areaIds;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 缺失的getter/setter方法 - 根据错误日志添加
    public BigDecimal getSalePrice() {
        return this.salePrice != null ? this.salePrice : this.price;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Boolean getRecommended() {
        return this.recommended;
    }

    public void setRecommended(Boolean recommended) {
        this.recommended = recommended;
    }

    public Boolean getOnline() {
        return this.online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 获取是否可用
     * <p>
     * 兼容方法：如果available为空，则根据status判断
     * </p>
     *
     * @return 是否可用（1-可用 0-不可用）
     */
    public Integer getAvailable() {
        // 兼容Boolean和Integer类型
        if (this.available != null) {
            return this.available;
        }
        // 如果available为空，则根据status判断
        return (this.status != null && this.status == 1) ? 1 : 0;
    }

    /**
     * 设置是否可用
     *
     * @param available 是否可用
     */
    public void setAvailable(Integer available) {
        this.available = available;
    }

    /**
     * 设置是否可用（Boolean兼容）
     * <p>
     * 修复：部分业务代码以boolean/Boolean赋值，实体字段为Integer导致编译失败
     * </p>
     *
     * @param available 是否可用
     */
    public void setAvailable(Boolean available) {
        if (available == null) {
            this.available = null;
            return;
        }
        this.available = available ? 1 : 0;
    }

    /**
     * 获取关联区域ID列表
     *
     * @return 关联区域ID列表（JSON格式）
     */
    public String getAreaIds() {
        return this.areaIds;
    }

    /**
     * 设置关联区域ID列表
     *
     * @param areaIds 关联区域ID列表（JSON格式）
     */
    public void setAreaIds(String areaIds) {
        this.areaIds = areaIds;
    }

    /**
     * 获取是否可用（Boolean兼容方法）
     * <p>
     * 兼容代码中使用Boolean类型的场景
     * </p>
     *
     * @return 是否可用（Boolean类型）
     */
    public Boolean getAvailableAsBoolean() {
        Integer available = getAvailable();
        return available != null && available == 1;
    }

    /**
     * 获取产品编码（兼容旧字段名）
     *
     * @return 产品编码
     */
    public String getCode() {
        return this.productCode;
    }

    /**
     * 获取产品名称（兼容旧字段名）
     *
     * @return 产品名称
     */
    public String getName() {
        return this.productName;
    }

    /**
     * 获取分类ID
     *
     * @return 分类ID
     */
    public Long getCategoryId() {
        return this.categoryId;
    }

    /**
     * 获取是否推荐（兼容：getIsRecommended）
     *
     * @return 1-是 0-否
     */
    public Integer getIsRecommended() {
        return Boolean.TRUE.equals(this.recommended) ? 1 : 0;
    }

    /**
     * 获取是否热销（兼容：getIsHotSale）
     *
     * @return 1-是 0-否
     */
    public Integer getIsHotSale() {
        return Boolean.TRUE.equals(this.hotSale) ? 1 : 0;
    }

    /**
     * 获取是否新品（兼容：getIsNew）
     *
     * @return 1-是 0-否
     */
    public Integer getIsNew() {
        return Boolean.TRUE.equals(this.isNew) ? 1 : 0;
    }

    /**
     * 获取销量（空值安全）
     *
     * @return 销量
     */
    public Long getSaleQuantity() {
        return this.saleQuantity == null ? 0L : this.saleQuantity;
    }

    /**
     * 获取浏览量（空值安全）
     *
     * @return 浏览量
     */
    public Long getViewQuantity() {
        return this.viewQuantity == null ? 0L : this.viewQuantity;
    }

    /**
     * 获取评分（空值安全）
     *
     * @return 评分
     */
    public BigDecimal getRating() {
        return this.rating == null ? BigDecimal.ZERO : this.rating;
    }
}
