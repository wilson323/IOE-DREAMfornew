/*
 * 限额验证结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 限额验证结果
 * 封装消费限额验证的结果信息
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitValidationResult {

    /**
     * 是否通过验证
     */
    private boolean passed;

    /**
     * 验证结果代码
     */
    private String resultCode;

    /**
     * 验证结果消息
     */
    private String message;

    /**
     * 风险等级（SAFE/LOW/MEDIUM/HIGH/CRITICAL）
     */
    private String riskLevel;

    /**
     * 限制类型
     */
    private String limitType;

    /**
     * 限制值
     */
    private BigDecimal limitValue;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 尝试值（消费金额）
     */
    private BigDecimal attemptValue;

    /**
     * 剩余额度
     */
    private BigDecimal remainingAmount;

    /**
     * 剩余次数
     */
    private Integer remainingCount;

    /**
     * 超出金额
     */
    private BigDecimal exceededAmount;

    /**
     * 建议操作
     */
    private String suggestedAction;

    /**
     * 详细原因
     */
    private String detailReason;

    /**
     * 影响的限额规则列表
     */
    private List<String> affectedRules;

    /**
     * 建议限额调整
     */
    private BigDecimal suggestedLimit;

    /**
     * 验证时间
     */
    private java.time.LocalDateTime verifyTime;

    /**
     * 创建成功的验证结果
     */
    public static LimitValidationResult success() {
        return LimitValidationResult.builder()
                .passed(true)
                .resultCode("SUCCESS")
                .message("限额验证通过")
                .riskLevel("SAFE")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建成功的验证结果（带剩余额度信息）
     */
    public static LimitValidationResult success(BigDecimal remainingAmount, Integer remainingCount) {
        return LimitValidationResult.builder()
                .passed(true)
                .resultCode("SUCCESS")
                .message("限额验证通过")
                .riskLevel("SAFE")
                .remainingAmount(remainingAmount)
                .remainingCount(remainingCount)
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static LimitValidationResult failure(String limitType, String message) {
        return LimitValidationResult.builder()
                .passed(false)
                .resultCode("LIMIT_EXCEEDED")
                .limitType(limitType)
                .message(message)
                .riskLevel("HIGH")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建单次限额超出的结果
     */
    public static LimitValidationResult singleLimitExceeded(BigDecimal limit, BigDecimal amount) {
        return LimitValidationResult.builder()
                .passed(false)
                .resultCode("SINGLE_LIMIT_EXCEEDED")
                .limitType("SINGLE")
                .message("超出单次消费限额")
                .limitValue(limit)
                .attemptValue(amount)
                .exceededAmount(amount.subtract(limit))
                .remainingAmount(BigDecimal.ZERO)
                .riskLevel("HIGH")
                .suggestedAction("请降低消费金额")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建日度限额超出的结果
     */
    public static LimitValidationResult dailyLimitExceeded(BigDecimal limit, BigDecimal current, BigDecimal attempt) {
        BigDecimal exceeded = current.add(attempt).subtract(limit);
        return LimitValidationResult.builder()
                .passed(false)
                .resultCode("DAILY_LIMIT_EXCEEDED")
                .limitType("DAILY")
                .message("超出日度消费限额")
                .limitValue(limit)
                .currentValue(current)
                .attemptValue(attempt)
                .exceededAmount(exceeded)
                .remainingAmount(BigDecimal.ZERO)
                .riskLevel("HIGH")
                .suggestedAction("请明天再消费或申请临时额度")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建次数限制超出的结果
     */
    public static LimitValidationResult countLimitExceeded(String limitType, Integer limit) {
        return LimitValidationResult.builder()
                .passed(false)
                .resultCode("COUNT_LIMIT_EXCEEDED")
                .limitType(limitType)
                .message(String.format("超出%s消费次数限制", getLimitTypeDescriptionStatic(limitType)))
                .limitValue(new BigDecimal(limit.toString()))
                .remainingCount(0)
                .riskLevel("MEDIUM")
                .suggestedAction("请明天再消费")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 创建时间限制超出的结果
     */
    public static LimitValidationResult timeLimitExceeded(String timeSlot, String message) {
        return LimitValidationResult.builder()
                .passed(false)
                .resultCode("TIME_LIMIT_EXCEEDED")
                .limitType("TIME")
                .message(String.format("在%s时间段%s", timeSlot, message))
                .riskLevel("MEDIUM")
                .suggestedAction("请在允许的时间段内消费")
                .detailReason("当前时间段不在此配置的允许消费时间内")
                .verifyTime(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * 获取限额类型描述（静态方法）
     */
    public static String getLimitTypeDescriptionStatic(String limitType) {
        if (limitType == null) {
            return "未知";
        }
        switch (limitType) {
            case "SINGLE":
                return "单次限额";
            case "DAILY":
                return "日度限额";
            case "WEEKLY":
                return "周度限额";
            case "MONTHLY":
                return "月度限额";
            case "YEARLY":
                return "年度限额";
            case "TIME":
                return "时间限额";
            case "DAY_COUNT":
                return "日度次数";
            case "WEEK_COUNT":
                return "周度次数";
            case "MONTH_COUNT":
                return "月度次数";
            default:
                return limitType;
        }
    }

    /**
     * 获取风险等级描述
     */
    public String getRiskLevelDescription() {
        if (riskLevel == null) {
            return "未知";
        }
        switch (riskLevel) {
            case "SAFE":
                return "安全";
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return riskLevel;
        }
    }

    /**
     * 获取使用率
     */
    public BigDecimal getUsageRate() {
        if (limitValue == null || limitValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal usedValue = currentValue != null ? currentValue : BigDecimal.ZERO;
        return usedValue.divide(limitValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 检查是否需要警告
     */
    public boolean needsWarning() {
        if (passed) {
            return false;
        }
        return "MEDIUM".equals(riskLevel);
    }

    /**
     * 检查是否为严重违规
     */
    public boolean isCriticalViolation() {
        return !passed && ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel));
    }

    /**
     * 获取建议消息
     */
    public String getSuggestionMessage() {
        if (suggestedAction == null || suggestedAction.isEmpty()) {
            if (passed) {
                return "消费正常，请注意合理安排消费";
            } else {
                return "建议检查消费限额设置";
            }
        }
        return suggestedAction;
    }

    /**
     * 获取详细信息
     */
    public String getDetailInfo() {
        StringBuilder detail = new StringBuilder();
        detail.append("限额类型: ").append(getLimitTypeDescription());

        if (limitValue != null) {
            detail.append(", 限额值: ").append(limitValue);
        }

        if (currentValue != null) {
            detail.append(", 当前已用: ").append(currentValue);
        }

        if (attemptValue != null) {
            detail.append(", 尝试消费: ").append(attemptValue);
        }

        if (remainingAmount != null) {
            detail.append(", 剩余额度: ").append(remainingAmount);
        }

        if (exceededAmount != null && exceededAmount.compareTo(BigDecimal.ZERO) > 0) {
            detail.append(", 超出金额: ").append(exceededAmount);
        }

        return detail.toString();
    }
}
