package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费餐次分类更新表单
 * <p>
 * 包含完整的分类更新字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费餐次分类更新表单")
public class ConsumeMealCategoryUpdateForm {

    @Schema(description = "分类ID", example = "1")
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "分类编码", example = "BREAKFAST")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    private String categoryCode;

    @Schema(description = "分类名称", example = "早餐")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String categoryName;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

    @Schema(description = "分类层级", example = "1")
    @Min(value = 1, message = "分类层级不能小于1")
    @Max(value = 3, message = "分类层级不能大于3")
    private Integer categoryLevel;

    @Schema(description = "排序号", example = "1")
    @Min(value = 1, message = "排序号不能小于1")
    private Integer sortOrder;

    @Schema(description = "分类图标", example = "breakfast-icon.png")
    @Size(max = 255, message = "分类图标长度不能超过255个字符")
    private String categoryIcon;

    @Schema(description = "分类颜色", example = "#FF6B6B")
    @Size(max = 20, message = "分类颜色长度不能超过20个字符")
    private String categoryColor;

    @Schema(description = "分类状态", example = "1")
    @Min(value = 0, message = "分类状态值无效")
    @Max(value = 1, message = "分类状态值无效")
    private Integer categoryStatus;

    @Schema(description = "分类描述", example = "早餐分类，包含各类早餐食品")
    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    private String description;

    @Schema(description = "可用时间段", example = "[\"06:00-10:00\", \"18:00-20:00\"]")
    private String availableTimePeriods;

    @Schema(description = "备注", example = "早餐分类")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "是否默认", example = "0")
    private Integer isDefault;

    @Schema(description = "基础价格", example = "15.00")
    @DecimalMin(value = "0", message = "基础价格不能为负数")
    private BigDecimal basePrice;

    @Schema(description = "员工价格", example = "12.00")
    @DecimalMin(value = "0", message = "员工价格不能为负数")
    private BigDecimal staffPrice;

    @Schema(description = "学生价格", example = "8.00")
    @DecimalMin(value = "0", message = "学生价格不能为负数")
    private BigDecimal studentPrice;

    /**
     * 获取状态（兼容性方法）
     */
    public Integer getStatus() {
        return this.categoryStatus;
    }

    /**
     * 获取最大金额限制（兼容性方法）
     */
    public BigDecimal getMaxAmountLimit() {
        if (this.basePrice != null) {
            return this.basePrice.multiply(BigDecimal.valueOf(2));
        }
        return BigDecimal.valueOf(100.00);
    }

    /**
     * 获取最小金额限制（兼容性方法）
     */
    public BigDecimal getMinAmountLimit() {
        return BigDecimal.valueOf(0.01);
    }

    /**
     * 获取每日限制次数（兼容性方法）
     */
    public Integer getDailyLimitCount() {
        return 10;
    }

    /**
     * 获取是否允许折扣（兼容性方法）
     */
    public Integer getAllowDiscount() {
        return 1;
    }

    /**
     * 获取折扣率（兼容性方法）
     */
    public BigDecimal getDiscountRate() {
        return BigDecimal.valueOf(0.95);
    }

    /**
     * 获取版本号（兼容性方法）
     */
    public Integer getVersion() {
        return 1;
    }

    /**
     * 获取评分平均值（兼容性方法）
     */
    public Double getRatingAverage() {
        return 5.0;
    }
}
