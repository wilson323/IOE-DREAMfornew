package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费补贴添加表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段验证
 * - 业务规则验证
 * - 数据一致性检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费补贴添加表单")
public class ConsumeSubsidyAddForm {

    @Schema(description = "补贴编码", example = "SUBSIDY_001")
    @NotBlank(message = "补贴编码不能为空")
    @Size(max = 50, message = "补贴编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "补贴编码只能包含大写字母、数字和下划线")
    private String subsidyCode;

    @Schema(description = "补贴名称", example = "12月餐饮补贴")
    @NotBlank(message = "补贴名称不能为空")
    @Size(max = 100, message = "补贴名称长度不能超过100个字符")
    private String subsidyName;

    @Schema(description = "用户ID", example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "补贴类型", example = "1")
    @NotNull(message = "补贴类型不能为空")
    @Min(value = 1, message = "补贴类型值无效")
    @Max(value = 10, message = "补贴类型值无效")
    private Integer subsidyType;

    @Schema(description = "补贴周期", example = "4")
    @NotNull(message = "补贴周期不能为空")
    @Min(value = 1, message = "补贴周期值无效")
    @Max(value = 5, message = "补贴周期值无效")
    private Integer subsidyPeriod;

    @Schema(description = "补贴金额", example = "300.00")
    @NotNull(message = "补贴金额不能为空")
    @DecimalMin(value = "0.01", message = "补贴金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "补贴金额格式不正确")
    private BigDecimal subsidyAmount;

    @Schema(description = "生效日期", example = "2025-12-01T00:00:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "过期日期", example = "2025-12-31T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "适用商户", example = "[\"食堂\", \"餐厅\"]")
    private String applicableMerchants;

    @Schema(description = "使用时间段", example = "[\"07:00-20:00\"]")
    private String usageTimePeriods;

    @Schema(description = "每日限额", example = "50.00")
    @DecimalMin(value = "0", message = "每日限额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "每日限额格式不正确")
    private BigDecimal dailyLimit;

    @Schema(description = "最大折扣率", example = "1.00")
    @DecimalMin(value = "0", message = "最大折扣率不能为负数")
    @DecimalMax(value = "1", message = "最大折扣率不能超过1")
    @Digits(integer = 3, fraction = 2, message = "最大折扣率格式不正确")
    private BigDecimal maxDiscountRate;

    @Schema(description = "是否自动续期", example = "0")
    @NotNull(message = "是否自动续期不能为空")
    private Integer autoRenew;

    @Schema(description = "续期规则", example = "{}")
    private String renewalRule;

    @Schema(description = "备注", example = "12月员工餐饮补贴")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    // ==================== 业务验证方法 ====================

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
     * 验证金额合理性
     */
    public boolean isValidAmount() {
        return subsidyAmount != null && subsidyAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 验证每日限额合理性
     */
    public boolean isValidDailyLimit() {
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
     * 验证是否需要每日限额
     */
    public boolean requiresDailyLimit() {
        return subsidyPeriod != null && (subsidyPeriod == 2 || subsidyPeriod == 3); // 日度或周度补贴通常需要每日限额
    }

    /**
     * 获取补贴周期描述
     */
    public String getSubsidyPeriodDescription() {
        if (subsidyPeriod == null) {
            return "未知周期";
        }
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

    /**
     * 获取补贴类型描述
     */
    public String getSubsidyTypeDescription() {
        if (subsidyType == null) {
            return "未知类型";
        }
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
     * 获取自动续期描述
     */
    public String getAutoRenewDescription() {
        if (autoRenew == null) {
            return "未知";
        }
        return autoRenew == 1 ? "是" : "否";
    }

    /**
     * 转换为JSON格式的适用商户
     */
    public String getApplicableMerchantsJson() {
        if (applicableMerchants == null || applicableMerchants.trim().isEmpty()) {
            return "[]";
        }
        if (isValidMerchantsFormat()) {
            return applicableMerchants;
        }
        // 如果不是JSON格式，尝试转换为JSON数组
        return "[\"" + applicableMerchants.replace(",", "\",\"").replace(" ", "") + "\"]";
    }

    /**
     * 转换为JSON格式的使用时间段
     */
    public String getUsageTimePeriodsJson() {
        if (usageTimePeriods == null || usageTimePeriods.trim().isEmpty()) {
            return "[]";
        }
        if (isValidTimePeriodsFormat()) {
            return usageTimePeriods;
        }
        // 如果不是JSON格式，尝试转换为JSON数组
        return "[\"" + usageTimePeriods.replace(",", "\",\"").replace(" ", "") + "\"]";
    }

    /**
     * 生成默认的续期规则
     */
    public String generateDefaultRenewalRule() {
        if (autoRenew == null || autoRenew != 1) {
            return "{}";
        }

        StringBuilder rule = new StringBuilder();
        rule.append("{");
        rule.append("\"autoExtend\": true,");
        rule.append("\"extendDays\": 30,");
        rule.append("\"extendCondition\": \"before_expiry\",");
        rule.append("\"maxExtendCount\": 12");
        rule.append("}");

        return rule.toString();
    }

    /**
     * 获取表单摘要（用于日志）
     */
    public String getFormSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("补贴编码: ").append(subsidyCode);
        sb.append(", 补贴名称: ").append(subsidyName);
        sb.append(", 用户ID: ").append(userId);
        sb.append(", 类型: ").append(getSubsidyTypeDescription());
        sb.append(", 周期: ").append(getSubsidyPeriodDescription());
        sb.append(", 金额: ").append(subsidyAmount);
        if (autoRenew != null && autoRenew == 1) {
            sb.append(", 自动续期: 是");
        }
        if (remark != null && !remark.trim().isEmpty()) {
            sb.append(", 备注: ").append(remark);
        }
        return sb.toString();
    }

    /**
     * 验证完整的业务规则
     */
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证日期逻辑
        if (!isValidDateRange()) {
            errors.add("生效日期不能晚于过期日期");
        }

        // 验证金额合理性
        if (!isValidAmount()) {
            errors.add("补贴金额必须大于0");
        }

        // 验证每日限额
        if (!isValidDailyLimit()) {
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

        // 验证是否需要每日限额
        if (requiresDailyLimit() && dailyLimit == null) {
            errors.add(getSubsidyPeriodDescription() + "补贴建议设置每日限额");
        }

        return errors;
    }
}