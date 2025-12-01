package net.lab1024.sa.admin.module.consume.domain.pattern;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 位置模式
 * 严格遵循repowiki规范：数据传输对象，包含用户消费行为的位置模式特征
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPattern {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式ID
     */
    private Long patternId;

    /**
     * 位置模式类型：FIXED-固定位置，VARIED-多样化，OFFICE_CAMPUS-办公园区，RESIDENTIAL_AREA-居住区域
     */
    private String locationPatternType;

    /**
     * 主要消费位置1 ID
     */
    private Long primaryLocation1Id;

    /**
     * 主要消费位置1 名称
     */
    private String primaryLocation1Name;

    /**
     * 主要消费位置1 访问频率（0-100）
     */
    private BigDecimal primaryLocation1Frequency;

    /**
     * 主要消费位置2 ID
     */
    private Long primaryLocation2Id;

    /**
     * 主要消费位置2 名称
     */
    private String primaryLocation2Name;

    /**
     * 主要消费位置2 访问频率（0-100）
     */
    private BigDecimal primaryLocation2Frequency;

    /**
     * 主要消费位置3 ID
     */
    private Long primaryLocation3Id;

    /**
     * 主要消费位置3 名称
     */
    private String primaryLocation3Name;

    /**
     * 主要消费位置3 访问频率（0-100）
     */
    private BigDecimal primaryLocation3Frequency;

    /**
     * 平均移动距离（米）
     */
    private BigDecimal averageMovementDistance;

    /**
     * 最大移动距离（米）
     */
    private BigDecimal maxMovementDistance;

    /**
     * 位置稳定性评分（0-100）
     */
    private BigDecimal stabilityScore;

    /**
     * 位置可预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 独特位置数量
     */
    private Integer uniqueLocationCount;

    /**
     * 数据样本量
     */
    private Integer sampleSize;

    /**
     * 分析窗口期（天）
     */
    private Integer analysisWindow;

    /**
     * 模式创建时间
     */
    private LocalDateTime createTime;

    /**
     * 模式更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 模式有效期
     */
    private LocalDateTime validUntil;

    /**
     * 是否为当前有效模式
     */
    private Boolean isActive;

    /**
     * 位置分布统计
     */
    private Map<Long, Integer> locationDistribution;

    /**
     * 位置类别分布
     */
    private Map<String, BigDecimal> locationTypeDistribution;

    /**
     * 区域活动热力图
     */
    private Map<String, BigDecimal> activityHeatmap;

    /**
     * 工作日vs周末位置差异
     */
    private Map<String, Object> weekdayWeekendDifference;

    /**
     * 季节性位置变化
     */
    private Map<String, Object> seasonalLocationChanges;

    /**
     * 新位置探索趋势
     */
    private List<BigDecimal> newLocationExplorationTrend;

    /**
     * 位置忠诚度评分（0-100）
     */
    private BigDecimal loyaltyScore;

    /**
     * 探索性评分（0-100）
     */
    private BigDecimal explorationScore;

    /**
     * 模式置信度
     */
    private BigDecimal confidence;

    /**
     * 模式版本
     */
    private String patternVersion;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 创建固定位置模式
     */
    public static LocationPattern fixedLocationPattern(Long userId, Long locationId, String locationName,
                                                      BigDecimal frequency) {
        return LocationPattern.builder()
                .userId(userId)
                .locationPatternType("FIXED")
                .primaryLocation1Id(locationId)
                .primaryLocation1Name(locationName)
                .primaryLocation1Frequency(frequency)
                .uniqueLocationCount(1)
                .stabilityScore(new BigDecimal("95.0"))
                .predictabilityScore(new BigDecimal("90.0"))
                .loyaltyScore(new BigDecimal("90.0"))
                .explorationScore(new BigDecimal("10.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("92.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建办公园区模式
     */
    public static LocationPattern officeCampusPattern(Long userId, List<Map<String, Object>> campusLocations) {
        LocationPatternBuilder builder = LocationPattern.builder()
                .userId(userId)
                .locationPatternType("OFFICE_CAMPUS")
                .stabilityScore(new BigDecimal("85.0"))
                .predictabilityScore(new BigDecimal("80.0"))
                .loyaltyScore(new BigDecimal("75.0"))
                .explorationScore(new BigDecimal("20.0"))
                .uniqueLocationCount(campusLocations.size())
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("83.0"))
                .patternVersion("1.0");

        // 设置主要位置
        if (campusLocations.size() >= 1) {
            Map<String, Object> loc1 = campusLocations.get(0);
            builder.primaryLocation1Id((Long) loc1.get("id"));
            builder.primaryLocation1Name((String) loc1.get("name"));
            builder.primaryLocation1Frequency((BigDecimal) loc1.get("frequency"));
        }

        if (campusLocations.size() >= 2) {
            Map<String, Object> loc2 = campusLocations.get(1);
            builder.primaryLocation2Id((Long) loc2.get("id"));
            builder.primaryLocation2Name((String) loc2.get("name"));
            builder.primaryLocation2Frequency((BigDecimal) loc2.get("frequency"));
        }

        if (campusLocations.size() >= 3) {
            Map<String, Object> loc3 = campusLocations.get(2);
            builder.primaryLocation3Id((Long) loc3.get("id"));
            builder.primaryLocation3Name((String) loc3.get("name"));
            builder.primaryLocation3Frequency((BigDecimal) loc3.get("frequency"));
        }

        return builder.build();
    }

    /**
     * 创建多样化位置模式
     */
    public static LocationPattern variedLocationPattern(Long userId, Integer uniqueCount,
                                                       BigDecimal avgDistance, BigDecimal maxDistance) {
        return LocationPattern.builder()
                .userId(userId)
                .locationPatternType("VARIED")
                .uniqueLocationCount(uniqueCount)
                .averageMovementDistance(avgDistance)
                .maxMovementDistance(maxDistance)
                .stabilityScore(new BigDecimal("40.0"))
                .predictabilityScore(new BigDecimal("35.0"))
                .loyaltyScore(new BigDecimal("30.0"))
                .explorationScore(new BigDecimal("80.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(14))
                .confidence(new BigDecimal("65.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建居住区域模式
     */
    public static LocationPattern residentialAreaPattern(Long userId, String areaName, BigDecimal avgDistance) {
        return LocationPattern.builder()
                .userId(userId)
                .locationPatternType("RESIDENTIAL_AREA")
                .primaryLocation1Name(areaName)
                .averageMovementDistance(avgDistance)
                .uniqueLocationCount(5)
                .stabilityScore(new BigDecimal("70.0"))
                .predictabilityScore(new BigDecimal("65.0"))
                .loyaltyScore(new BigDecimal("60.0"))
                .explorationScore(new BigDecimal("40.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(21))
                .confidence(new BigDecimal("75.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 检查模式是否有效
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(isActive) && validUntil != null && validUntil.isAfter(LocalDateTime.now());
    }

    /**
     * 检查是否为稳定模式
     */
    public boolean isStable() {
        return stabilityScore != null && stabilityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查是否可预测
     */
    public boolean isPredictable() {
        return predictabilityScore != null && predictabilityScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查位置是否为主要位置
     */
    public boolean isPrimaryLocation(Long locationId) {
        return locationId.equals(primaryLocation1Id) ||
               locationId.equals(primaryLocation2Id) ||
               locationId.equals(primaryLocation3Id);
    }

    /**
     * 获取位置模式类型描述
     */
    public String getLocationPatternTypeDescription() {
        switch (locationPatternType) {
            case "FIXED":
                return "固定位置";
            case "VARIED":
                return "多样化位置";
            case "OFFICE_CAMPUS":
                return "办公园区";
            case "RESIDENTIAL_AREA":
                return "居住区域";
            default:
                return "未知模式";
        }
    }

    /**
     * 获取最高频率位置
     */
    public String getHighestFrequencyLocation() {
        if (primaryLocation1Frequency == null && primaryLocation2Frequency == null && primaryLocation3Frequency == null) {
            return "无";
        }

        BigDecimal maxFreq = primaryLocation1Frequency != null ? primaryLocation1Frequency : BigDecimal.ZERO;
        String maxLocation = primaryLocation1Name;

        if (primaryLocation2Frequency != null && primaryLocation2Frequency.compareTo(maxFreq) > 0) {
            maxFreq = primaryLocation2Frequency;
            maxLocation = primaryLocation2Name;
        }

        if (primaryLocation3Frequency != null && primaryLocation3Frequency.compareTo(maxFreq) > 0) {
            maxLocation = primaryLocation3Name;
        }

        return maxLocation != null ? maxLocation : "无";
    }

    /**
     * 获取格式化的平均移动距离
     */
    public String getFormattedAverageDistance() {
        if (averageMovementDistance == null) {
            return "0米";
        }
        if (averageMovementDistance.compareTo(new BigDecimal("1000")) < 0) {
            return averageMovementDistance.setScale(0, BigDecimal.ROUND_HALF_UP) + "米";
        } else {
            return averageMovementDistance.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP) + "公里";
        }
    }

    /**
     * 获取格式化的最大移动距离
     */
    public String getFormattedMaxDistance() {
        if (maxMovementDistance == null) {
            return "0米";
        }
        if (maxMovementDistance.compareTo(new BigDecimal("1000")) < 0) {
            return maxMovementDistance.setScale(0, BigDecimal.ROUND_HALF_UP) + "米";
        } else {
            return maxMovementDistance.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP) + "公里";
        }
    }

    /**
     * 获取主要位置描述
     */
    public String getPrimaryLocationsDescription() {
        StringBuilder description = new StringBuilder();

        if (primaryLocation1Name != null) {
            description.append(primaryLocation1Name);
            if (primaryLocation1Frequency != null) {
                description.append("(").append(primaryLocation1Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%)");
            }
        }

        if (primaryLocation2Name != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(primaryLocation2Name);
            if (primaryLocation2Frequency != null) {
                description.append("(").append(primaryLocation2Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%)");
            }
        }

        if (primaryLocation3Name != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(primaryLocation3Name);
            if (primaryLocation3Frequency != null) {
                description.append("(").append(primaryLocation3Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%)");
            }
        }

        return description.length() > 0 ? description.toString() : "无主要位置";
    }

    /**
     * 更新模式
     */
    public void updatePattern() {
        this.updateTime = LocalDateTime.now();
        if (this.validUntil != null) {
            this.validUntil = LocalDateTime.now().plusDays(30);
        }
    }

    /**
     * 获取模式质量评分
     */
    public BigDecimal getPatternQualityScore() {
        if (stabilityScore == null || predictabilityScore == null) {
            return BigDecimal.ZERO;
        }
        return stabilityScore.add(predictabilityScore).divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查是否为位置忠实用户
     */
    public boolean isLocationLoyal() {
        return loyaltyScore != null && loyaltyScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查是否为探索型用户
     */
    public boolean isExploratoryUser() {
        return explorationScore != null && explorationScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 获取位置多样性评分
     */
    public BigDecimal getLocationDiversityScore() {
        if (uniqueLocationCount == null) {
            return BigDecimal.ZERO;
        }

        if (uniqueLocationCount <= 3) {
            return new BigDecimal("20.0");
        } else if (uniqueLocationCount <= 10) {
            return new BigDecimal("60.0");
        } else if (uniqueLocationCount <= 20) {
            return new BigDecimal("80.0");
        } else {
            return new BigDecimal("95.0");
        }
    }
}