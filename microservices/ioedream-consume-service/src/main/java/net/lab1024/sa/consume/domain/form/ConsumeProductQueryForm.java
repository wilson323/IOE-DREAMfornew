package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import net.lab1024.sa.common.domain.form.PageForm;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;

/**
 * 消费产品查询表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的查询条件
 * - 分页参数
 * - 排序条件
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费产品查询表单")
public class ConsumeProductQueryForm extends PageForm {

    @Schema(description = "产品编码", example = "P001")
    private String productCode;

    @Schema(description = "产品名称", example = "宫保鸡丁")
    private String productName;

    @Schema(description = "产品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "产品类型", example = "2")
    private Integer productType;

    @Schema(description = "产品状态", example = "1")
    private Integer status;

    @Schema(description = "是否推荐", example = "1")
    private Integer isRecommended;

    @Schema(description = "价格范围-最小值", example = "5.00")
    @DecimalMin(value = "0.00", message = "最小价格不能小于0")
    private BigDecimal minPrice;

    @Schema(description = "价格范围-最大值", example = "50.00")
    @DecimalMin(value = "0.00", message = "最大价格不能小于0")
    private BigDecimal maxPrice;

    @Schema(description = "库存状态", example = "1")
    private Integer stockStatus;

    @Schema(description = "创建时间-开始", example = "2025-12-01 00:00:00")
    private String createTimeStart;

    @Schema(description = "创建时间-结束", example = "2025-12-31 23:59:59")
    private String createTimeEnd;

    @Schema(description = "更新时间-开始", example = "2025-12-01 00:00:00")
    private String updateTimeStart;

    @Schema(description = "更新时间-结束", example = "2025-12-31 23:59:59")
    private String updateTimeEnd;

    @Schema(description = "关键字搜索（产品名称或编码）", example = "宫保")
    private String keyword;

    @Schema(description = "排序字段", example = "createTime")
    private String sortBy;

    @Schema(description = "排序方向", example = "desc")
    private String sortDirection;

    @Schema(description = "是否查询详细信息", example = "false")
    private Boolean fetchDetail;

    @Schema(description = "是否包含已删除", example = "false")
    private Boolean includeDeleted;

    // ==================== 查询条件构建方法 ====================

    /**
     * 是否有产品编码查询条件
     */
    public boolean hasProductCode() {
        return productCode != null && !productCode.trim().isEmpty();
    }

    /**
     * 是否有产品名称查询条件
     */
    public boolean hasProductName() {
        return productName != null && !productName.trim().isEmpty();
    }

    /**
     * 是否有关键字查询条件
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 是否有价格范围查询条件
     */
    public boolean hasPriceRange() {
        return minPrice != null || maxPrice != null;
    }

    /**
     * 是否有创建时间范围查询条件
     */
    public boolean hasCreateTimeRange() {
        return createTimeStart != null || createTimeEnd != null;
    }

    /**
     * 是否有更新时间范围查询条件
     */
    public boolean hasUpdateTimeRange() {
        return updateTimeStart != null || updateTimeEnd != null;
    }

    /**
     * 是否有排序条件
     */
    public boolean hasSortCondition() {
        return sortBy != null && !sortBy.trim().isEmpty();
    }

    /**
     * 获取排序方向（默认为降序）
     */
    public String getSortDirection() {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return "desc";
        }
        return sortDirection.trim().toLowerCase();
    }

    /**
     * 获取排序字段（默认为创建时间）
     */
    public String getSortBy() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "createTime";
        }

        // 验证排序字段是否合法
        String[] allowedSortFields = {
            "productId", "productCode", "productName", "categoryId",
            "productType", "basePrice", "salePrice", "stockQuantity",
            "salesCount", "rating", "status", "createTime", "updateTime"
        };

        String sortField = sortBy.trim();
        for (String allowedField : allowedSortFields) {
            if (allowedField.equals(sortField)) {
                return sortField;
            }
        }

        return "createTime"; // 默认按创建时间排序
    }

    /**
     * 获取库存状态枚举值
     */
    public Integer getStockStatusValue() {
        if (stockStatus == null) {
            return null;
        }

        switch (stockStatus) {
            case 1: return 1; // 有库存
            case 2: return 2; // 库存不足
            case 3: return 3; // 无库存
            default: return null;
        }
    }

    /**
     * 构建完整的排序字符串
     */
    public String getSortClause() {
        if (!hasSortCondition()) {
            return "ORDER BY create_time DESC";
        }
        return String.format("ORDER BY %s %s", getSortBy(), getSortDirection().toUpperCase());
    }

    /**
     * 获取查询条件摘要（用于日志）
     */
    public String getQuerySummary() {
        StringBuilder sb = new StringBuilder();

        if (hasProductCode()) {
            sb.append("产品编码: ").append(productCode).append(", ");
        }

        if (hasProductName()) {
            sb.append("产品名称: ").append(productName).append(", ");
        }

        if (categoryId != null) {
            sb.append("分类ID: ").append(categoryId).append(", ");
        }

        if (productType != null) {
            sb.append("产品类型: ").append(productType).append(", ");
        }

        if (status != null) {
            sb.append("状态: ").append(status).append(", ");
        }

        if (isRecommended != null) {
            sb.append("推荐: ").append(isRecommended).append(", ");
        }

        if (hasPriceRange()) {
            sb.append("价格范围: ").append(minPrice).append("-").append(maxPrice).append(", ");
        }

        if (hasKeyword()) {
            sb.append("关键字: ").append(keyword).append(", ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // 移除最后的", "
        }

        return sb.toString();
    }
}