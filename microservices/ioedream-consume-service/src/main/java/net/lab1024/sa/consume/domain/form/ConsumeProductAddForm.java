package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.net.URL;

/**
 * 消费产品添加表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段验证
 * - 业务规则约束
 * - 数据格式验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费产品添加表单")
public class ConsumeProductAddForm {

    @Schema(description = "产品编码", example = "P001", required = true)
    @NotBlank(message = "产品编码不能为空")
    @Size(max = 50, message = "产品编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "产品编码只能包含字母、数字、下划线和连字符")
    private String productCode;

    @Schema(description = "产品名称", example = "宫保鸡丁套餐", required = true)
    @NotBlank(message = "产品名称不能为空")
    @Size(max = 100, message = "产品名称长度不能超过100个字符")
    private String productName;

    @Schema(description = "产品分类ID", example = "1", required = true)
    @NotNull(message = "产品分类不能为空")
    @Positive(message = "产品分类ID必须为正数")
    private Long categoryId;

    @Schema(description = "产品类型", example = "2", required = true)
    @NotNull(message = "产品类型不能为空")
    @Min(value = 1, message = "产品类型值不正确")
    @Max(value = 4, message = "产品类型值不正确")
    private Integer productType;

    @Schema(description = "产品规格", example = "大份/含米饭")
    @Size(max = 200, message = "产品规格长度不能超过200个字符")
    private String specification;

    @Schema(description = "产品单位", example = "份", required = true)
    @NotBlank(message = "产品单位不能为空")
    @Size(max = 20, message = "产品单位长度不能超过20个字符")
    private String unit;

    @Schema(description = "基础价格", example = "15.00", required = true)
    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0.01", message = "基础价格必须大于0")
    @Digits(integer = 10, fraction = 2, message = "基础价格格式不正确")
    private BigDecimal basePrice;

    @Schema(description = "当前售价", example = "12.00", required = true)
    @NotNull(message = "当前售价不能为空")
    @DecimalMin(value = "0.01", message = "当前售价必须大于0")
    @Digits(integer = 10, fraction = 2, message = "当前售价格式不正确")
    private BigDecimal salePrice;

    @Schema(description = "成本价", example = "8.00")
    @DecimalMin(value = "0.00", message = "成本价不能小于0")
    @Digits(integer = 10, fraction = 2, message = "成本价格式不正确")
    private BigDecimal costPrice;

    @Schema(description = "库存数量", example = "100")
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stockQuantity;

    @Schema(description = "警戒库存", example = "10")
    @Min(value = 0, message = "警戒库存不能小于0")
    private Integer warningStock;

    @Schema(description = "产品图片URL", example = "https://example.com/images/product1.jpg")
    @Size(max = 500, message = "产品图片URL长度不能超过500个字符")
    private String imageUrl;

    @Schema(description = "产品描述", example = "经典川菜，麻辣鲜香")
    @Size(max = 1000, message = "产品描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "营养成分（JSON格式）", example = "{\"calories\": 650, \"protein\": 25}")
    private String nutritionInfo;

    @Schema(description = "过敏原信息", example = "含花生、大豆")
    @Size(max = 200, message = "过敏原信息长度不能超过200个字符")
    private String allergenInfo;

    @Schema(description = "是否启用推荐", example = "1")
    @Min(value = 0, message = "是否启用推荐值不正确")
    @Max(value = 1, message = "是否启用推荐值不正确")
    private Integer isRecommended;

    @Schema(description = "推荐排序", example = "1")
    @Min(value = 0, message = "推荐排序不能小于0")
    private Integer recommendSort;

    @Schema(description = "产品状态", example = "1", required = true)
    @NotNull(message = "产品状态不能为空")
    @Min(value = 1, message = "产品状态值不正确")
    @Max(value = 3, message = "产品状态值不正确")
    private Integer status;

    @Schema(description = "是否允许折扣", example = "1")
    @Min(value = 0, message = "是否允许折扣值不正确")
    @Max(value = 1, message = "是否允许折扣值不正确")
    private Integer allowDiscount;

    @Schema(description = "最大折扣比例", example = "0.8")
    @DecimalMin(value = "0.0", message = "最大折扣比例不能小于0")
    @DecimalMax(value = "1.0", message = "最大折扣比例不能大于1")
    private BigDecimal maxDiscountRate;

    @Schema(description = "销售时间段（JSON格式）", example = "[\"11:00-14:00\", \"17:00-20:00\"]")
    private String saleTimePeriods;

    @Schema(description = "备注信息", example = "新品上市")
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;

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