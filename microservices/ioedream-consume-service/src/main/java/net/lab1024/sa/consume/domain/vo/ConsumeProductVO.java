package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费产品视图对象
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段定义
 * - 业务状态判断方法
 * - 价格计算方法
 * - 时间段验证方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费产品信息")
public class ConsumeProductVO {

    @Schema(description = "产品ID", example = "1")
    private Long productId;

    @Schema(description = "产品编码", example = "P001")
    private String productCode;

    @Schema(description = "产品名称", example = "宫保鸡丁套餐")
    private String productName;

    @Schema(description = "产品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "产品分类名称", example = "主食套餐")
    private String categoryName;

    @Schema(description = "产品类型", example = "2")
    private Integer productType;

    @Schema(description = "产品类型名称", example = "套餐")
    private String productTypeName;

    @Schema(description = "产品规格", example = "大份/含米饭")
    private String specification;

    @Schema(description = "产品单位", example = "份")
    private String unit;

    @Schema(description = "基础价格", example = "15.00")
    private BigDecimal basePrice;

    @Schema(description = "当前售价", example = "12.00")
    private BigDecimal salePrice;

    @Schema(description = "成本价", example = "8.00")
    private BigDecimal costPrice;

    @Schema(description = "库存数量", example = "100")
    private Integer stockQuantity;

    @Schema(description = "警戒库存", example = "10")
    private Integer warningStock;

    @Schema(description = "产品图片URL", example = "https://example.com/images/product1.jpg")
    private String imageUrl;

    @Schema(description = "产品描述", example = "经典川菜，麻辣鲜香")
    private String description;

    @Schema(description = "营养成分", example = "{\"calories\": 650, \"protein\": 25}")
    private String nutritionInfo;

    @Schema(description = "过敏原信息", example = "含花生、大豆")
    private String allergenInfo;

    @Schema(description = "是否启用推荐", example = "1")
    private Integer isRecommended;

    @Schema(description = "推荐排序", example = "1")
    private Integer recommendSort;

    @Schema(description = "销量统计", example = "1500")
    private Long salesCount;

    @Schema(description = "评分", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "评分人数", example = "120")
    private Integer ratingCount;

    @Schema(description = "产品状态", example = "1")
    private Integer status;

    @Schema(description = "产品状态名称", example = "上架")
    private String statusName;

    @Schema(description = "是否允许折扣", example = "1")
    private Integer allowDiscount;

    @Schema(description = "最大折扣比例", example = "0.8")
    private BigDecimal maxDiscountRate;

    @Schema(description = "销售时间段", example = "[\"11:00-14:00\", \"17:00-20:00\"]")
    private String saleTimePeriods;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    @Schema(description = "创建时间", example = "2025-12-21T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T00:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "折扣节省金额", example = "3.00")
    private BigDecimal discountSaving;

    @Schema(description = "利润率", example = "33.33")
    private BigDecimal profitRate;

    @Schema(description = "是否库存不足", example = "false")
    private Boolean isLowStock;

    @Schema(description = "是否有库存", example = "true")
    private Boolean hasStock;

    // ==================== 业务状态判断方法 ====================

    /**
     * 判断是否上架
     */
    public boolean isOnSale() {
        return status != null && status == 1;
    }

    /**
     * 判断是否下架
     */
    public boolean isOffSale() {
        return status != null && status == 2;
    }

    /**
     * 判断是否停产
     */
    public boolean isDiscontinued() {
        return status != null && status == 3;
    }

    /**
     * 判断是否推荐产品
     */
    public boolean isRecommended() {
        return isRecommended != null && isRecommended == 1;
    }

    /**
     * 判断是否允许折扣
     */
    public boolean canDiscount() {
        return allowDiscount != null && allowDiscount == 1
                && maxDiscountRate != null && maxDiscountRate.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否库存不足
     */
    public boolean isLowStock() {
        return warningStock != null && stockQuantity != null
                && stockQuantity.compareTo(warningStock) <= 0;
    }

    /**
     * 判断是否有库存
     */
    public boolean hasStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * 判断是否有利润
     */
    public boolean hasProfit() {
        return salePrice != null && costPrice != null
                && salePrice.compareTo(costPrice) > 0;
    }

    /**
     * 获取产品类型名称
     */
    public String getProductTypeName() {
        if (productType == null) {
            return "";
        }
        switch (productType) {
            case 1:
                return "单品";
            case 2:
                return "套餐";
            case 3:
                return "服务";
            case 4:
                return "虚拟";
            default:
                return "未知";
        }
    }

    /**
     * 获取产品状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1:
                return "上架";
            case 2:
                return "下架";
            case 3:
                return "停产";
            default:
                return "未知";
        }
    }

    /**
     * 计算利润率
     */
    public BigDecimal getProfitRate() {
        if (!hasProfit()) {
            return BigDecimal.ZERO;
        }
        return salePrice.subtract(costPrice)
                .divide(costPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算折扣节省金额
     */
    public BigDecimal getDiscountSaving() {
        if (basePrice == null || salePrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = basePrice.subtract(salePrice);
        return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO;
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
            BigDecimal maxDiscount = maxDiscountRate;
            if (maxDiscount != null && discountRate.compareTo(maxDiscount) > 0) {
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
     * 判断是否在指定时间可销售
     */
    public boolean isAvailableAtTime(String currentTime) {
        if (!isOnSale() || !hasStock()) {
            return false;
        }

        if (saleTimePeriods == null || saleTimePeriods.trim().isEmpty()) {
            return true; // 没有时间限制，一直可销售
        }

        try {
            // 简单的时间段检查逻辑
            String[] timePeriods = saleTimePeriods.split(",");
            for (String timePeriod : timePeriods) {
                String trimmed = timePeriod.trim();
                if (trimmed.contains("-")) {
                    String[] times = trimmed.split("-");
                    if (times.length == 2) {
                        String startTime = times[0].trim();
                        String endTime = times[1].trim();
                        // 简单验证时间格式
                        if (isValidTimeFormat(startTime) && isValidTimeFormat(endTime)) {
                            if (isTimeInRange(currentTime, startTime, endTime)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return true; // 解析失败时默认可销售，避免影响业务
        }
    }

    /**
     * 计算库存状态
     */
    public String getStockStatus() {
        if (!hasStock()) {
            return "无库存";
        }
        if (isLowStock()) {
            return "库存不足";
        }
        return "库存充足";
    }

    /**
     * 获取库存状态颜色
     */
    public String getStockStatusColor() {
        if (!hasStock()) {
            return "#ff4757"; // 红色
        }
        if (isLowStock()) {
            return "#ffa502"; // 橙色
        }
        return "#2ed573"; // 绿色
    }

    /**
     * 获取价格显示文本
     */
    public String getPriceDisplayText() {
        if (salePrice == null) {
            return "价格待定";
        }
        StringBuilder sb = new StringBuilder("¥").append(salePrice);
        if (basePrice != null && salePrice.compareTo(basePrice) < 0) {
            sb.append(" <del>¥").append(basePrice).append("</del>");
        }
        return sb.toString();
    }

    /**
     * 获取评分星级显示
     */
    public String getRatingStars() {
        if (rating == null) {
            return "暂无评分";
        }

        int fullStars = rating.intValue();
        boolean hasHalfStar = rating.subtract(new BigDecimal(fullStars)).compareTo(new BigDecimal("0.5")) >= 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            sb.append("★");
        }
        if (hasHalfStar && fullStars < 5) {
            sb.append("☆");
            fullStars++;
        }
        for (int i = fullStars; i < 5; i++) {
            sb.append("☆");
        }

        return sb.toString();
    }

    // ==================== 私有工具方法 ====================

    /**
     * 验证时间格式
     */
    private boolean isValidTimeFormat(String timeStr) {
        return timeStr.matches("^\\d{1,2}:\\d{2}$");
    }

    /**
     * 检查时间是否在范围内
     */
    private boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        try {
            if (currentTime.length() == 5) {
                currentTime = currentTime + ":00";
            }
            if (startTime.length() == 5) {
                startTime = startTime + ":00";
            }
            if (endTime.length() == 5) {
                endTime = endTime + ":00";
            }

            java.time.LocalTime current = java.time.LocalTime.parse(currentTime);
            java.time.LocalTime start = java.time.LocalTime.parse(startTime);
            java.time.LocalTime end = java.time.LocalTime.parse(endTime);

            return !current.isBefore(start) && !current.isAfter(end);
        } catch (Exception e) {
            return false;
        }
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
