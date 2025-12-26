package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费补贴更新表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段验证
 * - 业务规则验证
 * - 更新权限控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费补贴更新表单")
public class ConsumeSubsidyUpdateForm {

    @Schema(description = "补贴ID", example = "1")
    @NotNull(message = "补贴ID不能为空")
    private Long subsidyId;

    @Schema(description = "补贴编码", example = "SUBSIDY_001")
    @Size(max = 50, message = "补贴编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "补贴编码只能包含大写字母、数字和下划线")
    private String subsidyCode;

    @Schema(description = "补贴名称", example = "12月餐饮补贴（已更新）")
    @Size(max = 100, message = "补贴名称长度不能超过100个字符")
    private String subsidyName;

    @Schema(description = "补贴类型", example = "1")
    @Min(value = 1, message = "补贴类型值无效")
    @Max(value = 10, message = "补贴类型值无效")
    private Integer subsidyType;

    @Schema(description = "补贴周期", example = "4")
    @Min(value = 1, message = "补贴周期值无效")
    @Max(value = 5, message = "补贴周期值无效")
    private Integer subsidyPeriod;

    @Schema(description = "生效日期", example = "2025-12-01T00:00:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "过期日期", example = "2025-12-31T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "适用商户", example = "[\"食堂\", \"餐厅\", \"超市\"]")
    private String applicableMerchants;

    @Schema(description = "使用时间段", example = "[\"07:00-20:00\", \"11:30-13:30\"]")
    private String usageTimePeriods;

    @Schema(description = "每日限额", example = "60.00")
    @DecimalMin(value = "0", message = "每日限额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "每日限额格式不正确")
    private BigDecimal dailyLimit;

    @Schema(description = "最大折扣率", example = "0.95")
    @DecimalMin(value = "0", message = "最大折扣率不能为负数")
    @DecimalMax(value = "1", message = "最大折扣率不能超过1")
    @Digits(integer = 3, fraction = 2, message = "最大折扣率格式不正确")
    private BigDecimal maxDiscountRate;

    @Schema(description = "是否自动续期", example = "1")
    private Integer autoRenew;

    @Schema(description = "续期规则", example = "{\"autoExtend\": true, \"extendDays\": 30}")
    private String renewalRule;

    @Schema(description = "备注", example = "更新：增加了超市使用权限")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    @Schema(description = "版本号", example = "1")
    @NotNull(message = "版本号不能为空")
    private Integer version;

    @Schema(description = "更新原因", example = "业务需求变更")
    @Size(max = 200, message = "更新原因长度不能超过200个字符")
    private String updateReason;

    @Schema(description = "是否强制更新", example = "false")
    private Boolean forceUpdate;

    // ==================== 业务验证方法 ====================

    /**
     * 验证是否可以更新
     */
    public boolean canUpdate(Integer currentStatus, BigDecimal usedAmount) {
        if (currentStatus == null) {
            return false;
        }

        // 已使用、已过期、已作废的补贴通常不允许更新关键字段
        if (currentStatus >= 3) { // 3-已过期 4-已使用 5-已作废
            return forceUpdate != null && forceUpdate;
        }

        // 部分使用过的补贴需要谨慎更新
        if (usedAmount != null && usedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return forceUpdate != null && forceUpdate;
        }

        return true;
    }

    /**
     * 验证日期逻辑
     */
    public boolean isValidDateRange() {
        if (effectiveDate != null && expiryDate != null) {
            return effectiveDate.isBefore(expiryDate) || effectiveDate.isEqual(expiryDate);
        }
        return true; // 如果有一个或两个日期为空，由其他验证处理
    }

    /**
     * 验证每日限额合理性
     */
    public boolean isValidDailyLimit(BigDecimal subsidyAmount) {
        if (dailyLimit == null) {
            return true; // 每日限额可选
        }
        if (subsidyAmount == null) {
            return false; // 有每日限额但没有补贴金额
        }
        return dailyLimit.compareTo(BigDecimal.ZERO) >= 0 && dailyLimit.compareTo(subsidyAmount) <= 0;
    }

    /**
     * 验证折扣率合理性
     */
    public boolean isValidDiscountRate() {
        if (maxDiscountRate == null) {
            return true; // 折扣率可选
        }
        return maxDiscountRate.compareTo(BigDecimal.ZERO) >= 0 && maxDiscountRate.compareTo(BigDecimal.ONE) <= 0;
    }

    /**
     * 验证适用商户格式
     */
    public boolean isValidMerchantsFormat() {
        if (applicableMerchants == null || applicableMerchants.trim().isEmpty()) {
            return true; // 适用商户可选
        }
        try {
            // 简单验证是否为有效的JSON数组格式
            return applicableMerchants.trim().startsWith("[") && applicableMerchants.trim().endsWith("]");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证使用时间段格式
     */
    public boolean isValidTimePeriodsFormat() {
        if (usageTimePeriods == null || usageTimePeriods.trim().isEmpty()) {
            return true; // 使用时间段可选
        }
        try {
            // 简单验证是否为有效的JSON数组格式
            return usageTimePeriods.trim().startsWith("[") && usageTimePeriods.trim().endsWith("]");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证续期规则格式
     */
    public boolean isValidRenewalRuleFormat() {
        if (renewalRule == null || renewalRule.trim().isEmpty()) {
            return true; // 续期规则可选
        }
        if (autoRenew == null || autoRenew != 1) {
            return true; // 非自动续期时，续期规则可选
        }
        try {
            // 简单验证是否为有效的JSON格式
            return renewalRule.trim().startsWith("{") && renewalRule.trim().endsWith("}");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取可更新的字段列表
     */
    public java.util.List<String> getUpdatableFields(Integer currentStatus, BigDecimal usedAmount) {
        java.util.List<String> fields = new java.util.ArrayList<>();

        // 基础信息字段（通常可更新）
        if (subsidyName != null) fields.add("subsidyName");
        if (remark != null) fields.add("remark");

        // 状态相关字段（根据状态决定是否可更新）
        if (currentStatus != null && currentStatus < 3) { // 未过期
            if (applicableMerchants != null) fields.add("applicableMerchants");
            if (usageTimePeriods != null) fields.add("usageTimePeriods");
            if (dailyLimit != null) fields.add("dailyLimit");
            if (maxDiscountRate != null) fields.add("maxDiscountRate");

            // 时间字段（需要谨慎更新）
            if (effectiveDate != null) fields.add("effectiveDate");
            if (expiryDate != null) fields.add("expiryDate");

            // 续期相关字段
            if (autoRenew != null) fields.add("autoRenew");
            if (renewalRule != null) fields.add("renewalRule");
        }

        // 强制更新时允许更多字段
        if (forceUpdate != null && forceUpdate) {
            if (subsidyCode != null) fields.add("subsidyCode");
            if (subsidyType != null) fields.add("subsidyType");
            if (subsidyPeriod != null) fields.add("subsidyPeriod");
        }

        return fields;
    }

    /**
     * 获取不可更新的字段列表
     */
    public java.util.List<String> getNonUpdatableFields(Integer currentStatus, BigDecimal usedAmount) {
        java.util.List<String> nonUpdatable = new java.util.ArrayList<>();

        // 核心业务字段（通常不可更新）
        nonUpdatable.add("userId"); // 用户ID不可更改
        nonUpdatable.add("subsidyAmount"); // 金额不可更改

        // 已使用或过期的补贴限制更多字段
        if (currentStatus != null && currentStatus >= 3) {
            nonUpdatable.add("subsidyCode");
            nonUpdatable.add("subsidyType");
            nonUpdatable.add("subsidyPeriod");
            nonUpdatable.add("effectiveDate");
            nonUpdatable.add("expiryDate");
        }

        // 已部分使用的补贴限制时间字段更新
        if (usedAmount != null && usedAmount.compareTo(BigDecimal.ZERO) > 0) {
            nonUpdatable.add("effectiveDate");
            nonUpdatable.add("expiryDate");
        }

        return nonUpdatable;
    }

    /**
     * 获取更新影响的业务范围
     */
    public java.util.List<String> getUpdateImpact() {
        java.util.List<String> impacts = new java.util.ArrayList<>();

        if (applicableMerchants != null) {
            impacts.add("适用商户范围变更");
        }
        if (usageTimePeriods != null) {
            impacts.add("使用时间段限制变更");
        }
        if (dailyLimit != null) {
            impacts.add("每日限额调整");
        }
        if (maxDiscountRate != null) {
            impacts.add("折扣率调整");
        }
        if (effectiveDate != null || expiryDate != null) {
            impacts.add("有效期调整");
        }
        if (autoRenew != null || renewalRule != null) {
            impacts.add("续期规则调整");
        }

        return impacts;
    }

    /**
     * 获取更新摘要（用于日志）
     */
    public String getUpdateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("补贴ID: ").append(subsidyId);

        if (subsidyCode != null) {
            sb.append(", 补贴编码: ").append(subsidyCode);
        }
        if (subsidyName != null) {
            sb.append(", 补贴名称: ").append(subsidyName);
        }
        if (subsidyType != null) {
            sb.append(", 补贴类型: ").append(subsidyType);
        }
        if (subsidyPeriod != null) {
            sb.append(", 补贴周期: ").append(subsidyPeriod);
        }
        if (effectiveDate != null || expiryDate != null) {
            sb.append(", 有效期调整");
        }
        if (dailyLimit != null) {
            sb.append(", 每日限额: ").append(dailyLimit);
        }
        if (autoRenew != null) {
            sb.append(", 自动续期: ").append(autoRenew == 1 ? "是" : "否");
        }
        if (updateReason != null) {
            sb.append(", 更新原因: ").append(updateReason);
        }
        if (forceUpdate != null && forceUpdate) {
            sb.append(", 强制更新: 是");
        }

        return sb.toString();
    }

    /**
     * 验证完整的业务规则
     */
    public java.util.List<String> validateBusinessRules(Integer currentStatus, BigDecimal subsidyAmount, BigDecimal usedAmount) {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证更新权限
        if (!canUpdate(currentStatus, usedAmount)) {
            errors.add("当前状态的补贴不允许更新，如需更新请启用强制更新");
        }

        // 验证日期逻辑
        if (!isValidDateRange()) {
            errors.add("生效日期不能晚于过期日期");
        }

        // 验证每日限额
        if (!isValidDailyLimit(subsidyAmount)) {
            errors.add("每日限额必须大于0且不超过补贴金额");
        }

        // 验证折扣率
        if (!isValidDiscountRate()) {
            errors.add("最大折扣率必须在0-1之间");
        }

        // 验证适用商户格式
        if (!isValidMerchantsFormat()) {
            errors.add("适用商户格式不正确，应为JSON数组格式");
        }

        // 验证使用时间段格式
        if (!isValidTimePeriodsFormat()) {
            errors.add("使用时间段格式不正确，应为JSON数组格式");
        }

        // 验证续期规则格式
        if (!isValidRenewalRuleFormat()) {
            errors.add("续期规则格式不正确，应为JSON格式");
        }

        // 验证版本号
        if (version == null || version < 0) {
            errors.add("版本号必须大于等于0");
        }

        // 强制更新的额外验证
        if (forceUpdate != null && forceUpdate) {
            if (updateReason == null || updateReason.trim().isEmpty()) {
                errors.add("强制更新必须提供更新原因");
            }
        }

        return errors;
    }

    /**
     * 检查更新是否会用户产生影响
     */
    public boolean affectsUserExperience() {
        return applicableMerchants != null ||
               usageTimePeriods != null ||
               dailyLimit != null ||
               maxDiscountRate != null ||
               expiryDate != null;
    }

    /**
     * 获取用户通知信息
     */
    public String getUserNotificationMessage() {
        if (!affectsUserExperience()) {
            return null;
        }

        StringBuilder message = new StringBuilder("您的补贴信息已更新：");

        if (applicableMerchants != null) {
            message.append("适用商户范围已调整；");
        }
        if (usageTimePeriods != null) {
            message.append("使用时间段已调整；");
        }
        if (dailyLimit != null) {
            message.append("每日限额已调整；");
        }
        if (maxDiscountRate != null) {
            message.append("折扣政策已调整；");
        }
        if (expiryDate != null) {
            message.append("有效期已调整；");
        }

        return message.toString();
    }
}