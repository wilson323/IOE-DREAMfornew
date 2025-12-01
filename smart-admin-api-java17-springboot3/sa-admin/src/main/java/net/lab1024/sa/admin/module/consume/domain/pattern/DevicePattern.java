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
 * 设备模式
 * 严格遵循repowiki规范：数据传输对象，包含用户消费行为的设备模式特征
 *
 * @author SmartAdmin Team
 * @date 2025/11/19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevicePattern {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式ID
     */
    private Long patternId;

    /**
     * 设备模式类型：SINGLE_DEVICE-单一设备，MULTIPLE_DEVICES-多设备，MOBILE_PRIMARY-移动为主，FIXED_PRIMARY-固定为主
     */
    private String devicePatternType;

    /**
     * 主要使用设备1 ID
     */
    private String primaryDevice1Id;

    /**
     * 主要使用设备1 名称
     */
    private String primaryDevice1Name;

    /**
     * 主要使用设备1 类型：POS, KIOSK, MOBILE, CARD_READER
     */
    private String primaryDevice1Type;

    /**
     * 主要使用设备1 使用频率（0-100）
     */
    private BigDecimal primaryDevice1Frequency;

    /**
     * 主要使用设备2 ID
     */
    private String primaryDevice2Id;

    /**
     * 主要使用设备2 名称
     */
    private String primaryDevice2Name;

    /**
     * 主要使用设备2 类型
     */
    private String primaryDevice2Type;

    /**
     * 主要使用设备2 使用频率（0-100）
     */
    private BigDecimal primaryDevice2Frequency;

    /**
     * 主要使用设备3 ID
     */
    private String primaryDevice3Id;

    /**
     * 主要使用设备3 名称
     */
    private String primaryDevice3Name;

    /**
     * 主要使用设备3 类型
     */
    private String primaryDevice3Type;

    /**
     * 主要使用设备3 使用频率（0-100）
     */
    private BigDecimal primaryDevice3Frequency;

    /**
     * 设备多样性指数
     */
    private BigDecimal deviceDiversityIndex;

    /**
     * 设备切换频率（次/天）
     */
    private BigDecimal deviceSwitchFrequency;

    /**
     * 设备忠诚度评分（0-100）
     */
    private BigDecimal loyaltyScore;

    /**
     * 设备探索性评分（0-100）
     */
    private BigDecimal explorationScore;

    /**
     * 移动设备偏好度（0-100）
     */
    private BigDecimal mobilePreference;

    /**
     * 固定设备偏好度（0-100）
     */
    private BigDecimal fixedDevicePreference;

    /**
     * 设备使用稳定性评分（0-100）
     */
    private BigDecimal stabilityScore;

    /**
     * 设备使用可预测性评分（0-100）
     */
    private BigDecimal predictabilityScore;

    /**
     * 独特设备数量
     */
    private Integer uniqueDeviceCount;

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
     * 设备使用分布统计
     */
    private Map<String, Integer> deviceUsageDistribution;

    /**
     * 设备类型分布
     */
    private Map<String, BigDecimal> deviceTypeDistribution;

    /**
     * 设备位置关联
     */
    private Map<String, String> deviceLocationMapping;

    /**
     * 设备时段偏好
     */
    private Map<String, Map<String, BigDecimal>> deviceTimePreference;

    /**
     * 新设备采用趋势
     */
    private List<BigDecimal> newDeviceAdoptionTrend;

    /**
     * 设备更换历史
     */
    private List<Map<String, Object>> deviceChangeHistory;

    /**
     * 设备信任评分
     */
    private Map<String, BigDecimal> deviceTrustScores;

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
     * 创建单一设备模式
     */
    public static DevicePattern singleDevicePattern(Long userId, String deviceId, String deviceName,
                                                    String deviceType, BigDecimal frequency) {
        return DevicePattern.builder()
                .userId(userId)
                .devicePatternType("SINGLE_DEVICE")
                .primaryDevice1Id(deviceId)
                .primaryDevice1Name(deviceName)
                .primaryDevice1Type(deviceType)
                .primaryDevice1Frequency(frequency)
                .uniqueDeviceCount(1)
                .deviceDiversityIndex(new BigDecimal("0.0"))
                .deviceSwitchFrequency(BigDecimal.ZERO)
                .loyaltyScore(new BigDecimal("95.0"))
                .explorationScore(new BigDecimal("5.0"))
                .stabilityScore(new BigDecimal("98.0"))
                .predictabilityScore(new BigDecimal("95.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("96.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建移动为主模式
     */
    public static DevicePattern mobilePrimaryPattern(Long userId, String primaryMobileId, String primaryMobileName) {
        return DevicePattern.builder()
                .userId(userId)
                .devicePatternType("MOBILE_PRIMARY")
                .primaryDevice1Id(primaryMobileId)
                .primaryDevice1Name(primaryMobileName)
                .primaryDevice1Type("MOBILE")
                .primaryDevice1Frequency(new BigDecimal("70.0"))
                .mobilePreference(new BigDecimal("85.0"))
                .fixedDevicePreference(new BigDecimal("15.0"))
                .deviceDiversityIndex(new BigDecimal("40.0"))
                .loyaltyScore(new BigDecimal("70.0"))
                .explorationScore(new BigDecimal("60.0"))
                .stabilityScore(new BigDecimal("75.0"))
                .predictabilityScore(new BigDecimal("70.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(21))
                .confidence(new BigDecimal("80.0"))
                .patternVersion("1.0")
                .build();
    }

    /**
     * 创建多设备模式
     */
    public static DevicePattern multipleDevicesPattern(Long userId, List<Map<String, Object>> devices) {
        DevicePatternBuilder builder = DevicePattern.builder()
                .userId(userId)
                .devicePatternType("MULTIPLE_DEVICES")
                .uniqueDeviceCount(devices.size())
                .deviceDiversityIndex(new BigDecimal("75.0"))
                .deviceSwitchFrequency(new BigDecimal("2.5"))
                .loyaltyScore(new BigDecimal("50.0"))
                .explorationScore(new BigDecimal("75.0"))
                .stabilityScore(new BigDecimal("60.0"))
                .predictabilityScore(new BigDecimal("55.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(14))
                .confidence(new BigDecimal("70.0"))
                .patternVersion("1.0");

        // 设置主要设备
        if (devices.size() >= 1) {
            Map<String, Object> device1 = devices.get(0);
            builder.primaryDevice1Id((String) device1.get("id"));
            builder.primaryDevice1Name((String) device1.get("name"));
            builder.primaryDevice1Type((String) device1.get("type"));
            builder.primaryDevice1Frequency((BigDecimal) device1.get("frequency"));
        }

        if (devices.size() >= 2) {
            Map<String, Object> device2 = devices.get(1);
            builder.primaryDevice2Id((String) device2.get("id"));
            builder.primaryDevice2Name((String) device2.get("name"));
            builder.primaryDevice2Type((String) device2.get("type"));
            builder.primaryDevice2Frequency((BigDecimal) device2.get("frequency"));
        }

        if (devices.size() >= 3) {
            Map<String, Object> device3 = devices.get(3);
            builder.primaryDevice3Id((String) device3.get("id"));
            builder.primaryDevice3Name((String) device3.get("name"));
            builder.primaryDevice3Type((String) device3.get("type"));
            builder.primaryDevice3Frequency((BigDecimal) device3.get("frequency"));
        }

        return builder.build();
    }

    /**
     * 创建固定设备为主模式
     */
    public static DevicePattern fixedDevicePrimaryPattern(Long userId, String primaryFixedId, String primaryFixedName) {
        return DevicePattern.builder()
                .userId(userId)
                .devicePatternType("FIXED_PRIMARY")
                .primaryDevice1Id(primaryFixedId)
                .primaryDevice1Name(primaryFixedName)
                .primaryDevice1Type("POS")
                .primaryDevice1Frequency(new BigDecimal("80.0"))
                .mobilePreference(new BigDecimal("20.0"))
                .fixedDevicePreference(new BigDecimal("80.0"))
                .deviceDiversityIndex(new BigDecimal("25.0"))
                .loyaltyScore(new BigDecimal("85.0"))
                .explorationScore(new BigDecimal("25.0"))
                .stabilityScore(new BigDecimal("90.0"))
                .predictabilityScore(new BigDecimal("85.0"))
                .isActive(true)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(30))
                .confidence(new BigDecimal("88.0"))
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
     * 检查设备是否为主要设备
     */
    public boolean isPrimaryDevice(String deviceId) {
        return deviceId.equals(primaryDevice1Id) ||
               deviceId.equals(primaryDevice2Id) ||
               deviceId.equals(primaryDevice3Id);
    }

    /**
     * 获取设备模式类型描述
     */
    public String getDevicePatternTypeDescription() {
        switch (devicePatternType) {
            case "SINGLE_DEVICE":
                return "单一设备";
            case "MULTIPLE_DEVICES":
                return "多设备";
            case "MOBILE_PRIMARY":
                return "移动为主";
            case "FIXED_PRIMARY":
                return "固定为主";
            default:
                return "未知模式";
        }
    }

    /**
     * 获取最高频率设备
     */
    public String getHighestFrequencyDevice() {
        if (primaryDevice1Frequency == null && primaryDevice2Frequency == null && primaryDevice3Frequency == null) {
            return "无";
        }

        BigDecimal maxFreq = primaryDevice1Frequency != null ? primaryDevice1Frequency : BigDecimal.ZERO;
        String maxDevice = primaryDevice1Name;

        if (primaryDevice2Frequency != null && primaryDevice2Frequency.compareTo(maxFreq) > 0) {
            maxFreq = primaryDevice2Frequency;
            maxDevice = primaryDevice2Name;
        }

        if (primaryDevice3Frequency != null && primaryDevice3Frequency.compareTo(maxFreq) > 0) {
            maxDevice = primaryDevice3Name;
        }

        return maxDevice != null ? maxDevice : "无";
    }

    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDescription(String deviceType) {
        switch (deviceType) {
            case "POS":
                return "收银机";
            case "KIOSK":
                return "自助终端";
            case "MOBILE":
                return "移动设备";
            case "CARD_READER":
                return "读卡器";
            default:
                return "未知设备";
        }
    }

    /**
     * 获取主要设备描述
     */
    public String getPrimaryDevicesDescription() {
        StringBuilder description = new StringBuilder();

        if (primaryDevice1Name != null) {
            description.append(primaryDevice1Name);
            if (primaryDevice1Type != null) {
                description.append("(").append(getDeviceTypeDescription(primaryDevice1Type)).append(")");
            }
            if (primaryDevice1Frequency != null) {
                description.append("[").append(primaryDevice1Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%]");
            }
        }

        if (primaryDevice2Name != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(primaryDevice2Name);
            if (primaryDevice2Type != null) {
                description.append("(").append(getDeviceTypeDescription(primaryDevice2Type)).append(")");
            }
            if (primaryDevice2Frequency != null) {
                description.append("[").append(primaryDevice2Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%]");
            }
        }

        if (primaryDevice3Name != null) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(primaryDevice3Name);
            if (primaryDevice3Type != null) {
                description.append("(").append(getDeviceTypeDescription(primaryDevice3Type)).append(")");
            }
            if (primaryDevice3Frequency != null) {
                description.append("[").append(primaryDevice3Frequency.setScale(1, BigDecimal.ROUND_HALF_UP)).append("%]");
            }
        }

        return description.length() > 0 ? description.toString() : "无主要设备";
    }

    /**
     * 获取设备偏好描述
     */
    public String getDevicePreferenceDescription() {
        if (mobilePreference == null || fixedDevicePreference == null) {
            return "无偏好";
        }

        if (mobilePreference.compareTo(fixedDevicePreference) > 10) {
            return "偏好移动设备";
        } else if (fixedDevicePreference.compareTo(mobilePreference) > 10) {
            return "偏好固定设备";
        } else {
            return "无明显偏好";
        }
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
     * 检查是否为设备忠实用户
     */
    public boolean isDeviceLoyal() {
        return loyaltyScore != null && loyaltyScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 检查是否为设备探索型用户
     */
    public boolean isDeviceExploratoryUser() {
        return explorationScore != null && explorationScore.compareTo(new BigDecimal("70")) >= 0;
    }

    /**
     * 获取设备使用多样性评分
     */
    public BigDecimal getDeviceUsageDiversityScore() {
        if (uniqueDeviceCount == null) {
            return BigDecimal.ZERO;
        }

        if (uniqueDeviceCount <= 2) {
            return new BigDecimal("20.0");
        } else if (uniqueDeviceCount <= 5) {
            return new BigDecimal("50.0");
        } else if (uniqueDeviceCount <= 10) {
            return new BigDecimal("75.0");
        } else {
            return new BigDecimal("90.0");
        }
    }

    /**
     * 检查是否以移动设备为主
     */
    public boolean isMobilePrimary() {
        return "MOBILE_PRIMARY".equals(devicePatternType) ||
               (mobilePreference != null && mobilePreference.compareTo(new BigDecimal("60")) >= 0);
    }
}