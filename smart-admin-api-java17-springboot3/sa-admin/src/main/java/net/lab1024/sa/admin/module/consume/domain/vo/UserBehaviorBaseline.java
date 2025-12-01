/*
 * 用户行为基线
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
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为基线
 * 定义用户的正常行为模式和基线指标
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorBaseline {

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 每小时平均操作次数
     */
    private Double hourlyOperationCount;

    /**
     * 平均操作金额
     */
    private BigDecimal averageAmount;

    /**
     * 最大操作金额
     */
    private BigDecimal maxAmount;

    /**
     * 最小操作金额
     */
    private BigDecimal minAmount;

    /**
     * 最常用设备ID
     */
    private Long mostUsedDeviceId;

    /**
     * 设备使用频率统计
     */
    private Map<Long, Integer> deviceFrequency;

    /**
     * 常用位置IP
     */
    private String mostFrequentLocation;

    /**
     * 位置使用频率统计
     */
    private Map<String, Integer> locationFrequency;

    /**
     * 常用消费时间（小时）
     */
    private Integer peakHour;

    /**
     * 时间分布统计
     */
    private Map<Integer, Integer> timeDistribution;

    /**
     * 常用星期
     */
    private Integer peakDayOfWeek;

    /**
     * 星期分布统计
     */
    private Map<Integer, Integer> dayDistribution;

    /**
     * 基线建立周期（天数）
     */
    private Integer baselinePeriod;

    /**
     * 基线创建时间
     */
    private LocalDateTime createTime;

    /**
     * 基线更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 数据样本数量
     */
    private Integer sampleSize;

    /**
     * 基线置信度（0-100）
     */
    private Double confidence;

    /**
     * 是否为有效基线
     */
    private Boolean isValid;

    /**
     * 基线版本
     */
    private String version;

    /**
     * 扩展属性（JSON格式）
     */
    private String extendProperties;

    /**
     * 检查基线是否有效
     */
    public boolean isEffectiveBaseline() {
        return Boolean.TRUE.equals(isValid) &&
               confidence != null && confidence >= 70 &&
               sampleSize != null && sampleSize >= 10 &&
               baselinePeriod != null && baselinePeriod >= 7;
    }

    /**
     * 检查是否需要更新基线
     */
    public boolean needsUpdate() {
        if (updateTime == null) {
            return true;
        }

        // 如果基线超过30天未更新，建议更新
        return updateTime.isBefore(LocalDateTime.now().minusDays(30));
    }

    /**
     * 获取设备使用率
     */
    public double getDeviceUsageRate(Long deviceId) {
        if (deviceFrequency == null || deviceFrequency.isEmpty()) {
            return 0.0;
        }

        int deviceCount = deviceFrequency.getOrDefault(deviceId, 0);
        int totalCount = deviceFrequency.values().stream().mapToInt(Integer::intValue).sum();

        return totalCount > 0 ? (double) deviceCount / totalCount : 0.0;
    }

    /**
     * 获取位置使用率
     */
    public double getLocationUsageRate(String location) {
        if (locationFrequency == null || locationFrequency.isEmpty()) {
            return 0.0;
        }

        int locationCount = locationFrequency.getOrDefault(location, 0);
        int totalCount = locationFrequency.values().stream().mapToInt(Integer::intValue).sum();

        return totalCount > 0 ? (double) locationCount / totalCount : 0.0;
    }

    /**
     * 检查是否为常用设备
     */
    public boolean isFrequentDevice(Long deviceId) {
        return getDeviceUsageRate(deviceId) >= 0.3; // 使用率超过30%认为是常用设备
    }

    /**
     * 检查是否为常用位置
     */
    public boolean isFrequentLocation(String location) {
        return getLocationUsageRate(location) >= 0.3; // 使用率超过30%认为是常用位置
    }

    /**
     * 检查是否为常用时间
     */
    public boolean isFrequentTime(int hour) {
        if (timeDistribution == null || timeDistribution.isEmpty()) {
            return false;
        }

        int hourCount = timeDistribution.getOrDefault(hour, 0);
        int totalCount = timeDistribution.values().stream().mapToInt(Integer::intValue).sum();

        return totalCount > 0 && (double) hourCount / totalCount >= 0.1; // 使用率超过10%认为是常用时间
    }

    /**
     * 获取基线摘要信息
     */
    public String getBaselineSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("基线周期: ").append(baselinePeriod != null ? baselinePeriod + "天" : "未知");
        summary.append(", 样本数: ").append(sampleSize != null ? sampleSize : "未知");
        summary.append(", 置信度: ").append(confidence != null ? String.format("%.1f%%", confidence) : "未知");

        if (averageAmount != null) {
            summary.append(", 平均金额: ").append(averageAmount);
        }

        if (mostUsedDeviceId != null) {
            summary.append(", 主设备: ").append(mostUsedDeviceId);
        }

        if (mostFrequentLocation != null) {
            summary.append(", 主位置: ").append(mostFrequentLocation);
        }

        return summary.toString();
    }

    /**
     * 创建默认基线
     */
    public static UserBehaviorBaseline createDefault() {
        return UserBehaviorBaseline.builder()
                .hourlyOperationCount(2.0)
                .averageAmount(new BigDecimal("25.00"))
                .maxAmount(new BigDecimal("500.00"))
                .minAmount(new BigDecimal("0.01"))
                .peakHour(12)
                .peakDayOfWeek(5) // 周五
                .baselinePeriod(30)
                .confidence(50.0)
                .isValid(false)
                .version("1.0")
                .build();
    }

    /**
     * 创建高频用户基线
     */
    public static UserBehaviorBaseline createHighFrequencyBaseline() {
        return UserBehaviorBaseline.builder()
                .hourlyOperationCount(5.0)
                .averageAmount(new BigDecimal("50.00"))
                .maxAmount(new BigDecimal("1000.00"))
                .minAmount(new BigDecimal("0.01"))
                .peakHour(12)
                .peakDayOfWeek(5)
                .baselinePeriod(30)
                .confidence(80.0)
                .isValid(true)
                .version("1.0")
                .build();
    }

    /**
     * 创建低频用户基线
     */
    public static UserBehaviorBaseline createLowFrequencyBaseline() {
        return UserBehaviorBaseline.builder()
                .hourlyOperationCount(0.5)
                .averageAmount(new BigDecimal("15.00"))
                .maxAmount(new BigDecimal("200.00"))
                .minAmount(new BigDecimal("0.01"))
                .peakHour(12)
                .peakDayOfWeek(5)
                .baselinePeriod(30)
                .confidence(60.0)
                .isValid(true)
                .version("1.0")
                .build();
    }
}