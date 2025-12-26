package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消费补贴实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_subsidy")
@Schema(description = "消费补贴实体")
public class ConsumeSubsidyEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "补贴ID", example = "1")
    private Long subsidyId;

    @TableField("subsidy_code")
    @NotBlank(message = "补贴编码不能为空")
    @Size(max = 50, message = "补贴编码长度不能超过50个字符")
    @Schema(description = "补贴编码", example = "SUBSIDY_001")
    private String subsidyCode;

    @TableField("subsidy_name")
    @NotBlank(message = "补贴名称不能为空")
    @Size(max = 100, message = "补贴名称长度不能超过100个字符")
    @Schema(description = "补贴名称", example = "12月餐饮补贴")
    private String subsidyName;

    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @TableField(exist = false)
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @TableField("subsidy_type")
    @NotNull(message = "补贴类型不能为空")
    @Min(value = 1, message = "补贴类型值无效")
    @Max(value = 10, message = "补贴类型值无效")
    @Schema(description = "补贴类型", example = "1")
    private Integer subsidyType;

    @TableField(exist = false)
    @Schema(description = "补贴类型名称", example = "餐饮补贴")
    private String subsidyTypeName;

    @TableField("subsidy_period")
    @NotNull(message = "补贴周期不能为空")
    @Min(value = 1, message = "补贴周期值无效")
    @Max(value = 5, message = "补贴周期值无效")
    @Schema(description = "补贴周期", example = "1")
    private Integer subsidyPeriod;

    @TableField(exist = false)
    @Schema(description = "补贴周期名称", example = "月度")
    private String subsidyPeriodName;

    @TableField("subsidy_amount")
    @NotNull(message = "补贴金额不能为空")
    @DecimalMin(value = "0.01", message = "补贴金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "补贴金额格式不正确")
    @Schema(description = "补贴金额", example = "300.00")
    private BigDecimal subsidyAmount;

    @TableField("used_amount")
    @DecimalMin(value = "0", message = "已使用金额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "已使用金额格式不正确")
    @Schema(description = "已使用金额", example = "120.50")
    private BigDecimal usedAmount;

    @TableField(exist = false)
    @Schema(description = "剩余金额", example = "179.50")
    private BigDecimal remainingAmount;

    @TableField("subsidy_status")
    @NotNull(message = "补贴状态不能为空")
    @Min(value = 1, message = "补贴状态值无效")
    @Max(value = 5, message = "补贴状态值无效")
    @Schema(description = "补贴状态", example = "2")
    private Integer subsidyStatus;

    @TableField(exist = false)
    @Schema(description = "补贴状态名称", example = "已发放")
    private String subsidyStatusName;

    @TableField("effective_date")
    @Schema(description = "生效日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveDate;

    @TableField("expiry_date")
    @Schema(description = "过期日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiryDate;

    @TableField("issue_date")
    @Schema(description = "发放日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueDate;

    @TableField("issuer_id")
    @Schema(description = "发放人ID", example = "1")
    private Long issuerId;

    @TableField(exist = false)
    @Schema(description = "发放人姓名", example = "人力资源部")
    private String issuerName;

    @TableField("applicable_merchants")
    @Schema(description = "适用商户", example = "[\"食堂\", \"餐厅\"]")
    private String applicableMerchants;

    @TableField("usage_time_periods")
    @Schema(description = "使用时间段", example = "[\"07:00-20:00\"]")
    private String usageTimePeriods;

    @TableField("daily_limit")
    @DecimalMin(value = "0", message = "每日限额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "每日限额格式不正确")
    @Schema(description = "每日限额", example = "50.00")
    private BigDecimal dailyLimit;

    @TableField("daily_used_amount")
    @DecimalMin(value = "0", message = "每日已使用金额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "每日已使用金额格式不正确")
    @Schema(description = "每日已使用金额", example = "25.00")
    private BigDecimal dailyUsedAmount;

    @TableField("daily_usage_date")
    @Schema(description = "每日使用日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dailyUsageDate;

    @TableField("max_discount_rate")
    @DecimalMin(value = "0", message = "最大折扣率不能为负数")
    @DecimalMax(value = "1", message = "最大折扣率不能超过1")
    @Digits(integer = 3, fraction = 2, message = "最大折扣率格式不正确")
    @Schema(description = "最大折扣率", example = "1.00")
    private BigDecimal maxDiscountRate;

    @TableField("auto_renew")
    @NotNull(message = "是否自动续期不能为空")
    @Schema(description = "是否自动续期", example = "0")
    private Integer autoRenew;

    @TableField("renewal_rule")
    @Schema(description = "续期规则", example = "{}")
    private String renewalRule;

    @TableField("remark")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "12月员工餐饮补贴")
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

    // ==================== 业务状态方法 ====================

    /**
     * 检查补贴是否待发放
     */
    public boolean isPending() {
        return Integer.valueOf(1).equals(subsidyStatus);
    }

    /**
     * 检查补贴是否已发放
     */
    public boolean isIssued() {
        return Integer.valueOf(2).equals(subsidyStatus);
    }

    /**
     * 检查补贴是否已过期
     */
    public boolean isExpired() {
        return Integer.valueOf(3).equals(subsidyStatus);
    }

    /**
     * 检查补贴是否已使用
     */
    public boolean isUsed() {
        return Integer.valueOf(4).equals(subsidyStatus);
    }

    /**
     * 检查补贴是否已作废
     */
    public boolean isCancelled() {
        return Integer.valueOf(5).equals(subsidyStatus);
    }

    /**
     * 检查补贴是否生效(已发放且未过期)
     */
    public boolean isActive() {
        return isIssued() && !isExpired() && !isUsed() && !isCancelled();
    }

    /**
     * 检查补贴是否可用
     */
    public boolean isUsable() {
        LocalDateTime now = LocalDateTime.now();
        return isIssued() &&
               (effectiveDate == null || !now.isBefore(effectiveDate)) &&
               (expiryDate == null || !now.isAfter(expiryDate));
    }

    /**
     * 检查是否有剩余金额
     */
    public boolean hasRemaining() {
        BigDecimal remaining = getRemainingAmount();
        return remaining != null && remaining.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否有每日限额
     */
    public boolean hasDailyLimit() {
        return dailyLimit != null && dailyLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取剩余金额
     */
    public BigDecimal getRemainingAmount() {
        if (subsidyAmount == null || usedAmount == null) {
            return subsidyAmount != null ? subsidyAmount : BigDecimal.ZERO;
        }
        return subsidyAmount.subtract(usedAmount);
    }

    /**
     * 获取使用率
     */
    public BigDecimal getUsagePercentage() {
        if (subsidyAmount == null || subsidyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = usedAmount != null ? usedAmount : BigDecimal.ZERO;
        return used.divide(subsidyAmount, 4, BigDecimal.ROUND_HALF_UP)
                 .multiply(new BigDecimal("100"));
    }

    /**
     * 获取每日剩余限额
     */
    public BigDecimal getDailyRemainingLimit() {
        if (!hasDailyLimit()) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        BigDecimal dailyUsed = dailyUsedAmount != null ? dailyUsedAmount : BigDecimal.ZERO;
        return dailyLimit.subtract(dailyUsed).max(BigDecimal.ZERO);
    }

    /**
     * 检查今日是否达到限额
     */
    public boolean isDailyLimitReached() {
        if (!hasDailyLimit()) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (dailyUsageDate != null && dailyUsageDate.equals(today)) {
            BigDecimal dailyUsed = dailyUsedAmount != null ? dailyUsedAmount : BigDecimal.ZERO;
            return dailyUsed.compareTo(dailyLimit) >= 0;
        }

        return false;
    }

    /**
     * 检查补贴是否即将过期
     */
    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(days);

        return expiryDate.isBefore(threshold) && expiryDate.isAfter(now);
    }

    /**
     * 检查补贴是否可以删除
     */
    public boolean canDelete() {
        // 只有待发放状态的补贴才可以删除
        return isPending();
    }

    /**
     * 检查补贴是否即将用完
     */
    public boolean isNearlyDepleted(BigDecimal usageRateThreshold) {
        if (!hasRemaining()) {
            return false;
        }

        BigDecimal currentUsageRate = getUsagePercentage();
        return currentUsageRate.compareTo(usageRateThreshold.multiply(new BigDecimal("100"))) >= 0;
    }

    /**
     * 验证业务规则
     */
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 检查必填字段
        if (subsidyCode == null || subsidyCode.trim().isEmpty()) {
            errors.add("补贴编码不能为空");
        }
        if (subsidyName == null || subsidyName.trim().isEmpty()) {
            errors.add("补贴名称不能为空");
        }
        if (userId == null) {
            errors.add("用户ID不能为空");
        }
        if (subsidyAmount == null || subsidyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("补贴金额必须大于0");
        }

        // 检查日期逻辑
        if (effectiveDate != null && expiryDate != null && effectiveDate.isAfter(expiryDate)) {
            errors.add("生效日期不能晚于过期日期");
        }

        // 检查金额逻辑
        if (usedAmount != null && subsidyAmount != null && usedAmount.compareTo(subsidyAmount) > 0) {
            errors.add("已使用金额不能大于补贴金额");
        }

        // 检查每日限额逻辑
        if (dailyUsedAmount != null && dailyLimit != null && dailyUsedAmount.compareTo(dailyLimit) > 0) {
            errors.add("每日已使用金额不能大于每日限额");
        }

        // 检查折扣率
        if (maxDiscountRate != null && (maxDiscountRate.compareTo(BigDecimal.ZERO) < 0 || maxDiscountRate.compareTo(BigDecimal.ONE) > 0)) {
            errors.add("最大折扣率必须在0-1之间");
        }

        return errors;
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        switch (subsidyStatus) {
            case 1:
                return "待发放";
            case 2:
                return isUsable() ? "可用" : "已过期";
            case 3:
                return "已过期";
            case 4:
                return "已使用";
            case 5:
                return "已作废";
            default:
                return "未知状态";
        }
    }

    /**
     * 获取紧急程度
     */
    public String getUrgencyLevel() {
        if (isExpired()) {
            return "已过期";
        } else if (isExpiringSoon(3)) {
            return "即将过期";
        } else if (isNearlyDepleted(new BigDecimal("0.8"))) {
            return "即将用完";
        } else if (!hasRemaining()) {
            return "已用完";
        } else {
            return "正常";
        }
    }

    /**
     * 计算可用金额
     */
    public BigDecimal calculateAvailableAmount(BigDecimal requestedAmount) {
        if (!isUsable() || requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal availableByRemaining = getRemainingAmount();
        if (availableByRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 检查每日限额
        BigDecimal availableByDaily = requestedAmount;
        if (hasDailyLimit()) {
            BigDecimal dailyRemaining = getDailyRemainingLimit();
            availableByDaily = requestedAmount.min(dailyRemaining);
        }

        return availableByRemaining.min(availableByDaily);
    }

    /**
     * 检查是否可以作废
     */
    public boolean canCancel() {
        return isPending() || (isIssued() && !hasRemaining());
    }

    /**
     * 检查是否可以延期
     */
    public boolean canExtend() {
        return isIssued() && expiryDate != null && !isExpired();
    }

    /**
     * 获取补贴类型描述
     */
    public String getSubsidyTypeDescription() {
        switch (subsidyType) {
            case 1:
                return "餐饮补贴";
            case 2:
                return "交通补贴";
            case 3:
                return "通讯补贴";
            case 4:
                return "住宿补贴";
            case 5:
                return "其他补贴";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取补贴周期描述
     */
    public String getSubsidyPeriodDescription() {
        switch (subsidyPeriod) {
            case 1:
                return "一次性";
            case 2:
                return "日度";
            case 3:
                return "周度";
            case 4:
                return "月度";
            case 5:
                return "年度";
            default:
                return "未知周期";
        }
    }
}