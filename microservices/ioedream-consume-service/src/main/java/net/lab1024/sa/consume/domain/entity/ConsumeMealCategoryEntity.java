package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费餐次分类实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_meal_category")
@Schema(description = "消费餐次分类实体")
public class ConsumeMealCategoryEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @TableField("category_code")
    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    @Schema(description = "分类编码", example = "BREAKFAST")
    private String categoryCode;

    @TableField("category_name")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    @Schema(description = "分类名称", example = "早餐")
    private String categoryName;

    @TableField("parent_id")
    @Schema(description = "父分类ID", example = "0")
    private Long parentId;

    @TableField("category_level")
    @NotNull(message = "分类层级不能为空")
    @Min(value = 1, message = "分类层级不能小于1")
    @Max(value = 3, message = "分类层级不能大于3")
    @Schema(description = "分类层级", example = "1")
    private Integer categoryLevel;

    @TableField("sort_order")
    @NotNull(message = "排序号不能为空")
    @Min(value = 1, message = "排序号不能小于1")
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @TableField("category_icon")
    @Size(max = 255, message = "分类图标长度不能超过255个字符")
    @Schema(description = "分类图标", example = "breakfast-icon.png")
    private String categoryIcon;

    @TableField("category_color")
    @Size(max = 20, message = "分类颜色长度不能超过20个字符")
    @Schema(description = "分类颜色", example = "#FF6B6B")
    private String categoryColor;

    @TableField("is_system")
    @NotNull(message = "是否系统预设不能为空")
    @Schema(description = "是否系统预设", example = "1")
    private Integer isSystem;

    @TableField("category_status")
    @NotNull(message = "分类状态不能为空")
    @Min(value = 0, message = "分类状态值无效")
    @Max(value = 1, message = "分类状态值无效")
    @Schema(description = "分类状态", example = "1")
    private Integer categoryStatus;

    @TableField(exist = false)
    @Schema(description = "分类状态名称", example = "启用")
    private String categoryStatusName;

    // 扩展字段，为Service层提供支持
    @TableField(exist = false)
    @Schema(description = "基础价格", example = "15.00")
    private BigDecimal basePrice;

    @TableField(exist = false)
    @Schema(description = "员工价格", example = "12.00")
    private BigDecimal staffPrice;

    @TableField(exist = false)
    @Schema(description = "学生价格", example = "8.00")
    private BigDecimal studentPrice;

    @TableField(exist = false)
    @Schema(description = "是否默认", example = "0")
    private Integer isDefault;

    @TableField("available_time_periods")
    @Schema(description = "可用时间段", example = "[\"06:00-10:00\", \"18:00-20:00\"]")
    private String availableTimePeriods;

    @TableField("category_description")
    @Size(max = 500, message = "分类描述长度不能超过500个字符")
    @Schema(description = "分类描述", example = "早餐分类，包含各类早餐食品")
    private String description;

    @TableField("remark")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "早餐分类")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    @Version
    @TableField("version")
    @Schema(description = "版本号", example = "1")
    private Integer version;

    // 临时字段，用于树结构
    @TableField(exist = false)
    @Schema(description = "子分类列表")
    private List<ConsumeMealCategoryEntity> children;

    // ==================== 业务状态方法 ====================

    /**
     * 检查是否为根分类
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 检查是否为系统预设
     */
    public boolean isSystem() {
        return Integer.valueOf(1).equals(isSystem);
    }

    /**
     * 设置是否为系统预设
     */
    public void setIsSystem(Integer isSystem) {
        this.isSystem = isSystem;
    }

    /**
     * 检查是否为自定义分类
     */
    public boolean isCustom() {
        return Integer.valueOf(0).equals(isSystem);
    }

    /**
     * 检查是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(categoryStatus);
    }

    /**
     * 检查是否禁用
     */
    public boolean isDisabled() {
        return Integer.valueOf(0).equals(categoryStatus);
    }

    /**
     * 检查是否有子分类
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 获取分类全路径
     */
    public String getFullPath() {
        if (isRoot()) {
            return categoryName;
        }
        return "父分类 > " + categoryName;
    }

    /**
     * 检查层级是否有效
     */
    public boolean isValidLevel() {
        return categoryLevel != null && categoryLevel >= 1 && categoryLevel <= 3;
    }

    /**
     * 检查排序号是否有效
     */
    public boolean isValidSortOrder() {
        return sortOrder != null && sortOrder > 0;
    }

    /**
     * 检查分类是否可以删除
     */
    public boolean canDelete() {
        return isCustom() && !hasChildren();
    }

    /**
     * 检查分类是否可以编辑
     */
    public boolean canEdit() {
        return !isSystem() || (isSystem() && !hasChildren());
    }

    /**
     * 检查是否可以添加子分类
     */
    public boolean canAddChildren() {
        return isEnabled() && categoryLevel < 3;
    }

    /**
     * 获取下一个层级
     */
    public Integer getNextLevel() {
        return categoryLevel != null ? categoryLevel + 1 : 1;
    }

    /**
     * 检查是否在当前时间可用
     */
    public boolean isAvailableAtTime(LocalDateTime currentTime) {
        if (currentTime == null) {
            return false;
        }

        // 检查状态
        if (!isEnabled()) {
            return false;
        }

        // 检查时间段
        if (availableTimePeriods == null || availableTimePeriods.trim().isEmpty()) {
            return true; // 没有时间限制，一直可用
        }

        // 简化实现：假设时间段为HH:mm格式
        try {
            String[] periods = availableTimePeriods.split(",");
            for (String period : periods) {
                period = period.trim().replace("[", "").replace("]", "").replace("\"", "");
                if (isTimeInRange(currentTime.toLocalTime(), period)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 解析失败时默认可用，避免影响业务
        }

        return false;
    }

    /**
     * 检查时间是否在指定范围内
     */
    private boolean isTimeInRange(java.time.LocalTime currentTime, String timeRange) {
        try {
            String[] times = timeRange.split("-");
            if (times.length != 2) {
                return false;
            }

            java.time.LocalTime startTime = java.time.LocalTime.parse(times[0].trim());
            java.time.LocalTime endTime = java.time.LocalTime.parse(times[1].trim());

            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证业务规则
     */
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 检查必填字段
        if (categoryCode == null || categoryCode.trim().isEmpty()) {
            errors.add("分类编码不能为空");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            errors.add("分类名称不能为空");
        }

        // 检查编码格式（只允许字母、数字、下划线）
        if (categoryCode != null && !categoryCode.matches("^[A-Z0-9_]+$")) {
            errors.add("分类编码只能包含大写字母、数字和下划线");
        }

        // 检查层级
        if (!isValidLevel()) {
            errors.add("分类层级必须在1-3之间");
        }

        // 检查排序号
        if (!isValidSortOrder()) {
            errors.add("排序号必须大于0");
        }

        // 检查颜色格式（必须是十六进制颜色）
        if (categoryColor != null && !categoryColor.matches("^#[0-9A-Fa-f]{6}$")) {
            errors.add("分类颜色格式不正确，应为十六进制格式如#FF6B6B");
        }

        return errors;
    }

    /**
     * 检查父子关系是否合理
     */
    public boolean isValidParentRelation(ConsumeMealCategoryEntity parent) {
        if (isRoot()) {
            return true; // 根分类不需要父分类验证
        }

        if (parent == null) {
            return false; // 有父分类ID但父分类不存在
        }

        // 检查父分类状态
        if (!parent.isEnabled()) {
            return false;
        }

        // 检查层级关系
        if (categoryLevel != null && parent.getCategoryLevel() != null) {
            return categoryLevel.equals(parent.getCategoryLevel() + 1);
        }

        return true;
    }

    /**
     * 获取分类深度描述
     */
    public String getLevelDescription() {
        if (categoryLevel == null) {
            return "未知层级";
        }
        switch (categoryLevel) {
            case 1:
                return "一级分类";
            case 2:
                return "二级分类";
            case 3:
                return "三级分类";
            default:
                return "异常层级";
        }
    }

    /**
     * 获取类型描述
     */
    public String getTypeDescription() {
        if (isSystem()) {
            return "系统预设";
        } else {
            return "自定义";
        }
    }

    /**
     * 获取操作权限描述
     */
    public String getOperationPermission() {
        if (isSystem()) {
            return "只读";
        } else {
            return "可编辑";
        }
    }

    /**
     * 检查是否可以移动到指定父分类下
     */
    public boolean canMoveTo(Long newParentId) {
        // 不能移动到自己下
        if (newParentId != null && newParentId.equals(categoryId)) {
            return false;
        }

        // 系统预设分类的移动限制
        if (isSystem() && newParentId != null) {
            return false;
        }

        return true;
    }

    /**
     * 获取排序建议
     */
    public Integer getSortSuggestion() {
        return sortOrder != null ? sortOrder : 1;
    }

    /**
     * 检查分类是否完整
     */
    public boolean isComplete() {
        return categoryCode != null && !categoryCode.trim().isEmpty() &&
                categoryName != null && !categoryName.trim().isEmpty() &&
                categoryLevel != null &&
                sortOrder != null &&
                categoryStatus != null &&
                isSystem != null;
    }

    /**
     * 获取显示名称（带层级标识）
     */
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();

        // 添加层级缩进
        if (categoryLevel != null && categoryLevel > 1) {
            for (int i = 1; i < categoryLevel; i++) {
                sb.append("　　");
            }
        }

        sb.append(categoryName);

        // 添加系统标识
        if (isSystem()) {
            sb.append(" (系统)");
        }

        return sb.toString();
    }

    // ==================== 兼容性方法（为Service层提供支持）====================

    /**
     * 获取状态（兼容性方法）
     */
    public Integer getStatus() {
        return this.categoryStatus;
    }

    /**
     * 设置状态（兼容性方法）
     */
    public void setStatus(int status) {
        this.categoryStatus = status;
    }

    /**
     * 设置状态（兼容性方法，Integer类型）
     */
    public void setStatus(Integer status) {
        this.categoryStatus = status;
    }

    /**
     * 获取基础价格（兼容性方法）
     */
    public BigDecimal getBasePrice() {
        return this.basePrice;
    }

    /**
     * 设置基础价格（兼容性方法）
     */
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    /**
     * 获取员工价格（兼容性方法）
     */
    public BigDecimal getStaffPrice() {
        return this.staffPrice;
    }

    /**
     * 设置员工价格（兼容性方法）
     */
    public void setStaffPrice(BigDecimal staffPrice) {
        this.staffPrice = staffPrice;
    }

    /**
     * 获取学生价格（兼容性方法）
     */
    public BigDecimal getStudentPrice() {
        return this.studentPrice;
    }

    /**
     * 设置学生价格（兼容性方法）
     */
    public void setStudentPrice(BigDecimal studentPrice) {
        this.studentPrice = studentPrice;
    }

    /**
     * 获取是否默认（兼容性方法）
     */
    public Integer getIsDefault() {
        return this.isDefault;
    }

    /**
     * 设置是否默认（兼容性方法）
     */
    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 设置是否默认（兼容性方法，Integer类型）
     */
    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 获取版本号（兼容性方法）
     */
    public Integer getVersion() {
        return this.version;
    }

    /**
     * 获取描述（兼容性方法）
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置描述（兼容性方法）
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取最大消费金额限制（兼容性方法）
     */
    public java.math.BigDecimal getMaxAmountLimit() {
        return null; // 根据实际需求实现
    }

    /**
     * 设置最大消费金额限制（兼容性方法）
     */
    public void setMaxAmountLimit(java.math.BigDecimal maxAmountLimit) {
        // 临时字段，可根据实际需求存储到扩展字段中
    }

    /**
     * 获取最小消费金额限制（兼容性方法）
     */
    public java.math.BigDecimal getMinAmountLimit() {
        return null; // 根据实际需求实现
    }

    /**
     * 设置最小消费金额限制（兼容性方法）
     */
    public void setMinAmountLimit(java.math.BigDecimal minAmountLimit) {
        // 临时字段，可根据实际需求存储到扩展字段中
    }

    /**
     * 获取每日消费次数限制（兼容性方法）
     */
    public Integer getDailyLimitCount() {
        return null; // 根据实际需求实现
    }

    /**
     * 设置每日消费次数限制（兼容性方法）
     */
    public void setDailyLimitCount(Integer dailyLimitCount) {
        // 临时字段，可根据实际需求存储到扩展字段中
    }

    /**
     * 获取是否允许折扣（兼容性方法）
     */
    public Integer getAllowDiscount() {
        return null; // 根据实际需求实现
    }

    /**
     * 设置是否允许折扣（兼容性方法）
     */
    public void setAllowDiscount(Integer allowDiscount) {
        // 临时字段，可根据实际需求存储到扩展字段中
    }

    /**
     * 获取折扣比例（兼容性方法）
     */
    public java.math.BigDecimal getDiscountRate() {
        return null; // 根据实际需求实现
    }

    /**
     * 设置折扣比例（兼容性方法）
     */
    public void setDiscountRate(java.math.BigDecimal discountRate) {
        // 临时字段，可根据实际需求存储到扩展字段中
    }
}
