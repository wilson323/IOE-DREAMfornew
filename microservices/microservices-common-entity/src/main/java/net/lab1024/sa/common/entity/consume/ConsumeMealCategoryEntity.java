package net.lab1024.sa.common.entity.consume;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费餐次分类实体类
 * <p>
 * 管理消费餐次分类信息，支持树形层级结构
 * 提供早餐、午餐、晚餐等餐次管理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 餐次分类管理（早餐/午餐/晚餐）
 * - 树形层级结构（支持子分类）
 * - 时间段配置
 * - 默认餐次设置
 * - 系统预设分类
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_meal_category")
@Schema(description = "消费餐次分类实体")
public class ConsumeMealCategoryEntity extends BaseEntity {

    /**
     * 分类ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 分类编码（唯一）
     */
    @Schema(description = "分类编码")
    private String categoryCode;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    private String categoryName;

    /**
     * 父分类ID（null表示根分类）
     */
    @Schema(description = "父分类ID")
    private Long parentId;

    /**
     * 父分类名称（冗余字段）
     */
    @Schema(description = "父分类名称")
    private String parentName;

    /**
     * 分类层级（1-一级分类 2-二级分类 3-三级分类）
     */
    @Schema(description = "分类层级（1-一级分类 2-二级分类 3-三级分类）")
    private Integer categoryLevel;

    /**
     * 分类路径（如：1/2/3，用于快速查询层级路径）
     */
    @Schema(description = "分类路径")
    private String categoryPath;

    /**
     * 排序号（同级分类中排序）
     */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
     * 分类图标
     */
    @Schema(description = "分类图标")
    private String icon;

    /**
     * 分类颜色
     */
    @Schema(description = "分类颜色")
    private String color;

    /**
     * 分类状态（0-禁用 1-启用 2-已删除）
     */
    @Schema(description = "分类状态（0-禁用 1-启用 2-已删除）")
    private Integer status;

    /**
     * 是否默认分类（0-否 1-是）
     */
    @Schema(description = "是否默认分类")
    private Integer isDefault;

    /**
     * 是否系统预设（0-否 1-是，系统预设分类不可删除）
     */
    @Schema(description = "是否系统预设")
    private Integer isSystem;

    /**
     * 餐别类型（1-早餐 2-午餐 3-晚餐 4-夜宵 5-全天）
     */
    @Schema(description = "餐别类型（1-早餐 2-午餐 3-晚餐 4-夜宵 5-全天）")
    private Integer mealType;

    /**
     * 餐别类型名称
     */
    @Schema(description = "餐别类型名称")
    private String mealTypeName;

    /**
     * 早餐开始时间
     */
    @Schema(description = "早餐开始时间")
    private LocalTime breakfastStartTime;

    /**
     * 早餐结束时间
     */
    @Schema(description = "早餐结束时间")
    private LocalTime breakfastEndTime;

    /**
     * 午餐开始时间
     */
    @Schema(description = "午餐开始时间")
    private LocalTime lunchStartTime;

    /**
     * 午餐结束时间
     */
    @Schema(description = "午餐结束时间")
    private LocalTime lunchEndTime;

    /**
     * 晚餐开始时间
     */
    @Schema(description = "晚餐开始时间")
    private LocalTime dinnerStartTime;

    /**
     * 晚餐结束时间
     */
    @Schema(description = "晚餐结束时间")
    private LocalTime dinnerEndTime;

    /**
     * 夜宵开始时间
     */
    @Schema(description = "夜宵开始时间")
    private LocalTime snackStartTime;

    /**
     * 夜宵结束时间
     */
    @Schema(description = "夜宵结束时间")
    private LocalTime snackEndTime;

    /**
     * 是否启用日期限制（0-否 1-是）
     */
    @Schema(description = "是否启用日期限制")
    private Integer enableDateLimit;

    /**
     * 生效日期开始
     */
    @Schema(description = "生效日期开始")
    private LocalDateTime effectiveStartDate;

    /**
     * 生效日期结束
     */
    @Schema(description = "生效日期结束")
    private LocalDateTime effectiveEndDate;

    /**
     * 星期限制（JSON数组，如[1,2,3,4,5]表示周一到周五）
     */
    @Schema(description = "星期限制（JSON数组）")
    private String weekdays;

    /**
     * 优惠折扣（0-1之间的小数，如0.9表示9折）
     */
    @Schema(description = "优惠折扣")
    private String discountRate;

    /**
     * 补贴比例（0-1之间的小数，如0.5表示补贴50%）
     */
    @Schema(description = "补贴比例")
    private String subsidyRate;

    /**
     * 分类描述
     */
    @Schema(description = "分类描述")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否根分类
     *
     * @return true-根分类，false-子分类
     */
    public boolean isRootCategory() {
        return parentId == null || parentId == 0;
    }

    /**
     * 检查是否默认分类
     *
     * @return true-默认分类，false-非默认
     */
    public boolean isDefaultCategory() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 检查是否系统预设
     *
     * @return true-系统预设，false-用户自定义
     */
    public boolean isSystemCategory() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 检查当前时间是否在餐次时间范围内
     *
     * @param currentTime 当前时间
     * @return true-在时间范围内，false-不在范围内
     */
    public boolean isWithinMealTime(LocalTime currentTime) {
        if (currentTime == null || mealType == null) {
            return false;
        }

        switch (mealType) {
            case 1: // 早餐
                return isInTimeRange(currentTime, breakfastStartTime, breakfastEndTime);
            case 2: // 午餐
                return isInTimeRange(currentTime, lunchStartTime, lunchEndTime);
            case 3: // 晚餐
                return isInTimeRange(currentTime, dinnerStartTime, dinnerEndTime);
            case 4: // 夜宵
                return isInTimeRange(currentTime, snackStartTime, snackEndTime);
            case 5: // 全天
                return true;
            default:
                return false;
        }
    }

    /**
     * 检查时间是否在指定范围内
     *
     * @param time 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return true-在范围内，false-不在范围内
     */
    private boolean isInTimeRange(LocalTime time, LocalTime startTime, LocalTime endTime) {
        if (time == null || startTime == null || endTime == null) {
            return false;
        }

        if (startTime.isBefore(endTime)) {
            // 正常时间范围（如08:00-09:00）
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        } else {
            // 跨天时间范围（如22:00-06:00）
            return !time.isBefore(startTime) || !time.isAfter(endTime);
        }
    }

    /**
     * 检查是否在生效日期范围内
     *
     * @param currentDate 当前日期
     * @return true-在范围内，false-不在范围内
     */
    public boolean isWithinEffectiveDate(LocalDateTime currentDate) {
        if (currentDate == null) {
            return false;
        }

        if (effectiveStartDate != null && currentDate.isBefore(effectiveStartDate)) {
            return false;
        }

        if (effectiveEndDate != null && currentDate.isAfter(effectiveEndDate)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否匹配星期限制
     *
     * @param currentWeekday 当前星期（1-7，1=星期一）
     * @return true-匹配，false-不匹配
     */
    public boolean matchesWeekday(int currentWeekday) {
        if (weekdays == null || weekdays.isEmpty()) {
            return true; // 未设置星期限制，默认每天生效
        }

        try {
            // weekdays格式为JSON数组，如[1,2,3,4,5]
            String[] weekdayArray = weekdays.replaceAll("[\\[\\]\"]", "").split(",");
            for (String day : weekdayArray) {
                if (day.trim().equals(String.valueOf(currentWeekday))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 解析失败，返回true
        }

        return false;
    }

    /**
     * 获取餐别类型名称
     *
     * @return 餐别类型名称
     */
    public String getMealTypeDisplayName() {
        if (mealType == null) {
            return "未知";
        }
        switch (mealType) {
            case 1: return "早餐";
            case 2: return "午餐";
            case 3: return "晚餐";
            case 4: return "夜宵";
            case 5: return "全天";
            default: return "未知";
        }
    }

    /**
     * 获取当前餐次时间范围文本
     *
     * @return 时间范围文本（如"06:00-09:00"）
     */
    public String getCurrentMealTimeRange() {
        if (mealType == null) {
            return "";
        }

        switch (mealType) {
            case 1:
                return formatTimeRange(breakfastStartTime, breakfastEndTime);
            case 2:
                return formatTimeRange(lunchStartTime, lunchEndTime);
            case 3:
                return formatTimeRange(dinnerStartTime, dinnerEndTime);
            case 4:
                return formatTimeRange(snackStartTime, snackEndTime);
            case 5:
                return "全天";
            default:
                return "";
        }
    }

    /**
     * 格式化时间范围
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 格式化后的时间范围字符串
     */
    private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return "";
        }
        return startTime.toString() + "-" + endTime.toString();
    }

    /**
     * 检查是否可删除
     *
     * @return true-可删除，false-不可删除
     */
    public boolean canDelete() {
        // 系统预设分类不可删除
        if (isSystemCategory()) {
            return false;
        }
        // 有子分类的不可删除
        return false;
    }
}
