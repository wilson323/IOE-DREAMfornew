package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Version;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费产品实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_product")
@Schema(description = "消费产品实体")
public class ConsumeProductEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @TableField("product_code")
    @NotBlank(message = "产品编码不能为空")
    @Size(max = 50, message = "产品编码长度不能超过50个字符")
    @Schema(description = "产品编码", example = "PROD_001")
    private String productCode;

    @TableField("product_name")
    @NotBlank(message = "产品名称不能为空")
    @Size(max = 100, message = "产品名称长度不能超过100个字符")
    @Schema(description = "产品名称", example = "营养早餐套餐")
    private String productName;

    @TableField("product_category")
    @NotNull(message = "产品分类不能为空")
    @Schema(description = "产品分类", example = "1")
    private Integer productCategory;

    @TableField(exist = false)
    @Schema(description = "分类名称", example = "早餐")
    private String categoryName;

    @TableField("base_price")
    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0.01", message = "基础价格必须大于0")
    @Digits(integer = 10, fraction = 2, message = "基础价格格式不正确")
    @Schema(description = "基础价格", example = "15.00")
    private BigDecimal basePrice;

    @TableField("sale_price")
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.01", message = "销售价格必须大于0")
    @Digits(integer = 10, fraction = 2, message = "销售价格格式不正确")
    @Schema(description = "销售价格", example = "12.00")
    private BigDecimal salePrice;

    @TableField("cost_price")
    @DecimalMin(value = "0", message = "成本价格不能为负数")
    @Digits(integer = 10, fraction = 2, message = "成本价格格式不正确")
    @Schema(description = "成本价格", example = "8.00")
    private BigDecimal costPrice;

    @TableField("product_status")
    @NotNull(message = "产品状态不能为空")
    @Min(value = 0, message = "产品状态不能为负数")
    @Max(value = 1, message = "产品状态值无效")
    @Schema(description = "产品状态", example = "1")
    private Integer productStatus;

    @TableField(exist = false)
    @Schema(description = "产品状态名称", example = "上架")
    private String productStatusName;

    @TableField("stock_quantity")
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stockQuantity;

    @TableField("allow_discount")
    @NotNull(message = "是否允许折扣不能为空")
    @Schema(description = "是否允许折扣", example = "1")
    private Integer allowDiscount;

    @TableField("max_discount_rate")
    @DecimalMin(value = "0", message = "最大折扣率不能为负数")
    @DecimalMax(value = "1", message = "最大折扣率不能超过1")
    @Digits(integer = 3, fraction = 2, message = "最大折扣率格式不正确")
    @Schema(description = "最大折扣率", example = "0.3")
    private BigDecimal maxDiscountRate;

    @TableField("sale_time_periods")
    @Schema(description = "销售时间段", example = "[\"06:00-10:00\"]")
    private String saleTimePeriods;

    @TableField("product_image")
    @Size(max = 255, message = "产品图片路径长度不能超过255个字符")
    @Schema(description = "产品图片", example = "product-image.jpg")
    private String productImage;

    @TableField("product_description")
    @Size(max = 500, message = "产品描述长度不能超过500个字符")
    @Schema(description = "产品描述", example = "营养丰富的早餐套餐")
    private String productDescription;

    @TableField("product_tags")
    @Size(max = 255, message = "产品标签长度不能超过255个字符")
    @Schema(description = "产品标签", example = "营养,早餐,热销")
    private String productTags;

    @TableField("category_id")
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @TableField("product_type")
    @Schema(description = "产品类型", example = "1")
    private Integer productType;

    @TableField("specification")
    @Size(max = 100, message = "产品规格长度不能超过100个字符")
    @Schema(description = "产品规格", example = "标准份")
    private String specification;

    @TableField("unit")
    @Size(max = 20, message = "计量单位长度不能超过20个字符")
    @Schema(description = "计量单位", example = "份")
    private String unit;

    @TableField("warning_stock")
    @Min(value = 0, message = "预警库存不能为负数")
    @Schema(description = "预警库存", example = "10")
    private Integer warningStock;

    @TableField("image_url")
    @Size(max = 255, message = "图片URL长度不能超过255个字符")
    @Schema(description = "图片URL", example = "product-image.jpg")
    private String imageUrl;

    @TableField("description")
    @Size(max = 1000, message = "产品描述长度不能超过1000个字符")
    @Schema(description = "产品描述", example = "营养丰富的早餐套餐")
    private String description;

    @TableField("nutrition_info")
    @Size(max = 500, message = "营养信息长度不能超过500个字符")
    @Schema(description = "营养信息", example = "热量:350kcal, 蛋白质:15g")
    private String nutritionInfo;

    @TableField("allergen_info")
    @Size(max = 300, message = "过敏信息长度不能超过300个字符")
    @Schema(description = "过敏信息", example = "含鸡蛋、牛奶")
    private String allergenInfo;

    @TableField("is_recommended")
    @Schema(description = "是否推荐", example = "1")
    private Integer isRecommended;

    @TableField("recommend_sort")
    @Schema(description = "推荐排序", example = "100")
    private Integer recommendSort;

    @TableField("sales_count")
    @Min(value = 0, message = "销量不能为负数")
    @Schema(description = "销量", example = "500")
    private Long salesCount;

    @TableField("rating_average")
    @DecimalMin(value = "0", message = "平均评分不能为负数")
    @DecimalMax(value = "5", message = "平均评分不能超过5")
    @Digits(integer = 1, fraction = 2, message = "平均评分格式不正确")
    @Schema(description = "平均评分", example = "4.5")
    private BigDecimal rating;

    @TableField("rating_count")
    @Min(value = 0, message = "评分数量不能为负数")
    @Schema(description = "评分数量", example = "150")
    private Integer ratingCount;

    @TableField("create_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-12-21 10:00:00")
    private LocalDateTime createTime;

    @TableField("update_time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-12-21 10:00:00")
    private LocalDateTime updateTime;

    @TableField("version")
    @Version
    @Schema(description = "版本号", example = "1")
    private Integer version;

    // 审计字段继承自BaseEntity，避免重复定义

    // ==================== 业务状态方法 ====================

    /**
     * 检查产品是否上架
     */
    public boolean isOnSale() {
        return Integer.valueOf(1).equals(productStatus);
    }

    /**
     * 检查产品是否下架
     */
    public boolean isOffShelf() {
        return Integer.valueOf(0).equals(productStatus);
    }

    /**
     * 检查产品是否下架（兼容性方法）
     */
    public boolean isOffSale() {
        return isOffShelf();
    }

  
    /**
     * 检查是否有库存
     */
    public boolean hasStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * 检查是否允许折扣
     */
    public boolean canDiscount() {
        return Integer.valueOf(1).equals(allowDiscount);
    }

    /**
     * 检查是否热销
     */
    public boolean isHotSale() {
        return rating != null && rating.compareTo(new BigDecimal("4.5")) >= 0 &&
               ratingCount != null && ratingCount >= 50;
    }

    /**
     * 获取剩余库存百分比
     */
    public BigDecimal getStockPercentage() {
        if (stockQuantity == null || stockQuantity <= 0) {
            return BigDecimal.ZERO;
        }
        // 假设总库存为1000，计算剩余百分比
        return BigDecimal.valueOf(stockQuantity).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取使用率百分比
     */
    public BigDecimal getUsagePercentage() {
        if (ratingCount == null || ratingCount <= 0) {
            return BigDecimal.ZERO;
        }
        // 假设预期使用次数为500，计算使用率
        return BigDecimal.valueOf(ratingCount).divide(BigDecimal.valueOf(500), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 检查产品是否需要补货
     */
    public boolean needRestock() {
        return stockQuantity != null && stockQuantity <= 10;
    }

    /**
     * 检查产品价格是否合理
     */
    public boolean isPriceReasonable() {
        if (basePrice != null && salePrice != null) {
            return salePrice.compareTo(basePrice) <= 0;
        }
        if (costPrice != null && salePrice != null) {
            return salePrice.compareTo(costPrice) >= 0;
        }
        return true;
    }

    /**
     * 验证业务规则
     */
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 检查必填字段
        if (productCode == null || productCode.trim().isEmpty()) {
            errors.add("产品编码不能为空");
        }
        if (productName == null || productName.trim().isEmpty()) {
            errors.add("产品名称不能为空");
        }
        if (productCategory == null) {
            errors.add("产品分类不能为空");
        }

        // 检查价格合理性
        if (!isPriceReasonable()) {
            errors.add("价格设置不合理：售价不应高于基础价格，且不应低于成本价");
        }

        // 检查库存
        if (stockQuantity != null && stockQuantity < 0) {
            errors.add("库存数量不能为负数");
        }

        // 检查评分
        if (rating != null && (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0)) {
            errors.add("平均评分必须在0-5之间");
        }

        return errors;
    }

    /**
     * 计算实际售价（考虑折扣）
     */
    public BigDecimal calculateActualPrice(BigDecimal discountRate) {
        if (salePrice == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal actualPrice = salePrice;

        // 检查是否允许折扣
        if (discountRate != null && canDiscount()) {
            // 不能超过最大折扣比例
            BigDecimal maxDiscount = maxDiscountRate != null ? maxDiscountRate : new BigDecimal("0.3");
            if (discountRate.compareTo(maxDiscount) > 0) {
                discountRate = maxDiscount;
            }

            BigDecimal discountAmount = actualPrice.multiply(discountRate);
            actualPrice = actualPrice.subtract(discountAmount);

            // 确保折后价格不低于成本价
            if (costPrice != null && actualPrice.compareTo(costPrice) < 0) {
                actualPrice = costPrice;
            }
        }

        return actualPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 检查产品是否在当前时间可销售
     */
    public boolean isAvailableAtTime(java.time.LocalDateTime currentTime) {
        if (currentTime == null) {
            return false;
        }

        // 检查产品状态
        if (!isOnSale()) {
            return false;
        }

        // 检查库存
        if (!hasStock()) {
            return false;
        }

        // 检查销售时间段（简化实现）
        return true; // 默认一直可销售
    }

    /**
     * 获取销售状态描述
     */
    public String getSalesStatusDescription() {
        if (isOnSale()) {
            if (hasStock()) {
                return "在售";
            } else {
                return "缺货";
            }
        } else {
            return "下架";
        }
    }

    /**
     * 获取库存状态描述
     */
    public String getStockStatusDescription() {
        if (stockQuantity == null) {
            return "未知";
        }
        if (stockQuantity <= 0) {
            return "缺货";
        } else if (stockQuantity <= 10) {
            return "库存紧张";
        } else if (stockQuantity <= 50) {
            return "库存较少";
        } else {
            return "库存充足";
        }
    }

    /**
     * 检查是否为热销产品
     */
    public boolean isPopular() {
        return isHotSale() && rating != null &&
               rating.compareTo(new BigDecimal("4.8")) >= 0;
    }

    /**
     * 获取推荐等级
     */
    public String getRecommendationLevel() {
        if (isPopular()) {
            return "强烈推荐";
        } else if (isHotSale()) {
            return "推荐";
        } else if (rating != null && rating.compareTo(new BigDecimal("4.0")) >= 0) {
            return "良好";
        } else {
            return "普通";
        }
    }

    // ==================== 兼容性Getter方法 ====================

    /**
     * 获取平均评分（兼容性方法）
     */
    public BigDecimal getRatingAverage() {
        return this.rating;
    }

    /**
     * 设置平均评分（兼容性方法）
     */
    public void setRatingAverage(BigDecimal ratingAverage) {
        this.rating = ratingAverage;
    }

    /**
     * 获取产品状态（兼容性方法）
     */
    public Integer getStatus() {
        return this.productStatus;
    }

    /**
     * 设置产品状态（兼容性方法）
     */
    public void setStatus(Integer status) {
        this.productStatus = status;
    }

    /**
     * 获取产品状态（Integer类型）
     */
    public Integer getProductStatus() {
        return this.productStatus;
    }

    /**
     * 获取版本号（兼容性方法）
     */
    public Integer getVersion() {
        return this.version;
    }

    // ==================== 兼容性方法 ====================

    /**
     * 获取库存数量（兼容性方法）
     * @deprecated 使用 {@link #getStockQuantity()} 替代
     */
    @Deprecated
    public Integer getStock() {
        return stockQuantity;
    }

    /**
     * 设置库存数量（兼容性方法）
     * @deprecated 使用 {@link #setStockQuantity(Integer)} 替代
     */
    @Deprecated
    public void setStock(Integer stock) {
        this.stockQuantity = stock;
    }
}