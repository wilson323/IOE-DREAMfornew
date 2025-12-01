package net.lab1024.sa.admin.module.consume.engine.mode.impl;

import net.lab1024.sa.admin.module.consume.engine.mode.abstracts.AbstractConsumptionMode;
import net.lab1024.sa.admin.module.consume.domain.enums.MeteringUnitEnum;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * 计量计费消费模式
 * 严格遵循repowiki规范：实现电、水、气等计量资源的计费消费模式
 *
 * 支持功能：
 * - 8种标准计量单位（电、水、气、重量、时间、流量）
 * - 阶梯计价和时段计价
 * - 历史读数管理和趋势分析
 * - 用量异常检测和告警
 * - 多种计费策略（标准、阶梯、时段）
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Component("meteringMode")
public class MeteringMode extends AbstractConsumptionMode {

    // 计量精度配置
    private static final int USAGE_PRECISION = 4;
    private static final int COST_PRECISION = 2;

    // 历史读数配置
    private static final int MAX_HISTORY_READINGS = 12; // 最多12个历史读数
    private static final BigDecimal USAGE_VARIANCE_THRESHOLD = new BigDecimal("0.50"); // 50%差异阈值

    // 计费策略
    public enum BillingStrategy {
        STANDARD("标准计费", "按固定单价计费"),
        TIERED("阶梯计费", "按用量阶梯递增计费"),
        TIME_BASED("时段计费", "按峰谷时段计费"),
        COMPOUND("复合计费", "阶梯+时段复合计费");

        private final String name;
        private final String description;

        BillingStrategy(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    public MeteringMode() {
        super("METERING", "计量计费模式", "电、水、气等计量资源的按使用量计费模式");
    }

    @Override
    public BigDecimal calculateAmount(Map<String, Object> params) {
        try {
            // 获取基本参数
            MeteringUnitEnum unit = getMeteringUnit(params);
            BigDecimal currentUsage = getCurrentUsage(params);
            BigDecimal previousUsage = getPreviousUsage(params);
            BillingStrategy strategy = getBillingStrategy(params);

            // 计算实际用量
            BigDecimal actualUsage = calculateActualUsage(currentUsage, previousUsage, unit);

            // 验证用量合理性
            if (!unit.isValidUsage(actualUsage)) {
                throw new IllegalArgumentException("用量超出合理范围: " + unit.formatUsage(actualUsage));
            }

            // 根据计费策略计算费用
            BigDecimal cost;
            switch (strategy) {
                case TIERED:
                    cost = unit.calculateTieredCost(actualUsage);
                    break;
                case TIME_BASED:
                    String timeSlot = getTimeSlot(params);
                    cost = unit.calculateTimeBasedCost(actualUsage, timeSlot);
                    break;
                case COMPOUND:
                    cost = calculateCompoundCost(unit, actualUsage, params);
                    break;
                case STANDARD:
                default:
                    cost = unit.calculateCost(actualUsage);
                    break;
            }

            // 应用特殊调整
            cost = applyAdjustments(cost, params);

            return cost.setScale(COST_PRECISION, RoundingMode.HALF_UP);

        } catch (Exception e) {
            throw new IllegalArgumentException("计量计费计算失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected boolean doValidateParameters(Map<String, Object> params) {
        // 验证必填参数
        if (!hasRequiredParams(params, "meteringUnit", "currentUsage", "accountId")) {
            return false;
        }

        // 验证计量单位
        String unitCode = getStringFromParams(params, "meteringUnit");
        if (!MeteringUnitEnum.isValidCode(unitCode)) {
            return false;
        }

        // 验证用量数据
        BigDecimal currentUsage = getBigDecimalFromParams(params, "currentUsage");
        BigDecimal previousUsage = getBigDecimalFromParams(params, "previousUsage");

        if (currentUsage == null || currentUsage.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        if (previousUsage != null && previousUsage.compareTo(currentUsage) > 0) {
            // 历史读数大于当前读数，需要重置说明
            return hasRequiredParams(params, "resetReason");
        }

        // 验证计费策略
        String strategyCode = getStringFromParams(params, "billingStrategy");
        if (strategyCode != null) {
            try {
                BillingStrategy.valueOf(strategyCode);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        // 验证时段（如果使用时段计费）
        if ("TIME_BASED".equals(strategyCode) || "COMPOUND".equals(strategyCode)) {
            String timeSlot = getStringFromParams(params, "timeSlot");
            if (timeSlot != null && !isValidTimeSlot(timeSlot)) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected boolean doIsAllowed(Map<String, Object> params) {
        MeteringUnitEnum unit = getMeteringUnit(params);
        BigDecimal actualUsage = getActualUsage(params);

        // 检查用量限制
        if (!unit.isValidUsage(actualUsage)) {
            return false;
        }

        // 检查特殊限制
        String specialLimitType = getStringFromParams(params, "specialLimitType");
        if (specialLimitType != null) {
            return checkSpecialLimit(actualUsage, specialLimitType, unit);
        }

        // 检查设备状态（如果有）
        String deviceId = getStringFromParams(params, "deviceId");
        if (deviceId != null) {
            return checkDeviceStatus(deviceId);
        }

        return true;
    }

    @Override
    protected Map<String, Object> doPreProcess(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();

        // 预处理计量单位信息
        String unitCode = getStringFromParams(params, "meteringUnit");
        MeteringUnitEnum unit = MeteringUnitEnum.fromCode(unitCode);
        if (unit != null) {
            result.put("unitInfo", unit.getUnitInfo());
            result.put("baseRate", unit.getBaseRate());
            result.put("rateDisplay", unit.getRateDisplay());
        }

        // 预处理用量数据
        BigDecimal currentUsage = getBigDecimalFromParams(params, "currentUsage");
        BigDecimal previousUsage = getBigDecimalFromParams(params, "previousUsage");

        if (currentUsage != null && previousUsage != null) {
            BigDecimal actualUsage = currentUsage.subtract(previousUsage);
            result.put("actualUsage", actualUsage);
            result.put("usageDisplay", unit != null ? unit.formatUsage(actualUsage) : actualUsage.toString());

            // 检查用量异常
            BigDecimal avgUsage = getBigDecimalFromParams(params, "averageUsage");
            if (avgUsage != null && avgUsage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal variance = actualUsage.subtract(avgUsage).divide(avgUsage, 4, RoundingMode.HALF_UP);
                result.put("usageVariance", variance);
                result.put("isAbnormalUsage", variance.abs().compareTo(USAGE_VARIANCE_THRESHOLD) > 0);
            }
        }

        // 预处理历史读数
        List<Map<String, Object>> historyReadings = getListFromParams(params, "historyReadings");
        if (historyReadings != null && !historyReadings.isEmpty()) {
            result.put("readingsCount", historyReadings.size());
            result.put("hasEnoughHistory", historyReadings.size() >= 3);

            // 计算用量趋势
            result.put("usageTrend", calculateUsageTrend(historyReadings));
        }

        // 预处理时段信息
        String timeSlot = getStringFromParams(params, "timeSlot");
        if (timeSlot != null) {
            result.put("timeSlotDisplay", getTimeSlotDisplayName(timeSlot));
            result.put("isPeakHour", "peak".equals(timeSlot));
        }

        return result;
    }

    @Override
    protected Map<String, Object> doPostProcess(Map<String, Object> params, Map<String, Object> result) {
        Map<String, Object> postResult = new HashMap<>();

        // 获取计算结果
        BigDecimal actualUsage = (BigDecimal) result.get("actualUsage");
        BigDecimal finalCost = (BigDecimal) result.get("amount");

        if (actualUsage != null && finalCost != null) {
            // 计算单价
            BigDecimal unitPrice = actualUsage.compareTo(BigDecimal.ZERO) > 0
                ? finalCost.divide(actualUsage, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            postResult.put("actualUsage", actualUsage);
            postResult.put("unitPrice", unitPrice.setScale(4, RoundingMode.HALF_UP));
            postResult.put("totalCost", finalCost.setScale(2, RoundingMode.HALF_UP));
        }

        // 计费详情
        String unitCode = getStringFromParams(params, "meteringUnit");
        MeteringUnitEnum unit = MeteringUnitEnum.fromCode(unitCode);
        if (unit != null) {
            postResult.put("unitDisplay", unit.getUnit());
            postResult.put("category", unit.getCategory());

            // 基础费用对比
            BigDecimal baseCost = unit.calculateCost(actualUsage);
            if (finalCost.compareTo(baseCost) != 0) {
                BigDecimal savings = baseCost.subtract(finalCost);
                postResult.put("baseCost", baseCost);
                postResult.put("actualSavings", savings);
                postResult.put("savingsPercentage",
                    baseCost.compareTo(BigDecimal.ZERO) > 0
                        ? savings.divide(baseCost, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                        : BigDecimal.ZERO);
            }
        }

        // 计费策略信息
        BillingStrategy strategy = getBillingStrategy(params);
        postResult.put("billingStrategy", strategy.getName());
        postResult.put("billingDescription", strategy.getDescription());

        // 时段信息（如果有时段计费）
        String timeSlot = getStringFromParams(params, "timeSlot");
        if (timeSlot != null) {
            postResult.put("timeSlot", timeSlot);
            postResult.put("timeSlotDisplay", getTimeSlotDisplayName(timeSlot));
        }

        // 异常用量告警
        Boolean isAbnormalUsage = (Boolean) result.get("isAbnormalUsage");
        if (Boolean.TRUE.equals(isAbnormalUsage)) {
            postResult.put("abnormalUsageAlert", true);
            BigDecimal variance = (BigDecimal) result.get("usageVariance");
            postResult.put("variancePercentage", variance.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + "%");
        }

        return postResult;
    }

    @Override
    protected BigDecimal getMinAmount() {
        return new BigDecimal("0.01"); // 最小1分钱
    }

    @Override
    protected BigDecimal getMaxAmount() {
        return new BigDecimal("99999.99"); // 最大10万元
    }

    @Override
    protected String[] getSupportedFields() {
        return new String[]{
            "meteringUnit", "currentUsage", "previousUsage", "averageUsage",
            "billingStrategy", "timeSlot", "deviceId", "resetReason",
            "specialLimitType", "historyReadings", "notes"
        };
    }

    /**
     * 获取计量单位
     */
    private MeteringUnitEnum getMeteringUnit(Map<String, Object> params) {
        String unitCode = getStringFromParams(params, "meteringUnit");
        MeteringUnitEnum unit = MeteringUnitEnum.fromCode(unitCode);
        if (unit == null) {
            throw new IllegalArgumentException("无效的计量单位: " + unitCode);
        }
        return unit;
    }

    /**
     * 获取当前用量
     */
    private BigDecimal getCurrentUsage(Map<String, Object> params) {
        BigDecimal usage = getBigDecimalFromParams(params, "currentUsage");
        if (usage == null) {
            throw new IllegalArgumentException("当前用量不能为空");
        }
        return usage;
    }

    /**
     * 获取上次用量
     */
    private BigDecimal getPreviousUsage(Map<String, Object> params) {
        BigDecimal previousUsage = getBigDecimalFromParams(params, "previousUsage");
        return previousUsage != null ? previousUsage : BigDecimal.ZERO;
    }

    /**
     * 获取计费策略
     */
    private BillingStrategy getBillingStrategy(Map<String, Object> params) {
        String strategyCode = getStringFromParams(params, "billingStrategy");
        if (strategyCode != null) {
            try {
                return BillingStrategy.valueOf(strategyCode);
            } catch (IllegalArgumentException e) {
                // 默认使用标准计费
                return BillingStrategy.STANDARD;
            }
        }
        return BillingStrategy.STANDARD;
    }

    /**
     * 获取时段
     */
    private String getTimeSlot(Map<String, Object> params) {
        String timeSlot = getStringFromParams(params, "timeSlot");
        return timeSlot != null ? timeSlot : "flat";
    }

    /**
     * 计算实际用量
     */
    private BigDecimal calculateActualUsage(BigDecimal currentUsage, BigDecimal previousUsage, MeteringUnitEnum unit) {
        // 如果有重置原因（如换表、归零等）
        if (previousUsage.compareTo(currentUsage) > 0) {
            return currentUsage; // 从零开始计算
        }
        return currentUsage.subtract(previousUsage);
    }

    /**
     * 复合计费（阶梯+时段）
     */
    private BigDecimal calculateCompoundCost(MeteringUnitEnum unit, BigDecimal usage, Map<String, Object> params) {
        String timeSlot = getTimeSlot(params);

        // 先计算阶梯费用
        BigDecimal tieredCost = unit.calculateTieredCost(usage);

        // 再应用时段调整
        BigDecimal timeAdjustment = getTimeSlotAdjustment(timeSlot);

        return tieredCost.multiply(timeAdjustment).setScale(COST_PRECISION, RoundingMode.HALF_UP);
    }

    /**
     * 应用特殊调整
     */
    private BigDecimal applyAdjustments(BigDecimal cost, Map<String, Object> params) {
        BigDecimal adjustment = getBigDecimalFromParams(params, "adjustmentAmount");
        if (adjustment != null) {
            cost = cost.add(adjustment);
        }

        // 确保费用不为负数
        return cost.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : cost;
    }

    /**
     * 检查特殊限制
     */
    private boolean checkSpecialLimit(BigDecimal usage, String limitType, MeteringUnitEnum unit) {
        switch (limitType) {
            case "DAILY_LIMIT":
                return usage.compareTo(new BigDecimal("100")) <= 0;
            case "MONTHLY_LIMIT":
                return usage.compareTo(new BigDecimal("3000")) <= 0;
            case "LOW_USAGE":
                return usage.compareTo(new BigDecimal("1")) <= 0;
            case "HIGH_USAGE":
                return usage.compareTo(new BigDecimal("1000")) <= 0;
            default:
                return true;
        }
    }

    /**
     * 检查设备状态
     */
    private boolean checkDeviceStatus(String deviceId) {
        // TODO: 实现设备状态检查逻辑
        // 这里可以调用设备管理服务检查设备是否在线、是否正常等
        return true;
    }

    /**
     * 验证时段是否有效
     */
    private boolean isValidTimeSlot(String timeSlot) {
        return Arrays.asList("peak", "valley", "flat", "off_peak").contains(timeSlot);
    }

    /**
     * 获取时段显示名称
     */
    private String getTimeSlotDisplayName(String timeSlot) {
        switch (timeSlot) {
            case "peak": return "峰时段";
            case "valley": return "谷时段";
            case "flat": return "平时段";
            case "off_peak": return "低谷时段";
            default: return "未知时段";
        }
    }

    /**
     * 获取时段调整系数
     */
    private BigDecimal getTimeSlotAdjustment(String timeSlot) {
        switch (timeSlot) {
            case "peak": return new BigDecimal("1.5");
            case "valley": return new BigDecimal("0.7");
            case "off_peak": return new BigDecimal("0.8");
            case "flat":
            default: return BigDecimal.ONE;
        }
    }

    /**
     * 计算用量趋势
     */
    private String calculateUsageTrend(List<Map<String, Object>> historyReadings) {
        if (historyReadings.size() < 2) {
            return "insufficient_data";
        }

        // 简化实现：比较最近两次读数
        Map<String, Object> latest = historyReadings.get(historyReadings.size() - 1);
        Map<String, Object> previous = historyReadings.get(historyReadings.size() - 2);

        BigDecimal latestUsage = new BigDecimal(latest.get("usage").toString());
        BigDecimal previousUsage = new BigDecimal(previous.get("usage").toString());

        if (latestUsage.compareTo(previousUsage) > 0) {
            return "increasing";
        } else if (latestUsage.compareTo(previousUsage) < 0) {
            return "decreasing";
        } else {
            return "stable";
        }
    }

    /**
     * 获取实际用量（从预处理结果中获取）
     */
    private BigDecimal getActualUsage(Map<String, Object> params) {
        BigDecimal currentUsage = getCurrentUsage(params);
        BigDecimal previousUsage = getPreviousUsage(params);
        String unitCode = getStringFromParams(params, "meteringUnit");
        MeteringUnitEnum unit = MeteringUnitEnum.fromCode(unitCode);
        return calculateActualUsage(currentUsage, previousUsage, unit);
    }

    /**
     * 获取所有支持的计量单位
     */
    public static List<String> getSupportedMeteringUnits() {
        return MeteringUnitEnum.getAllCodes();
    }

    /**
     * 获取所有计费策略
     */
    public static List<String> getSupportedBillingStrategies() {
        return Arrays.asList(BillingStrategy.values())
                .stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查计量单位是否需要历史读数
     */
    public static boolean requiresHistoryReadings(String unitCode) {
        MeteringUnitEnum unit = MeteringUnitEnum.fromCode(unitCode);
        return unit != null && unit.requiresHistoricalReadings();
    }
}