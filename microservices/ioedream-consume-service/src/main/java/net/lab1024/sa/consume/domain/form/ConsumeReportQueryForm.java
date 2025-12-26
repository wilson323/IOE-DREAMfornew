package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import net.lab1024.sa.common.domain.form.PageForm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 消费报表查询表单
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的查询条件
 * - 报表类型选择
 * - 时间范围查询
 * - 导出格式选择
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费报表查询表单")
public class ConsumeReportQueryForm extends PageForm {

    @Schema(description = "报表类型", example = "daily")
    @Pattern(regexp = "^(daily|monthly|product|user|revenue|subsidy|recharge|refund|device|merchant|timeslot|area)$",
             message = "报表类型无效")
    private String reportType;

    @Schema(description = "报表格式", example = "excel")
    @Pattern(regexp = "^(excel|pdf|html|json|csv)$",
             message = "报表格式无效")
    private String reportFormat;

    @Schema(description = "开始日期", example = "2025-12-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-12-31")
    private LocalDate endDate;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "产品分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "统计周期", example = "day")
    @Pattern(regexp = "^(hour|day|week|month|quarter|year)$",
             message = "统计周期无效")
    private String statisticsPeriod;

    @Schema(description = "对比类型", example = "month")
    @Pattern(regexp = "^(day|week|month|year)$",
             message = "对比类型无效")
    private String compareType;

    @Schema(description = "预测天数", example = "30")
    @Min(value = 1, message = "预测天数必须大于0")
    private Integer forecastDays;

    @Schema(description = "最小金额", example = "10.00")
    private java.math.BigDecimal minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private java.math.BigDecimal maxAmount;

    @Schema(description = "排序字段", example = "date")
    private String sortBy;

    @Schema(description = "排序方向", example = "desc")
    private String sortDirection;

    @Schema(description = "是否包含图表数据", example = "true")
    private Boolean includeChartData;

    @Schema(description = "是否包含详细数据", example = "false")
    private Boolean includeDetailData;

    @Schema(description = "是否异步生成", example = "false")
    private Boolean asyncGenerate;

    @Schema(description = "数据维度", example = "summary")
    @Pattern(regexp = "^(summary|detail|both)$",
             message = "数据维度无效")
    private String dataDimension;

    @Schema(description = "过滤条件", example = "{}")
    private String filterConditions;

    @Schema(description = "分组字段", example = "category")
    private String groupBy;

    @Schema(description = "报表模板ID", example = "template_001")
    private String templateId;

    @Schema(description = "自定义参数", example = "{}")
    private String customParameters;

    // ==================== 查询条件构建方法 ====================

    /**
     * 是否有报表类型
     */
    public boolean hasReportType() {
        return reportType != null && !reportType.trim().isEmpty();
    }

    /**
     * 是否有时间范围
     */
    public boolean hasDateRange() {
        return startDate != null || endDate != null;
    }

    /**
     * 是否有用户过滤
     */
    public boolean hasUserFilter() {
        return userId != null;
    }

    /**
     * 是否有产品分类过滤
     */
    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    /**
     * 是否有商户过滤
     */
    public boolean hasMerchantFilter() {
        return merchantId != null;
    }

    /**
     * 是否有设备过滤
     */
    public boolean hasDeviceFilter() {
        return deviceId != null;
    }

    /**
     * 是否有区域过滤
     */
    public boolean hasAreaFilter() {
        return areaId != null;
    }

    /**
     * 是否有金额范围
     */
    public boolean hasAmountRange() {
        return minAmount != null || maxAmount != null;
    }

    /**
     * 是否需要图表数据
     */
    public boolean needChartData() {
        return includeChartData != null && includeChartData;
    }

    /**
     * 是否需要详细数据
     */
    public boolean needDetailData() {
        return includeDetailData != null && includeDetailData;
    }

    /**
     * 是否异步生成
     */
    public boolean isAsyncGenerate() {
        return asyncGenerate != null && asyncGenerate;
    }

    /**
     * 获取报表类型描述
     */
    public String getReportTypeDescription() {
        if (!hasReportType()) {
            return "未知报表";
        }
        switch (reportType) {
            case "daily": return "每日消费报表";
            case "monthly": return "月度汇总报表";
            case "product": return "产品分析报表";
            case "user": return "用户分析报表";
            case "revenue": return "收入分析报表";
            case "subsidy": return "补贴使用报表";
            case "recharge": return "充值统计报表";
            case "refund": return "退款分析报表";
            case "device": return "设备运营报表";
            case "merchant": return "商户分析报表";
            case "timeslot": return "时段分析报表";
            case "area": return "区域分析报表";
            default: return "未知报表";
        }
    }

    /**
     * 获取报表格式描述
     */
    public String getReportFormatDescription() {
        if (reportFormat == null) {
            return "未知格式";
        }
        switch (reportFormat) {
            case "excel": return "Excel表格";
            case "pdf": return "PDF文档";
            case "html": return "HTML网页";
            case "json": return "JSON数据";
            case "csv": return "CSV文件";
            default: return "未知格式";
        }
    }

    /**
     * 获取统计周期描述
     */
    public String getStatisticsPeriodDescription() {
        if (statisticsPeriod == null) {
            return "默认周期";
        }
        switch (statisticsPeriod) {
            case "hour": return "小时";
            case "day": return "天";
            case "week": return "周";
            case "month": return "月";
            case "quarter": return "季度";
            case "year": return "年";
            default: return "未知周期";
        }
    }

    /**
     * 获取数据维度描述
     */
    public String getDataDimensionDescription() {
        if (dataDimension == null) {
            return "汇总数据";
        }
        switch (dataDimension) {
            case "summary": return "汇总数据";
            case "detail": return "详细数据";
            case "both": return "汇总+详细";
            default: return "未知维度";
        }
    }

    /**
     * 验证时间范围合理性
     */
    public boolean isValidDateRange() {
        if (!hasDateRange()) {
            return true; // 没有时间范围限制
        }
        if (startDate == null || endDate == null) {
            return true; // 只有开始或结束时间
        }
        return !startDate.isAfter(endDate); // 开始时间不能晚于结束时间
    }

    /**
     * 验证金额范围合理性
     */
    public boolean isValidAmountRange() {
        if (!hasAmountRange()) {
            return true; // 没有金额范围限制
        }
        if (minAmount == null || maxAmount == null) {
            return true; // 只有最小或最大金额
        }
        return minAmount.compareTo(maxAmount) <= 0; // 最小金额不能大于最大金额
    }

    /**
     * 获取默认排序字段
     */
    public String getSortBy() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "date"; // 默认按日期排序
        }
        return sortBy.trim();
    }

    /**
     * 获取默认排序方向
     */
    public String getSortDirection() {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return "desc"; // 默认降序
        }
        return sortDirection.trim().toLowerCase();
    }

    /**
     * 获取默认报表格式
     */
    public String getReportFormat() {
        if (reportFormat == null || reportFormat.trim().isEmpty()) {
            return "excel"; // 默认Excel格式
        }
        return reportFormat.trim().toLowerCase();
    }

    /**
     * 获取过滤条件JSON
     */
    public String getFilterConditionsJson() {
        if (filterConditions == null || filterConditions.trim().isEmpty()) {
            return "{}";
        }
        return filterConditions.trim();
    }

    /**
     * 获取自定义参数JSON
     */
    public String getCustomParametersJson() {
        if (customParameters == null || customParameters.trim().isEmpty()) {
            return "{}";
        }
        return customParameters.trim();
    }

    /**
     * 构建查询条件摘要
     */
    public String getQuerySummary() {
        StringBuilder sb = new StringBuilder();

        if (hasReportType()) {
            sb.append("报表类型: ").append(getReportTypeDescription());
        }

        if (hasDateRange()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("时间范围: ");
            if (startDate != null) sb.append(startDate);
            if (startDate != null && endDate != null) sb.append(" ~ ");
            if (endDate != null) sb.append(endDate);
        }

        if (hasUserFilter()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("用户ID: ").append(userId);
        }

        if (hasCategoryFilter()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("产品分类ID: ").append(categoryId);
        }

        if (hasMerchantFilter()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("商户ID: ").append(merchantId);
        }

        if (hasAmountRange()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("金额范围: ").append(minAmount).append(" ~ ").append(maxAmount);
        }

        if (groupBy != null && !groupBy.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("分组: ").append(groupBy);
        }

        return sb.toString();
    }

    /**
     * 验证查询条件
     */
    public java.util.List<String> validateQueryConditions() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证必填字段
        if (!hasReportType()) {
            errors.add("报表类型不能为空");
        }

        // 验证时间范围
        if (!isValidDateRange()) {
            errors.add("开始日期不能晚于结束日期");
        }

        // 验证金额范围
        if (!isValidAmountRange()) {
            errors.add("最小金额不能大于最大金额");
        }

        // 验证预测天数
        if (forecastDays != null && (forecastDays < 1 || forecastDays > 365)) {
            errors.add("预测天数必须在1-365之间");
        }

        // 验证异步生成的必要条件
        if (isAsyncGenerate() && hasLargeDataRange()) {
            // 大数据量报表建议异步生成
        }

        return errors;
    }

    /**
     * 检查是否为大数据量查询
     */
    public boolean hasLargeDataRange() {
        if (!hasDateRange()) {
            return false;
        }
        if (startDate != null && endDate != null) {
            // 时间范围超过30天认为是大数据量
            return startDate.until(endDate).getDays() > 30;
        }
        return false;
    }

    /**
     * 获取建议的生成方式
     */
    public String getRecommendedGenerateMethod() {
        if (hasLargeDataRange() || needDetailData()) {
            return "异步生成";
        }
        return "同步生成";
    }

    /**
     * 获取报表标题
     */
    public String getReportTitle() {
        StringBuilder title = new StringBuilder();

        if (hasDateRange()) {
            if (startDate != null && endDate != null) {
                title.append(startDate).append("至").append(endDate);
            } else if (startDate != null) {
                title.append(startDate).append("至今");
            } else if (endDate != null) {
                title.append("截至").append(endDate);
            }
        } else {
            title.append("截至当前");
        }

        title.append(getReportTypeDescription());

        return title.toString();
    }

    /**
     * 构建缓存键
     */
    public String getCacheKey() {
        StringBuilder key = new StringBuilder("report:");
        key.append(hasReportType() ? reportType : "unknown");

        if (startDate != null) key.append(":").append(startDate);
        if (endDate != null) key.append(":").append(endDate);
        if (userId != null) key.append(":user").append(userId);
        if (categoryId != null) key.append(":cat").append(categoryId);
        if (merchantId != null) key.append(":merchant").append(merchantId);

        return key.toString();
    }
}