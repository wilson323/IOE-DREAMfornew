package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费补贴视图对象
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的字段定义
 * - 业务状态判断方法
 * - 余额计算方法
 * - 时间验证方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费补贴信息")
public class ConsumeSubsidyVO {

    @Schema(description = "补贴ID", example = "1")
    private Long subsidyId;

    @Schema(description = "补贴编码", example = "MEAL_001")
    private String subsidyCode;

    @Schema(description = "补贴名称", example = "员工餐补")
    private String subsidyName;

    @Schema(description = "补贴类型", example = "1")
    private Integer subsidyType;

    @Schema(description = "补贴类型名称", example = "餐补")
    private String subsidyTypeName;

    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "补贴金额", example = "300.00")
    private BigDecimal subsidyAmount;

    @Schema(description = "已使用金额", example = "125.50")
    private BigDecimal usedAmount;

    @Schema(description = "剩余金额", example = "174.50")
    private BigDecimal remainingAmount;

    @Schema(description = "补贴周期", example = "3")
    private Integer subsidyPeriod;

    @Schema(description = "补贴周期名称", example = "每月")
    private String subsidyPeriodName;

    @Schema(description = "发放日期", example = "2025-12-21T00:00:00")
    private LocalDateTime issueDate;

    @Schema(description = "生效日期", example = "2025-12-21T00:00:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "失效日期", example = "2025-12-31T23:59:59")
    private LocalDateTime expiryDate;

    @Schema(description = "补贴状态", example = "3")
    private Integer status;

    @Schema(description = "补贴状态名称", example = "已生效")
    private String statusName;

    @Schema(description = "使用限制", example = "2")
    private Integer usageLimit;

    @Schema(description = "使用限制名称", example = "仅餐饮")
    private String usageLimitName;

    @Schema(description = "适用商户ID列表", example = "[\"1\", \"2\", \"3\"]")
    private String applicableMerchants;

    @Schema(description = "适用商户名称列表", example = "[\"食堂\", \"餐厅\", \"超市\"]")
    private List<String> applicableMerchantNames;

    @Schema(description = "使用时间段", example = "[\"11:00-14:00\", \"17:00-20:00\"]")
    private String usageTimePeriods;

    @Schema(description = "每日使用限额", example = "50.00")
    private BigDecimal dailyLimit;

    @Schema(description = "每日已使用金额", example = "25.00")
    private BigDecimal dailyUsedAmount;

    @Schema(description = "每日使用最后日期", example = "2025-12-21")
    private LocalDateTime dailyUsageDate;

    @Schema(description = "最小消费金额", example = "0.00")
    private BigDecimal minConsumptionAmount;

    @Schema(description = "补贴比例", example = "0.3")
    private BigDecimal subsidyRate;

    @Schema(description = "是否自动续期", example = "1")
    private Integer autoRenew;

    @Schema(description = "续期规则", example = "{\"period\": \"monthly\", \"amount\": 300}")
    private String renewalRule;

    @Schema(description = "补贴来源", example = "1")
    private Integer subsidySource;

    @Schema(description = "补贴来源名称", example = "公司发放")
    private String subsidySourceName;

    @Schema(description = "发放部门ID", example = "5")
    private Long issueDepartmentId;

    @Schema(description = "发放部门名称", example = "人力资源部")
    private String issueDepartmentName;

    @Schema(description = "发放人ID", example = "456")
    private Long issuerId;

    @Schema(description = "发放人姓名", example = "李四")
    private String issuerName;

    @Schema(description = "审批人ID", example = "789")
    private Long approverId;

    @Schema(description = "审批人姓名", example = "王五")
    private String approverName;

    @Schema(description = "审批时间", example = "2025-12-20T10:00:00")
    private LocalDateTime approveTime;

    @Schema(description = "审批意见", example = "同意发放")
    private String approveRemark;

    @Schema(description = "备注", example = "2025年12月员工餐补")
    private String remark;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    @Schema(description = "创建时间", example = "2025-12-21T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T00:00:00")
    private LocalDateTime updateTime;

    // ==================== 计算字段 ====================

    @Schema(description = "使用比例", example = "41.83")
    private BigDecimal usagePercentage;

    @Schema(description = "是否可使用", example = "true")
    private Boolean isUsable;

    @Schema(description = "是否即将过期", example = "false")
    private Boolean isExpiringSoon;

    @Schema(description = "是否即将用完", example = "false")
    private Boolean isNearlyDepleted;

    @Schema(description = "剩余天数", example = "10")
    private Integer remainingDays;

    @Schema(description = "今日可使用金额", example = "25.00")
    private BigDecimal todayAvailableAmount;

    // ==================== 业务状态判断方法 ====================

    /**
     * 判断是否待发放
     */
    public boolean isPending() {
        return status != null && status == 1;
    }

    /**
     * 判断是否已发放
     */
    public boolean isIssued() {
        return status != null && status == 2;
    }

    /**
     * 判断是否已生效
     */
    public boolean isEffective() {
        return status != null && status == 3;
    }

    /**
     * 判断是否已使用
     */
    public boolean isUsed() {
        return status != null && status == 4;
    }

    /**
     * 判断是否已过期
     */
    public boolean isExpired() {
        return status != null && status == 5;
    }

    /**
     * 判断是否已作废
     */
    public boolean isVoid() {
        return status != null && status == 6;
    }

    /**
     * 判断是否可使用
     */
    public boolean isUsable() {
        if (!isEffective() || isExpired() || isVoid()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (effectiveDate != null && now.isBefore(effectiveDate)) {
            return false;
        }

        if (expiryDate != null && now.isAfter(expiryDate)) {
            return false;
        }

        return hasRemaining();
    }

    /**
     * 判断是否有剩余金额
     */
    public boolean hasRemaining() {
        return remainingAmount != null && remainingAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否有每日限额
     */
    public boolean hasDailyLimit() {
        return dailyLimit != null && dailyLimit.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否达到每日限额
     */
    public boolean isDailyLimitReached() {
        if (!hasDailyLimit()) {
            return false;
        }

        return dailyUsedAmount != null && dailyUsedAmount.compareTo(dailyLimit) >= 0;
    }

    /**
     * 判断是否今日首次使用
     */
    public boolean isTodayFirstUsage() {
        if (dailyUsageDate == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        return !dailyUsageDate.toLocalDate().equals(now.toLocalDate());
    }

    /**
     * 判断是否自动续期
     */
    public boolean isAutoRenew() {
        return autoRenew != null && autoRenew == 1;
    }

    /**
     * 获取补贴类型名称
     */
    public String getSubsidyTypeName() {
        if (subsidyType == null) {
            return "";
        }
        switch (subsidyType) {
            case 1:
                return "餐补";
            case 2:
                return "交通补贴";
            case 3:
                return "通讯补贴";
            case 4:
                return "住房补贴";
            case 5:
                return "其他补贴";
            default:
                return "未知";
        }
    }

    /**
     * 获取补贴周期名称
     */
    public String getSubsidyPeriodName() {
        if (subsidyPeriod == null) {
            return "";
        }
        switch (subsidyPeriod) {
            case 1:
                return "每日";
            case 2:
                return "每周";
            case 3:
                return "每月";
            case 4:
                return "每季度";
            case 5:
                return "每年";
            case 6:
                return "一次性";
            default:
                return "未知";
        }
    }

    /**
     * 获取补贴状态名称
     */
    public String getStatusName() {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1:
                return "待发放";
            case 2:
                return "已发放";
            case 3:
                return "已生效";
            case 4:
                return "已使用";
            case 5:
                return "已过期";
            case 6:
                return "已作废";
            default:
                return "未知";
        }
    }

    /**
     * 获取使用限制名称
     */
    public String getUsageLimitName() {
        if (usageLimit == null) {
            return "";
        }
        switch (usageLimit) {
            case 1:
                return "无限制";
            case 2:
                return "仅餐饮";
            case 3:
                return "仅交通";
            case 4:
                return "仅指定商户";
            default:
                return "未知";
        }
    }

    /**
     * 获取补贴来源名称
     */
    public String getSubsidySourceName() {
        if (subsidySource == null) {
            return "";
        }
        switch (subsidySource) {
            case 1:
                return "公司发放";
            case 2:
                return "政府补贴";
            case 3:
                return "项目预算";
            case 4:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 计算使用比例
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
     * 计算剩余天数
     */
    public Integer getRemainingDays() {
        if (expiryDate == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiryDate)) {
            return 0;
        }

        return (int) java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), expiryDate.toLocalDate());
    }

    /**
     * 判断是否即将过期（7天内）
     */
    public boolean isExpiringSoon() {
        Integer remainingDays = getRemainingDays();
        return remainingDays != null && remainingDays <= 7 && remainingDays > 0;
    }

    /**
     * 判断是否即将用完（使用率超过80%）
     */
    public boolean isNearlyDepleted() {
        BigDecimal usageRate = getUsagePercentage();
        return usageRate.compareTo(new BigDecimal("80")) >= 0 && hasRemaining();
    }

    /**
     * 计算今日可使用金额
     */
    public BigDecimal getTodayAvailableAmount() {
        if (!hasRemaining()) {
            return BigDecimal.ZERO;
        }

        BigDecimal availableByRemaining = remainingAmount;

        if (hasDailyLimit()) {
            BigDecimal dailyRemaining = calculateDailyRemaining();
            return dailyRemaining.min(availableByRemaining);
        }

        return availableByRemaining;
    }

    /**
     * 计算每日剩余金额
     */
    private BigDecimal calculateDailyRemaining() {
        if (!hasDailyLimit()) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }

        BigDecimal dailyUsed = dailyUsedAmount != null ? dailyUsedAmount : BigDecimal.ZERO;
        BigDecimal dailyLimit = getDailyLimit();

        if (isTodayFirstUsage()) {
            return dailyLimit;
        }

        return dailyLimit.subtract(dailyUsed).max(BigDecimal.ZERO);
    }

    /**
     * 获取余额状态文本
     */
    public String getBalanceStatus() {
        if (!hasRemaining()) {
            return "已用完";
        }
        if (isNearlyDepleted()) {
            return "即将用完";
        }
        return "余额充足";
    }

    /**
     * 获取余额状态颜色
     */
    public String getBalanceStatusColor() {
        if (!hasRemaining()) {
            return "#ff4757"; // 红色
        }
        if (isNearlyDepleted()) {
            return "#ffa502"; // 橙色
        }
        return "#2ed573"; // 绿色
    }

    /**
     * 获取状态颜色
     */
    public String getStatusColor() {
        if (status == null) {
            return "#95a5a6"; // 灰色
        }
        switch (status) {
            case 1:
                return "#feca57"; // 黄色 - 待发放
            case 2:
                return "#48dbfb"; // 蓝色 - 已发放
            case 3:
                return "#2ed573"; // 绿色 - 已生效
            case 4:
                return "#ff6348"; // 橙红色 - 已使用
            case 5:
                return "#ff4757"; // 红色 - 已过期
            case 6:
                return "#718093"; // 深灰色 - 已作废
            default:
                return "#95a5a6";
        }
    }

    /**
     * 获取使用进度条颜色
     */
    public String getProgressBarColor() {
        BigDecimal usageRate = getUsagePercentage();
        if (usageRate.compareTo(new BigDecimal("80")) >= 0) {
            return "#ff4757"; // 红色
        } else if (usageRate.compareTo(new BigDecimal("50")) >= 0) {
            return "#ffa502"; // 橙色
        } else {
            return "#2ed573"; // 绿色
        }
    }

    /**
     * 格式化金额显示
     */
    public String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "¥0.00";
        }
        return "¥" + amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    /**
     * 获取时间显示文本
     */
    public String getExpiryDisplayText() {
        if (expiryDate == null) {
            return "永久有效";
        }

        Integer remainingDays = getRemainingDays();
        if (remainingDays != null) {
            if (remainingDays <= 0) {
                return "已过期";
            } else if (remainingDays <= 7) {
                return "剩余" + remainingDays + "天";
            }
        }

        return expiryDate.toLocalDate().toString();
    }
}
