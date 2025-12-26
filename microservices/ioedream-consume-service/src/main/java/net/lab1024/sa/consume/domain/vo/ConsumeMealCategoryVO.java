package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费餐次分类视图对象
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段定义
 * - 层级结构支持
 * - 业务状态判断方法
 * - 时间段验证方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费餐次分类信息")
public class ConsumeMealCategoryVO {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "分类名称", example = "主食")
    private String categoryName;

    @Schema(description = "分类编码", example = "MAIN_FOOD")
    private String categoryCode;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

    @Schema(description = "分类层级", example = "1")
    private Integer categoryLevel;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类图标", example = "🍚")
    private String categoryIcon;

    @Schema(description = "分类颜色", example = "#FF6B6B")
    private String categoryColor;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "是否系统预设", example = "0")
    private Integer isSystem;

    @Schema(description = "描述信息", example = "主食类餐次，包括米饭、面条等")
    private String description;

    @Schema(description = "适用时间段", example = "[\"06:00-10:00\", \"11:00-14:00\", \"17:00-20:00\"]")
    private String availableTimePeriods;

    @Schema(description = "最大消费金额限制", example = "50.00")
    private BigDecimal maxAmountLimit;

    @Schema(description = "最小消费金额限制", example = "1.00")
    private BigDecimal minAmountLimit;

    @Schema(description = "每日消费次数限制", example = "3")
    private Integer dailyLimitCount;

    @Schema(description = "是否允许折扣", example = "1")
    private Integer allowDiscount;

    @Schema(description = "折扣比例", example = "0.9")
    private BigDecimal discountRate;

    @Schema(description = "分类路径", example = "餐饮 > 主食")
    private String categoryPath;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    @Schema(description = "创建时间", example = "2025-12-21T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T00:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "子分类列表", example = "[]")
    private List<ConsumeMealCategoryVO> children;

    @Schema(description = "消费记录数", example = "156")
    private Long consumptionCount;

    @Schema(description = "总消费金额", example = "3125.50")
    private BigDecimal totalConsumptionAmount;

    @Schema(description = "今日消费次数", example = "12")
    private Long todayConsumptionCount;

    @Schema(description = "今日消费金额", example = "180.00")
    private BigDecimal todayConsumptionAmount;

    // ==================== 业务状态判断方法 ====================

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断是否禁用
     */
    public boolean isDisabled() {
        return status != null && status == 0;
    }

    /**
     * 判断是否为根分类
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 判断是否为系统预设
     */
    public boolean isSystem() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 判断是否允许折扣
     */
    public boolean canDiscount() {
        return allowDiscount != null && allowDiscount == 1 && discountRate != null
                && discountRate.compareTo(BigDecimal.ZERO) > 0 && discountRate.compareTo(BigDecimal.ONE) < 1;
    }

    /**
     * 判断是否在指定时间可用
     */
    public boolean isAvailableAtTime(String currentTime) {
        if (!isEnabled() || availableTimePeriods == null || availableTimePeriods.trim().isEmpty()) {
            return true;
        }

        try {
            // 简单的时间段检查逻辑
            String[] timePeriods = availableTimePeriods.split(",");
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
            return true; // 解析失败时默认可用
        }
    }

    /**
     * 检查消费金额是否在限制范围内
     */
    public boolean isAmountInRange(BigDecimal amount) {
        if (amount == null)
            return false;

        if (minAmountLimit != null && amount.compareTo(minAmountLimit) < 0) {
            return false;
        }

        if (maxAmountLimit != null && amount.compareTo(maxAmountLimit) > 0) {
            return false;
        }

        return true;
    }

    /**
     * 计算折扣后金额
     */
    public BigDecimal calculateDiscountedAmount(BigDecimal originalAmount) {
        if (originalAmount == null || !canDiscount()) {
            return originalAmount;
        }

        return originalAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 检查是否超出每日限制
     */
    public boolean isExceedDailyLimit(int currentCount) {
        return dailyLimitCount != null && currentCount >= dailyLimitCount;
    }

    /**
     * 获取折扣比例
     */
    public BigDecimal getDiscountPercentage() {
        if (!canDiscount() || discountRate == null) {
            return BigDecimal.ZERO;
        }
        return discountRate.multiply(new BigDecimal("100")).subtract(discountRate.multiply(new BigDecimal("100")));
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
            java.time.LocalTime current = java.time.LocalTime.parse(currentTime);
            java.time.LocalTime start = java.time.LocalTime.parse(startTime);
            java.time.LocalTime end = java.time.LocalTime.parse(endTime);
            return !current.isBefore(start) && !current.isAfter(end);
        } catch (Exception e) {
            return false;
        }
    }
}
