package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import net.lab1024.sa.common.domain.form.PageForm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费补贴查询表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的查询条件
 * - 分页参数
 * - 排序条件
 * - 时间范围查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费补贴查询表单")
public class ConsumeSubsidyQueryForm extends PageForm {

    @Schema(description = "补贴编码", example = "SUBSIDY_001")
    private String subsidyCode;

    @Schema(description = "补贴名称", example = "12月餐饮补贴")
    private String subsidyName;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "补贴类型", example = "1")
    private Integer subsidyType;

    @Schema(description = "补贴周期", example = "4")
    private Integer subsidyPeriod;

    @Schema(description = "补贴状态", example = "2")
    private Integer subsidyStatus;

    @Schema(description = "金额范围-最小值", example = "100.00")
    private BigDecimal minAmount;

    @Schema(description = "金额范围-最大值", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "使用率范围-最小值", example = "0.0")
    private BigDecimal minUsageRate;

    @Schema(description = "使用率范围-最大值", example = "100.0")
    private BigDecimal maxUsageRate;

    @Schema(description = "是否自动续期", example = "1")
    private Integer autoRenew;

    @Schema(description = "即将过期（天数）", example = "7")
    private Integer expiringWithinDays;

    @Schema(description = "即将用完（使用率阈值）", example = "80.0")
    private BigDecimal nearlyDepletedThreshold;

    @Schema(description = "生效日期-开始", example = "2025-12-01")
    private LocalDate effectiveDateStart;

    @Schema(description = "生效日期-结束", example = "2025-12-31")
    private LocalDate effectiveDateEnd;

    @Schema(description = "过期日期-开始", example = "2025-12-01")
    private LocalDate expiryDateStart;

    @Schema(description = "过期日期-结束", example = "2025-12-31")
    private LocalDate expiryDateEnd;

    @Schema(description = "发放日期-开始", example = "2025-12-01 00:00:00")
    private String issueDateStart;

    @Schema(description = "发放日期-结束", example = "2025-12-31 23:59:59")
    private String issueDateEnd;

    @Schema(description = "创建时间-开始", example = "2025-12-01 00:00:00")
    private String createTimeStart;

    @Schema(description = "创建时间-结束", example = "2025-12-31 23:59:59")
    private String createTimeEnd;

    @Schema(description = "更新时间-开始", example = "2025-12-01 00:00:00")
    private String updateTimeStart;

    @Schema(description = "更新时间-结束", example = "2025-12-31 23:59:59")
    private String updateTimeEnd;

    @Schema(description = "关键字搜索（补贴名称或编码）", example = "餐饮")
    private String keyword;

    @Schema(description = "是否查询详细信息", example = "false")
    private Boolean fetchDetail;

    @Schema(description = "是否包含已删除", example = "false")
    private Boolean includeDeleted;

    @Schema(description = "排序字段", example = "createTime")
    private String sortBy;

    @Schema(description = "排序方向", example = "desc")
    private String sortDirection;

    @Schema(description = "查询类型", example = "all")
    private String queryType; // all, active, expired, used, pending

    // ==================== 查询条件构建方法 ====================

    /**
     * 是否有补贴编码查询条件
     */
    public boolean hasSubsidyCode() {
        return subsidyCode != null && !subsidyCode.trim().isEmpty();
    }

    /**
     * 是否有补贴名称查询条件
     */
    public boolean hasSubsidyName() {
        return subsidyName != null && !subsidyName.trim().isEmpty();
    }

    /**
     * 是否有用户ID查询条件
     */
    public boolean hasUserId() {
        return userId != null;
    }

    /**
     * 是否有用户姓名查询条件
     */
    public boolean hasUserName() {
        return userName != null && !userName.trim().isEmpty();
    }

    /**
     * 是否有关键字查询条件
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 是否有金额范围查询条件
     */
    public boolean hasAmountRange() {
        return minAmount != null || maxAmount != null;
    }

    /**
     * 是否有使用率范围查询条件
     */
    public boolean hasUsageRateRange() {
        return minUsageRate != null || maxUsageRate != null;
    }

    /**
     * 是否有生效日期范围查询条件
     */
    public boolean hasEffectiveDateRange() {
        return effectiveDateStart != null || effectiveDateEnd != null;
    }

    /**
     * 是否有过期日期范围查询条件
     */
    public boolean hasExpiryDateRange() {
        return expiryDateStart != null || expiryDateEnd != null;
    }

    /**
     * 是否有发放日期范围查询条件
     */
    public boolean hasIssueDateRange() {
        return issueDateStart != null || issueDateEnd != null;
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
     * 是否即将过期查询
     */
    public boolean isExpiringSoonQuery() {
        return expiringWithinDays != null && expiringWithinDays > 0;
    }

    /**
     * 是否即将用完查询
     */
    public boolean isNearlyDepletedQuery() {
        return nearlyDepletedThreshold != null &&
               nearlyDepletedThreshold.compareTo(BigDecimal.ZERO) > 0 &&
               nearlyDepletedThreshold.compareTo(BigDecimal.ONE) <= 0;
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
            "subsidyId", "subsidyCode", "subsidyName", "userId", "userName",
            "subsidyType", "subsidyPeriod", "subsidyAmount", "usedAmount",
            "remainingAmount", "subsidyStatus", "effectiveDate", "expiryDate",
            "issueDate", "usageRate", "createTime", "updateTime"
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
     * 获取查询类型枚举值
     */
    public String getQueryTypeValue() {
        if (queryType == null || queryType.trim().isEmpty()) {
            return "all";
        }
        String type = queryType.trim().toLowerCase();
        switch (type) {
            case "active": return "active";
            case "expired": return "expired";
            case "used": return "used";
            case "pending": return "pending";
            default: return "all";
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
     * 获取补贴状态过滤条件
     */
    public Integer getSubsidyStatusFilter() {
        if (queryType != null) {
            switch (getQueryTypeValue()) {
                case "active":
                    return 2; // 已发放
                case "expired":
                    return 3; // 已过期
                case "used":
                    return 4; // 已使用
                case "pending":
                    return 1; // 待发放
            }
        }
        return subsidyStatus; // 返回用户指定的状态
    }

    /**
     * 获取查询条件摘要（用于日志）
     */
    public String getQuerySummary() {
        StringBuilder sb = new StringBuilder();

        if (hasSubsidyCode()) {
            sb.append("补贴编码: ").append(subsidyCode).append(", ");
        }

        if (hasSubsidyName()) {
            sb.append("补贴名称: ").append(subsidyName).append(", ");
        }

        if (hasUserId()) {
            sb.append("用户ID: ").append(userId).append(", ");
        }

        if (hasUserName()) {
            sb.append("用户姓名: ").append(userName).append(", ");
        }

        if (subsidyType != null) {
            sb.append("补贴类型: ").append(subsidyType).append(", ");
        }

        if (subsidyPeriod != null) {
            sb.append("补贴周期: ").append(subsidyPeriod).append(", ");
        }

        if (subsidyStatus != null) {
            sb.append("补贴状态: ").append(subsidyStatus).append(", ");
        }

        if (hasAmountRange()) {
            sb.append("金额范围: ").append(minAmount).append("-").append(maxAmount).append(", ");
        }

        if (autoRenew != null) {
            sb.append("自动续期: ").append(autoRenew).append(", ");
        }

        if (isExpiringSoonQuery()) {
            sb.append("即将过期(").append(expiringWithinDays).append("天)").append(", ");
        }

        if (isNearlyDepletedQuery()) {
            sb.append("即将用完(").append(nearlyDepletedThreshold).append("%)").append(", ");
        }

        if (hasKeyword()) {
            sb.append("关键字: ").append(keyword).append(", ");
        }

        if (!"all".equals(getQueryTypeValue())) {
            sb.append("查询类型: ").append(getQueryTypeValue()).append(", ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // 移除最后的", "
        }

        return sb.toString();
    }

    /**
     * 验证查询条件是否合理
     */
    public java.util.List<String> validateQueryConditions() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证金额范围
        if (hasAmountRange()) {
            if (minAmount != null && minAmount.compareTo(BigDecimal.ZERO) < 0) {
                errors.add("最小金额不能小于0");
            }
            if (maxAmount != null && maxAmount.compareTo(BigDecimal.ZERO) < 0) {
                errors.add("最大金额不能小于0");
            }
            if (minAmount != null && maxAmount != null && minAmount.compareTo(maxAmount) > 0) {
                errors.add("最小金额不能大于最大金额");
            }
        }

        // 验证使用率范围
        if (hasUsageRateRange()) {
            if (minUsageRate != null && (minUsageRate.compareTo(BigDecimal.ZERO) < 0 || minUsageRate.compareTo(BigDecimal.ONE) > 0)) {
                errors.add("最小使用率必须在0-1之间");
            }
            if (maxUsageRate != null && (maxUsageRate.compareTo(BigDecimal.ZERO) < 0 || maxUsageRate.compareTo(BigDecimal.ONE) > 0)) {
                errors.add("最大使用率必须在0-1之间");
            }
            if (minUsageRate != null && maxUsageRate != null && minUsageRate.compareTo(maxUsageRate) > 0) {
                errors.add("最小使用率不能大于最大使用率");
            }
        }

        // 验证日期范围
        if (hasEffectiveDateRange()) {
            if (effectiveDateStart != null && effectiveDateEnd != null && effectiveDateStart.isAfter(effectiveDateEnd)) {
                errors.add("生效日期开始不能晚于生效日期结束");
            }
        }

        if (hasExpiryDateRange()) {
            if (expiryDateStart != null && expiryDateEnd != null && expiryDateStart.isAfter(expiryDateEnd)) {
                errors.add("过期日期开始不能晚于过期日期结束");
            }
        }

        // 验证即将过期天数
        if (isExpiringSoonQuery() && expiringWithinDays > 365) {
            errors.add("即将过期天数不能超过365天");
        }

        return errors;
    }

    /**
     * 获取优化后的查询参数
     */
    public Map<String, Object> getOptimizedQueryParams() {
        Map<String, Object> params = new java.util.HashMap<>();

        // 基础查询参数
        if (hasSubsidyCode()) {
            params.put("subsidyCode", subsidyCode + "%");
        }
        if (hasSubsidyName()) {
            params.put("subsidyName", "%" + subsidyName + "%");
        }
        if (hasUserName()) {
            params.put("userName", "%" + userName + "%");
        }
        if (hasKeyword()) {
            params.put("keyword", "%" + keyword + "%");
        }

        // 范围查询参数
        if (hasAmountRange()) {
            params.put("minAmount", minAmount);
            params.put("maxAmount", maxAmount);
        }

        // 日期范围参数
        if (hasEffectiveDateRange()) {
            params.put("effectiveDateStart", effectiveDateStart);
            params.put("effectiveDateEnd", effectiveDateEnd);
        }

        if (hasExpiryDateRange()) {
            params.put("expiryDateStart", expiryDateStart);
            params.put("expiryDateEnd", expiryDateEnd);
        }

        return params;
    }
}